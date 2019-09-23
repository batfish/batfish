package org.batfish.datamodel.routing_policy.communities;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;

/** Visitor for evaluating a {@link CommunitySetExpr} under a {@link CommunityContext}. */
public final class CommunitySetExprEvaluator
    implements CommunitySetExprVisitor<CommunitySet, CommunityContext> {

  public static @Nonnull CommunitySetExprEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public @Nonnull CommunitySet visitInputCommunities(
      InputCommunities inputCommunities, CommunityContext arg) {
    return arg.getInputCommunitySet();
  }

  @Override
  public @Nonnull CommunitySet visitCommunitySetDifference(
      CommunitySetDifference communitySetDifference, CommunityContext arg) {
    CommunitySet initial = communitySetDifference.getInitial().accept(this, arg);
    CommunityMatchExpr removalCriterion = communitySetDifference.getRemovalCriterion();
    return CommunitySet.of(
        initial.getCommunities().stream()
            .filter(c -> removalCriterion.accept(arg.getCommunityMatchExprEvaluator(), c))
            .collect(ImmutableSet.toImmutableSet()));
  }

  @Override
  public @Nonnull CommunitySet visitCommunitySetExprReference(
      CommunitySetExprReference communitySetExprReference, CommunityContext arg) {
    CommunitySetExpr communitySetExpr =
        arg.getCommunitySetExprs().get(communitySetExprReference.getName());
    // conversion to VI should guarantee communitySetExpr is not null
    assert communitySetExpr != null;
    return communitySetExpr.accept(this, arg);
  }

  @Override
  public @Nonnull CommunitySet visitCommunitySetReference(
      CommunitySetReference communitySetReference, CommunityContext arg) {
    CommunitySet communitySet = arg.getCommunitySets().get(communitySetReference.getName());
    // conversion to VI should guarantee communitySet is not null
    assert communitySet != null;
    return communitySet;
  }

  @Override
  public @Nonnull CommunitySet visitCommunitySetUnion(
      CommunitySetUnion communitySetUnion, CommunityContext arg) {
    return CommunitySet.of(
        ImmutableSet.<Community>builder()
            .addAll(communitySetUnion.getExpr1().accept(this, arg).getCommunities())
            .addAll(communitySetUnion.getExpr2().accept(this, arg).getCommunities())
            .build());
  }

  @Override
  public @Nonnull CommunitySet visitLiteralCommunitySet(
      LiteralCommunitySet literalCommunitySet, CommunityContext arg) {
    return literalCommunitySet.getCommunitySet();
  }

  private static final CommunitySetExprEvaluator INSTANCE = new CommunitySetExprEvaluator();
}
