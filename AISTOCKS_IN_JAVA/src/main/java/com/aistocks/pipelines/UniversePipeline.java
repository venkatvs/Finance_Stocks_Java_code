package com.aistocks.pipelines;

import com.aistocks.universe.AiUniverseRegistry;

import java.time.LocalDate;
import java.util.*;

/**
 * Port of {@code universe_pipeline} seed-universe path (stub PIT store → fixed 100 tickers).
 */
public final class UniversePipeline {

    private UniversePipeline() {}

    public record UniverseResult(
            LocalDate asofDate,
            int maxSize,
            List<String> tickers,
            Map<String, TickerMeta> tickerMetadata,
            List<String> warnings) {

        public String summary() {
            return "✅ Universe built: " + asofDate + "\n  Tickers: " + tickers.size();
        }
    }

    public record TickerMeta(String ticker, String sector) {}

    public static UniverseResult runUniverseConstruction(
            LocalDate asof, int maxSize, List<String> categoriesFilter) {
        List<String> base;
        if (categoriesFilter == null || categoriesFilter.isEmpty()) {
            base = AiUniverseRegistry.allTickersSorted();
        } else {
            base = AiUniverseRegistry.tickersForCategories(categoriesFilter);
        }
        List<String> tickers =
                base.size() > maxSize
                        ? List.copyOf(base.subList(0, maxSize))
                        : List.copyOf(base);
        Map<String, TickerMeta> meta = new LinkedHashMap<>();
        for (String t : tickers) {
            String cat = AiUniverseRegistry.categoryForTicker(t).orElse("unknown");
            meta.put(t, new TickerMeta(t, cat));
        }
        List<String> warnings =
                List.of("StubPITStore — same as Python placeholder until DuckDB PIT is wired for Java.");
        return new UniverseResult(asof, maxSize, tickers, meta, warnings);
    }
}
