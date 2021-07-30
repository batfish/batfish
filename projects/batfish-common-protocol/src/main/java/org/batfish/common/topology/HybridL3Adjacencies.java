package org.batfish.common.topology;

import static org.batfish.common.topology.TopologyUtil.isBorderToIspEdge;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Infers Layer 3 adjacencies using a hybrid L1/L2 and broadcast model. Specifically:
 *
 * <p>One set of nodes "have L1 information" (aka, have an outgoing edge in the raw {@link
 * Layer1Topology}), and for those nodes and interfaces the layer-3 adjacencies are inferred based
 * on the layer-2 configuration applied to them. All other interfaces are considered to be in the
 * same broadcast domain, but none of them are considered to be point-to-point connected.
 *
 * <p>An interface "has L1 information" if either 1) it is explicitly mentioned in the raw {@link
 * Layer1Topology}, 2) it aggregates a physical interface mentioned in the raw {@link
 * Layer1Topology}, or 3) it is on a node that "has L2 information", aka, originates any edge in
 * {@link Layer1Topology}. This last condition permits asymmetric modeling. For example, if we have
 * LLDP/CDP data for A but not B, we produce the edge A[eth1] to B[eth2] (and A[*] to all other
 * devices), so A's interfaces are required to be wired up, but B's are not.
 *
 * <p>A potential layer-3 edge is considered adjacent if: 1) both interfaces have L1 information and
 * they are in the same broadcast domain, 2) either source node does not "have L1 information". Note
 * that this admits edges between A and C even if A "has L1 information".
 *
 * <p>For unnumbered edges (using link-layer addresses, LLAs), edges are only considered connected
 * if there is an edge between the corresponding physical interfaces..
 */
@ParametersAreNonnullByDefault
public final class HybridL3Adjacencies implements L3Adjacencies {

  public static HybridL3Adjacencies create(
      Layer1Topologies layer1Topologies,
      Layer2Topology layer2Topology,
      Map<String, Configuration> configurations) {
    Set<String> nodesWithL1Topology =
        layer1Topologies.getCombinedL1().getGraph().edges().stream()
            .filter(
                // Ignore border-to-ISP edges when computing the set of nodes for which users
                // provided L1 topology. Batfish adds these edges during ISP modeling, and not
                // excluding them impact L3 edge inference for border.
                l1Edge -> !isBorderToIspEdge(l1Edge, configurations))
            .map(l1Edge -> l1Edge.getNode1().getHostname())
            .collect(ImmutableSet.toImmutableSet());
    Map<NodeInterfacePair, NodeInterfacePair> physicalPointToPoint =
        computePhysicalPointToPoint(layer1Topologies.getActiveLogicalL1());
    Map<NodeInterfacePair, NodeInterfacePair> l3ToPhysical = computeL3ToPhysical(configurations);
    Multimap<NodeInterfacePair, NodeInterfacePair> physicalToL3 = computePhysicalToL3(l3ToPhysical);
    return new HybridL3Adjacencies(
        nodesWithL1Topology, layer2Topology, physicalPointToPoint, l3ToPhysical, physicalToL3);
  }

  @Override
  public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    // true if either node is not in tail of edge in layer-1, or if vertices are in
    // same broadcast domain
    return !_nodesWithL1Topology.contains(i1.getHostname())
        || !_nodesWithL1Topology.contains(i2.getHostname())
        || _layer2Topology.inSameBroadcastDomain(i1, i2);
  }

  @Override
  public @Nonnull Optional<NodeInterfacePair> pairedPointToPointL3Interface(
      NodeInterfacePair iface) {
    NodeInterfacePair physical = _l3ToPhysical.get(iface);
    if (physical == null) {
      return Optional.empty();
    }
    NodeInterfacePair neighbor = _physicalPointToPoint.get(physical);
    if (neighbor == null) {
      return Optional.empty();
    }
    @Nullable NodeInterfacePair ret = null;
    for (NodeInterfacePair l3 : _physicalToL3.get(neighbor)) {
      if (l3 == null || !_layer2Topology.inSameBroadcastDomain(iface, l3)) {
        continue;
      } else if (ret != null) {
        // Two p2p neighbors, not unique. Should not happen.
        return Optional.empty();
      }
      ret = l3;
    }
    return Optional.ofNullable(ret);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof HybridL3Adjacencies)) {
      return false;
    }
    HybridL3Adjacencies that = (HybridL3Adjacencies) o;
    return _layer2Topology.equals(that._layer2Topology)
        && _nodesWithL1Topology.equals(that._nodesWithL1Topology)
        && _physicalPointToPoint.equals(that._physicalPointToPoint)
        && _l3ToPhysical.equals(that._l3ToPhysical)
        && _physicalToL3.equals(that._physicalToL3);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _nodesWithL1Topology, _layer2Topology, _physicalPointToPoint, _l3ToPhysical, _physicalToL3);
  }

  @VisibleForTesting
  static HybridL3Adjacencies createForTesting(
      Set<String> nodesWithL1Topology,
      Layer2Topology layer2Topology,
      Map<NodeInterfacePair, NodeInterfacePair> physicalPointToPoint,
      Map<NodeInterfacePair, NodeInterfacePair> l3ToPhysical,
      Multimap<NodeInterfacePair, NodeInterfacePair> physicalToL3) {
    return new HybridL3Adjacencies(
        nodesWithL1Topology, layer2Topology, physicalPointToPoint, l3ToPhysical, physicalToL3);
  }

  private HybridL3Adjacencies(
      Set<String> nodesWithL1Topology,
      Layer2Topology layer2Topology,
      Map<NodeInterfacePair, NodeInterfacePair> physicalPointToPoint,
      Map<NodeInterfacePair, NodeInterfacePair> l3ToPhysical,
      Multimap<NodeInterfacePair, NodeInterfacePair> physicalToL3) {
    _nodesWithL1Topology = nodesWithL1Topology;
    _layer2Topology = layer2Topology;
    _physicalPointToPoint = physicalPointToPoint;
    _l3ToPhysical = l3ToPhysical;
    _physicalToL3 = physicalToL3;
  }

  /**
   * Computes the set of interfaces connected via point-to-point links given a layer-1 topology.
   * Note that to be considered point-to-point, each interface can be connected to at most 1 other
   * interface, merged and checked in both directions.
   */
  @VisibleForTesting
  static Map<NodeInterfacePair, NodeInterfacePair> computePhysicalPointToPoint(
      Layer1Topology layer1LogicalTopology) {
    Map<NodeInterfacePair, NodeInterfacePair> pointToPoint = new HashMap<>();
    for (Layer1Node n : layer1LogicalTopology.getGraph().nodes()) {
      Set<Layer1Node> neighbors = layer1LogicalTopology.getGraph().adjacentNodes(n);
      if (neighbors.size() != 1) {
        // Not unique, so no point-to-point link.
        continue;
      }
      Layer1Node neighbor = Iterables.getOnlyElement(neighbors);
      Set<Layer1Node> neighborNeighbors = layer1LogicalTopology.getGraph().adjacentNodes(neighbor);
      if (!neighborNeighbors.isEmpty() && !neighborNeighbors.equals(ImmutableSet.of(n))) {
        // Neighbor has either too many neighbors or the wrong neighbor; empty is okay though.
        // (Keep in mind: topology is directed, and may be asymmetric.)
        continue;
      }
      NodeInterfacePair nip = NodeInterfacePair.of(n.getHostname(), n.getInterfaceName());
      NodeInterfacePair neighborNip =
          NodeInterfacePair.of(neighbor.getHostname(), neighbor.getInterfaceName());
      @Nullable NodeInterfacePair previous = pointToPoint.put(nip, neighborNip);
      assert previous == null || previous.equals(neighborNip); // sanity
      previous = pointToPoint.put(neighborNip, nip);
      assert previous == null || previous.equals(nip); // sanity
    }
    return ImmutableMap.copyOf(pointToPoint);
  }

  /**
   * Computes a mapping between each interface and the physical interface (expected to be in the
   * logical {@link Layer1Topology}) it corresponds to. The identity mapping for physical
   * interfaces, a mapping from a subinterface to its parent for logical subinterfaces, and no
   * mapping for virtual interfaces like VLANs/IRBs/etc. Virtual interfaces cannot be involved in
   * point-to-point links.
   */
  @VisibleForTesting
  static Map<NodeInterfacePair, NodeInterfacePair> computeL3ToPhysical(
      Map<String, Configuration> configs) {
    ImmutableMap.Builder<NodeInterfacePair, NodeInterfacePair> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      for (Interface i : c.getAllInterfaces().values()) {
        if (!i.getActive() || i.getSwitchport() || i.getAllAddresses().isEmpty()) {
          continue;
        }
        NodeInterfacePair l3 = NodeInterfacePair.of(i);
        if (i.getInterfaceType() == InterfaceType.PHYSICAL
            || i.getInterfaceType().equals(InterfaceType.AGGREGATED)
            || i.getInterfaceType().equals(InterfaceType.REDUNDANT)) {
          ret.put(l3, l3);
        } else {
          i.getDependencies().stream()
              .filter(d -> d.getType() == DependencyType.BIND)
              .findFirst()
              .map(Dependency::getInterfaceName)
              .map(n -> c.getAllInterfaces().get(n))
              .map(NodeInterfacePair::of)
              .ifPresent(parent -> ret.put(l3, parent));
        }
      }
    }
    return ret.build();
  }

  /**
   * Computes the reverse mapping to {@link #computeL3ToPhysical(Map)}. Note that a physical
   * interface can have many different subinterfaces, so this returns a multiple mapping.
   */
  private static @Nonnull Multimap<NodeInterfacePair, NodeInterfacePair> computePhysicalToL3(
      Map<NodeInterfacePair, NodeInterfacePair> l3ToPhysical) {
    ImmutableMultimap.Builder<NodeInterfacePair, NodeInterfacePair> physicalToL3 =
        ImmutableMultimap.builder();
    l3ToPhysical.forEach((l3, phys) -> physicalToL3.put(phys, l3));
    return physicalToL3.build();
  }

  private final Set<String> _nodesWithL1Topology;
  private final Layer2Topology _layer2Topology;
  private final Map<NodeInterfacePair, NodeInterfacePair> _physicalPointToPoint;
  private final Map<NodeInterfacePair, NodeInterfacePair> _l3ToPhysical;
  private final Multimap<NodeInterfacePair, NodeInterfacePair> _physicalToL3;
}
