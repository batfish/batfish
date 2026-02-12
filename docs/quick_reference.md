# Batfish Developer Quick Reference

This guide provides quick reference for developing and building Batfish.

**For user-facing Pybatfish documentation**, see [batfish.readthedocs.io](https://batfish.readthedocs.io/en/latest/quick_reference.html).

## Table of Contents

- [Bazel Commands](#bazel-commands)
- [Testing Commands](#testing-commands)
- [Code Formatting](#code-formatting)
- [Git Workflow](#git-workflow)
- [Keyboard Shortcuts](#keyboard-shortcuts)

---

## Bazel Commands

### Build

```bash
# Build everything
bazel build //...

# Build specific target
bazel build //projects/batfish/src/main/java/com/example:MyClass

# Build with optimizations
bazel build -c opt //...

# Build specific package
bazel build //projects/batfish/...
```

### Test

```bash
# Run all tests
bazel test //...

# Run specific test
bazel test //projects/batfish/src/test/java/com/example:MyTest

# Run specific test method
bazel test --test_filter=MyClass#myTestMethod //path/to:tests

# Run tests in verbose mode
bazel test --test_output=all //path/to:tests

# Run tests without cache
bazel test --nocache_test_results //path/to:tests

# Run multiple test targets
bazel test //projects/batfish:tests //projects/common:tests
```

### Run Batfish Server

```bash
# Start Batfish server
./tools/bazel_run.sh //projects/batfish:batfish

# Start with specific port
./tools/bazel_run.sh //projects/batfish:batfish -p 9999

# Start with custom settings
./tools/bazel_run.sh //projects/batfish:batfish -coordinatorArgs="-workerThreads 4"
```

### Bazel Utilities

```bash
# Clean build artifacts
bazel clean

# Clean everything (including external repos)
bazel clean --expunge

# Query dependencies
bazel query deps(//projects/batfish:batfish)

# Find all targets under a path
bazel query 'kind(.*_test, //projects/batfish/...)'

# Check configuration
bazel info

# Show Bazel version
bazel version
```

---

## Testing Commands

### Run Specific Tests

```bash
# Run a specific test class
bazel test \
  --test_filter=org.batfish.grammar.flatjuniper.JunosMplsAdminGroupTest \
  //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:tests

# Run a specific test method
bazel test \
  --test_filter=org.batfish.grammar.flatjuniper.JunosMplsAdminGroupTest#testAdminGroupDefinitions \
  //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:tests
```

### Run Reference Tests

```bash
# Update reference files
./tools/update_refs.sh

# Run reference tests
bazel test //tests/parsing-tests:ref_tests

# Run specific reference test
bazel test //tests/parsing-tests/org/batfish/parsing:parser_test
```

### Test Configuration

```bash
# Run with specific CPU count
bazel test --jobs=4 //...

# Run with memory limit
bazel test --test_timeout=300 //...

# Run tests and show output
bazel test --test_output=errors //...

# Run flaky test multiple times
bazel test --flaky_test_attempts=3 //path/to:test
```

---

## Code Formatting

### Pre-commit Hooks

```bash
# Install pre-commit
pip install pre-commit
pre-commit install

# Run pre-commit manually
pre-commit run --all-files

# Run on specific files
pre-commit run --files MyClass.java

# Update pre-commit hooks
pre-commit autoupdate
```

### Manual Formatting

```bash
# Format Java files
bazel run //tools/formatter:formatter -- --replace $(git ls-files '*.java')

# Check Java formatting (dry-run)
bazel run //tools/formatter:formatter -- --dry-run $(git ls-files '*.java')

# Format specific file
bazel run //tools/formatter:formatter -- --replace src/main/java/com/example/MyClass.java

# Format BUILD files
bazel run //tools/formatter:buildifier
```

---

## Git Workflow

### Feature Branch Workflow

```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "feat: add new feature"

# Push to remote
git push origin feature/my-feature

# Update branch with latest main
git fetch origin main
git rebase origin/main
```

### Interactive Rebase

```bash
# Squash last 3 commits
git rebase -i HEAD~3

# Clean up commit history
# Change "pick" to "squash" or "fixup" for commits to squash
# Change "pick" to "drop" for commits to remove

# Continue after fixing conflicts
git add .
git rebase --continue
```

### Resolve Merge Conflicts

```bash
# After conflict, mark files as resolved
git add <resolved-files>
git commit

# Or abort the merge
git merge --abort
```

---

## Keyboard Shortcuts

### IntelliJ IDEA (Recommended IDE)

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| Find class | ⌘O | Ctrl+N |
| Find file | ⇧⌘O | Ctrl+Shift+N |
| Find action | ⇧⌘A | Ctrl+Shift+A |
| Go to definition | ⌘B | Ctrl+B |
| Find usages | ⌥F7 | Alt+F7 |
| Format code | ⌘⌥L | Ctrl+Alt+L |
| Optimize imports | ⌃⌥O | Ctrl+Alt+O |
| Run | Ctrl+R | Shift+F10 |
| Debug | Ctrl+D | Shift+F9 |
| Toggle breakpoint | ⌘F8 | Ctrl+F8 |

### Vim

| Action | Command |
|--------|---------|
| Save | `:w` |
| Quit | `:q` |
| Save and quit | `:wq` |
| Undo | `u` |
| Redo | `Ctrl+r` |
| Find | `/pattern` |
| Replace | `:s/old/new` |

### Git Bash

| Action | Command |
|--------|---------|
| Status | `git status` |
| Log | `git log --oneline` |
| Diff | `git diff` |
| Branch | `git branch` |
| Checkout | `git checkout` |
| Commit | `git commit` |
| Push | `git push` |
| Pull | `git pull` |

---

## Summary

**Key resources:**
- **Bazel**: Build system for compiling and testing
- **Pre-commit**: Automatic code formatting
- **Git**: Version control

**Common workflows:**
1. Make code changes → Format with pre-commit → Test with Bazel → Commit

**User documentation:**
- [Pybatfish Quick Reference](https://batfish.readthedocs.io/en/latest/quick_reference.html) - For Batfish users
- [Pybatfish Performance Guide](https://batfish.readthedocs.io/en/latest/performance.html) - Performance tuning for users
- [Pybatfish Troubleshooting](https://batfish.readthedocs.io/en/latest/troubleshooting.html) - User troubleshooting guide

**Getting help:**
- [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/)
- [GitHub issues](https://github.com/batfish/batfish/issues)
