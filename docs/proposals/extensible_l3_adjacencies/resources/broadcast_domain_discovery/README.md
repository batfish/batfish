# Broadcast domain discovery

* Author: Dan Halperin
* Last updated: 2022-09-23
* Status: implemented, but switched off in `master`

## Background

See [L3 edge establishment and relation to L1/L2](../l3_edge_establishment_and_relation_to_l1_l2/README.md)
for key background information.

The basic challenge addressed in this document is deciding which L3 interfaces are in the same
broadcast domain, given L1 topology and L2 configuration. The solution need also be extensible to
other technologies that can bridge L2 domains, like VxLAN.

### Key concepts

TBD - what needs to go here?

L1 interface

L2 interface

L3 interface

Physical interface (Ethernet1, xe-0/0/0)

Logical subinterface (Ethernet1.5, xe-0/0/0.5)

Aggregate interface (port-channel1, ae0) and subinterfaces

Virtual interface (Vlan5, irb.5, Loopback0)

Untagged/Tagged frames

dot1q encapsulation

Access mode / Trunks

misc VxLAN concepts

### Goal

The goal of the broadcast domain discovery procedure is to identify which L3 interfaces can
communicate directly with which other L3 interfaces, aka, Ethernet frames containing IP packets
sourced by the first will be received by the second. In other words, this algorithm has the type
signature:

Tuple[Configurations, L1 Topology] -> List[Set[L3 interfaces in same broadcast domain]]

We then use these sets later in L3 topology establishment.

## Problems

The key problem is performance.

Initially, we modeled a complete graph between all (interface, vlan) pairs in the network and then
joined them into broadcast domains with union-find. The main problem with this is complexity: given
N trunks on a device, we get 4096*N^2 edges, and similar blow-ups for links across devices. This
quickly becomes infeasible.

We have since tried reducing complexity by using ranges of VLANs that have similar behavior. This
has proved challenging to get correct (Sharon
Saadon [reported on NTC Slack](https://networktocode.slack.com/archives/CCE02JK7T/p1619679666156900)
a bunch of bugs in the first version) and performant (on a large user network with complete L1
information, inferring the broadcast domains takes longer than parsing and dataplaning combined).

## Proposal

It seems to me that some of the following pieces of work we're doing may be unnecessary:

1. Pre-compute and materialize an explicit graph. This can be very large, can we access the parts we
   need on the fly?
2. Model what happens on "boring" VLANs – aka, VLANs that are not used by any L3 device. For
   example, if a trunk allows all VLANs but in practice just connects two interfaces that use Vlan
   5, we don't need to care about Vlans 1-4,6-4095.

Proposed algorithm:

1. Domains = List[]
2. Unknown = Set[All L3 interfaces]
3. While Unknown is not empty:
    1. Source = any interface in Unknown
    2. Reachable: Set[Interface] = {Source}
    3. Use any graph traversal algorithm that is robust to loops to model a broadcast packet sent
       from Source. The graph being traversed here includes many different kinds of edges:
        * "SubInterface -> Physical Interface + Tag" (Aka, Eth1.5 sends packets out Ethernet1 with
          Tag 5)
        * "Physical Interface + Tag -> Physical Interface + Tag" (Aka, Eth1 sends a packet with tag
          5, that is received at Eth2 with Tag 5)
        * "Physical Interface + Tag -> Subinterface" (Aka, Eth1 receives a packet with tag 5, and
          sends it to Eth1.5).
        * "Physical Interface + Tag -> internal switch on Vlan" (Aka, Eth1 is an access mode
          interface for vlan 7, and received an untagged frame. It is sent to the VRF-level switch
          on vlan 7).
        * "Switch &lt;> Vlan or Irb interface" (Aka, frames that reach the switch in VLAN 7 can
          reach the virtual interface Vlan7 or irb.7)
        * "Switch &lt;> Trunk + tag" (Aka, broadcast frames that reach the switch in VLAN 7 will
          also be sent out this trunk interface, with tag 7)
        * … and more. This can support VLAN as well with additional edges from Switch + Vlan.
    4. Add all L3 interfaces reached in the previous step to Reachable
    5. Unknown = Unknown \ Reachable
    6. Domains.append(Reachable).

This process should produce the same results as the current algorithm, with a relatively small
amount of working state and almost no up-front work. All of the steps during traversal can be done
on the fly for the specific VLANs or Tagged packets or VNIs being used, rather than precomputed. Any
VLAN/Switch/Interface/etc. not reachable from some L3 interface will not be processed. Etc.

## Graph Details

We’re reasoning about reachability of IP payloads through a graph. The payload is processed and
transmitted in several stages, each of which has its own internal data:

* Raw IP payload (L3 interface)
* IP payload paired with a VLAN ID (being processed inside a bridge domain of a router/switch with
  L2 configuration)
* IP payload paired with a VNI (inside VxLAN topology)
* IP payload wrapped in an Ethernet frame (on the wire)
    * Untagged
    * Tagged with 1-4095

Nodes in the graph represent these different processing stages.

Edges between nodes model the processing and/or transformation of a stage before it hands data to
the next.

* Constrain the data to be of a particular type
    * E.g. an edge from a switch to a tagged interface would only send payloads paired with the
      correct VLAN
* Transform data into the right type for the next stage
    * E.g. an edge from a physical subinterface into an l3 interface would unwrap the ethernet frame
      and remove the tag

Types of nodes:

L3Interface - start and end of travel. irb/vlan, physical subinterface.

    what it sends or receives: IP payload

    edges to: physical interface, switch

    exactly 1 edge

Physical interface - an ethernet port. also includes aggregated interfaces

    what it sends or receives: ethernet frame around IP payload. Frame may contain optional dot1q header


    no header: untagged


    header: tag \in 1-4095

    edges to: other physical interface, switch, L3 interface

    physical-physical: as many edges as in L1, likely 1 or a handful at most \
    physical-switch: edge represents translation from tags to switch vlans

    physical-L3: e.g., a subinterface. exactly 1.

Switch - represents the logical switch inside a bridge domain (Batfish currently only models 1. Plus
a separate domain for each L3 dot1q interface)

    what it sends or receives: IP Payload paired with a VLAN ID

    edges to: physical interface, L3Interface, VNI

    switch-physical: represents translation from vlans to tags

    switch-L3: L3 interface receiving packets on a specific VLAN

    switch-VNI: translation from VLAN into VNI, moving into EVPN topology

MegaSwitch - represents the logical switch connecting unclaimed L1 interfaces

    metadata: same as physical interface

    edges to: physical interface

    edges preserve untagged | valid tag

VNI - VxLAN concept used to bridge L2 domains (L2VNI)

     metadata: IP Payload paired with VNI number

     edges to: other VNI, Switch

     VNI-VNI: represent traversing the VNI topology

     VNI-VLAN: translation from VNI into VLAN, moving into L2 topology

### Simplest abstraction: explode nodes with metadata

L3Interface - 1 per interface

MegaSwitch - 1 globally

Physical interface - 4096 of them including untagged | valid tag per interface

Switch - 4095 per bridge domain (1 for now)

VNI - small fixed number

Edges have no data.

Built-in graph algorithms "just work" - traversal is trivial, visited set is just set of nodes.

Explosion in size of graph.

Vlan translation explicit in nodes + edges (Vlan10 -> SW 10 -> Eth1, no tag -> Eth2, no tag -> SW 20
-> Vlan20).

Not obvious how to handle q-in-q without again exploding the space.

### Compressed abstraction: simple nodes, edges represent transitions

L3Interface - 1 per interface

MegaSwitch - 1 globally

Physical interface - 1 per interface

Switch - 1 per bridge domain (1 for now)

VNI - small fixed number

Edges are directional and contain information about transitions. E.g.,

* L3Interface -> Switch assigns a VLAN to the IP payload.
* Switch -> Physical interface builds an Ethernet frame and optionally assigns a tag, stops tracking
  VLAN.
* Physical interface -> physical: propagates Ethernet frame as-is
* Switch -> VNI drops VLAN, assigned VNI number.
* L3Interface -> Physical interface (subinterfaces are not attached to switch)
* Physical -> Switch (convert tag to VLAN, assign VLAN for untagged, drop on floor)

Traversal is closer to traceroute - keeping track of current metadata while walking around the
graph. Visited set has to include nodes and metadata at those nodes.

Easy to handle q-in-q (double encapsulation) by tracking more data.

VLAN translation on edges from Switch->Phys and Phys->Switch.

