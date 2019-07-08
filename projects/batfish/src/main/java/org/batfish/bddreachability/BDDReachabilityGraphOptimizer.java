package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;
import static org.batfish.bddreachability.transition.Transitions.or;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.bddreachability.transition.AddLastHopConstraint;
import org.batfish.bddreachability.transition.AddNoLastHopConstraint;
import org.batfish.bddreachability.transition.AddSourceConstraint;
import org.batfish.bddreachability.transition.Constraint;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;

/**
 * Performs topological optimizations on the reachability graph: removing nodes (composing each
 * in-edge with each out-edge) and removing edges.
 */
public class BDDReachabilityGraphOptimizer {

  /**
   * Optimize a reachability graph by removing nodes and/or edges.
   *
   * @param edges The original collection of edges that comprise the graph.
   * @param statesToKeep States that must not be removed, e.g. because they may input or output
   *     points.
   * @param keepSelfLoops When true, self-loops will not be removed. When doing loop detection,
   *     loops should be preserved, but for other analyses it may be safe to remove them.
   * @return Edges of the optimized graph.
   */
  public static Collection<Edge> optimize(
      Collection<Edge> edges, Set<StateExpr> statesToKeep, boolean keepSelfLoops) {
    BDDReachabilityGraphOptimizer opt =
        new BDDReachabilityGraphOptimizer(edges, statesToKeep, keepSelfLoops);
    opt.optimize();
    return opt._edges.cellSet().stream()
        .map(cell -> new Edge(cell.getRowKey(), cell.getColumnKey(), cell.getValue()))
        .collect(ImmutableSet.toImmutableSet());
  }

  // These three maps need to be kept in sync.
  // source -> target -> transition function
  private final Table<StateExpr, StateExpr, Transition> _edges;
  private final Multimap<StateExpr, StateExpr> _inEdges;
  private final Multimap<StateExpr, StateExpr> _outEdges;

  private final Set<StateExpr> _statesToKeep;
  private final boolean _keepSelfLoops;

  private int _origEdges = 0;
  private int _rootsPruned = 0;
  private int _leavesPruned = 0;
  private int _nodesSpliced = 0;
  private int _splicedAndDropped = 0;
  private int _selfLoops = 0;

  private BDDReachabilityGraphOptimizer(
      Collection<Edge> edges, Set<StateExpr> statesToKeep, boolean keepSelfLoops) {
    _edges = HashBasedTable.create();
    _inEdges = HashMultimap.create();
    _outEdges = HashMultimap.create();
    for (Edge edge : edges) {
      _edges.put(edge.getPreState(), edge.getPostState(), edge.getTransition());
      _inEdges.put(edge.getPostState(), edge.getPreState());
      _outEdges.put(edge.getPreState(), edge.getPostState());
    }

    _statesToKeep = ImmutableSet.copyOf(statesToKeep);
    _keepSelfLoops = keepSelfLoops;
    _origEdges = _edges.size();
  }

  @SuppressWarnings("unused")
  private void printStats() {
    System.out.println(
        String.format(
            "origEdges: %s, "
                + "roots pruned: %s, "
                + "leaves pruned: %s, "
                + "spliced: %s, "
                + "spliced and dropped: %s, "
                + "self loops removed: %s, "
                + "finalEdges: %s",
            _origEdges,
            _rootsPruned,
            _leavesPruned,
            _nodesSpliced,
            _splicedAndDropped,
            _selfLoops,
            _edges.size()));
  }

  /**
   * A fixed-point computation to optimize the graph.
   *
   * <ol>
   *   <li>Mark every state in the graph as dirty.
   *   <li>For each dirty state, try to delete it. If successful, mark all affects states
   *       (neighboring states that lost incoming or outgoing edges) as dirty.
   *   <li>Repeat until convergence.
   * </ol>
   */
  private void optimize() {
    Set<StateExpr> candidateSet = new HashSet<>();
    candidateSet.addAll(_inEdges.keySet());
    candidateSet.addAll(_outEdges.keySet());
    Queue<StateExpr> candidateQueue = new ArrayDeque<>(candidateSet);

    while (!candidateQueue.isEmpty()) {
      // Invariant: candidateSet and candidateQueue have the same elements, queued exactly once.
      assert candidateQueue.size() == candidateSet.size();

      StateExpr candidate = candidateQueue.remove();
      candidateSet.remove(candidate);

      if (!_keepSelfLoops) {
        // Even if we want to keep candidate, it's always save to delete self loops.
        removeSelfLoops(candidate);
      }
      if (_statesToKeep.contains(candidate)) {
        // We need this state expr.
        continue;
      }

      // Get all affected states, and mark all the ones that were not already candidates as dirty.
      tryToRemove(candidate).stream().filter(candidateSet::add).forEach(candidateQueue::add);
    }
  }

  private void removeSelfLoops(StateExpr preState) {
    checkState(!_keepSelfLoops);
    @Nullable Transition t = _edges.get(preState, preState);
    if (t == null || !isRemovableSelfLoop(t)) {
      // not present or not safe to remove.
      return;
    }
    assert _inEdges.containsEntry(preState, preState);
    assert _outEdges.containsEntry(preState, preState);

    _selfLoops++;
    _edges.remove(preState, preState);
    _inEdges.remove(preState, preState);
    _outEdges.remove(preState, preState);
  }

  /**
   * Try to remove the candidate state, and return any neighboring states whose degrees changed as a
   * result.
   */
  private Collection<StateExpr> tryToRemove(StateExpr candidate) {
    assert !_statesToKeep.contains(candidate);
    Collection<StateExpr> inStates = _inEdges.get(candidate);
    if (inStates.isEmpty()) {
      // root node. prune
      _rootsPruned++;
      Collection<StateExpr> affectedStates = _outEdges.removeAll(candidate);
      for (StateExpr oldNext : affectedStates) {
        _edges.remove(candidate, oldNext);
        _inEdges.remove(oldNext, candidate);
      }
      return affectedStates;
    }
    Collection<StateExpr> outStates = _outEdges.get(candidate);
    if (outStates.isEmpty()) {
      // leaf node. prune
      _leavesPruned++;
      Collection<StateExpr> affectedStates = _inEdges.removeAll(candidate);
      for (StateExpr oldPrev : affectedStates) {
        _edges.remove(oldPrev, candidate);
        _outEdges.remove(oldPrev, candidate);
      }
      return affectedStates;
    }

    if (inStates.size() > 1 || outStates.size() > 1) {
      // For now, only consider merging edges when we can merge all the way through.
      return ImmutableSet.of();
    }

    /* Try to remove candidate and compose its incoming and outgoing edges. */

    StateExpr prev = Iterables.getOnlyElement(inStates);
    StateExpr next = Iterables.getOnlyElement(outStates);
    assert _edges.contains(prev, candidate);
    assert _edges.contains(candidate, next);

    if (prev.equals(candidate)) {
      // self-loop. handled elsewhere
      assert next.equals(candidate);
      return ImmutableSet.of();
    }

    @Nullable
    Transition composed = mergeComposed(_edges.get(prev, candidate), _edges.get(candidate, next));
    if (composed == null) {
      // do nothing. In some cases it may still be best to merge, but punting for now
      return ImmutableSet.of();
    }

    if (composed == ZERO) {
      _splicedAndDropped++;
    } else {
      Transition oldTransition = _edges.put(prev, next, composed);

      if (oldTransition == null) {
        // There wasn't already an edge from prev to next, so this did not change their degree.
        _nodesSpliced++;
        _outEdges.put(prev, next);
        _inEdges.put(next, prev);
      } else {
        // there already was an edge from prev to next did have an edge, so merge with it.
        // their degrees change, so mark them dirty
        _edges.put(prev, next, or(composed, oldTransition));
      }
    }

    // Remove old edges last to avoid deleting and then recreating the collections internal to the
    // maps; could happen during splicing.
    _edges.remove(prev, candidate);
    _inEdges.remove(candidate, prev);
    _outEdges.remove(prev, candidate);

    _edges.remove(candidate, next);
    _outEdges.remove(candidate, next);
    _inEdges.remove(next, candidate);

    return ImmutableList.of(prev, next);
  }

  private void removeEdge(StateExpr preState, StateExpr postState, Transition transition) {
    checkState(_edges.remove(preState, postState) == transition);
  }

  private boolean isRemovableSelfLoop(Transition t) {
    checkState(!_keepSelfLoops);
    if (t == ZERO || t == IDENTITY) {
      return true;
    }
    if (t instanceof Constraint
        || t instanceof AddSourceConstraint
        || t instanceof AddLastHopConstraint
        || t instanceof AddNoLastHopConstraint) {
      // forall x,y. x.or(x.and(y)) == x
      return true;
    }
    return false;
  }
}
