# Lexer Mode Patterns for Name Handling in Batfish

This document explains the patterns for handling names in Batfish lexers, using some real examples for common name handling scenarios.

## Basic Name Handling Modes

In the FlatJuniperLexer.g4 file, there are several modes for handling names:

### 1. Basic Name Mode (M_Name)

```antlr
mode M_Name;

M_Name_NAME: F_Name -> type(NAME), popMode;
M_Name_WS: F_WhitespaceChar+ -> skip;
M_Name_NEWLINE: F_Newline -> type(NEWLINE), popMode;
```

This is the basic mode for handling names. It emits a NAME token and returns to the previous mode.

### 2. Mode chains: Name List Mode (M_NameList and M_NameListInner)

This is an example of a mode with an inner mode: handling lists of names with brackets:

```antlr
mode M_NameList;

M_NameList_WS: F_WhitespaceChar+ -> skip;
M_NameList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_NameList_NAME: F_Name -> type(NAME), popMode;
M_NamList_OPEN_BRACKET: '[' -> type(OPEN_BRACKET), mode(M_NameListInner);

mode M_NameListInner;
M_NameListInner_WS: F_WhitespaceChar+ -> skip;
M_NameListInner_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_NameListInner_NAME: F_Name -> type(NAME);
M_NamList_CLOSE_BRACKET: ']' -> type(CLOSE_BRACKET), popMode;
```

### 3. Name or IP Mode (M_NameOrIp)

Sometimes you have to copy the `M_Name` to support extra keywords, such as this example for handling either a names or an IP addresses:

```antlr
mode M_NameOrIp;

M_NameOrIp_WS: F_WhitespaceChar+ -> skip;
M_NameOrIp_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_NameOrIp_IP_ADDRESS: F_IpAddress -> type(IP_ADDRESS), popMode;
M_NameOrIp_IPV6_ADDRESS: F_Ipv6Address -> type(IPV6_ADDRESS), popMode;
M_NameOrIp_NAME: F_Name -> type(NAME), popMode;
```

## The MPLS Admin-Group Pattern

The MPLS admin-group implementation demonstrates an important pattern for handling complex name-based configurations:

```antlr
mode M_AdminGroup;
M_AdminGroup_EXCLUDE: 'exclude' -> type(EXCLUDE), mode(M_Name);
M_AdminGroup_INCLUDE_ALL: 'include-all' -> type(INCLUDE_ALL), mode(M_Name);
M_AdminGroup_INCLUDE_ANY: 'include-any' -> type(INCLUDE_ANY), mode(M_Name);
M_AdminGroup_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_AdminGroup_NAME: F_Name -> type(NAME), popMode;
M_AdminGroup_WS: F_WhitespaceChar+ -> skip;
M_AdminGroup_NEWLINE: F_NewlineChar+ -> type(NEWLINE), popMode;
```

### Key Pattern: Mode Chaining for Keywords + Names

The important pattern here is:

1. Create a specialized mode (`M_AdminGroup`) that recognizes specific keywords
2. For those keywords, emit the appropriate token and then transition to M_Name mode
3. For simple names without keywords, emit NAME token and return to previous mode

This pattern is used when you need to handle both:

- Simple name references (e.g., `admin-group red`)
- Complex expressions with keywords (e.g., `admin-group exclude red`)

## Common Challenges and Solutions

### Challenge: Handling Keywords Before Names

When parsing constructs that can have either a simple name or a keyword followed by a name, you need to handle both cases correctly.

**Solution**: Create a specialized mode that:

- Recognizes specific keywords and transitions to M_Name
- Has a fallback rule for simple names

### Challenge: Nested Mode Transitions

When you need to handle complex nested structures (like lists of names with special keywords), mode transitions can become complex.

**Solution**:

- Use clear mode naming conventions
- Document mode transitions with comments
- Use the mode stack carefully (pushMode/popMode)

### Challenge: Error Recovery

Ensuring the lexer can recover from errors is critical, especially with complex mode transitions.

**Solution**:

- Always include NEWLINE rules that pop the mode
- Handle unexpected tokens appropriately
- Test error cases explicitly

## Implementation Guidelines

When implementing lexer modes for name handling:

1. **Use the Basic Pattern First**: Start with the basic M_Name mode pattern for simple name handling.

2. **Create Specialized Modes When Needed**: When you need to handle special keywords before a name, create a specialized mode like M_AdminGroup.

3. **Chain Modes Properly**: Use `mode(M_Name)` to transition to the name mode after handling keywords, rather than duplicating the name handling logic.

4. **Handle Error Recovery**: Include rules for newlines and other unexpected tokens to ensure the lexer can recover from errors.

5. **Document Mode Transitions**: When creating complex mode transitions, add comments explaining the expected token sequence and mode transitions.

## Example Implementation for New Features

When adding a new feature that requires name handling with keywords:

```antlr
// 1. Define the specialized mode
mode M_NewFeature;

// 2. Handle specific keywords with transition to M_Name
M_NewFeature_KEYWORD1: 'keyword1' -> type(KEYWORD1), mode(M_Name);
M_NewFeature_KEYWORD2: 'keyword2' -> type(KEYWORD2), mode(M_Name);

// 3. Handle simple names directly
M_NewFeature_NAME: F_Name -> type(NAME), popMode;

// 4. Standard whitespace and newline handling
M_NewFeature_WS: F_WhitespaceChar+ -> skip;
M_NewFeature_NEWLINE: F_Newline -> type(NEWLINE), popMode;
```

This pattern avoids duplicating the name handling logic while still allowing specialized keyword handling.

## Testing Lexer Modes

When testing lexer modes, consider:

1. **Simple Cases**: Test basic name handling
2. **Keyword Cases**: Test each keyword transition
3. **Error Cases**: Test recovery from missing closing tokens or unexpected input
4. **Nested Cases**: Test complex nested structures

## References

For more information on lexer modes in Batfish, see:

- [Parsing Documentation](../parsing/README.md)
- [Juniper-Specific Parsing](../parsing/vendors/juniper.md)
