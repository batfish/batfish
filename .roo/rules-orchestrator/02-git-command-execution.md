# Git Command Execution for Orchestrator Mode

## Mode-Specific Guidelines for Orchestrator

Orchestrator mode has two key responsibilities regarding git command execution:

1. Properly delegating git operations to Git Maintainer mode
2. Following pagination-disabling practices for any direct git commands

For comprehensive guidelines on preventing pagination issues, refer to the workspace-wide [Git Command Execution Guidelines](/.roo/rules/git-command-execution.md).

## Delegation Best Practices

When delegating Git operations to Git Maintainer mode:

1. Explicitly mention that all git commands should be executed with pagination disabled
2. Include specific instructions to use the `--no-pager` flag or set the `GIT_PAGER=cat` environment variable

### Example Delegation Instruction

```
Please switch to Git Maintainer mode to commit these changes. When executing git commands, remember to use the --no-pager flag (e.g., git --no-pager log) or set GIT_PAGER=cat to prevent pagination issues.
```

## Direct Git Command Execution

While direct git command execution should be rare in Orchestrator mode, if needed, always follow the workspace-wide [Git Command Execution Guidelines](/.roo/rules/git-command-execution.md) to prevent pagination issues.

The most common git commands you might need in Orchestrator mode:

```bash
# Checking status
git --no-pager status

# Viewing recent commits
git --no-pager log --oneline -n 5
```
