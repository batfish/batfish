package org.batfish.common.topology.bridge_domain;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import java.util.Map;
import java.util.Map.Entry;
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
import org.batfish.common.topology.bridge_domain.node.L1Hub;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.common.topology.bridge_domain.node.L2Interface;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L2VniHub;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.topology.DeviceTopology;
import org.batfish.datamodel.topology.DeviceTopologyUtils;
import org.batfish.datamodel.vxlan.VxlanNode;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.jgrapht.alg.util.UnionFind;

/** Computes the set of L3 interfaces that are in the same broadcast domain as a given interface. */
final class L3AdjacencyComputer {
  private static final Logger LOGGER = LogManager.getLogger(L3AdjacencyComputer.class);
  private final @Nonnull Map<String, DeviceTopology> _deviceTopologies;
  private final @Nonnull Layer1Topologies _layer1Topologies;
  private final @Nonnull Map<NodeInterfacePair, L1Interface> _l1Interfaces;
  private final @Nonnull Map<NodeInterfacePair, L2Interface> _l2Interfaces;
  private final @Nonnull Map<NodeInterfacePair, L3Interface> _l3Interfaces;

  @SuppressWarnings("unused")
  private final @Nonnull Map<String, L1Hub> _l1Hubs;

  private final @Nonnull Map<VxlanNode, L2Vni> _l2Vnis;

  @SuppressWarnings("unused")
  private final @Nonnull Map<String, L2VniHub> _l2VniHubs;

  @VisibleForTesting static final String BATFISH_GLOBAL_HUB = "Batfish Global L1 Hub";

  L3AdjacencyComputer(
      Map<String, Configuration> configs,
      Layer1Topologies layer1Topologies,
      VxlanTopology vxlanTopology) {
    _layer1Topologies = layer1Topologies;
    _deviceTopologies = computeDeviceTopologies(configs);
    _l1Interfaces = computeL1Interfaces(_deviceTopologies);
    _l2Interfaces = computeL2Interfaces(_deviceTopologies);
    _l1Hubs = computeL1Hubs(_l1Interfaces, _l2Interfaces, _layer1Topologies.getLogicalL1());
    _l2Vnis = computeL2Vnis(_deviceTopologies);
    _l2Vnis.values().forEach(L2Vni::clearHub);
    _l2VniHubs = computeL2VniHubs(_l2Vnis, vxlanTopology);
    _l3Interfaces = computeL3Interfaces(_deviceTopologies);
  }

  private @Nonnull Map<String, DeviceTopology> computeDeviceTopologies(
      Map<String, Configuration> configs) {
    return toImmutableMap(
        configs, Entry::getKey, e -> DeviceTopologyUtils.computeDeviceTopology(e.getValue()));
  }

  private @Nonnull Map<NodeInterfacePair, L2Interface> computeL2Interfaces(
      Map<String, DeviceTopology> deviceTopologies) {
    ImmutableMap.Builder<NodeInterfacePair, L2Interface> ret = ImmutableMap.builder();
    for (DeviceTopology t : deviceTopologies.values()) {
      for (L2Interface l2 : t.getL2Interfaces().values()) {
        ret.put(l2.getInterface(), l2);
      }
    }
    return ret.build();
  }

  private static @Nonnull Map<VxlanNode, L2Vni> computeL2Vnis(
      Map<String, DeviceTopology> deviceTopologies) {
    ImmutableMap.Builder<VxlanNode, L2Vni> ret = ImmutableMap.builder();
    for (DeviceTopology t : deviceTopologies.values()) {
      for (L2Vni l2Vni : t.getL2Vnis().values()) {
        ret.put(l2Vni.getNode(), l2Vni);
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

  private static Map<NodeInterfacePair, L1Interface> computeL1Interfaces(
      Map<String, DeviceTopology> deviceTopologies) {
    ImmutableMap.Builder<NodeInterfacePair, L1Interface> ret = ImmutableMap.builder();
    for (DeviceTopology t : deviceTopologies.values()) {
      for (L1Interface l1 : t.getL1Interfaces().values()) {
        ret.put(l1.getInterface(), l1);
      }
    }
    return ret.build();
  }

  private static @Nonnull Map<NodeInterfacePair, L3Interface> computeL3Interfaces(
      Map<String, DeviceTopology> deviceTopologies) {
    ImmutableMap.Builder<NodeInterfacePair, L3Interface> ret = ImmutableMap.builder();
    for (DeviceTopology t : deviceTopologies.values()) {
      for (L3Interface l3 : t.getL3Interfaces().values()) {
        ret.put(l3.getInterface(), l3);
      }
    }
    return ret.build();
  }

  @VisibleForTesting
  static @Nonnull Map<String, L1Hub> computeL1Hubs(
      Map<NodeInterfacePair, L1Interface> l1Interfaces,
      Map<NodeInterfacePair, L2Interface> l2Interfaces,
      Layer1Topology layer1Topology) {
    Set<NodeInterfacePair> physicalInterfacesMentionedInL1 =
        layer1Topology.getGraph().nodes().stream()
            .map(l1node -> NodeInterfacePair.of(l1node.getHostname(), l1node.getInterfaceName()))
            .sorted() // sorted for determinism
            .collect(ImmutableSet.toImmutableSet());

    // Build the global hub if needed.
    Set<NodeInterfacePair> interfacesAttachedToGlobalHub =
        Sets.difference(l1Interfaces.keySet(), physicalInterfacesMentionedInL1);
    ImmutableMap.Builder<String, L1Hub> ret = ImmutableMap.builder();
    if (interfacesAttachedToGlobalHub.isEmpty()) {
      LOGGER.debug("Not creating a global Ethernet hub: all physical interfaces have L1 edges");
    } else {
      LOGGER.debug(
          "Creating a global Ethernet hub with {} physical interfaces",
          interfacesAttachedToGlobalHub.size());
      L1Hub globalHub = new L1Hub(BATFISH_GLOBAL_HUB);
      Edges.connectToHub(
          globalHub,
          interfacesAttachedToGlobalHub.stream()
              .map(l1Interfaces::get)
              .toArray(L1Interface[]::new));
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
        if (l1Interfaces.containsKey(i1) && l1Interfaces.containsKey(i2)) {
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
          Sets.intersection(l1Interfaces.keySet(), physicalInterfacesMentionedInL1)) {
        groups.put(clusters.find(nip), nip);
      }
      // Create one EthernetHub for each group.
      groups
          .asMap()
          .forEach(
              (id, nodes) -> {
                L1Hub hub = new L1Hub("Hub for " + id);
                L1Interface[] interfaces = new L1Interface[nodes.size()];
                int i = 0;
                for (NodeInterfacePair node : nodes) {
                  L1Interface pi = l1Interfaces.get(node);
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
            .filter(l2Interfaces::containsKey)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
    Set<NodeInterfacePair> l2MentionedInL1 =
        physicalInterfacesMentionedInL1.stream()
            .filter(l2Interfaces::containsKey)
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
    SortedSet<NodeInterfacePair> unchecked = new TreeSet<>(_l3Interfaces.keySet());
    while (!unchecked.isEmpty()) {
      Set<NodeInterfacePair> domain = findBroadcastDomain(unchecked.first());
      int cur = unchecked.size();
      domain.forEach(i -> ret.put(i, cur));
      unchecked.removeAll(domain);
    }
    return ret.build();
  }

  private Set<NodeInterfacePair> findBroadcastDomain(NodeInterfacePair first) {
    L3Interface originator = _l3Interfaces.get(first);
    return Search.originate(originator).stream()
        .map(L3Interface::getInterface)
        .collect(ImmutableSet.toImmutableSet());
  }
}
