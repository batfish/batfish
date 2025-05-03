# Batfish Git Workflow Guide

This document outlines the Git workflow and best practices for the Batfish project.

## Commit Guidelines

### Commit Structure

- Each commit should represent a single logical change
- Keep commits small and focused
- Separate refactoring from feature changes
- Include tests with the code they test
- Ensure the project builds and tests pass after each commit

### Commit Messages

Batfish follows a structured commit message format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type** should be one of:

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes
- `style`: Formatting changes that don't affect code behavior
- `refactor`: Code changes that neither fix bugs nor add features
- `perf`: Performance improvements
- `test`: Adding or modifying tests
- `chore`: Changes to the build process or auxiliary tools

**Scope** (optional) indicates the module or component affected.

**Subject** is a short description of the change:

- Use imperative, present tense (e.g., "add" not "added" or "adds")
- Don't capitalize the first letter
- No period at the end
- Limit to 50 characters

**Body** (optional) provides detailed explanation:

- Use imperative, present tense
- Include motivation for the change
- Contrast with previous behavior
- Wrap at 72 characters

**Footer** (optional) contains references to issues and breaking changes:

- Reference issues with "Fixes #123" or "Closes #123"
- Indicate breaking changes with "BREAKING CHANGE:"

Example:

```
feat(parser): add support for Cisco ASA device

Implement grammar and extraction for Cisco ASA configurations.
This enables Batfish to parse and analyze ASA firewall configs.

Closes #456
```

## Branch Strategy

### Branch Naming

- Use descriptive names that reflect the purpose of the branch
- Include issue number when applicable
- Use kebab-case (lowercase with hyphens)
- Prefix with category when appropriate:
  - `feature/` for new features
  - `bugfix/` for bug fixes
  - `refactor/` for code refactoring
  - `docs/` for documentation changes

Examples:

- `feature/add-asa-support-123`
- `bugfix/fix-ospf-extraction-456`
- `refactor/simplify-route-filtering`
- `docs/update-contribution-guide`

### Branch Workflow

1. **Main Branch**: The `main` branch contains the latest stable code
2. **Feature Branches**: Create feature branches from `main` for new work
3. **Pull Requests**: Submit pull requests from feature branches to `main`
4. **Code Review**: All changes require code review before merging
5. **Merge Strategy**: Use squash merging to keep the main branch history clean

## When to Commit

- Commit when you complete a logical unit of work
- Commit when you want to save your progress
- Commit before making significant changes
- Commit after fixing a bug
- Commit when tests are passing

### When to Roll Back

Sometimes it's better to discard changes rather than commit them:

- When an approach isn't working out
- When you've made experimental changes you don't want to keep
- When you've accidentally committed sensitive information

```bash
# Discard all uncommitted changes
git reset --hard HEAD

# Discard changes to specific files
git checkout -- path/to/file

# Discard changes to specific files
git restore path/to/file
```

### Creating Checkpoints

When working on complex features, create checkpoint branches:

```bash
# Create a branch at this point for easy reference
git branch checkpoint/feature-name-date
```

### Interactive Rebase

Before submitting a pull request, clean up your commit history:

```bash
# Rebase the last N commits
git rebase -i HEAD~N

# Commands available in interactive rebase:
# p, pick = use commit
# r, reword = use commit, but edit the commit message
# e, edit = use commit, but stop for amending
# s, squash = use commit, but meld into previous commit
# f, fixup = like "squash", but discard this commit's log message
# d, drop = remove commit
```

### Cherry-picking

To apply specific commits from one branch to another:

```bash
# Cherry-pick a specific commit
git cherry-pick <commit-hash>
```

### Stashing

To temporarily save changes without committing:

```bash
# Stash changes
git stash save "description of changes"

# List stashes
git stash list

# Apply a stash (keeps the stash)
git stash apply stash@{0}

# Apply and remove a stash
git stash pop stash@{0}

# Remove a stash
git stash drop stash@{0}
```

## Git Hooks

Batfish uses Git hooks to enforce quality standards:

1. **pre-commit**: Runs code formatting and linting
2. **pre-push**: Runs tests before pushing
3. **commit-msg**: Validates commit message format

These hooks are managed using the pre-commit framework and are installed automatically when setting up the development environment.
