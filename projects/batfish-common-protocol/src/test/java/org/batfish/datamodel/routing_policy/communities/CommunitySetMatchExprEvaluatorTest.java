package org.batfish.datamodel.routing_policy.communities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunitySetMatchExprEvaluator}. */
public final class CommunitySetMatchExprEvaluatorTest {

  private static final CommunityContext CTX = CommunityContext.builder().build();

  private static @Nonnull CommunitySetMatchExprEvaluator eval(CommunitySet communitySet) {
    return new CommunitySetMatchExprEvaluator(CTX, communitySet);
  }

  @Test
  public void testVisitCommunitySetAcl() {
    // default deny
    assertFalse(new CommunitySetAcl(ImmutableList.of()).accept(eval(CommunitySet.empty())));

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
            eval(CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L)))));
    assertTrue(denyThenPermit.accept(eval(CommunitySet.of(StandardCommunity.of(1L)))));
    assertFalse(denyThenPermit.accept(eval(CommunitySet.empty())));
  }

  @Test
  public void testVisitCommunitySetMatchAll() {
    // empty permits everything
    assertTrue(new CommunitySetMatchAll(ImmutableList.of()).accept(eval(CommunitySet.empty())));

    CommunitySetMatchAll twoOneMatchable =
        new CommunitySetMatchAll(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new CommunitySetNot(new HasCommunity(AllStandardCommunities.instance()))));
    assertFalse(twoOneMatchable.accept(eval(CommunitySet.empty())));
    assertFalse(twoOneMatchable.accept(eval(CommunitySet.of(StandardCommunity.of(1L)))));

    CommunitySetMatchAll twoBothMatchable =
        new CommunitySetMatchAll(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new HasCommunity(AllExtendedCommunities.instance())));
    assertTrue(
        twoBothMatchable.accept(
            eval(CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L)))));
  }

  @Test
  public void testVisitCommunitySetMatchAny() {
    // empty denies everything
    assertFalse(new CommunitySetMatchAny(ImmutableList.of()).accept(eval(CommunitySet.empty())));

    CommunitySetMatchAny twoOneMatchable =
        new CommunitySetMatchAny(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new CommunitySetNot(new HasCommunity(AllStandardCommunities.instance()))));
    assertTrue(twoOneMatchable.accept(eval(CommunitySet.empty())));
    assertTrue(twoOneMatchable.accept(eval(CommunitySet.of(StandardCommunity.of(1L)))));

    CommunitySetMatchAny twoBothMatchable =
        new CommunitySetMatchAny(
            ImmutableList.of(
                new HasCommunity(AllStandardCommunities.instance()),
                new HasCommunity(AllExtendedCommunities.instance())));
    assertTrue(
        twoBothMatchable.accept(
            eval(CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L)))));
    assertFalse(twoBothMatchable.accept(eval(CommunitySet.empty())));
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
            new CommunitySetMatchExprEvaluator(ctx, CommunitySet.of(StandardCommunity.of(1L)))));
    assertFalse(r.accept(new CommunitySetMatchExprEvaluator(ctx, CommunitySet.empty())));

    // undefined reference should not match
    assertFalse(
        new CommunitySetMatchExprReference("undefined")
            .accept(new CommunitySetMatchExprEvaluator(ctx, CommunitySet.empty())));
  }

  @Test
  public void testVisitCommunitySetMatchRegex() {
    CommunitySetMatchRegex r =
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()), "^1:1 2:2$");
    assertTrue(
        r.accept(eval(CommunitySet.of(StandardCommunity.of(1, 1), StandardCommunity.of(2, 2)))));
    assertFalse(r.accept(eval(CommunitySet.of(StandardCommunity.of(1, 1)))));
  }

  @Test
  public void testVisitCommunitySetNot() {
    CommunitySetNot not = new CommunitySetNot(new HasCommunity(AllStandardCommunities.instance()));

    assertFalse(not.accept(eval(CommunitySet.of(StandardCommunity.of(1L)))));
    assertTrue(not.accept(eval(CommunitySet.empty())));
  }

  @Test
  public void testVisitHasCommunity() {
    HasCommunity hc = new HasCommunity(AllStandardCommunities.instance());

    assertTrue(hc.accept(eval(CommunitySet.of(StandardCommunity.of(1L)))));
    assertTrue(
        hc.accept(
            eval(CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.of(1, 1L, 1L)))));
    assertFalse(hc.accept(eval(CommunitySet.empty())));
  }
}
