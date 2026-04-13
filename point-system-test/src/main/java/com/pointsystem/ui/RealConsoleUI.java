package com.pointsystem.ui;

import java.util.Scanner;

/**
 * RealConsoleUI - Production implementation of ConsoleUI
 *
 * This class provides the actual console I/O implementation for production use.
 * It uses System.in and System.out for real user interaction.
 *
 * Note: This implementation is NOT suitable for automated testing because:
 * 1. It requires actual user input
 * 2. Output goes directly to console
 * 3. Cannot be controlled or verified programmatically
 *
 * For testing, use TestConsoleUIStub instead.
 */
public class RealConsoleUI implements ConsoleUI {

    private final Scanner scanner;

    public RealConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    @Override
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("[ERROR] Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid input. Please enter a number.");
            }
        }
    }

    @Override
    public void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    @Override
    public void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println(String message) {
        System.out.println(message);
    }

    /**
     * Close the scanner resource
     */
    public void close() {
        scanner.close();
    }
}
