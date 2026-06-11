# Nokia SR-OS-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting Nokia SR-OS
(SR-SIM) configurations in Batfish, following the pipeline from format detection
through parsing, extraction, conversion, post-processing, and RIB validation.

The baseline validation lab, referenced throughout, is a Nokia SR-OS router
(`r1`) peered over eBGP with an Arista cEOS router (`r2`); because cEOS is
already fully modeled, r2 acts as an independent oracle for the SR-OS side.
Later feature labs add more SR-OS routers (`r3`, PEs) and are linked under
[Feature coverage](#feature-coverage).

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
also map to `NOKIA_SROS`.

## Parser

The parser lives under the vendor-scoped path
[`org.batfish.vendor.sros.grammar`](../../../projects/batfish/src/main/antlr4/org/batfish/vendor/sros/grammar/):
`SrosLexer.g4`, `SrosParser.g4`, `SrosCombinedParser`, `SrosBaseLexer`,
`SrosControlPlaneExtractor`, and `SrosConfigurationBuilder`. The builder produces
the canonical statement tree; the extraction-stage classes that turn it into the
typed `SrosConfiguration` (in the `representation` subpackage) are covered under
[Extraction](#extraction).

### Canonical form: one hierarchical grammar, not a flatten pass

There are two viable architectures: flatten brace → flat `/configure …` lines
(the Junos/Palo Alto pattern), or keep a single hierarchical grammar and treat
flat lines as ordinary statements. SR-OS uses the **single-grammar
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
- **Fewest moving parts** to parse the captured config with zero FATAL
  warnings. The Palo Alto flattener already builds a `SetStatementTree`
  internally, so the "flatten" and "tree" camps converge anyway.

This mirrors the *spirit* of the Junos pipeline (one canonical statement form fed
to extraction) while avoiding the text round-trip.

### Tests

[`SrosGrammarTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosGrammarTest.java)
covers parsing the captured config with zero FATAL warnings and the mixed-form
acceptance requirement:

- the captured lab r1 `admin show configuration` parses with zero FATAL
  warnings and nothing unrecognized;
- pure-brace, pure-flat, and mixed inputs describing the same configuration
  produce the identical canonical statement list.

## Extraction

Extraction turns the canonical statement stream into a navigable tree and then a
typed feature model.

### Canonical tree (`SrosStatementTree`)

[`SrosConfigurationBuilder`](../../../projects/batfish/src/main/java/org/batfish/vendor/sros/grammar/SrosConfigurationBuilder.java)
builds an
[`SrosStatementTree`](../../../projects/batfish/src/main/java/org/batfish/vendor/sros/grammar/SrosStatementTree.java)
alongside the canonical statement list, from the [same word
stream](#canonical-form-one-hierarchical-grammar-not-a-flatten-pass), so the
brace, flat `/configure …`, and mixed forms all produce an identical tree. Every
word of every statement is one level of the tree, so a leaf's value is the single
child key under the leaf node (`card 1 card-type iom-1` →
`card → 1 → card-type → iom-1`), and a `policy [ a b ]` leaf-list is the ordered
children of the `policy` node (order preserved for `ordered-by user` lists).

### Preprocessor (`SrosPreprocessor`)

Two mechanisms kept out of the grammar run here, on the tree, before
feature extraction:

- **`apply-groups` expansion** — grafts the matching subtree of each
  `groups group "<name>"` definition onto the branch that applied it. Replicates
  the apply-groups precedence rules (MD-CLI User Guide §4.13, "Using
  configuration groups"): local config wins over inherited, first-listed group wins,
  `apply-groups-exclude` suppresses a group at a branch, and group list keys may
  be regexes (`interface "<to-.*>"`) matched against the branch path. Runs to
  convergence so groups that themselves apply groups resolve.
- **`delete`/`-` edits** — incremental mutations that remove the named subtree;
  deleting an absent element is a silent no-op (MD-CLI User Guide §4.5,
  "Deleting configuration elements"). After expansion so a delete can remove
  inherited content.

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
policy-statement entry by entry-id. BGP `group`→`neighbor` inheritance is
resolved here, on the model (`BgpNeighbor.inheritFrom`): after both are
extracted, each neighbor fills any unset `type`/`peer-as`/`import`/`export` from
its group, so conversion reads a fully-populated neighbor (the NX-OS
`doInherit`-style pattern).

Subtrees that are valid config but not control-plane-relevant — `system
security`/`ssh`/`user-params` and `persistent-indices` — are intentionally left
unread, with no warnings (the device accepts them and so do we). The one system
leaf that matters, `system name`, becomes the Batfish hostname.

[`SrosExtractionTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosExtractionTest.java)
asserts the full r1 feature model extracts with no warnings, the unmodeled
subtrees are silently skipped, and the preprocessor handles apply-groups (incl.
regex keys + exclude) and delete edits.

### Source provenance: line-stamped warnings + structure references

Because extraction runs on the tree rather than on the parse tree, the tree must
carry source provenance for warnings and reference tracking to work. Each
`SrosStatementTree` node records the `ParserRuleContext`(s) of the statement(s)
that created it (`addDefContext`). A statement's context lands on its **deepest**
node — which for a single-valued leaf is the *value* node (`autonomous-system
5000000000` → the `5000000000` node) and for a keyed list entry is the
*structure-key* node, whose context spans the whole brace block (`start..stop`).
This is the SR-OS analog of how Juniper threads `ParserRuleContext`s into its
extractor; SR-OS differs from Juniper in needing **no** `FlattenerLineMap`,
because it is not flattened — the hierarchical grammar parses brace, flat, and
mixed input directly, so `parser.getLine(token)` is the true source line. A node
may carry several contexts (the same path configured by mixed brace + flat input,
or content inherited via `apply-groups`, which `copyInto` propagates so an
inherited value or reference is attributed to the **group definition's** line).

Two things ride on this provenance:

- **Line-stamped value warnings.** Malformed or out-of-range values produce a
  `ParseWarning` on the offending source line, not a context-free red-flag. The
  range checks use the shared `toIntegerInSpace`/`toLongInSpace` idiom (as in
  flatjuniper/palo_alto) bounded to the YANG-authoritative spaces:
  `autonomous-system` 1–4294967295, interface `prefix-length` 0–32,
  policy-statement `entry-id` 1–65535. Unlike grammar-driven extractors, the SR-OS
  value is *not* grammar-guaranteed numeric (every leaf value is a generic word),
  so a non-numeric value is warned here rather than assumed away. Because these are
  `ParseWarning`s, the [`annotate`](../../../projects/batfish/src/main/java/org/batfish/main/annotate/Annotate.java)
  tool renders them inline above the source line (with a `#` comment header, since
  SR-OS uses `#` comments).
- **Structure definitions and references.** `defineStructure` records each
  `prefix-list`, `policy-statement`, and `bgp group` definition (with its
  definition lines); `referenceStructure` records each use — a neighbor's `group`,
  the BGP `import`/`export` policy lists, and a policy entry's
  `from prefix-list` — keyed to the referring line. `SrosConfiguration.toVendorIndependentConfigurations`
  finalizes with `markConcreteStructure`, so the `definedStructures`,
  `undefinedReferences`, and unused-structure (zero-referrer) questions all work
  for SR-OS. The conversion-time guards that skip an undefined prefix-list/group no
  longer warn — the undefined reference is reported once, by the structure manager.

`SrosExtractionTest` covers the line-stamped value warnings (including a value
inherited from a group, cited to the group's line); `SrosConversionTest` covers
definition lines (a multi-line brace block records >1 line), undefined references,
and unused (zero-referrer) structures; `AnnotateTest#testSros` covers the
end-to-end annotate output.

## Conversion

`SrosConfiguration.toVendorIndependentConfigurations()` converts the typed
feature model into the vendor-independent `Configuration`. The conversion logic lives in
[`SrosConversions`](../../../projects/batfish/src/main/java/org/batfish/vendor/sros/representation/SrosConversions.java).
The SR-OS-specific semantics that shape it:

### Router instance ↔ VRF

Each SR-OS `router "<name>"` instance becomes a VRF. The `Base` instance — the
main routing instance — maps to the Batfish default VRF (`"default"`); any other
instance maps to a VRF of the same name. Interfaces and the BGP process are
attached to their instance's VRF.

### Interfaces (the port / router-interface split)

SR-OS separates the physical **port** (e.g. `1/1/c1/1`) from the L3
**router-interface** (e.g. `to-r2`) that binds it — the same physical/logical
split as Junos (`ge-0/0/0` vs unit `ge-0/0/0.0`). Batfish models it the same way
Junos is modeled, so a user-provided Layer-1 topology (which names the physical
port) drives the router-interface's L3 adjacency:

- A router interface with **no port binding** (`system`, loopbacks) is a single
  `InterfaceType.LOOPBACK` holding the address.
- A **port-bound** router interface becomes **two** VI interfaces: an addressless
  `InterfaceType.PHYSICAL` interface named by the **port path** (the Layer-1
  endpoint), and an `InterfaceType.LOGICAL` interface named by the
  router-interface, which holds the `ipv4 primary {address, prefix-length}` and
  carries a `DependencyType.BIND` dependency on the port.

The hardware tree (cards/MDAs/ports) is otherwise **not** mapped to the VI model
— Batfish derives the active port interface from the router-interface's port
binding, not by walking the full physical port tree — so the cards/MDAs/ports are
left unconverted with a single red-flag warning (not a silent drop). The port's
`admin-state` is honored on the synthesized `PHYSICAL` interface.

### BGP group → neighbor inheritance and peer type

A neighbor inherits each per-peer attribute it does not set directly —
`type`, `peer-as`, and the `import`/`export` policy lists — from its `group`
(per-neighbor config wins). This is resolved **on the model at extraction**
(`BgpNeighbor.inheritFrom`), mirroring how the NX-OS model resolves template
inheritance with a `doInherit` pass before conversion rather than inline in
conversion; conversion therefore reads a fully-populated neighbor.

Each neighbor becomes a `BgpActivePeerConfig` keyed by peer IP, with `local-as`
from the router instance's `autonomous-system` and router-id derived from the
`system` interface address when not explicitly set. A session is iBGP or eBGP by
the SR-OS `type` (`internal`/`external`) when configured — like Junos, the type
is explicit and need not be inferred — otherwise by comparing `peer-as` to the
local AS. `local-ip` is left unset so Batfish resolves the source address from
the topology: for a directly-connected eBGP peer it picks the connected
interface toward the peer (matching the device; forcing the system address would
put the local IP off the peering subnet and the session would never establish).

### eBGP default-reject (the policy chain)

SR-OS drops eBGP routes in **both** directions unless an explicit policy accepts
them (`ebgp-default-reject-policy` defaults true). "No policy" ≠ "permit all".
This is realized with the Junos-style generated-peer-policy idiom:

- Each `policy-statement` becomes a **chainable** `RoutingPolicy`. Its numbered
  entries are walked in order; an entry's `from prefix-list` set is a
  disjunction of `MatchPrefixSet`/`NamedPrefixSet` guards, and the matching
  action (`accept`/`reject`/`next-entry`/`next-policy`) is emitted so the policy
  behaves correctly both as a subroutine (`ReturnTrue`/`ReturnFalse` under
  `CALL_EXPR_CONTEXT`) and standalone (`ExitAccept`/`ExitReject`). `next-policy`
  maps to `Statements.FallThrough` so a `FirstMatchChain` advances to the next
  policy; `next-entry` and an absent `default-action` emit nothing (fall
  through).
- Each peer gets a **generated** import and export `RoutingPolicy` that
  `SetDefaultPolicy(...)` to a shared accept-all (iBGP) or reject-all (eBGP)
  backstop, then evaluates a `FirstMatchChain` of `CallExpr`s over the peer's
  named policies; if the chain reaches the default (no named policy made a
  terminal decision), the eBGP backstop rejects.

So an eBGP peer with no accepting policy rejects in both directions, while an
iBGP peer defaults to accept — matching SR-OS.

### Tests

[`SrosConversionTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosConversionTest.java)
parses r1 through the full pipeline and asserts: the VI `Configuration`
(format/device-model/default VRF), interface types and addresses, the
prefix-list → `RouteFilterList`, the neighbor's resolved `peer-as`/`local-as`
and unset `local-ip`, **behavioral** eBGP default-reject (the generated export
policy accepts the system prefix and rejects everything else; the import policy
with a default-action accept permits all), and the hardware-not-converted
red-flag warning. It also asserts the port / router-interface split (`to-r2` is
`LOGICAL` with a `BIND` dependency on the addressless `PHYSICAL` port
`1/1/c1/1`), the structure definitions/references and undefined/unused-structure
detection, and the over-approximation warning for an unmodeled prefix-list match
type (see [Prefix-list match-type bounds](#prefix-list-match-type-bounds-sros_prefix_bounds-batfishbatfish9990)).
`SrosExtractionTest` covers group→neighbor inheritance on the model
(`type`/`peer-as`/policies).

## Post-processing

Post-processing (`Batfish.postProcessSnapshot`) is largely vendor-independent —
interface dependency resolution, derived bandwidth/IGP costs, Layer-1 topology
application, protocol-state init. The SR-OS-specific concern is that its
constructs flow through it correctly. The single design decision that matters
here is the **port / router-interface split** described under
[Conversion → Interfaces](#interfaces-the-port--router-interface-split), and it
exists *because of* post-processing:

- **Layer-1 topology lines up with a real interface.** Collected SR-OS labs name
  the Layer-1 endpoint by the **physical port path** (`1/1/c1/1`), not the L3
  router-interface (`to-r2`) — the port path is what containerlab/the device
  expose. Modeling the port as a distinct `PHYSICAL` interface means the L1 edge
  canonicalizes to a real interface and survives into the *active logical* L1
  topology. If the L3 interface were the only thing modeled,
  the L1 endpoint `1/1/c1/1` would resolve to `INVALID_INTERFACE`, the edge would
  be silently dropped, and the cross-vendor adjacency would form only by the
  same-subnet fallback in `HybridL3Adjacencies` (which holds only because the
  cEOS side is never the `node1` of a resolved L1 edge — fragile and accidental).
- **The L3 edge is driven by L1.** `PointToPointComputer` walks the `BIND`
  dependency from `to-r2` to its port `1/1/c1/1`, so the user L1 edge between
  ports yields the L3 edge between the router interfaces. This is the same
  mechanism Junos relies on for its unit↔physical split.
- **Interface activity is correct.** The port is admin-up (its `admin-state` is
  honored), so interface-dependency resolution leaves both the port and the
  bound `to-r2` active; `system` (loopback) stays active. No SR-OS interface is
  spuriously deactivated.

The bandwidth-sum and IGP-cost post-processing steps are exercised by later
features: OSPF derives per-interface cost from `reference-bandwidth` (see
[OSPF](#ospf-sros_ospf-sros_ospf_multiarea)), and a LAG sums its member-port
bandwidth (see [LAG](#lag-sros_aggregate)).

[`SrosPostProcessingTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosPostProcessingTest.java)
loads the captured `sros_ceos_ebgp` lab (SR-OS r1 + cEOS r2 + the user L1
topology) through the full pipeline and asserts, on the post-processed model:
every r1 interface is active with the expected type and the `to-r2`→`1/1/c1/1`
BIND dependency; the user L1 edge (named by the port) survives into the active
logical L1; the cross-vendor L3 edge `r1:to-r2 ↔ r2:Ethernet1` forms; and the
eBGP session between r1 and r2 is established.

## RIB validation

RIB validation checks the data-plane output of the SR-OS pipeline — r1's
computed main RIB and BGP RIB — against the device's own operational state, captured as JSON
from the MD-CLI state tree (`info json /state router "Base" route-table` and
`… bgp rib`). This is the SR-OS-side counterpart to what the cEOS validator does
for r2: it checks the data-plane output of every pipeline stage from parse
through post-processing against device ground truth, complementing the
per-stage unit tests. The validation harness lives in the sibling `lab-validation` repo
(`SrosValidator`); this section records the SR-OS modeling facts that make the
comparison correct, since they constrain conversion.

**Main RIB.** SR-OS reports the active route per prefix in
`/state … route-table`, keyed by an owner protocol (`local`, `bgp`, `static`,
`isis`, `ospf`) that maps to the Batfish protocol (`local` → `connected`). The
route `preference` is the Batfish admin distance (a learned eBGP route is
preference/admin **170** on SR-OS, not the 20 that IOS/EOS use), and a
local/connected route reports preference 0 with an interface (not IP) next-hop.
For the captured lab this is exactly three IPv4 routes: the system loopback
`1.1.1.1/32` (connected), the peering `10.0.0.0/31` (connected), and the
learned `2.2.2.2/32` (bgp, admin 170, next-hop 10.0.0.1) — and Batfish's r1
main RIB matches all three.

**BGP RIB.** The state `bgp rib local-rib` carries each route's attributes by
reference: an `attr-id` indexing a top-level `attr-sets` table that holds
origin, next-hop, MED, and the nested AS-path. The validator joins them. The
critical modeling fact is that the local-rib
**over-lists** relative to the operational BGP table: it includes the device's
own `owner == "local"` routes (the connected/loopback prefixes) alongside the
`owner == "bgp"` learned ones. Those local entries carry `in-rtm: false` and are
main-RIB routes that BGP can *advertise via an export policy* — they are **not**
BGP-originated. The device's `show router bgp routes` shows exactly the one
learned route (`2.2.2.2/32`), and Batfish's BGP RIB holds exactly that one route
(as-path `65002`, origin `igp`, next-hop `10.0.0.1`). The validator therefore
compares only the learned subset (`owner == "bgp"`, best) — like-for-like
against Batfish's BGP RIB, not a workaround.

**Why r1 advertises from the main RIB.** SR-OS advertises from the **main RIB**
(Junos-like), not by origination into the BGP RIB, so conversion sets
`setExportBgpFromBgpRib(false)`. The local-rib over-listing above is what makes
this non-obvious — taken at face value it would suggest BGP-RIB-based export —
but the RIB comparison passes with main-RIB export and confirms it.

The comparison uses the same cost-based route matcher as every other vendor
validator: unmatched routes on either side score infinite cost and surface as
failures, so a green result means the device and Batfish RIBs are
route-for-route equal — not that one side was empty. No r1 RIB check is
sickbayed.

## Feature coverage

The model covers the features below, each validated against device ground truth
captured from a live SR-SIM lab. The parenthesized name in each heading links to
that lab's snapshot in the separate
[`batfish/lab-validation`](https://github.com/batfish/lab-validation) repo. The
sections record the SR-OS facts that shaped conversion.

### Static routes ([`sros_static`][sros_static], [`sros_ecmp`][sros_ecmp])

`router "<name>" static-routes route <prefix> route-type unicast` with a
`next-hop <ip>` or a `blackhole` converts to a VI `StaticRoute` (`NextHopIp` /
`NextHopDiscard`). The SR-OS `preference` is the admin distance and `metric` the
route metric, with YANG defaults **5** and **1**. The decisive fact, confirmed
on the live device: **SR-OS does not install a static route unless its
next-hop/blackhole context is `admin-state enable`** — a route with no
admin-state leaf is accepted into config but absent from the route-table, so
conversion skips a non-enabled route. A `loopback` router-interface (no port)
becomes a VI `LOOPBACK` and installs its connected prefix.

A `static-routes route <prefix>` with multiple `next-hop` entries is an **ECMP**
route. Extraction emits one VI `StaticRoute` per next-hop (each with its own
admin-state/metric/preference) instead of taking the first and warning;
Batfish's best-preference selection installs the equal-preference legs as ECMP
and drops worse ones. Device-confirmed: two equal-preference next-hops both
install (route-table `nexthop` list has two entries); two unequal-preference
next-hops install only the lower-preference leg.

### Aggregate routes ([`sros_aggregate`][sros_aggregate])

`router "Base" aggregates aggregate <prefix>` converts to a discard
`GeneratedRoute` at admin distance 130 (the device's aggregate preference).
Batfish's generated-route activation matches SR-OS — the aggregate installs only
when a contributing more-specific route exists — so no explicit contributor
check is needed. Device-confirmed: with `summary-only`, the peer learns the /16
aggregate but not the /24 contributors.

### OSPF ([`sros_ospf`][sros_ospf], [`sros_ospf_multiarea`][sros_ospf_multiarea])

`router "<name>" ospf <instance>` converts to a VI `OspfProcess` (per VRF) with
its areas and, on each OSPF interface, `OspfInterfaceSettings` (area, cost,
network type, addresses). Batfish derives the adjacencies and neighbor configs
from those interface settings in post-processing, so conversion only sets the
interface settings and area membership. SR-OS facts: OSPF **internal** route
preference (admin distance) is **10** (external 150) — not the Cisco 110; the
default `reference-bandwidth` is 100 Gbps (so a 100 Gbps port derives cost 1); an
explicit interface `metric` wins, a loopback is cost 0. Two SR-OS routers form a
point-to-point adjacency and each learns the other's system /32.

**Multi-area** uses the same path — the area loop converts N areas to N VI
`OspfArea`s, making a router with interfaces in two areas an ABR, and Batfish's
dataplane computes the inter-area (IA) routes. Device-confirmed:
each side learns the other area's prefixes as OSPF IA, preference 10, via the
ABR.

**OSPF external redistribution does not work via an `export-policy` on SR-SIM
26.3.R1**: an `ospf <n> export-policy [p]` accepting static/connected routes
committed cleanly but never made the router an ASBR or originated any AS-external
LSA (`show router ospf status` stayed `AS Border Router: False`, 0 external
LSAs), for static (by prefix-list and by `from protocol name [static]`) and for
connected (`from protocol name [direct]`). BGP export of the same statics works
(see [Redistribution](#redistribution-and-route-origin-sros_redist)), so this is
OSPF-export-specific. Because the emulator produced no external LSAs to validate
against, OSPF-external (E1/E2) redistribution is left unmodeled and `ospf
export-policy` is not converted — this is a missing feature, not a Batfish bug to
fix, and would need the device (or a newer SR-OS release) to actually redistribute
before it could be modeled. (Generic OSPF external admin-distance defaults
`OSPF_E1`/`OSPF_E2` exist in the `RoutingProtocol` defaults, but no SR-OS path
currently produces such routes.)

### IS-IS ([`sros_isis`][sros_isis])

`router "<name>" isis <instance>` is parsed into a typed `IsisProcess`
(system-id, area-address, level-capability, per-interface interface-type/passive)
and converted to a VI `IsisProcess`: the NET is the first area-address +
system-id + an `00` N-selector, a passive interface advertises its subnet but
forms no adjacency (VI `PASSIVE` mode), and per-level interface settings carry
the point-to-point flag. SR-OS IS-IS route preferences (admin distances) are in
the `NOKIA_SROS` `RoutingProtocol` defaults — L1 15 / L2 18 internal, L1 160 / L2
165 external — confirmed on device (an L2 internal route installs at preference
18, default interface metric 10). Validated with a two-SR-SIM L2 adjacency: each
router learns the other's system loopback via IS-IS.

### LAG ([`sros_aggregate`][sros_aggregate])

`lag "<name>"` converts to an `AGGREGATED` interface whose member ports are
`AGGREGATE` dependencies (post-processing sums their bandwidth) and channel-group
members; each member's `channelGroup` is set to the LAG. That channel-group
linkage is essential: it lets the logical-Layer-1 computation collapse the
members' per-port physical edges into a single `lag`↔`lag` edge, so the LAG
comes up via a BIND dependency on its single logical neighbor. Without it, the
members look like two separate L1 neighbors and the dependency logic deactivates
the LAG as an LACP failure. The validator skips the synthetic `AGGREGATED` (and
`PHYSICAL`) interfaces, which are not in the device's router-interface state.
Note: the device interface state carries no bandwidth, so the lab validates the
LAG interface's presence and the routes over it, not the bandwidth-sum value
(that is unit-tested). A LACP `lag … { lacp { mode active } }` requires a
mandatory `administrative-key`, or the whole startup config is rejected at boot
("MGMT_CORE #236 … Missing mandatory fields").

### iBGP ([`sros_ibgp`][sros_ibgp])

A second SR-OS router peers iBGP (`group … type internal`, peer-as = local AS).
**iBGP default-accept propagates BGP routes; it does not pull connected/static/IGP
routes into BGP.** The default-accept policy is guarded on the route's protocol
being BGP/iBGP — a blanket accept-all, combined with `setExportBgpFromBgpRib(false)`
(export runs over the whole main RIB), would leak a router's connected /31s into
iBGP. On import the route is always a received BGP route, so the protocol guard
preserves import default-accept. Validator note: SR-OS reports both eBGP and iBGP
learned routes as protocol `bgp` in its route-table, while Batfish labels an
iBGP-learned main-RIB route `ibgp`; the validator maps SR-OS `bgp` to either.
iBGP-learned routes are admin **170**, same as eBGP on SR-OS.

### BGP route reflection ([`sros_rr`][sros_rr])

A BGP group/neighbor `cluster { cluster-id <ip> }` makes the peer a
route-reflector client: conversion sets the VI peer's `clusterId` and the address
family's `routeReflectorClient`, so Batfish reflects routes between clients.
`next-hop-self true` prepends a `SetNextHop(self)` to the peer export policy.
Both inherit group→neighbor. Lab finding: with no IGP underlay, an RR's clients
**receive** a reflected route but mark it invalid (unresolvable originator
next-hop) and don't install it — `next-hop-self` on the RR is what lets the
clients resolve it. With it, each client installs the other's loopback.

### BGP communities and complex attributes ([`sros_bgp_comm_in`][sros_bgp_comm_in])

The cEOS oracle (r2) advertises three loopbacks to r1 tagged with standard
communities, MED, and AS-path prepend; r1 imports all, so its BGP RIB carries
non-empty attributes validated on both sides. Finding: **SR-OS renders
well-known communities symbolically** (`no-export`), so the validator
canonicalizes them to numeric (`no-export` → `65535:65281`, etc.) to match
Batfish, raising on an unknown symbolic name rather than masking. Also: for a
BGP route the device's main-RIB metric is the IGP cost to the next-hop (0 for a
directly-connected peer) while Batfish carries the MED into the main-RIB metric,
so the validator compares the BGP MED on the BGP RIB and skips the main-RIB
metric for BGP-protocol routes.

### Routing-policy match and action clauses ([`sros_bgp_policy`][sros_bgp_policy], [`sros_policy_depth`][sros_policy_depth])

A `policy-statement` entry's match (`from`) and action clauses convert as
follows. Match clauses:

- `from prefix-list` → a disjunction of `MatchPrefixSet`/`NamedPrefixSet` guards
  (see [Conversion → eBGP default-reject](#ebgp-default-reject-the-policy-chain)).
- `from { protocol { name [static direct bgp ospf isis] } }` → a `MatchProtocol`
  guard (see [Redistribution](#redistribution-and-route-origin-sros_redist)).
- `from community { name <name> }` → match any member of the named community
  list (`MatchCommunities` over `HasCommunity(CommunityIs(...))`).
- `from as-path { name <name> }` → match the named as-path list's `expression`
  regex (`MatchAsPath` / `AsPathMatchRegex`; an `AsPathList` model holds it).

Action set-clauses: `metric set <n>` → `SetMetric` (BGP MED); `metric add` →
`IncrementMetric`; `as-path-prepend as-path <asn> [repeat <n>]` → `PrependAsPath`
(the AS repeated `n` times); `community add ["<name>"]` → `SetCommunities`
unioning the named `policy-options community` list's members onto the route; and
`local-preference` and `origin`. Flow control: `next-entry` falls through to the
next entry, `next-policy` advances to the next policy, and a `default-action`
sets the policy's fall-through decision. Device-confirmed behaviorally: a
community-tagged route is accepted with local-pref 250 and a community add; an
AS-path-matched route gets its metric incremented and origin set; r2 learns r1's
routes with the MED and prepended AS-path r1's policy sets.

### Prefix-list match-type bounds ([`sros_prefix_bounds`][sros_prefix_bounds], batfish/batfish#9990)

A prefix-list entry's match-type window is captured exactly where the type
supports it. `through-length` / `start-length` / `end-length` nest in the block
hanging off the type value word (`type <through|range> { … }`) in the canonical
tree, so `through` converts to the exact window `[len, through-length]` and
`range` to `[start-length, end-length]`.

`type to` is a **length-range match**: a route matches iff it is contained in the
base network and its length is in `[base-length .. to-prefix-length]` — the
to-prefix supplies the upper length bound (the sibling of `through`), and must be
nested in the base (commit rejects a non-nested to-prefix: `PLCY #1001 ... does
not share same most significant bits`). It is modeled as one exact
`RouteFilterLine` per ancestor length of each to-prefix, so off-path siblings do
not match (a single base-prefix line with a length range would wrongly match
them). `address-mask` with a contiguous mask equal to the base length is an exact
match; a non-contiguous mask cannot be a length `SubRange` and is
over-approximated with a warning.

### Redistribution and route origin ([`sros_redist`][sros_redist])

A policy entry `from { protocol { name [static direct bgp ospf isis] } }`
converts to a `MatchProtocol` guard (ANDed with any `from prefix-list`). An entry
whose only criterion is a from-protocol clause therefore matches only that
protocol's routes — without the guard it would match TRUE and leak **every**
route. Origin/MED on locally-sourced routes: a **connected/direct** route
advertised by an export policy carries origin **IGP**, a **redistributed** route
(static/OSPF/…) carries origin **INCOMPLETE**; and a non-BGP route is advertised
with **MED 0** unless a policy explicitly sets the metric (an entry's `metric
set` still wins). The cEOS
oracle confirms the redistributed static prefix appears with origin incomplete
and MED 0.

### Multi-VRF / VPRN ([`sros_vprn`][sros_vprn])

`service vprn "<name>"` is a routing instance in its own VRF, modeled as a
`Router` keyed by the service-name (reusing the interface/static/OSPF/BGP feature
set), which conversion maps to a same-named VRF — exactly like a non-Base
`router "<name>"`. So a VPRN's routes/interfaces land in their own VRF, separate
from Base, with no leak. The validator validates VPRN VRFs:
`info json /state service vprn "<name>" route-table`/`interface *` is captured
and compared (routes tagged with the service-name as the VRF) against Batfish's
corresponding VRF, so VRF separation is checked substantively, not just on Base.

### Multi-VRF L3VPN ([`sros_vprn_bgp`][sros_vprn_bgp], partial — batfish/batfish#9991)

A real MPLS L3VPN between two SR-SIM PEs (OSPF+LDP underlay, MP-BGP `vpn-ipv4`
between loopbacks, VPRN `red` with RD + shared `vrf-target`). SR-OS extraction
captures `bgp-ipvpn mpls` route-distinguisher + vrf-target import/export
route-targets (`BgpIpvpn`), and conversion sets the route-distinguisher on the
VI VRF. The PE-to-PE vpn-ipv4 route import is **not** modeled — the VI datamodel
has no VPNv4 address family and its cross-VRF leaking is intra-node only — so the
VPRN's RT-imported routes (e.g. pe1 `red` learning `172.16.2.1/32`) are absent
from the computed main RIB; those two main-RIB checks are sickbayed to
batfish/batfish#9991. Everything else (config/interfaces/BGP RIB/RD-on-VRF)
validates green. Device-confirmed: BGP-VPN learned route preference 170,
resolved over an LDP tunnel; VPRN `red` stays separate from Base.

### Diagnostics: deliberate misconfiguration ([`sros_misconfig`][sros_misconfig])

An AS-mismatch lab (r1's ebgp group sets peer-as 65099 while r2 is AS 65002)
exercises the **diagnostics** rather than any new conversion: Batfish predicts
the eBGP session `NOT_COMPATIBLE` on both sides and learns no BGP routes,
matching the device (r1's BGP RIB has 0 learned routes; r2 never learns r1's
system prefix). Finding: **SR-OS enforces the BGP import/export policy
leafref at commit** and rejects a reference to a non-existent policy-statement
(`MGMT_CORE #224`), aborting the whole startup config — so a committed device
config cannot carry an undefined policy reference. Batfish's `undefinedReferences`
for SR-OS policies is therefore unit-testable but not lab-observable on a device.

Tooling caveat: the `lab_builder` SR-OS `interface_up` precondition check is a
false-negative on this lab — its heuristic ("`up` present and `down` absent" in
the `show router interface` line) trips on the `Up/Down` column header, so it
reports the (genuinely up) link interface as down. It is a collection-time
precondition, not a lab test, so it does not affect the green result; the lab is
collected regardless and validates 13/13.

### Grammar: empty same-line block `{ }`

The device renders a list entry with no body inline as `<words> { }` (open
brace, optional whitespace, close brace, no newline) — e.g. the `to-prefix`es
of a `prefix-list ... type to`, `mask-pattern`s, or a community `member`. The
`block` rule originally required `OPEN_BRACE NEWLINE+ ...` and rejected `{ }`
with a FATAL "extraneous input '}' expecting NEWLINE", corrupting any empty-body
list entry; the rule also accepts `OPEN_BRACE CLOSE_BRACE`. Note: test fixtures
must mirror device-rendered output (inline empty blocks), not idealized
hand-written multi-line form — an idealized `{ \n }` fixture masks this case,
which only a device-collected config exposes.

<!-- Validation-lab snapshots in the batfish/lab-validation repo. -->
[sros_static]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_static
[sros_ecmp]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_ecmp
[sros_aggregate]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_aggregate
[sros_ospf]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_ospf
[sros_ospf_multiarea]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_ospf_multiarea
[sros_isis]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_isis
[sros_ibgp]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_ibgp
[sros_rr]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_rr
[sros_bgp_comm_in]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_bgp_comm_in
[sros_bgp_policy]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_bgp_policy
[sros_policy_depth]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_policy_depth
[sros_prefix_bounds]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_prefix_bounds
[sros_redist]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_redist
[sros_vprn]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_vprn
[sros_vprn_bgp]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_vprn_bgp
[sros_misconfig]: https://github.com/batfish/lab-validation/tree/main/snapshots/sros_misconfig
