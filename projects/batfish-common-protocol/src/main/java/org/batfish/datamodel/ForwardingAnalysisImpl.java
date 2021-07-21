package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Ordering.natural;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.bdd.MemoizedIpSpaceToBDD;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;

/** Implementation of {@link ForwardingAnalysis}. */
public final class ForwardingAnalysisImpl implements ForwardingAnalysis, Serializable {
  // node -> interface -> ips that the interface would reply arp request
  private final Map<String, Map<String, IpSpace>> _arpReplies;

  // node -> vrf -> nextVrf -> IPs that vrf delegates to nextVrf
  private final Map<String, Map<String, Map<String, IpSpace>>> _nextVrfIpsByNodeVrf;

  // node -> vrf -> destination IPs that will be null routes
  private final Map<String, Map<String, IpSpace>> _nullRoutedIps;

  // node -> vrf -> destination IPs that can be routed
  private final Map<String, Map<String, IpSpace>> _routableIps;

  // node -> vrf -> forwarding behavior for that VRF.
  private final Map<String, Map<String, VrfForwardingBehavior>> _vrfForwardingBehavior;

  public ForwardingAnalysisImpl(
      Map<String, Configuration> configurations,
      Map<String, Map<String, Fib>> fibs,
      Topology topology,
      Map<Location, LocationInfo> locationInfo) {
    Span span = GlobalTracer.get().buildSpan("Construct ForwardingAnalysis").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      // TODO accept IpSpaceToBDD as parameter
      IpSpaceToBDD ipSpaceToBDD =
          new MemoizedIpSpaceToBDD(new BDDPacket().getDstIp(), ImmutableMap.of());

      IpOwners ipOwners = new IpOwners(configurations);

      // IPs belonging to any interface in the network, even inactive interfaces
      // node -> interface -> IPs owned by that interface
      Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
          IpOwners.computeInterfaceOwnedIps(configurations, /*excludeInactive=*/ false);

      // Owned (i.e., internal to the network) IPs
      IpSpace ownedIps = computeOwnedIps(interfaceOwnedIps);
      // Unowned (i.e., external to the network) IPs
      BDD unownedIpsBDD = ipSpaceToBDD.visit(ownedIps).not();

      // IpSpaces matched by each prefix
      // -- only will have entries for active interfaces if FIB is correct
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps = computeMatchingIps(fibs);
      // Set of routes that forward out each interface
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop =
          computeRoutesWithNextHop(fibs);
      _nullRoutedIps = computeNullRoutedIps(matchingIps, fibs);
      _nextVrfIpsByNodeVrf = computeNextVrfIpsByNodeVrf(matchingIps, fibs);
      _routableIps = computeRoutableIps(fibs);

      /* Compute _arpReplies: for each interface, the set of arp IPs for which that interface will
       * respond.
       */
      {
        // mapping: node name -> vrf name -> interface name -> dst ips which are routed to the
        // interface. Should only include active interfaces.
        Map<String, Map<String, Map<String, IpSpace>>> ipsRoutedOutInterfaces =
            computeIpsRoutedOutInterfaces(matchingIps, routesWithNextHop);
        _arpReplies =
            computeArpReplies(
                configurations, ipsRoutedOutInterfaces, interfaceOwnedIps, _routableIps);
      }

      // mapping: hostname -> interface -> ips on which we should assume some external device (not
      // modeled in batfish) is listening, and would reply to ARP in the real world.
      Map<String, Map<String, IpSpace>> interfaceExternalArpIps =
          locationInfo.entrySet().stream()
              .filter(entry -> entry.getKey() instanceof InterfaceLinkLocation)
              .collect(
                  Collectors.groupingBy(
                      entry -> entry.getKey().getNodeName(),
                      Collectors.toMap(
                          entry -> ((InterfaceLinkLocation) entry.getKey()).getInterfaceName(),
                          entry -> entry.getValue().getArpIps())));

      Map<String, Map<String, BDD>> interfaceExternalArpIpBDDs =
          computeInterfaceExternalArpIpBDDs(interfaceExternalArpIps, ipSpaceToBDD);
      // hostname -> interfaces that are not full. I.e. could have neighbors not present in snapshot
      Map<String, Set<String>> interfacesWithMissingDevices =
          computeInterfacesWithMissingDevices(interfaceExternalArpIpBDDs, unownedIpsBDD);

      // ips belonging to any subnet in the network, including inactive interfaces.
      IpSpace internalIps = computeInternalIps(ipOwners.getAllInterfaceHostIps());

      // ips not belonging to any subnet in the network, including inactive interfaces.
      IpSpace externalIps = internalIps.complement();

      _vrfForwardingBehavior =
          toImmutableMap(
              configurations,
              Entry::getKey, // config
              nodeEntry ->
                  toImmutableMap(
                      nodeEntry.getValue().getVrfs(),
                      Entry::getKey, // vrf
                      vrfEntry -> {
                        String node = nodeEntry.getKey();
                        String vrf = vrfEntry.getKey();

                        Map<String, IpSpace> accepted =
                            ipOwners
                                .getVrfIfaceOwnedIpSpaces()
                                .getOrDefault(node, ImmutableMap.of())
                                .getOrDefault(vrf, ImmutableMap.of());

                        /*
                         * Mapping: route -> nexthopinterface -> resolved nextHopIp -> interfaceRoutes
                         */
                        Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
                            nextHopInterfaces = computeNextHopInterfaces(fibs.get(node).get(vrf));

                        /* interface -> set of routes on that vrf that forward out that interface,
                         * ARPing for the destination IP
                         */
                        Map<String, Set<AbstractRoute>> routesWhereDstIpCanBeArpIp =
                            computeRoutesWhereDstIpCanBeArpIp(
                                node, vrf, nextHopInterfaces, routesWithNextHop);

                        /* edge -> routes in this vrf that forward out the source of that edge,
                         * ARPing for the dest IP and receiving a response from the target of the edge.
                         *
                         * Note: the source interface of the edge must be in the node, but may not be in the vrf,
                         * due to route leaking, etc
                         */
                        Map<Edge, Set<AbstractRoute>> routesWithDestIpEdge =
                            computeRoutesWithDestIpEdge(node, topology, routesWhereDstIpCanBeArpIp);

                        /* edge -> dst ips for which this vrf forwards out the source of the edge,
                         * ARPing for the dest IP and receiving a reply from the target of the edge.
                         *
                         * Note: the source interface of the edge must be in the node, but may not be in the vrf,
                         * due to route leaking, etc
                         */
                        Map<Edge, IpSpace> arpTrueEdgeDestIp =
                            computeArpTrueEdgeDestIp(
                                node, vrf, matchingIps, routesWithDestIpEdge, _arpReplies);

                        /* edge -> dst ips for which that vrf forwards out the source of the edge,
                         * ARPing for some next-hop IP and receiving a reply from the target of the edge.
                         *
                         * Note: the source interface of the edge must be in the node, but may not be in the vrf,
                         * due to route leaking, etc
                         */
                        Map<Edge, Set<AbstractRoute>> routesWithNextHopIpArpTrue =
                            computeRoutesWithNextHopIpArpTrue(
                                node,
                                vrf,
                                nextHopInterfaces,
                                topology,
                                _arpReplies,
                                routesWithNextHop);

                        /* edge -> dst ips for which this vrf forwards out the source of the edge,
                         * ARPing for some next-hop IP and receiving a reply from the target of the edge.
                         *
                         * Note: the source interface of the edge must be in the node, but may not be in the vrf,
                         * due to route leaking, etc
                         */
                        Map<Edge, IpSpace> arpTrueEdgeNextHopIp =
                            computeArpTrueEdgeNextHopIp(
                                node, vrf, matchingIps, routesWithNextHopIpArpTrue);

                        Map<Edge, IpSpace> arpTrueEdge =
                            computeArpTrueEdge(arpTrueEdgeDestIp, arpTrueEdgeNextHopIp);

                        Map<String, InterfaceForwardingBehavior> interfaceForwardingBehavior =
                            toImmutableMap(
                                // routesWithNextHop may include interfaces in other VRFs that we
                                // forward out through due to VRF leaking. All active interfaces
                                // in this VRF are included due to local routes.
                                routesWithNextHop.get(node).get(vrf).keySet(),
                                Function.identity(),
                                iface -> {
                                  /* Compute ARP stuff bottom-up from _arpReplies. */
                                  IpSpace someoneReplies =
                                      computeSomeoneReplies(node, iface, topology, _arpReplies);

                                  /* set of routes on that vrf that forward out that interface
                                   * with a next hop ip that gets no arp replies
                                   */
                                  Set<AbstractRoute> routesWithNextHopIpArpFalse =
                                      computeRoutesWithNextHopIpArpFalse(
                                          node,
                                          vrf,
                                          iface,
                                          nextHopInterfaces,
                                          routesWithNextHop,
                                          someoneReplies);

                                  /* dst IPs for which this VRF forwards out that interface, ARPing
                                   * for the dst ip itself with no reply
                                   */
                                  IpSpace arpFalseDestIp =
                                      computeArpFalseDestIp(
                                          node,
                                          vrf,
                                          iface,
                                          matchingIps,
                                          routesWhereDstIpCanBeArpIp,
                                          someoneReplies);

                                  /* dst ips for which this vrf forwards out that interface,
                                   * ARPing for a next-hop IP and receiving no reply
                                   */
                                  IpSpace arpFalseNextHopIp =
                                      computeArpFalseNextHopIp(
                                          node, vrf, matchingIps, routesWithNextHopIpArpFalse);

                                  IpSpace arpFalse =
                                      AclIpSpace.union(arpFalseDestIp, arpFalseNextHopIp);

                                  /* dst IPs for which that VRF forwards out that interface, ARPing
                                   * for some unowned next-hop IP with no reply
                                   */
                                  IpSpace dstIpsWithUnownedNextHopIpArpFalse =
                                      computeDstIpsWithNextHopIpArpFalseFilter(
                                          node,
                                          vrf,
                                          matchingIps,
                                          routesWithNextHopIpArpFalse,
                                          route ->
                                              ipSpaceToBDD
                                                  .toBDD(route.getNextHopIp())
                                                  .andSat(unownedIpsBDD));

                                  /* dst IPs for which that VRF forwards out that interface, ARPing
                                   * for some owned next-hop IP with no reply
                                   */
                                  IpSpace dstIpsWithOwnedNextHopIpArpFalse =
                                      computeDstIpsWithNextHopIpArpFalseFilter(
                                          node,
                                          vrf,
                                          matchingIps,
                                          routesWithNextHopIpArpFalse,
                                          route ->
                                              !ipSpaceToBDD
                                                  .toBDD(route.getNextHopIp())
                                                  .andSat(unownedIpsBDD));

                                  IpSpace deliveredToSubnet =
                                      computeDeliveredToSubnet(
                                          node,
                                          iface,
                                          arpFalseDestIp,
                                          interfaceExternalArpIps,
                                          ownedIps);

                                  IpSpace exitsNetwork =
                                      computeExitsNetwork(
                                          node,
                                          iface,
                                          interfacesWithMissingDevices,
                                          dstIpsWithUnownedNextHopIpArpFalse,
                                          arpFalseDestIp,
                                          externalIps);

                                  IpSpace insufficientInfo =
                                      computeInsufficientInfo(
                                          node,
                                          iface,
                                          interfaceExternalArpIps,
                                          interfacesWithMissingDevices,
                                          arpFalseDestIp,
                                          dstIpsWithUnownedNextHopIpArpFalse,
                                          dstIpsWithOwnedNextHopIpArpFalse,
                                          internalIps);

                                  IpSpace neighborUnreachable =
                                      computeNeighborUnreachable(
                                          node,
                                          iface,
                                          arpFalse,
                                          interfacesWithMissingDevices,
                                          arpFalseDestIp,
                                          interfaceExternalArpIps,
                                          ownedIps);

                                  return InterfaceForwardingBehavior.builder()
                                      .setAccepted(accepted.get(iface))
                                      .setDeliveredToSubnet(deliveredToSubnet)
                                      .setExitsNetwork(exitsNetwork)
                                      .setInsufficientInfo(insufficientInfo)
                                      .setNeighborUnreachable(neighborUnreachable)
                                      .build();
                                });

                        return VrfForwardingBehavior.builder()
                            .setArpTrueEdge(arpTrueEdge)
                            .setInterfaceForwardingBehavior(interfaceForwardingBehavior)
                            .setNextVrf(_nextVrfIpsByNodeVrf.get(node).get(vrf))
                            .setNullRoutedIps(_nullRoutedIps.get(node).get(vrf))
                            .setRoutableIps(_routableIps.get(node).get(vrf))
                            .build();
                      }));

      assert sanityCheck(ipSpaceToBDD, configurations);
    } finally {
      span.finish();
    }
  }

  /**
   * Compute an IP address ACL for each interface of each node permitting only those IPs for which
   * the node would send out an ARP reply on that interface: <br>
   * <br>
   * 1) PERMIT IPs belonging to the interface.<br>
   * 2) (Proxy-ARP) DENY any IP for which there is a longest-prefix match entry in the FIB that goes
   * through the interface.<br>
   * 3) (Proxy-ARP) PERMIT any other IP routable via the VRF of the interface.<br>
   * 4) (Proxy-ARP) PERMIT any statically configured arp IPs.
   */
  @VisibleForTesting
  static Map<String, Map<String, IpSpace>> computeArpReplies(
      Map<String, Configuration> configurations,
      Map<String, Map<String, Map<String, IpSpace>>> ipsRoutedOutInterfaces,
      Map<String, Map<String, Set<Ip>>> interfaceOwnedIps,
      Map<String, Map<String, IpSpace>> routableIps) {
    Span span = GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeArpReplies").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return toImmutableMap(
          configurations,
          Entry::getKey,
          nodeEntry -> {
            String hostname = nodeEntry.getKey();
            return computeArpRepliesByInterface(
                nodeEntry.getValue().getActiveInterfaces(),
                routableIps.get(hostname),
                ipsRoutedOutInterfaces.get(hostname),
                interfaceOwnedIps);
          });
    } finally {
      span.finish();
    }
  }

  @VisibleForTesting
  static Map<String, IpSpace> computeArpRepliesByInterface(
      Map<String, Interface> interfaces,
      Map<String, IpSpace> routableIpsByVrf,
      Map<String, Map<String, IpSpace>> ipsRoutedOutInterfaces,
      Map<String, Map<String, Set<Ip>>> interfaceOwnedIps) {
    return toImmutableMap(
        interfaces,
        Entry::getKey,
        ifaceEntry ->
            computeInterfaceArpReplies(
                ifaceEntry.getValue(),
                /* We believe at this time that an interface would send an ARP reply only based
                 * on the routes in it's own VRF.
                 * This type of routing separation is the point of VRFs, and cross-VRF introspection
                 * for the purposes of ARP replies is unlikely to happen by default.
                 */
                routableIpsByVrf.get(ifaceEntry.getValue().getVrfName()),
                ipsRoutedOutInterfaces
                    .get(ifaceEntry.getValue().getVrfName())
                    .getOrDefault(ifaceEntry.getKey(), EmptyIpSpace.INSTANCE),
                interfaceOwnedIps));
  }

  @VisibleForTesting
  static Map<Edge, IpSpace> computeArpTrueEdge(
      Map<Edge, IpSpace> arpTrueEdgeDestIp, Map<Edge, IpSpace> arpTrueEdgeNextHopIp) {
    return toImmutableMap(
        Sets.union(arpTrueEdgeDestIp.keySet(), arpTrueEdgeNextHopIp.keySet()),
        Function.identity(), // edge
        edge -> AclIpSpace.union(arpTrueEdgeDestIp.get(edge), arpTrueEdgeNextHopIp.get(edge)));
  }

  @VisibleForTesting
  static Map<Edge, IpSpace> computeArpTrueEdgeDestIp(
      String node,
      String vrf,
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<Edge, Set<AbstractRoute>> routesWithDestIpEdge,
      Map<String, Map<String, IpSpace>> arpReplies) {
    return toImmutableMap(
        routesWithDestIpEdge,
        Entry::getKey, // edge
        edgeEntry -> {
          Edge edge = edgeEntry.getKey();
          Set<AbstractRoute> routes = edgeEntry.getValue();
          IpSpace dstIpMatchesSomeRoutePrefix =
              computeRouteMatchConditions(routes, matchingIps.get(node).get(vrf));
          String recvNode = edge.getNode2();
          String recvInterface = edge.getInt2();
          IpSpace recvReplies = arpReplies.get(recvNode).get(recvInterface);
          return AclIpSpace.rejecting(dstIpMatchesSomeRoutePrefix.complement())
              .thenPermitting(recvReplies)
              .build();
        });
  }

  @VisibleForTesting
  static Map<Edge, IpSpace> computeArpTrueEdgeNextHopIp(
      String node,
      String vrf,
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<Edge, Set<AbstractRoute>> routesWithNextHopIpArpTrue) {
    return toImmutableMap(
        routesWithNextHopIpArpTrue,
        Entry::getKey, // edge
        edgeEntry ->
            computeRouteMatchConditions(edgeEntry.getValue(), matchingIps.get(node).get(vrf)));
  }

  @VisibleForTesting
  static IpSpace computeInterfaceArpReplies(
      @Nonnull Interface iface,
      @Nonnull IpSpace routableIpsForThisVrf,
      @Nonnull IpSpace ipsRoutedThroughInterface,
      @Nonnull Map<String, Map<String, Set<Ip>>> interfaceOwnedIps) {
    IpSpace ipsAssignedToThisInterface =
        computeIpsAssignedToThisInterfaceForArpReplies(iface, interfaceOwnedIps);
    if (ipsAssignedToThisInterface == EmptyIpSpace.INSTANCE) {
      // if no IPs are assigned to this interface at all (not even link-local), it replies to no ARP
      // requests.
      return EmptyIpSpace.INSTANCE;
    }
    /* Accept IPs assigned to this interface */
    AclIpSpace.Builder interfaceArpReplies = AclIpSpace.permitting(ipsAssignedToThisInterface);

    /* Accept IPs configured statically */
    interfaceArpReplies.thenPermitting(iface.getAdditionalArpIps());

    if (iface.getProxyArp()) {
      /* Reject IPs routed through this interface */
      interfaceArpReplies.thenRejecting(ipsRoutedThroughInterface);

      /* Accept all other routable IPs */
      interfaceArpReplies.thenPermitting(routableIpsForThisVrf);
    }

    return interfaceArpReplies.build();
  }

  /**
   * Compute IP addresses "assigned" to this interface for the purposes for ARP replies. This is a
   * space of IPs that an interface will send an ARP reply for. Includes IPs that an interface owns
   * (explicitly assigned or virtual) as well as any defined link-local addresses.
   */
  @VisibleForTesting
  static IpSpace computeIpsAssignedToThisInterfaceForArpReplies(
      Interface iface, Map<String, Map<String, Set<Ip>>> interfaceOwnedIps) {
    /*
     * If a device has no interfaces with concrete IPs, it will not appear in interfaceOwnedIps.
     * When we get the owned IP space for such interfaces, there could be an NPE, work around that
     */
    Set<Ip> concreteIps =
        interfaceOwnedIps
            .getOrDefault(iface.getOwner().getHostname(), ImmutableMap.of())
            .getOrDefault(iface.getName(), ImmutableSet.of());
    Set<LinkLocalAddress> linkLocalIps = iface.getAllLinkLocalAddresses();
    if (concreteIps.isEmpty() && linkLocalIps.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }
    IpWildcardSetIpSpace.Builder ipsAssignedToThisInterfaceBuilder = IpWildcardSetIpSpace.builder();
    concreteIps.forEach(ip -> ipsAssignedToThisInterfaceBuilder.including(IpWildcard.create(ip)));
    linkLocalIps.forEach(
        addr -> ipsAssignedToThisInterfaceBuilder.including(IpWildcard.create(addr.getIp())));
    return ipsAssignedToThisInterfaceBuilder.build();
  }

  @VisibleForTesting
  static Map<String, Map<String, Map<String, IpSpace>>> computeIpsRoutedOutInterfaces(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop) {
    Span span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeIpsRoutedOutInterfaces")
            .start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return toImmutableMap(
          routesWithNextHop,
          Entry::getKey /* hostname */,
          nodeEntry -> {
            String hostname = nodeEntry.getKey();
            return toImmutableMap(
                nodeEntry.getValue(),
                Entry::getKey,
                vrfEntry -> {
                  String vrf = vrfEntry.getKey();
                  Map<Prefix, IpSpace> vrfMatchingIps = matchingIps.get(hostname).get(vrf);
                  return vrfEntry.getValue().entrySet().stream()
                      /*
                       *  Cannot determine IPs for null interface here because it is
                       *  not tied to a single VRF.
                       */
                      .filter(
                          ifaceEntry -> !ifaceEntry.getKey().equals(Interface.NULL_INTERFACE_NAME))
                      .map(
                          ifaceEntry -> {
                            String iface = ifaceEntry.getKey();
                            Set<AbstractRoute> routes = ifaceEntry.getValue();
                            return Maps.immutableEntry(
                                iface, computeRouteMatchConditions(routes, vrfMatchingIps));
                          })
                      .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
                });
          });
    } finally {
      span.finish();
    }
  }

  @VisibleForTesting
  static IpSpace computeArpFalseDestIp(
      String node,
      String vrf,
      String iface,
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<String, Set<AbstractRoute>> routesWhereDstIpCanBeArpIp,
      IpSpace someoneReplies) {
    Map<Prefix, IpSpace> vrfMatchingIps = matchingIps.get(node).get(vrf);
    Set<AbstractRoute> routes = routesWhereDstIpCanBeArpIp.get(iface);
    IpSpace ipsRoutedOutInterface = computeRouteMatchConditions(routes, vrfMatchingIps);
    return AclIpSpace.rejecting(someoneReplies).thenPermitting(ipsRoutedOutInterface).build();
  }

  @VisibleForTesting
  static IpSpace computeArpFalseNextHopIp(
      String node,
      String vrf,
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Set<AbstractRoute> routesWithNextHopIpArpFalse) {
    return computeRouteMatchConditions(routesWithNextHopIpArpFalse, matchingIps.get(node).get(vrf));
  }

  @VisibleForTesting
  static Map<String, Map<String, IpSpace>> computeNullRoutedIps(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<String, Map<String, Fib>> fibs) {
    Span span = GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeNullRoutedIps").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return fibs.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  fibsByHostnameEntry -> {
                    String hostname = fibsByHostnameEntry.getKey();
                    return fibsByHostnameEntry.getValue().entrySet().stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* vrf */,
                                fibsByVrfEntry -> {
                                  String vrf = fibsByVrfEntry.getKey();
                                  Fib fib = fibsByVrfEntry.getValue();
                                  Map<Prefix, IpSpace> vrfMatchingIps =
                                      matchingIps.get(hostname).get(vrf);
                                  Set<AbstractRoute> nullRoutes =
                                      fib.allEntries().stream()
                                          .filter(
                                              fibEntry ->
                                                  fibEntry.getAction() instanceof FibNullRoute)
                                          .map(FibEntry::getTopLevelRoute)
                                          .collect(ImmutableSet.toImmutableSet());
                                  return computeRouteMatchConditions(nullRoutes, vrfMatchingIps);
                                }));
                  }));
    } finally {
      span.finish();
    }
  }

  @VisibleForTesting
  static Map<String, Map<String, Map<String, IpSpace>>> computeNextVrfIpsByNodeVrf(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<String, Map<String, Fib>> fibs) {
    Span span = GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeNextVrfIps").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return fibs.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  fibsByHostnameEntry -> {
                    String hostname = fibsByHostnameEntry.getKey();
                    return fibsByHostnameEntry.getValue().entrySet().stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* vrf */,
                                fibsByVrfEntry ->
                                    computeNextVrfIps(
                                        fibsByVrfEntry.getValue(), /* fib */
                                        matchingIps
                                            .get(hostname)
                                            .get(fibsByVrfEntry.getKey()) /* matchingIps */)));
                  }));
    } finally {
      span.finish();
    }
  }

  private static Map<String, IpSpace> computeNextVrfIps(Fib fib, Map<Prefix, IpSpace> matchingIps) {
    return fib.allEntries().stream()
        .filter(fibEntry -> fibEntry.getAction() instanceof FibNextVrf)
        .collect(
            Collectors.groupingBy(
                fibEntry -> ((FibNextVrf) fibEntry.getAction()).getNextVrf(),
                Collectors.mapping(FibEntry::getTopLevelRoute, ImmutableSet.toImmutableSet())))
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* nextVrf */,
                routesByNextVrfEntry ->
                    computeRouteMatchConditions(
                        routesByNextVrfEntry.getValue() /* routes */, matchingIps)));
  }

  @VisibleForTesting
  static Map<String, Map<String, IpSpace>> computeRoutableIps(Map<String, Map<String, Fib>> fibs) {
    Span span = GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeRoutableIps").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return toImmutableMap(
          fibs,
          Entry::getKey, // node
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey, // vrf
                  vrfEntry ->
                      new IpWildcardSetIpSpace(
                          ImmutableSortedSet.of(),
                          vrfEntry.getValue().allEntries().stream()
                              .map(
                                  fibEntry ->
                                      IpWildcard.create(fibEntry.getTopLevelRoute().getNetwork()))
                              .collect(ImmutableSortedSet.toImmutableSortedSet(natural())))));
    } finally {
      span.finish();
    }
  }

  @VisibleForTesting
  static Map<String, Map<String, Map<Prefix, IpSpace>>> computeMatchingIps(
      Map<String, Map<String, Fib>> fibs) {
    Span span = GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeMatchingIps").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return toImmutableMap(
          fibs,
          Entry::getKey, // node
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey, // vrf
                  vrfEntry -> vrfEntry.getValue().getMatchingIps()));
    } finally {
      span.finish();
    }
  }

  @VisibleForTesting
  static IpSpace computeRouteMatchConditions(
      Set<AbstractRoute> routes, Map<Prefix, IpSpace> matchingIps) {
    // get the union of IpSpace that match one of the routes
    return computeRouteMatchConditionsFilter(routes, matchingIps, r -> true);
  }

  @VisibleForTesting
  static IpSpace computeRouteMatchConditionsFilter(
      Set<AbstractRoute> routes,
      Map<Prefix, IpSpace> matchingIps,
      Predicate<AbstractRoute> routeFilter) {
    // get the union of IpSpace that match one of the routes
    return firstNonNull(
        AclIpSpace.union(
            routes.stream()
                .filter(routeFilter)
                .map(AbstractRoute::getNetwork)
                .distinct()
                .map(matchingIps::get)
                .toArray(IpSpace[]::new)),
        EmptyIpSpace.INSTANCE);
  }

  /**
   * Mapping: interfacename -&gt; a set of routes where each route has at least one unset final next
   * hop ip
   */
  @VisibleForTesting
  static Map<String, Set<AbstractRoute>> computeRoutesWhereDstIpCanBeArpIp(
      String node,
      String vrf,
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop) {
    return toImmutableMap(
        routesWithNextHop.get(node).get(vrf),
        Entry::getKey /* interface */,
        ifaceEntry -> {
          String iface = ifaceEntry.getKey();
          // return a set of routes where each route has
          // some final next hop ip unset
          return ifaceEntry
              .getValue() // routes with this interface as
              // outgoing interfaces
              .stream()
              .filter(
                  route ->
                      nextHopInterfaces
                          .get(route)
                          .get(iface) // final next hop ips
                          .containsKey(Route.UNSET_ROUTE_NEXT_HOP_IP))
              .collect(ImmutableSet.toImmutableSet());
        });
  }

  @VisibleForTesting
  static Map<Edge, Set<AbstractRoute>> computeRoutesWithDestIpEdge(
      String node, Topology topology, Map<String, Set<AbstractRoute>> routesWhereDstIpCanBeArpIp) {
    return routesWhereDstIpCanBeArpIp.entrySet().stream()
        .flatMap(
            ifaceEntry -> {
              NodeInterfacePair out = NodeInterfacePair.of(node, ifaceEntry.getKey());
              Set<AbstractRoute> routes = ifaceEntry.getValue();
              return topology.getNeighbors(out).stream()
                  .map(receiver -> Maps.immutableEntry(new Edge(out, receiver), routes));
            })
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  /* Mapping: hostname -&gt; vrfname -&gt; interfacename -&gt; set of associated routes (i.e.,
   * routes that use the interface as outgoing interface */
  @VisibleForTesting
  static Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWithNextHop(
      Map<String, Map<String, Fib>> fibs) {
    Span span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeRoutesWithNextHop").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return toImmutableMap(
          fibs,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry ->
                      vrfEntry.getValue().allEntries().stream()
                          .filter(fibEntry -> fibEntry.getAction() instanceof FibForward)
                          .collect(
                              Collectors.groupingBy(
                                  fibEntry ->
                                      ((FibForward) fibEntry.getAction()).getInterfaceName(),
                                  Collectors.mapping(
                                      FibEntry::getTopLevelRoute, Collectors.toSet())))));
    } finally {
      span.finish();
    }
  }

  @VisibleForTesting
  static Set<AbstractRoute> computeRoutesWithNextHopIpArpFalse(
      String node,
      String vrf,
      String iface,
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop,
      IpSpace someoneReplies) {
    return computeRoutesWithNextHopIpArpFalseForInterface(
        nextHopInterfaces, iface, routesWithNextHop.get(node).get(vrf).get(iface), someoneReplies);
  }

  @VisibleForTesting
  static Set<AbstractRoute> computeRoutesWithNextHopIpArpFalseForInterface(
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces,
      String outInterface,
      Set<AbstractRoute> candidateRoutes,
      IpSpace someoneReplies) {
    return candidateRoutes.stream()
        .filter(
            candidateRoute ->
                nextHopInterfaces.get(candidateRoute).get(outInterface).keySet().stream()
                    .filter(ip -> !ip.equals(Route.UNSET_ROUTE_NEXT_HOP_IP))
                    .anyMatch(
                        nextHopIp -> !someoneReplies.containsIp(nextHopIp, ImmutableMap.of())))
        .collect(ImmutableSet.toImmutableSet());
  }

  @VisibleForTesting
  static Map<Edge, Set<AbstractRoute>> computeRoutesWithNextHopIpArpTrue(
      String node,
      String vrf,
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces,
      Topology topology,
      Map<String, Map<String, IpSpace>> arpReplies,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop) {
    return routesWithNextHop.get(node).get(vrf).entrySet().stream()
        .flatMap(
            ifaceEntry -> {
              String outInterface = ifaceEntry.getKey();
              Set<AbstractRoute> candidateRoutes = ifaceEntry.getValue();
              NodeInterfacePair out = NodeInterfacePair.of(node, outInterface);
              Set<NodeInterfacePair> receivers = topology.getNeighbors(out);
              return receivers.stream()
                  .map(
                      receiver -> {
                        String recvNode = receiver.getHostname();
                        String recvInterface = receiver.getInterface();
                        IpSpace recvReplies = arpReplies.get(recvNode).get(recvInterface);
                        Edge edge = new Edge(out, receiver);
                        Set<AbstractRoute> routes =
                            candidateRoutes.stream()
                                .filter(
                                    route ->
                                        nextHopInterfaces
                                            .get(route)
                                            .get(outInterface)
                                            .keySet() // nextHopIps
                                            .stream()
                                            .filter(ip -> !Route.UNSET_ROUTE_NEXT_HOP_IP.equals(ip))
                                            .anyMatch(
                                                nextHopIp ->
                                                    recvReplies.containsIp(
                                                        nextHopIp, ImmutableMap.of())))
                                .collect(ImmutableSet.toImmutableSet());
                        return routes.isEmpty() ? null : Maps.immutableEntry(edge, routes);
                      })
                  .filter(Objects::nonNull);
            })
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  @VisibleForTesting
  static @Nonnull IpSpace computeSomeoneReplies(
      String node, String iface, Topology topology, Map<String, Map<String, IpSpace>> arpReplies) {
    return firstNonNull(
        AclIpSpace.union(
            topology.getNeighbors(NodeInterfacePair.of(node, iface)).stream()
                .map(
                    neighbor -> arpReplies.get(neighbor.getHostname()).get(neighbor.getInterface()))
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }

  @Override
  public Map<String, Map<String, IpSpace>> getArpReplies() {
    return _arpReplies;
  }

  @Nonnull
  @Override
  public Map<String, Map<String, VrfForwardingBehavior>> getVrfForwardingBehavior() {
    return _vrfForwardingBehavior;
  }

  private Map<String, Map<String, Map<String, IpSpace>>> getDeliveredToSubnet() {
    return getInterfaceForwardingBehaviorIpSpaces(
        InterfaceForwardingBehavior::getDeliveredToSubnet);
  }

  private static Map<String, Map<String, BDD>> computeInterfaceExternalArpIpBDDs(
      Map<String, Map<String, IpSpace>> interfaceExternalArpIps, IpSpaceToBDD ipSpaceToBDD) {
    Span span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeInterfaceExternalArpIpBDDs")
            .start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return toImmutableMap(
          interfaceExternalArpIps,
          Entry::getKey /* host name */,
          nodeEntry ->
              nodeEntry.getValue().entrySet().stream()
                  .collect(
                      ImmutableMap.toImmutableMap(
                          Entry::getKey, ifaceEntry -> ipSpaceToBDD.visit(ifaceEntry.getValue()))));
    } finally {
      span.finish();
    }
  }

  static Map<String, Map<String, Map<String, IpSpace>>> union(
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces1,
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces2) {
    Span span = GlobalTracer.get().buildSpan("construct union").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      checkArgument(
          ipSpaces1.keySet().equals(ipSpaces2.keySet()),
          "Can't union with different nodes: %s and %s",
          ipSpaces1.keySet(),
          ipSpaces2.keySet());

      return toImmutableMap(
          ipSpaces1,
          Entry::getKey, /* hostname */
          nodeEntry -> {
            Map<String, Map<String, IpSpace>> nodeIpSpace2 = ipSpaces2.get(nodeEntry.getKey());
            checkArgument(
                nodeIpSpace2.keySet().equals(nodeEntry.getValue().keySet()),
                "Can't union with different VRFs in node %s: %s and %s",
                nodeEntry.getKey(),
                nodeEntry.getValue().keySet(),
                nodeIpSpace2.keySet());
            return toImmutableMap(
                nodeEntry.getValue(),
                Entry::getKey, /* vrf */
                vrfEntry -> {
                  Map<String, IpSpace> vrfIpSpaces2 = nodeIpSpace2.get(vrfEntry.getKey());
                  checkArgument(
                      vrfIpSpaces2.keySet().equals(vrfEntry.getValue().keySet()),
                      "Can't union with different interfaces in node %s VRF %s: %s and %s",
                      nodeEntry.getKey(),
                      vrfEntry.getKey(),
                      vrfEntry.getValue().keySet(),
                      vrfIpSpaces2.keySet());
                  return toImmutableMap(
                      vrfEntry.getValue(),
                      Entry::getKey, /* interface */
                      ifaceEntry ->
                          AclIpSpace.union(
                              ifaceEntry.getValue(), vrfIpSpaces2.get(ifaceEntry.getKey())));
                });
          });
    } finally {
      span.finish();
    }
  }

  private static IpSpace computeInternalIps(
      Map<String, Map<String, IpSpace>> interfaceHostSubnetIps) {
    Span span = GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeInternalIps").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return firstNonNull(
          AclIpSpace.union(
              interfaceHostSubnetIps.values().stream()
                  .flatMap(ifaceSubnetIps -> ifaceSubnetIps.values().stream())
                  .collect(Collectors.toList())),
          EmptyIpSpace.INSTANCE);
    } finally {
      span.finish();
    }
  }

  /*
   * Necessary and sufficient: Arping dst ip and the dst IP is not owned but is in an interface
   * subnet.
   */
  @VisibleForTesting
  static IpSpace computeDeliveredToSubnet(
      String node,
      String iface,
      IpSpace arpFalseDestIp,
      Map<String, Map<String, IpSpace>> interfaceExternalArpIps,
      IpSpace ownedIps) {
    IpSpace ifaceExternalArpIps = interfaceExternalArpIps.get(node).get(iface);
    return AclIpSpace.difference(
        AclIpSpace.intersection(arpFalseDestIp, ifaceExternalArpIps), ownedIps);
  }

  private Map<String, Map<String, Map<String, IpSpace>>> getExitsNetwork() {
    return getInterfaceForwardingBehaviorIpSpaces(InterfaceForwardingBehavior::getExitsNetwork);
  }

  /**
   * Necessary and sufficient: The connected subnet is not full, the dest IP is external, and path
   * is not expected to come back into network (i.e. the ARP IP is also external).
   */
  static IpSpace computeExitsNetwork(
      String node,
      String iface,
      Map<String, Set<String>> interfacesWithMissingDevices,
      IpSpace dstIpsWithUnownedNextHopIpArpFalse,
      IpSpace arpFalseDstIp,
      IpSpace externalIps) {
    // the connected subnet is full
    if (!interfacesWithMissingDevices.get(node).contains(iface)) {
      return EmptyIpSpace.INSTANCE;
    }

    // Returns the union of the following 2 cases:
    // 1. Arp for dst ip and dst ip is external
    // 2. Arp for next hop ip, next hop ip is not owned by any interfaces,
    // and dst ip is external
    return AclIpSpace.intersection(
        // dest ip is external
        externalIps,
        // arp for dst Ip OR arp for external next-hop IP
        AclIpSpace.union(arpFalseDstIp, dstIpsWithUnownedNextHopIpArpFalse));
  }

  /**
   * Necessary and sufficient: The connected subnet is not full, and when arping for dst ip, dst ip
   * is internal but not in the interface subnet, when arping for next hop ip, either next hop ip is
   * owned by interfaces or dst ip is internal.
   *
   * @param interfaceExternalArpIps Set of IPs for which some external device (not modeled by
   *     Batfish) would reply to ARP in the real world.
   * @param interfacesWithMissingDevices Interfaces whose attached subnets are not full -- there may
   *     be other devices connected to the subnet for which we don't have a config.
   * @param arpFalseDestIp For each interface, dst IPs that can be ARP IPs and that we will not
   *     receive an ARP response for.
   * @param dstIpsWithUnownedNextHopIpArpFalse node -> vrf -> iface -> dst IPs the vrf forwards out
   *     the interface, ARPing for some unowned next-hop IP and not receiving a reply.
   * @param dstIpsWithOwnedNextHopIpArpFalse node -> vrf -> iface -> dst IPs the vrf forwards out
   *     the interface, ARPing for some owned next-hop IP and not receiving a reply.
   * @param internalIps IPs owned by devices in the snapshot or in connected subnets.
   */
  @VisibleForTesting
  static IpSpace computeInsufficientInfo(
      String node,
      String iface,
      Map<String, Map<String, IpSpace>> interfaceExternalArpIps,
      Map<String, Set<String>> interfacesWithMissingDevices,
      IpSpace arpFalseDestIp,
      IpSpace dstIpsWithUnownedNextHopIpArpFalse,
      IpSpace dstIpsWithOwnedNextHopIpArpFalse,
      IpSpace internalIps) {
    // If interface is full (no missing devices), it cannot be insufficient
    // info
    if (!interfacesWithMissingDevices.get(node).contains(iface)) {
      return EmptyIpSpace.INSTANCE;
    }

    IpSpace ipSpaceElsewhere =
        AclIpSpace.difference(internalIps, interfaceExternalArpIps.get(node).get(iface));

    return AclIpSpace.union(
        // case 1: arp for dst ip, dst ip is internal but not in any subnet of
        // the interface
        AclIpSpace.intersection(arpFalseDestIp, ipSpaceElsewhere),
        // case 2: arp for nhip, nhip is not owned by interfaces, dst ip is
        // internal
        AclIpSpace.intersection(dstIpsWithUnownedNextHopIpArpFalse, internalIps),
        // case 3: arp for nhip, nhip is owned by some interfaces
        dstIpsWithOwnedNextHopIpArpFalse);
  }

  /**
   * Necessary and sufficient: No ARP response, and either: 1. the interface is full, or 2. we ARPed
   * for a dest IP that is in a connected subnet and is owned in the snapshot.
   *
   * <p>An interface is full if all subnets connected to it are full.
   */
  static IpSpace computeNeighborUnreachable(
      String node,
      String iface,
      IpSpace arpFalse,
      Map<String, Set<String>> interfacesWithMissingDevices,
      IpSpace arpFalseDestIp,
      Map<String, Map<String, IpSpace>> interfaceExternalArpIps,
      IpSpace ownedIps) {
    return interfacesWithMissingDevices.get(node).contains(iface)
        ? AclIpSpace.intersection(
            arpFalseDestIp, interfaceExternalArpIps.get(node).get(iface), ownedIps)
        : arpFalse;
  }

  private Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachable() {
    return getInterfaceForwardingBehaviorIpSpaces(
        InterfaceForwardingBehavior::getNeighborUnreachable);
  }

  private Map<String, Map<String, Map<String, IpSpace>>> getInterfaceForwardingBehaviorIpSpaces(
      Function<InterfaceForwardingBehavior, IpSpace> ipSpaceGetter) {
    return toImmutableMap(
        _vrfForwardingBehavior,
        Entry::getKey, // node
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue(),
                Entry::getKey, // vrf
                vrfEntry ->
                    toImmutableMap(
                        vrfEntry.getValue().getInterfaceForwardingBehavior(),
                        Entry::getKey, // iface
                        ifaceEntry -> ipSpaceGetter.apply(ifaceEntry.getValue()))));
  }

  /** hostname -> interfaces that are not full. I.e. could have neighbors not present in snapshot */
  private static Map<String, Set<String>> computeInterfacesWithMissingDevices(
      Map<String, Map<String, BDD>> interfaceExternalArpIpBDDs, BDD unownedIpsBDD) {
    Span span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeInterfacesWithMissingDevices")
            .start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return toImmutableMap(
          interfaceExternalArpIpBDDs,
          Entry::getKey,
          nodeEntry ->
              nodeEntry.getValue().entrySet().stream()
                  .filter(ifaceEntry -> ifaceEntry.getValue().andSat(unownedIpsBDD))
                  .map(Entry::getKey)
                  .collect(ImmutableSet.toImmutableSet()));
    } finally {
      span.finish();
    }
  }

  private Map<String, Map<String, Map<String, IpSpace>>> getInsufficientInfo() {
    return getInterfaceForwardingBehaviorIpSpaces(InterfaceForwardingBehavior::getInsufficientInfo);
  }

  private static IpSpace computeDstIpsWithNextHopIpArpFalseFilter(
      String node,
      String vrf,
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Set<AbstractRoute> routesWithNextHopIpArpFalse,
      Predicate<AbstractRoute> routeFilter) {
    return computeRouteMatchConditionsFilter(
        routesWithNextHopIpArpFalse, matchingIps.get(node).get(vrf), routeFilter);
  }

  private static IpSpace computeOwnedIps(Map<String, Map<String, Set<Ip>>> interfaceOwnedIps) {
    Span span = GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeOwnedIps").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      return IpWildcardSetIpSpace.builder()
          .including(
              interfaceOwnedIps.values().stream()
                  .flatMap(ifaceMap -> ifaceMap.values().stream())
                  .flatMap(Collection::stream)
                  .map(IpWildcard::create)
                  .collect(Collectors.toList()))
          .build();
    } finally {
      span.finish();
    }
  }

  /**
   * Run sanity checks over the computed variables. Can be slow so only run in debug/assertion mode.
   */
  private boolean sanityCheck(
      IpSpaceToBDD ipSpaceToBDD, Map<String, Configuration> configurations) {
    // Sanity check internal properties.
    assertAllInterfacesActiveNodeInterface(_arpReplies, configurations);
    assertAllInterfacesActiveVrfForwardingBehavior(_vrfForwardingBehavior, configurations);

    // Sanity check public APIs.
    assertAllInterfacesActiveNodeInterface(getArpReplies(), configurations);
    assertAllInterfacesActiveVrfForwardingBehavior(getVrfForwardingBehavior(), configurations);

    // Sanity check traceroute-reachability different variables.
    Map<String, Map<String, Map<String, IpSpace>>> unionOthers =
        union(
            getNeighborUnreachable(),
            union(
                getInsufficientInfo(), //
                union(getDeliveredToSubnet(), getExitsNetwork())));
    assertDeepIpSpaceEquality(
        union(getNeighborUnreachable(), getInsufficientInfo()),
        union(getInsufficientInfo(), getNeighborUnreachable()),
        ipSpaceToBDD);

    Map<String, Map<String, Map<String, IpSpace>>> union1 =
        union(getNeighborUnreachable(), getInsufficientInfo());
    Map<String, Map<String, Map<String, IpSpace>>> union2 = union(union1, getDeliveredToSubnet());
    Map<String, Map<String, Map<String, IpSpace>>> union3 = union(union2, getExitsNetwork());
    assertDeepIpSpaceEquality(unionOthers, union3, ipSpaceToBDD);
    // TODO move this somewhere we can do it
    //    assertDeepIpSpaceEquality(_arpFalse, unionOthers, ipSpaceToBDD);

    return true;
  }
  /** Asserts that all interfaces in the given nested map are active in the given configurations. */
  private static void assertAllInterfacesActiveVrfForwardingBehavior(
      Map<String, Map<String, VrfForwardingBehavior>> vrfForwardingBehavior,
      Map<String, Configuration> configurations) {
    vrfForwardingBehavior.forEach(
        (node, vrfMap) ->
            vrfMap.forEach(
                (vrf, vfb) ->
                    vfb.getInterfaceForwardingBehavior()
                        .keySet()
                        .forEach(i -> assertInterfaceActive(node, i, configurations))));
  }

  /**
   * Asserts that all interfaces in the given nested map are inactive in the given configurations.
   */
  private static void assertAllInterfacesActiveNodeInterface(
      Map<String, Map<String, IpSpace>> nodeInterfaceMap,
      Map<String, Configuration> configurations) {
    nodeInterfaceMap.forEach(
        (node, ifaceMap) ->
            ifaceMap.keySet().forEach(i -> assertInterfaceActive(node, i, configurations)));
  }

  private static void assertInterfaceActive(
      String node, String i, Map<String, Configuration> configurations) {
    if (i.equals(Interface.NULL_INTERFACE_NAME)) {
      return;
    }
    Configuration c = configurations.get(node);
    assert c != null : node + " is null";
    Interface iface = c.getAllInterfaces().get(i);
    assert iface != null : node + "[" + i + "] is null";
    assert iface.getActive() : node + "[" + i + "] is not active";
  }

  /**
   * Asserts that all interfaces in the given nested map are inactive in the given configurations.
   */
  private static void assertDeepIpSpaceEquality(
      Map<String, Map<String, Map<String, IpSpace>>> left,
      Map<String, Map<String, Map<String, IpSpace>>> right,
      IpSpaceToBDD toBDD) {
    assert left.keySet().equals(right.keySet())
        : "Different node sets " + left.keySet() + " " + right.keySet();
    left.forEach(
        (node, vrfIfaceMap) -> {
          Map<String, Map<String, IpSpace>> rightVrfIfaceMap = right.get(node);
          assert vrfIfaceMap.keySet().equals(rightVrfIfaceMap.keySet())
              : "Different VRFs for node " + node;
          vrfIfaceMap.forEach(
              (vrf, ifaceMap) -> {
                Map<String, IpSpace> rightIfaceMap = rightVrfIfaceMap.get(vrf);
                assert vrfIfaceMap.keySet().equals(rightVrfIfaceMap.keySet())
                    : "Different interfaces node " + node + " VRF " + vrf;
                ifaceMap.forEach(
                    (iface, ipSpace) -> {
                      IpSpace rightIpSpace = rightIfaceMap.get(iface);
                      BDD bdd = toBDD.visit(ipSpace);
                      BDD rightBDD = toBDD.visit(rightIpSpace);
                      assert !bdd.diffSat(rightBDD)
                          : "Left BDDs larger for node "
                              + node
                              + " VRF "
                              + vrf
                              + " interface "
                              + iface;
                      assert !rightBDD.diffSat(bdd)
                          : "Right BDDs larger for node "
                              + node
                              + " VRF "
                              + vrf
                              + " interface "
                              + iface;
                    });
              });
        });
  }

  /** Mapping: route -&gt; nexthopinterface -&gt; resolved nextHopIp -&gt; interfaceRoutes */
  private static Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
      computeNextHopInterfaces(Fib fib) {
    return fib.allEntries().stream()
        .filter(fibEntry -> fibEntry.getAction() instanceof FibForward)
        .collect(
            Collectors.groupingBy(
                FibEntry::getTopLevelRoute,
                Collectors.groupingBy(
                    fibEntry -> ((FibForward) fibEntry.getAction()).getInterfaceName(),
                    Collectors.groupingBy(
                        fibEntry -> ((FibForward) fibEntry.getAction()).getArpIp(),
                        Collectors.mapping(FibEntry::getResolvedToRoute, Collectors.toSet())))));
  }
}
