package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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

  // Mapping of interface -> set of all edges whose source or dest is that interface
  private final Map<NodeInterfacePair, SortedSet<Edge>> _interfaceEdges;

  // Mapping of node -> set of all edges whose source or dest is on that node
  private final Map<String, SortedSet<Edge>> _nodeEdges;

  public Topology(SortedSet<Edge> edges) {
    _edges = new TreeSet<>(edges);
    _nodeEdges = new HashMap<>();
    _interfaceEdges = new HashMap<>();
    rebuildFromEdges();
  }

  @JsonIgnore
  public SortedSet<Edge> getEdges() {
    return _edges;
  }

  @JsonIgnore
  public Map<NodeInterfacePair, SortedSet<Edge>> getInterfaceEdges() {
    return _interfaceEdges;
  }

  public Set<NodeInterfacePair> getNeighbors(NodeInterfacePair iface) {
    return getInterfaceEdges()
        .getOrDefault(iface, ImmutableSortedSet.of())
        .stream()
        .filter(e -> e.getTail().equals(iface))
        .map(Edge::getHead)
        .collect(ImmutableSet.toImmutableSet());
  }

  @JsonIgnore
  public Map<String, SortedSet<Edge>> getNodeEdges() {
    return _nodeEdges;
  }

  /** Removes the specified blacklists from the topology */
  public void prune(
      Set<Edge> blacklistEdges,
      Set<String> blacklistNodes,
      Set<NodeInterfacePair> blacklistInterfaces) {
    if (blacklistEdges != null) {
      _edges.removeAll(blacklistEdges);
    }
    if (blacklistNodes != null) {
      for (String blacklistNode : blacklistNodes) {
        _edges.removeAll(_nodeEdges.getOrDefault(blacklistNode, ImmutableSortedSet.of()));
      }
    }
    if (blacklistInterfaces != null) {
      for (NodeInterfacePair blacklistInterface : blacklistInterfaces) {
        _edges.removeAll(_interfaceEdges.getOrDefault(blacklistInterface, ImmutableSortedSet.of()));
      }
    }
    rebuildFromEdges();
  }

  private void rebuildFromEdges() {
    _nodeEdges.clear();
    _interfaceEdges.clear();
    for (Edge edge : _edges) {
      String node1 = edge.getNode1();
      String node2 = edge.getNode2();
      NodeInterfacePair iface1 = edge.getTail();
      NodeInterfacePair iface2 = edge.getHead();

      _nodeEdges.computeIfAbsent(node1, k -> new TreeSet<>()).add(edge);
      _nodeEdges.computeIfAbsent(node2, k -> new TreeSet<>()).add(edge);
      _interfaceEdges.computeIfAbsent(iface1, k -> new TreeSet<>()).add(edge);
      _interfaceEdges.computeIfAbsent(iface2, k -> new TreeSet<>()).add(edge);
    }
  }

  @JsonValue
  public SortedSet<Edge> sortedEdges() {
    return new TreeSet<>(_edges);
  }
}
