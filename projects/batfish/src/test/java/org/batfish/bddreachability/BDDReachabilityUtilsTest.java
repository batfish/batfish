package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ForwardingTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.StateExpr;
import org.junit.Test;

/** Tests of {@link BDDReachabilityUtils}. */
public final class BDDReachabilityUtilsTest {
  private final BDDPacket _pkt = new BDDPacket();

  @Test
  public void testBackwardFixpointPropagates() {
    StateExpr s1 = new OriginateVrf("n1", "v1");
    StateExpr s2 = new OriginateVrf("n2", "v2");
    Table<StateExpr, StateExpr, Transition> edges =
        ImmutableTable.<StateExpr, StateExpr, Transition>builder().put(s1, s2, IDENTITY).build();
    Map<StateExpr, BDD> reverseReachable = new HashMap<>();
    reverseReachable.put(s2, _pkt.getFactory().one());

    BDDReachabilityUtils.backwardFixpoint(edges, reverseReachable);

    assertThat(
        reverseReachable,
        equalTo(ImmutableMap.of(s1, _pkt.getFactory().one(), s2, _pkt.getFactory().one())));
  }

  @Test
  public void testBackwardFixpointWithNothingReachableDoesNotReadTheTable() {
    Table<StateExpr, StateExpr, Transition> untouchable =
        new ForwardingTable<StateExpr, StateExpr, Transition>() {
          @Override
          protected Table<StateExpr, StateExpr, Transition> delegate() {
            throw new AssertionError("read the edge table with nothing to propagate");
          }
        };
    Map<StateExpr, BDD> reverseReachable = new HashMap<>();

    BDDReachabilityUtils.backwardFixpoint(untouchable, reverseReachable);

    assertThat(reverseReachable, equalTo(ImmutableMap.of()));
  }
}
