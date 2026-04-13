# Quick Start Guide - Running Tests

## Project Overview

This is a complete JUnit testing project for the Points Redemption System.

## How to Run Tests

### Method 1: Command Line (Maven)

```bash
# Navigate to project directory
cd point-system-test

# Compile the project
mvn clean compile

# Run all tests
mvn test

# Run with detailed output
mvn test -X

# Run specific test class
mvn test -Dtest=PointSystemTest

# Run specific test method
mvn test -Dtest=PointSystemTest#testAddPoints

# Generate test coverage report
mvn clean test jacoco:report
# Then open: target/site/jacoco/index.html
```

### Method 2: IntelliJ IDEA

1. **Open Project**: File → Open → Select `point-system-test` folder
2. **Wait for Maven Import**: IntelliJ will automatically download dependencies
3. **Run Tests**:
   - Navigate to `src/test/java/com/pointsystem/service/PointSystemTest.java`
   - Right-click on the class name
   - Select **Run 'PointSystemTest'**
4. **View Results**: Test results will appear in the Run window

### Method 3: Eclipse

1. **Import Project**: File → Import → Existing Maven Projects
2. **Select Root Directory**: Browse to `point-system-test` folder
3. **Finish**: Wait for project to build
4. **Run Tests**:
   - Navigate to `PointSystemTest.java`
   - Right-click → Run As → JUnit Test
5. **View Results**: JUnit view will show test results

### Method 4: VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Maven for Java
2. **Open Folder**: File → Open Folder → Select `point-system-test`
3. **Run Tests**:
   - Open `PointSystemTest.java`
   - Click "Run Test" above the class or method

## Running on GitHub Actions

### Setup Steps

1. **Create GitHub Repository**:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/point-system-test.git
   git push -u origin main
   ```

2. **Verify Workflow**:
   - Go to your GitHub repository
   - Click **Actions** tab
   - You should see "Java CI with Maven" workflow
   - It will run automatically on push

3. **View Results**:
   - Click on a workflow run
   - Expand "Build & Test" job
   - View test output in the logs

### Workflow Triggers

The CI automatically runs on:
- Push to `main`, `master`, or `develop` branches
- Pull requests to `main` or `master`

## Understanding Test Results

### Console Output (Maven)

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.pointsystem.service.PointSystemTest
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results :
[INFO] 
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

### Test Report Location

After running tests, find reports at:
```
target/surefire-reports/
├── com.pointsystem.service.PointSystemTest.txt
└── TEST-com.pointsystem.service.PointSystemTest.xml
```

## Test Cases Included

| # | Test Name | Description |
|---|-----------|-------------|
| 1 | `testConstructorNullUserId` | Rejects null user ID |
| 2 | `testConstructorEmptyUserId` | Rejects empty user ID |
| 3 | `testConstructorNullConsoleUI` | Rejects null ConsoleUI |
| 4 | `testAdminDetection` | Detects admin user |
| 5 | `testUserNotAdmin` | Regular user not admin |
| 6 | `testAddPoints` | Admin adds points |
| 7 | `testDeductPoints` | Admin deducts points |
| 8 | `testDeductTooManyPoints` | Prevents overdraw |
| 9 | `testUserCannotChangePoints` | User permission denied |
| 10 | `testChangePointsByReason` | Points by reason name |
| 11 | `testInvalidReasonName` | Invalid reason fails |
| 12 | `testNegativePointReference` | Negative points work |
| 13 | `testSuccessfulRedemption` | Redeem with points |
| 14 | `testInsufficientPointsRedemption` | Fail without points |
| 15 | `testNonExistentItemRedemption` | Item not found |
| 16 | `testAdminAddItem` | Admin adds item |
| 17 | `testUserCannotAddItem` | User can't add item |
| 18 | `testAdminRestock` | Admin restocks item |
| 19 | `testInvalidRestockAmount` | Invalid restock fails |
| 20 | `testAdminAddPointReference` | Admin adds reference |
| 21 | `testUserCannotAddPointReference` | User can't add reference |
| 22 | `testGetAllItems` | Query all items |
| 23 | `testGetAllPointReferences` | Query all references |
| 24 | `testAddDifferentPoints` | Parameterized: multiple values |
| 25 | `testInvalidUserIds` | Parameterized: invalid IDs |
| 26 | `testCompleteWorkflow` | Integration: full workflow |
| 27 | `testUserIsolation` | Integration: user isolation |

## Troubleshooting

### Maven Not Found

**Linux/Mac:**
```bash
sudo apt-get install maven        # Ubuntu/Debian
brew install maven                # Mac
```

**Windows:**
Download from https://maven.apache.org/download.cgi

### Java Version Issues

```bash
# Check Java version
java -version

# Should show Java 11 or higher
# If not, download from https://adoptium.net/
```

### Tests Fail on GitHub Actions

1. Check `.github/workflows/ci.yml` syntax
2. Ensure all files are committed: `git status`
3. Push all branches: `git push origin main`

## Key Concepts

| Concept | Implementation |
|---------|----------------|
| **JUnit Framework** | JUnit 5 with `@Test`, `@BeforeEach` |
| **assertEquals** | `assertEquals(expected, actual)` |
| **Test Stubs** | `TestConsoleUIStub` class |
| **Test Lifecycle** | `@BeforeAll`, `@BeforeEach`, `@AfterEach`, `@AfterAll` |
| **CI/CD** | GitHub Actions workflow |

## Next Steps

1. Run tests locally with `mvn test`
2. Push to GitHub
3. Verify CI/CD pipeline runs
4. Add more test cases as needed
5. Check code coverage with `mvn jacoco:report`

---

**Need Help?** Check `README.md` for detailed documentation.
