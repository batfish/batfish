# Plan: Fix Cisco NXOS Grammar for Missing Top-Level Commands

## Context

The Cisco NXOS grammar is missing support for several top-level commands found in real-world configs:
- `power redundancy-mode ps-redundant`
- `hardware ejector enable`, `hardware access-list lou resource threshold 5`, `hardware profile multicast max-limit lpm-entries 2048`
- `ha-policy single-sup reload dual-sup switchover`
- `keystore type default`
- `telnet login-attempts 3`
- `vdc combined-hostname` (without `id` clause)

These cause parsing errors/warnings when processing configs.

---

## Current State Analysis

### What's already handled
| Token | Location | Status |
|-------|----------|--------|
| LICENSE | s_null | ✅ Handled |
| TELNET | no_ip_null | ⚠️ Only for `no ip telnet`, not `telnet ...` |
| HARDWARE | Lexer line 868 | ❌ Not in s_null |
| HA_POLICY | Lexer line 868 | ❌ Not in s_null |
| VDC | Lexer line 2515 | ⚠️ s_vdc requires `id` clause |

### What's missing
| Token | Needed In | Status |
|-------|-----------|--------|
| POWER | Lexer + s_null | ❌ Token doesn't exist |
| KEYSTORE | Lexer + s_null | ❌ Token doesn't exist |
| HARDWARE | s_null + no_null | ❌ Token exists, rule missing |
| HA_POLICY | s_null | ❌ Token exists, rule missing |
| TELNET | s_null | ❌ Token exists, rule missing |

---

## Important: `_null` Suffix Convention

Per `docs/parsing/README.md`:
- **Leaf rules** (don't call other rules) **SHOULD** have `_null` suffix if not extracted
- **Non-leaf rules** (call other rules) **SHOULD NOT** have `_null` suffix

Since we're using `null_rest_of_line` for simple passthrough, these are leaf rules but the convention for top-level statements is `s_xxx` without `_null` suffix, using `null_rest_of_line` internally.

---

## Implementation Decision

These commands should use `null_rest_of_line` because they:
- Don't affect control plane behavior
- Don't affect forwarding decisions
- Don't affect security posture (filtering)
- Are operational/management settings

---

## Incremental Change Plan (Stacked PRs for `spr`)

### PR 1: Add missing lexer tokens (POWER, KEYSTORE)
**Files:** `projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar/CiscoNxosLexer.g4`

Add tokens in alphabetical order:
```antlr
// After LINE: line 1156
KEYSTORE: 'keystore';

// After POINT_TO_POINT: line 1724
POWER: 'power';
```

**Verification:**
```bash
bazel build //projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar:grammar
```

---

### PR 2: Add HARDWARE to s_null and no_null
**Files:** `projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar/CiscoNxosParser.g4`

1. Add `HARDWARE` to `s_null` rule (line 350-364):
```antlr
s_null
:
  (
    CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | HARDWARE    // ADD THIS
    | LICENSE
    | SERVICE
    | SSH
    | SPANNING_TREE
    | USERNAME
    | USERPASSPHRASE
  ) null_rest_of_line
;
```

2. Add `HARDWARE` to `no_null` rule (line 408-415):
```antlr
no_null
:
  (
    FEATURE
    | HARDWARE    // ADD THIS
    | IP
    | NTP
  ) null_rest_of_line
;
```

This handles:
- `hardware ejector enable`
- `hardware access-list lou resource threshold 5`
- `hardware profile multicast max-limit lpm-entries 2048`
- `no hardware module boot-order reverse`

**Verification:**
```bash
bazel build //projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar:grammar
bazel test //projects/batfish/src/test/java/org/batfish/vendor/cisco_nxos/grammar:tests
```

---

### PR 3: Add TELNET to s_null
**Files:** `projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar/CiscoNxosParser.g4`

Add `TELNET` to `s_null` rule in alphabetical order:
```antlr
s_null
:
  (
    CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | HARDWARE
    | LICENSE
    | SERVICE
    | SPANNING_TREE
    | SSH
    | TELNET      // ADD THIS
    | USERNAME
    | USERPASSPHRASE
  ) null_rest_of_line
;
```

This handles: `telnet login-attempts 3`

**Verification:** Build + existing tests pass

---

### PR 4: Add POWER to s_null
**Files:** `projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar/CiscoNxosParser.g4`

Add `POWER` to `s_null` rule in alphabetical order:
```antlr
s_null
:
  (
    CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | HARDWARE
    | LICENSE
    | POWER       // ADD THIS
    | SERVICE
    | SPANNING_TREE
    | SSH
    | TELNET
    | USERNAME
    | USERPASSPHRASE
  ) null_rest_of_line
;
```

This handles: `power redundancy-mode ps-redundant`

**Verification:** Build + existing tests pass

---

### PR 5: Add HA_POLICY to s_null
**Files:** `projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar/CiscoNxosParser.g4`

Add `HA_POLICY` to `s_null` rule in alphabetical order:
```antlr
s_null
:
  (
    CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | HA_POLICY   // ADD THIS
    | HARDWARE
    | LICENSE
    | POWER
    | SERVICE
    | SPANNING_TREE
    | SSH
    | TELNET
    | USERNAME
    | USERPASSPHRASE
  ) null_rest_of_line
;
```

This handles: `ha-policy single-sup reload dual-sup switchover`

**Verification:** Build + existing tests pass

---

### PR 6: Add KEYSTORE to s_null
**Files:** `projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar/CiscoNxosParser.g4`

Add `KEYSTORE` to `s_null` rule in alphabetical order:
```antlr
s_null
:
  (
    CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | HA_POLICY
    | HARDWARE
    | KEYSTORE    // ADD THIS
    | LICENSE
    | POWER
    | SERVICE
    | SPANNING_TREE
    | SSH
    | TELNET
    | USERNAME
    | USERPASSPHRASE
  ) null_rest_of_line
;
```

This handles: `keystore type default`

**Verification:** Build + existing tests pass

---

### PR 7: Update VDC grammar for combined-hostname
**Files:** `projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar/CiscoNxos_vdc.g4`

Update `s_vdc` to make `id` clause optional:
```antlr
s_vdc
:
  VDC name = vdc_name
  (
    ID id = vdc_id NEWLINE
    (
      vdc_allocate
      | vdc_allow
      | vdc_cpu_share
      | vdc_limit_resource_ignored
    )*
    | NEWLINE  // Standalone vdc <name> like "vdc combined-hostname"
  )
;
```

This handles:
- `vdc combined-hostname` (new)
- `vdc test-switch id 1 ...` (existing behavior preserved)

**Verification:** Build + existing tests pass

---

## Final s_null Rule (After All PRs)

```antlr
s_null
:
  (
    CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | HA_POLICY
    | HARDWARE
    | KEYSTORE
    | LICENSE
    | POWER
    | SERVICE
    | SPANNING_TREE
    | SSH
    | TELNET
    | USERNAME
    | USERPASSPHRASE
  ) null_rest_of_line
;
```

---

## Files to Modify

| File | PR | Changes |
|------|-----|---------|
| `CiscoNxosLexer.g4` | 1 | Add POWER, KEYSTORE tokens |
| `CiscoNxosParser.g4` | 2-6 | Add tokens to s_null, HARDWARE to no_null |
| `CiscoNxos_vdc.g4` | 7 | Make s_vdc ID clause optional |

---

## Verification Plan

After each PR:
```bash
# Build grammar
bazel build //projects/batfish/src/main/antlr4/org/batfish/vendor/cisco_nxos/grammar:grammar

# Run tests
bazel test //projects/batfish/src/test/java/org/batfish/vendor/cisco_nxos/grammar:tests
```

Final verification: Parse the problematic config file and confirm no new errors.

---

## Using `spr` for Stacked PRs

```bash
# After making each change locally
git add -p
git commit -m "nxos: add POWER and KEYSTORE lexer tokens"

# Create PR
spr create

# Repeat for each PR in sequence
```

This creates stacked PRs where each PR depends on the previous one, making review easier.
