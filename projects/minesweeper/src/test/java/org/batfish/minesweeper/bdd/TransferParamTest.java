package org.batfish.minesweeper.bdd;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.minesweeper.bdd.TransferParam.CallContext;
import org.batfish.minesweeper.bdd.TransferParam.ChainContext;
import org.junit.Test;

public class TransferParamTest {
  @Test
  public void testEquals() {
    TransferParam p = new TransferParam(false);

    new EqualsTester()
        .addEqualityGroup(p, new TransferParam(false))
        .addEqualityGroup(p.setCallContext(CallContext.STMT_CALL))
        .addEqualityGroup(p.setChainContext(ChainContext.DISJUNCTION))
        .addEqualityGroup(p.setDefaultAccept(true))
        .addEqualityGroup(p.setDefaultAcceptLocal(true))
        .addEqualityGroup(p.setDefaultPolicy(new SetDefaultPolicy("foo")))
        .addEqualityGroup(p.setReadIntermediateBgpAttributes(true))
        .testEquals();
  }
}
