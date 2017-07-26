package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetLocalPref extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  private IntExpr _pref;

  public RoutePolicySetLocalPref(IntExpr intExpr) {
    _pref = intExpr;
  }

  public IntExpr getLocalPref() {
    return _pref;
  }

  @Override
  public Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new SetLocalPreference(_pref);
  }
}
