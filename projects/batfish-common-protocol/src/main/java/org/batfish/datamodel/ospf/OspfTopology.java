package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.ValueEdge;

/** A graph representing OSPF adjacencies */
@ParametersAreNonnullByDefault
public final class OspfTopology {

  public static final OspfTopology EMPTY = new OspfTopology(ValueGraphBuilder.directed().build());
  private static final String PROP_EDGES = "edges";
  private static final String PROP_NODES = "nodes";

  @JsonCreator
  private static @Nonnull OspfTopology create(
      @JsonProperty(PROP_EDGES) @Nullable
          List<ValueEdge<OspfNeighborConfigId, OspfSessionProperties>> edges,
      @JsonProperty(PROP_NODES) @Nullable Set<OspfNeighborConfigId> nodes) {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
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
    return new OspfTopology(graph);
  }

  private final @Nonnull ValueGraph<OspfNeighborConfigId, OspfSessionProperties> _graph;

  public OspfTopology(ValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  /**
   * Return a set of neighbors adjacent to a given node. If the node is not in a graph, an empty set
   * is returned.
   */
  public @Nonnull Set<OspfNeighborConfigId> neighbors(OspfNeighborConfigId node) {
    if (!_graph.nodes().contains(node)) {
      return ImmutableSet.of();
    }
    return _graph.adjacentNodes(node);
  }

  /** Return edges present in the topology */
  public @Nonnull Set<EdgeId> edges() {
    return _graph.edges().stream()
        .map(pair -> makeEdge(pair.nodeU(), pair.nodeV()))
        .collect(ImmutableSet.toImmutableSet());
  }

  @JsonProperty(PROP_EDGES)
  private List<ValueEdge<OspfNeighborConfigId, OspfSessionProperties>> getEdges() {
    return _graph.edges().stream()
        .map(
            endpointPair ->
                new ValueEdge<>(
                    endpointPair.source(),
                    endpointPair.target(),
                    _graph.edgeValue(endpointPair.source(), endpointPair.target()).get()))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a {@link Set} of all {@link OspfNeighborConfigId node}s present in the topology. This
   * also includes nodes which don't have any neighbors.
   *
   * @return {@link Set} of {@link OspfNeighborConfigId}s
   */
  @JsonProperty(PROP_NODES)
  public @Nonnull Set<OspfNeighborConfigId> getNodes() {
    return _graph.nodes();
  }

  public @Nonnull Optional<OspfSessionProperties> getSession(EdgeId id) {
    return _graph.edgeValue(id.getTail(), id.getHead());
  }

  /**
   * Return the set of incoming edges to the given OSPF neighbor.
   *
   * <p>Returns the empty set if the neighbor is not in the graph.
   */
  public @Nonnull Set<EdgeId> incomingEdges(OspfNeighborConfigId head) {
    if (!_graph.nodes().contains(head)) {
      return ImmutableSet.of();
    }
    return _graph.predecessors(head).stream()
        .map(tail -> makeEdge(tail, head))
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Return the {@link EdgeId} representing an adjacency between two {@link OspfNeighborConfigId}s
   */
  public static EdgeId makeEdge(OspfNeighborConfigId tail, OspfNeighborConfigId head) {
    return new EdgeId(tail, head);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OspfTopology topology = (OspfTopology) o;
    return _graph.equals(topology._graph);
  }

  /** Return the graph backing this topology */
  @JsonIgnore
  public ValueGraph<OspfNeighborConfigId, OspfSessionProperties> getGraph() {
    return _graph;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_graph);
  }

  /** Directed OSPF edge representing a link between two {@link OspfNeighborConfigId} */
  @ParametersAreNonnullByDefault
  public static final class EdgeId implements Comparable<EdgeId> {
    private static final String PROP_TAIL = "tail";
    private static final String PROP_HEAD = "head";

    private final @Nonnull OspfNeighborConfigId _tail;
    private final @Nonnull OspfNeighborConfigId _head;

    private static final Comparator<OspfNeighborConfigId> ID_COMPARATOR =
        Comparator.comparing(OspfNeighborConfigId::getHostname)
            .thenComparing(OspfNeighborConfigId::getVrfName)
            .thenComparing(OspfNeighborConfigId::getInterfaceName)
            .thenComparing(OspfNeighborConfigId::getProcName)
            .thenComparing(OspfNeighborConfigId::getAddress);

    @VisibleForTesting
    EdgeId(OspfNeighborConfigId tail, OspfNeighborConfigId head) {
      _tail = tail;
      _head = head;
    }

    @JsonCreator
    private static EdgeId create(
        @JsonProperty(PROP_TAIL) @Nullable OspfNeighborConfigId tail,
        @JsonProperty(PROP_HEAD) @Nullable OspfNeighborConfigId head) {
      checkArgument(tail != null, "Missing %s", PROP_TAIL);
      checkArgument(head != null, "Missing %s", PROP_HEAD);
      return new EdgeId(tail, head);
    }

    /** Return the tail/src node */
    @JsonProperty(PROP_TAIL)
    public @Nonnull OspfNeighborConfigId getTail() {
      return _tail;
    }

    /** Return the head/dst node */
    @JsonProperty(PROP_HEAD)
    public @Nonnull OspfNeighborConfigId getHead() {
      return _head;
    }

    @JsonIgnore
    public EdgeId reverse() {
      return makeEdge(_head, _tail);
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
      return Comparator.comparing(EdgeId::getTail, ID_COMPARATOR)
          .thenComparing(EdgeId::getHead, ID_COMPARATOR)
          .compare(this, o);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("tail", _tail).add("head", _head).toString();
    }
  }
}
