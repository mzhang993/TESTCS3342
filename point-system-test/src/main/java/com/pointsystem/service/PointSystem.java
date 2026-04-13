package com.pointsystem.service;

import com.pointsystem.ui.ConsoleUI;

import java.sql.*;

/**
 * Points Redemption System
 *
 * This class implements a points management system with the following features:
 * - Point management (add/deduct)
 * - Item redemption
 * - Admin/User role separation
 * - Transaction logging
 *
 * Testability Improvements:
 * 1. Dependency Injection: ConsoleUI is injected via constructor
 * 2. Database Abstraction: Connection can be injected for testing
 * 3. Test Stubs: ConsoleUI interface allows fake implementations
 * 4. Isolated Testing: No hard-coded dependencies
 */
public class PointSystem {

    // ==================== Database Configuration ====================
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/point_system?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "password";

    // ==================== Member Variables ====================
    private Connection connection;
    private final String currentUserId;
    private final boolean isAdmin;
    private final ConsoleUI consoleUI;
    private static final String ADMIN_ID = "System Administrator";

    // ==================== Constructors ====================

    /**
     * Constructor with ConsoleUI injection - RECOMMENDED for testing
     * This constructor follows the dependency injection pattern
     * allowing test stubs to be passed in.
     *
     * @param userId User ID ("System Administrator" for admin)
     * @param consoleUI ConsoleUI implementation (can be a test stub)
     * @param dbConnection Database connection (null for default connection)
     */
    public PointSystem(String userId, ConsoleUI consoleUI, Connection dbConnection) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (consoleUI == null) {
            throw new IllegalArgumentException("ConsoleUI cannot be null");
        }
        this.currentUserId = userId.trim();
        this.isAdmin = ADMIN_ID.equals(this.currentUserId);
        this.consoleUI = consoleUI;

        if (dbConnection != null) {
            this.connection = dbConnection;
        } else {
            initConnection();
        }

        initDatabase();
        ensureUserExists(this.currentUserId);
    }

    /**
     * Constructor for production use with default database
     */
    public PointSystem(String userId, ConsoleUI consoleUI) throws SQLException {
        this(userId, consoleUI, null);
    }

    // ==================== Initialization Methods ====================

    private void initConnection() throws SQLException {
        try {
            // Try MySQL first
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DEFAULT_DB_URL, DEFAULT_DB_USER, DEFAULT_DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            // Fall back to H2 for testing
            try {
                Class.forName("org.h2.Driver");
                this.connection = DriverManager.getConnection(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
            } catch (ClassNotFoundException ex) {
                throw new SQLException("No database driver available", ex);
            }
        }
    }

    /**
     * Initialize database tables
     */
    private void initDatabase() throws SQLException {
        String[] createTableSQLs = {
            // User points table
            "CREATE TABLE IF NOT EXISTS user_points (" +
                "user_id VARCHAR(50) PRIMARY KEY," +
                "points INT NOT NULL DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")",

            // Point reference table
            "CREATE TABLE IF NOT EXISTS point_reference (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL UNIQUE," +
                "points INT NOT NULL," +
                "description VARCHAR(255)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",

            // Redeemable items table
            "CREATE TABLE IF NOT EXISTS redeemable_items (" +
                "item_id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "points_required INT NOT NULL," +
                "stock INT NOT NULL DEFAULT 0," +
                "status TINYINT NOT NULL DEFAULT 1," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")",

            // Point transaction records table
            "CREATE TABLE IF NOT EXISTS point_transactions (" +
                "transaction_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id VARCHAR(50) NOT NULL," +
                "change_amount INT NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "related_item_id INT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",

            // Item transaction records table
            "CREATE TABLE IF NOT EXISTS item_transactions (" +
                "transaction_id INT AUTO_INCREMENT PRIMARY KEY," +
                "item_id INT NOT NULL," +
                "change_amount INT NOT NULL," +
                "type VARCHAR(10) NOT NULL," +
                "user_id VARCHAR(50)," +
                "admin_id VARCHAR(50)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : createTableSQLs) {
                stmt.execute(sql);
            }
        }
    }

    private void ensureUserExists(String userId) throws SQLException {
        String sql = "MERGE INTO user_points (user_id, points) KEY(user_id) VALUES (?, 0)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    // ==================== Core API Methods ====================

    /**
     * Get user's current points
     * @param userId User ID
     * @return Current points
     */
    public int getUserPoints(String userId) {
        String sql = "SELECT points FROM user_points WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("points");
            }
        } catch (SQLException e) {
            consoleUI.printError("Database error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Change points by reason name
     * @param targetUserId Target user
     * @param reasonName Reason name from point_reference
     * @return true if successful
     */
    public boolean changePointsByReason(String targetUserId, String reasonName) {
        String sql = "SELECT points, description FROM point_reference WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reasonName);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                consoleUI.printError("Point reason '" + reasonName + "' not found");
                return false;
            }

            int points = rs.getInt("points");
            String desc = rs.getString("description");
            String reason = (desc != null && !desc.isEmpty()) ? desc : reasonName;

            return changePoints(targetUserId, points, reason);

        } catch (SQLException e) {
            consoleUI.printError("Database error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Admin: Change points directly
     * @param targetUserId Target user
     * @param changeAmount Amount (positive/negative)
     * @param reason Reason for change
     * @return true if successful
     */
    public boolean adminChangePoints(String targetUserId, int changeAmount, String reason) {
        if (!isAdmin) {
            consoleUI.printError("Permission denied: Admin only");
            return false;
        }
        return changePoints(targetUserId, changeAmount, reason);
    }

    /**
     * Redeem an item
     * @param itemId Item ID to redeem
     * @return true if successful
     */
    public boolean redeemItem(int itemId) {
        String itemSql = "SELECT * FROM redeemable_items WHERE item_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(itemSql)) {
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                consoleUI.printError("Item not found");
                return false;
            }

            if (rs.getInt("status") != 1) {
                consoleUI.printError("Item not available for redemption");
                return false;
            }

            int stock = rs.getInt("stock");
            if (stock <= 0) {
                consoleUI.printError("Insufficient stock");
                return false;
            }

            int pointsRequired = rs.getInt("points_required");
            String itemName = rs.getString("name");

            int userPoints = getUserPoints(currentUserId);
            if (userPoints < pointsRequired) {
                consoleUI.printError("Insufficient points. Required: " + pointsRequired + ", Have: " + userPoints);
                return false;
            }

            // Execute transaction
            connection.setAutoCommit(false);
            try {
                // Deduct points
                String updateUser = "UPDATE user_points SET points = points - ? WHERE user_id = ?";
                try (PreparedStatement ups = connection.prepareStatement(updateUser)) {
                    ups.setInt(1, pointsRequired);
                    ups.setString(2, currentUserId);
                    ups.executeUpdate();
                }

                // Deduct stock
                String updateStock = "UPDATE redeemable_items SET stock = stock - 1 WHERE item_id = ?";
                try (PreparedStatement ups = connection.prepareStatement(updateStock)) {
                    ups.setInt(1, itemId);
                    ups.executeUpdate();
                }

                // Record point transaction
                String transSql = "INSERT INTO point_transactions (user_id, change_amount, reason, related_item_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement tps = connection.prepareStatement(transSql)) {
                    tps.setString(1, currentUserId);
                    tps.setInt(2, -pointsRequired);
                    tps.setString(3, "Redeemed: " + itemName);
                    tps.setInt(4, itemId);
                    tps.executeUpdate();
                }

                // Record item transaction
                String itemTransSql = "INSERT INTO item_transactions (item_id, change_amount, type, user_id) VALUES (?, ?, 'REDEEM', ?)";
                try (PreparedStatement itps = connection.prepareStatement(itemTransSql)) {
                    itps.setInt(1, itemId);
                    itps.setInt(2, -1);
                    itps.setString(3, currentUserId);
                    itps.executeUpdate();
                }

                connection.commit();
                consoleUI.printSuccess("Redeemed " + itemName + " for " + pointsRequired + " points");
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            consoleUI.printError("Redemption failed: " + e.getMessage());
            return false;
        }
    }

    // ==================== Admin Methods ====================

    /**
     * Admin: Add a redeemable item
     */
    public boolean addItem(String name, int pointsRequired, int stock) {
        if (!isAdmin) {
            consoleUI.printError("Permission denied");
            return false;
        }
        String sql = "INSERT INTO redeemable_items (name, points_required, stock) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, pointsRequired);
            ps.setInt(3, stock);
            ps.executeUpdate();
            consoleUI.printSuccess("Item added: " + name);
            return true;
        } catch (SQLException e) {
            consoleUI.printError("Add failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Admin: Add a point reference
     */
    public boolean addPointReference(String name, int points, String description) {
        if (!isAdmin) {
            consoleUI.printError("Permission denied");
            return false;
        }
        String sql = "INSERT INTO point_reference (name, points, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, points);
            ps.setString(3, description);
            ps.executeUpdate();
            consoleUI.printSuccess("Point reference added: " + name);
            return true;
        } catch (SQLException e) {
            consoleUI.printError("Add failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Admin: Restock an item
     */
    public boolean restockItem(int itemId, int amount) {
        if (!isAdmin) {
            consoleUI.printError("Permission denied");
            return false;
        }
        if (amount <= 0) {
            consoleUI.printError("Amount must be positive");
            return false;
        }
        try {
            String updateSql = "UPDATE redeemable_items SET stock = stock + ? WHERE item_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setInt(1, amount);
                ps.setInt(2, itemId);
                ps.executeUpdate();
            }
            String transSql = "INSERT INTO item_transactions (item_id, change_amount, type, admin_id) VALUES (?, ?, 'RESTOCK', ?)";
            try (PreparedStatement ps = connection.prepareStatement(transSql)) {
                ps.setInt(1, itemId);
                ps.setInt(2, amount);
                ps.setString(3, currentUserId);
                ps.executeUpdate();
            }
            consoleUI.printSuccess("Restocked item " + itemId + " with " + amount + " units");
            return true;
        } catch (SQLException e) {
            consoleUI.printError("Restock failed: " + e.getMessage());
            return false;
        }
    }

    // ==================== Query Methods ====================

    public String getAllItems() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM redeemable_items ORDER BY item_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            sb.append("=== Items ===\n");
            while (rs.next()) {
                String status = rs.getInt("status") == 1 ? "Available" : "Stopped";
                sb.append(String.format("[%d] %s | %d pts | Stock: %d | %s\n",
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getInt("points_required"),
                    rs.getInt("stock"),
                    status));
            }
        } catch (SQLException e) {
            return "Query failed: " + e.getMessage();
        }
        return sb.toString();
    }

    public String getAllPointReferences() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM point_reference ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            sb.append("=== Point References ===\n");
            while (rs.next()) {
                sb.append(String.format("[%d] %s | %+d pts | %s\n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("points"),
                    rs.getString("description")));
            }
        } catch (SQLException e) {
            return "Query failed: " + e.getMessage();
        }
        return sb.toString();
    }

    // ==================== Helper Methods ====================

    private boolean changePoints(String userId, int amount, String reason) {
        ensureUserExists(userId);
        if (amount < 0) {
            int currentPoints = getUserPoints(userId);
            if (currentPoints + amount < 0) {
                consoleUI.printError("Insufficient points for deduction");
                return false;
            }
        }
        try {
            String updateSql = "UPDATE user_points SET points = points + ? WHERE user_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setInt(1, amount);
                ps.setString(2, userId);
                ps.executeUpdate();
            }
            String transSql = "INSERT INTO point_transactions (user_id, change_amount, reason) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(transSql)) {
                ps.setString(1, userId);
                ps.setInt(2, amount);
                ps.setString(3, reason);
                ps.executeUpdate();
            }
            consoleUI.printSuccess((amount >= 0 ? "Added " : "Deducted ") +
                Math.abs(amount) + " points for " + userId);
            return true;
        } catch (SQLException e) {
            consoleUI.printError("Failed to change points: " + e.getMessage());
            return false;
        }
    }

    // ==================== Getters ====================

    public String getCurrentUserId() {
        return currentUserId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public int getCurrentUserPoints() {
        return getUserPoints(currentUserId);
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // Ignore
        }
    }
}
