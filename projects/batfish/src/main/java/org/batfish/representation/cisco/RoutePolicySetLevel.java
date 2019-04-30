package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IsisLevelExpr;
import org.batfish.datamodel.routing_policy.statement.SetIsisLevel;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetLevel extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  private IsisLevelExpr _level;

  public RoutePolicySetLevel(IsisLevelExpr level) {
    _level = level;
  }

  @Override
  protected Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new SetIsisLevel(_level);
  }
}
