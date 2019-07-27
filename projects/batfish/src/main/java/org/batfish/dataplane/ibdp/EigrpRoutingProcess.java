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
import com.google.common.graph.Network;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
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
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpNeighborConfig;
import org.batfish.datamodel.eigrp.EigrpNeighborConfigId;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpTopology;
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
  /** Configuration containing the process */
  @Nonnull private final Configuration _configuration;
  /** Name of the VRF in which this process resides */
  @Nonnull private final String _vrfName;
  /** Our AS number */
  private final long _asn;

  private final int _defaultExternalAdminCost;
  private final int _defaultInternalAdminCost;

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
  @Nonnull private RibDelta<? extends AnnotatedRoute<AbstractRoute>> _queuedForRedistribution;

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

  EigrpRoutingProcess(final EigrpProcess process, final String vrfName, final Configuration c) {
    _process = process;
    _asn = process.getAsn();
    _defaultExternalAdminCost =
        RoutingProtocol.EIGRP_EX.getDefaultAdministrativeCost(c.getConfigurationFormat());
    _defaultInternalAdminCost =
        RoutingProtocol.EIGRP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    _externalRib = new EigrpExternalRib();
    _internalRib = new EigrpInternalRib();
    _rib = new EigrpRib();
    _vrfName = vrfName;
    _configuration = c;

    // get EIGRP export policy name
    String exportPolicyName = process.getExportPolicy();
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
    if (!_initializationDelta.isEmpty()) {
      // If we haven't sent out the first round of updates after initialization, do so now. Then
      // clear the initialization delta
      sendOutInternalRoutes(_initializationDelta, allNodes);
      _initializationDelta = RibDelta.empty();
    }

    sendOutRoutesToNewEdges(_edgesWentUp, allNodes);
    _edgesWentUp = ImmutableSet.of();

    // TODO: optimize, don't recreate the map each iteration
    NetworkConfigurations nc =
        NetworkConfigurations.of(
            allNodes.entrySet().stream()
                .collect(
                    ImmutableMap.toImmutableMap(
                        Entry::getKey, e -> e.getValue().getConfiguration())));

    // Process internal routes
    RibDelta<EigrpInternalRoute> internalDelta = processInternalRoutes(nc);
    sendOutInternalRoutes(internalDelta, allNodes);

    // Filter and export redistribution queue according to per neighbor export policy and send
    // out/withdraw
    exportRedistributedForAllNeighbors(_queuedForRedistribution, allNodes);
    _queuedForRedistribution = RibDelta.empty();

    // Process new external routes and re-advertise them as necessary
    RibDelta<EigrpExternalRoute> externalDelta = processExternalRoutes(nc);
    sendOutExternalRoutes(externalDelta, allNodes);

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
   * Export routes in queueForRedistribution {@link RibDelta} to all neighbors using per neighbor
   * export policy
   */
  private void exportRedistributedForAllNeighbors(
      RibDelta<? extends AnnotatedRoute<AbstractRoute>> queueForRedistribution,
      Map<String, Node> allNodes) {
    for (EigrpEdge eigrpEdge : _incomingExternalRoutes.keySet()) {
      RoutingPolicy exportPolicyForEdge = getOwnExportPolicy(eigrpEdge.getNode2());
      RibDelta<EigrpExternalRoute> routesForExport =
          exportRedistriubtionDeltaForNeighbor(queueForRedistribution, exportPolicyForEdge);
      sendOutExternalRoutesPerNeighbor(routesForExport, allNodes, eigrpEdge);
    }
  }

  /**
   * Return exported routes in queueForRedistribution {@link RibDelta} using the provided
   * exportPolicy
   */
  private RibDelta<EigrpExternalRoute> exportRedistriubtionDeltaForNeighbor(
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
    _queuedForRedistribution = mainRibDelta;
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
    for (String ifaceName : c.getVrfs().get(vrfName).getInterfaceNames()) {
      Interface iface = c.getAllInterfaces().get(ifaceName);
      if (!iface.getActive()
          || iface.getEigrp() == null
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
                .setNetwork(prefix)
                .setProcessAsn(_asn)
                .build();
        builder.from(_internalRib.mergeRouteGetDelta(route));
      }
    }
    return builder.build();
  }

  @Nonnull
  private RibDelta<EigrpInternalRoute> processInternalRoutes(NetworkConfigurations nc) {
    // TODO: simplify all this later. Copied from old code
    Builder<EigrpInternalRoute> builder = RibDelta.builder();
    _incomingInternalRoutes.forEach(
        (edge, queue) -> {
          processInternalRoutesFromNeighbor(nc, builder, edge, queue);
        });
    return builder.build();
  }

  private void processInternalRoutesFromNeighbor(
      NetworkConfigurations nc,
      Builder<EigrpInternalRoute> builder,
      EigrpEdge edge,
      Queue<RouteAdvertisement<EigrpInternalRoute>> queue) {
    EigrpMetric connectingInterfaceMetric = edge.getNode2().getInterfaceSettings(nc).getMetric();
    Interface neighborInterface = edge.getNode1().getInterface(nc);
    Ip nextHopIp = neighborInterface.getConcreteAddress().getIp();
    while (!queue.isEmpty()) {
      RouteAdvertisement<EigrpInternalRoute> ra = queue.remove();
      EigrpInternalRoute route = ra.getRoute();
      EigrpMetric newMetric =
          connectingInterfaceMetric.accumulate(
              neighborInterface.getEigrp().getMetric(), route.getEigrpMetric());
      EigrpInternalRoute transformedRoute =
          EigrpInternalRoute.builder()
              .setAdmin(_defaultInternalAdminCost)
              .setEigrpMetric(newMetric)
              .setNetwork(route.getNetwork())
              .setNextHopIp(nextHopIp)
              .setProcessAsn(_asn)
              .build();
      if (ra.isWithdrawn()) {
        builder.from(_internalRib.removeRouteGetDelta(transformedRoute));
      } else {
        builder.from(_internalRib.mergeRouteGetDelta(transformedRoute));
      }
    }
  }

  @Nonnull
  private RibDelta<EigrpExternalRoute> processExternalRoutes(NetworkConfigurations nc) {
    // TODO: simplify all this later. Copied from old code
    RibDelta.Builder<EigrpExternalRoute> deltaBuilder = RibDelta.builder();
    EigrpExternalRoute.Builder routeBuilder = EigrpExternalRoute.builder();
    routeBuilder.setAdmin(_defaultExternalAdminCost).setProcessAsn(_asn);

    _incomingExternalRoutes.forEach(
        (edge, queue) -> {
          processExternalRoutesFromNeighbor(nc, deltaBuilder, routeBuilder, edge, queue);
        });

    return deltaBuilder.build();
  }

  private void processExternalRoutesFromNeighbor(
      NetworkConfigurations nc,
      Builder<EigrpExternalRoute> deltaBuilder,
      EigrpExternalRoute.Builder routeBuilder,
      EigrpEdge edge,
      Queue<RouteAdvertisement<EigrpExternalRoute>> queue) {
    Interface nextHopIntf = edge.getNode1().getInterface(nc);
    Interface connectingIntf = edge.getNode2().getInterface(nc);

    // Edge nodes must have EIGRP configuration
    if (nextHopIntf.getEigrp() == null || connectingIntf.getEigrp() == null) {
      return;
    }

    EigrpMetric nextHopIntfMetric = nextHopIntf.getEigrp().getMetric();
    EigrpMetric connectingIntfMetric = connectingIntf.getEigrp().getMetric();

    routeBuilder.setNextHopIp(nextHopIntf.getConcreteAddress().getIp());
    while (queue.peek() != null) {
      RouteAdvertisement<EigrpExternalRoute> routeAdvert = queue.remove();
      EigrpExternalRoute neighborRoute = routeAdvert.getRoute();
      EigrpMetric metric =
          connectingIntfMetric.accumulate(nextHopIntfMetric, neighborRoute.getEigrpMetric());
      routeBuilder
          .setDestinationAsn(neighborRoute.getDestinationAsn())
          .setEigrpMetric(metric)
          .setNetwork(neighborRoute.getNetwork());
      EigrpExternalRoute transformedRoute = routeBuilder.build();

      if (routeAdvert.isWithdrawn()) {
        deltaBuilder.from(_externalRib.removeRouteGetDelta(transformedRoute));
      } else {
        deltaBuilder.from(_externalRib.mergeRouteGetDelta(transformedRoute));
      }
    }
  }

  private void sendOutInternalRoutes(
      RibDelta<EigrpInternalRoute> initializationDelta, Map<String, Node> allNodes) {
    for (EigrpEdge eigrpEdge : _incomingInternalRoutes.keySet()) {
      sendOutInternalRoutesPerNeighbor(initializationDelta, allNodes, eigrpEdge);
    }
  }

  private void sendOutInternalRoutesPerNeighbor(
      RibDelta<EigrpInternalRoute> initializationDelta,
      Map<String, Node> allNodes,
      EigrpEdge eigrpEdge) {
    EigrpRoutingProcess neighborProc = getNeighborEigrpProcess(allNodes, eigrpEdge, _asn);
    neighborProc.enqueueInternalMessages(
        eigrpEdge.reverse(),
        initializationDelta
            .getActions()
            .filter(ra -> allowedByExportPolicy(eigrpEdge.getNode2(), ra.getRoute())));
  }

  private void sendOutExternalRoutes(
      RibDelta<EigrpExternalRoute> queuedForRedistribution, Map<String, Node> allNodes) {
    for (EigrpEdge eigrpEdge : _incomingExternalRoutes.keySet()) {
      sendOutExternalRoutesPerNeighbor(queuedForRedistribution, allNodes, eigrpEdge);
    }
  }

  private void sendOutExternalRoutesPerNeighbor(
      RibDelta<EigrpExternalRoute> queuedForRedistribution,
      Map<String, Node> allNodes,
      EigrpEdge eigrpEdge) {
    EigrpRoutingProcess neighborProc = getNeighborEigrpProcess(allNodes, eigrpEdge, _asn);
    neighborProc.enqueueExternalMessages(eigrpEdge.reverse(), queuedForRedistribution.getActions());
  }

  private void sendOutRoutesToNewEdges(
      Collection<EigrpEdge> edgesWentUp, Map<String, Node> allNodes) {
    for (EigrpEdge edge : edgesWentUp) {
      sendOutInternalRoutesPerNeighbor(
          RibDelta.<EigrpInternalRoute>builder().add(_internalRib.getTypedRoutes()).build(),
          allNodes,
          edge);
      sendOutExternalRoutesPerNeighbor(
          RibDelta.<EigrpExternalRoute>builder().add(_externalRib.getTypedRoutes()).build(),
          allNodes,
          edge);
    }
  }

  /** Checks if a given {@link EigrpRoute} is allowed to be sent out from a given neighbor */
  private boolean allowedByExportPolicy(
      EigrpNeighborConfigId neighborConfigId, EigrpRoute eigrpRoute) {
    RoutingPolicy exportPolicy = getOwnExportPolicy(neighborConfigId);
    return exportPolicy.process(eigrpRoute, eigrpRoute.toBuilder(), null, _vrfName, Direction.OUT);
  }

  /**
   * Gets the {@link RoutingPolicy} used by a given {@link EigrpNeighborConfigId neighborConfigId}
   */
  @Nonnull
  private RoutingPolicy getOwnExportPolicy(EigrpNeighborConfigId neighborConfigId) {
    EigrpNeighborConfig neighborConfig =
        _process.getNeighbors().get(neighborConfigId.getInterfaceName());
    assert neighborConfig != null;
    RoutingPolicy exportPolicy =
        _configuration.getRoutingPolicies().get(neighborConfig.getExportPolicy());
    assert exportPolicy != null;
    return exportPolicy;
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
    EigrpExternalRoute.Builder outputRouteBuilder = EigrpExternalRoute.builder();
    // Set the metric to match the route metric by default for EIGRP into EIGRP
    if (unannotatedPotentialRoute instanceof EigrpRoute) {
      outputRouteBuilder.setEigrpMetric(((EigrpRoute) unannotatedPotentialRoute).getEigrpMetric());
    }

    if (!exportPolicy.process(
        potentialExportRoute, outputRouteBuilder, null, _vrfName, Direction.OUT)) {
      return null;
    }
    outputRouteBuilder.setAdmin(_defaultExternalAdminCost);
    if (unannotatedPotentialRoute instanceof EigrpExternalRoute) {
      EigrpExternalRoute externalRoute = (EigrpExternalRoute) unannotatedPotentialRoute;
      outputRouteBuilder.setDestinationAsn(externalRoute.getDestinationAsn());
    } else {
      outputRouteBuilder.setDestinationAsn(_asn);
    }
    outputRouteBuilder.setNetwork(unannotatedPotentialRoute.getNetwork());
    outputRouteBuilder.setProcessAsn(_asn);
    outputRouteBuilder.setNonRouting(true);
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
    return Stream.of(
            _rib,
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
        .map(Node::getVirtualRouters)
        .map(vrs -> vrs.get(edge.getNode1().getVrf()))
        .map(vrf -> vrf.getEigrpProcess(asn))
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
