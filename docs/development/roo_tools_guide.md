# Roo Tools Guide for Batfish

This document provides guidance for Roo AI assistant tools when working with the Batfish project, particularly focusing on build and test operations.

## Build System

**IMPORTANT: Batfish uses Bazel, NOT Maven, as its build system.**

All build and test operations must use Bazel commands, not Maven (`mvn`) commands. Maven commands will not work with this project.

## Running Tests

### Preferred Testing Workflow

When implementing new features or fixing bugs, follow this testing workflow:

1. **First, run the specific test** that verifies your changes:

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

### Bazel Test Commands

```bash
# Run a specific test
bazel test //projects/coordinator:coordinator_tests

# Run a specific test method
bazel test --test_filter=org.batfish.coordinator.WorkMgrServiceTest#getNonExistNetwork$ -- //projects/coordinator:coordinator_tests

# Run tests in a specific package
bazel test //projects/coordinator/...

# Run all tests
bazel test //...
```

### Incorrect Way (DO NOT USE)

The following approaches will not work and should never be used:

```bash
# DO NOT USE - Maven is not used in this project
mvn test

# DO NOT USE - cd + mvn pattern is not applicable
cd some/directory && mvn test
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

### Running the Service

To run the Batfish service:

```bash
# Simple way
tools/bazel_run.sh

# Advanced way
bazel run //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
```

## Guidelines for Roo Tools

When using Roo tools with Batfish:

1. **Always use Bazel commands** for building, testing, and running the project
2. **Never suggest Maven commands** as they will not work
3. **Use the correct Bazel target syntax** (`//projects/...`) when referring to specific components
4. **Always run specific tests first** before running broader test suites
5. **Reference this guide** when unsure about the correct build or test command

## Additional Resources

For more detailed information about building and testing Batfish, refer to:

- [Building and Running Guide](../building_and_running/README.md)
- [Testing Guide](testing_guide.md)
