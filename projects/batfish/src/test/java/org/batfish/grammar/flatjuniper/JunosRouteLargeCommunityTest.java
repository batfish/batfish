package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV4_UNICAST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInformationBase;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosRouteLargeCommunityTest {

  private static final String HOSTNAME = "route-large-community";
  private static final Prefix AGGREGATE_ROUTE_PREFIX = Prefix.parse("10.21.0.0/16");
  private static final Prefix GENERATED_ROUTE_PREFIX = Prefix.parse("10.31.0.0/16");
  private static final Prefix AGGREGATE_DEFAULTS_PREFIX = Prefix.parse("10.50.0.0/16");
  private static final Prefix GENERATED_DEFAULTS_PREFIX = Prefix.parse("10.60.0.0/16");

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static void assertCommunities(Iterable<Community> actual, int value) {
    assertThat(
        actual,
        containsInAnyOrder(
            StandardCommunity.of(65001, value), LargeCommunity.of(65001L, value, 1L)));
  }

  private static GeneratedRoute getConvertedRoute(Configuration configuration, Prefix prefix) {
    return configuration.getDefaultVrf().getGeneratedRoutes().stream()
        .filter(route -> route.getNetwork().equals(prefix))
        .findAny()
        .orElseThrow();
  }

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    RoutingInstance routingInstance =
        juniperConfiguration.getMasterLogicalSystem().getDefaultRoutingInstance();
    RoutingInformationBase rib = routingInstance.getRibs().get(RIB_IPV4_UNICAST);

    assertCommunities(rib.getAggregateRoutes().get(AGGREGATE_ROUTE_PREFIX).getCommunities(), 21);
    assertCommunities(rib.getGeneratedRoutes().get(GENERATED_ROUTE_PREFIX).getCommunities(), 31);
    assertCommunities(routingInstance.getAggregateRouteDefaults().getCommunities(), 50);
    assertCommunities(routingInstance.getGeneratedRouteDefaults().getCommunities(), 60);
    assertThat(rib.getAggregateRoutes().get(AGGREGATE_DEFAULTS_PREFIX).getCommunities(), empty());
    assertThat(rib.getGeneratedRoutes().get(GENERATED_DEFAULTS_PREFIX).getCommunities(), empty());

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertCommunities(
        getConvertedRoute(configuration, AGGREGATE_ROUTE_PREFIX).getCommunities().getCommunities(),
        21);
    assertCommunities(
        getConvertedRoute(configuration, GENERATED_ROUTE_PREFIX).getCommunities().getCommunities(),
        31);
    assertCommunities(
        getConvertedRoute(configuration, AGGREGATE_DEFAULTS_PREFIX)
            .getCommunities()
            .getCommunities(),
        50);
    assertCommunities(
        getConvertedRoute(configuration, GENERATED_DEFAULTS_PREFIX)
            .getCommunities()
            .getCommunities(),
        60);
  }
}
