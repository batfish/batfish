# Profiling Architecture

Design of the profiling infrastructure for developer performance investigation.

## Design Goals

1. **Easy to use**: Single command to profile any test
2. **Low overhead**: Sampling-based profiling with <2% overhead
3. **Bazel-native**: Use Bazel targets and Maven dependencies
4. **Multi-format**: HTML (interactive), JFR (tooling), collapsed (scripting)

## Overview

```
Developer runs:
  ./tools/profiling/profile.sh //test:target [output] [format]

                 ↓

  profile.sh locates async-profiler native library
  runs test with -agentpath JVM flag

                 ↓

  async-profiler v4.2 samples call stacks during execution

                 ↓

  Output: profile.html (interactive flame graph)
          profile.jfr (JFR format)
          profile.txt (collapsed format)
```

## Components

### async-profiler

Sampling-based Java profiler (v4.2) from Maven Central.

Maven artifacts:
- `tools.profiler:async-profiler:4.2` - Main JAR
- `tools.profiler:async-profiler:4.2:linux-x64` - Linux x86_64 native library
- `tools.profiler:async-profiler:4.2:linux-arm64` - Linux ARM64 native library
- `tools.profiler:async-profiler:4.2:macos` - macOS native library

**Bazel target**: `//tools/profiling:native_libs` extracts platform-specific .so/.dylib from JARs

### profile.sh

Shell script that profiles a test target:
1. Builds the native library for current platform
2. Uses `bazel cquery` to locate the library
3. Runs target with `-agentpath` JVM flag
4. Saves output in requested format (HTML/JFR/collapsed)

### analyze_profile.py

Python tool for parsing collapsed format profiles:
- Extract top hotspots as JSON
- Compare two profiles to detect regressions

Used for scripting and automated analysis when JFR tooling isn't suitable.

## Output Formats

### HTML

Interactive flame graph for visual analysis. Open in browser to zoom, search, and explore.

### JFR

Java Flight Recorder format. Compatible with:
- Java Mission Control (JMC)
- JProfiler
- Other JFR-compatible tools
- JFR API for programmatic access

### Collapsed

Text format with stack traces and sample counts:
```
frame1;frame2;frame3 125
frame1;frame4 89
```

Used for lightweight scripting when JFR parsing would be overkill.

## Usage

### Basic Profiling

```bash
# HTML flame graph (default)
./tools/profiling/profile.sh //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:tests

# JFR format (for JProfiler, JMC, etc)
./tools/profiling/profile.sh //your:test profile.jfr jfr

# Collapsed format (for scripting)
./tools/profiling/profile.sh //your:test profile.txt collapsed
```

### Analyzing Collapsed Profiles

```bash
# Extract hotspots
bazel run //tools/profiling:analyze_profile -- hotspots profile.txt --top 20

# Compare profiles
bazel run //tools/profiling:analyze_profile -- compare current.txt --baseline baseline.txt
```

## How It Works

### Bazel Integration

1. Maven artifacts downloaded via `maven_install`
2. `genrule` extracts native .so/.dylib from JARs
3. `filegroup` with `select()` chooses platform-specific library
4. `profile.sh` uses `bazel cquery` to locate the library path

### Runtime

1. Test runs with JVM flag: `-agentpath:/path/to/libasyncProfiler.so=start,event=cpu,file=output.html`
2. async-profiler samples call stacks every 10ms (configurable)
3. Captures both Java and native frames
4. On shutdown, writes profile to specified file

### Path Resolution

- `profile.sh` uses `bazel cquery --output=files` to find library path automatically
- `analyze_profile.py` uses `BUILD_WORKING_DIRECTORY` environment variable for relative paths

## Performance Overhead

Sampling-based profiling:
- CPU profiling: ~1-2%
- Allocation profiling: ~3-5%
- Lock profiling: ~2-4%

## Output Locations

Profiles written to `/tmp/` (Bazel test sandbox restriction). Scripts copy to specified location.

## References

- [async-profiler documentation](https://github.com/async-profiler/async-profiler)
- [Flame graph interpretation](http://www.brendangregg.com/flamegraphs.html)
- [JFR format specification](https://docs.oracle.com/javacomponents/jmc-5-4/jfr-runtime-guide/index.html)
