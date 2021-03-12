package org.batfish.representation.cisco_asa;

import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;

/**
 * Handles the "route-map match community-list" command, which matches when at least one of the
 * named lists matches at least one community in the advertisement.
 */
public class RouteMapMatchCommunityListLine extends RouteMapMatchLine {

  private final Set<String> _listNames;

  public RouteMapMatchCommunityListLine(Set<String> names) {
    _listNames = names;
  }

  public Set<String> getListNames() {
    return _listNames;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, AsaConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (String listName : _listNames) {
      CommunityList list = c.getCommunityLists().get(listName);
      if (list != null) {
        disjuncts.add(new MatchCommunitySet(new NamedCommunitySet(listName)));
      }
    }
    return d.simplify();
  }

  @Override
  public <T> T accept(RouteMapMatchLineVisitor<T> visitor) {
    return visitor.visitRouteMapMatchCommunityListLine(this);
  }
}
