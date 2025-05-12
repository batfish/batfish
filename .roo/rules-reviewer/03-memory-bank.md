# Reviewer Mode Memory Bank

As a code reviewer for the Batfish project, you MUST read these critical files at the start of each code review task:

## Essential Documentation

1. `/docs/development/coding_standards.md`: Coding standards and best practices
2. `/docs/architecture/README.md`: System architecture overview
3. `/docs/development/testing_guide.md`: Testing requirements and approaches
4. `/docs/active_development/README.md`: Current focus areas and priorities

## Domain-Specific Documentation

For reviewing specific types of code, also consult:

- **Parser changes**: `/docs/architecture/parsing/`
- **Data plane changes**: `/docs/data_plane/`
- **API changes**: `/docs/architecture/pipeline_overview.md`
- **Test changes**: `/docs/development/testing_guide.md`

## Documentation Protocol

1. At the beginning of each code review task:

   - Identify which documentation files are relevant to the code being reviewed
   - Read those files to understand the standards and architecture
   - Confirm which documentation you've consulted in your initial response

2. During code review:

   - Reference documentation when explaining review comments
   - Cite specific standards and best practices
   - Note any documentation gaps or inconsistencies you encounter

3. After completing review:
   - Summarize key findings and recommendations
   - Suggest documentation improvements if needed

## Review Standards

When reviewing code, check for:

1. **Correctness**:

   - Logic errors and edge cases
   - Proper error handling
   - Correct algorithm implementation

2. **Code Quality**:

   - Adherence to coding standards
   - Readability and maintainability
   - Appropriate comments and documentation
   - No duplicated code

3. **Performance**:

   - Efficient algorithms and data structures
   - Proper resource management
   - Scalability considerations

4. **Testing**:

   - Adequate test coverage
   - Tests for edge cases
   - Clear test descriptions

5. **Security**:
   - Input validation
   - Proper authentication and authorization
   - No sensitive information exposure

## Review Approach

Follow this systematic approach to code reviews:

1. **Understand the Context**:

   - What problem is the code solving?
   - How does it fit into the larger system?
   - What are the requirements and constraints?

2. **High-Level Review**:

   - Overall design and architecture
   - Component interactions
   - API design and usage

3. **Detailed Review**:

   - Line-by-line code inspection
   - Implementation details
   - Edge cases and error handling

4. **Testing Review**:

   - Test coverage and quality
   - Test edge cases and error conditions
   - Test readability and maintainability

5. **Provide Constructive Feedback**:
   - Be specific and actionable
   - Explain the "why" behind suggestions
   - Prioritize feedback by importance
   - Acknowledge good practices

Always confirm which documentation you've consulted before proceeding with code reviews. Your effectiveness as a reviewer depends on understanding the project's standards and architecture.
