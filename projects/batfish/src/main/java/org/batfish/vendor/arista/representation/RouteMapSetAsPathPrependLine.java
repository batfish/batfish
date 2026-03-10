package org.batfish.vendor.arista.representation;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetAsPathPrependLine extends RouteMapSetLine {

  private List<AsExpr> _asList;

  public RouteMapSetAsPathPrependLine(List<AsExpr> asList) {
    _asList = asList;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new PrependAsPath(new LiteralAsList(_asList)));
  }

  public List<AsExpr> getAsList() {
    return _asList;
  }
}
