package org.batfish.bddreachability;

import static org.batfish.common.util.CommonUtil.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.z3.expr.StateExpr;

/**
 * Utility methods for {@link BDDReachabilityAnalysis} and {@link BDDReachabilityAnalysisFactory}.
 */
public final class BDDReachabilityUtils {
  static Map<StateExpr, Map<StateExpr, Edge>> computeForwardEdgeMap(Iterable<Edge> edges) {
    Map<StateExpr, Map<StateExpr, Edge>> forwardEdges = new HashMap<>();

    edges.forEach(
        edge ->
            forwardEdges
                .computeIfAbsent(edge.getPreState(), k -> new HashMap<>())
                .put(edge.getPostState(), edge));

    // freeze
    return toImmutableMap(
        forwardEdges, Entry::getKey, entry -> ImmutableMap.copyOf(entry.getValue()));
  }

  static Map<StateExpr, Map<StateExpr, Edge>> computeReverseEdgeMap(Iterable<Edge> edges) {
    Map<StateExpr, Map<StateExpr, Edge>> reverseEdges = new HashMap<>();
    edges.forEach(
        edge ->
            reverseEdges
                .computeIfAbsent(edge.getPostState(), k -> new HashMap<>())
                .put(edge.getPreState(), edge));
    // freeze
    return toImmutableMap(
        reverseEdges, Entry::getKey, entry -> ImmutableMap.copyOf(entry.getValue()));
  }
}
