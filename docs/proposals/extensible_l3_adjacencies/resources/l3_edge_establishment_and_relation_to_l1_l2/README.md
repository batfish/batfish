# L3 edge establishment and relation to L1/L2

* Author: Dan Halperin
* Last updated: 2022-09-23
* Status: implemented, but switched off in `master`

## Background

Batfish has a project-wide philosophy of trying to put as little work on the user as possible.
Although real networks have wires connecting specific interfaces, it is nice for users to not need
to upload a full wiring diagram. They just upload configs and Batfish "figures it out".

The default operating mode in Batfish is that every pair of compatible L3
interfaces[<sup>1</sup>](#1) is
connected.

This approach does not work for all users, typically because:

1. it permits L3 connectivity between parts of the network that are in reality isolated in different
   physical (L1) or logical (L2) domains. This is often when users have IP reuse.
2. it does not allow for analysis of L2 mis-configurations or L1 failures. For example, setting the
   wrong encapsulation vlan, or failing a key switch that partitions the L2 domain. Since we don't
   model the L1/L2 connectivity, these failures may have no impact on L3 edge establishment.

Users who want to be able to evaluate these aspects of their network supply L1 topology information
in the form of (directed) edges between interfaces (rack1[eth1] → rack2[eth2])
.[<sup>2</sup>](#2)

The presence of these links affects L3 edge establishment as
follows [[permalink](https://github.com/batfish/batfish/blob/1cd58cfdf7e121c5d9c773a391fea0b3832a71dd/projects/batfish-common-protocol/src/main/java/org/batfish/common/topology/TopologyUtil.java#L526-L534)]:

* We use the L1 topology and L2 configuration to figure out which L3 interfaces are in the same L2
  broadcast domain, in the subset of the network with L1 edges.
* An L3 edge whether the node owning either interface "has L1 topology" is considered adjacent only
  if the two interfaces are in the same L2 broadcast domain.
* A node "has L1 topology" if it is the tail of an L1 edge (aka, rack1[<sup>3</sup>](#3)
  above).
  This procedure was designed to:


* permit some L1 topology, but not require all of it
    * For example, if A &lt;> B &lt;> C, and the A&lt;>B edge needs L1 topology, the B&lt;>C might
      not need L1 topology to be established. To achieve this, we provide data only such that A "has
      L1 topology". The AB edge will only be established if the L2 configuration permits it, but the
      BC edge will be established purely based on address compatibility.
* handle asymmetric L1 data availability. For example, only some routers even run CDP/LLDP (the
  protocols that collect L1 data in practice). This provides a view from rack1's perspective of what
  other devices are connected to each of its interfaces – we don't know anything about rack2's other
  interfaces – so we honor directionality of this data and only force rack1's interfaces to have L2
  adjacency.

## Problems

#### Users can't understand which L1 data to supply

The current implementation in practice makes it pretty hard for users to understand what a partial
L1 configuration will do. For example, suppose I have a simple network with no IP reuse, and I want
to make sure that when I fail the switch the L3 edge will go down:

A[eth0,1.2.3.4/24] &lt;> [eth0 access 5]SW[eth1 access 5] &lt;> B[eth0, 1.2.3.5/24]

Which L1 edges do I need to add?

We might hope that just the edges from the SW will do it – it's the pure L2 device after all, and
all L2 use in the network is surrounding it. Unfortunately, that means that only SW will "have L1
topology", so that the L3 adjacency check between A and B will not consider broadcast domains.

Instead, we have to supply L1 data with A and B as the tail, and we have to populate L1 edges for
any other interfaces those nodes have.

→ It would be nice if we only needed to supply L1 info for edges used in L2 domains.

→ It would be nice if directionality didn't matter.

#### Nonsense L3 adjacencies between virtual and logical interfaces

1/ Consider the following network:

A[eth1.5, encapsulation dot1q 5, 1.2.3.4/24]

B[eth1.6, encapsulation dot1q 6, 1.2.3.5/24]

We assume that the L3 edge between these two interfaces will come up. However, this is likely to be
a false positive; different VLANs are probably not bridged except in rare cases. Instead, VLANs are
used for isolation and reuse.

2/ Consider a device with [Vlan5, no autostate, 1.2.3.4/24] and no physical interfaces active. Even
though Vlan5 is up, the fact that there are no physical interfaces active means that Vlan5 can't
communicate with any other device. This matter is completely ignored today.

→ It's not clear that treating all virtual or logical interfaces as in the same broadcast domain
makes sense.

#### (Minor) Engineering: essentially disjoint code paths

Most users do not use L1 data, and the code is much less well understood / well tested. We have a
hard time understanding real performance implications, some recent simplifications resulted in
correctness regressions, and recent correctness fixes destroyed performance again.

→ It would be better if this code was more used and more tested on a daily basis.

→ But, this code is a source of perf issues. Not doing it (all interfaces default to same domain) is
nice, in the common L3-only case.

→ But we have reason to believe that many users have extensive L1 use in their
networks.

_Aside: We have other plans [[link](../broadcast_domain_discovery/README.md)] to reimplement the
current L2 broadcast
domain identification. This should scale much better than current impl, we believe._

## Proposal

The proposal would be to rethink the default model.

1. Instead of "all L3 interfaces are in the same domain", it would be "all physical interfaces are
   connected to a 'MegaTrunk'". The MegaTrunk will send any frame received (preserving untagged or
   tag) to all other interfaces. \
   We'll then use whatever L1->L3 inference algorithm we have to connect up L3 interfaces.
2. Any L1 edge in the provided topology, regardless of direction, is treated as disconnecting both
   endpoints from the MegaTrunk, and wiring them together. (It seems okay to assume no interface is
   wired directly to multiple other interfaces, but without thinking hard I guess we could support
   this by just allowing multiple edges.)

I think this resolves all the above problems.

* Unless some device in the network does vlan remapping, Eth1.5 and Eth1.6 won't be connected.
* Inferring L3 adjacency given L1 requires virtual and logical interfaces to go over a wire, so
  Vlan5 even if forced up can't form adjacencies if all physical devices are down.
* The same codepath is used for all networks.
* and I think we no longer need to care about directionality or provide "more than minimally
  necessary" edges. And we can always understand the impact of failing an interface.

## Notes

#### 1

Compatible means same network, different IP addresses, not network or broadcast IP. 1.2.3.4/24 and
1.2.3.5/24, but not 1.2.3.0/24 or 1.2.3.255/24. /31s are special of course.

#### 2

We expect these are physical interfaces, but I believe nothing enforces this.

#### 3

The "tail" of an arrow is the flat side, the "head" is the pointy side. Think "arrowhead".

