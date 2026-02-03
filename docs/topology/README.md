# Topology

In Batfish, topology computation is the process of discovering and modeling the physical and logical connections between network devices. Topology information is critical for accurate data plane computation and forwarding analysis.

## Overview

Batfish computes topology at multiple layers:

1. **Layer 1 (Physical)**: Physical connections between interfaces
2. **Layer 2 (Data Link)**: Broadcast domains (VLANs, switching)
3. **Layer 3 (Network)**: IP routing adjacencies
4. **Overlay**: Virtual networks (VXLAN, tunnels, VPNs)
5. **Protocol**: Routing protocol sessions (BGP, OSPF, etc.)

## Topology Container

The central interface for accessing all computed topologies is **`TopologyContainer`**:

**Location:** `projects/common/src/main/java/org/batfish/common/topology/TopologyContainer.java`

```java
public interface TopologyContainer {
    Layer1Topologies getLayer1Topologies();
    Layer2Topology getLayer2Topology();
    Topology getLayer3Topology();
    VxlanTopology getVxlanTopology();
    BgpTopology getBgpTopology();
    OspfTopology getOspfTopology();
    // ... other protocol topologies
}
```

## Layer 1 (Physical) Topology

Layer 1 topology represents physical connections between network interfaces.

### Layer1Topology Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/Layer1Topology.java`

**Represents:** Physical connections between interfaces

**Data Structure:** Directed graph using Guava's `ValueGraph`

**Nodes:** `Layer1Node` (hostname + interface name)

**Edges:** `Layer1Edge` with properties:
- `int bandwidth`: Link bandwidth
- `boolean active`: Whether the link is active

### Layer1Topologies Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/Layer1Topologies.java`

Provides multiple views of L1 topology:

| Method | Description |
|--------|-------------|
| `getUserProvidedL1()` | User-defined physical topology from topology files |
| `getSynthesizedL1()` | Automatically generated edges (vendor-specific, ISP modeling) |
| `getLogicalL1()` | Physical interfaces replaced by aggregate interfaces |
| `getActiveLogicalL1()` | Only active interfaces, bidirectional edges |
| `getCombinedL1()` | All edges from user-provided and synthesized topologies |

### Layer1TopologiesFactory

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/Layer1TopologiesFactory.java`

**Key responsibilities:**
- Canonicalizes user-provided topology nodes to match actual configurations
- Converts physical interfaces to logical (aggregate) interfaces
- Validates interface existence
- Creates normalized views

### Input Sources

**User-provided topology:**
- JSON or YAML files defining physical connections
- Format: `{"node1_interface1": ["node2_interface1"]}`

**Synthesized topology:**
- Inferred from vendor-specific configurations
- Uses patterns like cable connections, CDP/LLDP information
- ISP and special network modeling

## Layer 2 (Data Link) Topology

Layer 2 topology represents broadcast domains (VLANs) and switching behavior.

### Layer2Topology Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/Layer2Topology.java`

**Represents:** Broadcast domains (VLANs)

**Data Structure:** Uses Union-Find (Disjoint Set Union) to group interfaces

**Nodes:** `Layer2Node` (hostname + interface name)

**Key method:**
```java
boolean inSameBroadcastDomain(Layer2Node n1, Layer2Node n2)
```

**Algorithm:** Union-Find with path compression and union by rank

### Computation Process

1. **Analyze interface configurations** for switchport modes
2. **Group trunk interfaces** by shared VLAN ranges
3. **Create broadcast domains** for access ports
4. **Union operations** to merge connected interfaces

**Example:**
```
Interface1 (VLAN 10) + Interface2 (VLAN 10) → Same broadcast domain
Interface3 (VLAN 20) → Different broadcast domain
```

## Layer 3 (IP/Routing) Topology

Layer 3 topology represents IP routing adjacencies between devices.

### Topology Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/Topology.java`

**Represents:** L3 adjacencies (router connections)

**Data Structure:** Set of `Edge` objects between `NodeInterfacePair` objects

**Key methods:**
```java
Set<Edge> getEdges()
Topology pruneEdges(Set<String> nodes, Set<String> interfaces)
```

### L3Adjacencies Interface

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/L3Adjacencies.java`

Defines behavior for determining L3 adjacencies:

**Key methods:**
- `boolean inSameBroadcastDomain(NodeInterfacePair n1, NodeInterfacePair n2)`
- `boolean inSamePointToPointDomain(NodeInterfacePair n1, NodeInterfacePair n2)`

### L3AdjacencyComputer Class

**Location:** `projects/batfish/src/main/java/org/batfish/datamodel/tunnel/L3AdjacencyComputer.java`

**Core algorithm** for computing broadcast domains across different network types:

**Key components:**
- `DeviceBroadcastDomain`: Per-device broadcast domain computation
- `PhysicalInterface`: Handles physical interface connections
- `L3Interface`: Manages layer 3 interfaces and their adjacencies
- `EthernetHub`: Represents traditional Ethernet broadcast domains
- `L2Vni`: Handles VXLAN VNI-based domains

**Process:**
1. For each interface, determine potential L3 neighbors
2. Check if interfaces share L2 broadcast domain
3. Check if interfaces are point-to-point connected
4. Create edges for valid L3 adjacencies

## Overlay Topologies

Overlay topologies represent virtual networks built on top of physical infrastructure.

### VxlanTopology Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/VxlanTopology.java`

**Represents:** VXLAN overlay topology

**Nodes:** `VxlanNode` (hostname + VTEP interface + VNI)

**Edges:** Compatibility between VNIs and endpoints

**Data Structure:** Undirected graph

### TunnelTopology Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/topology/TunnelTopology.java`

**Represents:** Tunnel endpoints and connections

**Includes:**
- GRE tunnels
- IPsec VPN tunnels
- Other tunnel types

**Key feature:** Prunes edges based on tunnel compatibility

### Other Overlay Types

- **Layer2Vni / Layer3Vni**: VXLAN virtual network interfaces
- **IpsecTopology**: VPN-specific topology
- **VxlanTopology**: EVPN-VXLAN overlay networks

## Protocol Topologies

Protocol topologies represent routing protocol sessions and adjacencies.

### BgpTopology Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/bgp/BgpTopology.java`

**Data Structure:** `ValueGraph<BgpNode, BgpEdge>`

**Nodes:** `BgpNode` (hostname + IP + VRF)

**Edges:** `BgpEdge` with session properties:
- Remote AS number
- Session type (eBGP/iBGP)
- Local and remote IPs

### OspfTopology Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/ospf/OspfTopology.java`

**Data Structure:** Graph with OSPF neighbor relationships

**Key information:**
- OSPF area assignments
- Neighbor adjacencies
- Interface costs

### Other Protocol Topologies

- **EigrpTopology**: EIGRP neighbor relationships
- **IsisTopology**: IS-IS adjacencies
- **RipTopology**: RIP peerings

## Topology Computation Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     Network Configurations                       │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Layer 1 Topology Computation                       │
│  - User-provided topology                                       │
│  - Synthesized topology                                         │
│  - Logical interface aggregation                                │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Layer 2 Topology Computation                       │
│  - Broadcast domain computation (Union-Find)                    │
│  - VLAN grouping                                                │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Layer 3 Topology Computation                       │
│  - L3 adjacency computation                                    │
│  - Point-to-point detection                                     │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Overlay Topology Computation                       │
│  - VXLAN topology                                               │
│  - Tunnel topology                                              │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Protocol Topology Computation                      │
│  - BGP sessions                                                 │
│  - OSPF adjacencies                                             │
│  - Other protocols                                              │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│                    TopologyContainer                            │
│              (All Computed Topologies)                          │
└─────────────────────────────────────────────────────────────────┘
```

## Key Algorithms and Data Structures

### Union-Find (Disjoint Set Union)

**Used in:** Layer 2 topology computation

**Purpose:** Efficiently manage connected components

**Features:**
- Path compression for faster lookups
- Union by rank for balanced trees
- Near-constant time complexity for operations

### Graph Representation

**Library:** Google Guava Graph

**Types used:**
- `ValueGraph<Node, Edge>`: Graphs with edge properties
- `ImmutableGraph`: Thread-safe, consistent graphs

**Benefits:**
- Efficient graph algorithms
- Immutable snapshots
- Type-safe operations

### Broadcast Domain Computation

**Algorithm:** `L3AdjacencyComputer`

**Handles:**
- Traditional Ethernet domains
- VXLAN VNIs
- Point-to-point links
- Subinterfaces and VLANs
- Trunk connections with VLAN ranges

**Complexity:** Linear in number of interfaces

### Topology Pruning

**Purpose:** Remove edges based on constraints

**Criteria:**
- Blacklisted nodes/interfaces
- Failed tunnels
- Non-reachable endpoints
- Inactive connections

## Integration with Data Plane

Topology information feeds into data plane computation:

1. **L1 topology** determines which interfaces can communicate
2. **L2 topology** determines broadcast domains for ARP
3. **L3 topology** provides initial adjacencies for routing
4. **Protocol topologies** seed routing protocol computation

## Topology in Forwarding Analysis

Forwarding analysis uses topology to:
- Determine ARP behavior
- Compute flow dispositions at network edges
- Identify neighbor unreachability
- Handle incomplete topology scenarios

See [Flow Dispositions](../flow_dispositions/README.md) for how topology affects disposition assignment.

## Key Classes Summary

| Class | Location | Purpose |
|-------|----------|---------|
| `TopologyContainer` | `projects/common/.../topology/` | Central interface for all topologies |
| `Layer1Topology` | `projects/common/.../topology/` | Physical connections |
| `Layer2Topology` | `projects/common/.../topology/` | Broadcast domains |
| `Topology` | `projects/common/.../topology/` | L3 adjacencies |
| `VxlanTopology` | `projects/common/.../topology/` | VXLAN overlay |
| `BgpTopology` | `projects/common/.../bgp/` | BGP sessions |
| `OspfTopology` | `projects/common/.../ospf/` | OSPF adjacencies |
| `L3AdjacencyComputer` | `projects/batfish/.../tunnel/` | L3 adjacency computation |

## Related Documentation

- [Data Plane](../data_plane/README.md): How topology is used in data plane computation
- [Forwarding Analysis](../forwarding_analysis/README.md): How topology affects packet forwarding
- [Flow Dispositions](../flow_dispositions/README.md): Dispositions based on topology
- [Post-processing](../post_processing/README.md): Topology-based interface processing
