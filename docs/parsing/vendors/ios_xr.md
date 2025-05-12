# Cisco IOS-XR-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting IOS-XR configurations in Batfish.

## IOS-XR Configuration Structure

IOS-XR configurations have several unique characteristics:

1. **Hierarchical Structure**: Configurations are organized in a hierarchical structure
2. **Command Syntax Flexibility**: Many commands support both single-line and multi-line syntax

## IOS-XR Grammar Structure

The IOS-XR grammar is split into several files:

- `IosXrLexer.g4`: Defines tokens for the lexer
- `IosXrParser.g4`: Main parser file
- `IosXr_acl.g4`: Access Control List-specific grammar
- `IosXr_bgp.g4`: BGP-specific grammar
- `IosXr_ospf.g4`: OSPF-specific grammar
- And many other feature-specific grammar files

## Common IOS-XR Parsing Patterns

### Single-Line vs Multi-Line Syntax

A key challenge in parsing IOS-XR configurations is handling commands that can be expressed in both single-line and multi-line formats. For example, access lists can be defined in either format:

**Single-line format:**

```
ipv4 access-list ACL-NAME permit tcp host 192.0.2.1 any eq 22
```

**Multi-line format:**

```
ipv4 access-list ACL-NAME
 10 permit tcp host 192.0.2.1 any eq ssh
!
```

Commands can be entered in single-line mode, but IOS-XR will display them in block mode when showing the configuration.

### Grammar Structure for Dual-Format Commands

To handle both formats, the grammar must be structured to recognize both patterns. This typically involves:

1. Creating a parent rule that can match either format
2. Creating separate child rules for single-line and multi-line formats
3. Ensuring the extraction logic handles both formats correctly

Example grammar structure for access lists:

```antlr
s_ipv4_access_list
:
  IPV4 ACCESS_LIST name = variable
  (
    ipv4_acl_block
    // Single acl lines below this
    | ipv4_acl_line
  )
;

ipv4_acl_block
:
  NEWLINE
  ipv4_acl_line*
;

ipv4_acl_line: ...;
```

Note that the block mode requires a `NEWLINE` and allows for 0 or more ACL lines, but when the newline is not present the `ipv4_acl_line` suffix is allowed exactly once.

## Implementation Decision Guide for IOS-XR

When implementing a new IOS-XR command, consider:

1. **Does the command support both single-line and multi-line syntax?** If so, structure the grammar to handle both formats
2. **Which configuration section does it belong to?** (interfaces, routing protocols, etc.)
3. **Is it a common pattern?** Look for similar commands as examples

### Example: Adding Support for Single-Line Access Lists

To add support for single-line access list syntax:

1. **Identify the existing multi-line grammar rules**:

   - Locate the rules that handle multi-line access list syntax

2. **Add support for single-line format**:

   - Modify the parent rule to accept either format
   - Create a new rule for the single-line format
   - Ensure the rule captures all necessary parameters

3. **Update extraction logic**:

   - Ensure the extractor can handle both formats
   - Extract the same data model objects regardless of format

4. **Add tests for both formats**:
   - Create test cases for both single-line and multi-line formats
   - Verify that both formats produce identical data model objects

## References

- [Parsing Documentation](../README.md)
- [Implementation Guide](../implementation_guide.md)
