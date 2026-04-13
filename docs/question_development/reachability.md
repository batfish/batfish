# reachability

## Overview

`reachability` finds flows that match specified header and path constraints and returns examples of such flows. It is one of Batfish's core questions, enabling users to verify that certain services are globally accessible and that parts of the network are isolated from each other.

**Location**: `projects/question/src/main/java/org/batfish/question/specifiers/`

**Question definition**: `questions/experimental/reachability.json`

## Why This Question Exists

Reachability analysis is the foundational use case for Batfish's symbolic analysis engine. Traditional network testing uses concrete packet examples — you test a few specific flows and hope they represent the full space of possible behaviors. This approach cannot provide guarantees: if a bug exists for packets you didn't test, you won't find it.

Batfish's symbolic approach models packets as mathematical variables and uses Binary Decision Diagrams (BDDs) to reason about **all possible flows** simultaneously. A single reachability query can answer questions like "can any packet from the internet reach the internal database server?" — covering the entire space of possible packet headers, not just a few examples.

The 2023 SIGCOMM paper describes this as "Symbolic Data Plane Analysis" (Section 4.2): by encoding packet header fields as BDD variables and forwarding behavior as BDD operations, Batfish provides exhaustive guarantees about network reachability in polynomial time relative to the size of the BDD representations.

Use cases include:

1. **Service accessibility verification**: Ensure critical services (DNS, DHCP, APIs) are reachable from all required sources
2. **Network segmentation testing**: Verify that sensitive subnets are isolated from unauthorized sources
3. **Firewall rule validation**: Confirm that intended traffic is permitted while blocked traffic is denied
4. **Routing policy verification**: Check that traffic follows expected paths through the network

## How It Works

### High-Level Flow

1. **Resolve specifiers** → convert user-provided location and IP space specifiers to concrete sets
2. **Build reachability graph** → create a graph of `StateExpr` nodes representing forwarding stages
3. **BDD constraint generation** → convert header constraints, ACLs, and FIBs to BDDs
4. **Backward fixpoint computation** → propagate reachable packet sets from destinations to sources
5. **Solution extraction** → convert result BDDs to concrete example flows
6. **Trace generation** → run traceroute on example flows to produce human-readable output

### The Reachability Graph

The analysis constructs a directed graph where:

- **Nodes** are `StateExpr` objects representing stages of packet processing
- **Edges** are `Transition` objects labeled with BDD constraints representing the packets that can traverse them

Key state expressions include:

| State | Description |
|-------|-------------|
| `OriginateVrf` / `OriginateInterfaceLink` | Packet origination points |
| `PreInInterface` | Before ingress ACL processing |
| `PostInInterface` | After ingress ACL, before routing |
| `PostInVrf` | In VRF, ready for FIB lookup |
| `PreOutVrf` | Before egress ACL processing |
| `PreOutEdge` / `PreOutEdgePostNat` | Before/after egress NAT |
| `InterfaceAccept` | Packet accepted by interface |
| `VrfAccept` / `NodeAccept` / `Accept` | Packet accepted (various aggregations) |
| `NodeDrop*` | Various drop dispositions (ACL, no route, null route) |
| `DeliveredToSubnet` / `ExitsNetwork` | Packets leaving the modeled network |
| `Query` | Terminal node for matching dispositions |

### BDD-Based Fixpoint Computation

The core analysis uses a **backward fixpoint algorithm**:

```
1. Initialize Query state with packets matching target dispositions
2. While changes occur:
     For each state with non-empty BDD:
       For each incoming edge:
         Propagate BDD backward through transition
         OR result into predecessor state's BDD
3. Result: each origination state has BDD of packets that can reach Query
```

Backward analysis is typically more efficient than forward analysis because:
- Destination constraints are often tighter than source constraints
- Only paths leading to target dispositions are explored
- Irrelevant parts of the network are never visited

The fixpoint algorithm (`BDDReachabilityUtils.fixpoint()`) uses a priority queue ordered by visit count to minimize redundant edge traversals.

### Transition Types

Edges in the graph are labeled with `Transition` objects:

- **Constraint**: restricts packets to those matching a BDD (e.g., ACL permit)
- **Transformation**: modifies packet fields (e.g., NAT)
- **Composition**: chains multiple transitions
- **Branch**: conditional transitions based on packet match

For NAT handling, the analysis uses "primed" BDD variables to represent transformed packet fields. The relationship between original and transformed values is encoded as BDD constraints, enabling precise reasoning about packet transformations without losing track of the original packet.

## Key Classes

| Class | Location | Responsibility |
|-------|----------|---------------|
| `SpecifiersReachabilityQuestion` | `question/specifiers/` | Question definition with header/path constraint parameters |
| `SpecifiersReachabilityAnswerer` | `question/specifiers/` | Invokes `IBatfish.standard()` and formats results |
| `ReachabilityParameters` | `common/question/` | Parameters for reachability analysis (specifiers, dispositions, etc.) |
| `BDDReachabilityAnalysisFactory` | `batfish/bddreachability/` | Constructs the reachability graph with all transitions |
| `BDDReachabilityAnalysis` | `batfish/bddreachability/` | Holds the graph and executes fixpoint computation |
| `BDDReachabilityUtils` | `batfish/bddreachability/` | Fixpoint algorithm and utility methods |
| `StateExpr` | `symbolic/state/` | Abstract base for graph nodes (many subclasses) |
| `Transition` | `bddreachability/transition/` | Edge labels encoding packet constraints/transformations |
| `BDDPacket` | `common/bdd/` | BDD variables for packet header fields |
| `IpAccessListToBdd` | `common/bdd/` | Converts ACLs to BDDs |
| `BDDFibGenerator` | `batfish/bddreachability/` | Generates FIB lookup transitions |
| `TracerouteAnswerer` | `question/traceroute/` | Converts flows to traces for output |

## BDD Infrastructure

The question depends heavily on the BDD infrastructure in `org.batfish.common.bdd`:

- **`BDDPacket`**: Allocates BDD variables for packet fields (~100 boolean variables for IP headers)
- **`IpSpaceToBDD`**: Converts IP prefix/range constraints to BDDs
- **`HeaderSpaceToBDD`**: Converts full header constraints to BDDs
- **`IpAccessListToBdd`**: Converts ACL match conditions to BDDs
- **`BDDSourceManager`**: Tracks source interface for `MatchSrcInterface` expressions

## Output Schema

The question returns the same schema as `traceroute`:

| Column | Schema | Description |
|--------|--------|-------------|
| `Flow` | FLOW | The example flow (5-tuple plus metadata) |
| `Traces` | Set\<TRACE\> | The paths this flow takes through the network |
| `TraceCount` | INTEGER | Total number of traces (may exceed displayed count if pruned) |

Each trace contains:
- The sequence of hops through the network
- The final disposition (ACCEPTED, DENIED_IN, DENIED_OUT, NO_ROUTE, NULL_ROUTED, etc.)
- Filter/ACL match details at each hop

## Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `actions` | DispositionSpec | "success" | Filter results to flows with these dispositions |
| `headers` | HeaderConstraint | unconstrained | Packet header constraints (IPs, ports, protocols) |
| `pathConstraints` | PathConstraint | unconstrained | Start/end locations, transit/forbidden nodes |
| `ignoreFilters` | boolean | false | Skip ACL processing during analysis |
| `invertSearch` | boolean | false | Search for packets outside the specified header space |
| `maxTraces` | integer | 256 | Maximum traces to return per flow |

## Performance Considerations

### BDD Efficiency

The 2023 SIGCOMM paper (Section 4.2) discusses why BDDs work well for network analysis:

1. **Compact representations**: Real-world ACLs and forwarding tables produce BDDs that are polynomial in size, not exponential
2. **Efficient operations**: BDD AND/OR/NOT operations are polynomial in BDD size
3. **Canonical form**: Equal sets always produce identical BDDs, enabling efficient equality checks

### Backward vs Forward Analysis

- **Backward analysis** (default) starts from target dispositions and works backward to sources. More efficient when destination constraints are tight.
- **Forward analysis** starts from sources and propagates forward. Better when few sources but many possible destinations.

The `BDDReachabilityAnalysis` class supports both directions; the standard reachability question uses backward analysis.

### Caching and Deduplication

- **ACL BDDs** are computed lazily and cached per-node
- **FIB BDDs** are computed once during factory construction
- **Transformation ranges** (NAT pool IPs) are precomputed to optimize NAT encoding

### Parallelization

Graph construction is parallelized across devices. The fixpoint computation itself is sequential but operates on a pre-built graph with cached BDD computations.

## Known Limitations

1. **Stateful firewall sessions not modeled in isolation**: The `reachability` question analyzes single-direction flows. It does not model established sessions that would affect return traffic. Use `bidirectionalReachability` for session-aware analysis.

2. **Example flows only**: The question returns example flows, not all matching flows. The BDD analysis is exhaustive, but output is limited to concrete examples for human readability.

3. **No differential mode**: Running `reachability` in differential mode is not supported. Use `differentialReachability` for comparing snapshots.

4. **Memory for large networks**: BDD memory usage can be significant for very large networks with complex policies. The implementation uses careful memory management (see `BDDReachabilityUtils.fixpoint()` for the pattern).

5. **ECMP behavior**: All ECMP paths are explored and returned as separate traces. This can produce many traces for networks with extensive multipathing.

## Common Sources of Confusion

### "Why doesn't it find flows I know exist?"

The most common causes:

1. **Source IP inference**: By default, source IPs are inferred from the start location's assigned addresses. If the source interface has no IP or an unexpected IP, packets may not originate.

2. **Implicit denies**: Most real ACLs have an implicit deny at the end. If your header constraints don't match any permit rule, flows will be denied.

3. **Routing**: Packets must have valid routes to reach destinations. Check `routes` question output if packets are unexpectedly dropped with NO_ROUTE.

4. **Disposition filter**: The default `actions` parameter is "success" — only successfully delivered flows are returned. Use `actions=failure` or `actions=*` to see dropped flows.

### "I want to verify isolation — how do I check that nothing can reach X?"

Use `invertSearch=true` with tight constraints on what should NOT reach X, or simply run reachability with the source/destination pair and verify no flows are returned.

For automated verification, check that the result set is empty:
```python
result = bf.q.reachability(
    pathConstraints=PathConstraints(startLocation="internet"),
    headers=HeaderConstraints(dstIps="10.0.0.0/24")
).answer()
assert result.frame().empty, "Unexpected reachability to internal network"
```

### "Why are there so many traces for a single flow?"

ECMP and policy-based routing can create multiple valid paths through the network. Each distinct path produces a separate trace. Use `maxTraces` to limit output, but be aware this only affects display — the underlying analysis considers all paths.

## Related Questions

| Question | Use Case |
|----------|----------|
| `bidirectionalReachability` | Verify flows can both reach destination AND receive a response (session-aware) |
| `differentialReachability` | Compare reachability between two snapshots (detect regressions) |
| `traceroute` | Trace a specific flow's path (when you know the exact packet) |
| `detectLoops` | Find flows that enter forwarding loops |
| `searchFilters` | Find flows that match/don't match specific filters |

## References

- [Symbolic Engine documentation](../symbolic_engine/README.md) — comprehensive guide to BDD-based analysis
- [Forwarding Analysis documentation](../forwarding_analysis/README.md) — how FIBs and forwarding decisions are computed
- [2023 SIGCOMM Paper](https://dl.acm.org/doi/10.1145/3603269.3604873) — "Lessons from Eight Years of Operating a Network Verification Tool" (Section 4.2 on BDD-based data plane analysis)
- [Pybatfish Reachability documentation](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html) — user-facing examples
