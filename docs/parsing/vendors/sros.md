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

## Conversion (P5)

`SrosConfiguration.toVendorIndependentConfigurations()` converts the typed P4
model into the vendor-independent `Configuration`. The conversion logic lives in
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
type. `SrosExtractionTest` covers group→neighbor inheritance on the model
(`type`/`peer-as`/policies).

## Post-processing (P6)

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
  topology. If the L3 interface were the only thing modeled (the pre-P6 design),
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

There are no SR-OS aggregate/redundant interfaces or OSPF/EIGRP in the current
lab, so the bandwidth-sum and IGP-cost post-processing steps are not yet
exercised; they will be when a later lab (P8+) introduces them.

[`SrosPostProcessingTest`](../../../projects/batfish/src/test/java/org/batfish/vendor/sros/grammar/SrosPostProcessingTest.java)
loads the captured `sros_ceos_ebgp` lab (SR-OS r1 + cEOS r2 + the user L1
topology) through the full pipeline and asserts, on the post-processed model:
every r1 interface is active with the expected type and the `to-r2`→`1/1/c1/1`
BIND dependency; the user L1 edge (named by the port) survives into the active
logical L1; the cross-vendor L3 edge `r1:to-r2 ↔ r2:Ethernet1` forms; and the
eBGP session between r1 and r2 is established.

## RIB validation (P7)

P7 validates the data-plane output of the SR-OS pipeline — r1's computed main
RIB and BGP RIB — against the device's own operational state, captured as JSON
from the MD-CLI state tree (`info json /state router "Base" route-table` and
`… bgp rib`). This is the SR-OS-side counterpart to what the cEOS validator does
for r2, and it closes the loop: every pipeline stage from parse through
post-processing is now checked against ground truth, not just asserted in unit
tests. The validation harness lives in the sibling `lab-validation` repo
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
critical modeling fact (grounded at P5-V, see below) is that the local-rib
**over-lists** relative to the operational BGP table: it includes the device's
own `owner == "local"` routes (the connected/loopback prefixes) alongside the
`owner == "bgp"` learned ones. Those local entries carry `in-rtm: false` and are
main-RIB routes that BGP can *advertise via an export policy* — they are **not**
BGP-originated. The device's `show router bgp routes` shows exactly the one
learned route (`2.2.2.2/32`), and Batfish's BGP RIB holds exactly that one route
(as-path `65002`, origin `igp`, next-hop `10.0.0.1`). The validator therefore
compares only the learned subset (`owner == "bgp"`, best) — like-for-like
against Batfish's BGP RIB, not a workaround.

**Why r1 advertises from the main RIB.** Because the local-rib over-lists,
naively treating it as BGP-origination would lead to switching Batfish to
BGP-RIB-based export. The evidence says otherwise: SR-OS advertises from the
**main RIB** (Junos-like), so conversion keeps `setExportBgpFromBgpRib(false)`.
This was the key conversion decision validated at P5-V and re-confirmed by the
P7 RIB comparison passing without it.

The comparison uses the same cost-based route matcher as every other vendor
validator: unmatched routes on either side score infinite cost and surface as
failures, so a green result means the device and Batfish RIBs are
route-for-route equal — not that one side was empty. No r1 RIB check is
sickbayed.

## Complexity ladder (P8)

After the first eBGP lab validated end to end, the model was grown one feature
"rung" at a time, each with its own live lab (in `lab-validation/snapshots/`)
validated against ground truth. All eight rungs below are done — interfaces +
static (L1), OSPF (L2), iBGP (L3), route reflection (L4), BGP policy depth (L5),
redistribution (L6), multi-VRF/VPRN (L7), and deliberate misconfiguration (L8).
The SR-OS facts each one pinned down:

### L1 — static routes (`sros_static`)

`router "<name>" static-routes route <prefix> route-type unicast` with a
`next-hop <ip>` or a `blackhole` converts to a VI `StaticRoute` (`NextHopIp` /
`NextHopDiscard`). The SR-OS `preference` is the admin distance and `metric` the
route metric, with YANG defaults **5** and **1**. The decisive fact, confirmed
on the live device: **SR-OS does not install a static route unless its
next-hop/blackhole context is `admin-state enable`** — a route with no
admin-state leaf is accepted into config but absent from the route-table, so
conversion skips a non-enabled route. A `loopback` router-interface (no port)
becomes a VI `LOOPBACK` and installs its connected prefix.

### L3 — iBGP (`sros_ibgp`)

A second SR-OS router peers iBGP (`group … type internal`, peer-as = local AS).
This exercised the P5 iBGP default-accept path for the first time and forced a
correction: **iBGP default-accept propagates BGP routes, it does not pull
connected/static/IGP routes into BGP**. The generated default-accept policy was
a blanket accept-all, which — combined with `setExportBgpFromBgpRib(false)`
(export runs over the whole main RIB) — leaked a router's connected /31s into
iBGP. The default-accept policy is now guarded on the route's protocol being
BGP/iBGP (on import the route is always a received BGP route, so import
default-accept is preserved). Validator note: SR-OS reports both eBGP and iBGP
learned routes as protocol `bgp` in its route-table, while Batfish labels an
iBGP-learned main-RIB route `ibgp`; the validator maps SR-OS `bgp` to either.
iBGP-learned routes are admin **170**, same as eBGP on SR-OS.

### L5 — BGP policy depth (`sros_bgp_policy`)

Policy-statement entry `action` set-clauses now convert: `metric set <n>` →
`SetMetric` (BGP MED), `as-path-prepend as-path <asn> [repeat <n>]` →
`PrependAsPath` (the AS repeated `n` times), and `community add ["<name>"]` →
`SetCommunities` unioning the named `policy-options community` list's members
onto the route. Prefix-list match-type **bounds** are now captured:
`through-length` / `start-length` / `end-length` nest in the block hanging off
the type value word (`type <through|range> { … }`) in the canonical tree, so
`through` converts to the exact window `[len, through-length]` and `range` to
`[start-length, end-length]` — removing the P5 "this length or longer"
over-approximation (and its warning) for those types (`to`/`address-mask` still
over-approximate). Validated via the cEOS oracle: r2 learns r1's routes with the
MED and prepended AS-path that r1's policy sets.

### L2 — OSPF single area (`sros_ospf`)

`router "<name>" ospf <instance>` converts to a VI `OspfProcess` (per VRF) with
its areas and, on each OSPF interface, `OspfInterfaceSettings` (area, cost,
network type, addresses). Batfish derives the adjacencies and neighbor configs
from those interface settings in post-processing, so conversion only sets the
interface settings and area membership. SR-OS facts: OSPF **internal** route
preference (admin distance) is **10** (external 150) — not the Cisco 110; the
default `reference-bandwidth` is 100 Gbps (so a 100 Gbps port derives cost 1); an
explicit interface `metric` wins, a loopback is cost 0. The lab's two SR-OS
routers form a point-to-point adjacency and each learns the other's system /32.

### L4 — BGP route reflection (`sros_rr`)

A BGP group/neighbor `cluster { cluster-id <ip> }` makes the peer a
route-reflector client: conversion sets the VI peer's `clusterId` and the address
family's `routeReflectorClient`, so Batfish reflects routes between clients.
`next-hop-self true` prepends a `SetNextHop(self)` to the peer export policy.
Both inherit group→neighbor. Lab finding: with no IGP underlay, an RR's clients
**receive** a reflected route but mark it invalid (unresolvable originator
next-hop) and don't install it — `next-hop-self` on the RR is what lets the
clients resolve it. With it, each client installs the other's loopback.

### L6 — redistribution (`sros_redist`)

A policy entry `from { protocol { name [static direct bgp ospf isis] } }`
converts to a `MatchProtocol` guard (ANDed with any `from prefix-list`). This
also fixed a correctness bug: an entry whose only criterion was a (previously
unmodeled) from-protocol guarded TRUE and leaked **every** route. Origin/MED on
locally-sourced routes was refined here: a **connected/direct** route advertised
by an export policy carries origin **IGP**, a **redistributed** route
(static/OSPF/…) carries origin **INCOMPLETE**; and a non-BGP route is advertised
with **MED 0** unless a policy explicitly sets the metric (an entry's `metric
set` still wins). The cEOS oracle confirms the redistributed static prefix
appears with origin incomplete and MED 0.

### L7 — multi-VRF / VPRN (`sros_vprn`)

`service vprn "<name>"` is a routing instance in its own VRF, modeled as a
`Router` keyed by the service-name (reusing the interface/static/OSPF/BGP feature
set), which conversion maps to a same-named VRF — exactly like a non-Base
`router "<name>"`. So a VPRN's routes/interfaces land in their own VRF, separate
from Base, with no leak. The validator was extended to validate VPRN VRFs:
`info json /state service vprn "<name>" route-table`/`interface *` is captured
and compared (routes tagged with the service-name as the VRF) against Batfish's
corresponding VRF, so VRF separation is checked substantively, not just on Base.

### L8 — deliberate misconfiguration (`sros_misconfig`)

An AS-mismatch lab (r1's ebgp group sets peer-as 65099 while r2 is AS 65002) —
no Batfish modeling change, this validates the **diagnostics**: Batfish predicts
the eBGP session `NOT_COMPATIBLE` on both sides and learns no BGP routes,
matching the device (r1's BGP RIB has 0 learned routes; r2 never learns r1's
system prefix). Finding: **SR-OS enforces the BGP import/export policy leafref
at commit** and rejects a reference to a non-existent policy-statement
(`MGMT_CORE #224`), aborting the whole startup config — so a committed device
config cannot carry an undefined policy reference. Batfish's `undefinedReferences`
for SR-OS policies is therefore unit-testable but not lab-observable on a device.
