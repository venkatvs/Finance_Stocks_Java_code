package com.aistocks.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "aistocks",
        mixinStandardHelpOptions = true,
        version = "AISTOCKS_IN_JAVA 1.0.0",
        description = "Java port / twin of the AI Stock Forecaster CLI (see README).",
        subcommands = {
                SubCommands.DownloadDataCommand.class,
                SubCommands.BuildUniverseCommand.class,
                SubCommands.BuildFeaturesCommand.class,
                SubCommands.TrainBaselinesCommand.class,
                SubCommands.ScoreCommand.class,
                SubCommands.MakeReportCommand.class,
                SubCommands.AuditPitCommand.class,
                SubCommands.AuditSurvivorshipCommand.class,
                SubCommands.ListUniverseCommand.class,
                SubCommands.RunFullPipelineCommand.class,
        })
public class AiStocksRootCommand implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Option(
            names = "--dotenv",
            description = "Path to a .env file, or a directory that contains .env (same keys as Python project).")
    Path dotenv;

    @Override
    public Integer call() {
        spec.commandLine().usage(System.out);
        return 0;
    }
}
