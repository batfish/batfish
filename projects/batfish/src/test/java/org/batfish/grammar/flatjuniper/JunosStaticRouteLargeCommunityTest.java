package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV4_UNICAST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.StaticRouteV4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosStaticRouteLargeCommunityTest {

  private static final String HOSTNAME = "static-route-large-community";
  private static final Prefix PREFIX = Prefix.parse("192.0.2.0/24");
  private static final LargeCommunity COMMUNITY = LargeCommunity.of(64512L, 1L, 2L);

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    StaticRouteV4 route =
        juniperConfiguration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV4_UNICAST)
            .getStaticRoutes()
            .get(PREFIX);
    assertThat(route.getCommunities(), contains(COMMUNITY));

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    BgpActivePeerConfig peer =
        configuration
            .getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .get(Ip.parse("192.0.2.2"));
    RoutingPolicy exportPolicy =
        configuration
            .getRoutingPolicies()
            .get(peer.getIpv4UnicastAddressFamily().getExportPolicy());
    StaticRoute inputRoute = StaticRoute.testBuilder().setNetwork(PREFIX).build();
    Bgpv4Route.Builder outputRoute = Bgpv4Route.testBuilder().setNetwork(PREFIX);

    assertThat(exportPolicy.process(inputRoute, outputRoute, Direction.OUT), equalTo(true));
    assertThat(outputRoute.build().getCommunities().getCommunities(), contains(COMMUNITY));
  }
}
