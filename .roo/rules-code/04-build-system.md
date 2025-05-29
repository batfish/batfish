# Batfish Build System Guidelines

## Build System

**IMPORTANT: Batfish uses Bazel, NOT Maven, as its build system.**

All build and test operations must use Bazel commands, not Maven (`mvn`) commands. Maven commands will not work with this project.

## Preferred Testing Workflow

When implementing new features or fixing bugs, follow this testing workflow:

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

## Building the Project

### Correct Way (Using Bazel)

To build Batfish, use Bazel commands:

```bash
# Build the entire project
bazel build //...

# Build a specific target
bazel build //projects/allinone:allinone_main
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

## Running the Service

To run the Batfish service:

```bash
# Simple way
tools/bazel_run.sh

# Advanced way
bazel run //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
```

## Implementation Guidelines

1. **ALWAYS run specific tests first** before running broader test suites
2. **Always use Bazel commands** for building, testing, and running the project
3. **Use the correct Bazel target syntax** (`//projects/...`) when referring to specific components
4. **Reference the [Roo Tools Guide](../docs/development/roo_tools_guide.md)** when unsure about the correct build or test command

## Additional Resources

For more detailed information about building and testing Batfish, refer to:

- [Building and Running Guide](../docs/building_and_running/README.md)
- [Testing Guide](../docs/development/testing_guide.md)
