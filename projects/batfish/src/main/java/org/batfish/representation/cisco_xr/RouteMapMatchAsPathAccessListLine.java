package org.batfish.representation.cisco_xr;

import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;

public class RouteMapMatchAsPathAccessListLine extends RouteMapMatchLine {

  private final Set<String> _listNames;

  public RouteMapMatchAsPathAccessListLine(Set<String> names) {
    _listNames = names;
  }

  public Set<String> getListNames() {
    return _listNames;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CiscoXrConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (String listName : _listNames) {
      IpAsPathAccessList list = cc.getAsPathAccessLists().get(listName);
      if (list != null) {
        disjuncts.add(new MatchAsPath(new NamedAsPathSet(listName)));
      }
    }
    return d.simplify();
  }
}
