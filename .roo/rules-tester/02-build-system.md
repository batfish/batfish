# Batfish Build System Guidelines

## Build System

**IMPORTANT: Batfish uses Bazel, NOT Maven, as its build system.**

All build and test operations must use Bazel commands, not Maven (`mvn`) commands. Maven commands will not work with this project.

## Preferred Testing Workflow

When implementing new tests or verifying fixes, follow this testing workflow:

1. **ALWAYS run the specific test first** that directly verifies your changes:

   ```bash
   # Run a specific test
   bazel test //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest

   # Run a specific test method
   bazel test --test_filter=org.batfish.grammar.flatjuniper.JunosMplsAdminGroupTest#testAdminGroupDefinitions //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest
   ```

2. **Only after the specific test passes**, run related tests or the full test suite:

   ```bash
   # Run all tests in a package
   bazel test //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper/...

   # Run all tests
   bazel test //...
   ```

## Running Tests

### Correct Way (Using Bazel)

```bash
# Run a specific test (PREFERRED FIRST STEP)
bazel test //projects/coordinator:coordinator_tests

# Run a specific test method (PREFERRED FIRST STEP)
bazel test --test_filter=org.batfish.coordinator.WorkMgrServiceTest#getNonExistNetwork$ -- //projects/coordinator:coordinator_tests

# Run tests in a specific package (AFTER specific tests pass)
bazel test //projects/coordinator/...

# Run all tests (ONLY AFTER specific tests pass)
bazel test //...
```

### Incorrect Way (DO NOT USE)

The following approaches will not work and should never be used:

```bash
# DO NOT USE - Maven is not used in this project
mvn test

# DO NOT USE - cd + mvn pattern is not applicable
cd some/directory && mvn test

# DO NOT USE - Running all tests first is inefficient
bazel test //... && bazel test //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest
```

## Test Execution Guidelines

1. **ALWAYS run specific tests first** before running broader test suites
2. **Always use Bazel commands** for running tests
3. **Never use Maven commands** as they will not work
4. **Use the correct Bazel target syntax** (`//projects/...`) when referring to specific tests
5. **Reference the [Roo Tools Guide](../docs/development/roo_tools_guide.md)** when unsure about the correct test command

## Additional Resources

For more detailed information about testing Batfish, refer to:

- [Building and Running Guide](../docs/building_and_running/README.md)
- [Testing Guide](../docs/development/testing_guide.md)
