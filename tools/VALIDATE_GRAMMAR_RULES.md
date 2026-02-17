# Grammar Validator

This tool validates Batfish ANTLR grammar files against the conventions documented in `docs/parsing/README.md`.

## What it validates

1. **Parser rule naming conventions**
   - Rules must be lowercase
   - Top-level rules should use `s_` prefix
   - `_null` suffix rules must be leaf rules (don't call other rules)
   - Non-leaf rules should NOT have `_null` suffix

2. **Lexer rule naming conventions**
   - Lexer rules must be ALL_CAPS with underscores
   - Fragments should use `F_CamelCase` prefix

3. **NEWLINE placement**
   - NEWLINE should be at leaf level, not parent level
   - Helps with error recovery

4. **LL(1) hints**
   - Warns if multiple alternatives start with same token
   - Helps identify potential LL(1) violations

5. **Import structure**
   - Warns about potential circular imports

## Usage

### Run manually on all grammar files
```bash
python3 tools/validate_grammar_rules.py
```

### Run on specific file
```bash
python3 tools/validate_grammar_rules.py projects/batfish/src/main/antlr4/org/batfish/grammar/cisco/Cisco_crypto.g4
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

### Good: Your recent change to `crypto_key`
```antlr
crypto_key
:
   KEY
   (
      ck_null          // specific subcommand
      | ck_pubkey_chain  // specific subcommand
      | null_rest_of_line  // fallback for unrecognized
   )
;
```
✅ No `_null` suffix (non-leaf rule)
✅ Has fallback for unrecognized commands
✅ Allows parsing without errors

### Bad: Incorrect _null usage
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
❌ Calls other rules but has `_null` suffix

### Good: Leaf rule with _null
```antlr
// CORRECT: Leaf rule (no children)
ck_import_null
:
   IMPORT null_rest_of_line
;
```
✅ Leaf rule with `_null` suffix
