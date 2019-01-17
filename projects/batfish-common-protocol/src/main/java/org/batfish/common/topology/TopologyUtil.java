package org.batfish.common.topology;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.batfish.common.Pair;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;

public final class TopologyUtil {

  /** Returns true iff the given trunk interface allows its own native vlan. */
  private static boolean trunkWithNativeVlanAllowed(Interface i) {
    return i.getSwitchportMode() == SwitchportMode.TRUNK
        && i.getNativeVlan() != null
        && i.getAllowedVlans().contains(i.getNativeVlan());
  }

  // Precondition: at least one of i1 and i2 is a trunk
  private static void addLayer2TrunkEdges(
      Interface i1,
      Interface i2,
      ImmutableSet.Builder<Layer2Edge> edges,
      Layer1Node node1,
      Layer1Node node2) {
    if (i1.getSwitchportMode() == SwitchportMode.TRUNK
        && i2.getSwitchportMode() == SwitchportMode.TRUNK) {
      // Both sides are trunks, so add edges from n1,v to n2,v for all shared VLANs.
      i1.getAllowedVlans().stream()
          .forEach(
              vlan -> {
                if (Objects.equals(i1.getNativeVlan(), vlan) && trunkWithNativeVlanAllowed(i2)) {
                  // This frame will not be tagged by i1, and i2 accepts untagged frames.
                  edges.add(new Layer2Edge(node1, vlan, node2, vlan, null /* untagged */));
                } else if (i2.getAllowedVlans().contains(vlan)) {
                  // This frame will be tagged by i1 and we can directly check whether i2 allows.
                  edges.add(new Layer2Edge(node1, vlan, node2, vlan, vlan));
                }
              });
    } else if (trunkWithNativeVlanAllowed(i1)) {
      // i1 is a trunk, but the other side is not. The only edge that will come up is i2 receiving
      // untagged packets.
      Integer node2VlanId =
          i2.getSwitchportMode() == SwitchportMode.ACCESS ? i2.getAccessVlan() : null;
      edges.add(new Layer2Edge(node1, i1.getNativeVlan(), node2, node2VlanId, null));
    } else if (trunkWithNativeVlanAllowed(i2)) {
      // i1 is not a trunk, but the other side is. The only edge that will come up is the other
      // side receiving untagged packets and treating them as native VLAN.
      Integer node1VlanId =
          i1.getSwitchportMode() == SwitchportMode.ACCESS ? i1.getAccessVlan() : null;
      edges.add(new Layer2Edge(node1, node1VlanId, node2, i2.getNativeVlan(), null));
    }
  }

  public static @Nonnull Layer1Topology computeLayer1Topology(
      @Nonnull Layer1Topology rawLayer1Topology,
      @Nonnull Map<String, Configuration> configurations) {
    /* Filter out inactive interfaces */
    return new Layer1Topology(
        rawLayer1Topology.getGraph().edges().stream()
            .filter(
                edge -> {
                  Interface i1 = getInterface(edge.getNode1(), configurations);
                  Interface i2 = getInterface(edge.getNode2(), configurations);
                  return i1 != null && i2 != null && i1.getActive() && i2.getActive();
                })
            .collect(ImmutableSet.toImmutableSet()));
  }

  private static Layer2Node find(Layer2Node n, Map<Layer2Node, Layer2Node> m) {
    Layer2Node v = m.get(n);
    if (n == v) {
      return v;
    }
    Layer2Node v1 = find(v, m);
    if (v != v1) {
      m.put(n, v1);
    }
    return v1;
  }

  private static void union(Layer2Node n1, Layer2Node n2, Map<Layer2Node, Layer2Node> m) {
    Layer2Node d1 = find(n1, m);
    Layer2Node d2 = find(n2, m);
    if (d1 != d2) {
      Layer2Node min = d1.compareTo(d2) < 0 ? d1 : d2;
      m.put(d1, min);
      m.put(d2, min);
    }
  }

  private static Layer2Topology computeLayer2Topology(
      Set<Layer2Edge> edges, Map<String, Configuration> configurations) {
    Map<Layer2Node, Layer2Node> domainMap = new HashMap<>();

    // initially, each node is in its own domain
    edges.stream().map(Layer2Edge::getNode1).forEach(n -> domainMap.put(n, n));
    edges.forEach(e -> union(e.getNode1(), e.getNode2(), domainMap));

    // invert to mapping from domain to nodes in the domain
    Map<Layer2Node, Set<Layer2Node>> nodesByDomainRepresentative = new HashMap<>();
    edges.stream()
        .map(Layer2Edge::getNode1)
        .forEach(
            n ->
                nodesByDomainRepresentative
                    .computeIfAbsent(find(n, domainMap), k -> new HashSet<>())
                    .add(n));

    // project to layer3 node broadcast domains
    Set<Set<NodeInterfacePair>> domains =
        nodesByDomainRepresentative.values().stream()
            .map(
                domain ->
                    domain.stream()
                        .map(l2Node -> getInterface(l2Node, configurations))
                        .filter(Objects::nonNull)
                        .filter(TopologyUtil::isLayer3Interface)
                        .map(
                            iface ->
                                new NodeInterfacePair(
                                    iface.getOwner().getHostname(), iface.getName()))
                        .collect(ImmutableSet.toImmutableSet()))
            .collect(ImmutableSet.toImmutableSet());
    return new Layer2Topology(domains);
  }

  private static boolean isLayer3Interface(Interface iface) {
    return iface.getSwitchportMode() == SwitchportMode.NONE;
  }

  private static void computeLayer2EdgesForLayer1Edge(
      @Nonnull Layer1Edge layer1Edge,
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull ImmutableSet.Builder<Layer2Edge> edges) {
    Layer1Node node1 = layer1Edge.getNode1();
    Layer1Node node2 = layer1Edge.getNode2();
    Interface i1 = getInterface(node1, configurations);
    Interface i2 = getInterface(node2, configurations);
    if (i1 == null || i2 == null) {
      return;
    }
    if (i1.getSwitchportMode() == SwitchportMode.TRUNK
        || i2.getSwitchportMode() == SwitchportMode.TRUNK) {
      addLayer2TrunkEdges(i1, i2, edges, node1, node2);
    } else {
      Integer node1VlanId =
          i1.getSwitchportMode() == SwitchportMode.ACCESS ? i1.getAccessVlan() : null;
      Integer node2VlanId =
          i2.getSwitchportMode() == SwitchportMode.ACCESS ? i2.getAccessVlan() : null;
      edges.add(new Layer2Edge(node1, node1VlanId, node2, node2VlanId, null));
    }
  }

  private static void computeLayer2SelfEdges(
      @Nonnull String hostname, @Nonnull Vrf vrf, @Nonnull ImmutableSet.Builder<Layer2Edge> edges) {
    Map<Integer, ImmutableList.Builder<String>> switchportsByVlan = new HashMap<>();
    vrf.getInterfaces().values().stream()
        .filter(Interface::getActive)
        .forEach(
            i -> {
              if (i.getSwitchportMode() == SwitchportMode.TRUNK) {
                i.getAllowedVlans().stream()
                    .forEach(
                        vlan ->
                            switchportsByVlan
                                .computeIfAbsent(vlan, n -> ImmutableList.builder())
                                .add(i.getName()));
              } else if (i.getSwitchportMode() == SwitchportMode.ACCESS
                  && i.getAccessVlan() != null) {
                switchportsByVlan
                    .computeIfAbsent(i.getAccessVlan(), n -> ImmutableList.builder())
                    .add(i.getName());
              }
            });
    switchportsByVlan.forEach(
        (vlanId, interfaceNamesBuilder) -> {
          List<String> interfaceNames = interfaceNamesBuilder.build();
          CommonUtil.forEachWithIndex(
              interfaceNames,
              (i, i1Name) -> {
                for (int j = i + 1; j < interfaceNames.size(); j++) {
                  String i2Name = interfaceNames.get(j);
                  edges.add(
                      new Layer2Edge(hostname, i1Name, vlanId, hostname, i2Name, vlanId, null));
                  edges.add(
                      new Layer2Edge(hostname, i2Name, vlanId, hostname, i1Name, vlanId, null));
                }
              });
        });
  }

  /**
   * Compute the layer-2 topology via the {@code level1Topology} and switching information contained
   * in the {@code configurations}.
   */
  public static @Nonnull Layer2Topology computeLayer2Topology(
      @Nonnull Layer1Topology layer1Topology, @Nonnull Map<String, Configuration> configurations) {
    ImmutableSet.Builder<Layer2Edge> edges = ImmutableSet.builder();
    // First add layer2 edges for physical links.
    layer1Topology
        .getGraph()
        .edges()
        .forEach(layer1Edge -> computeLayer2EdgesForLayer1Edge(layer1Edge, configurations, edges));

    // Then add edges within each node to connect switchports on the same VLAN(s).
    configurations
        .values()
        .forEach(
            c ->
                c.getVrfs()
                    .values()
                    .forEach(vrf -> computeLayer2SelfEdges(c.getHostname(), vrf, edges)));

    return computeLayer2Topology(edges.build(), configurations);
  }

  /**
   * Compute the layer 3 topology from the layer-2 topology and layer-3 information contained in the
   * configurations.
   */
  public static @Nonnull Topology computeLayer3Topology(
      @Nonnull Layer2Topology layer2Topology, @Nonnull Map<String, Configuration> configurations) {
    return new Topology(
        synthesizeL3Topology(configurations).getEdges().stream()
            .filter(edge -> layer2Topology.inSameBroadcastDomain(edge.getHead(), edge.getTail()))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
  }

  private static @Nullable Configuration getConfiguration(
      Layer1Node layer1Node, Map<String, Configuration> configurations) {
    return configurations.get(layer1Node.getHostname());
  }

  private static @Nullable Configuration getConfiguration(
      Layer2Node layer2Node, Map<String, Configuration> configurations) {
    return configurations.get(layer2Node.getHostname());
  }

  public static @Nullable Interface getInterface(
      @Nonnull Layer1Node layer1Node, @Nonnull Map<String, Configuration> configurations) {
    Configuration c = getConfiguration(layer1Node, configurations);
    if (c == null) {
      return null;
    }
    return c.getAllInterfaces().get(layer1Node.getInterfaceName());
  }

  public static @Nullable Interface getInterface(
      @Nonnull Layer2Node layer2Node, @Nonnull Map<String, Configuration> configurations) {
    Configuration c = getConfiguration(layer2Node, configurations);
    if (c == null) {
      return null;
    }
    return c.getAllInterfaces().get(layer2Node.getInterfaceName());
  }

  private TopologyUtil() {}

  /**
   * Compute the {@link Ip}s owned by each interface. hostname -&gt; interface name -&gt; {@link
   * Ip}s.
   */
  public static Map<String, Map<String, Set<Ip>>> computeInterfaceOwnedIps(
      Map<String, Configuration> configurations, boolean excludeInactive) {
    return computeInterfaceOwnedIps(
        computeIpInterfaceOwners(computeNodeInterfaces(configurations), excludeInactive));
  }

  /**
   * Invert a mapping from {@link Ip} to owner interfaces (Ip -&gt; hostname -&gt; interface name)
   * to (hostname -&gt; interface name -&gt; Ip).
   */
  static Map<String, Map<String, Set<Ip>>> computeInterfaceOwnedIps(
      Map<Ip, Map<String, Set<String>>> ipInterfaceOwners) {
    Map<String, Map<String, Set<Ip>>> ownedIps = new HashMap<>();

    ipInterfaceOwners.forEach(
        (ip, owners) ->
            owners.forEach(
                (host, ifaces) ->
                    ifaces.forEach(
                        iface ->
                            ownedIps
                                .computeIfAbsent(host, k -> new HashMap<>())
                                .computeIfAbsent(iface, k -> new HashSet<>())
                                .add(ip))));

    // freeze
    return CommonUtil.toImmutableMap(
        ownedIps,
        Entry::getKey, /* host */
        hostEntry ->
            CommonUtil.toImmutableMap(
                hostEntry.getValue(),
                Entry::getKey, /* interface */
                ifaceEntry -> ImmutableSet.copyOf(ifaceEntry.getValue())));
  }

  /**
   * Compute a mapping of IP addresses to a set of hostnames that "own" this IP (e.g., as a network
   * interface address)
   *
   * @param configurations {@link Configurations} keyed by hostname
   * @param excludeInactive Whether to exclude inactive interfaces
   * @return A map of {@link Ip}s to a set of hostnames that own this IP
   */
  public static Map<Ip, Set<String>> computeIpNodeOwners(
      Map<String, Configuration> configurations, boolean excludeInactive) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("TopologyUtil.computeIpNodeOwners excludeInactive=" + excludeInactive)
            .startActive()) {
      assert span != null; // avoid unused warning

      return CommonUtil.toImmutableMap(
          computeIpInterfaceOwners(computeNodeInterfaces(configurations), excludeInactive),
          Entry::getKey, /* Ip */
          ipInterfaceOwnersEntry ->
              /* project away interfaces */
              ipInterfaceOwnersEntry.getValue().keySet());
    }
  }

  /**
   * Compute a mapping from IP address to the interfaces that "own" that IP (e.g., as a network
   * interface address).
   *
   * <p>Takes into account VRRP configuration.
   *
   * @param allInterfaces A mapping of interfaces: hostname -&gt; set of {@link Interface}
   * @param excludeInactive whether to ignore inactive interfaces
   * @return A map from {@link Ip}s to hostname to set of interface names that own that IP.
   */
  public static Map<Ip, Map<String, Set<String>>> computeIpInterfaceOwners(
      Map<String, Set<Interface>> allInterfaces, boolean excludeInactive) {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    Map<Pair<InterfaceAddress, Integer>, Set<Interface>> vrrpGroups = new HashMap<>();
    allInterfaces.forEach(
        (hostname, interfaces) ->
            interfaces.forEach(
                i -> {
                  if ((!i.getActive() || i.getBlacklisted()) && excludeInactive) {
                    return;
                  }
                  // collect vrrp info
                  i.getVrrpGroups()
                      .forEach(
                          (groupNum, vrrpGroup) -> {
                            InterfaceAddress address = vrrpGroup.getVirtualAddress();
                            if (address == null) {
                              /*
                               * Invalid VRRP configuration. The VRRP has no source IP address that
                               * would be used for VRRP election. This interface could never win the
                               * election, so is not a candidate.
                               */
                              return;
                            }
                            Pair<InterfaceAddress, Integer> key = new Pair<>(address, groupNum);
                            Set<Interface> candidates =
                                vrrpGroups.computeIfAbsent(
                                    key, k -> Collections.newSetFromMap(new IdentityHashMap<>()));
                            candidates.add(i);
                          });
                  // collect prefixes
                  i.getAllAddresses().stream()
                      .map(InterfaceAddress::getIp)
                      .forEach(
                          ip ->
                              ipOwners
                                  .computeIfAbsent(ip, k -> new HashMap<>())
                                  .computeIfAbsent(hostname, k -> new HashSet<>())
                                  .add(i.getName()));
                }));
    vrrpGroups.forEach(
        (p, candidates) -> {
          InterfaceAddress address = p.getFirst();
          int groupNum = p.getSecond();
          /*
           * Compare priorities first. If tied, break tie based on highest interface IP.
           */
          Interface vrrpMaster =
              Collections.max(
                  candidates,
                  Comparator.comparingInt(
                          (Interface o) -> o.getVrrpGroups().get(groupNum).getPriority())
                      .thenComparing(o -> o.getAddress().getIp()));
          ipOwners
              .computeIfAbsent(address.getIp(), k -> new HashMap<>())
              .computeIfAbsent(vrrpMaster.getOwner().getHostname(), k -> new HashSet<>())
              .add(vrrpMaster.getName());
        });

    // freeze
    return CommonUtil.toImmutableMap(
        ipOwners,
        Entry::getKey,
        ipOwnersEntry ->
            CommonUtil.toImmutableMap(
                ipOwnersEntry.getValue(),
                Entry::getKey, // hostname
                hostIpOwnersEntry -> ImmutableSet.copyOf(hostIpOwnersEntry.getValue())));
  }

  /**
   * Compute a mapping of IP addresses to the VRFs that "own" this IP (e.g., as a network interface
   * address).
   *
   * @param excludeInactive whether to ignore inactive interfaces
   * @param enabledInterfaces A mapping of enabled interfaces hostname -&gt; interface name -&gt;
   *     {@link Interface}
   * @return A map of {@link Ip}s to a map of hostnames to vrfs that own the Ip.
   */
  public static Map<Ip, Map<String, Set<String>>> computeIpVrfOwners(
      boolean excludeInactive, Map<String, Set<Interface>> enabledInterfaces) {

    Map<String, Map<String, String>> interfaceVrfs =
        CommonUtil.toImmutableMap(
            enabledInterfaces,
            Entry::getKey, /* hostname */
            nodeInterfaces ->
                nodeInterfaces.getValue().stream()
                    .collect(
                        ImmutableMap.toImmutableMap(Interface::getName, Interface::getVrfName)));

    return CommonUtil.toImmutableMap(
        computeIpInterfaceOwners(enabledInterfaces, excludeInactive),
        Entry::getKey, /* Ip */
        ipInterfaceOwnersEntry ->
            CommonUtil.toImmutableMap(
                ipInterfaceOwnersEntry.getValue(),
                Entry::getKey, /* Hostname */
                ipNodeInterfaceOwnersEntry ->
                    ipNodeInterfaceOwnersEntry.getValue().stream()
                        .map(interfaceVrfs.get(ipNodeInterfaceOwnersEntry.getKey())::get)
                        .collect(ImmutableSet.toImmutableSet())));
  }

  /**
   * Aggregate a mapping (Ip -&gt; host name -&gt; interface name) to (Ip -&gt; host name -&gt; vrf
   * name)
   */
  public static Map<Ip, Map<String, Set<String>>> computeIpVrfOwners(
      Map<Ip, Map<String, Set<String>>> ipInterfaceOwners, Map<String, Configuration> configs) {
    return CommonUtil.toImmutableMap(
        ipInterfaceOwners,
        Entry::getKey, /* ip */
        ipEntry ->
            CommonUtil.toImmutableMap(
                ipEntry.getValue(),
                Entry::getKey, /* node */
                nodeEntry ->
                    ImmutableSet.copyOf(
                        nodeEntry.getValue().stream()
                            .map(
                                iface ->
                                    configs
                                        .get(nodeEntry.getKey())
                                        .getAllInterfaces()
                                        .get(iface)
                                        .getVrfName())
                            .collect(Collectors.toList()))));
  }

  /**
   * Invert a mapping from Ip to VRF owners (Ip -&gt; host name -&gt; VRF name) and combine all IPs
   * owned by each VRF into an IpSpace.
   */
  public static Map<String, Map<String, IpSpace>> computeVrfOwnedIpSpaces(
      Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    Map<String, Map<String, AclIpSpace.Builder>> builders = new HashMap<>();
    ipVrfOwners.forEach(
        (ip, ipNodeVrfs) ->
            ipNodeVrfs.forEach(
                (node, vrfs) ->
                    vrfs.forEach(
                        vrf ->
                            builders
                                .computeIfAbsent(node, k -> new HashMap<>())
                                .computeIfAbsent(vrf, k -> AclIpSpace.builder())
                                .thenPermitting(ip.toIpSpace()))));

    return CommonUtil.toImmutableMap(
        builders,
        Entry::getKey, /* node */
        nodeEntry ->
            CommonUtil.toImmutableMap(
                nodeEntry.getValue(),
                Entry::getKey, /* vrf */
                vrfEntry -> vrfEntry.getValue().build()));
  }

  /**
   * Compute the interfaces of each node.
   *
   * @param configurations The {@link Configuration}s for the network
   * @return A map from hostname to the interfaces of that node.
   */
  public static Map<String, Set<Interface>> computeNodeInterfaces(
      Map<String, Configuration> configurations) {
    return CommonUtil.toImmutableMap(
        configurations,
        Entry::getKey,
        e -> ImmutableSet.copyOf(e.getValue().getAllInterfaces().values()));
  }

  /** Returns {@code true} if any {@link Ip IP address} is owned by both devices. */
  private static boolean haveIpInCommon(Interface i1, Interface i2) {
    for (InterfaceAddress ia : i1.getAllAddresses()) {
      for (InterfaceAddress ia2 : i2.getAllAddresses()) {
        if (ia.getIp().equals(ia2.getIp())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns a {@link Topology} inferred from the L3 configuration of interfaces on the devices.
   *
   * <p>Ignores {@code Loopback} interfaces and inactive interfaces.
   */
  public static Topology synthesizeL3Topology(Map<String, Configuration> configurations) {
    Map<Prefix, List<Interface>> prefixInterfaces = new HashMap<>();
    configurations.forEach(
        (nodeName, node) -> {
          for (Interface iface : node.getAllInterfaces().values()) {
            if (iface.isLoopback(node.getConfigurationFormat()) || !iface.getActive()) {
              continue;
            }
            for (InterfaceAddress address : iface.getAllAddresses()) {
              if (address.getNetworkBits() < Prefix.MAX_PREFIX_LENGTH) {
                Prefix prefix = address.getPrefix();
                List<Interface> interfaceBucket =
                    prefixInterfaces.computeIfAbsent(prefix, k -> new LinkedList<>());
                interfaceBucket.add(iface);
              }
            }
          }
        });

    ImmutableSortedSet.Builder<Edge> edges = ImmutableSortedSet.naturalOrder();
    for (Entry<Prefix, List<Interface>> bucketEntry : prefixInterfaces.entrySet()) {
      Prefix p = bucketEntry.getKey();

      // Collect all interfaces that have subnets overlapping P iff they have an IP address in P.
      // Use an IdentityHashSet to prevent duplicates.
      Set<Interface> candidateInterfaces = Sets.newIdentityHashSet();
      IntStream.range(0, Prefix.MAX_PREFIX_LENGTH)
          .mapToObj(
              i ->
                  prefixInterfaces.getOrDefault(
                      Prefix.create(p.getStartIp(), i), ImmutableList.of()))
          .flatMap(Collection::stream)
          .filter(
              iface -> iface.getAllAddresses().stream().anyMatch(ia -> p.containsIp(ia.getIp())))
          .forEach(candidateInterfaces::add);

      for (Interface iface1 : bucketEntry.getValue()) {
        for (Interface iface2 : candidateInterfaces) {
          // No device self-adjacencies in the same VRF.
          if (iface1.getOwner() == iface2.getOwner()
              && iface1.getVrfName().equals(iface2.getVrfName())) {
            continue;
          }
          // don't connect interfaces that have any IP address in common
          if (haveIpInCommon(iface1, iface2)) {
            continue;
          }
          edges.add(new Edge(iface1, iface2));
        }
      }
    }
    return new Topology(edges.build());
  }
}
