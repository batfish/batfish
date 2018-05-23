package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

public final class PsFromPrefixListFilterOrLonger extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private String _prefixList;

  public PsFromPrefixListFilterOrLonger(String prefixList) {
    _prefixList = prefixList;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    PrefixList pl = jc.getPrefixLists().get(_prefixList);
    if (pl != null) {
      pl.getReferers().put(this, "from prefix-list-filter or-longer");
      if (pl.getIpv6()) {
        return BooleanExprs.FALSE;
      }
      RouteFilterList rf = c.getRouteFilterLists().get(_prefixList);
      String orLongerListName = "~" + _prefixList + "~ORLONGER~";
      RouteFilterList orLongerList = c.getRouteFilterLists().get(orLongerListName);
      if (orLongerList == null) {
        orLongerList = new RouteFilterList(orLongerListName);
        for (RouteFilterLine line : rf.getLines()) {
          Prefix prefix = line.getIpWildcard().toPrefix();
          LineAction action = line.getAction();
          SubRange orLongerLineRange =
              new SubRange(line.getLengthRange().getStart(), Prefix.MAX_PREFIX_LENGTH);
          RouteFilterLine orLongerLine = new RouteFilterLine(action, prefix, orLongerLineRange);
          orLongerList.addLine(orLongerLine);
          c.getRouteFilterLists().put(orLongerListName, orLongerList);
        }
      }
      return new MatchPrefixSet(new DestinationNetwork(), new NamedPrefixSet(orLongerListName));
    } else {
      warnings.redFlag("Reference to undefined prefix-list: \"" + _prefixList + "\"");
      return BooleanExprs.FALSE;
    }
  }
}
