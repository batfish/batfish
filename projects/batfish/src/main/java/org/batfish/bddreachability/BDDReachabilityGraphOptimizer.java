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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
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
        .collect(ImmutableList.toImmutableList());
  }

  // These three maps need to be kept in sync.
  // source -> target -> transition function
  private final Table<StateExpr, StateExpr, Transition> _edges;
  private final Multimap<StateExpr, StateExpr> _preStates;
  private final Multimap<StateExpr, StateExpr> _postStates;

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
    _preStates = HashMultimap.create();
    _postStates = HashMultimap.create();
    for (Edge edge : edges) {
      _edges.put(edge.getPreState(), edge.getPostState(), edge.getTransition());
      _preStates.put(edge.getPostState(), edge.getPreState());
      _postStates.put(edge.getPreState(), edge.getPostState());
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
    // A big first pass to delete roots and leaves, since that operation does not require expensive
    // transition merges.
    _rootsPruned = pruneAllRoots(_postStates, _preStates, _edges::remove, _statesToKeep);
    _leavesPruned =
        pruneAllRoots(_preStates, _postStates, (a, b) -> _edges.remove(b, a), _statesToKeep);

    Set<StateExpr> candidateSet = new HashSet<>();
    candidateSet.addAll(_preStates.keySet());
    candidateSet.addAll(_postStates.keySet());
    Queue<StateExpr> candidateQueue = new ArrayDeque<>(candidateSet);

    while (!candidateQueue.isEmpty()) {
      // Invariant: candidateSet and candidateQueue have the same elements, queued exactly once.
      assert candidateQueue.size() == candidateSet.size();

      StateExpr candidate = candidateQueue.remove();
      candidateSet.remove(candidate);

      if (!_keepSelfLoops) {
        // Even if we want to keep candidate, it's always safe to delete self loops.
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
    assert _preStates.containsEntry(preState, preState);
    assert _postStates.containsEntry(preState, preState);

    _selfLoops++;
    _edges.remove(preState, preState);
    _preStates.remove(preState, preState);
    _postStates.remove(preState, preState);
  }

  /**
   * Prunes a root (as defined by the parameters) and returns a collection of valid states that were
   * successors of the root.
   *
   * <p>This abstracts pruning for roots or leaves based on the inputs, which may be flipped.
   */
  private static Collection<StateExpr> pruneRoot(
      StateExpr root,
      Multimap<StateExpr, StateExpr> postStates,
      Multimap<StateExpr, StateExpr> preStates,
      BiConsumer<StateExpr, StateExpr> removeEdge,
      Set<StateExpr> statesToKeep) {
    assert !statesToKeep.contains(root);
    assert !preStates.containsKey(root);

    Collection<StateExpr> successors = postStates.removeAll(root);
    for (StateExpr successor : successors) {
      removeEdge.accept(root, successor);
      preStates.remove(successor, root);
    }
    // We deleted an edge from each successor. If this was the only edge attached to it, that state
    // is no longer in the table and is now invalid.
    return successors.stream()
        .filter(s -> postStates.containsKey(s) || preStates.containsKey(s))
        .collect(Collectors.toList());
  }

  /**
   * Iteratively prunes all roots (as defined by the parameters), returning the number of pruned
   * roots.
   *
   * <p>This abstracts pruning all roots or leaves, based on the direction of input parameters.
   */
  private static int pruneAllRoots(
      Multimap<StateExpr, StateExpr> postStates,
      Multimap<StateExpr, StateExpr> preStates,
      BiConsumer<StateExpr, StateExpr> removeEdge,
      Set<StateExpr> statesToKeep) {
    int count = 0;
    Collection<StateExpr> roots =
        postStates.keySet().stream()
            .filter(s -> !statesToKeep.contains(s) && !preStates.containsKey(s))
            .collect(Collectors.toList());
    while (!roots.isEmpty()) {
      count += roots.size();
      roots =
          roots.stream()
              .map(s -> pruneRoot(s, postStates, preStates, removeEdge, statesToKeep))
              .flatMap(Collection::stream)
              .filter(s -> !statesToKeep.contains(s) && !preStates.containsKey(s))
              .collect(Collectors.toSet());
    }
    return count;
  }

  /**
   * Try to remove the candidate state, returning any neighboring states whose edges were affected.
   */
  private Collection<StateExpr> tryToRemove(StateExpr candidate) {
    assert !_statesToKeep.contains(candidate);

    if (!_preStates.containsKey(candidate)) {
      ++_rootsPruned;
      return pruneRoot(candidate, _postStates, _preStates, _edges::remove, _statesToKeep);
    }

    if (!_postStates.containsKey(candidate)) {
      ++_leavesPruned;
      return pruneRoot(
          candidate, _preStates, _postStates, (a, b) -> _edges.remove(b, a), _statesToKeep);
    }

    Collection<StateExpr> inStates = _preStates.get(candidate);
    Collection<StateExpr> outStates = _postStates.get(candidate);
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
        // There wasn't already an edge from prev to next, Mark as a splice and update the
        // bookkeeping.
        _nodesSpliced++;
        _postStates.put(prev, next);
        _preStates.put(next, prev);
      } else {
        // There already was an edge from prev to next. Merge them, but don't update bookkeeping.
        _edges.put(prev, next, or(composed, oldTransition));
      }
    }

    // Remove old edges last to avoid deleting and then recreating the collections internal to the
    // maps; could happen during splicing.
    _edges.remove(prev, candidate);
    _preStates.remove(candidate, prev);
    _postStates.remove(prev, candidate);

    _edges.remove(candidate, next);
    _postStates.remove(candidate, next);
    _preStates.remove(next, candidate);

    return ImmutableList.of(prev, next);
  }

  /**
   * Returns true iff the given transition is safe to remove, namely that the set of flows after
   * transitioning the edge can never contain more flows than before transitioning.
   *
   * <p>Mathematically, this condition is {@code forall x, x.or(t.transitForward(x)) == x}.
   */
  private boolean isRemovableSelfLoop(Transition t) {
    checkState(!_keepSelfLoops);
    if (t == ZERO || t == IDENTITY) {
      return true;
    }
    if (t instanceof Constraint
        || t instanceof AddSourceConstraint
        || t instanceof AddLastHopConstraint
        || t instanceof AddNoLastHopConstraint) {
      return true;
    }
    return false;
  }
}
