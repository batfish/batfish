# Memory Bank Integration Plan for Batfish

> **Implementation Note:** After reviewing this plan, we decided to implement Option 3: Move all details into .roo/rules* and trim down docs/roo_modes. Instead of maintaining detailed documentation in both .roo/rules-* and docs/roo_modes, we created a single docs/development/roo.md file with a concise overview of Roo and pointers to the authoritative configuration in the .roo directory. The docs/roo_modes directory has been removed to eliminate duplication.

This document outlines a comprehensive plan for integrating memory bank concepts into the Batfish project documentation structure and creating project-local Roo modes.

## 1. Integrated Documentation Structure

```
docs/
├── README.md (Comprehensive overview with project brief and product context)
├── active_development/ (Replaces activeContext.md)
│   ├── README.md (Current focus areas and recent changes)
│   ├── roadmap.md (Short and long-term development plans)
│   └── known_issues.md (Current status and known issues)
├── architecture/ (Combines systemPatterns.md with technical docs)
│   ├── README.md (System architecture overview and key decisions)
│   ├── pipeline_overview.md (Enhanced with design decisions)
│   ├── parsing/ (Existing with added context)
│   ├── extraction/ (Existing with added context)
│   └── ... (Other existing pipeline components)
├── development/ (Combines techContext.md with setup docs)
│   ├── README.md (Development environment and technology stack)
│   ├── building_and_running/ (Existing)
│   ├── contributing/ (Enhanced with project principles)
│   ├── coding_standards.md (Comprehensive coding standards)
│   ├── testing_guide.md (Testing philosophy and practices)
│   ├── git_workflow.md (Git practices and standards)
│   └── intellij_setup/ (Existing)
├── user_guide/ (New section focused on users rather than developers)
│   ├── README.md (User-focused overview)
│   ├── use_cases.md (Common use cases and solutions)
│   └── best_practices.md (Recommended approaches)
└── roo_modes/ (Documentation for project-local Roo modes)
    ├── README.md (Overview of available modes and their purposes)
    ├── code_mode.md (Configuration and context for Code mode)
    ├── architect_mode.md (Configuration and context for Architect mode)
    ├── ask_mode.md (Configuration and context for Ask mode)
    ├── debug_mode.md (Configuration and context for Debug mode)
    ├── orchestrator_mode.md (Configuration and context for Orchestrator mode)
    ├── tester_mode.md (Configuration and context for new Tester mode)
    ├── reviewer_mode.md (Configuration and context for new Code Reviewer mode)
    └── git_maintainer_mode.md (Configuration and context for new Git Maintainer mode)
```

## 2. Content Integration Approach

### 2.1. Enhanced `docs/README.md`

The main README will be enhanced to incorporate project brief and product context information:

```markdown
# Batfish Documentation

## Project Overview

Batfish is a network validation tool that provides correctness guarantees for security, reliability, and compliance by analyzing the configuration of network devices. It builds complete models of network behavior from device configurations and finds violations of network policies (built-in, user-defined, and best-practices).

### Core Mission

Batfish enables network engineers to validate configuration changes _before_ deployment, closing a critical gap in existing network automation workflows. By including Batfish in automation workflows, network engineers can ensure that only correct changes are deployed.

### Key Problems Solved

- **Configuration Complexity**: Modern networks involve numerous devices with complex, interdependent configurations
- **High Cost of Errors**: Misconfigurations can lead to outages, security breaches, and compliance violations
- **Limited Testing**: Traditional network testing methods are manual, time-consuming, and often incomplete
- **Vendor Diversity**: Networks typically include devices from multiple vendors with different configuration languages

## Documentation Structure

This documentation is organized into several sections:

- **[Active Development](active_development/README.md)**: Current focus areas, roadmap, and known issues
- **[Architecture](architecture/README.md)**: System design, pipeline overview, and component details
- **[Development](development/README.md)**: Setup instructions, contribution guidelines, and technical context
- **[User Guide](user_guide/README.md)**: Usage instructions, use cases, and best practices

## For Developers

This documentation is primarily aimed at developers interested in understanding and improving Batfish. If you are interested in trying out Batfish on your network, check out our [instructions for getting started](https://pybatfish.readthedocs.io/en/latest/getting_started.html) instead.

## Getting Help

If you get stuck after reading all relevant documentation, you can ask questions on the [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTcyYzY3M2Q0NWUyYTRhYjdlM2IzYzRhZGU1NWFlNGU2MzlhNDY3OTJmMDIyMjQzYmRlNjhkMTRjNWIwNTUwNTQ).
```

### 2.2. New `active_development/README.md`

````markdown
# Active Development

This section provides information about the current state of Batfish development, including focus areas, recent changes, and known issues.

## Current Focus Areas

1. **Expanding Device Support**

   - Adding support for [specific vendor/device]
   - Enhancing support for [specific vendor/device]

2. **Improving Analysis Capabilities**

   - Enhancing [specific protocol] analysis
   - Adding new checks for [specific network issue]

3. **Performance Optimization**
   - Optimizing [specific component] for better performance
   - Reducing memory usage in [specific area]

## Recent Significant Changes

### [Month Year]

- Added support for [feature/device]
- Fixed issue with [component]
- Improved performance of [analysis type]

### [Month Year]

- Released version X.Y.Z
- Added new question type for [purpose]
- Enhanced documentation for [component]

## How to Contribute to Current Priorities

If you're interested in contributing to Batfish, here are the areas where help is most needed:

1. [Area 1]
2. [Area 2]
3. [Area 3]

## Additional Information

- [Roadmap](roadmap.md): Short and long-term development plans
- [Known Issues](known_issues.md): Current status and known issues

### 2.3. Enhanced `architecture/README.md`

```markdown
# Batfish Architecture

This document provides an overview of Batfish's architecture and key design decisions.

## Core Architecture

Batfish follows a pipeline architecture with distinct stages:

1. **[Parsing](parsing/README.md)**: Convert vendor-specific configurations into parse trees
2. **[Extraction](extraction/README.md)**: Extract data from parse trees into vendor-specific models
3. **[Conversion](conversion/README.md)**: Convert vendor-specific models into vendor-independent models
4. **[Post-processing](post_processing/README.md)**: Finalize and clean up the vendor-independent model
5. **[Data Plane Generation](data_plane/README.md)**: Compute routing information bases and forwarding tables
6. **[Forwarding Analysis](forwarding_analysis/README.md)**: Analyze forwarding behavior

## Key Design Decisions

### Vendor-Independent Model

Batfish converts all vendor-specific configurations into a unified vendor-independent model. This allows analysis algorithms to work consistently across different network devices.

**Rationale**: This approach enables Batfish to support multiple vendors without duplicating analysis code.

### Parse Tree-Based Extraction

Batfish uses ANTLR-generated parsers to create parse trees, which are then processed to extract configuration data.

**Rationale**: This approach provides a clean separation between syntax recognition and semantic interpretation.

### Symbolic Analysis

Batfish uses symbolic analysis techniques to efficiently reason about large sets of packets.

**Rationale**: This approach allows Batfish to analyze network behavior for all possible packets without having to enumerate them individually.

## Component Interactions

### Question Framework

Questions are the primary way users interact with Batfish. Each question is implemented as a plugin that can access the network model and data plane.

### Data Model Evolution

The data model evolves through these stages:

1. Vendor-specific text configurations
2. Parse trees
3. Vendor-specific Java objects
4. Vendor-independent Java objects
5. Data plane structures

## Extension Points

Batfish is designed to be extensible in several ways:

1. **Adding New Device Support**:

   - Implement a parser for the device's configuration language
   - Create extraction logic to populate vendor-specific models
   - Implement conversion logic to the vendor-independent model

2. **Adding New Analysis Capabilities**:

   - Implement new questions that use the existing data model
   - Extend the data model to capture new types of information
   - Add new analysis algorithms that operate on the data plane

3. **Integrating with External Systems**:
   - Use the RESTful API provided by the coordinator
   - Use the Python SDK (Pybatfish) to script interactions

## Pipeline Details

For detailed information on each pipeline stage, see the corresponding documentation:

- [Pipeline Overview](pipeline_overview.md)
- [Parsing](parsing/README.md)
- [Extraction](extraction/README.md)
- [Conversion](conversion/README.md)
- [Post-processing](post_processing/README.md)
- [Data Plane Generation](data_plane/README.md)
- [Forwarding Analysis](forwarding_analysis/README.md)
```
````

### 2.4. Enhanced `development/README.md`

```markdown
# Batfish Development

This section provides information about the Batfish development environment, technology stack, and contribution guidelines.

## Technology Stack

### Core Technologies

- **Java**: Primary implementation language
- **ANTLR**: Parser generator used for network configuration parsing
- **Python**: Used for the Pybatfish client library
- **Bazel**: Build system
- **Docker**: Container platform for deployment

### Key Libraries and Dependencies

- **BDD (Binary Decision Diagrams)**: Used for symbolic analysis
- **Jackson**: JSON processing
- **Jersey**: RESTful web services
- **JUnit**: Testing framework
- **Guava**: Google core libraries for Java

## Development Environment

### Prerequisites

- JDK 11 or later
- Bazel
- Python 3.9 or later (for Pybatfish)
- Docker (for containerized deployment)

### IDE Setup

Batfish developers primarily use IntelliJ IDEA. Detailed setup instructions are available in the [IntelliJ setup guide](intellij_setup/README.md).

### Build System

Batfish uses Bazel as its build system. Key commands:

- `bazel build //...`: Build all targets
- `bazel test //...`: Run all tests
- `bazel run //projects/batfish:batfish`: Run the Batfish server

## Getting Started

- [Building and Running](building_and_running/README.md): Instructions for building and running Batfish
- [Contributing](contributing/README.md): Guidelines for contributing to Batfish

## Development Workflow

1. **Issue Tracking**: GitHub Issues
2. **Version Control**: Git with GitHub
3. **Code Review**: Pull requests with required reviews
4. **Continuous Integration**: GitHub Actions

## Project Principles

1. **Correctness**: Batfish prioritizes accurate analysis over speed
2. **Usability**: The tool should be accessible to network engineers without requiring programming expertise
3. **Extensibility**: The architecture should allow for easy addition of new device types and analysis capabilities
4. **Community-Driven**: Development priorities are guided by real-world network engineering needs
```

### 2.5. New `user_guide/README.md`

```markdown
# Batfish User Guide

This section provides information for users of Batfish, focusing on practical usage rather than development.

## Getting Started

If you are new to Batfish, we recommend starting with the [official getting started guide](https://pybatfish.readthedocs.io/en/latest/getting_started.html).

## What Batfish Can Do

Batfish supports a wide range of network validation tasks:

### Configuration Compliance

## 3. Project-Local Roo Modes

### 3.1. Mode Configuration Structure
```

.roo/
├── modes/
│ ├── code.json
│ ├── architect.json
│ ├── ask.json
│ ├── debug.json
│ ├── orchestrator.json
│ ├── tester.json
│ ├── reviewer.json
│ └── git_maintainer.json
└── config.json

````

### 3.2. Mode Configuration Examples

#### Code Mode (`code.json`)

```json
{
  "name": "💻 Code",
  "slug": "code",
  "description": "Highly skilled software engineer focused on implementing features and fixing bugs",
  "system_instructions": "You are Roo, a highly skilled software engineer with extensive knowledge of the Batfish codebase. Your goal is to help implement features, fix bugs, and write efficient, maintainable code following the project's coding standards. Focus on the technical implementation details and ensure your code is well-tested and documented.",
  "allowed_file_patterns": [".*"],
  "context_files": [
    "docs/development/coding_standards.md",
    "docs/architecture/README.md"
  ]
}
````

#### Tester Mode (`tester.json`)

```json
{
  "name": "🧪 Tester",
  "slug": "tester",
  "description": "Quality assurance specialist focused on ensuring code correctness through testing",
  "system_instructions": "You are Roo, a quality assurance specialist focused on ensuring code correctness through comprehensive testing and validation. Your goal is to write effective tests, review existing tests for coverage and effectiveness, identify edge cases, and ensure test maintainability. Be skeptical and thorough in your approach to testing.",
  "allowed_file_patterns": [".*Test\\.java$", ".*test.*", ".*spec.*"],
  "context_files": [
    "docs/development/testing_guide.md",
    "docs/active_development/known_issues.md"
  ]
}
```

#### Code Reviewer Mode (`reviewer.json`)

```json
{
  "name": "👀 Reviewer",
  "slug": "reviewer",
  "description": "Code reviewer focused on ensuring code quality and adherence to standards",
  "system_instructions": "You are Roo, a thorough code reviewer focused on ensuring code quality, adherence to standards, and identifying potential issues. Your goal is to review code changes for quality and correctness, ensure adherence to project standards, identify potential bugs or performance issues, verify test coverage, and provide constructive feedback.",
  "allowed_file_patterns": [".*"],
  "context_files": [
    "docs/development/coding_standards.md",
    "docs/architecture/README.md",
    "docs/development/testing_guide.md"
  ]
}
```

#### Git Maintainer Mode (`git_maintainer.json`)

```json
{
  "name": "🔄 Git Maintainer",
  "slug": "git_maintainer",
  "description": "Version control specialist focused on maintaining a clean git history",
  "system_instructions": "You are Roo, a version control specialist focused on maintaining a clean and useful git history. Your goal is to create well-structured commits with meaningful messages after project checkpoints, manage branches effectively, and leverage git capabilities to roll back when development tasks go awry. Be judicious in your use of git commands and always prioritize maintaining a clean, useful history.",
  "allowed_file_patterns": [".*"],
  "context_files": [
    "docs/development/git_workflow.md",
    "docs/active_development/README.md"
  ]
}
```

## 4. Mode Documentation

### 4.1. `roo_modes/README.md`

```markdown
# Batfish Roo Modes

This directory contains documentation for the project-local Roo modes configured for the Batfish project.

## Available Modes

- **[💻 Code](code_mode.md)**: Highly skilled software engineer focused on implementing features and fixing bugs
- **[🏗️ Architect](architect_mode.md)**: Experienced technical leader focused on system design and planning
- **[❓ Ask](ask_mode.md)**: Knowledge assistant focused on answering questions about the codebase
- **[🪲 Debug](debug_mode.md)**: Debugging specialist focused on diagnosing and fixing issues
- **[🪃 Orchestrator](orchestrator_mode.md)**: Coordinator for complex tasks across multiple domains
- **[🧪 Tester](tester_mode.md)**: Quality assurance specialist focused on ensuring code correctness through testing
- **[👀 Reviewer](reviewer_mode.md)**: Code reviewer focused on ensuring code quality and adherence to standards
- **[🔄 Git Maintainer](git_maintainer_mode.md)**: Version control specialist focused on maintaining a clean git history

## Using Project-Local Modes

To use these modes:

1. Open the command palette in VS Code (Ctrl+Shift+P or Cmd+Shift+P)
2. Type "Roo: Switch Mode" and select it
3. Choose one of the Batfish-specific modes from the list

Each mode is configured with specific knowledge and context relevant to its purpose.
```

### 4.2. `roo_modes/code_mode.md`

```markdown
# Code Mode

## Description

In Code mode, Roo acts as a highly skilled software engineer focused on implementing features and fixing bugs in the Batfish codebase.

## Key Responsibilities

- Implementing new features
- Fixing bugs
- Writing efficient, maintainable code
- Following project coding standards

## Relevant Documentation

- [Development Guide](../development/README.md)
- [Architecture Overview](../architecture/README.md)
- [Pipeline Documentation](../architecture/pipeline_overview.md)

## Coding Standards

- Follow Java coding standards as outlined in [Coding Standards](../development/coding_standards.md)
- Ensure all new code has appropriate unit tests
- Maintain backward compatibility unless explicitly changing an API
- Document public APIs with Javadoc comments

## When to Use

- When implementing new features
- When fixing bugs
- When refactoring existing code
```

### 4.3. `roo_modes/tester_mode.md`

```markdown
# Tester Mode

## Description

In Tester mode, Roo acts as a quality assurance specialist focused on ensuring code correctness through comprehensive testing and validation.

## Key Responsibilities

- Writing unit, integration, and end-to-end tests
- Reviewing existing tests for coverage and effectiveness
- Identifying edge cases and potential failure modes
- Ensuring test maintainability

## Relevant Documentation

- [Testing Guide](../development/testing_guide.md)
- [Architecture Overview](../architecture/README.md)
- [Known Issues](../active_development/known_issues.md)

## Testing Standards

- Every feature must have unit tests
- Critical paths must have integration tests
- Tests should be independent and deterministic
- Tests should clearly indicate what they're testing
- Test coverage should focus on logic branches, not just line coverage
- Edge cases should be explicitly tested

## When to Use

- When writing tests for new features
- When reviewing test coverage
- When investigating flaky tests
- When designing test strategies for complex features
```

### 4.4. `roo_modes/reviewer_mode.md`

```markdown
# Code Reviewer Mode

## Description

In Code Reviewer mode, Roo acts as a thorough code reviewer focused on ensuring code quality, adherence to standards, and identifying potential issues.

## Key Responsibilities

- Reviewing code changes for quality and correctness
- Ensuring adherence to project standards
- Identifying potential bugs or performance issues
- Verifying test coverage
- Providing constructive feedback

## Relevant Documentation

- [Coding Standards](../development/coding_standards.md)
- [Architecture Overview](../architecture/README.md)
- [Testing Guide](../development/testing_guide.md)

## Review Standards

- Verify code meets project style guidelines
- Check for potential bugs and edge cases
- Ensure adequate test coverage
- Look for performance implications
- Verify documentation is updated
- Consider maintainability and readability
- Check for security implications

## When to Use

- When reviewing pull requests
- When conducting code quality audits
- When mentoring other developers
- When establishing or updating coding standards
```

## 5. Standards Documentation

### 5.1. `development/coding_standards.md`

```markdown
# Batfish Coding Standards

This document outlines the coding standards for the Batfish project. These standards ensure code consistency, maintainability, and quality across the codebase.

## Java Code Style

### Formatting

- Use 2-space indentation
- Maximum line length of 100 characters
- Use Unix-style line endings (LF)
- End files with a newline

### Naming Conventions

- Classes: `PascalCase`
- Methods and variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Package names: lowercase, no underscores

### Documentation

- All public classes and methods must have Javadoc comments
- Document parameters, return values, and exceptions
- Include examples for complex APIs

### Code Organization

- One top-level class per file
- Related classes should be in the same package
- Limit file size to 2000 lines
- Limit method size to 50 lines

## Testing Standards

### Unit Tests

- Every class should have a corresponding test class
- Test each public method
- Test edge cases and error conditions
- Use descriptive test method names

### Integration Tests

- Test interactions between components
- Verify end-to-end functionality
- Test with realistic inputs

### Reference Tests

- Compare output against known-good reference files
- Update reference files when behavior intentionally changes

## Error Handling

### Exceptions

- Use specific exception types
- Document exceptions in Javadoc
- Include helpful error messages
- Don't catch exceptions without handling them

### Null Handling

- Use `@Nullable` and `@Nonnull` annotations
- Check for null when appropriate
- Use Optional for values that might not be present

## Performance Considerations

### Memory Usage

- Be mindful of memory usage for large networks
- Avoid unnecessary object creation
- Use appropriate data structures

### CPU Usage

- Optimize critical paths
- Use efficient algorithms
- Consider parallelization for independent operations

## Security Considerations

### Input Validation

- Validate all external inputs
- Don't trust user-provided data
- Handle malformed inputs gracefully

### Output Sanitization

- Sanitize outputs to prevent injection attacks
- Don't expose sensitive information

## Version Control

### Commits

- Write clear commit messages
- Keep commits focused on a single change
- Reference issue numbers when applicable

### Pull Requests

- Keep PRs focused on a single feature or fix
- Include tests for new functionality
- Update documentation as needed
```

### 5.2. `development/testing_guide.md`

````markdown
# Batfish Testing Guide

This document provides guidelines for testing Batfish code. Comprehensive testing is essential to ensure the correctness and reliability of Batfish's analysis.

## Testing Philosophy

Batfish follows a multi-layered testing approach:

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test interactions between components
3. **Reference Tests**: Compare output against known-good reference files
4. **End-to-End Tests**: Test complete workflows using Pybatfish

## Unit Testing

### When to Write Unit Tests

- For all new classes and methods
- When fixing bugs
- When refactoring existing code

### Unit Test Best Practices

- Test one thing per test method
- Use descriptive test method names
- Set up test fixtures efficiently
- Test edge cases and error conditions
- Use mocks for external dependencies
- Keep tests independent and deterministic

### Example Unit Test

```java
@Test
public void testIpAddressContainedIn() {
  Ip ip = Ip.parse("192.0.2.1");
  Prefix prefix = Prefix.parse("192.0.2.0/24");
  assertTrue(prefix.containsIp(ip));

  Prefix otherPrefix = Prefix.parse("198.51.100.0/24");
  assertFalse(otherPrefix.containsIp(ip));
}
```
````

## Integration Testing

### When to Write Integration Tests

- For interactions between components
- For end-to-end functionality
- For complex workflows

### Integration Test Best Practices

- Use realistic test data
- Test complete workflows
- Verify all side effects
- Clean up test resources

## Reference Testing

Batfish uses reference tests to compare the output of parsing, conversion, and analysis against known-good reference files.

### How Reference Tests Work

1. Parse and process a test configuration
2. Serialize the resulting data structures
3. Compare with stored reference files
4. Update reference files when behavior intentionally changes

### Reference Test Best Practices

- Include diverse test configurations
- Review reference file changes carefully
- Document why reference files changed

## End-to-End Testing

End-to-end tests use Pybatfish to test complete workflows from a user's perspective.

### End-to-End Test Best Practices

- Test common user workflows
- Verify correct output for questions
- Test with realistic network configurations

## Test Coverage

Batfish aims for high test coverage, but focuses on logical coverage rather than just line coverage.

### Coverage Goals

- 90% line coverage for core components
- 100% coverage for critical paths
- Test all error conditions and edge cases

### Measuring Coverage

- Use Jacoco for Java code coverage
- Review coverage reports regularly
- Address coverage gaps in critical areas

## Testing Tools

### JUnit

Batfish uses JUnit 5 for Java tests.

### Hamcrest Matchers

Use Hamcrest matchers for readable assertions.

### Mockito

Use Mockito for mocking dependencies in unit tests.

## Continuous Integration

All tests run automatically on pull requests through GitHub Actions.

### CI Best Practices

- Fix failing tests promptly
- Don't disable tests without good reason
- Investigate flaky tests

````

### 5.3. `development/git_workflow.md`

```markdown
# Batfish Git Workflow Guide

This document outlines the git workflow and best practices for the Batfish project.

## Commit Guidelines

### Commit Structure

Each commit should:
- Represent a single logical change
- Be complete and self-contained
- Not break the build or tests
- Include relevant tests and documentation

### Commit Messages

Commit messages should follow this format:

````

<type>: <short summary>

<detailed description>

<references>
```

Where:

- **Type**: One of the following:
  - `feat`: A new feature
  - `fix`: A bug fix
  - `docs`: Documentation changes
  - `style`: Code style changes (formatting, etc.)
  - `refactor`: Code changes that neither fix bugs nor add features
  - `test`: Adding or modifying tests
  - `chore`: Changes to the build process or auxiliary tools
- **Short summary**: A concise description (50 chars or less)
- **Detailed description**: More detailed explanation when necessary
- **References**: GitHub issue references (e.g., "Fixes #123")

Example:

```
feat: add support for Arista EOS BGP configuration

This change adds parsing, extraction, and conversion support for
BGP configuration in Arista EOS devices. It includes:
- Grammar rules for BGP configuration blocks
- Extraction to vendor-specific model
- Conversion to vendor-independent model

Fixes #456
```

## Branch Management

### Branch Naming

Branch names should follow this format:

```
<type>/<description>
```

Where:

- **Type**: Same as commit types (feat, fix, docs, etc.)
- **Description**: Brief description using hyphens as separators

Example: `feat/arista-eos-bgp-support`

### Branch Workflow

1. Create a branch for each feature or fix
2. Make regular commits as you progress
3. Keep branches up to date with the main branch
4. Submit a pull request when ready for review

## When to Commit

Create commits at these checkpoints:

1. After setting up the initial structure for a task
2. After implementing core functionality
3. After adding tests
4. After addressing review feedback

## Using Git for Recovery

### When to Roll Back

Consider rolling back when:

- A development approach proves to be incorrect
- Changes introduce unexpected complications
- You want to try an alternative implementation

### How to Roll Back

#### For uncommitted changes:

```bash
# Discard all uncommitted changes
git restore .

# Discard changes to specific files
git restore <file1> <file2>
```

#### For committed changes:

```bash
# Soft reset (keep changes in working directory)
git reset --soft HEAD~1

# Hard reset (discard changes)
git reset --hard HEAD~1

# Revert a commit (create a new commit that undoes changes)
git revert <commit-hash>
```

### Creating Checkpoints

To create a checkpoint you might want to return to:

```bash
# Create a checkpoint commit
git commit -m "checkpoint: implemented feature X approach 1"

# Create a branch at this point for easy reference
git branch checkpoint/feature-x-approach-1
```

## Advanced Git Techniques

### Interactive Rebase

Use interactive rebase to clean up commits before submitting a pull request:

```bash
git rebase -i HEAD~<number-of-commits>
```

### Cherry-picking

Apply specific commits from one branch to another:

```bash
git cherry-pick <commit-hash>
```

### Stashing

Temporarily store changes when you need to switch contexts:

```bash
# Stash changes
git stash save "description of changes"

# List stashes
git stash list

# Apply a stash
git stash apply stash@{0}
```

## Git Hooks

Batfish uses pre-commit hooks to ensure code quality. These hooks:

- Format code according to project standards
- Run linters to catch common issues
- Verify that tests pass

See [.pre-commit-config.yaml](/.pre-commit-config.yaml) for details.

````

## 6. Implementation Plan

### Phase 1: Documentation Restructuring
1. Create the new integrated directory structure
2. Move and enhance existing documentation
3. Create new documentation for standards and guidelines
4. Add git workflow documentation

### Phase 2: Mode Configuration
1. Create the `.roo` directory with mode configurations
2. Write mode-specific documentation
3. Configure context files for each mode

### Phase 3: Testing and Refinement
1. Test each mode with typical tasks
2. Gather feedback from team members
3. Refine mode configurations and documentation

## 7. Conclusion

This comprehensive plan:
1. Integrates memory bank concepts directly into the existing documentation structure
2. Creates project-local Roo modes customized for Batfish development
3. Adds specialized modes for testing, code review, and git operations
4. Documents all standards and best practices
5. Ensures each mode has access to the most relevant context

The result will be a cohesive system that provides contextual awareness throughout the documentation and tailors the Roo experience to specific development tasks within the Batfish project.

### 4.5. `roo_modes/git_maintainer_mode.md`

```markdown
# Git Maintainer Mode

## Description

In Git Maintainer mode, Roo acts as a version control specialist focused on maintaining a clean and useful git history, creating meaningful commits, and leveraging git capabilities to manage project evolution.

## Key Responsibilities

- Creating well-structured commits after project checkpoints
- Writing clear, descriptive commit messages
- Managing branches for feature development
- Performing rollbacks when development tasks go awry
- Ensuring git history remains clean and useful

## Relevant Documentation

- [Git Workflow Guide](../development/git_workflow.md)
- [Active Development](../active_development/README.md)
- [Coding Standards](../development/coding_standards.md)

## Git Standards

- Create atomic commits that represent logical changes
- Write descriptive commit messages following project conventions
- Commit after each completed task or significant checkpoint
- Use branches appropriately for feature development
- Know when to leverage git history for rollbacks

## When to Use

- After completing a development task or checkpoint
- When needing to roll back to a previous state
- When managing complex feature branches
- When preparing code for submission
````

- Flag undefined-but-referenced or defined-but-unreferenced structures (e.g., ACLs, route maps)
- Configuration settings for MTUs, AAA, NTP, logging, etc. match templates
- Devices can only be accessed using SSHv2 and password is not null

### Reliability

- End-to-end reachability is not impacted for any flow after any single-link or single-device failure
- Certain services (e.g., DNS) are globally reachable

### Security

- Sensitive services can be reached only from specific subnets or devices
- Paths between endpoints are as expected (e.g., traverse a firewall, have at least 2 way ECMP, etc...)

### Change Analysis

- End-to-end reachability is identical across the current and a planned configuration
- Planned ACL or firewall changes are provably correct and causes no collateral damage for other traffic
- Two configurations, potentially from different vendors, are functionally equivalent

## Common Use Cases

See the [Use Cases](use_cases.md) document for examples of how Batfish is commonly used in network operations.

## Best Practices

See the [Best Practices](best_practices.md) document for recommendations on how to get the most out of Batfish.

## Supported Devices

Batfish supports configurations for a large and growing set of (physical and virtual) devices, including:

- Cisco (All Cisco NX-OS, IOS, IOS-XE, IOS-XR and ASA devices)
- Juniper (All JunOS platforms: MX, EX, QFX, SRX, T-series, PTX)
- Arista
- Palo Alto Networks
- F5 BIG-IP
- And many more...

For a complete list, see the [main README](../README.md#supported-network-device-and-operating-system-list).

```

```

```

```
