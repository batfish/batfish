package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV4_UNICAST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Iterables;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.AggregateRoute;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosAggregateRouteLargeCommunityTest {

  private static final String HOSTNAME = "aggregate-route-large-community";
  private static final Prefix PREFIX = Prefix.parse("192.0.2.0/24");
  private static final LargeCommunity COMMUNITY = LargeCommunity.of(64512L, 1L, 2L);

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    AggregateRoute route =
        juniperConfiguration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV4_UNICAST)
            .getAggregateRoutes()
            .get(PREFIX);
    assertThat(route.getCommunities(), contains(COMMUNITY));

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    GeneratedRoute generatedRoute =
        Iterables.getOnlyElement(configuration.getDefaultVrf().getGeneratedRoutes());
    assertThat(generatedRoute.getNetwork(), equalTo(PREFIX));
    assertThat(generatedRoute.getCommunities().getCommunities(), contains(COMMUNITY));
  }
}
