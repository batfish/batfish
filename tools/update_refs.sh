#!/usr/bin/env bash

set -euo pipefail

# Make sure running from project root
cd "$(dirname $0)/.."

# Run all tests to make sure the refs are updated. If all are up to date, just exit.
bazel test --keep_going //tests/... && echo "All refs are up to date" && exit 0 || true

# Patch all the refs.
cat $(find $(bazel info bazel-testlogs)/tests/ -name 'test.log') | patch -p0

# Patch and then rerun, which now should pass.
bazel test --keep_going //tests/... && echo "Updated refs and now tests pass" && exit 0

# If not, the tests are non-deterministic
echo "The ref tests seem not to be deterministic, please run 'git diff' to see what is changed."
exit 1
