package org.batfish.common.topology;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.graph.AbstractGraph;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link Serializable} immutable {@link Graph}. */
@ParametersAreNonnullByDefault
public class SerializableGraph<N extends Serializable> extends AbstractGraph<N>
    implements Serializable {

  private class GraphSupplier implements Supplier<ImmutableGraph<N>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final transient ImmutableGraph<N> _graph;

    private GraphSupplier(ImmutableGraph<N> graph) {
      _graph = graph;
    }

    @Override
    public @Nonnull ImmutableGraph<N> get() {
      return _graph != null ? _graph : computeGraph();
    }
  }

  private static final long serialVersionUID = 1L;

  private final boolean _allowSelfLoops;
  private final boolean _directed;
  private final ImmutableSet<Entry<N, N>> _edges;
  private final Supplier<ImmutableGraph<N>> _graph;
  private final ImmutableSet<N> _nodes;

  public SerializableGraph(Graph<N> graph) {
    ImmutableGraph<N> immutableGraph = ImmutableGraph.copyOf(graph);
    _allowSelfLoops = immutableGraph.allowsSelfLoops();
    _directed = immutableGraph.isDirected();
    _nodes = ImmutableSet.copyOf(immutableGraph.nodes());
    _edges =
        immutableGraph.edges().stream()
            .map(edge -> Maps.immutableEntry(edge.nodeU(), edge.nodeV()))
            .collect(ImmutableSet.toImmutableSet());
    _graph = Suppliers.memoize(new GraphSupplier(immutableGraph));
  }

  @Override
  public Set<N> adjacentNodes(N node) {
    return _graph.get().adjacentNodes(node);
  }

  @Override
  public boolean allowsSelfLoops() {
    return _graph.get().allowsSelfLoops();
  }

  private @Nonnull ImmutableGraph<N> computeGraph() {
    MutableGraph<N> graph =
        (_directed ? GraphBuilder.directed() : GraphBuilder.undirected())
            .allowsSelfLoops(_allowSelfLoops)
            .build();
    _nodes.forEach(graph::addNode);
    _edges.forEach(edge -> graph.putEdge(edge.getKey(), edge.getValue()));
    return ImmutableGraph.copyOf(graph);
  }

  @Override
  public int degree(N node) {
    return _graph.get().degree(node);
  }

  @Override
  public Set<EndpointPair<N>> edges() {
    return _graph.get().edges();
  }

  @Override
  public boolean hasEdgeConnecting(N nodeU, N nodeV) {
    return _graph.get().hasEdgeConnecting(nodeU, nodeV);
  }

  @Override
  public Set<EndpointPair<N>> incidentEdges(N node) {
    return _graph.get().incidentEdges(node);
  }

  @Override
  public int inDegree(N node) {
    return _graph.get().inDegree(node);
  }

  @Override
  public boolean isDirected() {
    return _graph.get().isDirected();
  }

  @Override
  public ElementOrder<N> nodeOrder() {
    return _graph.get().nodeOrder();
  }

  @Override
  public Set<N> nodes() {
    return _graph.get().nodes();
  }

  @Override
  public int outDegree(N node) {
    return _graph.get().outDegree(node);
  }

  @Override
  public Set<N> predecessors(N node) {
    return _graph.get().predecessors(node);
  }

  @Override
  public Set<N> successors(N node) {
    return _graph.get().successors(node);
  }
}
