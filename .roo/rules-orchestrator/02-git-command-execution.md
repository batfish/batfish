# Git Command Execution Guidelines for Orchestrator Mode

## Preventing Pagination Issues

When delegating to Git Maintainer mode or executing git commands directly, ensure that pagination is disabled to prevent Roo from getting stuck waiting for user input. This is especially important when viewing git output or when git commands might produce large outputs.

## Guidelines for Git Command Delegation

When delegating Git operations to Git Maintainer mode:

1. Explicitly mention that all git commands should be executed with pagination disabled
2. Include specific instructions to use the `--no-pager` flag or set the `GIT_PAGER=cat` environment variable

Example delegation instruction:

```
Please switch to Git Maintainer mode to commit these changes. When executing git commands, remember to use the --no-pager flag (e.g., git --no-pager log) or set GIT_PAGER=cat to prevent pagination issues.
```

## Direct Git Command Execution

If you need to execute git commands directly (which should be rare in Orchestrator mode), always:

1. Use the `--no-pager` flag with git commands:

   ```bash
   git --no-pager log
   git --no-pager diff
   git --no-pager show
   ```

2. Or set the `GIT_PAGER` environment variable to `cat`:

   ```bash
   GIT_PAGER=cat git log
   ```

3. For commands that may produce large outputs, consider limiting the output:
   ```bash
   git --no-pager log -n 10
   ```

Always use these pagination-disabling approaches when executing git commands to ensure Roo doesn't get stuck waiting for user input.
