package org.batfish.bddreachability;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.Query;
import org.batfish.symbolic.state.StateExpr;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link BidirectionalReachabilityReturnPassInstrumentation}. */
public class BidirectionalReachabilityReturnPassInstrumentationTest {
  private BDD _constraint;

  @Before
  public void setup() {
    BDDPacket bddPacket = new BDDPacket();
    _constraint = bddPacket.getDstIp().value(1);
  }

  @Test
  public void testNeighborUnreachable() {
    StateExpr stateExpr = new NodeInterfaceNeighborUnreachable("NODE", "IFACE");
    BidirectionalReachabilityReturnPassInstrumentation instrumentation =
        new BidirectionalReachabilityReturnPassInstrumentation(
            ImmutableMap.of(stateExpr, _constraint));

    // postState doesn't matter
    Edge origEdge = new Edge(stateExpr, Query.INSTANCE);
    Edge newEdge = new Edge(stateExpr, Query.INSTANCE, _constraint.not());
    assertEquals(instrumentation.instrumentReturnPassEdge(origEdge), newEdge);
  }

  @Test
  public void testInsufficientInfo() {
    StateExpr stateExpr = new NodeInterfaceInsufficientInfo("NODE", "IFACE");
    BidirectionalReachabilityReturnPassInstrumentation instrumentation =
        new BidirectionalReachabilityReturnPassInstrumentation(
            ImmutableMap.of(stateExpr, _constraint));

    // postState doesn't matter
    Edge origEdge = new Edge(stateExpr, Query.INSTANCE);
    Edge newEdge = new Edge(stateExpr, Query.INSTANCE, _constraint.not());
    assertEquals(instrumentation.instrumentReturnPassEdge(origEdge), newEdge);
  }
}
