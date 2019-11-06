package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetTag extends RoutePolicySetStatement {

  private LongExpr _tag;

  public RoutePolicySetTag(LongExpr longExpr) {
    _tag = longExpr;
  }

  public LongExpr getTag() {
    return _tag;
  }

  @Override
  protected Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetTag(_tag);
  }
}
