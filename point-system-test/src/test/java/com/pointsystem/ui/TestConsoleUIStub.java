package com.pointsystem.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * TestConsoleUIStub - Test Stub for ConsoleUI Interface
 *
 * Purpose:
 * This is a test stub that simulates console I/O operations for automated testing.
 * It allows tests to:
 * 1. Pre-configure input values that will be returned
 * 2. Capture and verify output messages
 * 3. Test the system without actual user interaction
 *
 * Design Pattern: Test Stub (Fake Object)
 *
 * Usage Example:
 * <pre>
 * TestConsoleUIStub stub = new TestConsoleUIStub();
 * stub.addIntInput(10);          // Will be returned by readInt
 * stub.addInputLine("username"); // Will be returned by readLine
 *
 * PointSystem system = new PointSystem("user", stub);
 * system.someOperation();
 *
 * assertTrue(stub.containsOutput("Success"));
 * </pre>
 */
public class TestConsoleUIStub implements ConsoleUI {

    // Queue of integer inputs to be returned
    private final List<Integer> intInputs = new ArrayList<>();
    private int intInputIndex = 0;

    // Queue of string inputs to be returned
    private final List<String> lineInputs = new ArrayList<>();
    private int lineInputIndex = 0;

    // Captured output messages
    private final List<String> outputs = new ArrayList<>();

    // Captured success messages
    private final List<String> successMessages = new ArrayList<>();

    // Captured error messages
    private final List<String> errorMessages = new ArrayList<>();

    /**
     * Add an integer input to the queue
     * @param value The integer value to return
     */
    public void addIntInput(int value) {
        intInputs.add(value);
    }

    /**
     * Add a string input to the queue (for readLine)
     * @param value The string value to return
     */
    public void addInputLine(String value) {
        lineInputs.add(value);
    }

    /**
     * Get all captured outputs
     * @return List of all output messages
     */
    public List<String> getOutputs() {
        return new ArrayList<>(outputs);
    }

    /**
     * Get all captured success messages
     * @return List of success messages
     */
    public List<String> getSuccessMessages() {
        return new ArrayList<>(successMessages);
    }

    /**
     * Get all captured error messages
     * @return List of error messages
     */
    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages);
    }

    /**
     * Check if output contains a specific message
     * @param message The message to search for
     * @return true if found
     */
    public boolean containsOutput(String message) {
        return outputs.stream().anyMatch(o -> o.contains(message));
    }

    /**
     * Check if success messages contain a specific message
     * @param message The message to search for
     * @return true if found
     */
    public boolean containsSuccess(String message) {
        return successMessages.stream().anyMatch(s -> s.contains(message));
    }

    /**
     * Check if error messages contain a specific message
     * @param message The message to search for
     * @return true if found
     */
    public boolean containsError(String message) {
        return errorMessages.stream().anyMatch(e -> e.contains(message));
    }

    /**
     * Clear all captured outputs
     */
    public void clearOutputs() {
        outputs.clear();
        successMessages.clear();
        errorMessages.clear();
    }

    @Override
    public String readLine(String prompt) {
        outputs.add("[PROMPT] " + prompt);
        if (lineInputIndex < lineInputs.size()) {
            return lineInputs.get(lineInputIndex++);
        }
        return ""; // Default empty string if no input configured
    }

    @Override
    public int readInt(String prompt, int min, int max) {
        outputs.add("[PROMPT] " + prompt + " (range: " + min + "-" + max + ")");
        if (intInputIndex < intInputs.size()) {
            int value = intInputs.get(intInputIndex++);
            // Validate the value is in range
            if (value >= min && value <= max) {
                return value;
            }
            throw new IllegalArgumentException(
                "Configured input " + value + " is out of range [" + min + ", " + max + "]");
        }
        throw new IllegalStateException("No more integer inputs configured for testing");
    }

    @Override
    public void printSuccess(String message) {
        successMessages.add(message);
        outputs.add("[SUCCESS] " + message);
    }

    @Override
    public void printError(String message) {
        errorMessages.add(message);
        outputs.add("[ERROR] " + message);
    }

    @Override
    public void print(String message) {
        outputs.add(message);
    }

    @Override
    public void println(String message) {
        outputs.add(message);
    }
}
