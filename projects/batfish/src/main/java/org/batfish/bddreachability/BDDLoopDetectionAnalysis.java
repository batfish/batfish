package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDReachabilityUtils.backwardFixpoint;
import static org.batfish.bddreachability.BDDReachabilityUtils.toIngressLocation;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
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
import net.sf.javabdd.BDDFactory;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.StateExpr;

/** A reachability analysis that detects loops. */
public class BDDLoopDetectionAnalysis {
  private final BDDPacket _bddPacket;
  private final Table<StateExpr, StateExpr, Transition> _forwardEdgeTable;
  private final Map<StateExpr, BDD> _ingressLocationStateBDDs;

  /**
   * Constructs a {@link BDDLoopDetectionAnalysis} that searches for loops reachable from the sets
   * of flows per ingress location state specified in {@code ingressLocationStateBDDs}.
   */
  public BDDLoopDetectionAnalysis(
      BDDPacket bddPacket,
      Table<StateExpr, StateExpr, Transition> forwardEdgeTable,
      Map<StateExpr, BDD> ingressLocationStateBDDs) {
    _bddPacket = bddPacket;
    _ingressLocationStateBDDs = ImmutableMap.copyOf(ingressLocationStateBDDs);
    _forwardEdgeTable = ImmutableTable.copyOf(forwardEdgeTable);
  }

  /**
   * Constructs a {@link BDDLoopDetectionAnalysis} that searches for loops reachable from the
   * specified {@code ingressLocationStates}.
   */
  public BDDLoopDetectionAnalysis(
      BDDPacket bddPacket, Stream<Edge> edges, Set<StateExpr> ingressLocationStates) {
    this(
        bddPacket,
        BDDReachabilityUtils.computeForwardEdgeTable(getLoopEdges(edges, ingressLocationStates)),
        buildIngressLocationStateBDDs(ingressLocationStates, bddPacket.getFactory().one()));
  }

  private static Collection<Edge> getLoopEdges(
      Stream<Edge> inEdges, Set<StateExpr> ingressLocationStates) {
    List<Edge> edges = inEdges.collect(Collectors.toList());
    return BDDReachabilityGraphOptimizer.optimize(edges, ingressLocationStates, true);
  }

  /**
   * Build a mapping from each input ingress location {@link StateExpr} to the input {@link BDD}.
   */
  private static Map<StateExpr, BDD> buildIngressLocationStateBDDs(
      Set<StateExpr> ingressLocationStates, BDD bdd) {
    return ingressLocationStates.stream()
        .collect(ImmutableMap.toImmutableMap(Function.identity(), k -> bdd));
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
    BDDFactory factory = _bddPacket.getFactory();
    long before = factory.numOutstandingBDDs();

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
    Map<StateExpr, BDD> loopBDDs = new HashMap<>();
    reachableInNRounds.forEach(
        (state, bdd) -> {
          if (confirmLoop(state, bdd)) {
            loopBDDs.put(state, bdd);
          } else {
            bdd.free();
          }
        });

    /*
     * Run backward to find the ingress locations/headerspaces that lead to loops. The fixpoint
     * replaces the map's values without freeing them.
     */
    List<BDD> candidates = ImmutableList.copyOf(loopBDDs.values());
    backwardFixpoint(_forwardEdgeTable, loopBDDs);
    candidates.forEach(BDD::free);

    /*
     * Extract the ingress location BDDs.
     */
    Map<StateExpr, BDD> result =
        getIngressStateExprBdds(loopBDDs, _ingressLocationStateBDDs, factory);
    loopBDDs.values().forEach(BDD::free);
    assert factory.numOutstandingBDDs() == before + result.size()
        : "detectLoopsStateExpr leaked BDDs";
    return result;
  }

  /**
   * Returns the flows reaching each state in one step from {@code bdds}. The caller owns the
   * returned BDDs; {@code bdds} is not modified.
   */
  private Map<StateExpr, BDD> propagate(Map<StateExpr, BDD> bdds) {
    BDDFactory factory = _bddPacket.getFactory();
    Map<StateExpr, BDD> newReachableInNRounds = new HashMap<>();
    bdds.forEach(
        (source, sourceBdd) ->
            _forwardEdgeTable
                .row(source)
                .forEach(
                    (target, transition) -> {
                      long priorBDDs = factory.numOutstandingBDDs();
                      BDD transited = transition.transitForward(sourceBdd);
                      assert factory.numOutstandingBDDs() - priorBDDs == 1
                          : "Leak of size "
                              + (factory.numOutstandingBDDs() - priorBDDs - 1)
                              + " transiting "
                              + source
                              + " -> "
                              + target;
                      if (transited.isZero()) {
                        transited.free();
                        return;
                      }
                      BDD targetBdd = newReachableInNRounds.get(target);
                      if (targetBdd == null) {
                        newReachableInNRounds.put(target, transited);
                      } else {
                        targetBdd.orWith(transited);
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
    BDDFactory factory = _bddPacket.getFactory();
    long before = factory.numOutstandingBDDs();
    Map<StateExpr, BDD> reachable = propagate(ImmutableMap.of(stateExpr, bdd));
    try {
      Set<StateExpr> dirty = new HashSet<>(reachable.keySet());
      while (!dirty.isEmpty()) {
        Set<StateExpr> newDirty = new HashSet<>();

        for (StateExpr preState : dirty) {
          BDD preStateBDD = reachable.get(preState);
          // Replaced BDDs are freed after the loop: one may be preStateBDD (a self-loop).
          List<BDD> replaced = new ArrayList<>();
          for (Entry<StateExpr, Transition> entry : _forwardEdgeTable.row(preState).entrySet()) {
            StateExpr postState = entry.getKey();
            Transition transition = entry.getValue();
            long priorBDDs = factory.numOutstandingBDDs();
            BDD result = transition.transitForward(preStateBDD);
            assert factory.numOutstandingBDDs() - priorBDDs == 1
                : "Leak of size "
                    + (factory.numOutstandingBDDs() - priorBDDs - 1)
                    + " transiting "
                    + preState
                    + " -> "
                    + postState;
            if (result.isZero()) {
              result.free();
              continue;
            }

            if (postState.equals(stateExpr) && result.andSat(bdd)) {
              result.free();
              replaced.forEach(BDD::free);
              return true;
            }

            // update postState BDD reverse-reachable from leaf
            BDD oldReach = reachable.get(postState);
            if (oldReach == null) {
              reachable.put(postState, result);
              newDirty.add(postState);
              continue;
            }
            BDD newReach = oldReach.or(result);
            result.free();
            if (oldReach.equals(newReach)) {
              newReach.free();
            } else {
              reachable.put(postState, newReach);
              replaced.add(oldReach);
              newDirty.add(postState);
            }
          }
          replaced.forEach(BDD::free);
        }

        dirty = newDirty;
      }
      return false;
    } finally {
      reachable.values().forEach(BDD::free);
      assert factory.numOutstandingBDDs() == before : "confirmLoop leaked BDDs";
    }
  }

  /** Returns the flows reachable in exactly {@code numRounds} steps; the caller owns them. */
  private Map<StateExpr, BDD> reachableInNRounds(int numRounds) {
    // All ingress locations are reachable in 0 rounds. That map is ours, not the caller's.
    Map<StateExpr, BDD> reachableInNRounds = _ingressLocationStateBDDs;

    for (int round = 0; !reachableInNRounds.isEmpty() && round < numRounds; round++) {
      Map<StateExpr, BDD> next = propagate(reachableInNRounds);
      if (reachableInNRounds != _ingressLocationStateBDDs) {
        reachableInNRounds.values().forEach(BDD::free);
      }
      reachableInNRounds = next;
    }
    if (reachableInNRounds == _ingressLocationStateBDDs) {
      return reachableInNRounds.entrySet().stream()
          .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().id()));
    }
    return reachableInNRounds;
  }

  private static Map<StateExpr, BDD> getIngressStateExprBdds(
      Map<StateExpr, BDD> stateReachableBdds,
      Map<StateExpr, BDD> ingressLocationStateBDDs,
      BDDFactory factory) {
    BDD zero = factory.zero();
    Map<StateExpr, BDD> result =
        toImmutableMap(
            ingressLocationStateBDDs,
            Map.Entry::getKey,
            entry -> stateReachableBdds.getOrDefault(entry.getKey(), zero).and(entry.getValue()));
    zero.free();
    return result;
  }
}
