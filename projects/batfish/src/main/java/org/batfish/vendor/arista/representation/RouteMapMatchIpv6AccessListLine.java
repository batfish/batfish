package org.batfish.vendor.arista.representation;

import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

public class RouteMapMatchIpv6AccessListLine extends RouteMapMatchLine {

  private final Set<String> _listNames;

  private boolean _routing;

  public RouteMapMatchIpv6AccessListLine(Set<String> names) {
    _listNames = names;
  }

  public Set<String> getListNames() {
    return _listNames;
  }

  public boolean getRouting() {
    return _routing;
  }

  public void setRouting(boolean routing) {
    _routing = routing;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, AristaConfiguration cc, Warnings w) {
    return BooleanExprs.FALSE;
  }
}
