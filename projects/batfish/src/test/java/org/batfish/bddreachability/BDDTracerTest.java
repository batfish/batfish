package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDTracer.BDDDisposition.LOOPED;
import static org.batfish.bddreachability.BDDTracer.BDDDisposition.TERMINATED;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDTracer.BDDDisposition;
import org.batfish.bddreachability.BDDTracer.BDDHop;
import org.batfish.bddreachability.BDDTracer.BDDTrace;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.state.StateExpr;
import org.batfish.symbolic.state.StateExprVisitor;
import org.junit.Test;

public class BDDTracerTest {
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
    public <R> R accept(StateExprVisitor<R> visitor) {
      throw new UnsupportedOperationException();
    }
  }

  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _one = _pkt.getFactory().one();

  private static final StateExpr A = new DummyState("A");
  private static final StateExpr B = new DummyState("B");
  private static final StateExpr C = new DummyState("C");

  private static BDDTrace trace(BDDDisposition disposition, BDDHop... hops) {
    return new BDDTrace(ImmutableList.copyOf(hops), disposition);
  }

  private BDDHop hop(StateExpr expr) {
    return new BDDHop(expr, _one);
  }

  private BDDHop hop(StateExpr expr, BDD bdd) {
    return new BDDHop(expr, bdd);
  }

  @Test
  public void testSimple() {
    ImmutableTable<StateExpr, StateExpr, Transition> table =
        ImmutableTable.<StateExpr, StateExpr, Transition>builder()
            .put(A, B, IDENTITY)
            .put(B, C, IDENTITY)
            .build();
    List<BDDTrace> traces = BDDTracer.getTraces(table, A, _one);
    assertThat(traces, contains(trace(TERMINATED, hop(A), hop(B), hop(C))));
  }

  @Test
  public void testLoop() {
    ImmutableTable<StateExpr, StateExpr, Transition> table =
        ImmutableTable.<StateExpr, StateExpr, Transition>builder()
            .put(A, B, IDENTITY)
            .put(B, A, IDENTITY)
            .build();
    List<BDDTrace> traces = BDDTracer.getTraces(table, A, _one);
    assertThat(traces, contains(trace(LOOPED, hop(A), hop(B), hop(A))));
  }

  @Test
  public void testLoopWithConstraint() {
    BDD dst1 = _pkt.getDstIpSpaceToBDD().toBDD(Prefix.parse("128.0.0.0/1"));
    ImmutableTable<StateExpr, StateExpr, Transition> table =
        ImmutableTable.<StateExpr, StateExpr, Transition>builder()
            .put(A, B, IDENTITY)
            .put(B, A, constraint(dst1))
            .build();
    List<BDDTrace> traces = BDDTracer.getTraces(table, A, _one);
    assertThat(
        traces,
        containsInAnyOrder(
            trace(TERMINATED, hop(A), hop(B), hop(B, dst1.not())),
            trace(LOOPED, hop(A), hop(B), hop(A, dst1))));
  }

  @Test
  public void testBranch() {
    BDD toB = _pkt.getDstIpSpaceToBDD().toBDD(Prefix.parse("0.0.0.0/1"));
    BDD toC = _pkt.getDstIpSpaceToBDD().toBDD(Prefix.parse("128.0.0.0/1"));
    ImmutableTable<StateExpr, StateExpr, Transition> table =
        ImmutableTable.<StateExpr, StateExpr, Transition>builder()
            .put(A, B, constraint(toB))
            .put(A, C, constraint(toC))
            .build();
    List<BDDTrace> traces = BDDTracer.getTraces(table, A, _one);
    assertThat(
        traces,
        contains(
            trace(TERMINATED, hop(A), hop(B, toB)), //
            trace(TERMINATED, hop(A), hop(C, toC))));
  }
}
