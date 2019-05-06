package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Represents a set of {@link Edge Edges} and provides methods to prune the edges with edge, node,
 * and interface blacklists.
 */
public final class Topology implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static Topology jacksonCreateTopology(SortedSet<Edge> edges) {
    return new Topology(firstNonNull(edges, ImmutableSortedSet.of()));
  }

  private final SortedSet<Edge> _edges;

  // Mapping of interface -> set of all neighboring interfaces
  private final Map<NodeInterfacePair, SortedSet<NodeInterfacePair>> _interfaceNeighbors;

  // Mapping of node -> set of all edges whose source or dest is on that node
  private final Map<String, SortedSet<Edge>> _nodeEdges;

  public Topology(SortedSet<Edge> edges) {
    _edges = ImmutableSortedSet.copyOf(edges);
    _nodeEdges = computeNodeEdges(edges);
    _interfaceNeighbors = computeInterfaceNeighbors(edges);
  }

  private static Map<NodeInterfacePair, SortedSet<NodeInterfacePair>> computeInterfaceNeighbors(
      Iterable<Edge> edges) {
    Map<NodeInterfacePair, ImmutableSortedSet.Builder<NodeInterfacePair>> builders =
        new HashMap<>();
    edges.forEach(
        edge ->
            builders
                .computeIfAbsent(edge.getTail(), k -> ImmutableSortedSet.naturalOrder())
                .add(edge.getHead()));
    return CommonUtil.toImmutableSortedMap(
        builders, Entry::getKey, entry -> entry.getValue().build());
  }

  private static Map<String, SortedSet<Edge>> computeNodeEdges(Iterable<Edge> edges) {
    Map<String, ImmutableSortedSet.Builder<Edge>> builders = new HashMap<>();
    for (Edge edge : edges) {
      String node1 = edge.getNode1();
      String node2 = edge.getNode2();

      builders.computeIfAbsent(node1, k -> ImmutableSortedSet.naturalOrder()).add(edge);
      builders.computeIfAbsent(node2, k -> ImmutableSortedSet.naturalOrder()).add(edge);
    }
    return CommonUtil.toImmutableSortedMap(
        builders, Entry::getKey, entry -> entry.getValue().build());
  }

  @JsonIgnore
  public SortedSet<Edge> getEdges() {
    return _edges;
  }

  @Nonnull
  public SortedSet<NodeInterfacePair> getNeighbors(NodeInterfacePair iface) {
    return _interfaceNeighbors.getOrDefault(iface, ImmutableSortedSet.of());
  }

  @JsonIgnore
  public Map<String, SortedSet<Edge>> getNodeEdges() {
    return _nodeEdges;
  }

  /** Removes the specified blacklists from the topology */
  public Topology prune(
      @Nonnull Set<Edge> blacklistEdges,
      @Nonnull Set<String> blacklistNodes,
      @Nonnull Set<NodeInterfacePair> blacklistInterfaces) {
    return new Topology(
        _edges.stream()
            .filter(
                edge ->
                    !blacklistEdges.contains(edge)
                        && !blacklistNodes.contains(edge.getNode1())
                        && !blacklistNodes.contains(edge.getNode2())
                        && !blacklistInterfaces.contains(edge.getTail())
                        && !blacklistInterfaces.contains(edge.getHead()))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural())));
  }

  @JsonValue
  public SortedSet<Edge> sortedEdges() {
    return _edges;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Topology)) {
      return false;
    }
    Topology topology = (Topology) o;
    return Objects.equals(_edges, topology._edges)
        && Objects.equals(_interfaceNeighbors, topology._interfaceNeighbors)
        && Objects.equals(_nodeEdges, topology._nodeEdges);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_edges, _interfaceNeighbors, _nodeEdges);
  }
}
