# Batfish Testing Guide

This document provides guidelines and best practices for testing Batfish code.

## Testing Philosophy

Batfish follows a comprehensive testing approach that includes:

1. **Unit Tests**: Testing individual components in isolation
2. **Integration Tests**: Testing interactions between components
3. **Reference Tests**: Testing parsing and conversion against known-good outputs
4. **End-to-End Tests**: Testing the complete system workflow

Our testing philosophy emphasizes:

- Test-driven development when appropriate
- Comprehensive test coverage
- Testing both normal cases and edge cases
- Maintaining test readability and maintainability
- Using the right type of test for each situation

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

```java
@Test
public void testIpAddressContainedIn() {
  // Arrange
  Ip ip = Ip.parse("192.0.2.1");
  Prefix prefix = Prefix.parse("192.0.2.0/24");
  Prefix nonContainingPrefix = Prefix.parse("198.51.100.0/24");

  // Act & Assert
  assertTrue("IP should be contained in prefix", prefix.containsIp(ip));
  assertFalse("IP should not be contained in non-containing prefix", nonContainingPrefix.containsIp(ip));
}
```

## Integration Testing

### When to Write Integration Tests

- When testing interactions between multiple components
- When testing database access
- When testing file I/O
- When testing network communication

### Integration Test Best Practices

1. **Minimize External Dependencies**: Use in-memory databases or mock servers when possible
2. **Clean Up After Tests**: Ensure tests clean up any resources they create
3. **Use Appropriate Fixtures**: Create reusable test fixtures for common setup
4. **Test Realistic Scenarios**: Design tests that reflect real-world usage
5. **Document Setup Requirements**: Clearly document any external setup needed

## Reference Testing

### How Reference Tests Work

Reference tests compare the output of a function (like parsing or conversion) against a known-good reference output. When the expected behavior changes, the reference files are updated.

1. **Input Files**: Configuration files or other inputs
2. **Reference Files**: Expected outputs stored in version control
3. **Test Logic**: Code that processes inputs and compares to reference outputs
4. **Update Mechanism**: Way to update reference files when behavior changes

### Reference Test Best Practices

1. **Version Control**: Store reference files in version control
2. **Clear Documentation**: Document what each reference test is testing
3. **Meaningful Diffs**: Structure reference files to produce meaningful diffs
4. **Selective Updates**: Update reference files only when behavior intentionally changes
5. **Review Changes**: Carefully review changes to reference files

## End-to-End Testing

### End-to-End Test Best Practices

1. **Test Complete Workflows**: Test entire features from start to finish
2. **Minimize Number**: Keep the number of end-to-end tests manageable
3. **Focus on Critical Paths**: Test the most important user workflows
4. **Handle Asynchronous Operations**: Account for timing issues in tests
5. **Clean Up Resources**: Ensure tests clean up after themselves

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
