package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.StateExpr;

/**
 * Utility methods for {@link BDDReachabilityAnalysis} and {@link BDDReachabilityAnalysisFactory}.
 */
public final class BDDReachabilityUtils {
  public static Table<StateExpr, StateExpr, Transition> computeForwardEdgeTable(Iterable<Edge> edges) {
    return computeForwardEdgeTable(Streams.stream(edges));
  }

  static Table<StateExpr, StateExpr, Transition> computeForwardEdgeTable(Stream<Edge> edges) {
    return edges.collect(
        toImmutableTable(
            Edge::getPreState,
            Edge::getPostState,
            Edge::getTransition,
            (t1, t2) -> Transitions.or(t1, t2)));
  }

  /** Apply edges to the reachableSets until a fixed point is reached. */
  @VisibleForTesting
  static void fixpoint(
      Map<StateExpr, BDD> reachableSets,
      Table<StateExpr, StateExpr, Transition> edges,
      BiFunction<Transition, BDD, BDD> traverse) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("BDDReachabilityAnalysis.fixpoint").startActive()) {
      assert span != null; // avoid unused warning
      Set<StateExpr> dirtyStates = ImmutableSet.copyOf(reachableSets.keySet());

      while (!dirtyStates.isEmpty()) {
        Set<StateExpr> newDirtyStates = new HashSet<>();

        dirtyStates.forEach(
            dirtyState -> {
              Map<StateExpr, Transition> dirtyStateEdges = edges.row(dirtyState);
              if (dirtyStateEdges == null) {
                // dirtyState has no edges
                return;
              }

              BDD dirtyStateBDD = reachableSets.get(dirtyState);
              dirtyStateEdges.forEach(
                  (neighbor, edge) -> {
                    BDD result = traverse.apply(edge, dirtyStateBDD);
                    if (result.isZero()) {
                      return;
                    }

                    // update neighbor's reachable set
                    BDD oldReach = reachableSets.get(neighbor);
                    BDD newReach = oldReach == null ? result : oldReach.or(result);
                    if (oldReach == null || !oldReach.equals(newReach)) {
                      reachableSets.put(neighbor, newReach);
                      newDirtyStates.add(neighbor);
                    }
                  });
            });

        dirtyStates = newDirtyStates;
      }
    }
  }

  @VisibleForTesting
  static IngressLocation toIngressLocation(StateExpr stateExpr) {
    checkArgument(stateExpr instanceof OriginateVrf || stateExpr instanceof OriginateInterfaceLink);

    if (stateExpr instanceof OriginateVrf) {
      OriginateVrf originateVrf = (OriginateVrf) stateExpr;
      return IngressLocation.vrf(originateVrf.getHostname(), originateVrf.getVrf());
    } else {
      OriginateInterfaceLink originateInterfaceLink = (OriginateInterfaceLink) stateExpr;
      return IngressLocation.interfaceLink(
          originateInterfaceLink.getHostname(), originateInterfaceLink.getInterface());
    }
  }

  public static void backwardFixpoint(
      Table<StateExpr, StateExpr, Transition> forwardEdgeTable,
      Map<StateExpr, BDD> reverseReachable) {
    fixpoint(reverseReachable, Tables.transpose(forwardEdgeTable), Transition::transitBackward);
  }

  static void forwardFixpoint(
      Table<StateExpr, StateExpr, Transition> forwardEdgeTable, Map<StateExpr, BDD> reachable) {
    fixpoint(reachable, forwardEdgeTable, Transition::transitForward);
  }

  static Map<IngressLocation, BDD> getIngressLocationBdds(
      Map<StateExpr, BDD> stateReachableBdds, Set<StateExpr> ingressLocationStates, BDD zero) {
    return toImmutableMap(
        ingressLocationStates,
        BDDReachabilityUtils::toIngressLocation,
        stateExpr -> stateReachableBdds.getOrDefault(stateExpr, zero));
  }
}
