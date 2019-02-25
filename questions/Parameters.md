Batfish questions have the following parameter types that support rich specifications whose grammar is described below. Before reading those grammars, we recommend reading the general notes. 

<!--
[comment]: # (* `bgpPropertySpec`)
[comment]: # (* `boolean`)
[comment]: # (* `comparator`)
[comment]: # (* `double`)
[comment]: # (* `float`)
[comment]: # (* `headerConstraint`)
[comment]: # (* `integer`)
[comment]: # (* `interfacePropertySpec`)
-->
* [`flowDispositionSpec`](#flow-disposition-specifier)

* [`filterSpec`](#filter-specifier)

* [`interfaceSpec`](#interface-specifier)

<!--
[comment]: # (* `ip`)
[comment]: # (* `ipProtocol`)
-->

* [`ipSpec`](#ip-specifier)

<!--
[comment]: # (* `ipWildcard`)
-->

* [`javaRegex`](#java-regular-expression)

<!--
[comment]: # (* `jsonPath`)
[comment]: # (* `jsonPathRegex`)
-->

* [`locationSpec`](#location-specifier)

<!--
[comment]: # (* `long`)
[comment]: # (* `namedStructureSpec`)
[comment]: # (* `nodePropertySpec`)
-->

* [`nodeSpec`](#node-specifier)

<!--
[comment]: # (* `ospfPropertySpec`)
[comment]: # (* `prefix`)
[comment]: # (* `prefixRange`)
[comment]: # (* `protocol`)
[comment]: # (* `question`)
[comment]: # (* `string`)
[comment]: # (* `subrange`)
-->

## General notes on the grammar 

* Set semantics and operations: Specifiers denote to sets of entities (e.g., nodeSpec resolves to a set of nodes), and in many cases, the grammar allows for union, intersection, and difference of such sets. The respective operators are `','`, `'&'`, and `'\'`.

* Case-insensitive names: All names and regexes use case-insensitive matching. Thus, `AS1BRODER1` is same as `as1border1` and `Ethernet0/0` is same as `ethernet0/0`.

* Complex names and quotes: In general, the names of entities such as nodes and interfaces do not need to be quoted. However, if the name begins with a digit (0-9), double quote ('"'), or slash ('/'), or if it contains space or one of `[,&()[]@!#$%^;?<>={}]` characters, it must be surrounded by double quotes. Thus, we can use `as1border1` as a name directly but must quote a name like `1startsWithDigit andHasSpaceand[brackets]`.

## Flow Disposition Specifier

A specification of flow dispositions, used to identify desired flow actions. Used in questions like `reachability`.
Disposition specifier takes as input a string of comma-separated disposition values, which are interpreted using logical OR.

There are two meta flow dispositions: `Success` and `Failure` used to indicate that a flow has been successfully delivered, 
or alternatively, has been dropped somewhere in the network. 

The following fine-grained disposition values are also supported:
* Success dispositions:
    * `Accepted` - a flow has been accepted by a device in the snapshot
    * `Delivered_to_subnet` - a flow has been delivered to the host subnet
    * `Exits_network` - a flow has been succesfully forwared to a device currently outside of the snapshot
* Failure dispositions:
    * `Denied_in` - a flow was denied by an input ACL on an interface
    * `Denied_out` - a flow was denied by an output ACL on an interface
    * `No_route` - a flow was dropped, no suitable route exists on device 
    * `Null_routed` - a flow was dropped, since it matched a `null` route 
    * `Neighbor_unreachable` - could not reach the next hop (e.g., an ARP failure)
    * `Loop` - the flow encountered a forwarding loop
    * `Insufficient_info` - Batfish does not have enough configuration info to make a determination with certainty (e.g., some device configs are missing)

## Filter Specifier

A specification for filters (ACLs or firewall rules) in the network.

* Filter name or a regex over the names indicate filters on all nodes in the network with that name or matching regex. For example, `filter1` includes all filters with that name and `/acl/` includes all filters whose names contain `acl`.

* `@in(interfaceSpec)` indicates filters that get applied when packets enter the interfaces denoted by `interfaceSpec`. For example, `@in(Ethernet0/0)` includes filters for incoming packets on interfaces named `Ethernet0/0`.

* `@out(intefaceSpec)` is similar except that it indicates filters that get applied when packets exit the interfaces denoted by the `interfaceSpec`. 

#### Filter Specifier Grammar

```
filterSpec := 
    filterTerm [(‘&’|’,’|’\’) filterTerm]

filterTerm := 
    <filter-name>
    | ‘/’<filter-name-regex>‘/’
    | filterFunc
    | ‘(‘filterTerm‘)’

filterFunc :=  
    @in(interfaceSpec)  
    | @out(intefaceSpec) 
```

## Interface Specifier

A specification for interfaces in the network.

* Interface name or a regex over the names indicate interfaces on all nodes in the network with that name or matching regex. For example, `Ethernet0/1` includes all filters with that name and `/Ethernet0/` includes all filters whose names contain `Ethernet0`.

* `@connectedTo(ipSpec)` indicates all interfaces with configured IPv4 networks that overlap with IPs denoted with `ipSpec`. For example, `@connectedTo(1.2.3.4/30)` includes interfaces that overlap the specified IPv4 prefix.

* `@ainterfaceGroup` looks in the configured reference library for an interface group and book of the given string names.

* `@interfaceType(interfaceType)` indicates all interfaces with the specified link type. The types of interfaces are listed below. 

* `@vrf(<vrf-name>)` indicates all interfaces configured to be in the VRF with name `vrf-name`.

* `@zone(<zone-name>)` indicates all interfaces configured to be in the zone with name `zone-name`.

#### Interface Specifier Grammar

```
interfaceSpec := 
    interfaceTerm [(‘&’|’,’|’\’) interfaceTerm]

interfaceTerm :=
    <interface name>
    | ‘/’<interface-name-regex>‘/’
    | interfaceFunc
    | ‘(‘interfaceTerm‘)’

interfaceFunc :=   
    @connectedTo(ipSpec)
    | @interfaceGroup(<address-group-name>, <reference-book-name>)    
    | @interfaceType(interfaceType)
    | @vrf(<vrf-name>)          
    | @zone(<zone-name>)
```

#### Interface Types

```
interfaceType = 
    aggregated
    | aggregate_child
    | logical
    | loopback
    | null
    | physical
    | redundant
    | tunnel
    | unknown 
    | vlan
    | vpn
```

## IP Specifier

A specification for a set of IPv4 addresses.

* Constant values that denote addresses, prefixes, address ranges, and wildcards may be used.

* `@addressGroup` looks in the configured reference library for an address group and book of the given string names.

* `locationSpec` can be used to denote addresses corresponding to the specified location (see [`locationSpec`](#location-specifier)).  For example, `as1border1[Ethernet0/0]` includes all IPv4 addresses configured on `as1border1` interface `Ethernet0/0`.

#### IP Specifier Grammar

```
ipSpec :=  
    ipTerm [’,’ ipTerm]

ipTerm :=  
    <ip-address (e.g., 1.2.3.4)>
    | <ip-prefix (e.g., 1.2.3.0/24)>
    | <ip-address-low - ip-address-high (e.g., 1.1.1.1 - 1.1.1.3)>
    | <ip wildcard (e.g., 1.2.3.4:255.255.255.0)>
    | ipFunc
    | locationSpec

ipFunc := 
    @addressGroup(<address-group-name>, <reference-book-name>)
```

## Java Regular Expression

A Java regular expression. For information on the syntax of these strings, see the [Java documentation](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum) for the `Pattern` class.

## Location Specifier

A specification for locations of packets, including where they start or terminate.

There are two types of `Location`:
* `InterfaceLocation` - at the interface, used to model packets that originate or terminate at the interface.
* `LinkLocation` - on the link connected to the interface, used to model packets before they enter the interface or after they exit.

Some examples:

* `as1border1[Ethernet0/0]` specifies the `InterfaceLocation` for `Ethernet0/0` on node `as1border1`.

* `as1border1` specifies the `InterfaceLocation` for *all* interfaces on node `as1border1`. It is same as `as1border1[/.*/]`.

* `@vrf(vrf1)` specifies the `InterfaceLocation` for any interface in `vrf1` on *all* nodes. It is same as `/.*/[@vrf(vrf1)]`.

* `@enter(as1border1[Ethernet0/0])` specifies the `LinkLocation` for packets entering `Ethernet0/0` on `as1border1`. 

#### Location Specifier Grammar

```
locationSpec :=
    locationTerm [(‘&’|’,’|’\’) locationTerm] 

locationTerm :=     
    locationInterface
    | locationSpecifier
    | ‘(‘ locationTerm ‘)’

locationInterface :=    
    nodeTerm
    | interfaceFunc
    | nodeTerm ‘[‘ interfaceExpr ‘]’        

locationFunc := 
    @enter(locationInterface)
```

## Node Specifier

A specification for nodes in the network.

* Node names or a regex over the names indicate nodes in the network with that name or matching regex. For example, `as1border1` indicates that node and `/as1/` indicates all nodes whose names contain `as1`.

* `@deviceType(deviceType)` indicates all nodes of the specified type.  The types of devices are listed below. 

* `@role` indicates all nodes with the specified role name in the specified dimension name.

#### Node Specifier Grammar

```
nodeExpr :=         
    nodeTerm [(‘&’|’,’|’\’) nodeTerm]

nodeTerm :=
    <node-name>
    | ‘/’<node-name-regex>‘/’
    | nodeFunc
    | ‘(‘ nodeTerm ‘)’

nodeFunc :=
    @deviceType(deviceType) 
    | @role(<role-name>, <dimension-name>)   
```

#### Device Types

```
deviceType = 
  host,
  internet,   // when the Internet is modeled 
  isp,        // when ISPs are modeled
  router,
  switch
```

