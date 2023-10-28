package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;

/** Implementation of {@link ForwardingAnalysis}. */
public final class ForwardingAnalysisImpl implements ForwardingAnalysis, Serializable {
  private static final Logger LOGGER = LogManager.getLogger(ForwardingAnalysisImpl.class);
  // node -> interface -> ips that the interface would reply arp request
  private final Map<String, Map<String, IpSpace>> _arpReplies;

  // node -> vrf -> forwarding behavior for that VRF.
  private final Map<String, Map<String, VrfForwardingBehavior>> _vrfForwardingBehavior;

  /** Helper function to materialize in random order the list of keys in Map of Maps. */
  @VisibleForTesting
  static @Nonnull <T> List<Map.Entry<String, String>> sparseKeys(Map<String, Map<String, T>> map) {
    List<Map.Entry<String, String>> sparseKeys =
        map.entrySet().parallelStream()
            .flatMap(
                e ->
                    e.getValue().entrySet().parallelStream()
                        .map(v -> new SimpleImmutableEntry<>(e.getKey(), v.getKey())))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.shuffle(sparseKeys);
    return ImmutableList.copyOf(sparseKeys);
  }

  public ForwardingAnalysisImpl(
      Map<String, Configuration> configurations,
      Map<String, Map<String, Fib>> fibs,
      Topology topology,
      Map<Location, LocationInfo> locationInfo,
      IpOwners ipOwners) {
    List<Map.Entry<String, String>> allVrfs = sparseKeys(fibs);

    // TODO accept IpSpaceToBDD as parameter to reuse work when we build forwarding analysis
    // multiple times.
    IpSpaceToBDD ipSpaceToBDD = new BDDPacket().getDstIpSpaceToBDD();

    LOGGER.info("Computing owned and unowned IPs");
    // IPs belonging to any interface in the network, even inactive interfaces
    // node -> interface -> IPs owned by that interface
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps = ipOwners.getInterfaceOwners(false);

    // Owned (i.e., internal to the network) IPs
    IpSpace ownedIps = computeOwnedIps(interfaceOwnedIps);
    // Unowned (i.e., external to the network) IPs
    BDD unownedIpsBDD = ipSpaceToBDD.visit(ownedIps).not();

    // ARP ips not belonging to any subnet in the network
    Set<Ip> unownedArpIps = computeUnownedArpIps(fibs, ipSpaceToBDD, unownedIpsBDD);

    LOGGER.info("Aggregating information about routing entries");
    // IpSpaces matched by each prefix
    // -- only will have entries for active interfaces if FIB is correct
    Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps = computeMatchingIps(fibs, allVrfs);
    // Set of routes that forward out each interface
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop =
        computeRoutesWithNextHop(fibs, allVrfs);
    // Node -> vrf -> destination IPs that can be routed
    Map<String, Map<String, IpSpace>> routableIps = computeRoutableIps(fibs, allVrfs);

    /* Compute _arpReplies: for each interface, the set of arp IPs for which that interface will
     * respond.
     */
    {
      // mapping: node name -> vrf name -> interface name -> dst ips which are routed to the
      // interface. Should only include active interfaces.
      LOGGER.info("Computing IPs routed out interfaces");
      Map<String, Map<String, Map<String, IpSpace>>> ipsRoutedOutInterfaces =
          computeIpsRoutedOutInterfaces(matchingIps, routesWithNextHop, allVrfs);
      LOGGER.info("Computing ARP replies");
      _arpReplies =
          computeArpReplies(configurations, ipsRoutedOutInterfaces, interfaceOwnedIps, routableIps);
    }

    // hostname -> interfaces that are not full. I.e. could have neighbors not present in snapshot
    LOGGER.info("Computing interfaces with missing devices");
    Multimap<String, String> interfacesWithMissingDevices =
        computeInterfacesWithMissingDevices(locationInfo, ipSpaceToBDD, unownedIpsBDD);

    // ips belonging to any subnet in the network, including inactive interfaces.
    LOGGER.info("Computing internal IPs");
    IpSpace internalIps = computeInternalIps(ipOwners.getAllInterfaceHostIps());

    // ips not belonging to any subnet in the network, including inactive interfaces.
    IpSpace externalIps = internalIps.complement();

    // Compute VrfForwardingBehavior, parallelizing across all VRFs.
    LOGGER.info("Computing VRF forwarding behavior for {} VRFs", allVrfs.size());
    AtomicInteger done = new AtomicInteger();
    _vrfForwardingBehavior =
        allVrfs.parallelStream()
            .collect(
                ImmutableTable.toImmutableTable(
                    Entry::getKey,
                    Entry::getValue,
                    e -> {
                      String node = e.getKey();
                      String vrf = e.getValue();
                      VrfForwardingBehavior ret =
                          computeVrfForwardingBehavior(
                              node,
                              vrf,
                              topology,
                              locationInfo,
                              ipSpaceToBDD,
                              ipOwners,
                              fibs.get(node).get(vrf),
                              unownedArpIps,
                              matchingIps.get(node).get(vrf),
                              ownedIps,
                              interfacesWithMissingDevices,
                              internalIps,
                              externalIps,
                              routableIps,
                              routesWithNextHop.get(node).get(vrf));
                      int processed = done.incrementAndGet();
                      if (processed % 100 == 0) {
                        LOGGER.info(
                            "Computed VRF forwarding behavior for {}/{} vrfs",
                            processed,
                            allVrfs.size());
                      }
                      return ret;
                    }))
            .rowMap();
    LOGGER.info("Done computing VRF forwarding behavior for {} devices", configurations.size());

    assert sanityCheck(configurations);
  }

  private VrfForwardingBehavior computeVrfForwardingBehavior(
      String node,
      String vrf,
      Topology topology,
      Map<Location, LocationInfo> locationInfo,
      // Note: BDDs are not thread-safe and this method is run in parallel.
      // all uses must synchronize on the BDDFactory within.
      // this is here only for sanity checking (only with assertions enabled)
      IpSpaceToBDD ipSpaceToBDDUnsafeDoNotUse,
      IpOwners ipOwners,
      Fib fib,
      Set<Ip> unownedArpIps,
      Map<Prefix, IpSpace> matchingIps,
      IpSpace ownedIps,
      Multimap<String, String> interfacesWithMissingDevices,
      IpSpace internalIps,
      IpSpace externalIps,
      Map<String, Map<String, IpSpace>> routableIps,
      Map<String, Set<AbstractRoute>> routesWithNextHop) {
    Map<String, IpSpace> accepted =
        ipOwners
            .getVrfIfaceOwnedIpSpaces()
            .getOrDefault(node, ImmutableMap.of())
            .getOrDefault(vrf, ImmutableMap.of());
    /*
     * Mapping: route -> nexthopinterface -> resolved nextHopIps (where Optional.empty() indicates
     *                                       dest IP should be used for ARP)
     */
    Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces =
        computeNextHopInterfaces(fib);

    /* interface -> set of routes on that vrf that forward out that interface,
     * ARPing for the destination IP
     */
    Map<String, Set<AbstractRoute>> routesWhereDstIpCanBeArpIp =
        computeRoutesWhereDstIpCanBeArpIp(nextHopInterfaces, routesWithNextHop);

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
        computeArpTrueEdgeDestIp(matchingIps, routesWithDestIpEdge, _arpReplies);

    /* edge -> routes for which an arp true response will be received when they are the lpm of
     * the dest IP
     *
     * Note: the source interface of the edge must be in the node, but may not be in the vrf,
     * due to route leaking, etc
     */
    Map<Edge, Set<AbstractRoute>> routesWithNextHopIpArpTrue =
        computeRoutesWithNextHopIpArpTrue(
            node, nextHopInterfaces, topology, _arpReplies, routesWithNextHop);

    /* edge -> dst ips for which this vrf forwards out the source of the edge,
     * ARPing for some next-hop IP and receiving a reply from the target of the edge.
     *
     * Note: the source interface of the edge must be in the node, but may not be in the vrf,
     * due to route leaking, etc
     */
    Map<Edge, IpSpace> arpTrueEdgeNextHopIp =
        computeArpTrueEdgeNextHopIp(matchingIps, routesWithNextHopIpArpTrue);

    Map<Edge, IpSpace> arpTrueEdge = computeArpTrueEdge(arpTrueEdgeDestIp, arpTrueEdgeNextHopIp);

    Map<String, InterfaceForwardingBehavior> interfaceForwardingBehavior =
        toImmutableMap(
            // routesWithNextHop may include interfaces in other VRFs that we
            // forward out through due to VRF leaking. All active interfaces
            // in this VRF are included due to local routes.
            routesWithNextHop.keySet(),
            Function.identity(),
            iface -> {
              IpSpace externalArpIps =
                  locationInfo
                      .getOrDefault(new InterfaceLinkLocation(node, iface), LocationInfo.NOTHING)
                      .getArpIps();

              /* Compute ARP stuff bottom-up from _arpReplies. */
              IpSpace someoneReplies = computeSomeoneReplies(node, iface, topology, _arpReplies);

              /* set of routes on that vrf that forward out that interface
               * with a next hop ip that gets no arp replies
               */
              Set<AbstractRoute> arpFalseNhipRoutes =
                  computeArpFalseNhipRoutes(
                      iface, nextHopInterfaces, routesWithNextHop.get(iface), someoneReplies);

              /* dst IPs for which this VRF forwards out that interface, ARPing
               * for the dst ip itself with no reply
               */
              IpSpace arpFalseDestIp =
                  computeArpFalseDestIp(
                      matchingIps, routesWhereDstIpCanBeArpIp.get(iface), someoneReplies);

              /* dst ips for which this vrf forwards out that interface,
               * ARPing for a next-hop IP and receiving no reply
               */
              IpSpace arpFalseNextHopIp = computeArpFalseNextHopIp(matchingIps, arpFalseNhipRoutes);

              IpSpace arpFalse = AclIpSpace.union(arpFalseDestIp, arpFalseNextHopIp);

              // Of the routes that ARP for a next-hop IP and don't receive a response,
              // determine which ARP for an owned IP, and which ARP for an unowned IP.
              // Note: Due to ECMP during resolution, these sets are not necessarily disjoint.
              List<AbstractRoute> arpFalseNhipRoutesWithUnownedArpIp = new ArrayList<>();
              List<AbstractRoute> arpFalseNhipRoutesWithOwnedArpIp = new ArrayList<>();
              classifyArpFalseNhipRoutes(
                  unownedArpIps,
                  nextHopInterfaces,
                  arpFalseNhipRoutes,
                  arpFalseNhipRoutesWithUnownedArpIp::add,
                  arpFalseNhipRoutesWithOwnedArpIp::add);

              /* dst IPs for which that VRF forwards out that interface, ARPing
               * for some unowned next-hop IP with no reply
               */
              IpSpace dstIpsWithUnownedNextHopIpArpFalse =
                  computeRouteMatchConditions(arpFalseNhipRoutesWithUnownedArpIp, matchingIps);

              /* dst IPs for which that VRF forwards out that interface, ARPing
               * for some owned next-hop IP with no reply.
               */
              IpSpace dstIpsWithOwnedNextHopIpArpFalse =
                  computeRouteMatchConditions(arpFalseNhipRoutesWithOwnedArpIp, matchingIps);

              IpSpace deliveredToSubnet =
                  computeDeliveredToSubnet(arpFalseDestIp, externalArpIps, ownedIps);

              boolean hasMissingDevices = interfacesWithMissingDevices.containsEntry(node, iface);

              IpSpace exitsNetwork =
                  computeExitsNetwork(
                      hasMissingDevices,
                      dstIpsWithUnownedNextHopIpArpFalse,
                      arpFalseDestIp,
                      externalIps);

              IpSpace insufficientInfo =
                  computeInsufficientInfo(
                      externalArpIps,
                      hasMissingDevices,
                      arpFalseDestIp,
                      dstIpsWithUnownedNextHopIpArpFalse,
                      dstIpsWithOwnedNextHopIpArpFalse,
                      internalIps);

              IpSpace neighborUnreachable =
                  computeNeighborUnreachable(
                      arpFalse, hasMissingDevices, arpFalseDestIp, externalArpIps, ownedIps);

              InterfaceForwardingBehavior ifb =
                  InterfaceForwardingBehavior.builder()
                      .setAccepted(accepted.get(iface))
                      .setDeliveredToSubnet(deliveredToSubnet)
                      .setExitsNetwork(exitsNetwork)
                      .setInsufficientInfo(insufficientInfo)
                      .setNeighborUnreachable(neighborUnreachable)
                      .build();

              assert sanityCheckInterfaceForwardingBehavior(
                  node, vrf, iface, ipSpaceToBDDUnsafeDoNotUse, arpFalse, ifb);
              return ifb;
            });

    // destination IPs that will be null routes
    IpSpace nullRoutedIps = computeNullRoutedIps(matchingIps, fib);

    // nextVrf -> dest IPs that vrf delegates to nextVrf
    Map<String, IpSpace> nextVrfIps = computeNextVrfIps(matchingIps, fib);

    return VrfForwardingBehavior.builder()
        .setArpTrueEdge(arpTrueEdge)
        .setInterfaceForwardingBehavior(interfaceForwardingBehavior)
        .setNextVrf(nextVrfIps)
        .setNullRoutedIps(nullRoutedIps)
        .setRoutableIps(routableIps.get(node).get(vrf))
        .build();
  }

  /**
   * Of the routes that ARP for a next-hop IP and don't receive a response, determine which ARP for
   * an owned IP, and which ARP for an unowned IP. Note: Due to ECMP during resolution, both may be
   * true for a single route.
   */
  private static void classifyArpFalseNhipRoutes(
      Set<Ip> unownedArpIps,
      Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces,
      Set<AbstractRoute> routesWithNextHopIpArpFalse,
      Consumer<AbstractRoute> arpFalseNhipRoutesWithUnownedArpIp,
      Consumer<AbstractRoute> arpFalseNhipRoutesWithOwnedArpIp) {
    routesWithNextHopIpArpFalse.forEach(
        route -> {
          // Iterate over the arpIps for this route, checking whether each is owned. Stop once
          // we've found one owned and one unowned.
          Iterator<Ip> it =
              nextHopInterfaces.get(route).values().stream()
                  .flatMap(Set::stream)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .iterator();
          boolean foundUnowned = false;
          boolean foundOwned = false;
          while (it.hasNext() && !(foundUnowned && foundOwned)) {
            Ip arpIp = it.next();
            if (!foundUnowned && unownedArpIps.contains(arpIp)) {
              foundUnowned = true;
              arpFalseNhipRoutesWithUnownedArpIp.accept(route);
            } else if (!foundOwned) {
              foundOwned = true;
              arpFalseNhipRoutesWithOwnedArpIp.accept(route);
            }
          }
        });
  }

  private static Set<Ip> computeUnownedArpIps(
      Map<String, Map<String, Fib>> fibs, IpSpaceToBDD ipSpaceToBDD, BDD unownedIpsBDD) {
    return fibs.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .map(Fib::allEntries)
        .flatMap(Collection::stream)
        .map(FibEntry::getAction)
        .filter(FibForward.class::isInstance)
        .map(fibAction -> ((FibForward) fibAction).getArpIp())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .distinct()
        .filter(arpIp -> ipSpaceToBDD.toBDD(arpIp).andSat(unownedIpsBDD))
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Compute an IP address ACL for each interface of each node permitting only those IPs for which
   * the node would send out an ARP reply on that interface: <br>
   * <br>
   * 1) PERMIT IPs belonging to the interface.<br>
   * 2) PERMIT any statically configured arp IPs.<br>
   * 3) (Proxy-ARP) PERMIT any other owned IPs of the VRF of the interface.<br>
   * 4) (Proxy-ARP) DENY any IP for which there is a longest-prefix match entry in the FIB that goes
   * through the interface.<br>
   * 5) (Proxy-ARP) PERMIT any other IP routable via the VRF of the interface.
   */
  @VisibleForTesting
  static @Nonnull Map<String, Map<String, IpSpace>> computeArpReplies(
      // node -> configuration
      Map<String, Configuration> configurations,
      // node -> vrf -> interface -> ipsRoutedOutInterface
      Map<String, Map<String, Map<String, IpSpace>>> ipsRoutedOutInterfaces,
      // node -> interface -> ownedIps
      Map<String, Map<String, Set<Ip>>> interfaceOwnedIps,
      // node -> vrf -> routable IPs
      Map<String, Map<String, IpSpace>> routableIps) {
    return toImmutableMap(
        configurations,
        Entry::getKey,
        nodeEntry -> {
          String hostname = nodeEntry.getKey();
          Configuration c = nodeEntry.getValue();
          // vrf -> ownedIps
          Map<String, IpSpace> ownedIpsByVrf =
              computeOwnedIpsByVrf(
                  c.getActiveInterfaces(),
                  interfaceOwnedIps.getOrDefault(hostname, ImmutableMap.of()));
          return computeArpRepliesByInterface(
              c.getActiveInterfaces(),
              routableIps.get(hostname),
              ipsRoutedOutInterfaces.get(hostname),
              interfaceOwnedIps,
              ownedIpsByVrf);
        });
  }

  /**
   * Returns a mapping from VRF name to the union of all IPs owned by all interfaces in that VRF.
   */
  @VisibleForTesting
  static @Nonnull Map<String, IpSpace> computeOwnedIpsByVrf(
      // interface name -> interface
      Map<String, Interface> activeInterfaces,
      // interface -> owned IPs
      Map<String, Set<Ip>> interfaceOwnedIps) {
    Map<String, Set<Ip>> ipsByVrf = new HashMap<>();
    activeInterfaces.forEach(
        (ifaceName, iface) -> {
          ipsByVrf
              .computeIfAbsent(iface.getVrfName(), v -> new HashSet<>())
              .addAll(interfaceOwnedIps.getOrDefault(ifaceName, ImmutableSet.of()));
        });
    return ipsByVrf.entrySet().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* vrfName */,
                ipsByVrfEntry -> ipSetToIpSpace(ipsByVrfEntry.getValue() /* ipsOwnedByVrf */)));
  }

  @VisibleForTesting
  static @Nonnull Map<String, IpSpace> computeArpRepliesByInterface(
      // interface name -> interface
      Map<String, Interface> interfaces,
      // vrf -> routable IPs
      Map<String, IpSpace> routableIpsByVrf,
      // vrf -> interface -> ipsRoutedOutInterface
      Map<String, Map<String, IpSpace>> ipsRoutedOutInterfaces,
      // node -> interface -> ownedIps
      Map<String, Map<String, Set<Ip>>> interfaceOwnedIps,
      // vrf -> ownedIps
      Map<String, IpSpace> ownedIpsByVrf) {
    return toImmutableMap(
        interfaces,
        Entry::getKey,
        ifaceEntry -> {
          String ifaceName = ifaceEntry.getKey();
          Interface iface = ifaceEntry.getValue();
          String vrfName = ifaceEntry.getValue().getVrfName();
          return computeInterfaceArpReplies(
              iface,
              /* We believe at this time that an interface would send an ARP reply only based
               * on the routes in it's own VRF.
               * This type of routing separation is the point of VRFs, and cross-VRF introspection
               * for the purposes of ARP replies is unlikely to happen by default.
               */
              routableIpsByVrf.get(vrfName),
              ipsRoutedOutInterfaces.get(vrfName).getOrDefault(ifaceName, EmptyIpSpace.INSTANCE),
              interfaceOwnedIps,
              ownedIpsByVrf.get(vrfName));
        });
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
      Map<Prefix, IpSpace> matchingIps,
      Map<Edge, Set<AbstractRoute>> routesWithDestIpEdge,
      Map<String, Map<String, IpSpace>> arpReplies) {
    return toImmutableMap(
        routesWithDestIpEdge,
        Entry::getKey, // edge
        edgeEntry -> {
          Edge edge = edgeEntry.getKey();
          Set<AbstractRoute> routes = edgeEntry.getValue();
          IpSpace dstIpMatchesSomeRoutePrefix = computeRouteMatchConditions(routes, matchingIps);
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
      Map<Prefix, IpSpace> matchingIps, Map<Edge, Set<AbstractRoute>> routesWithNextHopIpArpTrue) {
    return toImmutableMap(
        routesWithNextHopIpArpTrue,
        Entry::getKey, // edge
        edgeEntry -> computeRouteMatchConditions(edgeEntry.getValue(), matchingIps));
  }

  @VisibleForTesting
  static @Nonnull IpSpace computeInterfaceArpReplies(
      @Nonnull Interface iface,
      @Nonnull IpSpace routableIpsForThisVrf,
      @Nonnull IpSpace ipsRoutedThroughInterface,
      @Nonnull Map<String, Map<String, Set<Ip>>> interfaceOwnedIps,
      @Nonnull IpSpace vrfOwnedIps) {
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
      /* Accept all vrf-owned IPs */
      // TODO: There may be room for optimization, since this generally overlaps with both IPs owned
      //       by this interface as well as IPs routable for this VRF.
      interfaceArpReplies.thenPermitting(vrfOwnedIps);

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
    Set<LinkLocalAddress> linkLocalAddresses = iface.getAllLinkLocalAddresses();
    if (concreteIps.isEmpty() && linkLocalAddresses.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }
    Set<Ip> linkLocalIps =
        linkLocalAddresses.stream().map(LinkLocalAddress::getIp).collect(Collectors.toSet());
    Set<Ip> allIps = Sets.union(concreteIps, linkLocalIps);
    return ipSetToIpSpace(allIps);
  }

  private static @Nonnull IpSpace ipSetToIpSpace(Set<Ip> ips) {
    if (ips.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }
    if (ips.size() == 1) {
      return ips.iterator().next().toIpSpace();
    }
    Set<IpWildcard> wildcards =
        ips.stream().map(IpWildcard::create).collect(ImmutableSet.toImmutableSet());
    return IpWildcardSetIpSpace.create(ImmutableSet.of(), wildcards);
  }

  @VisibleForTesting
  static Map<String, Map<String, Map<String, IpSpace>>> computeIpsRoutedOutInterfaces(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop,
      List<Map.Entry<String, String>> allVrfs) {
    return allVrfs.parallelStream()
        .collect(
            ImmutableTable.toImmutableTable(
                Entry::getKey,
                Entry::getValue,
                e -> {
                  String hostname = e.getKey();
                  String vrf = e.getValue();
                  Map<Prefix, IpSpace> vrfMatchingIps = matchingIps.get(hostname).get(vrf);
                  return (Map<String, IpSpace>)
                      routesWithNextHop.get(hostname).get(vrf).entrySet().stream()
                          /*
                           *  Cannot determine IPs for null interface here because it is
                           *  not tied to a single VRF.
                           */
                          .filter(
                              ifaceEntry ->
                                  !ifaceEntry.getKey().equals(Interface.NULL_INTERFACE_NAME))
                          .map(
                              ifaceEntry -> {
                                String iface = ifaceEntry.getKey();
                                Set<AbstractRoute> routes = ifaceEntry.getValue();
                                return Maps.immutableEntry(
                                    iface, computeRouteMatchConditions(routes, vrfMatchingIps));
                              })
                          .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
                }))
        .rowMap();
  }

  @VisibleForTesting
  static IpSpace computeArpFalseDestIp(
      Map<Prefix, IpSpace> matchingIps,
      Set<AbstractRoute> routesWhereDstIpCanBeArpIp,
      IpSpace someoneReplies) {
    IpSpace ipsRoutedOutInterface =
        computeRouteMatchConditions(routesWhereDstIpCanBeArpIp, matchingIps);
    return AclIpSpace.rejecting(someoneReplies).thenPermitting(ipsRoutedOutInterface).build();
  }

  @VisibleForTesting
  static IpSpace computeArpFalseNextHopIp(
      Map<Prefix, IpSpace> matchingIps, Set<AbstractRoute> routesWithNextHopIpArpFalse) {
    return computeRouteMatchConditions(routesWithNextHopIpArpFalse, matchingIps);
  }

  @VisibleForTesting
  static IpSpace computeNullRoutedIps(Map<Prefix, IpSpace> matchingIps, Fib fib) {
    Set<AbstractRoute> nullRoutes =
        fib.allEntries().stream()
            .filter(fibEntry -> fibEntry.getAction() instanceof FibNullRoute)
            .map(FibEntry::getTopLevelRoute)
            .collect(ImmutableSet.toImmutableSet());
    return computeRouteMatchConditions(nullRoutes, matchingIps);
  }

  @VisibleForTesting
  static Map<String, IpSpace> computeNextVrfIps(Map<Prefix, IpSpace> matchingIps, Fib fib) {
    return computeNextVrfIps(fib, matchingIps);
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
  static IpSpace routableSpace(Fib fib) {
    Set<FibEntry> entries = fib.allEntries();
    if (entries.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }

    // Order prefixes by length (shorter first), enabling us to build
    // the simplest routable space.
    List<Prefix> routablePrefixes =
        entries.stream()
            .map(e -> e.getTopLevelRoute().getNetwork())
            .distinct()
            .sorted()
            .collect(Collectors.toList());

    PrefixSpace seen = new PrefixSpace();
    List<Prefix> routable = new LinkedList<>();
    for (Prefix network : routablePrefixes) {
      if (network.equals(Prefix.ZERO)) {
        // Default route -> all IPs are routable. Skip processing the rest.
        return UniverseIpSpace.INSTANCE;
      } else if (!seen.containsPrefix(network)) {
        routable.add(network);
        seen.addPrefixRange(PrefixRange.sameAsOrMoreSpecificThan(network));
      } // else skip prefix already contained in output space
    }

    if (routable.size() == 1) {
      return routable.get(0).toIpSpace();
    }

    return IpWildcardSetIpSpace.create(
        ImmutableSet.of(),
        routable.stream().map(IpWildcard::create).collect(ImmutableSet.toImmutableSet()));
  }

  @VisibleForTesting
  static Map<String, Map<String, IpSpace>> computeRoutableIps(
      Map<String, Map<String, Fib>> fibs, List<Map.Entry<String, String>> allVrfs) {
    return allVrfs.parallelStream()
        .collect(
            ImmutableTable.toImmutableTable(
                Entry::getKey,
                Entry::getValue,
                e -> routableSpace(fibs.get(e.getKey()).get(e.getValue()))))
        .rowMap();
  }

  static Map<String, Map<String, Map<Prefix, IpSpace>>> computeMatchingIps(
      Map<String, Map<String, Fib>> fibs, List<Entry<String, String>> allVrfs) {
    return allVrfs.parallelStream()
        .collect(
            ImmutableTable.toImmutableTable(
                Entry::getKey,
                Entry::getValue,
                e -> fibs.get(e.getKey()).get(e.getValue()).getMatchingIps()))
        .rowMap();
  }

  @VisibleForTesting
  static IpSpace computeRouteMatchConditions(
      Collection<AbstractRoute> routes, Map<Prefix, IpSpace> matchingIps) {
    // get the union of IpSpace that match one of the routes
    // get the union of IpSpace that match one of the routes
    return firstNonNull(
        AclIpSpace.union(
            routes.stream()
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
      Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces,
      Map<String, Set<AbstractRoute>> routesWithNextHop) {
    return toImmutableMap(
        routesWithNextHop,
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
                          .contains(Optional.<Ip>empty()))
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
      Map<String, Map<String, Fib>> fibs, List<Map.Entry<String, String>> allVrfs) {
    return allVrfs.parallelStream()
        .collect(
            ImmutableTable.toImmutableTable(
                Entry::getKey,
                Entry::getValue,
                e ->
                    fibs.get(e.getKey()).get(e.getValue()).allEntries().stream()
                        .filter(fibEntry -> fibEntry.getAction() instanceof FibForward)
                        .collect(
                            Collectors.groupingBy(
                                fibEntry -> ((FibForward) fibEntry.getAction()).getInterfaceName(),
                                Collectors.mapping(
                                    FibEntry::getTopLevelRoute, Collectors.toSet())))))
        .rowMap();
  }

  @VisibleForTesting
  static Set<AbstractRoute> computeArpFalseNhipRoutes(
      String iface,
      Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces,
      Set<AbstractRoute> routesWithNextHop,
      IpSpace someoneReplies) {
    return computeRoutesWithNextHopIpArpFalseForInterface(
        nextHopInterfaces, iface, routesWithNextHop, someoneReplies);
  }

  @VisibleForTesting
  static Set<AbstractRoute> computeRoutesWithNextHopIpArpFalseForInterface(
      Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces,
      String outInterface,
      Set<AbstractRoute> candidateRoutes,
      IpSpace someoneReplies) {
    return candidateRoutes.stream()
        .filter(
            candidateRoute ->
                nextHopInterfaces.get(candidateRoute).get(outInterface).stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .anyMatch(
                        nextHopIp -> !someoneReplies.containsIp(nextHopIp, ImmutableMap.of())))
        .collect(ImmutableSet.toImmutableSet());
  }

  @VisibleForTesting
  static @Nonnull Map<Edge, Set<AbstractRoute>> computeRoutesWithNextHopIpArpTrue(
      String node,
      Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces,
      Topology topology,
      Map<String, Map<String, IpSpace>> arpReplies,
      Map<String, Set<AbstractRoute>> routesWithNextHop) {
    return routesWithNextHop.entrySet().stream()
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
                                            .get(outInterface) // nextHopIps
                                            .stream()
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
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

  @Override
  public @Nonnull Map<String, Map<String, VrfForwardingBehavior>> getVrfForwardingBehavior() {
    return _vrfForwardingBehavior;
  }

  static Map<String, Map<String, Map<String, IpSpace>>> union(
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces1,
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces2) {
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
  }

  private static IpSpace computeInternalIps(
      Map<String, Map<String, IpSpace>> interfaceHostSubnetIps) {
    return firstNonNull(
        AclIpSpace.union(
            interfaceHostSubnetIps.values().parallelStream()
                .flatMap(ifaceSubnetIps -> ifaceSubnetIps.values().parallelStream())
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }

  /*
   * Necessary and sufficient: Arping dst ip and the dst IP is not owned but is in an interface
   * subnet.
   */
  @VisibleForTesting
  static IpSpace computeDeliveredToSubnet(
      IpSpace arpFalseDestIp, IpSpace externalArpIps, IpSpace ownedIps) {
    return AclIpSpace.difference(AclIpSpace.intersection(arpFalseDestIp, externalArpIps), ownedIps);
  }

  /**
   * Necessary and sufficient: The connected subnet is not full, the dest IP is external, and path
   * is not expected to come back into network (i.e. the ARP IP is also external).
   */
  static IpSpace computeExitsNetwork(
      boolean hasMissingDevices,
      IpSpace dstIpsWithUnownedNextHopIpArpFalse,
      IpSpace arpFalseDstIp,
      IpSpace externalIps) {
    // the connected subnet is full
    if (!hasMissingDevices) {
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
   * @param externalArpIps Set of IPs for which some external device (not modeled by Batfish) would
   *     reply to ARP in the real world.
   * @param hasMissingDevices Interfaces whose attached subnets are not full -- there may be other
   *     devices connected to the subnet for which we don't have a config.
   * @param arpFalseDestIp For each interface, dst IPs that can be ARP IPs and that we will not
   *     receive an ARP response for.
   * @param dstIpsWithUnownedNextHopIpArpFalse node -> vrf -> iface -> dst IPs the vrf forwards out
   *     the interface, ARPing for some unowned next-hop IP and not receiving a reply.
   * @param dstIpsWithOwnedNextHopIpArpFalse node -> vrf -> iface -> dst IPs the vrf forwards out
   *     the interface, ARPing for some owned next-hop IP and not receiving a reply.
   * @param internalIps IPs owned by devices in the snapshot or in connected subnets.
   */
  @VisibleForTesting
  static @Nonnull IpSpace computeInsufficientInfo(
      IpSpace externalArpIps,
      boolean hasMissingDevices,
      IpSpace arpFalseDestIp,
      IpSpace dstIpsWithUnownedNextHopIpArpFalse,
      IpSpace dstIpsWithOwnedNextHopIpArpFalse,
      IpSpace internalIps) {
    // If interface is full (no missing devices), it cannot be insufficient
    // info
    if (!hasMissingDevices) {
      return EmptyIpSpace.INSTANCE;
    }

    IpSpace ipSpaceElsewhere = AclIpSpace.difference(internalIps, externalArpIps);

    return firstNonNull(
        AclIpSpace.union(
            // case 1: arp for dst ip, dst ip is internal but not in any subnet of
            // the interface
            AclIpSpace.intersection(arpFalseDestIp, ipSpaceElsewhere),
            // case 2: arp for nhip, nhip is not owned by interfaces, dst ip is
            // internal
            AclIpSpace.intersection(dstIpsWithUnownedNextHopIpArpFalse, internalIps),
            // case 3: arp for nhip, nhip is owned by some interfaces
            dstIpsWithOwnedNextHopIpArpFalse),
        EmptyIpSpace.INSTANCE);
  }

  /**
   * Necessary and sufficient: No ARP response, and either: 1. the interface is full, or 2. we ARPed
   * for a dest IP that is in a connected subnet and is owned in the snapshot.
   *
   * <p>An interface is full if all subnets connected to it are full.
   */
  static @Nonnull IpSpace computeNeighborUnreachable(
      IpSpace arpFalse,
      boolean hasMissingDevices,
      IpSpace arpFalseDestIp,
      IpSpace externalArpIps,
      IpSpace ownedIps) {
    return hasMissingDevices
        ? firstNonNull(
            AclIpSpace.intersection(arpFalseDestIp, externalArpIps, ownedIps),
            EmptyIpSpace.INSTANCE)
        : arpFalse;
  }

  /** hostname -> interfaces that are not full. I.e. could have neighbors not present in snapshot */
  private static Multimap<String, String> computeInterfacesWithMissingDevices(
      Map<Location, LocationInfo> locationInfo, IpSpaceToBDD toBdd, BDD unownedIpsBDD) {
    ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
    locationInfo.forEach(
        (location, info) -> {
          if (!(location instanceof InterfaceLinkLocation)) {
            return;
          }
          if (toBdd.visit(info.getArpIps()).andSat(unownedIpsBDD)) {
            builder.put(
                location.getNodeName(), ((InterfaceLinkLocation) location).getInterfaceName());
          }
        });
    return builder.build();
  }

  private static IpSpace computeOwnedIps(Map<String, Map<String, Set<Ip>>> interfaceOwnedIps) {
    return IpWildcardSetIpSpace.builder()
        .including(
            interfaceOwnedIps.values().stream()
                .flatMap(ifaceMap -> ifaceMap.values().stream())
                .flatMap(Collection::stream)
                .map(IpWildcard::create)
                .collect(Collectors.toList()))
        .build();
  }

  /**
   * Run sanity checks over the computed variables. Can be slow so only run in debug/assertion mode.
   */
  private boolean sanityCheck(Map<String, Configuration> configurations) {
    LOGGER.info("Running expensive sanity checks");
    // Sanity check internal properties.
    assertAllInterfacesActiveNodeInterface(_arpReplies, configurations);
    assertAllInterfacesActiveVrfForwardingBehavior(_vrfForwardingBehavior, configurations);

    // Sanity check public APIs.
    assertAllInterfacesActiveNodeInterface(getArpReplies(), configurations);
    assertAllInterfacesActiveVrfForwardingBehavior(getVrfForwardingBehavior(), configurations);
    return true;
  }

  private boolean sanityCheckInterfaceForwardingBehavior(
      String node,
      String vrf,
      String iface,
      IpSpaceToBDD ipSpaceToBDD,
      IpSpace arpFalse,
      InterfaceForwardingBehavior ifb) {
    synchronized (ipSpaceToBDD.getBDDInteger().getFactory()) {
      BDD arpFalseBdd = ipSpaceToBDD.visit(arpFalse);
      BDD dispositionUnionBdd =
          ipSpaceToBDD.visit(
              firstNonNull(
                  AclIpSpace.union(
                      ifb.getDeliveredToSubnet(),
                      ifb.getExitsNetwork(),
                      ifb.getInsufficientInfo(),
                      ifb.getNeighborUnreachable()),
                  EmptyIpSpace.INSTANCE));
      assert !arpFalseBdd.diffSat(dispositionUnionBdd)
          : "arpFalseBdd larger than dispositionUnionBdd for node "
              + node
              + " VRF "
              + vrf
              + " interface "
              + iface;
      assert !dispositionUnionBdd.diffSat(arpFalseBdd)
          : "dispositionUnionBdd larger than arpFalseBdd for node "
              + node
              + " VRF "
              + vrf
              + " interface "
              + iface;
    }
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
   * Mapping: route -&gt; nexthopinterface -&gt; resolved nextHopIps (or {@link Optional#empty()})
   * if dest IP should be used for ARP
   */
  private static @Nonnull Map<AbstractRoute, Map<String, Set<Optional<Ip>>>>
      computeNextHopInterfaces(Fib fib) {
    return fib.allEntries().stream()
        .filter(fibEntry -> fibEntry.getAction() instanceof FibForward)
        .collect(
            Collectors.groupingBy(
                FibEntry::getTopLevelRoute,
                Collectors.groupingBy(
                    fibEntry -> ((FibForward) fibEntry.getAction()).getInterfaceName(),
                    Collectors.mapping(
                        fibEntry -> ((FibForward) fibEntry.getAction()).getArpIp(),
                        Collectors.toSet()))));
  }
}
