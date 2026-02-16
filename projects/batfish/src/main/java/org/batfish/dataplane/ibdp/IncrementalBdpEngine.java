package org.batfish.dataplane.ibdp;

import static java.util.stream.Collectors.toSet;
import static org.batfish.common.topology.TopologyUtil.computeLayer2Topology;
import static org.batfish.common.topology.TopologyUtil.computeLayer3Topology;
import static org.batfish.common.topology.TopologyUtil.computeRawLayer3Topology;
import static org.batfish.common.topology.TopologyUtil.pruneUnreachableTunnelEdges;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.common.util.IpsecUtil.retainReachableIpsecEdges;
import static org.batfish.common.util.IpsecUtil.toEdgeSet;
import static org.batfish.common.util.StreamUtil.toListInRandomOrder;
import static org.batfish.datamodel.bgp.BgpTopologyUtils.initBgpTopology;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.computeNextVxlanTopologyModuloReachability;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.prunedVxlanTopology;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.vxlanTopologyToLayer3Edges;
import static org.batfish.dataplane.ibdp.TrackReachabilityUtils.evaluateTrackReachability;
import static org.batfish.dataplane.rib.AbstractRib.importRib;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BdpOscillationException;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.HybridL3Adjacencies;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.topology.TunnelTopology;
import org.batfish.common.topology.broadcast.BroadcastL3Adjacencies;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.IncrementalBdpAnswerElement;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.eigrp.EigrpTopologyUtils;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.tracking.GenericTrackMethodVisitor;
import org.batfish.datamodel.tracking.NegatedTrackMethod;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackAll;
import org.batfish.datamodel.tracking.TrackInterface;
import org.batfish.datamodel.tracking.TrackMethodReference;
import org.batfish.datamodel.tracking.TrackReachability;
import org.batfish.datamodel.tracking.TrackRoute;
import org.batfish.datamodel.tracking.TrackTrue;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.batfish.dataplane.ibdp.DataplaneTrackEvaluator.DataPlaneTrackMethodEvaluatorProvider;
import org.batfish.dataplane.ibdp.TrackRouteUtils.GetRoutesForPrefix;
import org.batfish.dataplane.ibdp.schedule.IbdpSchedule;
import org.batfish.dataplane.ibdp.schedule.IbdpSchedule.Schedule;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.version.BatfishVersion;

/** Computes the entire dataplane by executing a fixed-point computation. */
final class IncrementalBdpEngine {

  private static final Logger LOGGER = LogManager.getLogger(IncrementalBdpEngine.class);

  /**
   * Maximum amount of topology iterations to do before deciding that the dataplane computation
   * cannot converge (there is some sort of flap)
   */
  private static final int MAX_TOPOLOGY_ITERATIONS = 10;

  private int _numIterations;
  private final IncrementalDataPlaneSettings _settings;

  IncrementalBdpEngine(IncrementalDataPlaneSettings settings) {
    _settings = settings;
  }

  /**
   * Returns the {@link PartialDataplane} corresponding to the given topology and nodes. FIBs,
   * ForwardingAnalysis, and other internals are recomputed based on the updated state in the {@code
   * nodes} and {@code vrs}.
   */
  private PartialDataplane nextDataplane(
      TopologyContext currentTopologyContext,
      SortedMap<String, Node> nodes,
      List<VirtualRouter> vrs,
      IpOwners currentIpOwners) {
    LOGGER.info("Updating dataplane");
    computeFibs(vrs);

    return PartialDataplane.builder()
        .setNodes(nodes)
        .setIpOwners(currentIpOwners)
        .setLayer3Topology(currentTopologyContext.getLayer3Topology())
        .setL3Adjacencies(currentTopologyContext.getL3Adjacencies())
        .build();
  }

  /**
   * Performs the iterative step in dataplane computations as topology changes.
   *
   * <p>The {@code currentTopologyContext} contains the connectivity learned so far in the network,
   * specifically for things like VXLAN, BGP, and others, and {@code nodes} contains the current
   * routing and forwarding tables.
   *
   * <p>Given these inputs, primarily the current Layer3 topology, the possible edges for each other
   * topology (obtained from {@code initialTopologyContext}) are pruned down based on which sessions
   * can be established given the current L3 topology and dataplane state. The resulting {@code
   * TopologyContext} for the next iteration of dataplane is returned.
   */
  private static TopologyContext nextTopologyContext(
      TopologyContext currentTopologyContext,
      PartialDataplane currentDataplane,
      TopologyContext initialTopologyContext,
      NetworkConfigurations networkConfigurations,
      Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    // Update topologies
    LOGGER.info("Updating dynamic topologies");

    Map<String, Configuration> configurations = networkConfigurations.getMap();
    TracerouteEngine trEngCurrentL3Topology =
        new TracerouteEngineImpl(
            currentDataplane, currentTopologyContext.getLayer3Topology(), configurations);

    // IPsec
    LOGGER.info("Updating IPsec topology");
    // Note: this uses the initial context since it is pruning down the potential edges initially
    // established.
    IpsecTopology newIpsecTopology =
        retainReachableIpsecEdges(
            initialTopologyContext.getIpsecTopology(), configurations, trEngCurrentL3Topology);

    // VXLAN
    LOGGER.info("Updating VXLAN topology");
    VxlanTopology newVxlanTopology =
        prunedVxlanTopology(
            computeNextVxlanTopologyModuloReachability(
                currentDataplane.getLayer2Vnis(), currentDataplane.getLayer3Vnis()),
            configurations,
            trEngCurrentL3Topology);

    // Tunnel topology
    LOGGER.info("Updating Tunnel topology");
    TunnelTopology newTunnelTopology =
        pruneUnreachableTunnelEdges(
            initialTopologyContext.getTunnelTopology(), // like IPsec, pruning initial tunnels
            networkConfigurations,
            trEngCurrentL3Topology);

    // EIGRP topology
    LOGGER.info("Updating EIGRP topology");
    EigrpTopology newEigrpTopology =
        EigrpTopologyUtils.initEigrpTopology(
            configurations, currentTopologyContext.getLayer3Topology());

    // Initialize BGP topology
    LOGGER.info("Updating BGP topology");
    BgpTopology newBgpTopology =
        initBgpTopology(
            configurations,
            ipVrfOwners,
            false,
            true,
            trEngCurrentL3Topology,
            currentDataplane.getFibs(),
            currentTopologyContext.getL3Adjacencies());

    // Update L3 adjacencies if necessary.
    L3Adjacencies newAdjacencies;
    if (!currentTopologyContext
        .getVxlanTopology()
        .getLayer2VniEdges()
        .collect(ImmutableSet.toImmutableSet())
        .equals(newVxlanTopology.getLayer2VniEdges().collect(ImmutableSet.toImmutableSet()))) {
      LOGGER.info("Updating Layer 3 adjacencies");
      if (L3Adjacencies.USE_NEW_METHOD) {
        newAdjacencies =
            BroadcastL3Adjacencies.create(
                initialTopologyContext.getLayer1Topologies(), newVxlanTopology, configurations);
      } else {
        Layer1Topologies topologies = initialTopologyContext.getLayer1Topologies();
        if (topologies.getCombinedL1().isEmpty()) {
          newAdjacencies = GlobalBroadcastNoPointToPoint.instance();
        } else {
          Layer2Topology l2 =
              computeLayer2Topology(
                  topologies.getActiveLogicalL1(), newVxlanTopology, configurations);
          newAdjacencies = HybridL3Adjacencies.create(topologies, l2, configurations);
        }
      }
    } else {
      newAdjacencies = currentTopologyContext.getL3Adjacencies();
    }

    // Layer-3
    Topology newLayer3Topology;
    if (!newIpsecTopology.equals(currentTopologyContext.getIpsecTopology())
        || !newTunnelTopology.equals(currentTopologyContext.getTunnelTopology())
        || !newAdjacencies.equals(currentTopologyContext.getL3Adjacencies())
        || !newVxlanTopology
            .getLayer3VniEdges()
            .collect(ImmutableSet.toImmutableSet())
            .equals(
                currentTopologyContext
                    .getVxlanTopology()
                    .getLayer3VniEdges()
                    .collect(ImmutableSet.toImmutableSet()))) {
      LOGGER.info("Updating Layer 3 topology");
      newLayer3Topology =
          computeLayer3Topology(
              computeRawLayer3Topology(newAdjacencies, configurations),
              // Overlay edges consist of "plain" tunnels and IPSec tunnels
              ImmutableSet.<Edge>builder()
                  .addAll(toEdgeSet(newIpsecTopology, configurations))
                  .addAll(newTunnelTopology.asEdgeSet())
                  .addAll(vxlanTopologyToLayer3Edges(newVxlanTopology, configurations))
                  .build());
    } else {
      newLayer3Topology = currentTopologyContext.getLayer3Topology();
    }

    return currentTopologyContext.toBuilder()
        .setBgpTopology(newBgpTopology)
        .setLayer3Topology(newLayer3Topology)
        .setL3Adjacencies(newAdjacencies)
        .setVxlanTopology(newVxlanTopology)
        .setIpsecTopology(newIpsecTopology)
        .setTunnelTopology(newTunnelTopology)
        .setEigrpTopology(newEigrpTopology)
        .build();
  }

  /** Helper method used to sample the change in tracks across iterations. */
  @VisibleForTesting
  static <T> @Nonnull Optional<String> compareTracks(
      Table<String, T, Boolean> current, Table<String, T, Boolean> next) {
    if (current.equals(next)) {
      return Optional.empty();
    }
    Set<String> currentTrue =
        current.cellSet().stream()
            .filter(Cell::getValue)
            .map(c -> String.format("%s > %s", c.getRowKey(), c.getColumnKey()))
            .collect(toSet());
    Set<String> nextTrue =
        next.cellSet().stream()
            .filter(Cell::getValue)
            .map(c -> String.format("%s > %s", c.getRowKey(), c.getColumnKey()))
            .collect(toSet());
    List<String> gained = ImmutableList.copyOf(Sets.difference(nextTrue, currentTrue));
    List<String> lost = ImmutableList.copyOf(Sets.difference(currentTrue, nextTrue));
    if (gained.isEmpty()) {
      return Optional.ofNullable(
          String.format(
              "lost %d including %s", lost.size(), lost.size() > 3 ? lost.subList(0, 3) : lost));
    } else if (lost.isEmpty()) {
      return Optional.ofNullable(
          String.format(
              "gained %d including %s",
              gained.size(), gained.size() > 3 ? gained.subList(0, 3) : gained));
    }
    return Optional.ofNullable(
        String.format(
            "gained %d including %s, lost %d including %s",
            gained.size(),
            gained.size() > 3 ? gained.subList(0, 3) : gained,
            lost.size(),
            lost.size() > 3 ? lost.subList(0, 3) : lost));
  }

  ComputeDataPlaneResult computeDataPlane(
      Map<String, Configuration> configurations,
      TopologyContext initialTopologyContext,
      Set<BgpAdvertisement> externalAdverts,
      IpOwners initialIpOwners) {
    LOGGER.info("Computing Data Plane using iBDP");

    Map<Ip, Map<String, Set<String>>> initialIpVrfOwners = initialIpOwners.getIpVrfOwners();

    // Generate our nodes, keyed by name, sorted for determinism
    SortedMap<String, Node> nodes =
        toImmutableSortedMap(configurations.values(), Configuration::getHostname, Node::new);
    // A collection of all the virtual routers in random order enables parallelization across all
    // VRs, and likely spreads nodes with similar hostnames across different cores. In contrast,
    // nodes.values().parallelStream().flatMap(get vrs stream) is only node-parallel and clusters
    // nodes by hostname. See https://github.com/batfish/batfish/pull/7054 description.
    List<VirtualRouter> vrs =
        toListInRandomOrder(nodes.values().stream().flatMap(n -> n.getVirtualRouters().stream()));
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);

    /*
     * Run the data plane computation here:
     * - First, let the IGP routes converge
     * - Second, re-init BGP neighbors with reachability checks
     * - Third, let the EGP routes converge
     * - Finally, compute FIBs, return answer
     */
    IncrementalBdpAnswerElement answerElement = new IncrementalBdpAnswerElement();
    // TODO: eventually, IGP needs to be part of fixed-point below, because tunnels.
    computeIgpDataPlane(nodes, vrs, initialTopologyContext, answerElement);

    LOGGER.info("Initialize virtual routers before topology fixed point");
    vrs.parallelStream()
        .forEach(
            vr -> vr.initForEgpComputationBeforeTopologyLoop(externalAdverts, initialIpVrfOwners));

    /*
     * Perform a fixed-point computation, in which every round the topology is updated based
     * on what we have learned in the previous round.
     */
    // Since the topology iterations are incremental, clear fields that are pruned to get the real
    // topology. They are not actually yet included in topologies.
    TopologyContext priorTopologyContext =
        initialTopologyContext.toBuilder()
            .setIpsecTopology(IpsecTopology.EMPTY)
            .setTunnelTopology(TunnelTopology.EMPTY)
            .setVxlanTopology(VxlanTopology.EMPTY)
            .build();
    PartialDataplane currentDataplane =
        nextDataplane(priorTopologyContext, nodes, vrs, initialIpOwners);

    TopologyContext currentTopologyContext =
        nextTopologyContext(
            priorTopologyContext,
            currentDataplane,
            initialTopologyContext,
            networkConfigurations,
            initialIpVrfOwners);
    Map<String, Collection<TrackRoute>> trackRoutesByHostname = collectTrackRoutes(configurations);
    Map<String, Collection<TrackReachability>> trackReachabilitiesByHostname =
        collectTrackReachabilities(configurations);
    Table<String, TrackReachability, Boolean> currentTrackReachabilityResults =
        nextTrackReachabilityResults(
            currentDataplane,
            currentTopologyContext,
            configurations,
            trackReachabilitiesByHostname);
    Table<String, TrackRoute, Boolean> currentTrackRouteResults =
        nextTrackRouteResults(trackRoutesByHostname, nodes);
    DataPlaneTrackMethodEvaluatorProvider currentTrackMethodEvaluatorProvider =
        nextTrackMethodEvaluatorProvider(currentTrackReachabilityResults, currentTrackRouteResults);
    DataPlaneIpOwners currentIpOwners =
        new DataPlaneIpOwners(
            configurations,
            currentTopologyContext.getL3Adjacencies(),
            currentTrackMethodEvaluatorProvider);
    int topologyIterations = 0;
    boolean converged = false;
    while (!converged && topologyIterations++ < MAX_TOPOLOGY_ITERATIONS) {
      LOGGER.info("Starting topology iteration {}", topologyIterations);
      boolean isOscillating =
          computeNonMonotonicPortionOfDataPlane(
              nodes,
              vrs,
              answerElement,
              currentTopologyContext,
              initialTopologyContext.getLayer3Topology(),
              currentIpOwners,
              networkConfigurations,
              currentTrackMethodEvaluatorProvider);
      if (isOscillating) {
        // If we are oscillating here, network has no stable solution.
        LOGGER.error("Network has no stable solution");
        throw new BdpOscillationException("Network has no stable solution");
      }

      updateLayer3Vnis(vrs);
      currentDataplane = null; // free the old one
      currentDataplane = nextDataplane(currentTopologyContext, nodes, vrs, currentIpOwners);
      TopologyContext nextTopologyContext =
          nextTopologyContext(
              currentTopologyContext,
              currentDataplane,
              initialTopologyContext,
              networkConfigurations,
              currentIpOwners.getIpVrfOwners());

      Table<String, TrackReachability, Boolean> nextTrackReachabilityResults =
          nextTrackReachabilityResults(
              currentDataplane,
              currentTopologyContext,
              configurations,
              trackReachabilitiesByHostname);
      Table<String, TrackRoute, Boolean> nextTrackRouteResults =
          nextTrackRouteResults(trackRoutesByHostname, nodes);
      currentTrackMethodEvaluatorProvider =
          nextTrackMethodEvaluatorProvider(nextTrackReachabilityResults, nextTrackRouteResults);
      DataPlaneIpOwners nextIpOwners =
          new DataPlaneIpOwners(
              configurations,
              nextTopologyContext.getL3Adjacencies(),
              currentTrackMethodEvaluatorProvider);
      converged = true;
      if (!currentTopologyContext.equals(nextTopologyContext)) {
        converged = false;
        LOGGER.info("Topologies changed in this iteration");
      }
      Optional<String> reachabilityDiff =
          compareTracks(currentTrackReachabilityResults, nextTrackReachabilityResults);
      Optional<String> routesDiff = compareTracks(currentTrackRouteResults, nextTrackRouteResults);
      if (reachabilityDiff.isPresent() || routesDiff.isPresent()) {
        converged = false;
        LOGGER.info("Tracks changed in this iteration");
        reachabilityDiff.ifPresent(s -> LOGGER.info("Reachability tracks: {}", s));
        routesDiff.ifPresent(s -> LOGGER.info("Route tracks: {}", s));
      }
      if (!currentIpOwners.equals(nextIpOwners)) {
        converged = false;
        LOGGER.info("IP ownership changed in this iteration");
      }
      currentTopologyContext = nextTopologyContext;
      currentTrackReachabilityResults = nextTrackReachabilityResults;
      currentTrackRouteResults = nextTrackRouteResults;
      currentIpOwners = nextIpOwners;
    }

    if (!converged) {
      LOGGER.error(
          "Could not reach a fixed point topology in {} iterations", MAX_TOPOLOGY_ITERATIONS);
      throw new BdpOscillationException(
          String.format(
              "Could not reach a fixed point topology in %d iterations", MAX_TOPOLOGY_ITERATIONS));
    }

    // Generate the answers from the computation, compute final FIBs
    // TODO: Properly finalize topologies, IpOwners, etc.
    LOGGER.info("Finalizing dataplane");
    answerElement.setVersion(BatfishVersion.getVersionStatic());
    IncrementalDataPlane finalDataplane =
        IncrementalDataPlane.builder()
            .setNodes(nodes)
            .setPartialDataplane(currentDataplane)
            .build();
    return new IbdpResult(answerElement, finalDataplane, currentTopologyContext, nodes);
  }

  private @Nonnull Table<String, TrackRoute, Boolean> nextTrackRouteResults(
      Map<String, Collection<TrackRoute>> trackRoutesByHostname, SortedMap<String, Node> nodes) {
    ImmutableTable.Builder<String, TrackRoute, Boolean> trackRouteResults =
        ImmutableTable.builder();
    trackRoutesByHostname.forEach(
        (hostname, trackRoutes) -> {
          Node node = nodes.get(hostname);
          trackRoutes.forEach(
              trackRoute ->
                  trackRouteResults.put(
                      hostname, trackRoute, evaluateTrackRoute(trackRoute, node)));
        });
    return trackRouteResults.build();
  }

  /**
   * Returns map: hostname of config with at least one {@link TrackRoute} -> {@link TrackRoute}s in
   * that config.
   */
  private static @Nonnull Map<String, Collection<TrackReachability>> collectTrackReachabilities(
      Map<String, Configuration> configurations) {
    ImmutableMap.Builder<String, Collection<TrackReachability>> builder = ImmutableMap.builder();
    configurations.forEach(
        (hostname, c) -> {
          Collection<TrackReachability> trackReachabilities =
              c.getTrackingGroups().values().stream()
                  .flatMap(TRACK_REACHABILITY_COLLECTOR::visit)
                  .collect(ImmutableSet.toImmutableSet());
          if (!trackReachabilities.isEmpty()) {
            builder.put(hostname, trackReachabilities);
          }
        });
    return builder.build();
  }

  private static final TrackReachabilityCollector TRACK_REACHABILITY_COLLECTOR =
      new TrackReachabilityCollector();

  private static final class TrackReachabilityCollector
      implements GenericTrackMethodVisitor<Stream<TrackReachability>> {

    @Override
    public Stream<TrackReachability> visitNegatedTrackMethod(
        NegatedTrackMethod negatedTrackMethod) {
      return visit(negatedTrackMethod.getTrackMethod());
    }

    @Override
    public Stream<TrackReachability> visitTrackAll(TrackAll trackAll) {
      return trackAll.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<TrackReachability> visitTrackInterface(TrackInterface trackInterface) {
      return Stream.of();
    }

    @Override
    public Stream<TrackReachability> visitTrackMethodReference(
        TrackMethodReference trackMethodReference) {
      // target will be found elsewhere
      return Stream.of();
    }

    @Override
    public Stream<TrackReachability> visitTrackReachability(TrackReachability trackReachability) {
      return Stream.of(trackReachability);
    }

    @Override
    public Stream<TrackReachability> visitTrackRoute(TrackRoute trackRoute) {
      return Stream.of();
    }

    @Override
    public Stream<TrackReachability> visitTrackTrue(TrackTrue trackTrue) {
      return Stream.of();
    }
  }

  /**
   * Returns map: hostname of config with at least one {@link TrackRoute} -> {@link TrackRoute}s in
   * that config.
   */
  private static @Nonnull Map<String, Collection<TrackRoute>> collectTrackRoutes(
      Map<String, Configuration> configurations) {
    ImmutableMap.Builder<String, Collection<TrackRoute>> builder = ImmutableMap.builder();
    configurations.forEach(
        (hostname, c) -> {
          Collection<TrackRoute> trackRoutes =
              c.getTrackingGroups().values().stream()
                  .flatMap(TRACK_ROUTE_COLLECTOR::visit)
                  .collect(ImmutableSet.toImmutableSet());
          if (!trackRoutes.isEmpty()) {
            builder.put(hostname, trackRoutes);
          }
        });
    return builder.build();
  }

  private static final TrackRouteCollector TRACK_ROUTE_COLLECTOR = new TrackRouteCollector();

  private static final class TrackRouteCollector
      implements GenericTrackMethodVisitor<Stream<TrackRoute>> {

    @Override
    public Stream<TrackRoute> visitNegatedTrackMethod(NegatedTrackMethod negatedTrackMethod) {
      return visit(negatedTrackMethod.getTrackMethod());
    }

    @Override
    public Stream<TrackRoute> visitTrackAll(TrackAll trackAll) {
      return trackAll.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<TrackRoute> visitTrackInterface(TrackInterface trackInterface) {
      return Stream.of();
    }

    @Override
    public Stream<TrackRoute> visitTrackMethodReference(TrackMethodReference trackMethodReference) {
      // target will be found elsewhere
      return Stream.of();
    }

    @Override
    public Stream<TrackRoute> visitTrackReachability(TrackReachability trackReachability) {
      return Stream.of();
    }

    @Override
    public Stream<TrackRoute> visitTrackRoute(TrackRoute trackRoute) {
      return Stream.of(trackRoute);
    }

    @Override
    public Stream<TrackRoute> visitTrackTrue(TrackTrue trackTrue) {
      return Stream.of();
    }
  }

  /**
   * Create a provider for data-plane-based track evaluation, which depends in general on the
   * contents of FIBs and RIBs.
   *
   * <p>Evaluation is currently performed in the following places:
   *
   * <ul>
   *   <li>Constructor of {@link DataPlaneIpOwners}. This happens between iterations, so is thread
   *       safe with respect to RIBs and FIBs.
   *   <li>{@link VirtualRouter#activateStaticRoutes}. This happens during evaluation of a parallel
   *       stream over all {@link VirtualRouter}s that modifies RIBs. In order to achieve
   *       thread-safety in the case where a {@link org.batfish.datamodel.tracking.TrackRoute}
   *       depends on information in a different VRF than that containing the static route, the
   *       evaulator must have an immutable view of the RIB being inspected. So we should depend on
   *       the routes from the beginning of the iteration (note we are only able to supply FIBs from
   *       the beginning of an iteration anyway). Since saving routes of a VRF can be expensive, we
   *       instead use pre-evaluated {@link org.batfish.datamodel.tracking.TrackRoute} and {@link
   *       org.batfish.datamodel.tracking.TrackReachability} results here.
   * </ul>
   */
  private static @Nonnull DataPlaneTrackMethodEvaluatorProvider nextTrackMethodEvaluatorProvider(
      Table<String, TrackReachability, Boolean> trackReachabilityResults,
      Table<String, TrackRoute, Boolean> trackRouteResults) {
    return DataplaneTrackEvaluator.createTrackMethodEvaluatorProvider(
        trackReachabilityResults, trackRouteResults);
  }

  private static @Nonnull Table<String, TrackReachability, Boolean> nextTrackReachabilityResults(
      PartialDataplane dp,
      TopologyContext topologyContext,
      Map<String, Configuration> configurations,
      Map<String, Collection<TrackReachability>> trackReachabilitiesByHostname) {
    TracerouteEngine tr =
        new TracerouteEngineImpl(dp, topologyContext.getLayer3Topology(), configurations);
    ImmutableTable.Builder<String, TrackReachability, Boolean> trackReachabilityResults =
        ImmutableTable.builder();
    trackReachabilitiesByHostname.forEach(
        (hostname, trackReachabilities) -> {
          Configuration config = configurations.get(hostname);
          Map<String, Fib> fibs = dp.getFibs().get(hostname);
          trackReachabilities.forEach(
              trackReachability ->
                  trackReachabilityResults.put(
                      hostname,
                      trackReachability,
                      evaluateTrackReachability(trackReachability, config, fibs, tr)));
        });
    return trackReachabilityResults.build();
  }

  @VisibleForTesting
  static boolean evaluateTrackRoute(TrackRoute trackRoute, Node node) {
    return switch (trackRoute.getRibType()) {
      case BGP ->
          TrackRouteUtils.evaluateTrackRoute(
              trackRoute,
              Optional.ofNullable(
                      node.getVirtualRouter(trackRoute.getVrf()).get().getBgpRoutingProcess())
                  .<GetRoutesForPrefix<Bgpv4Route>>map(brp -> brp::getBgpv4RoutesForPrefix)
                  .orElse(TrackRouteUtils::emptyGetRoutesForPrefix));
      case MAIN ->
          TrackRouteUtils.evaluateTrackRoute(
              trackRoute, node.getVirtualRouter(trackRoute.getVrf()).get().getMainRib()::getRoutes);
    };
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
   * @param vrs virtual routers that are participating in the computation
   * @param iterationLabel iteration label (for stats tracking)
   * @param allNodes all nodes in the network (for correct neighbor referencing)
   */
  private static void computeDependentRoutesIteration(
      List<VirtualRouter> vrs,
      String iterationLabel,
      Map<String, Node> allNodes,
      NetworkConfigurations networkConfigurations,
      DataPlaneTrackMethodEvaluatorProvider provider,
      int iteration) {
    LOGGER.info("{}: Compute dependent routes", iterationLabel);

    // Static nextHopIp routes
    LOGGER.info("{}: Recompute conditional static routes", iterationLabel);
    vrs.parallelStream()
        .forEach(vr -> vr.activateStaticRoutes(provider.forConfiguration(vr.getConfiguration())));

    // Generated/aggregate routes
    LOGGER.info("{}: Recompute aggregate/generated routes", iterationLabel);
    vrs.parallelStream().forEach(VirtualRouter::recomputeGeneratedRoutes);

    // EIGRP
    LOGGER.info("{}: Propagate EIGRP routes", iterationLabel);
    vrs.parallelStream().forEach(vr -> vr.eigrpIteration(allNodes));
    vrs.parallelStream().forEach(VirtualRouter::mergeEigrpRoutesToMainRib);

    // Re-initialize IS-IS exports.
    LOGGER.info("{}: Recompute IS-IS routes", iterationLabel);
    vrs.parallelStream()
        .forEach(vr -> vr.initIsisExports(iteration, allNodes, networkConfigurations));

    // IS-IS route propagation
    AtomicBoolean isisChanged = new AtomicBoolean(true);
    int isisSubIterations = 0;
    while (isisChanged.get()) {
      isisSubIterations++;
      LOGGER.info("{}: Recompute IS-IS routes: subIteration {}", iterationLabel, isisSubIterations);
      isisChanged.set(false);
      vrs.parallelStream()
          .forEach(
              vr -> {
                Entry<RibDelta<IsisRoute>, RibDelta<IsisRoute>> p =
                    vr.propagateIsisRoutes(networkConfigurations);
                if (p != null
                    && vr.unstageIsisRoutes(
                        allNodes, networkConfigurations, p.getKey(), p.getValue())) {
                  isisChanged.set(true);
                }
              });
    }

    LOGGER.info("{}: Propagate OSPF external", iterationLabel);
    vrs.parallelStream().forEach(vr -> vr.ospfIteration(allNodes));
    vrs.parallelStream().forEach(VirtualRouter::mergeOspfRoutesToMainRib);

    computeIterationOfBgpRoutes(iterationLabel, allNodes, vrs);

    leakAcrossVrfs(vrs, iterationLabel);

    // Tell each VR that a BGP route computation inner round (schedule) has ended.
    vrs.parallelStream().forEach(VirtualRouter::endOfEgpInnerRound);
  }

  private static void updateLayer3Vnis(List<VirtualRouter> vrs) {
    LOGGER.info("Update learned VTEP IPs for Layer3Vnis");
    vrs.parallelStream().forEach(VirtualRouter::updateLayer3Vnis);
  }

  private static void computeIterationOfBgpRoutes(
      String iterationLabel, Map<String, Node> allNodes, List<VirtualRouter> vrs) {
    LOGGER.info("{}: Init for new BGP iteration", iterationLabel);
    vrs.parallelStream().forEach(vr -> vr.bgpIteration(allNodes));
    LOGGER.info("{}: Init BGP generated/aggregate routes", iterationLabel);
    // first let's initialize nodes-level generated/aggregate routes
    vrs.parallelStream().forEach(VirtualRouter::initBgpAggregateRoutes);

    LOGGER.info("{}: Propagate BGP v4 routes", iterationLabel);

    // Merge BGP routes from BGP process into the main RIB
    vrs.parallelStream().forEach(VirtualRouter::mergeBgpRoutesToMainRib);
  }

  private static void queueRoutesForCrossVrfLeaking(List<VirtualRouter> vrs) {
    LOGGER.info("Queueing routes to leak across VRFs");
    vrs.parallelStream().forEach(VirtualRouter::queueCrossVrfImports);
  }

  private static void leakAcrossVrfs(List<VirtualRouter> vrs, String iterationLabel) {
    LOGGER.info("{}: Leaking routes across VRFs", iterationLabel);
    vrs.parallelStream().forEach(VirtualRouter::processCrossVrfRoutes);
  }

  /**
   * Run {@link VirtualRouter#computeFib} on all virtual routers
   *
   * @param vrs all virtual routers
   */
  private void computeFibs(List<VirtualRouter> vrs) {
    LOGGER.info("Compute FIBs");
    vrs.parallelStream().forEach(VirtualRouter::computeFib);
  }

  /**
   * Compute the IGP portion of the dataplane.
   *
   * @param nodes A dictionary of configuration-wrapping Bdp nodes keyed by name
   * @param topologyContext The topology context in which various adjacencies are stored
   * @param ae The output answer element in which to store a report of the computation. Also
   */
  private void computeIgpDataPlane(
      SortedMap<String, Node> nodes,
      List<VirtualRouter> vrs,
      TopologyContext topologyContext,
      IncrementalBdpAnswerElement ae) {
    LOGGER.info("Compute IGP");
    int numOspfInternalIterations;

    /*
     * For each virtual router, setup the initial easy-to-do routes, init protocol-based RIBs,
     * queue outgoing messages to neighbors
     */
    LOGGER.info("Initialize for IGP computation");
    vrs.parallelStream().forEach(vr -> vr.initForIgpComputation(topologyContext));

    // Apply rib-groups sequentially to avoid concurrent writes to same destination RIB
    LOGGER.info("Apply rib-groups for IGP");
    vrs.stream().forEach(VirtualRouter::applyRibGroupsForIgp);

    // OSPF internal routes
    numOspfInternalIterations = initOspfInternalRoutes(nodes, topologyContext.getOspfTopology());

    // RIP internal routes
    initRipInternalRoutes(nodes, vrs, topologyContext.getLayer3Topology());

    // Activate static routes
    LOGGER.info("Compute static routes post IGP convergence");
    vrs.parallelStream()
        .forEach(
            vr -> {
              importRib(vr.getMainRib(), vr._independentRib);
              // Use static evaluator since we don't have dataplane yet
              vr.activateStaticRoutes(new PreDataPlaneTrackMethodEvaluator(vr.getConfiguration()));
            });

    // Set iteration stats in the answer
    ae.setOspfInternalIterations(numOspfInternalIterations);
  }

  /**
   * Compute the EGP portion of the route exchange. Must be called after IGP routing has converged.
   *
   * @param nodes A dictionary of configuration-wrapping Bdp nodes keyed by name
   * @param ae The output answer element in which to store a report of the computation. Also
   *     contains the current recovery iteration.
   * @param topologyContext The various network topologies
   * @return true iff the computation is oscillating
   */
  private boolean computeNonMonotonicPortionOfDataPlane(
      SortedMap<String, Node> nodes,
      List<VirtualRouter> vrs,
      IncrementalBdpAnswerElement ae,
      TopologyContext topologyContext,
      Topology initialLayer3Topology,
      IpOwners ipOwners,
      NetworkConfigurations networkConfigurations,
      DataPlaneTrackMethodEvaluatorProvider provider) {
    LOGGER.info("Compute EGP");
    /*
     * Initialize all routers and their message queues (can be done as parallel as possible)
     */
    LOGGER.info("Initialize virtual routers with updated topologies");
    vrs.parallelStream()
        .forEach(vr -> vr.initForEgpComputationWithNewTopology(topologyContext, provider));

    LOGGER.info("Compute HMM routes");
    Map<String, Map<String, Set<Ip>>> interfaceOwners = ipOwners.getInterfaceOwners(true);
    vrs.parallelStream().forEach(vr -> vr.computeHmmRoutes(initialLayer3Topology, interfaceOwners));

    LOGGER.info("Compute kernel routes");
    vrs.parallelStream()
        .forEach(vr -> vr.computeConditionalKernelRoutes(ipOwners.getIpVrfOwners()));

    /*
     * Setup maps to track iterations. We need this for oscillation detection.
     * Specifically, if we detect that an iteration hashcode (a hash of all the nodes' RIBs)
     * has been previously encountered, we switch our schedule to a more restrictive one.
     */

    Map<Integer, SortedSet<Integer>> iterationsByHashCode = new HashMap<>();

    Schedule currentSchedule = _settings.getScheduleName();

    // Go into iteration mode, until the routes converge (or oscillation is detected)
    do {
      _numIterations++;
      LOGGER.info("Iteration {} begins", _numIterations);
      LOGGER.info("Compute schedule");
      // Compute node schedule
      IbdpSchedule schedule =
          IbdpSchedule.getSchedule(_settings, currentSchedule, nodes, topologyContext);

      // (Re)initialization of dependent route calculation
      //  Since this is a local step, coloring not required.

      LOGGER.info("Re-Init for new route iteration");
      vrs.parallelStream().forEach(VirtualRouter::reinitForNewIteration);

      /*
      Redistribution: take all the routes merged into the main RIB during previous iteration
      and offer them to each routing process.

      This must be called before any `executeIteration` calls on any routing process.
      Since this is a local step, coloring not required.
      */
      LOGGER.info("Redistribute");
      vrs.parallelStream().forEach(VirtualRouter::redistribute);

      // Handle process-specific route resolution and cross-VRF leaking here too.
      vrs.parallelStream().forEach(VirtualRouter::updateResolvableRoutes);
      queueRoutesForCrossVrfLeaking(vrs);

      // compute dependent routes for each allowable set of nodes until we cover all nodes
      int nodeSet = 0;
      while (schedule.hasNext()) {
        Map<String, Node> iterationNodes = schedule.next();
        List<VirtualRouter> iterationVrs =
            toListInRandomOrder(
                iterationNodes.values().stream().flatMap(n -> n.getVirtualRouters().stream()));
        String iterationlabel = String.format("Iteration %d Schedule %d", _numIterations, nodeSet);
        computeDependentRoutesIteration(
            iterationVrs, iterationlabel, nodes, networkConfigurations, provider, _numIterations);
        ++nodeSet;
      }

      // Tell each VR that a route computation round has ended.
      // This must be the last thing called on a VR in a routing round.
      vrs.parallelStream().forEach(VirtualRouter::endOfEgpRound);

      /*
       * Perform various bookkeeping at the end of the iteration:
       * - Collect sizes of certain RIBs this iteration
       * - Compute iteration hashcode
       * - Check for oscillations
       */
      computeIterationStatistics(vrs, ae, _numIterations);

      // This hashcode uniquely identifies the iteration (i.e., network state)
      int iterationHashCode = computeIterationHashCode(vrs);
      SortedSet<Integer> iterationsWithThisHashCode =
          iterationsByHashCode.computeIfAbsent(iterationHashCode, h -> new TreeSet<>());

      if (iterationsWithThisHashCode.isEmpty()) {
        iterationsWithThisHashCode.add(_numIterations);
      } else {
        // If oscillation detected, switch to a more restrictive schedule
        if (currentSchedule != Schedule.NODE_SERIALIZED) {
          LOGGER.debug(
              "Switching to a more restrictive schedule {}, iteration {}",
              Schedule.NODE_SERIALIZED,
              _numIterations);
          currentSchedule = Schedule.NODE_SERIALIZED;
        } else {
          return true; // Found an oscillation
        }
      }
    } while (hasNotReachedRoutingFixedPoint(vrs));

    ae.setDependentRoutesIterations(_numIterations);
    return false; // No oscillations
  }

  /** Check if we have reached a routing fixed point */
  private boolean hasNotReachedRoutingFixedPoint(List<VirtualRouter> vrs) {
    LOGGER.info("Iteration {}: Check if fixed point reached", _numIterations);
    return vrs.parallelStream().anyMatch(VirtualRouter::isDirty);
  }

  /**
   * Compute the hashcode that uniquely identifies the state of the network at a given iteration
   *
   * @param vrs all virtual routers in the network
   * @return integer hashcode
   */
  private int computeIterationHashCode(List<VirtualRouter> vrs) {
    LOGGER.info("Iteration {}: Compute hashCode", _numIterations);
    return vrs.parallelStream().mapToInt(VirtualRouter::computeIterationHashCode).sum();
  }

  private static void computeIterationStatistics(
      List<VirtualRouter> vrs, IncrementalBdpAnswerElement ae, int dependentRoutesIterations) {
    LOGGER.info("Iteration {}: Compute statistics", dependentRoutesIterations);
    int numBgpBestPathRibRoutes =
        vrs.parallelStream().mapToInt(VirtualRouter::getNumBgpBestPaths).sum();
    ae.getBgpBestPathRibRoutesByIteration().put(dependentRoutesIterations, numBgpBestPathRibRoutes);
    int numBgpMultipathRibRoutes =
        vrs.parallelStream().mapToInt(VirtualRouter::getNumBgpPaths).sum();
    ae.getBgpMultipathRibRoutesByIteration()
        .put(dependentRoutesIterations, numBgpMultipathRibRoutes);
    int numMainRibRoutes =
        vrs.parallelStream().mapToInt(vr -> vr.getMainRib().getRoutes().size()).sum();
    ae.getMainRibRoutesByIteration().put(dependentRoutesIterations, numMainRibRoutes);
  }

  /**
   * Return the main RIB routes for each node. Map structure: Hostname -&gt; VRF name -&gt; Set of
   * routes
   */
  @VisibleForTesting
  static SortedMap<String, SortedMap<String, Set<AbstractRoute>>> getRoutes(
      IncrementalDataPlane dp) {
    // Scan through all Nodes and their virtual routers, retrieve main rib routes
    return toImmutableSortedMap(
        dp.getRibsForTesting(),
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue(),
                Entry::getKey,
                vrfEntry -> ImmutableSet.copyOf(vrfEntry.getValue().getUnannotatedRoutes())));
  }

  private static final int MAX_OSPF_INTERNAL_ITERATIONS = 100000;

  /**
   * Run the IGP OSPF computation until convergence.
   *
   * @param allNodes list of nodes for which to initialize the OSPF routes
   * @param ospfTopology graph of OSPF adjacencies
   * @return the number of iterations it took for internal OSPF routes to converge
   */
  private int initOspfInternalRoutes(Map<String, Node> allNodes, OspfTopology ospfTopology) {
    int ospfInternalIterations = 0;
    boolean dirty = true;

    while (dirty) {
      ospfInternalIterations++;
      LOGGER.info("OSPF internal: Iteration {}", ospfInternalIterations);
      // Compute node schedule
      IbdpSchedule schedule =
          IbdpSchedule.getSchedule(
              _settings,
              _settings.getScheduleName(),
              allNodes,
              TopologyContext.builder().setOspfTopology(ospfTopology).build());

      while (schedule.hasNext()) {
        Map<String, Node> scheduleNodes = schedule.next();
        List<VirtualRouter> scheduleVrs =
            toListInRandomOrder(
                scheduleNodes.values().stream().flatMap(n -> n.getVirtualRouters().stream()));
        scheduleVrs.parallelStream()
            .forEach(virtualRouter -> virtualRouter.ospfIteration(allNodes));
        scheduleVrs.parallelStream().forEach(VirtualRouter::mergeOspfRoutesToMainRib);
      }
      dirty =
          allNodes.values().parallelStream()
              .flatMap(n -> n.getVirtualRouters().stream())
              .flatMap(vr -> vr.getOspfProcesses().values().stream())
              .anyMatch(OspfRoutingProcess::isDirty);
      if (ospfInternalIterations > MAX_OSPF_INTERNAL_ITERATIONS) {
        throw new BdpOscillationException(
            "OSPF did not converge after " + MAX_OSPF_INTERNAL_ITERATIONS + " iterations");
      }
    }
    return ospfInternalIterations;
  }

  /**
   * Run the IGP RIP computation until convergence
   *
   * @param nodes nodes for which to initialize the routes, keyed by name
   * @param topology network topology
   */
  private static void initRipInternalRoutes(
      SortedMap<String, Node> nodes, List<VirtualRouter> vrs, Topology topology) {
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
      LOGGER.info("RIP internal: Iteration {}", ripInternalIterations);
      vrs.parallelStream()
          .forEach(
              vr -> {
                if (vr.propagateRipInternalRoutes(nodes, topology)) {
                  ripInternalChanged.set(true);
                }
              });
      LOGGER.info("Unstage RIP internal: Iteration {}", ripInternalIterations);
      vrs.parallelStream().forEach(VirtualRouter::unstageRipInternalRoutes);

      LOGGER.info("Import RIP internal: Iteration {}", ripInternalIterations);
      vrs.parallelStream()
          .forEach(
              vr -> {
                importRib(vr._ripRib, vr._ripInternalRib);
                importRib(vr._independentRib, vr._ripRib, vr.getName());
              });
    }
  }
}
