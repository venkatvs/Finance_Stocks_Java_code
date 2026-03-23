package com.aistocks.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

class CredentialsTest {

    @AfterEach
    void tearDown() {
        EnvBootstrap.clearForTests();
    }

    @Test
    void fmpKeysFirstCommaWins() throws Exception {
        Path f = Files.createTempFile("env", ".properties");
        Files.writeString(f, "FMP_KEYS=aaa,bbb\n");
        EnvBootstrap.loadIfPresent(f);
        Assertions.assertEquals("aaa", Credentials.resolveFmpApiKey(Optional.empty()));
    }

    @Test
    void fmpApiKeyFallback() throws Exception {
        Path f = Files.createTempFile("env", ".properties");
        Files.writeString(f, "FMP_API_KEY=zzz\n");
        EnvBootstrap.loadIfPresent(f);
        Assertions.assertEquals("zzz", Credentials.resolveFmpApiKey(Optional.empty()));
    }
}
