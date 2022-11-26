package org.batfish.datamodel.routing_policy.communities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprEvaluator.RegexCacheKey;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link CommunitySetMatchExprEvaluator}. */
public final class CommunitySetMatchExprEvaluatorTest {

  private static final CommunitySetMatchExprEvaluator EVAL =
      CommunityContext.builder().build().getCommunitySetMatchExprEvaluator();

  @Test
  public void testVisitCommunitySetAcl() {
    // default deny
    assertFalse(new CommunitySetAcl(ImmutableList.of()).accept(EVAL, CommunitySet.empty()));

    CommunitySetAcl denyThenPermit =
        new CommunitySetAcl(
            ImmutableList.of(
                new CommunitySetAclLine(
                    LineAction.DENY,
                    new CommunitySetMatchAll(
                        ImmutableList.of(
                            new HasCommunity(AllStandardCommunities.instance()),
                            new HasCommunity(AllExtendedCommunities.instance())))),
                new CommunitySetAclLine(
                    LineAction.PERMIT, new HasCommunity(AllStandardCommunities.instance()))));
    assertFalse(
        denyThenPermit.accept(
            EVAL, CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L))));
    assertTrue(denyThenPermit.accept(EVAL, CommunitySet.of(StandardCommunity.of(1L))));
    assertFalse(denyThenPermit.accept(EVAL, CommunitySet.empty()));
  }

  @Test
  public void testVisitCommunitySetMatchAll() {
    // empty permits everything
    assertTrue(new CommunitySetMatchAll(ImmutableList.of()).accept(EVAL, CommunitySet.empty()));

    CommunitySetMatchAll twoOneMatchable =
        new CommunitySetMatchAll(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new CommunitySetNot(new HasCommunity(AllStandardCommunities.instance()))));
    assertFalse(twoOneMatchable.accept(EVAL, CommunitySet.empty()));
    assertFalse(twoOneMatchable.accept(EVAL, CommunitySet.of(StandardCommunity.of(1L))));

    CommunitySetMatchAll twoBothMatchable =
        new CommunitySetMatchAll(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new HasCommunity(AllExtendedCommunities.instance())));
    assertTrue(
        twoBothMatchable.accept(
            EVAL, CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L))));
  }

  @Test
  public void testVisitCommunitySetMatchAny() {
    // empty denies everything
    assertFalse(new CommunitySetMatchAny(ImmutableList.of()).accept(EVAL, CommunitySet.empty()));

    CommunitySetMatchAny twoOneMatchable =
        new CommunitySetMatchAny(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new CommunitySetNot(new HasCommunity(AllStandardCommunities.instance()))));
    assertTrue(twoOneMatchable.accept(EVAL, CommunitySet.empty()));
    assertTrue(twoOneMatchable.accept(EVAL, CommunitySet.of(StandardCommunity.of(1L))));

    CommunitySetMatchAny twoBothMatchable =
        new CommunitySetMatchAny(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new HasCommunity(AllExtendedCommunities.instance())));
    assertTrue(
        twoBothMatchable.accept(
            EVAL, CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L))));
    assertFalse(twoBothMatchable.accept(EVAL, CommunitySet.empty()));
  }

  @Test
  public void testVisitCommunitySetMatchExprReference() {
    CommunityContext ctx =
        CommunityContext.builder()
            .setCommunitySetMatchExprs(
                ImmutableMap.of("defined", new HasCommunity(AllStandardCommunities.instance())))
            .build();

    CommunitySetMatchExprReference r = new CommunitySetMatchExprReference("defined");
    assertTrue(
        r.accept(
            ctx.getCommunitySetMatchExprEvaluator(), CommunitySet.of(StandardCommunity.of(1L))));
    assertFalse(r.accept(ctx.getCommunitySetMatchExprEvaluator(), CommunitySet.empty()));
  }

  @Test
  public void testVisitCommunitySetMatchRegex() {
    CommunitySetMatchRegex r =
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()), "^1:1 2:2$");
    assertTrue(
        r.accept(EVAL, CommunitySet.of(StandardCommunity.of(1, 1), StandardCommunity.of(2, 2))));
    assertFalse(r.accept(EVAL, CommunitySet.of(StandardCommunity.of(1, 1))));
  }

  @Test
  public void testVisitCommunitySetNot() {
    CommunitySetNot not = new CommunitySetNot(new HasCommunity(AllStandardCommunities.instance()));

    assertFalse(not.accept(EVAL, CommunitySet.of(StandardCommunity.of(1L))));
    assertTrue(not.accept(EVAL, CommunitySet.empty()));
  }

  @Test
  public void testVisitHasCommunity() {
    HasCommunity hc = new HasCommunity(AllStandardCommunities.instance());

    assertTrue(hc.accept(EVAL, CommunitySet.of(StandardCommunity.of(1L))));
    assertTrue(
        hc.accept(
            EVAL, CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L))));
    assertFalse(hc.accept(EVAL, CommunitySet.empty()));
  }

  @Test
  public void testVisitHasSize() {
    HasSize hc = new HasSize(new IntComparison(IntComparator.LE, new LiteralInt(2)));
    StandardCommunity c1 = StandardCommunity.of(1);
    StandardCommunity c2 = StandardCommunity.of(2);
    StandardCommunity c3 = StandardCommunity.of(3);
    ExtendedCommunity ec = ExtendedCommunity.target(4, 5);

    // Accepts anything with 0-2 communities (including mixed types).
    for (CommunitySet cs :
        ImmutableList.of(
            CommunitySet.of(),
            CommunitySet.of(c1),
            CommunitySet.of(c1, c2),
            CommunitySet.of(c1, ec))) {
      assertTrue(hc.accept(EVAL, cs));
    }

    // Rejects 3 communities (including mixed type)
    assertFalse(hc.accept(EVAL, CommunitySet.of(c1, c2, c3)));
    assertFalse(hc.accept(EVAL, CommunitySet.of(c1, c2, ec)));
  }

  @Test
  public void testRegexCacheKey() {
    CommunitySetRendering rendering =
        new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance());
    CommunitySetMatchRegex regex1 = new CommunitySetMatchRegex(rendering, "regex");
    CommunitySetMatchRegex regex2 = new CommunitySetMatchRegex(rendering, "regex2");
    CommunitySet set1 = CommunitySet.empty();
    CommunitySet set2 = CommunitySet.of(StandardCommunity.ACCEPT_OWN);
    new EqualsTester()
        .addEqualityGroup(new RegexCacheKey(regex1, set1), new RegexCacheKey(regex1, set1))
        .addEqualityGroup(new RegexCacheKey(regex2, set1))
        .addEqualityGroup(new RegexCacheKey(regex2, set2))
        .testEquals();
  }
}
