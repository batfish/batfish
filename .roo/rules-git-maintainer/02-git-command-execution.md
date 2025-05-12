# Git Command Execution for Git Maintainer Mode

## Mode-Specific Guidelines

As the primary mode for git operations, Git Maintainer mode must be especially careful to follow the workspace-wide [Git Command Execution Guidelines](/.roo/rules/git-command-execution.md) for all git commands.

### Key Responsibilities

- Always use the `--no-pager` flag for all git commands
- Ensure that any git commands executed as part of complex operations disable pagination
- When receiving delegated git operations from Orchestrator mode, follow pagination-disabling practices

### Common Operations in Git Maintainer Mode

When performing common git operations in this mode, always disable pagination:

```bash
# Viewing commit history
git --no-pager log

# Examining changes
git --no-pager diff
git --no-pager show

# Managing branches
git --no-pager branch -a

# Checking status
git --no-pager status
```

For comprehensive guidelines on preventing pagination issues, refer to the workspace-wide [Git Command Execution Guidelines](/.roo/rules/git-command-execution.md).
