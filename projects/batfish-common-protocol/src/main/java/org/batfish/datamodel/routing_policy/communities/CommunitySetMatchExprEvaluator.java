package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;
import org.batfish.common.util.PatternProvider;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.Community;

/** A visitor for evaluating a {@link CommunitySetMatchExpr} under a {@link CommunityContext}. */
public final class CommunitySetMatchExprEvaluator
    implements CommunitySetMatchExprVisitor<Boolean, CommunitySet> {

  public CommunitySetMatchExprEvaluator(CommunityContext ctx) {
    _ctx = ctx;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetAcl(CommunitySetAcl communitySetAcl, CommunitySet arg) {
    for (CommunitySetAclLine line : communitySetAcl.getLines()) {
      if (line.getCommunitySetMatchExpr().accept(this, arg)) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchAll(
      CommunitySetMatchAll communitySetMatchAll, CommunitySet arg) {
    for (CommunitySetMatchExpr expr : communitySetMatchAll.getExprs()) {
      if (!expr.accept(this, arg)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchAny(
      CommunitySetMatchAny communitySetMatchAny, CommunitySet arg) {
    for (CommunitySetMatchExpr expr : communitySetMatchAny.getExprs()) {
      if (expr.accept(this, arg)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference, CommunitySet arg) {
    CommunitySetMatchExpr expr =
        _ctx.getCommunitySetMatchExprs().get(communitySetMatchExprReference.getName());
    // conversion to VI should guarantee expr is not null
    assert expr != null;
    return expr.accept(this, arg);
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchRegex(
      CommunitySetMatchRegex communitySetMatchRegex, CommunitySet arg) {
    return PatternProvider.fromString(communitySetMatchRegex.getRegex())
        .matcher(
            communitySetMatchRegex
                .getCommunitySetRendering()
                .accept(CommunitySetToRegexInputString.instance(), arg))
        .find();
  }

  @Override
  public @Nonnull Boolean visitCommunitySetNot(CommunitySetNot communitySetNot, CommunitySet arg) {
    return !communitySetNot.getExpr().accept(this, arg);
  }

  @Override
  public @Nonnull Boolean visitHasCommunity(HasCommunity hasCommunity, CommunitySet arg) {
    for (Community c : arg.getCommunities()) {
      if (hasCommunity.getExpr().accept(_ctx.getCommunityMatchExprEvaluator(), c)) {
        return true;
      }
    }
    return false;
  }

  private final @Nonnull CommunityContext _ctx;
}
