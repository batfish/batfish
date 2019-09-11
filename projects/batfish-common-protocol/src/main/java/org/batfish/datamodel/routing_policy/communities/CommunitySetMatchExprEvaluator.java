package org.batfish.datamodel.routing_policy.communities;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** A visitor for evaluating a {@link CommunitySetMatchExpr} under a {@link CommunityContext}. */
public final class CommunitySetMatchExprEvaluator
    implements CommunitySetMatchExprVisitor<Boolean, CommunitySet> {

  public CommunitySetMatchExprEvaluator(CommunityContext ctx) {
    _ctx = ctx;
  }

  @Override
  public Boolean visitCommunitySetAcl(CommunitySetAcl communitySetAcl, CommunitySet arg) {
    for (CommunitySetAclLine line : communitySetAcl.getLines()) {
      if (line.getCommunitySetMatchExpr().accept(this, arg)) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  @Override
  public Boolean visitCommunitySetMatchAll(
      CommunitySetMatchAll communitySetMatchAll, CommunitySet arg) {
    return communitySetMatchAll.getExprs().stream().allMatch(expr -> expr.accept(this, arg));
  }

  @Override
  public Boolean visitCommunitySetMatchAny(
      CommunitySetMatchAny communitySetMatchAny, CommunitySet arg) {
    return communitySetMatchAny.getExprs().stream().anyMatch(expr -> expr.accept(this, arg));
  }

  @Override
  public Boolean visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference, CommunitySet arg) {
    CommunitySetMatchExpr expr =
        _ctx.getCommunitySetMatchExprs().get(communitySetMatchExprReference.getName());
    if (expr == null) {
      return false;
    }
    return expr.accept(this, arg);
  }

  @Override
  public Boolean visitCommunitySetMatchRegex(
      CommunitySetMatchRegex communitySetMatchRegex, CommunitySet arg) {
    return Pattern.compile(communitySetMatchRegex.getRegex())
        .matcher(
            communitySetMatchRegex
                .getCommunitySetRendering()
                .accept(CommunitySetRenderer.instance(), arg))
        .find();
  }

  @Override
  public Boolean visitCommunitySetNot(CommunitySetNot communitySetNot, CommunitySet arg) {
    return !communitySetNot.getExpr().accept(this, arg);
  }

  @Override
  public Boolean visitHasCommunity(HasCommunity hasCommunity, CommunitySet arg) {
    return arg.getCommunities().stream()
        .anyMatch(c -> hasCommunity.getExpr().accept(_ctx.getCommunityMatchExprEvaluator(), c));
  }

  private final @Nonnull CommunityContext _ctx;
}
