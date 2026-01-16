#!/bin/bash
# Profile Java tests using async-profiler.
#
# Automatically locates the platform-specific profiler library and runs the
# specified test target with profiling enabled.
#
# Usage: ./tools/profiling/profile.sh <test_target> [output_file] [format]
#
# Examples:
#   ./tools/profiling/profile.sh //pkg:test
#   ./tools/profiling/profile.sh //pkg:test myprofile.jfr jfr
#   ./tools/profiling/profile.sh //pkg:test analysis.txt collapsed

set -e

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <test_target> [output_file] [format]"
    echo ""
    echo "Formats: html (default), jfr, collapsed"
    echo ""
    echo "Examples:"
    echo "  $0 //projects/allinone/src/test/java/org/batfish/e2e/isp:tests"
    echo "  $0 //projects/batfish:tests profile.html html"
    echo "  $0 //projects/batfish:tests profile.txt collapsed"
    exit 1
fi

TEST_TARGET="$1"
FORMAT="${3:-html}"
OUTPUT_FILE="${2:-profile.${FORMAT}}"
TMP_OUTPUT="/tmp/profile_$(date +%s).${FORMAT}"

echo "=== Profiling $TEST_TARGET ==="
echo ""

# Build the native library
bazel --quiet build //tools/profiling:native_libs >/dev/null 2>&1

# Query for the platform-specific library path
NATIVE_LIB=$(bazel --quiet cquery --output=files //tools/profiling:native_libs 2>/dev/null)
NATIVE_LIB_ABS=$(cd "$(dirname "$NATIVE_LIB")" && pwd)/$(basename "$NATIVE_LIB")

# Construct profiler arguments based on output format
case "$FORMAT" in
    collapsed)
        PROFILER_ARGS="start,event=cpu,file=${TMP_OUTPUT},collapsed"
        ;;
    jfr)
        PROFILER_ARGS="start,event=cpu,file=${TMP_OUTPUT},jfr"
        ;;
    *)
        PROFILER_ARGS="start,event=cpu,file=${TMP_OUTPUT}"
        ;;
esac

# Determine target type
TARGET_KIND=$(bazel --quiet query --output=label_kind "$TEST_TARGET" 2>/dev/null | cut -d' ' -f1)

# Execute target with profiling enabled
if [[ "$TARGET_KIND" == *"_test"* ]]; then
    bazel --quiet test \
        --test_output=streamed \
        --jvmopt=-agentpath:${NATIVE_LIB_ABS}=${PROFILER_ARGS} \
        "$TEST_TARGET"
else
    bazel --quiet run \
        --run_under= \
        "$TEST_TARGET" \
        -- -agentpath:${NATIVE_LIB_ABS}=${PROFILER_ARGS}
fi

# Copy profile to output location
if [[ ! -f "$TMP_OUTPUT" ]]; then
    echo "ERROR: Profile not created at $TMP_OUTPUT"
    exit 1
fi

cp "$TMP_OUTPUT" "$OUTPUT_FILE"
echo ""
echo "Profile created: $OUTPUT_FILE"
echo "Size: $(du -h "$OUTPUT_FILE" | cut -f1)"

case "$FORMAT" in
    html)
        echo ""
        echo "View: open $OUTPUT_FILE"
        ;;
    collapsed)
        echo ""
        echo "Analyze: bazel run //tools/profiling:analyze_profile -- hotspots $OUTPUT_FILE"
        ;;
esac
