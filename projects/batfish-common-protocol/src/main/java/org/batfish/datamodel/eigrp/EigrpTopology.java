package org.batfish.datamodel.eigrp;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;

/**
 * Control plane representation of EIGRP connectivity. Vertices are {@link EigrpInterface}s and
 * edges are {@link EigrpEdge}s.
 */
@ParametersAreNonnullByDefault
public final class EigrpTopology {

  public static final EigrpTopology EMPTY =
      new EigrpTopology(
          NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build());

  /** Initialize the EIGRP topology as a directed graph. */
  public static @Nonnull EigrpTopology initEigrpTopology(
      Map<String, Configuration> configurations, Topology topology) {
    Set<EigrpEdge> edges =
        topology.getEdges().stream()
            .map(edge -> EigrpEdge.edgeIfAdjacent(edge, configurations))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableSet.toImmutableSet());
    MutableNetwork<EigrpInterface, EigrpEdge> graph =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    ImmutableSet.Builder<EigrpInterface> nodes = ImmutableSet.builder();
    edges.forEach(
        edge -> {
          nodes.add(edge.getNode1());
          nodes.add(edge.getNode2());
        });
    nodes.build().forEach(graph::addNode);
    edges.forEach(edge -> graph.addEdge(edge.getNode1(), edge.getNode2(), edge));
    return new EigrpTopology(graph);
  }

  private final @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> _network;

  public EigrpTopology(Network<EigrpInterface, EigrpEdge> network) {
    _network = ImmutableNetwork.copyOf(network);
  }

  public @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> getNetwork() {
    return _network;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EigrpTopology)) {
      return false;
    }
    return _network.equals(((EigrpTopology) obj)._network);
  }

  @Override
  public int hashCode() {
    return _network.hashCode();
  }
}
