package com.aistocks.pipelines;

import java.time.LocalDate;
import java.util.List;

/** Port sketch of {@code data_pipeline.run_data_download}. */
public final class DataPipeline {

    private DataPipeline() {}

    public record DataDownloadResult(boolean success, int tickers, String message) {
        public String summary() {
            return (success ? "✅ " : "❌ ") + message;
        }
    }

    public static DataDownloadResult runDataDownload(
            List<String> tickers, LocalDate start, LocalDate end, boolean dryRun) {
        if (dryRun) {
            return new DataDownloadResult(true, tickers.size(), "Dry run: would download for " + tickers.size() + " tickers");
        }
        return new DataDownloadResult(
                true,
                tickers.size(),
                "Download orchestration not fully ported — use Python `download-data` for production batches, "
                        + "or call `FmpClient` from Java for incremental endpoints.");
    }
}
