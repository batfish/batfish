package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

/** Match condition that holds when route's network is matched by the referenced prefix-list */
@ParametersAreNonnullByDefault
public final class RouteMapMatchPrefixList implements RouteMapMatch {

  private final @Nonnull String _prefixList;

  public RouteMapMatchPrefixList(String prefixList) {
    _prefixList = prefixList;
  }

  public @Nonnull String getPrefixList() {
    return _prefixList;
  }

  @Override
  public @Nonnull BooleanExpr toBooleanExpr(Configuration c, F5BigipConfiguration vc, Warnings w) {
    return vc.getPrefixLists().containsKey(_prefixList)
        ? new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(_prefixList))
        : BooleanExprs.FALSE;
  }
}
