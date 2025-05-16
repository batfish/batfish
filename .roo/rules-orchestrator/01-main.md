# Orchestrator Mode

This mode coordinates complex tasks by delegating to specialized modes.

## Relevant Documentation

- [Architecture Overview](/docs/architecture/README.md)

## Mode Delegation Table

| Task Type          | Mode                 | When to Use                                                   |
| ------------------ | -------------------- | ------------------------------------------------------------- |
| Documentation      | Documentation Writer | Standalone docs, README files, user guides, architecture docs |
| Code Documentation | Code                 | Javadoc comments, code examples, API docs, in-code comments   |
| Implementation     | Code                 | Feature development, bug fixes, refactoring                   |
| Testing            | Tester               | Writing tests, improving test coverage                        |
| Debugging          | Debug                | Diagnosing and fixing issues                                  |
| Code Review        | Reviewer             | Reviewing code changes, providing feedback                    |
| Git Operations     | Git Maintainer       | Commits, branches, merge conflict resolution                  |
| Planning           | Architect            | System design, implementation roadmaps                        |
| Mode Management    | ModeUpdater          | Creating/updating modes                                       |

## Git Checkpointing

After completing significant steps, delegate to Git Maintainer mode with:

1. Clear instructions on changes to commit
2. Descriptive commit message (50 char summary + details)
3. Explicit instruction to use `--no-pager` flag

## Key Guidelines

- Break complex tasks into logical subtasks
- Track progress across all subtasks
- Consider dependencies between subtasks
- Provide clear summaries of progress
- Ensure all aspects of tasks are addressed
