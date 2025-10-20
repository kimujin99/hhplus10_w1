# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a TDD (Test-Driven Development) practice project for implementing a point management system. The project focuses on:
- TDD methodology following Red-Green-Refactor cycle
- Implementing point operations (charge, use, query, history)
- Concurrency control for point transactions
- Building test-first, production-ready code

## Build and Test Commands

```bash
# Build the project
./gradlew build

# Run tests (configured with ignoreFailures = true)
./gradlew test

# Run tests with coverage report (JaCoCo)
./gradlew test jacocoTestReport

# Run the Spring Boot application
./gradlew bootRun

# Clean build
./gradlew clean build
```

## Project Structure

```
src/main/java/io/hhplus/tdd/
├── TddApplication.java           # Spring Boot main application
├── ApiControllerAdvice.java      # Global exception handler
├── ErrorResponse.java            # Error response DTO
├── point/
│   ├── PointController.java      # REST API endpoints for point operations
│   ├── UserPoint.java            # Point data record
│   ├── PointHistory.java         # Transaction history record
│   └── TransactionType.java      # Enum: CHARGE, USE
└── database/
    ├── UserPointTable.java       # In-memory user point storage (DO NOT MODIFY)
    └── PointHistoryTable.java    # In-memory history storage (DO NOT MODIFY)
```

## Architecture and Key Constraints

### Database Layer (DO NOT MODIFY)
The `UserPointTable` and `PointHistoryTable` classes simulate database operations with intentional delays:
- `UserPointTable.selectById()`: ~0-200ms delay
- `UserPointTable.insertOrUpdate()`: ~0-300ms delay
- `PointHistoryTable.insert()`: ~0-300ms delay
- **Important**: Only use the public API methods provided by these classes
- These delays simulate real database latency and make concurrency issues more apparent

### Service Layer Design
When implementing business logic:
- Create service classes in the `point` package
- Services should handle:
  - Business validation (e.g., sufficient balance, valid amounts)
  - Concurrency control for point operations
  - Coordination between UserPointTable and PointHistoryTable
- Follow TDD: write tests first, then implement

### Controller Layer
The `PointController` has TODO markers for implementing:
- `GET /point/{id}` - Query user points
- `GET /point/{id}/histories` - Query transaction history
- `PATCH /point/{id}/charge` - Charge points
- `PATCH /point/{id}/use` - Use points

## Testing Requirements

Follow TDD principles:
1. Write failing test first (Red)
2. Write minimum code to pass (Green)
3. Refactor while keeping tests green (Refactor)

Required test coverage:
- Unit tests for all business logic
- Exception cases (insufficient balance, invalid amounts, etc.)
- Integration tests for API endpoints
- Concurrency tests for simultaneous operations

## Concurrency Control

Concurrency control is a critical requirement:
- Multiple threads may attempt to modify the same user's points simultaneously
- Without proper control, race conditions can lead to incorrect balances
- Implementation approach should be documented in README.md
- Consider locks, synchronized blocks, or other concurrency primitives

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Gradle with Kotlin DSL
- JaCoCo for code coverage
- JUnit 5 for testing
- Lombok for boilerplate reduction

## Development Workflow

When implementing features:
1. Start with unit tests for the smallest unit of work
2. Implement service layer logic to pass tests
3. Add integration tests for controller endpoints
4. Consider edge cases and exception scenarios
5. Add concurrency tests if relevant
6. Document concurrency approach in README.md

## PR Requirements

Based on `.github/pull_request_template.md`, PRs should include:
- TDD basics: implementation + tests, Red-Green-Refactor cycle
- Exception handling tests
- Integration tests
- Concurrency control implementation
- README.md documentation of concurrency approach
- Commit links for each feature/test
- Review points and retrospective
