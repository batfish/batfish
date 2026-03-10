# Batfish Coding Standards

This document outlines the coding standards and best practices for contributing to the Batfish project.

## Java Standards

### Formatting

- Use the Google Java Style Guide
- 2-space indentation (no tabs)
- Maximum line length of 100 characters
- Use trailing commas in multi-line lists and arrays
- Use braces for all control structures, even single-line blocks
- Place opening braces on the same line as the declaration

### Naming Conventions

- Classes: `PascalCase` (e.g., `BatfishException`)
- Methods and variables: `camelCase` (e.g., `parseConfig`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_TIMEOUT_MS`)
- Package names: lowercase, no underscores (e.g., `org.batfish.datamodel`)
- Test classes: Named after the class they test with `Test` suffix (e.g., `BatfishTest`)

### Documentation

- All public APIs must have Javadoc comments
- Document non-obvious implementation details with inline comments
- Keep comments up-to-date with code changes
- Use `@Override` annotation when implementing or overriding methods
- Include `@Nullable` annotations where appropriate

### Code Organization

- One top-level class per file
- Group related functionality into classes and packages
- Prefer composition over inheritance
- Keep methods focused on a single responsibility
- Limit method length (aim for methods that fit on a single screen)
- Order class members logically (constants, fields, constructors, methods)

## Testing Standards

### Unit Tests

- Write unit tests for all non-trivial code
- Use descriptive test method names that explain what is being tested
- Structure tests using the Arrange-Act-Assert pattern
- Test both normal and edge cases
- Mock external dependencies
- Keep tests independent of each other

### Integration Tests

- Write integration tests for complex interactions between components
- Use appropriate test fixtures and setup/teardown methods
- Minimize dependencies on external systems
- Document any required external setup

### Reference Tests

- Use reference tests for parsing and conversion logic
- Store reference files in version control
- Document the purpose and expected behavior of reference tests
- Update reference files when behavior intentionally changes

## Error Handling

### Exceptions

- Use specific exception types rather than generic ones
- Document exceptions in method Javadoc
- Include useful information in exception messages
- Don't catch exceptions without handling them appropriately
- Use try-with-resources for automatic resource cleanup
- Prefer unchecked exceptions for programming errors

### Null Handling

- Use `@Nullable` and `@Nonnull` annotations to document nullability
- Check for null when accepting parameters that might be null
- Don't return null from collections or arrays; return empty ones instead
- Use `Optional<T>` for return values that might not exist
- Be consistent with null handling within a codebase

## Performance Considerations

### Memory Usage

- Be mindful of memory usage, especially for large networks
- Avoid unnecessary object creation
- Use appropriate data structures for the task
- Consider using primitive types instead of wrapper classes when appropriate
- Release references to objects when they are no longer needed

### CPU Usage

- Profile code to identify bottlenecks
- Use efficient algorithms and data structures
- Consider parallelization for CPU-intensive tasks
- Avoid premature optimization
- Document performance characteristics of public APIs

## Security Considerations

### Input Validation

- Validate all external inputs
- Don't trust user-provided data
- Use parameterized queries for database access
- Be careful with deserialization of untrusted data
- Validate file paths to prevent path traversal attacks

### Output Sanitization

- Sanitize data before displaying it to users
- Be careful with error messages that might expose sensitive information
- Use secure defaults
- Follow the principle of least privilege

## Version Control Practices

### Commits

- Write clear, descriptive commit messages
- Keep commits focused on a single logical change
- Include the issue number in commit messages when applicable
- Make sure all tests pass before committing
- Don't commit generated files or IDE-specific files

### Pull Requests

- Keep pull requests focused on a single issue
- Write a clear description of the changes
- Include tests for new functionality
- Address all code review comments
- Update documentation as needed
- Squash fixup commits before merging
