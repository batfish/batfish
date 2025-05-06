# Batfish Testing Guide

This document provides guidelines and best practices for testing Batfish code.

## Testing Philosophy

Batfish follows a comprehensive testing approach that includes:

1. **Unit Tests**: Testing individual components in isolation
2. **Reference Tests**: Testing parsing and conversion against known-good outputs

Our testing philosophy emphasizes:

- Test-driven development when appropriate
- Comprehensive test coverage
- Testing both normal cases and edge cases
- Maintaining test readability and maintainability
- Using the right type of test for each situation

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

This approach is more efficient and helps you focus on fixing the specific issue at hand before verifying broader compatibility.

## Unit Testing

### When to Write Unit Tests

- For all non-trivial methods and classes
- When fixing bugs (to prevent regression)
- When implementing new features
- When refactoring existing code

### Unit Test Best Practices

1. **Test One Thing**: Each test should focus on testing one specific behavior
2. **Use Descriptive Names**: Test method names should clearly describe what is being tested
3. **Follow AAA Pattern**: Arrange (setup), Act (execute), Assert (verify)
4. **Keep Tests Independent**: Tests should not depend on each other
5. **Mock Dependencies**: Use mocking frameworks to isolate the code under test
6. **Test Edge Cases**: Include tests for boundary conditions and error cases
7. **Keep Tests Fast**: Unit tests should execute quickly
8. **Make Tests Deterministic**: Tests should produce the same result every time they run

### Example Unit Test

From `PrefixTest.java`:

```java
@Test
public void testContainsIp() {
  Prefix p = Prefix.parse("192.0.2.0/24");
  assertTrue(p.containsIp(Ip.parse("192.0.2.1")));
  assertFalse(p.containsIp(Ip.parse("127.0.0.1")));
}
```

### Writing Effective Unit Tests

1. **Use Meaningful Test Names**: Name tests after the functionality being tested (e.g., `testContainsIp` instead of generic names like `testMethod1`).
2. **Write Direct, Readable Assertions**: Avoid unnecessary temporary variables when direct assertions are clearer.
3. **Clear Test Structure**: Follow the AAA pattern (Arrange-Act-Assert) with clear separation between sections.
4. **Self-Explanatory Tests**: A test should clearly communicate what it's testing without requiring additional documentation.

## Reference Testing

### How Reference Tests Work

Reference tests compare the output of a function (like parsing or conversion) against a known-good reference output. When the expected behavior changes, the reference files are updated.

1. **Input Files**: Configuration files or other inputs
2. **Reference Files**: Expected outputs stored in version control
3. **Test Logic**: Code that processes inputs and compares to reference outputs
4. **Update Mechanism**: Way to update reference files when behavior changes

### Reference Test Structure

Reference tests are located in the `//tests` directory, with subdirectories for different types of tests:

- `parsing-tests`: Tests for parsing various network configurations
- `parsing-errors-tests`: Tests for handling parsing errors
- `aws`: Tests for AWS-specific functionality
- `basic`: Basic functionality tests
- `questions`: Tests for Batfish questions

Each test directory typically contains:

- A `commands` file: Contains the test commands to execute
- `.ref` files: Reference output files that tests are compared against
- A `networks` directory: Contains input network configurations
- A `BUILD.bazel` file: Defines the tests using the `ref_tests` rule

### Running Reference Tests

To run a specific reference test:

```bash
bazel test //tests/parsing-tests:ref_tests
```

To run all reference tests (only after specific tests pass):

```bash
bazel test //tests/...
```

### Updating Reference Tests

When you make changes that intentionally modify the expected output of tests, you need to update the reference files. Batfish provides a script to automate this process:

```bash
./tools/update_refs.sh
```

This script:

1. Runs all tests in the `//tests` directory
2. If tests fail, patches the reference files using the test logs
3. Runs the tests again to verify they now pass
4. Warns if tests are non-deterministic

Always carefully review the changes to reference files after running this script to ensure they match your expectations.

### Reference Test Best Practices

1. **Version Control**: Store reference files in version control
2. **Clear Documentation**: Document what each reference test is testing
3. **Meaningful Diffs**: Structure reference files to produce meaningful diffs
4. **Selective Updates**: Update reference files only when behavior intentionally changes
5. **Review Changes**: Carefully review changes to reference files

## Test Coverage

### Coverage Goals

- Aim for high test coverage of business logic
- Focus on testing complex and critical code paths
- Don't obsess over 100% coverage
- Consider both line coverage and branch coverage
- Ensure all error handling paths are tested

### Measuring Coverage

- Use JaCoCo for Java code coverage
- Review coverage reports regularly
- Address coverage gaps in critical components
- Don't write tests just to increase coverage numbers

## Testing Tools

### JUnit

- Use JUnit 5 for new tests
- Use parameterized tests for testing multiple inputs
- Use test fixtures for common setup

### Hamcrest Matchers

- Use Hamcrest matchers for more readable assertions
- Create custom matchers for domain-specific assertions

### Mockito

- Use Mockito for mocking dependencies
- Use strict stubbing to catch unexpected interactions

## Continuous Integration

Batfish uses GitHub Actions for continuous integration. All tests are run on:

- Pull requests
- Merges to main branch
- Scheduled runs

### CI Best Practices

1. **Fix Failing Tests Promptly**: Address failing tests as soon as they're detected
2. **Don't Disable Tests**: Fix flaky tests rather than disabling them
3. **Keep the Build Fast**: Optimize tests to keep CI times reasonable
4. **Test on Multiple Platforms**: Test on all supported platforms
5. **Include Performance Tests**: Run performance tests to catch regressions
6. **Review Test Results**: Regularly review test results and coverage reports
7. **Don't Merge with Failing Tests**: Don't merge code that fails tests
