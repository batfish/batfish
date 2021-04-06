package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertMatchesEveryToCommunitySetMatchExpr;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

public final class XrRoutePolicyBooleanCommunityMatchesEvery extends RoutePolicyBoolean {

  public XrRoutePolicyBooleanCommunityMatchesEvery(XrCommunitySetExpr expr) {
    _expr = expr;
  }

  public @Nonnull XrCommunitySetExpr getExpr() {
    return _expr;
  }

  @Override
  public @Nonnull BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return _expr
        .accept(MatchesEveryCommunitySetExprToCommunitySetMatchExpr.INSTANCE, c)
        .<BooleanExpr>map(matchExpr -> new MatchCommunities(InputCommunities.instance(), matchExpr))
        .orElse(BooleanExprs.FALSE);
  }

  private static class MatchesEveryCommunitySetExprToCommunitySetMatchExpr
      implements XrCommunitySetExprVisitor<Optional<CommunitySetMatchExpr>, Configuration> {
    private static final MatchesEveryCommunitySetExprToCommunitySetMatchExpr INSTANCE =
        new MatchesEveryCommunitySetExprToCommunitySetMatchExpr();

    @Override
    public Optional<CommunitySetMatchExpr> visitCommunitySetParameterReference(
        XrCommunitySetParameterReference xrCommunitySetParameterReference) {
      // TODO: implement route-policy parameters
      return Optional.empty();
    }

    @Override
    public Optional<CommunitySetMatchExpr> visitCommunitySetReference(
        XrCommunitySetReference communitySetReference, Configuration arg) {
      // return reference to computed match-every CommunitySetMatchExpr if it exists, else empty
      // Optional.
      return Optional.of(communitySetReference.getName())
          .map(CiscoXrConfiguration::computeCommunitySetMatchEveryName)
          .filter(arg.getCommunitySetMatchExprs()::containsKey)
          .map(CommunitySetMatchExprReference::new);
    }

    @Override
    public Optional<CommunitySetMatchExpr> visitInlineCommunitySet(
        XrInlineCommunitySet inlineCommunitySet, Configuration arg) {
      return Optional.of(
          convertMatchesEveryToCommunitySetMatchExpr(inlineCommunitySet.getCommunitySet(), arg));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof XrRoutePolicyBooleanCommunityMatchesEvery)) {
      return false;
    }
    XrRoutePolicyBooleanCommunityMatchesEvery that = (XrRoutePolicyBooleanCommunityMatchesEvery) o;
    return _expr.equals(that._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  private final @Nonnull XrCommunitySetExpr _expr;
}
