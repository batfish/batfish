package org.batfish.ibdp;

import static org.batfish.common.util.CommonUtil.initBgpTopology;
import static org.batfish.ibdp.AbstractRib.importRib;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import com.google.common.graph.Network;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BdpOscillationException;
import org.batfish.common.Version;
import org.batfish.common.plugin.FlowProcessor;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSession;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.BdpAnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.ibdp.schedule.IbdpSchedule;
import org.batfish.ibdp.schedule.IbdpSchedule.Schedule;

public class IncrementalBdpEngine implements FlowProcessor {

  private static final String TRACEROUTE_INGRESS_NODE_INTERFACE_NAME =
      "traceroute_source_interface";

  private static final String TRACEROUTE_INGRESS_NODE_NAME = "traceroute_source_node";

  private int _numIterations;

  /**
   * Applies the given list of source NAT rules to the given flow and returns the new transformed
   * flow. If {@code sourceNats} is null, empty, or does not contain any ACL rules matching the
   * {@link Flow}, the original flow is returned.
   *
   * <p>Each {@link SourceNat} is expected to be valid: it must have a NAT IP or pool.
   */
  static Flow applySourceNat(
      Flow flow,
      @Nullable String srcInterface,
      Map<String, IpAccessList> aclDefinitions,
      @Nullable List<SourceNat> sourceNats) {
    if (CommonUtil.isNullOrEmpty(sourceNats)) {
      return flow;
    }
    Optional<SourceNat> matchingSourceNat =
        sourceNats
            .stream()
            .filter(
                sourceNat ->
                    sourceNat.getAcl() != null
                        && sourceNat.getAcl().filter(flow, srcInterface, aclDefinitions).getAction()
                            != LineAction.REJECT)
            .findFirst();
    if (!matchingSourceNat.isPresent()) {
      // No NAT rule matched.
      return flow;
    }
    SourceNat sourceNat = matchingSourceNat.get();
    Ip natPoolStartIp = sourceNat.getPoolIpFirst();
    if (natPoolStartIp == null) {
      throw new BatfishException(
          String.format(
              "Error processing Source NAT rule %s: missing NAT address or pool", sourceNat));
    }
    Flow.Builder transformedFlowBuilder = new Flow.Builder(flow);
    transformedFlowBuilder.setSrcIp(natPoolStartIp);
    return transformedFlowBuilder.build();
  }

  private final BatfishLogger _bfLogger;

  private final BiFunction<String, Integer, AtomicInteger> _newBatch;

  private final IncrementalDataPlaneSettings _settings;

  public IncrementalBdpEngine(
      IncrementalDataPlaneSettings settings,
      BatfishLogger logger,
      BiFunction<String, Integer, AtomicInteger> newBatch) {
    _settings = settings;
    _bfLogger = logger;
    _newBatch = newBatch;
  }

  private void collectFlowTraces(
      IncrementalDataPlane dp,
      String currentNodeName,
      Set<Edge> visitedEdges,
      List<FlowTraceHop> hopsSoFar,
      Set<FlowTrace> flowTraces,
      Flow originalFlow,
      Flow transformedFlow,
      boolean ignoreAcls) {
    Ip dstIp = transformedFlow.getDstIp();
    Set<String> dstIpOwners = dp._ipOwners.get(dstIp);
    if (dstIpOwners != null && dstIpOwners.contains(currentNodeName)) {
      FlowTrace trace =
          new FlowTrace(FlowDisposition.ACCEPTED, hopsSoFar, FlowDisposition.ACCEPTED.toString());
      flowTraces.add(trace);
    } else {
      Node currentNode = dp._nodes.get(currentNodeName);
      Map<String, IpAccessList> aclDefinitions = currentNode._c.getIpAccessLists();
      String vrfName;
      String srcInterface;
      if (hopsSoFar.isEmpty()) {
        vrfName = transformedFlow.getIngressVrf();
        srcInterface = null;
      } else {
        FlowTraceHop lastHop = hopsSoFar.get(hopsSoFar.size() - 1);
        srcInterface = lastHop.getEdge().getInt2();
        vrfName = currentNode._c.getInterfaces().get(srcInterface).getVrf().getName();
      }
      VirtualRouter currentVirtualRouter = currentNode._virtualRouters.get(vrfName);
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute =
          currentVirtualRouter._fib.getNextHopInterfacesByRoute(dstIp);
      Map<String, Map<Ip, Set<AbstractRoute>>> nextHopInterfacesWithRoutes =
          currentVirtualRouter._fib.getNextHopInterfaces(dstIp);
      if (!nextHopInterfacesWithRoutes.isEmpty()) {
        for (String nextHopInterfaceName : nextHopInterfacesWithRoutes.keySet()) {
          // SortedSet<String> routesForThisNextHopInterface = new
          // TreeSet<>(
          // nextHopInterfacesWithRoutes.get(nextHopInterfaceName)
          // .stream().map(ar -> ar.toString())
          // .collect(Collectors.toSet()));
          SortedSet<String> routesForThisNextHopInterface = new TreeSet<>();
          Ip finalNextHopIp = null;
          for (Entry<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> e :
              nextHopInterfacesByRoute.entrySet()) {
            AbstractRoute routeCandidate = e.getKey();
            Map<String, Map<Ip, Set<AbstractRoute>>> routeCandidateNextHopInterfaces = e.getValue();
            if (routeCandidateNextHopInterfaces.containsKey(nextHopInterfaceName)) {
              Ip nextHopIp = routeCandidate.getNextHopIp();
              if (!nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
                Set<Ip> finalNextHopIps =
                    routeCandidateNextHopInterfaces.get(nextHopInterfaceName).keySet();
                if (finalNextHopIps.size() > 1) {
                  throw new BatfishException(
                      "Can not currently handle multiple final next hop ips across multiple "
                          + "routes leading to one next hop interface");
                }
                Ip newFinalNextHopIp = finalNextHopIps.iterator().next();
                if (finalNextHopIp != null && !newFinalNextHopIp.equals(finalNextHopIp)) {
                  throw new BatfishException(
                      "Can not currently handle multiple final next hop ips for same next hop "
                          + "interface");
                }
                finalNextHopIp = newFinalNextHopIp;
              }
              routesForThisNextHopInterface.add(routeCandidate + "_fnhip:" + finalNextHopIp);
            }
          }
          NodeInterfacePair nextHopInterface =
              new NodeInterfacePair(currentNodeName, nextHopInterfaceName);
          if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
            List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
            Edge newEdge =
                new Edge(
                    nextHopInterface,
                    new NodeInterfacePair(
                        Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
            FlowTraceHop newHop =
                new FlowTraceHop(
                    newEdge, routesForThisNextHopInterface, hopFlow(originalFlow, transformedFlow));
            newHops.add(newHop);
            FlowTrace nullRouteTrace =
                new FlowTrace(
                    FlowDisposition.NULL_ROUTED, newHops, FlowDisposition.NULL_ROUTED.toString());
            flowTraces.add(nullRouteTrace);
          } else {
            Interface outgoingInterface =
                dp._nodes
                    .get(nextHopInterface.getHostname())
                    ._c
                    .getInterfaces()
                    .get(nextHopInterface.getInterface());

            // Apply any relevant source NAT rules.
            transformedFlow =
                applySourceNat(
                    transformedFlow,
                    srcInterface,
                    aclDefinitions,
                    outgoingInterface.getSourceNats());

            SortedSet<Edge> edges = dp._topology.getInterfaceEdges().get(nextHopInterface);
            if (edges != null) {
              boolean continueToNextNextHopInterface = false;
              continueToNextNextHopInterface =
                  processCurrentNextHopInterfaceEdges(
                      dp,
                      currentNodeName,
                      visitedEdges,
                      hopsSoFar,
                      flowTraces,
                      originalFlow,
                      transformedFlow,
                      srcInterface,
                      aclDefinitions,
                      dstIp,
                      dstIpOwners,
                      nextHopInterfaceName,
                      routesForThisNextHopInterface,
                      finalNextHopIp,
                      nextHopInterface,
                      edges,
                      true,
                      ignoreAcls);
              if (continueToNextNextHopInterface) {
                continue;
              }
            } else {
              /*
               * Should only get here for delta environment where
               * non-flow-sink interface from base has no edges in delta
               */
              Edge neighborUnreachbleEdge =
                  new Edge(
                      nextHopInterface,
                      new NodeInterfacePair(
                          Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
              FlowTraceHop neighborUnreachableHop =
                  new FlowTraceHop(
                      neighborUnreachbleEdge,
                      routesForThisNextHopInterface,
                      hopFlow(originalFlow, transformedFlow));
              List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
              newHops.add(neighborUnreachableHop);
              /* Check if denied out. If not, make standard neighbor-unreachable trace. */
              IpAccessList outFilter = outgoingInterface.getOutgoingFilter();
              boolean denied = false;
              if (outFilter != null) {
                FlowDisposition disposition = FlowDisposition.DENIED_OUT;
                denied =
                    flowTraceDeniedHelper(
                        flowTraces,
                        originalFlow,
                        transformedFlow,
                        srcInterface,
                        aclDefinitions,
                        newHops,
                        outFilter,
                        disposition);
              }
              if (!denied) {
                FlowTrace trace =
                    new FlowTrace(
                        FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
                        newHops,
                        FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK.toString());
                flowTraces.add(trace);
              }
            }
          }
        }
      } else {
        FlowTrace trace =
            new FlowTrace(FlowDisposition.NO_ROUTE, hopsSoFar, FlowDisposition.NO_ROUTE.toString());
        flowTraces.add(trace);
      }
    }
  }

  IncrementalDataPlane computeDataPlane(
      boolean differentialContext,
      Map<String, Configuration> configurations,
      Topology topology,
      Set<BgpAdvertisement> externalAdverts,
      BdpAnswerElement ae) {
    _bfLogger.resetTimer();
    IncrementalDataPlane dp = new IncrementalDataPlane();
    _bfLogger.info("\nComputing Data Plane using iBDP\n");

    Map<Ip, Set<String>> ipOwners = CommonUtil.computeIpOwners(configurations, true);
    Map<Ip, String> ipOwnersSimple = CommonUtil.computeIpOwnersSimple(ipOwners);
    dp.initIpOwners(configurations, ipOwners, ipOwnersSimple);

    // Generate our nodes, keyed by name, sorted for determinism
    ImmutableSortedMap.Builder<String, Node> builder =
        new ImmutableSortedMap.Builder<>(Ordering.natural());
    configurations.values().forEach(c -> builder.put(c.getHostname(), new Node(c)));
    ImmutableSortedMap<String, Node> nodes = builder.build();
    dp.setNodes(nodes);
    dp.setTopology(topology);

    /*
     * Run the data plane computation here:
     * - First, let the IGP routes converge
     * - Second, re-init BGP neighbors with reachability checks
     * - Third, let the EGP routes converge
     * - Finally, compute FIBs, return answer
     */
    computeIgpDataPlane(nodes, topology, ae);
    computeFibs(nodes);

    Network<BgpNeighbor, BgpSession> bgpTopology =
        initBgpTopology(configurations, dp.getIpOwners(), false, true, this, dp);
    boolean isOscillating =
        computeNonMonotonicPortionOfDataPlane(
            nodes, topology, dp, externalAdverts, ae, true, bgpTopology);
    if (isOscillating) {
      // If we are oscillating here, network has no stable solution.
      throw new BdpOscillationException("Network has no stable solution");
    }

    if (_settings.getCheckBgpSessionReachability()) {
      computeFibs(nodes);
      bgpTopology = initBgpTopology(configurations, dp.getIpOwners(), false, true, this, dp);
      // Update queues (if necessary) based on new neighbor relationships
      final Network<BgpNeighbor, BgpSession> finalBgpTopology = bgpTopology;
      nodes
          .values()
          .parallelStream()
          .forEach(
              n ->
                  n.getVirtualRouters().values().forEach(vr -> vr.initBgpQueues(finalBgpTopology)));
      // Do another pass on EGP computation in case any new sessions have been established
      computeNonMonotonicPortionOfDataPlane(
          nodes, topology, dp, externalAdverts, ae, false, bgpTopology);
    }
    // Generate the answers from the computation, compute final FIBs
    computeFibs(nodes);
    ae.setVersion(Version.getVersion());
    _bfLogger.printElapsedTime();
    return dp;
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
   * @param topology network Topology
   * @param dp data place instance
   * @param iteration iteration number (for stats tracking)
   * @param allNodes all nodes in the network (for correct neighbor referencing)
   * @param bgpTopology the bgp peering relationships
   */
  private void computeDependentRoutesIteration(
      Map<String, Node> nodes,
      Topology topology,
      IncrementalDataPlane dp,
      int iteration,
      Map<String, Node> allNodes,
      Network<BgpNeighbor, BgpSession> bgpTopology) {

    // (Re)initialization of dependent route calculation
    AtomicInteger reinitializeDependentCompleted =
        _newBatch.apply("Iteration " + iteration + ": Reinitialize dependent routes", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {

                /*
                 * For RIBs that do not require comparison to previous version, just re-init
                 */
                vr.reinitForNewIteration(allNodes);
              }
              reinitializeDependentCompleted.incrementAndGet();
            });

    // Static nextHopIp routes
    AtomicInteger recomputeStaticCompleted =
        _newBatch.apply(
            "Iteration " + iteration + ": Recompute static routes with next-hop IP", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {
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
              for (VirtualRouter vr : n._virtualRouters.values()) {
                vr.recomputeGeneratedRoutes();
              }
              recomputeAggregateCompleted.incrementAndGet();
            });

    // OSPF external routes
    // recompute exports
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {
                vr.initOspfExports();
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
                for (VirtualRouter vr : n._virtualRouters.values()) {
                  Entry<RibDelta<OspfExternalType1Route>, RibDelta<OspfExternalType2Route>> p =
                      vr.propagateOspfExternalRoutes(allNodes, topology);
                  if (p != null && vr.unstageOspfExternalRoutes(p.getKey(), p.getValue())) {
                    ospfExternalChanged.set(true);
                  }
                }
                propagateOspfExternalCompleted.incrementAndGet();
              });
    }

    computeIterationOfBgpRoutes(nodes, dp, iteration, allNodes, bgpTopology);
  }

  private void computeIterationOfBgpRoutes(
      Map<String, Node> nodes,
      IncrementalDataPlane dp,
      int iteration,
      Map<String, Node> allNodes,
      Network<BgpNeighbor, BgpSession> bgpTopology) {
    // BGP routes
    // first let's initialize nodes-level generated/aggregate routes
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {
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
              for (VirtualRouter vr : n._virtualRouters.values()) {
                BgpProcess proc = vr._vrf.getBgpProcess();
                if (proc == null) {
                  continue;
                }
                Map<BgpMultipathRib, RibDelta<BgpRoute>> deltas =
                    vr.processBgpMessages(dp.getIpOwners(), bgpTopology);
                vr.finalizeBgpRoutesAndQueueOutgoingMessages(
                    proc.getMultipathEbgp(),
                    proc.getMultipathIbgp(),
                    deltas,
                    allNodes,
                    bgpTopology);
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
              for (VirtualRouter vr : n._virtualRouters.values()) {
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
   * @param ae The output answer element in which to store a report of the computation. Also
   *     contains the current recovery iteration.
   */
  private void computeIgpDataPlane(
      SortedMap<String, Node> nodes, Topology topology, BdpAnswerElement ae) {

    int numOspfInternalIterations;

    /*
     * For each virtual router, setup the initial easy-to-do routes, init protocol-based RIBs,
     * queue outgoing messages to neighbors
     */
    AtomicInteger initialCompleted =
        _newBatch.apply(
            "Compute initial connected and static routes, ospf setup, bgp setup", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {
                vr.initForIgpComputation();
              }
              initialCompleted.incrementAndGet();
            });

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
              for (VirtualRouter vr : n._virtualRouters.values()) {
                importRib(vr._mainRib, vr._independentRib);
                vr.activateStaticRoutes();
              }
              staticRoutesAfterIgp.incrementAndGet();
            });

    // Set iteration stats in the answer
    ae.setOspfInternalIterations(numOspfInternalIterations);
  }

  /**
   * Compute the EGP portion of the dataplane. Must be called after IGP has converged.
   *
   * @param nodes A dictionary of configuration-wrapping Bdp nodes keyed by name
   * @param topology The topology representing Layer 3 adjacencies between interface of the nodes
   * @param dp The output data plane
   * @param externalAdverts the set of external BGP advertisements
   * @param ae The output answer element in which to store a report of the computation. Also
   *     contains the current recovery iteration.
   * @return true iff the computation is oscillating
   */
  private boolean computeNonMonotonicPortionOfDataPlane(
      SortedMap<String, Node> nodes,
      Topology topology,
      IncrementalDataPlane dp,
      Set<BgpAdvertisement> externalAdverts,
      BdpAnswerElement ae,
      boolean firstPass,
      Network<BgpNeighbor, BgpSession> bgpTopology) {

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
                for (VirtualRouter vr : n._virtualRouters.values()) {
                  vr.initForEgpComputation(
                      dp.getIpOwners(), externalAdverts, nodes, topology, bgpTopology);
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
                for (VirtualRouter vr : n._virtualRouters.values()) {
                  vr.initBaseBgpRibs(externalAdverts, dp.getIpOwners(), nodes, bgpTopology);
                  vr.queueInitialBgpMessages(bgpTopology, nodes);
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
            iterationNodes, topology, dp, _numIterations, nodes, bgpTopology);
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
    } while (!areQueuesEmpty(nodes, bgpTopology) || dependentRoutesChanged.get());

    // After convergence, compute BGP advertisements sent to the outside of the network
    AtomicInteger computeBgpAdvertisementsToOutsideCompleted =
        _newBatch.apply("Compute BGP advertisements sent to outside", nodes.size());
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {
                vr.computeBgpAdvertisementsToOutside(dp.getIpOwners());
              }
              computeBgpAdvertisementsToOutsideCompleted.incrementAndGet();
            });

    dp.setBgpTopology(bgpTopology);
    ae.setDependentRoutesIterations(_numIterations);
    return false; // No oscillations
  }

  private void compareToPreviousIteration(
      Map<String, Node> nodes,
      AtomicBoolean dependentRoutesChanged,
      AtomicInteger checkFixedPointCompleted) {
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {
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
   * @param bgpTopology the bgp peering relationships
   * @return true iff all queues are empty
   */
  private boolean areQueuesEmpty(
      Map<String, Node> nodes, Network<BgpNeighbor, BgpSession> bgpTopology) {
    AtomicInteger computeQueuesAreEmpty =
        _newBatch.apply("Check for convergence (are queues empty?)", nodes.size());
    AtomicBoolean areEmpty = new AtomicBoolean(true);
    nodes
        .values()
        .parallelStream()
        .forEach(
            n -> {
              for (VirtualRouter vr : n._virtualRouters.values()) {
                if (!vr.hasProcessedAllMessages(bgpTopology)) {
                  areEmpty.set(false);
                }
              }
              computeQueuesAreEmpty.incrementAndGet();
            });

    return areEmpty.get();
  }

  /**
   * Compute the hashcode that uniquely identifies the state of hte network at a given iteration
   *
   * @param nodes map of nodes
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

  private void computeIterationStatistics(
      Map<String, Node> nodes, BdpAnswerElement ae, int dependentRoutesIterations) {
    int numBgpBestPathRibRoutes =
        nodes
            .values()
            .stream()
            .flatMap(n -> n._virtualRouters.values().stream())
            .mapToInt(vr -> vr._bgpBestPathRib.getRoutes().size())
            .sum();
    ae.getBgpBestPathRibRoutesByIteration().put(dependentRoutesIterations, numBgpBestPathRibRoutes);
    int numBgpMultipathRibRoutes =
        nodes
            .values()
            .stream()
            .flatMap(n -> n._virtualRouters.values().stream())
            .mapToInt(vr -> vr._bgpMultipathRib.getRoutes().size())
            .sum();
    ae.getBgpMultipathRibRoutesByIteration()
        .put(dependentRoutesIterations, numBgpMultipathRibRoutes);
    int numMainRibRoutes =
        nodes
            .values()
            .stream()
            .flatMap(n -> n._virtualRouters.values().stream())
            .mapToInt(vr -> vr._mainRib.getRoutes().size())
            .sum();
    ae.getMainRibRoutesByIteration().put(dependentRoutesIterations, numMainRibRoutes);
  }

  private boolean flowTraceDeniedHelper(
      Set<FlowTrace> flowTraces,
      Flow originalFlow,
      Flow transformedFlow,
      String srcInterface,
      Map<String, IpAccessList> aclDefinitions,
      List<FlowTraceHop> newHops,
      IpAccessList filter,
      FlowDisposition disposition) {
    boolean out = disposition == FlowDisposition.DENIED_OUT;
    FilterResult outResult = filter.filter(transformedFlow, srcInterface, aclDefinitions);
    boolean denied = outResult.getAction() == LineAction.REJECT;
    if (denied) {
      String outFilterName = filter.getName();
      Integer matchLine = outResult.getMatchLine();
      String lineDesc;
      if (matchLine != null) {
        lineDesc = filter.getLines().get(matchLine).getName();
        if (lineDesc == null) {
          lineDesc = "line:" + matchLine;
        }
      } else {
        lineDesc = "no-match";
      }
      String notes = disposition + "{" + outFilterName + "}{" + lineDesc + "}";
      if (out) {
        FlowTraceHop lastHop = newHops.get(newHops.size() - 1);
        newHops.remove(newHops.size() - 1);
        Edge lastEdge = lastHop.getEdge();
        Edge deniedOutEdge =
            new Edge(
                lastEdge.getFirst(),
                new NodeInterfacePair(Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
        FlowTraceHop deniedOutHop =
            new FlowTraceHop(
                deniedOutEdge, lastHop.getRoutes(), hopFlow(originalFlow, transformedFlow));
        newHops.add(deniedOutHop);
      }
      FlowTrace trace = new FlowTrace(disposition, newHops, notes);
      flowTraces.add(trace);
    }
    return denied;
  }

  SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes(
      IncrementalDataPlane dp) {
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByHostname =
        new TreeMap<>();
    for (Entry<String, Node> e : dp._nodes.entrySet()) {
      String hostname = e.getKey();
      Node node = e.getValue();
      for (Entry<String, VirtualRouter> e2 : node._virtualRouters.entrySet()) {
        String vrfName = e2.getKey();
        VirtualRouter vrf = e2.getValue();
        for (AbstractRoute route : vrf._mainRib.getRoutes()) {
          SortedMap<String, SortedSet<AbstractRoute>> routesByVrf =
              routesByHostname.computeIfAbsent(hostname, k -> new TreeMap<>());
          SortedSet<AbstractRoute> routes =
              routesByVrf.computeIfAbsent(vrfName, k -> new TreeSet<>());
          routes.add(route);
        }
      }
    }
    return routesByHostname;
  }

  @Nullable
  private Flow hopFlow(Flow originalFlow, Flow transformedFlow) {
    if (originalFlow == transformedFlow) {
      return null;
    } else {
      return transformedFlow;
    }
  }

  /**
   * Run the IGP OSPF computation until convergence.
   *
   * @param nodes list of nodes for which to initialize the OSPF routes
   * @param topology the network topology
   * @return the number of iterations it took for internal OSPF routes to converge
   */
  int initOspfInternalRoutes(Map<String, Node> nodes, Topology topology) {
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
                for (VirtualRouter vr : n._virtualRouters.values()) {
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
                for (VirtualRouter vr : n._virtualRouters.values()) {
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
                for (VirtualRouter vr : n._virtualRouters.values()) {
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
              for (VirtualRouter vr : n._virtualRouters.values()) {
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
                for (VirtualRouter vr : n._virtualRouters.values()) {
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
                for (VirtualRouter vr : n._virtualRouters.values()) {
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
              for (VirtualRouter vr : n._virtualRouters.values()) {
                importRib(vr._ripRib, vr._ripInternalRib);
                importRib(vr._independentRib, vr._ripRib);
              }
              ripInternalImportCompleted.incrementAndGet();
            });
    return ripInternalIterations;
  }

  private FlowTrace neighborUnreachableTrace(
      List<FlowTraceHop> completedHops,
      NodeInterfacePair srcInterface,
      SortedSet<String> routes,
      Flow originalFlow,
      Flow transformedFlow) {
    Edge neighborUnreachbleEdge =
        new Edge(
            srcInterface,
            new NodeInterfacePair(Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
    FlowTraceHop neighborUnreachableHop =
        new FlowTraceHop(neighborUnreachbleEdge, routes, hopFlow(originalFlow, transformedFlow));
    List<FlowTraceHop> newHops = new ArrayList<>(completedHops);
    newHops.add(neighborUnreachableHop);
    FlowTrace trace =
        new FlowTrace(
            FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
            newHops,
            FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK.toString());
    return trace;
  }

  private boolean processCurrentNextHopInterfaceEdges(
      IncrementalDataPlane dp,
      String currentNodeName,
      Set<Edge> visitedEdges,
      List<FlowTraceHop> hopsSoFar,
      Set<FlowTrace> flowTraces,
      Flow originalFlow,
      Flow transformedFlow,
      String srcInterface,
      Map<String, IpAccessList> aclDefinitions,
      Ip dstIp,
      Set<String> dstIpOwners,
      @Nullable String nextHopInterfaceName,
      SortedSet<String> routesForThisNextHopInterface,
      @Nullable Ip finalNextHopIp,
      @Nullable NodeInterfacePair nextHopInterface,
      SortedSet<Edge> edges,
      boolean arp,
      boolean ignoreAcls) {
    boolean continueToNextNextHopInterface = false;
    int unreachableNeighbors = 0;
    int potentialNeighbors = 0;
    for (Edge edge : edges) {
      if (!edge.getNode1().equals(currentNodeName)) {
        continue;
      }
      potentialNeighbors++;
      List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
      Set<Edge> newVisitedEdges = new LinkedHashSet<>(visitedEdges);
      FlowTraceHop newHop =
          new FlowTraceHop(
              edge, routesForThisNextHopInterface, hopFlow(originalFlow, transformedFlow));
      newVisitedEdges.add(edge);
      newHops.add(newHop);
      /*
       * Check to see whether neighbor would refrain from sending ARP reply
       * (NEIGHBOR_UNREACHABLE)
       *
       * This occurs if:
       *
       * - Using interface-only route
       *
       * AND
       *
       * - Neighbor does not own arpIp
       *
       * AND EITHER
       *
       * -- Neighbor not using proxy-arp
       *
       * - OR
       *
       * -- Subnet of neighbor's receiving-interface contains arpIp
       */
      if (arp) {
        Ip arpIp;
        Set<String> arpIpOwners;
        if (finalNextHopIp == null) {
          arpIp = dstIp;
          arpIpOwners = dstIpOwners;
        } else {
          arpIp = finalNextHopIp;
          arpIpOwners = dp._ipOwners.get(arpIp);
        }
        // using interface-only route
        String node2 = edge.getNode2();
        if (arpIpOwners == null || !arpIpOwners.contains(node2)) {
          // neighbor does not own arpIp
          String int2Name = edge.getInt2();
          Interface int2 = dp._nodes.get(node2)._c.getInterfaces().get(int2Name);
          boolean neighborUnreachable = false;
          Boolean proxyArp = int2.getProxyArp();
          if (proxyArp == null || !proxyArp) {
            // TODO: proxyArp probably shouldn't be null
            neighborUnreachable = true;
          } else {
            for (InterfaceAddress address : int2.getAllAddresses()) {
              if (address.getPrefix().containsIp(arpIp)) {
                neighborUnreachable = true;
                break;
              }
            }
          }
          if (neighborUnreachable) {
            unreachableNeighbors++;
            continue;
          }
        }
      }
      if (visitedEdges.contains(edge)) {
        FlowTrace trace =
            new FlowTrace(FlowDisposition.LOOP, newHops, FlowDisposition.LOOP.toString());
        flowTraces.add(trace);
        potentialNeighbors--;
        continue;
      }
      String nextNodeName = edge.getNode2();
      // now check output filter and input filter
      if (nextHopInterfaceName != null) {
        IpAccessList outFilter =
            dp._nodes
                .get(currentNodeName)
                ._c
                .getInterfaces()
                .get(nextHopInterfaceName)
                .getOutgoingFilter();
        if (!ignoreAcls && outFilter != null) {
          FlowDisposition disposition = FlowDisposition.DENIED_OUT;
          boolean denied =
              flowTraceDeniedHelper(
                  flowTraces,
                  originalFlow,
                  transformedFlow,
                  srcInterface,
                  aclDefinitions,
                  newHops,
                  outFilter,
                  disposition);
          if (denied) {
            potentialNeighbors--;
            continue;
          }
        }
      }
      IpAccessList inFilter =
          dp._nodes.get(nextNodeName)._c.getInterfaces().get(edge.getInt2()).getIncomingFilter();
      if (!ignoreAcls && inFilter != null) {
        FlowDisposition disposition = FlowDisposition.DENIED_IN;
        boolean denied =
            flowTraceDeniedHelper(
                flowTraces,
                originalFlow,
                transformedFlow,
                srcInterface,
                aclDefinitions,
                newHops,
                inFilter,
                disposition);
        if (denied) {
          potentialNeighbors--;
          continue;
        }
      }
      // recurse
      collectFlowTraces(
          dp,
          nextNodeName,
          newVisitedEdges,
          newHops,
          flowTraces,
          originalFlow,
          transformedFlow,
          ignoreAcls);
    }
    if (arp && unreachableNeighbors > 0 && unreachableNeighbors == potentialNeighbors) {
      FlowTrace trace =
          neighborUnreachableTrace(
              hopsSoFar,
              nextHopInterface,
              routesForThisNextHopInterface,
              originalFlow,
              transformedFlow);
      flowTraces.add(trace);
      continueToNextNextHopInterface = true;
    }
    return continueToNextNextHopInterface;
  }

  @Override
  public SortedMap<Flow, Set<FlowTrace>> processFlows(
      DataPlane dataPlane, Set<Flow> flows, boolean ignoreAcls) {
    Map<Flow, Set<FlowTrace>> flowTraces = new ConcurrentHashMap<>();
    IncrementalDataPlane dp = (IncrementalDataPlane) dataPlane;
    flows
        .parallelStream()
        .forEach(
            flow -> {
              Set<FlowTrace> currentFlowTraces = new TreeSet<>();
              flowTraces.put(flow, currentFlowTraces);
              String ingressNodeName = flow.getIngressNode();
              if (ingressNodeName == null) {
                throw new BatfishException(
                    "Cannot construct flow trace since ingressNode is not specified");
              }
              Ip dstIp = flow.getDstIp();
              if (dstIp == null) {
                throw new BatfishException(
                    "Cannot construct flow trace since dstIp is not specified");
              }
              Set<Edge> visitedEdges = Collections.emptySet();
              List<FlowTraceHop> hops = new ArrayList<>();
              Set<String> dstIpOwners = dp._ipOwners.get(dstIp);
              SortedSet<Edge> edges = new TreeSet<>();
              String ingressInterfaceName = flow.getIngressInterface();
              if (ingressInterfaceName != null) {
                edges.add(
                    new Edge(
                        TRACEROUTE_INGRESS_NODE_NAME,
                        TRACEROUTE_INGRESS_NODE_INTERFACE_NAME,
                        ingressNodeName,
                        ingressInterfaceName));
                processCurrentNextHopInterfaceEdges(
                    dp,
                    TRACEROUTE_INGRESS_NODE_NAME,
                    visitedEdges,
                    hops,
                    currentFlowTraces,
                    flow,
                    flow,
                    ingressInterfaceName,
                    dp._nodes.get(ingressNodeName)._c.getIpAccessLists(),
                    dstIp,
                    dstIpOwners,
                    null,
                    new TreeSet<>(),
                    null,
                    null,
                    edges,
                    false,
                    ignoreAcls);
              } else {
                collectFlowTraces(
                    dp,
                    ingressNodeName,
                    visitedEdges,
                    hops,
                    currentFlowTraces,
                    flow,
                    flow,
                    ignoreAcls);
              }
            });
    return new TreeMap<>(flowTraces);
  }
}
