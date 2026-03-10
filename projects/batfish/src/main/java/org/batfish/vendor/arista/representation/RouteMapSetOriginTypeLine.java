package org.batfish.vendor.arista.representation;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetOriginTypeLine extends RouteMapSetLine {

  private OriginExpr _originExpr;

  public RouteMapSetOriginTypeLine(OriginExpr originExpr) {
    _originExpr = originExpr;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetOrigin(_originExpr));
  }

  public OriginExpr getOriginExpr() {
    return _originExpr;
  }

  public void setOriginExpr(OriginExpr originExpr) {
    _originExpr = originExpr;
  }
}
