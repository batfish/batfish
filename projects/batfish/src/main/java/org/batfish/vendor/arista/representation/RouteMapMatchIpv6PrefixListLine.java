package org.batfish.vendor.arista.representation;

import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

public class RouteMapMatchIpv6PrefixListLine extends RouteMapMatchLine {

  private final Set<String> _listNames;

  public RouteMapMatchIpv6PrefixListLine(Set<String> names) {
    _listNames = names;
  }

  public Set<String> getListNames() {
    return _listNames;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, AristaConfiguration cc, Warnings w) {
    return BooleanExprs.FALSE;
  }
}
