package org.batfish.dataplane.ibdp;

import static java.util.Objects.requireNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.dataplane.rib.AbstractRib.importRib;
import static org.batfish.dataplane.rib.RibDelta.importRibDelta;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.Network;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.batfish.datamodel.eigrp.EigrpNeighborConfigId;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.dataplane.rib.EigrpExternalRib;
import org.batfish.dataplane.rib.EigrpInternalRib;
import org.batfish.dataplane.rib.EigrpRib;
import org.batfish.dataplane.rib.Rib;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/** An instance of an EigrpProcess as constructed and used by {@link VirtualRouter} */
@ParametersAreNonnullByDefault
final class EigrpRoutingProcess implements RoutingProcess<EigrpTopology, EigrpRoute> {

  private final long _asn;
  private final int _defaultExternalAdminCost;
  private final int _defaultInternalAdminCost;
  /** Helper RIB containing EIGRP external paths */
  @Nonnull private final EigrpExternalRib _externalRib;

  @Nonnull private final List<EigrpNeighborConfigId> _interfaces;
  /** Helper RIB containing all EIGRP paths internal to this router's ASN. */
  @Nonnull private final EigrpInternalRib _internalRib;
  /**
   * Helper RIBs containing paths obtained with EIGRP during current iteration. An Adj-RIB of sorts.
   */
  @Nonnull private final EigrpInternalRib _internalStagingRib;

  @Nonnull private final String _vrfName;
  /** Helper RIBs containing EIGRP internal and external paths. */
  @Nonnull private final EigrpRib _rib;
  /** Routing policy to determine whether and how to export */
  @Nullable private final RoutingPolicy _exportPolicy;
  /** Incoming internal route messages into this router from each EIGRP adjacency */
  @Nonnull
  private SortedMap<EigrpEdge, Queue<RouteAdvertisement<EigrpInternalRoute>>>
      _incomingInternalRoutes;
  /** Incoming external route messages into this router from each EIGRP adjacency */
  @Nonnull @VisibleForTesting
  SortedMap<EigrpEdge, Queue<RouteAdvertisement<EigrpExternalRoute>>> _incomingExternalRoutes;

  @Nonnull private EigrpExternalRib _externalStagingRib;
  /** Current known EIGRP topology */
  @Nonnull private EigrpTopology _topology;

  /** A {@link RibDelta} indicating which internal routes we initialized */
  @Nonnull private RibDelta<EigrpInternalRoute> _initializationDelta;

  /**
   * A {@link RibDelta} containing external routes we need to send/withdraw based on most recent
   * round of route redistribution
   */
  @Nonnull private RibDelta<EigrpExternalRoute> _queuedForRedistribution;

  EigrpRoutingProcess(final EigrpProcess process, final String vrfName, final Configuration c) {
    _asn = process.getAsn();
    _defaultExternalAdminCost =
        RoutingProtocol.EIGRP_EX.getDefaultAdministrativeCost(c.getConfigurationFormat());
    _defaultInternalAdminCost =
        RoutingProtocol.EIGRP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    _externalRib = new EigrpExternalRib();
    _externalStagingRib = new EigrpExternalRib();
    _interfaces = new ArrayList<>();
    _internalRib = new EigrpInternalRib();
    _internalStagingRib = new EigrpInternalRib();
    _rib = new EigrpRib();
    _vrfName = vrfName;

    initInternalRoutes(vrfName, c);

    // get EIGRP export policy name
    String exportPolicyName = process.getExportPolicy();
    _exportPolicy = exportPolicyName != null ? c.getRoutingPolicies().get(exportPolicyName) : null;
    _topology = EigrpTopology.EMPTY;
    _initializationDelta = RibDelta.empty();
    _queuedForRedistribution = RibDelta.empty();
    _incomingInternalRoutes = ImmutableSortedMap.of();
    _incomingExternalRoutes = ImmutableSortedMap.of();
  }

  @Override
  public void initialize(Node n) {
    _initializationDelta = initInternalRoutes(_vrfName, n.getConfiguration());
  }

  @Override
  public void updateTopology(EigrpTopology topology) {
    _topology = topology;
    updateQueues(topology);
  }

  @Override
  public void executeIteration(Map<String, Node> allNodes) {
    if (!_initializationDelta.isEmpty()) {
      // If we haven't sent out the first round of updates after initialization, do so now. Then
      // clear the initialization delta
      sendOutInternalRoutes(_initializationDelta, allNodes, _topology);
      _initializationDelta = RibDelta.empty();
    }

    // Process internal routes
    RibDelta<EigrpInternalRoute> internalDelta = processInternalRoutes();
    sendOutInternalRoutes(internalDelta, allNodes, _topology);

    // Send out anything we had queued for redistribution
    sendOutExternalRoutes(_queuedForRedistribution, allNodes, _topology);
    _queuedForRedistribution = RibDelta.<EigrpExternalRoute>builder().build();

    // Process new external routes and re-advertise them as necessary
    RibDelta<EigrpExternalRoute> externalDelta = processExternalRoutes();
    sendOutExternalRoutes(externalDelta, allNodes, _topology);
  }

  @Nonnull
  @Override
  public RibDelta<EigrpRoute> getUpdatesForMainRib() {
    return RibDelta.<EigrpRoute>builder().build();
  }

  @Override
  public void redistribute(RibDelta<? extends AnnotatedRoute<AbstractRoute>> mainRibDelta) {}

  @Override
  public boolean isDirty() {
    return false;
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
      _interfaces.add(new EigrpNeighborConfigId(c.getHostname(), iface));
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

  private RibDelta<EigrpInternalRoute> processInternalRoutes() {
    assert _incomingInternalRoutes != null;
    return RibDelta.<EigrpInternalRoute>builder().build();
  }

  private RibDelta<EigrpExternalRoute> processExternalRoutes() {
    return RibDelta.<EigrpExternalRoute>builder().build();
  }

  private void sendOutInternalRoutes(
      @SuppressWarnings("unused") RibDelta<EigrpInternalRoute> initializationDelta,
      @SuppressWarnings("unused") Map<String, Node> allNodes,
      @SuppressWarnings("unused") EigrpTopology topology) {
    // TODO: flesh out route advertisements
  }

  @SuppressWarnings("unsused")
  private void sendOutExternalRoutes(
      @SuppressWarnings("unused") RibDelta<EigrpExternalRoute> queuedForRedistribution,
      @SuppressWarnings("unused") Map<String, Node> allNodes,
      @SuppressWarnings("unused") EigrpTopology topology) {
    // TODO: flesh out route advertisements
  }

  /**
   * Computes an exportable EIGRP route from policy and existing routes
   *
   * @param potentialExportRoute Route to consider exporting
   * @return The computed export route or null if no route will be exported
   */
  @Nullable
  private EigrpExternalRoute computeEigrpExportRoute(
      AnnotatedRoute<AbstractRoute> potentialExportRoute) {
    AbstractRoute unannotatedPotentialRoute = potentialExportRoute.getRoute();
    EigrpExternalRoute.Builder outputRouteBuilder = EigrpExternalRoute.builder();
    // Set the metric to match the route metric by default for EIGRP into EIGRP
    if (unannotatedPotentialRoute instanceof EigrpRoute) {
      outputRouteBuilder.setEigrpMetric(((EigrpRoute) unannotatedPotentialRoute).getEigrpMetric());
    }
    // Export based on the policy result of processing the potentialExportRoute
    boolean accept =
        _exportPolicy != null
            && _exportPolicy.process(
                potentialExportRoute, outputRouteBuilder, null, _vrfName, Direction.OUT);
    if (!accept) {
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
   *   <li>"external" RIB ({@link #_externalRib})
   *   <li>message queue ({@link #_incomingExternalRoutes})
   * </ul>
   *
   * @return integer hashcode
   */
  int computeIterationHashCode() {
    return _externalRib.getTypedRoutes().hashCode()
        + _incomingExternalRoutes.values().stream()
            .flatMap(Queue::stream)
            .mapToInt(RouteAdvertisement::hashCode)
            .sum();
  }

  long getAsn() {
    return _asn;
  }

  /** Merge internal EIGRP RIB into a general EIGRP RIB, then merge that into the independent RIB */
  void importInternalRoutes(Rib independentRib, String vrfName) {
    importRib(_rib, _internalRib);
    importRib(independentRib, _rib, vrfName);
  }

  void initExports(Map<String, Node> allNodes, Set<AnnotatedRoute<AbstractRoute>> mainRoutes) {

    // For each route in the previous RIB, compute an export route and add it
    RibDelta.Builder<EigrpExternalRoute> builder = RibDelta.builder();

    for (AnnotatedRoute<AbstractRoute> potentialExport : mainRoutes) {
      EigrpExternalRoute outputRoute = computeEigrpExportRoute(potentialExport);
      if (outputRoute == null) {
        continue; // no need to export
      }
      builder.from(_externalRib.mergeRouteGetDelta(outputRoute));
    }

    queueOutgoingExternalRoutes(allNodes, builder.build());
  }

  /**
   * Initialize incoming EIGRP message queues for each adjacency
   *
   * @param eigrpTopology The topology representing EIGRP adjacencies
   */
  void updateQueues(EigrpTopology eigrpTopology) {
    Network<EigrpNeighborConfigId, EigrpEdge> network = eigrpTopology.getNetwork();
    _incomingExternalRoutes =
        _interfaces.stream()
            .filter(network.nodes()::contains)
            .flatMap(n -> network.inEdges(n).stream())
            .collect(toImmutableSortedMap(Function.identity(), e -> new ConcurrentLinkedQueue<>()));
    _incomingInternalRoutes =
        _interfaces.stream()
            .filter(network.nodes()::contains)
            .flatMap(n -> network.inEdges(n).stream())
            .collect(toImmutableSortedMap(Function.identity(), e -> new ConcurrentLinkedQueue<>()));
  }

  /**
   * Propagate EIGRP external routes from our neighbors by reading EIGRP route "advertisements" from
   * our queues.
   *
   * @param nc All network configurations
   * @return a {@link RibDelta}
   */
  @Nonnull
  RibDelta<EigrpExternalRoute> propagateExternalRoutes(NetworkConfigurations nc) {

    RibDelta.Builder<EigrpExternalRoute> deltaBuilder = RibDelta.builder();
    EigrpExternalRoute.Builder routeBuilder = EigrpExternalRoute.builder();
    routeBuilder.setAdmin(_defaultExternalAdminCost).setProcessAsn(_asn);

    _incomingExternalRoutes.forEach(
        (edge, queue) -> {
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
            EigrpExternalRoute newRoute = routeBuilder.build();

            if (routeAdvert.isWithdrawn()) {
              deltaBuilder.remove(newRoute, Reason.WITHDRAW);
            } else {
              deltaBuilder.from(_externalStagingRib.mergeRouteGetDelta(newRoute));
            }
          }
        });

    return deltaBuilder.build();
  }

  /**
   * Propagate EIGRP internal routes from every valid EIGRP neighbors
   *
   * @param nodes mapping of node names to instances.
   * @param eigrpTopology EIGRP session topology
   * @param nc All network configurations
   * @return true if new routes have been added to the staging RIB
   */
  boolean propagateInternalRoutes(
      Map<String, Node> nodes, EigrpTopology eigrpTopology, NetworkConfigurations nc) {
    Network<EigrpNeighborConfigId, EigrpEdge> network = eigrpTopology.getNetwork();
    Set<EigrpNeighborConfigId> eigrpNodes = network.nodes();
    return _interfaces.stream()
        .filter(eigrpNodes::contains)
        .flatMap(n -> network.inEdges(n).stream())
        .map(
            edge ->
                propagateInternalRoutesFromNeighbor(
                    nodes.get(edge.getNode1().getHostname()),
                    edge.getNode2().getInterfaceSettings(nc).getMetric(),
                    edge.getNode1().getInterface(nc)))
        .reduce(false, (a, b) -> a || b);
  }

  /**
   * Propagate EIGRP Internal routes from a single neighbor.
   *
   * @param neighbor the neighbor
   * @param connectingInterfaceMetric EIGRP Metric of the interface on which we are connected to the
   *     neighbor
   * @param neighborInterface interface that the neighbor uses to connect to us
   * @return true if new routes have been added to the staging RIB
   */
  private boolean propagateInternalRoutesFromNeighbor(
      Node neighbor, EigrpMetric connectingInterfaceMetric, Interface neighborInterface) {

    // neighbor is in EIGRP topology, so getEigrp() is not null
    EigrpMetric neighborInterfaceMetric = requireNonNull(neighborInterface.getEigrp()).getMetric();

    /*
     * An EIGRP neighbor relationship exists on this edge. We will examine all internal routes
     * belonging to the neighbor to see what should be propagated to this router. We compute
     * the new cost associated with our settings and the existing metrics and use the
     * neighborInterface's address as the next hop ip.
     */
    String neighborVrfName = neighborInterface.getVrfName();
    VirtualRouter neighborVirtualRouter = neighbor.getVirtualRouters().get(neighborVrfName);
    long asn = neighborInterface.getEigrp().getAsn();
    Ip nextHopIp = neighborInterface.getConcreteAddress().getIp();
    boolean changed = false;
    Set<EigrpInternalRoute> neighborRoutes =
        requireNonNull(neighborVirtualRouter.getEigrpProcess(asn))._internalRib.getTypedRoutes();
    for (EigrpInternalRoute neighborRoute : neighborRoutes) {
      EigrpMetric newMetric =
          connectingInterfaceMetric.accumulate(
              neighborInterfaceMetric, neighborRoute.getEigrpMetric());
      EigrpInternalRoute newRoute =
          EigrpInternalRoute.builder()
              .setAdmin(_defaultInternalAdminCost)
              .setEigrpMetric(newMetric)
              .setNetwork(neighborRoute.getNetwork())
              .setNextHopIp(nextHopIp)
              .setProcessAsn(_asn)
              .build();
      changed |= _internalStagingRib.mergeRoute(newRoute);
    }
    return changed;
  }

  private void queueOutgoingExternalRoutes(
      Map<String, Node> allNodes, RibDelta<EigrpExternalRoute> delta) {
    // Loop over neighbors, enqueue messages
    for (EigrpEdge edge : _incomingExternalRoutes.keySet()) {
      Queue<RouteAdvertisement<EigrpExternalRoute>> queue =
          requireNonNull(
                  allNodes
                      .get(edge.getNode1().getHostname())
                      .getVirtualRouters()
                      .get(edge.getNode1().getVrf())
                      .getEigrpProcess(_asn))
              ._incomingExternalRoutes
              .get(edge.reverse());
      VirtualRouter.queueDelta(queue, delta);
    }
  }

  /** Re-initialize RIBs (at the start of each iteration). */
  void reInitForNewIteration() {
    /*
     * Staging RIBs can be re-initialized
     */
    _externalStagingRib = new EigrpExternalRib();
    /*
     * Re-add independent EIGRP routes to eigrpRib for tie-breaking
     */
    importRib(_rib, _internalRib);
  }

  /**
   * Merges staged EIGRP external route into "real" EIGRP-external RIB. Following that, move any
   * resulting deltas into the combined EIGRP RIB, and finally, main RIB.
   *
   * @param allNodes all network nodes, keyed by hostname
   * @param delta staging delta
   * @param mainRibRouteDeltaBuilder Keeps track of changes to the main RIB
   * @param mainRib The finalized RIB, a combination of different protocol RIBs
   * @return true if any routes from delta were merged into the combined EIGRP RIB.
   */
  boolean unstageExternalRoutes(
      Map<String, Node> allNodes,
      @Nonnull RibDelta<EigrpExternalRoute> delta,
      RibDelta.Builder<AnnotatedRoute<AbstractRoute>> mainRibRouteDeltaBuilder,
      Rib mainRib,
      String vrfName) {
    RibDelta<EigrpExternalRoute> ribDelta = importRibDelta(_externalRib, delta);
    queueOutgoingExternalRoutes(allNodes, delta);
    RibDelta.Builder<EigrpRoute> eigrpDeltaBuilder = RibDelta.builder();
    eigrpDeltaBuilder.from(importRibDelta(_rib, ribDelta));
    mainRibRouteDeltaBuilder.from(
        RibDelta.importRibDelta(mainRib, eigrpDeltaBuilder.build(), vrfName));
    return !ribDelta.isEmpty();
  }

  /** Merges staged EIGRP internal routes into the "real" EIGRP-internal RIBs */
  void unstageInternalRoutes() {
    importRib(_internalRib, _internalStagingRib);
  }
}
