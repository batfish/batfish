package org.batfish.representation.cisco;

import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork6;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchIp6AccessList;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.NamedPrefix6Set;

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
  public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (String listName : _listNames) {
      Object list;
      Ip6AccessList ipAccessList = null;
      Route6FilterList routeFilterList = null;
      if (_routing) {
        routeFilterList = c.getRoute6FilterLists().get(listName);
        list = routeFilterList;
      } else {
        ipAccessList = c.getIp6AccessLists().get(listName);
        list = ipAccessList;
      }
      if (list != null) {
        if (_routing) {
          disjuncts.add(
              new MatchPrefix6Set(new DestinationNetwork6(), new NamedPrefix6Set(listName)));
        } else {
          disjuncts.add(new MatchIp6AccessList(listName));
        }
      }
    }
    return d.simplify();
  }

  @Override
  public <T> T accept(RouteMapMatchLineVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpv6AccessListLine(this);
  }
}
