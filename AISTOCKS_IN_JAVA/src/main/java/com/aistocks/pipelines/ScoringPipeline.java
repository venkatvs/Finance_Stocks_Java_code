package com.aistocks.pipelines;

import java.time.LocalDate;
import java.util.*;

/**
 * Port of {@code scoring_pipeline.ScoringResult} and placeholder ranking (deterministic RNG).
 */
public final class ScoringPipeline {

    private ScoringPipeline() {}

    public record ScoringResult(
            LocalDate asofDate,
            int universeSize,
            int signalsGenerated,
            List<Integer> horizons,
            Map<String, String> modelVersions,
            double durationSeconds,
            boolean pitValidated,
            List<String> warnings,
            List<String> tickers) {

        public String summary() {
            String status = pitValidated ? "✅" : "⚠️";
            return status + " Scoring Complete: " + asofDate + "\n"
                    + "  Universe: " + universeSize + " stocks\n"
                    + "  Signals: " + signalsGenerated + "\n"
                    + "  Horizons: " + horizons + "\n"
                    + "  Duration: " + String.format("%.1f", durationSeconds) + "s\n"
                    + (warnings.isEmpty() ? "" : "  Warnings: " + warnings.size());
        }
    }

    public static ScoringResult runScoring(LocalDate asof, List<String> tickers, List<Integer> horizons) {
        long t0 = System.nanoTime();
        List<String> t = tickers == null ? com.aistocks.universe.AiUniverseRegistry.allTickersSorted() : tickers;
        List<String> warnings =
                List.of("Scoring pipeline not yet implemented - awaiting model integration (Python parity).");
        Map<String, String> mv = Map.of("kronos", "placeholder", "fintext", "placeholder", "baseline", "placeholder", "fusion", "placeholder");
        double sec = (System.nanoTime() - t0) / 1e9;
        int sig = t.size() * horizons.size();
        return new ScoringResult(asof, t.size(), sig, horizons, mv, sec, true, warnings, new ArrayList<>(t));
    }

    /**
     * Same structure as Python {@code placeholder_top_ranked_summary}; {@link Random} differs from CPython
     * {@code random.Random}, so ranks are not byte-identical across languages.
     */
    public static String placeholderTopRankedSummary(LocalDate asof, List<String> tickers, int horizonDays, int topN) {
        int seed = asof.getYear() * 10000 + asof.getMonthValue() * 100 + asof.getDayOfMonth();
        Random rng = new Random(seed);
        record Row(double alpha, String ticker, double meanExc, double p5, double p95) {}
        List<Row> rows = new ArrayList<>();
        for (String sym : tickers) {
            double er = -0.15 + rng.nextDouble() * (0.22 + 0.15);
            double std = Math.abs(er) * 2 + 0.05;
            double p5 = er - 1.65 * std;
            double p95 = er + 1.65 * std;
            double alpha = er * 10.0 + rng.nextGaussian() * 0.05;
            rows.add(new Row(alpha, sym, er, p5, p95));
        }
        rows.sort(Comparator.comparingDouble(Row::alpha).reversed());
        StringBuilder sb = new StringBuilder();
        sb.append("Top ")
                .append(topN)
                .append(" (placeholder ranking, ")
                .append(horizonDays)
                .append("d horizon, seeded by ")
                .append(asof)
                .append("):\n");
        for (int i = 0; i < Math.min(topN, rows.size()); i++) {
            Row r = rows.get(i);
            sb.append(String.format(
                    "  %d. %s  mean excess=%+.2f%%  range [P5,P95]=[%+.2f%%, %+.2f%%]%n",
                    i + 1,
                    r.ticker,
                    r.meanExc * 100,
                    r.p5 * 100,
                    r.p95 * 100));
        }
        return sb.toString().stripTrailing();
    }
}
