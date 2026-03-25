package com.github.ngeor;

import java.util.Collection;

public final class AppUtil {
    private AppUtil() {}

    public static int runSteps(Collection<StepDefinition> steps) throws InterruptedException {
        int stepNumber = 0;
        for (StepDefinition stepDefinition : steps) {
            try {
                stepNumber++;
                stepDefinition.step().run();
            } catch (ProcessFailException ex) {
                System.err.printf(
                        "[%s] Child process exited with code %d : %s%n",
                        stepDefinition.name(), ex.getCode(), ex.getMessage());
                return stepNumber;
            } catch (RuntimeException ex) {
                System.err.printf("[%s] %s%n", stepDefinition.name(), ex.getMessage());
                return stepNumber;
            }
        }

        return 0;
    }
}
