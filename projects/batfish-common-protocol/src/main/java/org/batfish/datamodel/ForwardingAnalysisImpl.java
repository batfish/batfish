package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.CommonUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
  // a set of routes that with external next hop ip (i.e., ips not in snapshot) but no arp replies
  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      _routesWithExternalNextHopIpArpFalse;

  // mapping: node name -> vrf name -> interface name ->
  // a set of routes that with external next hop ip (i.e., ips not in snapshot) but no arp replies
  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      _routesWithInternalNextHopIpArpFalse;

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

  private final Map<String, Map<String, Map<String, IpSpace>>> _dstIpsWithExternalNextHopIpArpFalse;

  private final Map<String, Map<String, Map<String, IpSpace>>> _dstIpsWithInternalNextHopIpArpFalse;

  private final IpSpaceToBDD _ipSpaceToBDD;

  private final Map<String, Map<String, BDD>> _interfaceHostSubnetIpBDDs;

  // ips belonging to any interface in the network
  private final IpSpace _ownedIps;

  // ips belonging to any subnet in the network
  private final IpSpace _internalIps;

  // ips not belonging to any subnet in the network
  private final IpSpace _externalIps;

  private final BDD _internalIpsBDD;

  private final BDD _unownedIpsBDD;

  private static IpSpaceToBDD initIpSpaceToBDD() {
    BDDPacket bddPacket = new BDDPacket();
    return new IpSpaceToBDD(bddPacket.getFactory(), bddPacket.getDstIp());
  }

  public ForwardingAnalysisImpl(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Map<String, Map<String, Fib>> fibs,
      Topology topology) {
    // TODO accept IpSpaceToBDD as parameter
    _ipSpaceToBDD = initIpSpaceToBDD();
    _interfaceHostSubnetIps = computeInterfaceHostSubnetIps(configurations);
    _interfaceOwnedIps = TopologyUtil.computeInterfaceOwnedIps(configurations, false);
    _ownedIps = computeOwnedIps();
    _unownedIpsBDD = computeUnownedIpsBDD();
    _internalIps = computeInternalIps();
    _internalIpsBDD = _ipSpaceToBDD.visit(_internalIps);
    _externalIps = _internalIps.complement();
    _interfaceHostSubnetIpBDDs = computeInterfaceHostSubnetIpBDDs();
    _interfacesWithMissingDevices = computeInterfacesWithMissingDevices(configurations);
    _nullRoutedIps = computeNullRoutedIps(ribs, fibs);
    _routableIps = computeRoutableIps(ribs);
    _routesWithNextHop = computeRoutesWithNextHop(configurations, fibs);
    _ipsRoutedOutInterfaces = computeIpsRoutedOutInterfaces(ribs);
    _arpReplies = computeArpReplies(configurations, ribs);
    _someoneReplies = computeSomeoneReplies(topology);
    _routesWithNextHopIpArpFalse = computeRoutesWithNextHopIpArpFalse(fibs);
    _routesWithExternalNextHopIpArpFalse = computeRoutesWithExternalNextHopIpArpFalse();
    _routesWithInternalNextHopIpArpFalse = computeRoutesWithInternalNextHopIpArpFalse();
    _arpFalseNextHopIp = computeArpFalseNextHopIp(ribs);
    _routesWithNextHopIpArpTrue = computeRoutesWithNextHopIpArpTrue(fibs, topology);
    _arpTrueEdgeNextHopIp = computeArpTrueEdgeNextHopIp(configurations, ribs);
    _routesWhereDstIpCanBeArpIp = computeRoutesWhereDstIpCanBeArpIp(fibs);
    _arpFalseDestIp = computeArpFalseDestIp(ribs);
    _arpFalse = computeArpFalse();
    _routesWithDestIpEdge = computeRoutesWithDestIpEdge(topology);
    _arpTrueEdgeDestIp = computeArpTrueEdgeDestIp(configurations, ribs);
    _arpTrueEdge = computeArpTrueEdge();
    _dstIpsWithExternalNextHopIpArpFalse =
        computeDstIpsWithExternalNextHopIpArpFalse(configurations, ribs);
    _dstIpsWithInternalNextHopIpArpFalse =
        computeDstIpsWithInternalNextHopIpArpFalse(configurations, ribs);
    _deliveredToSubnet = computeDeliveredToSubnet();
    _exitsNetwork = computeExitsNetwork(configurations);
    _insufficientInfo = computeInsufficientInfo(configurations);
    _neighborUnreachable = computeNeighborUnreachable();
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
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithExternalNextHopIpArpFalse,
      Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithInternalNextHopIpArpFalse,
      Map<String, Map<String, Map<String, IpSpace>>> dstIpsWithExternalNextHopIpArpFalse,
      Map<String, Map<String, Map<String, IpSpace>>> dstIpsWithInternalNextHopIpArpFalse,
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
    _routesWithExternalNextHopIpArpFalse = routesWithExternalNextHopIpArpFalse;
    _routesWithInternalNextHopIpArpFalse = routesWithInternalNextHopIpArpFalse;
    _interfaceHostSubnetIps = interfaceHostSubnetIps;
    _interfacesWithMissingDevices = interfacesWithMissingDevices;
    _neighborUnreachable = null;
    _deliveredToSubnet = null;
    _insufficientInfo = null;
    _exitsNetwork = null;
    _dstIpsWithInternalNextHopIpArpFalse = dstIpsWithInternalNextHopIpArpFalse;
    _dstIpsWithExternalNextHopIpArpFalse = dstIpsWithExternalNextHopIpArpFalse;

    _ipSpaceToBDD = initIpSpaceToBDD();
    _internalIps = internalIps;
    _externalIps = _internalIps.complement();
    _interfaceHostSubnetIpBDDs = computeInterfaceHostSubnetIpBDDs();
    _internalIpsBDD = _ipSpaceToBDD.visit(_internalIps);
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
  Map<String, Map<String, IpSpace>> computeArpReplies(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    Map<String, Map<String, IpSpace>> routableIpsByNodeVrf = computeRoutableIpsByNodeVrf(ribs);
    return toImmutableMap(
        configurations,
        Entry::getKey,
        nodeEntry -> {
          String hostname = nodeEntry.getKey();
          Map<String, Interface> interfaces = nodeEntry.getValue().getAllInterfaces();
          Map<String, IpSpace> routableIpsByVrf = routableIpsByNodeVrf.get(hostname);
          Map<String, IpSpace> ipsRoutedOutInterfaces = _ipsRoutedOutInterfaces.get(hostname);
          return computeArpRepliesByInterface(interfaces, routableIpsByVrf, ipsRoutedOutInterfaces);
        });
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
    return Sets.union(_arpTrueEdgeDestIp.keySet(), _arpTrueEdgeNextHopIp.keySet())
        .stream()
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

  @VisibleForTesting
  Map<Edge, IpSpace> computeArpTrueEdgeDestIp(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return _routesWithDestIpEdge
        .entrySet()
        .stream()
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
                  GenericRib<AbstractRoute> rib = ribs.get(hostname).get(vrf);
                  IpSpace dstIpMatchesSomeRoutePrefix = computeRouteMatchConditions(routes, rib);
                  String recvNode = edge.getNode2();
                  String recvInterface = edge.getInt2();
                  IpSpace recvReplies = _arpReplies.get(recvNode).get(recvInterface);
                  return AclIpSpace.rejecting(dstIpMatchesSomeRoutePrefix.complement())
                      .thenPermitting(recvReplies)
                      .build();
                }));
  }

  @VisibleForTesting
  Map<Edge, IpSpace> computeArpTrueEdgeNextHopIp(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return _routesWithNextHopIpArpTrue
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* edge */,
                routesWithNextHopIpArpTrueEntry -> {
                  Edge edge = routesWithNextHopIpArpTrueEntry.getKey();
                  String hostname = edge.getNode1();
                  String iface = edge.getInt1();
                  String vrf =
                      configurations.get(hostname).getAllInterfaces().get(iface).getVrfName();
                  GenericRib<AbstractRoute> rib = ribs.get(hostname).get(vrf);
                  Set<AbstractRoute> routes = routesWithNextHopIpArpTrueEntry.getValue();
                  return computeRouteMatchConditions(routes, rib);
                }));
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
    Set<Ip> ips = _interfaceOwnedIps.get(iface.getOwner().getHostname()).get(iface.getName());
    if (ips == null || ips.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }
    IpWildcardSetIpSpace.Builder ipsAssignedToThisInterfaceBuilder = IpWildcardSetIpSpace.builder();
    ips.forEach(ip -> ipsAssignedToThisInterfaceBuilder.including(new IpWildcard(ip)));
    IpWildcardSetIpSpace ipsAssignedToThisInterface = ipsAssignedToThisInterfaceBuilder.build();
    return ipsAssignedToThisInterface;
  }

  @VisibleForTesting
  Map<String, Map<String, IpSpace>> computeIpsRoutedOutInterfaces(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return toImmutableMap(
        _routesWithNextHop,
        Entry::getKey /* hostname */,
        nodeEntry -> {
          String hostname = nodeEntry.getKey();
          return nodeEntry
              .getValue()
              .entrySet()
              .stream()
              .flatMap(
                  vrfEntry -> {
                    String vrf = vrfEntry.getKey();
                    GenericRib<AbstractRoute> rib = ribs.get(hostname).get(vrf);
                    return vrfEntry
                        .getValue()
                        .entrySet()
                        .stream()
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
                                  iface, computeRouteMatchConditions(routes, rib));
                            });
                  })
              .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
        });
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeArpFalse() {
    return union(_arpFalseDestIp, _arpFalseNextHopIp);
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeArpFalseDestIp(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return _routesWhereDstIpCanBeArpIp
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                routesWhereDstIpCanBeArpIpByHostnameEntry -> {
                  String hostname = routesWhereDstIpCanBeArpIpByHostnameEntry.getKey();
                  return routesWhereDstIpCanBeArpIpByHostnameEntry
                      .getValue()
                      .entrySet()
                      .stream()
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Entry::getKey /* vrf */,
                              routesWhereDstIpCanBeArpIpByVrfEntry -> {
                                String vrf = routesWhereDstIpCanBeArpIpByVrfEntry.getKey();
                                return routesWhereDstIpCanBeArpIpByVrfEntry
                                    .getValue()
                                    .entrySet()
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
                                              GenericRib<AbstractRoute> rib =
                                                  ribs.get(hostname).get(vrf);
                                              IpSpace ipsRoutedOutInterface =
                                                  computeRouteMatchConditions(routes, rib);
                                              return AclIpSpace.rejecting(someoneReplies)
                                                  .thenPermitting(ipsRoutedOutInterface)
                                                  .build();
                                            }));
                              }));
                }));
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeArpFalseNextHopIp(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return _routesWithNextHopIpArpFalse
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                routesWithNextHopIpArpFalseByHostnameEntry -> {
                  String hostname = routesWithNextHopIpArpFalseByHostnameEntry.getKey();
                  return routesWithNextHopIpArpFalseByHostnameEntry
                      .getValue()
                      .entrySet()
                      .stream()
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Entry::getKey /* vrf */,
                              routesWithNextHopIpArpFalseByVrfEntry -> {
                                String vrf = routesWithNextHopIpArpFalseByVrfEntry.getKey();
                                return routesWithNextHopIpArpFalseByVrfEntry
                                    .getValue()
                                    .entrySet()
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
                                                    ribs.get(hostname).get(vrf))));
                              }));
                }));
  }

  @VisibleForTesting
  Map<String, Map<String, IpSpace>> computeNullRoutedIps(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Map<String, Map<String, Fib>> fibs) {
    return fibs.entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                fibsByHostnameEntry -> {
                  String hostname = fibsByHostnameEntry.getKey();
                  return fibsByHostnameEntry
                      .getValue()
                      .entrySet()
                      .stream()
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Entry::getKey /* vrf */,
                              fibsByVrfEntry -> {
                                String vrf = fibsByVrfEntry.getKey();
                                Fib fib = fibsByVrfEntry.getValue();
                                GenericRib<AbstractRoute> rib = ribs.get(hostname).get(vrf);
                                Set<AbstractRoute> nullRoutes =
                                    fib.getNextHopInterfaces()
                                        .entrySet()
                                        .stream()
                                        .filter(
                                            nextHopInterfacesByRouteEntry ->
                                                nextHopInterfacesByRouteEntry
                                                    .getValue()
                                                    .keySet()
                                                    .contains(Interface.NULL_INTERFACE_NAME))
                                        .map(Entry::getKey)
                                        .collect(ImmutableSet.toImmutableSet());
                                return computeRouteMatchConditions(nullRoutes, rib);
                              }));
                }));
  }

  @VisibleForTesting
  Map<String, Map<String, IpSpace>> computeRoutableIps(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return ribs.entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                ribsByHostnameEntry ->
                    ribsByHostnameEntry
                        .getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* vrf */,
                                ribsByVrfEntry -> ribsByVrfEntry.getValue().getRoutableIps()))));
  }

  /** Compute for each VRF of each node the IPs that are routable. */
  @VisibleForTesting
  Map<String, Map<String, IpSpace>> computeRoutableIpsByNodeVrf(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return ribs.entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey, // hostname
                ribsByNodeEntry ->
                    ribsByNodeEntry
                        .getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey, // vrfName
                                ribsByVrfEntry -> ribsByVrfEntry.getValue().getRoutableIps()))));
  }

  @VisibleForTesting
  IpSpace computeRouteMatchConditions(Set<AbstractRoute> routes, GenericRib<AbstractRoute> rib) {
    Map<Prefix, IpSpace> matchingIps = rib.getMatchingIps();
    // get the union of IpSpace that match one of the routes
    return AclIpSpace.permitting(
            routes
                .stream()
                .map(AbstractRoute::getNetwork)
                .collect(ImmutableSet.toImmutableSet())
                .stream()
                .map(matchingIps::get))
        .build();
  }

  /*
   * Mapping: hostname -&gt; vrfname -&gt; interfacename -&gt; a set of routes where each route
   * has at least one unset final next hop ip
   */
  @VisibleForTesting
  Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWhereDstIpCanBeArpIp(
      Map<String, Map<String, Fib>> fibs) {
    return _routesWithNextHop
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                routesWithNextHopByHostnameEntry -> {
                  String hostname = routesWithNextHopByHostnameEntry.getKey();
                  return routesWithNextHopByHostnameEntry
                      .getValue()
                      .entrySet()
                      .stream()
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Entry::getKey /* vrf */,
                              routesWithNextHopByVrfEntry -> {
                                String vrf = routesWithNextHopByVrfEntry.getKey();
                                Fib fib = fibs.get(hostname).get(vrf);
                                return routesWithNextHopByVrfEntry
                                    .getValue()
                                    .entrySet()
                                    .stream()
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

  @VisibleForTesting
  Map<Edge, Set<AbstractRoute>> computeRoutesWithDestIpEdge(Topology topology) {
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
                              receiver -> routesByEdgeBuilder.put(new Edge(out, receiver), routes));
                        })));
    return routesByEdgeBuilder.build();
  }

  /* Mapping: hostname -&gt; vrfname -&gt; interfacename -&gt; set of associated routes (i.e.,
   * routes that use the interface as outgoing interface */
  @VisibleForTesting
  Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWithNextHop(
      Map<String, Configuration> configurations, Map<String, Map<String, Fib>> fibs) {
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
                        ifaceEntry ->
                            fibs.get(nodeEntry.getKey())
                                .get(vrfEntry.getKey())
                                .getRoutesByNextHopInterface()
                                .getOrDefault(ifaceEntry.getKey(), ImmutableSet.of()))));
  }

  Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      computeRoutesWithNextHopIpArpFalseFilter(Function<AbstractRoute, Boolean> routeFilter) {
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
                            routesWithNextHopByOutInterfaceEntry
                                .getValue()
                                .stream()
                                .filter(routeFilter::apply)
                                .collect(Collectors.toSet()))));
  }

  Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      computeRoutesWithExternalNextHopIpArpFalse() {
    return computeRoutesWithNextHopIpArpFalseFilter(route -> !isInternal(route.getNextHopIp()));
  }

  Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      computeRoutesWithInternalNextHopIpArpFalse() {
    return computeRoutesWithNextHopIpArpFalseFilter(route -> isInternal(route.getNextHopIp()));
  }

  @VisibleForTesting
  Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWithNextHopIpArpFalse(
      Map<String, Map<String, Fib>> fibs) {
    return _routesWithNextHop
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                routesWithNextHopByHostnameEntry -> {
                  String hostname = routesWithNextHopByHostnameEntry.getKey();
                  return routesWithNextHopByHostnameEntry
                      .getValue()
                      .entrySet()
                      .stream()
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Entry::getKey /* vrf */,
                              routesWithNextHopByVrfEntry -> {
                                String vrf = routesWithNextHopByVrfEntry.getKey();
                                return routesWithNextHopByVrfEntry
                                    .getValue()
                                    .entrySet()
                                    .stream()
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

  @VisibleForTesting
  Set<AbstractRoute> computeRoutesWithNextHopIpArpFalseForInterface(
      Fib fib, String hostname, String outInterface, Set<AbstractRoute> candidateRoutes) {
    IpSpace someoneReplies =
        _someoneReplies
            .getOrDefault(hostname, ImmutableMap.of())
            .getOrDefault(outInterface, EmptyIpSpace.INSTANCE);
    return candidateRoutes
        .stream()
        .filter(
            candidateRoute ->
                fib.getNextHopInterfaces()
                    .get(candidateRoute)
                    .get(outInterface)
                    .keySet()
                    .stream()
                    .filter(ip -> !ip.equals(Route.UNSET_ROUTE_NEXT_HOP_IP))
                    .anyMatch(
                        nextHopIp -> !someoneReplies.containsIp(nextHopIp, ImmutableMap.of())))
        .collect(ImmutableSet.toImmutableSet());
  }

  @VisibleForTesting
  Map<Edge, Set<AbstractRoute>> computeRoutesWithNextHopIpArpTrue(
      Map<String, Map<String, Fib>> fibs, Topology topology) {
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
                                IpSpace recvReplies = _arpReplies.get(recvNode).get(recvInterface);
                                Edge edge = new Edge(out, receiver);
                                Set<AbstractRoute> routes =
                                    candidateRoutes
                                        .stream()
                                        .filter(
                                            route ->
                                                fib.getNextHopInterfaces()
                                                    .get(route)
                                                    .get(outInterface)
                                                    .keySet() /* nextHopIps */
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

  @VisibleForTesting
  Map<String, Map<String, IpSpace>> computeSomeoneReplies(Topology topology) {
    Map<String, Map<String, AclIpSpace.Builder>> someoneRepliesByNode = new HashMap<>();
    topology
        .getEdges()
        .forEach(
            edge ->
                someoneRepliesByNode
                    .computeIfAbsent(edge.getNode1(), n -> new HashMap<>())
                    .computeIfAbsent(edge.getInt1(), i -> AclIpSpace.builder())
                    .thenPermitting((_arpReplies.get(edge.getNode2()).get(edge.getInt2()))));
    return someoneRepliesByNode
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                someoneRepliesByNodeEntry ->
                    someoneRepliesByNodeEntry
                        .getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* interface */,
                                someoneRepliesByInterfaceEntry ->
                                    someoneRepliesByInterfaceEntry.getValue().build()))));
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

  private boolean isInternal(Ip ip) {
    return !_ipSpaceToBDD.toBDD(ip).and(_internalIpsBDD).isZero();
  }

  private Map<String, Map<String, BDD>> computeInterfaceHostSubnetIpBDDs() {
    return toImmutableMap(
        _interfaceHostSubnetIps,
        Entry::getKey /* host name */,
        nodeEntry ->
            nodeEntry
                .getValue()
                .values()
                .stream()
                .flatMap(entry -> entry.entrySet().stream())
                .collect(
                    ImmutableMap.toImmutableMap(
                        Entry::getKey, ifaceEntry -> _ipSpaceToBDD.visit(ifaceEntry.getValue()))));
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

  @VisibleForTesting
  static Map<String, Map<String, Map<String, IpSpace>>> computeInterfaceHostSubnetIps(
      Map<String, Configuration> configs) {
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
                                    ifaceEntry
                                        .getValue()
                                        .getAllAddresses()
                                        .stream()
                                        .map(InterfaceAddress::getPrefix)
                                        .map(ForwardingAnalysisImpl::hostSubnetIpSpace)
                                        .collect(ImmutableList.toImmutableList())),
                                EmptyIpSpace.INSTANCE))));
  }

  @Nonnull
  static IpSpace hostSubnetIpSpace(Prefix prefix) {
    return prefix.getPrefixLength() >= 31
        ? prefix.toIpSpace()
        : AclIpSpace.rejecting(prefix.getStartIp().toIpSpace())
            .thenRejecting(prefix.getEndIp().toIpSpace())
            .thenPermitting(prefix.toIpSpace())
            .build();
  }

  private IpSpace computeInternalIps() {
    return firstNonNull(
        AclIpSpace.union(
            _interfaceHostSubnetIps
                .values()
                .stream()
                .flatMap(vrfSubnetIps -> vrfSubnetIps.values().stream())
                .flatMap(ifaceSubnetIps -> ifaceSubnetIps.values().stream())
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }

  /*
   * Necessary and sufficient: Arping dst ip and the dst IP is not owned but is in an interface
   * subnet.
   */
  @VisibleForTesting
  Map<String, Map<String, Map<String, IpSpace>>> computeDeliveredToSubnet() {
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

    IpSpace dstIpsWithExternalNextHopIpArpFalsePerInterface =
        _dstIpsWithExternalNextHopIpArpFalse.get(hostname).get(vrfName).get(interfaceName);

    // Returns the union of the following 2 cases:
    // 1. Arp for dst ip and dst ip is external
    // 2. Arp for next hop ip, next hop ip is external, and dst ip is external
    return AclIpSpace.intersection(
        // dest ip is external
        _externalIps,
        // arp for dst Ip OR arp for external next-hop IP
        AclIpSpace.union(
            _arpFalseDestIp.get(hostname).get(vrfName).get(interfaceName),
            dstIpsWithExternalNextHopIpArpFalsePerInterface));
  }

  Map<String, Map<String, Map<String, IpSpace>>> computeExitsNetwork(
      Map<String, Configuration> configurations) {
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

  /*
   * Necessary and sufficient: The connected subnet is not full, and
   * when arping for dst ip, dst ip is internal but not in the interface subnet, when
   * arping for next hop ip, either next hop ip or dst ip is internal.
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

    // case 2: arp for nhip, nhip is external, dst ip is internal
    IpSpace dstIpsWithExternalNextHopIpArpFalsePerInterafce =
        _dstIpsWithExternalNextHopIpArpFalse.get(hostname).get(vrfName).get(interfaceName);

    IpSpace ipSpaceInternalDstIpExternalNexthopIp =
        AclIpSpace.intersection(dstIpsWithExternalNextHopIpArpFalsePerInterafce, _internalIps);

    // case 3: arp for nhip, nhip is internal
    IpSpace ipSpaceInternalNextHopIp =
        _dstIpsWithInternalNextHopIpArpFalse.get(hostname).get(vrfName).get(interfaceName);

    return AclIpSpace.union(
        ipSpaceInternalDstIp, ipSpaceInternalDstIpExternalNexthopIp, ipSpaceInternalNextHopIp);
  }

  private Map<String, Map<String, Map<String, IpSpace>>> computeInsufficientInfo(
      Map<String, Configuration> configurations) {
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
                          return computeInsufficientInfoPerInterface(hostname, vrfName, ifaceName);
                        })));
  }

  /*
   * Necessary and sufficient: No ARP response, and either:
   * 1. the interface is full, or
   * 2. we ARPed for a dest IP that is in a conncected subnet and is owned in the snapshot.
   *
   * An interface is full if all subnets connected to it are full.
   */
  Map<String, Map<String, Map<String, IpSpace>>> computeNeighborUnreachable() {
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
    return toImmutableMap(
        configurations,
        Entry::getKey,
        nodeEntry ->
            nodeEntry
                .getValue()
                .getAllInterfaces()
                .entrySet()
                .stream()
                .filter(
                    ifaceEntry ->
                        hasMissingDevicesOnInterface(nodeEntry.getKey(), ifaceEntry.getKey()))
                .map(Entry::getKey)
                .collect(Collectors.toSet()));
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getInsufficientInfo() {
    return _insufficientInfo;
  }

  private Map<String, Map<String, Map<String, IpSpace>>> computeDstIpsWithExternalNextHopIpArpFalse(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
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
                          Set<AbstractRoute> routesWithExternalNextHopIpArpFalse =
                              _routesWithExternalNextHopIpArpFalse
                                  .get(nodeEntry.getKey())
                                  .get(vrfEntry.getKey())
                                  .get(ifaceEntry.getKey());
                          GenericRib<AbstractRoute> rib =
                              ribs.get(nodeEntry.getKey()).get(vrfEntry.getKey());
                          return computeRouteMatchConditions(
                              routesWithExternalNextHopIpArpFalse, rib);
                        })));
  }

  private Map<String, Map<String, Map<String, IpSpace>>> computeDstIpsWithInternalNextHopIpArpFalse(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
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
                          Set<AbstractRoute> routesWithInternalNextHopIpArpFalse =
                              _routesWithInternalNextHopIpArpFalse
                                  .get(nodeEntry.getKey())
                                  .get(vrfEntry.getKey())
                                  .get(ifaceEntry.getKey());
                          GenericRib<AbstractRoute> rib =
                              ribs.get(nodeEntry.getKey()).get(vrfEntry.getKey());
                          return computeRouteMatchConditions(
                              routesWithInternalNextHopIpArpFalse, rib);
                        })));
  }

  private IpSpace computeOwnedIps() {
    return IpWildcardSetIpSpace.builder()
        .including(
            _interfaceOwnedIps
                .values()
                .stream()
                .flatMap(ifaceMap -> ifaceMap.values().stream())
                .flatMap(Collection::stream)
                .map(IpWildcard::new)
                .collect(Collectors.toList()))
        .build();
  }

  private BDD computeUnownedIpsBDD() {
    return _ipSpaceToBDD.visit(_ownedIps).not();
  }
}
