# Batfish Build System Guidelines for Debugging

## Build System

**IMPORTANT: Batfish uses Bazel, NOT Maven, as its build system.**

All build, test, and debugging operations must use Bazel commands, not Maven (`mvn`) commands. Maven commands will not work with this project.

## Preferred Debugging Workflow

When debugging issues, follow this testing workflow:

1. **ALWAYS run the specific test first** with debug output enabled:

   ```bash
   # Run a specific test with debug output
   bazel test --test_output=all //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest

   # Run a specific test method with debug output
   bazel test --test_output=all --test_filter=org.batfish.grammar.flatjuniper.JunosMplsAdminGroupTest#testAdminGroupDefinitions //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest
   ```

2. **Only after debugging the specific test**, run related tests if necessary:
   ```bash
   # Run all tests in a package with debug output
   bazel test --test_output=all //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper/...
   ```

## Running Tests for Debugging

### Correct Way (Using Bazel)

```bash
# Run a specific test with debug output (PREFERRED FIRST STEP)
bazel test --test_output=all //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest

# Run a specific test method with debug output (PREFERRED FIRST STEP)
bazel test --test_output=all --test_filter=org.batfish.grammar.flatjuniper.JunosMplsAdminGroupTest#testAdminGroupDefinitions //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest

# Run tests with Java assertions enabled
bazel test --jvmopt=-ea //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest
```

### Incorrect Way (DO NOT USE)

The following approaches will not work and should never be used:

```bash
# DO NOT USE - Maven is not used in this project
mvn test

# DO NOT USE - cd + mvn pattern is not applicable
cd some/directory && mvn test

# DO NOT USE - Running all tests first is inefficient
bazel test --test_output=all //... && bazel test --test_output=all //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:JunosMplsAdminGroupTest
```

## Debugging the Service

To run the Batfish service with debugging options:

```bash
# Run with Java assertions enabled
bazel run --jvmopt=-ea //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"

# Run with remote debugging enabled
bazel run --jvmopt=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
```

## Debugging Guidelines

1. **ALWAYS debug specific tests first** before running broader test suites
2. **Always use Bazel commands** for building, testing, and debugging the project
3. **Never use Maven commands** as they will not work
4. **Use the correct Bazel target syntax** (`//projects/...`) when referring to specific components
5. **Use appropriate debug flags** with Bazel commands for effective debugging
6. **Reference the [Roo Tools Guide](../docs/development/roo_tools_guide.md)** when unsure about the correct debug command

## Additional Resources

For more detailed information about debugging Batfish, refer to:

- [Building and Running Guide](../docs/building_and_running/README.md)
- [Testing Guide](../docs/development/testing_guide.md)
