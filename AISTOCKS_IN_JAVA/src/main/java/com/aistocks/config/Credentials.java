package com.aistocks.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Same credential <strong>names</strong> and precedence as the Python forecaster
 * ({@code src/utils/env.py}, {@code src/config.py}, data clients).
 */
public final class Credentials {

    private Credentials() {}

    /**
     * FMP: CLI override &gt; first of {@code FMP_KEYS} (comma-separated) &gt; {@code FMP_API_KEY}.
     */
    public static String resolveFmpApiKey(Optional<String> cliKey) {
        if (cliKey.isPresent() && !cliKey.get().isBlank()) {
            return cliKey.get().strip();
        }
        String fmpKeys = EnvBootstrap.getenv("FMP_KEYS");
        if (fmpKeys != null && !fmpKeys.isBlank()) {
            List<String> keys =
                    Arrays.stream(fmpKeys.split(",")).map(String::strip).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            if (!keys.isEmpty()) {
                return keys.get(0);
            }
        }
        String single = EnvBootstrap.getenv("FMP_API_KEY");
        if (single != null && !single.isBlank()) {
            return single.strip();
        }
        throw new IllegalStateException(
                "FMP API key not found. Set FMP_KEYS or FMP_API_KEY in .env / environment, "
                        + "or pass --api-key (same as Python resolve_fmp_key).");
    }

    public static Optional<String> polygonFirstKey() {
        return firstCommaSeparated(EnvBootstrap.getenv("POLYGON_KEYS"));
    }

    public static Optional<String> alphaVantageFirstKey() {
        return firstCommaSeparated(EnvBootstrap.getenv("ALPHAVANTAGE_KEYS"));
    }

    public static Optional<String> secContactEmail() {
        String e = EnvBootstrap.getenv("SEC_CONTACT_EMAIL");
        if (e == null || e.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(e.strip());
    }

    private static Optional<String> firstCommaSeparated(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(raw.split(",")).map(String::strip).filter(s -> !s.isEmpty()).findFirst();
    }
}
