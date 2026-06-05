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

## Preprocessing decision

SR-OS will follow the Junos two-grammar **flatten → preprocess** model (mirrors
`juniper` → `JuniperFlattener` → `flatjuniper`), decided during
characterization. The rationale:

- Both brace and flat forms are legitimate, mixable inputs, so an outer grammar
  recognizes both and flattens braced blocks to one canonical absolute-path
  `/configure …` stream.
- Incremental edit verbs are grammar (`delete`/`-`/`~`/`insert before|after`/
  `replace … with`/`copy`/`rename`); a preprocessor applies them in order.
- `apply-groups` config-group inheritance must be expanded by Batfish (configs
  ship unexpanded): regex/wildcard key match, first-listed-wins precedence,
  `apply-groups-exclude`.
- Per-list ordering comes from YANG `ordered-by`; an absent leaf means the YANG
  default. BGP group→neighbor inheritance is resolved in extraction.

The grammar, preprocessor, and extractor are implemented in later phases (P3+).
