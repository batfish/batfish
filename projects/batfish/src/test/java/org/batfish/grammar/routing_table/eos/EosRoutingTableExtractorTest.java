package org.batfish.grammar.routing_table.eos;

import static org.batfish.datamodel.matchers.RouteMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.RouteMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.RouteMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link EosRoutingTableExtractor}. */
public final class EosRoutingTableExtractorTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  private static final String NODE1 = "NODE1";
  private static final String NODE2 = "NODE2";

  private RoutesByVrf testNextHop_RoutesByVrf() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_NX);
    Configuration c1 = cb.setHostname(NODE1).build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    nf.interfaceBuilder()
        .setOwner(c1)
        .setVrf(v1)
        .setActive(true)
        .setAddresses(new InterfaceAddress("1.2.3.5/32"), new InterfaceAddress("1.2.3.6/32"))
        .build();

    Configuration c2 = cb.setHostname(NODE2).build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(v2)
        .setActive(true)
        .setAddress(new InterfaceAddress("1.2.3.6/32"))
        .build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2), temp);

    GrammarSettings settings = new MockGrammarSettings(true, 0, 0, 0, false, false, true, true);
    Warnings warnings = new Warnings();

    String showIpRoute =
        CommonUtil.readResource("org/batfish/grammar/routing_table/eos/test_next_hop.txt");

    EosRoutingTableCombinedParser parser = new EosRoutingTableCombinedParser(showIpRoute, settings);
    EosRoutingTableExtractor extractor =
        new EosRoutingTableExtractor("hostname", showIpRoute, parser, warnings, batfish);

    ParserRuleContext tree = Batfish.parse(parser, batfish.getLogger(), new Settings());
    extractor.processParseTree(tree);
    return extractor.getRoutesByVrf();
  }

  @Test
  public void testNextHop() throws IOException {
    RoutesByVrf routesByVrf = testNextHop_RoutesByVrf();
    Set<Route> defaultVrfRoutes = routesByVrf.get("default");
    assertThat(defaultVrfRoutes, hasSize(3));

    // nhip 1.2.3.4 has no owners
    assertThat(
        defaultVrfRoutes,
        hasItem(
            allOf(
                hasPrefix(Prefix.parse("1.2.3.4/0")),
                hasNextHopIp(equalTo(new Ip("1.2.3.4"))),
                hasNextHop(equalTo(Route.UNSET_NEXT_HOP)))));

    // nhip 1.2.3.5 has a single owner
    assertThat(
        defaultVrfRoutes,
        hasItem(
            allOf(
                hasPrefix(Prefix.parse("1.2.3.4/24")),
                hasNextHopIp(equalTo(new Ip("1.2.3.5"))),
                hasNextHop(equalTo(NODE1)))));

    // nhip 1.2.3.6 has two owners
    assertThat(
        defaultVrfRoutes,
        hasItem(
            allOf(
                hasPrefix(Prefix.parse("1.2.3.4/32")),
                hasNextHopIp(equalTo(new Ip("1.2.3.6"))),
                hasNextHop(equalTo(Route.AMBIGUOUS_NEXT_HOP)))));
  }
}
