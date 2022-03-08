package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.PointToPointComputer;
import org.batfish.common.topology.PointToPointInterfaces;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vxlan.VxlanTopology;

/**
 * An {@link L3Adjacencies} implementation based on fully modeling L1 and L2 domains, even when no
 * layer-1 information is provided with the snapshot.
 *
 * @see L3AdjacencyComputer
 */
public class BroadcastL3Adjacencies implements L3Adjacencies {
  public static BroadcastL3Adjacencies create(
      Layer1Topologies l1, VxlanTopology vxlan, Map<String, Configuration> configs) {
    PointToPointInterfaces p2p = PointToPointComputer.compute(l1.getLogicalL1(), configs);
    L3AdjacencyComputer adj = new L3AdjacencyComputer(configs, l1, vxlan);
    return new BroadcastL3Adjacencies(adj.findAllBroadcastDomains(), p2p);
  }

  private BroadcastL3Adjacencies(
      Map<NodeInterfacePair, Integer> domains, PointToPointInterfaces pointToPointInterfaces) {
    _domains = ImmutableMap.copyOf(domains);
    _pointToPointInterfaces = pointToPointInterfaces;
  }

  @Override
  public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    checkArgument(_domains.containsKey(i1), "Missing domain for %s: is it an L3 interface?", i1);
    checkArgument(_domains.containsKey(i2), "Missing domain for %s: is it an L3 interface?", i2);
    return _domains.get(i1).equals(_domains.get(i2));
  }

  @Override
  public @Nonnull Optional<NodeInterfacePair> pairedPointToPointL3Interface(
      NodeInterfacePair iface) {
    checkArgument(
        _domains.containsKey(iface), "Missing domain for %s: is it an L3 interface?", iface);
    NodeInterfacePair ret = null;
    for (NodeInterfacePair otherIf : _pointToPointInterfaces.pointToPointInterfaces(iface)) {
      if (!_domains.containsKey(otherIf) || !inSameBroadcastDomain(iface, otherIf)) {
        // otherIf is not L3 or not in same domain.
        continue;
      } else if (ret != null) {
        // Two p2p neighbors, not unique. Should not happen.
        return Optional.empty();
      }
      ret = otherIf;
    }
    return Optional.ofNullable(ret);
  }

  private final @Nonnull Map<NodeInterfacePair, Integer> _domains;
  private final @Nonnull PointToPointInterfaces _pointToPointInterfaces;
}
