# Point System - JUnit Test Project

[![CI — Build & Test](https://github.com/YOUR_USERNAME/point-system-test/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/point-system-test/actions/workflows/ci.yml)

A Points Redemption System with JUnit 5 tests running on GitHub Actions.

## Project Structure

```
point-system-test/
├── .github/workflows/
│   ├── ci.yml          # CI: Build & Test
│   └── release.yml     # Release: Build JAR
├── src/
│   ├── main/java/com/pointsystem/
│   │   ├── Main.java
│   │   ├── service/PointSystem.java
│   │   └── ui/ConsoleUI.java, RealConsoleUI.java
│   └── test/java/com/pointsystem/
│       ├── service/PointSystemTest.java (27 tests)
│       └── ui/TestConsoleUIStub.java
└── pom.xml
```

## Quick Start

### Prerequisites
- Java 17
- Maven 3.6+

### Run Tests Locally

```bash
# Compile and run tests
mvn -B verify

# Run specific test
mvn test -Dtest=PointSystemTest#testAddPoints
```

### Build JAR (skip tests)

```bash
mvn -B -DskipTests package
```

## GitHub Actions

| Workflow | Trigger | Command |
|----------|---------|---------|
| **CI** | Push/PR to `main` | `mvn -B verify` |
| **Release** | Push to `main` or manual | `mvn -B -DskipTests package` |

## Test Cases (27 total)

| Category | Tests |
|----------|-------|
| Constructor | 3 |
| Admin Detection | 2 |
| Point Management | 4 |
| Point References | 3 |
| Item Redemption | 3 |
| Item Management | 3 |
| Query Methods | 2 |
| Parameterized | 2 |
| Integration | 3 |

## License

For educational purposes.
