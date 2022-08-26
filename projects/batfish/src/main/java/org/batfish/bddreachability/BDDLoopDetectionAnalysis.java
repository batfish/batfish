package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDReachabilityUtils.backwardFixpoint;
import static org.batfish.bddreachability.BDDReachabilityUtils.getIngressStateExprBdds;
import static org.batfish.bddreachability.BDDReachabilityUtils.toIngressLocation;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
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

  public Map<IngressLocation, BDD> detectLoops() {
    return detectLoopsStateExpr().entrySet().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                entry -> toIngressLocation(entry.getKey()), Entry::getValue));
  }

  /*
   * Detect infinite routing loops in the network.
   */
  public Map<StateExpr, BDD> detectLoopsStateExpr() {
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
    return getIngressStateExprBdds(
        loopBDDs, _ingressLocationStates, _bddPacket.getFactory().zero());
  }

  private Map<StateExpr, BDD> propagate(Map<StateExpr, BDD> bdds) {
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
  }

  /**
   * Run BFS from one step past the initial state. Each round, check if the initial state has been
   * reached yet.
   */
  @VisibleForTesting
  boolean confirmLoop(StateExpr stateExpr, BDD bdd) {
    Map<StateExpr, BDD> reachable = propagate(ImmutableMap.of(stateExpr, bdd));
    Set<StateExpr> dirty = new HashSet<>(reachable.keySet());

    BDD zero = _bddPacket.getFactory().zero();
    while (!dirty.isEmpty()) {
      Set<StateExpr> newDirty = new HashSet<>();

      for (StateExpr preState : dirty) {
        BDD preStateBDD = reachable.get(preState);
        for (Entry<StateExpr, Transition> entry : _forwardEdgeTable.row(preState).entrySet()) {
          StateExpr postState = entry.getKey();
          Transition transition = entry.getValue();
          BDD result = transition.transitForward(preStateBDD);
          if (result.isZero()) {
            continue;
          }

          if (postState.equals(stateExpr) && result.andSat(bdd)) {
            return true;
          }

          // update postState BDD reverse-reachable from leaf
          BDD oldReach = reachable.getOrDefault(postState, zero);
          BDD newReach = oldReach == null ? result : oldReach.or(result);
          if (oldReach == null || !oldReach.equals(newReach)) {
            reachable.put(postState, newReach);
            newDirty.add(postState);
          }
        }
      }

      dirty = newDirty;
    }
    return false;
  }

  private Map<StateExpr, BDD> reachableInNRounds(int numRounds) {
    BDD one = _bddPacket.getFactory().one();

    // All ingress locations are reachable in 0 rounds.
    Map<StateExpr, BDD> reachableInNRounds =
        toImmutableMap(_ingressLocationStates, Function.identity(), k -> one);

    for (int round = 0; !reachableInNRounds.isEmpty() && round < numRounds; round++) {
      reachableInNRounds = propagate(reachableInNRounds);
    }
    return reachableInNRounds;
  }
}
