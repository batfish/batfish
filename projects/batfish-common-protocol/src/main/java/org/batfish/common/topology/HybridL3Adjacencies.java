package org.batfish.common.topology;

import static org.batfish.common.topology.TopologyUtil.isBorderToIspEdge;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
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
        Stream.concat(
                layer1Topologies.getUserProvidedL1().edgeStream(),
                layer1Topologies.getActiveLogicalL1().edgeStream())
            .filter(
                // Ignore border-to-ISP edges when computing the set of nodes for which users
                // provided L1 topology. Batfish adds these edges during ISP modeling, and not
                // excluding them impact L3 edge inference for border.
                l1Edge -> !isBorderToIspEdge(l1Edge, configurations))
            .map(l1Edge -> l1Edge.getNode1().getHostname())
            .collect(ImmutableSet.toImmutableSet());
    PointToPointInterfaces pointToPointInterfaces =
        PointToPointComputer.compute(layer1Topologies.getLogicalL1(), configurations);
    return new HybridL3Adjacencies(nodesWithL1Topology, layer2Topology, pointToPointInterfaces);
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
    NodeInterfacePair ret = null;
    for (NodeInterfacePair l3 : _pointToPointInterfaces.pointToPointInterfaces(iface)) {
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
        && _pointToPointInterfaces.equals(that._pointToPointInterfaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodesWithL1Topology, _layer2Topology, _pointToPointInterfaces);
  }

  @VisibleForTesting
  static HybridL3Adjacencies createForTesting(
      Set<String> nodesWithL1Topology,
      Layer2Topology layer2Topology,
      PointToPointInterfaces pointToPointInterfaces) {
    return new HybridL3Adjacencies(nodesWithL1Topology, layer2Topology, pointToPointInterfaces);
  }

  private HybridL3Adjacencies(
      Set<String> nodesWithL1Topology,
      Layer2Topology layer2Topology,
      PointToPointInterfaces pointToPointInterfaces) {
    _nodesWithL1Topology = nodesWithL1Topology;
    _layer2Topology = layer2Topology;
    _pointToPointInterfaces = pointToPointInterfaces;
  }

  private final Set<String> _nodesWithL1Topology;
  private final Layer2Topology _layer2Topology;
  private final PointToPointInterfaces _pointToPointInterfaces;
}
