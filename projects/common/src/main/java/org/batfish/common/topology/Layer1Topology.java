package org.batfish.common.topology;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
      @JsonProperty(PROP_EDGES) @Nullable Iterable<Layer1Edge> edges) {
    return new Layer1Topology(edges != null ? edges : ImmutableSortedSet.of());
  }

  private final ImmutableGraph<Layer1Node> _graph;

  @VisibleForTesting
  public Layer1Topology(@Nonnull Layer1Edge... edges) {
    this(Arrays.stream(edges));
  }

  public Layer1Topology(@Nonnull Iterable<Layer1Edge> edges) {
    this(StreamSupport.stream(edges.spliterator(), false));
  }

  public Layer1Topology(@Nonnull Stream<Layer1Edge> edges) {
    ImmutableGraph.Builder<Layer1Node> graph =
        GraphBuilder.directed().allowsSelfLoops(false).immutable();
    edges.forEach(
        edge -> {
          if (edge.getNode1().equals(edge.getNode2())) {
            // Ignore self-loops
            return;
          }
          graph.putEdge(edge.getNode1(), edge.getNode2());
        });
    _graph = graph.build();
  }

  @JsonIgnore
  public @Nonnull Stream<Layer1Edge> edgeStream() {
    return _graph.edges().stream().map(ep -> new Layer1Edge(ep.nodeU(), ep.nodeV()));
  }

  @JsonIgnore
  public @Nonnull Set<Layer1Node> adjacentNodes(Layer1Node node) {
    if (!_graph.nodes().contains(node)) {
      return ImmutableSet.of();
    }
    return _graph.adjacentNodes(node);
  }

  @JsonIgnore
  public @Nonnull Set<Layer1Node> nodes() {
    return _graph.nodes();
  }

  /** Returns true if this {@link Layer1Topology} has no edges (it may have nodes). */
  @JsonIgnore
  public boolean isEmpty() {
    return _graph.edges().isEmpty();
  }

  @JsonProperty(PROP_EDGES)
  private SortedSet<Layer1Edge> getJsonEdges() {
    return edgeStream().collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
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
