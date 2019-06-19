package org.batfish.common.topology;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents connections between physical or logical interfaces. */
@ParametersAreNonnullByDefault
public final class Layer1Topology {
  public static final Layer1Topology EMPTY = new Layer1Topology(ImmutableList.of());
  private static final String PROP_EDGES = "edges";

  @JsonCreator
  private static @Nonnull Layer1Topology create(
      @Nullable @JsonProperty(PROP_EDGES) Iterable<Layer1Edge> edges) {
    return new Layer1Topology(edges != null ? edges : ImmutableSortedSet.of());
  }

  private final ImmutableNetwork<Layer1Node, Layer1Edge> _graph;

  public Layer1Topology(@Nonnull Iterable<Layer1Edge> edges) {
    MutableNetwork<Layer1Node, Layer1Edge> graph =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    edges.forEach(
        edge -> {
          if (edge.getNode1().equals(edge.getNode2()) || graph.edges().contains(edge)) {
            // Ignore self-loops and parallel edges
            return;
          }
          graph.addEdge(edge.getNode1(), edge.getNode2(), edge);
        });
    _graph = ImmutableNetwork.copyOf(graph);
  }

  @JsonIgnore
  public @Nonnull ImmutableNetwork<Layer1Node, Layer1Edge> getGraph() {
    return _graph;
  }

  @JsonProperty(PROP_EDGES)
  private SortedSet<Layer1Edge> getJsonEdges() {
    return ImmutableSortedSet.copyOf(_graph.edges());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Layer1Topology)) {
      return false;
    }
    return _graph.equals(((Layer1Topology) obj)._graph);
  }

  @Override
  public int hashCode() {
    return _graph.hashCode();
  }
}
