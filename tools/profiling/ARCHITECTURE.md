# Profiling Architecture

Design of the profiling infrastructure for both human and automated use.

## Design Goals

1. **Human-friendly**: Interactive flame graphs for performance investigation
2. **Bot-friendly**: Machine-readable output for CI/CD and regression detection
3. **Bazel-native**: Use Bazel targets, not external scripts when possible
4. **Low overhead**: Sampling-based profiling with <2% overhead
5. **Multi-format**: HTML (humans), JFR (tooling), collapsed (scripting)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Profiling Infrastructure                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────┐         ┌────────────────┐                  │
│  │  Human Users   │         │  Bots/CI       │                  │
│  └────────┬───────┘         └────────┬───────┘                  │
│           │                          │                           │
│           ▼                          ▼                           │
│  ┌────────────────┐         ┌────────────────┐                  │
│  │ Flame Graphs   │         │ JSON Reports   │                  │
│  │ (HTML)         │         │ (analysis)     │                  │
│  └────────┬───────┘         └────────┬───────┘                  │
│           │                          │                           │
│           └──────────┬───────────────┘                           │
│                      │                                           │
│                      ▼                                           │
│           ┌──────────────────┐                                   │
│           │  Profiling Layer │                                   │
│           ├──────────────────┤                                   │
│           │ profile.sh       │ ← Shell script                    │
│           │ analyze_profile  │ ← Analysis tools                  │
│           └────────┬─────────┘                                   │
│                    │                                             │
│                    ▼                                             │
│           ┌──────────────────┐                                   │
│           │  async-profiler  │                                   │
│           │  (native)        │ ← v4.1, multiple platforms        │
│           └──────────────────┘                                   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Components

### 1. Native Libraries (`//tools/profiling:native_libs`)

Platform-specific async-profiler libraries from Maven:
- Linux x86_64: `tools.profiler:async-profiler:4.2:linux-x64`
- Linux ARM64: `tools.profiler:async-profiler:4.2:linux-arm64`
- macOS: `tools.profiler:async-profiler:4.2:macos`

### 2. Analysis Tools

#### Human Analysis: Interactive Flame Graphs

```bash
./tools/profiling/profile.sh //your:test [output.html]
```

Output: HTML file with interactive flame graph visualization

#### Automated Analysis: Machine-Readable Formats

```bash
# Generate collapsed format
./tools/profiling/profile.sh //your:test profile.txt collapsed

# Analyze hotspots
bazel run //tools/profiling:analyze_profile -- \
  hotspots profile.txt --format json

# Compare profiles for regressions
bazel run //tools/profiling:analyze_profile -- \
  compare current.txt --baseline baseline.txt --format json
```

Output: JSON with hotspots, regressions, and metrics

## Output Formats

### HTML (Human-Friendly)

Interactive flame graph with zoom, search, and hover details.

**Use case**: Performance investigation by developers

### JFR (Tool-Friendly)

Java Flight Recorder format for Java Mission Control and JFR API.

**Use case**: Integration with existing Java tooling

### Collapsed (Bot-Friendly)

Text format with stack traces and sample counts:
```
frame1;frame2;frame3 count
frame1;frame4 count
```

**Use case**: CI/CD analysis, regression detection, scripting

## Usage Patterns

### Pattern 1: Developer Investigation

1. Developer notices slow performance
2. Run: `./tools/profiling/profile.sh //their:test`
3. Open generated HTML in browser
4. Identify hotspots visually
5. Fix and re-profile to verify

### Pattern 2: CI Regression Detection

```yaml
# .github/workflows/profile.yml
- name: Profile and analyze
  run: |
    ./tools/profiling/profile.sh \
      //critical:tests critical_tests.txt collapsed

    # Compare against baseline
    bazel run //tools/profiling:analyze_profile -- \
      compare critical_tests.txt \
      --baseline baseline_profiles/critical_tests.txt \
      --format json > regression_report.json

    # Check for significant regressions
    python check_regressions.py regression_report.json
```

### Pattern 3: Performance Dashboard

```python
# Automated profiling and metrics collection
profiles = []
for test in performance_tests:
    run_profiled_test(test)
    metrics = extract_metrics(test.profile)
    profiles.append(metrics)

# Upload to time-series DB
upload_to_dashboard(profiles)
```

## Integration Points

### CI/CD Integration

1. **GitHub Actions**: Add profiling step to workflows
2. **Profile Storage**: Store profiles as build artifacts
3. **Regression Detection**: Compare against main branch
4. **Alerts**: Notify on significant regressions

### Development Workflow

1. **Local profiling**: Run helper scripts
2. **Code review**: Attach profiles to PRs for performance changes
3. **Benchmarking**: Regular profiling of critical paths
4. **Documentation**: Reference profiles in design docs

## Best Practices

### Human Use

- Use HTML output for interactive exploration
- Focus on hot frames (top of flame graph)
- Compare before/after profiles visually

### Automated Use

- Use collapsed format for analysis
- Store baseline profiles for comparison
- Set regression thresholds (e.g., >5% increase)
- Track metrics over time

### General

- Profile representative workloads
- Use consistent test inputs
- Document profiling methodology

## Output Locations

Profiles are written to `/tmp/` due to Bazel sandbox restrictions. Helper scripts copy them to convenient locations.

## Performance Overhead

Sampling-based profiling with minimal overhead:
- CPU profiling: ~1-2%
- Allocation profiling: ~3-5%
- Lock profiling: ~2-4%

## Future Enhancements

- Bazel aspect for automatic profiling
- Profile comparison in code review
- Historical profile storage
- Performance regression tests in CI
- Profile-guided optimization integration

## References

- [async-profiler documentation](https://github.com/async-profiler/async-profiler)
- [Flame graph interpretation](http://www.brendangregg.com/flamegraphs.html)
- [JFR format specification](https://docs.oracle.com/javacomponents/jmc-5-4/jfr-runtime-guide/index.html)
