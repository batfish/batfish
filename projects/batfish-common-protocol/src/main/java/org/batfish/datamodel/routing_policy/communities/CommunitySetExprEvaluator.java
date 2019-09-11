package org.batfish.datamodel.routing_policy.communities;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;

/** Visitor for evaluating a {@link CommunitySetExpr} under a {@link CommunityContext}. */
public final class CommunitySetExprEvaluator implements CommunitySetExprVisitor<CommunitySet> {

  public CommunitySetExprEvaluator(CommunityContext ctx) {
    _ctx = ctx;
  }

  @Override
  public CommunitySet visitInputCommunities(InputCommunities inputCommunities) {
    return _ctx.getInputCommunitySet();
  }

  @Override
  public CommunitySet visitCommunitySetDifference(CommunitySetDifference communitySetDifference) {
    CommunitySet initial = communitySetDifference.getInitial().accept(this);
    CommunityMatchExpr removalCriterion = communitySetDifference.getRemovalCriterion();
    return CommunitySet.of(
        initial.getCommunities().stream()
            .filter(c -> removalCriterion.accept(_ctx.getCommunityMatchExprEvaluator(), c))
            .collect(ImmutableSet.toImmutableSet()));
  }

  @Override
  public CommunitySet visitCommunitySetExprReference(
      CommunitySetExprReference communitySetExprReference) {
    CommunitySetExpr communitySetExpr =
        _ctx.getCommunitySetExprs().get(communitySetExprReference.getName());
    if (communitySetExpr == null) {
      return CommunitySet.empty();
    }
    return communitySetExpr.accept(this);
  }

  @Override
  public CommunitySet visitCommunitySetReference(CommunitySetReference communitySetReference) {
    CommunitySet communitySet = _ctx.getCommunitySets().get(communitySetReference.getName());
    if (communitySet == null) {
      return CommunitySet.empty();
    }
    return communitySet;
  }

  @Override
  public CommunitySet visitCommunitySetUnion(CommunitySetUnion communitySetUnion) {
    return CommunitySet.of(
        ImmutableSet.<Community>builder()
            .addAll(communitySetUnion.getExpr1().accept(this).getCommunities())
            .addAll(communitySetUnion.getExpr2().accept(this).getCommunities())
            .build());
  }

  @Override
  public CommunitySet visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet) {
    return literalCommunitySet.getCommunitySet();
  }

  private final @Nonnull CommunityContext _ctx;
}
