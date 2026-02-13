package com.springhex.command;

import com.springhex.util.BuildToolDetector;
import com.springhex.util.BuildToolDetector.BuildTool;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "db:seed",
    mixinStandardHelpOptions = true,
    description = "Run database seeders"
)
public class DbSeedCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..1", description = "Seeder class name to run (e.g., UserSeeder)")
    private String seederName;

    @Option(names = "--all", description = "Run all seeders")
    private boolean all;

    private final BuildToolDetector buildToolDetector;

    public DbSeedCommand() {
        this.buildToolDetector = new BuildToolDetector();
    }

    @Override
    public Integer call() {
        if (seederName == null && !all) {
            System.err.println("Error: Specify a seeder name or use --all to run all seeders.");
            System.err.println("Usage: spring-hex db:seed <SeederName>");
            System.err.println("       spring-hex db:seed --all");
            return 1;
        }

        String baseDir = System.getProperty("user.dir");
        BuildTool tool = buildToolDetector.detect(baseDir);
        if (tool == null) {
            System.err.println("Error: No build tool detected. Ensure you are in a Maven or Gradle project directory.");
            return 1;
        }

        String executable = buildToolDetector.resolveExecutable(baseDir, tool);
        String seedTarget = all ? "all" : seederName;
        List<String> command = buildCommand(executable, tool, seedTarget);

        System.out.println("Running seeder: " + seedTarget);
        System.out.println("Executing: " + String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(baseDir));
            pb.inheritIO();
            Process process = pb.start();
            return process.waitFor();
        } catch (IOException e) {
            System.err.println("Error running seeder: " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Seeder execution interrupted.");
            return 1;
        }
    }

    private List<String> buildCommand(String executable, BuildTool tool, String seedTarget) {
        List<String> command = new ArrayList<>();
        command.add(executable);

        if (tool == BuildTool.MAVEN) {
            command.add("spring-boot:run");
            command.add("-Dspring-boot.run.arguments=--seed=" + seedTarget);
        } else {
            command.add("bootRun");
            command.add("--args=--seed=" + seedTarget);
        }

        return command;
    }
}
