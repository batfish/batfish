package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;

/** Represents a "from policy" line in a {@link PsTerm} */
public final class PsFromPolicyStatement extends PsFrom {

  private final String _policyStatement;

  public PsFromPolicyStatement(String policyStatement) {
    _policyStatement = policyStatement;
  }

  public String getPolicyStatement() {
    return _policyStatement;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    if (!jc.getMasterLogicalSystem().getPolicyStatements().containsKey(_policyStatement)) {
      return BooleanExprs.FALSE;
    }
    return new CallExpr(_policyStatement);
  }
}
