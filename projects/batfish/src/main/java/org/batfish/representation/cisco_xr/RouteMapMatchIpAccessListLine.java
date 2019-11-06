package org.batfish.representation.cisco_xr;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

@ParametersAreNonnullByDefault
public final class RouteMapMatchIpAccessListLine extends RouteMapMatchLine {

  @Nonnull private final Set<String> _listNames;

  public RouteMapMatchIpAccessListLine(Set<String> listNames) {
    _listNames = listNames;
  }

  @Nonnull
  public Set<String> getListNames() {
    return _listNames;
  }

  @Override
  @Nonnull
  public BooleanExpr toBooleanExpr(Configuration c, CiscoXrConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (String listName : _listNames) {
      RouteFilterList routeFilterList = c.getRouteFilterLists().get(listName);
      if (routeFilterList != null) {
        disjuncts.add(
            new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(listName)));
      }
    }
    return d.simplify();
  }
}
