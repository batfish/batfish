package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;

@ParametersAreNonnullByDefault
public final class BDDTracer {
  public static final class BDDHop {
    private final StateExpr _state;
    private final BDD _bdd;

    public BDDHop(StateExpr state, BDD bdd) {
      _state = state;
      _bdd = bdd;
    }

    public StateExpr getState() {
      return _state;
    }

    public BDD getBdd() {
      return _bdd;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof BDDHop)) {
        return false;
      }
      BDDHop bddHop = (BDDHop) o;
      return _state.equals(bddHop._state) && _bdd.equals(bddHop._bdd);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_state, _bdd);
    }
  }

  public enum BDDDisposition {
    LOOPED,
    TERMINATED
  }

  public static final class BDDTrace {
    private final @Nonnull List<BDDHop> _hops;
    private final @Nonnull BDDDisposition _bddDisposition;

    public BDDTrace(List<BDDHop> hops, BDDDisposition bddDisposition) {
      _hops = hops;
      _bddDisposition = bddDisposition;
    }

    public @Nonnull BDDDisposition getBddDisposition() {
      return _bddDisposition;
    }

    public @Nonnull List<BDDHop> getHops() {
      return _hops;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof BDDTrace)) {
        return false;
      }
      BDDTrace bddTrace = (BDDTrace) o;
      return _hops.equals(bddTrace._hops) && _bddDisposition == bddTrace._bddDisposition;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_hops, _bddDisposition);
    }
  }

  private final Consumer<BDDTrace> _traces;
  private final Table<StateExpr, StateExpr, Transition> _reachabilityGraph;
  private final Stack<BDDHop> _hops = new Stack<>();

  public BDDTracer(
      Table<StateExpr, StateExpr, Transition> reachabilityGraph, Consumer<BDDTrace> traces) {
    _traces = traces;
    _reachabilityGraph = reachabilityGraph;
  }

  private void buildTrace(BDDDisposition bddDisposition) {
    _traces.accept(new BDDTrace(ImmutableList.copyOf(_hops), bddDisposition));
  }

  private void buildTrace(BDDHop bddHop, BDDDisposition bddDisposition) {
    BDDHop lastHop = _hops.peek();
    if (lastHop.equals(bddHop)) {
      buildTrace(bddDisposition);
    } else {
      _hops.push(bddHop);
      buildTrace(bddDisposition);
      _hops.pop();
    }
  }

  public static List<BDDTrace> getTraces(
      Table<StateExpr, StateExpr, Transition> reachabilityGraph,
      StateExpr initialState,
      BDD initialBdd) {
    ImmutableList.Builder<BDDTrace> traces = ImmutableList.builder();
    new BDDTracer(reachabilityGraph, traces::add).generateTraces(initialState, initialBdd);
    return traces.build();
  }

  private BDD stopAt(StateExpr state, BDD preStateBdd) {
    Map<StateExpr, Transition> outEdges = _reachabilityGraph.rowMap().get(state);
    if (outEdges == null) {
      // nowhere to go
      return preStateBdd;
    }
    BDD one = preStateBdd.getFactory().one();
    BDD goSomewhere =
        preStateBdd
            .getFactory()
            .orAll(
                outEdges.values().stream()
                    .map(
                        transition ->
                            // find the subset of preStateBdd that can cross the transition
                            preStateBdd.and(transition.transitBackward(one)))
                    .collect(ImmutableList.toImmutableList()));
    return preStateBdd.diff(goSomewhere);
  }

  private void generateTraces(StateExpr state, BDD bdd) {
    checkArgument(!bdd.isZero());
    BDD stopHereBdd = stopAt(state, bdd);
    _hops.push(new BDDHop(state, bdd));
    try {
      if (!stopHereBdd.isZero()) {
        buildTrace(new BDDHop(state, stopHereBdd), BDDDisposition.TERMINATED);
      }
      if (stopHereBdd.equals(bdd)) {
        return;
      }

      Map<StateExpr, Transition> outEdges = _reachabilityGraph.rowMap().get(state);
      outEdges.forEach(
          (postState, transition) -> {
            BDD postStateBdd = transition.transitForward(bdd);
            if (!postStateBdd.isZero()) {
              detectLoopsAndGenerateTraces(postState, postStateBdd);
            }
          });
    } finally {
      _hops.pop();
    }
  }

  private void detectLoopsAndGenerateTraces(StateExpr state, BDD bdd) {
    List<BDD> loopBdds =
        _hops.stream()
            .map(
                prevHop -> {
                  if (!prevHop.getState().equals(state)) {
                    return null;
                  }
                  BDD loopBdd = prevHop.getBdd().and(bdd);
                  if (loopBdd.isZero()) {
                    return null;
                  }
                  return loopBdd;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    loopBdds.forEach(loopBdd -> buildTrace(new BDDHop(state, loopBdd), BDDDisposition.LOOPED));

    BDD loopBdd = bdd.getFactory().orAll(loopBdds);
    BDD nonLoopBdd = bdd.diff(loopBdd);
    if (nonLoopBdd.isZero()) {
      return;
    }
    generateTraces(state, nonLoopBdd);
  }
}
