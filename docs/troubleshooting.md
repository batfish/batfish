# Troubleshooting Guide

This guide helps you diagnose and resolve common issues when developing, building, testing, and running Batfish.

**User-facing troubleshooting** (runtime errors, performance problems, memory issues when using Batfish) is available in the [Pybatfish documentation](https://batfish.readthedocs.io/en/latest/troubleshooting.html).

This developer-focused guide covers build issues, test failures, parser/lexer issues, and BDD engine problems.

## Table of Contents

- [Build Issues](#build-issues)
- [Test Failures](#test-failures)
- [Reference Test Failures](#reference-test-failures)
- [Parser and Lexer Issues](#parser-and-lexer-issues)
- [BDD and Symbolic Engine Issues](#bdd-and-symbolic-engine-issues)
- [Getting Help](#getting-help)

---

## Build Issues

### Bazel Build Fails

#### Error: `java.lang.OutOfMemoryError: Java heap space`

**Symptoms**: Build fails with Java heap space error during compilation.

**Diagnosis**:
```bash
# Check current Bazel JVM settings
bazel info --jvmopt

# Try a simple build with more memory
bazel build --jvmopt=-Xmx4g //projects/allinone:allinone_main
```

**Solutions**:
```bash
# Solution 1: Increase Bazel heap (temporary)
export BAZEL_JAVAC_OPTS="-J-Xmx4g"
bazel build //...

# Solution 2: Set in ~/.bazelrc (persistent)
echo "build --jvmopt=-Xmx4g" >> ~/.bazelrc

# Solution 3: Build fewer targets at once
bazel build //projects/batfish:batfish
```

---

#### Error: `Target //... is not declared in package`

**Symptoms**: Bazel cannot find a target that should exist.

**Diagnosis**:
```bash
# Verify target exists
ls -la projects/batfish/BUILD.bazel

# Check for typos in target name
bazel query //projects/batfish:all
```

**Solutions**:
1. Check for typos in target name (case-sensitive)
2. Run `bazel sync` to refresh workspace
3. Delete `bazel-*` directories and rebuild:
   ```bash
   bazel clean --expunge
   bazel build //...
   ```

---

#### Error: `external dependency not found`

**Symptoms**: Build fails with missing external dependency.

**Diagnosis**:
```bash
# Check if dependency is in MODULE.bazel
grep -i "dependency-name" MODULE.bazel

# Check maven_install.json
cat bazel-out/external/maven/v1/maven_install.json | grep "dependency-name"
```

**Solutions**:
```bash
# Re-pin dependencies
REPIN=1 bazel run @maven//:pin

# If still failing, clear Bazel cache
bazel clean --expunge
REPIN=1 bazel run @maven//:pin
```

---

### Java Version Mismatch

**Symptoms**: Compilation errors about unsupported class files or features.

**Diagnosis**:
```bash
# Check Java version
java -version  # Should be Java 17

# Check what Bazel is using
bazel info --jvmopt
```

**Solutions**:
```bash
# Set JAVA_HOME explicitly
export JAVA_HOME=/path/to/java17

# Or use Bazel config
echo "build --java_language_version=17" >> ~/.bazelrc
echo "build --tool_java_language_version=17" >> ~/.bazelrc
```

---

## Test Failures

### Test Passes Locally but Fails in CI

**Symptoms**: Test succeeds on your machine but fails in GitHub Actions.

**Diagnosis**:
```bash
# Run with Bazel cache disabled
bazel test --nocache_test_results //path/to:test

# Run multiple times to check for flakiness
for i in {1..5}; do
  bazel test //path/to:test || echo "Run $i failed"
done

# Run with verbose output
bazel test --test_output=all //path/to:test
```

**Common Causes and Solutions**:

1. **Order-dependent tests**: Tests that depend on execution order
   - Solution: Make tests independent (don't share static state)

2. **Timing-dependent tests**: Race conditions or timeouts
   - Solution: Increase timeout: `bazel test --test_timeout=300 //path/to:test`

3. **Platform-specific behavior**: Different behavior on Linux vs macOS
   - Solution: Mock platform-specific dependencies

4. **Random data**: Tests using random values without fixed seed
   - Solution: Always seed random number generators

---

### Test Throws Exception

**Symptoms**: Test fails with unexpected exception.

**Diagnosis**:
```bash
# Run with Java debugger attached
bazel test --jvmopt=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 //path/to:test

# Run with assertions enabled
bazel test --jvmopt=-ea //path/to:test

# View full stack trace
bazel test --test_output=streamed //path/to:test
```

**Solutions**:
1. Read stack trace carefully - identify the failing line
2. Check if expected setup was completed (e.g., snapshot initialized)
3. Verify test data files exist and are correctly formatted
4. Check for null pointer exceptions - add null checks

---

### Flaky Test (Intermittent Failures)

**Symptoms**: Test sometimes passes, sometimes fails.

**Diagnosis**:
```bash
# Run many times to identify flakiness
bazel test --runs_per_test=100 //path/to:test

# Check for common issues:
grep -r "Thread.sleep" projects/*/src/test/
grep -r "System.currentTimeMillis" projects/*/src/test/
```

**Common Causes**:
1. **Race conditions**: Concurrent access without synchronization
2. **Resource leaks**: Not closing files/connections
3. **Timing assumptions**: Assuming operations complete instantly
4. **Shared state**: Tests modifying shared static fields

**Solutions**:
- Use proper synchronization
- Always close resources in `finally` blocks or try-with-resources
- Use explicit waits with timeouts instead of `Thread.sleep()`
- Reset shared state in `@Before` or `@After` methods

---

## Parser and Lexer Issues

### ANTLR Parser Grammar Won't Compile

**Symptoms**: `bazel build` fails on ANTLR grammar with syntax errors.

**Diagnosis**:
```bash
# Try compiling grammar in isolation
bazel build //projects/vendor/src/main/antlr4/org/batfish/grammar/vendor:vendor_grammar

# Check for common issues:
# 1. Left recursion (not allowed in ANTLR4)
# 2. Ambiguous token matches
# 3. Missing token definitions
```

**Common Grammar Errors**:

1. **Left recursion**: Rule directly calls itself without consuming a token
   ```antlr
   // BAD - left recursion
   expr: expr '+' expr
       | INT
       ;

   // GOOD - precedence climbing
   expr: expr '+' INT
       | INT
       ;
   ```

2. **Ambiguous lexer rules**: Multiple lexer rules can match same input
   ```antlr
   // BAD - both match "abc"
   STRING: [a-z]+;
   WORD: [a-z]+;

   // GOOD - more specific first
   STRING: '"' .* '"';  // Must be in quotes
   WORD: [a-z]+;
   ```

3. **Missing fragment rules**: Referenced but not defined
   ```
   error(139): BadVendor.g4:22:0: grammar rule 'ws' must be defined or imported
   ```
   Solution: Define `fragment ws: [ \t\r\n]+;`

**Solutions**:
- Review [Parser Conventions](../parsing/parser_rule_conventions.md)
- Check grammar with ANTLR Tools in IntelliJ
- Test with simple input strings

---

### Parser Produces Unexpected Parse Tree

**Symptoms**: Parser compiles but produces wrong structure.

**Diagnosis**:
```bash
# Find the grammar test for your vendor
find projects -name "*GrammarTest.java"

# Run with verbose output
bazel test --test_output=streamed //projects/vendor/src/test/...

# Print parse tree
# In test: System.out.println(tree.toStringTree(parser));
```

**Common Causes**:
1. **Precedence issues**: Operators in wrong order
   - Solution: Use separate rules for each precedence level

2. **Ambiguous grammar**: Multiple valid parse trees
   - Solution: Restructure grammar to eliminate ambiguity

3. **Lexer mode issues**: Wrong tokens being produced
   - Solution: Check mode transitions in base lexer

**Debugging Tips**:
```bash
# Visualize parse tree with ANTLR plugin in IntelliJ
# Or use: grun YourGrammar startRule -tree
```

---

### Extraction Fails with NullPointerException

**Symptoms**: Extraction code throws NPE when walking parse tree.

**Diagnosis**:
```bash
# Check which rule is causing issues
# Stack trace will show line in BatfishListener or extractor

# Verify parse tree structure
bazel test //projects/vendor/src/test/... --test_output=streamed
```

**Common Causes**:
1. **Assuming node exists**: Accessing child that may be null
   ```java
   // BAD
   String ip = ctx.IP().getText();

   // GOOD
   if (ctx.IP() != null) {
       String ip = ctx.IP().getText();
   }
   ```

2. **Wrong rule assumption**: Assuming context is specific type
   ```java
   // BAD - assumes ctx is always interface
   InterfaceContext iface = (InterfaceContext) ctx;

   // GOOD - check type first
   if (ctx instanceof InterfaceContext) {
       InterfaceContext iface = (InterfaceContext) ctx;
   }
   ```

**Solutions**:
- Always check for null before accessing parse tree nodes
- Use `@nullable` annotations to indicate optional fields
- Add defensive checks in extraction code

---

### Reference Test Failures

Reference tests compare Batfish's output against known-good reference files. For comprehensive guidance on reference testing including structure, how to run them, and how to update them, see [Testing Guide](../development/testing_guide.md#reference-testing).

**Quick reference for ref test failures**:
```bash
# Run specific vendor ref tests
bazel test //projects/vendor/src/test/java/org/batfish/grammar/vendor:VendorGrammarTest

# Run all ref tests
bazel test //projects/batfish:batfish_tests

# Update references when behavior changes intentionally
UPDATE_REFS=true bazel test //projects/batfish:batfish_tests
```

**Common failure causes**:
1. **Legitimate regression**: Your change broke existing behavior
2. **Intentional behavior change**: Reference needs updating (use `UPDATE_REFS=true`)
3. **Non-determinism**: Output ordering or timing varies

See [Testing Guide](../development/testing_guide.md) for detailed information on reference test structure and best practices.

---

## Parser Runtime Issues

### Configuration Processing Fails

**Symptoms**: Exception thrown during configuration conversion or post-processing.

**Diagnosis**:
```bash
# Run with stack trace
bazel test --test_output=streamed //projects/batfish:batfish_tests

# Check for specific vendor issues
bazel test //projects/vendor/src/test/... --test_filter=MyVendorTest
```

**Common causes**:
1. **Unimplemented grammar rules**: Configuration uses unsupported syntax
2. **Extraction bugs**: Parse tree walker misses edge cases
3. **Type conversion errors**: Wrong type assumed for a value

**For developers**:
- Add test configs that trigger the issue
- Verify grammar handles all vendor-specific syntax
- Check for null/optional values in extraction code

## BDD and Symbolic Engine Issues

### BDD Memory Leak

**Symptoms**: `java.lang.OutOfMemoryError` when running many questions, or `numOutstandingBDDs()` keeps increasing.

**Diagnosis**:
```java
// Add this code to track BDD leaks
long before = numOutstandingBDDs();
// ... your code here ...
long after = numOutstandingBDDs();
System.err.println("BDDs created: " + (after - before));
```

**Common Causes**:
1. **Not freeing BDDs**: Creating BDDs without calling `dereference()` or `free()`
   ```java
   // BAD - BDD never freed
   BDD bdd = factory.constant(true);

   // GOOD - free when done
   BDD bdd = factory.constant(true);
   try {
       // use bdd
   } finally {
       bdd.free();
   }
   ```

2. **Not using `id()` correctly**: Copying BDDs without incrementing reference count
   ```java
   // BAD - reference count not incremented
   BDD bdd1 = factory.constant(true);
   BDD bdd2 = bdd1;  // Both point to same BDD

   // GOOD - use id() to create copy
   BDD bdd1 = factory.constant(true);
   BDD bdd2 = bdd1.id();  // Increment reference count
   ```

**Solutions**:
- See [BDD Best Practices](../development/bdd_best_practices.md)
- Always use `try-finally` to free BDDs
- Use `with` operations when consuming BDD values
- Run tests with `-Dbdd.checkLeaks=true` (if available)

---

### BDD Variable Ordering Issues

**Symptoms**: BDD operations are very slow or use excessive memory.

**Diagnosis**:
```java
// Check variable ordering
System.err.println("Variable ordering: " + bdd.getVariableOrdering());

// Check BDD size
System.err.println("BDD node count: " + bdd.getNodeCount());
```

**Common Causes**:
- **Poor variable ordering**: Variables not grouped by related fields
- **Too many variables**: Creating unnecessary variables

**Solutions**:
```java
// Group related variables together
// GOOD - variables grouped by packet field
var ipSrc = vars.allocate("ipSrc");
var ipDst = vars.allocate("ipDst");
var tcpSrcPort = vars.allocate("tcpSrcPort");
var tcpDstPort = vars.allocate("tcpDstPort");

// BAD - interleaved variables
var ipSrc = vars.allocate("ipSrc");
var tcpSrcPort = vars.allocate("tcpSrcPort");
var ipDst = vars.allocate("ipDst");
var tcpDstPort = vars.allocate("tcpDstPort");
```

---

### Symbolic Analysis Times Out

**Symptoms**: Analysis takes very long or hangs.

**Diagnosis**:
```bash
# Check if it's actually making progress
# Enable detailed logging
bazel run --jvmopt=-Dloglevel=DEBUG //projects/allinone:allinone_main

# Profile the analysis
bazel run --jvmopt=-Xrunhprof:cpu=samples,depth=10 //projects/allinone:allinone_main
```

**Common Causes**:
1. **Large search space**: Too many possible packet headers
2. **Complex policies**: Many nested ACLs or route maps
3. **Oscillation**: Routes keep changing during fixed-point computation

**Solutions**:
- Tighten header constraints
- Use differential analysis (compare two snapshots)
- Check for oscillation in data plane computation
- See [Performance Tuning](../performance.md) for optimization tips

---

## Getting Help

### When to Ask for Help

Don't hesitate to ask for help if:
- You've spent >1 hour on an issue without progress
- The error message doesn't make sense
- You're unsure about the right approach
- You think you found a bug

### Before Asking

1. **Search existing issues**: Check if problem is already reported
2. **Gather information**:
   ```bash
   # Collect diagnostic info
   java -version
   bazel version
   git log -1  # Commit hash
   ```

3. **Create minimal repro**: Simplify to smallest failing case

4. **Check logs**: Look for error messages and stack traces

### Where to Ask

- **GitHub Issues**: For bugs and feature requests
- **Batfish Slack**: For questions and discussions: [join.slack.com/t/batfish-org](https://join.slack.com/t/batfish-org/shared_invite/)
- **GitHub Discussions**: For general questions

### What to Include

When reporting an issue, always include:
1. **What you were trying to do**
2. **What happened instead** (error messages, stack traces)
3. **Expected behavior**
4. **Steps to reproduce**
5. **Environment info**:
   - Batfish version (git commit hash)
   - Java version
   - OS
   - Example configs (if relevant)

### Example Issue Report

```
**Problem**: Reachability analysis returns empty results

**Expected**: Should find path from 10.0.0.1 to 10.0.1.10

**Actual**: No flows returned

**Steps**:
```python
bf.init_snapshot('configs/', name='test')
bf.set_snapshot('test')
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        src_ips=ip_to_header_space("10.0.0.1/32"),
        dst_ips=ip_to_header_space("10.0.1.10/32")
    )
)
print(result.flows())  # Empty list
```

**Environment**:
- Batfish: commit abc123
- Java: 17.0.2
- OS: macOS 13.0
- Config: [attached router.cfg]
```

---

## Quick Reference

### Common Diagnostic Commands

```bash
# Check Java version
java -version

# Check Bazel version
bazel version

# Check what's using port 9997
lsof -i :9997

# Monitor Java process
jps -l
jstat -gc <PID> 1000

# Take thread dump
jstack <PID>

# Take heap dump
jmap -dump:format=b,file=heap.bin <PID>

# Run with debugger
bazel run --jvmopt=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 //projects/allinone:allinone_main

# Clean rebuild
bazel clean --expunge
bazel build //...

# Run specific test with output
bazel test --test_output=streamed //path/to:test

# Check Bazel configuration
bazel info
bazel config
```

---

### Log Locations

- **Batfish service logs**: `batfish.log` (in working directory)
- **Bazel test logs**: `bazel-testlogs/`
- **Container data**: `containers/` (default location)

---

## Related Documentation

- [Building and Running](../building_and_running/README.md)
- [Development Guide](../development/README.md)
- [Performance Tuning](../performance.md)
- [BDD Best Practices](../development/bdd_best_practices.md)
- [Parser Implementation Guide](../parsing/implementation_guide.md)
