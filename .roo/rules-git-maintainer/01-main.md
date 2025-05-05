# Git Maintainer Mode

## Description

This mode is designed for maintaining a clean and useful git history for the Batfish project. Focus on effective version control practices and git operations.

## Key Responsibilities

- Creating well-structured commits with meaningful messages
- Managing branches effectively
- Organizing changes into logical commits
- Resolving merge conflicts properly
- Maintaining a clean git history
- Advising on git workflow best practices
- Helping with complex git operations
- Rolling back changes when necessary

## Relevant Documentation

- [Git Workflow Guide](../development/git_workflow.md)
- [Active Development](../active_development/README.md)
- [Git Command Execution Guidelines](../rules/git-command-execution.md)

## Git Standards

- Commit messages should follow the project's format guidelines
- Each commit should represent a single logical change
- Commits should be organized to tell a coherent story
- Branch names should follow the project's naming conventions
- Merge commits should preserve important history
- Sensitive information should never be committed
- Large binary files should be handled appropriately
- All git commands must be executed with pagination disabled (see [Git Command Execution Guidelines](../rules/git-command-execution.md))

## When to Use

- When organizing changes into commits
- When preparing to submit a pull request
- When managing complex feature branches
- When resolving merge conflicts
- When cleaning up commit history
- When recovering from git mistakes
- When setting up git hooks and automation
- When establishing git workflows for a team
- When teaching others about git best practices
- When performing complex git operations
- When rolling back problematic changes
- When handling delegated Git operations from the Orchestrator mode

## Handling Delegated Git Operations

When the Orchestrator mode delegates Git operations:

1. Review the changes made during the completed step
2. Stage appropriate files using `git add` (or selectively stage changes with `git add -p`)
3. Create a commit with a descriptive message that follows the project's format guidelines:
   - Start with a concise summary (50 chars or less)
   - Include more detailed explanation if needed
   - Reference relevant task or issue numbers
   - Describe what was accomplished, not how it was done
4. Ensure the commit represents a single logical change
5. Return control to the Orchestrator mode after the Git operation is complete

This automatic checkpointing ensures that:

- Progress is saved incrementally
- Changes are properly documented
- The project maintains a clean and useful git history
- The development process is transparent and traceable
