package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
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
    assert opt.checkInvariants();
    opt.optimize();
    assert opt.checkInvariants();
    return opt._outEdges.values();
  }

  private final Multimap<StateExpr, Edge> _outEdges;
  private final Multimap<StateExpr, Edge> _inEdges;
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
    _outEdges = computeEdgeMap(edges, Edge::getPreState);
    _inEdges = computeEdgeMap(edges, Edge::getPostState);
    _statesToKeep = statesToKeep;
    _keepSelfLoops = keepSelfLoops;
    _origEdges = _outEdges.size();
  }

  private static Multimap<StateExpr, Edge> computeEdgeMap(
      Collection<Edge> edges, Function<Edge, StateExpr> getState) {
    Multimap<StateExpr, Edge> edgeMap = HashMultimap.create();
    edges.forEach(edge -> edgeMap.put(getState.apply(edge), edge));
    return edgeMap;
  }

  private boolean checkInvariants() {
    assert ImmutableSet.copyOf(_outEdges.values()).equals(ImmutableSet.copyOf(_inEdges.values()));
    return true;
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
            _outEdges.size()));
  }

  private void optimize() {
    Queue<StateExpr> candidates =
        new ArrayDeque<>(Sets.union(_outEdges.keySet(), _inEdges.keySet()));
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

  private void removeSelfLoops(StateExpr candidate) {
    checkState(!_keepSelfLoops);
    for (Edge edge : _inEdges.get(candidate)) {
      if (isRemovableSelfLoop(edge)) {
        // self-loop that adds nothing
        _selfLoops++;
        removeEdge(edge);
      }
    }
  }

  private Stream<StateExpr> tryToRemove(StateExpr candidate) {
    assert !_statesToKeep.contains(candidate);
    Collection<Edge> inEdges = _inEdges.get(candidate);
    if (inEdges.isEmpty()) {
      // root node. prune
      _rootsPruned++;
      return _outEdges.removeAll(candidate).stream()
          .peek(this::removeInEdge)
          .map(Edge::getPostState);
    }
    Collection<Edge> outEdges = _outEdges.get(candidate);
    if (outEdges.isEmpty()) {
      // leaf node. prune
      _leavesPruned++;
      return _inEdges.removeAll(candidate).stream()
          .peek(this::removeOutEdge)
          .map(Edge::getPreState);
    }
    if (inEdges.size() == 1 && outEdges.size() == 1) {
      // try to remove candidate and compose its edges
      Edge inEdge = Iterables.getOnlyElement(inEdges);
      Edge outEdge = Iterables.getOnlyElement(outEdges);
      assert candidate.equals(inEdge.getPostState());
      assert candidate.equals(outEdge.getPreState());

      if (inEdge.equals(outEdge)) {
        // self-loop. handled elsewhere
        return Stream.of();
      }

      @Nullable Transition merged = mergeComposed(inEdge.getTransition(), outEdge.getTransition());
      if (merged == null) {
        // do nothing. In some cases it may still be best to merge, but punting for now
        return Stream.of();
      }

      removeEdge(inEdge);
      removeEdge(outEdge);

      StateExpr prev = inEdge.getPreState();
      StateExpr next = outEdge.getPostState();

      Edge edge = new Edge(prev, next, merged);
      if (edge.getTransition() == ZERO) {
        _splicedAndDropped++;
        return Stream.of(prev, next);
      } else {
        _nodesSpliced++;
        addEdge(edge);

        // didn't change the degree or neighbors
        return Stream.of();
      }
    }

    // do nothing
    return Stream.of();
  }

  private void addEdge(Edge edge) {
    _inEdges.put(edge.getPostState(), edge);
    _outEdges.put(edge.getPreState(), edge);
  }

  private void removeEdge(Edge edge) {
    checkState(_inEdges.remove(edge.getPostState(), edge));
    checkState(_outEdges.remove(edge.getPreState(), edge));
  }

  private void removeInEdge(Edge edge) {
    checkState(_inEdges.remove(edge.getPostState(), edge));
  }

  private void removeOutEdge(Edge edge) {
    checkState(_outEdges.remove(edge.getPreState(), edge));
  }

  private boolean isRemovableSelfLoop(Edge edge) {
    checkState(!_keepSelfLoops);
    if (!edge.getPreState().equals(edge.getPostState())) {
      return false;
    }
    Transition t = edge.getTransition();
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
