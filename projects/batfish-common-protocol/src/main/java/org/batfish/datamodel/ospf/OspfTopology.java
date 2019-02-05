package org.batfish.datamodel.ospf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A graph representing OSPF adjacencies */
@ParametersAreNonnullByDefault
public final class OspfTopology {

  private final ValueGraph<OspfNeighborConfigId, OspfSessionProperties> _graph;

  public OspfTopology(ValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  /**
   * Return a set of neighbors adjacent to a given node. If the node is not in a graph, an empty set
   * is returned.
   */
  @Nonnull
  public Set<OspfNeighborConfigId> neighbors(OspfNeighborConfigId node) {
    if (!_graph.nodes().contains(node)) {
      return ImmutableSet.of();
    }
    return _graph.adjacentNodes(node);
  }

  @Nonnull
  public Optional<OspfSessionProperties> getSession(EdgeId id) {
    return _graph.edgeValue(id.getTail(), id.getHead());
  }

  /**
   * Return the set of incoming edges to the given OSPF neighbor.
   *
   * <p>Returns the empty set if the neighbor is not in the graph.
   */
  @Nonnull
  public Set<EdgeId> incomingEdges(OspfNeighborConfigId head) {
    if (!_graph.nodes().contains(head)) {
      return ImmutableSet.of();
    }
    return _graph.predecessors(head).stream()
        .map(tail -> makeEdge(tail, head))
        .collect(ImmutableSet.toImmutableSet());
  }

  /** Return an empty topology (no nodes and no edges) */
  public static OspfTopology empty() {
    return new OspfTopology(ValueGraphBuilder.directed().build());
  }

  /**
   * Return the {@link EdgeId} represending an adjacency between two {@link OspfNeighborConfigId}s
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
    return Objects.equals(_graph, topology._graph);
  }

  /** Return the graph backing this topology */
  @VisibleForTesting
  ValueGraph<OspfNeighborConfigId, OspfSessionProperties> getGraph() {
    return _graph;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_graph);
  }

  /** Directed OSPF edge representing a link between two {@link OspfNeighborConfigId} */
  @ParametersAreNonnullByDefault
  public static final class EdgeId implements Comparable<EdgeId> {
    private final OspfNeighborConfigId _tail;
    private final OspfNeighborConfigId _head;

    private static final Comparator<OspfNeighborConfigId> ID_COMPARATOR =
        Comparator.comparing(OspfNeighborConfigId::getHostname)
            .thenComparing(OspfNeighborConfigId::getVrfName)
            .thenComparing(OspfNeighborConfigId::getInterfaceName);

    private EdgeId(OspfNeighborConfigId tail, OspfNeighborConfigId head) {
      _tail = tail;
      _head = head;
    }

    /** Return the tail/src node */
    public OspfNeighborConfigId getTail() {
      return _tail;
    }

    /** Return the head/dst node */
    public OspfNeighborConfigId getHead() {
      return _head;
    }

    public EdgeId reverse() {
      return makeEdge(_head, _tail);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      EdgeId other = (EdgeId) o;
      return Objects.equals(_tail, other._tail) && Objects.equals(_head, other._head);
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
