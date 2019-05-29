package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
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

  // source -> target -> transition function
  private final Table<StateExpr, StateExpr, Transition> _edges;
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
    _edges = computeEdgeTable(edges);
    _statesToKeep = statesToKeep;
    _keepSelfLoops = keepSelfLoops;
    _origEdges = _edges.size();
  }

  private static Table<StateExpr, StateExpr, Transition> computeEdgeTable(Collection<Edge> edges) {
    Table<StateExpr, StateExpr, Transition> tbl = HashBasedTable.create();
    edges.forEach(edge -> tbl.put(edge.getPreState(), edge.getPostState(), edge.getTransition()));
    return tbl;
  }

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

  private void optimize() {
    Queue<StateExpr> candidates =
        new ArrayDeque<>(Sets.union(_edges.rowKeySet(), _edges.columnKeySet()));
    Set<StateExpr> candidateSet = new HashSet<>(candidates);
    while (!candidates.isEmpty()) {
      StateExpr candidate = candidates.poll();
      candidateSet.remove(candidate);
      if (!_keepSelfLoops) {
        removeSelfLoops(candidate);
      }
      if (!_statesToKeep.contains(candidate)) {
        tryToRemove(candidate).filter(candidateSet::add).forEach(candidates::add);
      }
    }
  }

  private void removeSelfLoops(StateExpr preState) {
    checkState(!_keepSelfLoops);
    for (Entry<StateExpr, Transition> outEdge : _edges.row(preState).entrySet()) {
      StateExpr postState = outEdge.getKey();
      Transition transition = outEdge.getValue();
      if (isRemovableSelfLoop(preState, postState, transition)) {
        // self-loop that adds nothing
        _selfLoops++;
        removeEdge(preState, postState, transition);
      }
    }
  }

  private Stream<StateExpr> tryToRemove(StateExpr candidate) {
    assert !_statesToKeep.contains(candidate);
    Map<StateExpr, Transition> inEdges = _edges.column(candidate);
    if (inEdges.isEmpty()) {
      // root node. prune
      _rootsPruned++;
      return _edges.row(candidate).entrySet().stream()
          .peek(entry -> this.removeEdge(candidate, entry.getKey(), entry.getValue()))
          .map(Entry::getKey);
    }
    Map<StateExpr, Transition> outEdges = _edges.row(candidate);
    if (outEdges.isEmpty()) {
      // leaf node. prune
      _leavesPruned++;
      return _edges.column(candidate).entrySet().stream()
          .peek(entry -> this.removeEdge(entry.getKey(), candidate, entry.getValue()))
          .map(Entry::getKey);
    }
    if (inEdges.size() == 1 && outEdges.size() == 1) {
      // try to remove candidate and compose its edges
      Entry<StateExpr, Transition> inEdge = Iterables.getOnlyElement(inEdges.entrySet());
      Entry<StateExpr, Transition> outEdge = Iterables.getOnlyElement(outEdges.entrySet());

      if (inEdge.getKey().equals(candidate)) {
        // self-loop. handled elsewhere
        assert inEdge.equals(outEdge);
        return Stream.of();
      }

      @Nullable Transition composed = mergeComposed(inEdge.getValue(), outEdge.getValue());
      if (composed == null) {
        // do nothing. In some cases it may still be best to merge, but punting for now
        return Stream.of();
      }

      removeEdge(inEdge.getKey(), candidate, inEdge.getValue());
      removeEdge(candidate, outEdge.getKey(), outEdge.getValue());

      StateExpr prev = inEdge.getKey();
      StateExpr next = outEdge.getKey();

      if (composed == ZERO) {
        _splicedAndDropped++;
        return Stream.of(prev, next);
      } else {
        Transition oldTransition = _edges.put(prev, next, composed);

        if (oldTransition == null) {
          // prev and next didn't have an edge between them, so this didn't change their degree
          _nodesSpliced++;
          return Stream.of();
        } else {
          // prev and next did have an edge, so we need to merge it.
          Transition merged = compose(composed, oldTransition);
          if (merged == ZERO) {
            _edges.remove(prev, next);
          } else {
            _edges.put(prev, next, merged);
          }
          return Stream.of(prev, next);
        }
      }
    }

    // do nothing
    return Stream.of();
  }

  private void removeEdge(StateExpr preState, StateExpr postState, Transition transition) {
    checkState(_edges.remove(preState, postState) == transition);
  }

  private boolean isRemovableSelfLoop(StateExpr preState, StateExpr postState, Transition t) {
    checkState(!_keepSelfLoops);
    if (!preState.equals(postState)) {
      return false;
    }
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
