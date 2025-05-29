package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.minesweeper.bdd.TransferParam.CallContext;
import org.batfish.minesweeper.bdd.TransferParam.ChainContext;
import org.junit.Test;

public class TransferParamTest {
  @Test
  public void testEquals() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute route = new BDDRoute(factory, 0, 0, 0, 0, 0, 0, ImmutableList.of());
    BDDRoute route2 = new BDDRoute(route);
    route2.getAdminDist().setValue(1);
    TransferParam p = new TransferParam(route, false);

    new EqualsTester()
        .addEqualityGroup(p, new TransferParam(route, false))
        .addEqualityGroup(p.setCallContext(CallContext.STMT_CALL))
        .addEqualityGroup(p.setChainContext(ChainContext.DISJUNCTION))
        .addEqualityGroup(p.setData(route2))
        .addEqualityGroup(p.setDefaultAccept(true))
        .addEqualityGroup(p.setDefaultAcceptLocal(true))
        .addEqualityGroup(p.setDefaultPolicy(new SetDefaultPolicy("foo")))
        .addEqualityGroup(p.setReadIntermediateBgpAttributes(true))
        .testEquals();
  }
}
