package org.batfish.datamodel.bgp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.ValueEdge;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;

/** A topology representing all BGP peerings. */
@ParametersAreNonnullByDefault
public final class BgpTopology {

  public static final BgpTopology EMPTY =
      new BgpTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build());
  private static final String PROP_EDGES = "edges";
  private static final String PROP_NODES = "nodes";

  @JsonCreator
  private static @Nonnull BgpTopology create(
      @JsonProperty(PROP_EDGES) @Nullable
          List<ValueEdge<BgpPeerConfigId, BgpSessionProperties>> edges,
      @JsonProperty(PROP_NODES) @Nullable Set<BgpPeerConfigId> nodes) {
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    if (nodes != null) {
      nodes.forEach(graph::addNode);
    }
    if (edges != null) {
      edges.forEach(
          valueEdge ->
              graph.putEdgeValue(
                  valueEdge.getSource(), valueEdge.getTarget(), valueEdge.getValue()));
    }
    return new BgpTopology(graph);
  }

  private final ValueGraph<BgpPeerConfigId, BgpSessionProperties> _graph;

  public BgpTopology(ValueGraph<BgpPeerConfigId, BgpSessionProperties> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  @JsonProperty(PROP_EDGES)
  private List<ValueEdge<BgpPeerConfigId, BgpSessionProperties>> getEdges() {
    return _graph.edges().stream()
        .map(
            endpointPair ->
                new ValueEdge<>(
                    endpointPair.source(),
                    endpointPair.target(),
                    _graph.edgeValue(endpointPair.source(), endpointPair.target()).get()))
        .collect(ImmutableList.toImmutableList());
  }

  @JsonIgnore
  public @Nonnull ValueGraph<BgpPeerConfigId, BgpSessionProperties> getGraph() {
    return _graph;
  }

  @JsonProperty(PROP_NODES)
  private @Nonnull Set<BgpPeerConfigId> getNodes() {
    return _graph.nodes();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BgpTopology)) {
      return false;
    }
    return _graph.equals(((BgpTopology) obj)._graph);
  }

  @Override
  public int hashCode() {
    return _graph.hashCode();
  }

  /** Directional, reversible BGP edge pointing to two {@link BgpPeerConfigId}. */
  @ParametersAreNonnullByDefault
  public static final class EdgeId implements Comparable<EdgeId> {

    private final @Nonnull BgpPeerConfigId _tail;
    private final @Nonnull BgpPeerConfigId _head;
    private static final Comparator<EdgeId> COMPARATOR =
        Comparator.comparing(EdgeId::tail).thenComparing(EdgeId::head);

    public EdgeId(BgpPeerConfigId tail, BgpPeerConfigId head) {
      _tail = tail;
      _head = head;
    }

    public BgpPeerConfigId tail() {
      return _tail;
    }

    public BgpPeerConfigId head() {
      return _head;
    }

    public EdgeId reverse() {
      return new EdgeId(_head, _tail);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof EdgeId)) {
        return false;
      }
      EdgeId other = (EdgeId) o;
      return _tail.equals(other._tail) && _head.equals(other._head);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_tail, _head);
    }

    @Override
    public int compareTo(EdgeId o) {
      return COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("tail", _tail).add("head", _head).toString();
    }
  }
}
