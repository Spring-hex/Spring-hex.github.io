package com.springhex.command;

import com.springhex.config.ConfigResolver;
import com.springhex.config.ConfigurationException;
import com.springhex.config.HexPathResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.generator.FileGenerator;
import com.springhex.generator.StubProcessor;
import com.springhex.util.PackageResolver;
import com.springhex.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:factory",
    mixinStandardHelpOptions = true,
    description = "Generate a data factory class using Datafaker"
)
public class MakeFactoryCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Entity name (e.g., User, Product)")
    private String entityName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeFactoryCommand() {
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
        this.packageResolver = new PackageResolver();
    }

    @Override
    public Integer call() {
        try {
            ResolvedConfig config = ConfigResolver.resolve(mixin.getOutputDir(), mixin.getBasePackage());
            String resolvedPackage = config.getBasePackage();
            HexPathResolver pathResolver = config.getPathResolver();

            String capitalized = StringUtils.capitalize(entityName);
            String aggregateLower = aggregate.toLowerCase();

            String factoryPackage = pathResolver.resolve("factory", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{PACKAGE}}", factoryPackage);
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{ENTITY_NAME}}", capitalized);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            String content = stubProcessor.process("data/factory", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), capitalized + "Factory", factoryPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            System.out.println("\nFactory generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating factory: " + e.getMessage());
            return 1;
        }
    }
}
