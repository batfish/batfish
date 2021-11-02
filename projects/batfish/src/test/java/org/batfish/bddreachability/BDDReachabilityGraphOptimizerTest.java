package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDReachabilityGraphOptimizer.optimize;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.symbolic.state.StateExpr;
import org.batfish.symbolic.state.StateExprVisitor;
import org.junit.Test;

public class BDDReachabilityGraphOptimizerTest {
  private static final class DummyState implements StateExpr {
    private final String _name;

    private DummyState(String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DummyState)) {
        return false;
      }
      DummyState that = (DummyState) o;
      return _name.equals(that._name);
    }

    @Override
    public int hashCode() {
      return _name.hashCode();
    }

    @Override
    public <R> R accept(StateExprVisitor<R> visitor) {
      throw new UnsupportedOperationException();
    }
  }

  private static final class DummyTransition implements Transition {
    @Override
    public BDD transitForward(BDD bdd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public BDD transitBackward(BDD bdd) {
      throw new UnsupportedOperationException();
    }
  }

  // some transition that we know can't be merged
  private static final DummyTransition DUMMY = new DummyTransition();

  private final BDDFactory _bddFactory = JFactory.init(100, 100);

  public BDDReachabilityGraphOptimizerTest() {
    _bddFactory.setVarNum(10);
  }

  private BDD bdd(int n) {
    return _bddFactory.ithVar(n);
  }

  private StateExpr state(int n) {
    return new DummyState(String.format("STATE%d", n));
  }

  private Transition constraint(int n) {
    return Transitions.constraint(bdd(n));
  }

  private Transition constraint(BDD bdd) {
    return Transitions.constraint(bdd);
  }

  @Test
  public void testStatesToKeep() {
    Edge edge = new Edge(state(1), state(2), IDENTITY);
    assertThat(
        optimize(ImmutableSet.of(edge), ImmutableSet.of(state(1), state(2)), false),
        contains(edge));
  }

  @Test
  public void testPruneRoot() {
    Edge e1 = new Edge(state(1), state(2), IDENTITY);
    Edge e2 = new Edge(state(2), state(3), IDENTITY);
    assertThat(
        optimize(ImmutableSet.of(e1, e2), ImmutableSet.of(state(2), state(3)), false),
        contains(e2));
  }

  @Test
  public void testPruneLeaf() {
    Edge e1 = new Edge(state(1), state(2), IDENTITY);
    Edge e2 = new Edge(state(2), state(3), IDENTITY);
    assertThat(
        optimize(ImmutableSet.of(e1, e2), ImmutableSet.of(state(1), state(2)), false),
        contains(e1));
  }

  @Test
  public void testPruneLeaf2() {
    // going to prune both STATE2 and STATE3
    assertThat(
        optimize(
            ImmutableSet.of(
                new Edge(state(1), state(2), IDENTITY), new Edge(state(2), state(3), IDENTITY)),
            ImmutableSet.of(state(1)),
            false),
        empty());
  }

  @Test
  public void testSplice() {
    Edge edge1 = new Edge(state(1), state(2), constraint(0));
    Edge edge2 = new Edge(state(2), state(3), constraint(1));
    Edge merged = new Edge(state(1), state(3), constraint(bdd(0).and(bdd(1))));
    assertThat(
        optimize(ImmutableSet.of(edge1, edge2), ImmutableSet.of(state(1), state(3)), false),
        contains(merged));
  }

  @Test
  public void testUnmergeable() {
    // would splice, but the transitions don't merge
    Edge edge1 = new Edge(state(1), state(2), constraint(0));
    Edge edge2 = new Edge(state(2), state(3), DUMMY);
    assertThat(
        optimize(ImmutableSet.of(edge1, edge2), ImmutableSet.of(state(1), state(3)), false),
        containsInAnyOrder(edge1, edge2));
  }

  @Test
  public void testSpliceAndDrop() {
    Edge edge1 = new Edge(state(1), state(2), constraint(bdd(0)));
    Edge edge2 = new Edge(state(2), state(3), constraint(bdd(0).not()));
    assertThat(
        optimize(ImmutableSet.of(edge1, edge2), ImmutableSet.of(state(1), state(3)), false),
        empty());
  }

  @Test
  public void testDropSelfLoopIdentity() {
    assertThat(
        optimize(
            ImmutableSet.of(new Edge(state(1), state(1), IDENTITY)),
            ImmutableSet.of(state(1)),
            false),
        empty());
  }

  @Test
  public void testDropSelfLoopConstraint() {
    assertThat(
        optimize(
            ImmutableSet.of(new Edge(state(1), state(1), constraint(1))),
            ImmutableSet.of(state(1)),
            false),
        empty());
  }

  @Test
  public void testKeepSelfLoop_Edge() {
    // keep a self-loop when not sure the edge is safe to remove
    Edge edge = new Edge(state(1), state(1), DUMMY);
    assertThat(optimize(ImmutableSet.of(edge), ImmutableSet.of(state(1)), false), contains(edge));
  }

  @Test
  public void testKeepSelfLoop_keepSelfLoops() {
    // keep a self-loop for loop detection
    Edge edge = new Edge(state(1), state(1), IDENTITY);
    assertThat(optimize(ImmutableSet.of(edge), ImmutableSet.of(state(1)), true), contains(edge));
  }

  @Test
  public void testCycle() {
    // cycles can be completely removed when keepLoops is false
    Edge edge1 = new Edge(state(1), state(2), IDENTITY);
    Edge edge2 = new Edge(state(2), state(3), IDENTITY);
    Edge edge3 = new Edge(state(3), state(1), IDENTITY);
    assertThat(optimize(ImmutableSet.of(edge1, edge2, edge3), ImmutableSet.of(), false), empty());
  }

  @Test
  public void testCycle_keepSelfLoops() {
    // cycles can be optimized to a single node with a self-loop when keepLoops is true
    Edge edge1 = new Edge(state(1), state(2), IDENTITY);
    Edge edge2 = new Edge(state(2), state(3), IDENTITY);
    Edge edge3 = new Edge(state(3), state(1), IDENTITY);
    Collection<Edge> optimize =
        optimize(ImmutableSet.of(edge1, edge2, edge3), ImmutableSet.of(), true);
    assertThat(
        optimize,
        anyOf(
            contains(new Edge(state(1), state(1), IDENTITY)),
            contains(new Edge(state(2), state(2), IDENTITY)),
            contains(new Edge(state(3), state(3), IDENTITY))));
  }

  @Test
  public void testEnterCycle() {
    Edge edge1 = new Edge(state(1), state(2), constraint(0));
    Edge edge2 = new Edge(state(2), state(3), constraint(1));
    Edge edge3 = new Edge(state(3), state(1), constraint(2));
    Edge edge4 = new Edge(state(4), state(1), constraint(3));
    assertThat(
        optimize(
            ImmutableSet.of(edge1, edge2, edge3, edge4),
            ImmutableSet.of(state(4), state(2)),
            false),
        containsInAnyOrder(
            new Edge(state(4), state(1), constraint(3)),
            new Edge(state(1), state(2), constraint(0)),
            new Edge(state(2), state(1), constraint(bdd(1).and(bdd(2))))));
  }

  @Test
  public void mergeWithExistingEdge() {
    Edge edge1 = new Edge(state(1), state(2), constraint(0));
    Edge edge2 = new Edge(state(2), state(3), constraint(1));
    Edge edge3 = new Edge(state(1), state(3), constraint(2));
    Collection<Edge> optimize =
        optimize(ImmutableSet.of(edge1, edge2, edge3), ImmutableSet.of(state(1), state(3)), false);
    assertThat(
        optimize,
        contains(new Edge(state(1), state(3), constraint(bdd(0).and(bdd(1)).or(bdd(2))))));
  }

  @Test
  public void removeIdentityIn() {
    Edge edge1 = new Edge(state(1), state(3), constraint(0));
    Edge edge2 = new Edge(state(2), state(3), constraint(1));
    Edge edge3 = new Edge(state(3), state(4), constraint(2));
    Edge edge4 = new Edge(state(3), state(5), IDENTITY); // state(5) will be removed
    Edge edge5 = new Edge(state(5), state(6), constraint(4));
    Edge edge6 = new Edge(state(5), state(7), constraint(5));
    assertThat(
        optimize(
            ImmutableSet.of(edge1, edge2, edge3, edge4, edge5, edge6),
            ImmutableSet.of(state(1), state(2), state(4), state(6), state(7)),
            false),
        containsInAnyOrder(
            edge1,
            edge2,
            edge3,
            new Edge(state(3), state(6), constraint(4)),
            new Edge(state(3), state(7), constraint(5))));
  }

  @Test
  public void removeIdentityIn_keepSelfLoops() {
    Edge edge1 = new Edge(state(1), state(1), IDENTITY);
    Edge edge2 = new Edge(state(1), state(2), constraint(0));
    Edge edge3 = new Edge(state(1), state(3), constraint(1));
    Collection<Edge> optimize =
        optimize(ImmutableSet.of(edge1, edge2, edge3), ImmutableSet.of(state(2), state(3)), true);
    // we remove 1 because it's unreachable, then 2 and 3 have no in-edges.
    assertThat(optimize, empty());
  }

  @Test
  public void removeIdentityOut() {
    Edge edge1 = new Edge(state(1), state(3), constraint(0));
    Edge edge2 = new Edge(state(2), state(3), constraint(1));
    Edge edge3 = new Edge(state(3), state(5), IDENTITY); // state(3) will be removed
    Edge edge4 = new Edge(state(4), state(5), constraint(2));
    Edge edge5 = new Edge(state(5), state(6), constraint(4));
    Edge edge6 = new Edge(state(5), state(7), constraint(5));
    assertThat(
        optimize(
            ImmutableSet.of(edge1, edge2, edge3, edge4, edge5, edge6),
            ImmutableSet.of(state(1), state(2), state(4), state(6), state(7)),
            false),
        containsInAnyOrder(
            new Edge(state(1), state(5), constraint(0)),
            new Edge(state(2), state(5), constraint(1)),
            edge4,
            edge5,
            edge6));
  }

  @Test
  public void removeIdentityOut_keepSelfLoops() {
    Edge edge1 = new Edge(state(1), state(3), constraint(1));
    Edge edge2 = new Edge(state(2), state(3), constraint(2));
    Edge edge3 = new Edge(state(3), state(3), IDENTITY);
    Collection<Edge> optimize =
        optimize(ImmutableSet.of(edge1, edge2, edge3), ImmutableSet.of(state(1), state(2)), true);
    // remove nothing
    assertThat(optimize, containsInAnyOrder(edge1, edge2, edge3));
  }
}
