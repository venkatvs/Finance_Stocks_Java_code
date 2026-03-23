package com.aistocks.config;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads {@code .env} into an in-memory overlay. Lookup order matches python-dotenv with
 * {@code override=False}: existing {@link System#getenv()} wins; file fills only missing keys.
 */
public final class EnvBootstrap {

    private static final Map<String, String> FROM_DOTENV = new ConcurrentHashMap<>();

    private EnvBootstrap() {}

    public static void clearForTests() {
        FROM_DOTENV.clear();
    }

    public static int loadIfPresent(Path envFile) throws Exception {
        if (!Files.isRegularFile(envFile)) {
            return 0;
        }
        int n = 0;
        try (BufferedReader r = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = unquote(line.substring(eq + 1).trim());
                if (key.isEmpty()) {
                    continue;
                }
                if (System.getenv(key) != null) {
                    continue;
                }
                if (FROM_DOTENV.putIfAbsent(key, value) == null) {
                    n++;
                }
            }
        }
        return n;
    }

    public static String getenv(String key) {
        String sys = System.getenv(key);
        if (sys != null) {
            return sys;
        }
        return FROM_DOTENV.get(key);
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char a = value.charAt(0);
            char b = value.charAt(value.length() - 1);
            if ((a == '"' && b == '"') || (a == '\'' && b == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
