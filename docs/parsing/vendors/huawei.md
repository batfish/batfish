# Huawei Parsing Notes

This page documents Huawei VRP parsing details that differ from common Batfish grammar patterns.

## Huawei-Specific Behaviors

1. Block termination uses the `return` keyword.
2. `undo` negates a setting (similar to `no` on other vendors).
3. Interface names include `/` and `-` forms (`GigabitEthernet0/0/0`, `Eth-Trunk1`, `LoopBack0`).

## Current Grammar Structure

- Lexer: `projects/batfish/src/main/antlr4/org/batfish/vendor/huawei/grammar/HuaweiLexer.g4`
- Parser: `projects/batfish/src/main/antlr4/org/batfish/vendor/huawei/grammar/HuaweiParser.g4`
- Extractor: `projects/batfish/src/main/java/org/batfish/vendor/huawei/grammar/HuaweiControlPlaneExtractor.java`
- Representation: `projects/batfish/src/main/java/org/batfish/vendor/huawei/representation/`

## Core Patterns

### Return-Terminated Blocks

Use a dedicated `return_line` rule as the block terminator:

```antlr
s_interface
  : INTERFACE name=interface_name NEWLINE interface_statement* return_line
  ;

return_line: RETURN NEWLINE;
interface_name: word;
```

### Undo Handling

Avoid separate positive/negative rules that share the same prefix. Prefer one LL(1) rule:

```antlr
is_shutdown: UNDO? SHUTDOWN NEWLINE;
```

Extraction decides meaning from the presence of `UNDO`.

### Numeric Tokens

Do not use a generic decimal token. Define typed numeric tokens in the lexer (`UINT8`, `UINT16`, `UINT32`) and use parser rules with explicit intent (for example `bgp_asn`, `vlan_id`, `ospf_process_id`).

## Adding Huawei Support for a New Command

1. Add lexer token(s) in `projects/batfish/src/main/antlr4/org/batfish/vendor/huawei/grammar/HuaweiLexer.g4`.
2. Add parser rule(s) in `projects/batfish/src/main/antlr4/org/batfish/vendor/huawei/grammar/HuaweiParser.g4`.
3. Add extractor logic in `projects/batfish/src/main/java/org/batfish/vendor/huawei/grammar/HuaweiControlPlaneExtractor.java`.
4. Add/update fields in `projects/batfish/src/main/java/org/batfish/vendor/huawei/representation/` if vendor state changes.
5. Add conversion logic in `projects/batfish/src/main/java/org/batfish/vendor/huawei/representation/HuaweiConversions.java` if VI model changes.
6. Add parser/extractor/conversion tests under `projects/batfish/src/test/java/org/batfish/vendor/huawei/`.

## Test Guidance

Prefer checked-in resource configs under `projects/batfish/src/test/resources/org/batfish/vendor/huawei/grammar/testconfigs/` for multiline scenarios. Keep inline test strings for very small focused cases only.

## References

- [Parsing Documentation](../README.md)
- [Implementation Guide](../implementation_guide.md)
- [ANTLR4 Tips and Tricks](../antlr4_tips.md)
