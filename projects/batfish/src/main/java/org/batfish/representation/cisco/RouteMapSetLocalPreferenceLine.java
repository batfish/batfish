package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetLocalPreferenceLine extends RouteMapSetLine {

  private static final long serialVersionUID = 1L;

  private IntExpr _localPreference;

  public RouteMapSetLocalPreferenceLine(IntExpr localPreference) {
    _localPreference = localPreference;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetLocalPreference(_localPreference));
  }

  public IntExpr getLocalPreference() {
    return _localPreference;
  }

  @Override
  public RouteMapSetType getType() {
    return RouteMapSetType.LOCAL_PREFERENCE;
  }
}
