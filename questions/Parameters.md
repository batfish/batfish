Batfish questions have the following parameter types, with linked descriptions:

* [`answerElement`](#answerelement)

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
-->

* [`roleSpec`](#role-specifier)

<!--
[comment]: # (* `question`)
[comment]: # (* `string`)
[comment]: # (* `subrange`)
-->

## Answer Element

A JSON-encoded Batfish Java [`AnswerElement`](https://www.batfish.org/docs/org/batfish/datamodel/answers/AnswerElement.html).

## Interface Specifier

A specification for a filter on Batfish [`Interface`](https://www.batfish.org/docs/org/batfish/datamodel/Interface.html) objects.

```
interfaceSpec =
    hasSubnet(<ipSpec>)
    | vrf(<javaRegex>)
    | <javaRegex>
```

where:

* `hasSubnet` indicates all interfaces with configured IPv4 networks that overlap the specified IPs. For example, `haSubnet(1.2.3.4/30)` includes interfaces that overlap the specified IPv4 prefix.

* `vrf` indicates all interfaces configured to be in the VRF with name matching the given `<javaRegex>`. For example, `vrf(default)` includes interfaces in the default VRF.

* and if none of the above are matched, the default behavior is to include interfaces with names that match the supplied `<javaRegex>`. For example, `ae-.*` includes all Juniper aggregated ethernet interfaces.

## IP Specifier

TODO

## Java Regular Expression

TODO

## Location Specifier

A specification for a Batfish [`Location`](https://www.batfish.org/docs/org/batfish/specifier/Location.html), which indicates specific places in the network.

There are two types of `Location`:
* [`InterfaceLinkLocation`](https://www.batfish.org/docs/org/batfish/specifier/InterfaceLinkLocation.html) - on the link connected to the interface, usually used to model a source or destination of a flow entering or exiting the specified interface.
* [`InterfaceLocation`](https://www.batfish.org/docs/org/batfish/specifier/InterfaceLocation.html)  - on the interface or the device itself.

A `locationSpec` is a string that indicates `Location` using the following syntax

```
interfaceLocationSpec =
    [<interfaceSpec>]
    | <nodeSpec>[<interfaceSpec>]

interfaceLinkSpec =
    enter(<interfaceLocationSpec>)
    | exit(<interfaceLocationSpec>)

locationSpec =
    <interfaceLocationSpec>
    | <interfaceLinkLocationSpec>
```

Some examples:

* `as1border1[Ethernet0/0]` -- specifies the `InterfaceLocation` for `Ethernet0/0` on node `as1border1`.

* `as1border1` -- specifies the `InterfaceLocation` for any interface on node `as1border1`. It is interpreted as `as1border1[.*]`.

* `[Ethernet0/0]` -- specifies the `InterfaceLocation` for any interface `Ethernet0/0` on any node. It is interpreted as `.*[Ethernet0/0]`.

* `enter([Ethernet0/0])` -- specifies the `InterfaceLinkLocation` for any the link of interface `Ethernet0/0` on any node. It is interpreted as `enter(.*[Ethernet0/0])`.

## Node Specifier

A specification for a filter on Batfish [`Configuration`](https://www.batfish.org/docs/org/batfish/datamodel/Configuration.html) objects, which correspond to nodes in the network.

```
nodeSpec =
    ref.noderole(<roleSpec>)
    | <javaRegex>
```

where:

* `ref.noderole` finds all nodes with the specified roles.

* and if none of the above are matched, the default behavior is to include nodes with hostnames that match the supplied `<javaRegex>`. For example, `as1.*` includes all devices in AS 1 in the example network.

## Role Specifier

TODO
