package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class Topology implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static Topology jacksonCreateTopology(SortedSet<Edge> edges) {
    return new Topology(edges);
  }

  private final SortedSet<Edge> _edges;

  private final Map<NodeInterfacePair, SortedSet<Edge>> _interfaceEdges;

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
      SortedSet<Edge> edges = getEdges();
      edges.removeAll(blacklistEdges);
    }
    if (blacklistNodes != null) {
      for (String blacklistNode : blacklistNodes) {
        removeNode(blacklistNode);
      }
    }
    if (blacklistInterfaces != null) {
      for (NodeInterfacePair blacklistInterface : blacklistInterfaces) {
        removeInterface(blacklistInterface);
      }
    }
    rebuildFromEdges();
  }

  private void rebuildFromEdges() {
    _nodeEdges.clear();
    _interfaceEdges.clear();
    for (Edge edge : getEdges()) {
      String node1 = edge.getNode1();
      String node2 = edge.getNode2();
      NodeInterfacePair int1 = edge.getInterface1();
      NodeInterfacePair int2 = edge.getInterface2();

      SortedSet<Edge> node1Edges = _nodeEdges.computeIfAbsent(node1, k -> new TreeSet<>());
      node1Edges.add(edge);

      SortedSet<Edge> node2Edges = _nodeEdges.computeIfAbsent(node2, k -> new TreeSet<>());
      node2Edges.add(edge);

      SortedSet<Edge> interface1Edges = _interfaceEdges.computeIfAbsent(int1, k -> new TreeSet<>());
      interface1Edges.add(edge);

      SortedSet<Edge> interface2Edges = _interfaceEdges.computeIfAbsent(int2, k -> new TreeSet<>());
      interface2Edges.add(edge);
    }
  }

  public void removeInterface(NodeInterfacePair iface) {
    SortedSet<Edge> interfaceEdges = _interfaceEdges.get(iface);
    if (interfaceEdges != null) {
      _edges.removeAll(interfaceEdges);
    }
  }

  public void removeNode(String hostname) {
    SortedSet<Edge> nodeEdges = _nodeEdges.get(hostname);
    if (nodeEdges != null) {
      _edges.removeAll(nodeEdges);
    }
  }

  @JsonValue
  public SortedSet<Edge> sortedEdges() {
    return new TreeSet<>(_edges);
  }
}
