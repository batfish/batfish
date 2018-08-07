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

public final class LiteralCommunitySetTest {

  @Test
  public void testAsLiteralCommunities() {
    LiteralCommunitySet l = new LiteralCommunitySet(ImmutableSet.of(1L, 2L));

    assertThat(l, asLiteralCommunities(null, equalTo(ImmutableSet.of(1L, 2L))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new LiteralCommunitySet(ImmutableSet.of(1L, 2L)),
            new LiteralCommunitySet(ImmutableSet.of(2L, 1L)))
        .addEqualityGroup(new LiteralCommunitySet(ImmutableSet.of(2L)))
        .testEquals();
  }

  @Test
  public void testMatchAnyCommunity() {
    LiteralCommunitySet l = new LiteralCommunitySet(ImmutableSet.of(1L, 3L));
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(l, matchAnyCommunity(null, communityCandidates));
  }

  @Test
  public void testMatchCommunities() {
    Set<Long> matchingCommunitySetCandidate1 = ImmutableSet.of(1L, 2L);
    Set<Long> matchingCommunitySetCandidate2 = ImmutableSet.of(1L);
    Set<Long> nonMatchingCommunitySetCandidate = ImmutableSet.of(2L);
    LiteralCommunitySet l = new LiteralCommunitySet(ImmutableSet.of(1L, 3L));

    assertThat(l, matchCommunities(null, matchingCommunitySetCandidate1));
    assertThat(l, matchCommunities(null, matchingCommunitySetCandidate2));
    assertThat(l, not(matchCommunities(null, nonMatchingCommunitySetCandidate)));
  }

  @Test
  public void testMatchCommunity() {
    LiteralCommunitySet l = new LiteralCommunitySet(ImmutableSet.of(1L, 3L));

    assertThat(l, matchCommunity(null, 1L));
    assertThat(l, not(matchCommunity(null, 2L)));
  }

  @Test
  public void testMatchedCommunities() {
    LiteralCommunitySet l = new LiteralCommunitySet(ImmutableSet.of(1L, 3L));
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(l, matchedCommunities(null, communityCandidates, ImmutableSet.of(1L)));
  }
}
