# Batfish Build System Guidelines

## Build System

**IMPORTANT: Batfish uses Bazel, NOT Maven, as its build system.**

All build and test operations must use Bazel commands, not Maven (`mvn`) commands. Maven commands will not work with this project.

## Running Tests

### Correct Way (Using Bazel)

To run tests in Batfish, always use Bazel commands:

```bash
# Run all tests
bazel test //...

# Run tests in a specific package
bazel test //projects/coordinator/...

# Run a specific test
bazel test //projects/batfish:pmd

# Run a specific test method
bazel test --test_filter=org.batfish.coordinator.WorkMgrServiceTest#getNonExistNetwork$ -- //projects/coordinator:coordinator_tests
```

### Incorrect Way (DO NOT USE)

The following approaches will not work and should never be used:

```bash
# DO NOT USE - Maven is not used in this project
mvn test

# DO NOT USE - cd + mvn pattern is not applicable
cd some/directory && mvn test
```

## Test Execution Guidelines

1. **Always use Bazel commands** for running tests
2. **Never use Maven commands** as they will not work
3. **Use the correct Bazel target syntax** (`//projects/...`) when referring to specific tests

## Additional Resources

For more detailed information about testing Batfish, refer to:

- [Building and Running Guide](../docs/building_and_running/README.md)
- [Testing Guide](../docs/development/testing_guide.md)
