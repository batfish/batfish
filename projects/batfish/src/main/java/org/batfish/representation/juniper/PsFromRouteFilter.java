package org.batfish.representation.juniper;

import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork6;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefix6Set;
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
    Route6FilterList rfl6 = c.getRoute6FilterLists().get(_routeFilterName);
    BooleanExpr match4 = null;
    BooleanExpr match6 = null;
    if (rfl != null) {
      match4 =
          new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(_routeFilterName));
    }
    if (rfl6 != null) {
      match6 =
          new MatchPrefix6Set(new DestinationNetwork6(), new NamedPrefix6Set(_routeFilterName));
    }
    if (match4 != null && match6 == null) {
      return match4;
    } else if (rfl == null && rfl6 != null) {
      return match6;
    } else if (rfl != null && rfl6 != null) {
      Disjunction d = new Disjunction();
      d.getDisjuncts().add(match4);
      d.getDisjuncts().add(match6);
      return d;
    } else {
      throw new VendorConversionException(
          "missing route filter list: \"" + _routeFilterName + "\"");
    }
  }
}
