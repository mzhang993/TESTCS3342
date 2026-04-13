package com.pointsystem.ui;

/**
 * ConsoleUI Interface - Abstraction for console input/output operations
 *
 * Design Purpose:
 * This interface abstracts console I/O operations to make the code testable.
 * By depending on this interface rather than directly using System.in/System.out,
 * we can create test stubs that simulate user input and capture output.
 *
 * Testability Benefits:
 * 1. Allows test stubs to provide controlled input
 * 2. Allows test stubs to capture and verify output
 * 3. Isolates the system under test from actual console operations
 */
public interface ConsoleUI {

    /**
     * Read a line of text from the user
     * @param prompt The prompt message to display
     * @return The user's input as a string
     */
    String readLine(String prompt);

    /**
     * Read an integer from the user with range validation
     * @param prompt The prompt message to display
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return The validated integer input
     */
    int readInt(String prompt, int min, int max);

    /**
     * Print a success message
     * @param message The success message to display
     */
    void printSuccess(String message);

    /**
     * Print an error message
     * @param message The error message to display
     */
    void printError(String message);

    /**
     * Print a regular message
     * @param message The message to display
     */
    void print(String message);

    /**
     * Print a message with newline
     * @param message The message to display
     */
    void println(String message);
}
