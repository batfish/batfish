# Extensible frame-aware layer-3 adjacencies model

* Author: Ari Fogel
* Last updated: 2022-09-21
* Status: In progress [[Draft PR](https://github.com/batfish/batfish/pull/8246)]

## Background and motivation

Handling of ethernet traffic is accomplished in various ways across vendors:

* IOS, NX-OS, Arista
    * Physical or aggregate non-sub-interfaces may be configured as switchports. There are (ignoring
      extra features) trunk and access ports. Trunk ports have a native vlan, which is the vlan
      assumed for untagged packets. They allow a range of vlans, which may include the native vlan (
      if not, untagged frames are dropped). Access ports assume a traffic is in a specific access
      vlan, and accept untagged frames (and maybe sometimes frames tagged with the access vlan. Need
      to verify). Traffic from a particular vlan can be modeled as being forwarded out all access
      ports for that vlan, and all trunk ports allowing that vlan.
    * Physical or aggregate layer-3 (sub-)interfaces may be configured for a specific encapsulation
      vlan. Traffic for that vlan is consumed by the interface, and not bridged.
    * A "Vlan" layer-3 interface may be configured for a specific VLAN. L3 Traffic received for that
      vlan destined to an IP address on the Vlan interface is consumed by it. All other traffic is
      forwarded out appropriate access and trunk ports.
* Juniper
    * Similar to IOS and NX-OS, except that L2 trunk/access configuration is configured on a
      subinterface of a physical or aggregate interface
    * Juniper has "irb" interfaces instead of "Vlan" interfaces that perform the same function.
    * A layer-3 sub-interface may also be configured to accept traffic for a particular VLAN and not
      bridge it.
    * Juniper also has bridge domains
* IOS-XR
    * Bridging only happens between interfaces in the same bridge domain. A (sub-)interface in a
      bridge domain must be configured as "l2transport". Its configuration must include the
      conditions for traffic it will accept, and may include actions to take on the tagging of the
      frame in the incoming and outgoing directions. Either a parent physical or aggregate interface
      may be configured as l2transport, or subinterfaces of such an interface may be configured as
      such. Multiple subinterfaces of the same parent interface may be in the same bridge domain. In
      that case, the subinterface receiving the frame will be that with the most specific applicable
      match condition. This matters, because it determines which actions will be taken on the tags.
    * A bridge domain may include a routed L3 "BVI" interface. Traffic will only be consumed by the
      routed interface if it is L3 and matches one of its IPs, and all tags have been removed on
      ingress. Traffic not consumed will be forwarded out the other interfaces in the bridge domain.
    * Traffic bridged to another interface will have the inverse of the incoming action applied, if
      such an action was configured with the "symmetric" flag.
* Cumulus
    * interfaces may be added to a vlan-aware or non-vlan-aware bridge.
    * One vlan aware bridge may be configured for a range of vlans and a native vlan. All physical
      or aggregate interfaces in the bridge act as trunks with the same configuration as the bridge,
      unless they have overridden configuration. Such overridden configuration may be for trunking
      with a subset of the bridge vlans, or access mode for a single vlan of the bridge vlans. An
      interface may also be configured to drop untagged frames. A "vlan" layer-3 interface may be
      configured for a given VLAN. It functions like a Cisco "Vlan" interface. A vlan interface
      belongs to the vlan-aware bridge. Traffic for a given vlan not destined to a vlan interface is
      mirrored out all members of the bridge configured to carry traffic for that vlan (access ports
      and trunk ports).
    * One or more non-vlan-aware bridges may be configured for a single vlan each. Physical or
      aggregate interfaces or sub-interfaces may be added to a non-vlan-aware bridge. The bridge
      itself may have L3 configuration. Traffic not destined to the bridge interface is mirrored out
      the members of the bridge.
    * CLAG (Cumulus MLAG) is usually configured on a "peerlink" interface, plus a "peerlink.4094" L3
      subinterface. The peerlink interface itself is typically added to a bridge that does not
      include vlan 4094 so that traffic for vlan 4094 is not bridged from other interfaces. That is,
      the "peerlink" interface is typically a trunk, while the "peerlink.4094" subinterface is an L3
      interface on the same physical medium.
* A10? Fortinet?

### Current support and limitations

Except for IOS-XR and bits of Cumulus, Batfish has some existing support for bridging configuration
via the old L3 adjacencies algorithm. Some of this support is perhaps accidental, relying on L3
inference where certain configuration paradigms are employed.

The new L3 adjacencies algorithm only really supports the IOS/NX-OS/Arista model, some parts of
Cumulus (excluding e.g. full modeling of an L2 peerlink trunk with L3 subinterface), and has hacky
support for Juniper where L2 configuration is done on a subinterface. There is currently no notion
of separate bridge domains within a device.

Neither algorithm supports any form of Q-in-Q. VLAN translation on send/receive from L2 interfaces
is also not supported, but could be added easily to the new algorithm if modeled in VI.

### New L3 adjacencies overview (existing, not proposed)

L3 adjacencies are computed based on a search originating from each L3 interface in a graph
containing:

(This section omits a fairly straightforward extension to L2 VXLAN, but that does not affect this
document.)

### Nodes

* L1Interface
* L3Interface
* EthernetHub
    * Optional global hub for interfaces not mentioned in L1 topology
    * One hub for each connected component of L1 interfaces in the L1 topology
* DeviceBroadcastDomain - one per device

### Edges

* L3Interface -> DeviceBroadcastDomain
    * IRB sends to device internal switch with a given vlan
* L3Interface -> PhysicalInterface
    * sends out physical interface with an optional tag
* PhysicalInterface -> DeviceBroadcastDomain
    * access mode interface sends to internal device switch with access vlan
    * trunk mode interface sends to internal device switch in vlan corresponding to received tag (or
      native vlan if received untagged)
* DeviceBroadcastDomain -> PhysicalInterface
    * internal switch sends out an access mode interface with no tag
    * internal switch sends out a trunk interface with vlan ID in tag (except native vlan)
* PhysicalInterface -> EthernetHub
    * send to hub with outgoing tag
* EthernetHub -> PhysicalInterface
    * Send to all non-source interfaces with tag

The graph is produced from logical (?) L1 topology, interface switchport properties (trunk/access
settings), and interface dependencies. Notably, there is no concept of L2 interfaces separate from
physical interfaces. But at least in IOS-XR, we need to support multiple l2 subinterfaces per
physical interface (as well as having both l2 and l3 subinterfaces on a physical interface). And we
also need another mechanism in VI to represent the granularity of operations available on
l2transport interfaces in XR.

## Problems

* The new L3 adjacencies algorithm does not support multiple L2 (sub-)interfaces nor combinations of
  L2 and L3 interfaces off the same physical/aggregate interface.
* Existing VI structures are insufficient to model IOS-XR L2 behavior even with a single L2
  interface per physical interface, e.g. arbitrary/asymmetric tag rewrites
* As a result of the above:
    * L3 adjacencies are not correctly computed for IOS-XR devices with bridge domains, which is
      using and must continue to use the new algorithm.
    * Cumulus peerlink interfaces (L2 parent interface, L3 subinterface) are not properly modeled
* Independent of IOS-XR, the new l3 adjacencies algorithm represents a regression for some networks
  and causes some tests at master to fail

## Goals

* Update the VI model so it can represent the L2 interfaces and operations used by IOS-XR
* Update the new L3 adjacencies algorithm to support:
    * combinations of L2/L3 (sub-)interfaces
    * IOS-XR longest tag match and rewrite (a)symmetric primitives

### Non-goals

The following are out of scope:

* Supporting Q-in-Q (802.1ad) encapsulation
* Backporting any new support to the old L3 adjacencies algorithm

## Proposed changes

**In this section, "tag" refers to a concrete tag or no tag.**

Which (sub-)interface(s) receive a frame/packet needs to be determinable from VI information. The
new logic/structure may be split between the VI model and the L3 adjacencies computer.

At minimum, the VI model needs to be able to represent:

* bridge domains and their member L2/L3 (sub-)interfaces
* conditions on Ethernet traffic received by a physical/aggregate interface for which traffic should
  be handled by each L2/L3 (sub-)interface
* ingress tag transformations performed by each L2 (sub-)interface before sending to a bridge domain
* conditions on which a bridge domain sends traffic to an L2/L3 (sub-)interface
* egress tag transformations performed when sending from bridge domain to an L2 (sub-)interface

### Model changes

#### Adjacencies computation model

Edges are directed. Each node stores its out-edges.

##### Internal nodes

Nodes of the following types are determined from each individual post-processed Configuration

* L1Interface (non-terminal/original)
    * NodeInterfacePair interface
    * logical layer-1 interface, i.e. physical/aggregated/redundant
* Layer2Interface (non-terminal/original)
    * NodeInterfacePair interface
* VlanAwareBridgeDomain (non-terminal/original)
    * BridgeId id (hostname + bridge name)
* NonVlanAwareBridgeDomain (non-terminal/original)
    * BridgeId id (hostname + bridge name)
* Layer3Interface (terminal/original)
    * L3NonBridgedInterface
        * NodeInterfacePair interface
    * L3BridgedInterface
        * NodeInterfacePair interface
* L2Vni
    * VxlanNode vxlanNode

##### External nodes

These nodes and their respective edges are determined from topology input

* L1Hub
    * Determined from logical L1 topology
    * NodeInterfacePair representative
* L2VniHub
    * Determined from VxLAN topology
    * VxlanNode representative

##### Edges

* Edge types are based on source and destination node type
* Edges from or to an external node can be considered external edges
* Edges from and to an internal node can be considered internal edges
* All edges are identified with a partial function on TagStack X VlanId.
    * Search should terminate for inputs outside the co-domain.
* Consider vlan 0 to mean no vlan id.
* Consider position 0 to be the outer position in the TagStack, and where a pushed tag goes.

Edge functions:

* Identity
    * maps (tagStack, vlanId) -> (tagStack, vlanId)
* Compose(func1, func2)
    * maps (tagStack, vlanId) -> func1(func2((tagStack, vlanId))
* FilterByOuterTag(IntegerSpace allowedOuterTags, boolean allowUntagged)
    * maps (tagStack, vlanId) -> (tagStack, vlanId) if
        * allowUntagged and tagStack empty
        * allowedOuterTags contains tagStack[0]
* FilterByVlanId(IntegerSpace allowedVlans)
    * maps (tagStack, vlanId) -> (tagStack, vlanId) if vlanId in allowedVlans
* AssignVlanFromOuterTag(nullable integer nativeVlan)
    * maps ([], vlanId) -> ([], nativeVlan) if nativeVlan non-null
    * maps (tagStack, vlanId) -> (tagStack[1..], tackStack[0])
* PopTag(int count)
    * maps (tagStack, vlanId) -> (tagStack[count..], vlanId)
* TranslateVlan(TotalIntFunction translations)
    * maps (tagStack, vlanId) -> (tagStack, translations(vlanId])
* ClearVlan
    * maps (tagStack, vlanId) -> (tagStack, 0)
* PushVlanId(int exceptVlan)
    * maps (tagStack, vlanId) ->
        * ([vlanId] + tagStack, vlanId) if vlanId != exceptVlan
        * (tagStack, vlanId) if vlanId == exceptVlan
* PushTag(int tag)
    * maps (tagStack, vlanId) -> ([tag] + tagStack, vlanId)
* SetVlanId(toVlanId)
    * maps (tagStack, vlanId) -> (tagStack, toVlanId)

Constructing edges:

* To construct trunk edges
    * for L1Interface -> trunk (Layer2Interface)
        * compose
            * FilterByOuterTag(allowedVlans, allowUntagged)
            * AssignVlanFromOuterTag(nativeVlan)
    * for trunk (Layer2Interface) -> VlanAwareBridgeDomain
        * TranslateVlan(translations)
    * for VlanAwareBridgeDomain -> trunk (Layer2Interface)
        * Compose
            * TranslateVlan(translations)
            * FilterByVlanId(allowedVlans)
    * for trunk (Layer2Interface) -> L1Interface
        * Compose
            * PushVlanId(exceptVlan)
            * ClearVlanId
* To construct access port edges
    * for L1Interface -> access port (Layer2Interface)
        * FilterByOuterTag({}, true)
    * for access port (Layer2Interface) -> VlanAwareBridgeDomain
        * SetVlanId(accessVlan)
    * for VlanAwareBridgeDomain -> access port (Layer2Interface)
        * FilterByVlanId({accessVlan})
    * for access port (Layer2Interface) -> L1Interface
        * ClearVlanId
* To construct l2transport interface edges
    * for L1Interface -> l2transport (Layer2Interface) (igoring 802.1ad)
        * FilterByOuterTag(allowedTag)
    * for l2transport (Layer2Interface) -> NonVlanAwareBridgeDomain
        * PopTag(1) or identity
    * for NonVlanAwareBridgeDomain -> l2transport (Layer2Interface)
        * Identity
    * for l2transport (Layer2Interface) -> L1Interface
        * PushTag(tag) or Identity
* To construct non-bridged layer-3 interface edges
    * for L1Interface -> L3NonBridgedInterface (terminal edge)
        * FilterByOuterTag([{tag}, false) or FilterByOuterTag({}, true)
    * for NonBridgedLayer3Interface -> L1Interface (origination edge)
        * PushTag(tag) or Identity
* To construct bridged layer-3 interface edges
    * for VlanAwareBridgeDomain -> L3BridgedInterface (terminal edge)
        * FilterByVlanId({{bdVlanId}) for junos bridged or IRB/Vlan interface
    * for NonVlanAwareBridgeDomain -> L3BridgedInterface
        * FilterByOuterTag({}, true) for BVI
    * for L3BridgedInterface to VlanAwareBridgeDomain (origination edge)
        * SetVlanId(irbVlan) for IRB/Vlan
    * for L3BridgedInterface to NonVlanAwareBridgeDomain (origination edge)
        * Identity for BVI
* To construct vni edges:
    * for VlanAwareBridgeDomain -> L2Vni
        * compose
            * FilterByVlanId(vlan)
            * ClearVlanId
    * for L2Vni -> VlanAwareBridgeDomain
        * SetVlanId(vlan)
    * for NonVlanAwareBridgeDomain -> L2Vni
        * IOS-XR, Juniper bridge, Cumulus non-vlan-aware bridge
        * identity
    * for L2Vni -> NonVlanAwareBridgeDomain
        * identity

#### VI model changes

We will preserve existing switchport settings as informational.

There are several options for changes to Configuration, Interface, etc.:

##### Add new unified VI properties for bridge domains, l2transport, and legacy settings

* Precompute and store all data necessary to compute graph during conversion
    * Everything that does not rely on post-processing, i.e. don't include stuff dependent on final
      active status or structures that may be pruned
* Compute intra-device edges during conversion
* Compute the intra-device nodes during topology computation
    * takes into account final active status
    * Eventually, may want to precompute at end of post-processing, but will need to be extra
      careful how we update based on active status changes, other changes in tests
* Compute the inter-device graph during topology computation
* Add new class InterfaceTopologyData
    * added to Interface
    * Fully populated during conversion
* Add new class DeviceTopology
    * for v0, computed during topology computation, but as noted above may eventually make a
      Configuration property and compute and end of post-processing
* Layer2Settings InterfaceTopologyData.layer2Settings
    * add helpers to compute from traditional switchport settings to aid in migration
    * String physicalInterface
    * L2ToL1 toL1
    * L1ToL2 fromL1
    * Layer2BridgeSettings bridgeSettings
        * one of:
            * Layer2VlanAwareBridgeSettings
                * String vlanAwareBridgeDomain
                * L2ToVlanAwareBridgeDomain toBridgeDomain
                * VlanAwareBridgeDomainToL2 fromBridgeDomain
            * Layer2NonVlanAwareBridgeSettings
                * String nonVlanAwareBridgeDomain
                * L2ToNonVlanAwareBridgeDomain toBridgeDomain
                * NonVlanAwareBridgeDomainToL2 fromBridgeDomain


* Layer3Settings InterfaceTopologyData.layer3Settings
    * one of:
        * Layer3VlanAwareBridgeSettings
            * for VLAN interfaces
            * String vlanAwareBridge
            * L3ToVlanAwareBridgeDomain toBridgeDomain
                * e.g. set vlan
            * VlanAwareBridgeDomainToL3 fromBridgeDomain
                * e.g. filter by vlan
        * Layer3NonVlanAwareBridgeSettings
            * for IOS-XR/Juniper routed-interface, linux bridge interface
            * String nonVlanAwareBridge
                * for linux bridges, will be name of self
                * for IOS-XR/Juniper bridges, will be name of bridge
            * L3ToNonVlanAwareBridgeDomain toBridgeDomain
                * e.g. pop tag
            * NonVlanAwareBridgeDomainToL3 fromBridgeDomain
                * e.g. push tag
        * Layer3NonBridgedSettings
            * String l1Interface
            * for non-BRIDGE l3 interfaces
            * L3ToL1 toL1
                * e.g. set tag
            * L1ToL3 fromL1
                * e.g. filter by tag
* Layer2Vni
    * Instead of just having vlan property, have both of vlan and vlanAwareBridgeDomain or just
      nonVlanAwareBridgeDomain

##### Store adjacencies model classes directly in Configuration

* On each node, compute and store its inner L3 adjacencies graph.
    * Add a VendorConfiguration function computeInternalL3AdjacenciesGraph which each vendor must
      implement to produce the appropriate nodes and edges
    * Call at end of post-processing
    * Add a common helper implementation that can produce this graph from VI
      InterfaceType/switchport settings for applicable vendors
* Store interface type nodes under their respective Interface
* Store L2Vni nodes under Configuration
* Store BridgeDomain nodes under Configuration
* Initialize graph as union of all internal nodes/edges from all Configurations
* During adjacencies computation, only need to compute external nodes and edges

Pros:

* Provides persistent internal bridge domain model
    * Can be used after the fact to answer questions like "which is this interface's physical
      interface?"
* No need to convert VS data to intermediate structures that are later used by adjacencies
  computation
* No need to read Configurations at all during adjacencies computation

Cons:

* Developers will need to somehow populate the internal graphs for tests that construct VI
  Configurations
* More persistent memory usage
* Graphs are not legibly Jackson serializable, if that matters

## Resources

* [L3 edge establishment and relation to L1/L2](resources/l3_edge_establishment_and_relation_to_l1_l2/README.md)
* [L2 Switching Configuration to XR L2VPN Configuration Conversion Process](https://www.cisco.com/c/en/us/support/docs/routers/asr-9000-series-aggregation-services-routers/116500-problemsolution-product-00.html)
* [VLAN-aware Bridge Mode | Cumulus Linux 4.2](https://docs.nvidia.com/networking-ethernet-software/cumulus-linux-42/Layer-2/Ethernet-Bridging-VLANs/VLAN-aware-Bridge-Mode/)
* [Multi-Chassis Link Aggregation - MLAG | Cumulus Linux 4.2](https://docs.nvidia.com/networking-ethernet-software/cumulus-linux-42/Layer-2/Multi-Chassis-Link-Aggregation-MLAG/)
* [(Juniper) Configuring a Bridge Domain](https://www.juniper.net/documentation/us/en/software/junos/bridging-learning/topics/task/layer-2-services-bridge-domains-configuring.html)
