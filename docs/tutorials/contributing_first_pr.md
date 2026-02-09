# Contributing Your First PR

**Time**: 30 minutes | **Difficulty**: Beginner

This tutorial walks you through the complete process of contributing to Batfish, from finding an issue to merging your first pull request.

## What You'll Build

You'll make a simple documentation fix or improvement to Batfish. This could be:
- Fixing a typo in documentation
- Adding a clarifying example
- Improving error messages
- Adding a missing test case

**Why start with documentation or tests?**
- Lower risk than code changes
- Easier to review
- Great way to learn the workflow
- Still valuable to the project!

---

## Prerequisites

Before starting, ensure you have:

1. **Git installed**: `git --version`
2. **Java 17 JDK**: `java -version` (should show 17.x.x)
3. **Bazelisk**: `bazelisk version`
4. **GitHub account**: [Create one here](https://github.com/signup)

**Don't have these set up yet?** Follow the [Building and Running guide](../building_and_running/README.md).

---

## Step 1: Find an Issue

### Good First Issues

Look for issues labeled `good first issue`:
1. Go to [Batfish GitHub Issues](https://github.com/batfish/batfish/issues)
2. Click on "Labels" → Select "good first issue"
3. Browse the list for something that interests you

**Alternative**: Look for issues labeled `documentation` or `help wanted`.

### Claim the Issue

Once you find an issue:
1. **Comment on the issue**: "I'd like to work on this"
2. **Wait for assignment**: Maintainers will assign it to you
3. **Ask questions**: If anything is unclear, ask in comments

---

## Step 2: Set Up Your Fork

### Fork the Repository

1. Go to [batfish/batfish](https://github.com/batfish/batfish)
2. Click "Fork" button (top right)
3. Choose your GitHub account

### Clone Your Fork

```bash
# Clone YOUR fork (replace YOUR_USERNAME)
git clone https://github.com/YOUR_USERNAME/batfish.git
cd batfish
```

### Add Upstream Remote

```bash
# Add the main Batfish repository as "upstream"
git remote add upstream https://github.com/batfish/batfish.git

# Verify remotes
git remote -v
# Should show:
# origin    https://github.com/YOUR_USERNAME/batfish.git (fetch)
# origin    https://github.com/YOUR_USERNAME/batfish.git (push)
# upstream  https://github.com/batfish/batfish.git (fetch)
# upstream  https://github.com/batfish/batfish.git (push)
```

---

## Step 3: Create a Branch

Always create a new branch for your work:

```bash
# Fetch latest from upstream
git fetch upstream

# Create a new branch from upstream/main
git checkout -b your-branch-name upstream/main

# Example:
# git checkout -b fix-typo-in-readme upstream/main
```

**Branch naming tips**:
- `fix-description`: For bug fixes
- `add-description`: For new features
- `docs-description`: For documentation changes
- `test-description`: For test additions

---

## Step 4: Make Your Changes

### Example: Fixing a Documentation Typo

1. **Find the file**:
   ```bash
   # Search for the file
   find . -name "*.md" -type f | grep -i readme
   ```

2. **Edit the file**:
   ```bash
   # Use your favorite editor
   vim README.md
   # or
   code README.md
   # or
   nano README.md
   ```

3. **Make your changes**:
   ```markdown
   # Before:
   Batfish is a network configuration anaylsis tool.

   # After:
   Batfish is a network configuration analysis tool.
   ```

### Example: Adding a Test

1. **Find the test file**:
   ```bash
   # Find test related to your change
   find projects -name "*Test.java" -type f | grep -i "your-class"
   ```

2. **Add a test case**:
   ```java
   @Test
   public void testNewFeature() {
       // Your test code here
       assertThat(result).isEqualTo(expected);
   }
   ```

---

## Step 5: Test Your Changes

### Build Batfish

```bash
# Build the project
bazel build //...

# If build fails, fix errors and rebuild
```

### Run Tests

```bash
# Run all tests (can take a while)
bazel test //...

# Or run specific tests related to your change
bazel test //projects/your/package/...
```

### Check Formatting

Batfish uses automatic code formatting:

```bash
# Java code is formatted via pre-commit hooks
# Install pre-commit (if not already installed)
pip install pre-commit

# Install hooks
pre-commit install

# Run manually
pre-commit run --all-files
```

**If formatting fails**:
```bash
# Let pre-commit fix formatting issues
pre-commit run --all-files --fix-all
```

---

## Step 6: Commit Your Changes

### Review Your Changes

```bash
# See what changed
git status
git diff

# Review each file
git diff README.md
```

### Stage and Commit

```bash
# Stage your changes
git add README.md

# Or stage all changes
git add .

# Commit with a clear message
git commit -m "Fix typo in README: anaylsis -> analysis"
```

**Commit message format**:
```
<type>: <description>

<optional body>
```

**Types**: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`

**Examples**:
```
docs: fix typo in README

Corrected spelling of "analysis" in project overview.
```

```
fix: handle null pointer in extractor

Added null check before accessing parse tree node.
Fixes #1234
```

---

## Step 7: Push Your Changes

```bash
# Push to YOUR fork (origin)
git push origin your-branch-name

# Example:
# git push origin fix-typo-in-readme
```

**First time pushing?** You may need to authenticate:
- GitHub will prompt you to authenticate
- Follow the provided instructions

---

## Step 8: Create Pull Request

### Open Pull Request

1. Go to your fork on GitHub: `https://github.com/YOUR_USERNAME/batfish`
2. You should see a banner: "your-branch-name is ready to push"
3. Click "Compare & pull request"
4. Or go to "Pull requests" → "New pull request"

### Fill PR Template

Batfish uses a PR template. Fill it out:

```markdown
## Description
Fixed typo in README: changed "anaylsis" to "analysis".

## Fixes
Fixes #1234

## Type of change
- [x] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [x] Documentation update

## How Has This Been Tested?
- [x] Verified change locally by viewing README.md
- [ ] Ran full test suite

## Checklist:
- [x] My code follows the style guidelines of this project
- [x] I have performed a self-review of my code
- [x] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [x] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [x] New and existing unit tests pass locally with my changes
- [x] I have tested this change on the following configurations:
  - [x] macOS
  - [ ] Ubuntu
```

### Review and Submit

1. **Review the diff**: Make sure it looks right
2. **Title**: Should match your commit message (e.g., "docs: fix typo in README")
3. **Click "Create pull request"**

---

## Step 9: Respond to Review Feedback

### Automated Checks

Your PR will run automated tests:
- **Build**: Must compile successfully
- **Tests**: All tests must pass
- **Linting**: Code must be properly formatted
- **Security**: No vulnerabilities introduced

**If checks fail**:
1. Click "Details" on failing check
2. Read the error message
3. Fix the issue locally
4. Commit and push changes
5. Checks will automatically re-run

### Human Review

A maintainer will review your PR. You may receive feedback:

**Common feedback types**:
1. **Request changes**: Fix issues before merging
2. **Suggestions**: Optional improvements
3. **Questions**: Clarifications needed
4. **Approval**: Ready to merge!

### Making Updates

```bash
# Make requested changes locally
vim README.md

# Test again
bazel test //...

# Commit changes
git add README.md
git commit -m "Address review feedback: clarify example"

# Push to your branch
git push origin your-branch-name
```

**No need to create new PR** - updates go to same branch.

---

## Step 10: Merge and Cleanup

### After Merge

Once your PR is approved and passes all checks:
1. Maintainer will merge your PR
2. You'll receive a notification
3. GitHub will show "Pull request merged"

### Update Your Fork

```bash
# Fetch latest from upstream
git fetch upstream

# Merge upstream/main into your local main
git checkout main
git merge upstream/main

# Push to your fork
git push origin main
```

### Delete Your Branch

```bash
# Delete local branch
git branch -d your-branch-name

# Delete remote branch
git push origin --delete your-branch-name
```

---

## Next Steps

Congratulations on your first PR! 🎉

### What to Do Next

1. **Find another issue**: Look for more `good first issue` labels
2. **Tackle something harder**: Try code changes, not just docs
3. **Learn more**: Read other tutorials:
   - [Writing a Custom Question](writing_custom_questions.md)
   - [Debugging Parser Issues](debugging_parser_issues.md)
4. **Join the community**: [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/)

### Common Mistakes to Avoid

❌ **Don't**: Commit directly to `main` branch
✅ **Do**: Always create a feature branch

❌ **Don't**: Ignore failing tests
✅ **Do**: Fix all failures before submitting

❌ **Don't**: Write vague commit messages
✅ **Do**: Use clear, descriptive messages

❌ **Don't**: Be afraid to ask questions
✅ **Do**: Comment on issues if you're unsure

---

## Getting Help

### If You Get Stuck

1. **Check existing docs**:
   - [Troubleshooting Guide](../troubleshooting.md)
   - [Development Guide](../development/README.md)

2. **Search for similar issues**:
   - Check closed PRs for examples
   - Look at commit history

3. **Ask for help**:
   - Comment on your PR or the issue
   - Join [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/)
   - Be specific about what you tried and what didn't work

---

## Quick Reference

### Essential Commands

```bash
# Setup
git remote add upstream https://github.com/batfish/batfish.git
git fetch upstream

# Branching
git checkout -b my-feature upstream/main

# Development
bazel build //...
bazel test //...
pre-commit run --all-files

# Committing
git add .
git commit -m "type: description"

# Pushing
git push origin my-feature

# Updating
git fetch upstream
git merge upstream/main
```

### Workflow Summary

```
1. Find issue → 2. Fork & clone → 3. Create branch
                                   ↓
8. Merge ← 7. Address feedback ← 6. Create PR ← 5. Commit
               ↓                                    ↑
            Update PR ← Fix issues ← Test changes ← 4. Make changes
```

---

## Related Documentation

- [Development Guide](../development/README.md)
- [Building and Running](../building_and_running/README.md)
- [Git Workflow](../development/git_workflow.md)
- [Contributing Guidelines](../../CONTRIBUTING.md)
