package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.IntExprEvaluator;

/** Concretely evaluates a {@link CommunityExpr}, resulting in an {@link Community}. */
public final class CommunityExprEvaluator
    implements CommunityExprVisitor<Community, CommunityContext> {

  public static @Nonnull CommunityExprEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public Community visitStandardCommunityHighLowExprs(
      StandardCommunityHighLowExprs standardCommunityHighLowExprs, CommunityContext arg) {
    int high =
        standardCommunityHighLowExprs.getHighExpr().accept(IntExprEvaluator.instance(), null);
    int low = standardCommunityHighLowExprs.getLowExpr().accept(IntExprEvaluator.instance(), null);
    // conversion should ensure these values are in range
    return StandardCommunity.of(high, low);
  }

  private static final CommunityExprEvaluator INSTANCE = new CommunityExprEvaluator();

  private CommunityExprEvaluator() {}
}
