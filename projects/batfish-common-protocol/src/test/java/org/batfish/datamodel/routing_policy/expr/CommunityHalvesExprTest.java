package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.common.util.CommonUtil.communityStringToLong;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchAnyCommunity;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchCommunities;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchCommunity;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchedCommunities;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommunityHalvesExprTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testAsLiteralCommunities() {
    CommunityHalvesExpr r =
        new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(2));

    _thrown.expect(UnsupportedOperationException.class);
    r.asLiteralCommunities(null);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(2)),
            new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(2)))
        .addEqualityGroup(
            new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(3)))
        .testEquals();
  }

  @Test
  public void testMatchAnyCommunity() {
    CommunityHalvesExpr expr =
        new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(1));
    Set<Long> communityCandidates = ImmutableSet.of(communityStringToLong("1:1"), 2L);

    assertThat(expr, matchAnyCommunity(null, communityCandidates));
  }

  @Test
  public void testMatchCommunities() {
    Set<Long> matchingCommunitySetCandidate1 =
        ImmutableSet.of(communityStringToLong("1:1"), communityStringToLong("2:2"));
    Set<Long> matchingCommunitySetCandidate2 = ImmutableSet.of(communityStringToLong("1:1"));
    Set<Long> nonMatchingCommunitySetCandidate = ImmutableSet.of(communityStringToLong("2:2"));
    CommunityHalvesExpr expr =
        new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(1));

    assertThat(expr, matchCommunities(null, matchingCommunitySetCandidate1));
    assertThat(expr, matchCommunities(null, matchingCommunitySetCandidate2));
    assertThat(expr, not(matchCommunities(null, nonMatchingCommunitySetCandidate)));
  }

  @Test
  public void testMatchCommunity() {
    CommunityHalvesExpr expr =
        new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(1));

    assertThat(expr, matchCommunity(null, communityStringToLong("1:1")));
    assertThat(expr, not(matchCommunity(null, 2L)));
  }

  @Test
  public void testMatchedCommunities() {
    CommunityHalvesExpr expr =
        new CommunityHalvesExpr(new LiteralCommunityHalf(1), new LiteralCommunityHalf(1));
    Set<Long> communityCandidates = ImmutableSet.of(communityStringToLong("1:1"), 2L);

    assertThat(
        expr,
        matchedCommunities(
            null, communityCandidates, ImmutableSet.of(communityStringToLong("1:1"))));
  }
}
