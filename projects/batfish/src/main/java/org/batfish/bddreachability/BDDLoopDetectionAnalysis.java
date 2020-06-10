package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDReachabilityUtils.backwardFixpoint;
import static org.batfish.bddreachability.BDDReachabilityUtils.getIngressLocationBdds;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.StateExpr;

/** A reachability analysis that detects loops. */
public class BDDLoopDetectionAnalysis {
  private final BDDPacket _bddPacket;
  private final Table<StateExpr, StateExpr, Transition> _forwardEdgeTable;
  private final Set<StateExpr> _ingressLocationStates;

  private BDDLoopDetectionAnalysis(
      BDDPacket bddPacket,
      Table<StateExpr, StateExpr, Transition> forwardEdgeTable,
      Set<StateExpr> ingressLocationStates) {
    _bddPacket = bddPacket;
    _ingressLocationStates = ingressLocationStates;
    _forwardEdgeTable = forwardEdgeTable;
  }

  private static Collection<Edge> getLoopEdges(
      Stream<Edge> inEdges, Set<StateExpr> ingressLocationStates) {
    List<Edge> edges = inEdges.collect(Collectors.toList());
    return BDDReachabilityGraphOptimizer.optimize(edges, ingressLocationStates, true);
  }

  public BDDLoopDetectionAnalysis(
      BDDPacket bddPacket, Stream<Edge> edges, Set<StateExpr> ingressLocationStates) {
    this(
        bddPacket,
        BDDReachabilityUtils.computeForwardEdgeTable(getLoopEdges(edges, ingressLocationStates)),
        ingressLocationStates);
  }

  /*
   * Detect infinite routing loops in the network.
   */
  public Map<IngressLocation, BDD> detectLoops() {
    Span span = GlobalTracer.get().buildSpan("BDDLoopDetectionAnalysis.detectLoops").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      /*
       * Run enough rounds to exceed the max TTL (255). It takes at most 6 iterations to go between
       * hops:
       * PreInInterface -> PostInInterface -> PostInVrf -> PreOutVrf -> PreOutEdge ->
       * PreOutEdgePostNat -> PreInInterface
       *
       * Since we don't model TTL, all packets on loops will loop forever. But most paths will stop
       * long before numRounds. What's left will be a few candidate location/headerspace pairs that
       * may be on loops. In practice this is most likely way more iterations than necessary.
       */
      int numRounds = 256 * 6;
      Map<StateExpr, BDD> reachableInNRounds = reachableInNRounds(numRounds);

      /*
       * Identify which of the candidates are actually on loops
       */
      Map<StateExpr, BDD> loopBDDs =
          reachableInNRounds.entrySet().stream()
              .filter(entry -> confirmLoop(entry.getKey(), entry.getValue()))
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      /*
       * Run backward to find the ingress locations/headerspaces that lead to loops.
       */
      backwardFixpoint(_forwardEdgeTable, loopBDDs);

      /*
       * Extract the ingress location BDDs.
       */
      return getIngressLocationBdds(
          loopBDDs, _ingressLocationStates, _bddPacket.getFactory().zero());
    } finally {
      span.finish();
    }
  }

  private Map<StateExpr, BDD> propagate(Map<StateExpr, BDD> bdds) {
    Span span = GlobalTracer.get().buildSpan("BDDLoopDetectionAnalysis.propagate").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      BDD zero = _bddPacket.getFactory().zero();
      Map<StateExpr, BDD> newReachableInNRounds = new HashMap<>();
      bdds.forEach(
          (source, sourceBdd) ->
              _forwardEdgeTable
                  .row(source)
                  .forEach(
                      (target, transition) -> {
                        BDD targetBdd = newReachableInNRounds.getOrDefault(target, zero);
                        BDD newTragetBdd = targetBdd.or(transition.transitForward(sourceBdd));
                        if (!newTragetBdd.isZero()) {
                          newReachableInNRounds.put(target, newTragetBdd);
                        }
                      }));
      return newReachableInNRounds;
    } finally {
      span.finish();
    }
  }

  /**
   * Run BFS from one step past the initial state. Each round, check if the initial state has been
   * reached yet.
   */
  private boolean confirmLoop(StateExpr stateExpr, BDD bdd) {
    Span span = GlobalTracer.get().buildSpan("BDDLoopDetectionAnalysis.confirmLoop").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      Map<StateExpr, BDD> reachable = propagate(ImmutableMap.of(stateExpr, bdd));
      Set<StateExpr> dirty = new HashSet<>(reachable.keySet());

      BDD zero = _bddPacket.getFactory().zero();
      while (!dirty.isEmpty()) {
        Set<StateExpr> newDirty = new HashSet<>();

        dirty.forEach(
            preState -> {
              Map<StateExpr, Transition> preStateOutEdges = _forwardEdgeTable.row(preState);
              if (preStateOutEdges == null) {
                // preState has no out-edges
                return;
              }

              BDD preStateBDD = reachable.get(preState);
              preStateOutEdges.forEach(
                  (postState, transition) -> {
                    BDD result = transition.transitForward(preStateBDD);
                    if (result.isZero()) {
                      return;
                    }

                    // update postState BDD reverse-reachable from leaf
                    BDD oldReach = reachable.getOrDefault(postState, zero);
                    BDD newReach = oldReach == null ? result : oldReach.or(result);
                    if (oldReach == null || !oldReach.equals(newReach)) {
                      reachable.put(postState, newReach);
                      newDirty.add(postState);
                    }
                  });
            });

        dirty = newDirty;
        if (dirty.contains(stateExpr)) {
          if (reachable.get(stateExpr).andSat(bdd)) {
            return true;
          }
        }
      }
      return false;
    } finally {
      span.finish();
    }
  }

  private Map<StateExpr, BDD> reachableInNRounds(int numRounds) {
    Span span = GlobalTracer.get().buildSpan("BDDLoopDetectionAnalysis.reachableInNRounds").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      BDD one = _bddPacket.getFactory().one();

      // All ingress locations are reachable in 0 rounds.
      Map<StateExpr, BDD> reachableInNRounds =
          toImmutableMap(_ingressLocationStates, Function.identity(), k -> one);

      for (int round = 0; !reachableInNRounds.isEmpty() && round < numRounds; round++) {
        reachableInNRounds = propagate(reachableInNRounds);
      }
      return reachableInNRounds;
    } finally {
      span.finish();
    }
  }
}
