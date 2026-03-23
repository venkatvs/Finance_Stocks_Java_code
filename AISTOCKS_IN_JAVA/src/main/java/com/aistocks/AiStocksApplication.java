package com.aistocks;

import com.aistocks.cli.AiStocksRootCommand;
import picocli.CommandLine;

public final class AiStocksApplication {

    public static void main(String[] args) {
        int code = new CommandLine(new AiStocksRootCommand()).setExecutionExceptionHandler(new PrintException()).execute(args);
        System.exit(code);
    }

    static final class PrintException implements CommandLine.IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) {
            System.err.println(ex.getMessage());
            return 1;
        }
    }
}
