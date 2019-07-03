package org.batfish.representation.vyos;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

/**
 * A route-map condition requiring the candidate route be for a network allowed by a specified
 * prefix-list.
 */
public class RouteMapMatchPrefixList implements RouteMapMatch {

  private final String _prefixList;

  private final int _statementLine;

  public RouteMapMatchPrefixList(String prefixList, int statementLine) {
    _prefixList = prefixList;
    _statementLine = statementLine;
  }

  public String getPrefixList() {
    return _prefixList;
  }

  @Override
  public BooleanExpr toBooleanExpr(VyosConfiguration vc, Configuration c, Warnings w) {
    PrefixList pl = vc.getPrefixLists().get(_prefixList);
    if (pl != null) {
      return new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(_prefixList));
    } else {
      vc.undefined(
          VyosStructureType.PREFIX_LIST,
          _prefixList,
          VyosStructureUsage.ROUTE_MAP_MATCH_PREFIX_LIST,
          _statementLine);
      // TODO: see if vyos treats as true, false, or disallows
      return BooleanExprs.TRUE;
    }
  }
}
