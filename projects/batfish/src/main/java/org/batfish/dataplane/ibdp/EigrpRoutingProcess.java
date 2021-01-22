package org.batfish.dataplane.ibdp;

import static java.util.Objects.requireNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.common.util.CollectionUtil.toOrderedHashCode;
import static org.batfish.dataplane.rib.RibDelta.importRibDelta;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.graph.Network;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.EigrpInternalRoute;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpNeighborConfig;
import org.batfish.datamodel.eigrp.EigrpNeighborConfigId;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.dataplane.rib.EigrpExternalRib;
import org.batfish.dataplane.rib.EigrpInternalRib;
import org.batfish.dataplane.rib.EigrpRib;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/** An instance of an EigrpProcess as constructed and used by {@link VirtualRouter} */
@ParametersAreNonnullByDefault
final class EigrpRoutingProcess implements RoutingProcess<EigrpTopology, EigrpRoute> {

  /** Parent process containing configuration */
  @Nonnull private final EigrpProcess _process;
  /** All routing policies present at our parent node */
  @Nonnull private final RoutingPolicies _routingPolicies;
  /** Name of the VRF in which this process resides */
  @Nonnull private final String _vrfName;
  /** Our AS number */
  private final long _asn;

  // RIBs and RIB deltas

  /** Helper RIB containing EIGRP external paths */
  @Nonnull private final EigrpExternalRib _externalRib;
  /** Helper RIB containing all EIGRP paths internal to this router's ASN. */
  @Nonnull private final EigrpInternalRib _internalRib;
  /** Helper RIBs containing EIGRP internal and external paths. */
  @Nonnull private final EigrpRib _rib;
  /** A {@link RibDelta} indicating which internal routes we initialized */
  @Nonnull private RibDelta<EigrpInternalRoute> _initializationDelta;

  /**
   * A {@link RibDelta} containing delta of the main RIB which will get exported as external routes
   * and get withdrawn/sent in the next iteration
   */
  @Nonnull private RibDelta<EigrpExternalRoute> _queuedForRedistribution;

  /** Set of routes to be merged to the main RIB at the end of the iteration */
  @Nonnull private RibDelta.Builder<EigrpRoute> _changeSet;

  // Message queues

  /** Incoming internal route messages into this router from each EIGRP adjacency */
  @Nonnull
  private SortedMap<EigrpEdge, Queue<RouteAdvertisement<EigrpInternalRoute>>>
      _incomingInternalRoutes;
  /** Incoming external route messages into this router from each EIGRP adjacency */
  @Nonnull @VisibleForTesting
  SortedMap<EigrpEdge, Queue<RouteAdvertisement<EigrpExternalRoute>>> _incomingExternalRoutes;

  /** Current known EIGRP topology */
  @Nonnull private EigrpTopology _topology;
  /** Set of edges in the topology that are new in the current iteration */
  private Collection<EigrpEdge> _edgesWentUp = ImmutableSet.of();

  EigrpRoutingProcess(EigrpProcess process, String vrfName, RoutingPolicies policies) {
    _process = process;
    _asn = process.getAsn();
    _externalRib = new EigrpExternalRib();
    _internalRib = new EigrpInternalRib();
    _rib = new EigrpRib();
    _vrfName = vrfName;
    _routingPolicies = policies;
    _topology = EigrpTopology.EMPTY;
    _initializationDelta = RibDelta.empty();
    _queuedForRedistribution = RibDelta.empty();
    _incomingInternalRoutes = ImmutableSortedMap.of();
    _incomingExternalRoutes = ImmutableSortedMap.of();
    _changeSet = RibDelta.builder();
  }

  @Override
  public void initialize(Node n) {
    _initializationDelta = initInternalRoutes(_vrfName, n.getConfiguration());
  }

  @Override
  public void updateTopology(EigrpTopology topology) {
    EigrpTopology oldTopology = _topology;
    _topology = topology;
    updateQueues(_topology);

    _edgesWentUp =
        Sets.difference(
            getIncomingEdgeStream(topology).collect(ImmutableSet.toImmutableSet()),
            getIncomingEdgeStream(oldTopology).collect(ImmutableSet.toImmutableSet()));

    // TODO: compute edges that went down, remove routes we received from those neighbors
  }

  @Override
  public void executeIteration(Map<String, Node> allNodes) {
    _changeSet = RibDelta.builder();
    // TODO: optimize, don't recreate the map each iteration
    NetworkConfigurations nc =
        NetworkConfigurations.of(
            allNodes.entrySet().stream()
                .collect(
                    ImmutableMap.toImmutableMap(
                        Entry::getKey, e -> e.getValue().getConfiguration())));

    if (!_initializationDelta.isEmpty()) {
      // If we haven't sent out the first round of updates after initialization, do so now. Then
      // clear the initialization delta
      sendOutInternalRoutes(_initializationDelta, allNodes, nc);
      _initializationDelta = RibDelta.empty();
    }

    sendOutRoutesToNewEdges(_edgesWentUp, allNodes, nc);
    _edgesWentUp = ImmutableSet.of();

    // Process internal routes
    RibDelta<EigrpInternalRoute> internalDelta = processInternalRoutes(nc);
    sendOutInternalRoutes(internalDelta, allNodes, nc);

    // Filter and export redistribution queue according to per neighbor export policy and send
    // out/withdraw
    sendOutExternalRoutes(_queuedForRedistribution, allNodes, nc);
    _queuedForRedistribution = RibDelta.empty();

    // Process new external routes and re-advertise them as necessary
    RibDelta<EigrpExternalRoute> externalDelta = processExternalRoutes(nc);
    sendOutExternalRoutes(externalDelta, allNodes, nc);

    // Keep track of what what updates will go into the main RIB
    _changeSet.from(importRibDelta(_rib, internalDelta));
    _changeSet.from(importRibDelta(_rib, externalDelta));
  }

  @Nonnull
  @Override
  public RibDelta<EigrpRoute> getUpdatesForMainRib() {
    return _changeSet.build();
  }

  /**
   * Return exported routes in queueForRedistribution {@link RibDelta} using the provided
   * exportPolicy
   */
  private RibDelta<EigrpExternalRoute> redistributeWithPolicy(
      RibDelta<? extends AnnotatedRoute<AbstractRoute>> queueForRedistribution,
      RoutingPolicy exportPolicy) {
    RibDelta.Builder<EigrpExternalRoute> builder = RibDelta.builder();
    queueForRedistribution
        .getActions()
        .forEach(
            ra -> {
              EigrpExternalRoute outputRoute = computeEigrpExportRoute(exportPolicy, ra.getRoute());
              if (outputRoute == null) {
                return; // no need to export
              }
              // Do not use builder.from(_externalRib.merge/remove) here
              // The goal is to send out redistributed routes regardless
              // of whether they are new to our RIB as long as export policy allows them
              if (!ra.isWithdrawn()) {
                builder.add(outputRoute);
                _externalRib.mergeRouteGetDelta(outputRoute);
              } else {
                builder.remove(outputRoute, Reason.WITHDRAW);
                _externalRib.removeRouteGetDelta(outputRoute);
              }
            });
    return builder.build();
  }

  @Override
  public void redistribute(RibDelta<? extends AnnotatedRoute<AbstractRoute>> mainRibDelta) {
    if (_process.getRedistributionPolicy() == null) {
      return;
    }
    RoutingPolicy exportPolicy =
        _routingPolicies.get(_process.getRedistributionPolicy()).orElse(null);
    if (exportPolicy == null) {
      return;
    }
    _queuedForRedistribution = redistributeWithPolicy(mainRibDelta, exportPolicy);
  }

  @Override
  public boolean isDirty() {
    return !_incomingInternalRoutes.values().stream().allMatch(Queue::isEmpty)
        || !_incomingExternalRoutes.values().stream().allMatch(Queue::isEmpty)
        || !_changeSet.isEmpty()
        || !_queuedForRedistribution.isEmpty()
        || !_initializationDelta.isEmpty();
  }

  /**
   * Init internal routes from connected routes. For each interface prefix, construct a new internal
   * route.
   */
  private RibDelta<EigrpInternalRoute> initInternalRoutes(String vrfName, Configuration c) {
    Builder<EigrpInternalRoute> builder = RibDelta.builder();
    for (String ifaceName : c.getActiveInterfaces(vrfName).keySet()) {
      Interface iface = c.getAllInterfaces().get(ifaceName);
      assert iface.getActive();
      if (iface.getEigrp() == null
          || iface.getEigrp().getAsn() != _asn
          || !iface.getEigrp().getEnabled()) {
        continue;
      }
      requireNonNull(iface.getEigrp());
      Set<Prefix> allNetworkPrefixes =
          iface.getAllConcreteAddresses().stream()
              .map(ConcreteInterfaceAddress::getPrefix)
              .collect(Collectors.toSet());
      for (Prefix prefix : allNetworkPrefixes) {
        EigrpInternalRoute route =
            EigrpInternalRoute.builder()
                .setAdmin(
                    RoutingProtocol.EIGRP.getDefaultAdministrativeCost(c.getConfigurationFormat()))
                .setEigrpMetric(iface.getEigrp().getMetric())
                .setEigrpMetricVersion(_process.getMetricVersion())
                .setNetwork(prefix)
                .setProcessAsn(_asn)
                .setNextHop(NextHopInterface.of(ifaceName))
                .build();
        builder.from(_internalRib.mergeRouteGetDelta(route));
      }
    }
    return builder.build();
  }

  @Nonnull
  private RibDelta<EigrpInternalRoute> processInternalRoutes(NetworkConfigurations nc) {
    // TODO: simplify all this later. Copied from old code
    Builder<EigrpInternalRoute> deltaBuilder = RibDelta.builder();
    _incomingInternalRoutes.forEach(
        (edge, queue) -> processInternalRoutesFromNeighbor(nc, deltaBuilder, edge, queue));
    return deltaBuilder.build();
  }

  private void processInternalRoutesFromNeighbor(
      NetworkConfigurations nc,
      Builder<EigrpInternalRoute> deltaBuilder,
      EigrpEdge edge,
      Queue<RouteAdvertisement<EigrpInternalRoute>> queue) {
    Interface remoteIface = edge.getNode1().getInterface(nc);
    assert remoteIface.getEigrp() != null;
    Ip nextHopIp = remoteIface.getConcreteAddress().getIp();
    EigrpInterfaceSettings localEigrpIface = edge.getNode2().getInterfaceSettings(nc);
    EigrpMetric connectingInterfaceMetric = localEigrpIface.getMetric();
    @Nullable
    RoutingPolicy importPolicy =
        Optional.ofNullable(localEigrpIface.getImportPolicy())
            .flatMap(_routingPolicies::get)
            .orElse(null);
    while (!queue.isEmpty()) {
      RouteAdvertisement<EigrpInternalRoute> ra = queue.remove();
      EigrpInternalRoute route = ra.getRoute();
      Optional<EigrpInternalRoute> transformedRoute =
          transformAndFilterInternalRouteFromNeighbor(
              route, connectingInterfaceMetric, nextHopIp, importPolicy);
      if (!transformedRoute.isPresent()) {
        continue;
      }
      if (!ra.isWithdrawn()) {
        deltaBuilder.from(_internalRib.mergeRouteGetDelta(transformedRoute.get()));
      } else {
        deltaBuilder.from(_internalRib.removeRouteGetDelta(transformedRoute.get()));
      }
    }
  }

  @VisibleForTesting
  Optional<EigrpInternalRoute> transformAndFilterInternalRouteFromNeighbor(
      EigrpInternalRoute route,
      EigrpMetric connectingInterfaceMetric,
      Ip nextHopIp,
      @Nullable RoutingPolicy importPolicy) {

    EigrpMetric newMetric = connectingInterfaceMetric.add(route.getEigrpMetric());
    EigrpInternalRoute.Builder outputRouteBuilder =
        EigrpInternalRoute.builder()
            .setAdmin(_process.getInternalAdminCost())
            .setEigrpMetric(newMetric)
            .setEigrpMetricVersion(_process.getMetricVersion())
            .setNetwork(route.getNetwork())
            .setNextHop(NextHopIp.of(nextHopIp))
            .setProcessAsn(_asn);
    return filterRouteOnImport(route, outputRouteBuilder, importPolicy);
  }

  /** Run an EIGRP route though the given import policy */
  @VisibleForTesting
  <B extends EigrpRoute.Builder<B, R>, R extends EigrpRoute> Optional<R> filterRouteOnImport(
      R route, B outputBuilder, @Nullable RoutingPolicy policy) {
    if (policy == null) {
      return Optional.of(outputBuilder.build());
    }
    boolean allowed = policy.process(route, outputBuilder, _process, Direction.IN);
    return allowed ? Optional.of(outputBuilder.build()) : Optional.empty();
  }

  @Nonnull
  private RibDelta<EigrpExternalRoute> processExternalRoutes(NetworkConfigurations nc) {
    RibDelta.Builder<EigrpExternalRoute> deltaBuilder = RibDelta.builder();
    _incomingExternalRoutes.forEach(
        (edge, queue) -> processExternalRoutesFromNeighbor(nc, deltaBuilder, edge, queue));
    return deltaBuilder.build();
  }

  private void processExternalRoutesFromNeighbor(
      NetworkConfigurations nc,
      Builder<EigrpExternalRoute> deltaBuilder,
      EigrpEdge edge,
      Queue<RouteAdvertisement<EigrpExternalRoute>> queue) {
    Interface remoteIface = edge.getNode1().getInterface(nc);
    assert remoteIface.getEigrp() != null;
    Interface localIface = edge.getNode2().getInterface(nc);
    assert localIface.getEigrp() != null;

    while (queue.peek() != null) {
      RouteAdvertisement<EigrpExternalRoute> routeAdvert = queue.remove();
      EigrpExternalRoute neighborRoute = routeAdvert.getRoute();

      @Nullable
      RoutingPolicy importPolicy =
          Optional.ofNullable(localIface.getEigrp().getImportPolicy())
              .flatMap(_routingPolicies::get)
              .orElse(null);
      Optional<EigrpExternalRoute> transformedRoute =
          transformAndFilterExternalRouteFromNeighbor(
              neighborRoute,
              localIface.getEigrp().getMetric(),
              remoteIface.getConcreteAddress().getIp(),
              importPolicy);
      if (!transformedRoute.isPresent()) {
        continue;
      }
      if (!routeAdvert.isWithdrawn()) {
        deltaBuilder.from(_externalRib.mergeRouteGetDelta(transformedRoute.get()));
      } else {
        deltaBuilder.from(_externalRib.removeRouteGetDelta(transformedRoute.get()));
      }
    }
  }

  @Nonnull
  @VisibleForTesting
  Optional<EigrpExternalRoute> transformAndFilterExternalRouteFromNeighbor(
      EigrpExternalRoute route,
      EigrpMetric connectingIntfMetric,
      Ip nextHopIp,
      @Nullable RoutingPolicy importPolicy) {
    EigrpMetric newMetric = connectingIntfMetric.add(route.getEigrpMetric());

    EigrpExternalRoute.Builder routeBuilder =
        EigrpExternalRoute.builder()
            .setAdmin(_process.getExternalAdminCost())
            .setProcessAsn(_asn)
            .setNextHop(NextHopIp.of(nextHopIp))
            .setDestinationAsn(route.getDestinationAsn())
            .setEigrpMetric(newMetric)
            .setEigrpMetricVersion(_process.getMetricVersion())
            .setNetwork(route.getNetwork())
            .setTag(route.getTag());
    return filterRouteOnImport(route, routeBuilder, importPolicy);
  }

  /**
   * Send out internal routes to all neighbors.
   *
   * <p>Note: {@code routes} do not have to be filtered, this function filters them based on
   * per-neighbor export policy
   */
  private void sendOutInternalRoutes(
      RibDelta<EigrpInternalRoute> routes, Map<String, Node> allNodes, NetworkConfigurations nc) {
    for (EigrpEdge eigrpEdge : _incomingInternalRoutes.keySet()) {
      sendOutInternalRoutesPerNeighbor(routes, allNodes, eigrpEdge, nc);
    }
  }

  /**
   * Send out internal routes to a given neighbor.
   *
   * <p>Note: {@code routes} do not have to be filtered, this function filters them based on
   * per-neighbor export policy
   */
  private void sendOutInternalRoutesPerNeighbor(
      RibDelta<EigrpInternalRoute> routes,
      Map<String, Node> allNodes,
      EigrpEdge eigrpEdge,
      NetworkConfigurations nc) {
    EigrpRoutingProcess neighborProc =
        getNeighborEigrpProcess(allNodes, eigrpEdge, _asn); // TODO: cleanup, this logic is ugly
    Ip neighborIp = eigrpEdge.getNode1().getInterface(nc).getConcreteAddress().getIp();
    neighborProc.enqueueInternalMessages(
        eigrpEdge.reverse(),
        routes
            .getActions()
            .filter(ra -> allowedByExportPolicy(eigrpEdge.getNode2(), ra.getRoute()))
            // Approximate split horizon: don't send the route to a neighbor if the neighbor is the
            // next hop IP for the route.
            .filter(ra -> !ra.getRoute().getNextHopIp().equals(neighborIp)));
  }

  /**
   * Send out internal routes to all neighbors.
   *
   * <p>Note: {@code routes} do not have to be filtered, this function filters them based on
   * per-neighbor export policy
   */
  private void sendOutExternalRoutes(
      RibDelta<EigrpExternalRoute> routes, Map<String, Node> allNodes, NetworkConfigurations nc) {
    for (EigrpEdge eigrpEdge : _incomingExternalRoutes.keySet()) {
      sendOutExternalRoutesPerNeighbor(
          filterExternalRoutes(routes, eigrpEdge), allNodes, eigrpEdge, nc);
    }
  }

  /**
   * Send out external route advertisements to a single neighbor.
   *
   * <p><em>Note</em>: {@code routes} must already be properly transformed and must have gone
   * through routing policy.
   */
  private void sendOutExternalRoutesPerNeighbor(
      RibDelta<EigrpExternalRoute> routes,
      Map<String, Node> allNodes,
      EigrpEdge eigrpEdge,
      NetworkConfigurations nc) {
    EigrpRoutingProcess neighborProc = getNeighborEigrpProcess(allNodes, eigrpEdge, _asn);
    // TODO: cleanup, this logic is ugly
    Ip neighborIp = eigrpEdge.getNode1().getInterface(nc).getConcreteAddress().getIp();
    neighborProc.enqueueExternalMessages(
        eigrpEdge.reverse(),
        routes
            .getActions()
            // Approximate split horizon: don't send the route to a neighbor if the neighbor is the
            // next hop IP for the route.
            .filter(ra -> !ra.getRoute().getNextHopIp().equals(neighborIp)));
  }

  /** Filter (and transform) a RibDelta of external routes to ones allowed by export policy */
  @Nonnull
  private RibDelta<EigrpExternalRoute> filterExternalRoutes(
      RibDelta<EigrpExternalRoute> routes, EigrpEdge eigrpEdge) {
    // TODO: this is likely inefficient. optimize
    return RibDelta.<EigrpExternalRoute>builder()
        .from(
            routes
                .getActions()
                .map(
                    ra -> {
                      Optional<EigrpExternalRoute> transformExternalRoute =
                          filterAndTransformExternalRoute(eigrpEdge.getNode2(), ra.getRoute());
                      return transformExternalRoute
                          .map(
                              eigrpExternalRoute ->
                                  ra.toBuilder().setRoute(eigrpExternalRoute).build())
                          .orElse(null);
                    })
                .filter(Objects::nonNull))
        .build();
  }

  private void sendOutRoutesToNewEdges(
      Collection<EigrpEdge> edgesWentUp, Map<String, Node> allNodes, NetworkConfigurations nc) {
    for (EigrpEdge edge : edgesWentUp) {
      sendOutInternalRoutesPerNeighbor(
          RibDelta.<EigrpInternalRoute>builder().add(_internalRib.getTypedRoutes()).build(),
          allNodes,
          edge,
          nc);
      sendOutExternalRoutesPerNeighbor(
          filterExternalRoutes(
              RibDelta.<EigrpExternalRoute>builder().add(_externalRib.getTypedRoutes()).build(),
              edge),
          allNodes,
          edge,
          nc);
    }
  }

  /** Checks if a given {@link EigrpRoute} is allowed to be sent out to a given neighbor */
  private boolean allowedByExportPolicy(
      EigrpNeighborConfigId neighborConfigId, EigrpRoute eigrpRoute) {
    RoutingPolicy exportPolicy = getOwnExportPolicy(neighborConfigId);
    return exportPolicy.process(eigrpRoute, eigrpRoute.toBuilder(), _process, Direction.OUT);
  }

  /**
   * Execute neighbor-specific filtering/transformation for external routes. Returns an {@link
   * Optional#empty()} if the route should not be sent to the remote neighbor.
   */
  private Optional<EigrpExternalRoute> filterAndTransformExternalRoute(
      EigrpNeighborConfigId neighborConfigId, EigrpExternalRoute route) {
    RoutingPolicy exportPolicy = getOwnExportPolicy(neighborConfigId);
    EigrpExternalRoute.Builder builder = route.toBuilder();
    boolean allowed = exportPolicy.process(route, builder, _process, Direction.OUT);
    return allowed ? Optional.of(builder.build()) : Optional.empty();
  }

  /**
   * Gets the {@link RoutingPolicy} used by a given {@link EigrpNeighborConfigId neighborConfigId}
   */
  @Nonnull
  private RoutingPolicy getOwnExportPolicy(EigrpNeighborConfigId neighborConfigId) {
    EigrpNeighborConfig neighborConfig =
        _process.getNeighbors().get(neighborConfigId.getInterfaceName());
    assert neighborConfig != null;
    return _routingPolicies.getOrThrow(neighborConfig.getExportPolicy());
  }

  /**
   * Computes an exportable EIGRP route from export policy and existing potential route for export
   *
   * @param potentialExportRoute Route to consider exporting
   * @return The computed export route or null if the export policy denies the route
   */
  @Nullable
  private EigrpExternalRoute computeEigrpExportRoute(
      RoutingPolicy exportPolicy, AnnotatedRoute<AbstractRoute> potentialExportRoute) {
    AbstractRoute unannotatedPotentialRoute = potentialExportRoute.getRoute();
    EigrpExternalRoute.Builder outputRouteBuilder =
        EigrpExternalRoute.builder().setEigrpMetricVersion(_process.getMetricVersion());
    // Set the metric to match the route metric by default for EIGRP into EIGRP
    if (unannotatedPotentialRoute instanceof EigrpRoute) {
      outputRouteBuilder.setEigrpMetric(((EigrpRoute) unannotatedPotentialRoute).getEigrpMetric());
    }

    if (!exportPolicy.process(potentialExportRoute, outputRouteBuilder, _process, Direction.OUT)) {
      return null;
    }
    outputRouteBuilder.setAdmin(_process.getExternalAdminCost());
    if (unannotatedPotentialRoute instanceof EigrpExternalRoute) {
      EigrpExternalRoute externalRoute = (EigrpExternalRoute) unannotatedPotentialRoute;
      outputRouteBuilder.setDestinationAsn(externalRoute.getDestinationAsn());
    } else {
      outputRouteBuilder.setDestinationAsn(_asn);
    }
    outputRouteBuilder.setNetwork(unannotatedPotentialRoute.getNetwork());
    outputRouteBuilder.setProcessAsn(_asn);
    outputRouteBuilder.setNonRouting(true);
    outputRouteBuilder.setNextHop(unannotatedPotentialRoute.getNextHop());
    return outputRouteBuilder.build();
  }

  /**
   * Compute the "hashcode" of this router for the iBDP purposes. The hashcode is computed from the
   * following data structures:
   *
   * <ul>
   *   <li>EIGRP Rib {@link #_rib}
   *   <li>message queues ({@link #_incomingExternalRoutes}, {@link #_incomingInternalRoutes})
   * </ul>
   *
   * @return integer hashcode
   */
  int computeIterationHashCode() {
    return Streams.concat(
            Stream.of(_rib),
            _incomingInternalRoutes.values().stream(),
            _incomingExternalRoutes.values().stream())
        .collect(toOrderedHashCode());
  }

  /** Return the AS number of this process */
  long getAsn() {
    return _asn;
  }

  /**
   * Initialize incoming EIGRP message queues for each adjacency
   *
   * @param eigrpTopology The topology representing EIGRP adjacencies
   */
  private void updateQueues(EigrpTopology eigrpTopology) {
    _incomingExternalRoutes =
        getIncomingEdgeStream(eigrpTopology)
            .collect(toImmutableSortedMap(Function.identity(), e -> new ConcurrentLinkedQueue<>()));
    _incomingInternalRoutes =
        getIncomingEdgeStream(eigrpTopology)
            .collect(toImmutableSortedMap(Function.identity(), e -> new ConcurrentLinkedQueue<>()));
  }

  /** Returns all incoming edges as a stream */
  @Nonnull
  private Stream<EigrpEdge> getIncomingEdgeStream(EigrpTopology eigrpTopology) {
    Network<EigrpNeighborConfigId, EigrpEdge> graph = eigrpTopology.getNetwork();
    return _process.getNeighbors().values().stream()
        .map(EigrpNeighborConfig::getId)
        .filter(n -> graph.nodes().contains(n))
        .flatMap(head -> eigrpTopology.getNetwork().inEdges(head).stream());
  }

  /**
   * Get the neighboring EIGRP process correspoding to the tail node of {@code edge}
   *
   * @throws IllegalStateException if the EIGRP process cannot be found
   */
  @Nonnull
  private static EigrpRoutingProcess getNeighborEigrpProcess(
      Map<String, Node> allNodes, EigrpEdge edge, long asn) {
    return Optional.ofNullable(allNodes.get(edge.getNode1().getHostname()))
        .map(n -> n.getVirtualRouterOrThrow(edge.getNode1().getVrf()))
        .map(vr -> vr.getEigrpProcess(asn))
        .orElseThrow(
            () -> new IllegalStateException("Cannot find EigrpProcess for " + edge.getNode1()));
  }

  /**
   * Tell this process that a collection of internal route advertisements is coming in on a given
   * edge
   */
  private void enqueueInternalMessages(
      EigrpEdge edge, Stream<RouteAdvertisement<EigrpInternalRoute>> routes) {
    Queue<RouteAdvertisement<EigrpInternalRoute>> queue = _incomingInternalRoutes.get(edge);
    assert queue != null;
    routes.forEach(queue::add);
  }

  /**
   * Tell this process that a collection of external route advertisements is coming in on a given
   * edge
   */
  private void enqueueExternalMessages(
      EigrpEdge edge, Stream<RouteAdvertisement<EigrpExternalRoute>> routes) {
    Queue<RouteAdvertisement<EigrpExternalRoute>> queue = _incomingExternalRoutes.get(edge);
    assert queue != null;
    routes.forEach(queue::add);
  }
}
