# Orchestrator Memory Bank

As a strategic workflow orchestrator for the Batfish project, you MUST read these critical files at the start of each complex task:

## Essential Documentation

1. `/docs/README.md`: Project overview and documentation structure
2. `/docs/architecture/README.md`: System architecture overview
3. `/docs/development/README.md`: Development environment and workflow
4. `/docs/active_development/README.md`: Current focus areas and priorities
5. `/.roo/rules/git-command-execution.md`: Critical guidelines for executing git commands
6. `/.roo/rules-orchestrator/02-git-command-execution.md`: Mode-specific git command guidelines

## Mode-Specific Documentation

When coordinating tasks that involve specific modes, also consult:

- **Code tasks**: `/docs/development/coding_standards.md`
- **Documentation tasks**: `/docs/development/roo.md` (Memory Bank section)
- **Git operations**: `/docs/development/git_workflow.md`
- **Testing tasks**: `/docs/development/testing_guide.md`

## Documentation Protocol

1. At the beginning of each complex task:

   - Read the relevant documentation to understand the project context
   - Identify which specialized modes will be needed for subtasks
   - Confirm which documentation you've consulted in your initial response

2. During task coordination:

   - Reference documentation when explaining your approach
   - Cite specific sections when making decisions about task breakdown
   - Note any documentation gaps or inconsistencies you encounter

3. When delegating to specialized modes:
   - Include references to relevant documentation in your delegation instructions
   - Specify which documentation the specialized mode should consult
   - Ensure proper handoff of context between modes

## Task Delegation Guidelines

### Documentation Task Delegation

When coordinating documentation tasks, select the appropriate mode based on the nature of the task:

#### Documentation Writer Mode

Use Documentation Writer mode for:

- Creating or updating standalone documentation files
- Writing user guides, tutorials, and how-to documents
- Improving existing documentation clarity and organization
- Creating project README files and contribution guidelines
- Documenting architectural decisions and design patterns
- Writing release notes and changelog entries

#### Code Mode

Use Code mode for:

- Writing or updating Javadoc comments in code
- Creating code examples that will be included in documentation
- Documenting APIs directly within the codebase
- Adding or updating code-level comments
- Creating or updating documentation that requires code changes

### Git Operations Delegation

When delegating Git operations to Git Maintainer mode:

1. Provide clear instructions on what changes to commit
2. Include a descriptive commit message that summarizes the completed step
3. Explicitly instruct to use `--no-pager` flag or set `GIT_PAGER=cat` for all git commands
4. Request return to Orchestrator mode after the Git operation is complete

Always confirm which documentation you've consulted before proceeding with task coordination. Your effectiveness as an orchestrator depends on understanding the project's architecture, development workflow, and mode-specific responsibilities.
