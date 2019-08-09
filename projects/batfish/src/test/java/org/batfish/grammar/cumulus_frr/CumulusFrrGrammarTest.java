package org.batfish.grammar.cumulus_frr;

import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.main.Batfish;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.RouteMap;
import org.batfish.representation.cumulus.RouteMapEntry;
import org.batfish.representation.cumulus.StaticRoute;
import org.batfish.representation.cumulus.Vrf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link CumulusFrrParser}. */
public class CumulusFrrGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cumulus_frr/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static CumulusNcluConfiguration parseVendorConfig(String filename) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    return parseVendorConfig(filename, settings);
  }

  private static CumulusNcluConfiguration parseVendorConfig(String filename, Settings settings) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + filename);
    return parseFromTextWithSettings(src, settings);
  }

  private static CumulusNcluConfiguration parse(String src) {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);

    return parseFromTextWithSettings(src, settings);
  }

  @Nonnull
  private static CumulusNcluConfiguration parseFromTextWithSettings(String src, Settings settings) {
    CumulusNcluConfiguration configuration = new CumulusNcluConfiguration();
    CumulusFrrCombinedParser parser = new CumulusFrrCombinedParser(src, settings, 1, 0);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    CumulusFrrConfigurationBuilder cb = new CumulusFrrConfigurationBuilder(configuration);
    walker.walk(cb, tree);
    return cb.getVendorConfiguration();
  }

  @Test
  public void testCumulusFrrVrf() {
    CumulusNcluConfiguration config = parse("vrf NAME\n exit-vrf");
    assertThat(config.getVrfs().keySet(), equalTo(ImmutableSet.of("NAME")));
  }

  @Test
  public void testCumulusFrrVrfVni() {
    CumulusNcluConfiguration config = parse("vrf NAME\n vni 170000\n exit-vrf");
    Vrf vrf = config.getVrfs().get("NAME");
    assertThat(vrf.getVni(), equalTo(170000));
  }

  @Test
  public void testCumulusFrrVrfIpRoutes() {
    CumulusNcluConfiguration config =
        parse("vrf NAME\n ip route 1.0.0.0/8 10.0.2.1\n ip route 0.0.0.0/0 10.0.0.1\n exit-vrf");
    assertThat(
        config.getVrfs().get("NAME").getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                new StaticRoute(Prefix.parse("1.0.0.0/8"), Ip.parse("10.0.2.1"), null),
                new StaticRoute(Prefix.parse("0.0.0.0/0"), Ip.parse("10.0.0.1"), null))));
  }

  @Test
  public void testCumulusFrrVrfRouteMap() {
    String name = "ROUTE-MAP-NAME";
    CumulusNcluConfiguration config =
        parse(String.format("route-map %s permit 10\nroute-map %s deny 20\n", name, name));
    assertThat(config.getRouteMaps().keySet(), equalTo(ImmutableSet.of(name)));

    RouteMap rm = config.getRouteMaps().get(name);
    assertThat(rm.getEntries().keySet(), equalTo(ImmutableSet.of(10, 20)));

    RouteMapEntry entry1 = rm.getEntries().get(10);
    assertThat(entry1.getAction(), equalTo(LineAction.PERMIT));

    RouteMapEntry entry2 = rm.getEntries().get(20);
    assertThat(entry2.getAction(), equalTo(LineAction.DENY));
  }

  @Test
  public void testCumulusFrrVrfRouteMapDescription() {
    String name = "ROUTE-MAP-NAME";
    String description = "PERmit Xxx Yy_+!@#$%^&*()";

    CumulusNcluConfiguration config =
        parse(String.format("route-map %s permit 10\ndescription %s\n", name, description));

    RouteMap rm = config.getRouteMaps().get(name);
    RouteMapEntry entry1 = rm.getEntries().get(10);
    assertThat(entry1.getDescription(), equalTo(description));
  }

  @Test
  public void testCumulusFrrVrfRouteMapMatchCommunity() {
    String name = "ROUTE-MAP-NAME";

    CumulusNcluConfiguration config =
        parse(String.format("route-map %s permit 10\nmatch community CN1 CN2\n", name));

    RouteMapEntry entry = config.getRouteMaps().get(name).getEntries().get(10);
    assertThat(
        entry.getMatchCommunity().getNames(), equalTo(ImmutableList.of("CN1", "CN2")));
  }
}
