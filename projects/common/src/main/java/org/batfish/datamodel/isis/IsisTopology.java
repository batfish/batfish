package org.batfish.datamodel.isis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

/** A topology representing IS-IS sessions */
@ParametersAreNonnullByDefault
public final class IsisTopology {

  public static final IsisTopology EMPTY =
      new IsisTopology(
          NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build());
  private static final String PROP_EDGES = "edges";
  private static final String PROP_NODES = "nodes";

  @JsonCreator
  private static @Nonnull IsisTopology create(
      @JsonProperty(PROP_EDGES) @Nullable Set<IsisEdge> edges,
      @JsonProperty(PROP_NODES) @Nullable Set<IsisNode> nodes) {
    MutableNetwork<IsisNode, IsisEdge> network =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    if (nodes != null) {
      nodes.forEach(network::addNode);
    }
    if (edges != null) {
      edges.forEach(edge -> network.addEdge(edge.getNode1(), edge.getNode2(), edge));
    }
    return new IsisTopology(network);
  }

  /** Initialize the IS-IS topology as a directed graph. */
  public static @Nonnull IsisTopology initIsisTopology(
      Map<String, Configuration> configurations, Topology topology) {
    Set<IsisEdge> edges =
        topology.getEdges().stream()
            .map(edge -> IsisEdge.edgeIfCircuit(edge, configurations))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableSet.toImmutableSet());
    MutableNetwork<IsisNode, IsisEdge> graph =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    ImmutableSet.Builder<IsisNode> nodes = ImmutableSet.builder();
    edges.forEach(
        edge -> {
          nodes.add(edge.getNode1());
          nodes.add(edge.getNode2());
        });
    nodes.build().forEach(graph::addNode);
    edges.forEach(edge -> graph.addEdge(edge.getNode1(), edge.getNode2(), edge));
    return new IsisTopology(graph);
  }

  private final @Nonnull Network<IsisNode, IsisEdge> _network;

  public IsisTopology(Network<IsisNode, IsisEdge> network) {
    _network = ImmutableNetwork.copyOf(network);
  }

  @JsonProperty(PROP_EDGES)
  private @Nonnull Set<IsisEdge> getEdges() {
    return _network.edges();
  }

  @JsonIgnore
  public @Nonnull Network<IsisNode, IsisEdge> getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NODES)
  private @Nonnull Set<IsisNode> getNodes() {
    return _network.nodes();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IsisTopology)) {
      return false;
    }
    return _network.equals(((IsisTopology) obj)._network);
  }

  @Override
  public int hashCode() {
    return _network.hashCode();
  }
}
