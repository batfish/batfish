package org.batfish.datamodel.routing_policy.communities;

import static org.batfish.datamodel.routing_policy.communities.CommunityExprEvaluator.instance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.junit.Test;

/** Test of {@link CommunityExprEvaluator}. */
public final class CommunityExprEvaluatorTest {

  private static final CommunityContext CTX = CommunityContext.builder().build();

  @Test
  public void testVisitRouteTargetExtendedCommunityExpr() {
    {
      // global administrator fitting within 16 bits
      RouteTargetExtendedCommunityExpr expr =
          new RouteTargetExtendedCommunityExpr(new LiteralLong(1L), new LiteralInt(1));
      assertThat(expr.accept(instance(), CTX), equalTo(ExtendedCommunity.target(1L, 1)));
    }
    {
      // global administrator requiring 32 bits
      RouteTargetExtendedCommunityExpr expr =
          new RouteTargetExtendedCommunityExpr(new LiteralLong(1000000L), new LiteralInt(1));
      assertThat(expr.accept(instance(), CTX), equalTo(ExtendedCommunity.target(1000000L, 1)));
    }
  }

  @Test
  public void testVisitStandardCommunityHighLowExprs() {
    StandardCommunityHighLowExprs expr =
        new StandardCommunityHighLowExprs(new LiteralInt(1), new LiteralInt(2));
    assertThat(expr.accept(instance(), CTX), equalTo(StandardCommunity.of(1, 2)));
  }
}
