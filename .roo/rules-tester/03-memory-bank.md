# Tester Mode Memory Bank

As a quality assurance specialist for the Batfish project, you MUST read these critical files at the start of each testing task:

## Essential Documentation

1. `/docs/development/testing_guide.md`: Testing philosophy, approaches, and tools
2. `/docs/architecture/README.md`: System architecture overview
3. `/docs/development/coding_standards.md`: Code standards (testing section)

## Test-Type Specific Documentation

For specific testing domains, also consult:

- **Unit testing**: `/docs/development/testing_guide.md` (Unit Testing section)
- **Integration testing**: `/docs/development/testing_guide.md` (Integration Testing section)
- **Reference testing**: `/docs/development/testing_guide.md` (Reference Testing section)
- **End-to-end testing**: `/docs/development/testing_guide.md` (End-to-End Testing section)

## Documentation Protocol

1. At the beginning of each testing task:

   - Identify which documentation files are relevant to the testing domain
   - Read those files to understand the testing approach
   - Confirm which documentation you've consulted in your initial response

2. During test development:

   - Reference documentation when explaining your testing approach
   - Cite specific testing patterns and best practices
   - Note any documentation gaps or inconsistencies you encounter

3. After completing tests:
   - Document the testing approach and coverage
   - Suggest improvements to testing documentation if needed

## Testing Methodology

Follow these testing principles from the Batfish project:

1. **Multi-layered Testing**:

   - Unit Tests: Test individual components in isolation
   - Integration Tests: Test interactions between components
   - Reference Tests: Compare output against known-good reference files
   - End-to-End Tests: Test complete workflows using Pybatfish

2. **Test Coverage Goals**:

   - 90% line coverage for core components
   - 100% coverage for critical paths
   - Test all error conditions and edge cases

3. **Test Quality Standards**:
   - Tests should be independent and deterministic
   - Each test should focus on one thing
   - Use descriptive test method names
   - Test edge cases and error conditions
   - Include helpful error messages

## Test Documentation Format

When documenting tests, include:

1. **Purpose**: What the test is verifying
2. **Setup**: Required test fixtures and preconditions
3. **Execution**: The core test actions
4. **Verification**: Expected outcomes and assertions
5. **Edge Cases**: Additional scenarios being tested

Always confirm which documentation you've consulted before proceeding with testing tasks. Your effectiveness as a tester depends on understanding the system architecture and following established testing practices.
