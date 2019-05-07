package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.StateExpr;

/**
 * Instruments the return-pass graph edges of a bidirectional reachability analysis.
 *
 * <p>Ordinarily, we consider flows with disposition NEIGHBOR_UNREACHABLE or INSUFFICIENT_INFO,
 * which are instances of ARP failure, to be failures. In the return pass of a bidirectional
 * reachability query, however, those flows are considered successful if they represent the return
 * flow returning to the origination point of a forward flow. This instrumentation takes input
 * identifying flows in the return pass graph that are considered success, and if they ordinarily
 * are considered failures, instruments the return pass graph to make sure they are not considered
 * failures.
 *
 * <p>In the future, we may also want to consider flows that are successfully delivered to the wrong
 * place to be errors. This would require adding more instrumentation to the graph. It also would
 * require the origination states to have disjoint headerspaces.
 */
final class BidirectionalReachabilityReturnPassInstrumentation {
  /**
   * For a particular {@link NodeInterfaceInsufficientInfo} or {@link
   * NodeInterfaceNeighborUnreachable} state, this map identifies packets which, should they reach
   * that state, should not be considered failures in the return pass. The return pass graph will be
   * instrumented to block those packets from the out-edges leaving that state.
   */
  private final Map<StateExpr, BDD> _returnPassSuccessConstraints;

  BidirectionalReachabilityReturnPassInstrumentation(
      Map<StateExpr, BDD> returnPassSuccessConstraints) {
    _returnPassSuccessConstraints = ImmutableMap.copyOf(returnPassSuccessConstraints);
  }

  static Stream<Edge> instrumentReturnPassEdges(
      Map<StateExpr, BDD> returnPassSuccessConstraints, Stream<Edge> edges) {
    return new BidirectionalReachabilityReturnPassInstrumentation(returnPassSuccessConstraints)
        .instrumentReturnPassEdges(edges);
  }

  Stream<Edge> instrumentReturnPassEdges(Stream<Edge> edges) {
    return edges.map(this::instrumentReturnPassEdge);
  }

  Edge instrumentReturnPassEdge(Edge edge) {
    StateExpr preState = edge.getPreState();
    if (!(preState instanceof NodeInterfaceInsufficientInfo
        || preState instanceof NodeInterfaceNeighborUnreachable)) {
      return edge;
    }

    BDD successFlows = _returnPassSuccessConstraints.get(preState);
    if (successFlows == null) {
      // Forward pass didn't originate at this node/interface.
      return edge;
    }

    /* Forward pass did origiante at this node/interface. Prevent the bidirectional success
     * flows from also being considered failures.
     */
    return new Edge(
        preState,
        edge.getPostState(),
        compose(edge.getTransition(), constraint(successFlows.not())));
  }
}
