package com.renatoback.module1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal .env loader for the modified course materials.
 *
 * Values from a local ".env" file (in the working directory or project root)
 * take precedence over process environment variables.
 */
final class DotEnvConfig {

    private static final Map<String, String> ENV_FROM_FILE = loadDotEnv();

    private DotEnvConfig() {
    }

    static String get(String name, String defaultValue) {
        String value = ENV_FROM_FILE.get(name);
        if (value != null && !value.isBlank()) {
            return value;
        }
        value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static Map<String, String> loadDotEnv() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path dotEnv = cwd.resolve(".env");

        if (!Files.exists(dotEnv)) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();
        try {
            for (String line : Files.readAllLines(dotEnv, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, idx).trim();
                String rawValue = trimmed.substring(idx + 1).trim();

                // Remove optional surrounding quotes
                if ((rawValue.startsWith("\"") && rawValue.endsWith("\""))
                        || (rawValue.startsWith("'") && rawValue.endsWith("'"))) {
                    rawValue = rawValue.substring(1, rawValue.length() - 1);
                }

                if (!key.isEmpty()) {
                    result.put(key, rawValue);
                }
            }
        } catch (IOException ignored) {
            return Collections.emptyMap();
        }
        return result;
    }
}

