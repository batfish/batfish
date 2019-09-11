package org.batfish.datamodel.routing_policy.communities;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** A visitor for evaluating a {@link CommunitySetMatchExpr} under a {@link CommunityContext}. */
public final class CommunitySetMatchExprEvaluator implements CommunitySetMatchExprVisitor<Boolean> {

  public CommunitySetMatchExprEvaluator(CommunityContext ctx, CommunitySet communitySet) {
    _ctx = ctx;
    _communitySet = communitySet;
  }

  @Override
  public Boolean visitCommunitySetAcl(CommunitySetAcl communitySetAcl) {
    for (CommunitySetAclLine line : communitySetAcl.getLines()) {
      if (line.getCommunitySetMatchExpr().accept(this)) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  @Override
  public Boolean visitCommunitySetMatchAll(CommunitySetMatchAll communitySetMatchAll) {
    return communitySetMatchAll.getExprs().stream().allMatch(expr -> expr.accept(this));
  }

  @Override
  public Boolean visitCommunitySetMatchAny(CommunitySetMatchAny communitySetMatchAny) {
    return communitySetMatchAny.getExprs().stream().anyMatch(expr -> expr.accept(this));
  }

  @Override
  public Boolean visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference) {
    CommunitySetMatchExpr expr =
        _ctx.getCommunitySetMatchExprs().get(communitySetMatchExprReference.getName());
    if (expr == null) {
      return false;
    }
    return expr.accept(this);
  }

  @Override
  public Boolean visitCommunitySetMatchRegex(CommunitySetMatchRegex communitySetMatchRegex) {
    return Pattern.compile(communitySetMatchRegex.getRegex())
        .matcher(
            communitySetMatchRegex
                .getCommunitySetRendering()
                .accept(new CommunitySetRenderer(_communitySet)))
        .find();
  }

  @Override
  public Boolean visitCommunitySetNot(CommunitySetNot communitySetNot) {
    return !communitySetNot.getExpr().accept(this);
  }

  @Override
  public Boolean visitHasCommunity(HasCommunity hasCommunity) {
    return _communitySet.getCommunities().stream()
        .anyMatch(c -> hasCommunity.getExpr().accept(new CommunityMatchExprEvaluator(c, _ctx)));
  }

  private final @Nonnull CommunityContext _ctx;
  private final @Nonnull CommunitySet _communitySet;
}
