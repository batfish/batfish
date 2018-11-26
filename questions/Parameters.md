Batfish questions have the following parameter types, with linked descriptions:

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
* [`dispositionSpec`](#disposition-specifier)

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

## Disposition specifier

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

* `inFilterOf` indicates filters that get applied when packets enter the interfaces denoted by the `interfaceSpec`. For example, `inFilterOf(Ethernet0/0)` includes filters for incoming packets on interfaces named `Ethernet0/0`.

* `outFilterOf` is similar to above except that it indicates filters that get applied when packets exit the interfaces denoted by the `interfaceSpec`. For example, `outFilterOf(Ethernet0/0)` includes all filters for outgoing packets on interfaces named `Ethernet0/0`.

* and if none of the above are matched, the default behavior is to include filters with names that match the supplied `<javaRegex>`. For example, `acl-.*` includes all filters whose names begin with `acl-`.

#### Filter Specifier Grammar

```
filterSpec =
    inFilterOf(<interfaceSpec>)
    | outFilterOf(<interfaceSpec>)
    | <javaRegex>
```

## Interface Specifier

A specification for interfaces in the network.

* `connectedTo` indicates all interfaces with configured IPv4 networks that overlap the specified IPs. For example, `connectedTo(1.2.3.4/30)` includes interfaces that overlap the specified IPv4 prefix.

* `type` indicates all interfaces with the specified type. For example, `type(loopback)` includes loopback interfaces.

* `vrf` indicates all interfaces configured to be in the VRF with name matching the given `<javaRegex>`. For example, `vrf(default)` includes interfaces in the default VRF.

* `zone` indicates all interfaces configured to be in the (firewall) zone with name matching the given `<javaRegex>`. For example, `zone(admin)` includes interfaces in the zone named admin.

* and if none of the above are matched, the default behavior is to include interfaces with names that match the supplied `<javaRegex>`. For example, `ae-.*` includes all Juniper aggregated ethernet interfaces.

#### Interface Specifier Grammar

```
interfaceSpec =
    connectedTo(<ipSpec>)
    | type(<interfaceType>)
    | vrf(<javaRegex>)
    | zone(<javaRegex>)
    | <javaRegex>
```

#### Interface Types

```
interfaceType = 
    aggregated
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

A specification for IPv4 addresses. An `ipSpec` is a string with the following syntax:

* `ref.addressbook` looks in the configured reference books for an address group and book of the given string names.

* `ofLocation` returns the IPv4 address or addresses corresponding to the specified location (see [`locationSpec`](#location-specifier)).  For example, `ofLocation(as1border1[Ethernet0/0])` includes all IPv4 addresses configured on `as1border1` interface `Ethernet0/0`.

* and if none of the above are matched, the default behavior is to parse the supplied string as an IPv4 address, IPv4 prefix, or IPv4 wildcard. For example, `1.2.3.4` is an IPv4 address, `1.2.3.4/30` is an IPv4 prefix, and `1.2.3.4:0.0.0.3` is an IPv4 wildcard equivalent to `1.2.3.4/30`.

    A difference of IPv4 literals is also supported. For example, `1.2.3.4/30 - 1.2.3.4` specifies every IPv4 address in that prefix except `1.2.3.4`, aka, `1.2.3.5`, `1.2.3.6`, and `1.2.3.7`.

#### IP Specifier Grammar

```
ipSpec =
    ref.addressbook(<group=string>,<book=string>)
    | ofLocation(<locationSpec>)
    | <ipv4spec>
    | <ipv4spec> - <ipv4spec>

<ipv4spec> =
    <IPv4 address in A.B.C.D form>
    | <IPv4 prefix in A.B.C.D/L form>
    | <IPv4 wildcard in A.B.C.D:M.N.O.P form>
```

## Java Regular Expression

A Java regular expression. For information on the syntax of these strings, see the [Java documentation](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum) for the `Pattern` class.

## Location Specifier

A precise specification for locations of packets.

There are two types of `Location`:
* `Interface Location` - at the interface, used to model packets that originate or terminate at the interface.
* `InterfaceLinkLocation` - on the link connected to the interface, used to model packets before they enter the interface or after they exit.

Some examples:

* `as1border1[Ethernet0/0]` - specifies the `InterfaceLocation` for `Ethernet0/0` on node `as1border1`.

* `as1border1` - specifies the `InterfaceLocation` for *all* interfaces on node `as1border1`. It is interpreted as `as1border1[.*]`.

* `[Ethernet0/0]` - specifies the `InterfaceLocation` for any interface `Ethernet0/0` on any node. It is interpreted as `.*[Ethernet0/0]`.

* `enter([Ethernet0/0])` - specifies the `InterfaceLinkLocation` for any the link of interface `Ethernet0/0` on any node. It is interpreted as `enter(.*[Ethernet0/0])`.

#### Location Specifier Grammar

```
locationSpec =
    <interfaceLocationSpec>
    | <interfaceLinkLocationSpec>

interfaceLocationSpec =
    [<interfaceSpec>]
    | <nodeSpec>[<interfaceSpec>]

interfaceLinkSpec =
    enter(<interfaceLocationSpec>)
    | exit(<interfaceLocationSpec>)
```

## Node Specifier

A specification for nodes in the network.

* `ref.noderole` finds all nodes with the role whose name matches `roleRegex` in node role dimension `dimension`.

* and if none of the above are matched, the default behavior is to include nodes with hostnames that match the supplied `<javaRegex>`. For example, `as1.*` includes all devices in AS 1 in the example network.

#### Node Specifier Grammar

```
nodeSpec =
    ref.noderole(<roleRegex=javaRegex>,<dimension=string>)
    | <javaRegex>
```
