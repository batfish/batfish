package org.batfish.datamodel.routing_policy.communities;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
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
  public CommunitySet visitCommunityExprsSet(
      CommunityExprsSet communityExprsSet, CommunityContext arg) {
    return CommunitySet.of(
        communityExprsSet.getExprs().stream()
            .map(expr -> expr.accept(CommunityExprEvaluator.instance(), arg))
            .collect(ImmutableSet.toImmutableSet()));
  }

  @Override
  public @Nonnull CommunitySet visitCommunitySetDifference(
      CommunitySetDifference communitySetDifference, CommunityContext arg) {
    CommunitySet initial = communitySetDifference.getInitial().accept(this, arg);
    if (initial.getCommunities().isEmpty()) {
      return initial;
    }
    CommunityMatchExpr removalCriterion = communitySetDifference.getRemovalCriterion();
    boolean changed = false;
    List<Community> ret = new ArrayList<>(initial.getCommunities().size());
    for (Community c : initial.getCommunities()) {
      if (!removalCriterion.accept(arg.getCommunityMatchExprEvaluator(), c)) {
        ret.add(c);
      } else {
        changed = true;
      }
    }
    return changed ? CommunitySet.of(ret) : initial;
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
    ImmutableSet.Builder<Community> builder = ImmutableSet.builder();
    communitySetUnion
        .getExprs()
        .forEach(expr -> builder.addAll(expr.accept(this, arg).getCommunities()));
    return CommunitySet.of(builder.build());
  }

  @Override
  public @Nonnull CommunitySet visitLiteralCommunitySet(
      LiteralCommunitySet literalCommunitySet, CommunityContext arg) {
    return literalCommunitySet.getCommunitySet();
  }

  private CommunitySetExprEvaluator() {}

  private static final CommunitySetExprEvaluator INSTANCE = new CommunitySetExprEvaluator();
}
