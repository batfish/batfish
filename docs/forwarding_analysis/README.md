# Forwarding Analysis

In Batfish, forwarding analysis is the process of determining how packets flow through the network based on the computed data plane (FIBs, routes, ARP information). It analyzes the forwarding behavior of each VRF and interface to determine flow dispositions and support questions like traceroute and reachability.

## Overview

Forwarding analysis sits between data plane computation and question answering:

```
Vendor-Independent Model → [Data Plane Computation] → Forwarding Analysis → [Questions]
                                            ↓
                                    FIBs, Routes, ARP
                                            ↓
                                  Forwarding Analysis
                                            ↓
                             Flow Dispositions, Traces
```

## Input

- **Post-processed vendor-independent configuration objects**: Finalized network configurations
- **Forwarding information base (FIB) entries**: Computed routes for each VRF
- **L3 topology**: IP routing adjacencies between devices
- **IP address ownership information**: Which interfaces own which IPs

## Output

- **ARP behavior for each interface**: Which IPs each interface responds to
- **Forwarding behavior for each VRF**: How packets are forwarded
- **Flow dispositions at each interface**: What happens to packets (accept, drop, exit network, etc.)
- **IP space mappings**: Which destination IPs map to which dispositions

## Main ForwardingAnalysis Interface

**Location:** `projects/common/src/main/java/org/batfish/datamodel/ForwardingAnalysis.java`

```java
public interface ForwardingAnalysis extends Serializable {
    /**
     * Returns mapping from hostname → interface → IP space for which the interface
     * would reply to ARP requests.
     */
    Map<String, Map<String, IpSpace>> getArpReplies();

    /**
     * Returns mapping from hostname → VRF name → VRF forwarding behavior.
     */
    Map<String, Map<String, VrfForwardingBehavior>> getVrfForwardingBehavior();
}
```

## ForwardingAnalysisImpl

**Location:** `projects/common/src/main/java/org/batfish/datamodel/ForwardingAnalysisImpl.java`

The main implementation of forwarding analysis.

### Key Responsibilities

1. **Compute ARP reply behavior** for each interface
2. **Analyze FIBs** to determine forwarding dispositions
3. **Handle VRF-specific forwarding** behavior
4. **Compute IP spaces** for different flow dispositions
5. **Aggregate routing information** from FIBs

### Main Processing Steps

```
┌─────────────────────────────────────────────────────────────────┐
│           Compute owned and unowned IPs                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│           Aggregate routing information                          │
│     (FIB entries, routes, matching IPs)                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Compute ARP replies                                 │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│        Compute interfaces with missing devices                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│           Compute VRF forwarding behavior                        │
│              (parallel per-VRF computation)                      │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│                    ForwardingAnalysis                            │
└─────────────────────────────────────────────────────────────────┘
```

## VrfForwardingBehavior

**Location:** `projects/common/src/main/java/org/batfish/datamodel/VrfForwardingBehavior.java`

Represents the forwarding behavior of a single VRF.

### Key Data Structures

```java
public class VrfForwardingBehavior {
    // Edges where forwarding succeeds with ARP response
    private Map<Edge, IpSpace> _arpTrueEdge;

    // Per-interface forwarding behavior
    private Map<String, InterfaceForwardingBehavior> _interfaceForwardingBehavior;

    // IPs delegated to other VRFs
    private Map<String, IpSpace> _nextVrf;

    // Destination IPs that are null-routed
    private IpSpace _nullRoutedIps;

    // Destination IPs with routes
    private IpSpace _routableIps;
}
```

## InterfaceForwardingBehavior

**Location:** `projects/common/src/main/java/org/batfish/datamodel/InterfaceForwardingBehavior.java`

Represents forwarding behavior for a single interface.

### IP Space Mappings

| Field | Disposition | Description |
|-------|-------------|-------------|
| `_acceptedIps` | ACCEPTED | IPs accepted by this interface (interface owns destination IP) |
| `_deliveredToSubnet` | DELIVERED_TO_SUBNET | IPs forwarded to unowned IPs in interface's connected subnet |
| `_exitsNetwork` | EXITS_NETWORK | External IPs forwarded out this interface |
| `_neighborUnreachable` | NEIGHBOR_UNREACHABLE | IPs where ARP fails for full subnet or owned IP |
| `_insufficientInfo` | INSUFFICIENT_INFO | IPs where ARP fails and topology appears incomplete |

## Flow Dispositions

Flow dispositions describe what ultimately happens to a packet. See [Flow Dispositions](../flow_dispositions/README.md) for complete details.

### Success Dispositions

| Disposition | Description |
|-------------|-------------|
| `ACCEPTED` | Flow reaches an interface that owns the destination IP |
| `DELIVERED_TO_SUBNET` | Flow forwarded out interface, ARP fails, destination is unowned and in interface's connected subnet |
| `EXITS_NETWORK` | Flow forwarded out interface, ARP fails, destination is external |

### Failure Dispositions

| Disposition | Description |
|-------------|-------------|
| `DENIED_IN` | Flow was denied by an ingress filter/ACL |
| `DENIED_OUT` | Flow was denied by an egress filter/ACL |
| `NO_ROUTE` | Flow reached a VRF whose FIB had no route for destination IP |
| `NULL_ROUTED` | Flow reached a VRF whose longest-matching route was a null route |
| `LOOP` | Flow entered a forwarding loop |
| `NEIGHBOR_UNREACHABLE` | Flow forwarded out interface, ARP fails, subnet is full or destination is owned elsewhere |
| `INSUFFICIENT_INFO` | Flow forwarded out interface, ARP fails, topology appears incomplete |

## ARP Analysis

ARP behavior computation is a key component of forwarding analysis.

### ARP Reply Logic

**Location:** `ForwardingAnalysisImpl.computeArpReplies()`

Interfaces respond to ARP requests in the following cases:

1. **Interface IPs**: Reply for IPs assigned to the interface
2. **Static ARP IPs**: Reply for IPs in statically configured ARP entries
3. **Proxy ARP**: If enabled, reply for all VRF-owned IPs except those routed through the interface
4. **Routing-based**: Reply for routable IPs not rejected by routing

### ARP Failure Handling

When ARP fails (no response), Batfish uses heuristics to determine the flow disposition:

**Key factors:**
- Is the ARP IP owned by any interface in the snapshot?
- Is the ARP IP internal or external to the network?
- Is the interface's connected subnet "full" (all IPs are owned)?
- Is the ARP IP in the interface's connected subnet?

**Disposition assignment:**
- `DELIVERED_TO_SUBNET`: Unowned IP in interface's connected subnet
- `EXITS_NETWORK`: External IP
- `NEIGHBOR_UNREACHABLE`: Full subnet or owned IP elsewhere in network
- `INSUFFICIENT_INFO`: Internal IP but subnet not full (likely missing device)

## Relationship with Data Plane

The `ForwardingAnalysis` is accessed through the `DataPlane` interface:

**Location:** `projects/common/src/main/java/org/batfish/datamodel/DataPlane.java`

```java
public interface DataPlane extends Serializable {
    // ... other methods ...

    /**
     * Returns the ForwardingAnalysis computed from this data plane.
     */
    @Nonnull
    ForwardingAnalysis getForwardingAnalysis();
}
```

The `DataPlane` contains:
- FIBs for each node/VRF
- BGP routes
- EVPN routes
- RIBs (Routing Information Bases)
- **ForwardingAnalysis** (computed from FIBs and topology)

## Flow Tracing and Traceroute

Flow tracing determines the path a packet takes through the network.

### FlowTracer

**Location:** `projects/batfish/src/main/java/org/batfish/dataplane/traceroute/FlowTracer.java`

Main class that traces individual flows through the network.

### TracerouteEngineImplContext

Context containing network-wide state for tracing:
- Data plane (FIBs, routes)
- Forwarding analysis results
- Topology information
- Session state

### Tracing Process

1. **Ingress interface processing**: Apply ingress filters, transformations
2. **Session matching**: Check for existing firewall sessions
3. **VRF acceptance**: Verify VRF accepts the packet
4. **FIB lookup**: Find longest-matching route
5. **Outgoing interface processing**: Apply egress filters
6. **ARP resolution**: Determine if next-hop is reachable
7. **Disposition assignment**: Use ForwardingAnalysis to determine final disposition
8. **Loop detection**: Track visited nodes to detect loops

### Tracing Output

```java
FlowTrace trace = FlowTrace.builder()
    .setDisposition(disposition)
    .setHops(hops)
    .setFlow(flow)
    .build();
```

**FlowTrace components:**
- `FlowDisposition`: Final outcome
- `List<FlowTraceHop>`: Ordered list of hops through the network
- `Flow`: The packet being traced

## Interface to Symbolic Engine

The symbolic engine (BDD-based reachability) interfaces with forwarding analysis.

### BDDReachabilityAnalysis

**Location:** `projects/batfish/src/main/java/org/batfish/bddreachability/BDDReachabilityAnalysis.java`

Uses BDDs for symbolic reachability computation.

### Integration Points

1. **StateExpr nodes**: Represent different stages of packet processing
   - `PreInInterface`: Before ingress filter
   - `PostInInterface`: After ingress filter
   - `InterfaceAccept`: Interface accepts packet
   - `ExitsNetwork`: Packet exits network
   - `NodeAccept`: Packet accepted by node

2. **Transitions**: Represent packet transformations and forwarding decisions
   - Filter transitions (permit/deny)
   - FIB transitions (route lookup)
   - Disposition transitions (final outcome)

3. **ForwardingAnalysis results**: Used to determine packet dispositions at network boundaries
   - ARP behavior for neighbor resolution
   - IP ownership for acceptance decisions
   - Edge cases (missing devices, incomplete topology)

### Symbolic Analysis Flow

```
Header Constraints + Path Constraints
                 ↓
        Convert to BDD constraints
                 ↓
    Build reachability graph
        (StateExpr nodes + Transitions)
                 ↓
    Compute fixpoint (propagate BDD packet sets)
                 ↓
    Extract solutions (BDD to concrete packets)
```

## Key Design Principles

1. **Separation of Concerns**: FIB computation vs. forwarding analysis vs. flow tracing
2. **Parallel Processing**: VRF forwarding behavior computed in parallel
3. **IP Space Reasoning**: Uses IpSpace and BDDs for efficient IP set operations
4. **Deterministic Ordering**: ECMP branches processed in deterministic order
5. **Comprehensive Dispositions**: Detailed categorization of flow outcomes
6. **Topology Awareness**: Handles incomplete topology gracefully

## Data Structures

### IpSpace

**Location:** `projects/common/src/main/java/org/batfish/datamodel/IpSpace.java`

Represents sets of IP addresses efficiently using:
- Individual IPs
- Prefixes
- Ranges
- Unions
- Complements
- BDD-backed representations

### Edge

**Location:** `projects/common/src/main/java/org/batfish/datamodel/Edge.java`

Represents a directed edge between two interfaces:

```java
Edge edge = new Edge(node1, interface1, node2, interface2);
```

## Key Classes Summary

| Class | Location | Purpose |
|-------|----------|---------|
| `ForwardingAnalysis` | `projects/common/.../datamodel/` | Interface for forwarding analysis |
| `ForwardingAnalysisImpl` | `projects/common/.../datamodel/` | Main implementation |
| `VrfForwardingBehavior` | `projects/common/.../datamodel/` | Per-VRF forwarding behavior |
| `InterfaceForwardingBehavior` | `projects/common/.../datamodel/` | Per-interface behavior |
| `FlowTracer` | `projects/batfish/.../traceroute/` | Individual flow tracing |
| `BDDReachabilityAnalysis` | `projects/batfish/.../bddreachability/` | Symbolic reachability |

## Common Questions

### How does Batfish determine if a neighbor is reachable?

Batfish uses ARP analysis to determine neighbor reachability:
1. Checks if destination IP is owned by any interface
2. Checks if ARP would succeed (ARP reply mapping)
3. If ARP fails, applies heuristics based on subnet fullness and IP ownership

### How are ECMP paths handled?

ECMP (Equal-Cost Multi-Path) routing is handled by:
1. Computing all valid next-hops for a given destination
2. Processing each next-hop in deterministic order
3. Producing separate traces for each path
4. Symbolic analysis considers all ECMP paths simultaneously

### How does Batfish handle incomplete topology?

When ARP fails for an internal IP and the subnet is not full:
1. Assigns `INSUFFICIENT_INFO` disposition
2. Indicates that the snapshot may be missing a device
3. Does not assume success or failure (graceful degradation)

## Related Documentation

- [Data Plane](../data_plane/README.md): Data plane computation produces FIBs used by forwarding analysis
- [Flow Dispositions](../flow_dispositions/README.md): Complete disposition reference
- [Symbolic Engine](../symbolic_engine/README.md): BDD-based reachability analysis
- [Topology](../topology/README.md): Topology computation affects forwarding analysis
