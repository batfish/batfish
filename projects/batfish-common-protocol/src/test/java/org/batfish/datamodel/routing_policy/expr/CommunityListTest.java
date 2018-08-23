package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.asLiteralCommunities;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchAnyCommunity;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchCommunities;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchCommunity;
import static org.batfish.datamodel.matchers.CommunitySetExprMatchers.matchedCommunities;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.Set;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.LineAction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class CommunityListTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testAsLiteralCommunitiesSupported() {
    CommunityList expr =
        new CommunityList(
            "", ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))), false);

    assertThat(expr, asLiteralCommunities(null, equalTo(ImmutableSet.of(1L))));
  }

  @Test
  public void testAsLiteralCommunitiesUnsupported() {
    CommunityList expr =
        new CommunityList(
            "",
            ImmutableList.of(
                CommunityListLine.accepting(new LiteralCommunityConjunction(ImmutableSet.of(1L)))),
            false);

    _thrown.expect(UnsupportedOperationException.class);
    expr.asLiteralCommunities(null);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new CommunityList(
                "", ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))), false),
            new CommunityList(
                "", ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))), false))
        .addEqualityGroup(
            new CommunityList(
                "a",
                ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))),
                false))
        .addEqualityGroup(new CommunityList("", ImmutableList.of(), false))
        .addEqualityGroup(new CommunityList("", ImmutableList.of(), true))
        .testEquals();
  }

  @Test
  public void testMatchAnyCommunity() {
    CommunityList accepting =
        new CommunityList(
            "", ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))), false);
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(accepting, matchAnyCommunity(null, communityCandidates));
  }

  @Test
  public void testMatchCommunities() {
    Set<Long> matchingCommunitySetCandidate1 = ImmutableSet.of(1L, 2L);
    Set<Long> matchingCommunitySetCandidate2 = ImmutableSet.of(1L);
    Set<Long> nonMatchingCommunitySetCandidate = ImmutableSet.of(2L);
    CommunityList accepting =
        new CommunityList(
            "", ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))), false);
    CommunityList acceptingSecond =
        new CommunityList(
            "",
            ImmutableList.of(
                new CommunityListLine(LineAction.DENY, EmptyCommunitySetExpr.INSTANCE),
                CommunityListLine.accepting(new LiteralCommunity(1L))),
            false);
    CommunityList rejecting =
        new CommunityList(
            "",
            ImmutableList.of(new CommunityListLine(LineAction.DENY, new LiteralCommunity(1L))),
            false);
    CommunityList empty = new CommunityList("", ImmutableList.of(), false);

    assertThat(accepting, matchCommunities(null, matchingCommunitySetCandidate1));
    assertThat(accepting, matchCommunities(null, matchingCommunitySetCandidate2));
    assertThat(accepting, not(matchCommunities(null, nonMatchingCommunitySetCandidate)));
    assertThat(acceptingSecond, matchCommunities(null, matchingCommunitySetCandidate1));
    assertThat(acceptingSecond, matchCommunities(null, matchingCommunitySetCandidate2));
    assertThat(acceptingSecond, not(matchCommunities(null, nonMatchingCommunitySetCandidate)));
    assertThat(rejecting, not(matchCommunities(null, matchingCommunitySetCandidate1)));
    assertThat(rejecting, not(matchCommunities(null, matchingCommunitySetCandidate2)));
    assertThat(rejecting, not(matchCommunities(null, nonMatchingCommunitySetCandidate)));
    assertThat(empty, not(matchCommunities(null, matchingCommunitySetCandidate1)));
    assertThat(empty, not(matchCommunities(null, matchingCommunitySetCandidate2)));
    assertThat(empty, not(matchCommunities(null, nonMatchingCommunitySetCandidate)));
  }

  @Test
  public void testMatchCommunity() {
    CommunityList accepting =
        new CommunityList(
            "", ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))), false);
    CommunityList acceptingSecond =
        new CommunityList(
            "",
            ImmutableList.of(
                new CommunityListLine(LineAction.DENY, EmptyCommunitySetExpr.INSTANCE),
                CommunityListLine.accepting(new LiteralCommunity(1L))),
            false);
    CommunityList rejecting =
        new CommunityList(
            "",
            ImmutableList.of(new CommunityListLine(LineAction.DENY, new LiteralCommunity(1L))),
            false);
    CommunityList empty = new CommunityList("", ImmutableList.of(), false);

    assertThat(accepting, matchCommunity(null, 1L));
    assertThat(accepting, not(matchCommunity(null, 2L)));
    assertThat(acceptingSecond, matchCommunity(null, 1L));
    assertThat(acceptingSecond, not(matchCommunity(null, 2L)));
    assertThat(rejecting, not(matchCommunity(null, 1L)));
    assertThat(rejecting, not(matchCommunity(null, 2L)));
    assertThat(empty, not(matchCommunity(null, 1L)));
    assertThat(empty, not(matchCommunity(null, 2L)));
  }

  @Test
  public void testMatchedCommunitiesSupported() {
    CommunityList accepting =
        new CommunityList(
            "", ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))), false);
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(accepting, matchedCommunities(null, communityCandidates, ImmutableSet.of(1L)));
  }

  @Test
  public void testMatchedCommunitiesUnsupported() {
    CommunityList unsupported =
        new CommunityList(
            "",
            ImmutableList.of(
                CommunityListLine.accepting(
                    new LiteralCommunityConjunction(ImmutableSet.of(1L, 2L)))),
            false);

    _thrown.expect(UnsupportedOperationException.class);
    unsupported.matchedCommunities(null, ImmutableSet.of(1L, 2L));
  }
}
