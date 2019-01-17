package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.ValueGraph;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
    return getInterfaceEdges().getOrDefault(iface, ImmutableSortedSet.of()).stream()
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

  /**
   * Given a {@link ValueGraph} between {@link IpsecPeerConfigId}s, trims the edges in current
   * {@link Topology} which are not fully established in the IPsec topology (e.g. they do not have a
   * negotiated {@link IpsecPhase2Proposal})
   *
   * @param ipsecTopology original IPsec topology's {@link ValueGraph}
   * @param configurations {@link Map} of {@link Configuration} to configuration names
   */
  public void pruneFailedIpsecSessionEdges(
      ValueGraph<IpsecPeerConfigId, IpsecSession> ipsecTopology,
      Map<String, Configuration> configurations) {
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);

    // Set of successful IPsec edges
    Set<Edge> successfulIPsecEdges = new HashSet<>();
    // NodeInterface pairs running IPsec peering on tunnel interfaces
    Set<NodeInterfacePair> tunnelIpsecEndpoints = new HashSet<>();

    for (IpsecPeerConfigId endPointU : ipsecTopology.nodes()) {
      IpsecPeerConfig ipsecPeerU = networkConfigurations.getIpsecPeerConfig(endPointU);
      // not considering endpoints not based on Tunnel interfaces
      if (ipsecPeerU == null || ipsecPeerU.getTunnelInterface() == null) {
        continue;
      }

      NodeInterfacePair tunnelEndPointU =
          new NodeInterfacePair(endPointU.getHostName(), ipsecPeerU.getTunnelInterface());
      tunnelIpsecEndpoints.add(tunnelEndPointU);

      for (IpsecPeerConfigId endPointV : ipsecTopology.adjacentNodes(endPointU)) {
        IpsecPeerConfig ipsecPeerV = networkConfigurations.getIpsecPeerConfig(endPointV);

        // not considering endpoints not based on Tunnel interfaces
        if (ipsecPeerV == null || ipsecPeerV.getTunnelInterface() == null) {
          continue;
        }

        NodeInterfacePair tunnelEndPointV =
            new NodeInterfacePair(endPointV.getHostName(), ipsecPeerV.getTunnelInterface());

        // checking IPsec session and adding edge
        Optional<IpsecSession> edgeIpsecSession = ipsecTopology.edgeValue(endPointU, endPointV);
        if (edgeIpsecSession.isPresent()
            && edgeIpsecSession.get().getNegotiatedIpsecP2Proposal() != null) {
          successfulIPsecEdges.add(new Edge(tunnelEndPointU, tunnelEndPointV));
        }
      }
    }

    Set<Edge> failedIpsecEdges = new HashSet<>();

    for (Edge edge : _edges) {
      NodeInterfacePair tail = edge.getTail();
      NodeInterfacePair head = edge.getHead();
      if ((tunnelIpsecEndpoints.contains(tail) || tunnelIpsecEndpoints.contains(head))
          && (!successfulIPsecEdges.contains(edge)
              || !successfulIPsecEdges.contains(edge.reverse()))) {
        failedIpsecEdges.add(edge);
      }
    }

    prune(failedIpsecEdges, ImmutableSet.of(), ImmutableSet.of());
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
