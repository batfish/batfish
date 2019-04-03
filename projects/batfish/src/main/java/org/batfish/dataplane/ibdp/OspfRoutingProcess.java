package org.batfish.dataplane.ibdp;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.OspfRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfNeighborConfigId;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.OspfSessionProperties;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.dataplane.rib.AbstractRib;
import org.batfish.dataplane.rib.OspfInterAreaRib;
import org.batfish.dataplane.rib.OspfIntraAreaRib;
import org.batfish.dataplane.rib.OspfRib;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/** An OSPF routing process, a dataplane version of {@link OspfProcess} */
@ParametersAreNonnullByDefault
final class OspfRoutingProcess implements RoutingProcess<OspfTopology, OspfRoute> {
  /* Provided configuration */
  /** The configuration/datamodel process */
  @Nonnull private final OspfProcess _process;
  /** The parent {@link Configuration} */
  @Nonnull private final Configuration _c;
  /** The name of the VRF we are in */
  @Nonnull private final String _vrfName;
  /** The current known topology */
  @Nonnull private OspfTopology _topology;

  /* Computed configuration & cached variables */
  private final Boolean _useMinMetricForSummaries;

  /* Internal RIBs */
  @Nonnull private final OspfIntraAreaRib _intraAreaRib;
  @Nonnull private final OspfInterAreaRib _interAreaRib;
  @Nonnull private final OspfRib _ospfRib;

  /* Message queues */
  @Nonnull
  private SortedMap<OspfTopology.EdgeId, Queue<RouteAdvertisement<OspfIntraAreaRoute>>>
      _intraAreaIncomingRoutes = ImmutableSortedMap.of();

  @Nonnull
  private SortedMap<EdgeId, Queue<RouteAdvertisement<OspfInterAreaRoute>>>
      _interAreaIncomingRoutes = ImmutableSortedMap.of();

  /** Delta which captures process initialization (creating intra-area routes based on interfaces */
  @Nonnull private RibDelta<OspfIntraAreaRoute> _initializationDelta;
  /** Delta to pass to the main RIB */
  @Nonnull private RibDelta.Builder<OspfRoute> _changeset;

  OspfRoutingProcess(
      OspfProcess process, String vrfName, Configuration configuration, OspfTopology topology) {
    _c = configuration;
    _vrfName = vrfName;
    _process = process;
    _topology = topology;

    _intraAreaRib = new OspfIntraAreaRib();
    _interAreaRib = new OspfInterAreaRib();
    _ospfRib = new OspfRib();

    updateQueues(topology);

    // Determine whether to use min metric by default, based on RFC1583 compatibility setting.
    // Routers (at least Cisco and Juniper) default to min metric unless using RFC2328 with
    // RFC1583 compatibility explicitly disabled, in which case they default to max.
    _useMinMetricForSummaries = firstNonNull(process.getRfc1583Compatible(), Boolean.TRUE);

    _changeset = RibDelta.builder();
    _initializationDelta = RibDelta.empty();
  }

  @Override
  public void initialize() {
    initializeIntraAreaRoutes();
  }

  @Override
  public void executeIteration(Map<String, Node> allNodes) {
    // Clear changeset from previous iteration
    _changeset = RibDelta.builder();

    if (!_initializationDelta.isEmpty()) {
      // If we haven't sent out the first round of updates after initialization, do so now. Then
      // clear the initialization delta
      sendOutInternalRoutes(
          new InternalDelta(_initializationDelta, RibDelta.empty()), allNodes, _topology);
      _initializationDelta = RibDelta.empty();
    }

    // Process internal routes
    InternalDelta internalDelta = processInternalRoutes();
    sendOutInternalRoutes(internalDelta, allNodes, _topology);

    // TODO: Process external routes

    // Keep track of what what updates will go into the main RIB
    _changeset.from(RibDelta.importRibDelta(_ospfRib, internalDelta._intraArea));
    _changeset.from(RibDelta.importRibDelta(_ospfRib, internalDelta._interArea));
  }

  /** Update incoming message queues based on a new topology */
  private void updateQueues(OspfTopology topology) {
    // Preserve existing intra-area queues
    ImmutableSortedMap.Builder<EdgeId, Queue<RouteAdvertisement<OspfIntraAreaRoute>>>
        intraAreaBuilder = ImmutableSortedMap.naturalOrder();
    intraAreaBuilder.putAll(_intraAreaIncomingRoutes);
    // Preserve existing inter-area queues
    ImmutableSortedMap.Builder<EdgeId, Queue<RouteAdvertisement<OspfInterAreaRoute>>>
        interAreaBuilder = ImmutableSortedMap.naturalOrder();
    interAreaBuilder.putAll(_interAreaIncomingRoutes);

    getEdgeStream(topology)
        .forEach(
            edgeId -> {
              if (!_intraAreaIncomingRoutes.keySet().contains(edgeId)) {
                intraAreaBuilder.put(edgeId, new ConcurrentLinkedQueue<>());
              }
              if (!_interAreaIncomingRoutes.keySet().contains(edgeId)) {
                interAreaBuilder.put(edgeId, new ConcurrentLinkedQueue<>());
              }
            });
    // TODO: add external queues
    _intraAreaIncomingRoutes = intraAreaBuilder.build();
    _interAreaIncomingRoutes = interAreaBuilder.build();
  }

  @Override
  public void updateTopology(OspfTopology topology) {
    _topology = topology;
    updateQueues(topology);
  }

  @Nonnull
  @Override
  public RibDelta<OspfRoute> getUpdatesForMainRib() {
    return _changeset.build();
  }

  @Override
  public void redistribute(RibDelta<? extends AbstractRouteDecorator> mainRibDelta) {
    // TODO: take routes initialize external routes from them, send out to neighbors
  }

  @Override
  public boolean isDirty() {
    return !_changeset.build().isEmpty()
        || !_interAreaIncomingRoutes.values().stream().allMatch(Queue::isEmpty)
        || !_intraAreaIncomingRoutes.values().stream().allMatch(Queue::isEmpty);
  }

  /** Initialize intra-area routes based on available interfaces. */
  private void initializeIntraAreaRoutes() {
    RibDelta.Builder<OspfIntraAreaRoute> deltaBuilder = RibDelta.builder();
    _process.getAreas().values().forEach(area -> deltaBuilder.from(initializeRoutesByArea(area)));
    _initializationDelta = deltaBuilder.build();
    _changeset.from(RibDelta.importRibDelta(_ospfRib, _initializationDelta));
  }

  /**
   * Initialize intra-area routes based on available interfaces in each OSPF area.
   *
   * @param area {@link OspfArea area configuration}
   */
  @Nonnull
  @VisibleForTesting
  RibDelta<OspfIntraAreaRoute> initializeRoutesByArea(OspfArea area) {
    RibDelta.Builder<OspfIntraAreaRoute> deltaBuilder = RibDelta.builder();

    for (String ifaceName : area.getInterfaces()) {
      Interface iface = _c.getAllInterfaces().get(ifaceName);
      if (iface == null) {
        // Skip non-existent interfaces
        continue;
      }
      if (!iface.getActive()) {
        // Skip interfaces that are down
        continue;
      }
      // Create a route for each interface address
      Set<OspfIntraAreaRoute> allRoutes =
          iface.getAllAddresses().stream()
              .map(
                  ifaceAddr ->
                      computeIntraAreaRouteFromInterface(area.getAreaNumber(), iface, ifaceAddr))
              .collect(ImmutableSet.toImmutableSet());

      allRoutes.forEach(r -> deltaBuilder.from(_intraAreaRib.mergeRouteGetDelta(r)));
    }
    return deltaBuilder.build();
  }

  /**
   * Make an intraAreaRoute given the interface and one of its prefixes.
   *
   * @param areaNum the OSPF area the interface (and thus the route) belongs to
   * @param iface the {@link Interface} from which the route will be constructed
   * @param ifaceAddress the {@link InterfaceAddress} for which the route is being constructed. Will
   *     inform the route's network and next hop IP.
   */
  @Nonnull
  private OspfIntraAreaRoute computeIntraAreaRouteFromInterface(
      Long areaNum, Interface iface, InterfaceAddress ifaceAddress) {

    return OspfIntraAreaRoute.builder()
        .setNetwork(ifaceAddress.getPrefix())
        .setNextHopIp(ifaceAddress.getIp())
        .setAdmin(_process.getAdminCosts().get(RoutingProtocol.OSPF))
        .setMetric(getIncrementalCost(iface, true))
        .setArea(areaNum)
        .build();
  }

  /** Check if this is an area border router */
  private boolean isABR() {
    return _process.isAreaBorderRouter();
  }

  /**
   * Extract the {@link OspfRoutingProcess} belonging to the given {@link OspfNeighborConfigId
   * OspfNeighborId}
   */
  @Nullable
  private static OspfRoutingProcess getNeighborProcess(
      OspfNeighborConfigId ospfNeighborId, Map<String, Node> allNodes) {
    return allNodes
        .get(ospfNeighborId.getHostname())
        .getVirtualRouters()
        .get(ospfNeighborId.getVrfName())
        .getOspfProcesses()
        .get(ospfNeighborId.getProcName());
  }

  @Nonnull
  private Stream<EdgeId> getEdgeStream(OspfTopology topology) {
    return _process.getOspfNeighborConfigs().keySet().stream()
        .flatMap(
            interfaceName ->
                topology
                    .incomingEdges(
                        new OspfNeighborConfigId(
                            _c.getHostname(), _vrfName, _process.getProcessId(), interfaceName))
                    .stream());
  }

  /**
   * Given a route advertisement, add or remove the route from the RIB. Any (import) transformations
   * must already have been applied to the route.
   */
  @Nonnull
  private static <R extends AbstractRouteDecorator, T extends R>
      RibDelta<R> processRouteAdvertisement(RouteAdvertisement<T> ra, AbstractRib<R> rib) {
    if (ra.isWithdrawn()) {
      rib.removeBackupRoute(ra.getRoute());
      return rib.removeRouteGetDelta(ra.getRoute(), ra.getReason());
    } else {
      rib.addBackupRoute(ra.getRoute());
      return rib.mergeRouteGetDelta(ra.getRoute());
    }
  }

  /** Process all OSPF internal messages from all the message queues */
  @Nonnull
  private InternalDelta processInternalRoutes() {
    RibDelta<OspfIntraAreaRoute> intraAreaDelta = processIntraAreaRoutes();
    RibDelta.Builder<OspfInterAreaRoute> interAreaDelta = processInterAreaRoutes();
    RibDelta<OspfInterAreaRoute> deltaOfSummaries = computeInterAreaSummaries();
    return new InternalDelta(intraAreaDelta, interAreaDelta.from(deltaOfSummaries).build());
  }

  /**
   * Return the interface cost for a given interface.
   *
   * <p>For actual details see {@link #getIncrementalCost(Interface, boolean)}
   *
   * @param interfaceName name of the interface
   * @param stub whether this is a stub network
   */
  @VisibleForTesting
  long getIncrementalCost(String interfaceName, boolean stub) {
    assert _c.getAllInterfaces().get(interfaceName) != null;
    Interface iface = _c.getAllInterfaces().get(interfaceName);
    return getIncrementalCost(iface, stub);
  }

  /**
   * Return the interface cost for a given interface (used as increment to route metric).
   *
   * @param iface the {@link Interface} to for which to compute the cost
   * @param considerP2PasStub whether or not to consider point-to-point links as stub networks.
   *     <strong>Do not confuse this with stub areas</strong>. Stub here means a non-transit link.
   *     If point-to-point links needs to be considered as stub links (which can happen during
   *     initialization), this can affect cost computation if {@link
   *     OspfProcess#getMaxMetricStubNetworks()} is set.
   */
  private long getIncrementalCost(Interface iface, boolean considerP2PasStub) {
    long cost = iface.getOspfCost();
    if (iface.getOspfPassive() || (considerP2PasStub && iface.getOspfPointToPoint())) {
      cost = firstNonNull(_process.getMaxMetricStubNetworks(), cost);
    } else {
      cost = firstNonNull(_process.getMaxMetricTransitLinks(), cost);
    }
    return cost;
  }

  /**
   * Examine and merge all incoming inter-area route advertisements
   *
   * @return the resulting inter-area RIB delta builder.
   */
  @Nonnull
  private Builder<OspfInterAreaRoute> processInterAreaRoutes() {
    Builder<OspfInterAreaRoute> interAreaDelta = RibDelta.builder();
    _interAreaIncomingRoutes.forEach(
        (edgeId, queue) -> {
          long incrementalCost = getIncrementalCost(edgeId.getHead().getInterfaceName(), false);
          while (queue.peek() != null) {
            RouteAdvertisement<OspfInterAreaRoute> routeAdvertisement = queue.remove();
            transformInterAreaRouteOnImport(routeAdvertisement, incrementalCost)
                .ifPresent(r -> interAreaDelta.from(processRouteAdvertisement(r, _interAreaRib)));
          }
        });
    return interAreaDelta;
  }

  /**
   * Filter and transform inter-area route advertisement on import.
   *
   * @param routeAdvertisement the {@link RouteAdvertisement} to transform
   * @param incrementalCost incremental cost of the OSPF link to be added to the route.
   * @return {@link Optional#empty()} if the route should be filtered out, otherwise a {@link
   *     RouteAdvertisement} containing the transformed route.
   */
  @Nonnull
  @VisibleForTesting
  Optional<RouteAdvertisement<OspfInterAreaRoute>> transformInterAreaRouteOnImport(
      RouteAdvertisement<OspfInterAreaRoute> routeAdvertisement, long incrementalCost) {
    OspfInterAreaRoute route = routeAdvertisement.getRoute();
    if (isABR() && route.getNetwork().equals(Prefix.ZERO)) {
      // ABR should not accept default inter-area routes, as it is the one that originates them
      return Optional.empty();
    }
    // Transform the route
    routeAdvertisement =
        routeAdvertisement
            .toBuilder()
            .setRoute(
                route
                    .toBuilder()
                    .setMetric(route.getMetric() + incrementalCost)
                    .setAdmin(_process.getAdminCosts().get(route.getProtocol()))
                    // Clear any non-routing or non-forwarding bit
                    .setNonRouting(false)
                    .setNonRouting(false)
                    .build())
            .build();
    return Optional.of(routeAdvertisement);
  }

  /**
   * Examine and merge all incoming intra-area route advertisements
   *
   * @return the resulting intra-area RIB delta builder.
   */
  @Nonnull
  private RibDelta<OspfIntraAreaRoute> processIntraAreaRoutes() {
    Builder<OspfIntraAreaRoute> intraAreaDelta = RibDelta.builder();
    _intraAreaIncomingRoutes.forEach(
        (edgeId, queue) -> {
          long incrementalCost = getIncrementalCost(edgeId.getHead().getInterfaceName(), false);
          while (queue.peek() != null) {
            RouteAdvertisement<OspfIntraAreaRoute> routeAdvertisement = queue.remove();
            intraAreaDelta.from(
                processRouteAdvertisement(
                    transformIntraAreaRouteOnImport(routeAdvertisement, incrementalCost),
                    _intraAreaRib));
          }
        });
    return intraAreaDelta.build();
  }

  /**
   * Transform intra-area route advertisement on import.
   *
   * @param routeAdvertisement the {@link RouteAdvertisement} to transform
   * @param incrementalCost incremental cost of the OSPF link to be added to the route.
   * @return A {@link RouteAdvertisement} containing the transformed route.
   */
  @Nonnull
  @VisibleForTesting
  RouteAdvertisement<OspfIntraAreaRoute> transformIntraAreaRouteOnImport(
      RouteAdvertisement<OspfIntraAreaRoute> routeAdvertisement, long incrementalCost) {
    OspfIntraAreaRoute route = routeAdvertisement.getRoute();
    // Transform the route
    routeAdvertisement =
        routeAdvertisement
            .toBuilder()
            .setRoute(
                route
                    .toBuilder()
                    .setMetric(route.getMetric() + incrementalCost)
                    .setAdmin(_process.getAdminCosts().get(RoutingProtocol.OSPF))
                    .build())
            .build();
    return routeAdvertisement;
  }

  /**
   * Compute inter-area summaries. Only applies to an ABR. Will return a {@link RibDelta} for all
   * new inter-area summaries to advertise, or empty delta if not an ABR.
   */
  @Nonnull
  private RibDelta<OspfInterAreaRoute> computeInterAreaSummaries() {
    if (!isABR()) {
      return RibDelta.empty();
    }
    RibDelta.Builder<OspfInterAreaRoute> deltaBuilder = RibDelta.builder();
    _process.getAreas().values().stream()
        .map(this::computeInterAreaSummariesForArea)
        .forEach(deltaBuilder::from);
    return deltaBuilder.build();
  }

  /** Compute inter-area summaries for a single area. */
  @Nonnull
  private RibDelta<OspfInterAreaRoute> computeInterAreaSummariesForArea(OspfArea area) {
    Builder<OspfInterAreaRoute> deltaBuilder = RibDelta.builder();
    area.getSummaries()
        .forEach(
            (prefix, summary) ->
                computeSummaryRoute(prefix, summary, area.getAreaNumber())
                    .ifPresent(r -> deltaBuilder.from(_interAreaRib.mergeRouteGetDelta(r))));
    return deltaBuilder.build();
  }

  /**
   * Compute a summary route for a given area and prefix.
   *
   * <p><emph>Note:</emph> The resulting route will be marked non-routing (see {@link
   * AbstractRoute#getNonRouting})
   *
   * @param prefix The prefix for which to create a summary route
   * @param summary The {@link OspfAreaSummary summary configuration} (e.g., whether or not to
   *     advertise)
   * @param areaNumber area number for which to generate the route.r
   * @return {@link Optional#empty()} if the summary is not supposed to be advertised or there are
   *     no contributing routes, otherwise an optional containing a new inter-area route
   */
  @Nonnull
  private Optional<OspfInterAreaRoute> computeSummaryRoute(
      Prefix prefix, OspfAreaSummary summary, long areaNumber) {
    if (!summary.getAdvertised()) {
      return Optional.empty();
    }

    Stream<Long> contributingMetrics =
        Stream.concat(
                _intraAreaRib.getTypedRoutes().stream(), _interAreaRib.getTypedRoutes().stream())
            .filter(
                /*
                 * Only routes in the same area and within the summary prefix can
                 * contribute to creation of summary route
                 */
                candidateContributor ->
                    candidateContributor.getArea() == areaNumber
                        && prefix.containsPrefix(candidateContributor.getNetwork()))
            .map(OspfRoute::getMetric);

    // Use the metric as a proxy value to determine if there are any contributing values (e.g.,
    // stream empty or not)
    Long computedMetric =
        _useMinMetricForSummaries
            ? contributingMetrics.min(Comparator.naturalOrder()).orElse(null)
            : contributingMetrics.max(Comparator.naturalOrder()).orElse(null);
    if (computedMetric == null) {
      return Optional.empty();
    }

    OspfInterAreaRoute summaryRoute =
        OspfInterAreaRoute.builder()
            .setNetwork(prefix)
            .setNextHopIp(Ip.ZERO)
            // TODO: update to be truly vendor-independent
            .setAdmin(
                RoutingProtocol.OSPF_IA.getSummaryAdministrativeCost(_c.getConfigurationFormat()))
            .setMetric(firstNonNull(summary.getMetric(), computedMetric))
            .setArea(areaNumber)
            // Note the non-routing bit: must not go into our own main RIB
            .setNonRouting(true)
            .build();
    return Optional.of(summaryRoute);
  }

  /** Send out updates of intra- and inter-area routes to all neighbors */
  private void sendOutInternalRoutes(
      InternalDelta delta, Map<String, Node> allNodes, OspfTopology topology) {
    _intraAreaIncomingRoutes
        .keySet()
        .forEach(edgeId -> sendOutInternalRoutesPerEdge(delta, allNodes, topology, edgeId));
  }

  /**
   * Send out updates of intra- and inter-area routes to a single neighbor
   *
   * @param delta the {@link InternalDelta} captures the updates
   * @param allNodes all nodes participating in DP computation
   * @param topology the current OSPF topology
   * @param edgeId the edge identifying neighbor relationship
   */
  private void sendOutInternalRoutesPerEdge(
      InternalDelta delta, Map<String, Node> allNodes, OspfTopology topology, EdgeId edgeId) {
    OspfRoutingProcess remoteProcess = getNeighborProcess(edgeId.getTail(), allNodes);
    assert remoteProcess != null; // Otherwise the edge should not have been established

    Optional<OspfSessionProperties> session = topology.getSession(edgeId);
    assert session.isPresent(); // Otherwise the edge should not have been established

    OspfArea areaConfig = _process.getAreas().get(session.get().getArea());
    sendOutIntraAreaRoutesPerEdge(
        delta._intraArea, edgeId, remoteProcess, areaConfig, session.get());
    if (isABR()) {
      sendOutInterAreaRoutesPerEdgeABR(delta, edgeId, remoteProcess, areaConfig, session.get());
    } else {
      sendOutInterAreaRoutesPerEdgeNonABR(
          delta._interArea, edgeId, remoteProcess, areaConfig, session.get());
    }
  }

  /** Send out intra-area routes to a given neighbor */
  private void sendOutIntraAreaRoutesPerEdge(
      RibDelta<OspfIntraAreaRoute> delta,
      EdgeId edgeId,
      OspfRoutingProcess remoteProcess,
      OspfArea areaConfig,
      OspfSessionProperties session) {
    /*
     * For intra-area routes, send the route to all neighbors in the same area as
     * the route's area
     */
    remoteProcess.enqueueMessagesIntra(
        edgeId.reverse(),
        delta.getActions().stream()
            .filter(r -> r.getRoute().getArea() == areaConfig.getAreaNumber())
            .map(
                r ->
                    r.toBuilder()
                        .setRoute(
                            r.getRoute()
                                .toBuilder()
                                .setMetric(r.getRoute().getMetric())
                                .setNextHopIp(session.getIpLink().getIp2())
                                .build())
                        .build())
            .collect(ImmutableList.toImmutableList()));
  }

  /** Send out inter-area routes from a regular (non-ABR) router to a neighbor */
  private void sendOutInterAreaRoutesPerEdgeNonABR(
      RibDelta<OspfInterAreaRoute> delta,
      EdgeId edgeId,
      OspfRoutingProcess remoteProcess,
      OspfArea areaConfig,
      OspfSessionProperties sessionProperties) {
    /*
     * A regular (non-ABR) router can continue re-advertising
     * inter-area routes for area X in area X.
     */
    Ip nextHopIp = sessionProperties.getIpLink().getIp2();
    Collection<RouteAdvertisement<OspfInterAreaRoute>> updatedDelta =
        delta.getActions().stream()
            .filter(r -> r.getRoute().getArea() == areaConfig.getAreaNumber())
            .map(
                r ->
                    r.toBuilder()
                        .setRoute(
                            r.getRoute()
                                .toBuilder()
                                .setMetric(
                                    firstNonNull(
                                        _process.getMaxMetricTransitLinks(),
                                        r.getRoute().getMetric()))
                                .setNextHopIp(nextHopIp)
                                .build())
                        .build())
            .collect(ImmutableList.toImmutableList());
    remoteProcess.enqueueMessagesInter(edgeId.reverse(), updatedDelta);
  }

  /** Send out inter-area routes from an ABR to a neighbor */
  private void sendOutInterAreaRoutesPerEdgeABR(
      InternalDelta delta,
      EdgeId edgeId,
      OspfRoutingProcess remoteProcess,
      OspfArea areaConfig,
      OspfSessionProperties sessionProperties) {
    /*
     The area border router (ABR) can:
     - convert intra-area routes from area X into inter-area routes and send them
       to neighbors in different areas (Y, Z, ...)
     - re-advertise inter-area routes of area X into other areas Y, Z, ...
     - Inject default routes into stub areas

     The ABR must NOT:
     - Send inter-area routes to totally stubby areas
     - Send routes that are covered by a summary (this is represented using a route
       filter list in batfish)
    */

    RouteFilterList filterList = null;
    if (areaConfig.getSummaryFilter() != null) {
      filterList = _c.getRouteFilterLists().get(areaConfig.getSummaryFilter());
    }
    // Note: localIp == ip2
    Ip nextHopIp = sessionProperties.getIpLink().getIp2();

    remoteProcess.enqueueMessagesInter(
        edgeId.reverse(),
        Streams.concat(
                convertAndFilterIntraAreaRoutesToPropagate(
                    delta._intraArea,
                    areaConfig,
                    filterList,
                    nextHopIp,
                    _process.getMaxMetricSummaryNetworks()),
                filterInterAreaRoutesToPropagate(
                    delta._interArea,
                    areaConfig,
                    filterList,
                    nextHopIp,
                    _process.getMaxMetricSummaryNetworks()),
                computeDefaultInterAreaRouteToInject(areaConfig, nextHopIp)
                    .map(Stream::of)
                    .orElse(Stream.empty()))
            .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Return the routes that can be propagated from an ABR to a different OSPF neighbor, based on the
   * STUB settings for the area and the filter list defined for that area. May return an empty
   * stream if no routes need to be sent.
   *
   * @param delta ABR's inter- or intra- RIB delta
   * @param areaConfig area configuration at the ABR for this neighbor adjacency
   * @param filterList route filter list defined at the ABR (to enable correct summarization)
   * @param nextHopIp next hop ip to use when creating the route.
   * @param customMetric if provided (i.e., not {@code null}) it will be used instead of the routes
   *     original metric
   */
  @Nonnull
  @VisibleForTesting
  static Stream<RouteAdvertisement<OspfInterAreaRoute>> filterInterAreaRoutesToPropagate(
      RibDelta<OspfInterAreaRoute> delta,
      OspfArea areaConfig,
      @Nullable RouteFilterList filterList,
      Ip nextHopIp,
      @Nullable Long customMetric) {
    if (areaConfig.getStubType() == StubType.STUB && areaConfig.getStub().getSuppressType3()
        || areaConfig.getStubType() == StubType.NSSA && areaConfig.getNssa().getSuppressType3()) {
      // Nothing to do for totally stubby areas, where summaries are suppressed
      return Stream.empty();
    }
    return delta.getActions().stream()
        // Only propagate routes from different areas
        .filter(r -> r.getRoute().getArea() != areaConfig.getAreaNumber())
        // Only propagate routes permitted by the filter list
        // Fail open. Treat missing filter list as "allow all".
        .filter(r -> filterList == null || filterList.permits(r.getRoute().getNetwork()))
        // Overwrite area on the route before sending it out
        .map(
            r ->
                r.toBuilder()
                    .setRoute(
                        r.getRoute()
                            .toBuilder()
                            .setArea(areaConfig.getAreaNumber())
                            .setMetric(firstNonNull(customMetric, r.getRoute().getMetric()))
                            .setNextHopIp(nextHopIp)
                            .build())
                    .build());
  }

  /**
   * Convert intra-area routes to inter-area routes. Only routes that pass filtering conditions are
   * returned.
   */
  @Nonnull
  @VisibleForTesting
  static Stream<RouteAdvertisement<OspfInterAreaRoute>> convertAndFilterIntraAreaRoutesToPropagate(
      RibDelta<OspfIntraAreaRoute> delta,
      OspfArea areaConfig,
      @Nullable RouteFilterList filterList,
      Ip nextHopIp,
      @Nullable Long customMetric) {
    if (areaConfig.getStubType() == StubType.STUB && areaConfig.getStub().getSuppressType3()
        || areaConfig.getStubType() == StubType.NSSA && areaConfig.getNssa().getSuppressType3()) {
      // Nothing to do for totally stubby areas, where summaries are suppressed
      return Stream.empty();
    }
    return delta.getActions().stream()
        // Only propagate routes from different areas
        .filter(r -> r.getRoute().getArea() != areaConfig.getAreaNumber())
        // Only propagate routes permitted by the filter list
        // Fail open. Treat missing filter list as "allow all".
        .filter(r -> filterList == null || filterList.permits(r.getRoute().getNetwork()))
        // Overwrite area on the route before sending it out
        .map(
            r ->
                RouteAdvertisement.<OspfInterAreaRoute>builder()
                    .setRoute(
                        OspfInterAreaRoute.builder(r.getRoute())
                            .setArea(areaConfig.getAreaNumber())
                            .setMetric(firstNonNull(customMetric, r.getRoute().getMetric()))
                            .setNextHopIp(nextHopIp)
                            .build())
                    .setReason(r.getReason())
                    .build());
  }

  /**
   * Return a default route if one needs to be injected from an ABR to a neighbor.
   *
   * @param areaConfig {@link OspfArea} for which the ABR should consider injecting a route into
   * @param nextHopIp the next hop IP to use for the generated route
   * @return a stream containing the default route. May be empty if there is no route to inject
   */
  @Nonnull
  @VisibleForTesting
  static Optional<RouteAdvertisement<OspfInterAreaRoute>> computeDefaultInterAreaRouteToInject(
      OspfArea areaConfig, Ip nextHopIp) {
    return areaConfig.getStubType() == StubType.STUB
            || (areaConfig.getStubType() == StubType.NSSA
                && areaConfig.getNssa().getDefaultOriginateType()
                    == OspfDefaultOriginateType.INTER_AREA)
        ? Optional.of(
            RouteAdvertisement.<OspfInterAreaRoute>builder()
                .setReason(Reason.ADD)
                .setRoute(
                    OspfInterAreaRoute.builder()
                        .setNetwork(Prefix.ZERO)
                        .setNextHopIp(nextHopIp)
                        // Intentionally large. Must be correctly set on the receiver's side
                        .setAdmin(Integer.MAX_VALUE)
                        .setMetric(areaConfig.getMetricOfDefaultRoute())
                        .setArea(areaConfig.getAreaNumber())
                        .build())
                .build())
        : Optional.empty();
  }

  /**
   * Tell this process that a collection of intra-area route advertisements is coming in on a given
   * edge.
   *
   * @param edge {@link EdgeId} as with edge head pointing at {@code this} process
   * @param routes collection of route advertisements
   */
  @VisibleForTesting
  void enqueueMessagesIntra(
      EdgeId edge, Collection<RouteAdvertisement<OspfIntraAreaRoute>> routes) {
    assert _intraAreaIncomingRoutes.keySet().contains(edge);
    _intraAreaIncomingRoutes.get(edge).addAll(routes);
  }

  /**
   * Tell this process that a collection of inter-area route advertisements is coming in on a given
   * edge
   *
   * @param edge {@link EdgeId} as with edge head pointing at {@code this} process
   * @param routes collection of route advertisements
   */
  @VisibleForTesting
  void enqueueMessagesInter(
      EdgeId edge, Collection<RouteAdvertisement<OspfInterAreaRoute>> routes) {
    assert _interAreaIncomingRoutes.keySet().contains(edge);
    _interAreaIncomingRoutes.get(edge).addAll(routes);
  }

  /** Wrapper around intra- and inter-area RIB deltas */
  private static final class InternalDelta {
    @Nonnull private final RibDelta<OspfIntraAreaRoute> _intraArea;
    @Nonnull private final RibDelta<OspfInterAreaRoute> _interArea;

    InternalDelta(RibDelta<OspfIntraAreaRoute> intraArea, RibDelta<OspfInterAreaRoute> interArea) {
      _intraArea = intraArea;
      _interArea = interArea;
    }
  }
}
