# Grammar Validator

This tool validates Batfish ANTLR grammar files against the conventions documented in `docs/parsing/README.md`.

## What it validates

1. **Parser rule naming conventions**
   - Rules must be lowercase
   - Top-level rules should use `s_` prefix
   - `_null` suffix rules must be leaf rules (don't call other non-null rules)
   - Non-leaf rules should NOT have `_null` suffix
   - **Note**: References to null helpers (`null_rest_of_line`, `null_filler`, etc.) do NOT make a rule non-leaf

2. **Lexer rule naming conventions**
   - Lexer rules must be ALL_CAPS with underscores
   - Fragments should use `F_CamelCase` naming (e.g., `F_Uint8`, `F_Ipv4Address`)
   - Mode tokens should follow `M_ModeName_TOKEN_NAME` pattern

3. **NEWLINE placement**
   - NEWLINE should be at leaf level, not parent level
   - Helps with error recovery

4. **LL(1) hints**
   - Warns if multiple alternatives start with same token
   - Helps identify potential LL(1) violations

5. **Import structure**
   - Warns about potential circular imports

6. **Rule ordering and grouping** (warnings only)
   - Rules should be sorted alphabetically within prefix groups
   - Rules with the same prefix should be grouped together

## Usage

### Run manually on all grammar files
```bash
python3 tools/validate_grammar_rules.py
```

### Run on specific file
```bash
python3 tools/validate_grammar_rules.py projects/batfish/src/main/antlr4/org/batfish/grammar/cisco/Cisco_crypto.g4
```

### Verbose mode (shows warnings)
```bash
python3 tools/validate_grammar_rules.py --verbose
```

### Validate only changed rules (via git diff)
```bash
python3 tools/validate_grammar_rules.py --changed-only path/to/file.g4
```

### Pre-commit hook
The validator is automatically run by pre-commit on any `.g4` files you modify. To install:

```bash
pip install pre-commit
pre-commit install
```

## Exit codes

- `0`: All validations passed (or only warnings)
- `1`: One or more errors found

Use `--warn-only` to never fail:
```bash
python3 tools/validate_grammar_rules.py --warn-only
```

## Examples

### Good: Non-leaf rule without _null suffix
```antlr
crypto_key
:
   KEY
   (
      ck_null           // specific subcommand
      | ck_pubkey_chain // specific subcommand
      | null_rest_of_line // fallback for unrecognized
   )
;
```
✅ No `_null` suffix (non-leaf rule - calls other rules)
✅ Has fallback for unrecognized commands
✅ Allows parsing without errors

### Bad: Incorrect _null usage on non-leaf rule
```antlr
// WRONG: Non-leaf rule with _null suffix
crypto_key_null
:
   KEY
   (
      ck_null
      | ck_pubkey_chain
   )
;
```
❌ Calls other rules (`ck_null`, `ck_pubkey_chain`) but has `_null` suffix

### Good: Leaf rule with _null suffix
```antlr
// CORRECT: Leaf rule with null helper - null_rest_of_line doesn't count as a child rule
ck_import_null
:
   IMPORT null_rest_of_line
;
```
✅ Leaf rule with `_null` suffix
✅ `null_rest_of_line` is a null helper, doesn't make this non-leaf

### Good: Fragment naming
```antlr
// CORRECT: Fragment with F_CamelCase naming
fragment
F_Uint8
:
   [0-9]
   | [1-9][0-9]
   | '1'[0-9][0-9]
   | '2'[0-4][0-9]
   | '25'[0-5]
;
```
✅ Fragment uses `F_` prefix with CamelCase

### Good: Mode token naming
```antlr
mode M_StringList;

M_StringList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_StringList_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_DoubleQuotedString);
```
✅ Mode tokens follow `M_ModeName_TOKEN_NAME` pattern
