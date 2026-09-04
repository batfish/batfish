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
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

  /** A forwarding analysis with the given, already computed, contents, as read from storage. */
  public static ForwardingAnalysisImpl of(
      Map<String, Map<String, IpSpace>> arpReplies,
      Map<String, Map<String, VrfForwardingBehavior>> vrfForwardingBehavior) {
    return new ForwardingAnalysisImpl(arpReplies, vrfForwardingBehavior);
  }

  private ForwardingAnalysisImpl(
      Map<String, Map<String, IpSpace>> arpReplies,
      Map<String, Map<String, VrfForwardingBehavior>> vrfForwardingBehavior) {
    _arpReplies = ImmutableMap.copyOf(arpReplies);
    _vrfForwardingBehavior = ImmutableMap.copyOf(vrfForwardingBehavior);
  }

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
          computeIpsRoutedOutInterfaces(fibs, allVrfs);
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
                              ownedIps,
                              interfacesWithMissingDevices,
                              internalIps,
                              externalIps,
                              routableIps);
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
      IpSpace ownedIps,
      Multimap<String, String> interfacesWithMissingDevices,
      IpSpace internalIps,
      IpSpace externalIps,
      Map<String, Map<String, IpSpace>> routableIps) {
    Map<String, IpSpace> accepted =
        ipOwners
            .getVrfIfaceOwnedIpSpaces()
            .getOrDefault(node, ImmutableMap.of())
            .getOrDefault(vrf, ImmutableMap.of());
    VrfForwardingIndex index = new VrfForwardingIndex(fib);

    /* interface -> dst IPs for which this vrf forwards out that interface, ARPing for the dest IP.
     *
     * Note: the interface must be in the node, but may not be in the vrf, due to route leaking, etc
     */
    Map<String, IpSpace> dstIpsArpingForDestIp =
        toImmutableMap(
            index.getInterfaces(),
            Function.identity(),
            iface -> index.matchingIps(index.getRoutesArpingForDestIp(iface)));

    /* edge -> dst ips for which this vrf forwards out the source of the edge,
     * ARPing for the dest IP and receiving a reply from the target of the edge.
     *
     * Note: the source interface of the edge must be in the node, but may not be in the vrf,
     * due to route leaking, etc
     */
    Map<Edge, IpSpace> arpTrueEdgeDestIp =
        computeArpTrueEdgeDestIp(node, topology, dstIpsArpingForDestIp, _arpReplies);

    /* edge -> dst ips for which this vrf forwards out the source of the edge,
     * ARPing for some next-hop IP and receiving a reply from the target of the edge.
     *
     * Note: the source interface of the edge must be in the node, but may not be in the vrf,
     * due to route leaking, etc
     */
    Map<Edge, IpSpace> arpTrueEdgeNextHopIp =
        computeArpTrueEdgeNextHopIp(node, index, topology, _arpReplies);

    Map<Edge, IpSpace> arpTrueEdge = computeArpTrueEdge(arpTrueEdgeDestIp, arpTrueEdgeNextHopIp);

    Map<String, InterfaceForwardingBehavior> interfaceForwardingBehavior =
        toImmutableMap(
            // The index may include interfaces in other VRFs that we forward out through due to
            // VRF leaking. All active interfaces in this VRF are included due to local routes.
            index.getInterfaces(),
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
                  computeArpFalseNhipRoutes(index, iface, someoneReplies);

              /* dst IPs for which this VRF forwards out that interface, ARPing
               * for the dst ip itself with no reply
               */
              IpSpace arpFalseDestIp =
                  computeArpFalseDestIp(dstIpsArpingForDestIp.get(iface), someoneReplies);

              /* dst ips for which this vrf forwards out that interface,
               * ARPing for a next-hop IP and receiving no reply
               */
              IpSpace arpFalseNextHopIp = index.matchingIps(arpFalseNhipRoutes);

              IpSpace arpFalse = AclIpSpace.union(arpFalseDestIp, arpFalseNextHopIp);

              // Of the routes that ARP for a next-hop IP and don't receive a response,
              // determine which ARP for an owned IP, and which ARP for an unowned IP.
              // Note: Due to ECMP during resolution, these sets are not necessarily disjoint.
              List<AbstractRoute> arpFalseNhipRoutesWithUnownedArpIp = new ArrayList<>();
              List<AbstractRoute> arpFalseNhipRoutesWithOwnedArpIp = new ArrayList<>();
              classifyArpFalseNhipRoutes(
                  unownedArpIps,
                  index,
                  arpFalseNhipRoutes,
                  arpFalseNhipRoutesWithUnownedArpIp::add,
                  arpFalseNhipRoutesWithOwnedArpIp::add);

              /* dst IPs for which that VRF forwards out that interface, ARPing
               * for some unowned next-hop IP with no reply
               */
              IpSpace dstIpsWithUnownedNextHopIpArpFalse =
                  index.matchingIps(arpFalseNhipRoutesWithUnownedArpIp);

              /* dst IPs for which that VRF forwards out that interface, ARPing
               * for some owned next-hop IP with no reply.
               */
              IpSpace dstIpsWithOwnedNextHopIpArpFalse =
                  index.matchingIps(arpFalseNhipRoutesWithOwnedArpIp);

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
    IpSpace nullRoutedIps = computeNullRoutedIps(fib);

    // nextVrf -> dest IPs that vrf delegates to nextVrf
    Map<String, IpSpace> nextVrfIps = computeNextVrfIps(fib);

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
      VrfForwardingIndex index,
      Set<AbstractRoute> routesWithNextHopIpArpFalse,
      Consumer<AbstractRoute> arpFalseNhipRoutesWithUnownedArpIp,
      Consumer<AbstractRoute> arpFalseNhipRoutesWithOwnedArpIp) {
    for (AbstractRoute route : routesWithNextHopIpArpFalse) {
      // Iterate over the arpIps for this route (on any interface), checking whether each is owned.
      // Stop once we've found one owned and one unowned.
      boolean foundUnowned = false;
      boolean foundOwned = false;
      for (Ip arpIp : index.getArpIps(route)) {
        if (!foundUnowned && unownedArpIps.contains(arpIp)) {
          foundUnowned = true;
          arpFalseNhipRoutesWithUnownedArpIp.accept(route);
        } else if (!foundOwned) {
          foundOwned = true;
          arpFalseNhipRoutesWithOwnedArpIp.accept(route);
        }
        if (foundUnowned && foundOwned) {
          break;
        }
      }
    }
  }

  private static Set<Ip> computeUnownedArpIps(
      Map<String, Map<String, Fib>> fibs, IpSpaceToBDD ipSpaceToBDD, BDD unownedIpsBDD) {
    // Collect the distinct ARP IPs in parallel, then test them against the BDD sequentially, since
    // BDDFactory is not thread safe.
    //
    // Worth splitting because the two halves have very different shapes: the first walks every FIB
    // entry in the snapshot, which is tens of millions of them on a large one, while the set it
    // produces stays in the thousands. Done as a single sequential stream this was one of the few
    // parts of dataplane computation that used one core, and it runs once per topology iteration.
    Set<Ip> arpIps =
        fibs.values().parallelStream()
            .flatMap(fibsByVrf -> fibsByVrf.values().stream())
            .flatMap(fib -> fib.allEntries().stream())
            .map(FibEntry::getAction)
            .filter(FibForward.class::isInstance)
            .map(fibAction -> ((FibForward) fibAction).getArpIp())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    ImmutableSet.Builder<Ip> unownedArpIps = ImmutableSet.builder();
    for (Ip arpIp : arpIps) {
      BDD arpIpBdd = ipSpaceToBDD.toBDD(arpIp);
      if (arpIpBdd.andSat(unownedIpsBDD)) {
        unownedArpIps.add(arpIp);
      }
      arpIpBdd.free();
    }
    return unownedArpIps.build();
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

  /**
   * Mapping: edge -&gt; dst IPs for which this node forwards out the source of the edge, ARPing for
   * the dest IP and receiving a reply from the target of the edge.
   *
   * @param dstIpsArpingForDestIp interface -&gt; dst IPs forwarded out that interface, ARPing for
   *     the dest IP
   */
  @VisibleForTesting
  static Map<Edge, IpSpace> computeArpTrueEdgeDestIp(
      String node,
      Topology topology,
      Map<String, IpSpace> dstIpsArpingForDestIp,
      Map<String, Map<String, IpSpace>> arpReplies) {
    ImmutableMap.Builder<Edge, IpSpace> result = ImmutableMap.builder();
    dstIpsArpingForDestIp.forEach(
        (iface, dstIpMatchesSomeRoutePrefix) -> {
          NodeInterfacePair out = NodeInterfacePair.of(node, iface);
          for (NodeInterfacePair receiver : topology.getNeighbors(out)) {
            IpSpace recvReplies =
                arpReplies.get(receiver.getHostname()).get(receiver.getInterface());
            result.put(
                new Edge(out, receiver),
                AclIpSpace.rejecting(dstIpMatchesSomeRoutePrefix.complement())
                    .thenPermitting(recvReplies)
                    .build());
          }
        });
    return result.build();
  }

  /**
   * Mapping: edge -&gt; dst IPs for which this node forwards out the source of the edge, ARPing for
   * some next-hop IP and receiving a reply from the target of the edge. Only edges with such IPs
   * are present.
   */
  @VisibleForTesting
  static @Nonnull Map<Edge, IpSpace> computeArpTrueEdgeNextHopIp(
      String node,
      VrfForwardingIndex index,
      Topology topology,
      Map<String, Map<String, IpSpace>> arpReplies) {
    ImmutableMap.Builder<Edge, IpSpace> result = ImmutableMap.builder();
    for (String iface : index.getInterfaces()) {
      NodeInterfacePair out = NodeInterfacePair.of(node, iface);
      for (NodeInterfacePair receiver : topology.getNeighbors(out)) {
        IpSpace recvReplies = arpReplies.get(receiver.getHostname()).get(receiver.getInterface());
        Set<AbstractRoute> routes =
            index.getRoutesArpingForNextHopIp(
                iface, nextHopIp -> recvReplies.containsIp(nextHopIp, ImmutableMap.of()));
        if (!routes.isEmpty()) {
          result.put(new Edge(out, receiver), index.matchingIps(routes));
        }
      }
    }
    return result.build();
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
      Map<String, Map<String, Fib>> fibs, List<Map.Entry<String, String>> allVrfs) {
    return allVrfs.parallelStream()
        .collect(
            ImmutableTable.toImmutableTable(
                Entry::getKey,
                Entry::getValue,
                e -> computeIpsRoutedOutInterfaces(fibs.get(e.getKey()).get(e.getValue()))))
        .rowMap();
  }

  /** Mapping: interface -&gt; dst IPs for which the given FIB forwards out that interface. */
  @VisibleForTesting
  static Map<String, IpSpace> computeIpsRoutedOutInterfaces(Fib fib) {
    // interface -> the distinct networks of routes forwarding out it, in FIB entry order
    Map<String, Set<Prefix>> networksByInterface = new LinkedHashMap<>();
    for (FibEntry entry : fib.allEntries()) {
      if (!(entry.getAction() instanceof FibForward)) {
        continue;
      }
      String iface = ((FibForward) entry.getAction()).getInterfaceName();
      // Cannot determine IPs for null interface here because it is not tied to a single VRF.
      if (iface.equals(Interface.NULL_INTERFACE_NAME)) {
        continue;
      }
      networksByInterface
          .computeIfAbsent(iface, i -> new LinkedHashSet<>())
          .add(entry.getTopLevelRoute().getNetwork());
    }
    return toImmutableMap(
        networksByInterface,
        Entry::getKey,
        ifaceEntry -> matchingIps(ifaceEntry.getValue(), fib::matchingIps));
  }

  /**
   * dst IPs for which this VRF forwards out an interface, ARPing for the dst IP itself with no
   * reply.
   *
   * @param dstIpsArpingForDestIp dst IPs forwarded out the interface, ARPing for the dest IP
   */
  @VisibleForTesting
  static IpSpace computeArpFalseDestIp(IpSpace dstIpsArpingForDestIp, IpSpace someoneReplies) {
    return AclIpSpace.rejecting(someoneReplies).thenPermitting(dstIpsArpingForDestIp).build();
  }

  @VisibleForTesting
  static IpSpace computeNullRoutedIps(Fib fib) {
    Set<AbstractRoute> nullRoutes =
        fib.allEntries().stream()
            .filter(fibEntry -> fibEntry.getAction() instanceof FibNullRoute)
            .map(FibEntry::getTopLevelRoute)
            .collect(ImmutableSet.toImmutableSet());
    return computeRouteMatchConditions(nullRoutes, fib);
  }

  @VisibleForTesting
  static Map<String, IpSpace> computeNextVrfIps(Fib fib) {
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
                        routesByNextVrfEntry.getValue() /* routes */, fib)));
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

  /** The dst IPs whose longest match in {@code fib} is one of the given routes. */
  @VisibleForTesting
  static IpSpace computeRouteMatchConditions(Collection<AbstractRoute> routes, Fib fib) {
    return matchingIps(distinctNetworks(routes), fib::matchingIps);
  }

  private static @Nonnull Set<Prefix> distinctNetworks(Collection<AbstractRoute> routes) {
    Set<Prefix> networks = new LinkedHashSet<>();
    for (AbstractRoute route : routes) {
      networks.add(route.getNetwork());
    }
    return networks;
  }

  /** The union of {@code matchingIps} over the given networks, in order. */
  private static @Nonnull IpSpace matchingIps(
      Collection<Prefix> networks, Function<Prefix, IpSpace> matchingIps) {
    if (networks.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }
    IpSpace[] spaces = new IpSpace[networks.size()];
    int i = 0;
    for (Prefix network : networks) {
      spaces[i++] = matchingIps.apply(network);
    }
    return firstNonNull(AclIpSpace.union(spaces), EmptyIpSpace.INSTANCE);
  }

  /* Mapping: hostname -&gt; vrfname -&gt; interfacename -&gt; set of associated routes (i.e.,
  /**
   * Routes that forward out {@code iface} ARPing for a next-hop IP that gets no reply from any
   * neighbor.
   */
  @VisibleForTesting
  static Set<AbstractRoute> computeArpFalseNhipRoutes(
      VrfForwardingIndex index, String iface, IpSpace someoneReplies) {
    return index.getRoutesArpingForNextHopIp(
        iface, nextHopIp -> !someoneReplies.containsIp(nextHopIp, ImmutableMap.of()));
  }

  /**
   * The forwarding entries of one VRF's FIB, grouped by outgoing interface and by the IP ARPed for
   * out that interface ({@link Optional#empty()} when it is the destination IP). Every per-route
   * decision in forwarding analysis depends only on that pair, so the routes in a group are handled
   * together and an ARP reply check runs once per group rather than once per route. Routes keep FIB
   * entry order.
   */
  @VisibleForTesting
  static final class VrfForwardingIndex {
    private final @Nonnull Fib _fib;

    /** interface -&gt; ARP IP -&gt; routes forwarding out that interface ARPing for that IP. */
    private final @Nonnull Map<String, Map<Optional<Ip>, Set<AbstractRoute>>>
        _routesByInterfaceAndArpIp;

    /** route -&gt; the next-hop IPs it ARPs for on any interface. */
    private final @Nonnull Map<AbstractRoute, Set<Ip>> _arpIpsByRoute;

    /** {@link Fib#matchingIps} memoized across the several unions each network takes part in. */
    private final @Nonnull Map<Prefix, IpSpace> _matchingIps;

    VrfForwardingIndex(Fib fib) {
      _fib = fib;
      Map<String, Map<Optional<Ip>, ImmutableSet.Builder<AbstractRoute>>> groups =
          new LinkedHashMap<>();
      Map<AbstractRoute, ImmutableSet.Builder<Ip>> arpIps = new HashMap<>();
      for (FibEntry entry : fib.allEntries()) {
        if (!(entry.getAction() instanceof FibForward)) {
          continue;
        }
        FibForward forward = (FibForward) entry.getAction();
        AbstractRoute route = entry.getTopLevelRoute();
        groups
            .computeIfAbsent(forward.getInterfaceName(), i -> new LinkedHashMap<>())
            .computeIfAbsent(forward.getArpIp(), ip -> ImmutableSet.builder())
            .add(route);
        if (forward.getArpIp().isPresent()) {
          arpIps.computeIfAbsent(route, r -> ImmutableSet.builder()).add(forward.getArpIp().get());
        }
      }
      _routesByInterfaceAndArpIp =
          toImmutableMap(
              groups,
              Entry::getKey,
              ifaceEntry ->
                  toImmutableMap(
                      ifaceEntry.getValue(), Entry::getKey, group -> group.getValue().build()));
      _arpIpsByRoute = toImmutableMap(arpIps, Entry::getKey, e -> e.getValue().build());
      _matchingIps = new HashMap<>();
    }

    /** Interfaces some route forwards out of, in FIB entry order. */
    @Nonnull
    Set<String> getInterfaces() {
      return _routesByInterfaceAndArpIp.keySet();
    }

    /** Routes forwarding out {@code iface} that ARP for the destination IP. */
    @Nonnull
    Set<AbstractRoute> getRoutesArpingForDestIp(String iface) {
      return _routesByInterfaceAndArpIp
          .getOrDefault(iface, ImmutableMap.of())
          .getOrDefault(Optional.empty(), ImmutableSet.of());
    }

    /**
     * Routes forwarding out {@code iface} that ARP for a next-hop IP accepted by {@code test},
     * which is evaluated once per distinct next-hop IP.
     */
    @Nonnull
    Set<AbstractRoute> getRoutesArpingForNextHopIp(String iface, Predicate<Ip> test) {
      ImmutableSet.Builder<AbstractRoute> routes = ImmutableSet.builder();
      _routesByInterfaceAndArpIp
          .getOrDefault(iface, ImmutableMap.of())
          .forEach(
              (arpIp, groupRoutes) -> {
                if (arpIp.isPresent() && test.test(arpIp.get())) {
                  routes.addAll(groupRoutes);
                }
              });
      return routes.build();
    }

    /** The next-hop IPs {@code route} ARPs for on any interface. */
    @Nonnull
    Set<Ip> getArpIps(AbstractRoute route) {
      return _arpIpsByRoute.getOrDefault(route, ImmutableSet.of());
    }

    /** The dst IPs whose longest match in this FIB is one of the given routes. */
    @Nonnull
    IpSpace matchingIps(Collection<AbstractRoute> routes) {
      return ForwardingAnalysisImpl.matchingIps(
          distinctNetworks(routes),
          network -> _matchingIps.computeIfAbsent(network, _fib::matchingIps));
    }
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
}
