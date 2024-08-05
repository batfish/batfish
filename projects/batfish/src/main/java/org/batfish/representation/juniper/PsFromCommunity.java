package org.batfish.representation.juniper;

import java.util.Collection;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
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
    throw new UnsupportedOperationException("Expecting to use #groupToMatchCommunities");
  }

  /**
   * Converts multiple {@link PsFromCommunity} in the same policy term into a {@link BooleanExpr}.
   */
  public static BooleanExpr groupToMatchCommunities(
      Configuration c, Collection<PsFromCommunity> froms) {
    List<CommunitySetMatchExpr> names =
        froms.stream()
            .map(PsFromCommunity::getName)
            .filter(c.getCommunitySetMatchExprs()::containsKey)
            .map(CommunitySetMatchExprReference::new)
            .map(CommunitySetMatchExpr.class::cast)
            .toList();
    if (names.isEmpty()) {
      // There are some, but they are undefined. We already warn on undefined reference.
      return BooleanExprs.FALSE;
    } else if (names.size() == 1) {
      return new MatchCommunities(InputCommunities.instance(), names.get(0));
    } else {
      return new MatchCommunities(InputCommunities.instance(), new CommunitySetMatchAny(names));
    }
  }
}
