package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;

public class RoutePolicyBooleanAsPathIn extends RoutePolicyBoolean {

  private final AsPathSetExpr _asExpr;

  public RoutePolicyBooleanAsPathIn(AsPathSetExpr expr) {
    _asExpr = expr;
  }

  public AsPathSetExpr getName() {
    return _asExpr;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    if (_asExpr instanceof NamedAsPathSet) {
      NamedAsPathSet named = (NamedAsPathSet) _asExpr;
      String name = named.getName();
      AsPathSet asPathSet = cc.getAsPathSets().get(name);
      if (asPathSet == null) {
        // Undefined, return false.
        return BooleanExprs.FALSE;
      }
    }
    MatchAsPath match = new MatchAsPath(_asExpr);
    return match;
  }
}
