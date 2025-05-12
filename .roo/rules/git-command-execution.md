# Git Command Execution Guidelines

## Introduction

These guidelines provide essential instructions for executing git commands across all Roo modes in the Batfish project. Following these guidelines is critical to ensure smooth operation of Roo when working with git.

## Preventing Pagination Issues

When executing git commands in any mode, always disable pagination to prevent Roo from getting stuck waiting for user input. By default, many git commands use a pager (like `less`) for output, which requires user interaction to continue. Since Roo cannot interact with these pagers, it will become unresponsive until the user manually intervenes.

This is a critical requirement for all git operations, regardless of which mode is executing them.

## Standard Approaches

Use one of these approaches for all git commands:

1. Use the `--no-pager` flag (preferred method):

   ```bash
   git --no-pager log
   git --no-pager diff
   git --no-pager show
   git --no-pager branch
   git --no-pager status
   ```

2. For commands that may produce large outputs, consider limiting the output:
   ```bash
   git --no-pager log -n 10
   ```

## Common Git Commands with Pagination Disabled

```bash
# View commit history without pagination
git --no-pager log

# Show differences without pagination
git --no-pager diff

# Show commit details without pagination
git --no-pager show

# List branches without pagination
git --no-pager branch -a

# Show status without pagination
git --no-pager status

# Show remote information without pagination
git --no-pager remote -v
```

## Implementation in Different Modes

Each mode has specific responsibilities regarding git command execution:

### Git Maintainer Mode

As the primary mode for git operations, Git Maintainer mode must be especially careful to follow these guidelines. See the [Git Maintainer mode-specific guidelines](/.roo/rules-git-maintainer/02-git-command-execution.md) for detailed instructions.

### Orchestrator Mode

Orchestrator mode has two key responsibilities:

1. Properly delegating git operations to Git Maintainer mode
2. Following pagination-disabling practices for any direct git commands

See the [Orchestrator mode-specific guidelines](/.roo/rules-orchestrator/02-git-command-execution.md) for detailed instructions on delegation.

### All Other Modes

Any mode that needs to execute git commands directly must follow these same guidelines to prevent pagination issues. When in doubt, use the `--no-pager` flag with all git commands.

## Importance and Troubleshooting

Following these guidelines is essential to prevent Roo from getting stuck in an unresponsive state waiting for user input when git output is paginated. This ensures a smooth user experience and prevents workflow interruptions.

If Roo becomes unresponsive after executing a git command, it's likely because pagination was not disabled. The user will need to manually interact with the terminal to exit the pager (typically by pressing 'q') before Roo can continue.

## Additional Resources

- [Git Documentation on Environment Variables](https://git-scm.com/book/en/v2/Git-Internals-Environment-Variables)
- [Git Documentation on Pager Configuration](https://git-scm.com/docs/git-config#Documentation/git-config.txt-corepager)
