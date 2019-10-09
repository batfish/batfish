package org.batfish.datamodel.routing_policy.communities;

import static org.batfish.datamodel.routing_policy.communities.CommunityExprEvaluator.instance;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link CommunityExprEvaluator}. */
public final class CommunityExprEvaluatorTest {

  private static final CommunityContext CTX = CommunityContext.builder().build();

  @Test
  public void testVisitStandardCommunityHighLowExprs() {
    StandardCommunityHighLowExprs expr =
        new StandardCommunityHighLowExprs(new LiteralInt(1), new LiteralInt(2));
    assertThat(expr.accept(instance(), CTX), equalTo(StandardCommunity.of(1, 2)));
  }
}
