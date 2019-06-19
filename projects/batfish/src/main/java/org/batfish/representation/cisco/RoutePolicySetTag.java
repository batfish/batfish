package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetTag extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  private IntExpr _tag;

  public RoutePolicySetTag(IntExpr intExpr) {
    _tag = intExpr;
  }

  public IntExpr getTag() {
    return _tag;
  }

  @Override
  protected Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new SetTag(_tag);
  }
}
