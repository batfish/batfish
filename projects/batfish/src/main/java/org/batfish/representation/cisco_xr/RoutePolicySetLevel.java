package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IsisLevelExpr;
import org.batfish.datamodel.routing_policy.statement.SetIsisLevel;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetLevel extends RoutePolicySetStatement {

  private IsisLevelExpr _level;

  public RoutePolicySetLevel(IsisLevelExpr level) {
    _level = level;
  }

  @Override
  protected Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetIsisLevel(_level);
  }
}
