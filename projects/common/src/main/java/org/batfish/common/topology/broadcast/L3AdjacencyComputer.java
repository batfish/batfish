package org.batfish.common.topology.broadcast;

import static com.google.common.base.Verify.verify;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1Topology;
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
public class L3AdjacencyComputer {
  private static final Logger LOGGER = LogManager.getLogger(L3AdjacencyComputer.class);
  private final @Nonnull Layer1Topologies _layer1Topologies;
  private final @Nonnull Map<NodeInterfacePair, PhysicalInterface> _physicalInterfaces;
  private final @Nonnull Map<NodeInterfacePair, L3Interface> _layer3Interfaces;

  @SuppressWarnings("unused")
  private final @Nonnull Map<String, EthernetHub> _ethernetHubs;

  private final @Nonnull Map<VxlanNode, L2VNI> _l2vnis;

  @SuppressWarnings("unused")
  private final @Nonnull Map<String, L2VNIHub> _l2vniHubs;

  private final @Nonnull Map<String, DeviceBroadcastDomain> _deviceBroadcastDomains;

  private static final EnumSet<InterfaceType> PHYSICAL_INTERFACE_TYPES =
      EnumSet.of(InterfaceType.PHYSICAL, InterfaceType.AGGREGATED);
  @VisibleForTesting static final String BATFISH_GLOBAL_HUB = "Batfish Global Ethernet Hub";

  public L3AdjacencyComputer(
      Map<String, Configuration> configs,
      Layer1Topologies layer1Topologies,
      VxlanTopology vxlanTopology) {
    _layer1Topologies = layer1Topologies;
    _physicalInterfaces = computePhysicalInterfaces(configs);
    _ethernetHubs =
        computeEthernetHubs(configs, _physicalInterfaces, _layer1Topologies.getLogicalL1());
    _deviceBroadcastDomains = computeDeviceBroadcastDomains(configs, _physicalInterfaces);
    _l2vnis = computeL2VNIs(configs, _deviceBroadcastDomains);
    _l2vniHubs = computeL2VNIHubs(_l2vnis, vxlanTopology);
    _layer3Interfaces =
        computeLayer3Interfaces(configs, _deviceBroadcastDomains, _physicalInterfaces);
  }

  private static Map<String, DeviceBroadcastDomain> computeDeviceBroadcastDomains(
      Map<String, Configuration> configs,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces) {
    ImmutableMap.Builder<String, DeviceBroadcastDomain> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      ret.put(c.getHostname(), domain);
      for (Interface i : c.getAllInterfaces().values()) {
        connectL2InterfaceToBroadcastDomain(i, c.getAllInterfaces(), physicalInterfaces, domain);
      }
    }
    return ret.build();
  }

  @VisibleForTesting
  static void connectL2InterfaceToBroadcastDomain(
      Interface i,
      Map<String, Interface> allInterfaces,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces,
      DeviceBroadcastDomain domain) {
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (!i.getSwitchport()) {
      LOGGER.debug("Skipping non-L2 interface {}: switchport is not set", nip);
      return;
    }

    // Identify the physical interface corresponding to this L2 interface.
    Optional<PhysicalInterface> maybeIface =
        findCorrespondingPhysicalInterface(i, nip, allInterfaces, physicalInterfaces);
    if (!maybeIface.isPresent()) {
      // Already warned/logged inside the prior function.
      return;
    }
    PhysicalInterface iface = maybeIface.get();
    if (!iface.getIface().equals(nip)) {
      // TODO: allow multiple L2 subinterfaces of a given physical interface
      LOGGER.warn("Faking L2 connection for subinterface {} to parent {}", nip, iface.getIface());
    }

    // Connect physical interface to domain based on L2 config.
    if (i.getSwitchportMode() == SwitchportMode.ACCESS) {
      Integer vlan = i.getAccessVlan();
      if (vlan == null) {
        LOGGER.warn("Skipping L2 connection for {}: access mode vlan is missing", nip);
        return;
      }
      Edges.connectInAccessMode(vlan, iface, domain);
    } else if (i.getSwitchportMode() == SwitchportMode.TRUNK) {
      Edges.connectTrunk(iface, domain, i.getAllowedVlans(), i.getNativeVlan());
    } else {
      LOGGER.warn("Surprised by L2 interface {}: unsure how to connect", nip);
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
  static Optional<PhysicalInterface> findCorrespondingPhysicalInterface(
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

  private static Map<VxlanNode, L2VNI> computeL2VNIs(
      Map<String, Configuration> configs, Map<String, DeviceBroadcastDomain> domains) {
    ImmutableMap.Builder<VxlanNode, L2VNI> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      DeviceBroadcastDomain domain = domains.get(c.getHostname());
      verify(domain != null, "Broadcast domain not yet created for device %s", c.getHostname());
      for (Vrf vrf : c.getVrfs().values()) {
        for (Layer2Vni vniSettings : vrf.getLayer2Vnis().values()) {
          VxlanNode node = new VxlanNode(c.getHostname(), vniSettings.getVni(), VniLayer.LAYER_2);
          L2VNI vni = new L2VNI(node);
          ret.put(node, vni);
          L2VniToVlan connection = new L2VniToVlan(vniSettings.getVlan());
          vni.connectToVlan(domain, connection::receiveFromVxlan);
          domain.attachL2VNI(vni, connection::sendToVxlan);
        }
      }
    }
    return ret.build();
  }

  @VisibleForTesting
  static Map<String, L2VNIHub> computeL2VNIHubs(
      Map<VxlanNode, L2VNI> l2vnis, VxlanTopology vxlanTopology) {
    Set<EndpointPair<VxlanNode>> l2edges =
        vxlanTopology.getLayer2VniEdges().collect(Collectors.toSet());
    if (l2vnis.isEmpty() || l2edges.isEmpty()) {
      return ImmutableMap.of();
    }

    // Build a hub for every L2VNI cluster.
    Set<VxlanNode> nodesWithEdges =
        l2edges.stream().flatMap(e -> Stream.of(e.nodeU(), e.nodeV())).collect(Collectors.toSet());
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
    ImmutableMap.Builder<String, L2VNIHub> ret = ImmutableMap.builder();
    groups
        .asMap()
        .forEach(
            (id, nodes) -> {
              L2VNIHub hub = new L2VNIHub("Hub for " + id);
              L2VNI[] vnis = new L2VNI[nodes.size()];
              int i = 0;
              for (VxlanNode node : nodes) {
                L2VNI vni = l2vnis.get(node);
                assert vni != null;
                vnis[i] = vni;
                ++i;
              }
              Edges.connectToL2VNIHub(hub, vnis);
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

  private static Map<NodeInterfacePair, L3Interface> computeLayer3Interfaces(
      Map<String, Configuration> configs,
      Map<String, DeviceBroadcastDomain> deviceBroadcastDomains,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces) {
    ImmutableMap.Builder<NodeInterfacePair, L3Interface> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      for (Interface i : c.getAllInterfaces().values()) {
        if (!shouldCreateL3Interface(i)) {
          continue;
        }
        NodeInterfacePair nip = NodeInterfacePair.of(i);
        L3Interface iface = new L3Interface(nip);
        ret.put(nip, iface);
        connectL3InterfaceToPhysicalOrDomain(
            i, iface, c.getAllInterfaces(), physicalInterfaces, deviceBroadcastDomains);
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
  static void connectL3InterfaceToPhysicalOrDomain(
      Interface i,
      L3Interface iface,
      Map<String, Interface> deviceInterfaces,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces,
      Map<String, DeviceBroadcastDomain> deviceBroadcastDomains) {
    NodeInterfacePair nip = iface.getIface();
    if (PHYSICAL_INTERFACE_TYPES.contains(i.getInterfaceType())) {
      PhysicalInterface physIface = physicalInterfaces.get(nip);
      if (physIface == null) {
        LOGGER.warn("L3 interface {}: surprised not to find physical interface; skipping", nip);
        return;
      }
      // This is a physical interface with an IP address. Either it has encapsulation or
      // it sends out untagged.
      if (i.getEncapsulationVlan() == null) {
        LOGGER.debug("L3 interface {} connected to physical interface {} untagged", nip, nip);
        Edges.connectL3Untagged(iface, physIface);
        return;
      } else {
        LOGGER.debug(
            "L3 interface {} connected to physical interface {} in vlan {}",
            nip,
            nip,
            i.getEncapsulationVlan());
        Edges.connectL3Dot1q(iface, physIface, i.getEncapsulationVlan());
        return;
      }
    }

    Optional<Dependency> parent =
        i.getDependencies().stream().filter(d -> d.getType() == DependencyType.BIND).findFirst();
    if (parent.isPresent()) {
      String parentName = parent.get().getInterfaceName();
      NodeInterfacePair parentNip = NodeInterfacePair.of(nip.getHostname(), parentName);
      if (!deviceInterfaces.containsKey(parentName)) {
        LOGGER.warn("Not connecting L3 interface {} to parent: {} not found", nip, parentNip);
        return;
      }
      PhysicalInterface parentIface = physicalInterfaces.get(parentNip);
      if (parentIface == null) {
        LOGGER.warn(
            "Not connecting L3 interface {} to parent {}: physical interface not found",
            nip,
            parentNip);
        return;
      } else if (i.getEncapsulationVlan() == null) {
        LOGGER.debug("L3 interface {} connected to physical interface {} untagged", nip, parentNip);
        Edges.connectL3Untagged(iface, parentIface);
        return;
      }
      LOGGER.debug(
          "Connecting L3 interface {} to physical interface {} in vlan {}",
          nip,
          parentNip,
          i.getEncapsulationVlan());
      Edges.connectL3Dot1q(iface, parentIface, i.getEncapsulationVlan());
      return;
    }

    if (i.getInterfaceType() == InterfaceType.TUNNEL) {
      // These interfaces do not use L2 broadcast domains / adjacency to establish edges
      return;
    }

    if (i.getInterfaceType() == InterfaceType.VLAN) {
      Integer vlan = i.getVlan();
      if (vlan == null) {
        LOGGER.warn("Not connecting L3 interface {}: surprised vlan is not set", nip);
        return;
      }
      DeviceBroadcastDomain domain = deviceBroadcastDomains.get(nip.getHostname());
      if (domain == null) {
        LOGGER.warn(
            "Not connecting L3 interface {}: surprised not to find device broadcast domain", nip);
        return;
      }
      LOGGER.debug(
          "Connecting L3 interface {} to broadcast domain {} in vlan {}",
          nip,
          domain.getHostname(),
          vlan);
      Edges.connectIRB(iface, domain, vlan);
      return;
    }

    LOGGER.warn(
        "Surprised by L3 interface {} of type {}: unsure how to connect",
        nip,
        i.getInterfaceType());
  }

  @VisibleForTesting
  static Map<String, EthernetHub> computeEthernetHubs(
      Map<String, Configuration> configs,
      Map<NodeInterfacePair, PhysicalInterface> physicalInterfaces,
      Layer1Topology layer1Topology) {
    Set<NodeInterfacePair> physicalInterfacesMentionedInL1 =
        layer1Topology.nodes().stream()
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
      layer1Topology
          .edgeStream()
          .forEach(
              edge -> {
                NodeInterfacePair i1 =
                    NodeInterfacePair.of(
                        edge.getNode1().getHostname(), edge.getNode1().getInterfaceName());
                NodeInterfacePair i2 =
                    NodeInterfacePair.of(
                        edge.getNode2().getHostname(), edge.getNode2().getInterfaceName());
                if (physicalInterfaces.containsKey(i1) && physicalInterfaces.containsKey(i2)) {
                  // Only apply L1 edges where both interfaces exist.
                  clusters.union(i1, i2);
                }
              });
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

  public Map<NodeInterfacePair, Integer> findAllBroadcastDomains() {
    ImmutableMap.Builder<NodeInterfacePair, Integer> ret = ImmutableMap.builder();
    TreeSet<NodeInterfacePair> unchecked = new TreeSet<>(_layer3Interfaces.keySet());
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
    Set<L3Interface> domain = new HashSet<>();
    Set<NodeAndData<?, ?>> visited = new HashSet<>();
    originator.originate(domain, visited);
    return domain.stream().map(L3Interface::getIface).collect(ImmutableSet.toImmutableSet());
  }

  private static boolean isAggregated(Interface i) {
    return i.getChannelGroup() != null;
  }

  private static Interface getInterface(NodeInterfacePair i, Map<String, Configuration> configs) {
    return configs.get(i.getHostname()).getAllInterfaces().get(i.getInterface());
  }
}
