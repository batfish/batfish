# Data plane generation
Once the vendor independent (VI) model is fully generated and initial layer 3 topology is computed, Batfish is ready to generate the dataplane.

### What is the dataplane?
The output of Batfish's dataplane computation is a `ComputeDataPlaneResult`, which contains:
* A [`DataPlane`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/DataPlane.java) describing the  routing and forwarding behavior of the network. This includes:
  * [FIBs](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/Fib.java) and [forwarding analysis](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/ForwardingAnalysis.java). This information is used for computing [packet forwarding](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html) results such as traceroute and reachability.
  * Routes contained in routing information bases (main, BGP, and EVPN RIBs). This information is used for answering [routes questions](https://pybatfish.readthedocs.io/en/latest/notebooks/routingTables.html).
* A [`TopologyContainer`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/topology/TopologyContainer.java) with the finalized L1-L3, overlay, and routing protocol topologies.

The `DataPlane` and topologies are serialized to disk when dataplane computation is complete.

### Oscillations
Some networks oscillate and do not have a stable state. If Batfish detects oscillation during dataplane computation, it raises a [`BdpOscillationException`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/BdpOscillationException.java) (BDP stands for Batfish dataplane). No dataplane is generated and dataplane-dependent questions cannot be run.

### Nondeterminism
Some networks have nondeterministic final routing state, often caused by tiebreaking based on arrival order. However, Batfish's dataplane computation is deterministic: it will always produce the same dataplane given the same snapshot. The RIBs and FIBs Batfish produces for nondeterministic networks may or may not be identical to those in the live network.

### How is the dataplane computed?
Batfish's dataplane computation algorithm is called incremental Batfish dataplane (IBDP). Alternative algorithms could be introduced by adding new implementations of [`DataPlanePlugin`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/plugin/DataPlanePlugin.java), but currently IBDP is the default and only option. This section describes IBDP.

#### Code pointers
IBDP is implemented by [`IncrementalDataPlanePlugin`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/IncrementalDataPlanePlugin.java), which uses [`IncrementalDataPlaneEngine`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/IncrementalBdpEngine.java) for the main computation.

Within IBDP, there are specialized representations for devices and VRFs:
* A [`Node`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/Node.java) represents each device.
* A [`VirtualRouter`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/VirtualRouter.java) represents each VRF. Most dataplane computation steps are parallelized across `VirtualRouter` objects. Each `VirtualRouter` has a main [`Rib`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/rib/Rib.java) containing the current active routes (corresponding to Cisco-syntax `show ip route`), as well as protocol-specific RIBs containing the routes of each active protocol.
* In VRFs running BGP, EIGRP, or OSPF, the `VirtualRouter` does not handle the protocol RIB itself, but instead has [`RoutingProcess`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/RoutingProcess.java) objects of the corresponding type to handle the routes for each process.

The result of the dataplane computation is an [`IncrementalDataPlane`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/IncrementalDataPlane.java), or IDP.

#### How it works
IBDP is based on incremental fixed point operations:
* **Incremental**: The operation breaks down into "rounds" or "iterations". In each round, the network reacts (e.g. generates and exports routes) based on what changed in the last round.
* **Fixed point**: The computation is complete when the network comes to a fixed point, meaning that the topology and routing state at the end of the final round is identical to the state at the end of the previous (second-to-last) round. In some cases, Batfish determines that a fixed point has been reached without running the final round, by observing that there would be no changes to process in the upcoming round.

There are several incremental fixed point operations involved in IBDP. Here is an overview of the dataplane computation process.
1. `IncrementalBdpEngine.computeIgpDataPlane`
   * On each `VirtualRouter`, compute initial IGP routes, independent of neighboring devices. This includes connected, local, and static routes, OSFP intra-area routes, RIP internal routes, and EIGRP internal routes.
   * Propagate OSPF internal routes between devices until an OSPF fixed point is reached.
   * Propagate RIP internal routes between devices until a RIP fixed point is reached.
1. Stage any [user-provided external BGP advertisements](https://pybatfish.readthedocs.io/en/latest/formats.html?highlight=external%20advertisements#external-bgp-announcements) to be processed by the receiving [`BgpRoutingProcess`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/BgpRoutingProcess.java) in the first BGP round.
1. Compute topology context based on the routes so far. (See [topology page](topology.md) for more information about topology computation.)
1. Compute other non-route information that can affect route presence. For example:
   * Static routes can be configured to depend on reachability to a given IP. When such routes are present, IBDP computes reachability to those target IPs at this point.
   * HMM and kernel routes are dependent on what IPs each interface or VRF owns. IP ownership is computed at this point.
1. **Start of topology fixed point operation.** This is the main fixed point operation of IBDP, and the remaining steps compose one topology iteration. When it converges, the dataplane is complete.
1. `IncrementalBdpEngine.computeNonMonotonicPortionOfDataPlane`: Routing/EGP fixed point operation. This operation brings all devices' routes to a fixed point based on the current topology, reachability, and IP ownership state.
   * Compute HMM and kernel routes.
   * Redistribute: In each `VirtualRouter`, perform redistribution of routes into BGP, OSPF, EIGRP, and IS-IS. Redistribution in Batfish is a local operation where a redistribution-specific [`RoutingPolicy`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/routing_policy/RoutingPolicy.java) is applied to main RIB routes, and any routes that pass the policy are added to the target protocol's RIB. From there they may be exported later.
   * Update resolvable routes in protocol RIBs, specifically BGP (the only protocol that checks routes' resolvability). BGP routes are activated or deactivated based on whether their next hop IPs are resolvable in the main RIB.
   * Queue cross-VRF imports. `VirtualRouter._crossVrfIncomingRoutes` is a queue of routes being sent to that VRF from other VRFs, and it is populated in this step. This is only for leaking routes between main RIBs; there is a separate pipeline for leaking between BGP RIBs.
   * `IncrementalBdpEngine.computeDependentRoutesIteration`: Compute and propagate dependent routes. "Dependent" means these routes can come and go depending on the current topology or available routes; they are not guaranteed based on the configuration. This includes conditional static routes, generated routes, EIGRP routes, IS-IS routes, OSPF external routes, BGP routes, and the cross-VRF routes that were queued in the previous step.
   * At this point the routing changes for this EGP round are done. Check for an oscillation:
      * Compute an iteration hashcode representing the current state of the network. Check if this hashcode has been encountered before. If so, raise a `BdpOscillationException`.
   * Check if a routing fixed point has been reached, based on `VirtualRouter.isDirty()`. This checks whether the VRF has any route changes to process or incoming routes queued. If any VRF is dirty, IBDP enters another EGP round. Otherwise, the EGP fixed point operation is complete for this topology round.
1. Repeat steps 4 and 5 to compute new topology and supplemental information based on the new routing state.
1. Check if the new topology and supplemental information is all the same as that of the current round. If so, a fixed point has been reached and the dataplane is complete. Otherwise, IBDP enters another topology iteration.
   * No iteration hashcodes are generated for topology iterations, so oscillations cannot be detected as they are for routing iterations. Instead, if the fixed point operation exceeds `IncrementalBdpEngine.MAX_TOPOLOGY_ITERATIONS` iterations (currently set to 10), IBDP stops and raises a `BdpOscillationException`.

#### Node scheduling
In some networks, processing nodes in parallel during a routing iteration can cause false positive oscillations. However, parallelization across nodes is safe in most cases, and greatly improves performance.

The [`IbdpSchedule`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/schedule/IbdpSchedule.java) dictates which nodes may be processed in parallel during routing iterations. By default, IBDP uses graph coloring to process non-adjacent nodes in parallel (see [`NodeColoredSchedule`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/schedule/NodeColoredSchedule.java)). Nodes are considered adjacent if they have an OSPF adjacency or established BGP session.

However, parallelization according to graph coloring does not completely prevent false positive oscillations. If a routing oscillation occurs, IBDP switches to a fully restrictive [`NodeSerializedSchedule`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/dataplane/ibdp/schedule/NodeSerializedSchedule.java), where no parallelization is allowed across nodes. If the oscillation persists, then IBDP raises a `BdpOscillationException`.

### When is the dataplane computed?
The dataplane is not automatically computed as part of Pybatfish's `bf.init_snapshot(SNAPSHOT_PATH)` because computing it can require several minutes on larger networks, and many questions can be run without it. The computation is triggered automatically the first time the user runs a dataplane-dependent question on a new snapshot.

#### Using `bf.generate_dataplane()`
Dataplane computation can be triggered manually with `bf.generate_dataplane()`. Even if Batfish has already computed a dataplane for the current snapshot, running this command will cause Batfish to recompute and overwrite it. This is useful for modifying or debugging dataplane computation, since you only need to run `init_snapshot` once.

If dataplane computation crashes, `bf.generate_dataplane()` provides a better error message than running a dataplane-dependent question.

#### Dataplane reuse
When Batfish computes the dataplane, it stores the resulting [DataPlane](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/DataPlane.java) to disk and reuses it for future dataplane-dependent questions on that snapshot. The dataplane will never be recomputed unless the user runs `bf.generate_dataplane()`.

No dataplane computation is reused across snapshots, even if they are very similar or one is forked from another. Batfish assumes any configuration change can cause route changes across the whole network.
