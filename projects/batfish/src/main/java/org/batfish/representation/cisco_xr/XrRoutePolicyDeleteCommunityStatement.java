package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConversions.toCommunityMatchExpr;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * A route-policy statement that deletes communities matched by the provided {@link
 * CommunitySetExpr} under the provided negation flag.
 */
@ParametersAreNonnullByDefault
public class XrRoutePolicyDeleteCommunityStatement extends RoutePolicySetStatement {

  public XrRoutePolicyDeleteCommunityStatement(boolean negated, XrCommunitySetExpr expr) {
    _negated = negated;
    _expr = expr;
  }

  public @Nonnull XrCommunitySetExpr getExpr() {
    return _expr;
  }

  public boolean getNegated() {
    return _negated;
  }

  @Override
  public Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return _expr
        .accept(CommunitySetExprToCommunityMatchExpr.INSTANCE, c)
        .map(
            communityMatchExpr ->
                _negated ? new CommunityNot(communityMatchExpr) : communityMatchExpr)
        .<Statement>map(
            communityMatchExpr ->
                new SetCommunities(
                    new CommunitySetDifference(InputCommunities.instance(), communityMatchExpr)))
        .orElse(INVALID);
  }

  private static final class CommunitySetExprToCommunityMatchExpr
      implements XrCommunitySetExprVisitor<Optional<CommunityMatchExpr>, Configuration> {
    private static final CommunitySetExprToCommunityMatchExpr INSTANCE =
        new CommunitySetExprToCommunityMatchExpr();

    @Override
    public Optional<CommunityMatchExpr> visitCommunitySetReference(
        XrCommunitySetReference communitySetReference, Configuration arg) {
      // return reference to computed CommunityMatchExpr if it exists, else empty Optional.
      return Optional.of(communitySetReference.getName())
          .filter(arg.getCommunityMatchExprs()::containsKey)
          .map(CommunityMatchExprReference::new);
    }

    @Override
    public Optional<CommunityMatchExpr> visitInlineCommunitySet(
        XrInlineCommunitySet inlineCommunitySet, Configuration arg) {
      return Optional.of(toCommunityMatchExpr(inlineCommunitySet.getCommunitySet(), arg));
    }
  }

  private static final Statement INVALID = new Comment("(invalid community-set reference)");

  private final @Nonnull XrCommunitySetExpr _expr;
  private final boolean _negated;
}
