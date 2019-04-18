package org.batfish.grammar.f5_bigip_imish;

import static org.batfish.common.util.CommonUtil.communityStringToLong;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasDescription;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalIp;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasActiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEquivalentAsPathMatchMode;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isBgpRouteThat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDefaultVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.KernelRouteMatchers.isKernelRouteThat;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.computeAccessListRouteFilterName;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.BGP_NEIGHBOR;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.BGP_PROCESS;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.ROUTE_MAP;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDPrefix;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredCombinedParser;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
import org.batfish.vendor.VendorConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class F5BigipImishGrammarTest {
  private static final String SNAPSHOTS_PREFIX = "org/batfish/grammar/f5_bigip_imish/snapshots/";
  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/grammar/f5_bigip_imish/testconfigs/";

  private BDDPrefix _bddPrefix;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private void initBddPrefix() {
    BDDPacket packet = new BDDPacket();
    _bddPrefix = new BDDPrefix(packet.getDstIp(), packet.getDscp());
  }

  private BgpRoute.Builder makeBgpOutputRouteBuilder() {
    return BgpRoute.builder()
        .setNetwork(Prefix.ZERO)
        .setOriginType(OriginType.INCOMPLETE)
        .setOriginatorIp(Ip.ZERO)
        .setProtocol(RoutingProtocol.BGP);
  }

  private Configuration parseConfig(String hostname) throws IOException {
    return parseTextConfigs(hostname).get(hostname.toLowerCase());
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  private @Nonnull F5BigipConfiguration parseVendorConfig(String hostname) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + hostname);
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);
    F5BigipStructuredCombinedParser parser = new F5BigipStructuredCombinedParser(src, settings);
    ParseTreeSentences pts = new ParseTreeSentences();
    F5BigipStructuredControlPlaneExtractor extractor =
        new F5BigipStructuredControlPlaneExtractor(
            src,
            parser,
            new Warnings(),
            String.format("configs/%s", hostname),
            () -> pts,
            settings.getPrintParseTreeLineNums());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(tree);
    assertThat(
        String.format("Ensure '%s' was successfully parsed", hostname),
        extractor.getVendorConfiguration(),
        notNullValue());
    VendorConfiguration vc = extractor.getVendorConfiguration();
    assertThat(vc, instanceOf(F5BigipConfiguration.class));
    return (F5BigipConfiguration) vc;
  }

  @Test
  public void testAccessList() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_access_list");
    String acl1Name = "acl1";
    String acl2Name = "acl2";

    // setup
    IpAccessListToBdd toBDD = toBDD();

    // acl1
    assertThat(c.getIpAccessLists(), hasKey(acl1Name));

    IpAccessList acl1 = c.getIpAccessLists().get(acl1Name);

    // acl1 should permit everything
    assertTrue(toBDD.toBdd(acl1).isOne());

    // acl2
    assertThat(c.getIpAccessLists(), hasKey(acl2Name));

    IpAccessList acl2 = c.getIpAccessLists().get(acl2Name);

    assertThat(
        "acl2 permits packets with source IP in 192.0.2.0/24 except 192.0.2.128",
        toBDD.toBdd(acl2),
        equalTo(
            toBDD.toBdd(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(
                            AclIpSpace.difference(
                                Prefix.parse("192.0.2.0/24").toIpSpace(),
                                Ip.parse("192.0.2.128").toIpSpace()))
                        .build()))));
  }

  @Test
  public void testAccessListToRouteFilter() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_route_map");
    String acl1RfName = computeAccessListRouteFilterName("acl1");
    String acl2RfName = computeAccessListRouteFilterName("acl2");

    // setup
    initBddPrefix();

    // acl1
    assertThat(c.getRouteFilterLists(), hasKey(acl1RfName));

    RouteFilterList acl1Rf = c.getRouteFilterLists().get(acl1RfName);

    // acl1 should permit everything
    assertThat(
        _bddPrefix.permittedByRouteFilterList(acl1Rf),
        equalTo(
            _bddPrefix.inPrefixRange(
                new PrefixRange(Prefix.ZERO, new SubRange(0, Prefix.MAX_PREFIX_LENGTH)))));

    // acl2
    assertThat(c.getRouteFilterLists(), hasKey(acl2RfName));

    RouteFilterList acl2Rf = c.getRouteFilterLists().get(acl2RfName);

    assertThat(
        "acl2 permits packets with source IP in 192.0.2.0/24 except 192.0.2.128",
        _bddPrefix.permittedByRouteFilterList(acl2Rf),
        equalTo(
            _bddPrefix
                .inPrefixRange(
                    new PrefixRange(
                        Prefix.strict("192.0.2.0/24"), new SubRange(24, Prefix.MAX_PREFIX_LENGTH)))
                .and(
                    _bddPrefix
                        .inPrefixRange(
                            new PrefixRange(
                                Prefix.strict("192.0.2.128/32"),
                                new SubRange(Prefix.MAX_PREFIX_LENGTH, Prefix.MAX_PREFIX_LENGTH)))
                        .not())));
  }

  @Test
  public void testBgpAlwaysCompareMedExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_always_compare_med");

    assertTrue(
        "Ensure always-compare-med is extracted",
        vc.getBgpProcesses().get("123").getAlwaysCompareMed());
  }

  @Test
  public void testBgpDeterministicMedExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_deterministic_med");

    assertTrue(
        "Ensure deterministic-med is extracted",
        vc.getBgpProcesses().get("123").getDeterministicMed());
  }

  @Test
  public void testBgpKernelRouteRedistribution() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(SNAPSHOTS_PREFIX + "bgp_e2e", "r1", "r2")
                .build(),
            _folder);
    batfish.computeDataPlane();
    DataPlane dp = batfish.loadDataPlane();
    Set<AbstractRoute> routes1 =
        dp.getRibs().get("r1").get(Configuration.DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> routes2 =
        dp.getRibs().get("r2").get(Configuration.DEFAULT_VRF_NAME).getRoutes();

    // kernel routes should be installed
    assertThat(routes1, hasItem(isKernelRouteThat(hasPrefix(Prefix.strict("10.0.0.1/32")))));
    assertThat(routes2, hasItem(isKernelRouteThat(hasPrefix(Prefix.strict("10.0.0.2/32")))));

    // kernel routes should be redistributed
    assertThat(routes1, hasItem(isBgpRouteThat(hasPrefix(Prefix.strict("10.0.0.2/32")))));
    assertThat(routes2, hasItem(isBgpRouteThat(hasPrefix(Prefix.strict("10.0.0.1/32")))));
  }

  @Test
  public void testBgpNextHopSelfExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_next_hop_self");

    assertThat(
        vc.getBgpProcesses().get("65501").getNeighbors(),
        hasKeys("192.0.2.1", "192.0.2.2", "192.0.2.3"));
    assertTrue(
        "Ensure next-hop-self is extracted for ip neighbor",
        vc.getBgpProcesses().get("65501").getNeighbors().get("192.0.2.1").getNextHopSelf());
    assertTrue(
        "Ensure next-hop-self is extracted for peer-group",
        vc.getBgpProcesses().get("65501").getPeerGroups().get("pg1").getNextHopSelf());
    assertFalse(
        "Ensure next-hop-self is non-inherited in VS for ip neighbor",
        vc.getBgpProcesses().get("65501").getNeighbors().get("192.0.2.2").getNextHopSelf());
    assertFalse(
        "Ensure next-hop-self is false by default",
        vc.getBgpProcesses().get("65501").getNeighbors().get("192.0.2.3").getNextHopSelf());
  }

  @Test
  public void testBgpProcessConversion() throws IOException {
    String hostname = "f5_bigip_imish_bgp";
    Configuration c = parseConfig(hostname);

    // process config
    assertThat(c, hasDefaultVrf(hasBgpProcess(hasMultipathEquivalentAsPathMatchMode(EXACT_PATH))));

    // peer config
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(
                hasActiveNeighbor(
                    Prefix.strict("192.0.2.1/32"),
                    hasDescription("Cool IPv4 BGP neighbor description")))));
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(hasActiveNeighbor(Prefix.strict("192.0.2.1/32"), hasLocalAs(123L)))));
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(
                hasActiveNeighbor(
                    Prefix.strict("192.0.2.1/32"), hasLocalIp(Ip.parse("192.0.2.2"))))));
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(hasActiveNeighbor(Prefix.strict("192.0.2.1/32"), hasRemoteAs(456L)))));

    //// generated routing policies
    String bgpProcessName = "123";
    String commonExportPolicyName =
        F5BigipConfiguration.computeBgpCommonExportPolicyName(bgpProcessName);
    String peerExportPolicyName =
        F5BigipConfiguration.computeBgpPeerExportPolicyName(bgpProcessName, Ip.parse("192.0.2.1"));

    BgpRoute.Builder bgpRouteBuilder =
        BgpRoute.builder()
            .setAdmin(10)
            .setMetric(10)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("1.2.3.4"));
    BgpRoute bgpRouteAllowedByPeerPolicy =
        bgpRouteBuilder.setNetwork(Prefix.strict("10.0.0.0/24")).build();
    BgpRoute bgpRouteAllowedOnlyByCommonPolicy =
        bgpRouteBuilder.setNetwork(Prefix.strict("10.0.1.0/24")).build();
    ConnectedRoute connectedRoute = new ConnectedRoute(Prefix.strict("10.0.0.0/24"), "blah");
    KernelRoute kernelRoute = new KernelRoute(Prefix.strict("10.0.0.0/24"));

    // common export policy
    assertThat(c.getRoutingPolicies(), hasKey(commonExportPolicyName));
    RoutingPolicy commonExportPolicy = c.getRoutingPolicies().get(commonExportPolicyName);

    // peer export policy
    assertThat(c.getRoutingPolicies(), hasKey(peerExportPolicyName));
    RoutingPolicy peerExportPolicy = c.getRoutingPolicies().get(peerExportPolicyName);

    {
      // BGP input route acceptable to common export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          commonExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(bgpRouteAllowedByPeerPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      BgpRoute outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities(empty()));
    }

    {
      // BGP input route acceptable to peer export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          peerExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(bgpRouteAllowedByPeerPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      BgpRoute outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities(contains(communityStringToLong("2:2"))));
    }

    {
      // With below test, BGP input route acceptable ONLY to common export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          commonExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(bgpRouteAllowedOnlyByCommonPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // BGP input route unacceptable to peer export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertFalse(
          peerExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(bgpRouteAllowedOnlyByCommonPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Connected input route unacceptable to common export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertFalse(
          commonExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(connectedRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Connected input route unacceptable to peer export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertFalse(
          peerExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(connectedRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Kernel input route acceptable to common export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          commonExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(kernelRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      BgpRoute outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities(empty()));
    }

    {
      // Kernel input route acceptable to peer export policy
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          peerExportPolicy
              .call(
                  Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                      .setOriginalRoute(kernelRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      BgpRoute outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities(contains(communityStringToLong("2:2"))));
    }
  }

  @Test
  public void testBgpProcessReferences() throws IOException {
    String hostname = "f5_bigip_imish_bgp_process_references";
    String file = "configs/" + hostname;
    String used = "123";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ans =
        batfish.loadConvertConfigurationAnswerElementOrReparse();

    // detect all structure references
    assertThat(ans, hasNumReferrers(file, BGP_PROCESS, used, 1));
    assertThat(ans, hasNumReferrers(file, BGP_NEIGHBOR, "192.0.2.1", 1));
  }

  @Test
  public void testBgpRouterIdAuto() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_router_id_auto");

    // BGP Router-ID automatically chosen from highest IP address
    assertThat(c, hasDefaultVrf(hasBgpProcess(hasRouterId(Ip.parse("192.0.2.1")))));
  }

  @Test
  public void testBgpRouterIdManual() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_router_id_manual");

    // BGP Router-ID manually set
    assertThat(c, hasDefaultVrf(hasBgpProcess(hasRouterId(Ip.parse("192.0.2.1")))));
  }

  @Test
  public void testRecovery() throws IOException {
    String hostname = "f5_bigip_imish_recovery";
    String filename = String.format("configs/%s", hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.getSettings().setDisableUnrecognized(false);
    batfish.getSettings().setThrowOnLexerError(false);
    batfish.getSettings().setThrowOnParserError(false);
    Configuration c = batfish.loadConfigurations().get(hostname);
    assertThat(c, hasIpAccessLists(hasKey("acl2")));
    InitInfoAnswerElement initAns = batfish.initInfo(false, true);
    assertThat(initAns.getParseStatus().get(filename), equalTo(ParseStatus.PARTIALLY_UNRECOGNIZED));
  }

  @Test
  public void testRouteMap() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_route_map");
    String acceptAllName = "ACCEPT_ALL";
    String rm1Name = "rm1";

    // ACCEPT_ALL
    assertThat(c.getRoutingPolicies(), hasKey(acceptAllName));
    assertTrue(
        "ACCEPT_ALL accepts arbitrary prefix 10.0.0.0/24",
        c.getRoutingPolicies()
            .get(acceptAllName)
            .call(
                Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                    .setDirection(Direction.OUT)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.0.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());

    // rm1
    assertThat(c.getRoutingPolicies(), hasKey(rm1Name));

    RoutingPolicy rm1 = c.getRoutingPolicies().get(rm1Name);

    assertTrue(
        "rm1 denies prefix 10.0.0.0/24 (via 10)",
        !rm1.call(
                Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                    .setDirection(Direction.OUT)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.0.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());

    ConnectedRoute acceptedRoute =
        new ConnectedRoute(Prefix.strict("10.0.1.0/24"), "/Common/outint");
    BgpRoute.Builder outputRoute =
        BgpRoute.builder()
            .setNetwork(acceptedRoute.getNetwork())
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP);
    Environment acceptedPrefixEnvironment =
        Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
            .setDirection(Direction.OUT)
            .setOutputRoute(outputRoute)
            .setOriginalRoute(acceptedRoute)
            .build();
    Result acceptedBy20 = rm1.call(acceptedPrefixEnvironment);

    assertTrue("rm1 accepts prefix 10.0.1.0/24 (via 20)", acceptedBy20.getBooleanValue());
    assertThat(
        "rm1 sets communities 1:2 and 33:44 on the output route",
        outputRoute.build().getCommunities(),
        equalTo(
            Stream.of("1:2", "33:44")
                .map(CommonUtil::communityStringToLong)
                .collect(ImmutableSet.toImmutableSet())));

    assertTrue(
        "rm1 rejects prefix 10.0.2.0/24 (no matching entry)",
        !rm1.call(
                Environment.builder(c, Configuration.DEFAULT_VRF_NAME)
                    .setDirection(Direction.OUT)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.2.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());
  }

  @Test
  public void testRouteMapReferences() throws IOException {
    String hostname = "f5_bigip_imish_route_map_references";
    String file = "configs/" + hostname;
    String undefined = "route-map-undefined";
    String unused = "route-map-unused";
    String used = "route-map-used";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ans =
        batfish.loadConvertConfigurationAnswerElementOrReparse();

    // detect undefined reference
    assertThat(ans, hasUndefinedReference(file, ROUTE_MAP, undefined));

    // detected unused structure
    assertThat(ans, hasNumReferrers(file, ROUTE_MAP, unused, 0));

    // detect all structure references
    assertThat(ans, hasNumReferrers(file, ROUTE_MAP, used, 2));
  }

  private @Nonnull IpAccessListToBdd toBDD() {
    BDDPacket pkt = new BDDPacket();
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(pkt, ImmutableSet.of("dummy"));
    return new IpAccessListToBddImpl(pkt, mgr, ImmutableMap.of(), ImmutableMap.of());
  }
}
