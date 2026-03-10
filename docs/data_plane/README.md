# Data Plane Generation

This document explains how Batfish generates the data plane - the computation of routing tables, forwarding information bases (FIBs), and network topology that powers Batfish's analysis capabilities.

## Table of Contents

- [Overview](#overview)
- [What is the Data Plane?](#what-is-the-data-plane)
- [When is the Data Plane Computed?](#when-is-the-data-plane-computed)
- [IBDP Algorithm](#ibdp-algorithm)
  - [Computation Process](#computation-process)
  - [Node Scheduling](#node-scheduling)
  - [Fixed Point Operations](#fixed-point-operations)
- [Key Data Structures](#key-data-structures)
- [Route Types](#route-types)
- [Oscillations and Nondeterminism](#oscillations-and-nondeterminism)
- [Integration with Other Components](#integration-with-other-components)
- [Performance Considerations](#performance-considerations)
- [Troubleshooting](#troubleshooting)
- [Related Documentation](#related-documentation)

---

## Overview

The data plane represents the actual forwarding behavior of a network - what each device will do with packets it receives. Batfish computes the data plane by simulating the distributed routing protocols and computing the resulting routing and forwarding state.

**Key characteristics:**
- **Comprehensive**: Models all routing protocols (BGP, OSPF, IS-IS, EIGRP, RIP, static, connected)
- **Accurate**: Simulates protocol interactions, route redistribution, and policy effects
- **Deterministic**: Same input always produces the same output
- **Incremental**: Uses efficient fixed-point iteration to converge on stable state

---

## What is the Data Plane?

### Output Structure

The output of Batfish's data plane computation is a `ComputeDataPlaneResult` containing:

#### 1. DataPlane Object

The [`DataPlane`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/DataPlane.java) describes routing and forwarding behavior:

**Forwarding Information Bases (FIBs)**
- Per-VRF forwarding tables
- Next-hop information for each prefix
- Outgoing interface and next-hop IP
- Used for packet forwarding analysis (traceroute, reachability)

**Forwarding Analysis**
- Pre-computed reachability between interfaces
- ARP/NDP resolution information
- Enables efficient flow tracing

**Routing Information Bases (RIBs)**
- Main RIB: Active routes (like `show ip route`)
- BGP RIB: BGP routes (advertised and received)
- EVPN RIB: EVPN MAC/IP bindings
- Used for routes questions

#### 2. TopologyContainer

The [`TopologyContainer`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/topology/TopologyContainer.java) contains:

- **Layer 1 topology**: Physical connections
- **Layer 2 topology**: Switching, VLANs, trunks
- **Layer 3 topology**: IP links, subnet associations
- **Overlay topologies**: VXLAN, BGP EVPN
- **Protocol topologies**: OSPF adjacencies, BGP sessions

#### 3. Serialization

The `DataPlane` and topologies are serialized to disk for reuse:
- Location: Batfish storage for the snapshot
- Format: Optimized Java serialization
- Reused across questions until explicitly regenerated

---

## When is the Data Plane Computed?

### Automatic Computation

The data plane is **NOT** automatically computed during `bf.init_snapshot()` because:
- Computation can take minutes on large networks
- Many questions don't require it (e.g., configuration checks)
- Allows users to prepare snapshots without delay

It is triggered automatically when you run:
- Any data-plane-dependent question (reachability, traceroute, routes)
- `bf.generate_dataplane()` explicitly

### Manual Computation

**Using `bf.generate_dataplane()`:**

```python
# Trigger data plane computation
bf.generate_dataplane()

# Force recomputation (even if already computed)
bf.generate_dataplane()
```

**When to use manually:**
- Debugging data plane computation
- Making code changes to dataplane logic
- Testing with modified snapshots
- Getting better error messages on failures

### Data Plane Reuse

**Reuse within snapshot:**
- Computed once per snapshot
- Reused across all questions
- Never recomputed unless explicitly requested

**No reuse across snapshots:**
- Each snapshot has independent data plane
- Even similar/forked snapshots recompute
- Configuration changes can propagate globally

---

## IBDP Algorithm

Batfish uses the **Incremental Batfish Data Plane (IBDP)** algorithm - an iterative fixed-point computation that simulates protocol interactions.

### Implementation Classes

- **Main class**: [`IncrementalDataPlanePlugin`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/IncrementalDataPlanePlugin.java)
- **Engine**: [`IncrementalDataPlaneEngine`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/IncrementalBdpEngine.java)
- **Result**: [`IncrementalDataPlane`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/IncrementalDataPlane.java)

### Core Data Structures

#### Node
Each network device is represented by a [`Node`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/Node.java):
- Container for all VRFs on the device
- Coordinates parallel computation
- Manages node-level state

#### VirtualRouter
Each VRF is represented by a [`VirtualRouter`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/VirtualRouter.java):
- Main `Rib`: Active routes (like `show ip route`)
- Protocol-specific RIBs: BGP, OSPF, IS-IS, EIGRP, RIP
- Routes are imported/exported between RIBs

#### RoutingProcess
For complex protocols, a [`RoutingProcess`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/RoutingProcess.java) handles protocol logic:
- `BgpRoutingProcess`: BGP sessions, route selection, policies
- `OspfRoutingProcess`: OSPF areas, LSAs, route computation
- `IsisRoutingProcess`: IS-IS levels, TLVs
- `EigrpRoutingProcess`: EIGRP neighbors, DUAL computation
- `RipRoutingProcess`: RIP updates

### Computation Process

#### Phase 1: Initial IGP Routes

**Method**: `IncrementalBdpEngine.computeIgpDataPlane`

Each `VirtualRouter` independently computes:
1. **Connected routes**: Directly connected subnets
2. **Local routes**: Local interfaces (loopbacks, management)
3. **Static routes**: Static routes (if resolvable)
4. **OSPF intra-area routes**: Within each OSPF area
5. **RIP internal routes**: RIP-advertised routes
6. **EIGRP internal routes**: EIGRP internal routes

**Protocol propagation:**
- OSPF routes propagate between devices until **OSPF fixed point** (no new LSAs)
- RIP routes propagate until **RIP fixed point** (no new updates)
- EIGRP routes propagate until **EIGRP fixed point** (DUAL convergence)

#### Phase 2: External BGP Advertisements

Stage user-provided external BGP advertisements:
- Loaded from `external_bgp_announcements` file
- Injected into receiving `BgpRoutingProcess` objects
- Processed in first BGP iteration

#### Phase 3: Initial Topology Context

Compute topology based on routes so far:
- Layer 3 topology from IP interfaces
- OSPF adjacencies from OSPF routes
- BGP sessions from BGP configurations

#### Phase 4: Supplemental Information

Compute non-route information affecting routes:
- **Static route reachability**: Can static route next-hops be resolved?
- **IP ownership**: Which interfaces/VRFs own which IPs
- **HMM routes**: Host mobility manager routes
- **Kernel routes**: Routes from OS routing table

#### Phase 5: Topology Fixed Point (Main Loop)

This is the core of IBDP - repeat steps 5-7 until fixed point:

##### Step 5a: Routing/EGP Fixed Point

**Method**: `IncrementalBdpEngine.computeNonMonotonicPortionOfDataPlane`

**Sub-phases (one EGP round):**

1. **Compute HMM and kernel routes**: Based on current IP ownership

2. **Redistribution**: Import routes from main RIB to protocol RIBs
   - Apply routing policies (route-maps, distribute-lists)
   - Match routes: prefix, community, AS-path, etc.
   - Set attributes: local-pref, metric, next-hop, etc.
   - Add to protocol RIB if accepted

3. **Update resolvable routes**: (BGP only)
   - Check if BGP next-hop is in main RIB
   - Activate/deactivate routes accordingly
   - Affects route selection

4. **Queue cross-VRF imports**:
   - Routes to leak between VRFs
   - Stored in `VirtualRouter._crossVrfIncomingRoutes`
   - Processed in next phase

5. **Compute dependent routes**: `IncrementalBdpEngine.computeDependentRoutesIteration`

   **Dependent routes** depend on current topology/routes:
   - **Conditional static routes**: Static routes with track/interfaces
   - **Generated routes**: Aggregate routes, default routes
   - **EIGRP routes**: Summary routes, external routes
   - **IS-IS routes**: All IS-IS routes (leak levels, redistribute)
   - **OSPF external routes**: Type 1/2, redistributed routes
   - **BGP routes**: All BGP routes (iBGP, eBGP, redistributed)
   - **Cross-VRF routes**: Leaked between VRF main RIBs

6. **Oscillation check**:
   - Compute iteration hashcode
   - If seen before → `BdpOscillationException`
   - Prevents infinite loops

7. **Fixed point check**:
   - If no VRF is `dirty` → EGP fixed point reached
   - If any VRF is `dirty` → another EGP round
   - A VRF is `dirty` if it has route changes or incoming routes

##### Step 5b: Topology Recomputation

**Methods**: `computeTopology` + `computeSupplementalInformation`

Recompute topology and supplemental info based on new routing state:
- L3 topology may have changed (new links from routing)
- IP ownership may have changed
- Static route next-hop reachability may have changed

##### Step 5c: Topology Fixed Point Check

- If topology unchanged → **fixed point reached, dataplane complete**
- If topology changed → another topology iteration (back to Step 5a)
- If > `MAX_TOPOLOGY_ITERATIONS` (10) → `BdpOscillationException`

### Node Scheduling

In each routing iteration, which `VirtualRouter` objects can be processed in parallel?

#### Default: Graph Coloring

[`NodeColoredSchedule`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/schedule/NodeColoredSchedule.java):
- **Adjacent nodes** (OSPF neighbors or BGP peers) cannot run in parallel
- **Non-adjacent nodes** can run in parallel
- Computed via graph coloring algorithm
- Improves performance significantly

#### Fallback: Serialized Schedule

[`NodeSerializedSchedule`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/schedule/NodeSerializedSchedule.java):
- No parallelization
- All nodes process sequentially
- Activated if oscillation detected with graph coloring
- If still oscillates → `BdpOscillationException`

### Fixed Point Operations

#### Incremental Fixed Point

**Two dimensions of incrementality:**

1. **Routing iterations**: Within a topology iteration
   - Devices exchange routes
   - Routes propagate through protocols
   - Converge when no new routes generated

2. **Topology iterations**: Outer loop
   - Topology changes (new links discovered)
   - Supplemental info changes (IP ownership)
   - Converge when topology stable

**Why multiple fixed points?**
- Routing depends on topology (OSPF adjacencies, BGP sessions)
- Topology depends on routing (routed links, redistributed routes)
- Must iterate until both stabilize

---

## Key Data Structures

### Rib

The [`Rib`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/rib/Rib.java) class represents a routing table:

```java
class Rib {
    // Main route lookup structure
    private Map<IpPrefix, Set<Route>> _routes;

    // Protocol-specific RIBs
    private Rib _bgpRib;
    private Rib _ospfRib;
    private Rib _isisRib;
    private Rib _eigrpRib;
    private Rib _ripRib;

    // Route operations
    public void addRoute(Route route);
    public Set<Route> getRoutes(IpPrefix prefix);
    public void merge(Rib other);  // Combine two RIBs
}
```

### Route

Base class for all route types:
```java
abstract class Route {
    IpPrefix _network;
    int _admin;          // Administrative distance
    long _metric;         // Route metric
    Ip _nextHopIp;       // Next-hop IP address
    String _nextHopInterface;  // Egress interface

    // Protocol-specific attributes
    abstract int getProtocol();
}
```

**Route types:**
- `ConnectedRoute`: Directly connected networks
- `StaticRoute`: Static routes
- `OspfRoute`: OSPF routes (intra-area, inter-area, external)
- `BgpRoute`: BGP routes (iBGP, eBGP)
- `IsisRoute`: IS-IS routes
- `EigrpRoute`: EIGRP routes
- `RipRoute`: RIP routes

### ForwardingAnalysis

[`ForwardingAnalysis`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/ForwardingAnalysis.java):
- Pre-computed forwarding information
- Used by symbolic engine for reachability
- Contains:
  - `Map<String, Map<String, Fib>>`: FIBs per VRF per node
  - `ArpIpMapping`: ARP/NDP resolution
  - `InterfaceDependencies`: Which interfaces depend on which

### Fib

[`Fib`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/Fib.java):
- Forwarding Information Base
- Per-VRF forwarding table
- Maps prefix → next-hop(s)
- Supports multipath (ECMP)

---

## Route Types

### Connected Routes
- **Source**: Interface IP configurations
- **Admin distance**: 0
- **Always present**: If interface is up
- **Redistribution**: Can be redistributed into protocols

### Static Routes
- **Source**: Static route configurations
- **Admin distance**: Varies (1 for default, configurable)
- **Types**:
  - Simple static: Next-hop IP or egress interface
  - Conditional static: Track interface or IP reachability
  - Floating static: Higher admin distance, backup route

### OSPF Routes
- **Intra-area**: Type 1, within same area
- **Inter-area**: Type 2, between areas
- **External Type 1**: Redistributed (metric includes internal cost)
- **External Type 2**: Redistributed (metric doesn't include internal cost)

### BGP Routes
- **iBGP**: From internal BGP peers
- **eBGP**: From external BGP peers
- **Route selection**: Longest prefix, highest weight, local-pref, AS-path length, etc.
- **Next-hop resolution**: Must have resolvable next-hop

### IS-IS Routes
- **Level 1**: Intra-area
- **Level 2**: Inter-area
- **Level 1/2**: Both levels

### EIGRP Routes
- **Internal**: From EIGRP neighbors
- **External**: Redistributed

### RIP Routes
- **Internal**: From RIP neighbors
- **Default metric**: Hop count

---

## Oscillations and Nondeterminism

### Oscillations

**What is oscillation?**
- Network never reaches stable state
- Routes keep changing indefinitely
- Can be caused by:
  - Route feedback loops
  - Conflicting redistribution policies
  - Interaction of multiple protocols

**Detection:**
- Routing iterations: Hashcode-based detection
- Topology iterations: Max iteration limit (10)

**Result:** `BdpOscillationException` raised, no dataplane generated

**Example oscillation scenario:**
```
Router A: redistribute BGP → OSPF
Router B: redistribute OSPF → BGP
Result: Routes loop between BGP and OSPF indefinitely
```

### Nondeterminism

**What is nondeterminism?**
- Real network has multiple stable states
- Depends on tie-breaking (e.g., route arrival order)
- Examples:
  - ECMP with different next-hop selections
  - BGP route selection with equal attributes
  - OSPF external routes with equal metrics

**Batfish's approach:**
- Computation is **always deterministic**
- Same input → same output
- May not match live network's exact state
- But will represent **a** valid stable state

**Why this is acceptable:**
- Network should be designed to avoid nondeterminism
- Batfish reveals configuration issues
- Any valid state is acceptable for analysis

---

## Integration with Other Components

### With Parsing/Extraction
- Input: Vendor-independent configurations
- Used for: Initial topology, protocol configurations

### With Post-processing
- Post-processing finalizes configurations before dataplane
- Resolves interface references, validates settings

### With Topology
- Dataplane computes routing topology
- Feeds back into L3 topology (routed links)

### With Forwarding Analysis
- Dataplane produces FIBs
- Forwarding Analysis uses FIBs for packet forwarding
- Results used by symbolic engine

### With Symbolic Engine
- Symbolic engine uses DataPlane and ForwardingAnalysis
- Computes reachability, traceroute, etc.
- BDD-based analysis of all possible flows

---

## Performance Considerations

### Computational Complexity

**Worst case:** Exponential (oscillating networks)
**Typical case:** Polynomial (converges in few iterations)

**Factors affecting performance:**
1. **Network size**: More devices → more routes
2. **Protocol complexity**: BGP is slower than OSPF
3. **Redistribution**: Increases dependencies
4. **Route leaking**: Cross-VRF, cross-protocol
5. **Multipath**: ECMP increases FIB size

### Optimization Techniques

1. **Parallelization**: Graph coloring for non-adjacent nodes
2. **Incremental updates**: Only process changed routes
3. **Early termination**: Stop when fixed point detected
4. **Caching**: Reuse computed data (ARP, topology)

### Memory Usage

**Key memory consumers:**
1. **RIBs**: All routes for all protocols
2. **FIBs**: Forwarding entries (can be large with many prefixes)
3. **Topology**: All layers, all links
4. **Forwarding analysis**: Pre-computed reachability

**Typical memory:**
- Small network (< 100 devices): < 1 GB
- Medium network (100-1000 devices): 1-5 GB
- Large network (> 1000 devices): 5-20 GB

---

## Troubleshooting

### BdpOscillationException

**Symptoms:**
```
org.batfish.common.BdpOscillationException: Oscillation detected
```

**Causes:**
1. Route redistribution loop (BGP ↔ OSPF)
2. Conflicting routing policies
3. Mutual route redistribution between protocols

**Solutions:**
1. Check redistribution configurations
2. Use route tags to prevent loops
3. Add filtering to prevent feedback
4. Review route maps/distribute-lists

**Example fix:**
```c
! Bad: causes oscillation
router bgp 65000
  redistribute ospf 1

router ospf 1
  redistribute bgp 65000

! Good: tag routes to prevent loop
router bgp 65000
  redistribute ospf 1 route-map TAG_OSPF
!
route-map TAG_OSPF permit 10
  set tag 100

router ospf 1
  redistribute bgp 65000 route-map DENY_TAGGED
!
route-map DENY_TAGGED permit 10
  match tag 100
  set distance 255  ! Make route unacceptable
```

### Slow Dataplane Computation

**Symptoms:** Dataplane takes > 10 minutes

**Potential causes:**
1. Large network (thousands of devices)
2. Complex redistribution (many protocols interacting)
3. Full Internet BGP table (many routes)
4. ECMP with many next-hops

**Mitigation:**
1. Remove unused redistribution
2. Simplify routing policies
3. Filter unnecessary routes
4. Increase parallelization (more CPU)

### Missing Routes

**Symptoms:** Expected routes not in FIB

**Debug steps:**
1. Check RIBs: Are routes in main RIB?
2. Check protocol RIBs: Was route received/learned?
3. Check redistribution: Was route redistributed?
4. Check filters: Is route being filtered?
5. Check next-hop: Is next-hop resolvable?

**Example using Pybatfish:**
```python
# Check if route exists in main RIB
rib = bf.get_routes(nodes="router1", vrf="default")

# Check BGP routes specifically
bgp_rib = bf.get_bgp_routes(nodes="router1")

# Check if next-hop is resolvable
fib = bf.get_fib(nodes="router1")
```

### Incorrect Next-Hop

**Symptoms:** Route has wrong next-hop

**Causes:**
1. Route redistribution not setting next-hop
2. BGP next-hop-self not configured
3. OSPF next-hop propagation issue

**Solutions:**
1. Configure next-hop-self for iBGP
2. Use route-map to set next-hop
3. Check OSPF network type (broadcast vs point-to-point)

---

## Related Documentation

- [Topology](../topology/README.md): How topology is computed and used
- [Forwarding Analysis](../forwarding_analysis/README.md): Using the data plane for analysis
- [Symbolic Engine](../symbolic_engine/README.md): BDD-based analysis using data plane
- [Post-processing](../post_processing/README.md): Pre-processing before data plane
- [Architecture](../architecture/README.md): Overall system architecture

---

## Summary

**Key points:**
1. **IBDP algorithm**: Incremental fixed-point computation
2. **Two-level iteration**: Routing iterations within topology iterations
3. **Parallelization**: Graph coloring for performance
4. **Deterministic**: Same input always produces same output
5. **Oscillation detection**: Prevents infinite loops
6. **Reusable**: Computed once, used many times
