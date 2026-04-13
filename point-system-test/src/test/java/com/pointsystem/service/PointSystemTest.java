package com.pointsystem.service;

import com.pointsystem.ui.TestConsoleUIStub;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PointSystem JUnit Test Class
 *
 * This test class contains 27 test cases for the Points Redemption System.
 * Tests cover constructor validation, admin detection, point management,
 * item redemption, and integration scenarios.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PointSystemTest {

    // Test fixtures
    private static Connection testConnection;
    private TestConsoleUIStub consoleStub;
    private PointSystem adminSystem;
    private PointSystem userSystem;

    // Test constants
    private static final String ADMIN_ID = "System Administrator";
    private static final String TEST_USER = "test_user";
    private static final String TEST_USER2 = "test_user2";

    /**
     * Setup test environment for all tests
     */
    @BeforeAll
    public void setUpBeforeClass() throws SQLException {
        // Use H2 in-memory database for testing
        testConnection = DriverManager.getConnection(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
    }

    /**
     * Setup test fixture before each test case
     */
    @BeforeEach
    public void setUp() throws SQLException {
        // Create fresh test stub for each test
        consoleStub = new TestConsoleUIStub();

        // Create admin system for setup operations
        adminSystem = new PointSystem(ADMIN_ID, consoleStub, testConnection);

        // Create regular user system for user tests
        userSystem = new PointSystem(TEST_USER, consoleStub, testConnection);

        // Clear any existing data and set up fresh test data
        setupTestData();
    }

    /**
     * Helper method to setup test data
     */
    private void setupTestData() {
        // Add point references
        adminSystem.addPointReference("TEST_BONUS", 100, "Test bonus points");
        adminSystem.addPointReference("TEST_PENALTY", -50, "Test penalty");
        adminSystem.addPointReference("RECYCLE", 200, "Recycle garbage");

        // Add test items
        adminSystem.addItem("Test Item 1", 100, 10);
        adminSystem.addItem("Test Item 2", 200, 5);

        // Clear outputs from setup
        consoleStub.clearOutputs();
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor should reject null userId")
    public void testConstructorNullUserId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PointSystem(null, consoleStub, testConnection);
        });
        assertEquals("User ID cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor should reject empty userId")
    public void testConstructorEmptyUserId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PointSystem("   ", consoleStub, testConnection);
        });
        assertEquals("User ID cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor should reject null ConsoleUI")
    public void testConstructorNullConsoleUI() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PointSystem(TEST_USER, null, testConnection);
        });
        assertEquals("ConsoleUI cannot be null", exception.getMessage());
    }

    // ==================== Admin Detection Tests ====================

    @Test
    @DisplayName("System Administrator should have admin privileges")
    public void testAdminDetection() {
        assertTrue(adminSystem.isAdmin(), "System Administrator should be admin");
        assertEquals(ADMIN_ID, adminSystem.getCurrentUserId());
    }

    @Test
    @DisplayName("Regular user should not have admin privileges")
    public void testUserNotAdmin() {
        assertFalse(userSystem.isAdmin(), "Regular user should not be admin");
        assertEquals(TEST_USER, userSystem.getCurrentUserId());
    }

    // ==================== Point Management Tests ====================

    @Test
    @DisplayName("Admin should be able to add points to user")
    public void testAddPoints() {
        // Initial points should be 0
        assertEquals(0, adminSystem.getUserPoints(TEST_USER), "Initial points should be 0");

        // Add 100 points
        boolean result = adminSystem.adminChangePoints(TEST_USER, 100, "Test add");

        // Verify success
        assertTrue(result, "Add points should succeed");
        assertEquals(100, adminSystem.getUserPoints(TEST_USER), "Points should be 100 after addition");
        assertTrue(consoleStub.containsSuccess("Added 100 points"));
    }

    @Test
    @DisplayName("Admin should be able to deduct points from user")
    public void testDeductPoints() {
        // Setup: Add initial points
        adminSystem.adminChangePoints(TEST_USER, 200, "Initial");
        assertEquals(200, adminSystem.getUserPoints(TEST_USER));

        // Deduct 50 points
        boolean result = adminSystem.adminChangePoints(TEST_USER, -50, "Test deduct");

        // Verify
        assertTrue(result, "Deduct points should succeed");
        assertEquals(150, adminSystem.getUserPoints(TEST_USER), "Points should be 150 after deduction");
    }

    @Test
    @DisplayName("Should not allow deduction exceeding user's points")
    public void testDeductTooManyPoints() {
        // User has 0 points
        assertEquals(0, userSystem.getCurrentUserPoints());

        // Try to deduct 100 points
        boolean result = adminSystem.adminChangePoints(TEST_USER, -100, "Overdraw");

        // Should fail
        assertFalse(result, "Should not allow overdraw");
        assertTrue(consoleStub.containsError("Insufficient points"));
        assertEquals(0, userSystem.getCurrentUserPoints(), "Points should remain 0");
    }

    @Test
    @DisplayName("Regular user should not be able to change points")
    public void testUserCannotChangePoints() {
        boolean result = userSystem.adminChangePoints(TEST_USER2, 100, "Unauthorized");

        assertFalse(result, "User should not be able to change points");
        assertTrue(consoleStub.containsError("Permission denied"));
    }

    // ==================== Point Reference Tests ====================

    @Test
    @DisplayName("Should change points using point reference name")
    public void testChangePointsByReason() {
        // Add points using reason name
        boolean result = adminSystem.changePointsByReason(TEST_USER, "TEST_BONUS");

        assertTrue(result, "Should succeed with valid reason");
        assertEquals(100, userSystem.getCurrentUserPoints(), "Should add 100 points");
    }

    @Test
    @DisplayName("Should fail with invalid point reference name")
    public void testInvalidReasonName() {
        boolean result = adminSystem.changePointsByReason(TEST_USER, "INVALID_REASON");

        assertFalse(result, "Should fail with invalid reason");
        assertTrue(consoleStub.containsError("not found"));
    }

    @Test
    @DisplayName("Negative point reference should deduct points")
    public void testNegativePointReference() {
        // Setup: Give user initial points
        adminSystem.adminChangePoints(TEST_USER, 200, "Initial");

        // Apply penalty
        boolean result = adminSystem.changePointsByReason(TEST_USER, "TEST_PENALTY");

        assertTrue(result, "Should succeed");
        assertEquals(150, userSystem.getCurrentUserPoints(), "Should deduct 50 points");
    }

    // ==================== Item Redemption Tests ====================

    @Test
    @DisplayName("User should be able to redeem item with sufficient points")
    public void testSuccessfulRedemption() {
        // Setup: Give user enough points
        adminSystem.adminChangePoints(TEST_USER, 200, "Initial");
        consoleStub.clearOutputs();

        // Redeem item (item 1 costs 100 points)
        boolean result = userSystem.redeemItem(1);

        assertTrue(result, "Redemption should succeed");
        assertEquals(100, userSystem.getCurrentUserPoints(), "Should deduct 100 points");
        assertTrue(consoleStub.containsSuccess("Redeemed"));
    }

    @Test
    @DisplayName("Should fail redemption with insufficient points")
    public void testInsufficientPointsRedemption() {
        // User has 0 points, item costs 100
        boolean result = userSystem.redeemItem(1);

        assertFalse(result, "Should fail with insufficient points");
        assertTrue(consoleStub.containsError("Insufficient points"));
    }

    @Test
    @DisplayName("Should fail redemption of non-existent item")
    public void testNonExistentItemRedemption() {
        // Give user points
        adminSystem.adminChangePoints(TEST_USER, 1000, "Rich");
        consoleStub.clearOutputs();

        // Try to redeem item 999 (doesn't exist)
        boolean result = userSystem.redeemItem(999);

        assertFalse(result, "Should fail for non-existent item");
        assertTrue(consoleStub.containsError("Item not found"));
    }

    // ==================== Item Management Tests ====================

    @Test
    @DisplayName("Admin should be able to add new item")
    public void testAdminAddItem() {
        boolean result = adminSystem.addItem("New Test Item", 300, 20);

        assertTrue(result, "Admin should be able to add item");
        assertTrue(consoleStub.containsSuccess("Item added"));
        assertTrue(adminSystem.getAllItems().contains("New Test Item"));
    }

    @Test
    @DisplayName("Regular user should not be able to add item")
    public void testUserCannotAddItem() {
        boolean result = userSystem.addItem("Hacked Item", 1, 999);

        assertFalse(result, "User should not be able to add item");
        assertTrue(consoleStub.containsError("Permission denied"));
    }

    @Test
    @DisplayName("Admin should be able to restock item")
    public void testAdminRestock() {
        boolean result = adminSystem.restockItem(1, 50);

        assertTrue(result, "Admin should be able to restock");
        assertTrue(consoleStub.containsSuccess("Restocked"));
    }

    @Test
    @DisplayName("Should fail restock with non-positive amount")
    public void testInvalidRestockAmount() {
        boolean result = adminSystem.restockItem(1, 0);

        assertFalse(result, "Should fail with zero amount");
        assertTrue(consoleStub.containsError("must be positive"));
    }

    // ==================== Point Reference Management Tests ====================

    @Test
    @DisplayName("Admin should be able to add point reference")
    public void testAdminAddPointReference() {
        boolean result = adminSystem.addPointReference("NEW_REF", 500, "New reference");

        assertTrue(result, "Admin should be able to add reference");
        assertTrue(consoleStub.containsSuccess("Point reference added"));
        assertTrue(adminSystem.getAllPointReferences().contains("NEW_REF"));
    }

    @Test
    @DisplayName("Regular user should not be able to add point reference")
    public void testUserCannotAddPointReference() {
        boolean result = userSystem.addPointReference("HACKED", 9999, "Hack");

        assertFalse(result, "User should not be able to add reference");
        assertTrue(consoleStub.containsError("Permission denied"));
    }

    // ==================== Query Tests ====================

    @Test
    @DisplayName("getAllItems should return formatted item list")
    public void testGetAllItems() {
        String items = adminSystem.getAllItems();

        assertNotNull(items);
        assertTrue(items.contains("Test Item 1"));
        assertTrue(items.contains("Test Item 2"));
        assertTrue(items.contains("pts")); // Points indicator
    }

    @Test
    @DisplayName("getAllPointReferences should return formatted reference list")
    public void testGetAllPointReferences() {
        String refs = adminSystem.getAllPointReferences();

        assertNotNull(refs);
        assertTrue(refs.contains("TEST_BONUS"));
        assertTrue(refs.contains("TEST_PENALTY"));
        assertTrue(refs.contains("pts")); // Points indicator
    }

    // ==================== Parameterized Tests ====================

    @ParameterizedTest
    @CsvSource({
        "50, 50",     // Add 50, expect 50
        "100, 100",   // Add 100, expect 100
        "200, 200",   // Add 200, expect 200
        "0, 0"        // Add 0, expect 0
    })
    @DisplayName("Parameterized test for adding different point amounts")
    public void testAddDifferentPoints(int amount, int expected) {
        adminSystem.adminChangePoints(TEST_USER, amount, "Test");
        assertEquals(expected, userSystem.getCurrentUserPoints());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Various empty/whitespace user IDs should be rejected")
    public void testInvalidUserIds(String invalidUserId) {
        assertThrows(IllegalArgumentException.class, () -> {
            new PointSystem(invalidUserId, consoleStub, testConnection);
        });
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Complete workflow: earn points and redeem item")
    public void testCompleteWorkflow() {
        // Step 1: User starts with 0 points
        assertEquals(0, userSystem.getCurrentUserPoints());

        // Step 2: Earn points through activity
        adminSystem.changePointsByReason(TEST_USER, "RECYCLE"); // +200 points
        assertEquals(200, userSystem.getCurrentUserPoints());

        // Step 3: Redeem item worth 100 points
        boolean redeemResult = userSystem.redeemItem(1);
        assertTrue(redeemResult);
        assertEquals(100, userSystem.getCurrentUserPoints());

        // Step 4: Earn more points
        adminSystem.changePointsByReason(TEST_USER, "TEST_BONUS"); // +100 points
        assertEquals(200, userSystem.getCurrentUserPoints());

        // Step 5: Redeem higher value item
        redeemResult = userSystem.redeemItem(2); // 200 points
        assertTrue(redeemResult);
        assertEquals(0, userSystem.getCurrentUserPoints());
    }

    @Test
    @DisplayName("Multiple users should have isolated point balances")
    public void testUserIsolation() throws SQLException {
        // Create second user
        PointSystem user2System = new PointSystem(TEST_USER2, consoleStub, testConnection);

        // Add points to user1 only
        adminSystem.adminChangePoints(TEST_USER, 500, "To user1");

        // Verify isolation
        assertEquals(500, userSystem.getCurrentUserPoints(), "User1 should have 500");
        assertEquals(0, user2System.getCurrentUserPoints(), "User2 should have 0");

        user2System.close();
    }

    // ==================== Test Lifecycle Cleanup ====================

    @AfterEach
    public void tearDown() {
        // Clear outputs
        if (consoleStub != null) {
            consoleStub.clearOutputs();
        }
    }

    @AfterAll
    public void tearDownAfterClass() {
        // Close systems
        if (adminSystem != null) {
            adminSystem.close();
        }
        if (userSystem != null) {
            userSystem.close();
        }
        // Close database connection
        try {
            if (testConnection != null && !testConnection.isClosed()) {
                testConnection.close();
            }
        } catch (SQLException e) {
            // Ignore
        }
    }
}
