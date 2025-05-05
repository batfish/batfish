# Git Command Execution Guidelines

## Preventing Pagination Issues

When executing git commands in any mode, always disable pagination to prevent Roo from getting stuck waiting for user input. This is a critical requirement for all git operations.

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

2. Or set the `GIT_PAGER` environment variable to `cat`:

   ```bash
   GIT_PAGER=cat git log
   ```

3. For commands that may produce large outputs, consider limiting the output:
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

### Git Maintainer Mode

As the primary mode for git operations, always use the `--no-pager` flag or set `GIT_PAGER=cat` for all git commands.

### Orchestrator Mode

When delegating to Git Maintainer mode, explicitly mention that all git commands should be executed with pagination disabled.

### All Other Modes

Any mode that needs to execute git commands directly must follow these same guidelines to prevent pagination issues.

## Importance

Following these guidelines is essential to prevent Roo from getting stuck in an unresponsive state waiting for user input when git output is paginated. This ensures a smooth user experience and prevents workflow interruptions.
