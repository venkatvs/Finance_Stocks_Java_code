package com.aistocks.cli;

import com.aistocks.audits.AuditStubs;
import com.aistocks.config.Credentials;
import com.aistocks.config.ProjectPaths;
import com.aistocks.data.fmp.FmpClient;
import com.aistocks.pipelines.*;
import com.aistocks.universe.AiUniverseRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

final class SubCommands {

    private SubCommands() {}

    @Command(name = "download-data", description = "Download market data (FMP) — batch parity via Python; Java can smoke-test quote.")
    static class DownloadDataCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--start", required = true, description = "Start date (YYYY-MM-DD)")
        LocalDate start;

        @Option(names = "--end", required = true, description = "End date (YYYY-MM-DD)")
        LocalDate end;

        @Option(names = "--tickers", description = "Comma-separated tickers (default: full AI universe)")
        String tickers;

        @Option(names = "--dry-run", description = "Plan only")
        boolean dryRun;

        @Option(names = "--api-key", description = "Override FMP key (else FMP_KEYS / FMP_API_KEY)")
        String apiKey;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            List<String> list =
                    tickers == null || tickers.isBlank()
                            ? AiUniverseRegistry.allTickersSorted()
                            : Arrays.stream(tickers.split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList();
            var r = DataPipeline.runDataDownload(list, start, end, dryRun);
            System.out.println(r.summary());
            if (!dryRun && list.size() <= 3) {
                String key = Credentials.resolveFmpApiKey(Optional.ofNullable(apiKey));
                Path cache = ProjectPaths.findJavaProjectRoot().resolve("data/cache/fmp");
                FmpClient c = new FmpClient(key, cache, true, Duration.ofHours(24));
                for (String sym : list) {
                    JsonNode q = c.getQuote(sym);
                    System.out.println("FMP quote sample (" + sym + "): " + q.toString().substring(0, Math.min(200, q.toString().length())));
                }
            }
            return 0;
        }
    }

    @Command(name = "build-universe", description = "Build universe as-of date")
    static class BuildUniverseCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--asof", required = true)
        LocalDate asof;

        @Option(names = "--max-size", defaultValue = "100")
        int maxSize;

        @Option(names = "--categories", description = "Comma-separated category keys")
        String categories;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            List<String> cats = null;
            if (categories != null && !categories.isBlank()) {
                cats = Arrays.stream(categories.split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList();
            }
            var u = UniversePipeline.runUniverseConstruction(asof, maxSize, cats);
            System.out.println(u.summary());
            for (String w : u.warnings()) {
                System.out.println("WARNING: " + w);
            }
            System.out.println("\nConstituents:");
            int i = 1;
            for (String t : u.tickers()) {
                var m = u.tickerMetadata().get(t);
                String cat = m != null ? m.sector() : "unknown";
                System.out.printf("  %3d. %-6s (%s)%n", i++, t, cat);
            }
            return 0;
        }
    }

    @Command(name = "build-features", description = "Build features (not yet ported)")
    static class BuildFeaturesCommand implements Callable<Integer> {
        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--asof", required = true)
        LocalDate asof;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            System.out.println("build-features: not yet implemented in Java — use Python pipeline.");
            return 0;
        }
    }

    @Command(name = "train-baselines", description = "Train baselines (not yet ported)")
    static class TrainBaselinesCommand implements Callable<Integer> {
        @ParentCommand AiStocksRootCommand root;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            System.out.println("train-baselines: not yet implemented in Java.");
            return 0;
        }
    }

    @Command(name = "score", description = "Generate signals (placeholder scorer matches Python)")
    static class ScoreCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--asof", required = true)
        LocalDate asof;

        @Option(names = "--tickers", description = "Comma-separated tickers")
        String tickers;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            List<String> t =
                    tickers == null || tickers.isBlank()
                            ? null
                            : Arrays.stream(tickers.split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList();
            var horizons = List.of(20, 60, 90);
            var result = ScoringPipeline.runScoring(asof, t, horizons);
            System.out.println(result.summary());
            for (String w : result.warnings()) {
                System.out.println("WARNING: " + w);
            }
            if (!result.tickers().isEmpty()) {
                System.out.println();
                System.out.println(ScoringPipeline.placeholderTopRankedSummary(asof, result.tickers(), 20, 3));
            }
            return 0;
        }
    }

    @Command(name = "make-report", description = "Generate reports (stub)")
    static class MakeReportCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--asof", required = true)
        LocalDate asof;

        @Option(names = "--formats", description = "e.g. text,csv,json,html")
        String formats;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            List<String> f =
                    formats == null || formats.isBlank()
                            ? List.of("text")
                            : Arrays.stream(formats.split(",")).map(String::strip).toList();
            System.out.println(ReportPipeline.runReportGeneration(asof, f).summary());
            return 0;
        }
    }

    @Command(name = "audit-pit", description = "PIT audit (delegate to Python for full run)")
    static class AuditPitCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--start", required = true)
        LocalDate start;

        @Option(names = "--end", required = true)
        LocalDate end;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            System.out.println(AuditStubs.runPitAudit(start, end).summary());
            return 0;
        }
    }

    @Command(name = "audit-survivorship", description = "Survivorship audit (delegate to Python)")
    static class AuditSurvivorshipCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--start", required = true)
        LocalDate start;

        @Option(names = "--end", required = true)
        LocalDate end;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            System.out.println(AuditStubs.runSurvivorshipAudit(start, end).summary());
            return 0;
        }
    }

    @Command(name = "list-universe", description = "List AI stock universe")
    static class ListUniverseCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--category", description = "Single category key")
        String category;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            System.out.println("=".repeat(70));
            System.out.println("AI STOCK UNIVERSE (Java bundle)");
            System.out.println("=".repeat(70));
            var all = AiUniverseRegistry.allTickersSorted();
            System.out.println("\nTotal: " + all.size() + " tickers\n");
            if (category != null && !category.isBlank()) {
                if (!AiUniverseRegistry.categories().containsKey(category)) {
                    System.err.println("Unknown category: " + category);
                    return 1;
                }
                System.out.println("Tickers in " + category + ":");
                for (String t : AiUniverseRegistry.categories().get(category)) {
                    System.out.println("  " + t);
                }
                return 0;
            }
            for (var e : AiUniverseRegistry.categories().entrySet()) {
                String desc = AiUniverseRegistry.categoryDescriptions().getOrDefault(e.getKey(), "");
                System.out.println(e.getKey() + " (" + e.getValue().size() + " stocks)");
                System.out.println("  " + desc);
                System.out.println("  Tickers: " + e.getValue().stream().limit(12).collect(Collectors.joining(", "))
                        + (e.getValue().size() > 12 ? ", ..." : ""));
                System.out.println();
            }
            return 0;
        }
    }

    @Command(name = "run", description = "Full pipeline sketch")
    static class RunFullPipelineCommand implements Callable<Integer> {

        @ParentCommand AiStocksRootCommand root;

        @Option(names = "--asof", required = true)
        LocalDate asof;

        @Override
        public Integer call() throws Exception {
            CliSupport.bootstrapDotenv(root.dotenv);
            System.out.println("Step 1: universe");
            var u = UniversePipeline.runUniverseConstruction(asof, 100, null);
            System.out.println(u.summary());
            System.out.println("Step 2: features — skipped (not ported)");
            System.out.println("Step 3: score");
            var s = ScoringPipeline.runScoring(asof, u.tickers(), List.of(20, 60, 90));
            System.out.println(s.summary());
            System.out.println("Step 4: report");
            System.out.println(ReportPipeline.runReportGeneration(asof, List.of("text")).summary());
            return 0;
        }
    }
}
