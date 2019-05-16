package org.batfish.bddreachability;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.symbolic.state.StateExpr;

/**
 * Utility methods for {@link BDDReachabilityAnalysis} and {@link BDDReachabilityAnalysisFactory}.
 */
final class BDDReachabilityUtils {
  static Table<StateExpr, StateExpr, Edge> computeForwardEdgeTable(Collection<Edge> edges) {
    Table<StateExpr, StateExpr, Edge> initial =
        edges.stream()
            .collect(
                Tables.toTable(
                    Edge::getPreState,
                    Edge::getPostState,
                    Function.identity(),
                    (oldVal, newVal) ->
                        new Edge(
                            oldVal.getPreState(),
                            oldVal.getPostState(),
                            Transitions.or(oldVal.getTransition(), newVal.getTransition())),
                    HashBasedTable::create));
    System.err.println("Initial size: " + initial.size());

    Multimap<StateExpr, StateExpr> fwdMap =
        edges.stream()
            .collect(
                Multimaps.toMultimap(Edge::getPreState, Edge::getPostState, HashMultimap::create));
    Multimap<StateExpr, StateExpr> revMap =
        edges.stream()
            .collect(
                Multimaps.toMultimap(Edge::getPostState, Edge::getPreState, HashMultimap::create));
    Set<StateExpr> oneInOneOut =
        fwdMap.keySet().stream()
            .filter(s -> fwdMap.get(s).size() == 1)
            .filter(s -> revMap.get(s).size() == 1)
            .collect(Collectors.toSet());

    oneInOneOut.forEach(
        s -> {
          StateExpr preS = Iterables.getOnlyElement(revMap.get(s));
          StateExpr postS = Iterables.getOnlyElement(fwdMap.get(s));
          Edge inbound = initial.remove(preS, s);
          assert inbound != null;
          assert inbound.getPreState() == preS;
          Edge outbound = initial.remove(s, postS);
          assert outbound != null;
          assert outbound.getPostState() == postS;
          initial.put(
              preS,
              postS,
              new Edge(
                  preS,
                  postS,
                  Transitions.compose(inbound.getTransition(), outbound.getTransition())));
          fwdMap.remove(preS, s);
          fwdMap.put(preS, postS);
          revMap.remove(postS, s);
          revMap.put(postS, preS);
        });
    System.err.println("Final size: " + initial.size());

    return ImmutableTable.copyOf(initial);
  }
}
