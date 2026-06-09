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

### Tests

[`SrosGrammarTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosGrammarTest.java)
covers the P3 gate and the mixed-form acceptance requirement:

- the captured P0 lab r1 `admin show configuration` parses with zero FATAL
  warnings and nothing unrecognized;
- pure-brace, pure-flat, and mixed inputs describing the same configuration
  produce the identical canonical statement list.

## Extraction (P4)

Extraction turns the canonical statement stream into a navigable tree and then a
typed feature model.

### Canonical tree (`SrosStatementTree`)

[`SrosConfigurationBuilder`](../../../projects/batfish/src/main/java/org/batfish/vendor/sros/grammar/SrosConfigurationBuilder.java)
builds an
[`SrosStatementTree`](../../../projects/batfish/src/main/java/org/batfish/vendor/sros/grammar/SrosStatementTree.java)
alongside the P3 canonical statement list, from the **same word stream**, so the
brace, flat `/configure …`, and mixed forms all produce an identical tree. Every
word of every statement is one level of the tree, so a leaf's value is the single
child key under the leaf node (`card 1 card-type iom-1` →
`card → 1 → card-type → iom-1`), and a `policy [ a b ]` leaf-list is the ordered
children of the `policy` node (order preserved for `ordered-by user` lists).

### Preprocessor (`SrosPreprocessor`)

The two mechanisms P1 deferred from the grammar run here, on the tree, before
feature extraction:

- **`apply-groups` expansion** — grafts the matching subtree of each
  `groups group "<name>"` definition onto the branch that applied it. Replicates
  the §4.13 rules: local config wins over inherited, first-listed group wins,
  `apply-groups-exclude` suppresses a group at a branch, and group list keys may
  be regexes (`interface "<to-.*>"`) matched against the branch path. Runs to
  convergence so groups that themselves apply groups resolve.
- **`delete`/`-` edits** — incremental mutations that remove the named subtree;
  deleting an absent element is a silent no-op (§4.5). After expansion so a
  delete can remove inherited content.

The `groups` definition container is pruned afterward (it is inheritance source,
not configuration).

### Feature model (`SrosFeatureExtractor`)

Walks the preprocessed tree into the typed
[representation](../../../projects/batfish/src/main/java/org/batfish/vendor/sros/representation/):
hardware (`Card`/`Mda`, `Port`), routers (`Router`, `RouterInterface`), BGP
(`BgpProcess`, `BgpGroup`, `BgpNeighbor`), and routing policy (`PrefixList` with
composite prefix+type entries, `PolicyStatement` with numbered entries and a
default-action). List keys follow YANG: card by slot, mda by slot, port by path
string, router/interface by name, BGP group by name, neighbor by IP,
policy-statement entry by entry-id. BGP `neighbor/group` inheritance is recorded
on the neighbor and resolved at conversion (P5).

Subtrees that are valid config but not control-plane-relevant — `system
security`/`ssh`/`user-params` and `persistent-indices` — are intentionally left
unread, with no warnings (the device accepts them and so do we). The one system
leaf that matters, `system name`, becomes the Batfish hostname.

[`SrosExtractionTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosExtractionTest.java)
asserts the full r1 feature model extracts with no warnings, the unmodeled
subtrees are silently skipped, and the preprocessor handles apply-groups (incl.
regex keys + exclude) and delete edits.
