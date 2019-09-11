package org.batfish.datamodel.routing_policy.communities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunityMatchExprEvaluator}. */
public final class CommunityMatchExprEvaluatorTest {

  private static final CommunityContext CTX = CommunityContext.builder().build();

  private static @Nonnull CommunityMatchExprEvaluator eval(Community community) {
    return new CommunityMatchExprEvaluator(community, CTX);
  }

  @Test
  public void testVisitAllExtendedCommunities() {
    assertFalse(AllExtendedCommunities.instance().accept(eval(StandardCommunity.of(1L))));
    assertTrue(AllExtendedCommunities.instance().accept(eval(ExtendedCommunity.of(1, 1L, 1L))));
    assertFalse(AllExtendedCommunities.instance().accept(eval(LargeCommunity.of(1L, 1L, 1L))));
  }

  @Test
  public void testVisitAllLargeCommunities() {
    assertFalse(AllLargeCommunities.instance().accept(eval(StandardCommunity.of(1L))));
    assertFalse(AllLargeCommunities.instance().accept(eval(ExtendedCommunity.of(1, 1L, 1L))));
    assertTrue(AllLargeCommunities.instance().accept(eval(LargeCommunity.of(1L, 1L, 1L))));
  }

  @Test
  public void testVisitAllStandardCommunities() {
    assertTrue(AllStandardCommunities.instance().accept(eval(StandardCommunity.of(1L))));
    assertFalse(AllStandardCommunities.instance().accept(eval(ExtendedCommunity.of(1, 1L, 1L))));
    assertFalse(AllStandardCommunities.instance().accept(eval(LargeCommunity.of(1L, 1L, 1L))));
  }

  @Test
  public void testVisitCommunityAcl() {
    assertFalse(new CommunityAcl(ImmutableList.of()).accept(eval(StandardCommunity.of(1L))));

    CommunityAcl denyThenPermit =
        new CommunityAcl(
            ImmutableList.of(
                new CommunityAclLine(LineAction.DENY, AllStandardCommunities.instance()),
                new CommunityAclLine(LineAction.PERMIT, AllExtendedCommunities.instance()),
                new CommunityAclLine(LineAction.PERMIT, AllStandardCommunities.instance())));
    assertFalse(denyThenPermit.accept(eval(StandardCommunity.of(1L))));
    assertTrue(denyThenPermit.accept(eval(ExtendedCommunity.of(1, 1L, 1L))));
  }

  @Test
  public void testVisitCommunityIs() {
    CommunityIs is = new CommunityIs(StandardCommunity.of(1L));

    assertTrue(is.accept(eval(StandardCommunity.of(1L))));
    assertFalse(is.accept(eval(StandardCommunity.of(2L))));
  }

  @Test
  public void testVisitCommunityMatchAll() {
    // empty permits everything
    assertTrue(new CommunityMatchAll(ImmutableList.of()).accept(eval(StandardCommunity.of(1L))));

    CommunityMatchAll twoOneMatchable =
        new CommunityMatchAll(
            ImmutableList.of(
                new CommunityIs(StandardCommunity.of(1L)),
                new CommunityIs(StandardCommunity.of(2L))));
    assertFalse(twoOneMatchable.accept(eval(StandardCommunity.of(1L))));
    assertFalse(twoOneMatchable.accept(eval(StandardCommunity.of(2L))));
    assertFalse(twoOneMatchable.accept(eval(StandardCommunity.of(3L))));

    CommunityMatchAll twoBothMatchable =
        new CommunityMatchAll(
            ImmutableList.of(
                new CommunityIs(StandardCommunity.of(1L)), AllStandardCommunities.instance()));
    assertTrue(twoBothMatchable.accept(eval(StandardCommunity.of(1L))));
  }

  @Test
  public void testVisitCommunityMatchAny() {
    // empty denies everything
    assertFalse(new CommunityMatchAny(ImmutableList.of()).accept(eval(StandardCommunity.of(1L))));

    CommunityMatchAny twoOneMatchable =
        new CommunityMatchAny(
            ImmutableList.of(
                new CommunityIs(StandardCommunity.of(1L)),
                new CommunityIs(StandardCommunity.of(2L))));
    assertTrue(twoOneMatchable.accept(eval(StandardCommunity.of(1L))));
    assertTrue(twoOneMatchable.accept(eval(StandardCommunity.of(2L))));
    assertFalse(twoOneMatchable.accept(eval(StandardCommunity.of(3L))));

    CommunityMatchAny twoBothMatchable =
        new CommunityMatchAny(
            ImmutableList.of(
                new CommunityIs(StandardCommunity.of(1L)), AllStandardCommunities.instance()));
    assertTrue(twoBothMatchable.accept(eval(StandardCommunity.of(1L))));
  }

  @Test
  public void testVisitCommunityMatchExprReference() {
    CommunityContext ctx =
        CommunityContext.builder()
            .setCommunityMatchExprs(
                ImmutableMap.of("defined", new CommunityIs(StandardCommunity.of(1L))))
            .build();

    CommunityMatchExprReference r = new CommunityMatchExprReference("defined");
    assertTrue(r.accept(new CommunityMatchExprEvaluator(StandardCommunity.of(1L), ctx)));
    assertFalse(r.accept(new CommunityMatchExprEvaluator(StandardCommunity.of(2L), ctx)));

    // undefined reference should not match
    assertFalse(
        new CommunityMatchExprReference("undefined")
            .accept(new CommunityMatchExprEvaluator(StandardCommunity.of(1L), ctx)));
  }

  @Test
  public void testVisitCommunityMatchRegex() {
    CommunityMatchRegex r = new CommunityMatchRegex(ColonSeparatedRendering.instance(), "^1:1$");
    assertTrue(r.accept(eval(StandardCommunity.of(1, 1))));
    assertFalse(r.accept(eval(StandardCommunity.of(11, 11))));
  }

  @Test
  public void testVisitCommunityNot() {
    CommunityNot not = new CommunityNot(AllStandardCommunities.instance());

    assertFalse(not.accept(eval(StandardCommunity.of(1L))));
    assertTrue(not.accept(eval(ExtendedCommunity.of(1, 1L, 1L))));
  }

  @Test
  public void testVisitRouteTargetExtendedCommunities() {
    assertTrue(
        RouteTargetExtendedCommunities.instance()
            .accept(eval(ExtendedCommunity.of(0x0002, 1L, 1L))));
    assertFalse(
        RouteTargetExtendedCommunities.instance()
            .accept(eval(ExtendedCommunity.of(0x0000, 1L, 1L))));
    assertFalse(RouteTargetExtendedCommunities.instance().accept(eval(StandardCommunity.of(1L))));
  }

  @Test
  public void testVisitSiteOfOriginExtendedCommunities() {
    assertTrue(
        SiteOfOriginExtendedCommunities.instance()
            .accept(eval(ExtendedCommunity.of(0x0003, 1L, 1L))));
    assertFalse(
        SiteOfOriginExtendedCommunities.instance()
            .accept(eval(ExtendedCommunity.of(0x0000, 1L, 1L))));
    assertFalse(SiteOfOriginExtendedCommunities.instance().accept(eval(StandardCommunity.of(1L))));
  }

  @Test
  public void testVisitVpnDistinguisherExtendedCommunities() {
    assertTrue(
        VpnDistinguisherExtendedCommunities.instance()
            .accept(eval(ExtendedCommunity.of(0x0010, 1L, 1L))));
    assertFalse(
        VpnDistinguisherExtendedCommunities.instance()
            .accept(eval(ExtendedCommunity.of(0x0000, 1L, 1L))));
    assertFalse(
        VpnDistinguisherExtendedCommunities.instance().accept(eval(StandardCommunity.of(1L))));
  }
}
