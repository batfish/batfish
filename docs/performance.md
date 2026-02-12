# Performance Tuning Guide

This guide helps you optimize Batfish performance for development, testing, and production workloads.

**User-facing performance guidance** (JVM heap sizing, analysis optimization, scaling strategies) has moved to the [Pybatfish documentation](https://batfish.readthedocs.io/en/latest/performance.html).

This developer-focused guide covers BDD memory management, data plane parallelization, profiling, and internal performance considerations.

## Table of Contents

- [JVM Configuration](#jvm-configuration)
- [BDD Memory Management](#bdd-memory-management)
- [Data Plane Parallelization](#data-plane-parallelization)
- [Profiling and Monitoring](#profiling-and-monitoring)
- [Internal Performance Considerations](#internal-performance-considerations)

---

## JVM Configuration

### Heap Configuration

Batfish performance is sensitive to heap sizing. For user-facing heap sizing recommendations based on network scale, see the [Pybatfish documentation](https://batfish.readthedocs.io/en/latest/performance.html).

This section covers developer-focused heap configuration for local development and testing.
```bash
# For local development
export JAVA_OPTS="-Xmx8g -Xms8g"
./tools/bazel_run.sh

# For Bazel
bazel run --jvmopt=-Xmx8g --jvmopt=-Xms8g //projects/allinone:allinone_main

# For Docker
docker run -e JAVA_OPTS="-Xmx8g" batfish/allinone
```

**Why set both -Xmx and -Xms?**
- Pre-allocating heap prevents resizing pauses
- Reduces GC overhead during analysis
- More predictable performance

---

### Garbage Collection Tuning

For optimal performance, tune GC based on your workload:

**Large network analysis** (heap > 8GB):
```bash
JAVA_OPTS="-Xmx16g -Xms16g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1ReservePercent=20 \
  -XX:InitiatingHeapOccupancyPercent=45"
```

**Many small questions** (frequent GC cycles):
```bash
JAVA_OPTS="-Xmx8g -Xms8g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:G1ReservePercent=10"
```

**Monitoring GC performance**:
```bash
# Enable GC logging
JAVA_OPTS="-Xmx8g -Xms8g -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

# Analyze later with GCViewer or similar tools
```

**Key GC metrics to watch**:
- **Full GC frequency**: Should be rare (<1 per hour for normal workloads)
- **GC pause time**: Should be <200ms for G1GC
- **Heap usage after GC**: Should be <70% sustained

---

### JIT Compilation

Batfish benefits from JIT compilation after warmup.

**For long-running services**:
```bash
# Tiered compilation (default, good for most cases)
JAVA_OPTS="-Xmx8g -Xms8g -XX:+TieredCompilation"

# For maximum steady-state performance (longer warmup)
JAVA_OPTS="-Xmx8g -Xms8g -XX:-TieredCompilation"
```

**Quick warmup for development**:
```bash
# Compile everything at startup
JAVA_OPTS="-Xmx8g -Xms8g -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
```

**Measuring JIT impact**:
```bash
# Log compilations
JAVA_OPTS="-XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions"

# Compare cold vs warm performance:
# First run: compilation overhead
# Second run: fully optimized
```

---

## BDD Memory Management

BDDs (Binary Decision Diagrams) are central to Batfish symbolic analysis but require careful memory management.

### Core Principles

1. **Every BDD must be freed exactly once**
2. **Reference counting tracks shared BDDs**
3. **Memory leaks accumulate quickly** (thousands of BDDs per second)

### Memory Leak Detection

**Enable leak checking** (development mode):
```bash
bazel test --jvmopt=-Dbatfish.bdd.checkLeaks=true //projects/batfish:batfish_tests
```

**Manual leak detection**:
```java
long before = numOutstandingBDDs();
try {
    // ... code that creates BDDs ...
} finally {
    long after = numOutstandingBDDs();
    if (before != after) {
        System.err.println("BDD leak: " + (after - before) + " BDDs not freed");
    }
}
```

**Common leak patterns**:

1. **Not freeing in all paths**:
   ```java
   // BAD - BDD leaked on exception
   BDD bdd = factory.constant(true);
   if (condition) {
       bdd.free();
   }

   // GOOD - always free
   BDD bdd = factory.constant(true);
   try {
       if (condition) {
           // use bdd
       }
   } finally {
       bdd.free();
   }
   ```

2. **Forgetting to free copies**:
   ```java
   // BAD - bdd2 not freed
   BDD bdd1 = factory.constant(true);
   BDD bdd2 = bdd1.id();
   bdd1.free();

   // GOOD - both freed
   BDD bdd1 = factory.constant(true);
   BDD bdd2 = bdd1.id();
   try {
       // use both
   } finally {
       bdd1.free();
       bdd2.free();
   }
   ```

See [BDD Best Practices](../development/bdd_best_practices.md) for comprehensive guide.

---

### BDD Variable Ordering

**CRITICAL**: Variable ordering dramatically impacts BDD performance and memory usage.

**Good ordering** (group related variables):
```java
// Group by packet field
var ipSrc = vars.allocate("ipSrc");
var ipDst = vars.allocate("ipDst");
var tcpSrcPort = vars.allocate("tcpSrcPort");
var tcpDstPort = vars.allocate("tcpDstPort");
```

**Bad ordering** (interleaved):
```java
// Variables from different fields interleaved
var ipSrc = vars.allocate("ipSrc");
var tcpSrcPort = vars.allocate("tcpSrcPort");
var ipDst = vars.allocate("ipDst");
var tcpDstPort = vars.allocate("tcpDstPort");
```

**Measuring ordering impact**:
```java
BDD bdd = ...;
System.err.println("Node count: " + bdd.getNodeCount());
System.err.println("Path count: " + bdd.pathCount());
```

**Guidelines**:
- Group variables by logical field (IP, ports, protocols)
- Keep related variables contiguous
- Minimize variable count (use bit vectors efficiently)

---

### BDD Operation Optimization

**Use built-in operations when possible**:
```java
// GOOD - uses optimized implementation
BDD result = bdd1.and(bdd2);

// SLOWER - manual implementation
BDD result = factory.apply(bdd1, bdd2, BDDOp.AND);
```

**Reuse BDDs across questions**:
```java
// Compute once, use many times
BDD allTcp = buildTcpBDD();  // Expensive
try {
    BDD https = allTcp.and(portBDD(443));
    BDD http = allTcp.and(portBDD(80));
    // use both
} finally {
    allTcp.free();
}
```

---

## Data Plane Parallelization

The IBDP (Incremental Batfish Data Plane) algorithm parallelizes computation across nodes.

### Thread Pool Configuration

**Default settings**:
```bash
# Defaults to number of CPU cores
# Typically optimal for most workloads
```

**Manual configuration**:
```bash
# For CPU-bound workloads (most cases)
bazel run --jvmopt=-Dbatfish.dp.workers=8 //projects/allinone:allinone_main

# For I/O-bound workloads (e.g., reading many configs)
bazel run --jvmopt=-Dbatfish.dp.workers=16 //projects/allinone:allinone_main
```

**Guidelines**:
- **CPU cores <= 8**: Set workers = cores
- **CPU cores > 8**: Set workers = cores * 0.75
- **Memory-constrained**: Reduce workers to avoid GC pressure

---

### Oscillation Detection

Route oscillation can cause infinite loops in data plane computation.

**Symptoms**: Analysis never completes, CPU at 100%

**Detection**: Batfish automatically detects oscillation and throws exception.

**Configuration**:
```bash
# Increase max iterations (rarely needed)
bazel run --jvmopt=-Dbatfish.dp.maxIterations=1000 //projects/allinone:allinone_main
```

**Common causes**:
1. **Route maps with asymmetric policies**
2. **BGP med oscillation**
3. **Improper redistribution**

**Debugging oscillation**:
```bash
# Enable detailed logging
bazel run --jvmopt="-Dloglevel=TRACE -Dlogger.org.batfish.dataplane=TRACE" \
  //projects/allinone:allinone_main
```

See [Data Plane Documentation](../data_plane/README.md) for details on IBDP algorithm.

---

## Profiling and Monitoring

### Enable Profiling

**CPU profiling**:
```bash
# Sample-based profiling (low overhead)
bazel run --jvmopt=-Xrunhprof:cpu=samples,depth=10,interval=1 \
  //projects/allinone:allinone_main

# Output: java.hprof.txt
# Analyze with: VisualVM, JProfiler, or yourkit
```

**Memory profiling**:
```bash
# Heap dump on OOM
bazel run --jvmopt="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp" \
  //projects/allinone:allinone_main

# Manual heap dump
jmap -dump:format=b,file=heap.bin <PID>
```

**Allocation profiling**:
```bash
# Track allocations
bazel run --jvmopt="-XX:+PrintAllocationStatistics -XX:+PrintGCDetails" \
  //projects/allinone:allinone_main
```

---

### Runtime Monitoring

**Check JVM stats**:
```bash
# Find Batfish PID
jps -l | grep batfish

# Monitor GC
jstat -gc <PID> 1000  # Sample every 1 second

# Monitor heap
jmap -heap <PID>

# Thread dump
jstack <PID> > threads.txt
```

**Key metrics**:
- **Heap usage**: Should be stable, not continuously growing
- **GC frequency**: Young GC frequent is OK; Full GC should be rare
- **CPU utilization**: Should be high during analysis, low when idle

---

### Application-Level Metrics

Batfish exposes internal metrics through logging:

**Enable performance logging**:
```bash
bazel run --jvmopt="-Dloglevel=INFO -Dlogger.org.batfish.dataplane=DEBUG" \
  //projects/allinone:allinone_main
```

**Look for**:
- `Data plane computation took X ms`
- `Reachability analysis took X ms`
- `BDD node count: X`
- `Number of flows: X`

---

## Internal Performance Considerations

This section covers internal Batfish implementation bottlenecks that developers should be aware of.

### BDD Size Explosion

**Issue**: Certain configuration patterns cause BDD node count to grow exponentially.

**Symptoms**:
- Memory usage spikes during data plane computation
- Analysis time increases dramatically with small config changes
- `OutOfMemoryError` on moderately-sized networks

**Common causes**:
1. **Complex NAT rules**: Extensive NAT with many overlapping rules
2. **Large route maps**: Route maps with many clauses and complex match conditions
3. **Deep ACL nesting**: Many nested ACLs in security policies

**For developers**: When implementing new vendor support, be mindful of how configuration structures map to BDD operations. Consider:
- Can intermediate results be cached?
- Can the grammar structure be optimized?
- Are there opportunities to simplify BDD construction?

---

### Oscillation in Data Plane Computation

**Issue**: Route oscillation can cause infinite loops in fixed-point computation.

**Symptoms**:
- Analysis never completes
- CPU usage stays at 100%
- Log shows repeated iterations without convergence

**Detection**: Batfish automatically detects oscillation and throws exception after threshold.

**For developers**: When modifying data plane computation logic:
1. Test with configs known to oscillate
2. Verify detection mechanisms work correctly
3. Consider adding vendor-specific oscillation prevention

See [Data Plane Documentation](../data_plane/README.md) for algorithm details.

---

### Thread Pool Saturation

**Issue**: Data plane parallelization may not scale linearly with CPU cores.

**Symptoms**:
- Adding more workers doesn't improve performance
- CPU usage below expected level

**Common causes**:
1. **Memory contention**: Too many workers cause GC pressure
2. **I/O bottleneck**: Reading configs becomes bottleneck
3. **Lock contention**: Shared data structures cause serialization

**For developers**: When optimizing parallelization:
- Profile with different worker counts
- Consider work-stealing vs fixed partitioning
- Monitor GC impact of increased parallelism

---

## Quick Reference

### Performance Diagnostic Commands

```bash
# Profile CPU
bazel run --jvmopt=-Xrunhprof:cpu=samples,depth=10 //projects/allinone:allinone_main

# Monitor heap
jstat -gc <PID> 1000

# Thread dump
jstack <PID>

# Heap dump on OOM
--jvmopt=-XX:+HeapDumpOnOutOfMemoryError

# Check BDD leaks
bazel test --jvmopt=-Dbatfish.bdd.checkLeaks=true //...

# Enable performance logging
--jvmopt="-Dloglevel=INFO -Dlogger.org.batfish.dataplane=DEBUG"
```

---

## Related Documentation

- [Troubleshooting Guide](../troubleshooting.md)
- [Data Plane Documentation](../data_plane/README.md)
- [Symbolic Engine Documentation](../symbolic_engine/README.md)
- [BDD Best Practices](../development/bdd_best_practices.md)
- [Building and Running](../building_and_running/README.md)
