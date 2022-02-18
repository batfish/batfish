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

/** Represents a "from prefix-list-filter PREFIX_LIST_NAME longer" line in a {@link PsTerm} */
@ParametersAreNonnullByDefault
public final class PsFromPrefixListFilterLonger extends PsFrom {

  private final @Nonnull String _prefixList;

  public PsFromPrefixListFilterLonger(String prefixList) {
    _prefixList = prefixList;
  }

  @VisibleForTesting
  static String name(String plName) {
    return "~" + plName + "~LONGER~";
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

    RouteFilterList longerList = c.getRouteFilterLists().get(name(_prefixList));
    if (longerList == null) {
      longerList = new RouteFilterList(name(_prefixList));
      c.getRouteFilterLists().put(longerList.getName(), longerList);
      for (Prefix prefix : pl.getPrefixes()) {
        if (prefix.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
          // Skip this prefix; there may be others in the prefix-list.
          continue;
        }
        SubRange longerLineRange =
            new SubRange(prefix.getPrefixLength() + 1, Prefix.MAX_PREFIX_LENGTH);
        longerList.addLine(new RouteFilterLine(LineAction.PERMIT, prefix, longerLineRange));
      }
    }
    return new MatchPrefixSet(
        DestinationNetwork.instance(), new NamedPrefixSet(longerList.getName()));
  }
}
