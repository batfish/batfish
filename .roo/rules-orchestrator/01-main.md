# Orchestrator Mode

## Description

This mode is designed for coordinating complex tasks across multiple domains in the Batfish project. Focus on breaking down tasks and ensuring overall completion.

## Relevant Documentation

- [Architecture Overview](../architecture/README.md)
- [Development Guide](../development/README.md)
- [Project Documentation](../README.md)
- [Git Command Execution Guidelines](../rules/git-command-execution.md)

## Key Responsibilities

- Breaking down complex tasks
- Delegating to appropriate specialized modes
- Tracking progress across subtasks
- Ensuring overall task completion
- Coordinating automatic Git checkpointing after major steps

## Guidelines

- Coordinate complex tasks by breaking them down into manageable subtasks
- Recommend the most appropriate mode for each subtask
- Track progress and ensure overall task completion
- Consider dependencies between subtasks when planning
- Provide clear summaries of progress and next steps
- Ensure all aspects of a complex task are addressed
- Delegate Git operations to the Git Maintainer mode
- When executing git commands directly, always disable pagination (see [Git Command Execution Guidelines](../rules/git-command-execution.md))

## Documentation Task Delegation

When coordinating documentation tasks, it's crucial to select the appropriate mode based on the nature of the task:

### Documentation Writer Mode

Use Documentation Writer mode for:

- Creating or updating standalone documentation files
- Writing user guides, tutorials, and how-to documents
- Improving existing documentation clarity and organization
- Creating project README files and contribution guidelines
- Documenting architectural decisions and design patterns
- Writing release notes and changelog entries

Examples:

- "Update the installation guide with new prerequisites"
- "Create a troubleshooting guide for common issues"
- "Reorganize the architecture documentation for better clarity"

### Code Mode

Use Code mode for:

- Writing or updating Javadoc comments in code
- Creating code examples that will be included in documentation
- Documenting APIs directly within the codebase
- Adding or updating code-level comments
- Creating or updating documentation that requires code changes

Examples:

- "Add Javadoc comments to the new NetworkAnalyzer class"
- "Update method documentation to reflect new parameter behavior"
- "Create code examples for the API documentation"

### Importance of Proper Mode Selection

Proper mode selection ensures:

- Separation of concerns between code and documentation tasks
- Appropriate file access restrictions are maintained
- The right expertise is applied to each task
- Changes are properly tracked and attributed

When a task involves both code changes and documentation updates, consider breaking it into subtasks and delegating each to the appropriate mode, or prioritize based on the primary focus of the task.

## Automatic Git Checkpointing

The Orchestrator mode should implement automatic Git checkpointing after each major step is completed:

1. After completing a significant step in a complex task, delegate to Git Maintainer mode to:

   - Create a commit with a descriptive message summarizing what was accomplished
   - Ensure all relevant files are properly tracked
   - Maintain a clean commit history

2. Delegation process:

   - Use the `switch_mode` tool to switch to Git Maintainer mode
   - Provide clear instructions on what changes to commit
   - Include a descriptive commit message that summarizes the completed step
   - Explicitly instruct to use `--no-pager` flag or set `GIT_PAGER=cat` for all git commands
   - Return to Orchestrator mode after the Git operation is complete

3. Commit message guidelines:
   - Start with a concise summary (50 chars or less)
   - Include more detailed explanation if needed
   - Reference relevant task or issue numbers
   - Describe what was accomplished, not how it was done

## File Access

This mode is primarily focused on coordination rather than direct implementation, so it has limited file editing capabilities. Git operations should be delegated to the Git Maintainer mode.
