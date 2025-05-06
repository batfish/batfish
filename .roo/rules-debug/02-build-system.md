# Batfish Build System Guidelines for Debugging

## Build System

**IMPORTANT: Batfish uses Bazel, NOT Maven, as its build system.**

All build, test, and debugging operations must use Bazel commands, not Maven (`mvn`) commands. Maven commands will not work with this project.

## Running Tests for Debugging

### Correct Way (Using Bazel)

To run tests in Batfish for debugging purposes, always use Bazel commands:

```bash
# Run all tests with debug output
bazel test --test_output=all //...

# Run a specific test with debug output
bazel test --test_output=all //projects/batfish:pmd

# Run a specific test method with debug output
bazel test --test_output=all --test_filter=org.batfish.coordinator.WorkMgrServiceTest#getNonExistNetwork$ -- //projects/coordinator:coordinator_tests

# Run tests with Java assertions enabled
bazel test --jvmopt=-ea //...
```

### Incorrect Way (DO NOT USE)

The following approaches will not work and should never be used:

```bash
# DO NOT USE - Maven is not used in this project
mvn test

# DO NOT USE - cd + mvn pattern is not applicable
cd some/directory && mvn test
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

1. **Always use Bazel commands** for building, testing, and debugging the project
2. **Never use Maven commands** as they will not work
3. **Use the correct Bazel target syntax** (`//projects/...`) when referring to specific components
4. **Use appropriate debug flags** with Bazel commands for effective debugging

## Additional Resources

For more detailed information about debugging Batfish, refer to:

- [Building and Running Guide](../docs/building_and_running/README.md)
- [Testing Guide](../docs/development/testing_guide.md)
