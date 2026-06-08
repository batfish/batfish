# Nokia SR-OS-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting Nokia SR-OS
(SR-SIM) configurations in Batfish. It is written incrementally as support is
built; sections appear as the corresponding pipeline stage lands.

## Configuration format

SR-OS runs two CLI engines: classic CLI and MD-CLI. Modern collected
configurations are MD-CLI, a curly-brace hierarchical format rooted at
`configure { ... }` — structurally closer to Junos than to line-oriented IOS.
A device emits this form via `admin show configuration`.

```
# TiMOS-B-26.3.R1 both/x86_64 Nokia 7750 SR-1 Copyright (c) 2000-2026 Nokia.
# Configuration format version 26.3 revision 0
configure {
    router "Base" {
        autonomous-system 65001
        ...
    }
}
```

SR-OS also supports an absolute-path flat form — each line is a full path from
root, e.g. `/configure router "Base" autonomous-system 65001`. This is the
Junos-`set` analog (`pwc cli-path`, `info full-context`): position-independent,
copy-pasteable, and an editable input. The two forms can be **mixed in one
input** (a brace production config with appended flat `/configure …` edits).

## Format detection

SR-OS is registered as `ConfigurationFormat.NOKIA_SROS`. Detection lives in
[`VendorConfigurationFormatDetector`](../../../projects/batfish/src/main/java/org/batfish/grammar/VendorConfigurationFormatDetector.java)
(`checkSros`) and keys on SR-OS-specific tells rather than brace structure
alone (which collides with Juniper/Palo Alto heuristics):

- the TiMOS banner (`# TiMOS-…`),
- the `# Configuration format version <X.Y> revision <N>` header, or
- absolute-path flat lines (`/configure …`).

`checkSros` runs **before** `checkJuniper`/`checkCisco` because an SR-OS brace
config contains tokens those heuristics would otherwise claim (e.g.
`policy-options {`, `interface …`). RANCID content types `sros` and `sros-md`
also map to `NOKIA_SROS` (previously routed to `UNSUPPORTED`).

## Parser (P3)

The parser lives under the vendor-scoped path
[`org.batfish.vendor.sros.grammar`](../../../projects/batfish/src/main/antlr4/org/batfish/vendor/sros/grammar/):
`SrosLexer.g4`, `SrosParser.g4`, `SrosCombinedParser`, `SrosBaseLexer`,
`SrosControlPlaneExtractor`, and `SrosConfigurationBuilder`. It produces an
`SrosConfiguration` (in the `representation` subpackage).

### Canonical form: one hierarchical grammar, not a flatten pass

P1 left an open architecture question: flatten brace → flat `/configure …` lines
(the Junos/Palo Alto pattern), or keep a single hierarchical grammar and treat
flat lines as ordinary statements. **P3 chose the single-grammar
hierarchical-canonical approach.** One grammar accepts all three input forms,
because they are structurally the same token stream of statements:

- the brace/hierarchical form from `admin show configuration` (`configure { … }`),
- the absolute-path flat form, where each line is a `/configure …` statement, and
- a mix of the two in one file.

A flat `/configure …` line is simply a leaf `statement` whose first word begins
with `/`. The `SrosConfigurationBuilder` walks the tree and normalizes **every**
leaf (or empty block) to one canonical absolute-path string — the path words
joined by spaces, with a leading `/configure` rewritten to `configure`. So all
three forms yield the identical set of canonical statements; the mixed case (the
one that breaks designs assuming one form per file) is correct by construction.

Why this over the flatten pipeline:

- **Mixed-form equivalence is the literal output**, with no second grammar and no
  `FlattenerLineMap` round-trip.
- **True source line numbers are preserved**, so parse warnings point at the
  original config without offset bookkeeping.
- **Fewest moving parts** for the P3 gate (parse the captured config with zero
  FATAL warnings). The Palo Alto flattener already builds a `SetStatementTree`
  internally, so the "flatten" and "tree" camps converge anyway.

This mirrors the *spirit* of the Junos pipeline (one canonical statement form fed
to extraction) while avoiding the text round-trip.

### Deferred to extraction (P4+)

The following were characterized in P1 and are deferred to the extraction phase,
where feature-specific modeling needs them; they operate on the canonical
statement tree this parser produces:

- Incremental edit verbs (`delete`/`-`/`~`/`insert before|after`/`replace … with`/
  `copy`/`rename`) — applied in order as a preprocessor stage.
- `apply-groups` config-group inheritance — Batfish must expand it (configs ship
  unexpanded): regex/wildcard key match, first-listed-wins precedence,
  `apply-groups-exclude`.
- Per-list ordering from YANG `ordered-by`; absent leaf ⇒ YANG default. BGP
  group→neighbor inheritance resolved in extraction.

As of P3 the builder records the canonical statements and extracts the system
name as the hostname; feature extraction and conversion are P4/P5.

### Tests

[`SrosGrammarTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosGrammarTest.java)
covers the P3 gate and the mixed-form acceptance requirement:

- the captured P0 lab r1 `admin show configuration` parses with zero FATAL
  warnings and nothing unrecognized;
- pure-brace, pure-flat, and mixed inputs describing the same configuration
  produce the identical canonical statement list.
