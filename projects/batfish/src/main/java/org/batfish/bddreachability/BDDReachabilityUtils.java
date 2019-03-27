package org.batfish.bddreachability;

import static com.google.common.collect.ImmutableTable.toImmutableTable;

import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import java.util.function.Function;
import org.batfish.z3.expr.StateExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for {@link BDDReachabilityAnalysis} and {@link BDDReachabilityAnalysisFactory}.
 */
final class BDDReachabilityUtils {
  private static final Logger logger = LoggerFactory.getLogger(BDDReachabilityUtils.class);

  static Table<StateExpr, StateExpr, Edge> computeForwardEdgeTable(Iterable<Edge> edges) {
    return Streams.stream(edges)
        .collect(
            toImmutableTable(
                Edge::getPreState,
                Edge::getPostState,
                Function.identity(),
                (oldVal, newVal) -> {
                  // The prior implementation using HashMap overwrote with new value. Keep that
                  // behavior but warn.
                  logger.warn("Overwriting old transition {} with {}", oldVal, newVal);
                  return newVal;
                }));
  }
}
