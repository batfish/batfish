package org.batfish.dataplane.ibdp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.common.util.CollectionUtil.toOrderedHashCode;
import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;
import static org.batfish.datamodel.routing_policy.Environment.Direction.IN;
import static org.batfish.dataplane.protocols.IsisProtocolHelper.convertRouteLevel1ToLevel2;
import static org.batfish.dataplane.protocols.IsisProtocolHelper.exportNonIsisRouteToIsis;
import static org.batfish.dataplane.protocols.IsisProtocolHelper.setOverloadOnAllRoutes;
import static org.batfish.dataplane.protocols.StaticRouteHelper.shouldActivateNextHopIpRoute;
import static org.batfish.dataplane.rib.AbstractRib.importRib;
import static org.batfish.dataplane.rib.RibDelta.importRibDelta;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import com.google.common.graph.Network;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.EvpnType3Route;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibImpl;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.GenericRibReadOnly;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.LocalRoute;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.RipInternalRoute;
import org.batfish.datamodel.RipProcess;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrfLeakingConfig;
import org.batfish.datamodel.bgp.BgpTopology;
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
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.MainRib;
import org.batfish.datamodel.routing_policy.expr.RibExpr;
import org.batfish.datamodel.visitors.RibExprVisitor;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.dataplane.protocols.GeneratedRouteHelper;
import org.batfish.dataplane.rib.AbstractRib;
import org.batfish.dataplane.rib.AnnotatedRib;
import org.batfish.dataplane.rib.ConnectedRib;
import org.batfish.dataplane.rib.IsisLevelRib;
import org.batfish.dataplane.rib.IsisRib;
import org.batfish.dataplane.rib.KernelRib;
import org.batfish.dataplane.rib.LocalRib;
import org.batfish.dataplane.rib.Rib;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RipInternalRib;
import org.batfish.dataplane.rib.RipRib;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.StaticRib;

public final class VirtualRouter {

  /** Visitor that evaluates a {@link RibExpr}, yielding an {@link AbstractRib} . */
  @ParametersAreNonnullByDefault
  public static final class RibExprEvaluator
      implements RibExprVisitor<GenericRibReadOnly<?>, Void>,
          BiFunction<RibExpr, PrefixSpace, Boolean> {

    public RibExprEvaluator(GenericRibReadOnly<?> mainRib) {
      // TODO: cleaner construction, especially when multiple RIBs might be required
      _mainRib = mainRib;
    }

    @Nonnull
    @Override
    public GenericRibReadOnly<?> visitMainRib(MainRib mainRib, Void arg) {
      return _mainRib;
    }

    private final GenericRibReadOnly<?> _mainRib;

    @Override
    public Boolean apply(RibExpr ribExpr, PrefixSpace prefixSpace) {
      GenericRibReadOnly<?> rib = ribExpr.accept(this, null);
      return rib.intersectsPrefixSpace(prefixSpace);
    }
  }

  /** The BGP routing process. {@code null} if BGP is not configured for this VRF */
  @Nullable BgpRoutingProcess _bgpRoutingProcess;

  /** Parent configuration for this virtual router */
  @Nonnull private final Configuration _c;

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

  IsisLevelRib _isisL1Rib;
  IsisLevelRib _isisL2Rib;
  private IsisLevelRib _isisL1StagingRib;
  private IsisLevelRib _isisL2StagingRib;
  private IsisRib _isisRib;
  KernelRib _kernelRib;
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
  @Nonnull private final String _name;
  /** Parent {@link Node} on which this virtual router resides */
  @Nonnull private final Node _node;

  private Map<String, OspfRoutingProcess> _ospfProcesses;

  RipInternalRib _ripInternalRib;
  RipInternalRib _ripInternalStagingRib;
  RipRib _ripRib;
  StaticRib _staticUnconditionalRib;
  StaticRib _staticNextHopRib;

  /** FIB (forwarding information base) built from the main RIB */
  private Fib _fib;

  /** RIB containing generated routes */
  private Rib _generatedRib;

  /** Metadata about propagated prefixes to/from neighbors */
  @Nonnull private PrefixTracer _prefixTracer;

  /** List of all EIGRP processes in this VRF */
  @VisibleForTesting ImmutableMap<Long, EigrpRoutingProcess> _eigrpProcesses;

  /**
   * Layer 2 VNI settings that are updated dynamically as the dataplane is being computed (e.g.,
   * based on EVPN route advertisements).
   */
  private Set<Layer2Vni> _layer2Vnis;
  /**
   * Layer 3 VNI settings that are updated dynamically as the dataplane is being computed (e.g.,
   * based on EVPN route advertisements).
   */
  private Set<Layer3Vni> _layer3Vnis;

  /** A {@link Vrf} that this virtual router represents */
  final Vrf _vrf;

  @Nonnull private final RibExprEvaluator _ribExprEvaluator;

  @Nonnull
  private final ResolutionRestriction<AnnotatedRoute<AbstractRoute>> _resolutionRestriction;

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
    _layer3Vnis = ImmutableSet.copyOf(_vrf.getLayer3Vnis().values());
    if (_vrf.getBgpProcess() != null) {
      _bgpRoutingProcess =
          new BgpRoutingProcess(
              _vrf.getBgpProcess(), _c, _name, _mainRib, BgpTopology.EMPTY, _prefixTracer);
    }
    _ribExprEvaluator = new RibExprEvaluator(_mainRib);
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
    delta
        .getActions()
        .forEach(
            r -> {
              @SuppressWarnings("unchecked") // Ok to upcast to R since immutable.
              RouteAdvertisement<R> cast = (RouteAdvertisement<R>) r;
              queue.add(cast);
            });
  }

  /**
   * Initializes helper data structures and easy-to-compute RIBs that are not affected by BDP
   * iterations (e.g., static route RIB, connected route RIB, etc.)
   */
  @VisibleForTesting
  void initForIgpComputation(TopologyContext topologyContext) {
    initConnectedRib();
    initKernelRib();
    initLocalRib();
    initStaticRibs();
    // Always import local and connected routes into your own rib
    importRib(_independentRib, _connectedRib);
    importRib(_independentRib, _kernelRib);
    importRib(_independentRib, _localRib);
    importRib(_independentRib, _staticUnconditionalRib, _name);
    importRib(_mainRib, _independentRib);

    // Now check whether any rib groups are applied
    RibGroup connectedRibGroup = _vrf.getAppliedRibGroups().get(RoutingProtocol.CONNECTED);
    importRib(_mainRib, _connectedRib);
    if (connectedRibGroup != null) {
      applyRibGroup(connectedRibGroup, _connectedRib);
    }
    RibGroup localRibGroup = _vrf.getAppliedRibGroups().get(RoutingProtocol.LOCAL);
    if (localRibGroup != null) {
      applyRibGroup(localRibGroup, _localRib);
    }

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

  /** Apply a rib group to a given source rib (which belongs to this VRF) */
  private void applyRibGroup(@Nonnull RibGroup ribGroup, @Nonnull AnnotatedRib<?> sourceRib) {
    RoutingPolicy policy = _c.getRoutingPolicies().get(ribGroup.getImportPolicy());
    checkState(policy != null, "RIB group %s is missing import policy", ribGroup.getName());
    sourceRib.getTypedRoutes().stream()
        .map(
            route -> {
              AbstractRouteBuilder<?, ?> builder = route.getRoute().toBuilder();
              boolean accept = policy.process(route, builder, IN, _ribExprEvaluator);
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
   */
  void initForEgpComputationWithNewTopology(TopologyContext topologyContext) {
    assert _mainRibRouteDeltaBuilder.isEmpty(); // or else invariant is not maintained

    initQueuesAndDeltaBuilders(topologyContext);
    if (_bgpRoutingProcess != null) {
      // If the process exists, update the topology
      _bgpRoutingProcess.updateTopology(topologyContext.getBgpTopology());
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
        RibDelta.<AnnotatedRoute<AbstractRoute>>builder().add(_mainRib.getTypedRoutes()).build();
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
    for (VrfLeakingConfig leakConfig : _vrf.getVrfLeakConfigs()) {
      String importFromVrf = leakConfig.getImportFromVrf();
      VirtualRouter exportingVR = _node.getVirtualRouterOrThrow(importFromVrf);
      CrossVrfEdgeId otherVrfToOurRib = new CrossVrfEdgeId(importFromVrf, RibId.DEFAULT_RIB_NAME);
      enqueueCrossVrfRoutes(
          otherVrfToOurRib,
          // TODO Will need to update once support is added for cross-VRF export policies
          exportingVR._mainRib.getTypedRoutes().stream().map(RouteAdvertisement::new),
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
              gr, generationPolicy, _mainRib.getTypedRoutes(), _ribExprEvaluator);

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
      d.getActions()
          .filter(RouteAdvertisement::isWithdrawn)
          .forEach(r -> _bgpRoutingProcess.removeAggregate(r.getRoute().getRoute()));
    }
  }

  /**
   * Activate static routes with next hop IP. Adds a static route {@code route} to the main RIB if
   * there exists an active route to the {@code routes}'s next-hop-ip.
   *
   * <p>Removes static route from the main RIB for which next-hop-ip has become unreachable.
   */
  void activateStaticRoutes() {
    for (StaticRoute sr : _staticNextHopRib.getTypedRoutes()) {
      if (shouldActivateNextHopIpRoute(sr, _mainRib, _resolutionRestriction)) {
        _mainRibRouteDeltaBuilder.from(_mainRib.mergeRouteGetDelta(annotateRoute(sr)));
      } else {
        /*
         * If the route is not in the RIB, this has no effect. But might add some overhead (TODO)
         */
        _mainRibRouteDeltaBuilder.from(_mainRib.removeRouteGetDelta(annotateRoute(sr)));
      }
    }
  }

  /** Compute the FIB from the main RIB */
  public void computeFib() {
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
  @Nonnull
  private static Stream<ConnectedRoute> generateConnectedRoutes(@Nonnull Interface iface) {
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
  @Nonnull
  static ConnectedRoute generateConnectedRoute(
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
   * Initialize the kernel RIB -- a RIB containing non-forwarding routes installed unconditionally
   * for the purpose of redistribution
   */
  @VisibleForTesting
  void initKernelRib() {
    _vrf.getKernelRoutes().stream().map(this::annotateRoute).forEach(_kernelRib::mergeRoute);
  }

  /**
   * Initialize the local RIB -- a RIB containing non-forwarding /32 routes for exact addresses of
   * interfaces
   */
  @VisibleForTesting
  void initLocalRib() {
    // Look at all interfaces in our VRF
    _c.getActiveInterfaces(_name).values().stream()
        .flatMap(VirtualRouter::generateLocalRoutes)
        .forEach(r -> _localRib.mergeRoute(annotateRoute(r)));
  }

  /**
   * Generate local routes for a given active interface. Returns an empty stream if the interface
   * generates no local routes based on {@link ConnectedRouteMetadata#getGenerateLocalRoute()} or
   * Batfish policy (only addresses with network length of < /32 are considered).
   */
  @Nonnull
  private static Stream<LocalRoute> generateLocalRoutes(@Nonnull Interface iface) {
    assert iface.getActive();
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
  @Nonnull
  static LocalRoute generateLocalRoute(
      @Nonnull ConcreteInterfaceAddress address,
      @Nonnull String ifaceName,
      @Nullable ConnectedRouteMetadata metadata) {
    LocalRoute.Builder builder =
        LocalRoute.builder()
            .setNetwork(address.getIp().toPrefix())
            .setSourcePrefixLength(address.getNetworkBits())
            .setNextHopInterface(ifaceName);
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
      _routesForIsisRedistribution.add(_mainRib.getTypedRoutes());
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
    _routesForIsisRedistribution
        .build()
        .getActions()
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
    _kernelRib = new KernelRib();
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
    _staticNextHopRib = new StaticRib();
    _staticUnconditionalRib = new StaticRib();
  }

  private boolean isL1Only() {
    IsisProcess proc = _vrf.getIsisProcess();
    if (proc == null) {
      return false;
    }
    return proc.getLevel1() != null && proc.getLevel2() == null;
  }

  /**
   * Initialize the static route RIBs from the VRF config. Interface and next-vrf routes go into
   * {@link #_staticUnconditionalRib}; routes that only have next-hop-ip go into {@link
   * #_staticNextHopRib}
   */
  @VisibleForTesting
  void initStaticRibs() {
    for (StaticRoute sr : _vrf.getStaticRoutes()) {
      new NextHopVisitor<Void>() {

        @Override
        public Void visitNextHopIp(NextHopIp nextHopIp) {
          // We have a next-hop-ip route, keep in it that RIB
          _staticNextHopRib.mergeRouteGetDelta(sr);
          return null;
        }

        @Override
        public Void visitNextHopInterface(NextHopInterface nextHopInterface) {
          // We have an interface route, check that interface exists and is active
          Interface iface = _c.getAllInterfaces().get(nextHopInterface.getInterfaceName());
          if (iface == null || !iface.getActive()) {
            return null;
          }
          _staticUnconditionalRib.mergeRouteGetDelta(sr);
          return null;
        }

        @Override
        public Void visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          // Null routes are always active unconditionally
          _staticUnconditionalRib.mergeRouteGetDelta(sr);
          return null;
        }

        @Override
        public Void visitNextHopVrf(NextHopVrf nextHopVrf) {
          // next vrf routes are always active unconditionally
          _staticUnconditionalRib.mergeRouteGetDelta(sr);
          return null;
        }
      }.visit(sr.getNextHop());
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
              deltaBuilder.remove(newRoute);
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
        for (RipInternalRoute neighborRoute :
            neighborVirtualRouter._ripInternalRib.getTypedRoutes()) {
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
          correctedL1Delta
              .getActions()
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
                        upgradedRoutes.remove(newRoute.get());
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

  /** Re-initialize RIBs (at the start of each iteration). */
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
    return _bgpRoutingProcess == null ? 0 : _bgpRoutingProcess.getBestPathRoutes().size();
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
            Stream.of(_mainRib.getTypedRoutes()),
            // Exported routes
            // Message queues
            Stream.of(_isisIncomingRoutes, _crossVrfIncomingRoutes)
                .flatMap(m -> m.values().stream())
                .flatMap(Queue::stream),
            Stream.of(_routesForIsisRedistribution),
            // Processes
            Stream.of(_ospfProcesses.values().stream().map(OspfRoutingProcess::iterationHashCode)),
            Stream.of(_eigrpProcesses)
                .flatMap(m -> m.values().stream())
                .map(EigrpRoutingProcess::computeIterationHashCode),
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
                    if (policy.process(annotatedRoute, routeBuilder, IN, _ribExprEvaluator)) {
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
    for (VrfLeakingConfig leakConfig : _vrf.getVrfLeakConfigs()) {
      if (leakConfig.leakAsBgp()) {
        /* handled in bgpIteration() */
        continue;
      }
      String importFromVrf = leakConfig.getImportFromVrf();
      VirtualRouter exportingVR = _node.getVirtualRouterOrThrow(importFromVrf);
      CrossVrfEdgeId otherVrfToOurRib = new CrossVrfEdgeId(importFromVrf, RibId.DEFAULT_RIB_NAME);
      enqueueCrossVrfRoutes(
          otherVrfToOurRib,
          // TODO Will need to update once support is added for cross-VRF export policies
          exportingVR._mainRibDeltaPrevRound.getActions(),
          leakConfig.getImportPolicy());
    }
  }

  private <R extends AbstractRoute> AnnotatedRoute<R> annotateRoute(@Nonnull R route) {
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
  public Set<Layer3Vni> getLayer3Vnis() {
    return _layer3Vnis;
  }

  /** Check whether this virtual router has any remaining computation to do */
  boolean isDirty() {
    return
    // Route Deltas
    !_mainRibRouteDeltaBuilder.isEmpty()
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
    // If we must leak routes as BGP, do so here.
    bgpVrfLeak();
    updateFloodLists();
  }

  /** Import BGP routes from other VRFs on the same node, if configured */
  private void bgpVrfLeak() {
    assert _bgpRoutingProcess != null; // invariant of being called from bgpIteration
    for (VrfLeakingConfig vrfLeakConfig : _vrf.getVrfLeakConfigs()) {
      if (!vrfLeakConfig.leakAsBgp()) {
        /* Handled in queueCrossVrfImports() */
        continue;
      }
      LOGGER.debug("Leaking BGP routes from {} to {}", vrfLeakConfig.getImportFromVrf(), _name);
      Optional<BgpRoutingProcess> exportingBgpProc =
          _node
              .getVirtualRouter(vrfLeakConfig.getImportFromVrf())
              .map(VirtualRouter::getBgpRoutingProcess);
      if (exportingBgpProc.isPresent()) {
        assert vrfLeakConfig.getBgpConfig() != null; // invariant of leakAsBgp()
        _bgpRoutingProcess.importCrossVrfV4Routes(
            exportingBgpProc.get().getRoutesToLeak(),
            vrfLeakConfig.getImportPolicy(),
            vrfLeakConfig.getImportFromVrf(),
            vrfLeakConfig.getBgpConfig());
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
  @Nonnull
  public String getName() {
    return _name;
  }
}
