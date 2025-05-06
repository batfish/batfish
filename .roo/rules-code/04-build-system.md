# Batfish Build System Guidelines

## Build System

**IMPORTANT: Batfish uses Bazel, NOT Maven, as its build system.**

All build and test operations must use Bazel commands, not Maven (`mvn`) commands. Maven commands will not work with this project.

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
mvn compile
mvn package

# DO NOT USE - cd + mvn pattern is not applicable
cd some/directory && mvn test
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

1. **Always use Bazel commands** for building, testing, and running the project
2. **Never suggest Maven commands** as they will not work
3. **Use the correct Bazel target syntax** (`//projects/...`) when referring to specific components

## Additional Resources

For more detailed information about building and testing Batfish, refer to:

- [Building and Running Guide](../docs/building_and_running/README.md)
- [Testing Guide](../docs/development/testing_guide.md)
