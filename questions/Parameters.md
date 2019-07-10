## Grammar for rich parameter types   

Batfish questions support parameters with rich specifications for nodes, interfaces etc. The grammar for parameter types is described below. Before reading those grammars, we recommend reading the general notes. 

For many parameters types, there is a "resolver" question that may be used to learn what a given specification expands to. For instance, `resolveNodeSpecifier` is the resolver for `nodeSpec`, and `bfq.resolveNodeSpecifier(nodes="/bor/")` (Pybatfish syntax) will return the set of nodes that match `/bor/`. 

* [`applicationSpec`](#application-specifier)

* [`bgpPeerPropertySpec`](#bgp-peer-property-specifier)

* [`bgpProcessPropertySpec`](#bgp-process-property-specifier)

* [`bgpSessionCompatStatusSpec`](#bgp-session-compat-status-specifier)

* [`bgpSessionStatusSpec`](#bgp-session-status-specifier)

* [`bgpSessionTypeSpec`](#bgp-session-type-specifier)

* [`dispositionSpec`](#disposition-specifier)

* [`filterSpec`](#filter-specifier)

* [`interfacePropertySpec`](#interface-property-specifier)

* [`interfaceSpec`](#interface-specifier)

* [`ipSpec`](#ip-specifier)

* [`ipProtocolSpec`](#ip-protocol-specifier)

* [`locationSpec`](#location-specifier)

* [`mlag-id-specifier`](#mlag-id-specifier)

* [`namedStructureSpec`](#named-structure-specifier)

* [`nodePropertySpec`](#node-property-specifier)

* [`nodeSpec`](#node-specifier)

* [`routingPolicySpec`](#routing-policy-specifier)

* [`vxlanVniPropertySpec`](#vxlan-vni-property-specifier)

## General notes on the grammar 

* **Set operations:** Specifiers denote sets of entities (e.g., nodeSpec resolves to a set of nodes). In many cases, the grammar allows for union, intersection, and difference of such sets, respectively, using `,`, `&`, and `\`. Thus, `(node1, node2)\node1` will resolve to `node1`.

* **Escaping names:** Names of entities such as nodes and interfaces must be double-quoted if they begin with a digit (0-9), double quote ('"'), or slash ('/'), or they contain a space or one of `,&()[]@!#$%^;?<>={}`. Thus, the following names are legal: 
  * `as1border1` (no quotes)
  * `as1-border1`
  * `"as1border1"` (quotes unnecessary, but OK)
  * `"1startsWithADigit"` (quotes needed)
  * `"has space"`
  * `"has["`

* **Regexes:** Regular expressions must be enclosed by `/`s like `/abc/`. Batfish uses [Java's syntax and semantics](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum) for regular expressions. For simple expressions, this language is similar to others. For example:
  * `/abc/`, `/^abc/`, `/abc$/` match strings strings containing, beginning with, and ending with 'abc'
  * `/ab[c-d]/` and `/ab(c|d)/` match strings 'abc' and 'abd'.

* **Set of enums** Many parameter types such as `applicationSpec` or `mlagIdSpec` represent a set of values. Such parameters share a common grammar, though with different base values. Example expressions for this grammar are `val1`, `/val.*/` and `val1, val2`, which respectively, refer to the value "val1", all values that match "val.*", and a set with two values. 

   The specification of this grammar is:

   <pre>
   enumSetSpec :=
       enumSetTerm [<b>,</b> enumSetTerm]

   enumSetTerm :=
       &lt;<i>enum-value</i>&gt;
       | <b>/</b>&lt;<i>regex-over-enum-values</i>&gt;<b>/</b>
   </pre>


* **Case-insensitive names:** All names and regexes use case-insensitive matching. Thus, `AS1BORDER1` is same as `as1border1` and `Ethernet0/0` is same as `ethernet0/0`.

## Application Specifier

A combined specification for an IP protocol (e.g., TCP) and *destination* port to denote packets for common applications.

An application specifier is a set of enums (see above) over the following names (with the corresponding IP protocol and destination port in parenthesis): DNS(UDP, 53), HTTP(TCP, 80), HTTPS(TCP, 443), SNMP(UDP, 161), SSH(TCP, 22), TELNET(TCP, 23).

## BGP Peer Property Specifier

A specification for a set of BGP peer properties (e.g., those returned by the `bgpPeerConfiguration` question).

A BGP peer property property specifier is a set of enums (see above) over the following values: Local_AS, Local_IP, Is_Passive, Remote_AS, Route_Reflector_Client, Cluster_ID, Peer_Group, Import_Policy, Export_Policy, Send_Community.


## BGP Process Property Specifier

A specification for a set of BGP process properties (e.g., those returned by the `bgpProcessConfiguration` question).

A BGP process property specifier is a set of enums (see above) over the following values: Multipath_Match_Mode, Multipath_EBGP, Multipath_IBGP, Neighbors, Route_Reflector, Tie_Breaker. 

## BGP Session Compat Status Specifier

A specification for a set of BGP session compatibility statuses.

* `UNIQUE_MATCH` specifies a singleton set with that status.

* `UNIQUE_MATCH, DYNAMIC_MATCH` specifies a set with those statuses.

* `/match/` specifies a set with statuses that contain that substring.

#### BGP Session Compat Status Specifier Grammar

A BGP session status specifier is a set of enums (see above) over the following values: LOCAL_IP_UNKNOWN_STATICALLY, NO_LOCAL_IP, NO_LOCAL_AS, NO_REMOTE_IP, NO_REMOTE_PREFIX, NO_REMOTE_AS, INVALID_LOCAL_IP, UNKNOWN_REMOTE, HALF_OPEN, MULTIPLE_REMOTES, UNIQUE_MATCH, DYNAMIC_MATCH, NO_MATCH_FOUND. 

## BGP Session Status Specifier

A specification for a set of BGP session statuses.

* `ESTABLISHED` specifies a singleton set with that status.

* `NOT_ESTABLISHED, NOT_COMPATIBLE` specifies a set with those statuses.

* `/estab/` specifies a set with statuses that contain that substring.

#### BGP Session Status Specifier Grammar

A BGP session status specifier is a set of enums (see above) over the following values: ESTABLISHED, NOT_ESTABLISHED, NOT_COMPATIBLE. 

## BGP Session Type Specifier

A specification for a set of BGP session types.

* `IBGP` indicates iBGP sessions.

* `EBGP_SINGLEHOP, EBGP_MULTIHOP` indicates single and multi-hop eBGP sessions.

* `/EBGP/` indicates all types of eBGP sessions.

#### BGP Session Type Specifier Grammar

A BGP session type specifier is a set of enums (see above) over the following values: IBGP, EBGP_SINGLEHOP, EBGP_MULTIHOP, EBGP_UNNUMBERED, IBGP_UNNUMBERED, UNSET. 

## Disposition Specifier

Flow dispositions are used in questions like [reachability](https://pybatfish.readthedocs.io/en/latest/questions.html#pybatfish.question.bfq.reachability) to identify flow outcomes. The disposition specifier takes as input a comma-separated list of disposition values, which are interpreted using logical OR.

There are two coarse-grained flow dispositions:
  * `Success`: a flow has been successfully delivered
  * `Failure`: a flow has been dropped somewhere in the network

The following fine-grained disposition values are also supported:
* Success dispositions:
    * `Accepted`: a flow has been accepted by a device in the snapshot
    * `Delivered_to_subnet`: a flow has been delivered to a host subnet
    * `Exits_network`: a flow has been successfully forwarded to a device currently outside of the snapshot
* Failure dispositions:
    * `Denied_in`: a flow was denied by an input filter (an ACL or a firewall rule) on an interface
    * `Denied_out`: a flow was denied by an output filter on an interface
    * `No_route`: a flow was dropped because no matching route exists on device 
    * `Null_routed`: a flow was dropped because it matched a `null` route 
    * `Neighbor_unreachable`: a flow was dropped because it could not reach the next hop (e.g., an ARP failure)
    * `Loop`: the flow encountered a forwarding loop
    * `Insufficient_info`: Batfish does not have enough information to make a determination with certainty (e.g., some device configs are missing)

## Filter Specifier

A specification for filters (ACLs or firewall rules) in the network.

* Filter name or a regex over the names indicate filters on all nodes in the network with that name or matching regex. For example, `filter1` includes all filters with that name and `/acl/` includes all filters whose names contain 'acl'.

* `nodeTerm[filterWithoutNode]` indicates filters that match the `filterWithoutNode` specification on nodes that match the `nodeTerm` specification. A simple example is `as1border1[filter1]` which refers to the filter `filter1` on `as1border1`.

* `@in(interfaceSpec)` refers to filters that get applied when packets enter the specified interfaces. For example, `@in(Ethernet0/0)` includes filters for incoming packets on interfaces named `Ethernet0/0` on all nodes.

* `@out(interfaceSpec)` is similar except that it indicates filters that get applied when packets exit the specified interfaces. 

#### Filter Specifier Grammar

<pre>
filterSpec :=
    filterTerm [(<b>&</b>|<b>,</b>|<b>\</b>) filterTerm]

filterTerm :=
    filterWithNode
    | filterWithoutNode 
    | <b>(</b>filterSpec<b>)</b>

filterWithNode := 
    nodeTerm<b>[</b>filterWithoutNode<b>]</b>

filterWithoutNode :=
    filterWithoutNodeTerm [(<b>&</b>|<b>,</b>|<b>\</b>) filterWithoutNodeTerm]

filterWithoutNodeTerm :=
    &lt;<i>filter-name</i>&gt;
    | <b>/</b>&lt;<i>filter-name-regex</i>&gt;<b>/</b>
    | <b>@in(</b>interfaceSpec<b>)</b>
    | <b>@out(</b>interfaceSpec<b>)</b>
    | <b>(</b>filterWithoutNode<b>)</b>


</pre>

#### Filter Specifier Resolver

* `resolveFilterSpecifier` shows the set of filters represented by the given input.

## Interface Property Specifier

A specification for a set of interface-level properties (e.g., those returned by the `interfaceProperties` question).

An interface property specifier is a set of enums (see above) over the following values: Access_VLAN, Active, Allowed_VLANs, All_Prefixes, Auto_State_VLAN, Bandwidth, Blacklisted, Channel_Group, Channel_Group_Members, Declared_Names, Description, DHCP_Relay_Addresses, Encapsulation_VLAN, HSRP_Groups, HSRP_Version, Incoming_Filter_Name, Interface_Type, MLAG_ID, MTU, Native_VLAN, OSPF_Area_Name, OSPF_Cost, OSPF_Enabled, OSPF_Passive, OSPF_Point_To_Point, Outgoing_Filter_Name, PBR_Policy_Name, Primary_Address, Primary_Network, Proxy_ARP, Rip_Enabled, Rip_Passive, Spanning_Tree_Portfast, Speed, Switchport, Switchport_Mode, Switchport_Trunk_Encapsulation, VRF, VRRP_Groups, Zone_Name. 


## Interface Specifier

A specification for interfaces in the network.

* Interface name or a regex over the names indicate interfaces on all nodes in the network with that name or matching regex. For example, `Ethernet0/1` includes all interfaces with that name and `/Ethernet0/` includes all interfaces whose names contain 'Ethernet0'.

* `nodeTerm[interfaceWithoutNode]` indicates interfaces that match the `interfaceWithoutNode` specification on nodes that match the `nodeTerm` specification. A simple example is `as1border1[Ethernet0/1]` which refers to the interface `Ethernet0/1` on `as1border1`.

* `@connectedTo(ipSpec)` indicates all interfaces with configured IPv4 networks that overlap with specified IPs (see [`ipSpec`](#ip-specifier))

* `@interfaceGroup(book, group)` looks in the configured reference library for an interface group with name 'group' in book with name 'book'.

* `@vrf(vrf1)` indicates all interfaces configured to be in the VRF with name 'vrf1'.

* `@zone(zone3)` indicates all interfaces configured to be in the zone with name 'zone3'.

#### Interface Specifier Grammar

<pre>
interfaceSpec :=
    interfaceTerm [(<b>&</b>|<b>,</b>|<b>\</b>) interfaceTerm]

interfaceTerm :=
    interfaceWithNode
    | interfaceWithoutNode 
    | <b>(</b>interfaceSpec<b>)</b>

interfaceWithNode := 
    nodeTerm<b>[</b>interfaceWithoutNode<b>]</b>

interfaceWithoutNode :=
    interfaceWithoutNodeTerm [(<b>&</b>|<b>,</b>|<b>\</b>) interfaceWithoutNodeTerm]

interfaceWithoutNodeTerm :=
    &lt;<i>interface-name</i>&gt;
    | <b>/</b>&lt;<i>interface-name-regex</i>&gt;<b>/</b>
    | interfaceFunc
    | <b>(</b>interfaceWithoutNode<b>)</b>

interfaceFunc :=
    <b>@connectedTo(</b>ipSpec<b>)</b>
    | <b>@interfaceGroup(</b>&lt;<i>reference-book-name</i>&gt;<b>,</b> <&lt;<i>interface-group-name</i>&gt;<b>)</b>
    | <b>@vrf(</b>&lt;<i>vrf-name</i>&gt;<b>)</b>
    | <b>@zone(</b>&lt;<i>zone-name</i>&gt;<b>)</b>
</pre>

#### Interface Specifier Resolver

* `resolveInterfaceSpecifier` shows the set of interfaces represented by the given input.

## IP Protocol Specifier

A specification for a set of IP protocols.

* IP protocol names from the list below, such as `TCP`,  may be used.

* IP protocol numbers between 0 and 255 (inclusive), such as `6` to denote TCP, may be used. 

* A negation operator `!` may be used to denote all IP protocols other than the one specified. The semantics of negation is:

   * `!TCP` refers to all IP protocols other than TCP
   * `!TCP, !UDP` refers to all IP protocols other than TCP and UDP
   * `TCP, !UDP` refers to TCP 
 
#### IP Protocol Specifier Grammar

<pre>
ipProtocolSpec :=
    ipProtocolTerm [<b>,</b> ipProtocolTerm]

ipProtocolTerm :=
    ipProtocol
    | <b>!</b>ipProtocol

ipProtocol := 
    &lt;<i>ip-protocol-name</i>&gt;
    | &lt;<i>ip-protocol-number</i>&gt;
</pre>

#### IP Protocol Names

Batfish understands the following protocol names: AHP(51), AN(107), ANY_0_HOP_PROTOCOL(114), ANY_DISTRIBUTED_FILE_SYSTEM(68), ANY_HOST_INTERNAL_PROTOCOL(61), ANY_LOCAL_NETWORK(63), ANY_PRIVATE_ENCRYPTION_SCHEME(99), ARGUS(13), ARIS(104), AX25(93), BBN_RCC_MON(10), BNA(49), BR_SAT_MON(76), CBT(7), CFTP(62), CHAOS(16), COMPAQ_PEER(110), CPHB(73), CPNX(72), CRTP(126), CRUDP(127), DCCP(33), DCN_MEAS(19), DDP(37), DDX(116), DGP(86), EGP(8), EIGRP(88), EMCON(14), ENCAP(98), ESP(50), ETHERIP(97), FC(133), FIRE(125), GGP(3), GMTP(100), GRE(47), HIP(139), HMP(20), HOPOPT(0), I_NLSP(52), IATP(117),IPV6_ROUTE(43),IPX_IN_IP(111),IRTP(28), ISIS(124), ISO_IP(80), ISO_TP4(29), KRYPTOLAN(65), L2TP(115), LARP(91), LEAF1(25), LEAF2(26), MANAET(138), MERIT_INP(32), MFE_NSP(31), MHRP(48), MICP(95), MOBILE(55), MOBILITY(135), MPLS_IN_IP(137), MTP(92), MUX(18), NARP(54), NETBLT(30), NSFNET_IGP(85), NVPII(11), OSPF(89), PGM(113), PIM(103), PIPE(131), PNNI(102), PRM(21), PTP(123), PUP(12), PVP(75), QNX(106), RDP(27), ROHC(142), RSVP(46), RSVP_E2E_IGNORE(134), RVD(66), SAT_EXPAK(64), SAT_MON(69), SCC_SP(96), SCPS(105), SCTP(132), SDRP(42), SECURE_VMTP(82), SHIM6(140), SKIP(57), SM(122), SMP(121), SNP(109), SPRITE_RPC(90), SPS(130), SRP(119), SSCOPMCE(128), ST(5), STP(118), SUN_ND(77), SWIPE(53), TCF(87), TCP(6), THREE_PC(34), TLSP(56), TPPLUSPLUS(39), TRUNK1(23), TRUNK2(24), TTP(84), UDP(17), UDP_LITE(136), UTI(120), VINES(83), VISA(70), VMTP(81), VRRP(112), WB_EXPAK(79), WB_MON(78), WESP(141), WSN(74), XNET(15), XNS_IDP(22), XTP(36).

In addition, a special name `IP` may be used to denote all IP protocols. 


## IP Specifier

A specification for a set of IPv4 addresses.

* Constant values that denote addresses (e.g., `1.2.3.4`), prefixes (e.g., `1.2.3.0/24`), address ranges (e.g., `1.2.3.4 - 1.2.3.7`), and wildcards (e.g., `1.2.3.4:255.255.255.0`) may be used.

* `@addressGroup(book, group)` looks in the configured reference library for an address group name 'group' in book name 'book'.

* `locationSpec` can be used to denote addresses corresponding to the specified location (see [`locationSpec`](#location-specifier)).  For example, `as1border1[Ethernet0/0]` includes all IPv4 addresses configured on `as1border1` interface `Ethernet0/0`.

#### IP Specifier Grammar

<pre>
ipSpec :=
    ipTerm [(<b>&</b>|<b>,</b>|<b>\</b>) ipTerm]

ipTerm :=
    &lt;<i>ip-address</i>&gt;
    | &lt;<i>ip-prefix</i>&gt;
    | &lt;<i>ip-address-low</i>&gt; <b>-</b> &lt;<i>ip-address-high</i>&gt;
    | &lt;<i>ip wildcard</i>&gt;
    | <b>@addressGroup(</b>&lt;<i>reference-book-name</i>&gt;<b>,</b> &lt;<i>address-group-name</i>&gt;<b>)</b>
    | locationSpec
</pre>

#### IP Specifier Resolver

* `resolveIpSpecifier` shows the set of IP addresses represented by the given input.

## Location Specifier

A specification for locations of packets, including where they start or terminate.

There are two types of locations:
* `InterfaceLocation`: at the interface, used to model packets that originate or terminate at the interface
* `InterfaceLinkLocation`: on the link connected to the interface, used to model packets before they enter the interface or after they exit

Unless expilcitly specified, questions like `traceroute` and `reachability` will automatically assign IP addresses to packets based on their location. For `InterfaceLocation`, the set of assigned addresses is the interface address(es). This set is empty for interfaces that do not have an assigned address. For `InterfaceLinkLocation`, the set of assigned addresses corresponds to what (hypothetical) hosts attached to that interface can have, which includes all addresses in the subnet except for the address of the interface and the first and last addresses of the subnet. This set is empty for interface subnets that are `/30` or longer (e.g., loopback interfaces). 

Locations for which Batfish cannot automatically assign a viable IP are ignored. To force their consideration, explicit source IPs must be specified.

Some examples:

* `as1border1` specifies the `InterfaceLocation` for *all* interfaces on node `as1border1`. Any `nodeTerm` (see [node specifier grammar](#node-specifier-grammar)) can be used as a location specifier.

* `as1border1[Ethernet0/0]` specifies the `InterfaceLocation` for `Ethernet0/0` on node `as1border1`. Any valid `interfaceWithNode` expression can be used as a location specifier.

* `@vrf(vrf1)` specifies the `InterfaceLocation` for any interface in `vrf1` on *all* nodes. Any `interfaceFunc` can be used as a location specifier.

* `@enter(as1border1[Ethernet0/0])` specifies the `InterfaceLinkLocation` for packets entering `Ethernet0/0` on `as1border1`. 

#### Location Specifier Grammar

<pre>
locationSpec :=
    locationTerm [(<b>&</b>|<b>,</b>|<b>\</b>) locationTerm]

locationTerm :=
    locationInterface
    | <b>@enter(</b>locationInterface<b>)</b>
    | (locationSpec)

locationInterface :=
    nodeTerm
    | interfaceFunc
    | interfaceWithNode
</pre>

#### Location Specifier Resolver

* `resolveLocationSpecifier` shows the set of locations represented by the given input.
* `resolveIpsOfLocationSpecifier` shows the mapping from locations to IPs that will be used in `traceroute` and   `reachability` questions when IPs are not explicitly specified. 


## MLAG ID Specifier

A specification for a set of MLAG domain identifiers.

* `domain1` indicates a single domain with that name.

* `domain1, domain2` indicates these two domains.

* `/domain/` indicates all domains with that substring.

#### MLAG ID Specifier Grammar

An MLAG ID specifier grammar follows the enum set grammar (see above) with the base set of values as all Ids that appear in the snapshot. 

## Named Structure Specifier

A specification for a set of structure types in Batfish's vendor independent model. 

A named structure specifier is a set of enums (see above) over the following values: AS_PATH_ACCESS_LIST, AUTHENTICATION_KEY_CHAIN, COMMUNITY_LIST, IKE_PHASE1_KEYS, IKE_PHASE1_POLICIES, IKE_PHASE1_PROPOSALS, IP_ACCESS_LIST, IP_6_ACCESS_LIST, IPSEC_PEER_CONFIGS, IPSEC_PHASE2_POLICIES, IPSEC_PHASE2_PROPOSALS, PBR_POLICY, ROUTE_FILTER_LIST, ROUTE_6_FILTER_LIST, ROUTING_POLICY, VRF, ZONE.


## Node Property Specifier

A specification for a set of node-level properties (e.g., those returned by the `nodeProperties` question).

A node property specifier is a set of enums (see above) over the following values: AS_Path_Access_Lists, Authentication_Key_Chains, Canonical_IP, Community_Lists, Configuration_Format, Default_Cross_Zone_Action, Default_Inbound_Action, Device_Type, DNS_Servers, DNS_Source_Interface, Domain_Name, Hostname, IKE_Phase1_Keys, IKE_Phase1_Policies, IKE_Phase1_Proposals, Interfaces, IP_Access_Lists, IP_Spaces, IP6_Access_Lists, IPsec_Peer_Configs, IPsec_Phase2_Policies, IPsec_Phase2_Proposals, IPSec_Vpns, Logging_Servers, Logging_Source_Interface, NTP_Servers, NTP_Source_Interface, PBR_Policies, Route_Filter_Lists, Route6_Filter_Lists, Routing_Policies, SNMP_Source_Interface, SNMP_Trap_Servers, TACACS_Servers, TACACS_Source_Interface, Vendor_Family, VRFs, Zones. 


## Node Specifier

A specification for nodes in the network.

* Node names or a regex over the names indicate nodes in the network with that name or matching regex. For example, `as1border1` indicates that node and `/as1/` indicates all nodes whose names contain `as1`.

* `@deviceType(type1)` indicates all nodes of the type 'type1'. The types of devices are listed [here](#device-types).

* `@role(dim, role)` indicates all nodes with role name 'role' in dimension name 'dim'.

#### Node Specifier Grammar

<pre>
nodeSpec :=
    nodeTerm [(<b>&</b>|<b>,</b>|<b>\</b>) nodeTerm]

nodeTerm :=
    &lt;<i>node-name</i>&gt;
    | /&lt;<i>node-name-regex</i>&gt;/
    | nodeFunc
    | <b>(</b>nodeSpec<b>)</b>

nodeFunc :=
    <b>@deviceType(</b><i>device-type</i><b>)</b>
    | <b>@role(</b>&lt;<i>dimension-name</i>&gt;<b>,</b> &lt;<i>role-name</i>&gt;<b>)</b>
</pre>

#### Node Specifier Resolver

* `resolveNodeSpecifier` shows the set of nodes represented by the given input.

#### Device Types

Batfish has the following device types.

* `Host`: An end host. 
* `Internet`: A logical device that represents the Internet. It is present when external connectivity is modeled. 
* `ISP`: A logical devie that represents a neighboring ISP. It is present when external connectivity is modeled.
* `Router`: A device that does L3 routing and forwarding.
* `Switch`: A device that only does L2 forwarding.

## Routing Policy Specifier

A specification for routing policies in the network.

* Routing policy name or a regex over the names indicate routing policies on all nodes in the network with that name or matching regex. For example, `routingPolicy1` includes all routing policies with that name and `/rtpol/` includes all routing policies whose names contain 'rtpol'.

#### Routing Policy Grammar

<pre>
routingPolicySpec :=
    routingPolicyTerm [(<b>&</b>|<b>,</b>|<b>\</b>) routingPolicyTerm]

routingPolicyTerm :=
    &lt;<i>routing-policy-name</i>&gt;
    | <b>/</b>&lt;<i>routing-policy-name-regex</i>&gt;<b>/</b>
    | <b>(</b>routingPolicySpec<b>)</b>
</pre>

## VXLAN VNI Property Specifier

 A specification for a set of VXLAN VNI properties.

 * `LOCAL_VTEP_IP` indicates the local VTEP IP.

 * `LOCAL_VTEP_IP, VTEP_FLOOD_LIST` indicates the two properties specified.

 * `/VTEP/` indicates all properties with VTEP in their name.

 #### VXLAN VNI Property Specifier Grammar

 A VXLAN VNI property specifier is a set of enums (see above) over the following values: LOCAL_VTEP_IP, MULTICAST_GROUP, VLAN, VNI, VTEP_FLOOD_LIST, VXLAN_PORT. 
