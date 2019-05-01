package org.batfish.common.topology;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link Serializable} immutable {@link Network}. */
@ParametersAreNonnullByDefault
public class SerializableNetwork<N extends Serializable, E extends Serializable>
    extends AbstractNetwork<N, E> implements Serializable {

  private class NetworkSupplier implements Supplier<ImmutableNetwork<N, E>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final transient ImmutableNetwork<N, E> _network;

    private NetworkSupplier(ImmutableNetwork<N, E> network) {
      _network = network;
    }

    @Override
    public @Nonnull ImmutableNetwork<N, E> get() {
      return _network != null ? _network : computeNetwork();
    }
  }

  private static final long serialVersionUID = 1L;

  private final boolean _allowParallelEdges;
  private final boolean _allowSelfLoops;
  private final boolean _directed;
  private final ImmutableMap<E, Entry<N, N>> _edges;
  private final Supplier<ImmutableNetwork<N, E>> _network;
  private final ImmutableSet<N> _nodes;

  public SerializableNetwork(Network<N, E> network) {
    ImmutableNetwork<N, E> immutableNetwork = ImmutableNetwork.copyOf(network);
    _allowParallelEdges = immutableNetwork.allowsParallelEdges();
    _allowSelfLoops = immutableNetwork.allowsSelfLoops();
    _directed = immutableNetwork.isDirected();
    _nodes = ImmutableSet.copyOf(immutableNetwork.nodes());
    _edges =
        immutableNetwork.edges().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(),
                    edge -> {
                      EndpointPair<N> pair = immutableNetwork.incidentNodes(edge);
                      return Maps.immutableEntry(pair.nodeU(), pair.nodeV());
                    }));
    _network = Suppliers.memoize(new NetworkSupplier(immutableNetwork));
  }

  @Override
  public Set<N> adjacentNodes(N node) {
    return _network.get().adjacentNodes(node);
  }

  @Override
  public boolean allowsParallelEdges() {
    return _network.get().allowsParallelEdges();
  }

  @Override
  public boolean allowsSelfLoops() {
    return _network.get().allowsSelfLoops();
  }

  private @Nonnull ImmutableNetwork<N, E> computeNetwork() {
    MutableNetwork<N, E> network =
        (_directed ? NetworkBuilder.directed() : NetworkBuilder.undirected())
            .allowsParallelEdges(_allowParallelEdges)
            .allowsSelfLoops(_allowSelfLoops)
            .build();
    _nodes.forEach(network::addNode);
    _edges.forEach((edge, nodes) -> network.addEdge(nodes.getKey(), nodes.getValue(), edge));
    return ImmutableNetwork.copyOf(network);
  }

  @Override
  public ElementOrder<E> edgeOrder() {
    return _network.get().edgeOrder();
  }

  @Override
  public Set<E> edges() {
    return _network.get().edges();
  }

  @Override
  public Set<E> incidentEdges(N node) {
    return _network.get().incidentEdges(node);
  }

  @Override
  public EndpointPair<N> incidentNodes(E edge) {
    return _network.get().incidentNodes(edge);
  }

  @Override
  public Set<E> inEdges(N node) {
    return _network.get().inEdges(node);
  }

  @Override
  public boolean isDirected() {
    return _network.get().isDirected();
  }

  @Override
  public ElementOrder<N> nodeOrder() {
    return _network.get().nodeOrder();
  }

  @Override
  public Set<N> nodes() {
    return _network.get().nodes();
  }

  @Override
  public Set<E> outEdges(N node) {
    return _network.get().outEdges(node);
  }

  @Override
  public Set<N> predecessors(N node) {
    return _network.get().predecessors(node);
  }

  @Override
  public Set<N> successors(N node) {
    return _network.get().successors(node);
  }
}
