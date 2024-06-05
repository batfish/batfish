package org.batfish.datamodel.routing_policy.communities;

import static org.batfish.datamodel.routing_policy.communities.CommunityContext.fromEnvironment;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Test of {@link CommunityContext}. */
public final class CommunityContextTest {

  @Test
  public void testFromEnvironment() {
    Map<String, CommunityMatchExpr> communityMatchExprs =
        ImmutableMap.of("cme", AllStandardCommunities.instance());
    Map<String, CommunitySetExpr> communitySetExprs =
        ImmutableMap.of("cse", new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(2L))));
    Map<String, CommunitySetMatchExpr> communitySetMatchExprs =
        ImmutableMap.of("csme", new HasCommunity(AllStandardCommunities.instance()));
    Map<String, CommunitySet> communitySets =
        ImmutableMap.of("cs", CommunitySet.of(StandardCommunity.of(1L)));

    Configuration c = new Configuration("a", ConfigurationFormat.CISCO_IOS);
    c.setCommunityMatchExprs(communityMatchExprs);
    c.setCommunitySetExprs(communitySetExprs);
    c.setCommunitySetMatchExprs(communitySetMatchExprs);
    c.setCommunitySets(communitySets);

    CommunitySet outputCommunitySet = CommunitySet.of(StandardCommunity.of(1L));
    CommunitySet intermediateCommunitySet = CommunitySet.of(StandardCommunity.of(2L));
    CommunitySet inputRouteCommunitySet = CommunitySet.of(StandardCommunity.of(3L));

    CommunityContext useOutput =
        fromEnvironment(
            Environment.builder(c)
                .setUseOutputAttributes(true)
                .setOutputRoute(
                    Bgpv4Route.testBuilder().setCommunities(outputCommunitySet.getCommunities()))
                .build());

    // first check attributes from configuration
    assertThat(useOutput.getCommunityMatchExprs(), equalTo(communityMatchExprs));
    assertThat(useOutput.getCommunitySetExprs(), equalTo(communitySetExprs));
    assertThat(useOutput.getCommunitySetMatchExprs(), equalTo(communitySetMatchExprs));
    assertThat(useOutput.getCommunitySets(), equalTo(communitySets));

    // test of proper input communities
    assertThat(useOutput.getInputCommunitySet(), equalTo(outputCommunitySet));

    CommunityContext useIntermediate =
        fromEnvironment(
            Environment.builder(c)
                .setReadFromIntermediateBgpAttributes(true)
                .setIntermediateBgpAttributes(
                    Bgpv4Route.testBuilder()
                        .setCommunities(intermediateCommunitySet.getCommunities()))
                .build());
    assertThat(useIntermediate.getInputCommunitySet(), equalTo(intermediateCommunitySet));

    CommunityContext useInputRoute =
        fromEnvironment(
            Environment.builder(c)
                .setOriginalRoute(
                    Bgpv4Route.testBuilder()
                        .setCommunities(inputRouteCommunitySet.getCommunities())
                        .setNetwork(Prefix.ZERO)
                        .setOriginatorIp(Ip.ZERO)
                        .setOriginType(OriginType.INCOMPLETE)
                        .setProtocol(RoutingProtocol.BGP)
                        .build())
                .build());
    assertThat(useInputRoute.getInputCommunitySet(), equalTo(inputRouteCommunitySet));
  }
}
