package org.batfish.representation.cisco;

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

  private static final long serialVersionUID = 1L;

  private final Set<String> _listNames;

  private final int _statementLine;

  public RouteMapMatchCommunityListLine(Set<String> names, int statementLine) {
    _listNames = names;
    _statementLine = statementLine;
  }

  public Set<String> getListNames() {
    return _listNames;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (String listName : _listNames) {
      CommunityList list = c.getCommunityLists().get(listName);
      if (list != null) {
        String msg = "match community line";
        StandardCommunityList standardCommunityList = cc.getStandardCommunityLists().get(listName);
        if (standardCommunityList != null) {
          standardCommunityList.getReferers().put(this, msg);
        }
        ExpandedCommunityList expandedCommunityList = cc.getExpandedCommunityLists().get(listName);
        if (expandedCommunityList != null) {
          expandedCommunityList.getReferers().put(this, msg);
        }
        disjuncts.add(new MatchCommunitySet(new NamedCommunitySet(listName)));
      } else {
        cc.undefined(
            CiscoStructureType.COMMUNITY_LIST,
            listName,
            CiscoStructureUsage.ROUTE_MAP_MATCH_COMMUNITY_LIST,
            _statementLine);
      }
    }
    return d.simplify();
  }
}
