package org.batfish.common.topology.bridge_domain;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.common.topology.bridge_domain.node.BridgeDomain.newVlanAwareBridge;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.bridge_domain.edge.Edges;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain.BridgeId;
import org.batfish.common.topology.bridge_domain.node.BridgedL3Interface;
import org.batfish.common.topology.bridge_domain.node.DisconnectedL3Interface;
import org.batfish.common.topology.bridge_domain.node.EthernetHub;
import org.batfish.common.topology.bridge_domain.node.L2Interface;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L2VniHub;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.common.topology.bridge_domain.node.NonBridgedL3Interface;
import org.batfish.common.topology.bridge_domain.node.PhysicalInterface;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.VniLayer;
import org.batfish.datamodel.vxlan.VxlanNode;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.jgrapht.alg.util.UnionFind;

/** Computes the set of L3 interfaces that are in the same broadcast domain as a given interface. */
final class L3AdjacencyComputer {
  private static final Logger LOGGER = LogManager.getLogger(L3AdjacencyComputer.class);
  private final @Nonnull Layer1Topologies _layer1Topologies;
  private final @Nonnull Map<NodeInterfacePair, PhysicalInterface> _physicalInterfaces;
  private final @Nonnull Map<NodeInterfacePair, L3Interface> _layer3Interfaces;

  @SuppressWarnings("unused")
  private final @Nonnull Map<String, EthernetHub> _ethernetHubs;

  private final @Nonnull Map<VxlanNode, L2Vni> _l2Vnis;

  @SuppressWarnings("unused")
  private final @Nonnull Map<String, L2VniHub> _l2VniHubs;

  @SuppressWarnings("unused")
  private final @Nonnull Map<BridgeId, BridgeDomain> _bridgeDomains;

  private static final @Nonnull EnumSet<InterfaceType> PHYSICAL_INTERFACE_TYPES =
      EnumSet.of(InterfaceType.PHYSICAL, InterfaceType.AGGREGATED);
  @VisibleForTesting static final String BATFISH_GLOBAL_HUB = "Batfish Global Ethernet Hub";

  L3AdjacencyComputer(
      Map<String, Configuration> configs,
      Layer1Topologies layer1Topologies,
      VxlanTopology vxlanTopology) {
    _layer1Topologies = layer1Topologies;
    _physicalInterfaces = computePhysicalInterfaces(configs);
    _ethernetHubs =
        computeEthernetHubs(configs, _physicalInterfaces, _layer1Topologies.getLogicalL1());
    _bridgeDomains = computeBridgeDomains(configs, _physicalInterfaces);
    _l2Vnis = computeL2Vnis(configs, _bridgeDomains);
    _l2VniHubs = computeL2VniHubs(_l2Vnis, vxlanTopology);
    _layer3Interfaces = computeLayer3Interfaces(configs, _bridgeDomains, _physicalInterfaces);
  }

  private static @Nonnull Map<BridgeId, BridgeDomain> computeBridgeDomains(
      Map<String, Configuration> configs,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces) {
    ImmutableMap.Builder<BridgeId, BridgeDomain> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      // TODO: do something different for devices with bridge interfaces or named bridge domains.
      BridgeDomain vlanAwareBridge = newVlanAwareBridge(c.getHostname());
      ret.put(vlanAwareBridge.getId(), vlanAwareBridge);
      for (Interface i : c.getAllInterfaces().values()) {
        connectL2InterfaceToBridgeDomainAndPhysicalInterface(
            i, c.getAllInterfaces(), physicalInterfaces, vlanAwareBridge);
      }
    }
    return ret.build();
  }

  @VisibleForTesting
  static @Nonnull Optional<L2Interface> connectL2InterfaceToBridgeDomainAndPhysicalInterface(
      Interface i,
      Map<String, Interface> allInterfaces,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces,
      BridgeDomain bridgeDomain) {
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (!i.getSwitchport()) {
      LOGGER.debug("Skipping non-L2 interface {}: switchport is not set", nip);
      return Optional.empty();
    }

    // Identify the physical interface corresponding to this L2 interface.
    Optional<PhysicalInterface> maybeIface =
        findCorrespondingPhysicalInterface(i, nip, allInterfaces, physicalInterfaces);
    if (!maybeIface.isPresent()) {
      // Already warned/logged inside the prior function.
      return Optional.empty();
    }
    PhysicalInterface physicalInterface = maybeIface.get();
    L2Interface l2Interface = new L2Interface(nip);

    // Connect L2 interface to domain and physical interface based on L2 config.
    if (i.getSwitchportMode() == SwitchportMode.ACCESS) {
      Integer vlan = i.getAccessVlan();
      if (vlan == null) {
        LOGGER.warn("Skipping L2 connection for {}: access mode vlan is missing", nip);
        return Optional.empty();
      }
      Edges.connectAccessToBridgeDomainAndPhysical(
          vlan, l2Interface, bridgeDomain, physicalInterface, i.getAccessVlan());
      return Optional.of(l2Interface);
    } else if (i.getSwitchportMode() == SwitchportMode.TRUNK) {
      assert i.getAllowedVlans() != null;
      Edges.connectTrunkToBridgeDomainAndPhysical(
          l2Interface, bridgeDomain, physicalInterface, i.getNativeVlan(), i.getAllowedVlans());
      return Optional.of(l2Interface);
    } else {
      // TODO: l2transport
      LOGGER.warn("Unsupported L2 interface {}: unsure how to connect", nip);
      return Optional.empty();
    }
  }

  /**
   * Returns the {@link PhysicalInterface} corresponding to the given {@link Interface}, if one
   * exists. This function returns a present {@link Optional} if {@code i} is a physical interface
   * already, or if {@code i} is a correctly configured subinterface.
   *
   * <p>In other cases (virtual interface like Vlan, missing parent interface, etc.) the return
   * value will be {@link Optional#empty()}.
   */
  @VisibleForTesting
  static @Nonnull Optional<PhysicalInterface> findCorrespondingPhysicalInterface(
      Interface i,
      NodeInterfacePair nip,
      Map<String, Interface> deviceInterfaces,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces) {
    PhysicalInterface iface = physicalInterfaces.get(nip);
    if (iface != null) {
      return Optional.of(iface);
    }
    Optional<Dependency> parent =
        i.getDependencies().stream().filter(d -> d.getType() == DependencyType.BIND).findFirst();
    if (!parent.isPresent()) {
      LOGGER.debug("No corresponding physical interface found for {}", nip);
      return Optional.empty();
    }
    String parentName = parent.get().getInterfaceName();
    NodeInterfacePair parentNip = NodeInterfacePair.of(nip.getHostname(), parentName);
    if (!deviceInterfaces.containsKey(parentName)) {
      LOGGER.warn("Subinterface {}: missing parent {}, skipping", nip, parentNip);
      return Optional.empty();
    }
    iface = physicalInterfaces.get(parentNip);
    if (iface == null) {
      LOGGER.debug("Subinterface {}: parent {} has no physical interface", nip, parentNip);
      return Optional.empty();
    } else {
      return Optional.of(iface);
    }
  }

  private static @Nonnull Map<VxlanNode, L2Vni> computeL2Vnis(
      Map<String, Configuration> configs, Map<BridgeId, BridgeDomain> bridgeDomains) {
    ImmutableMap.Builder<VxlanNode, L2Vni> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      // TODO: support other bridge domains
      BridgeDomain bridgeDomain = bridgeDomains.get(BridgeId.vlanAwareBridgeId(c.getHostname()));
      checkState(
          bridgeDomain != null,
          "Device bridge domain not yet created for device %s",
          c.getHostname());
      for (Vrf vrf : c.getVrfs().values()) {
        for (Layer2Vni vniSettings : vrf.getLayer2Vnis().values()) {
          VxlanNode node = new VxlanNode(c.getHostname(), vniSettings.getVni(), VniLayer.LAYER_2);
          L2Vni vni = L2Vni.of(node);
          ret.put(node, vni);
          Edges.connectVniToVlanAwareBridgeDomain(vni, bridgeDomain, vniSettings.getVlan());
        }
      }
    }
    return ret.build();
  }

  @VisibleForTesting
  static @Nonnull Map<String, L2VniHub> computeL2VniHubs(
      Map<VxlanNode, L2Vni> l2vnis, VxlanTopology vxlanTopology) {
    Set<EndpointPair<VxlanNode>> l2edges =
        vxlanTopology.getLayer2VniEdges().collect(ImmutableSet.toImmutableSet());
    if (l2vnis.isEmpty() || l2edges.isEmpty()) {
      return ImmutableMap.of();
    }

    // Build a hub for every L2VNI cluster.
    Set<VxlanNode> nodesWithEdges =
        l2edges.stream()
            .flatMap(e -> Stream.of(e.nodeU(), e.nodeV()))
            .collect(ImmutableSet.toImmutableSet());
    UnionFind<VxlanNode> clusters = new UnionFind<>(nodesWithEdges);
    for (EndpointPair<VxlanNode> edge : l2edges) {
      clusters.union(edge.nodeU(), edge.nodeV());
    }

    // Build up the set of L2VNIs attached to each hub.
    Multimap<VxlanNode, VxlanNode> groups = LinkedListMultimap.create(clusters.numberOfSets());
    for (VxlanNode node : nodesWithEdges) {
      groups.put(clusters.find(node), node);
    }
    // Create one L2VNIHub for each group.
    ImmutableMap.Builder<String, L2VniHub> ret = ImmutableMap.builder();
    groups
        .asMap()
        .forEach(
            (id, nodes) -> {
              L2VniHub hub = new L2VniHub("Hub for " + id);
              L2Vni[] vnis = new L2Vni[nodes.size()];
              int i = 0;
              for (VxlanNode node : nodes) {
                L2Vni vni = l2vnis.get(node);
                assert vni != null;
                vnis[i] = vni;
                ++i;
              }
              Edges.connectToL2VniHub(hub, vnis);
              ret.put(hub.getName(), hub);
            });
    return ret.build();
  }

  private static Map<NodeInterfacePair, PhysicalInterface> computePhysicalInterfaces(
      Map<String, Configuration> configs) {
    ImmutableMap.Builder<NodeInterfacePair, PhysicalInterface> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      for (Interface i : c.getAllInterfaces().values()) {
        if (shouldCreatePhysicalInterface(i)) {
          NodeInterfacePair nip = NodeInterfacePair.of(i);
          ret.put(nip, new PhysicalInterface(nip));
        }
      }
    }
    return ret.build();
  }

  /**
   * Returns whether this {@link Interface} should be modeled as a {@link PhysicalInterface}. Only
   * active, interfaces that should be in the logical {@link Layer1Topology} are included --
   * physical interfaces and aggregated interfaces.
   */
  @VisibleForTesting
  static boolean shouldCreatePhysicalInterface(Interface i) {
    if (!PHYSICAL_INTERFACE_TYPES.contains(i.getInterfaceType())) {
      LOGGER.debug(
          "Not creating physical interface {}: not a physical interface", NodeInterfacePair.of(i));
      return false;
    } else if (isAggregated(i)) {
      LOGGER.debug(
          "Not creating physical interface {}: physical interface but aggregated",
          NodeInterfacePair.of(i));
      return false;
    } else if (!i.getActive()) {
      LOGGER.debug("Not creating physical interface {}: not active", NodeInterfacePair.of(i));
      return false;
    }
    return true;
  }

  private static @Nonnull Map<NodeInterfacePair, L3Interface> computeLayer3Interfaces(
      Map<String, Configuration> configs,
      Map<BridgeId, BridgeDomain> bridgeDomains,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces) {
    ImmutableMap.Builder<NodeInterfacePair, L3Interface> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      for (Interface i : c.getAllInterfaces().values()) {
        if (!shouldCreateL3Interface(i)) {
          continue;
        }
        L3Interface l3Interface =
            connectL3InterfaceToPhysicalOrDomain(
                    i, c.getAllInterfaces(), physicalInterfaces, bridgeDomains)
                .orElse(new DisconnectedL3Interface(NodeInterfacePair.of(i)));
        ret.put(l3Interface.getInterface(), l3Interface);
      }
    }
    return ret.build();
  }

  @VisibleForTesting
  static boolean shouldCreateL3Interface(Interface i) {
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (i.getAllAddresses().isEmpty()) {
      LOGGER.debug("Not creating L3 interface {}: no addresses", nip);
      return false;
    } else if (!i.getActive()) {
      LOGGER.debug("Not creating L3 interface {}: not active", nip);
      return false;
    } else if (i.getInterfaceType() == InterfaceType.LOOPBACK) {
      LOGGER.debug("Skipping L3 interface {}: loopback", nip);
      return false;
    } else if (i.getSwitchport()) {
      LOGGER.warn("Skipping L3 interface {}: has switchport set to true", nip);
      return false;
    }
    LOGGER.debug("Created L3 interface for {} with addresses {}", nip, i.getAllAddresses());
    return true;
  }

  @VisibleForTesting
  static @Nonnull Optional<L3Interface> connectL3InterfaceToPhysicalOrDomain(
      Interface i,
      Map<String, Interface> deviceInterfaces,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces,
      Map<BridgeId, BridgeDomain> bridgeDomains) {
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (PHYSICAL_INTERFACE_TYPES.contains(i.getInterfaceType())) {
      PhysicalInterface physicalInterface = physicalInterfaces.get(nip);
      if (physicalInterface == null) {
        LOGGER.warn("L3 interface {}: surprised not to find physical interface; skipping", nip);
        return Optional.empty();
      }
      // This is a physical interface with an IP address. Either it has encapsulation or
      // it sends out untagged.
      NonBridgedL3Interface l3Interface = new NonBridgedL3Interface(nip);
      Integer encapsulationVlan = i.getEncapsulationVlan();
      if (encapsulationVlan == null) {
        LOGGER.debug("L3 interface {} connected to physical interface {} untagged", nip, nip);
      } else {
        LOGGER.debug(
            "L3 interface {} connected to physical interface {} in vlan {}",
            nip,
            nip,
            i.getEncapsulationVlan());
      }
      Edges.connectNonBridgedL3ToPhysical(l3Interface, physicalInterface, encapsulationVlan);
      return Optional.of(l3Interface);
    }

    Optional<Dependency> parent =
        i.getDependencies().stream().filter(d -> d.getType() == DependencyType.BIND).findFirst();
    if (parent.isPresent()) {
      String parentName = parent.get().getInterfaceName();
      NodeInterfacePair parentNip = NodeInterfacePair.of(nip.getHostname(), parentName);
      if (!deviceInterfaces.containsKey(parentName)) {
        LOGGER.warn("Not connecting L3 interface {} to parent: {} not found", nip, parentNip);
        return Optional.empty();
      }
      PhysicalInterface parentIface = physicalInterfaces.get(parentNip);
      if (parentIface == null) {
        LOGGER.warn(
            "Not connecting L3 interface {} to parent {}: physical interface not found",
            nip,
            parentNip);
        return Optional.empty();
      }
      NonBridgedL3Interface l3Interface = new NonBridgedL3Interface(nip);
      Integer encapsulationVlan = i.getEncapsulationVlan();
      if (encapsulationVlan == null) {
        LOGGER.debug("L3 interface {} connected to physical interface {} untagged", nip, parentNip);
      } else {
        LOGGER.debug(
            "Connecting L3 interface {} to physical interface {} in vlan {}",
            nip,
            parentNip,
            i.getEncapsulationVlan());
      }
      Edges.connectNonBridgedL3ToPhysical(l3Interface, parentIface, encapsulationVlan);
      return Optional.of(l3Interface);
    }

    if (i.getInterfaceType() == InterfaceType.TUNNEL) {
      // These interfaces do not use L2 broadcast domains / adjacency to establish edges
      return Optional.empty();
    }

    if (i.getInterfaceType() == InterfaceType.VLAN) {
      Integer vlan = i.getVlan();
      if (vlan == null) {
        LOGGER.warn("Not connecting L3 interface {}: surprised vlan is not set", nip);
        return Optional.empty();
      }
      // TODO: store bridge domain in interface
      BridgeDomain bridgeDomain = bridgeDomains.get(BridgeId.vlanAwareBridgeId(nip.getHostname()));
      if (bridgeDomain == null) {
        LOGGER.warn("Not connecting L3 interface {}: surprised not to find vlan-aware bridge", nip);
        return Optional.empty();
      }
      LOGGER.debug(
          "Connecting L3 interface {} to bridge domain {} in vlan {}",
          nip,
          bridgeDomain.getId(),
          vlan);
      BridgedL3Interface bridgedL3Interface = new BridgedL3Interface(nip);
      Edges.connectIrbToBridgeDomain(bridgedL3Interface, bridgeDomain, vlan);
      return Optional.of(bridgedL3Interface);
    }

    LOGGER.warn(
        "Surprised by L3 interface {} of type {}: unsure how to connect",
        nip,
        i.getInterfaceType());
    return Optional.empty();
  }

  @VisibleForTesting
  static @Nonnull Map<String, EthernetHub> computeEthernetHubs(
      Map<String, Configuration> configs,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces,
      Layer1Topology layer1Topology) {
    Set<NodeInterfacePair> physicalInterfacesMentionedInL1 =
        layer1Topology.getGraph().nodes().stream()
            .map(l1node -> NodeInterfacePair.of(l1node.getHostname(), l1node.getInterfaceName()))
            .sorted() // sorted for determinism
            .collect(ImmutableSet.toImmutableSet());

    // Build the global hub if needed.
    Set<NodeInterfacePair> interfacesAttachedToGlobalHub =
        Sets.difference(physicalInterfaces.keySet(), physicalInterfacesMentionedInL1);
    ImmutableMap.Builder<String, EthernetHub> ret = ImmutableMap.builder();
    if (interfacesAttachedToGlobalHub.isEmpty()) {
      LOGGER.debug("Not creating a global Ethernet hub: all physical interfaces have L1 edges");
    } else {
      LOGGER.debug(
          "Creating a global Ethernet hub with {} physical interfaces",
          interfacesAttachedToGlobalHub.size());
      EthernetHub globalHub = new EthernetHub(BATFISH_GLOBAL_HUB);
      Edges.connectToHub(
          globalHub,
          interfacesAttachedToGlobalHub.stream()
              .filter(nip -> getInterface(nip, configs).getActive())
              .map(physicalInterfaces::get)
              .toArray(PhysicalInterface[]::new));
      ret.put(globalHub.getId(), globalHub);
    }

    // Build an EthernetHub for every L1 cluster.
    if (physicalInterfacesMentionedInL1.isEmpty()) {
      LOGGER.debug("L1 topology is empty, so only the global hub exists");
    } else {
      UnionFind<NodeInterfacePair> clusters = new UnionFind<>(physicalInterfacesMentionedInL1);
      for (Layer1Edge edge : layer1Topology.getGraph().edges()) {
        NodeInterfacePair i1 =
            NodeInterfacePair.of(edge.getNode1().getHostname(), edge.getNode1().getInterfaceName());
        NodeInterfacePair i2 =
            NodeInterfacePair.of(edge.getNode2().getHostname(), edge.getNode2().getInterfaceName());
        if (physicalInterfaces.containsKey(i1) && physicalInterfaces.containsKey(i2)) {
          // Only apply L1 edges where both interfaces exist.
          clusters.union(i1, i2);
        }
      }
      // Build up the set of interfaces attached to each hub. Note that since we are only looking
      // at interfaces that exist in this snapshot, and we only applied edges that exist in this
      // snapshot, we guarantee both the representative NodeInterfacePair and the elements in the
      // group are actually in the snapshot.
      Multimap<NodeInterfacePair, NodeInterfacePair> groups =
          LinkedListMultimap.create(clusters.numberOfSets());
      for (NodeInterfacePair nip :
          Sets.intersection(physicalInterfaces.keySet(), physicalInterfacesMentionedInL1)) {
        groups.put(clusters.find(nip), nip);
      }
      // Create one EthernetHub for each group.
      groups
          .asMap()
          .forEach(
              (id, nodes) -> {
                EthernetHub hub = new EthernetHub("Hub for " + id);
                PhysicalInterface[] interfaces = new PhysicalInterface[nodes.size()];
                int i = 0;
                for (NodeInterfacePair node : nodes) {
                  PhysicalInterface pi = physicalInterfaces.get(node);
                  assert pi != null;
                  interfaces[i] = pi;
                  ++i;
                }
                Edges.connectToHub(hub, interfaces);
                ret.put(hub.getId(), hub);
              });
    }

    // Log if at least one L2 interface is mentioned in L1 and at least one L2 interface is attached
    // to global hub.
    Set<NodeInterfacePair> l2AttachedToGlobalHub =
        interfacesAttachedToGlobalHub.stream()
            .filter(
                nip ->
                    Optional.ofNullable(configs.get(nip.getHostname()))
                        .map(c -> c.getAllInterfaces().get(nip.getInterface()))
                        .map(Interface::getSwitchport)
                        .orElse(false))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
    Set<NodeInterfacePair> l2MentionedInL1 =
        physicalInterfacesMentionedInL1.stream()
            .filter(
                nip ->
                    Optional.ofNullable(configs.get(nip.getHostname()))
                        .map(c -> c.getAllInterfaces().get(nip.getInterface()))
                        .map(Interface::getSwitchport)
                        .orElse(false))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
    if (!l2AttachedToGlobalHub.isEmpty() && !l2MentionedInL1.isEmpty()) {
      LOGGER.warn(
          "Surprised that some L2 interfaces are mentioned in L1 ({}) but not all ({}): {} are not",
          l2MentionedInL1.size(),
          l2AttachedToGlobalHub.size(),
          l2AttachedToGlobalHub);
    }

    return ret.build();
  }

  public @Nonnull Map<NodeInterfacePair, Integer> findAllBroadcastDomains() {
    ImmutableMap.Builder<NodeInterfacePair, Integer> ret = ImmutableMap.builder();
    SortedSet<NodeInterfacePair> unchecked = new TreeSet<>(_layer3Interfaces.keySet());
    while (!unchecked.isEmpty()) {
      Set<NodeInterfacePair> domain = findBroadcastDomain(unchecked.first());
      int cur = unchecked.size();
      domain.forEach(i -> ret.put(i, cur));
      unchecked.removeAll(domain);
    }
    return ret.build();
  }

  private Set<NodeInterfacePair> findBroadcastDomain(NodeInterfacePair first) {
    L3Interface originator = _layer3Interfaces.get(first);
    return Search.originate(originator).stream()
        .map(L3Interface::getInterface)
        .collect(ImmutableSet.toImmutableSet());
  }

  private static boolean isAggregated(Interface i) {
    return i.getChannelGroup() != null;
  }

  private static Interface getInterface(NodeInterfacePair i, Map<String, Configuration> configs) {
    return configs.get(i.getHostname()).getAllInterfaces().get(i.getInterface());
  }
}
