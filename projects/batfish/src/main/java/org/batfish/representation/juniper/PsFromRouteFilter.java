package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

/** Represents a "from route-filter" line in a {@link PsTerm} */
public final class PsFromRouteFilter extends PsFrom {

  private String _routeFilterName;

  public PsFromRouteFilter(String routeFilterName) {
    _routeFilterName = routeFilterName;
  }

  public String getRouteFilterName() {
    return _routeFilterName;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    RouteFilterList rfl = c.getRouteFilterLists().get(_routeFilterName);
    if (rfl != null) {
      return new MatchPrefixSet(
          DestinationNetwork.instance(), new NamedPrefixSet(_routeFilterName));
    }
    return BooleanExprs.FALSE; // V6 route filter
  }
}
