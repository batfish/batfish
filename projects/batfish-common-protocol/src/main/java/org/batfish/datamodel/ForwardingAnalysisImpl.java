package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.CommonUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.datamodel.collections.NodeInterfacePair;

public final class ForwardingAnalysisImpl implements ForwardingAnalysis {

  // mapping: node name -> interface name -> ips that the interface would reply arp request
  private final Map<String, Map<String, IpSpace>> _arpReplies;

  private final Map<Edge, IpSpace> _arpTrueEdge;

  // mapping: edge -> dst ips for which end up forwarding to this edge and arp for the dst ip itself
  // and get response
  private final Map<Edge, IpSpace> _arpTrueEdgeDestIp;

  // mapping: edge -> dst ip for which end up forwarding to this edge arp for some other ip and get
  // response
  private final Map<Edge, IpSpace> _arpTrueEdgeNextHopIp;

  private final Map<String, Map<String, Set<Ip>>> _interfaceOwnedIps;

  // mapping: node name -> interface name -> dst ips which are routed to the interface
  private final Map<String, Map<String, IpSpace>> _ipsRoutedOutInterfaces;

  private final Map<String, Map<String, Map<String, IpSpace>>> _arpFalse;

  // mapping: node name -> vrf name -> interface name -> dst ips
  // for which arp dst ip itself but would not be replied
  private final Map<String, Map<String, Map<String, IpSpace>>> _arpFalseDestIp;

  // mapping: node name -> vrf name -> interface name -> dst ips
  // for which arp another ip but would not be replied
  private final Map<String, Map<String, Map<String, IpSpace>>> _arpFalseNextHopIp;

  private final Map<String, Map<String, IpSpace>> _nullRoutedIps;

  private final Map<String, Map<String, IpSpace>> _routableIps;

  // mapping: node name -> vrf name -> interface name -> a set of
  // routes in which the arp ip is dst ip
  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      _routesWhereDstIpCanBeArpIp;

  private final Map<Edge, Set<AbstractRoute>> _routesWithDestIpEdge;

  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWithNextHop;

  // mapping: node name -> vrf name -> interface name ->
  // a set of routes that with next hop ip but no arp replies
  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      _routesWithNextHopIpArpFalse;

  // mapping: node name -> vrf name -> interface name ->
  // a set of routes that with next hop ip owned by some interfaces but no arp replies
  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      _routesWithOwnedNextHopIpArpFalse;

  // mapping: node name -> vrf name -> interface name ->
  // a set of routes that with next hop ip not owned by any interfaces but no arp replies
  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      _routesWithUnownedNextHopIpArpFalse;

  private final Map<Edge, Set<AbstractRoute>> _routesWithNextHopIpArpTrue;

  private final Map<String, Map<String, IpSpace>> _someoneReplies;

  // mapping: hostname -> vrf name -> interfacename -> dst ips that end up with neighbor unreachable
  private final Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachable;

  // mapping: hostname -> vrf name -> interfacename -> dst ips that end up delivered to subnet
  private final Map<String, Map<String, Map<String, IpSpace>>> _deliveredToSubnet;

  // mapping: hostname -> vrf name -> interfacename -> dst ips that end up exiting the network
  private final Map<String, Map<String, Map<String, IpSpace>>> _exitsNetwork;

  // mapping: hostname -> vrf name -> interfacename -> dst ips that end up with insufficient info
  private final Map<String, Map<String, Map<String, IpSpace>>> _insufficientInfo;

  // mapping: hostname -> set of interfacenames that is not full
  private final Map<String, Set<String>> _interfacesWithMissingDevices;

  // mapping: hostname -> vrf name -> interfacename -> ips belonging to a subnet of the interface
  private final Map<String, Map<String, Map<String, IpSpace>>> _interfaceHostSubnetIps;

  private final Map<String, Map<String, Map<String, IpSpace>>> _dstIpsWithUnownedNextHopIpArpFalse;

  private final Map<String, Map<String, Map<String, IpSpace>>> _dstIpsWithOwnedNextHopIpArpFalse;

  private final IpSpaceToBDD _ipSpaceToBDD;

  private final Map<String, Map<String, BDD>> _interfaceHostSubnetIpBDDs;

  // ips belonging to any interface in the network
  private final IpSpace _ownedIps;

  // ips belonging to any subnet in the network
  private final IpSpace _internalIps;

  // ips not belonging to any subnet in the network
  private final IpSpace _externalIps;

  private final BDD _unownedIpsBDD;

  private static IpSpaceToBDD initIpSpaceToBDD() {
    BDDPacket bddPacket = new BDDPacket();
    return new IpSpaceToBDD(bddPacket.getDstIp());
  }

  public <T extends HasAbstractRoute> ForwardingAnalysisImpl(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<T>>> ribs,
      Map<String, Map<String, Fib>> fibs,
      Topology topology) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("Construct ForwardingAnalysisImpl").startActive()) {
      assert span != null; // avoid unused warning

      // TODO accept IpSpaceToBDD as parameter
      _ipSpaceToBDD = initIpSpaceToBDD();
      _interfaceHostSubnetIps = computeInterfaceHostSubnetIps(configurations);
      _interfaceOwnedIps = TopologyUtil.computeInterfaceOwnedIps(configurations, false);
      _ownedIps = computeOwnedIps();
      _unownedIpsBDD = computeUnownedIpsBDD();
      _internalIps = computeInternalIps();
      _externalIps = _internalIps.complement();
      _interfaceHostSubnetIpBDDs = computeInterfaceHostSubnetIpBDDs();
      _interfacesWithMissingDevices = computeInterfacesWithMissingDevices(configurations);
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps = computeMatchingIps(ribs);
      _nullRoutedIps = computeNullRoutedIps(matchingIps, fibs);
      _routableIps = computeRoutableIps(ribs);
      _routesWithNextHop = computeRoutesWithNextHop(configurations, fibs);
      _ipsRoutedOutInterfaces = computeIpsRoutedOutInterfaces(matchingIps);
      _arpReplies = computeArpReplies(configurations, ribs);
      _someoneReplies = computeSomeoneReplies(topology);
      _routesWithNextHopIpArpFalse = computeRoutesWithNextHopIpArpFalse(fibs);
      _routesWithUnownedNextHopIpArpFalse = computeRoutesWithUnownedNextHopIpArpFalse();
      _routesWithOwnedNextHopIpArpFalse = computeRoutesWithOwnedNextHopIpArpFalse();
      _arpFalseNextHopIp = computeArpFalseNextHopIp(matchingIps);
      _routesWithNextHopIpArpTrue = computeRoutesWithNextHopIpArpTrue(fibs, topology);
      _arpTrueEdgeNextHopIp = computeArpTrueEdgeNextHopIp(configurations, matchingIps);
      _routesWhereDstIpCanBeArpIp = computeRoutesWhereDstIpCanBeArpIp(fibs);
      _arpFalseDestIp = computeArpFalseDestIp(matchingIps);
      _arpFalse = computeArpFalse();
      _routesWithDestIpEdge = computeRoutesWithDestIpEdge(topology);
      _arpTrueEdgeDestIp = computeArpTrueEdgeDestIp(configurations, matchingIps);
      _arpTrueEdge = computeArpTrueEdge();
      _dstIpsWithUnownedNextHopIpArpFalse = computeDstIpsWithUnownedNextHopIpArpFalse(matchingIps);
      _dstIpsWithOwnedNextHopIpArpFalse = computeDstIpsWithOwnedNextHopIpArpFalse(matchingIps);
      _deliveredToSubnet = computeDeliveredToSubnet();
      _exitsNetwork = computeExitsNetwork(configurations);
      _insufficientInfo = computeInsufficientInfo(configurations);
      _neighborUnreachable = computeNeighborUnreachable();
    }
  }

  /* The constructor should only be used for tests */
  @VisibleForTesting
  ForwardingAnalysisImpl(
      Map<String, Map<String, IpSpace>> arpReplies,
      Map<Edge, IpSpace> arpTrueEdge,
      Map<Edge, IpSpace> arpTrueEdgeDestIp,
      Map<Edge, IpSpace> arpTrueEdgeNextHopIp,
      Map<String, Map<String, Set<Ip>>> interfaceOwnedIps,
      Map<String, Map<String, IpSpace>> ipsRoutedOutInterfaces,
      Map<String, Map<String, Map<String, IpSpace>>> arpFalse,
      Map<String, Map<String, Map<String, IpSpace>>> arpFalseDestIp,
      Map<String, Map<String, Map<String, IpSpace>>> arpFalseNextHopIp,
      Map<String, Map<String, IpSpace>> nullRoutedIps,
      Map<String, Map<String, IpSpace>> routableIps,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWhereDstIpCanBeArpIp,
      Map<Edge, Set<AbstractRoute>> routesWithDestIpEdge,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHopIpArpFalse,
      Map<Edge, Set<AbstractRoute>> routesWithNextHopIpArpTrue,
      Map<String, Map<String, IpSpace>> someoneReplies,
      Map<String, Map<String, Map<String, IpSpace>>> interfaceHostSubnetIps,
      Map<String, Set<String>> interfacesWithMissingDevices,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithUnownedNextHopIpArpFalse,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithOwnedNextHopIpArpFalse,
      Map<String, Map<String, Map<String, IpSpace>>> dstIpsWithUnownedNextHopIpArpFalse,
      Map<String, Map<String, Map<String, IpSpace>>> dstIpsWithOwnedNextHopIpArpFalse,
      IpSpace internalIps) {
    _nullRoutedIps = nullRoutedIps;
    _routableIps = routableIps;
    _routesWithNextHop = routesWithNextHop;
    _interfaceOwnedIps = interfaceOwnedIps;
    _ipsRoutedOutInterfaces = ipsRoutedOutInterfaces;
    _arpReplies = arpReplies;
    _someoneReplies = someoneReplies;
    _routesWithNextHopIpArpFalse = routesWithNextHopIpArpFalse;
    _arpFalseNextHopIp = arpFalseNextHopIp;
    _routesWithNextHopIpArpTrue = routesWithNextHopIpArpTrue;
    _arpTrueEdgeNextHopIp = arpTrueEdgeNextHopIp;
    _routesWhereDstIpCanBeArpIp = routesWhereDstIpCanBeArpIp;
    _arpFalseDestIp = arpFalseDestIp;
    _arpFalse = arpFalse;
    _routesWithDestIpEdge = routesWithDestIpEdge;
    _arpTrueEdgeDestIp = arpTrueEdgeDestIp;
    _arpTrueEdge = arpTrueEdge;
    _routesWithUnownedNextHopIpArpFalse = routesWithUnownedNextHopIpArpFalse;
    _routesWithOwnedNextHopIpArpFalse = routesWithOwnedNextHopIpArpFalse;
    _interfaceHostSubnetIps = interfaceHostSubnetIps;
    _interfacesWithMissingDevices = interfacesWithMissingDevices;
    _neighborUnreachable = null;
    _deliveredToSubnet = null;
    _insufficientInfo = null;
    _exitsNetwork = null;
    _dstIpsWithOwnedNextHopIpArpFalse = dstIpsWithOwnedNextHopIpArpFalse;
    _dstIpsWithUnownedNextHopIpArpFalse = dstIpsWithUnownedNextHopIpArpFalse;

    _ipSpaceToBDD = initIpSpaceToBDD();
    _internalIps = internalIps;
    _externalIps = _internalIps.complement();
    _interfaceHostSubnetIpBDDs = computeInterfaceHostSubnetIpBDDs();
    _ownedIps = computeOwnedIps();
    _unownedIpsBDD = computeUnownedIpsBDD();
  }

  /**
   * Compute an IP address ACL for each interface of each node permitting only those IPs for which
   * the node would send out an ARP reply on that interface: <br>
   * <br>
   * 1) PERMIT IPs belonging to the interface.<br>
   * 2) (Proxy-ARP) DENY any IP for which there is a longest-prefix match entry in the FIB that goes
   * through the interface.<br>
   * 3) (Proxy-ARP) PERMIT any other IP routable via the VRF of the interface.
   */
  @VisibleForTesting
  <T extends HasAbstractRoute> Map<String, Map<String, IpSpace>> computeArpReplies(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<T>>> ribs) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeArpReplies").startActive()) {
      assert span != null; // avoid unused warning
      Map<String, Map<String, IpSpace>> routableIpsByNodeVrf = computeRoutableIpsByNodeVrf(ribs);
      return toImmutableMap(
          configurations,
          Entry::getKey,
          nodeEntry -> {
            String hostname = nodeEntry.getKey();
            Map<String, Interface> interfaces = nodeEntry.getValue().getAllInterfaces();
            Map<String, IpSpace> routableIpsByVrf = routableIpsByNodeVrf.get(hostname);
            Map<String, IpSpace> ipsRoutedOutInterfaces = _ipsRoutedOutInterfaces.get(hostname);
            return computeArpRepliesByInterface(
                interfaces, routableIpsByVrf, ipsRoutedOutInterfaces);
          });
    }
  }

  @VisibleForTesting
  Map<String, IpSpace> computeArpRepliesByInterface(
      Map<String, Interface> interfaces,
      Map<String, IpSpace> routableIpsByVrf,
      Map<String, IpSpace> ipsRoutedOutInterfaces) {
    return toImmutableMap(
        interfaces,
        Entry::getKey,
        ifaceEntry ->
            computeInterfaceArpReplies(
                ifaceEntry.getValue(),
                routableIpsByVrf.get(ifaceEntry.getValue().getVrfName()),
                ipsRoutedOutInterfaces.get(ifaceEntry.getKey())));
  }

  @VisibleForTesting
  Map<Edge, IpSpace> computeArpTrueEdge() {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeArpTrueEdge").startActive()) {
      assert span != null; // avoid unused warning
      return Sets.union(_arpTrueEdgeDestIp.keySet(), _arpTrueEdgeNextHopIp.keySet()).stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Function.identity(),
                  edge -> {
                    AclIpSpace.Builder ipSpace = AclIpSpace.builder();
                    IpSpace dstIp = _arpTrueEdgeDestIp.get(edge);
                    if (dstIp != null) {
                      ipSpace.thenPermitting(dstIp);
                    }
                    IpSpace nextHopIp = _arpTrueEdgeNextHopIp.get(edge);
                    if (nextHopIp != null) {
                      ipSpace.thenPermitting(nextHopIp);
                    }
                    return ipSpace.build();
                  }));
    }
  }

  @VisibleForTesting
  Map<Edge, IpSpace> computeArpTrueEdgeDestIp(
      Map<String, Configuration> configurations,
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeArpTrueEdgeDestIp")
            .startActive()) {
      assert span != null; // avoid unused warning
      return _routesWithDestIpEdge.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* edge */,
                  routesWithDestIpEdgeEntry -> {
                    Edge edge = routesWithDestIpEdgeEntry.getKey();
                    Set<AbstractRoute> routes = routesWithDestIpEdgeEntry.getValue();
                    String hostname = edge.getNode1();
                    String iface = edge.getInt1();
                    String vrf =
                        configurations.get(hostname).getAllInterfaces().get(iface).getVrfName();
                    IpSpace dstIpMatchesSomeRoutePrefix =
                        computeRouteMatchConditions(routes, matchingIps.get(hostname).get(vrf));
                    String recvNode = edge.getNode2();
                    String recvInterface = edge.getInt2();
                    IpSpace recvReplies = _arpReplies.get(recvNode).get(recvInterface);
                    return AclIpSpace.rejecting(dstIpMatchesSomeRoutePrefix.complement())
                        .thenPermitting(recvReplies)
                        .build();
                  }));
    }
  }

  @VisibleForTesting
  Map<Edge, IpSpace> computeArpTrueEdgeNextHopIp(
      Map<String, Configuration> configurations,
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeArpTrueEdgeNextHopIp")
            .startActive()) {
      assert span != null; // avoid unused warning
      return _routesWithNextHopIpArpTrue.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* edge */,
                  routesWithNextHopIpArpTrueEntry -> {
                    Edge edge = routesWithNextHopIpArpTrueEntry.getKey();
                    String hostname = edge.getNode1();
                    String iface = edge.getInt1();
                    String vrf =
                        configurations.get(hostname).getAllInterfaces().get(iface).getVrfName();
                    Set<AbstractRoute> routes = routesWithNextHopIpArpTrueEntry.getValue();
                    return computeRouteMatchConditions(routes, matchingIps.get(hostname).get(vrf));
                  }));
    }
  }

  @VisibleForTesting
  IpSpace computeInterfaceArpReplies(
      Interface iface, IpSpace routableIpsForThisVrf, IpSpace ipsRoutedThroughInterface) {
    IpSpace ipsAssignedToThisInterface = computeIpsAssignedToThisInterface(iface);
    if (ipsAssignedToThisInterface == EmptyIpSpace.INSTANCE) {
      // if no IPs are assigned to this interface, it replies to no ARP requests.
      return EmptyIpSpace.INSTANCE;
    }
    /* Accept IPs assigned to this interface */
    AclIpSpace.Builder interfaceArpReplies = AclIpSpace.permitting(ipsAssignedToThisInterface);
    if (iface.getProxyArp()) {
      /* Reject IPs routed through this interface */
      interfaceArpReplies.thenRejecting(ipsRoutedThroughInterface);

      /* Accept all other routable IPs */
      interfaceArpReplies.thenPermitting(routableIpsForThisVrf);
    }
    return interfaceArpReplies.build();
  }

  @VisibleForTesting
  IpSpace computeIpsAssignedToThisInterface(Interface iface) {
    if (iface.getAllAddresses().isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }

    Set<Ip> ips = _interfaceOwnedIps.get(iface.getOwner().getHostname()).get(iface.getName());
    if (ips == null || ips.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }
    IpWildcardSetIpSpace.Builder ipsAssignedToThisInterfaceBuilder = IpWildcardSetIpSpace.builder();
    ips.forEach(ip -> ipsAssignedToThisInterfaceBuilder.including(new IpWildcard(ip)));
    return ipsAssignedToThisInterfaceBuilder.build();
  }

  @VisibleForTesting
  Map<String, Map<String, IpSpace>> computeIpsRoutedOutInterfaces(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeIpsRoutedOutInterfaces")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _routesWithNextHop,
          Entry::getKey /* hostname */,
          nodeEntry -> {
            String hostname = nodeEntry.getKey();
            return nodeEntry.getValue().entrySet().stream()
                .flatMap(
                    vrfEntry -> {
                      String vrf = vrfEntry.getKey();
                      Map<Prefix, IpSpace> vrfMatchingIps = matchingIps.get(hostname).get(vrf);
                      return vrfEntry.getValue().entrySet().stream()
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
                              });
                    })
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
          });
    }
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeArpFalse() {
    return union(_arpFalseDestIp, _arpFalseNextHopIp);
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeArpFalseDestIp(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeArpFalseDestIp")
            .startActive()) {
      assert span != null; // avoid unused warning
      return _routesWhereDstIpCanBeArpIp.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  routesWhereDstIpCanBeArpIpByHostnameEntry -> {
                    String hostname = routesWhereDstIpCanBeArpIpByHostnameEntry.getKey();
                    return routesWhereDstIpCanBeArpIpByHostnameEntry.getValue().entrySet().stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* vrf */,
                                routesWhereDstIpCanBeArpIpByVrfEntry -> {
                                  String vrf = routesWhereDstIpCanBeArpIpByVrfEntry.getKey();
                                  Map<Prefix, IpSpace> vrfMatchingIps =
                                      matchingIps.get(hostname).get(vrf);
                                  return routesWhereDstIpCanBeArpIpByVrfEntry.getValue().entrySet()
                                      .stream()
                                      /* null_interface is handled in computeNullRoutedIps */
                                      .filter(
                                          entry ->
                                              !entry.getKey().equals(Interface.NULL_INTERFACE_NAME))
                                      .collect(
                                          ImmutableMap.toImmutableMap(
                                              Entry::getKey /* outInterface */,
                                              routesWhereDstIpCanBeArpIpByInterfaceEntry -> {
                                                String outInterface =
                                                    routesWhereDstIpCanBeArpIpByInterfaceEntry
                                                        .getKey();
                                                Set<AbstractRoute> routes =
                                                    routesWhereDstIpCanBeArpIpByInterfaceEntry
                                                        .getValue();
                                                IpSpace someoneReplies =
                                                    _someoneReplies
                                                        .getOrDefault(hostname, ImmutableMap.of())
                                                        .getOrDefault(
                                                            outInterface, EmptyIpSpace.INSTANCE);
                                                IpSpace ipsRoutedOutInterface =
                                                    computeRouteMatchConditions(
                                                        routes, vrfMatchingIps);
                                                return AclIpSpace.rejecting(someoneReplies)
                                                    .thenPermitting(ipsRoutedOutInterface)
                                                    .build();
                                              }));
                                }));
                  }));
    }
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeArpFalseNextHopIp(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeArpFalseNextHopIp")
            .startActive()) {
      assert span != null; // avoid unused warning
      return _routesWithNextHopIpArpFalse.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  routesWithNextHopIpArpFalseByHostnameEntry -> {
                    String hostname = routesWithNextHopIpArpFalseByHostnameEntry.getKey();
                    return routesWithNextHopIpArpFalseByHostnameEntry.getValue().entrySet().stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* vrf */,
                                routesWithNextHopIpArpFalseByVrfEntry -> {
                                  String vrf = routesWithNextHopIpArpFalseByVrfEntry.getKey();
                                  return routesWithNextHopIpArpFalseByVrfEntry.getValue().entrySet()
                                      .stream()
                                      /* null_interface is handled in computeNullRoutedIps */
                                      .filter(
                                          entry ->
                                              !entry.getKey().equals(Interface.NULL_INTERFACE_NAME))
                                      .collect(
                                          ImmutableMap.toImmutableMap(
                                              Entry::getKey /* outInterface */,
                                              routesWithNextHopIpArpFalseByOutInterfaceEntry ->
                                                  computeRouteMatchConditions(
                                                      routesWithNextHopIpArpFalseByOutInterfaceEntry
                                                          .getValue(),
                                                      matchingIps.get(hostname).get(vrf))));
                                }));
                  }));
    }
  }

  @VisibleForTesting
  static Map<String, Map<String, IpSpace>> computeNullRoutedIps(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps,
      Map<String, Map<String, Fib>> fibs) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeNullRoutedIps").startActive()) {
      assert span != null; // avoid unused warning
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
                                      fib.getNextHopInterfaces().entrySet().stream()
                                          .filter(
                                              nextHopInterfacesByRouteEntry ->
                                                  nextHopInterfacesByRouteEntry
                                                      .getValue()
                                                      .keySet()
                                                      .contains(Interface.NULL_INTERFACE_NAME))
                                          .map(Entry::getKey)
                                          .collect(ImmutableSet.toImmutableSet());
                                  return computeRouteMatchConditions(nullRoutes, vrfMatchingIps);
                                }));
                  }));
    }
  }

  @VisibleForTesting
  static <T extends HasAbstractRoute> Map<String, Map<String, IpSpace>> computeRoutableIps(
      SortedMap<String, SortedMap<String, GenericRib<T>>> ribs) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeRoutableIps").startActive()) {
      assert span != null; // avoid unused warning
      return ribs.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  ribsByHostnameEntry ->
                      ribsByHostnameEntry.getValue().entrySet().stream()
                          .collect(
                              ImmutableMap.toImmutableMap(
                                  Entry::getKey /* vrf */,
                                  ribsByVrfEntry -> ribsByVrfEntry.getValue().getRoutableIps()))));
    }
  }

  /** Compute for each VRF of each node the IPs that are routable. */
  @VisibleForTesting
  static <T extends HasAbstractRoute> Map<String, Map<String, IpSpace>> computeRoutableIpsByNodeVrf(
      SortedMap<String, SortedMap<String, GenericRib<T>>> ribs) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeRoutableIpsByNodeVrf")
            .startActive()) {
      assert span != null; // avoid unused warning
      return ribs.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey, // hostname
                  ribsByNodeEntry ->
                      ribsByNodeEntry.getValue().entrySet().stream()
                          .collect(
                              ImmutableMap.toImmutableMap(
                                  Entry::getKey, // vrfName
                                  ribsByVrfEntry -> ribsByVrfEntry.getValue().getRoutableIps()))));
    }
  }

  @VisibleForTesting
  static <T extends HasAbstractRoute>
      Map<String, Map<String, Map<Prefix, IpSpace>>> computeMatchingIps(
          SortedMap<String, SortedMap<String, GenericRib<T>>> ribs) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeMatchingIps").startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          ribs,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry -> vrfEntry.getValue().getMatchingIps()));
    }
  }

  @VisibleForTesting
  static IpSpace computeRouteMatchConditions(
      Set<AbstractRoute> routes, Map<Prefix, IpSpace> matchingIps) {
    // get the union of IpSpace that match one of the routes
    return firstNonNull(
        AclIpSpace.union(
            routes.stream()
                .map(AbstractRoute::getNetwork)
                .collect(ImmutableSet.toImmutableSet())
                .stream()
                .map(matchingIps::get)
                .toArray(IpSpace[]::new)),
        EmptyIpSpace.INSTANCE);
  }

  /*
   * Mapping: hostname -&gt; vrfname -&gt; interfacename -&gt; a set of routes where each route
   * has at least one unset final next hop ip
   */
  @VisibleForTesting
  Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWhereDstIpCanBeArpIp(
      Map<String, Map<String, Fib>> fibs) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeRoutesWhereDstIpCanBeArpIp")
            .startActive()) {
      assert span != null; // avoid unused warning
      return _routesWithNextHop.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  routesWithNextHopByHostnameEntry -> {
                    String hostname = routesWithNextHopByHostnameEntry.getKey();
                    return routesWithNextHopByHostnameEntry.getValue().entrySet().stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* vrf */,
                                routesWithNextHopByVrfEntry -> {
                                  String vrf = routesWithNextHopByVrfEntry.getKey();
                                  Fib fib = fibs.get(hostname).get(vrf);
                                  return routesWithNextHopByVrfEntry.getValue().entrySet().stream()
                                      .collect(
                                          ImmutableMap.toImmutableMap(
                                              Entry::getKey /* interface */,
                                              routesWithNextHopByInterfaceEntry -> {
                                                String iface =
                                                    routesWithNextHopByInterfaceEntry.getKey();
                                                // return a set of routes where each route has
                                                // some final next hop ip unset
                                                return routesWithNextHopByInterfaceEntry
                                                    .getValue() // routes with this interface as
                                                    // outgoing interfaces
                                                    .stream()
                                                    .filter(
                                                        route ->
                                                            fib.getNextHopInterfaces()
                                                                .get(route)
                                                                .get(iface)
                                                                .keySet() // final next hop ips
                                                                .contains(
                                                                    Route.UNSET_ROUTE_NEXT_HOP_IP))
                                                    .collect(ImmutableSet.toImmutableSet());
                                              }));
                                }));
                  }));
    }
  }

  @VisibleForTesting
  Map<Edge, Set<AbstractRoute>> computeRoutesWithDestIpEdge(Topology topology) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeRoutesWithDestIpEdge")
            .startActive()) {
      assert span != null; // avoid unused warning
      ImmutableMap.Builder<Edge, Set<AbstractRoute>> routesByEdgeBuilder = ImmutableMap.builder();
      _routesWhereDstIpCanBeArpIp.forEach(
          (hostname, routesWhereDstIpCanBeArpIpByVrf) ->
              routesWhereDstIpCanBeArpIpByVrf.forEach(
                  (vrf, routesWhereDstIpCanBeArpIpByOutInterface) ->
                      routesWhereDstIpCanBeArpIpByOutInterface.forEach(
                          (outInterface, routes) -> {
                            NodeInterfacePair out = new NodeInterfacePair(hostname, outInterface);
                            Set<NodeInterfacePair> receivers = topology.getNeighbors(out);
                            receivers.forEach(
                                receiver ->
                                    routesByEdgeBuilder.put(new Edge(out, receiver), routes));
                          })));
      return routesByEdgeBuilder.build();
    }
  }

  /* Mapping: hostname -&gt; vrfname -&gt; interfacename -&gt; set of associated routes (i.e.,
   * routes that use the interface as outgoing interface */
  @VisibleForTesting
  static Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWithNextHop(
      Map<String, Configuration> configurations, Map<String, Map<String, Fib>> fibs) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeRoutesWithNextHop")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          configurations,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue().getVrfs(),
                  Entry::getKey,
                  vrfEntry -> {
                    Map<String, Set<AbstractRoute>> routesByNextHopInterface =
                        fibs.get(nodeEntry.getKey())
                            .get(vrfEntry.getKey())
                            .getRoutesByNextHopInterface();
                    return toImmutableMap(
                        vrfEntry.getValue().getInterfaces(),
                        Entry::getKey,
                        ifaceEntry ->
                            routesByNextHopInterface.getOrDefault(
                                ifaceEntry.getKey(), ImmutableSet.of()));
                  }));
    }
  }

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      computeRoutesWithNextHopIpArpFalseFilter(Function<AbstractRoute, Boolean> routeFilter) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpFalseFilter")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _routesWithNextHopIpArpFalse,
          Entry::getKey /* hostname */,
          routesWithNextHopByHostnameEntry ->
              toImmutableMap(
                  routesWithNextHopByHostnameEntry.getValue(),
                  Entry::getKey /* vrf */,
                  routesWithNextHopByVrfEntry ->
                      toImmutableMap(
                          routesWithNextHopByVrfEntry.getValue(),
                          Entry::getKey /* outInterface */,
                          routesWithNextHopByOutInterfaceEntry ->
                              routesWithNextHopByOutInterfaceEntry.getValue().stream()
                                  .filter(routeFilter::apply)
                                  .collect(Collectors.toSet()))));
    }
  }

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      computeRoutesWithOwnedNextHopIpArpFalse() {
    return computeRoutesWithNextHopIpArpFalseFilter(
        route -> _ipSpaceToBDD.toBDD(route.getNextHopIp()).and(_unownedIpsBDD).isZero());
  }

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      computeRoutesWithUnownedNextHopIpArpFalse() {
    return computeRoutesWithNextHopIpArpFalseFilter(
        route -> !_ipSpaceToBDD.toBDD(route.getNextHopIp()).and(_unownedIpsBDD).isZero());
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWithNextHopIpArpFalse(
      Map<String, Map<String, Fib>> fibs) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpFalse")
            .startActive()) {
      assert span != null; // avoid unused warning
      return _routesWithNextHop.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  routesWithNextHopByHostnameEntry -> {
                    String hostname = routesWithNextHopByHostnameEntry.getKey();
                    return routesWithNextHopByHostnameEntry.getValue().entrySet().stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* vrf */,
                                routesWithNextHopByVrfEntry -> {
                                  String vrf = routesWithNextHopByVrfEntry.getKey();
                                  return routesWithNextHopByVrfEntry.getValue().entrySet().stream()
                                      .collect(
                                          ImmutableMap.toImmutableMap(
                                              Entry::getKey /* outInterface */,
                                              routesWithNextHopByOutInterfaceEntry -> {
                                                Fib fib = fibs.get(hostname).get(vrf);
                                                String outInterface =
                                                    routesWithNextHopByOutInterfaceEntry.getKey();
                                                Set<AbstractRoute> candidateRoutes =
                                                    routesWithNextHopByOutInterfaceEntry.getValue();
                                                return computeRoutesWithNextHopIpArpFalseForInterface(
                                                    fib, hostname, outInterface, candidateRoutes);
                                              }));
                                }));
                  }));
    }
  }

  @VisibleForTesting
  Set<AbstractRoute> computeRoutesWithNextHopIpArpFalseForInterface(
      Fib fib, String hostname, String outInterface, Set<AbstractRoute> candidateRoutes) {
    IpSpace someoneReplies =
        _someoneReplies
            .getOrDefault(hostname, ImmutableMap.of())
            .getOrDefault(outInterface, EmptyIpSpace.INSTANCE);
    return candidateRoutes.stream()
        .filter(
            candidateRoute ->
                fib.getNextHopInterfaces().get(candidateRoute).get(outInterface).keySet().stream()
                    .filter(ip -> !ip.equals(Route.UNSET_ROUTE_NEXT_HOP_IP))
                    .anyMatch(
                        nextHopIp -> !someoneReplies.containsIp(nextHopIp, ImmutableMap.of())))
        .collect(ImmutableSet.toImmutableSet());
  }

  @VisibleForTesting
  Map<Edge, Set<AbstractRoute>> computeRoutesWithNextHopIpArpTrue(
      Map<String, Map<String, Fib>> fibs, Topology topology) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpTrue")
            .startActive()) {
      assert span != null; // avoid unused warning
      ImmutableMap.Builder<Edge, Set<AbstractRoute>> routesByEdgeBuilder = ImmutableMap.builder();
      _routesWithNextHop.forEach(
          (hostname, routesWithNextHopByVrf) ->
              routesWithNextHopByVrf.forEach(
                  (vrf, routesWithNextHopByInterface) ->
                      routesWithNextHopByInterface.forEach(
                          (outInterface, candidateRoutes) -> {
                            Fib fib = fibs.get(hostname).get(vrf);
                            NodeInterfacePair out = new NodeInterfacePair(hostname, outInterface);
                            Set<NodeInterfacePair> receivers = topology.getNeighbors(out);
                            receivers.forEach(
                                receiver -> {
                                  String recvNode = receiver.getHostname();
                                  String recvInterface = receiver.getInterface();
                                  IpSpace recvReplies =
                                      _arpReplies.get(recvNode).get(recvInterface);
                                  Edge edge = new Edge(out, receiver);
                                  Set<AbstractRoute> routes =
                                      candidateRoutes.stream()
                                          .filter(
                                              route ->
                                                  fib.getNextHopInterfaces().get(route)
                                                      .get(outInterface).keySet() /* nextHopIps */
                                                      .stream()
                                                      .filter(
                                                          ip ->
                                                              !ip.equals(
                                                                  Route.UNSET_ROUTE_NEXT_HOP_IP))
                                                      .anyMatch(
                                                          nextHopIp ->
                                                              recvReplies.containsIp(
                                                                  nextHopIp, ImmutableMap.of())))
                                          .collect(ImmutableSet.toImmutableSet());
                                  routesByEdgeBuilder.put(edge, routes);
                                });
                          })));
      return routesByEdgeBuilder.build();
    }
  }

  @VisibleForTesting
  Map<String, Map<String, IpSpace>> computeSomeoneReplies(Topology topology) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeSomeoneReplies")
            .startActive()) {
      assert span != null; // avoid unused warning
      Map<String, Map<String, AclIpSpace.Builder>> someoneRepliesByNode = new HashMap<>();
      topology
          .getEdges()
          .forEach(
              edge ->
                  someoneRepliesByNode
                      .computeIfAbsent(edge.getNode1(), n -> new HashMap<>())
                      .computeIfAbsent(edge.getInt1(), i -> AclIpSpace.builder())
                      .thenPermitting((_arpReplies.get(edge.getNode2()).get(edge.getInt2()))));
      return someoneRepliesByNode.entrySet().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey /* hostname */,
                  someoneRepliesByNodeEntry ->
                      someoneRepliesByNodeEntry.getValue().entrySet().stream()
                          .collect(
                              ImmutableMap.toImmutableMap(
                                  Entry::getKey /* interface */,
                                  someoneRepliesByInterfaceEntry ->
                                      someoneRepliesByInterfaceEntry.getValue().build()))));
    }
  }

  @Override
  public Map<String, Map<String, IpSpace>> getArpReplies() {
    return _arpReplies;
  }

  @Override
  public Map<Edge, IpSpace> getArpTrueEdge() {
    return _arpTrueEdge;
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachableOrExitsNetwork() {
    /* The old NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK disposition is all dst IPs for which ARP
     * fails.
     */
    return _arpFalse;
  }

  @Override
  public Map<String, Map<String, IpSpace>> getNullRoutedIps() {
    return _nullRoutedIps;
  }

  @Override
  public Map<String, Map<String, IpSpace>> getRoutableIps() {
    return _routableIps;
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getDeliveredToSubnet() {
    return _deliveredToSubnet;
  }

  private Stream<Entry<String, IpSpace>> getInterfaceIpSpaceEntries(
      Map<String, Map<String, IpSpace>> vrfInterfaceIpSpaceMap) {
    return vrfInterfaceIpSpaceMap.values().stream().flatMap(entry -> entry.entrySet().stream());
  }

  private Map<String, Map<String, BDD>> computeInterfaceHostSubnetIpBDDs() {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeInterfaceHostSubnetIpBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _interfaceHostSubnetIps,
          Entry::getKey /* host name */,
          nodeEntry ->
              getInterfaceIpSpaceEntries(nodeEntry.getValue())
                  .collect(
                      ImmutableMap.toImmutableMap(
                          Entry::getKey,
                          ifaceEntry -> _ipSpaceToBDD.visit(ifaceEntry.getValue()))));
    }
  }

  private static Map<String, Map<String, Map<String, IpSpace>>> union(
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces1,
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces2) {
    return merge(ipSpaces1, ipSpaces2, AclIpSpace::union);
  }

  private static Map<String, Map<String, Map<String, IpSpace>>> intersection(
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces1,
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces2) {
    return merge(ipSpaces1, ipSpaces2, AclIpSpace::intersection);
  }

  private static Map<String, Map<String, Map<String, IpSpace>>> merge(
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces1,
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaces2,
      BiFunction<IpSpace, IpSpace, IpSpace> op) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.merge").startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          ipSpaces1,
          Entry::getKey, /* hostname */
          nodeEntry -> {
            Map<String, Map<String, IpSpace>> nodeIpSpace2 = ipSpaces2.get(nodeEntry.getKey());
            return toImmutableMap(
                nodeEntry.getValue(),
                Entry::getKey, /* vrf */
                vrfEntry -> {
                  Map<String, IpSpace> vrfIpSpaces2 = nodeIpSpace2.get(vrfEntry.getKey());
                  return toImmutableMap(
                      vrfEntry.getValue(),
                      Entry::getKey, /* interface */
                      ifaceEntry ->
                          op.apply(ifaceEntry.getValue(), vrfIpSpaces2.get(ifaceEntry.getKey())));
                });
          });
    }
  }

  @VisibleForTesting
  static Map<String, Map<String, Map<String, IpSpace>>> computeInterfaceHostSubnetIps(
      Map<String, Configuration> configs) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeInterfaceHostSubnetIps")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          configs,
          Entry::getKey, /* hostname */
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue().getVrfs(),
                  Entry::getKey, /* vrf */
                  vrfEntry ->
                      toImmutableMap(
                          vrfEntry.getValue().getInterfaces(),
                          Entry::getKey, /* interface */
                          ifaceEntry ->
                              firstNonNull(
                                  AclIpSpace.union(
                                      ifaceEntry.getValue().getAllAddresses().stream()
                                          .map(InterfaceAddress::getPrefix)
                                          .map(Prefix::toHostIpSpace)
                                          .collect(ImmutableList.toImmutableList())),
                                  EmptyIpSpace.INSTANCE))));
    }
  }

  private IpSpace computeInternalIps() {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeInternalIps").startActive()) {
      assert span != null; // avoid unused warning
      return firstNonNull(
          AclIpSpace.union(
              _interfaceHostSubnetIps.values().stream()
                  .flatMap(vrfSubnetIps -> vrfSubnetIps.values().stream())
                  .flatMap(ifaceSubnetIps -> ifaceSubnetIps.values().stream())
                  .collect(Collectors.toList())),
          EmptyIpSpace.INSTANCE);
    }
  }

  /*
   * Necessary and sufficient: Arping dst ip and the dst IP is not owned but is in an interface
   * subnet.
   */
  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeDeliveredToSubnet() {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeDeliveredToSubnet")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          intersection(_arpFalseDestIp, _interfaceHostSubnetIps),
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry ->
                      toImmutableMap(
                          vrfEntry.getValue(),
                          Entry::getKey,
                          ifaceEntry -> AclIpSpace.difference(ifaceEntry.getValue(), _ownedIps))));
    }
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getExitsNetwork() {
    return _exitsNetwork;
  }

  /*
   * Necessary and sufficient: The connected subnet is not full, the dest IP is external,
   * and path is not expected to come back into network (i.e. the ARP IP is also external).
   */
  @VisibleForTesting
  IpSpace computeExitsNetworkPerInterface(String hostname, String vrfName, String interfaceName) {
    // the connected subnet is full
    if (!_interfacesWithMissingDevices.get(hostname).contains(interfaceName)) {
      return EmptyIpSpace.INSTANCE;
    }

    IpSpace dstIpsWithUnownedNextHopIpArpFalsePerInterface =
        _dstIpsWithUnownedNextHopIpArpFalse.get(hostname).get(vrfName).get(interfaceName);

    // Returns the union of the following 2 cases:
    // 1. Arp for dst ip and dst ip is external
    // 2. Arp for next hop ip, next hop ip is not owned by any interfaces, and dst ip is external
    return AclIpSpace.intersection(
        // dest ip is external
        _externalIps,
        // arp for dst Ip OR arp for external next-hop IP
        AclIpSpace.union(
            _arpFalseDestIp.get(hostname).get(vrfName).get(interfaceName),
            dstIpsWithUnownedNextHopIpArpFalsePerInterface));
  }

  private Map<String, Map<String, Map<String, IpSpace>>> computeExitsNetwork(
      Map<String, Configuration> configurations) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeExitsNetwork").startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          configurations,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue().getVrfs(),
                  Entry::getKey,
                  vrfEntry ->
                      toImmutableMap(
                          vrfEntry.getValue().getInterfaces(),
                          Entry::getKey,
                          ifaceEntry -> {
                            String hostname = nodeEntry.getKey();
                            String vrfName = vrfEntry.getKey();
                            String ifaceName = ifaceEntry.getKey();
                            return computeExitsNetworkPerInterface(hostname, vrfName, ifaceName);
                          })));
    }
  }

  /*
   * Necessary and sufficient: The connected subnet is not full, and
   * when arping for dst ip, dst ip is internal but not in the interface subnet, when
   * arping for next hop ip, either next hop ip is owned by interfaces or dst ip is internal.
   */
  @VisibleForTesting
  IpSpace computeInsufficientInfoPerInterface(
      String hostname, String vrfName, String interfaceName) {
    // If interface is full (no missing devices), it cannot be insufficient info
    if (!_interfacesWithMissingDevices.get(hostname).contains(interfaceName)) {
      return EmptyIpSpace.INSTANCE;
    }

    IpSpace ipSpaceElsewhere =
        AclIpSpace.difference(
            _internalIps, _interfaceHostSubnetIps.get(hostname).get(vrfName).get(interfaceName));

    // case 1: arp for dst ip, dst ip is internal but not in any subnet of the interface
    IpSpace ipSpaceInternalDstIp =
        AclIpSpace.intersection(
            _arpFalseDestIp.get(hostname).get(vrfName).get(interfaceName), ipSpaceElsewhere);

    // case 2: arp for nhip, nhip is not owned by interfaces, dst ip is internal
    IpSpace dstIpsWithUnownedNextHopIpArpFalsePerInterafce =
        _dstIpsWithUnownedNextHopIpArpFalse.get(hostname).get(vrfName).get(interfaceName);

    IpSpace ipSpaceInternalDstIpUnownedNexthopIp =
        AclIpSpace.intersection(dstIpsWithUnownedNextHopIpArpFalsePerInterafce, _internalIps);

    // case 3: arp for nhip, nhip is owned by some interfaces
    IpSpace ipSpaceOwnedNextHopIp =
        _dstIpsWithOwnedNextHopIpArpFalse.get(hostname).get(vrfName).get(interfaceName);

    return AclIpSpace.union(
        ipSpaceInternalDstIp, ipSpaceInternalDstIpUnownedNexthopIp, ipSpaceOwnedNextHopIp);
  }

  private Map<String, Map<String, Map<String, IpSpace>>> computeInsufficientInfo(
      Map<String, Configuration> configurations) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeInsufficientInfo")
            .startActive()) {
      assert span != null; // avoid unused warning

      return toImmutableMap(
          configurations,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue().getVrfs(),
                  Entry::getKey,
                  vrfEntry ->
                      toImmutableMap(
                          vrfEntry.getValue().getInterfaces(),
                          Entry::getKey,
                          ifaceEntry -> {
                            String hostname = nodeEntry.getKey();
                            String vrfName = vrfEntry.getKey();
                            String ifaceName = ifaceEntry.getKey();
                            return computeInsufficientInfoPerInterface(
                                hostname, vrfName, ifaceName);
                          })));
    }
  }

  /*
   * Necessary and sufficient: No ARP response, and either:
   * 1. the interface is full, or
   * 2. we ARPed for a dest IP that is in a conncected subnet and is owned in the snapshot.
   *
   * An interface is full if all subnets connected to it are full.
   */
  Map<String, Map<String, Map<String, IpSpace>>> computeNeighborUnreachable() {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeNeighborUnreachable")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _arpFalse,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry ->
                      toImmutableMap(
                          vrfEntry.getValue(),
                          Entry::getKey,
                          ifaceEntry -> {
                            String node = nodeEntry.getKey();
                            String vrf = vrfEntry.getKey();
                            String iface = ifaceEntry.getKey();

                            IpSpace arpFalse = ifaceEntry.getValue();

                            IpSpace arpFalseDstIpOwnedAndInSubnet =
                                AclIpSpace.intersection(
                                    _arpFalseDestIp.get(node).get(vrf).get(iface),
                                    _interfaceHostSubnetIps.get(node).get(vrf).get(iface),
                                    _ownedIps);

                            return _interfacesWithMissingDevices.get(node).contains(iface)
                                ? arpFalseDstIpOwnedAndInSubnet
                                : arpFalse;
                          })));
    }
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachable() {
    return _neighborUnreachable;
  }

  // If one subnet of an interface has missing devices, then packets going out of the interface
  // may potentially be sent to the subnet and be forwarded further. Therefore, instead of consider
  // whether each subnet has missing devices, we just need to consider if one of the subnets has
  // missing devices.
  @VisibleForTesting
  boolean hasMissingDevicesOnInterface(String hostname, String ifaceName) {
    // ips in interface subnet has at least one unowned IP
    return !_interfaceHostSubnetIpBDDs.get(hostname).get(ifaceName).and(_unownedIpsBDD).isZero();
  }

  private Map<String, Set<String>> computeInterfacesWithMissingDevices(
      Map<String, Configuration> configurations) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeInterfacesWithMissingDevices")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          configurations,
          Entry::getKey,
          nodeEntry ->
              nodeEntry.getValue().getAllInterfaces().entrySet().stream()
                  .filter(
                      ifaceEntry ->
                          hasMissingDevicesOnInterface(nodeEntry.getKey(), ifaceEntry.getKey()))
                  .map(Entry::getKey)
                  .collect(Collectors.toSet()));
    }
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getInsufficientInfo() {
    return _insufficientInfo;
  }

  private Map<String, Map<String, Map<String, IpSpace>>> computeDstIpsWithOwnedNextHopIpArpFalse(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeDstIpsWithOwnedNextHopIpArpFalse")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _routesWithOwnedNextHopIpArpFalse,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry -> {
                    Map<Prefix, IpSpace> vrfMatchingIps =
                        matchingIps.get(nodeEntry.getKey()).get(vrfEntry.getKey());
                    return toImmutableMap(
                        vrfEntry.getValue(),
                        Entry::getKey,
                        ifaceEntry ->
                            computeRouteMatchConditions(ifaceEntry.getValue(), vrfMatchingIps));
                  }));
    }
  }

  private Map<String, Map<String, Map<String, IpSpace>>> computeDstIpsWithUnownedNextHopIpArpFalse(
      Map<String, Map<String, Map<Prefix, IpSpace>>> matchingIps) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("ForwardingAnalysisImpl.computeDstIpsWithUnownedNextHopIpArpFalse")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _routesWithUnownedNextHopIpArpFalse,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry -> {
                    Map<Prefix, IpSpace> vrfMatchingIps =
                        matchingIps.get(nodeEntry.getKey()).get(vrfEntry.getKey());
                    return toImmutableMap(
                        vrfEntry.getValue(),
                        Entry::getKey,
                        ifaceEntry ->
                            computeRouteMatchConditions(ifaceEntry.getValue(), vrfMatchingIps));
                  }));
    }
  }

  private IpSpace computeOwnedIps() {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("ForwardingAnalysisImpl.computeOwnedIps").startActive()) {
      assert span != null; // avoid unused warning
      return IpWildcardSetIpSpace.builder()
          .including(
              _interfaceOwnedIps.values().stream()
                  .flatMap(ifaceMap -> ifaceMap.values().stream())
                  .flatMap(Collection::stream)
                  .map(IpWildcard::new)
                  .collect(Collectors.toList()))
          .build();
    }
  }

  private BDD computeUnownedIpsBDD() {
    return _ipSpaceToBDD.visit(_ownedIps).not();
  }
}
