package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.junit.Test;

public class PsFromCommunityTest {
  @Test
  public void testFrom() {
    PsFromCommunity undefined = new PsFromCommunity("undefined");
    PsFromCommunity c1 = new PsFromCommunity("c1");
    PsFromCommunity c2 = new PsFromCommunity("c2");
    Configuration c =
        Configuration.builder()
            .setHostname("ps-from-community-test")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    c.getCommunitySetMatchExprs()
        .put("c1", new HasCommunity(new CommunityIs(StandardCommunity.of(1))));
    c.getCommunitySetMatchExprs()
        .put("c2", new HasCommunity(new CommunityIs(StandardCommunity.of(2))));

    assertThat(
        PsFromCommunity.groupToMatchCommunities(c, ImmutableList.of(undefined)),
        equalTo(BooleanExprs.FALSE));

    assertThat(
        PsFromCommunity.groupToMatchCommunities(c, ImmutableList.of(c1)),
        equalTo(
            new MatchCommunities(
                InputCommunities.instance(), new CommunitySetMatchExprReference("c1"))));

    assertThat(
        PsFromCommunity.groupToMatchCommunities(c, ImmutableList.of(c1, c2)),
        equalTo(
            new MatchCommunities(
                InputCommunities.instance(),
                new CommunitySetMatchAny(
                    ImmutableList.of(
                        new CommunitySetMatchExprReference("c1"),
                        new CommunitySetMatchExprReference("c2"))))));
  }
}
