# Symbolic Routing Policy Engine

This document describes Batfish's symbolic routing-policy analysis: the
`TransferBDD` interpreter and its supporting infrastructure in the
`projects/minesweeper` project. It is a developer-oriented overview aimed at
engineers extending the engine or debugging its results.

The engine powers the `searchRoutePolicies`, `compareRoutePolicies`, and
`transferBDDValidation` questions. For user-facing documentation of those
questions, see [searchRoutePolicies.md](../question_development/searchRoutePolicies.md)
and [testRoutePolicies.md](../question_development/testRoutePolicies.md). This
document complements the [reachability-focused symbolic engine
README](README.md), which covers BDD-based packet/data-plane analysis.

## Table of Contents

- [Overview](#overview)
- [Symbolic Route Representation](#symbolic-route-representation)
- [Atomic Predicates](#atomic-predicates)
- [Path Enumeration in TransferBDD](#path-enumeration-in-transferbdd)
- [Communities](#communities)
- [AS-Path](#as-path)
- [Local Preference (and other scalar attributes)](#local-preference-and-other-scalar-attributes)
- [Non-BGP Routes](#non-bgp-routes)
- [Tracking "Unsupported" Paths](#tracking-unsupported-paths)
- [Known Gaps](#known-gaps)
- [Related Documentation](#related-documentation)

---

## Overview

The symbolic routing-policy engine answers questions of the form: "for *all*
input route announcements, what does this routing policy do?" rather than
testing one concrete route at a time.

The engine works in three steps:

1. **Compute atomic predicates** for the policy's regex-based attributes
   (communities and AS-paths) so that all regexes in the policy can be
   represented as disjoint, finite-domain predicates. See
   `ConfigAtomicPredicates`.
2. **Symbolically interpret** the routing policy using `TransferBDD`. The
   interpreter walks the vendor-independent `RoutingPolicy` AST and produces a
   list of `TransferReturn` results — one per execution path — each consisting
   of a BDD over input-route attributes (the path condition), a `BDDRoute`
   describing the path's output, and a permit/deny verdict.
3. **Combine with user constraints and extract a witness**. Question answerers
   intersect path conditions with user-supplied input/output constraints and
   ask `ModelGeneration` to materialize a satisfying assignment as a concrete
   route.

The interpreter operates entirely on the vendor-independent
`RoutingPolicy`/`Statement`/`BooleanExpr` model — it does not know about
vendors. Anything a vendor parser fails to lower into one of those constructs
is invisible to the engine.

## Symbolic Route Representation

`BDDRoute` represents a route announcement as a fixed-width tuple of symbolic
attributes:

| Attribute | Encoding |
|----------|----------|
| Prefix / prefix length | `MutableBDDInteger` (32 + 6 bits) |
| Local preference, MED, tag, weight, cluster-list length | `MutableBDDInteger` |
| Administrative distance | `MutableBDDInteger` (sized to fit max admin distance) |
| Next-hop IP | `MutableBDDInteger` (32 bits) plus a `NextHopType` enum and a "was set" flag |
| Origin type, OSPF metric type, routing protocol | `BDDDomain<Enum>` (logarithmic-bit one-hot encoding with a built-in mutual-exclusion constraint) |
| Communities | `BDD[]` — one variable per community atomic predicate |
| AS-path regex match | `BDDDomain<Integer>` — one mutually-exclusive value per AS-path atomic predicate |
| AS-path prepends | concrete `List<Long>` (not symbolic — see [AS-Path](#as-path)) |
| Next-hop interface, source VRF, peer address, tunnel-encap | `BDDDomain<Integer>` over the finite set of values referenced by the policy |
| Tracks | `BDD[]` — one variable per track |
| Unsupported flag | `boolean` |

The full set of variables is allocated when the `BDDRoute` is constructed; the
factory's variable count is sized accordingly.

The encoding follows a standard symbolic-execution pattern: each bit `i` of the
output route is represented by a BDD `f_i` over the *input* route's bits. After
interpretation, `f_i` describes the conditions under which output bit `i` is 1.

Not every bit pattern over these variables corresponds to a valid route: prefix
length must be ≤ 32, BDDDomain values must lie in their declared range, and
(for BGP-only analyses) the protocol must be a BGP protocol.
`BDDRoute.wellFormednessConstraints(boolean onlyBgp)` produces this constraint.
The path conditions returned by `TransferBDD` do *not* include
well-formedness — it is the client's responsibility to conjoin the constraint
before extracting a concrete witness via `ModelGeneration`.

## Atomic Predicates

Communities and AS-paths are matched via regular expressions. Naively encoding
arbitrary regex sets as BDDs would not scale. The engine instead computes a
minimal partition of the underlying space such that every regex in the policy
either accepts all values in a partition cell or none. Each cell is an *atomic
predicate*; only one BDD variable (or one `BDDDomain` value) per atomic
predicate is needed in the route encoding.

`RegexAtomicPredicates` implements the partition-refinement algorithm. It is
parameterized over `SymbolicRegex` for communities (`CommunityVar`) and
AS-paths (`SymbolicAsPathRegex`).

`ConfigAtomicPredicates` is built by the client over a chosen set of
`(configuration, policies)` pairs. The granularity is up to the caller and
is a tradeoff: amortizing AP computation across many policies avoids
recomputing the partition (which can be slow, especially for AS-path
regexes), but a smaller set of policies has fewer regexes and therefore
fewer APs. `SearchRoutePoliciesAnswerer` builds one
`ConfigAtomicPredicates` per node, scoped to the policies on that node.
A `ConfigAtomicPredicates` contains:

- `getStandardCommunityAtomicPredicates()` — atomic predicates for
  regex/standard-community matches.
- `getNonStandardCommunityLiterals()` — extended/large community literal
  values, each given its own AP slot (these are not regex-matched).
- `getAsPathRegexAtomicPredicates()` — AS-path APs.
- Finite-domain enumerations for `nextHopInterfaces`, `peerAddresses`,
  `sourceVrfs`, `tracks`, `tunnelEncapsulationAttributes` — so each can be
  modeled with a small BDDDomain.

## Path Enumeration in TransferBDD

`TransferBDD` is a visitor over the routing-policy AST. Conceptually, given a
policy and an initial `BDDRoute`, it produces a `List<TransferReturn>`:

```text
TransferReturn = (BDDRoute outputRoute, BDD inputCondition, boolean accepted)
```

Each `TransferReturn` describes one symbolic execution path. The interpreter
threads a list of `TransferBDDState` objects through statements: each state
bundles a `TransferParam` (call context, scope stack, default-action flags)
with a `TransferResult` (the in-flight return value plus control-flow flags
for `return`, `exit`, fall-through, and route suppression).

Branching constructs (e.g., `If`, `Conjunction`, `Disjunction`,
`FirstMatchChain`) split a state into multiple states with disjoint conditions.
Statements whose effects depend on the matched condition (e.g., a `set`
inside an `if`) are applied only on the matching branch. The relevant entry
points are `TransferBDD.computePaths` and the overloaded `compute(...)` methods
for `Statement`, `BooleanExpr`, and `Statement` lists.

`CallStatement` and `CallExpr` (subroutine policies) are inlined recursively.
The caller's `returnAssignedValue` flag is saved and restored across the call,
so a `return` inside the callee terminates the callee but lets the caller
continue. A `CallContext` enum distinguishes statement-context vs.
expression-context calls so that `CallStatementContext` / `CallExprContext`
predicates evaluate correctly.

After all statements are visited, paths whose output `BDDRoute`s agree can
optionally be merged (their input conditions are OR'd) so that the result list
does not blow up exponentially. See `TransferBDD.combineSymbolicResults`.

There is one important caveat: because the prepend list in `BDDRoute` is
concrete (see [AS-Path](#as-path)), only paths with identical prepend
sequences can be merged.

## Communities

### Representation

A community appears symbolically as a `CommunityVar`, which is one of:

- **`REGEX`** — a user-provided regex (e.g., `^65000:.*$`). Standard-community
  regexes are normalized by intersecting with `COMMUNITY_FSM`, which encodes
  the standard-community language `ASN:value`.
- **`EXACT`** — a literal community value: a `StandardCommunity`,
  `ExtendedCommunity`, or `LargeCommunity`.

The engine handles standard communities (regexes plus standard literals) and
non-standard literals (extended/large) separately:

- **Standard communities** all share the same atomic-predicate space. Any
  regex or literal becomes a `CommunityVar`, the partition refinement combines
  them, and matching reduces to OR-ing the BDD variables of the relevant APs.
- **Non-standard communities** are not regex-matched. Each distinct extended
  or large literal that appears in the policy gets its own AP slot tacked on
  after the standard APs.

### Match expressions

`CommunityMatchExprToBDD` handles single-community match expressions
(`CommunityIs`, `CommunityMatchRegex`, `StandardCommunityHighMatch`,
`StandardCommunityLowMatch`, `ExtendedCommunity`/`LargeCommunity` filters,
etc.). For each, it collects the relevant `CommunityVar`s, looks up their
atomic-predicate indexes, and OR-reduces the corresponding `BDDRoute`
community-AP BDDs.

`CommunitySetMatchExprToBDD` handles set-level constraints. The most common
case, `HasCommunity(matcher)`, is converted to "there exists at least one
community in the route's set that matches `matcher`", which evaluates to the
OR of community-AP BDDs whose APs overlap the matcher's accepted values.
`HasSize` is handled heuristically: it assumes a route carries no more than
~64 communities (a semi-arbitrary threshold) and uses that to give
conservative `LE`/`LT`/`GE`/`GT` answers; `EQ` is unsupported.

### Set operations

`SetCommunitiesVisitor` and `CommunityAPDispositions` together model the
effect of `SetCommunities` statements. A disposition records, for each AP,
whether it must be present, must be absent, or is preserved from the input:

- **Additive** (`additive`/union): merges the new APs into the input set —
  only `mustExist` is touched.
- **Replacement** (`exact set`): explicitly marks the matched APs as
  `mustExist` and all others as `mustNotExist`.
- **Delete** (set difference): marks matched APs as `mustNotExist` while
  preserving the others.

`TransferBDD.updateCommunities` applies a disposition to the route by forcing
each `mustExist` AP's BDD to `one` and each `mustNotExist` AP's BDD to `zero`,
leaving the rest unchanged.

## AS-Path

AS-path is handled differently from communities for two reasons:

1. **Mutually exclusive matching.** Unlike a route's *set* of communities, the
   AS-path is a single string. Exactly one AS-path AP matches any concrete
   path, so the engine uses `BDDDomain<Integer>` rather than `BDD[]`. This
   takes only ⌈log₂(numAPs)⌉ BDD variables and gets mutual exclusion for free.
2. **Prepending is structural.** A `set as-path prepend` doesn't just narrow
   the set of matching atomic predicates — it transforms the underlying
   string. Reasoning about this symbolically across many paths is hard, so
   the engine uses a hybrid encoding.

### Regex-to-automaton conversion

`SymbolicAsPathRegex` and `SymbolicRegex` convert vendor regex syntax to
[dk.brics.automaton](http://www.brics.dk/automaton/) automata, normalize
syntax (e.g., Juniper's anchor conventions), and intersect with `AS_PATH_FSM`
to enforce the AS-path grammar (space-separated 32-bit ASNs with start/end
anchors).

`AsPathMatchExprToRegexes` converts vendor-independent match expressions
(`AsPathMatchRegex`, `AsSetsMatchingRanges`, `HasAsPathLength`, etc.) into a
set of `SymbolicAsPathRegex` objects.

### The prepend list

`BDDRoute._prependedASes` is a *concrete* `List<Long>` rather than a symbolic
encoding. The reasoning: along a single execution path through a policy, the
sequence of prepended ASNs is fixed (the prepend statements are determined by
the path's branches). One concrete list per path therefore suffices.

`PrependAsPath` statements simply append literal ASNs to this list. The
engine refuses to match an AS-path attribute *after* a prepend has occurred on
that path — `TransferBDD.checkForAsPathMatchAfterUpdate` raises
`UnsupportedOperationException` so that the path is marked unsupported. This
keeps the engine sound but is a real expressiveness gap (see [Known
Gaps](#known-gaps)).

### Output constraints

When a question imposes constraints on the *output* AS-path, the prepend list
must be reconciled with the AP encoding. `AsPathRegexAtomicPredicates`
provides `prependAPs(prependedASes)`, which rewrites each AP's automaton to
accept languages of the form "prepended-sequence followed by something the
original AP would accept". The answerer (e.g.,
`SearchRoutePoliciesAnswerer`) calls `prependAPs` on a copy of the APs, then
applies output constraints via `constrainAPs`. The final BDD is the OR of all
APs whose post-prepend, post-constraint automata are nonempty.

## Local Preference (and other scalar attributes)

Local preference is a 32-bit `MutableBDDInteger` field on `BDDRoute`. It is
modified by `SetLocalPreference`, which dispatches through
`applyLongExprModification`:

- `LiteralLong` → set to the literal value.
- `IncrementLocalPreference` → `addClipping` (saturates at the integer
  bounds rather than wrapping).
- `DecrementLocalPreference` → `subClipping`.
- Anything else → `UnsupportedOperationException`.

`SetMetric`, `SetTag`, and `SetWeight` use the same infrastructure with their
respective `BDDRoute` integer fields.

There is no `MatchLocalPreference` handler in `TransferBDD`. Any policy that
uses `MatchLocalPreference` falls through to the catch-all and is marked
unsupported. By contrast, `MatchMetric` and `MatchTag` are supported via
direct integer comparisons. Adding `MatchLocalPreference` would be a
mechanical change.

## Non-BGP Routes

The protocol field on `BDDRoute` is a full `BDDDomain<RoutingProtocol>` over
all `RoutingProtocol` enum values, so the *interpreter* itself can handle
`MatchProtocol` for any protocol — it just becomes a constraint on the
protocol BDDDomain.

What constrains the engine to BGP is the question layer:

- `BgpRouteConstraints.validate` requires the user-supplied `protocol` set to
  be a subset of `BDDRoute.ALL_BGP_PROTOCOLS` (`AGGREGATE`, `BGP`, `IBGP`).
- `routeConstraintsToBDD` calls `wellFormednessConstraints(true)`, which adds
  a constraint that the route's protocol is one of the BGP protocols.

Static-route support added in batfish/batfish#9339 partially relaxes this:

- `ModelGeneration.satAssignmentToInputRoute` dispatches on the satisfying
  assignment's protocol value: `RoutingProtocol.STATIC` produces a
  `StaticRoute` (with prefix, next-hop, and tag) via
  `satAssignmentToStaticInputRoute`; otherwise a `Bgpv4Route`.
- However, the question answerers all currently call
  `satAssignmentToBgpInputRoute` directly, so the protocol-dispatching path
  is reachable from tests but not from `searchRoutePolicies` itself.
- `satAssignmentToOutputRoute` always produces a `BgpRoute`. When the input
  was static, it sets `protocol=BGP`, `srcProtocol=STATIC`,
  `originType=INCOMPLETE`, and `originMechanism=NETWORK` — matching how
  `testRoutePolicies` simulates a redistributed static route.

In short: the engine has the machinery to reason about non-BGP route inputs,
but the user-facing questions are scoped to BGP, with limited static-route
support layered on for the redistribution use case. Other protocols (OSPF,
ISIS, EIGRP) are not exposed to users.

## Tracking "Unsupported" Paths

When the interpreter cannot model a construct precisely, it must not silently
return a wrong answer. `TransferBDD.unsupported` is the central handler:

- It is invoked from `catch (UnsupportedOperationException)` blocks around
  individual statement and expression visits.
- It logs a warning (deduplicated per construct) and sets the
  `_unsupported` flag on the path's `BDDRoute`.
- The path is **not pruned** — it remains in the result list. Question
  answerers can then surface unsupported results to the user, or order them
  last so that supported answers are returned first.

This is a deliberate soundness/completeness tradeoff: the engine never claims
a construct does X when the real semantics are unknown; it instead admits "I
do not know" and defers to the answerer to decide what to do.

## Known Gaps

The list below is non-exhaustive but covers the most common surprises. All
references are to classes in
`projects/minesweeper/src/main/java/org/batfish/minesweeper/`.

### Architectural

- **Concrete prepend list.** `BDDRoute` stores prepends concretely
  (`_prependedASes`), so only paths with identical prepend sequences can be
  merged in `combineSymbolicResults`. Policies with many distinct prepend
  combinations produce many paths.
- **No `MatchLocalPreference`.** No handler exists in `TransferBDD`; affected
  paths are marked unsupported.
- **No symbolic intermediate-attribute tracking across calls in some cases.**
  `TransferParam` updates made inside expression visits can be dropped — see
  the TODO in `TransferBDD.compute(BooleanExpr, ...)`.

### AS-path

- **Match-after-prepend is unsupported.** Once a path has appended ASNs to
  `_prependedASes`, any `match as-path` on that path raises
  `UnsupportedOperationException` (`checkForAsPathMatchAfterUpdate`).
- **`HasAsPathLength` with `EQ` and non-literal lengths is unsupported.**
  The supported cases compile to a length cap (currently 64, see
  `AsPathMatchExprToRegexes`).
- **AS-sets** (multiple ASNs at one position in a regex) are not modeled by
  `SymbolicAsPathRegex`.
- **Only `LiteralAsList` of `ExplicitAs` prepends are supported.** Other
  `AsPathSetExpr` forms are unsupported.

### Communities

- **`CommunitySetMatchRegex` is unsupported** (regex over the set's
  serialization rather than over individual community values).
- **Extended-community high/low/full GA matches and LA matches are
  unsupported** (see `CommunityMatchExprToBDD`'s
  `ExtendedCommunityGlobalAdministratorMatch`,
  `ExtendedCommunityLocalAdministratorMatch`).
- **`HasSize` with `EQ` or `GT`** falls back to conservative answers
  (`CommunitySetMatchExprToBDD`).

### Next-hop

- **Match-after-set is unsupported.** Matching on the next-hop after a set
  has changed it raises `UnsupportedOperationException`.
- **`NextHopExpr` variants beyond IP, peer-address, and self are
  unsupported.**

### Set/Match scalar expressions

- **Non-literal `SetOrigin`, non-literal `SetWeight`, non-literal
  `IntComparison`** are unsupported.
- **`RouteFilterLine` with an `IpWildcard` that is not a valid prefix** is
  unsupported.

### Other

- **`WithEnvironmentExpr` pre/post statements** are noted in the code as not
  fully handled.
- **`ConjunctionChain`** is marked as untested/unmaintained;
  `FirstMatchChain` is the supported chain construct.

For the most up-to-date list, search for `setUnsupported`, `unsupported(`,
and `UnsupportedOperationException` under
`projects/minesweeper/src/main/java/`.

## Related Documentation

- [Reachability symbolic engine](README.md) — BDDs over packets, not routes.
- [searchRoutePolicies](../question_development/searchRoutePolicies.md) —
  user-facing question that drives this engine.
- [testRoutePolicies](../question_development/testRoutePolicies.md) —
  concrete-route counterpart.
- [BDD memory management](../development/bdd_best_practices.md) — applies
  here too: every BDD must be freed exactly once.
