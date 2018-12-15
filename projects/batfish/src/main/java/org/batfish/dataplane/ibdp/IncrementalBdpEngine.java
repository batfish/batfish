package org.batfish.dataplane.ibdp;

import static org.batfish.common.topology.TopologyUtil.computeIpNodeOwners;
import static org.batfish.common.topology.TopologyUtil.computeIpVrfOwners;
import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;
import static org.batfish.common.util.CommonUtil.toImmutableSortedMap;
import static org.batfish.datamodel.bgp.BgpTopologyUtils.initBgpTopology;
import static org.batfish.dataplane.rib.AbstractRib.importRib;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.Network;
import com.google.common.graph.ValueGraph;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BdpOscillationException;
import org.batfish.common.Version;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.IncrementalBdpAnswerElement;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterface;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.batfish.dataplane.ibdp.schedule.IbdpSchedule;
import org.batfish.dataplane.ibdp.schedule.IbdpSchedule.Schedule;
import org.batfish.dataplane.rib.BgpRib;
import org.batfish.dataplane.rib.RibDelta;

class IncrementalBdpEngine {

  private int _numIterations;

  private final BatfishLogger _bfLogger;

  private final BiFunction<String, Integer, AtomicInteger> _newBatch;

  private final IncrementalDataPlaneSettings _settings;

  IncrementalBdpEngine(
      IncrementalDataPlaneSettings settings,
      BatfishLogger logger,
      BiFunction<String, Integer, AtomicInteger> newBatch) {
    _settings = settings;
    _bfLogger = logger;
    _newBatch = newBatch;
  }

  ComputeDataPlaneResult computeDataPlane(
      Map<String, Configuration> configurations,
      Topology topology,
      Set<BgpAdvertisement> externalAdverts) {
    _bfLogger.resetTimer();
    IncrementalDataPlane.Builder dpBuilder = IncrementalDataPlane.builder();
    _bfLogger.info("\nComputing Data Plane using iBDP\n");

    Map<Ip, Set<String>> ipOwners = computeIpNodeOwners(configurations, true);
    Map<Ip, Map<String, Set<String>>> ipVrfOwners =
        computeIpVrfOwners(true, computeNodeInterfaces(configurations));
    dpBuilder.setIpVrfOwners(ipVrfOwners);

    // Generate our nodes, keyed by name, sorted for determinism
    SortedMap<String, Node> nodes =
        toImmutableSortedMap(configurations.values(), Configuration::getHostname, Node::new);
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    dpBuilder.setNodes(nodes);
    dpBuilder.setTopology(topology);

    Network<EigrpInterface, EigrpEdge> eigrpTopology =
        EigrpTopology.initEigrpTopology(configurations, topology);

    /*
     * Run the data plane computation here:
     * - First, let the IGP routes converge
     * - Second, re-init BGP neighbors with reachability checks
     * - Third, let the EGP routes converge
     * - Finally, compute FIBs, return answer
     */
    IncrementalBdpAnswerElement answerElement = new IncrementalBdpAnswerElement();
    computeIgpDataPlane(nodes, topology, eigrpTopology, answerElement, networkConfigurations);
    computeFibs(nodes);

    IncrementalDataPlane dp = dpBuilder.build();

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        initBgpTopology(
            configurations, ipOwners, false, true, TracerouteEngineImpl.getInstance(), dp);

    Network<IsisNode, IsisEdge> isisTopology =
        IsisTopology.initIsisTopology(configurations, topology);

    boolean isOscillating =
        computeNonMonotonicPortionOfDataPlane(
            nodes,
            topology,
            externalAdverts,
            answerElement,
            true,
            bgpTopology,
            eigrpTopology,
            isisTopology,
            networkConfigurations,
            ipOwners);
    if (isOscillating) {
      // If we are oscillating here, network has no stable solution.
      throw new BdpOscillationException("Network has no stable solution");
    }

    // update the dataplane with bgpTopology.
    // this will cause forwarding analysis, etc to be reinitialized
    dp = dpBuilder.setBgpTopology(bgpTopology).build();

    if (_settings.getCheckBgpSessionReachability()) {
      computeFibs(nodes);
      bgpTopology =
          initBgpTopology(
              configurations, ipOwners, false, true, TracerouteEngineImpl.getInstance(), dp);
      // Update queues (if necessary) based on new neighbor relationships
      final ValueGraph<BgpPeerConfigId, BgpSessionProperties> finalBgpTopology = bgpTopology;
      nodes
          .values()
          .parallelStream()
          .forEach(
              n ->
                  n.getVirtualRouters().values().forEach(vr -> vr.initBgpQueues(finalBgpTopology)));
      // Do another pass on EGP computation in case any new sessions have been established
      computeNonMonotonicPortionOfDataPlane(
          nodes,
          topology,
          externalAdverts,
          answerElement,
          false,
          bgpTopology,
          eigrpTopology,
          isisTopology,
          networkConfigurations,
          ipOwners);
    }
    // Generate the answers from the computation, compute final FIBs
    computeFibs(nodes);
    answerElement.setVersion(Version.getVersion());
    _bfLogger.printElapsedTime();
    return new ComputeDataPlaneResult(answerElement, dp);
  }

  /**
   * Perform one iteration of the "dependent routes" dataplane computation. Dependent routes refers
   * to routes that could change because other routes have changed. For example, this includes:
   *
   * <ul>
   *   <li>static routes with next hop IP
   *   <li>aggregate routes
   *   <li>EGP routes (various protocols)
   * </ul>
   *
   * @param nodes nodes that are participating in the computation
   * @param iteration iteration number (for stats tracking)
   * @param allNodes all nodes in the network (for correct neighbor referencing)
   * @param bgpTopology the bgp peering relationships
   */
  private void computeDependentRoutesIteration(
      Map<String, Node> nodes,
      int iteration,
      Map<String, Node> allNodes,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology,
      NetworkConfigurations networkConfigurations) {

    // (Re)initialization of dependent route calculation
    nodes
        .values()
        .parallelStream()
        .flatMap(n -> n.getVirtualRouters().values().parallelStream())
        .forEach(VirtualRouter::reinitForNewIteration);

    // Static nextHopIp routes
    AtomicInteger recomputeStaticCompleted =
        _newBatch.apply(
            "Iteration " + iteration + ": Recompute static routes with next-hop IP", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.activateStaticRoutes();
              }
              recomputeStaticCompleted.incrementAndGet();
            });

    // Generated/aggregate routes
    AtomicInteger recomputeAggregateCompleted =
        _newBatch.apply(
            "Iteration " + iteration + ": Recompute aggregate/generated routes", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.recomputeGeneratedRoutes();
              }
              recomputeAggregateCompleted.incrementAndGet();
            });

    // EIGRP external routes
    // recompute exports
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.initEigrpExports(allNodes);
              }
            });

    // Re-propagate EIGRP exports
    AtomicBoolean eigrpExternalChanged = new AtomicBoolean(true);
    int eigrpExternalSubIterations = 0;
    while (eigrpExternalChanged.get()) {
      eigrpExternalSubIterations++;
      AtomicInteger propagateEigrpExternalCompleted =
          _newBatch.apply(
              "Iteration "
                  + iteration
                  + ": Propagate EIGRP external routes: subIteration: "
                  + eigrpExternalSubIterations,
              nodes.size());
      eigrpExternalChanged.set(false);
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  if (vr.propagateEigrpExternalRoutes(allNodes, networkConfigurations)) {
                    eigrpExternalChanged.set(true);
                  }
                }
                propagateEigrpExternalCompleted.incrementAndGet();
              });
    }

    // Re-initialize IS-IS exports.
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.initIsisExports(allNodes, networkConfigurations);
              }
            });
    // IS-IS route propagation
    AtomicBoolean isisChanged = new AtomicBoolean(true);
    int isisSubIterations = 0;
    while (isisChanged.get()) {
      isisSubIterations++;
      AtomicInteger propagateIsisCompleted =
          _newBatch.apply(
              "Iteration "
                  + iteration
                  + ": Propagate IS-IS routes: subIteration: "
                  + isisSubIterations,
              nodes.size());
      isisChanged.set(false);
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  Entry<RibDelta<IsisRoute>, RibDelta<IsisRoute>> p =
                      vr.propagateIsisRoutes(networkConfigurations);
                  if (p != null
                      && vr.unstageIsisRoutes(
                          allNodes, networkConfigurations, p.getKey(), p.getValue())) {
                    isisChanged.set(true);
                  }
                }
                propagateIsisCompleted.incrementAndGet();
              });
    }

    // OSPF external routes
    // recompute exports
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.initOspfExports(allNodes);
              }
            });

    // Re-propagate OSPF exports
    AtomicBoolean ospfExternalChanged = new AtomicBoolean(true);
    int ospfExternalSubIterations = 0;
    while (ospfExternalChanged.get()) {
      ospfExternalSubIterations++;
      AtomicInteger propagateOspfExternalCompleted =
          _newBatch.apply(
              "Iteration "
                  + iteration
                  + ": Propagate OSPF external routes: subIteration: "
                  + ospfExternalSubIterations,
              nodes.size());
      ospfExternalChanged.set(false);
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  Entry<RibDelta<OspfExternalType1Route>, RibDelta<OspfExternalType2Route>> p =
                      vr.propagateOspfExternalRoutes(allNodes);
                  if (p != null
                      && vr.unstageOspfExternalRoutes(allNodes, p.getKey(), p.getValue())) {
                    ospfExternalChanged.set(true);
                  }
                }
                propagateOspfExternalCompleted.incrementAndGet();
              });
    }

    computeIterationOfBgpRoutes(nodes, iteration, allNodes, bgpTopology, networkConfigurations);
  }

  private void computeIterationOfBgpRoutes(
      Map<String, Node> nodes,
      int iteration,
      Map<String, Node> allNodes,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology,
      NetworkConfigurations networkConfigurations) {
    // BGP routes
    // first let's initialize nodes-level generated/aggregate routes
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                if (vr._vrf.getBgpProcess() != null) {
                  vr.initBgpAggregateRoutes();
                }
              }
            });
    AtomicInteger propagateBgpCompleted =
        _newBatch.apply("Iteration " + iteration + ": Propagate BGP routes", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                BgpProcess proc = vr._vrf.getBgpProcess();
                if (proc == null) {
                  continue;
                }
                Map<BgpRib, RibDelta<BgpRoute>> deltas =
                    vr.processBgpMessages(bgpTopology, networkConfigurations);
                vr.finalizeBgpRoutesAndQueueOutgoingMessages(
                    deltas, allNodes, bgpTopology, networkConfigurations);
              }
              propagateBgpCompleted.incrementAndGet();
            });
  }

  /**
   * Run {@link VirtualRouter#computeFib} on all of the given nodes (and their virtual routers)
   *
   * @param nodes mapping of node names to node instances
   */
  private void computeFibs(Map<String, Node> nodes) {
    AtomicInteger completed = _newBatch.apply("Computing FIBs", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.computeFib();
              }
              completed.incrementAndGet();
            });
  }

  /**
   * Compute the IGP portion of the dataplane.
   *
   * @param nodes A dictionary of configuration-wrapping Bdp nodes keyed by name
   * @param topology The topology representing Layer 3 adjacencies between interface of the nodes
   * @param eigrpTopology The topology representing EIGRP adjacencies
   * @param ae The output answer element in which to store a report of the computation. Also
   *     contains the current recovery iteration.
   * @param networkConfigurations All configurations in the network
   */
  private void computeIgpDataPlane(
      SortedMap<String, Node> nodes,
      Topology topology,
      Network<EigrpInterface, EigrpEdge> eigrpTopology,
      IncrementalBdpAnswerElement ae,
      NetworkConfigurations networkConfigurations) {

    int numOspfInternalIterations;
    int numEigrpInternalIterations;

    /*
     * For each virtual router, setup the initial easy-to-do routes, init protocol-based RIBs,
     * queue outgoing messages to neighbors
     */
    AtomicInteger initialCompleted =
        _newBatch.apply(
            "Compute initial connected and static routes, eigrp setup, ospf setup, bgp setup",
            nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.initForIgpComputation();
              }
              initialCompleted.incrementAndGet();
            });

    // EIGRP internal routes
    numEigrpInternalIterations =
        initEigrpInternalRoutes(nodes, eigrpTopology, networkConfigurations);

    // OSPF internal routes
    numOspfInternalIterations = initOspfInternalRoutes(nodes, topology);

    // RIP internal routes
    initRipInternalRoutes(nodes, topology);

    // Activate static routes
    AtomicInteger staticRoutesAfterIgp =
        _newBatch.apply("Compute static routes after IGP protocol convergence", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                importRib(vr._mainRib, vr._independentRib);
                vr.activateStaticRoutes();
              }
              staticRoutesAfterIgp.incrementAndGet();
            });

    // Set iteration stats in the answer
    ae.setOspfInternalIterations(numOspfInternalIterations);
    ae.setEigrpInternalIterations(numEigrpInternalIterations);
  }

  /**
   * Compute the EGP portion of the dataplane. Must be called after IGP has converged.
   *
   * @param nodes A dictionary of configuration-wrapping Bdp nodes keyed by name
   * @param topology The topology representing Layer 3 adjacencies between interface of the nodes
   * @param externalAdverts the set of external BGP advertisements
   * @param ae The output answer element in which to store a report of the computation. Also
   *     contains the current recovery iteration.
   * @param eigrpTopology The topology representing EIGRP adjacencies
   * @param ipOwners The ip owner mapping
   * @return true iff the computation is oscillating
   */
  private boolean computeNonMonotonicPortionOfDataPlane(
      SortedMap<String, Node> nodes,
      Topology topology,
      Set<BgpAdvertisement> externalAdverts,
      IncrementalBdpAnswerElement ae,
      boolean firstPass,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology,
      Network<EigrpInterface, EigrpEdge> eigrpTopology,
      Network<IsisNode, IsisEdge> isisTopology,
      NetworkConfigurations networkConfigurations,
      Map<Ip, Set<String>> ipOwners) {

    /*
     * Initialize all routers and their message queues (can be done as parallel as possible)
     */
    if (firstPass) {
      AtomicInteger setupCompleted =
          _newBatch.apply("Initialize virtual routers for iBDP-external", nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  vr.initForEgpComputation(
                      nodes, topology, bgpTopology, eigrpTopology, isisTopology);
                }
                setupCompleted.incrementAndGet();
              });

      // Queue initial outgoing messages
      AtomicInteger queueInitial = _newBatch.apply("Queue initial bgp messages", nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  vr.initBaseBgpRibs(
                      externalAdverts, ipOwners, nodes, bgpTopology, networkConfigurations);
                  vr.queueInitialBgpMessages(bgpTopology, nodes, networkConfigurations);
                }
                queueInitial.incrementAndGet();
              });
    }

    /*
     * Setup maps to track iterations. We need this for oscillation detection.
     * Specifically, if we detect that an iteration hashcode (a hash of all the nodes' RIBs)
     * has been previously encountered, we switch our schedule to a more restrictive one.
     */

    Map<Integer, SortedSet<Integer>> iterationsByHashCode = new HashMap<>();

    AtomicBoolean dependentRoutesChanged = new AtomicBoolean(false);

    // Go into iteration mode, until the routes converge (or oscillation is detected)
    do {
      _numIterations++;

      AtomicBoolean currentChangedMonitor;
      currentChangedMonitor = dependentRoutesChanged;
      currentChangedMonitor.set(false);

      // Compute node schedule
      IbdpSchedule schedule = IbdpSchedule.getSchedule(_settings, nodes, bgpTopology);

      // compute dependent routes for each allowable set of nodes until we cover all nodes
      while (schedule.hasNext()) {
        Map<String, Node> iterationNodes = schedule.next();
        computeDependentRoutesIteration(
            iterationNodes, _numIterations, nodes, bgpTopology, networkConfigurations);
      }

      /*
       * Perform various bookkeeping at the end of the iteration:
       * - Collect sizes of certain RIBs this iteration
       * - Compute iteration hashcode
       * - Check for oscillations
       */
      computeIterationStatistics(nodes, ae, _numIterations);

      // Check to see if hash has changed
      AtomicInteger checkFixedPointCompleted =
          _newBatch.apply(
              "Iteration " + _numIterations + ": Check if fixed-point reached", nodes.size());

      // This hashcode uniquely identifies the iteration (i.e., network state)
      int iterationHashCode = computeIterationHashCode(nodes);
      SortedSet<Integer> iterationsWithThisHashCode =
          iterationsByHashCode.computeIfAbsent(iterationHashCode, h -> new TreeSet<>());

      if (iterationsWithThisHashCode.isEmpty()) {
        iterationsWithThisHashCode.add(_numIterations);
      } else {
        // If oscillation detected, switch to a more restrictive schedule
        if (_settings.getScheduleName() != Schedule.NODE_SERIALIZED) {
          _bfLogger.debugf(
              "Switching to a more restrictive schedule %s, iteration %d\n",
              Schedule.NODE_SERIALIZED, _numIterations);
          _settings.setScheduleName(Schedule.NODE_SERIALIZED);
        } else {
          return true; // Found an oscillation
        }
      }

      compareToPreviousIteration(nodes, dependentRoutesChanged, checkFixedPointCompleted);
    } while (!areQueuesEmpty(nodes) || dependentRoutesChanged.get());

    ae.setDependentRoutesIterations(_numIterations);
    return false; // No oscillations
  }

  private static void compareToPreviousIteration(
      Map<String, Node> nodes,
      AtomicBoolean dependentRoutesChanged,
      AtomicInteger checkFixedPointCompleted) {
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                if (vr.hasOutstandingRoutes()) {
                  dependentRoutesChanged.set(true);
                }
              }
              checkFixedPointCompleted.incrementAndGet();
            });
  }

  /**
   * Check that the routers have processed all messages, queues are empty and there is nothing else
   * to do (i.e., we've converged to a stable network solution)
   *
   * @param nodes nodes to check
   * @return true iff all queues are empty
   */
  private boolean areQueuesEmpty(Map<String, Node> nodes) {
    AtomicInteger computeQueuesAreEmpty =
        _newBatch.apply("Check for convergence (are queues empty?)", nodes.size());
    AtomicBoolean areEmpty = new AtomicBoolean(true);
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                if (!vr.hasProcessedAllMessages()) {
                  areEmpty.set(false);
                }
              }
              computeQueuesAreEmpty.incrementAndGet();
            });

    return areEmpty.get();
  }

  /**
   * Compute the hashcode that uniquely identifies the state of the network at a given iteration
   *
   * @param nodes map of nodes, keyed by hostname
   * @return integer hashcode
   */
  private static int computeIterationHashCode(Map<String, Node> nodes) {
    return nodes
        .values()
        .parallelStream()
        .flatMap(node -> node.getVirtualRouters().values().stream())
        .mapToInt(VirtualRouter::computeIterationHashCode)
        .sum();
  }

  private static void computeIterationStatistics(
      Map<String, Node> nodes, IncrementalBdpAnswerElement ae, int dependentRoutesIterations) {
    int numBgpBestPathRibRoutes =
        nodes
            .values()
            .stream()
            .flatMap(n -> n.getVirtualRouters().values().stream())
            .mapToInt(vr -> vr.getBgpRib().getBestPathRoutes().size())
            .sum();
    ae.getBgpBestPathRibRoutesByIteration().put(dependentRoutesIterations, numBgpBestPathRibRoutes);
    int numBgpMultipathRibRoutes =
        nodes
            .values()
            .stream()
            .flatMap(n -> n.getVirtualRouters().values().stream())
            .mapToInt(vr -> vr.getBgpRib().getRoutes().size())
            .sum();
    ae.getBgpMultipathRibRoutesByIteration()
        .put(dependentRoutesIterations, numBgpMultipathRibRoutes);
    int numMainRibRoutes =
        nodes
            .values()
            .stream()
            .flatMap(n -> n.getVirtualRouters().values().stream())
            .mapToInt(vr -> vr._mainRib.getRoutes().size())
            .sum();
    ae.getMainRibRoutesByIteration().put(dependentRoutesIterations, numMainRibRoutes);
  }

  /**
   * Return the main RIB routes for each node. Map structure: Hostname -&gt; VRF name -&gt; Set of
   * routes
   */
  static SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes(
      IncrementalDataPlane dp) {
    // Scan through all Nodes and their virtual routers, retrieve main rib routes
    return toImmutableSortedMap(
        dp.getNodes(),
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry ->
                    ImmutableSortedSet.copyOf(vrfEntry.getValue().getMainRib().getRoutes())));
  }

  /**
   * Run the IGP EIGRP computation until convergence.
   *
   * @param nodes list of nodes for which to initialize the EIGRP routes
   * @param eigrpTopology The topology representing EIGRP adjacencies
   * @param networkConfigurations All configurations in the network
   * @return the number of iterations it took for internal EIGRP routes to converge
   */
  private int initEigrpInternalRoutes(
      Map<String, Node> nodes,
      Network<EigrpInterface, EigrpEdge> eigrpTopology,
      NetworkConfigurations networkConfigurations) {
    AtomicBoolean eigrpInternalChanged = new AtomicBoolean(true);
    int eigrpInternalIterations = 0;
    while (eigrpInternalChanged.get()) {
      eigrpInternalIterations++;
      eigrpInternalChanged.set(false);

      AtomicInteger eigrpInternalCompleted =
          _newBatch.apply(
              "Compute EIGRP Internal routes: iteration " + eigrpInternalIterations, nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  if (vr.propagateEigrpInternalRoutes(
                      nodes, eigrpTopology, networkConfigurations)) {
                    eigrpInternalChanged.set(true);
                  }
                }
                eigrpInternalCompleted.incrementAndGet();
              });
      AtomicInteger eigrpInternalUnstageCompleted =
          _newBatch.apply(
              "Unstage EIGRP Internal routes: iteration " + eigrpInternalIterations, nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  vr.unstageEigrpInternalRoutes();
                }
                eigrpInternalUnstageCompleted.incrementAndGet();
              });
    }
    AtomicInteger eigrpInternalImportCompleted =
        _newBatch.apply("Import EIGRP Internal routes", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.importEigrpInternalRoutes();
              }
              eigrpInternalImportCompleted.incrementAndGet();
            });
    return eigrpInternalIterations;
  }

  /**
   * Run the IGP OSPF computation until convergence.
   *
   * @param nodes list of nodes for which to initialize the OSPF routes
   * @param topology the network topology
   * @return the number of iterations it took for internal OSPF routes to converge
   */
  private int initOspfInternalRoutes(Map<String, Node> nodes, Topology topology) {
    AtomicBoolean ospfInternalChanged = new AtomicBoolean(true);
    int ospfInternalIterations = 0;
    while (ospfInternalChanged.get()) {
      ospfInternalIterations++;
      ospfInternalChanged.set(false);

      AtomicInteger ospfInterAreaSummaryCompleted =
          _newBatch.apply(
              "Compute OSPF Inter-area summaries: iteration " + ospfInternalIterations,
              nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  if (vr.computeInterAreaSummaries()) {
                    ospfInternalChanged.set(true);
                  }
                }
                ospfInterAreaSummaryCompleted.incrementAndGet();
              });
      AtomicInteger ospfInternalCompleted =
          _newBatch.apply(
              "Compute OSPF Internal routes: iteration " + ospfInternalIterations, nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  if (vr.propagateOspfInternalRoutes(nodes, topology)) {
                    ospfInternalChanged.set(true);
                  }
                }
                ospfInternalCompleted.incrementAndGet();
              });
      AtomicInteger ospfInternalUnstageCompleted =
          _newBatch.apply(
              "Unstage OSPF Internal routes: iteration " + ospfInternalIterations, nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  vr.unstageOspfInternalRoutes();
                }
                ospfInternalUnstageCompleted.incrementAndGet();
              });
    }
    AtomicInteger ospfInternalImportCompleted =
        _newBatch.apply("Import OSPF Internal routes", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                vr.importOspfInternalRoutes();
              }
              ospfInternalImportCompleted.incrementAndGet();
            });
    return ospfInternalIterations;
  }

  /**
   * Run the IGP RIP computation until convergence
   *
   * @param nodes nodes for which to initialize the routes, keyed by name
   * @param topology network topology
   * @return number of iterations it took to complete the initialization
   */
  private int initRipInternalRoutes(SortedMap<String, Node> nodes, Topology topology) {
    /*
     * Consider this method to be a simulation within a simulation. Since RIP routes are not
     * affected by other protocols, we propagate all RIP routes amongst the nodes prior to
     * processing other routing protocols (e.g., OSPF & BGP)
     */
    AtomicBoolean ripInternalChanged = new AtomicBoolean(true);
    int ripInternalIterations = 0;
    while (ripInternalChanged.get()) {
      ripInternalIterations++;
      ripInternalChanged.set(false);
      AtomicInteger ripInternalCompleted =
          _newBatch.apply(
              "Compute RIP Internal routes: iteration " + ripInternalIterations, nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  if (vr.propagateRipInternalRoutes(nodes, topology)) {
                    ripInternalChanged.set(true);
                  }
                }
                ripInternalCompleted.incrementAndGet();
              });
      AtomicInteger ripInternalUnstageCompleted =
          _newBatch.apply(
              "Unstage RIP Internal routes: iteration " + ripInternalIterations, nodes.size());
      nodes
          .values()
          .parallelStream()
          .forEach(
              n -> {
                for (VirtualRouter vr : n.getVirtualRouters().values()) {
                  vr.unstageRipInternalRoutes();
                }
                ripInternalUnstageCompleted.incrementAndGet();
              });
    }
    AtomicInteger ripInternalImportCompleted =
        _newBatch.apply("Import RIP Internal routes", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n.getVirtualRouters().values()) {
                importRib(vr._ripRib, vr._ripInternalRib);
                importRib(vr._independentRib, vr._ripRib);
              }
              ripInternalImportCompleted.incrementAndGet();
            });
    return ripInternalIterations;
  }
}
