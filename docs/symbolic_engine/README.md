Several Batfish questions are implemented using _symbolic analyses_ that model packets as mathematical variables, 
put constraints on those variables, and extract solutions.  The primary benefit of this approach is comprehensiveness:
the analysis considers all possible packets, whereas traditional testing methods can only consider a small number of examples.
This page describes those questions and the architecture of the symbolic analysis engine.

# Use cases
There are two high-level use cases supported by symbolic analysis in Batfish: searching for flows that satisfy particular
structural and behavioral constraint inputs, and comparing behavior of two snapshots.

## Search
Symbolic analysis enables _comprehensive_ search in which all possible flows are considered. Users can input 
constraints on structure of the packet (e.g. non-SSH TCP packets) using 
[`HeaderConstraints`](https://pybatfish.readthedocs.io/en/latest/datamodel.html#pybatfish.datamodel.flow.HeaderConstraints)
and behavioral properties (e.g. reaches an internal service without transiting a firewall) using 
[`PathConstraints`](https://pybatfish.readthedocs.io/en/latest/datamodel.html#pybatfish.datamodel.flow.PathConstraints).

Forwarding behavior:
* [`reachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html#Reachability) searches across all flows that match the specified conditions and returns examples of such flows. This question can be used to ensure that certain services are globally accessible and parts of the network are perfectly isolated from each other.
* [`bidirectionalReachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html#Bi-directional-Reachability) performs two reachability analyses, first originating from specified sources, then returning back to those sources. After the first (forward) pass, sets up sessions in the network and creates returning flows for each successfully delivered forward flow. The second pass searches for return flows that can be successfully delivered in the presence of the setup sessions.
* [`detectLoops`](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html#Loop-detection) searches across all possible flows in the network and returns example flows that will experience forwarding loops.
* [`loopbackMultipathConsistency`](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html#Multipath-Consistency-for-router-loopbacks) finds flows between loopbacks that are treated differently (i.e., dropped versus forwarded) by different paths in the presence of multipath routing.
* [`subnetMultipathConsistency`](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html#Multipath-Consistency-for-host-subnets) searches across all flows between subnets that are treated differently (i.e., dropped versus forwarded) by different paths in the network and returns example flows.

Filters/ACLs:
* [`searchFilters`](https://pybatfish.readthedocs.io/en/latest/notebooks/filters.html#Search-Filters) finds flows for which a filter takes a particular behavior. 
* [`filterLineReachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/filters.html#Filter-Line-Reachability) returns unreachable lines in filters (ACLs and firewall rules).

BGP routing policies:
* [`searchRoutePolicies`](https://pybatfish.readthedocs.io/en/latest/notebooks/routingProtocols.html#Search-Route-Policies) finds route announcements for which a route policy has a particular behavior. 

### Intent verification
Comprehensive search enables verification of network behavior. For example, 
[`reachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html#Reachability) can verify 
reachability properties like accessibility and isolation. Suppose a network policy requires that an internal service be 
isolated (inaccessible) from the internet. The [`reachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html#Reachability) question
can verify isolation by searching for packets that violate the policy -- that is packets that can reach the service from the internet. If none is found, the network correctly meets 
that policy; otherwise, the reachability question returns an example flow to the user. 

For details with examples, see:
* [Introduction to Forwarding Analysis using Batfish](https://pybatfish.readthedocs.io/en/latest/notebooks/linked/introduction-to-forwarding-analysis.html)
* [Analyzing ACLs and firewall rules with Batfish](https://pybatfish.readthedocs.io/en/latest/notebooks/linked/analyzing-acls-and-firewall-rules.html)
* [Analyzing BGP Route Policies](https://pybatfish.readthedocs.io/en/latest/notebooks/linked/analyzing-routing-policies.html#Analyzing-BGP-Route-Policies)

## Network behavior comparison
Symbolic analysis can be used to quickly identify differences in the behavior of two network snapshots. 

Questions:
* [`compareFilters`](https://pybatfish.readthedocs.io/en/latest/notebooks/differentialQuestions.html?highlight=compare%20filters#Compare-Filters) returns pairs of lines, one from each filter, that match the same flow(s) but treat them differently (i.e. one permits and the other denies the flow).
* [`differentialReachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/differentialQuestions.html?highlight=compare%20filters#Differential-Reachability) returns flows that are successful in one snapshot but not in another.
* [`searchFilters`](https://pybatfish.readthedocs.io/en/latest/notebooks/filters.html#Search-Filters) can be run in differential mode, which returns flows treated differently by the two versions of each specified filter.

### Change validation 
The differential/comparison questions can be used to validate planned changes 1) have the intended effect and 2) have
no collateral damage. For more details with examples, see:
* [Provably safe ACL and firewall rule changes](https://pybatfish.readthedocs.io/en/latest/notebooks/linked/provably-safe-acl-and-firewall-changes.html) 
* [Safely refactoring ACLs and firewall rules](https://pybatfish.readthedocs.io/en/latest/notebooks/linked/safely-refactoring-acls-and-firewall-rules.html).

### Fault tolerance
Fault tolerance can be verified by comparing network behavior before and after a (simulated) failure. See
[Analyzing the Impact of Failures (and letting loose a Chaos Monkey)](https://pybatfish.readthedocs.io/en/latest/notebooks/linked/analyzing-the-impact-of-failures-and-letting-loose-a-chaos-monkey.html) for more detail.

# The symbolic engine
Batfish builds symbolic constraints on flows using [Binary Decision Diagrams (BDDs)](https://en.wikipedia.org/wiki/Binary_decision_diagram),
which efficiently perform boolean operations on sets of boolean variables. Batfish includes classes for representing 
packets using BDDs and building constraints on them (Java to BDD), and extracting solutions (BDD to Java).

Representation:
* [`BDDFactory`](https://github.com/batfish/batfish/blob/master/projects/bdd/src/main/java/net/sf/javabdd/BDDFactory.java) and [BDD](https://github.com/batfish/batfish/blob/master/projects/bdd/src/main/java/net/sf/javabdd/BDD.java) are at the core of the symbolic analysis engine.
* [`BDDInteger`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/BDDInteger.java) and its subclasses represents symbolic integers and IP addresses as BDD bitvectors (i.e. an array of boolean variables).
* [`PrimedBDDInteger`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/PrimedBDDInteger.java) represents a symbolic integer that can be modified (e.g. by a NAT rule).
* [`BDDPacket`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/BDDPacket.java) represents a symbolic packet using BDDIntegers and individual BDD variables.
* [`BDDFiniteDomain`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/BDDFiniteDomain.java) represents a variable over a finite set of values.

Conversion to BDD constraints:
* [`BDDInteger`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/BDDInteger.java) has methods to create constraints on symbolic integers and IP address, including equality (`value`), inequality (`leq`, `geq`), etc.
* [`IpSpaceToBDD`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/IpSpaceToBDD.java) converts Batfish [IpSpaces](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/datamodel/IpSpace.java) to BDD.
* [`HeaderSpaceToBDD`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/HeaderSpaceToBDD.java) converts Batfish [Headerspace](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/datamodel/HeaderSpace.java) constraints (that may occur in filter definitions or question input, for example) to BDD.
* [`IpAccessListToBdd`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/IpAccessListToBdd.java) converts Batfish's vendor-independent [IpAccessLists](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/datamodel/IpAccessList.java) (i.e. filters) to BDD.

## Analyzing forwarding behavior
Analysis of forwarding behavior (reachability, loop detection, multipath consistency, etc) is implemented as
[data-flow analysis](https://en.wikipedia.org/wiki/Data-flow_analysis) using BDDs. We build a dataflow graph with nodes
representing stages of each device's forwarding pipeline, and edges encoding how data flows from stage to stage.

Example: `PreInInterface` represents the stage when a packet been received on an interface, just before the ingress filter is applied.
It has a transition to `PostInInterface` that adds a constraint that the ingress filter permits the packet, and 
it has a transition to `NodeDropAclIn` that adds a constraint that the ingress filter denies the packet.

[`BDDReachabilityAnalysisFactory`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/bddreachability/BDDReachabilityAnalysisFactory.java) computes a [BDDReachabilityAnalysis](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/bddreachability/BDDReachabilityAnalysis.java) graph from a set of inputs including:
* Batfish vendor-independent configurations.
* [`ForwardingAnalysis`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/datamodel/ForwardingAnalysis.java) synthesized from the FIBs produced by dataplane simulation.
* User-input parameters from
  [`HeaderConstraints`](https://pybatfish.readthedocs.io/en/latest/datamodel.html#pybatfish.datamodel.flow.HeaderConstraints)
 and
 [`PathConstraints`](https://pybatfish.readthedocs.io/en/latest/datamodel.html#pybatfish.datamodel.flow.PathConstraints).

### Encoding NAT
NAT rules are represented as input-output relationships over two integer/IP variables. 
[`PrimedBDDInteger`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/bdd/PrimedBDDInteger.java) defines the two variables,
and [`TransformationToTransition`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/bddreachability/transition/TransformationToTransition.java) converts
NAT rules ([`Transformation`](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/datamodel/transformation/Transformation.java) objects in Batfish vendor-independent model) to reachability graph transitions.

## Implementation Architecture

### Graph Construction Process
The symbolic engine builds a reachability graph where nodes represent forwarding pipeline stages and edges represent packet flow transitions:

1. **BDDReachabilityAnalysisFactory** orchestrates graph construction:
   - Converts ACLs and header constraints to BDD using `IpAccessListToBdd`, `HeaderSpaceToBDD`
   - Generates state nodes for each forwarding stage (interface processing, VRF lookup, dispositions)
   - Creates transition edges between states labeled with BDD constraints
   - Delegates FIB rule encoding to **BDDFibGenerator**

2. **BDDFibGenerator** encodes forwarding behavior:
   - Takes ForwardingAnalysis results (FIB entries, ARP reachability) as input
   - Generates edges representing packet flow through VRFs and interfaces
   - Handles disposition outcomes (accept, drop, exit network, neighbor unreachable)

### State Expression Pipeline
Packets flow through a standardized pipeline of state expressions:
- **Interface ingress**: `PreInInterface` → `PostInInterface` → `InterfaceAccept`
- **VRF processing**: `PostInVrf` → `PreOutVrf` → `VrfAccept`
- **Final dispositions**: `NodeAccept`, `NodeDropAclIn`, `ExitsNetwork`, etc.

### Analysis Execution
- **Fixpoint computation**: `BDDReachabilityUtils.fixpoint()` iteratively propagates BDD packet sets through the graph until convergence
- **Backward analysis** (default): More efficient for comprehensive queries - starts from target dispositions and computes which ingress locations can reach them
- **Forward analysis**: Available for tight source constraints - propagates from specific ingress points

### Solution Extraction
**BDDRepresentativePicker** converts BDD solution sets to concrete packet examples using `BDD.satOne()` to find satisfying assignments that represent actual flows.
