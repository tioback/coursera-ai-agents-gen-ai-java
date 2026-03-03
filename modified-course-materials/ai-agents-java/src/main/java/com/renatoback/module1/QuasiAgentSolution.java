package com.renatoback.module1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.renatoback.module1.Message.Roles;

/**
 * This exercise consists of three prompts:
 * <ol>
 * <li>First Prompt:
 * <ul>
 * <li>Ask the user what function they want to create
 * <li>Ask the LLM to write a basic Java function based on the user’s
 * description
 * <li>Store the response for use in subsequent prompts
 * <li>Parse the response to separate the code from the commentary by the LLM
 * </ul>
 * <li>Second Prompt:
 * <ul>
 * <li>Pass the code generated from the first prompt
 * <li>Ask the LLM to add comprehensive documentation including:
 * <ul>
 * <li>Function description
 * <li>Parameter descriptions
 * <li>Return value description
 * <li>Example usage
 * <li>Edge cases
 * </ul>
 * </ul>
 * <li>Third Prompt:
 * <ul>
 * <li>Pass the documented code generated from the second prompt
 * <li>Ask the LLM to add test cases using JUnit framework
 * <li>Tests should cover:
 * <ul>
 * <li>Basic functionality
 * <li>Edge cases
 * <li>Error cases
 * <li>Various input scenarios
 * </ul>
 * </ul>
 * <p>
 * <b>Requirements:</b>
 * </p>
 * <ul>
 * <li>Maintain conversation context between prompts
 * <li>Print each step of the development process
 * <li>Save both the documented function and the test class to separate files
 * </ul>
 */
public class QuasiAgentSolution {

    private final List<Message> conversationHistory = new ArrayList<>();
    private String targetClassName;

    public QuasiAgentSolution() {
        // Initialize with system message
        conversationHistory.add(Message.of(Roles.SYSTEM,
                """
                        - You're an expert Java developer who specializes in writing clean, efficient, and well-documented code.
                        - When writing functions, follow best practices and provide comprehensive documentation and test cases.
                        - ALWAYS write functions inside classes.
                        - If multiple classes are needed, use wrapper class.
                        - Provide ALL code in a single class.
                        - Class naming pattern: Example{N} where N is a random Integer that you will calculate.
                        - Test class naming pattern: {SOURCE}Test, where SOURCE is the name of the class being tested.
                        - DON'T repeat class names.
                        - The only code block in your answer should be the class itself.
                        - Since you only generate Java code, use the java as the programming language of the block.
                """));
                        
                //         - You will receive a manual user prompt which will ask you to implement a specific function.
                //         - On your first answer, you will ONLY deliver the function code. DON'T worry about tests, comments or documentation.
                //         - On your second prompt, the user will send you a function and yo will add comprehensive documentation to it.
                //         - Then, on the third prompt, you will receive a documented code and you will generate tests for:
                //           - Basic functionality;
                //           - Edge cases;
                //           - Error cases;
                //           - various input scenarios
                // """));
    }

    public void run(LLM llm, String userFunctionRequest) {
        try {
            // Step 1: Generate basic function
            String basicFunction = generateBasicFunction(userFunctionRequest, llm);
            System.out.println("\nGenerated Basic Function:\n" + basicFunction);

            // Step 2: Add comprehensive documentation
            String documentedFunction = addDocumentation(basicFunction, llm);
            System.out.println("\nFunction with Documentation:\n" + documentedFunction);

            // Step 3: Add test cases
            String functionWithTests = addTestCases(documentedFunction, llm);
            System.out.println("\nFunction with Tests:\n" + functionWithTests);

            // Save to files
            saveToFile(documentedFunction);
            saveToFile(functionWithTests);

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateBasicFunction(String userFunctionRequest, LLM llm) {
        // 1. Add user message to conversationHistory
        conversationHistory.add(Message.of(Roles.USER, """
            Inside a java class, create a function and implement it for the given specs:

                %s
            
            Don't write any documentation, nor tests yet.
            """.formatted(userFunctionRequest)));
        // 2. Send to LLM
        String response = llm.generateResponse(conversationHistory);
        // 3. Store LLM response in conversationHistory
        conversationHistory.add(Message.of(Roles.ASSISTANT, response));
        // 4. Extract code from response
        String code = extractCodeFromResponse(response);
        // 5. Return the code
        return code;
    }

    private String addDocumentation(String basicFunction, LLM llm) {
        // 1. Add user message asking for documentation to conversationHistory
        conversationHistory.add(Message.of(Roles.USER, """
            Add comprehensive documentation to the following code:
            ```java
            %s
            ```
            """.formatted(basicFunction)));
        // 2. Send to LLM
        String response = llm.generateResponse(conversationHistory);
        // 3. Store LLM response in conversationHistory
        conversationHistory.add(Message.of(Roles.ASSISTANT, response));
        // 4. Extract documented code from response
        String code = extractCodeFromResponse(response);
        // 5. Return the documented code
        return code;
    }

    private String addTestCases(String documentedFunction, LLM llm) {
        // 1. Add user message asking for test cases to conversationHistory
        conversationHistory.add(Message.of(Roles.USER, """
            Given the class:
            ```java
            %s
            ```

            Create a test class covering:
            - Basic functionality;
            - Edge cases;
            - Error cases;
            - various input scenarios
            """.formatted(documentedFunction)));
        // 2. Send to LLM
        String response = llm.generateResponse(conversationHistory);
        // 3. Store LLM response in conversationHistory
        conversationHistory.add(Message.of(Roles.ASSISTANT, response));
        // 4. Extract code with tests from response
        String code = extractCodeFromResponse(response);
        // 5. Return the code with tests
        return code;
    }

    private void saveToFile(String code) throws IOException {
        // Extract class name from the code to use as filename
        String className = extractClassName(code);
        String filename = className + ".java";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(code);
        }

        System.out.println("\nSaved to file: " + filename);
    }

    private String extractClassName(String code) {
        // Simple regex to find class name
        Pattern pattern = Pattern.compile("class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Default name if class name not found
        return "GeneratedFunction";
    }

    private String extractCodeFromResponse(String response) {
        // Try to extract code between ```java and ``` markers
        Pattern pattern = Pattern.compile("```java\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // If no code block markers found, try to extract any Java-like code
        pattern = Pattern.compile("(public\\s+.*?\\{.*?\\})", Pattern.DOTALL);
        matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // If nothing else works, return the full response
        System.out.println("Warning: Could not extract code from response, returning full text");
        return response;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Create the LLM instance
            LLM llm = new LLM();

            // Create the agent
            QuasiAgentSolution agent = new QuasiAgentSolution();

            // Get user input
            System.out.print(
                    "What Java function would you like me to create for you? Please describe what it should do: ");
            String userFunctionRequest = scanner.nextLine();

            // Run the agent
            agent.run(llm, userFunctionRequest);

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}