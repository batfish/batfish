package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** Represents a "from community" line in a {@link PsTerm} */
public final class PsFromCommunity extends PsFrom {

  private final String _name;

  public PsFromCommunity(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    if (!c.getCommunitySetMatchExprs().containsKey(_name)) {
      // undefined reference; illegal config, but just treat as unmatchable for best-effort
      return BooleanExprs.FALSE;
    }
    return new MatchCommunities(
        InputCommunities.instance(), new CommunitySetMatchExprReference(_name));
  }
}
