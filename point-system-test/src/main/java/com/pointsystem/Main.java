package com.pointsystem;

import com.pointsystem.service.PointSystem;
import com.pointsystem.ui.RealConsoleUI;

import java.sql.SQLException;

/**
 * Main Application Entry Point
 *
 * This class demonstrates the PointSystem in production mode.
 * It uses RealConsoleUI for actual user interaction.
 *
 * For testing, see: PointSystemTest.java
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         Points Redemption System - Demo Application          ║");
        System.out.println("║                                                              ║");
        System.out.println("║  Note: This requires MySQL database to be running.           ║");
        System.out.println("║        For testing, use PointSystemTest with H2 database.    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Create real console UI for production
        RealConsoleUI consoleUI = new RealConsoleUI();

        try {
            // Create system as admin
            PointSystem adminSystem = new PointSystem("System Administrator", consoleUI);

            System.out.println("Current User: " + adminSystem.getCurrentUserId());
            System.out.println("Is Admin: " + adminSystem.isAdmin());
            System.out.println();

            // Display all point references
            System.out.println(adminSystem.getAllPointReferences());
            System.out.println();

            // Display all items
            System.out.println(adminSystem.getAllItems());
            System.out.println();

            // Example: Add a new point reference
            adminSystem.addPointReference("DEMO_BONUS", 500, "Demo bonus points");

            // Example: Add a new item
            adminSystem.addItem("Demo Item", 300, 50);

            // Display updated lists
            System.out.println("\n=== After adding demo data ===\n");
            System.out.println(adminSystem.getAllPointReferences());
            System.out.println();
            System.out.println(adminSystem.getAllItems());

            // Cleanup
            adminSystem.close();
            consoleUI.close();

            System.out.println("\nDemo completed successfully!");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            System.err.println("\nPlease ensure:");
            System.err.println("1. MySQL is running");
            System.err.println("2. Database 'point_system' exists");
            System.err.println("3. Credentials are correct");
            System.err.println("\nFor testing without MySQL, run: mvn test");
        }
    }
}
