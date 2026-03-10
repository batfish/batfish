package org.batfish.vendor.arista.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.matchers.ParseWarningMatchers;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.arista.representation.AristaConfiguration;
import org.batfish.vendor.arista.representation.RouteMap;
import org.batfish.vendor.arista.representation.RouteMapClause;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class AristaRouteMapTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/arista/grammar/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static @Nonnull AristaConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    AristaCombinedParser parser = new AristaCombinedParser(src, settings);
    Warnings warnings = new Warnings(true, true, true);
    AristaControlPlaneExtractor extractor =
        new AristaControlPlaneExtractor(
            src, parser, ConfigurationFormat.ARISTA, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    AristaConfiguration vendorConfiguration =
        (AristaConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    AristaConfiguration cloned = SerializationUtils.clone(vendorConfiguration);
    // restore warnings after cloning.
    cloned.setWarnings(warnings);
    return cloned;
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      String canonicalHostname = hostname.toLowerCase();
      assertThat(configs, hasKey(canonicalHostname));
      Configuration c = configs.get(canonicalHostname);
      assertThat(c, hasConfigurationFormat(ConfigurationFormat.ARISTA));
      return c;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private static void assertRoutingPolicyDeniesRoute(
      RoutingPolicy routingPolicy, AbstractRoute route) {
    assertFalse(
        routingPolicy.process(
            route, Bgpv4Route.testBuilder().setNetwork(route.getNetwork()), Direction.OUT));
  }

  private static @Nonnull Bgpv4Route processRouteIn(RoutingPolicy routingPolicy, Bgpv4Route route) {
    Bgpv4Route.Builder builder = route.toBuilder();
    assertTrue(routingPolicy.process(route, builder, Direction.IN));
    return builder.build();
  }

  @Test
  public void testRouteMap() {
    Configuration c = parseConfig("arista_route_map");
    final Ip origNextHopIp = Ip.parse("192.0.2.254");
    final Bgpv4Route baseBgpRoute =
        Bgpv4Route.testBuilder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(origNextHopIp)
            .setNetwork(Prefix.ZERO)
            .setTag(0L)
            .build();
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("SET_DISTANCE");
      assertThat(rp, notNullValue());
      Bgpv4Route processed = processRouteIn(rp, baseBgpRoute);
      assertThat(processed.getAdministrativeCost(), equalTo(180L));
    }
  }

  @Test
  public void testMisconfiguredRouteMapExtraction() {
    AristaConfiguration c = parseVendorConfig("arista_route_map_misconfigured");
    RouteMap map = c.getRouteMaps().get("MAP");
    assertThat(map, notNullValue());
    assertThat(map.getClauses(), hasKeys(10, 30, 40));
    // Undefined continue is extracted.
    RouteMapClause clause10 = map.getClauses().get(10);
    assertThat(clause10.getContinueLine().getTarget(), equalTo(20));
    // Loop continue not even extracted.
    RouteMapClause clause30 = map.getClauses().get(30);
    assertThat(clause30.getContinueLine(), nullValue());
    // Warnings
    assertThat(
        c.getWarnings().getParseWarnings(),
        hasItem(
            ParseWarningMatchers.hasComment(
                "Route-map MAP entry 30: continue 10 introduces a loop")));
  }

  @Test
  public void testMisconfiguredRouteMapConversion() {
    Configuration c = parseConfig("arista_route_map_misconfigured");
    final Bgpv4Route testRoute =
        Bgpv4Route.testBuilder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHopDiscard.instance())
            .setNetwork(Prefix.ZERO)
            .setTag(1L)
            .build();
    RoutingPolicy rp = c.getRoutingPolicies().get("MAP");
    assertThat(rp, notNullValue());
    {
      // Tag 1 is matched by first term, and the continue is ignored.
      Bgpv4Route processed = processRouteIn(rp, testRoute);
      assertThat(processed.getLocalPreference(), equalTo(75L));
    }
    {
      // Not tag 1 is not matched by first term, and the unconditional deny is applied.
      // Loop does not happen (continue 10), nor does term 40 get applied.
      assertRoutingPolicyDeniesRoute(rp, testRoute.toBuilder().setTag(0L).build());
    }
  }
}
