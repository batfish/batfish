# ANTLR4 Tips and Tricks

This document collects practical tips, tricks, and common patterns for working with ANTLR4 in Batfish. These are born from real-world experience and can save you hours of debugging.

## Table of Contents

- [Lexer Rule Ordering](#lexer-rule-ordering)
- [Fragment Rules](#fragment-rules)
- [Predicates for Disambiguation](#predicates-for-disambiguation)
- [Parser Listeners vs Visitors](#parser-listeners-vs-visitors)
- [Grammar Testing](#grammar-testing)
- [Common Pitfalls](#common-pitfalls)
- [Performance Tips](#performance-tips)

---

## Lexer Rule Ordering

### The Golden Rule: First-Defined Wins

**CRITICAL**: When multiple lexer rules can match the same input, ANTLR4 chooses:
1. **Longest match** (maximal munch)
2. **First defined** if same length

**It is NOT alphabetical!** The order of definition in your `.g4` file matters.

### Common Mistake

```antlr
// ❌ WRONG: WORD defined first, so KEYWORD never matches
WORD: [a-zA-Z]+;
KEYWORD: 'interface' | 'ip' | 'route';

// ✅ CORRECT: Most specific rules first
KEYWORD: 'interface' | 'ip' | 'route';
WORD: [a-zA-Z]+;
```

In the WRONG example above, if the input is "interface", the `WORD` rule matches first (since it's defined first) and consumes the entire token. The `KEYWORD` rule never gets a chance.

### Guidelines

1. **Order rules from most specific to least specific**
   - Keywords and literals first
   - Complex patterns before simple ones
   - Longer matches before shorter ones

2. **Test your lexer** with ambiguous input to verify correct rule matches
3. **Use `grun` tool** (see Grammar Testing below) to tokenize input and see which rule matches

### Real-World Batfish Example

```antlr
// From Cisco grammar - correct ordering
// Most specific patterns first
INTERFACE_NAME: [a-zA-Z][a-zA-Z0-9-]*[a-zA-Z0-9-]*;
IP_ADDRESS: ...;
// General fallback
WORD: [a-zA-Z0-9-]+;
```

---

## Fragment Rules

Fragment rules are reusable lexer components that cannot match on their own. They're only used by other lexer rules.

### Benefits

1. **Reuse common patterns** across multiple rules
2. **Improve readability** of complex regex patterns
3. **Easier maintenance** - change once, affects all references

### Example

```antlr
// ❌ BAD: Repeating same pattern
IPV4_ADDRESS: DIGIT '.' DIGIT '.' DIGIT '.' DIGIT;
IPV6_ADDRESS: [0-9A-Fa-f]+ ':' [0-9A-Fa-f]+;

// ✅ GOOD: Use fragment
fragment DIGIT: [0-9];
fragment HEXDIGIT: [0-9a-fA-F];

IPV4_ADDRESS: DIGIT '.' DIGIT '.' DIGIT '.' DIGIT;
IPV6_ADDRESS: [0-9A-Fa-f]+ ':' [0-9A-Fa-f]+;
```

### Batfish Convention

Batfish uses fragment rules extensively. See existing grammars for examples:
- `fragment DIGIT` - common digit pattern
- `fragment HEXDIGIT` - hex digit pattern
- `fragment WORD` - word pattern (when not a token itself)

---

## Predicates for Disambiguation

When the grammar is ambiguous (multiple valid parse trees), use predicates to guide ANTLR4's choice.

### Syntax

```antlr
ambiguous_rule
    : alternative1 => predicate()
    | alternative2
    ;
```

The `=> predicate` evaluates to true or false at runtime to choose the correct alternative.

### Common Use Cases

**1. Lookahead Disambiguation**

```antlr
// Without predicate: "interface" could be parsed as command or name
statement: 'interface' | name_expr;

// With predicate: if followed by newline, it's a command
statement: 'interface' => { !isCurrentTokenInFollowing()? } name_expr
```

**2. Token Property Disambiguation**

```antlr
// Using token properties in predicates
STRING
    : '"' .* '"'? =>
      { $string.length() > 0 }  // Non-empty only
    ;
```

### Batfish Patterns

Batfish uses predicates for:
- Keyword vs name disambiguation in base lexer
- Mode selection logic
- Token boundary detection

For examples, see:
- `BatfishLexer.g4` - Base lexer with keyword handling
- Vendor-specific lexers inheriting from base

---

## Parser Listeners vs Visitors

Both are ways to traverse parse trees, but serve different purposes.

### Parser Listeners

**Use when**: You need to perform actions **during parsing**

**Characteristics**:
- Event-driven: React to parse events
- No return values: Methods return `void`
- Enter/exit events: `enterEveryRule()`, `exitEveryRule()`
- Can't control traversal: Always walks entire tree

**Batfish uses**: Listeners for extraction (see `BatfishListener.java`)

**Example**:
```java
public class MyExtractor extends BatfishBaseListener {
    @Override
    public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        // Extract interface info as we parse
        String name = ctx.name.getText();
        // Store in data structure
    }
}
```

### Parser Visitors

**Use when**: You need to **transform or query** parse trees

**Characteristics**:
- Visit pattern: Explicit control of traversal
- Has return values: Methods can return values
- Can prune: Skip subtrees by returning early
- More flexible: Can implement different traversals

**Example**:
```java
public class InterfaceVisitor extends AbstractParseTreeVisitor<Integer> {
    @Override
    public Integer visitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        // Count interfaces
        return 1;  // Each visit returns count
    }
}

// Usage:
InterfaceVisitor visitor = new InterfaceVisitor();
int count = visitor.visit(parseTree);
```

### When to Use Each

| Scenario | Use Listener | Use Visitor |
|----------|--------------|--------------|
| Extraction during parsing | ✅ Listener | ❌ Visitor |
| Building data structure | ✅ Listener | ❌ Visitor |
| Tree transformation | ❌ Listener | ✅ Visitor |
| Querying tree | ❌ Listener | ✅ Visitor |
| Selective processing | ❌ Listener | ✅ Visitor |

Batfish primarily uses **listeners** for extraction since we need to build structures during parsing.

---

## Grammar Testing

Don't wait until full integration to test your grammar changes. Use ANTLR's testing tools.

### Using `grun` Tool

The `grun` (grammar test run) tool lets you quickly test lexer/parser rules.

**Basic usage**:
```bash
# Test lexer
antlr4 -Dlanguage=Java MyLexer.g4
grun org.batfish.grammar.my.MyLexer tokens -input test.txt

# Test parser
antlr4 -Dlanguage=Java MyParser.g4
grun org.batfish.grammar.my.MyParser startRule -input test.txt -tree
```

**Common options**:
```bash
-tree        # Show parse tree (LISP-style)
-gui         # Show parse tree visually
-tokens       # Show token stream
-ps          # Show tokens in PSV format
-diagnostics  # Show tool diagnostics
```

### Testing Workflow

1. **Test lexer in isolation** before integrating
   ```bash
   # Create test input file
   echo "interface ge-0/0/0" > test.txt

   # Test lexer
   java org.batfish.grammar.MyLexer test.txt
   ```

2. **Test parser with known-good and known-bad inputs**
   ```bash
   # Should parse successfully
   echo "interface ge-0/0/0" > good.txt
   grun ... good.txt -tree

   # Should fail gracefully
   echo "interface ge-0/0/0 SYNTAX_ERROR" > bad.txt
   grun ... bad.txt -tree
   ```

3. **Verify parse tree structure** matches expectations

### Unit Testing Grammar

Integrate grammar tests into Batfish test suite:

```java
@Test
public void testMyNewRule() {
    // Parse test input
    MyParser parser = createParser("interface ge-0/0/0");

    // Verify structure
    assertNotNull(parser.interfaceDeclaration());
    assertEquals("ge-0/0/0", parser.interfaceDeclaration().name.getText());
}
```

---

## Common Pitfalls

### 1. Left Recursion (Not Allowed in ANTLR4)

```antlr
// ❌ WRONG: Direct left recursion
expr: expr '+' expr
    | INT
    ;

// ✅ CORRECT: Left-factor or refactor
expr: expr '+' INT
    | INT
    ;
```

ANLTR4 **does not support direct left recursion**. You'll get a grammar error.

### 2. Forgetting to Push/Pop Modes

```antlr
mode M_Name;
M_Name_NAME: F_Name -> type(NAME), popMode;  // Don't forget popMode!
```

Common error: Transitioning to a mode but forgetting to pop back. Result: Parser gets stuck in wrong mode.

**Always pair**: `mode(NewMode)` with corresponding `popMode` action.

### 3. Case-Insensitive Lexer Issues

```antlr
// ❌ PROBLEMATIC: Can conflict with token names
fragment WORD: [a-zA-Z]+;
fragment WORD_UPPER: [A-Z]+;

// ✅ BETTER: Use explicit case handling
STRING: '"' .* '"';

// Or in parser (preferred)
identifier: [a-zA-Z] [a-zA-Z0-9_]*;
```

When mixing case-insensitive tokens with token names, create ambiguities. Prefer case handling in parser rules.

### 4. Overusing Lexer Modes

```antlr
// ❌ BAD: Mode for every keyword variation
mode KEYWORD_INTERFACE;
mode KEYWORD_IP;
mode KEYWORD_ROUTE;
// ... 20 more modes for each keyword

// ✅ GOOD: Reuse modes and handle in parser
mode M_Name;
M_Name_KEYWORD: 'interface' -> type(KEYWORD), mode(M_Default);
M_Name_KEYWORD: 'ip' -> type(KEYWORD), mode(M_Default);
```

Too many modes make the lexer hard to maintain. Use modes sparingly.

### 5. Not Handling NEWLINE in Modes

```antlr
mode M_Example;
M_Example_TOKEN: 'some_token';
// ❌ WRONG: No NEWLINE handling, can't recover from errors
```

Always include NEWLINE handling in modes to ensure lexer can recover and continue parsing.

---

## Performance Tips

### 1. Reduce Lexer Backtracking

Excessive lexer backtracking slows down parsing significantly.

```antlr
// ❌ BAD: High backtracking potential
complex_rule: (option1 | option2 | option3)+;

// ✅ BETTER: Restrict alternatives
complex_rule: (option1 | option2 | option3)?;  // Optional, not greedy
```

### 2. Memoization

ANTLR4 automatically memoizes parsing results for left-recursive rules. Ensure rules are properly structured for memoization to work.

**Signs of good memoization**:
- Rules are left-recursive (or can be refactored to be)
- Predicates don't prevent memoization
- Parse tree is reasonable depth

### 3. Token Lookahead Limits

Adjust ANTLR4 lookahead if needed (rare):

```bash
# Default is usually sufficient
antlr4 -Dlanguage=Java MyGrammar.g4

# Increase if you have complex ambiguities
antlr4 -Dlanguage=Java -Dk=2 MyGrammar.g4
```

Don't increase without reason - larger k = slower parser.

### 4. Use Set Types for Literals

```antlr
// ❌ BAD: Could be parsed as name
TOKEN: 'token';

// ✅ GOOD: Clear it's a literal
TOKEN: [tT][oO][tT][kK][eE][nN];
```

Batfish convention: Use quoted strings for keywords in lexer.

---

## Debugging Grammars

### Enable ANTLR4 Output

```bash
# Generate parser with debug output
antlr4 -Dlanguage=Java -Xlog MyGrammar.g4
```

### Use ANTLR4 Plugin

In IntelliJ IDEA:
- Install ANTLR4 plugin
- Right-click `.g4` file → Test Rule
- Visualize parse trees
- Debug tokenization

---

## Related Documentation

- [Parser Rule Conventions](parser_rule_conventions.md) - Batfish-specific conventions
- [Lexer Mode Patterns](lexer_mode_patterns.md) - Batfish mode patterns
- [Implementation Guide](implementation_guide.md) - Adding new commands
- [ANTLR4 Documentation](https://github.com/antlr/antlr4/blob/master/doc/index.md) - Official ANTLR4 docs

---

## Quick Reference

### Lexer Rule Checklist
- [ ] Most specific rules first
- [ ] No direct left recursion
- [ ] Proper mode push/pop
- [ ] NEWLINE handling in all modes
- [ ] Fragments for reusable patterns
- [ ] Tested with `grun` tool

### Parser Rule Checklist
- [ ] Single token advancement (where applicable)
- [ ] LL(1) compliant structure
- [ ] Named rules for complex alternatives
- [ ] Error handling considered
- [ ] Tests written for new rules
