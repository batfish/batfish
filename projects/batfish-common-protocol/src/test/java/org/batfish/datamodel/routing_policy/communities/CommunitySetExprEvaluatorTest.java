package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link CommunitySetExprEvaluator}. */
public final class CommunitySetExprEvaluatorTest {

  private static final CommunityContext CTX = CommunityContext.builder().build();

  private static @Nonnull CommunitySet eval(CommunitySetExpr communitySetExpr) {
    return communitySetExpr.accept(CommunitySetExprEvaluator.instance(), CTX);
  }

  @Test
  public void testVisitInputCommunities() {
    CommunitySet cs = CommunitySet.of(StandardCommunity.of(1L));
    CommunityContext ctx = CommunityContext.builder().setInputCommunitySet(cs).build();

    assertThat(
        InputCommunities.instance().accept(CommunitySetExprEvaluator.instance(), ctx), equalTo(cs));
  }

  @Test
  public void testVisitCommunityExprsSet() {
    assertThat(
        eval(
            CommunityExprsSet.of(
                new StandardCommunityHighLowExprs(new LiteralInt(1), new LiteralInt(2)))),
        equalTo(CommunitySet.of(StandardCommunity.of(1, 2))));
  }

  @Test
  public void testVisitCommunitySetDifference() {
    CommunitySet cs = CommunitySet.of(StandardCommunity.of(1L), StandardCommunity.of(2L));

    assertThat(
        eval(
            new CommunitySetDifference(
                new LiteralCommunitySet(cs), new CommunityIs(StandardCommunity.of(1L)))),
        equalTo(CommunitySet.of(StandardCommunity.of(2L))));
  }

  @Test
  public void testVisitCommunitySetExprReference() {
    CommunitySet cs = CommunitySet.of(StandardCommunity.of(1L));
    CommunityContext ctx =
        CommunityContext.builder()
            .setCommunitySetExprs(ImmutableMap.of("defined", new LiteralCommunitySet(cs)))
            .build();

    assertThat(
        new CommunitySetExprReference("defined").accept(CommunitySetExprEvaluator.instance(), ctx),
        equalTo(cs));
  }

  @Test
  public void testVisitCommunitySetReference() {
    CommunitySet cs = CommunitySet.of(StandardCommunity.of(1L));
    CommunityContext ctx =
        CommunityContext.builder().setCommunitySets(ImmutableMap.of("defined", cs)).build();

    assertThat(
        new CommunitySetReference("defined").accept(CommunitySetExprEvaluator.instance(), ctx),
        equalTo(cs));
  }

  @Test
  public void testVisitCommunitySetUnion() {
    CommunitySet cs1 = CommunitySet.of(StandardCommunity.of(1L));
    CommunitySet cs2 = CommunitySet.of(StandardCommunity.of(2L));

    assertThat(
        eval(CommunitySetUnion.of(new LiteralCommunitySet(cs1), new LiteralCommunitySet(cs2))),
        equalTo(CommunitySet.of(StandardCommunity.of(1L), StandardCommunity.of(2L))));
  }

  @Test
  public void testVisitLiteralCommunitySet() {
    CommunitySet cs = CommunitySet.of(StandardCommunity.of(1L));

    assertThat(
        eval(new LiteralCommunitySet(cs)), equalTo(CommunitySet.of(StandardCommunity.of(1L))));
  }
}
