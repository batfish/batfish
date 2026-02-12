# Debugging Parser Issues

**Time**: 1 hour | **Difficulty**: Intermediate

This tutorial teaches you how to debug and fix common parser issues in Batfish. You'll learn practical techniques for diagnosing and resolving grammar and extractor problems.

## What You'll Learn

By the end of this tutorial, you'll be able to:
- Identify different types of parse errors
- Use ANTLR debugging tools effectively
- Debug extractor issues
- Fix common grammar problems
- Write tests to verify fixes

---

## Prerequisites

Before starting, ensure you have:

1. **Working on a parser**: You're modifying or creating a grammar/extractor
2. **ANTLR basics**: Understand lexer/parser concepts
3. **Read parser docs**: [Parsing README](../parsing/README.md)

**New to parsers?** Start with [Adding Your First Vendor Support](adding_vendor_support.md).

---

## Types of Parser Issues

Parser issues generally fall into three categories:

1. **Grammar compilation errors**: Grammar file won't compile
2. **Parse errors**: Config files fail to parse
3. **Extraction errors**: Parses but extraction fails

Let's tackle each type.

---

## Type 1: Grammar Compilation Errors

### Problem: ANTLR Won't Compile Grammar

**Symptoms**:
```
error(139): Vendor.g4:42:0: grammar error
```

**Step 1: Identify the Error**

```bash
# Try building the grammar in isolation
bazel build //projects/vendor/src/main/antlr4/org/batfish/grammar/vendor:vendor_grammar

# Read the error message carefully
# Line numbers point to the issue
```

**Common Grammar Errors**:

#### Error: Left Recursion

```antlr
// BAD - ANTLR4 doesn't allow left recursion
expr: expr '+' expr
    | INT
    ;

// GOOD - different precedence levels
expr: expr '+' INT
    | INT
    ;
```

**Diagnosis**:
```
error(139): Vendor.g4:10:0: left recursion rules
```

**Fix**: Restructure grammar to eliminate direct recursion on the left side.

---

#### Error: Ambiguous Lexer Rules

```antlr
// BAD - both rules can match "abc"
WORD: [a-z]+;
IDENTIFIER: [a-z]+;

// GOOD - make them mutually exclusive
WORD: [a-z]+;
IDENTIFIER: [a-z]+ '_';  // Must end with underscore
```

**Diagnosis**:
```
warning(139): Vendor.g4:15:0: lexer rule WORD can match input with multiple rules
```

**Fix**: Make rules more specific or use modes to separate contexts.

---

#### Error: Missing Fragment Rule

```
error(139): Vendor.g4:22:0: grammar rule 'ws' must be defined
```

**Diagnosis**: Referenced a rule that doesn't exist.

**Fix**: Define the fragment rule:
```antlr
fragment ws: [ \t\r\n]+;
```

---

### Step 2: Use ANTLR Tools

**In IntelliJ IDEA**:

1. Install "ANTLR4" plugin
2. Open your `.g4` file
3. Right-click → "Test Rule VendorLexer"
4. Enter sample input
5. Visualize tokens and parse tree

**Command-line** (for quick testing):

```bash
# After building, use antlr4-tools
cd projects/vendor/src/main/antlr4/org/batfish/grammar/vendor
antlr4-tools VendorParser.g4

# Interactive testing
antlr4-parse VendorParser.g4 startRule -tree
```

---

### Step 3: Test Grammar Incrementally

When adding new rules:

```antlr
// Start simple
interface: INTERFACE interface_name;

// Test it works
// Then add complexity
interface: INTERFACE interface_name NEWLINE config_lines;

// Test again
// Add more
interface: INTERFACE interface_name NEWLINE config_lines EXIT_INTERFACE;
```

**Why**: Easier to identify which change broke things.

---

## Type 2: Parse Errors

### Problem: Config File Fails to Parse

**Symptoms**:
```
ERROR: line 42:15 mismatched input 'expecting' expecting {'ip', 'vlan'}
```

**Step 1: Reproduce the Error**

```bash
# Find the test that's failing
bazel test //projects/vendor/src/test/... --test_output=streamed

# Or parse manually
cat test_configs/router.cfg | \
  bazel run //projects/vendor/src/main/java/org/batfish/grammar/vendor:parse_cli
```

**Step 2: Isolate the Problematic Line**

```
ERROR: line 42:15 mismatched input
```

This means:
- **Line 42**: The problematic line
- **Column 15**: Where the error starts
- **"mismatched input"**: Parser saw something unexpected

**Step 3: Check the Actual vs. Expected**

```
mismatched input '10.0.0.1' expecting {'ip', 'vlan'}
```

This means:
- Parser found: `10.0.0.1`
- Parser wanted: either `ip` or `vlan` keywords

**Common causes**:
1. **Grammar doesn't handle this syntax** → Add rule
2. **Wrong lexer mode** → Check mode transitions
3. **Token not matching** → Check lexer rules

---

### Step 4: Check Lexer Output

```java
// In your combined parser, add debugging
public VendorCombinedParser(String configuration) {
    super(configuration, VendorLexer.class, VendorParser.class);
    // Add this:
    ((VendorLexer) _lexer).removeErrorListeners();
    ((VendorLexer) _lexer).addErrorListener(new DiagnosticErrorListener());
}
```

**Output**:
```
line 42:15 token recognition error at: '10.0.0.1'
```

This tells you the lexer itself can't recognize the token.

**Fix**: Check lexer rules:
```antlr
// Maybe IP addresses aren't recognized
IP: [0-9]+ '.' [0-9]+ '.' [0-9]+ '.' [0-9]+;
```

---

### Step 5: Use Parse Tree Visualization

**In your test**:

```java
@Test
public void testParseTree() {
    VendorParser parser = parse("interface Ethernet0/0\n ip address 10.0.0.1 255.255.255.0\n");

    // Print the tree
    System.err.println(parser.parseTree.toStringTree(parser));

    // Or inspect specific parts
    InterfaceContext iface = parser.interface();
    assertNotNull(iface);
}
```

**Example output**:
```
(interface (interface Ethernet0/0) (config (ip address 10.0.0.1 255.255.255.0)))
```

**Compare expected vs. actual**:
```
Expected: (interface (interface NAME) (config ...))
Actual:   (interface <missing 'interface'> NAME (config ...))
```

This shows the `interface` keyword isn't being matched.

---

### Common Parse Error Patterns

#### Pattern: Semicolon Issues

```antlr
// Vendor uses semicolons
config_line: COMMAND parameter ';';

// But config might have:
COMMAND parameter    // Missing semicolon
```

**Fix**: Make semicolon optional:
```antlr
config_line: COMMAND parameter ';'?;  // Optional semicolon
```

**Or** use a mode to handle both styles:
```antlr
// Mode for semicolon-terminated
mode SEMICOLON_MODE;
SCOLON: ';';
```

---

#### Pattern: Indentation-Based Syntax

Some vendors use indentation (like Python/PyYAML).

```antlr
// This won't work for indented configs
block: START_BLOCK line* END_BLOCK;

// Instead, track indentation
block: START_BLOCK indented_line* END_BLOCK;
```

**Solution**: Use lexer actions or preprocessor to add explicit markers.

---

#### Pattern: Keywords in Values

```
interface Interface
  ip address 10.0.0.1 255.255.255.0
  shutdown
```

The word `interface` in `interface Interface` might be confused.

**Fix**: Use parser rules to distinguish:
```antlr
// Parser knows context
interface_stmt: INTERFACE interface_name;

interface_name: WORD | INTERFACE;  // Can use keyword as name
```

---

## Type 3: Extraction Errors

### Problem: Parses But Extraction Fails

**Symptoms**:
```
NullPointerException in BatfishListener
```

**Step 1: Find the Failing Line**

```bash
# Run test with full stack trace
bazel test --test_output=streamed //projects/vendor/src/test/...

# Stack trace shows:
# at VendorExtractor.enterInterface(VendorExtractor.java:123)
```

**Line 123** is where the error occurred.

---

### Step 2: Check for Null Nodes

```java
// BAD - assumes IP exists
String ip = ctx.IP().getText();

// GOOD - check for null
if (ctx.IP() != null) {
    String ip = ctx.IP().getText();
}
```

**Why**: ANTLR creates null for optional parse tree nodes.

**Diagnostic pattern**:
```java
public void exitInterface(InterfaceContext ctx) {
    if (ctx == null) {
        System.err.println("Interface context is null");
        return;
    }

    if (ctx.IP() != null) {
        String ip = ctx.IP().getText();
        // use ip
    } else {
        System.err.println("No IP address in interface at line " + ctx.getStart().getLine());
        _w.redFlag("Interface without IP address");
    }
}
```

---

### Step 3: Verify Context Types

```java
// BAD - assumes context is specific type
InterfaceContext iface = (InterfaceContext) ctx;

// GOOD - check type first
if (ctx instanceof InterfaceContext) {
    InterfaceContext iface = (InterfaceContext) ctx;
    // process interface
}
```

**Why**: Generic `RuleContext` might not be the specific type you expect.

---

### Step 4: Use Parse Tree Inspection

```java
// In your extractor
@Override
public void exitEveryRule(ParserRuleContext ctx) {
    // Debug: print all children
    for (int i = 0; i < ctx.getChildCount(); i++) {
        ParseTree child = ctx.getChild(i);
        System.err.println("Child " + i + ": " + child.getClass().getSimpleName());
    }
}
```

**Output**:
```
Child 0: Interface_ipContext
Child 1: TerminalNodeImpl
Child 2: Interface_shutdownContext
```

This helps you understand the parse tree structure.

---

### Step 5: Test Extractor Incrementally

```java
@Test
public void testExtractInterface() {
    // Minimal example
    String config = "interface Ethernet0/0\n";

    VendorExtractor extractor = new VendorExtractor(config);
    Configuration c = extractor.extract();

    assertNotNull(c.getInterfaces());
    assertEquals(1, c.getInterfaces().size());

    Interface iface = c.getInterfaces().iterator().next();
    assertEquals("Ethernet0/0", iface.getName());
}
```

**Start small** - test one feature at a time.

---

## Debugging Workflow

### Systematic Approach

1. **Reproduce the error** → Get exact error message
2. **Isolate the issue** → Minimal test case
3. **Diagnose root cause** → Grammar vs lexer vs extractor
4. **Fix incrementally** → One change at a time
5. **Verify the fix** → Add test, run full suite

---

### Quick Diagnostic Commands

```bash
# Build grammar with verbose output
bazel build --verbose_failures //projects/vendor/src/main/antlr4/...

# Test specific grammar file
bazel test //projects/vendor/src/test/...:VendorGrammarTest

# Run with ANTLR diagnostics
bazel test --jvmopt=-Dantlr4.debug=true //projects/vendor/src/test/...

# Parse a single file
bazel run //projects/vendor:parse_cli -- test_configs/router.cfg
```

---

## Common Pitfalls and Solutions

### Pitfall 1: Forgetting `_null` Suffix

```antlr
// Rule that's parsed but never used
vendor_specific: COMMAND parameter;
```

**Fix**: Add `_null` suffix to indicate ignored:
```antlr
vendor_specific_null: COMMAND parameter;
```

**Why**: Signals intent to other developers.

---

### Pitfall 2: Not Cleaning Up After Mode Changes

```java
// In base lexer
@Override
public void popMode() {
    super.popMode();
    mode(DEFAULT_MODE);  // Explicitly return to default
}
```

**Why**: Modes can stack and cause unexpected behavior.

---

### Pitfall 3: Assuming Token Text Format

```java
// BAD - assumes IP is in specific format
String ip = ctx.IP().getText();  // "10.0.0.1"
String[] parts = ip.split("\\.");  // Might fail if format changes

// GOOD - parse robustly
try {
    InetAddress addr = InetAddress.getByName(ctx.IP().getText());
    // use addr
} catch (UnknownHostException e) {
    _w.redFlag("Invalid IP address: " + ctx.IP().getText());
}
```

---

## Writing Tests for Parser Fixes

### Regression Test

```java
@Test
public void testBug123_InterfaceWithIp() {
    // Test for specific bug
    String config = "interface Ethernet0/0\n ip address 10.0.0.1 255.255.255.0\n";

    VendorExtractor extractor = new VendorExtractor(config);
    Configuration c = extractor.extract();

    Interface iface = c.getInterfaces().get("Ethernet0/0");
    assertNotNull("Interface should exist", iface);
    assertNotNull("Interface should have IP", iface.getPrimaryAddress());
}
```

### Parameterized Test

```java
@Parameters(name = "{0}")
public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"interface Ethernet0/0", "Ethernet0/0"},
        {"interface GigabitEthernet1/0/1", "GigabitEthernet1/0/1"},
        {"interface Vlan100", "Vlan100"}
    });
}

@Test
public void testInterfaceName(String config, String expectedName) {
    VendorExtractor extractor = new VendorExtractor("interface " + config + "\n");
    Configuration c = extractor.extract();

    assertNotNull(c.getInterfaces().get(expectedName));
}
```

---

## Quick Reference

### Common Debugging Techniques

| Issue Type | Diagnosis Tool | Common Fix |
|------------|----------------|------------|
| Grammar won't compile | Bazel build output | Fix left recursion, ambiguous rules |
| Token not recognized | DiagnosticErrorListener | Add/fix lexer rule |
| Parse fails | `--test_output=streamed` | Add grammar rule for syntax |
| Extraction NPE | Stack trace | Add null checks |
| Wrong values | Parse tree inspection | Fix extractor logic |

### Essential Commands

```bash
# Build grammar
bazel build //projects/vendor/src/main/antlr4/...

# Test with output
bazel test --test_output=streamed //projects/vendor/src/test/...

# Parse single file
bazel run //projects/vendor:parse_cli -- config.cfg

# ANTLR diagnostics
bazel test --jvmopt=-Dantlr4.debug=true //...
```

---

## Next Steps

Now that you can debug parser issues:

1. **Fix real bugs**: Look for `bug` label in issues
2. **Add tests**: Improve test coverage for parsers
3. **Learn advanced patterns**: [Parser Conventions](../parsing/parser_rule_conventions.md)
4. **Add new vendor**: [Adding Vendor Support](adding_vendor_support.md)

---

## Related Documentation

- [Parsing README](../parsing/README.md)
- [Parser Conventions](../parsing/parser_rule_conventions.md)
- [Lexer Mode Patterns](../parsing/lexer_mode_patterns.md)
- [Implementation Guide](../parsing/implementation_guide.md)
- [Troubleshooting](../troubleshooting.md)
