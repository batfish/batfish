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

/** Represents a "from prefix-list-filter PREFIX_LIST_NAME longer" line in a {@link PsTerm} */
public final class PsFromPrefixListFilterLonger extends PsFrom {

  private String _prefixList;

  public PsFromPrefixListFilterLonger(String prefixList) {
    _prefixList = prefixList;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_prefixList);
    if (pl != null) {
      if (pl.getIpv6()) {
        return BooleanExprs.FALSE;
      }
      RouteFilterList rf = c.getRouteFilterLists().get(_prefixList);
      String longerListName = "~" + _prefixList + "~LONGER~";
      RouteFilterList longerList = c.getRouteFilterLists().get(longerListName);
      if (longerList == null) {
        longerList = new RouteFilterList(longerListName);
        for (RouteFilterLine line : rf.getLines()) {
          Prefix prefix = line.getIpWildcard().toPrefix();
          LineAction action = line.getAction();
          SubRange longerLineRange =
              new SubRange(line.getLengthRange().getStart() + 1, Prefix.MAX_PREFIX_LENGTH);
          if (longerLineRange.getStart() > Prefix.MAX_PREFIX_LENGTH) {
            warnings.redFlag(
                "'prefix-list-filter "
                    + _prefixList
                    + " longer' cannot match more specific prefix than "
                    + prefix);
            continue;
          }
          RouteFilterLine orLongerLine = new RouteFilterLine(action, prefix, longerLineRange);
          longerList.addLine(orLongerLine);
          c.getRouteFilterLists().put(longerListName, longerList);
        }
      }
      return new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(longerListName));
    } else {
      warnings.redFlag("Reference to undefined prefix-list: \"" + _prefixList + "\"");
      return BooleanExprs.FALSE;
    }
  }
}
