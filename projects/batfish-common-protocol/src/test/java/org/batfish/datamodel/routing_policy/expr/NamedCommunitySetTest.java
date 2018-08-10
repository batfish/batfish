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
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class NamedCommunitySetTest {

  private static final String COMMUNITY_LIST_NAME = "referent";

  private Environment _env;

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    CommunityList referent =
        new CommunityList(
            COMMUNITY_LIST_NAME,
            ImmutableList.of(CommunityListLine.accepting(new LiteralCommunity(1L))),
            false);
    c.getCommunityLists().put(COMMUNITY_LIST_NAME, referent);
    nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    _env = Environment.builder(c).setVrf(Configuration.DEFAULT_VRF_NAME).build();
  }

  @Test
  public void testAsLiteralCommunities() {
    long val = 1L;
    NamedCommunitySet expr = new NamedCommunitySet(COMMUNITY_LIST_NAME);

    assertThat(expr, asLiteralCommunities(_env, equalTo(ImmutableSet.of(val))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new NamedCommunitySet(COMMUNITY_LIST_NAME), new NamedCommunitySet(COMMUNITY_LIST_NAME))
        .addEqualityGroup(new NamedCommunitySet("other"))
        .testEquals();
  }

  @Test
  public void testMatchAnyCommunity() {
    NamedCommunitySet expr = new NamedCommunitySet(COMMUNITY_LIST_NAME);
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(expr, matchAnyCommunity(_env, communityCandidates));
  }

  @Test
  public void testMatchCommunities() {
    Set<Long> matchingCommunitySetCandidate1 = ImmutableSet.of(1L, 2L);
    Set<Long> matchingCommunitySetCandidate2 = ImmutableSet.of(1L);
    Set<Long> nonMatchingCommunitySetCandidate = ImmutableSet.of(2L);
    NamedCommunitySet expr = new NamedCommunitySet(COMMUNITY_LIST_NAME);

    assertThat(expr, matchCommunities(_env, matchingCommunitySetCandidate1));
    assertThat(expr, matchCommunities(_env, matchingCommunitySetCandidate2));
    assertThat(expr, not(matchCommunities(_env, nonMatchingCommunitySetCandidate)));
  }

  @Test
  public void testMatchCommunity() {
    NamedCommunitySet expr = new NamedCommunitySet(COMMUNITY_LIST_NAME);

    assertThat(expr, matchCommunity(_env, 1L));
    assertThat(expr, not(matchCommunity(_env, 2L)));
  }

  @Test
  public void testMatchedCommunities() {
    NamedCommunitySet expr = new NamedCommunitySet(COMMUNITY_LIST_NAME);
    Set<Long> communityCandidates = ImmutableSet.of(1L, 2L);

    assertThat(expr, matchedCommunities(_env, communityCandidates, ImmutableSet.of(1L)));
  }
}
