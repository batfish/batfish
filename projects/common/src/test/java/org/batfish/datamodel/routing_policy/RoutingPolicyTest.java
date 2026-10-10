package org.batfish.datamodel.routing_policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Test;

/** Tests of {@link RoutingPolicy}. */
public final class RoutingPolicyTest {

  /** A Junos-format device, whose policies read attributes from the output route. */
  private static Configuration junos() {
    return Configuration.builder()
        .setHostname("c")
        .setConfigurationFormat(ConfigurationFormat.FLAT_JUNIPER)
        .build();
  }

  private static StaticRoute route(long tag) {
    return StaticRoute.testBuilder()
        .setNetwork(Prefix.parse("10.0.0.0/8"))
        .setNextHop(NextHopDiscard.instance())
        .setAdministrativeCost(1)
        .setTag(tag)
        .build();
  }

  @Test
  public void testProcessReadOnlyMatchesInputTagOnJunos() {
    Configuration c = junos();
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setOwner(c)
            .setName("P")
            .addStatement(
                new If(
                    new MatchTag(IntComparator.EQ, new LiteralLong(5)),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    // A read-only evaluation has no output route; the input route's tag must be what is matched.
    assertTrue(policy.processReadOnly(route(5)));
    assertFalse(policy.processReadOnly(route(6)));
  }

  @Test
  public void testProcessReadOnlyMatchesInputCommunitiesOnJunos() {
    Configuration c = junos();
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setOwner(c)
            .setName("P")
            .addStatement(
                new If(
                    new MatchCommunities(
                        InputCommunities.instance(),
                        new CommunitySetMatchAny(
                            ImmutableList.of(
                                new HasCommunity(new CommunityIs(StandardCommunity.of(1, 1)))))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();
    org.batfish.datamodel.Bgpv4Route.Builder bgp =
        org.batfish.datamodel.Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setNextHop(NextHopDiscard.instance());
    assertTrue(
        policy.processReadOnly(
            bgp.setCommunities(CommunitySet.of(StandardCommunity.of(1, 1))).build()));
    assertFalse(policy.processReadOnly(bgp.setCommunities(CommunitySet.empty()).build()));
  }
}
