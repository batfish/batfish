package org.batfish.datamodel.ospf;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;

/** Candidate OSPF topology, including incompatible OSPF neighbors. */
public class CandidateOspfTopology {
  @Nonnull private final ValueGraph<OspfNeighborConfigId, OspfSessionStatus> _graph;

  CandidateOspfTopology(ValueGraph<OspfNeighborConfigId, OspfSessionStatus> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  public ValueGraph<OspfNeighborConfigId, OspfSessionStatus> getGraph() {
    return _graph;
  }

  @Nonnull
  public Optional<OspfSessionStatus> getSession(EdgeId id) {
    return _graph.edgeValue(id.getTail(), id.getHead());
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
}
