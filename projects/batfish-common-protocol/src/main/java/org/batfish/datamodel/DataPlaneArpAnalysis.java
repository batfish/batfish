package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class DataPlaneArpAnalysis implements ArpAnalysis {

  private final Map<String, Map<String, IpSpace>> _arpReplies;

  private final Map<
          String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
      _arpRequests;

  private final Map<Edge, IpSpace> _arpTrueEdgeDestIp;

  private final Map<Edge, IpSpace> _arpTrueEdgeNextHopIp;

  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      _routesWhereDstIpCanBeArpIp;

  private final Map<Edge, Set<AbstractRoute>> _routesWithDestIpEdge;

  private final Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWithNextHop;

  private final Map<Edge, Set<AbstractRoute>> _routesWithNextHopIpArpTrue;

  private final Map<String, Map<String, IpSpace>> _ipsRoutedOutInterfaces;

  public DataPlaneArpAnalysis(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Map<String, Map<String, Fib>> fibs,
      Topology topology) {
    _routesWithNextHop = computeRoutesWithNextHop(fibs);
    _ipsRoutedOutInterfaces = computeIpsRoutedOutInterfaces(ribs);
    _arpReplies = computeArpReplies(configurations, ribs, fibs);
    _routesWithNextHopIpArpTrue = computeRoutesWithNextHopIpArpTrue(fibs, topology);
    _arpTrueEdgeNextHopIp = computeArpTrueEdgeNextHopIp(configurations, ribs);
    _routesWhereDstIpCanBeArpIp = computeRoutesWhereDstIpCanBeArpIp(fibs);
    _routesWithDestIpEdge = computeRoutesWithDestIpEdge(fibs, topology);
    _arpTrueEdgeDestIp = computeArpTrueEdgeDestIp(configurations, ribs);
    _arpTrueEdge = computeArpTrueEdge();

    _arpRequests = computeArpRequests(fibs, topology);
  }

  private Map<String, Map<String, IpSpace>> computeIpsRoutedOutInterfaces(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
    return _routesWithNextHop
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                routesWithNextHopByHostnameEntry -> {
                  String hostname = routesWithNextHopByHostnameEntry.getKey();
                  ImmutableMap.Builder<String, IpSpace> ipsRoutedOutInterfacesByInterface =
                      ImmutableMap.builder();
                  routesWithNextHopByHostnameEntry
                      .getValue()
                      .forEach(
                          (vrf, routesWithNextHopByInterface) -> {
                            GenericRib<AbstractRoute> rib = ribs.get(hostname).get(vrf);
                            routesWithNextHopByInterface.forEach(
                                (iface, routes) -> {
                                  ipsRoutedOutInterfacesByInterface.put(
                                      iface, computeRouteMatchConditions(routes, rib));
                                });
                          });
                  return ipsRoutedOutInterfacesByInterface.build();
                }));
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
  private Map<String, Map<String, IpSpace>> computeArpReplies(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Map<String, Map<String, Fib>> fibs) {
    Map<String, Map<String, IpSpace>> routableIpsByNodeVrf = computeRoutableIpsByNodeVrf(ribs);
    return ribs.entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey, // hostname
                ribsByNodeEntry -> {
                  String hostname = ribsByNodeEntry.getKey();
                  Map<String, Interface> interfaces = configurations.get(hostname).getInterfaces();
                  Map<String, Fib> fibsByVrf = fibs.get(hostname);
                  Map<String, IpSpace> routableIpsByVrf = routableIpsByNodeVrf.get(hostname);
                  SortedMap<String, GenericRib<AbstractRoute>> ribsByVrf =
                      ribsByNodeEntry.getValue();
                  Map<String, IpSpace> ipsRoutedOutInterfaces =
                      _ipsRoutedOutInterfaces.get(hostname);
                  return computeArpRepliesByInterface(
                      interfaces, fibsByVrf, routableIpsByVrf, ribsByVrf, ipsRoutedOutInterfaces);
                }));
  }

  private Map<String, IpSpace> computeArpRepliesByInterface(
      Map<String, Interface> interfaces,
      Map<String, Fib> fibsByVrf,
      Map<String, IpSpace> routableIpsByVrf,
      SortedMap<String, GenericRib<AbstractRoute>> ribsByVrf,
      Map<String, IpSpace> ipsRoutedOutInterfaces) {
    ImmutableMap.Builder<String, IpSpace> arpRepliesByInterfaceBuilder = ImmutableMap.builder();
    /*
     * Interfaces are partitioned by VRF, so we can safely flatten out a stream
     * of them from the VRFs without worrying about duplicate keys.
     */
    ribsByVrf.forEach(
        (vrf, rib) -> {
          IpSpace routableIpsForThisVrf = routableIpsByVrf.get(vrf);
          ipsRoutedOutInterfaces.forEach(
              (iface, ipsRoutedOutIface) ->
                  arpRepliesByInterfaceBuilder.put(
                      iface,
                      computeInterfaceArpReplies(
                          interfaces.get(iface), routableIpsForThisVrf, ipsRoutedOutIface)));
        });
    return arpRepliesByInterfaceBuilder.build();
  }

  private Map<String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
      computeArpRequests(Map<String, Map<String, Fib>> fibs, Topology topology) {
    return fibs.entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey, // hostname
                fibsByNodeEntry -> {
                  String hostname = fibsByNodeEntry.getKey();
                  return fibsByNodeEntry
                      .getValue()
                      .entrySet()
                      .stream()
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Entry::getKey, // vrfName
                              fibsByVrfEntry -> {
                                String vrfName = fibsByVrfEntry.getKey();
                                return computeFibArpRequests(
                                    hostname, vrfName, fibsByVrfEntry.getValue(), topology);
                              }));
                }));
  }

  private Map<ArpIpChoice, Set<IpSpace>> computeArpRequestsFromNextHopIps(
      String hostname,
      String vrfName,
      String outInterface,
      Set<Ip> nextHopIps,
      Set<NodeInterfacePair> receivers) {
    Set<ArpIpChoice> arpIpChoices =
        nextHopIps.stream().map(ArpIpChoice::of).collect(ImmutableSet.toImmutableSet());
    if (arpIpChoices.equals(ImmutableSet.of(ArpIpChoice.USE_DST_IP))) {
      /*
       * The union of legal ARP replies of each peer (replyingNode,replyingInterface)
       * of the outgoing interface.The ACL is the one generated for the peer
       * (node,interface) pair by algorithm 1.
       */
      return ImmutableMap.of(
          ArpIpChoice.USE_DST_IP,
          receivers
              .stream()
              .map(receiver -> _arpReplies.get(receiver.getHostname()).get(receiver.getInterface()))
              .collect(ImmutableSet.toImmutableSet()));
    } else {
      /* All nextHopIps should be actual IPs. */
      return arpIpChoices
          .stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Function.identity(),
                  arpIpChoice -> {
                    boolean someoneWillReply =
                        receivers
                            .stream()
                            .map(
                                receiver ->
                                    _arpReplies
                                        .get(receiver.getHostname())
                                        .get(receiver.getInterface()))
                            .anyMatch(
                                receiverReplies -> receiverReplies.contains(arpIpChoice.getIp()));
                    if (someoneWillReply) {
                      return ImmutableSet.of(IpAddressAcl.PERMIT_ALL);
                    } else {
                      return ImmutableSet.of(IpAddressAcl.DENY_ALL);
                    }
                  }));
    }
  }

  private Map<Edge, IpSpace> computeArpTrueEdgeDestIp(
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
                  String vrf = configurations.get(hostname).getInterfaces().get(iface).getVrfName();
                  GenericRib<AbstractRoute> rib = ribs.get(hostname).get(vrf);
                  IpSpace dstIpMatchesSomeRoutePrefix = computeRouteMatchConditions(routes, rib);
                  String recvNode = edge.getNode2();
                  String recvInterface = edge.getInt2();
                  IpSpace recvReplies = _arpReplies.get(recvNode).get(recvInterface);
                  return IpAddressAcl.builder()
                      .setLines(
                          ImmutableList.of(
                              IpAddressAclLine.builder()
                                  .setIpSpace(dstIpMatchesSomeRoutePrefix)
                                  .setNegate(true)
                                  .setAction(LineAction.REJECT)
                                  .build(),
                              IpAddressAclLine.builder()
                                  .setIpSpace(recvReplies)
                                  .setNegate(true)
                                  .setAction(LineAction.REJECT)
                                  .build(),
                              IpAddressAclLine.PERMIT_ALL))
                      .build();
                }));
  }

  private final Map<Edge, IpSpace> _arpTrueEdge;

  @Override
  public Map<Edge, IpSpace> getArpTrueEdge() {
    return _arpTrueEdge;
  }

  private Map<Edge, IpSpace> computeArpTrueEdge() {
    return Sets.union(_arpTrueEdgeDestIp.keySet(), _arpTrueEdgeNextHopIp.keySet())
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                edge -> {
                  ImmutableList.Builder<IpAddressAclLine> lines = ImmutableList.builder();
                  IpSpace dstIp = _arpTrueEdgeDestIp.get(edge);
                  if (dstIp != null) {
                    lines.add(IpAddressAclLine.builder().setIpSpace(dstIp).build());
                  }
                  IpSpace nextHopIp = _arpTrueEdgeNextHopIp.get(edge);
                  if (nextHopIp != null) {
                    lines.add(IpAddressAclLine.builder().setIpSpace(nextHopIp).build());
                  }
                  return IpAddressAcl.builder().setLines(lines.build()).build();
                }));
  }

  private Map<Edge, IpSpace> computeArpTrueEdgeNextHopIp(
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
                  String vrf = configurations.get(hostname).getInterfaces().get(iface).getVrfName();
                  GenericRib<AbstractRoute> rib = ribs.get(hostname).get(vrf);
                  Set<AbstractRoute> routes = routesWithNextHopIpArpTrueEntry.getValue();
                  return computeRouteMatchConditions(routes, rib);
                }));
  }

  private Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>> computeFibArpRequests(
      String hostname, String vrfName, Fib fib, Topology topology) {
    return fib.getNextHopInterfaces()
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* route */,
                nextHopInterfacesByRouteEntry ->
                    nextHopInterfacesByRouteEntry
                        .getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey /* outInterface */,
                                nextHopInfoByOutInterfaceEntry -> {
                                  String outInterface = nextHopInfoByOutInterfaceEntry.getKey();
                                  Set<Edge> outInterfaceEdges =
                                      topology
                                          .getInterfaceEdges()
                                          .get(new NodeInterfacePair(hostname, outInterface));
                                  Set<NodeInterfacePair> receivers =
                                      outInterfaceEdges
                                          .stream()
                                          .filter(
                                              edge ->
                                                  edge.getNode1().equals(hostname)
                                                      && edge.getInt1().equals(outInterface))
                                          .map(Edge::getInterface2)
                                          .collect(ImmutableSet.toImmutableSet());
                                  Set<Ip> nextHopIps =
                                      nextHopInfoByOutInterfaceEntry.getValue().keySet();
                                  return computeArpRequestsFromNextHopIps(
                                      hostname, vrfName, outInterface, nextHopIps, receivers);
                                }))));
  }

  private IpSpace computeInterfaceArpReplies(
      Interface iface, IpSpace routableIpsForThisVrf, IpSpace ipsRoutedThroughInterface) {
    ImmutableList.Builder<IpAddressAclLine> lines = ImmutableList.builder();
    IpSpace ipsAssignedToThisInterface = computeIpsAssignedToThisInterface(iface);
    /* Accept IPs assigned to this interface */
    lines.add(IpAddressAclLine.builder().setIpSpace(ipsAssignedToThisInterface).build());

    if (iface.getProxyArp()) {
      /* Reject IPs routed through this interface */
      lines.add(
          IpAddressAclLine.builder()
              .setIpSpace(ipsRoutedThroughInterface)
              .setAction(LineAction.REJECT)
              .build());

      /* Accept all other routable IPs */
      lines.add(IpAddressAclLine.builder().setIpSpace(routableIpsForThisVrf).build());
    }
    return IpAddressAcl.builder().setLines(lines.build()).build();
  }

  private IpSpace computeIpsAssignedToThisInterface(Interface iface) {
    IpWildcardSetIpSpace.Builder ipsAssignedToThisInterfaceBuilder = IpWildcardSetIpSpace.builder();
    iface
        .getAllAddresses()
        .stream()
        .map(InterfaceAddress::getIp)
        .forEach(ip -> ipsAssignedToThisInterfaceBuilder.including(new IpWildcard(ip)));
    IpWildcardSetIpSpace ipsAssignedToThisInterface = ipsAssignedToThisInterfaceBuilder.build();
    return ipsAssignedToThisInterface;
  }

  /** Compute for each VRF of each node the IPs that are routable. */
  private Map<String, Map<String, IpSpace>> computeRoutableIpsByNodeVrf(
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

  private IpSpace computeRouteMatchConditions(
      Set<AbstractRoute> routes, GenericRib<AbstractRoute> rib) {
    Map<Prefix, IpSpace> matchingIps = rib.getMatchingIps();
    return IpAddressAcl.builder()
        .setLines(
            routes
                .stream()
                .map(AbstractRoute::getNetwork)
                .collect(ImmutableSet.toImmutableSet())
                .stream()
                .map(matchingIps::get)
                .map(ipSpace -> IpAddressAclLine.builder().setIpSpace(ipSpace).build())
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>>
      computeRoutesWhereDstIpCanBeArpIp(Map<String, Map<String, Fib>> fibs) {
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
                                              return routesWithNextHopByInterfaceEntry
                                                  .getValue()
                                                  .stream()
                                                  .filter(
                                                      route ->
                                                          fib.getNextHopInterfaces()
                                                              .get(route)
                                                              .get(iface)
                                                              .keySet()
                                                              .contains(
                                                                  Route.UNSET_ROUTE_NEXT_HOP_IP))
                                                  .collect(ImmutableSet.toImmutableSet());
                                            }));
                              }));
                }));
  }

  private Map<Edge, Set<AbstractRoute>> computeRoutesWithDestIpEdge(
      Map<String, Map<String, Fib>> fibs, Topology topology) {
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

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>> computeRoutesWithNextHop(
      Map<String, Map<String, Fib>> fibs) {
    return fibs.entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* hostname */,
                fibsByHostnameEntry -> {
                  return fibsByHostnameEntry
                      .getValue()
                      .entrySet()
                      .stream()
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Entry::getKey /* vrf */,
                              fibsByVrfEntry -> {
                                Fib fib = fibsByVrfEntry.getValue();
                                return getRoutesByNextHopInterface(fib);
                              }));
                }));
  }

  private Map<Edge, Set<AbstractRoute>> computeRoutesWithNextHopIpArpTrue(
      Map<String, Map<String, Fib>> fibs, Topology topology) {
    ImmutableMap.Builder<Edge, Set<AbstractRoute>> routesByEdgeBuilder = ImmutableMap.builder();
    _routesWithNextHop.forEach(
        (hostname, routesWithNextHopByVrf) ->
            routesWithNextHopByVrf.forEach(
                (vrf, routesWithNextHopByInterface) ->
                    routesWithNextHopByInterface.forEach(
                        (outInterface, candidateRoutes) -> {
                          Fib fib = fibs.get(hostname).get(outInterface);
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
                                                        ip -> ip != Route.UNSET_ROUTE_NEXT_HOP_IP)
                                                    .anyMatch(recvReplies::contains))
                                        .collect(ImmutableSet.toImmutableSet());
                                routesByEdgeBuilder.put(edge, routes);
                              });
                        })));
    return routesByEdgeBuilder.build();
  }

  @Override
  public Map<String, Map<String, IpSpace>> getArpReplies() {
    return _arpReplies;
  }

  @Override
  public Map<String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
      getArpRequests() {
    return _arpRequests;
  }

  private Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface(Fib fib) {
    Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> fibNextHopInterfaces =
        fib.getNextHopInterfaces();
    Map<String, ImmutableSet.Builder<AbstractRoute>> routesByNextHopInterface = new HashMap<>();
    fibNextHopInterfaces.forEach(
        (route, nextHopInterfaceMap) ->
            nextHopInterfaceMap
                .keySet()
                .forEach(
                    nextHopInterface ->
                        routesByNextHopInterface
                            .computeIfAbsent(nextHopInterface, n -> ImmutableSet.builder())
                            .add(route)));
    return routesByNextHopInterface
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* interfaceName */,
                routesByNextHopInterfaceEntry -> routesByNextHopInterfaceEntry.getValue().build()));
  }
}
