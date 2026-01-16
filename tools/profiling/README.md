# Profiling Tools

Tools for profiling Java code in Batfish using [async-profiler](https://github.com/async-profiler/async-profiler).

Supports both human use (interactive flame graphs) and automated use (machine-readable analysis). See [ARCHITECTURE.md](ARCHITECTURE.md) for design details.

## Quick Start

### Option 1: Helper Script (Recommended)

The helper script handles all setup automatically:

```bash
./tools/profiling/profile.sh <test_target> [output_file] [format]
```

Examples:
```bash
# Profile to HTML (default - interactive flame graph)
./tools/profiling/profile.sh //projects/allinone/src/test/java/org/batfish/e2e/isp:tests

# Profile to specific file
./tools/profiling/profile.sh //projects/batfish:tests my_profile.html

# Profile to collapsed format (for analysis)
./tools/profiling/profile.sh //projects/batfish:tests analysis.txt collapsed

# Profile to JFR format (for Java Mission Control)
./tools/profiling/profile.sh //projects/batfish:tests profile.jfr jfr
```

The script:
- Builds the native profiler library
- Locates the correct platform-specific library
- Runs your test with profiling enabled
- Saves the profile to your specified location

### Option 2: Direct Bazel Command

For more control, use Bazel directly:

```bash
# Find the platform-specific library
NATIVE_LIB=$(bazel cquery --output=files //tools/profiling:native_libs 2>/dev/null)
NATIVE_LIB_ABS=$(cd "$(dirname "$NATIVE_LIB")" && pwd)/$(basename "$NATIVE_LIB")

# Run test with profiler
bazel test \
  --test_output=streamed \
  --jvmopt=-agentpath:${NATIVE_LIB_ABS}=start,event=cpu,file=/tmp/profile.html \
  //your:test

# Copy profile from /tmp
cp /tmp/profile.html .
```

**Note:** Use `/tmp/` for output as Bazel's test sandbox restricts other locations.

## Output Formats

- **HTML** (default): Interactive flame graph for visual analysis
- **JFR**: Java Flight Recorder format for Java Mission Control
- **Collapsed**: Text format for scripting and CI/CD analysis

## Platform Support

- Linux x86_64
- Linux ARM64
- macOS (both Intel and Apple Silicon)

## Viewing Results

### HTML Output (Default)

Open the generated `.html` file in a web browser. The flame graph is interactive:
- Click on a frame to zoom in
- Mouse over frames to see details
- Use the search box to highlight specific methods

### JFR Output

Open with Java Mission Control:
```bash
jmc profile.jfr
```

### Collapsed Format

Convert to SVG with [FlameGraph](https://github.com/brendangregg/FlameGraph):
```bash
flamegraph.pl profile.txt > profile.svg
```

## Examples

Profile specific test targets:

```bash
# Profile BgpRib tests
./tools/profiling/profile.sh //projects/batfish:tests bgp_rib_profile.html

# Profile with collapsed format for analysis
./tools/profiling/profile.sh //projects/batfish:tests analysis.txt collapsed
```

## CI and Automation

### Analyze Hotspots

```bash
# Extract top hotspots as JSON
bazel run //tools/profiling:analyze_profile -- \
  hotspots profile.txt --format json --top 20 > hotspots.json

# Text output for quick checking
bazel run //tools/profiling:analyze_profile -- \
  hotspots profile.txt --top 10
```

Output:
```json
{
  "total_samples": 15432,
  "hotspots": [
    {
      "method": "org.batfish.dataplane.protocols.BgpProtocolHelper.processRoutes",
      "samples": 2341,
      "percent": 15.17
    },
    ...
  ]
}
```

### Compare Profiles (Regression Detection)

```bash
# Compare current run against baseline
bazel run //tools/profiling:analyze_profile -- \
  compare current.txt --baseline baseline.txt --format json > regression.json

# Text output
bazel run //tools/profiling:analyze_profile -- \
  compare current.txt --baseline baseline.txt
```

Output shows regressions:
```json
{
  "baseline_samples": 10000,
  "current_samples": 12000,
  "sample_delta_percent": 20.0,
  "regressions": [
    {
      "stack": "org.batfish.dataplane.BgpRib.mergeRoute",
      "baseline_percent": 5.2,
      "current_percent": 12.8,
      "delta_percent": 7.6
    }
  ]
}
```

### CI Integration Example

```yaml
# .github/workflows/performance.yml
- name: Profile and check for regressions
  run: |
    # Generate profile
    ./tools/profiling/profile.sh //critical:tests profile.txt collapsed

    # Compare against baseline
    bazel run //tools/profiling:analyze_profile -- \
      compare profile.txt --baseline baseline.txt --format json > regression.json

    # Fail if significant regressions detected
    python3 -c "
    import json, sys
    with open('regression.json') as f:
        data = json.load(f)
        if any(r['delta_percent'] > 10 for r in data['regressions']):
            sys.exit(1)
    "
```

## References

- [ARCHITECTURE.md](ARCHITECTURE.md) - Design and integration guide
- [async-profiler documentation](https://github.com/async-profiler/async-profiler)
- [Profiling guide](https://krzysztofslusarski.github.io/2022/12/12/async-manual.html)
- [Flame graph interpretation](http://www.brendangregg.com/flamegraphs.html)
