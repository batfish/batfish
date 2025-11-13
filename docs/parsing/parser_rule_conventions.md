# Parser Rule Conventions for ANTLR4 Grammars in Batfish

This document explains the conventions and best practices for writing ANTLR4 parser rules in Batfish grammars. Following these conventions ensures consistency across all grammars and helps maintain the performance and reliability of the parsing system.

## LL(1) Grammar Design

Batfish uses LL(1) grammars, which means:

1. **Left Recursive Grammar**: Rules are designed to be left recursive, which is more efficient for ANTLR4's parsing algorithm.

2. **Single Token Advancement**: Each rule typically advances only one token at a time, which helps with error recovery and makes the grammar more maintainable.

3. **Predictable Alternatives**: Rules are written so that a decision among alternatives can be made by looking ahead only a single token (LL(1)).

### Example of LL(1) Grammar Structure

```antlr
// Good: LL(1) structure with clear token advancement
s_system
:
   SYSTEM
   (
      sys_host_name
      | sys_login_banner
   )
;

sys_host_name: HOST_NAME name = string NEWLINE;
sys_login_banner: LOGIN_BANNER banner = string NEWLINE;
```

### Non-LL(1) Patterns to Avoid

```antlr
// BAD: Not LL(1), requires looking ahead two tokens
s_system
:
   sys_host_name
   | sys_login_banner
;

sys_host_name: SYSTEM HOST_NAME ...;
sys_login_banner: SYSTEM LOGIN_BANNER ...;
```

## Single Token Advancement: A Critical Principle

### The Fundamental Rule

**Each rule should typically advance only one token at a time.** This is one of the most important principles for maintaining an LL(1) grammar that is robust, maintainable, and has good error recovery.

### What This Means in Practice

1. **Parent rules** should consume exactly one token and then delegate to child rules
2. **Child rules** should handle the rest of the parsing for their specific path
3. **Alternatives** should be handled by separate child rules, not within a single rule

### Examples

#### ✅ CORRECT: Single Token Advancement

```antlr
// Parent rule consumes one token (KEYWORD_A) then delegates
parent_rule
:
    KEYWORD_A (child_rule_1 | child_rule_2)
;

// Child rule for first alternative
child_rule_1
:
    OPTION_1
;

// Child rule for second alternative
child_rule_2
:
    OPTION_2 variable_input
;
```

#### ❌ INCORRECT: Multiple Tokens in One Rule

```antlr
// Bad: Consumes multiple tokens in a single rule
parent_rule
:
    KEYWORD_A
    (
        OPTION_1
        | OPTION_2 variable_input
    )
;
```

### Why This Matters

1. **Error Recovery**: When each rule advances only one token, the parser can recover more effectively from syntax errors
2. **Maintainability**: Rules are simpler and easier to understand
3. **Extensibility**: Adding new alternatives becomes easier
4. **Consistency**: Following this pattern creates a uniform grammar structure

## Rule Naming Conventions

### Basic Naming Pattern

Parser rules in Batfish follow a specific naming convention:

```
<prefix>_next_token
```

Where:

- `<prefix>` is a short identifier for the parent context
- `next_token` is the next token in the rule. It can be hyphenated like `next-token`, which would be written `_next_token`

### Examples

```antlr
// Top-level rule for SNMP configuration
s_snmp
:
   SNMP
   (
      snmp_community
      | snmp_name
      | snmp_trap_group
   )
;

// Rule for SNMP community configuration, where "community" is the last token
snmp_community
:
   COMMUNITY comm = junos_name
   (
      apply
      | snmpc_authorization
      | snmpc_client_list_name
   )
;
```

### Child Rule Naming

For child rules (rules that are only referenced by a specific parent rule), use a local extension of the parent prefix:

```
<parent_prefix><local_extension>_next_token
```

Where:

- `<parent_prefix>` is the prefix of the parent rule
- `<local_extension>` is typically a shortened version of the parent's last token
- `next_token` is the next token in this rule

### Examples

```antlr
// Parent rule
snmp_community: COMMUNITY comm = junos_name (...);

// Child rule with local extension 'c' from 'community'
snmpc_authorization: AUTHORIZATION (READ_ONLY | READ_WRITE);

// Another child rule
snmpc_client_list_name: CLIENT_LIST_NAME name = junos_name;
```

## Top-Level Rule Conventions

Top-level rules (those representing the first command on a line or in a hierarchy) often follow a special convention:

```
s_command_name
```

Where:

- `s_` prefix indicates a "statement" or top-level command
- `command_name` is the name of the command

### Examples

```antlr
s_snmp: SNMP (...);
s_system: SYSTEM (...);
s_interfaces: INTERFACES (...);
```

## Handling Multiple Next Tokens with Similar Shortenings

When multiple next tokens have the same obvious shortening, use these guidelines to choose prefixes:

1. **Frequency of occurrence**: More common constructs get the simpler/shorter prefix
2. **Complexity of grammar**: More complex grammar structures get priority for simpler prefixes
3. **Consistency across grammars**: Maintain consistent prefixes across different vendor grammars
4. **Multiple obvious shortenings**: Use the most distinctive shortening when multiple options exist

### Examples

For "interface" vs "ip" commands:

- Use `i` or `if` for interface (more common and complex grammar)
- Use `ip` for ip commands (already short)
- Use `ip_acl` for "ip access-list" (more specific than just `ip_a`)

## Handling Prefix Collisions

When a natural prefix is already in use, choose a longer prefix that avoids collisions.

**Example**: In Cisco grammars, `dt_` is used for "depi-tunnel" so "device-tracking" uses `dtr_` instead.

## Inlining Simple Alternatives

Inline simple alternatives instead of creating separate child rules:

**Inline** when alternatives are simple tokens without parameters (enable/disable, true/false).

**Don't inline** when alternatives have different parameters, require different extraction logic, or form a long list (4+ alternatives).

**See**: Cisco_device_tracking.g4 `dtrp_tracking` (inlined) vs `dtrp_limit` (separate child rules).

## Opportunistic Improvements

While these conventions should be followed for all new grammar rules, existing rules may not always conform to these patterns. When working with existing code:

1. Fix non-conforming rules opportunistically when making other changes to that area
2. Ensure backward compatibility when renaming rules
3. Update all references when changing rule names
4. Document significant refactoring in commit messages

## Testing Grammar Rules

When writing or modifying grammar rules:

1. Test with valid configurations to ensure correct parsing
2. Test with invalid configurations to verify error recovery
3. Check that the parse tree structure is as expected
4. Verify that extraction works correctly for the parsed rules

## References

For more information on Batfish parsing, see:

- [Parsing Documentation](README.md)
- [Implementation Guide](implementation_guide.md)
- [Lexer Mode Patterns](lexer_mode_patterns.md)
