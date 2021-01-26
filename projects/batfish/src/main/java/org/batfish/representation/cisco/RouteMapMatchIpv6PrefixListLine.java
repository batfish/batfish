package org.batfish.representation.cisco;

import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork6;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.NamedPrefix6Set;

public class RouteMapMatchIpv6PrefixListLine extends RouteMapMatchLine {

  private final Set<String> _listNames;

  public RouteMapMatchIpv6PrefixListLine(Set<String> names) {
    _listNames = names;
  }

  public Set<String> getListNames() {
    return _listNames;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (String listName : _listNames) {
      Prefix6List list = cc.getPrefix6Lists().get(listName);
      if (list != null) {
        disjuncts.add(
            new MatchPrefix6Set(new DestinationNetwork6(), new NamedPrefix6Set(listName)));
      }
    }
    return d.simplify();
  }

  @Override
  public <T> T accept(RouteMapMatchLineVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpv6PrefixListLine(this);
  }
}
