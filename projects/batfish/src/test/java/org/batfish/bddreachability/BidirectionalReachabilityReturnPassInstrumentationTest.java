package org.batfish.bddreachability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.Query;
import org.batfish.symbolic.state.StateExpr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link BidirectionalReachabilityReturnPassInstrumentation}. */
class BidirectionalReachabilityReturnPassInstrumentationTest {
  private BDD _constraint;

  @BeforeEach
  void setup() {
    BDDPacket bddPacket = new BDDPacket();
    _constraint = bddPacket.getDstIp().value(1);
  }

  @Test
  void testNeighborUnreachable() {
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
  void testInsufficientInfo() {
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
