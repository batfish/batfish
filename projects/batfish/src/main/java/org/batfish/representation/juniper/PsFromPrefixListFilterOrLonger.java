package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
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

/** Represents a "from prefix-list-filter PREFIX_LIST_NAME orlonger" line in a {@link PsTerm} */
@ParametersAreNonnullByDefault
public final class PsFromPrefixListFilterOrLonger extends PsFrom {

  private final @Nonnull String _prefixList;

  public PsFromPrefixListFilterOrLonger(String prefixList) {
    _prefixList = prefixList;
  }

  @VisibleForTesting
  static String name(String plName) {
    return "~" + plName + "~ORLONGER~";
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_prefixList);
    if (pl == null) {
      warnings.redFlag("Reference to undefined prefix-list: \"" + _prefixList + "\"");
      return BooleanExprs.FALSE;
    }

    if (pl.getPrefixes().isEmpty()) {
      if (!pl.getHasIpv6()) {
        warnings.redFlag("Empty prefix-list: \"" + _prefixList + "\"");
      }
      return BooleanExprs.FALSE;
    }

    String orLongerListName = name(_prefixList);
    RouteFilterList orLongerList = c.getRouteFilterLists().get(orLongerListName);
    if (orLongerList == null) {
      orLongerList = new RouteFilterList(orLongerListName);
      c.getRouteFilterLists().put(orLongerList.getName(), orLongerList);
      for (Prefix prefix : pl.getPrefixes()) {
        SubRange orLongerLineRange =
            new SubRange(prefix.getPrefixLength(), Prefix.MAX_PREFIX_LENGTH);
        RouteFilterLine orLongerLine =
            new RouteFilterLine(LineAction.PERMIT, prefix, orLongerLineRange);
        orLongerList.addLine(orLongerLine);
      }
    }
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new NamedPrefixSet(orLongerList.getName()));
  }
}
