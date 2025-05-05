# Git Command Execution Guidelines

## Preventing Pagination Issues

When executing git commands, always disable pagination to prevent Roo from getting stuck waiting for user input. Use one of the following approaches:

1. Use the `--no-pager` flag with git commands:

   ```bash
   git --no-pager log
   git --no-pager diff
   git --no-pager show
   ```

2. Or set the `GIT_PAGER` environment variable to `cat` before executing git commands:

   ```bash
   GIT_PAGER=cat git log
   ```

3. For commands that may produce large outputs, consider limiting the output:
   ```bash
   git --no-pager log -n 10
   ```

## Examples of Common Git Commands with Pagination Disabled

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
```

Always use these pagination-disabling approaches when executing git commands to ensure Roo doesn't get stuck waiting for user input.
