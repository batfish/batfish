package org.batfish.common.topology;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.Map;
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

/** Computes the set of physical interfaces that are directly connected. */
@ParametersAreNonnullByDefault
public final class PointToPointComputer {
  /**
   * Computes {@link PointToPointInterfaces} for the given layer-1 topology and snapshot
   * configurations.
   *
   * @see PointToPointInterfaces
   * @see PointToPointInterfaces#pointToPointInterfaces(NodeInterfacePair)
   */
  public static @Nonnull PointToPointInterfaces compute(
      Layer1Topology topology, Map<String, Configuration> configs) {
    Map<NodeInterfacePair, NodeInterfacePair> p2p = pointToPointInterfaces(topology);
    Map<NodeInterfacePair, NodeInterfacePair> ifaceToParent = computeInterfaceToParent(configs);
    return new PointToPointInterfaces(p2p, ifaceToParent);
  }

  /**
   * Returns a (symmetric) mapping of pairs of interfaces from the given topology that are directly
   * connected in the given {@link Layer1Topology}.
   *
   * <p>To be directly connected, there must be at least one edge between them (unidirectional is
   * okay), and all edges on either interface must be only to each other, including edges to {@link
   * Layer1Topologies#INVALID_INTERFACE}.
   *
   * <p>That is, if the same physical interface is connected twice (not necessarily in the
   * direction) to two different other interfaces, it cannot be point-to-point (there must be a
   * switch in between the 3+ interfaces).
   *
   * <p>Note: this should likely be called with {@link Layer1Topologies#getCombinedL1()} (for
   * physical topology) or {@link Layer1Topologies#getLogicalL1()} (for logical topology). It is
   * <strong>not safe</strong> to call with {@link Layer1Topologies#getActiveLogicalL1()}, as that
   * drops edges from the logical topology that might otherwise have indicated multiple wires to the
   * same logical interface.
   */
  @VisibleForTesting
  static Map<NodeInterfacePair, NodeInterfacePair> pointToPointInterfaces(Layer1Topology topology) {
    Map<NodeInterfacePair, NodeInterfacePair> pointToPoint = new HashMap<>();
    for (Layer1Node n : topology.nodes()) {
      if (n.equals(Layer1Topologies.INVALID_INTERFACE)) {
        // Not valid point-to-point interface.
        continue;
      }
      Set<Layer1Node> neighbors = topology.adjacentNodes(n);
      if (neighbors.size() != 1) {
        // Not unique, so no point-to-point link.
        continue;
      }
      Layer1Node neighbor = Iterables.getOnlyElement(neighbors);
      if (neighbor.equals(Layer1Topologies.INVALID_INTERFACE)) {
        // Not valid point-to-point interface.
        continue;
      }
      Set<Layer1Node> neighborNeighbors = topology.adjacentNodes(neighbor);
      if (!neighborNeighbors.isEmpty() && !neighborNeighbors.equals(ImmutableSet.of(n))) {
        // Neighbor has either too many neighbors or the wrong neighbor; empty is okay though.
        // (Keep in mind: topology is directed, and may be asymmetric.)
        continue;
      }
      // Because we want to permit asymmetric or symmetric edges, a given n, neighbor pair may be
      // found in both directions. Add the edge in both directions, with integrity checks that a
      // previous entry (if present from reverse edge) agrees with this edge.
      NodeInterfacePair iface = n.asNodeInterfacePair();
      NodeInterfacePair neighborIface = neighbor.asNodeInterfacePair();
      @Nullable NodeInterfacePair previous = pointToPoint.put(iface, neighborIface);
      assert previous == null || previous.equals(neighborIface); // sanity
      previous = pointToPoint.put(neighborIface, iface);
      assert previous == null || previous.equals(iface); // sanity
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
  static Map<NodeInterfacePair, NodeInterfacePair> computeInterfaceToParent(
      Map<String, Configuration> configs) {
    ImmutableMap.Builder<NodeInterfacePair, NodeInterfacePair> ret = ImmutableMap.builder();
    for (Configuration c : configs.values()) {
      for (Interface i : c.getAllInterfaces().values()) {
        if (!i.getActive()) {
          continue;
        }
        NodeInterfacePair iface = NodeInterfacePair.of(i);
        if (i.getInterfaceType() == InterfaceType.PHYSICAL
            || i.getInterfaceType() == InterfaceType.AGGREGATED
            || i.getInterfaceType() == InterfaceType.REDUNDANT) {
          ret.put(iface, iface);
        } else {
          i.getDependencies().stream()
              .filter(d -> d.getType() == DependencyType.BIND)
              .findFirst()
              .map(Dependency::getInterfaceName)
              .map(n -> c.getAllInterfaces().get(n))
              .map(NodeInterfacePair::of)
              .ifPresent(parent -> ret.put(iface, parent));
        }
      }
    }
    return ret.build();
  }

  private PointToPointComputer() {} // prevent instantiation of utility class
}
