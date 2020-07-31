#!/usr/bin/env bash

set -euo pipefail

# Overwrite the version we ship with the latest.
curl https://ip-ranges.amazonaws.com/ip-ranges.json -o projects/batfish/src/main/resources/org/batfish/representation/aws/ip-ranges.json

# Regenerate references, failing if they do not need regenerating
bazel test --keep_going //tests/{aws,parsing-tests}:ref_tests && exit 1 || true

# Patch and then rerun, which now should pass
cat $(find $(bazel info bazel-testlogs)/tests/{parsing-tests,aws} -name 'test.log') | patch -p0
bazel test --test_tag_filters=-z3 //tests/{aws,parsing-tests}:ref_tests
