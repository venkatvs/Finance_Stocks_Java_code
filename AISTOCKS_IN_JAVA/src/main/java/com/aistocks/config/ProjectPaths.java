package com.aistocks.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Locate project root (directory containing {@code settings.gradle.kts} for this repo).
 */
public final class ProjectPaths {

    private ProjectPaths() {}

    /**
     * Optional override: absolute path to directory or file for .env (if file, parent used only for display).
     */
    public static Optional<Path> dotenvPathFromEnv() {
        String p = System.getenv("AISTOCKS_DOTENV");
        if (p == null || p.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Path.of(p).toAbsolutePath().normalize());
    }

    public static Path findJavaProjectRoot() {
        Path start = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path found = walkUpForFile(start, "settings.gradle.kts");
        if (found != null) {
            return found.getParent();
        }
        return start;
    }

    /**
     * Find sibling AI Stock Forecaster (Python) repo root by walking up for {@code requirements.txt} + {@code src/cli.py}.
     */
    public static Optional<Path> findPythonForecasterRoot(Path startDir) {
        Path p = startDir.toAbsolutePath();
        for (int i = 0; i < 12 && p != null; i++) {
            if (Files.isRegularFile(p.resolve("requirements.txt"))
                    && Files.isRegularFile(p.resolve("src/cli.py"))) {
                return Optional.of(p);
            }
            p = p.getParent();
        }
        return Optional.empty();
    }

    private static Path walkUpForFile(Path start, String filename) {
        Path p = start.toAbsolutePath();
        for (int i = 0; i < 12 && p != null; i++) {
            if (Files.isRegularFile(p.resolve(filename))) {
                return p.resolve(filename);
            }
            p = p.getParent();
        }
        return null;
    }
}
