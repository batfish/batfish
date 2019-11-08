package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConversions.toCommunitySetExpr;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A route-policy statement that sets or appends to the standard community attribute of a route. */
@ParametersAreNonnullByDefault
public final class RoutePolicySetExtcommunityRt extends RoutePolicySetStatement {

  public RoutePolicySetExtcommunityRt(ExtcommunitySetRtExpr expr, boolean additive) {
    _expr = expr;
    _additive = additive;
  }

  public boolean getAdditive() {
    return _additive;
  }

  public @Nonnull ExtcommunitySetRtExpr getExpr() {
    return _expr;
  }

  @Override
  public @Nonnull Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return _expr
        .accept(XrToViCommunitySetExpr.INSTANCE, c)
        .map(
            communitySetExpr ->
                _additive
                    ? CommunitySetUnion.of(InputCommunities.instance(), communitySetExpr)
                    : CommunitySetUnion.of(
                        new CommunitySetDifference(
                            InputCommunities.instance(), RouteTargetExtendedCommunities.instance()),
                        communitySetExpr))
        .<Statement>map(SetCommunities::new)
        .orElse(INVALID);
  }

  private static final Comment INVALID = new Comment("(invalid community-set expression)");

  private static final class XrToViCommunitySetExpr
      implements ExtcommunitySetRtExprVisitor<Optional<CommunitySetExpr>, Configuration> {
    private static final XrToViCommunitySetExpr INSTANCE = new XrToViCommunitySetExpr();

    @Override
    public Optional<CommunitySetExpr> visitExtcommunitySetRtReference(
        ExtcommunitySetRtReference extcommunitySetRtReference, Configuration arg) {
      // return reference to computed CommunitySetExpr if it exists, else empty Optional.
      return Optional.of(extcommunitySetRtReference.getName())
          .map(CiscoXrConfiguration::computeExtcommunitySetRtName)
          .filter(arg.getCommunitySetExprs()::containsKey)
          .map(CommunitySetExprReference::new);
    }

    @Override
    public Optional<CommunitySetExpr> visitInlineExtcommunitySetRt(
        InlineExtcommunitySetRt inlineExtcommunitySetRt, Configuration arg) {
      return Optional.of(toCommunitySetExpr(inlineExtcommunitySetRt.getExtcommunitySetRt(), arg));
    }
  }

  private final boolean _additive;
  private final ExtcommunitySetRtExpr _expr;
}
