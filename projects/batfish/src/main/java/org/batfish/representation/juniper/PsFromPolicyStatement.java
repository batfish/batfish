package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;

public final class PsFromPolicyStatement extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _policyStatement;

  public PsFromPolicyStatement(String policyStatement) {
    _policyStatement = policyStatement;
  }

  public String getPolicyStatement() {
    return _policyStatement;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new CallExpr(_policyStatement);
  }
}
