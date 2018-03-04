package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class DataPlaneArpAnalysis implements ArpAnalysis {

  private final Map<String, Map<String, IpAddressAcl>> _arpReplies;

  private final Map<
          String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpAddressAcl>>>>>>
      _arpRequests;

  public DataPlaneArpAnalysis(
      Map<String, Configuration> configurations,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Map<String, Map<String, Fib>> fibs,
      Topology topology) {
    _arpReplies = computeArpReplies(configurations, ribs, fibs);
    _arpRequests = computeArpRequests(fibs, topology);
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
  private Map<String, Map<String, IpAddressAcl>> computeArpReplies(
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
                  return computeArpRepliesByInterface(
                      interfaces, fibsByVrf, routableIpsByVrf, ribsByVrf);
                }));
  }

  private Map<String, IpAddressAcl> computeArpRepliesByInterface(
      Map<String, Interface> interfaces,
      Map<String, Fib> fibsByVrf,
      Map<String, IpSpace> routableIpsByVrf,
      SortedMap<String, GenericRib<AbstractRoute>> ribsByVrf) {
    return ribsByVrf
        .entrySet() // vrfName -> RIB
        .stream()
        /*
         * Interfaces are partitioned by VRF, so we can safely flatten out a stream
         * of them from the VRFs without worrying about duplicate keys.
         */
        .flatMap(
            ribsByVrfEntry -> {
              String vrf = ribsByVrfEntry.getKey();
              Fib fib = fibsByVrf.get(vrf);
              GenericRib<AbstractRoute> rib = ribsByVrfEntry.getValue();
              Map<Prefix, IpSpace> matchingIpsByPrefix = rib.getMatchingIps();
              Map<String, Set<IpSpace>> interfaceIpSpaces =
                  computeInterfaceIpSpaces(rib, fib, matchingIpsByPrefix);
              IpSpace routableIpsForThisVrf = routableIpsByVrf.get(vrf);
              return interfaceIpSpaces
                  .entrySet()
                  .stream()
                  .map(
                      interfaceIpSpacesEntry ->
                          Maps.immutableEntry(
                              interfaceIpSpacesEntry.getKey(),
                              computeInterfaceIpAddressAcl(
                                  interfaces.get(interfaceIpSpacesEntry.getKey()),
                                  routableIpsForThisVrf,
                                  interfaceIpSpacesEntry.getValue())));
            })
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  private Map<
          String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpAddressAcl>>>>>>
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

  private Map<ArpIpChoice, Set<IpAddressAcl>> computeArpRequestsFromNextHopIps(
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
                                receiverReplies -> receiverReplies.permits(arpIpChoice.getIp()));
                    if (someoneWillReply) {
                      return ImmutableSet.of(IpAddressAcl.PERMIT_ALL);
                    } else {
                      return ImmutableSet.of(IpAddressAcl.DENY_ALL);
                    }
                  }));
    }
  }

  private Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpAddressAcl>>>>
      computeFibArpRequests(String hostname, String vrfName, Fib fib, Topology topology) {
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

  private IpAddressAcl computeInterfaceIpAddressAcl(
      Interface iface, IpSpace routableIpsForThisVrf, Set<IpSpace> ipSpaces) {
    ImmutableList.Builder<IpAddressAclLine> lines = ImmutableList.builder();
    IpSpace ipsAssignedToThisInterface = computeIpsAssignedToThisInterface(iface);
    /* Accept IPs assigned to this interface */
    lines.add(new IpAddressAclLine(ipsAssignedToThisInterface, LineAction.ACCEPT));

    if (iface.getProxyArp()) {
      /* Reject IPs routed through this interface */
      ipSpaces
          .stream()
          .map(ipSpace -> new IpAddressAclLine(ipSpace, LineAction.REJECT))
          .forEach(lines::add);

      /* Accept all other routable IPs */
      lines.add(new IpAddressAclLine(routableIpsForThisVrf, LineAction.ACCEPT));
    }
    return IpAddressAcl.builder().setLines(lines.build()).build();
  }

  private Map<String, Set<IpSpace>> computeInterfaceIpSpaces(
      GenericRib<AbstractRoute> rib, Fib fib, Map<Prefix, IpSpace> matchingIpsByPrefix) {
    Map<String, ImmutableSet.Builder<IpSpace>> interfaceIpSpacesBuilders = new HashMap<>();
    rib.getRoutes()
        .forEach(
            route -> {
              IpSpace matchingIps = matchingIpsByPrefix.get(route.getNetwork());
              fib.getNextHopInterfaces()
                  .get(route)
                  .keySet()
                  .forEach(
                      ifaceName -> {
                        interfaceIpSpacesBuilders
                            .computeIfAbsent(ifaceName, n -> ImmutableSet.builder())
                            .add(matchingIps);
                      });
            });
    return interfaceIpSpacesBuilders
        .entrySet()
        .stream()
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().build()));
  }

  private IpSpace computeIpsAssignedToThisInterface(Interface iface) {
    IpSpace.Builder ipsAssignedToThisInterfaceBuilder = IpSpace.builder();
    iface
        .getAllAddresses()
        .stream()
        .map(InterfaceAddress::getIp)
        .forEach(ip -> ipsAssignedToThisInterfaceBuilder.including(new IpWildcard(ip)));
    IpSpace ipsAssignedToThisInterface = ipsAssignedToThisInterfaceBuilder.build();
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

  @Override
  public Map<String, Map<String, IpAddressAcl>> getArpReplies() {
    return _arpReplies;
  }

  @Override
  public Map<
          String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpAddressAcl>>>>>>
      getArpRequests() {
    return _arpRequests;
  }
}
