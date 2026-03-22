package com.github.ngeor;

@FunctionalInterface
interface Step {
    void run() throws InterruptedException;
}
