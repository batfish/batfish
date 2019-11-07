package org.batfish.grammar.cisco_xr;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasRoute6FilterList;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasRouteFilterList;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.DataModelMatchers.permits;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.representation.cisco_xr.CiscoXrConfiguration.computeCommunitySetMatchAnyName;
import static org.batfish.representation.cisco_xr.CiscoXrConfiguration.computeCommunitySetMatchEveryName;
import static org.batfish.representation.cisco_xr.CiscoXrStructureType.PREFIX_SET;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.WellKnownCommunity;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunityContext;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprEvaluator;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cisco_xr.OspfProcess;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link CiscoXrParser} and {@link CiscoXrControlPlaneExtractor}. */
@ParametersAreNonnullByDefault
public final class XrGrammarTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_xr/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    try {
      return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      assertThat(configs, hasKey(hostname.toLowerCase()));
      Configuration c = configs.get(hostname.toLowerCase());
      assertThat(c, hasConfigurationFormat(ConfigurationFormat.CISCO_IOS_XR));
      // Ensure that we used the CiscoXr parser.
      assertThat(c.getVendorFamily().getCiscoXr(), notNullValue());
      return c;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void assertRoutingPolicyDeniesRoute(
      RoutingPolicy routingPolicy, AbstractRoute route) {
    assertFalse(
        routingPolicy.process(
            route, Bgpv4Route.builder().setNetwork(route.getNetwork()), Direction.OUT));
  }

  private static void assertRoutingPolicyPermitsRoute(
      RoutingPolicy routingPolicy, AbstractRoute route) {
    assertTrue(
        routingPolicy.process(
            route, Bgpv4Route.builder().setNetwork(route.getNetwork()), Direction.OUT));
  }

  private static @Nonnull Bgpv4Route processRouteIn(RoutingPolicy routingPolicy, Bgpv4Route route) {
    Bgpv4Route.Builder builder = route.toBuilder();
    assertTrue(routingPolicy.process(route, builder, Direction.IN));
    return builder.build();
  }

  @Test
  public void testBanner() {
    Configuration c = parseConfig("banner");
    assertThat(
        c.getVendorFamily().getCiscoXr().getBanners(),
        equalTo(
            ImmutableMap.of(
                "exec",
                "First line.\nSecond line, with no ignored text.",
                "login",
                "First line.\nSecond line.")));
  }

  /**
   * Regression test for a parser crash related to peer stack indexing issues.
   *
   * <p>The test config is a minimized version of user configuration submitted through Batfish
   * diagnostics.
   */
  @Test
  public void testBgpNeighborCrash() {
    // Don't crash.
    parseConfig("bgp-neighbor-crash");
  }

  @Test
  public void testCommunitySet() {
    Configuration c = parseConfig("community-set");
    CommunityContext ctx = CommunityContext.builder().build();

    // Test CommunityMatchExprs
    assertThat(c.getCommunityMatchExprs(), hasKeys("universe", "mixed"));
    {
      CommunityMatchExpr expr = c.getCommunityMatchExprs().get("universe");
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(1L)));
    }
    {
      CommunityMatchExpr expr = c.getCommunityMatchExprs().get("mixed");
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(1234, 1)));
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(1, 2)));
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(2, 3)));
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(4, 5)));
      assertFalse(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(6, 99)));
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(6, 100)));
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(6, 101)));
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(6, 102)));
      assertTrue(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(6, 103)));
      assertFalse(expr.accept(ctx.getCommunityMatchExprEvaluator(), StandardCommunity.of(6, 104)));
    }

    // Test CommunitySetExprs
    assertThat(c.getCommunitySetExprs(), hasKeys("universe", "mixed"));
    {
      CommunitySetExpr expr = c.getCommunitySetExprs().get("universe");
      assertThat(
          expr.accept(CommunitySetExprEvaluator.instance(), ctx), equalTo(CommunitySet.empty()));
    }
    {
      CommunitySetExpr expr = c.getCommunitySetExprs().get("mixed");
      assertThat(
          expr.accept(CommunitySetExprEvaluator.instance(), ctx),
          equalTo(CommunitySet.of(StandardCommunity.of(1, 2))));
    }

    // Test CommunitySetMatchExprs
    assertThat(
        c.getCommunitySetMatchExprs(),
        hasKeys(
            computeCommunitySetMatchAnyName("universe"),
            computeCommunitySetMatchEveryName("universe"),
            computeCommunitySetMatchAnyName("mixed"),
            computeCommunitySetMatchEveryName("mixed")));
    {
      CommunitySetMatchExpr expr =
          c.getCommunitySetMatchExprs().get(computeCommunitySetMatchAnyName("universe"));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(5, 5), StandardCommunity.of(7, 7))));
    }
    {
      CommunitySetMatchExpr expr =
          c.getCommunitySetMatchExprs().get(computeCommunitySetMatchEveryName("universe"));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(5, 5), StandardCommunity.of(7, 7))));
    }
    {
      CommunitySetMatchExpr expr =
          c.getCommunitySetMatchExprs().get(computeCommunitySetMatchAnyName("mixed"));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(1234, 1))));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(1, 2))));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(2, 3))));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(4, 5))));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(6, 100))));
    }
    {
      CommunitySetMatchExpr expr =
          c.getCommunitySetMatchExprs().get(computeCommunitySetMatchEveryName("mixed"));
      assertFalse(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(1234, 1))));
      assertFalse(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(1, 2))));
      assertFalse(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(2, 3))));
      assertFalse(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(4, 5))));
      assertFalse(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(StandardCommunity.of(6, 100))));
      assertTrue(
          expr.accept(
              ctx.getCommunitySetMatchExprEvaluator(),
              CommunitySet.of(
                  StandardCommunity.of(1234, 1),
                  StandardCommunity.of(1, 2),
                  StandardCommunity.of(2, 3),
                  StandardCommunity.of(4, 5),
                  StandardCommunity.of(6, 100))));
    }

    // Test route-policy match and set
    assertThat(
        c.getRoutingPolicies(),
        hasKeys(
            "any",
            "every",
            "setmixed",
            "setmixedadditive",
            "deleteall",
            "deletein",
            "deleteininline",
            "deletenotin"));
    Ip origNextHopIp = Ip.parse("192.0.2.254");
    Bgpv4Route base =
        Bgpv4Route.builder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(origNextHopIp)
            .setNetwork(Prefix.ZERO)
            .setTag(0L)
            .build();
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("any");
      assertRoutingPolicyDeniesRoute(rp, base);
      Bgpv4Route routeOneMatchingCommunity =
          base.toBuilder().setCommunities(ImmutableSet.of(StandardCommunity.of(3, 3))).build();
      assertRoutingPolicyPermitsRoute(rp, routeOneMatchingCommunity);
      Bgpv4Route routeNoMatchingCommunity =
          base.toBuilder().setCommunities(ImmutableSet.of(StandardCommunity.of(9, 9))).build();
      assertRoutingPolicyDeniesRoute(rp, routeNoMatchingCommunity);
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("every");
      assertRoutingPolicyDeniesRoute(rp, base);
      Bgpv4Route routeOneMatchingCommunity =
          base.toBuilder().setCommunities(ImmutableSet.of(StandardCommunity.of(3, 3))).build();
      assertRoutingPolicyDeniesRoute(rp, routeOneMatchingCommunity);
      Bgpv4Route routeAllMatchingCommunities =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      StandardCommunity.of(1234, 1),
                      StandardCommunity.of(1, 2),
                      StandardCommunity.of(2, 3),
                      StandardCommunity.of(4, 5),
                      StandardCommunity.of(6, 100)))
              .build();
      assertRoutingPolicyPermitsRoute(rp, routeAllMatchingCommunities);
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("setmixed");
      Bgpv4Route inRoute =
          base.toBuilder().setCommunities(ImmutableSet.of(StandardCommunity.of(9, 9))).build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(route.getCommunities(), containsInAnyOrder(StandardCommunity.of(1, 2)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("setmixedadditive");
      Bgpv4Route inRoute =
          base.toBuilder().setCommunities(ImmutableSet.of(StandardCommunity.of(9, 9))).build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities(),
          containsInAnyOrder(StandardCommunity.of(1, 2), StandardCommunity.of(9, 9)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("deleteall");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(WellKnownCommunity.INTERNET)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities(),
          containsInAnyOrder(StandardCommunity.of(WellKnownCommunity.INTERNET)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("deletein");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(WellKnownCommunity.INTERNET)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(route.getCommunities(), empty());
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("deleteininline");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(WellKnownCommunity.INTERNET)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(route.getCommunities(), empty());
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("deletenotin");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(WellKnownCommunity.INTERNET)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities(),
          containsInAnyOrder(
              StandardCommunity.of(1, 1), StandardCommunity.of(WellKnownCommunity.INTERNET)));
    }
  }

  @Test
  public void testOspfInterface() {
    Configuration c = parseConfig("ospf-interface");
    String ifaceName = "Bundle-Ethernet201";
    Map<String, Interface> ifaces = c.getAllInterfaces();
    assertThat(ifaces.keySet(), contains(ifaceName));

    // Confirm the interface has the correct OSPF process and area
    assertThat(ifaces.get(ifaceName).getOspfProcess(), equalTo("2"));
    assertThat(ifaces.get(ifaceName).getOspfAreaName(), equalTo(0L));
  }

  @Test
  public void testOspfCost() {
    Configuration manual = parseConfig("ospf-cost");
    assertThat(
        manual.getDefaultVrf().getOspfProcesses().get("1").getReferenceBandwidth(), equalTo(10e6d));

    Configuration defaults = parseConfig("ospf-cost-defaults");
    assertThat(
        defaults.getDefaultVrf().getOspfProcesses().get("1").getReferenceBandwidth(),
        equalTo(OspfProcess.DEFAULT_OSPF_REFERENCE_BANDWIDTH));
  }

  @Test
  public void testPrefixSet() {
    String hostname = "prefix-set";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations().get(hostname);

    Prefix permittedPrefix = Prefix.parse("1.2.3.4/30");
    Prefix6 permittedPrefix6 = Prefix6.parse("2001::ffff:0/124");
    Prefix rejectedPrefix = Prefix.parse("1.2.4.4/30");
    Prefix6 rejectedPrefix6 = Prefix6.parse("2001::fffe:0/124");

    /*
     * Confirm the generated route filter lists permit correct prefixes and do not permit others
     */
    assertThat(c, hasRouteFilterList("pre_ipv4", permits(permittedPrefix)));
    assertThat(c, hasRouteFilterList("pre_ipv4", not(permits(rejectedPrefix))));
    assertThat(c, hasRoute6FilterList("pre_ipv6", permits(permittedPrefix6)));
    assertThat(c, hasRoute6FilterList("pre_ipv6", not(permits(rejectedPrefix6))));
    assertThat(c, hasRouteFilterList("pre_combo", permits(permittedPrefix)));
    assertThat(c, hasRouteFilterList("pre_combo", not(permits(rejectedPrefix))));
    assertThat(c, hasRoute6FilterList("pre_combo", permits(permittedPrefix6)));
    assertThat(c, hasRoute6FilterList("pre_combo", not(permits(rejectedPrefix6))));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse();
    String filename = "configs/" + hostname;
    /*
     * pre_combo should be the only prefix set without a referrer
     */
    assertThat(ccae, hasNumReferrers(filename, PREFIX_SET, "pre_ipv4", 1));
    assertThat(ccae, hasNumReferrers(filename, PREFIX_SET, "pre_ipv6", 1));
    assertThat(ccae, hasNumReferrers(filename, PREFIX_SET, "pre_combo", 0));

    /*
     * pre_undef should be the only undefined reference
     */
    assertThat(ccae, not(hasUndefinedReference(filename, PREFIX_SET, "pre_ipv4")));
    assertThat(ccae, not(hasUndefinedReference(filename, PREFIX_SET, "pre_ipv6")));
    assertThat(ccae, hasUndefinedReference(filename, PREFIX_SET, "pre_undef"));
  }

  @Test
  public void testRoutePolicyDone() {
    String hostname = "route-policy-done";
    Configuration c = parseConfig(hostname);

    Prefix permittedPrefix = Prefix.parse("1.2.3.4/32");
    Prefix permittedPrefix2 = Prefix.parse("1.2.3.5/32");
    Prefix rejectedPrefix = Prefix.parse("2.0.0.0/8");

    StaticRoute permittedRoute =
        StaticRoute.builder().setAdministrativeCost(1).setNetwork(permittedPrefix).build();
    StaticRoute permittedRoute2 =
        StaticRoute.builder().setAdministrativeCost(1).setNetwork(permittedPrefix2).build();
    StaticRoute rejectedRoute =
        StaticRoute.builder().setAdministrativeCost(1).setNetwork(rejectedPrefix).build();

    // The route-policy accepts and rejects the same prefixes.
    RoutingPolicy rp = c.getRoutingPolicies().get("rp_ip");
    assertThat(rp, notNullValue());
    assertTrue(rp.process(permittedRoute, Bgpv4Route.builder(), Direction.OUT));
    assertTrue(rp.process(permittedRoute2, Bgpv4Route.builder(), Direction.OUT));
    assertFalse(rp.process(rejectedRoute, Bgpv4Route.builder(), Direction.OUT));

    // The BGP peer export policy also accepts and rejects the same prefixes.
    BgpActivePeerConfig bgpCfg =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Prefix.parse("10.1.1.1/32"));
    assertThat(bgpCfg, notNullValue());
    RoutingPolicy bgpRpOut =
        c.getRoutingPolicies().get(bgpCfg.getIpv4UnicastAddressFamily().getExportPolicy());
    assertThat(bgpRpOut, notNullValue());

    assertTrue(bgpRpOut.process(permittedRoute, Bgpv4Route.builder(), Direction.OUT));
    assertTrue(bgpRpOut.process(permittedRoute2, Bgpv4Route.builder(), Direction.OUT));
    assertFalse(bgpRpOut.process(rejectedRoute, Bgpv4Route.builder(), Direction.OUT));
  }
}
