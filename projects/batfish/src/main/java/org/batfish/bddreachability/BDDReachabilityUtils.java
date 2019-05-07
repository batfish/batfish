package org.batfish.bddreachability;

import static com.google.common.collect.ImmutableTable.toImmutableTable;

import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import java.util.function.Function;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.symbolic.state.StateExpr;

/**
 * Utility methods for {@link BDDReachabilityAnalysis} and {@link BDDReachabilityAnalysisFactory}.
 */
final class BDDReachabilityUtils {
  static Table<StateExpr, StateExpr, Edge> computeForwardEdgeTable(Iterable<Edge> edges) {
    return Streams.stream(edges)
        .collect(
            toImmutableTable(
                Edge::getPreState,
                Edge::getPostState,
                Function.identity(),
                (oldVal, newVal) ->
                    new Edge(
                        oldVal.getPreState(),
                        oldVal.getPostState(),
                        Transitions.or(oldVal.getTransition(), newVal.getTransition()))));
  }
}
