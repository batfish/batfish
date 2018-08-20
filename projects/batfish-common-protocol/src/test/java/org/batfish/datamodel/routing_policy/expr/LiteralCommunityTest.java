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
import com.google.common.testing.EqualsTester;
import java.util.Set;
import org.junit.Test;

public final class LiteralCommunityTest {

  @Test
  public void testAsLiteralCommunities() {
    long val = 1L;
    LiteralCommunity l = new LiteralCommunity(val);

    assertThat(l, asLiteralCommunities(null, equalTo(ImmutableSet.of(val))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new LiteralCommunity(1L), new LiteralCommunity(1L))
        .addEqualityGroup(new LiteralCommunity(2L))
        .testEquals();
  }

  @Test
  public void testMatchAnyCommunity() {
    LiteralCommunity l = new LiteralCommunity(1L);
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(l, matchAnyCommunity(null, communityCandidates));
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
    LiteralCommunity l = new LiteralCommunity(1L);

    assertThat(l, matchCommunity(null, 1L));
    assertThat(l, not(matchCommunity(null, 2L)));
  }

  @Test
  public void testMatchedCommunities() {
    LiteralCommunity l = new LiteralCommunity(1L);
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(l, matchedCommunities(null, communityCandidates, ImmutableSet.of(1L)));
  }
}
