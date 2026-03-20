package com.github.ngeor;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Hello world!
 */
@Command(name = "app", mixinStandardHelpOptions = true, version = "1.0",
    description = "A sample Picocli application.")
public final class App implements Callable<Integer> {
    @Option(names = "--dry-run", description = "A dummy dry-run argument.")
    private boolean dryRun;

    private App() {}

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }

    @Override
    public Integer call() {
        System.out.println("Hello World! dryRun was " + dryRun);
        return 0;
    }
}
