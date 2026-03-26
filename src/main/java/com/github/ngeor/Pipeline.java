package com.github.ngeor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public record Pipeline(List<StepDefinition> steps, List<StepDefinition> tearDownSteps) implements Callable<Integer> {
    public Pipeline {
        Objects.requireNonNull(steps, "Steps cannot be null");
        Objects.requireNonNull(tearDownSteps, "Tear down steps cannot be null");
    }

    @Override
    public Integer call() throws InterruptedException {
        try {
            return runSteps();
        } finally {
            runTearDownSteps();
        }
    }

    private Integer runSteps() throws InterruptedException {
        int stepNumber = 0;
        for (StepDefinition stepDefinition : steps) {
            try {
                stepNumber++;
                printStep(stepNumber, stepDefinition);
                stepDefinition.step().run();
            } catch (ProcessFailException ex) {
                System.err.printf(
                    "[%s] Child process exited with code %d : %s%n",
                    stepDefinition.name(), ex.getCode(), ex.getMessage());
                return stepNumber;
            } catch (RuntimeException ex) {
                printErrStep(stepDefinition, ex);
                return stepNumber;
            }
        }

        return 0;
    }

    private void runTearDownSteps() {
        int stepNumber = steps.size();
        for (StepDefinition stepDefinition : tearDownSteps) {
           stepNumber++;
           printStep(stepNumber, stepDefinition);
           try {
               stepDefinition.step().run();
           } catch (InterruptedException ex) {
               Thread.currentThread().interrupt();
               printErrStep(stepDefinition, ex);
           } catch (Exception ex) {
               printErrStep(stepDefinition, ex);
           }
        }
    }

    private int totalStepCount() {
        return steps.size() + tearDownSteps.size();
    }

    private void printStep(int stepNumber, StepDefinition stepDefinition) {
        System.out.printf("%d/%d - %s%n", stepNumber, totalStepCount(), stepDefinition.name());
    }

    private void printErrStep(StepDefinition stepDefinition, Exception ex) {
        System.err.printf("[%s] %s%n", stepDefinition.name(), ex.getMessage());
    }
}
