package com.renatoback.module2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renatoback.core.Action;
import com.renatoback.core.ActionResult;
import com.renatoback.core.LLM;
import com.renatoback.core.Message;
import com.renatoback.core.Tool;
import com.renatoback.core.Message.Roles;
import com.renatoback.core.Prompt;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AgentLoopFunctionCallingJavaDoc {

    private final Map<String, Function<Map<String, Object>, Object>> toolFunctions = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Tool> tools;
    private final int maxIterations;
    private final String root;

    public AgentLoopFunctionCallingJavaDoc(int maxIterations, String root) {
        this.tools = new ArrayList<>();
        this.maxIterations = maxIterations;
        this.root = root;
    }

    /**
     * Register a tool with the agent.
     *
     * @param toolJson JSON defining the tool
     * @param function The function to execute when the tool is called
     */
    public void registerTool(String toolJson, Function<Map<String, Object>, Object> function) {
        Tool tool = Tool.fromJson(toolJson);
        tools.add(tool);
        toolFunctions.put(tool.toolName(), function);
    }

    /**
     * Parse the LLM response into an Action object.
     *
     * @param response The LLM response string
     * @return The parsed Action object
     * @throws Exception If the response cannot be parsed
     */
    
    @SuppressWarnings("unchecked")
    private Action parseAction(String response) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        String toolName = (String) responseMap.get("tool");
        Map<String, Object> toolArgs = (Map<String, Object>) responseMap.get("args");
        return new Action(toolName, toolArgs);
    }

    /**
     * Execute a single action and return the result.
     *
     * @param action The action to execute
     * @return The result of the action
     */
    private ActionResult executeAction(Action action) {
        System.out.println("Executing: " + action.toolName() + " with args " + action.args());

        try {
            if (toolFunctions.containsKey(action.toolName())) {
                Object result = toolFunctions.get(action.toolName()).apply(action.args());

                if (result instanceof String && ((String)result).startsWith("Error:")) {
                    return new ActionResult(null, (String)result);
                }

                return new ActionResult(result, null);
            }
            
            return new ActionResult(null, "Unknown tool: " + action.toolName());
        } catch (Exception e) {
            return new ActionResult(null,
                    "Error executing " + action.toolName() + ": " + e.getMessage());
        }
    }

    /**
     * Run the agent loop with the given user request.
     *
     * @param userRequest The initial user request.
     * @param llm The LLM instance to use.
     * @return The final state of the memory list.
     */
    public List<Message> run(String userRequest, LLM<Prompt> llm) {
        // Initialize memory with system message and user request
        List<Message> memory = new ArrayList<>();
        memory.add(Message.of(Roles.SYSTEM,
                """
                You are an AI agent that can perform tasks by using available tools.
                
                If a user asks about files, documents, or content, first list the files before reading them.
                
                When you are done, terminate the conversation by using the "terminate" tool and I will provide the results to the user.
                """
        ));
        memory.add(Message.of(Roles.USER, userRequest));

        int iterations = 0;

        // The Agent Loop
        while (iterations < maxIterations) {
            // Create LLM prompt with current memory and tools
            Prompt prompt = new Prompt(memory, tools);

            // Get LLM response
            String response = llm.generateResponse(prompt);

            try {
                // Parse the response to extract the action
                Action action = parseAction(response);

                if ("terminate".equals(action.toolName())) {
                    System.out.println("Termination message: " + action.args().get("message"));
                    memory.add(Message.of(Roles.ASSISTANT, response));
                    break;
                } else {
                    // Execute the action and get result
                    ActionResult result = executeAction(action);
                    System.out.println("Result: " + result.toMap());

                    // Update memory
                    memory.add(Message.of(Roles.ASSISTANT, response));
                    memory.add(Message.of(Roles.USER, objectMapper.writeValueAsString(result.toMap())));
                }
            } catch (Exception e) {
                // Handle standard text response or parsing error
                System.out.println("Response: " + response);
                memory.add(Message.of(Roles.ASSISTANT, response));
                break;
            }

            iterations++;
        }

        return memory;
    }

    public static void main(String[] args) {
        try {
            String root = args.length == 0 ? null : args[0];

            checkSrcFolder(root);

            // Get user input
            System.out.print("What would you like me to do? ");
            Scanner scanner = new Scanner(System.in);
            String userTask = scanner.nextLine();
            scanner.close();

            // Define tools using our Tool class with JSON
            String listJavaFilesJson = """
            {
                "toolName": "listJavaFiles",
                "description": "Returns a list of Java files in the src/ directory.",
                "parameters": {
                    "type": "object",
                    "properties": {},
                    "required": []
                }
            }
            """;

            String readJavaFileJson = """
            {
                "toolName": "readJavaFile",
                "description": "Reads the content of a Java file from the src/ directory.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "fileName": { 
                            "type": "string",
                            "description": "Name of the Java file to read (must end with .java)"
                        }
                    },
                    "required": ["fileName"]
                }
            }
            """;

            String terminateJson = """
            {
              "toolName": "terminate",
              "description": "Terminates the conversation. No further actions or interactions are possible after this. Prints the provided message for the user.",
              "parameters": {
                "type": "object",
                "properties": {
                  "message": { 
                    "type": "string",
                    "description": "The final message to display to the user"
                  }
                },
                "required": ["message"]
              }
            }
            """;

            // Create agent instance
            AgentLoopFunctionCallingJavaDoc agent = new AgentLoopFunctionCallingJavaDoc(10, root);

            // Register tools
            agent.registerTool(listJavaFilesJson, targs -> agent.listJavaFiles());
            agent.registerTool(readJavaFileJson, targs -> agent.readJavaFile((String) targs.get("fileName")));
            agent.registerTool(terminateJson, targs -> {
                System.out.println("Termination message: " + targs.get("message"));
                return "Agent terminated";
            });

            // Initialize the LLM
            LLM<Prompt> llm = LLM.promptFromEnv();

            // Run the agent
            List<Message> finalMemory = agent.run(userTask, llm);

            System.out.println("Agent completed with " + finalMemory.size() + " memory entries.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkSrcFolder(String root) {
        File srcDir = new File(root, "src");
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            throw new RuntimeException("Could not find 'src' folder in the current working directory. Quitting application.");
        }
        System.out.println("Using root directory: " + root);
	}

	// Tool implementation methods...
    private Object listJavaFiles() {
        try {
            Path dir = Paths.get(root, "src");
            return Files.walk(dir)
                .filter(p -> p.toString().endsWith(".java"))
                .map(p -> dir.relativize(p).toString())
                .collect(Collectors.toList());
        } catch (Exception e) {
            return "Error listing Java files: " + e.getMessage();
        }
    }

    private String readJavaFile(String fileName) {
        // Register the tool implementation
        Path filePath = Paths.get(root, "src", fileName);
        
        if (!fileName.endsWith(".java")) {
            // Return just the result - ActionResult is created in executeAction method
            return "Error: Invalid file type. Only Java files can be read. Call the listJavaFiles function to get a list of valid files.";
        }
        
        if (!Files.exists(filePath)) {
            return "Error: File '" + fileName + "' does not exist in the src/ directory. Call the listJavaFiles function to get a list of available files.";
        }
        
        try {
            return new String(Files.readAllBytes(filePath));
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }
}