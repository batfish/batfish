package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.asLiteralCommunities;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchAnyCommunity;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchCommunities;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchCommunity;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchedCommunities;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;

public final class EmptyCommunitySetExprTest {

  @Test
  public void testAsLiteralCommunities() {
    assertThat(
        EmptyCommunitySetExpr.INSTANCE, asLiteralCommunities(null, equalTo(ImmutableSet.of())));
  }

  @Test
  public void testMatchAnyCommunity() {
    assertThat(EmptyCommunitySetExpr.INSTANCE, not(matchAnyCommunity(null, ImmutableSet.of())));
    assertThat(EmptyCommunitySetExpr.INSTANCE, not(matchAnyCommunity(null, ImmutableSet.of(1L))));
  }

  @Test
  public void testMatchCommunities() {
    Set<Long> matchingCommunitySetCandidate1 = ImmutableSet.of(1L, 2L);
    Set<Long> matchingCommunitySetCandidate2 = ImmutableSet.of(1L);
    Set<Long> nonMatchingCommunitySetCandidate = ImmutableSet.of(2L);
    LiteralCommunity l = new LiteralCommunity(1L);

    assertThat(l, matchCommunities(null, matchingCommunitySetCandidate1));
    assertThat(l, matchCommunities(null, matchingCommunitySetCandidate2));
    assertThat(l, not(matchCommunities(null, nonMatchingCommunitySetCandidate)));
  }

  @Test
  public void testMatchCommunity() {
    assertThat(EmptyCommunitySetExpr.INSTANCE, not(matchCommunity(null, 1L)));
  }

  @Test
  public void testMatchedCommunities() {
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(
        EmptyCommunitySetExpr.INSTANCE,
        matchedCommunities(null, communityCandidates, ImmutableSet.of()));
  }
}
