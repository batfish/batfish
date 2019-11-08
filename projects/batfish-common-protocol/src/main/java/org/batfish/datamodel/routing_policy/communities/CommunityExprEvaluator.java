package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.IntExprEvaluator;
import org.batfish.datamodel.routing_policy.expr.LongExprEvaluator;

/** Concretely evaluates a {@link CommunityExpr}, resulting in an {@link Community}. */
public final class CommunityExprEvaluator
    implements CommunityExprVisitor<Community, CommunityContext> {

  public static @Nonnull CommunityExprEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public Community visitRouteTargetExtendedCommunityExpr(
      RouteTargetExtendedCommunityExpr routeTargetExtendedCommunityExpr, CommunityContext arg) {
    long ga =
        routeTargetExtendedCommunityExpr.getGaExpr().accept(LongExprEvaluator.instance(), null);
    int la = routeTargetExtendedCommunityExpr.getLaExpr().accept(IntExprEvaluator.instance(), null);
    return ExtendedCommunity.target(ga, la);
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
