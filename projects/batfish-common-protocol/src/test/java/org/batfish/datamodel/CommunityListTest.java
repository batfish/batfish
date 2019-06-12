package org.batfish.datamodel;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.junit.Test;

public class CommunityListTest {

  private static final StandardCommunity COMMUNITY1 = StandardCommunity.of(1L);
  private static final StandardCommunity COMMUNITY2 = StandardCommunity.of(2L);

  private static final String NAME = "cl";

  @Test
  public void testDefaultReject() {
    CommunityList cl = new CommunityList(NAME, ImmutableList.of(), false);

    assertThat(cl.matchCommunity(null, COMMUNITY1), equalTo(false));
  }

  @Test
  public void testPermit() {
    CommunityList cl =
        new CommunityList(
            NAME,
            ImmutableList.of(
                new CommunityListLine(LineAction.PERMIT, new LiteralCommunity(COMMUNITY1))),
            false);

    assertThat(cl.matchCommunity(null, COMMUNITY1), equalTo(true));
  }

  @Test
  public void testReject() {
    CommunityList cl =
        new CommunityList(
            NAME,
            ImmutableList.of(
                new CommunityListLine(LineAction.DENY, new LiteralCommunity(COMMUNITY1))),
            false);

    assertThat(cl.matchCommunity(null, COMMUNITY1), equalTo(false));
  }

  @Test
  public void testReferences() {
    // A community list that permits COMMUNITY1
    CommunityList matchC1 =
        new CommunityList(
            "matchC1",
            ImmutableList.of(
                new CommunityListLine(LineAction.PERMIT, new LiteralCommunity(COMMUNITY1))),
            false);
    // A community list that permits things that matchC1 permits (has a reference)
    CommunityList ref =
        new CommunityList(
            "ref",
            ImmutableList.of(
                new CommunityListLine(LineAction.PERMIT, new NamedCommunitySet(matchC1.getName()))),
            false);
    // A community list that permits things that cl permits (has a transitive reference)
    CommunityList trans =
        new CommunityList(
            "trans",
            ImmutableList.of(
                new CommunityListLine(LineAction.PERMIT, new NamedCommunitySet(ref.getName()))),
            false);
    Map<String, CommunityList> cls =
        ImmutableMap.of(matchC1.getName(), matchC1, ref.getName(), ref, trans.getName(), trans);

    // The test environment has any route and a complete map of community lists.
    Environment env =
        Environment.builder(new Configuration("host", ConfigurationFormat.CISCO_IOS))
            .setOriginalRoute(new ConnectedRoute(Prefix.ZERO, "Ethernet0"))
            .setCommunityLists(cls)
            .build();

    // References work
    assertThat(ref.asLiteralCommunities(env), contains(COMMUNITY1));
    assertThat(ref.matchCommunity(env, COMMUNITY1), equalTo(true));
    assertThat(ref.matchCommunity(env, COMMUNITY2), equalTo(false));

    // Transitive references work
    assertThat(trans.asLiteralCommunities(env), contains(COMMUNITY1));
    assertThat(trans.matchCommunity(env, COMMUNITY1), equalTo(true));
    assertThat(trans.matchCommunity(env, COMMUNITY2), equalTo(false));
  }
}
