# PR Preparation Guide

This guide covers the complete process of preparing a Pull Request (PR) for submission to the Batfish project. Following these steps ensures your PR will pass CI checks and be ready for efficient code review.

## Table of Contents

- [Pre-Commit Checklist](#pre-commit-checklist)
- [Code Formatting](#code-formatting)
- [Running Tests](#running-tests)
- [Updating Reference Files](#updating-reference-files)
- [Pre-Push Verification](#pre-push-verification)
- [Creating the PR](#creating-the-pr)
- [After Submission](#after-submission)

---

## Pre-Commit Checklist

Before committing your changes, ensure:

- [ ] Code follows [coding standards](coding_standards.md)
- [ ] All new code includes appropriate tests
- [ ] Tests pass locally
- [ ] Reference files are updated (if needed)
- [ ] Documentation is updated (if needed)
- [ ] Commit messages follow [guidelines](git_workflow.md)

---

## Code Formatting

### Setting Up Pre-Commit Hooks

Batfish uses **pre-commit** to automatically format and check code before commits. **This is required** - if you skip this step, your PR will likely fail CI checks.

#### Prerequisites

1. **Python 3.10+**: Required for pre-commit
2. **pip**: Python package installer

#### Step-by-Step Setup

**Option 1: Quick Setup (System Python)**

```bash
# Navigate to batfish repository
cd /path/to/batfish

# Install pre-commit
pip install pre-commit

# Install pre-commit hooks in your repository
pre-commit install
```

**Option 2: Recommended Setup (Using pyenv and Virtualenv)**

For better isolation and dependency management:

```bash
# 1. Install pyenv if not already installed
# Follow instructions at: https://github.com/pyenv/pyenv-installer#install

# 2. Install Python 3.10.18 (or newer)
pyenv install 3.10.18

# 3. Create a virtualenv for pre-commit
pyenv virtualenv 3.10.18 pre-commit

# 4. Activate the virtualenv
pyenv activate pre-commit

# 5. Install pre-commit
pip install pre-commit

# 6. Navigate to batfish repository
cd /path/to/batfish

# 7. Install pre-commit hooks
pre-commit install
```

#### Verifying Installation

```bash
# Check pre-commit is installed
pre-commit --version

# List installed hooks
pre-commit run --list-files

# Verify hooks are installed in git
ls .git/hooks/pre-commit
```

#### What Pre-Commit Does

Batfish's pre-commit configuration (`.pre-commit-config.yaml`) runs:

1. **java-format**: Runs `google-java-format` on all Java files
   - Enforces Google Java Style Guide
   - Sorts and deduplicates imports
   - Fixes spacing and indentation

2. **buildifier**: Formats Bazel BUILD files
   - Standardizes BUILD file formatting
   - Sorts targets alphabetically

3. **black**: Formats Python files
   - Enforces PEP 8 style
   - Consistent formatting across codebase

4. **isort**: Sorts Python imports
   - Organizes imports alphabetically
   - Separates standard library, third-party, and local imports

5. **autoflake**: Removes unused Python imports and variables
   - Cleans up unused imports
   - Removes unused variables

#### How Pre-Commit Works

When you run `git commit`:
1. Pre-commit scans staged files
2. Runs applicable hooks on matching files
3. If any hook fails, the commit is blocked
4. Pre-commit applies fixes automatically where possible
5. You review changes with `git diff`
6. Stage fixed files and commit again

**Example workflow:**
```bash
# Make some changes
vim MyClass.java

# Stage changes
git add MyClass.java

# Commit - pre-commit runs automatically
git commit -m "feat: add new feature"

# Output:
# java-format............................Failed
# - hook id: java-format
# - files were modified by this hook

# Review the automatic formatting
git diff

# If changes look good, stage them
git add .

# Commit again
git commit -m "feat: add new feature"
# Success!
```

### Running Pre-Commit Manually

Sometimes you want to run pre-commit without committing:

**Run on all files:**
```bash
pre-commit run --all-files
```

**Run on specific files:**
```bash
pre-commit run --files MyClass.java
pre-commit run --files src/main/java/com/example/*.java
```

**Run only specific hooks:**
```bash
pre-commit run java-format --all-files
pre-commit run buildifier --all-files
```

**Show which files would be checked:**
```bash
pre-commit run --list-files
```

### Skipping Pre-Commit (Not Recommended)

If you absolutely need to skip pre-commit (not recommended - CI will fail anyway):

```bash
git commit --no-verify -m "message"
```

**Warning:** This bypasses important quality checks and should only be used in exceptional cases (e.g., documentation-only changes where you're confident formatting is correct).

### Updating Pre-Commit Hooks

When the repository's pre-commit configuration changes:

```bash
# Update to latest hooks
pre-commit autoupdate

# Re-install hooks
pre-commit install
```

### Troubleshooting Pre-Commit

**Issue: "pre-commit: command not found"**

**Solution:** Pre-commit is not installed or not in PATH
```bash
# Verify installation
which pre-commit

# If not found, install it
pip install pre-commit

# Or if using virtualenv
pyenv activate pre-commit  # or: source /path/to/venv/bin/activate
```

**Issue: Pre-commit is very slow**

**Solutions:**
- Run on specific files only: `pre-commit run --files path/to/file`
- Use `--verbose` to see which hook is slow: `pre-commit run --verbose --all-files`
- Consider skipping `buildifier` for full runs (it's a global check)

**Issue: Hook fails but you don't understand why**

**Solution:** Run with verbose output
```bash
pre-commit run --verbose --all-files
```

**Issue: Pre-commit modifies files but doesn't stage them**

This is expected behavior - you need to review and stage changes manually:
```bash
# See what changed
git diff

# Stage the changes
git add .

# Commit again
git commit -m "message"
```

**Issue: Pre-commit passes but CI fails on formatting**

This can happen if your pre-commit version is out of sync:
```bash
# Update pre-commit hooks
pre-commit autoupdate
pre-commit install

# Run again
pre-commit run --all-files
```

**Issue: "Permission denied" when running hooks**

The pre-commit scripts may need execute permissions:
```bash
chmod +x tools/fix_java_format.sh
```

**Issue: Python version incompatibility**

Pre-commit requires Python 3.10 or later:
```bash
# Check Python version
python --version

# If using pyenv, ensure correct version
pyenv version
pyenv local 3.10.18
```

### Manual Formatting (If Pre-Commit Fails)

If you need to format code manually without pre-commit:

**Java files:**
```bash
# Format all Java files
bazel run //tools/formatter:formatter -- --replace $(git ls-files '*.java')

# Format specific files
bazel run //tools/formatter:formatter -- --replace path/to/file.java
```

**Markdown/JSON/YAML:**
```bash
# Run prettier
npx prettier --write '**/*.{md,json,yaml,yml}'
```

### Formatting Verification

Check if files need formatting:
```bash
# Check Java formatting (dry-run)
bazel run //tools/formatter:formatter -- --dry-run $(git ls-files '*.java')

# Check prettier formatting
npx prettier --check '**/*.{md,json,yaml,yml}'
```

---

## Running Tests

### Test Strategy Overview

Batfish uses multiple types of tests:
- **Unit tests**: Test individual classes and methods
- **Reference tests**: Compare parsing/conversion output against known-good references
- **Integration tests**: Test multi-component interactions

### Running Tests Before Commit

**1. Run affected tests first** (recommended workflow):

```bash
# Run a specific test
bazel test --test_filter=MyClass#myTestMethod //path/to:tests

# Run all tests in a class
bazel test --test_filter=MyClass //path/to:tests

# Run all tests in a package
bazel test //projects/batfish/src/test/java/org/batfish/grammar/...
```

**2. Run broader test suite:**

```bash
# Run all tests in a project
bazel test //projects/batfish:tests

# Run all parsing tests
bazel test //projects/batfish/src/test/java/org/batfish/grammar/...

# Run all tests (only after specific tests pass)
bazel test //...
```

### Test Configuration

**Run specific test with verbose output:**
```bash
bazel test --test_output=all //path/to:test_target
```

**Run tests with cache disabled:**
```bash
bazel test --nocache_test_results //path/to:test_target
```

**Run multiple test targets in parallel:**
```bash
bazel test //projects/batfish:tests //projects/common:tests
```

### When Tests Fail

1. **Check the test output** - Read the error message carefully
2. **Run the test locally** - Reproduce the failure
3. **Fix the issue** - Update code or test as needed
4. **Re-run the test** - Verify the fix
5. **Check for side effects** - Run related tests to ensure no regressions

---

## Updating Reference Files

Reference tests compare actual output against stored reference files. When behavior changes intentionally, you must update reference files.

### When to Update References

Update reference files when:
- Adding new test configurations
- Intentionally changing parsing/extraction behavior
- Adding new fields to data structures
- Fixing bugs that change test output

**DO NOT update references when:**
- Introducing a bug that causes test failures
- Breaking existing functionality
- The change should be backward-compatible

### Running update_refs.sh

The `./tools/update_refs.sh` script updates reference files automatically:

```bash
./tools/update_refs.sh
```

**What the script does:**
1. Runs all tests in the `//tests` directory
2. If tests fail due to reference mismatches, patches the reference files
3. Re-runs tests to verify they now pass
4. Warns if tests are non-deterministic

### Manual Reference Updates

For individual test files:

```bash
# Run specific reference test
bazel test //tests/parsing-tests:ref_tests

# If it fails, the log shows the diff
# Review the diff to ensure changes are expected
```

### Reviewing Reference Changes

After running `update_refs.sh`:

```bash
# Review what changed
git diff tests/

# Look for unexpected changes
git diff tests/parsing-tests/sometest/
```

**Things to check:**
- Are all changes explained by your PR?
- Are there unexpected files modified?
- Do the changes make sense given your code changes?
- Are you introducing test nondeterminism?

### Committing Reference Updates

**Option 1: Include with your code changes**
```bash
git add .
git commit -m "feat(parser): add support for new command"
```

**Option 2: Separate commit for references**
```bash
git add src/ tests/
git commit -m "feat(parser): add support for new command

- Implement parser for new command
- Update reference tests for new behavior"
```

---

## Pre-Push Verification

Before pushing your changes (and creating a PR), perform these final checks:

### 1. Code Quality Checks

```bash
# Ensure all formatting is applied
pre-commit run --all-files

# Or manually
bazel run //tools/formatter:formatter -- --replace $(git ls-files '*.java')
```

### 2. Build Check

```bash
# Ensure everything builds
bazel build //...
```

### 3. Test Check

```bash
# Run all affected tests
bazel test //projects/batfish/...
bazel test //projects/common/...
# Or run everything if changes are widespread
bazel test //...
```

### 4. Reference Test Check

If your changes affect parsing or conversion:

```bash
# Run reference tests
bazel test //tests/...

# If needed, update references
./tools/update_refs.sh

# Verify references pass again
bazel test //tests/...
```

### 5. Documentation Check

If your changes affect user-facing functionality:
- Updated relevant documentation
- Added examples for new features
- Updated README or guides

### 6. Clean Up History

Before creating a PR, clean up your commit history:

```bash
# Interactive rebase to squash/fixup commits
git rebase -i HEAD~n  # where n is number of commits

# Ensure each commit is:
# - Buildable
# - Tested
# - Logically complete
```

### 7. Sync with Main

Ensure your branch is up to date with main:

```bash
# Fetch latest main
git fetch origin main

# Rebase your branch on main
git rebase origin/main

# Fix any conflicts
# Run tests again
bazel test //...
```

---

## Creating the PR

### 1. Push Your Changes

```bash
# Push your branch to origin
git push origin feature/your-branch-name

# Or with --force if rebased
git push --force origin feature/your-branch-name
```

### 2. Create Pull Request on GitHub

1. Go to https://github.com/batfish/batfish
2. Click "Pull Requests" → "New Pull Request"
3. Select your branch
4. Provide a clear title and description

### PR Title Format

Use the same format as commit messages:

```
type(scope): subject
```

Examples:
- `feat(parser): add support for Juniper new command`
- `fix(conversion): correct BGP neighbor extraction`
- `docs(parsing): clarify lexer mode patterns`

### PR Description Template

```markdown
## Summary
<!-- Brief description of changes -->

## Changes
<!-- List of specific changes -->
- Added support for X
- Fixed bug in Y
- Updated documentation

## Test Plan
<!-- How you tested the changes -->
- [ ] Unit tests pass
- [ ] Reference tests updated (if applicable)
- [ ] Manual testing performed (if applicable)

## Checklist
- [ ] Code follows [coding standards](docs/development/coding_standards.md)
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] All tests pass locally
- [ ] Reference files updated (if needed)

## Related Issues
<!-- Fixes #123, Closes #456, etc. -->
```

### PR Best Practices

**DO:**
- Keep PRs focused on a single issue
- Provide clear descriptions of changes
- Link to related issues
- Include test plans
- Respond to review comments promptly
- Add reviewers who are familiar with the code

**DON'T:**
- Mix unrelated changes
- Skip testing
- Ignore review comments
- Create massive PRs (break them up instead)
- Push directly to main

---

## After Submission

### Responding to Review Comments

1. **Address each comment** - Either make changes or explain why not
2. **Use GitHub's review features** - Reply inline to specific comments
3. **Request clarification** - If something is unclear
4. **Keep discussion civil** - Focus on technical merits

### Making Changes After Review

```bash
# Make changes locally
git commit -m "fix(conversion): address review comments"

# Push to update PR
git push origin feature/your-branch-name
```

### CI Failures

If CI fails after you've pushed:

1. **Check the CI logs** - Understand what failed
2. **Reproduce locally** - Run the same tests
3. **Fix the issue** - Update code or tests
4. **Push again** - CI will re-run

Common CI failures:
- **Flaky tests**: Tests that fail intermittently - report these
- **Platform-specific issues**: Some tests only fail on certain platforms
- **Resource exhaustion**: Tests timeout - may need performance optimization

### CI Checks

Batfish uses GitHub Actions for CI. Checks include:
- **Build**: Ensures code compiles
- **Tests**: Runs full test suite
- **Coverage**: Tracks code coverage
- **Linting**: Checks formatting and style
- **Security**: Scans for vulnerabilities

### After Merge

Once your PR is merged:

1. **Delete your branch** (optional but recommended)
   ```bash
   git branch -d feature/your-branch-name
   git push origin --delete feature/your-branch-name
   ```

2. **Update your local main**
   ```bash
   git fetch origin main
   git checkout main
   git pull origin main
   ```

3. **Start on next issue** - Create a new branch from updated main

---

## Troubleshooting

### Pre-commit Issues

**Pre-commit is slow:**
```bash
# Run on specific files only
pre-commit run --files path/to/file.java

# Skip pre-commit (not recommended)
git commit --no-verify -m "message"
```

**Pre-commit fails:**
- Read the error message carefully
- Fix the issue manually
- Run `pre-commit run --all-files` again

### Bazel Build Issues

**Out-of-date build:**
```bash
# Clean and rebuild
bazel clean --expunge
bazel build //...
```

**Bazelisk version issues:**
```bash
# Check Bazel version
bazelisk version

# Update Bazel version in .bazelversion if needed
```

### Test Failures

**Flaky test:**
```bash
# Run multiple times to confirm flakiness
for i in {1..10}; do
  bazel test //path/to:test
done

# Report flaky tests with reproduction steps
```

**Reference test nondeterminism:**
- Check if test output includes timestamps or ordering
- Look for HashMap/HashSet usage (use LinkedHashMap/LinkedHashSet)
- Sort collections before comparison

### Large PRs

If your PR becomes too large:

1. **Split into multiple PRs**
   - PR1: Core functionality
   - PR2: Additional features
   - PR3: Documentation updates

2. **Use feature branches**
   ```bash
   # Create branches from your feature branch
   git checkout feature/main
   git checkout feature/additional-feature

   # Submit separate PRs for each
   ```

---

## Additional Resources

- [Building and Running](../building_and_running/README.md): Build system details
- [Testing Guide](testing_guide.md): Comprehensive testing information
- [Coding Standards](coding_standards.md): Style and conventions
- [Git Workflow](git_workflow.md): Git best practices
- [Contributing](../contributing/README.md): Contribution guidelines

---

## Quick Reference

### Common Commands

```bash
# Format code
pre-commit run --all-files

# Run specific test
bazel test --test_filter=MyClass#myMethod //path/to:tests

# Run all tests
bazel test //...

# Update reference files
./tools/update_refs.sh

# Push changes
git push origin feature/my-feature

# Create PR (via GitHub CLI)
gh pr create --title "feat: description" --body "Details..."
```

### PR Preparation Checklist

- [ ] Code formatted with `pre-commit`
- [ ] Build succeeds: `bazel build //...`
- [ ] Unit tests pass: `bazel test //...`
- [ ] Reference tests pass: `bazel test //tests/...`
- [ ] References updated if needed: `./tools/update_refs.sh`
- [ ] Documentation updated (if applicable)
- [ ] Commit history cleaned up
- [ ] Branch rebased on latest main
- [ ] Clear PR title and description
- [ ] Related issues linked
