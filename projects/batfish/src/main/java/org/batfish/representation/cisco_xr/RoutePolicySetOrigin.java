package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetOrigin extends RoutePolicySetStatement {

  private OriginExpr _origin;

  public RoutePolicySetOrigin(OriginExpr origin) {
    _origin = origin;
  }

  public OriginExpr getOrigin() {
    return _origin;
  }

  @Override
  protected Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetOrigin(_origin);
  }
}
