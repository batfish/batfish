package org.batfish.dataplane.ibdp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.alwaysFalse;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.common.util.CollectionUtil.toOrderedHashCode;
import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;
import static org.batfish.datamodel.routing_policy.Environment.Direction.IN;
import static org.batfish.dataplane.ibdp.DataplaneUtil.messageQueueStream;
import static org.batfish.dataplane.protocols.IsisProtocolHelper.convertRouteLevel1ToLevel2;
import static org.batfish.dataplane.protocols.IsisProtocolHelper.exportNonIsisRouteToIsis;
import static org.batfish.dataplane.protocols.IsisProtocolHelper.setOverloadOnAllRoutes;
import static org.batfish.dataplane.protocols.StaticRouteHelper.shouldActivateNextHopIpRoute;
import static org.batfish.dataplane.rib.AbstractRib.importRib;
import static org.batfish.dataplane.rib.RibDelta.importRibDelta;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Streams;
import com.google.common.graph.Network;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpVrfLeakConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Bgpv4ToEvpnVrfLeakConfig;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.EvpnToBgpv4VrfLeakConfig;
import org.batfish.datamodel.EvpnType3Route;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibImpl;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.HmmRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.LocalRoute;
import org.batfish.datamodel.MainRibVrfLeakConfig;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.RipInternalRoute;
import org.batfish.datamodel.RipProcess;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrfLeakConfig;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.dataplane.rib.RibGroup;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.route.nh.NextHopVtep;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.tracking.TrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackMethodEvaluatorProvider;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.dataplane.protocols.GeneratedRouteHelper;
import org.batfish.dataplane.rib.AnnotatedRib;
import org.batfish.dataplane.rib.ConnectedRib;
import org.batfish.dataplane.rib.IsisLevelRib;
import org.batfish.dataplane.rib.IsisRib;
import org.batfish.dataplane.rib.LocalRib;
import org.batfish.dataplane.rib.Rib;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RipInternalRib;
import org.batfish.dataplane.rib.RipRib;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.batfish.dataplane.rib.StaticRib;

public final class VirtualRouter {

  /** The BGP routing process. {@code null} if BGP is not configured for this VRF */
  @Nullable BgpRoutingProcess _bgpRoutingProcess;

  /** Parent configuration for this virtual router */
  private final @Nonnull Configuration _c;

  /** The RIB containing connected routes */
  private ConnectedRib _connectedRib;

  /**
   * Queues containing routes that are coming in from other VRFs (as a result of explicitly
   * configured leaking or applied RIB groups).
   */
  private SortedMap<CrossVrfEdgeId, Queue<RouteAdvertisement<AnnotatedRoute<AbstractRoute>>>>
      _crossVrfIncomingRoutes;

  /**
   * The independent RIB contains connected and static routes, which are unaffected by BDP
   * iterations (hence, independent).
   */
  Rib _independentRib;

  /** Incoming messages into this router from each IS-IS circuit */
  SortedMap<IsisEdge, Queue<RouteAdvertisement<IsisRoute>>> _isisIncomingRoutes;

  /** Routes in main RIB to redistribute into IS-IS */
  RibDelta.Builder<AnnotatedRoute<AbstractRoute>> _routesForIsisRedistribution;

  @VisibleForTesting @Nonnull List<KernelRoute> _kernelConditionalRoutes;
  private @Nonnull List<HmmRoute> _hmmRoutes;

  IsisLevelRib _isisL1Rib;
  IsisLevelRib _isisL2Rib;
  private IsisLevelRib _isisL1StagingRib;
  private IsisLevelRib _isisL2StagingRib;
  private IsisRib _isisRib;
  LocalRib _localRib;

  /** The default main RIB, contains routes from different protocol RIBs */
  private final Rib _mainRib;

  /** All named main RIBs, including {@link RibId#DEFAULT_RIB_NAME} */
  private final Map<String, Rib> _mainRibs;

  /** Keeps track of changes to the main RIB in the current iteration. */
  @VisibleForTesting RibDelta.Builder<AnnotatedRoute<AbstractRoute>> _mainRibRouteDeltaBuilder;

  /**
   * All of the routes that were merged/withdrawn for the main RIB in this the previous iteration
   * Will inform redistribution/VRF leaking in current round.
   */
  RibDelta<AnnotatedRoute<AbstractRoute>> _mainRibDeltaPrevRound;

  /** The VRF name for this virtual router */
  private final @Nonnull String _name;

  /** Parent {@link Node} on which this virtual router resides */
  private final @Nonnull Node _node;

  private Map<String, OspfRoutingProcess> _ospfProcesses;

  RipInternalRib _ripInternalRib;
  RipInternalRib _ripInternalStagingRib;
  RipRib _ripRib;
  StaticRib _staticUnconditionalRib;
  StaticRib _staticConditionalRib;

  /** FIB (forwarding information base) built from the main RIB */
  private Fib _fib;

  /** RIB containing generated routes */
  private Rib _generatedRib;

  /** Metadata about propagated prefixes to/from neighbors */
  private @Nonnull PrefixTracer _prefixTracer;

  /** List of all EIGRP processes in this VRF */
  @VisibleForTesting ImmutableMap<Long, EigrpRoutingProcess> _eigrpProcesses;

  /**
   * Layer 2 VNI settings that are updated dynamically as the dataplane is being computed (e.g.,
   * based on EVPN route advertisements).
   */
  private Set<Layer2Vni> _layer2Vnis;

  /**
   * Map of VNI to Layer 3 VNI settings that are updated dynamically as the dataplane is being
   * computed (e.g., based on EVPN route advertisements).
   */
  private Map<Integer, Layer3Vni> _layer3Vnis;

  /** A {@link Vrf} that this virtual router represents */
  final Vrf _vrf;

  private final @Nonnull ResolutionRestriction<AnnotatedRoute<AbstractRoute>>
      _resolutionRestriction;

  private static final Logger LOGGER = LogManager.getLogger(VirtualRouter.class);

  VirtualRouter(@Nonnull String name, @Nonnull Node node) {
    _node = node;
    _c = node.getConfiguration();
    _name = name;
    _vrf = _c.getVrfs().get(name);
    String resolutionPolicy = _vrf.getResolutionPolicy();
    _resolutionRestriction =
        resolutionPolicy == null
            ? alwaysTrue()
            : _c.getRoutingPolicies().get(resolutionPolicy)::processReadOnly;
    // Main RIB + delta builder
    _mainRib = new Rib(_c.getMainRibEnforceResolvability() ? _resolutionRestriction : null);
    _mainRibs = ImmutableMap.of(RibId.DEFAULT_RIB_NAME, _mainRib);
    _mainRibDeltaPrevRound = RibDelta.empty();
    _mainRibRouteDeltaBuilder = RibDelta.builder();
    _routesForIsisRedistribution = RibDelta.builder();
    // Init rest of the RIBs
    initRibs();

    _prefixTracer = new PrefixTracer();
    _eigrpProcesses = ImmutableMap.of();
    _ospfProcesses = ImmutableMap.of();
    _layer2Vnis = ImmutableSet.copyOf(_vrf.getLayer2Vnis().values());
    _layer3Vnis = ImmutableMap.copyOf(_vrf.getLayer3Vnis());
    if (_vrf.getBgpProcess() != null) {
      _bgpRoutingProcess =
          new BgpRoutingProcess(
              _vrf.getBgpProcess(), _c, _name, _mainRib, BgpTopology.EMPTY, _prefixTracer);
    }
    _hmmRoutes = ImmutableList.of();
    _kernelConditionalRoutes = ImmutableList.of();
  }

  @VisibleForTesting
  void initCrossVrfQueues() {
    // TODO: also handle non-default RIBs
    // https://github.com/batfish/batfish/issues/3050
    _crossVrfIncomingRoutes =
        _node.getVirtualRouters().stream()
            .filter(vr -> !_name.equals(vr.getName()))
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Ordering.natural(),
                    vr -> new CrossVrfEdgeId(vr.getName(), RibId.DEFAULT_RIB_NAME),
                    vr -> new ConcurrentLinkedQueue<>()));
  }

  /**
   * Convert a given RibDelta into {@link RouteAdvertisement} objects and enqueue them onto a given
   * queue.
   *
   * @param queue the message queue
   * @param delta {@link RibDelta} representing changes.
   */
  static <R extends AbstractRoute, D extends R> void queueDelta(
      Queue<RouteAdvertisement<R>> queue, @Nonnull RibDelta<D> delta) {
    for (RouteAdvertisement<D> r : delta.getActions()) {
      @SuppressWarnings("unchecked") // Ok to upcast to R since immutable.
      RouteAdvertisement<R> sanitized = (RouteAdvertisement<R>) r.sanitizeForExport();
      queue.add(sanitized);
    }
  }

  /**
   * Initializes helper data structures and easy-to-compute RIBs that are not affected by BDP
   * iterations (e.g., static route RIB, connected route RIB, etc.)
   *
   * <p>This method initializes only local RIBs (no cross-VRF writes). Call {@link
   * #applyRibGroupsForIgp()} after all VRFs complete this phase to avoid concurrent writes to
   * shared RIBs.
   */
  @VisibleForTesting
  void initForIgpComputation(TopologyContext topologyContext) {
    initConnectedRib();
    initKernelRoutes();
    initLocalRib();
    initStaticRibs();
    // Always import local and connected routes into your own rib
    importRib(_independentRib, _connectedRib);
    importRib(_independentRib, _localRib);
    importRib(_independentRib, _staticUnconditionalRib, _name);
    importRib(_mainRib, _independentRib);
    importRib(_mainRib, _connectedRib);

    _ospfProcesses =
        _vrf.getOspfProcesses().entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey,
                    e ->
                        new OspfRoutingProcess(
                            e.getValue(), _name, _c, topologyContext.getOspfTopology())));
    _ospfProcesses.values().forEach(p -> p.initialize(_node));

    initEigrp();
    initBaseRipRoutes();
  }

  /**
   * Apply rib-groups to export routes to other VRFs.
   *
   * <p>Must be called after all VRFs complete {@link #initForIgpComputation(TopologyContext)} to
   * avoid concurrent writes to shared RIBs. Should be called sequentially (not in parallel) since
   * multiple VRFs may export to the same destination RIB.
   */
  void applyRibGroupsForIgp() {
    // Apply rib groups for connected and local routes
    RibGroup connectedRibGroup = _vrf.getAppliedRibGroups().get(RoutingProtocol.CONNECTED);
    if (connectedRibGroup != null) {
      applyRibGroup(connectedRibGroup, _connectedRib);
    }
    RibGroup localRibGroup = _vrf.getAppliedRibGroups().get(RoutingProtocol.LOCAL);
    if (localRibGroup != null) {
      applyRibGroup(localRibGroup, _localRib);
    }
  }

  /** Recompute HMM routes, and import delta into main RIB. */
  void computeHmmRoutes(
      Topology initialLayer3Topology, Map<String, Map<String, Set<Ip>>> interfaceOwners) {
    RibDelta.Builder<HmmRoute> delta = RibDelta.builder();
    _hmmRoutes.forEach(oldHmmRoute -> delta.remove(oldHmmRoute, Reason.WITHDRAW));
    ImmutableList.Builder<HmmRoute> newHmmRoutes = ImmutableList.builder();
    _c.getAllInterfaces(_vrf.getName())
        .forEach(
            (ifaceName, iface) -> {
              if (!iface.getHmm()) {
                return;
              }
              Set<NodeInterfacePair> neighbors =
                  initialLayer3Topology.getNeighbors(NodeInterfacePair.of(iface));
              for (NodeInterfacePair neighbor : neighbors) {
                Set<Ip> neighborIps =
                    interfaceOwners
                        .getOrDefault(neighbor.getHostname(), ImmutableMap.of())
                        .getOrDefault(neighbor.getInterface(), ImmutableSet.of());
                // add an hmm route for every owned IP address on every neighbor of this interface
                neighborIps.stream()
                    .map(
                        ip ->
                            HmmRoute.builder()
                                .setNetwork(ip.toPrefix())
                                // TODO: set custom administrative distance
                                .setNextHop(NextHopInterface.of(iface.getName()))
                                .build())
                    .forEach(
                        hmmRoute -> {
                          delta.add(hmmRoute);
                          newHmmRoutes.add(hmmRoute);
                        });
              }
            });
    for (RouteAdvertisement<HmmRoute> action : delta.build().getActions()) {
      if (action.isWithdrawn()) {
        _mainRibRouteDeltaBuilder.from(
            _mainRib.removeRouteGetDelta(annotateRoute(action.getRoute())));
      } else {
        _mainRibRouteDeltaBuilder.from(
            _mainRib.mergeRouteGetDelta(annotateRoute(action.getRoute())));
      }
    }
    _hmmRoutes = newHmmRoutes.build();
  }

  /** Recompute conditional kernel routes, and import delta into main RIB. */
  public void computeConditionalKernelRoutes(Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    for (KernelRoute kernelRoute : _kernelConditionalRoutes) {
      if (shouldActivateConditionalKernelRoute(kernelRoute, ipVrfOwners)) {
        _mainRibRouteDeltaBuilder.from(_mainRib.mergeRouteGetDelta(annotateRoute(kernelRoute)));
      } else {
        _mainRibRouteDeltaBuilder.from(_mainRib.removeRouteGetDelta(annotateRoute(kernelRoute)));
      }
    }
  }

  /** Apply a rib group to a given source rib (which belongs to this VRF) */
  private void applyRibGroup(@Nonnull RibGroup ribGroup, @Nonnull AnnotatedRib<?> sourceRib) {
    RoutingPolicy policy = _c.getRoutingPolicies().get(ribGroup.getImportPolicy());
    checkState(policy != null, "RIB group %s is missing import policy", ribGroup.getName());
    sourceRib.getRoutes().stream()
        .map(
            route -> {
              AbstractRouteBuilder<?, ?> builder = route.getRoute().toBuilder();
              boolean accept = policy.process(route, builder, IN, alwaysFalse());
              return accept ? new AnnotatedRoute<AbstractRoute>(builder.build(), _name) : null;
            })
        .filter(Objects::nonNull)
        .forEach(
            r ->
                ribGroup
                    .getImportRibs()
                    .forEach(ribId -> _node.getRib(ribId).ifPresent(rib -> rib.mergeRoute(r))));
  }

  /** Initialize EIGRP processes */
  private void initEigrp() {
    _eigrpProcesses =
        _vrf.getEigrpProcesses().values().stream()
            .map(
                eigrpProcess ->
                    new EigrpRoutingProcess(eigrpProcess, _name, RoutingPolicies.from(_c)))
            .collect(ImmutableMap.toImmutableMap(EigrpRoutingProcess::getAsn, Function.identity()));
    _eigrpProcesses.values().forEach(p -> p.initialize(_node));
  }

  /**
   * Prepare for the EGP part of the computation. Handles updating routing processes given new
   * topology information.
   *
   * <p>Must be called between rounds, aka, all delta builder should be empty.
   *
   * @param topologyContext The various network topologies
   * @param trackMethodEvaluatorProvider current data plane iteration's {@link
   *     TrackMethodEvaluatorProvider} used for computing the iteration's values for BGP-watched
   *     tracks
   */
  void initForEgpComputationWithNewTopology(
      TopologyContext topologyContext, TrackMethodEvaluatorProvider trackMethodEvaluatorProvider) {
    assert _mainRibRouteDeltaBuilder.isEmpty(); // or else invariant is not maintained

    initQueuesAndDeltaBuilders(topologyContext);
    if (_bgpRoutingProcess != null) {
      // If the process exists, update the topology and BGP track states
      _bgpRoutingProcess.updateTopology(topologyContext.getBgpTopology());
      _bgpRoutingProcess.updateWatchedTrackStates(trackMethodEvaluatorProvider);
    }
  }

  /**
   * Initialize for EGP computation. Handles any state that does <b>not</b> depend on neighbor
   * relationships (i.e., purely local), but is allowed to process external bgp advertisements.
   */
  void initForEgpComputationBeforeTopologyLoop(
      Set<BgpAdvertisement> externalAdverts, Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    /*
    Merge post-IGP main rib in to a mainRibDelta.
    This effectively makes the entire IGP computation a "previous round".
    */
    _mainRibDeltaPrevRound =
        RibDelta.<AnnotatedRoute<AbstractRoute>>builder().add(_mainRib.getRoutes()).build();
    _mainRibRouteDeltaBuilder = RibDelta.builder();

    if (_bgpRoutingProcess != null && !_bgpRoutingProcess.isInitialized()) {
      _bgpRoutingProcess.initialize(_node);
      _bgpRoutingProcess.stageExternalAdvertisements(externalAdverts, ipVrfOwners);
    }
  }

  /**
   * Initializes RIB delta builders and protocol message queues.
   *
   * @param topologyContext The various network topologies
   */
  @VisibleForTesting
  void initQueuesAndDeltaBuilders(TopologyContext topologyContext) {
    // Update topology/re-initialize message queues for EIGRP neighbors
    _eigrpProcesses
        .values()
        .forEach(proc -> proc.updateTopology(topologyContext.getEigrpTopology()));
    // Initialize message queues for each IS-IS neighbor
    initIsisQueues(topologyContext.getIsisTopology());
    // Initialize message queues for all neighboring VRFs/VirtualRouters
    initCrossVrfQueues();
  }

  /**
   * Goes through VRFs that can leak routes into this routing instance, and imports all routes from
   * their main ribs to {@link #_crossVrfIncomingRoutes}.
   */
  void initCrossVrfImports() {
    VrfLeakConfig vrfLeakConfig = _vrf.getVrfLeakConfig();
    if (vrfLeakConfig == null || vrfLeakConfig.getLeakAsBgp()) {
      return;
    }
    for (MainRibVrfLeakConfig leakConfig : vrfLeakConfig.getMainRibVrfLeakConfigs()) {
      String importFromVrf = leakConfig.getImportFromVrf();
      VirtualRouter exportingVR = _node.getVirtualRouterOrThrow(importFromVrf);
      CrossVrfEdgeId otherVrfToOurRib = new CrossVrfEdgeId(importFromVrf, RibId.DEFAULT_RIB_NAME);
      enqueueCrossVrfRoutes(
          otherVrfToOurRib,
          // TODO Will need to update once support is added for cross-VRF export policies
          exportingVR._mainRib.getRoutes().stream().map(RouteAdvertisement::new),
          leakConfig.getImportPolicy());
    }
  }

  private void initIsisQueues(IsisTopology isisTopology) {
    Network<IsisNode, IsisEdge> network = isisTopology.getNetwork();
    // Initialize message queues for each IS-IS circuit
    if (_vrf.getIsisProcess() == null) {
      _isisIncomingRoutes = ImmutableSortedMap.of();
    } else {
      _isisIncomingRoutes =
          _c.getAllInterfaces(_vrf.getName()).keySet().stream()
              .map(ifaceName -> new IsisNode(_c.getHostname(), ifaceName))
              .filter(network.nodes()::contains)
              .flatMap(n -> network.inEdges(n).stream())
              .collect(
                  toImmutableSortedMap(Function.identity(), e -> new ConcurrentLinkedQueue<>()));
    }
  }

  /**
   * Activate generated routes.
   *
   * @return a new {@link RibDelta} if a new route has been activated, otherwise {@code null}
   */
  private RibDelta<AnnotatedRoute<AbstractRoute>> activateGeneratedRoutes() {
    RibDelta.Builder<AnnotatedRoute<AbstractRoute>> builder = RibDelta.builder();

    /*
     * Loop over all generated routes and check whether any of the contributing routes can trigger
     * activation.
     */
    for (GeneratedRoute gr : _vrf.getGeneratedRoutes()) {
      String policyName = gr.getGenerationPolicy();
      RoutingPolicy generationPolicy =
          policyName != null ? _c.getRoutingPolicies().get(policyName) : null;
      GeneratedRoute.Builder grb =
          GeneratedRouteHelper.activateGeneratedRoute(
              gr, generationPolicy, _mainRib.getRoutes(), null);

      if (grb != null) {
        // Routes have been changed
        builder.from(_generatedRib.mergeRouteGetDelta(annotateRoute(grb.build())));
      }
    }
    return builder.build();
  }

  /**
   * Recompute generated routes. If new generated routes were activated, process them into the main
   * RIB. Check if any BGP aggregates were affected by the new generated routes.
   */
  void recomputeGeneratedRoutes() {
    RibDelta<AnnotatedRoute<AbstractRoute>> d;
    RibDelta.Builder<AnnotatedRoute<AbstractRoute>> generatedRouteDeltaBuilder = RibDelta.builder();
    do {
      d = activateGeneratedRoutes();
      generatedRouteDeltaBuilder.from(d);
    } while (!d.isEmpty());

    d = generatedRouteDeltaBuilder.build();
    // Update main rib as well
    _mainRibRouteDeltaBuilder.from(importRibDelta(_mainRib, d));

    /*
     * Check dependencies for BGP aggregates.
     *
     * Updates from these BGP deltas into mainRib will be handled in finalizeBgp routes
     */
    if (!d.isEmpty() && _bgpRoutingProcess != null) {
      for (RouteAdvertisement<AnnotatedRoute<AbstractRoute>> r : d.getActions()) {
        if (r.isWithdrawn()) {
          _bgpRoutingProcess.removeAggregate(r.getRoute().getRoute());
        }
      }
    }
  }

  /**
   * Activate conditional static routes, i.e. static routes with next hop IP or track. Adds a static
   * route {@code route} to the main RIB if there exists an active route to the {@code routes}'s
   * next-hop-ip (if present), and if the track succeeds (if present).
   *
   * <p>Removes static route from the main RIB for which next-hop-ip has become unreachable.
   */
  void activateStaticRoutes(TrackMethodEvaluator trackMethodEvaluator) {
    for (StaticRoute sr : _staticConditionalRib.getRoutes()) {
      if (shouldActivateConditionalStaticRoute(trackMethodEvaluator, sr)) {
        _mainRibRouteDeltaBuilder.from(_mainRib.mergeRouteGetDelta(annotateRoute(sr)));
      } else {
        /*
         * If the route is not in the RIB, this has no effect. But might add some overhead (TODO)
         */
        _mainRibRouteDeltaBuilder.from(_mainRib.removeRouteGetDelta(annotateRoute(sr)));
      }
    }
  }

  private boolean shouldActivateConditionalStaticRoute(
      TrackMethodEvaluator trackMethodEvaluator, StaticRoute sr) {
    if (!shouldActivateConditionalStaticRouteNextHop(sr)) {
      return false;
    }
    if (sr.getTrack() != null && !evaluateTrack(trackMethodEvaluator, sr.getTrack())) {
      // Required track failed
      return false;
    }
    return true;
  }

  private boolean shouldActivateConditionalStaticRouteNextHop(StaticRoute sr) {
    return new ShouldActivateConditionalStaticRouteNextHop(sr).visit(sr.getNextHop());
  }

  private final class ShouldActivateConditionalStaticRouteNextHop
      implements NextHopVisitor<Boolean> {

    private ShouldActivateConditionalStaticRouteNextHop(StaticRoute sr) {
      _sr = sr;
    }

    private final @Nonnull StaticRoute _sr;

    @Override
    public Boolean visitNextHopIp(NextHopIp nextHopIp) {
      return shouldActivateNextHopIpRoute(_sr, _mainRib, _resolutionRestriction);
    }

    @Override
    public Boolean visitNextHopInterface(NextHopInterface nextHopInterface) {
      String iface = nextHopInterface.getInterfaceName();
      assert _c.getAllInterfaces().containsKey(iface);
      return _c.getAllInterfaces().get(iface).getActive();
    }

    @Override
    public Boolean visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
      assert _sr.getTrack() != null;
      return true;
    }

    @Override
    public Boolean visitNextHopVrf(NextHopVrf nextHopVrf) {
      assert _sr.getTrack() != null;
      return true;
    }

    @Override
    public Boolean visitNextHopVtep(NextHopVtep nextHopVtep) {
      throw new IllegalArgumentException("StaticRoute cannot have next hop VTEP");
    }
  }

  private boolean isConditionalStaticRoute(StaticRoute sr) {
    return sr.getTrack() != null || isConditionalStaticRouteNextHop(sr.getNextHop());
  }

  private boolean isConditionalStaticRouteNextHop(NextHop nextHop) {
    return _isConditionalStaticRouteNextHop.visit(nextHop);
  }

  private final @Nonnull NextHopVisitor<Boolean> _isConditionalStaticRouteNextHop =
      new NextHopVisitor<Boolean>() {
        @Override
        public Boolean visitNextHopIp(NextHopIp nextHopIp) {
          return true;
        }

        @Override
        public Boolean visitNextHopInterface(NextHopInterface nextHopInterface) {
          assert _c.getAllInterfaces().containsKey(nextHopInterface.getInterfaceName());
          return true;
        }

        @Override
        public Boolean visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          return false;
        }

        @Override
        public Boolean visitNextHopVrf(NextHopVrf nextHopVrf) {
          assert _c.getVrfs().containsKey(nextHopVrf.getVrfName());
          return false;
        }

        @Override
        public Boolean visitNextHopVtep(NextHopVtep nextHopVtep) {
          // should not be possible; only EVPN and BGP routes have this next hop type.
          throw new IllegalStateException("Static routes cannot forward via VXLAN tunnel");
        }
      };

  /**
   * Evaluates the {@link TrackMethod} indexed by {@code trackName} and returns {@code true} iff the
   * track succeeds.
   */
  private boolean evaluateTrack(TrackMethodEvaluator trackMethodEvaluator, String trackName) {
    TrackMethod method = _c.getTrackingGroups().get(trackName);
    assert method != null;
    return trackMethodEvaluator.visit(method);
  }

  /** Compute the FIB from the main RIB */
  public void computeFib() {
    _fib = null; // free the old one.
    _fib = new FibImpl(_mainRib, _resolutionRestriction);
  }

  void initBgpAggregateRoutes() {
    if (_bgpRoutingProcess == null) {
      return;
    }
    _bgpRoutingProcess.initBgpAggregateRoutesLegacy(_generatedRib.getRoutes());
  }

  /** Initialize RIP routes from the interface prefixes */
  @VisibleForTesting
  void initBaseRipRoutes() {
    if (_vrf.getRipProcess() == null) {
      return; // nothing to do
    }

    // init internal routes from connected routes
    for (String ifaceName : _vrf.getRipProcess().getInterfaces()) {
      Interface iface = _c.getAllInterfaces(_vrf.getName()).get(ifaceName);
      if (iface.getActive()) {
        Set<Prefix> allNetworkPrefixes =
            iface.getAllConcreteAddresses().stream()
                .map(ConcreteInterfaceAddress::getPrefix)
                .collect(Collectors.toSet());
        long cost = RipProcess.DEFAULT_RIP_COST;
        for (Prefix prefix : allNetworkPrefixes) {
          RipInternalRoute route =
              RipInternalRoute.builder()
                  .setNetwork(prefix)
                  .setNextHop(NextHopInterface.of(ifaceName))
                  .setAdmin(
                      RoutingProtocol.RIP.getDefaultAdministrativeCost(_c.getConfigurationFormat()))
                  .setMetric(cost)
                  .build();
          _ripInternalRib.mergeRouteGetDelta(route);
        }
      }
    }
  }

  /**
   * Initialize the connected RIB -- a RIB containing connected routes (i.e., direct connections to
   * neighbors).
   */
  @VisibleForTesting
  void initConnectedRib() {
    // Look at all interfaces in our VRF
    _c.getActiveInterfaces(_name).values().stream()
        .flatMap(VirtualRouter::generateConnectedRoutes)
        .forEach(r -> _connectedRib.mergeRoute(annotateRoute(r)));
  }

  /** Generate connected routes for a given active interface. */
  private static @Nonnull Stream<ConnectedRoute> generateConnectedRoutes(@Nonnull Interface iface) {
    assert iface.getActive();
    return iface.getAllConcreteAddresses().stream()
        .filter(addr -> shouldGenerateConnectedRoute(iface.getAddressMetadata().get(addr)))
        .map(
            addr ->
                generateConnectedRoute(
                    addr, iface.getName(), iface.getAddressMetadata().get(addr)));
  }

  /**
   * Returns true if a connected route should be generated for the provided {@link
   * ConnectedRouteMetadata}.
   */
  @VisibleForTesting
  static boolean shouldGenerateConnectedRoute(
      @Nullable ConnectedRouteMetadata connectedRouteMetadata) {
    if (connectedRouteMetadata != null
        && connectedRouteMetadata.getGenerateConnectedRoute() != null) {
      // Use the metadata if set.
      return connectedRouteMetadata.getGenerateConnectedRoute();
    }
    // If the metadata are not provided or no value, generate the connected route.
    return true;
  }

  /** Generate a connected route for a given address (and associated metadata). */
  @VisibleForTesting
  static @Nonnull ConnectedRoute generateConnectedRoute(
      @Nonnull ConcreteInterfaceAddress address,
      @Nonnull String ifaceName,
      @Nullable ConnectedRouteMetadata metadata) {
    ConnectedRoute.Builder builder =
        ConnectedRoute.builder().setNetwork(address.getPrefix()).setNextHopInterface(ifaceName);
    if (metadata != null) {
      if (metadata.getAdmin() != null) {
        builder.setAdmin(metadata.getAdmin());
      }
      if (metadata.getTag() != null) {
        builder.setTag(metadata.getTag());
      }
    }
    return builder.build();
  }

  /**
   * Initialize the kernel routes -- a set of non-forwarding routes installed for the purpose of
   * redistribution.
   *
   * <ul>
   *   <li>Kernel routes with no dependencies are added to {@code _independentRib}.
   *   <li>Kernel routes with dependencies are stored in {@code _kernelConditionalRoutes}, to be
   *       processed each data plane iteration.
   * </ul>
   */
  @VisibleForTesting
  void initKernelRoutes() {
    ImmutableList.Builder<KernelRoute> kernelConditionalRoutesBuilder = ImmutableList.builder();
    for (KernelRoute kernelRoute : _vrf.getKernelRoutes()) {
      if (kernelRoute.getRequiredOwnedIp() != null) {
        kernelConditionalRoutesBuilder.add(kernelRoute);
      } else {
        _independentRib.mergeRoute(annotateRoute(kernelRoute));
      }
    }
    _kernelConditionalRoutes = kernelConditionalRoutesBuilder.build();
  }

  @VisibleForTesting
  boolean shouldActivateConditionalKernelRoute(
      KernelRoute kr, Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    Ip requiredOwnedIp = kr.getRequiredOwnedIp();
    assert requiredOwnedIp != null;
    return ipVrfOwners
        .getOrDefault(requiredOwnedIp, ImmutableMap.of())
        .getOrDefault(_c.getHostname(), ImmutableSet.of())
        .contains(_name);
  }

  /**
   * Initialize the local RIB -- a RIB containing non-forwarding /32 routes for exact addresses of
   * interfaces
   */
  @VisibleForTesting
  void initLocalRib() {
    // Look at all interfaces in our VRF
    _c.getAllInterfaces(_name).values().stream()
        .flatMap(VirtualRouter::generateLocalRoutes)
        .forEach(r -> _localRib.mergeRoute(annotateRoute(r)));
  }

  /**
   * Generate local routes for a given active interface. Returns an empty stream if the interface
   * generates no local routes based on {@link ConnectedRouteMetadata#getGenerateLocalRoute()} or
   * Batfish policy (only addresses with network length of < /32 are considered).
   */
  private static @Nonnull Stream<LocalRoute> generateLocalRoutes(@Nonnull Interface iface) {
    if (!iface.getActive()) {
      return iface.getAllConcreteAddresses().stream()
          .map(
              addr ->
                  generateLocalNullRouteForDownInterface(
                      addr, iface.getAddressMetadata().get(addr)))
          .filter(Optional::isPresent)
          .map(Optional::get);
    }
    return iface.getAllConcreteAddresses().stream()
        .filter(
            addr ->
                shouldGenerateLocalRoute(
                    addr.getNetworkBits(), iface.getAddressMetadata().get(addr)))
        .map(
            addr ->
                generateLocalRoute(addr, iface.getName(), iface.getAddressMetadata().get(addr)));
  }

  /**
   * Returns true if a local route should be generated for the provided {@link
   * ConnectedRouteMetadata}.
   */
  @VisibleForTesting
  static boolean shouldGenerateLocalRoute(
      int prefixLength, @Nullable ConnectedRouteMetadata connectedRouteMetadata) {
    if (connectedRouteMetadata != null && connectedRouteMetadata.getGenerateLocalRoute() != null) {
      // Use the metadata if set.
      return connectedRouteMetadata.getGenerateLocalRoute();
    }
    // If the metadata are not provided or no value for generate local routes, use Batfish default.
    return prefixLength < Prefix.MAX_PREFIX_LENGTH;
  }

  /** Generate a connected route for a given address (and associated metadata). */
  @VisibleForTesting
  static @Nonnull LocalRoute generateLocalRoute(
      @Nonnull ConcreteInterfaceAddress address,
      @Nonnull String ifaceName,
      @Nullable ConnectedRouteMetadata metadata) {
    return seedLocalRoute(address, metadata).setNextHop(NextHopInterface.of(ifaceName)).build();
  }

  private static @Nonnull LocalRoute.Builder seedLocalRoute(
      ConcreteInterfaceAddress address, ConnectedRouteMetadata metadata) {
    LocalRoute.Builder builder =
        LocalRoute.builder()
            .setNetwork(address.getIp().toPrefix())
            .setSourcePrefixLength(address.getNetworkBits());
    if (metadata != null) {
      if (metadata.getAdmin() != null) {
        builder.setAdmin(metadata.getAdmin());
      }
      if (metadata.getTag() != null) {
        builder.setTag(metadata.getTag());
      }
    }
    return builder;
  }

  @VisibleForTesting
  static @Nonnull Optional<LocalRoute> generateLocalNullRouteForDownInterface(
      ConcreteInterfaceAddress address, @Nullable ConnectedRouteMetadata meta) {
    if (meta == null || !firstNonNull(meta.getGenerateLocalNullRouteIfDown(), false)) {
      return Optional.empty();
    }
    return Optional.of(seedLocalRoute(address, meta).setNextHop(NextHopDiscard.instance()).build());
  }

  void initIsisExports(int numIterations, Map<String, Node> allNodes, NetworkConfigurations nc) {
    /* TODO: https://github.com/batfish/batfish/issues/1703 */
    IsisProcess proc = _vrf.getIsisProcess();
    if (proc == null) {
      return; // nothing to do
    }
    RibDelta.Builder<IsisRoute> d1 = RibDelta.builder();
    RibDelta.Builder<IsisRoute> d2 = RibDelta.builder();
    /*
     * init L1 and L2 routes from connected routes
     */
    int l1Admin = RoutingProtocol.ISIS_L1.getDefaultAdministrativeCost(_c.getConfigurationFormat());
    int l2Admin = RoutingProtocol.ISIS_L2.getDefaultAdministrativeCost(_c.getConfigurationFormat());
    IsisLevelSettings l1Settings = proc.getLevel1();
    IsisLevelSettings l2Settings = proc.getLevel2();
    IsisRoute.Builder ifaceRouteBuilder =
        new IsisRoute.Builder()
            .setArea(proc.getNetAddress().getAreaIdString())
            .setSystemId(proc.getNetAddress().getSystemIdString());
    _c.getActiveInterfaces(_vrf.getName())
        .values()
        .forEach(
            iface ->
                generateAllIsisInterfaceRoutes(
                    d1, d2, l1Admin, l2Admin, l1Settings, l2Settings, ifaceRouteBuilder, iface));

    // export default route for L1 neighbors on L1L2 routers that are not overloaded
    if (l1Settings != null && l2Settings != null && !proc.getOverload()) {
      IsisRoute defaultRoute =
          new IsisRoute.Builder()
              .setAdmin(l1Admin)
              .setArea(proc.getNetAddress().getAreaIdString())
              .setAttach(true)
              .setLevel(IsisLevel.LEVEL_1)
              .setMetric(0L)
              .setNetwork(Prefix.ZERO)
              .setNextHopIp(Route.UNSET_ROUTE_NEXT_HOP_IP)
              .setProtocol(RoutingProtocol.ISIS_L1)
              .setSystemId(proc.getNetAddress().getSystemIdString())
              .build();
      d1.from(_isisL1Rib.mergeRouteGetDelta(defaultRoute));
    }

    if (numIterations == 1) {
      // Add initial routes from main rib
      _routesForIsisRedistribution.add(_mainRib.getRoutes());
    }
    addRedistributedRoutesToDeltas(d1, d2, proc);
    _routesForIsisRedistribution = RibDelta.builder();

    queueOutgoingIsisRoutes(allNodes, nc, d1.build(), d2.build());
  }

  /**
   * If IS-IS process redistributes routes, run the given routes through the IS-IS export policy and
   * merge any redistributable routes into the IS-IS level RIBs. Any changes will be recorded in the
   * given RIB deltas.
   */
  void addRedistributedRoutesToDeltas(
      @Nonnull RibDelta.Builder<IsisRoute> l1Delta,
      @Nonnull RibDelta.Builder<IsisRoute> l2Delta,
      @Nonnull IsisProcess proc) {
    if (proc.getExportPolicy() == null) {
      return;
    }
    // If process has level 1 enabled, routes should be added to IS-IS rib as level 1.
    // Otherwise if it has level 2 enabled, routes should be added as level 2.
    IsisLevelSettings activeLevelSettings = proc.getLevel1();
    boolean isLevel1 = activeLevelSettings != null;
    if (!isLevel1) {
      activeLevelSettings = proc.getLevel2();
    }
    if (activeLevelSettings == null) {
      // Neither level enabled
      return;
    }
    _routesForIsisRedistribution.build().stream()
        // Don't redistribute IS-IS routes into IS-IS...
        .filter(ra -> !(ra.getRoute().getRoute() instanceof IsisRoute))
        .map(ra -> exportNonIsisRouteToIsis(ra.getRoute(), proc, isLevel1, _c))
        .filter(Objects::nonNull)
        .forEach(
            isisRoute -> {
              if (isisRoute.getLevel() == IsisLevel.LEVEL_1) {
                l1Delta.from(_isisL1Rib.mergeRouteGetDelta(isisRoute));
              } else if (isisRoute.getLevel() == IsisLevel.LEVEL_2) {
                l2Delta.from(_isisL2Rib.mergeRouteGetDelta(isisRoute));
              }
            });
  }

  /**
   * Generate IS-IS L1/L2 routes from a given interface and merge them into appropriate L1/L2 RIBs.
   */
  private void generateAllIsisInterfaceRoutes(
      Builder<IsisRoute> d1,
      Builder<IsisRoute> d2,
      int l1Admin,
      int l2Admin,
      @Nullable IsisLevelSettings l1Settings,
      @Nullable IsisLevelSettings l2Settings,
      IsisRoute.Builder routeBuilder,
      Interface iface) {
    IsisInterfaceSettings ifaceSettings = iface.getIsis();
    if (ifaceSettings == null) {
      return;
    }
    IsisInterfaceLevelSettings ifaceL1Settings = ifaceSettings.getLevel1();
    IsisInterfaceLevelSettings ifaceL2Settings = ifaceSettings.getLevel2();
    if (ifaceL1Settings != null && l1Settings != null) {
      generateIsisInterfaceRoutesPerLevel(l1Admin, routeBuilder, iface, IsisLevel.LEVEL_1)
          .forEach(r -> d1.from(_isisL1Rib.mergeRouteGetDelta(r)));
    }
    if (ifaceL2Settings != null && l2Settings != null) {
      generateIsisInterfaceRoutesPerLevel(l2Admin, routeBuilder, iface, IsisLevel.LEVEL_2)
          .forEach(r -> d2.from(_isisL2Rib.mergeRouteGetDelta(r)));
    }
  }

  /**
   * Generate IS-IS from a given interface for a given level (with a given metric/admin cost) and
   * merge them into the appropriate RIB.
   */
  private static Set<IsisRoute> generateIsisInterfaceRoutesPerLevel(
      int adminCost, IsisRoute.Builder routeBuilder, Interface iface, IsisLevel level) {
    IsisInterfaceLevelSettings ifaceLevelSettings =
        level == IsisLevel.LEVEL_1 ? iface.getIsis().getLevel1() : iface.getIsis().getLevel2();
    RoutingProtocol isisProtocol =
        level == IsisLevel.LEVEL_1 ? RoutingProtocol.ISIS_L1 : RoutingProtocol.ISIS_L2;
    long metric =
        ifaceLevelSettings.getMode() == IsisInterfaceMode.PASSIVE
            ? 0L
            : firstNonNull(ifaceLevelSettings.getCost(), IsisRoute.DEFAULT_METRIC);
    routeBuilder.setAdmin(adminCost).setLevel(level).setMetric(metric).setProtocol(isisProtocol);
    return iface.getAllConcreteAddresses().stream()
        .map(
            address ->
                routeBuilder.setNetwork(address.getPrefix()).setNextHopIp(address.getIp()).build())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Nullable
  EigrpRoutingProcess getEigrpProcess(long asn) {
    return _eigrpProcesses.get(asn);
  }

  /** Initialize all ribs on this router. All RIBs will be empty */
  @VisibleForTesting
  final void initRibs() {
    // Non-learned-protocol RIBs
    _connectedRib = new ConnectedRib();
    _localRib = new LocalRib();
    _generatedRib = new Rib();
    _independentRib = new Rib();

    // ISIS
    _isisRib = new IsisRib(isL1Only());
    _isisL1Rib = new IsisLevelRib(true);
    _isisL2Rib = new IsisLevelRib(true);
    _isisL1StagingRib = new IsisLevelRib(false);
    _isisL2StagingRib = new IsisLevelRib(false);

    // RIP
    _ripInternalRib = new RipInternalRib();
    _ripInternalStagingRib = new RipInternalRib();
    _ripRib = new RipRib();

    // Static
    _staticConditionalRib = new StaticRib();
    _staticUnconditionalRib = new StaticRib();
  }

  private boolean isL1Only() {
    IsisProcess proc = _vrf.getIsisProcess();
    if (proc == null) {
      return false;
    }
    return proc.getLevel1() != null && proc.getLevel2() == null;
  }

  /** Initialize the static route RIBs from the VRF config. */
  @VisibleForTesting
  void initStaticRibs() {
    for (StaticRoute sr : _vrf.getStaticRoutes()) {
      if (isConditionalStaticRoute(sr)) {
        _staticConditionalRib.mergeRouteGetDelta(sr);
      } else {
        _staticUnconditionalRib.mergeRouteGetDelta(sr);
      }
    }
  }

  @Nullable
  Entry<RibDelta<IsisRoute>, RibDelta<IsisRoute>> propagateIsisRoutes(NetworkConfigurations nc) {
    if (_vrf.getIsisProcess() == null) {
      return null;
    }
    RibDelta.Builder<IsisRoute> l1DeltaBuilder = RibDelta.builder();
    RibDelta.Builder<IsisRoute> l2DeltaBuilder = RibDelta.builder();
    _isisIncomingRoutes.forEach(
        (edge, queue) -> {
          Ip nextHopIp = edge.getNode1().getInterface(nc).getConcreteAddress().getIp();
          Interface iface = edge.getNode2().getInterface(nc);
          while (queue.peek() != null) {
            RouteAdvertisement<IsisRoute> routeAdvert = queue.remove();
            IsisRoute neighborRoute = routeAdvert.getRoute();
            IsisLevel routeLevel = neighborRoute.getLevel();
            IsisInterfaceLevelSettings isisLevelSettings =
                routeLevel == IsisLevel.LEVEL_1
                    ? iface.getIsis().getLevel1()
                    : iface.getIsis().getLevel2();

            // Do not propagate route if ISIS interface is not active at this level
            if (isisLevelSettings.getMode() != IsisInterfaceMode.ACTIVE) {
              continue;
            }
            boolean withdraw = routeAdvert.isWithdrawn();
            int adminCost =
                neighborRoute
                    .getProtocol()
                    .getDefaultAdministrativeCost(_c.getConfigurationFormat());
            RibDelta.Builder<IsisRoute> deltaBuilder =
                routeLevel == IsisLevel.LEVEL_1 ? l1DeltaBuilder : l2DeltaBuilder;
            long incrementalMetric =
                firstNonNull(isisLevelSettings.getCost(), IsisRoute.DEFAULT_METRIC);
            IsisRoute newRoute =
                neighborRoute.toBuilder()
                    .setAdmin(adminCost)
                    .setLevel(routeLevel)
                    .setMetric(incrementalMetric + neighborRoute.getMetric())
                    .setNextHopIp(nextHopIp)
                    // Just imported, so set nonrouting false
                    .setNonRouting(false)
                    .build();
            if (withdraw) {
              deltaBuilder.remove(newRoute, Reason.WITHDRAW);
            } else {
              IsisLevelRib levelStagingRib =
                  routeLevel == IsisLevel.LEVEL_1 ? _isisL1StagingRib : _isisL2StagingRib;
              deltaBuilder.from(levelStagingRib.mergeRouteGetDelta(newRoute));
            }
          }
        });
    return new SimpleEntry<>(l1DeltaBuilder.build(), l2DeltaBuilder.build());
  }

  /**
   * Process RIP routes from our neighbors.
   *
   * @param nodes Mapping of node names to Node instances
   * @param topology The network topology
   * @return True if the rib has changed as a result of route propagation
   */
  boolean propagateRipInternalRoutes(Map<String, Node> nodes, Topology topology) {
    boolean changed = false;

    // No rip process, nothing to do
    if (_vrf.getRipProcess() == null) {
      return false;
    }

    String node = _c.getHostname();
    int admin = RoutingProtocol.RIP.getDefaultAdministrativeCost(_c.getConfigurationFormat());
    SortedSet<Edge> edges = topology.getNodeEdges().get(node);
    if (edges == null) {
      // there are no edges, so RIP won't produce anything
      return false;
    }

    for (Edge edge : edges) {
      // Do not accept routes from ourselves
      if (!edge.getNode1().equals(node)) {
        continue;
      }

      // Get interface
      String connectingInterfaceName = edge.getInt1();
      Interface connectingInterface =
          _c.getAllInterfaces(_vrf.getName()).get(connectingInterfaceName);
      if (connectingInterface == null) {
        // wrong vrf, so skip
        continue;
      }

      // Get the neighbor and its interface + VRF
      String neighborName = edge.getNode2();
      Node neighbor = nodes.get(neighborName);
      String neighborInterfaceName = edge.getInt2();
      Interface neighborInterface =
          neighbor.getConfiguration().getAllInterfaces().get(neighborInterfaceName);
      String neighborVrfName = neighborInterface.getVrfName();
      VirtualRouter neighborVirtualRouter =
          nodes.get(neighborName).getVirtualRouterOrThrow(neighborVrfName);

      if (connectingInterface.getRipEnabled()
          && !connectingInterface.getRipPassive()
          && neighborInterface.getRipEnabled()
          && !neighborInterface.getRipPassive()) {
        /*
         * We have a RIP neighbor relationship on this edge. So we should add all RIP routes
         * from this neighbor into our RIP internal staging rib, adding the incremental cost
         * (?), and using the neighborInterface's address as the next hop ip
         */
        for (RipInternalRoute neighborRoute : neighborVirtualRouter._ripInternalRib.getRoutes()) {
          long newCost = neighborRoute.getMetric() + RipProcess.DEFAULT_RIP_COST;
          Ip nextHopIp = neighborInterface.getConcreteAddress().getIp();
          RipInternalRoute newRoute =
              RipInternalRoute.builder()
                  .setNetwork(neighborRoute.getNetwork())
                  .setNextHop(NextHopIp.of(nextHopIp))
                  .setAdmin(admin)
                  .setMetric(newCost)
                  .build();
          if (!_ripInternalStagingRib.mergeRouteGetDelta(newRoute).isEmpty()) {
            changed = true;
          }
        }
      }
    }
    return changed;
  }

  private void queueOutgoingIsisRoutes(
      @Nonnull Map<String, Node> allNodes,
      NetworkConfigurations nc,
      @Nonnull RibDelta<IsisRoute> l1delta,
      @Nonnull RibDelta<IsisRoute> l2delta) {
    if (_vrf.getIsisProcess() == null || _isisIncomingRoutes == null) {
      return;
    }
    // All outgoing routes should have overload bit set
    RibDelta<IsisRoute> correctedL1Delta =
        _vrf.getIsisProcess().getOverload() ? setOverloadOnAllRoutes(l1delta) : l1delta;
    RibDelta<IsisRoute> correctedL2Delta =
        _vrf.getIsisProcess().getOverload() ? setOverloadOnAllRoutes(l2delta) : l2delta;

    // If this is an L1_L2 router, it must "upgrade" L1 routes to L2 routes
    boolean upgradeL1Routes =
        _vrf.getIsisProcess().getLevel1() != null
            && _vrf.getIsisProcess().getLevel2() != null
            // An L1-L2 router in overload mode stops leaking route information between L1 and L2
            // levels and clears its attached bit.
            && !_vrf.getIsisProcess().getOverload();

    // Loop over neighbors, enqueue messages
    for (IsisEdge edge : _isisIncomingRoutes.keySet()) {
      // Do not queue routes on non-active ISIS interface levels
      Interface iface = edge.getNode2().getInterface(nc);
      IsisInterfaceLevelSettings level1Settings = iface.getIsis().getLevel1();
      IsisInterfaceLevelSettings level2Settings = iface.getIsis().getLevel2();
      IsisLevel activeLevels = null;
      if (level1Settings != null && level1Settings.getMode() == IsisInterfaceMode.ACTIVE) {
        activeLevels = IsisLevel.LEVEL_1;
      }
      if (level2Settings != null && level2Settings.getMode() == IsisInterfaceMode.ACTIVE) {
        activeLevels = IsisLevel.union(activeLevels, IsisLevel.LEVEL_2);
      }
      if (activeLevels == null) {
        continue;
      }

      VirtualRouter remoteVr =
          allNodes
              .get(edge.getNode1().getNode())
              .getVirtualRouterOrThrow(edge.getNode1().getInterface(nc).getVrfName());
      Queue<RouteAdvertisement<IsisRoute>> queue = remoteVr._isisIncomingRoutes.get(edge.reverse());
      IsisLevel circuitType = edge.getCircuitType();
      if (circuitType.includes(IsisLevel.LEVEL_1) && activeLevels.includes(IsisLevel.LEVEL_1)) {
        queueDelta(queue, correctedL1Delta);
      }
      if (circuitType.includes(IsisLevel.LEVEL_2) && activeLevels.includes(IsisLevel.LEVEL_2)) {
        queueDelta(queue, correctedL2Delta);
        if (upgradeL1Routes) {
          // TODO: a little cumbersome, simplify later
          RibDelta.Builder<IsisRoute> upgradedRoutes = RibDelta.builder();
          correctedL1Delta.stream()
              .forEach(
                  ra -> {
                    IsisRoute l1Route = ra.getRoute();
                    RoutingProtocol l1Protocol = l1Route.getProtocol();
                    RoutingProtocol upgradedProtocol;
                    if (l1Protocol == RoutingProtocol.ISIS_L1) {
                      upgradedProtocol = RoutingProtocol.ISIS_L2;
                    } else if (l1Protocol == RoutingProtocol.ISIS_EL1) {
                      upgradedProtocol = RoutingProtocol.ISIS_EL2;
                    } else {
                      throw new IllegalStateException(
                          String.format("Unrecognized ISIS level 1 protocol: %s", l1Protocol));
                    }
                    int upgradedAdmin =
                        upgradedProtocol.getDefaultAdministrativeCost(_c.getConfigurationFormat());
                    Optional<IsisRoute> newRoute =
                        convertRouteLevel1ToLevel2(ra.getRoute(), upgradedProtocol, upgradedAdmin);
                    if (newRoute.isPresent()) {
                      IsisRoute r = newRoute.get();
                      if (ra.isWithdrawn()) {
                        _isisL2StagingRib.removeRoute(r);
                        upgradedRoutes.remove(newRoute.get(), ra.getReason());
                      } else {
                        _isisL2StagingRib.mergeRoute(r);
                        upgradedRoutes.add(newRoute.get());
                      }
                    }
                  });
          queueDelta(queue, upgradedRoutes.build());
        }
      }
    }
  }

  /**
   * Move IS-IS routes from L1/L2 staging RIBs into their respective "proper" RIBs. Following that,
   * move any resulting deltas into the combined IS-IS RIB, and finally, main RIB.
   *
   * @param allNodes all network nodes, keyed by hostname
   * @param l1Delta staging Level 1 delta
   * @param l2Delta staging Level 2 delta
   * @return true if any routes from given deltas were merged into the combined IS-IS RIB.
   */
  boolean unstageIsisRoutes(
      Map<String, Node> allNodes,
      NetworkConfigurations nc,
      RibDelta<IsisRoute> l1Delta,
      RibDelta<IsisRoute> l2Delta) {
    RibDelta<IsisRoute> d1 = importRibDelta(_isisL1Rib, l1Delta);
    RibDelta<IsisRoute> d2 = importRibDelta(_isisL2Rib, l2Delta);
    queueOutgoingIsisRoutes(allNodes, nc, d1, d2);
    Builder<IsisRoute> isisDeltaBuilder = RibDelta.builder();
    isisDeltaBuilder.from(importRibDelta(_isisRib, d1));
    isisDeltaBuilder.from(importRibDelta(_isisRib, d2));
    _mainRibRouteDeltaBuilder.from(
        RibDelta.importRibDelta(_mainRib, isisDeltaBuilder.build(), _name));
    return !d1.isEmpty() || !d2.isEmpty();
  }

  /** Merges staged RIP routes into the "real" RIP RIB */
  void unstageRipInternalRoutes() {
    importRib(_ripInternalRib, _ripInternalStagingRib);
  }

  /** Re-initialize RIBs (at the start of each iteration) and process-specific track states. */
  void reinitForNewIteration() {
    /*
     * RIBs not read from can just be re-initialized
     */
    _ripRib = new RipRib();

    /*
     * Add routes that cannot change (does not affect below computation)
     */
    _mainRibRouteDeltaBuilder.from(importRib(_mainRib, _independentRib));

    /*
     * Re-add independent RIP routes to ripRib for tie-breaking
     */
    importRib(_ripRib, _ripInternalRib);
  }

  public Configuration getConfiguration() {
    return _c;
  }

  ConnectedRib getConnectedRib() {
    return _connectedRib;
  }

  public Fib getFib() {
    return _fib;
  }

  Rib getMainRib() {
    return _mainRib;
  }

  /** Get current BGP routes. After dataplane computation, gets convergent BGP routes. */
  @Nonnull
  Set<Bgpv4Route> getBgpRoutes() {
    return _bgpRoutingProcess == null ? ImmutableSet.of() : _bgpRoutingProcess.getV4Routes();
  }

  /**
   * Get current BGP backup routes. After dataplane computation, gets convergent BGP backup routes.
   */
  @Nonnull
  Set<Bgpv4Route> getBgpBackupRoutes() {
    return _bgpRoutingProcess == null ? ImmutableSet.of() : _bgpRoutingProcess.getV4BackupRoutes();
  }

  /** Get the number of best-path BGP routes. To be used during dataplane computation only */
  int getNumBgpBestPaths() {
    return _bgpRoutingProcess == null ? 0 : _bgpRoutingProcess.getNumBgpv4BestPaths();
  }

  /**
   * Get the number of all BGP routes (with multipath, if applicable). To be used during dataplane
   * computation only
   */
  int getNumBgpPaths() {
    return _bgpRoutingProcess == null ? 0 : _bgpRoutingProcess.getV4Routes().size();
  }

  /** Convenience method to get the VirtualRouter's hostname */
  String getHostname() {
    return _c.getHostname();
  }

  /**
   * Compute the "hashcode" of this router for the iBDP purposes. The hashcode is computed from the
   * following data structures:
   *
   * <ul>
   *   <li>RIBs (e.g., {@link #_mainRib})
   *   <li>message queues
   *   <li>Routing processes
   * </ul>
   *
   * @return integer hashcode
   */
  int computeIterationHashCode() {
    return Streams.concat(
            // RIB State
            Stream.of(_mainRib.getRoutes()),
            // Message queues
            messageQueueStream(_isisIncomingRoutes),
            messageQueueStream(_crossVrfIncomingRoutes),
            // Exported routes
            _routesForIsisRedistribution.build().stream(),
            // Processes
            _ospfProcesses.values().stream().map(OspfRoutingProcess::iterationHashCode),
            _eigrpProcesses.values().stream().map(EigrpRoutingProcess::computeIterationHashCode),
            Stream.of(_bgpRoutingProcess == null ? 0 : _bgpRoutingProcess.iterationHashCode()))
        .collect(toOrderedHashCode());
  }

  @Nonnull
  PrefixTracer getPrefixTracer() {
    return _prefixTracer;
  }

  Optional<Rib> getRib(RibId id) {
    if (!_name.equals(id.getVrfName())) {
      return Optional.empty();
    }
    return Optional.ofNullable(_mainRibs.get(id.getRibName()));
  }

  public void enqueueCrossVrfRoutes(
      @Nonnull CrossVrfEdgeId remoteVrfToOurRib,
      @Nonnull Stream<RouteAdvertisement<AnnotatedRoute<AbstractRoute>>> routeAdverts,
      @Nullable String policyName) {
    if (!_crossVrfIncomingRoutes.containsKey(remoteVrfToOurRib)) {
      // We either messed up royally or https://github.com/batfish/batfish/issues/3050
      return;
    }

    Stream<RouteAdvertisement<AnnotatedRoute<AbstractRoute>>> filteredRoutes = routeAdverts;
    if (policyName != null) {
      RoutingPolicy policy = _c.getRoutingPolicies().get(policyName);
      filteredRoutes =
          routeAdverts
              .map(
                  ra -> {
                    AnnotatedRoute<AbstractRoute> annotatedRoute = ra.getRoute();
                    AbstractRouteBuilder<?, ?> routeBuilder = annotatedRoute.getRoute().toBuilder();
                    if (policy.process(annotatedRoute, routeBuilder, IN, alwaysFalse())) {
                      // Preserve original route's source VRF
                      return ra.toBuilder()
                          .setRoute(
                              new AnnotatedRoute<>(
                                  routeBuilder.build(), annotatedRoute.getSourceVrf()))
                          .build();
                    }
                    return null;
                  })
              .filter(Objects::nonNull);
    }

    Queue<RouteAdvertisement<AnnotatedRoute<AbstractRoute>>> queue =
        _crossVrfIncomingRoutes.get(remoteVrfToOurRib);
    filteredRoutes.forEach(queue::add);
  }

  void processCrossVrfRoutes() {
    _crossVrfIncomingRoutes.forEach(
        (edgeId, queue) -> {
          while (queue.peek() != null) {
            RouteAdvertisement<AnnotatedRoute<AbstractRoute>> ra = queue.remove();
            // TODO: handle non-default main RIBs based on RIB specified in edgeID
            // https://github.com/batfish/batfish/issues/3050
            if (ra.isWithdrawn()) {
              _mainRibRouteDeltaBuilder.from(_mainRib.removeRouteGetDelta(ra.getRoute()));
            } else {
              _mainRibRouteDeltaBuilder.from(_mainRib.mergeRouteGetDelta(ra.getRoute()));
            }
          }
        });
  }

  /**
   * Goes through VRFs that can leak routes into this routing instance, and enqueues all their new
   * routes in {@link #_crossVrfIncomingRoutes}.
   */
  void queueCrossVrfImports() {
    VrfLeakConfig vrfLeakConfig = _vrf.getVrfLeakConfig();
    if (vrfLeakConfig == null || vrfLeakConfig.getLeakAsBgp()) {
      return;
    }
    for (MainRibVrfLeakConfig leakConfig : vrfLeakConfig.getMainRibVrfLeakConfigs()) {
      String importFromVrf = leakConfig.getImportFromVrf();
      VirtualRouter exportingVR = _node.getVirtualRouterOrThrow(importFromVrf);
      CrossVrfEdgeId otherVrfToOurRib = new CrossVrfEdgeId(importFromVrf, RibId.DEFAULT_RIB_NAME);
      enqueueCrossVrfRoutes(
          otherVrfToOurRib,
          // TODO Will need to update once support is added for cross-VRF export policies
          exportingVR._mainRibDeltaPrevRound.stream(),
          leakConfig.getImportPolicy());
    }
  }

  @VisibleForTesting
  <R extends AbstractRoute> AnnotatedRoute<R> annotateRoute(@Nonnull R route) {
    return new AnnotatedRoute<>(route, _name);
  }

  @Nullable
  BgpRoutingProcess getBgpRoutingProcess() {
    return _bgpRoutingProcess;
  }

  /** Return all OSPF processes for this VRF */
  public Map<String, OspfRoutingProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  /** Return the current set of {@link Layer2Vni} associated with this VRF */
  public Set<Layer2Vni> getLayer2Vnis() {
    return _layer2Vnis;
  }

  /** Return the current set of {@link Layer3Vni} associated with this VRF */
  public Map<Integer, Layer3Vni> getLayer3Vnis() {
    return _layer3Vnis;
  }

  /** Check whether this virtual router has any remaining computation to do */
  boolean isDirty() {
    return
    // Route Deltas
    !_mainRibDeltaPrevRound.isEmpty()
        // Message queues
        || !_isisIncomingRoutes.values().stream().allMatch(Queue::isEmpty)
        || !_routesForIsisRedistribution.isEmpty()
        || !_crossVrfIncomingRoutes.values().stream().allMatch(Queue::isEmpty)
        // Processes
        || _ospfProcesses.values().stream().anyMatch(OspfRoutingProcess::isDirty)
        || _eigrpProcesses.values().stream().anyMatch(EigrpRoutingProcess::isDirty)
        || (_bgpRoutingProcess != null && _bgpRoutingProcess.isDirty());
  }

  void eigrpIteration(Map<String, Node> allNodes) {
    _eigrpProcesses.values().forEach(p -> p.executeIteration(allNodes));
  }

  /** Execute one OSPF iteration, for all processes */
  void ospfIteration(Map<String, Node> allNodes) {
    _ospfProcesses.values().forEach(p -> p.executeIteration(allNodes));
  }

  /** Execute one iteration of BGP route propagation. */
  void bgpIteration(Map<String, Node> allNodes) {
    if (_bgpRoutingProcess == null) {
      return;
    }
    _bgpRoutingProcess.startOfInnerRound();
    _bgpRoutingProcess.executeIteration(allNodes);
    // If we leak to or from EVPN or leak routes as BGP, do so here.
    if (_vrf.getVrfLeakConfig() != null) {
      bgpVrfLeak();
      evpnVrfLeak(allNodes);
    }
    updateFloodLists();
  }

  /** Import BGP routes from other VRFs on the same node, if configured */
  private void bgpVrfLeak() {
    // invariants of being called from bgpIteration
    assert _bgpRoutingProcess != null && _vrf.getVrfLeakConfig() != null;
    for (BgpVrfLeakConfig vrfLeakConfig : _vrf.getVrfLeakConfig().getBgpVrfLeakConfigs()) {
      LOGGER.debug("Leaking BGP routes from {} to {}", vrfLeakConfig.getImportFromVrf(), _name);
      Optional<BgpRoutingProcess> exportingBgpProc =
          _node
              .getVirtualRouter(vrfLeakConfig.getImportFromVrf())
              .map(VirtualRouter::getBgpRoutingProcess);
      if (exportingBgpProc.isPresent()) {
        _bgpRoutingProcess.importCrossVrfV4Routes(
            exportingBgpProc.get().getRoutesToLeak(), vrfLeakConfig);
      } else {
        LOGGER.error(
            "Leaking BGP routes from VRF {} to VRF {} on node {} failed. Exporting VRF has no BGP"
                + " process",
            vrfLeakConfig.getImportFromVrf(),
            _name,
            _c.getHostname());
      }
    }
  }

  /** Leak BGPv4 routes from other VRFs into EVPN on this VRF */
  private void evpnVrfLeak(Map<String, Node> allNodes) {
    // invariants of being called from bgpIteration
    assert _bgpRoutingProcess != null && _vrf.getVrfLeakConfig() != null;

    for (EvpnToBgpv4VrfLeakConfig leakConfig :
        _vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs()) {
      Optional<BgpRoutingProcess> exportingBgpProc =
          _node
              .getVirtualRouter(leakConfig.getImportFromVrf())
              .map(VirtualRouter::getBgpRoutingProcess);
      if (exportingBgpProc.isPresent()) {
        _bgpRoutingProcess.importCrossVrfEvpnRoutesToV4(
            exportingBgpProc.get().getEvpnRoutesToLeak(), leakConfig);
      } else {
        LOGGER.error(
            "Exporting EVPN routes to BGP from VRF {} to VRF {} on node {} failed. Exporting VRF"
                + " has no BGP process",
            leakConfig.getImportFromVrf(),
            _name,
            _c.getHostname());
      }
    }

    NetworkConfigurations nc =
        NetworkConfigurations.of(
            allNodes.entrySet().stream()
                .collect(
                    ImmutableMap.toImmutableMap(
                        Entry::getKey, e -> e.getValue().getConfiguration())));
    for (Bgpv4ToEvpnVrfLeakConfig leakConfig :
        _vrf.getVrfLeakConfig().getBgpv4ToEvpnVrfLeakConfigs()) {
      Optional<VirtualRouter> exportingVr = _node.getVirtualRouter(leakConfig.getImportFromVrf());
      Optional<BgpRoutingProcess> exportingBgpProc =
          exportingVr.map(VirtualRouter::getBgpRoutingProcess);
      if (exportingBgpProc.isPresent()) {
        // Use immutable _vrf.getLayer3Vnis() since we don't care about learned IPs here.
        Collection<Layer3Vni> exportingVrfL3Vnis = exportingVr.get()._vrf.getLayer3Vnis().values();
        if (exportingVrfL3Vnis.size() == 1) {
          int vni = exportingVrfL3Vnis.iterator().next().getVni();
          _bgpRoutingProcess.importCrossVrfV4RoutesToEvpn(
              exportingBgpProc.get().getRoutesToLeak(), leakConfig, vni, nc, allNodes);
        } else {
          LOGGER.error(
              "Exporting BGP routes to EVPN from VRF {} to VRF {} on node {} failed. Exporting VRF"
                  + " expected to have exactly one layer 3 VNI, but has {}",
              leakConfig.getImportFromVrf(),
              _name,
              _c.getHostname(),
              exportingVrfL3Vnis.size());
        }
      } else {
        LOGGER.error(
            "Exporting BGP routes to EVPN from VRF {} to VRF {} on node {} failed. Exporting VRF"
                + " has no BGP process",
            leakConfig.getImportFromVrf(),
            _name,
            _c.getHostname());
      }
    }
  }

  /**
   * Process EVPN type 3 routes in our RIB and update flood lists for any {@link Layer2Vni} if
   * necessary.
   */
  private void updateFloodLists() {
    if (_bgpRoutingProcess == null) {
      // an extra safe guard; should only be called from bgpIteration
      return;
    }
    for (EvpnType3Route route : _bgpRoutingProcess.getEvpnType3Routes()) {
      _layer2Vnis =
          _layer2Vnis.stream()
              .map(vs -> updateVniFloodList(vs, route))
              .collect(ImmutableSet.toImmutableSet());
    }
  }

  /**
   * Update flood list for the given {@link Layer2Vni} based on information contained in {@code
   * route}. Only updates the VNI if the route is <strong>not</strong> for the VNI's source address
   * and if the {@link Layer2Vni#getBumTransportMethod()} is unicast flood group (otherwise returns
   * the original {@code vs}).
   */
  private static Layer2Vni updateVniFloodList(Layer2Vni vs, EvpnType3Route route) {
    if (vs.getBumTransportMethod() != BumTransportMethod.UNICAST_FLOOD_GROUP
        || route.getVniIp().equals(vs.getSourceAddress())) {
      // Only update settings if transport method is unicast.
      // Do not add our own source to the flood list.
      return vs;
    }
    return vs.addToFloodList(route.getVniIp());
  }

  /**
   * Process EVPN type 5 routes in our RIB and update learned VTEPs on corresponding {@link
   * Layer3Vni}s if necessary.
   */
  public void updateLayer3Vnis() {
    if (_bgpRoutingProcess == null) {
      // won't have next hop VTEP routes without a BGP process
      return;
    }
    SetMultimap<Integer, Ip> vtepsByVni = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
    _mainRib.getRoutes().stream()
        .map(r -> r.getAbstractRoute().getNextHop())
        .filter(NextHopVtep.class::isInstance)
        .map(NextHopVtep.class::cast)
        .forEach(nextHopVtep -> vtepsByVni.put(nextHopVtep.getVni(), nextHopVtep.getVtepIp()));
    Map<Integer, Layer3Vni> newLayer3Vnis = new HashMap<>(_layer3Vnis);
    vtepsByVni
        .asMap()
        .forEach(
            (vni, ips) -> {
              Layer3Vni l3Vni = _layer3Vnis.get(vni);
              if (l3Vni == null) {
                // shouldn't happen, but skip just in case
                LOGGER.warn(
                    String.format(
                        "updateLayer3Vnis: Host '%s' vrf '%s' has route(s) whose next hop is"
                            + " unconfigured VNI %d with VTEP IP(s) %s",
                        _c.getHostname(), getName(), vni, ips));
                return;
              }
              newLayer3Vnis.put(
                  vni,
                  l3Vni.toBuilder().setLearnedNexthopVtepIps(ImmutableSet.copyOf(ips)).build());
            });
    _layer3Vnis = ImmutableMap.copyOf(newLayer3Vnis);
  }

  /**
   * Activates and deactivates routes in protocol-specific routing processes based on what NHIPs are
   * now resolvable.
   */
  void updateResolvableRoutes() {
    if (_bgpRoutingProcess != null) {
      _bgpRoutingProcess.updateResolvableRoutes(_mainRibDeltaPrevRound);
    }
  }

  /** Redistribute routes learned in the previous round into known routing processes */
  void redistribute() {
    Streams.concat(
            _ospfProcesses.values().stream(),
            _eigrpProcesses.values().stream(),
            _bgpRoutingProcess != null ? Stream.of(_bgpRoutingProcess) : Stream.empty())
        .forEach(p -> p.redistribute(_mainRibDeltaPrevRound));
    if (_vrf.getIsisProcess() != null) {
      _routesForIsisRedistribution.from(_mainRibDeltaPrevRound);
    }
  }

  void mergeEigrpRoutesToMainRib() {
    _eigrpProcesses
        .values()
        .forEach(
            p ->
                _mainRibRouteDeltaBuilder.from(
                    importRibDelta(_mainRib, p.getUpdatesForMainRib(), _name)));
  }

  void mergeOspfRoutesToMainRib() {
    _ospfProcesses
        .values()
        .forEach(
            p ->
                _mainRibRouteDeltaBuilder.from(
                    importRibDelta(_mainRib, p.getUpdatesForMainRib(), _name)));
  }

  void mergeBgpRoutesToMainRib() {
    if (_bgpRoutingProcess == null) {
      return;
    }
    _mainRibRouteDeltaBuilder.from(
        importRibDelta(_mainRib, _bgpRoutingProcess.getUpdatesForMainRib(), _name));
  }

  /** End of a single "EGP" routing round. */
  void endOfEgpRound() {
    _mainRibDeltaPrevRound = _mainRibRouteDeltaBuilder.build();
    _mainRibRouteDeltaBuilder = RibDelta.builder();
    if (_bgpRoutingProcess != null) {
      _bgpRoutingProcess.endOfRound();
    }
  }

  /** End of a single "EGP" inner routing round (schedule). */
  void endOfEgpInnerRound() {
    if (_bgpRoutingProcess != null) {
      _bgpRoutingProcess.endOfInnerRound();
    }
  }

  /** Return all EVPN routes in this VRF */
  @Nonnull
  Set<EvpnRoute<?, ?>> getEvpnRoutes() {
    return _bgpRoutingProcess == null ? ImmutableSet.of() : _bgpRoutingProcess.getEvpnRoutes();
  }

  /** Return all EVPN backup routes in this VRF */
  @Nonnull
  Set<EvpnRoute<?, ?>> getEvpnBackupRoutes() {
    return _bgpRoutingProcess == null
        ? ImmutableSet.of()
        : _bgpRoutingProcess.getEvpnBackupRoutes();
  }

  /** Return the VRF name */
  public @Nonnull String getName() {
    return _name;
  }
}
