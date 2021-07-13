package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topology;
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
      Layer1Topology l1, VxlanTopology vxlan, Map<String, Configuration> configs) {
    assert vxlan != null; // suppress unused warning.
    L3AdjacencyComputer adj = new L3AdjacencyComputer(configs, l1);
    return new BroadcastL3Adjacencies(adj.findAllBroadcastDomains());
  }

  private BroadcastL3Adjacencies(Map<NodeInterfacePair, Integer> domains) {
    _domains = ImmutableMap.copyOf(domains);
  }

  @Override
  public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    checkArgument(_domains.containsKey(i1), "Missing domain for %s: is it an L3 interface?", i1);
    checkArgument(_domains.containsKey(i2), "Missing domain for %s: is it an L3 interface?", i2);
    return _domains.get(i1).equals(_domains.get(i2));
  }

  @Override
  public boolean inSamePointToPointDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    // TODO
    return false;
  }

  @Nonnull
  @Override
  public Optional<NodeInterfacePair> pairedPointToPointL3Interface(NodeInterfacePair iface) {
    // TODO
    return Optional.empty();
  }

  private final @Nonnull Map<NodeInterfacePair, Integer> _domains;
}
