package org.batfish.datamodel.ospf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/** Candidate OSPF topology, including unestablished/incompatible OSPF neighbors. */
public final class CandidateOspfTopology {
  private final @Nonnull ValueGraph<OspfNeighborConfigId, OspfSessionStatus> _graph;

  public static final CandidateOspfTopology EMPTY =
      new CandidateOspfTopology(ValueGraphBuilder.directed().build());

  public CandidateOspfTopology(ValueGraph<OspfNeighborConfigId, OspfSessionStatus> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  /** Get the underlying graph representing this OSPF topology */
  @VisibleForTesting
  ValueGraph<OspfNeighborConfigId, OspfSessionStatus> getGraph() {
    return _graph;
  }

  /**
   * Get the session status for the edge between the specified neighbors. If the specified edge is
   * not in the graph, an empty Optional is returned.
   */
  public @Nonnull Optional<OspfSessionStatus> getSessionStatus(
      OspfNeighborConfigId n, OspfNeighborConfigId n1) {
    return _graph.edgeValue(n, n1);
  }

  /**
   * Return a set of neighbors adjacent to a given node. If the node is not in the graph, an empty
   * set is returned.
   */
  public @Nonnull Set<OspfNeighborConfigId> neighbors(OspfNeighborConfigId node) {
    if (!_graph.nodes().contains(node)) {
      return ImmutableSet.of();
    }
    return _graph.adjacentNodes(node);
  }
}
