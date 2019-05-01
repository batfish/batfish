package org.batfish.common.topology;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.graph.AbstractValueGraph;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link Serializable} immutable {@link ValueGraph}. */
@ParametersAreNonnullByDefault
public class SerializableValueGraph<N, V> extends AbstractValueGraph<N, V> implements Serializable {

  private class GraphSupplier implements Supplier<ImmutableValueGraph<N, V>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final transient ImmutableValueGraph<N, V> _graph;

    private GraphSupplier(ImmutableValueGraph<N, V> graph) {
      _graph = graph;
    }

    @Override
    public @Nonnull ImmutableValueGraph<N, V> get() {
      return _graph != null ? _graph : computeGraph();
    }
  }

  private static final long serialVersionUID = 1L;

  private final boolean _allowSelfLoops;
  private final boolean _directed;
  private final ImmutableSet<Entry<N, N>> _edges;
  private final Supplier<ImmutableValueGraph<N, V>> _graph;
  private final ImmutableSet<N> _nodes;
  private final ImmutableMap<Entry<N, N>, V> _values;

  public SerializableValueGraph(ValueGraph<N, V> graph) {
    ImmutableValueGraph<N, V> immutableGraph = ImmutableValueGraph.copyOf(graph);
    _allowSelfLoops = immutableGraph.allowsSelfLoops();
    _directed = immutableGraph.isDirected();
    _nodes = ImmutableSet.copyOf(immutableGraph.nodes());
    _edges =
        immutableGraph.edges().stream()
            .map(edge -> Maps.immutableEntry(edge.nodeU(), edge.nodeV()))
            .collect(ImmutableSet.toImmutableSet());
    _values =
        _edges.stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(),
                    edge -> immutableGraph.edgeValue(edge.getKey(), edge.getValue()).get()));
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

  private @Nonnull ImmutableValueGraph<N, V> computeGraph() {
    MutableValueGraph<N, V> graph =
        (_directed ? ValueGraphBuilder.directed() : ValueGraphBuilder.undirected())
            .allowsSelfLoops(_allowSelfLoops)
            .build();
    _nodes.forEach(graph::addNode);
    _edges.forEach(edge -> graph.putEdgeValue(edge.getKey(), edge.getValue(), _values.get(edge)));
    return ImmutableValueGraph.copyOf(graph);
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
  public V edgeValueOrDefault(N nodeU, N nodeV, V defaultValue) {
    return _graph.get().edgeValueOrDefault(nodeU, nodeV, defaultValue);
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
