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

/** Match condition that holds when route's network is matched by the referenced access-list */
@ParametersAreNonnullByDefault
public class MatchAccessList implements RouteMapMatch {

  private final @Nonnull String _name;

  public MatchAccessList(String name) {
    _name = name;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, F5BigipConfiguration vc, Warnings w) {
    String rflName = F5BigipConfiguration.computeAccessListRouteFilterName(_name);
    return vc.getAccessLists().containsKey(_name) && c.getRouteFilterLists().containsKey(rflName)
        ? new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(rflName))
        : BooleanExprs.FALSE;
  }
}
