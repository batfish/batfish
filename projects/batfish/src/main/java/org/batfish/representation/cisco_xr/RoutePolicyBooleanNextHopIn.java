package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.IpPrefix;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyBooleanNextHopIn extends RoutePolicyBoolean {

  private final RoutePolicyPrefixSet _prefixSet;

  public RoutePolicyBooleanNextHopIn(RoutePolicyPrefixSet prefixSet) {
    _prefixSet = prefixSet;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    PrefixSetExpr prefixSetExpr = _prefixSet.toPrefixSetExpr(cc, c, w);
    if (prefixSetExpr != null) {
      return new MatchPrefixSet(
          new IpPrefix(NextHopIp.instance(), new LiteralInt(Prefix.MAX_PREFIX_LENGTH)),
          prefixSetExpr);
    }
    // Only a V6 PrefixSet
    return BooleanExprs.FALSE;
  }
}
