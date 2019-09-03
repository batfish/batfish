package org.batfish.datamodel.ospf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;

/** Candidate OSPF topology, including unestablished/incompatible OSPF neighbors. */
public final class CandidateOspfTopology {
  @Nonnull private final ValueGraph<OspfNeighborConfigId, OspfSessionStatus> _graph;

  public static final CandidateOspfTopology EMPTY =
      new CandidateOspfTopology(ValueGraphBuilder.directed().build());

  CandidateOspfTopology(ValueGraph<OspfNeighborConfigId, OspfSessionStatus> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  /** Get the underlying graph representing this OSPF topology */
  @VisibleForTesting
  ValueGraph<OspfNeighborConfigId, OspfSessionStatus> getGraph() {
    return _graph;
  }

  /**
   * Get the session status for the specified edge. If the specified edge is not in the graph, an
   * empty Optional is returned.
   */
  @Nonnull
  public Optional<OspfSessionStatus> getSessionStatus(EdgeId id) {
    return _graph.edgeValue(id.getTail(), id.getHead());
  }

  /**
   * Return a set of neighbors adjacent to a given node. If the node is not in the graph, an empty
   * set is returned.
   */
  @Nonnull
  public Set<OspfNeighborConfigId> neighbors(OspfNeighborConfigId node) {
    if (!_graph.nodes().contains(node)) {
      return ImmutableSet.of();
    }
    return _graph.adjacentNodes(node);
  }
}
