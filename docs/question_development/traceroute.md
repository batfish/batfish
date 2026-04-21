# traceroute

## Overview

`traceroute` simulates the path of a packet through the network from a starting location to a destination IP, reporting every hop along the way including routing decisions, filter evaluations, NAT transformations, and the final disposition.

**Location**: `projects/question/src/main/java/org/batfish/question/traceroute/`

**Question definition**: `questions/experimental/traceroute.json`

## Why This Question Exists

Traceroute is the fundamental debugging tool for network forwarding analysis:

1. **Path visualization**: Engineers need to see the exact path a packet takes through the network, including which routes are matched, which interfaces are traversed, and what transformations occur.

2. **Debugging connectivity failures**: When traffic is blocked, traceroute shows exactly where and why - whether due to ACL denials, routing black holes, loops, or missing ARP entries.

3. **Unidirectional analysis**: Unlike real traceroute (which requires ICMP responses), Batfish's traceroute is directional - it traces the forward path without requiring reverse connectivity. This helps isolate issues when debugging asymmetric routing problems.

4. **ECMP path enumeration**: Real networks often have multiple equal-cost paths. Batfish's traceroute discovers all possible paths, revealing whether some ECMP branches fail while others succeed.

The 2023 SIGCOMM paper discusses how forwarding analysis ("Lesson 4: Faithfully modeling the data plane is essential") requires precise FIB computation and handling of edge cases like ARP failures, which traceroute exposes directly to users.

## How It Works

### High-Level Flow

```
User inputs (startLocation, headers)
        |
        v
[HeaderConstraintsToFlows] -- resolves specifiers, builds Flow objects
        |
        v
[TracerouteEngineImpl] -- chunks flows, creates context
        |
        v
[FlowTracer] -- depth-first traversal of network paths
        |
        v
[DagTraceRecorder] -- compresses paths into DAG, builds Traces
        |
        v
[TracerouteAnswerer] -- formats results into table rows
```

### Flow Construction

`HeaderConstraintsToFlows` converts user inputs into concrete `Flow` objects:

1. **Resolve location specifier** to source locations (interfaces or nodes)
2. **Resolve IP constraints** to concrete source and destination IPs
3. **Build flow from header constraints** using BDDs with traceroute-biased preferences (e.g., prefer TCP, common ports)
4. **Generate one flow per source location** for cross-product analysis

### The FlowTracer Algorithm

`FlowTracer` implements a depth-first traversal of all possible paths through the network. At each hop:

1. **Enter interface** - record entering the device
2. **Session matching** - check for existing firewall sessions that would alter processing
3. **Policy-based routing** - apply packet policies if configured on ingress interface
4. **Ingress filter** - evaluate input ACLs
5. **Ingress transformation** - apply NAT/transformations
6. **VRF acceptance check** - if destination IP is owned by this VRF, accept
7. **FIB lookup** - find longest-prefix match routes
8. **For each FIB action** (ECMP branches are explored in parallel):
   - **Forward**: apply egress filters, transformations, session setup, then ARP resolution
   - **Next-VRF**: delegate to another VRF (route leaking)
   - **Null-route**: discard packet
9. **ARP resolution** - determine if next-hop is reachable via topology
10. **Loop detection** - track visited (node, VRF, interface, flow) tuples as breadcrumbs
11. **Recurse to next hop** or terminate with disposition

### Loop Detection

Loop detection uses "breadcrumbs" - tuples of (node, VRF, ingress interface, flow headers). When the tracer visits a state it has seen before on the current path, it terminates with `LOOP` disposition.

The breadcrumb comparison is optimized: only interfaces that are actually referenced in ACL `match-src-interface` conditions are included, reducing false loop detection when the same flow re-enters a node via different interfaces that don't affect processing.

### DAG Compression

Large networks with ECMP can produce exponentially many traces. The `DagTraceRecorder` compresses traces into a directed acyclic graph (DAG):

- **Shared suffixes**: If multiple paths converge, they share the suffix subgraph
- **Reusable nodes**: A hop node can be reused by different prefixes if loop constraints match
- **Memory efficiency**: The DAG has `O(nodes * interfaces)` size vs `O(paths)` for flat traces

Traces are lazily expanded from the DAG, limited to 10,000 per flow by default.

### Flow Dispositions

Every trace ends with a disposition indicating what happened to the packet. See [Flow Dispositions](../flow_dispositions/README.md) for the complete reference:

**Success dispositions**: `ACCEPTED`, `DELIVERED_TO_SUBNET`, `EXITS_NETWORK`

**Failure dispositions**: `DENIED_IN`, `DENIED_OUT`, `NO_ROUTE`, `NULL_ROUTED`, `LOOP`, `NEIGHBOR_UNREACHABLE`, `INSUFFICIENT_INFO`

## Key Classes

| Class | Location | Responsibility |
|-------|----------|---------------|
| `TracerouteQuestion` | `projects/question/.../traceroute/` | Question parameters: start location, headers, ignoreFilters, maxTraces |
| `TracerouteAnswerer` | `projects/question/.../traceroute/` | Orchestrates question execution, formats output table |
| `HeaderConstraintsToFlows` | `projects/question/.../question/` | Resolves specifiers, constructs Flow objects from constraints |
| `TracerouteEngine` | `projects/common/.../plugin/` | Interface for computing traces from flows |
| `TracerouteEngineImpl` | `projects/batfish/.../dataplane/` | Main implementation: chunks flows, builds context, coordinates tracing |
| `TracerouteEngineImplContext` | `projects/batfish/.../dataplane/traceroute/` | Per-trace-batch context: FIBs, forwarding analysis, sessions, topology |
| `FlowTracer` | `projects/batfish/.../dataplane/traceroute/` | Core tracing logic: DFS traversal, filter evaluation, routing decisions |
| `DagTraceRecorder` | `projects/batfish/.../dataplane/traceroute/` | Compresses traces into DAG for memory efficiency |
| `TraceDag` | `projects/common/.../traceroute/` | Interface for DAG of traces |
| `Trace` | `projects/common/.../datamodel/flow/` | A single trace: list of hops with final disposition |
| `Hop` | `projects/common/.../datamodel/flow/` | One hop through a device: node name and list of steps |
| `Step` | `projects/common/.../datamodel/flow/` | An action within a hop (enter interface, filter, route, transform, exit) |
| `Breadcrumb` | `projects/batfish/.../dataplane/traceroute/` | Loop detection state: (node, VRF, interface, flow) |

## Output Schema

| Column | Type | Description |
|--------|------|-------------|
| `Flow` | Flow | The packet being traced (5-tuple and other headers) |
| `Traces` | List of Trace | All discovered paths through the network |
| `TraceCount` | Integer | Total number of traces (may exceed `maxTraces` limit) |

For differential mode (`answerDiff`), output includes base and delta variants of Traces and TraceCount.

### Trace Structure

Each `Trace` contains:
- `disposition`: Final outcome (ACCEPTED, DENIED_IN, etc.)
- `hops`: List of `Hop` objects

Each `Hop` contains:
- `node`: Device name
- `steps`: List of `Step` objects showing actions taken

Step types include:
- `EnterInputIfaceStep`: Packet enters device on interface
- `OriginateStep`: Packet originates from device
- `FilterStep`: ACL evaluation (PERMITTED or DENIED)
- `RoutingStep`: FIB lookup result with matched routes
- `TransformationStep`: NAT or other header modification
- `SetupSessionStep`: Firewall session established
- `MatchSessionStep`: Existing session matched
- `ExitOutputIfaceStep`: Packet transmitted out interface
- `ArpErrorStep`: ARP resolution failed
- `DeliveredStep`: Packet delivered (success dispositions)
- `InboundStep`: Packet accepted by device
- `LoopStep`: Loop detected

## Performance Considerations

- **Flow chunking**: `TracerouteEngineImpl` processes flows in chunks of 256 to bound memory usage per batch.

- **Parallel tracing**: Flows within a chunk are traced in parallel using Java streams.

- **Breadcrumb interning**: Breadcrumbs are interned (`Interners.newStrongInterner()`) to avoid duplicate object creation during loop detection (#6906).

- **DAG compression**: The `DagTraceRecorder` avoids exponential blowup from ECMP by sharing common sub-paths.

- **FIB caching**: FIBs are computed once during data plane generation and reused across all traceroute invocations.

- **IpSpaceContainsIp caching**: Forwarding disposition lookups cache `IpSpaceContainsIp` evaluators to avoid repeated BDD operations (#7473).

- **Trace limit**: By default, only 256 traces per flow are returned (`DEFAULT_MAX_TRACES`). The `maxTraces` parameter controls this limit.

## Known Limitations

1. **No TTL modeling**: Batfish does not model TTL decrement or TTL-exceeded responses. Traces show the full path regardless of hop count.

2. **No packet fragmentation**: MTU limits and fragmentation are not modeled.

3. **Stateless ACL evaluation**: ACLs are evaluated statelessly. While session setup/matching is modeled, the actual stateful tracking of TCP connections is not.

4. **ARP heuristics for incomplete snapshots**: When ARP fails (no device responds), Batfish uses heuristics based on IP ownership and subnet configuration to assign disposition. See [Flow Dispositions](../flow_dispositions/README.md) for details.

5. **Single representative flow per constraint set**: When header constraints allow multiple flows, Batfish picks one representative flow (biased toward traceroute-like traffic). Use `reachability` for exhaustive analysis.

## Common Sources of Confusion

### "Why does my trace show multiple paths?"

ECMP (Equal-Cost Multi-Path) routing creates multiple valid forwarding paths. Batfish explores all of them. If some paths succeed and others fail, this reveals potential problems with partial reachability.

### "Why does the trace end with INSUFFICIENT_INFO?"

This disposition indicates the packet was forwarded out an interface, but no device in the snapshot responded to ARP. The subnet appears incomplete (not all IPs are accounted for), suggesting the snapshot may be missing a device. This is different from `NEIGHBOR_UNREACHABLE`, which indicates the destination should be reachable but isn't.

### "Why doesn't my session match?"

Sessions are scoped to specific interfaces or VRFs. Check that:
1. The return flow enters on an interface in the session's scope
2. The session hasn't been consumed (sessions match once per direction)
3. The protocol supports sessions (TCP, UDP, ICMP have sessions; others may not)

### "The trace shows DENIED_IN but I expected my ACL to permit"

Check the step details for which ACL and line denied the packet. Common causes:
- Implicit deny at end of ACL
- Earlier line shadowing the permit
- Wrong source IP (check if NAT should have occurred first)
- Interface has multiple ACLs (ingress filter vs post-transformation filter)

## Related Questions

- **`bidirectionalTraceroute`**: Traces both forward and reverse paths, useful for verifying symmetric connectivity.
- **`reachability`**: Symbolic analysis that finds all flows matching constraints, rather than tracing specific flows.
- **`differentialReachability`**: Compares reachability between two snapshots.
- **`detectLoops`**: Finds all flows that would loop, using symbolic analysis.
- **`routes`**: Shows the RIB (routing table) used to make forwarding decisions.
- **`searchFilters`**: Finds flows that match/don't match specific ACLs.

## References

- [Forwarding Analysis documentation](../forwarding_analysis/README.md) - details on FIB lookup and disposition computation
- [Data Plane documentation](../data_plane/README.md) - how routes and FIBs are computed
- [Flow Dispositions documentation](../flow_dispositions/README.md) - complete disposition reference
- [Pybatfish forwarding notebook](https://github.com/batfish/pybatfish/blob/master/jupyter_notebooks/Forwarding%20Analysis.ipynb) - user-facing examples
- batfish/batfish#7956 - faster loop detection with interface-aware breadcrumbs
- batfish/batfish#7473 - IpSpaceContainsIp caching for performance
- batfish/batfish#6906 - breadcrumb interning optimization
