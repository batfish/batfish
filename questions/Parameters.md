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

* **Case-insensitive names:** All names and regexes use case-insensitive matching. Thus, `AS1BORDER1` is same as `as1border1` and `Ethernet0/0` is same as `ethernet0/0`.

## Flow Disposition Specifier

Flow dispositions identify flow actions. Used in questions like [reachability](https://pybatfish.readthedocs.io/en/latest/questions.html#pybatfish.question.bfq.reachability), the disposition specifier takes as input a comma-separated list of disposition values, which are interpreted using logical OR.

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

* `@in(interfaceSpec)` refers to filters that get applied when packets enter the specified interfaces. For example, `@in(Ethernet0/0)` includes filters for incoming packets on interfaces named `Ethernet0/0` on all nodes.

* `@out(interfaceSpec)` is similar except that it indicates filters that get applied when packets exit the specified interfaces. 

#### Filter Specifier Grammar

<pre>
filterSpec :=
    filterTerm [(<b>&</b>|<b>,</b>|<b>\</b>) filterTerm]

filterTerm :=
    &lt;<i>filter-name</i>&gt;
    | <b>/</b>&lt;<i>filter-name-regex</i>&gt;<b>/</b>
    | <b>@in(</b>interfaceSpec<b>)</b>
    | <b>@out(</b>interfaceSpec<b>)</b>
    | <b>(</b>filterSpec<b>)</b>
</pre>

## Interface Specifier

A specification for interfaces in the network.

* Interface name or a regex over the names indicate interfaces on all nodes in the network with that name or matching regex. For example, `Ethernet0/1` includes all interfaces with that name and `/Ethernet0/` includes all interfaces whose names contain 'Ethernet0'.

* `@connectedTo(ipSpec)` indicates all interfaces with configured IPv4 networks that overlap with specified IPs (see [`ipSpec`](#ip-specifier))

* `@interfaceGroup(group, book)` looks in the configured reference library for an interface group with name 'group' and book with name 'book'.

* `@vrf(vrf1)` indicates all interfaces configured to be in the VRF with name 'vrf1'.

* `@zone(zone3)` indicates all interfaces configured to be in the zone with name 'zone3'.

#### Interface Specifier Grammar

<pre>
interfaceSpec :=
    interfaceTerm [(<b>&</b>|<b>,</b>|<b>\</b>) interfaceTerm]

interfaceTerm :=
    &lt;<i>interface-name</i>&gt;
    | <b>/</b>&lt;<i>interface-name-regex</i>&gt;<b>/</b>
    | interfaceFunc
    | <b>(</b>interfaceSpec<b>)</b>

interfaceFunc :=
    <b>@connectedTo(</b>ipSpec<b>)</b>
    | <b>@interfaceGroup(</b>&lt;<i>address-group-name</i>&gt;<b>,</b> &lt;<i>reference-book-name</i>&gt;<b>)</b>
    | <b>@vrf(</b>&lt;<i>vrf-name</i>&gt;<b>)</b>
    | <b>@zone(</b>&lt;<i>zone-name</i>&gt;<b>)</b>
</pre>

## IP Specifier

A specification for a set of IPv4 addresses.

* Constant values that denote addresses (e.g., `1.2.3.4`), prefixes (e.g., `1.2.3.0/24`), address ranges (e.g., `1.2.3.4 - 1.2.3.7`), and wildcards (e.g., `1.2.3.4:255.255.255.0`) may be used.

* `@addressGroup(group, book)` looks in the configured reference library for an address group name 'group' and book name 'book'.

* `locationSpec` can be used to denote addresses corresponding to the specified location (see [`locationSpec`](#location-specifier)).  For example, `as1border1[Ethernet0/0]` includes all IPv4 addresses configured on `as1border1` interface `Ethernet0/0`.

#### IP Specifier Grammar

<pre>
ipSpec :=
    ipTerm [<b>,</b> ipTerm]

ipTerm :=
    &lt;<i>ip-address</i>&gt;
    | &lt;<i>ip-prefix</i>&gt;
    | &lt;<i>ip-address-low</i>&gt; <b>-</b> &lt;<i>ip-address-high</i>&gt;
    | &lt;<i>ip wildcard</i>&gt;
    | <b>@addressGroup</b>(&lt;<i>address-group-name</i>&gt;<b>,</b> &lt;<i>reference-book-name</i>&gt;<b>)</b>
    | locationSpec
</pre>

## Location Specifier

A specification for locations of packets, including where they start or terminate.

There are two types of locations:
* `InterfaceLocation`: at the interface, used to model packets that originate or terminate at the interface
* `InterfaceLinkLocation`: on the link connected to the interface, used to model packets before they enter the interface or after they exit

Some examples:

* `as1border1` specifies the `InterfaceLocation` for *all* interfaces on node `as1border1`. Any `nodeTerm` (see [node specifier grammar](#node-specifier-grammar)) can be used as a location specifier.

* `as1border1[Ethernet0/0]` specifies the `InterfaceLocation` for `Ethernet0/0` on node `as1border1`. A `nodeTerm` and an `interfaceSpec` can be combined this way as a location specifier.  

* `@vrf(vrf1)` specifies the `InterfaceLocation` for any interface in `vrf1` on *all* nodes. Any `interfaceFunc` can be used as a location specifier.

* `@enter(as1border1[Ethernet0/0])` specifies the `InterfaceLinkLocation` for packets entering `Ethernet0/0` on `as1border1`. 

#### Location Specifier Grammar

<pre>
locationSpec :=
    locationTerm [(<b>&</b>|<b>,</b>|<b>\</b>) locationTerm]

locationTerm :=
    locationInterface
    | locationSpecifier
    | (locationSpec)

locationInterface :=
    nodeTerm
    | interfaceFunc
    | nodeTerm<b>[</b>interfaceSpec<b>]</b>

locationFunc :=
    <b>@enter(</b>locationInterface<b>)</b>
</pre>

## Node Specifier

A specification for nodes in the network.

* Node names or a regex over the names indicate nodes in the network with that name or matching regex. For example, `as1border1` indicates that node and `/as1/` indicates all nodes whose names contain `as1`.

* `@deviceType(type1)` indicates all nodes of the type 'type1'. The types of devices are listed [here](#device-types).

* `@role(role, dim)` indicates all nodes with role name 'role' in dimension name 'dim'.

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
    | <b>@role(</b>&lt;<i>role-name</i>&gt;<b>,</b> &lt;<i>dimension-name</i>&gt;<b>)</b>
</pre>

#### Device Types

Batfish has the following device types.

* `Host`: An end host. 
* `Internet`: A logical device that represents the Internet. It is present when external connectivity is modeled. 
* `ISP`: A logical devie that represents a neighboring ISP. It is present when external connectivity is modeled.
* `Router`: A device that does L3 routing and forwarding.
* `Switch`: A device that only does L2 forwarding.
