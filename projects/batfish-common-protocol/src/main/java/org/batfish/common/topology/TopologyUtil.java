package org.batfish.common.topology;

import static org.batfish.common.util.IpsecUtil.initIpsecTopology;
import static org.batfish.common.util.IpsecUtil.retainCompatibleTunnelEdges;
import static org.batfish.datamodel.Interface.TUNNEL_INTERFACE_TYPES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.graph.EndpointPair;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.batfish.common.util.CollectionUtil;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.batfish.datamodel.vxlan.VxlanNode;
import org.batfish.datamodel.vxlan.VxlanTopology;

public final class TopologyUtil {

  /** Returns true iff the given trunk interface allows its own native vlan. */
  private static boolean trunkWithNativeVlanAllowed(Interface i) {
    return i.getSwitchportMode() == SwitchportMode.TRUNK
        && i.getNativeVlan() != null
        && i.getAllowedVlans().contains(i.getNativeVlan());
  }

  // Precondition: at least one of i1 and i2 is a trunk
  private static void addLayer2TrunkEdges(
      Interface i1, Interface i2, Consumer<Layer2Edge> edges, Layer1Node node1, Layer1Node node2) {
    Integer i1Tag = i1.getEncapsulationVlan();
    Integer i2Tag = i2.getEncapsulationVlan();
    if (i1.getSwitchportMode() == SwitchportMode.TRUNK
        && i2.getSwitchportMode() == SwitchportMode.TRUNK) {
      // Both sides are trunks, so add edges from n1,v to n2,v for all shared VLANs.
      i1.getAllowedVlans().stream()
          .forEach(
              vlan -> {
                if (Objects.equals(i1.getNativeVlan(), vlan) && trunkWithNativeVlanAllowed(i2)) {
                  // This frame will not be tagged by i1, and i2 accepts untagged frames.
                  edges.accept(new Layer2Edge(node1, vlan, node2, vlan, null /* untagged */));
                } else if (i2.getAllowedVlans().contains(vlan)) {
                  // This frame will be tagged by i1 and we can directly check whether i2 allows.
                  edges.accept(new Layer2Edge(node1, vlan, node2, vlan, vlan));
                }
              });
    } else if (i1Tag != null) {
      // i1 is a tagged layer-3 interface, and the other side is a trunk. The only possible edge is
      // i2 receiving frames for a non-native allowed vlan.
      if (!i1Tag.equals(i2.getNativeVlan()) && i2.getAllowedVlans().contains(i1Tag)) {
        edges.accept(new Layer2Edge(node1, null, node2, i1Tag, i1Tag));
      }
    } else if (i2Tag != null) {
      // i1 is a trunk, and the other side is a tagged layer-3 interface. The only possible edge is
      // i2 receiving frames for from a non-native allowed vlan of i1.
      if (!i2Tag.equals(i1.getNativeVlan()) && i1.getAllowedVlans().contains(i2Tag)) {
        edges.accept(new Layer2Edge(node1, i2Tag, node2, null, i2Tag));
      }
    } else if (trunkWithNativeVlanAllowed(i1)) {
      // i1 is a trunk, but the other side is not and does not use tags. The only edge that will
      // come up is i2 receiving untagged packets.
      Integer node2VlanId =
          i2.getSwitchportMode() == SwitchportMode.ACCESS ? i2.getAccessVlan() : null;
      edges.accept(new Layer2Edge(node1, i1.getNativeVlan(), node2, node2VlanId, null));
    } else if (trunkWithNativeVlanAllowed(i2)) {
      // i1 is not a trunk and does not use tags, but the other side is a trunk. The only edge that
      // will come up is the other side receiving untagged packets and treating them as native VLAN.
      Integer node1VlanId =
          i1.getSwitchportMode() == SwitchportMode.ACCESS ? i1.getAccessVlan() : null;
      edges.accept(new Layer2Edge(node1, node1VlanId, node2, i2.getNativeVlan(), null));
    }
  }

  public static @Nonnull Layer1Topology computeLayer1PhysicalTopology(
      @Nonnull Layer1Topology rawLayer1Topology,
      @Nonnull Map<String, Configuration> configurations) {
    ImmutableSet.Builder<Layer1Edge> edges = ImmutableSet.builder();
    rawLayer1Topology.getGraph().edges().stream()
        .filter(
            edge -> {
              Interface i1 = getInterface(edge.getNode1(), configurations);
              Interface i2 = getInterface(edge.getNode2(), configurations);
              return i1 != null && i2 != null && i1.getActive() && i2.getActive();
            })
        .forEach(
            edge -> {
              edges.add(edge);
              edges.add(edge.reverse());
            });
    /* Filter out inactive interfaces */
    return new Layer1Topology(edges.build());
  }

  private static void computeLayer2EdgesForLayer1Edge(
      @Nonnull Layer1Edge layer1Edge,
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull Consumer<Layer2Edge> edges,
      Map<Layer1Node, Set<Layer1Node>> parentChildrenMap) {
    Layer1Node node1 = layer1Edge.getNode1();
    Layer1Node node2 = layer1Edge.getNode2();

    // Map each layer1-node to a set of layer-1 nodes corresponding to its children. Attempt to
    // construct a layer-2 edge between each child in the cross product of the child sets.
    parentChildrenMap
        .getOrDefault(node1, ImmutableSet.of(node1))
        .forEach(
            node1Child ->
                parentChildrenMap
                    .getOrDefault(node2, ImmutableSet.of(node2))
                    .forEach(
                        node2Child ->
                            tryComputeLayer2EdgesForLayer1ChildEdge(
                                new Layer1Edge(node1Child, node2Child), configurations, edges)));
  }

  private static void tryComputeLayer2EdgesForLayer1ChildEdge(
      Layer1Edge layer1MappedEdge,
      Map<String, Configuration> configurations,
      Consumer<Layer2Edge> edges) {
    Layer1Node node1 = layer1MappedEdge.getNode1();
    Layer1Node node2 = layer1MappedEdge.getNode2();
    Interface i1 = getInterface(node1, configurations);
    Interface i2 = getInterface(node2, configurations);
    // Exit early if either interface is missing
    if (i1 == null || i2 == null) {
      return;
    }
    if (i1.getSwitchportMode() == SwitchportMode.TRUNK
        || i2.getSwitchportMode() == SwitchportMode.TRUNK) {
      addLayer2TrunkEdges(i1, i2, edges, node1, node2);
    } else if (i1.getEncapsulationVlan() != null || i2.getEncapsulationVlan() != null) {
      // Both interfaces are tagged Layer3 interfaces
      tryAddLayer2TaggedNonTrunkEdge(i1, i2, edges, node1, node2);
    } else {
      Integer node1VlanId =
          i1.getSwitchportMode() == SwitchportMode.ACCESS ? i1.getAccessVlan() : null;
      Integer node2VlanId =
          i2.getSwitchportMode() == SwitchportMode.ACCESS ? i2.getAccessVlan() : null;
      edges.accept(new Layer2Edge(node1, node1VlanId, node2, node2VlanId, null));
    }
  }

  private static void tryAddLayer2TaggedNonTrunkEdge(
      Interface i1, Interface i2, Consumer<Layer2Edge> edges, Layer1Node node1, Layer1Node node2) {
    Integer i1Tag = i1.getEncapsulationVlan();
    Integer i2Tag = i2.getEncapsulationVlan();
    // precondition: i1Tag != null || i2Tag != null
    if (i1Tag == null || i2Tag == null || !i1Tag.equals(i2Tag)) {
      return;
    }
    edges.accept(new Layer2Edge(node1, null, node2, null, i1Tag));
  }

  @VisibleForTesting
  static String computeVniName(int vni) {
    return String.format("~vni~%d", vni);
  }

  @VisibleForTesting
  static void computeLayer2SelfEdges(
      @Nonnull Configuration config, @Nonnull Consumer<Layer2Edge> edges) {
    String hostname = config.getHostname();
    Map<Integer, ImmutableList.Builder<String>> switchportsByVlanBuilder = new HashMap<>();
    config.getAllInterfaces().values().stream()
        .filter(Interface::getActive)
        .forEach(
            i -> {
              if (i.getSwitchportMode() == SwitchportMode.TRUNK) {
                i.getAllowedVlans().stream()
                    .forEach(
                        vlan ->
                            switchportsByVlanBuilder
                                .computeIfAbsent(vlan, n -> ImmutableList.builder())
                                .add(i.getName()));
              } else if (i.getSwitchportMode() == SwitchportMode.ACCESS
                  && i.getAccessVlan() != null) {
                switchportsByVlanBuilder
                    .computeIfAbsent(i.getAccessVlan(), n -> ImmutableList.builder())
                    .add(i.getName());
              }
            });
    Map<Integer, List<String>> switchportsByVlan =
        CollectionUtil.toImmutableMap(
            switchportsByVlanBuilder, Entry::getKey, e -> e.getValue().build());
    switchportsByVlan.forEach(
        (vlanId, interfaceNames) -> {
          CommonUtil.forEachWithIndex(
              interfaceNames,
              (i, i1Name) -> {
                for (int j = i + 1; j < interfaceNames.size(); j++) {
                  String i2Name = interfaceNames.get(j);
                  edges.accept(
                      new Layer2Edge(hostname, i1Name, vlanId, hostname, i2Name, vlanId, null));
                  edges.accept(
                      new Layer2Edge(hostname, i2Name, vlanId, hostname, i1Name, vlanId, null));
                }
              });
        });
    Map<Integer, VniSettings> vniSettingsByVlan =
        config.getVrfs().values().stream()
            .flatMap(vrf -> vrf.getVniSettings().values().stream())
            .filter(vniSettings -> vniSettings.getVlan() != null)
            .collect(ImmutableMap.toImmutableMap(VniSettings::getVlan, Function.identity()));
    config.getAllInterfaces().values().stream()
        .filter(Interface::getActive)
        .filter(i -> i.getInterfaceType() == InterfaceType.VLAN && i.getVlan() != null)
        .forEach(
            irbInterface -> {
              String irbName = irbInterface.getName();
              int vlanId = irbInterface.getVlan();
              computeSelfSwitchportNonSwitchportEdges(switchportsByVlan, hostname, irbName, vlanId)
                  .forEach(edges::accept);
              // Link IRB to VNI in same VLAN
              Optional.ofNullable(vniSettingsByVlan.get(vlanId))
                  .map(vniSettings -> computeVniName(vniSettings.getVni()))
                  .ifPresent(
                      vniName -> {
                        edges.accept(
                            new Layer2Edge(hostname, irbName, null, hostname, vniName, null, null));
                        edges.accept(
                            new Layer2Edge(hostname, vniName, null, hostname, irbName, null, null));
                      });
            });
    // Link each VNI to switchports in same VLAN
    vniSettingsByVlan.forEach(
        (vlanId, vniSettings) -> {
          String vniName = computeVniName(vniSettings.getVni());
          computeSelfSwitchportNonSwitchportEdges(switchportsByVlan, hostname, vniName, vlanId)
              .forEach(edges::accept);
        });
  }

  /**
   * Computes intra-node edges between non-switchport layer-2 entity (IRB or VNI) and switchport
   * interfaces associated with the entity's VLAN
   */
  private static @Nonnull Stream<Layer2Edge> computeSelfSwitchportNonSwitchportEdges(
      Map<Integer, List<String>> switchportsByVlan,
      String hostname,
      String nonSwitchportName,
      int vlanId) {
    Stream.Builder<Layer2Edge> edges = Stream.builder();
    switchportsByVlan
        .getOrDefault(vlanId, ImmutableList.of())
        .forEach(
            switchportName -> {
              edges.accept(
                  new Layer2Edge(
                      hostname, nonSwitchportName, null, hostname, switchportName, vlanId, null));
              edges.accept(
                  new Layer2Edge(
                      hostname, switchportName, vlanId, hostname, nonSwitchportName, null, null));
            });
    return edges.build();
  }

  /**
   * Compute the layer-2 topology via:
   *
   * <ul>
   *   <li>wiring information from {@code layer1LogicalTopology}
   *   <li>established VXLAN bridges from {@code vxlanTopology}
   *   <li>switching information from {@code configurations}
   * </ul>
   */
  public static @Nonnull Layer2Topology computeLayer2Topology(
      @Nonnull Layer1Topology layer1LogicalTopology,
      VxlanTopology vxlanTopology,
      @Nonnull Map<String, Configuration> configurations) {
    Layer2Topology.Builder l2TopologyBuilder = Layer2Topology.builder();

    // Compute mapping from parent interface -> child interfaces
    Map<Layer1Node, Set<Layer1Node>> parentChildrenMap = computeParentChildrenMap(configurations);

    // First add layer2 edges for physical links.
    layer1LogicalTopology
        .getGraph()
        .edges()
        .forEach(
            layer1Edge ->
                computeLayer2EdgesForLayer1Edge(
                    layer1Edge, configurations, l2TopologyBuilder::addEdge, parentChildrenMap));

    // Then add edges within each node to connect switchports and VNIs on the same VLAN(s).
    configurations.values().forEach(c -> computeLayer2SelfEdges(c, l2TopologyBuilder::addEdge));

    // Finally add edges between connected VNIs on different nodes
    computeVniInterNodeEdges(vxlanTopology).forEach(l2TopologyBuilder::addEdge);

    return l2TopologyBuilder.build();
  }

  /**
   * Create {@link Layer2Edge}s corresponding the to inter-node {@link VxlanNode} pairs in the
   * {@link VxlanTopology}.
   */
  @VisibleForTesting
  static @Nonnull Stream<Layer2Edge> computeVniInterNodeEdges(VxlanTopology vxlanTopology) {
    return vxlanTopology.getGraph().edges().stream().flatMap(TopologyUtil::toVniVniEdges);
  }

  /**
   * Create pair of directional {@link Layer2Edge}s for undirected pair of inter-node {@link
   * VxlanNode}s.
   */
  private static @Nonnull Stream<Layer2Edge> toVniVniEdges(EndpointPair<VxlanNode> edge) {
    VxlanNode n1 = edge.nodeU();
    VxlanNode n2 = edge.nodeV();
    String h1 = n1.getHostname();
    String h2 = n2.getHostname();
    int vni1 = n1.getVni();
    int vni2 = n2.getVni();
    String vni1Name = computeVniName(vni1);
    String vni2Name = computeVniName(vni2);
    return Stream.of(
        new Layer2Edge(h1, vni1Name, null, h2, vni2Name, null, null),
        new Layer2Edge(h2, vni2Name, null, h1, vni1Name, null, null));
  }

  private static Map<Layer1Node, Set<Layer1Node>> computeParentChildrenMap(
      Map<String, Configuration> configurations) {
    // parent -> set of children
    Map<Layer1Node, ImmutableSet.Builder<Layer1Node>> builderMap = new HashMap<>();

    // Map each parent interface that is the target of a bind dependency to the set of its child
    // interfaces (e.g. Juniper units).
    configurations.forEach(
        (hostname, c) ->
            c.getAllInterfaces()
                .forEach(
                    (iName, i) ->
                        // i is a potential child interface
                        i.getDependencies().stream()
                            .filter(dependency -> dependency.getType() == DependencyType.BIND)
                            .forEach(
                                bindDependency ->
                                    builderMap
                                        .computeIfAbsent(
                                            new Layer1Node(
                                                hostname, bindDependency.getInterfaceName()),
                                            n -> ImmutableSet.builder())
                                        .add(new Layer1Node(hostname, iName)))));
    // finalize and freeze
    return CollectionUtil.toImmutableMap(builderMap, Entry::getKey, e -> e.getValue().build());
  }

  /**
   * Compute the raw layer 3 topology from the layer-1 and layer-2 topologies, and layer-3
   * information contained in the configurations.
   */
  @VisibleForTesting
  static @Nonnull Topology computeRawLayer3Topology(
      @Nonnull Layer1Topology rawLayer1Topology,
      @Nonnull Layer2Topology layer2Topology,
      @Nonnull Map<String, Configuration> configurations) {
    Set<String> rawLayer1TailNodes =
        rawLayer1Topology.getGraph().edges().stream()
            .map(l1Edge -> l1Edge.getNode1().getHostname())
            .collect(ImmutableSet.toImmutableSet());
    return new Topology(
        synthesizeL3Topology(configurations).getEdges().stream()
            // keep if either node is in tail of edge in raw layer-1, or if vertices are in same
            // broadcast domain
            .filter(
                edge ->
                    !rawLayer1TailNodes.contains(edge.getNode1())
                        || !rawLayer1TailNodes.contains(edge.getNode2())
                        || layer2Topology.inSameBroadcastDomain(edge.getHead(), edge.getTail()))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
  }

  /**
   * Compute the raw layer 3 topology from information contained in the configurations, and also the
   * layer-1 and layer-2 topologies if present. It also removes the overlay edges from the computed
   * layer 3 edges.
   */
  public static @Nonnull Topology computeRawLayer3Topology(
      @Nonnull Optional<Layer1Topology> rawLayer1PhysicalTopology,
      @Nonnull Optional<Layer2Topology> layer2Topology,
      @Nonnull Map<String, Configuration> configurations) {
    return rawLayer1PhysicalTopology
        .map(l1 -> computeRawLayer3Topology(l1, layer2Topology.get(), configurations))
        .orElse(synthesizeL3Topology(configurations));
  }

  /**
   * Compute the layer-3 topology from the raw layer-3 topology, configuration information, and
   * overlay edges.
   *
   * @param rawLayer3Topology raw layer 3 {@link Topology}
   * @param overlayEdges overlay edges to be added to the rawLayer3Topology
   * @param configurations configurations for which these edges exist
   * @return
   */
  public static @Nonnull Topology computeLayer3Topology(
      Topology rawLayer3Topology,
      Set<Edge> overlayEdges,
      Map<String, Configuration> configurations) {
    return new Topology(
        ImmutableSortedSet.<Edge>naturalOrder()
            .addAll(rawLayer3Topology.getEdges())
            .addAll(overlayEdges)
            .build());
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
   * Computes the {@link IpsecTopology} from {@link Configuration}s. The returned topology will only
   * contain the compatible edges which have successfully negotiated {@link IpsecSession}s
   *
   * @param configurations {@link Map} of {@link Configuration}s
   * @return {@link IpsecTopology}
   */
  public static IpsecTopology computeIpsecTopology(Map<String, Configuration> configurations) {
    return retainCompatibleTunnelEdges(initIpsecTopology(configurations), configurations);
  }

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
    return CollectionUtil.toImmutableMap(
        ownedIps,
        Entry::getKey, /* host */
        hostEntry ->
            CollectionUtil.toImmutableMap(
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

      return CollectionUtil.toImmutableMap(
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
    Table<InterfaceAddress, Integer, Set<Interface>> vrrpGroups = HashBasedTable.create();
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
                            Set<Interface> candidates = vrrpGroups.get(address, groupNum);
                            if (candidates == null) {
                              candidates = Collections.newSetFromMap(new IdentityHashMap<>());
                              vrrpGroups.put(address, groupNum, candidates);
                            }
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
    vrrpGroups
        .cellSet()
        .forEach(
            cell -> {
              InterfaceAddress address = cell.getRowKey();
              assert address != null;
              Integer groupNum = cell.getColumnKey();
              assert groupNum != null;
              Set<Interface> candidates = cell.getValue();
              assert candidates != null;
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
    return CollectionUtil.toImmutableMap(
        ipOwners,
        Entry::getKey,
        ipOwnersEntry ->
            CollectionUtil.toImmutableMap(
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
        CollectionUtil.toImmutableMap(
            enabledInterfaces,
            Entry::getKey, /* hostname */
            nodeInterfaces ->
                nodeInterfaces.getValue().stream()
                    .collect(
                        ImmutableMap.toImmutableMap(Interface::getName, Interface::getVrfName)));

    return CollectionUtil.toImmutableMap(
        computeIpInterfaceOwners(enabledInterfaces, excludeInactive),
        Entry::getKey, /* Ip */
        ipInterfaceOwnersEntry ->
            CollectionUtil.toImmutableMap(
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
    return CollectionUtil.toImmutableMap(
        ipInterfaceOwners,
        Entry::getKey, /* ip */
        ipEntry ->
            CollectionUtil.toImmutableMap(
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

    return CollectionUtil.toImmutableMap(
        builders,
        Entry::getKey, /* node */
        nodeEntry ->
            CollectionUtil.toImmutableMap(
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
    return CollectionUtil.toImmutableMap(
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
          // don't connect if any of the two endpoint interfaces have Tunnel or VPN interfaceTypes
          if (TUNNEL_INTERFACE_TYPES.contains(iface1.getInterfaceType())
              || TUNNEL_INTERFACE_TYPES.contains(iface2.getInterfaceType())) {
            continue;
          }
          edges.add(new Edge(iface1, iface2));
        }
      }
    }
    return new Topology(edges.build());
  }

  public static @Nonnull Layer1Topology computeLayer1LogicalTopology(
      Layer1Topology layer1PhysicalTopology, Map<String, Configuration> configurations) {
    return new Layer1Topology(
        layer1PhysicalTopology.getGraph().edges().stream()
            .map(pEdge -> pEdge.toLogicalEdge(NetworkConfigurations.of(configurations)))
            .filter(Objects::nonNull)
            .collect(ImmutableSet.toImmutableSet()));
  }
}
