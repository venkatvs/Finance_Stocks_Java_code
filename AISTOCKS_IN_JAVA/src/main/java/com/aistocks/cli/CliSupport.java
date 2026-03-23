package com.aistocks.cli;

import com.aistocks.config.EnvBootstrap;
import com.aistocks.config.ProjectPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class CliSupport {

    private CliSupport() {}

    /**
     * Load .env with Python-parity precedence: explicit path, then {@code AISTOCKS_DOTENV}, then
     * {@code AISTOCKS_IN_JAVA/.env}, then parent Python repo {@code .env} (fills only unset vars).
     */
    public static void bootstrapDotenv(Path dotenvFlag) throws Exception {
        if (dotenvFlag != null) {
            if (Files.isRegularFile(dotenvFlag)) {
                EnvBootstrap.loadIfPresent(dotenvFlag);
            } else {
                EnvBootstrap.loadIfPresent(dotenvFlag.resolve(".env"));
            }
            return;
        }
        Optional<Path> fromEnv = ProjectPaths.dotenvPathFromEnv();
        if (fromEnv.isPresent()) {
            Path p = fromEnv.get();
            if (Files.isRegularFile(p)) {
                EnvBootstrap.loadIfPresent(p);
            } else {
                EnvBootstrap.loadIfPresent(p.resolve(".env"));
            }
            return;
        }
        Path javaRoot = ProjectPaths.findJavaProjectRoot();
        EnvBootstrap.loadIfPresent(javaRoot.resolve(".env"));
        ProjectPaths.findPythonForecasterRoot(javaRoot).ifPresent(py -> EnvBootstrap.loadIfPresent(py.resolve(".env")));
    }
}
