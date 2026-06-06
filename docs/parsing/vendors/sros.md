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

## Conversion (P5)

`SrosConfiguration.toVendorIndependentConfigurations()` converts the typed P4
model into the vendor-independent `Configuration`. The conversion logic lives in
[`SrosConversions`](../../../projects/batfish/src/main/java/org/batfish/vendor/sros/representation/SrosConversions.java).
Three SR-OS-specific semantics drive the design:

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

### BGP group → neighbor inheritance

P4 records only the neighbor's `group` leafref. Conversion resolves the
inheritance: a neighbor's `peer-as` and `import`/`export` policy lists come from
its own config if set, otherwise from its group (per-neighbor wins). Each
neighbor becomes a `BgpActivePeerConfig` keyed by peer IP, with `local-as` from
the router instance's `autonomous-system` and `local-ip`/router-id derived from
the `system` interface address when not explicitly set.

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
prefix-list → `RouteFilterList`, BGP group→neighbor inheritance (inherited
`peer-as`, `local-as`, `local-ip`), **behavioral** eBGP default-reject (the
generated export policy accepts the system prefix and rejects everything else;
the import policy with a default-action accept permits all), and the
hardware-not-converted red-flag warning. It also asserts the port /
router-interface split: `to-r2` is `LOGICAL` with a `BIND` dependency on the
addressless `PHYSICAL` port `1/1/c1/1`.

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
