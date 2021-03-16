package org.batfish.grammar.f5_bigip_imish;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.Route.UNSET_ROUTE_NEXT_HOP_IP;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AddressFamilyMatchers.hasImportPolicy;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasDescription;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasIpv4UnicastAddressFamily;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalIp;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasActiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEquivalentAsPathMatchMode;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasNeighbors;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasOriginType;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isBgpv4RouteThat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDefaultVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.KernelRouteMatchers.isKernelRouteThat;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.computeAccessListRouteFilterName;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.computeBgpCommonExportPolicyName;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.computeBgpPeerExportPolicyName;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.BGP_NEIGHBOR;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.BGP_PROCESS;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.ROUTE_MAP;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDPrefix;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
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
import org.batfish.datamodel.bgp.BgpConfederation;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredCombinedParser;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.representation.f5_bigip.AggregateAddress;
import org.batfish.representation.f5_bigip.BgpNeighbor;
import org.batfish.representation.f5_bigip.BgpNeighborIpv4AddressFamily;
import org.batfish.representation.f5_bigip.BgpPeerGroup;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
import org.batfish.representation.f5_bigip.ImishInterface;
import org.batfish.representation.f5_bigip.OspfInterface;
import org.batfish.representation.f5_bigip.OspfNetworkType;
import org.batfish.representation.f5_bigip.OspfProcess;
import org.batfish.representation.f5_bigip.PrefixList;
import org.batfish.representation.f5_bigip.PrefixListEntry;
import org.batfish.representation.f5_bigip.RouteMap;
import org.batfish.representation.f5_bigip.RouteMapEntry;
import org.batfish.representation.f5_bigip.RouteMapMatchPrefixList;
import org.batfish.representation.f5_bigip.RouteMapSetIpNextHop;
import org.batfish.representation.f5_bigip.RouteMapSetMetric;
import org.batfish.representation.f5_bigip.RouteMapSetOrigin;
import org.batfish.representation.f5_bigip.UpdateSource;
import org.batfish.representation.f5_bigip.UpdateSourceInterface;
import org.batfish.representation.f5_bigip.UpdateSourceIp;
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

  private void assertAcceptsKernelRoute(RoutingPolicy rp) {
    Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
    assertTrue(rp.process(new KernelRoute(Prefix.ZERO), outputBuilder, Direction.OUT));
  }

  private void assertRejectsKernelRoute(RoutingPolicy rp) {
    Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
    assertFalse(rp.process(new KernelRoute(Prefix.ZERO), outputBuilder, Direction.OUT));
  }

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private void initBddPrefix() {
    BDDPacket packet = new BDDPacket();
    _bddPrefix = new BDDPrefix(packet.getDstIp(), packet.getDscp());
  }

  private Bgpv4Route.Builder makeBgpOutputRouteBuilder() {
    return Bgpv4Route.testBuilder()
        .setNetwork(Prefix.ZERO)
        .setOriginType(OriginType.INCOMPLETE)
        .setOriginatorIp(Ip.ZERO)
        .setProtocol(RoutingProtocol.BGP);
  }

  private @Nonnull Bgpv4Route makeBgpRoute(Prefix prefix) {
    return Bgpv4Route.testBuilder()
        .setNetwork(prefix)
        .setOriginType(OriginType.INCOMPLETE)
        .setOriginatorIp(Ip.ZERO)
        .setProtocol(RoutingProtocol.BGP)
        .build();
  }

  private Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    assertThat(configs, hasKey(hostname.toLowerCase()));
    Configuration c = configs.get(hostname.toLowerCase());
    assertThat(c.getConfigurationFormat(), equalTo(ConfigurationFormat.F5_BIGIP_STRUCTURED));
    return c;
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  private @Nonnull F5BigipConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
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
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    assertThat(
        String.format("Ensure '%s' was successfully parsed", hostname),
        extractor.getVendorConfiguration(),
        notNullValue());
    VendorConfiguration vc = extractor.getVendorConfiguration();
    assertThat(vc, instanceOf(F5BigipConfiguration.class));
    // crash if not serializable
    SerializationUtils.clone(vc);
    return (F5BigipConfiguration) vc;
  }

  private @Nonnull Bgpv4Route processBgpRoute(
      RoutingPolicy rp1, BgpSessionProperties sessionProps) {
    Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
    assertTrue(
        rp1.processBgpRoute(makeBgpRoute(Prefix.ZERO), outputBuilder, sessionProps, Direction.OUT));
    return outputBuilder.build();
  }

  /** Processes a BGP route through the given policy without providing specific peer data. */
  private @Nonnull Bgpv4Route processBgpRouteNoPeerContext(RoutingPolicy rp1) {
    Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
    assertTrue(rp1.process(makeBgpRoute(Prefix.ZERO), outputBuilder, Direction.OUT));
    return outputBuilder.build();
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
                                SubRange.singleton(Prefix.MAX_PREFIX_LENGTH)))
                        .not())));
  }

  @Test
  public void testBgpAggregateAddressExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_aggregate_address");

    Map<Prefix, AggregateAddress> aggregateAddresses =
        vc.getBgpProcesses().get("65001").getAggregateAddresses();
    assertThat(
        aggregateAddresses,
        hasKeys(
            Prefix.strict("10.2.0.0/24"),
            Prefix.strict("10.3.0.0/24"),
            Prefix.strict("10.4.0.0/24"),
            Prefix.strict("10.5.0.0/24")));
    {
      AggregateAddress aa = aggregateAddresses.get(Prefix.strict("10.2.0.0/24"));
      assertFalse(aa.getAsSet());
      assertFalse(aa.getSummaryOnly());
    }
    {
      AggregateAddress aa = aggregateAddresses.get(Prefix.strict("10.3.0.0/24"));
      assertTrue(aa.getAsSet());
      assertFalse(aa.getSummaryOnly());
    }
    {
      AggregateAddress aa = aggregateAddresses.get(Prefix.strict("10.4.0.0/24"));
      assertFalse(aa.getAsSet());
      assertTrue(aa.getSummaryOnly());
    }
    {
      AggregateAddress aa = aggregateAddresses.get(Prefix.strict("10.5.0.0/24"));
      assertTrue(aa.getAsSet());
      assertTrue(aa.getSummaryOnly());
    }
  }

  @Test
  public void testBgpAggregateAddressConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_aggregate_address");
    String routingPolicyName = computeBgpCommonExportPolicyName("65001");
    RoutingPolicy exportPolicy = c.getRoutingPolicies().get(routingPolicyName);
    assertNotNull(exportPolicy);

    {
      // more specific route should be allowed since summary-only is not specified
      Bgpv4Route bgpRoute =
          Bgpv4Route.testBuilder()
              .setOriginatorIp(Ip.parse("10.0.0.1"))
              .setOriginType(OriginType.IGP)
              .setProtocol(RoutingProtocol.BGP)
              .setNetwork(Prefix.strict("10.2.0.1/32"))
              .build();

      Environment env =
          Environment.builder(c).setDirection(Direction.OUT).setOriginalRoute(bgpRoute).build();

      assertTrue(exportPolicy.call(env).getBooleanValue());
      assertNull(env.getSuppressed());
    }

    {
      // more specific route should NOT be allowed since summary-only is specified
      Bgpv4Route bgpRoute =
          Bgpv4Route.testBuilder()
              .setOriginatorIp(Ip.parse("10.0.0.1"))
              .setOriginType(OriginType.IGP)
              .setProtocol(RoutingProtocol.BGP)
              .setNetwork(Prefix.strict("10.4.0.1/32"))
              .build();

      Environment env =
          Environment.builder(c).setDirection(Direction.OUT).setOriginalRoute(bgpRoute).build();

      exportPolicy.call(env);
      assertTrue(env.getSuppressed());
    }

    {
      // a valid aggregated route should be allowed
      Bgpv4Route bgpRoute =
          Bgpv4Route.testBuilder()
              .setOriginatorIp(Ip.parse("10.0.0.1"))
              .setOriginType(OriginType.IGP)
              .setProtocol(RoutingProtocol.AGGREGATE)
              .setNetwork(Prefix.strict("10.4.0.0/24"))
              .build();

      Environment env =
          Environment.builder(c).setDirection(Direction.OUT).setOriginalRoute(bgpRoute).build();

      assertTrue(exportPolicy.call(env).getBooleanValue());
      assertNull(env.getSuppressed());
    }
  }

  @Test
  public void testBgpNullParsing() {
    // test that ignored BGP lines parse successfully
    assertNotNull(parseVendorConfig("f5_bigip_imish_bgp_null"));
  }

  @Test
  public void testBgpAlwaysCompareMedExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_always_compare_med");

    assertTrue(
        "Ensure always-compare-med is extracted",
        vc.getBgpProcesses().get("123").getAlwaysCompareMed());
  }

  @Test
  public void testBgpConfederationExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_confederation");
    assertThat(vc.getBgpProcesses().get("65001").getConfederation().getId(), equalTo(65010L));
    assertThat(vc.getBgpProcesses(), hasKeys("65001"));
    assertNotNull(vc.getBgpProcesses().get("65001").getConfederation());
    assertThat(vc.getBgpProcesses().get("65001").getConfederation().getId(), equalTo(65010L));
    assertThat(
        vc.getBgpProcesses().get("65001").getConfederation().getPeers(), contains(65012L, 65013L));
  }

  @Test
  public void testBgpConfederationConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_confederation");
    BgpProcess bgpProcess = c.getDefaultVrf().getBgpProcess();
    assertThat(
        bgpProcess.getConfederation(),
        equalTo(new BgpConfederation(65010, ImmutableSet.of(65012L, 65013L))));
    {
      BgpActivePeerConfig neighbor =
          bgpProcess.getActiveNeighbors().get(Prefix.parse("10.0.1.10/32"));
      assertNotNull(neighbor);
      assertThat(neighbor.getConfederationAsn(), equalTo(65010L));
      assertThat(neighbor.getLocalAs(), equalTo(65001L));
    }
    {
      BgpActivePeerConfig neighbor =
          bgpProcess.getActiveNeighbors().get(Prefix.parse("10.0.1.2/32"));
      assertNotNull(neighbor);
      assertThat(neighbor.getConfederationAsn(), equalTo(65010L));
      assertThat(neighbor.getLocalAs(), equalTo(65001L));
    }
    {
      BgpActivePeerConfig neighbor =
          bgpProcess.getActiveNeighbors().get(Prefix.parse("10.0.1.3/32"));
      assertNotNull(neighbor);
      assertThat(neighbor.getConfederationAsn(), equalTo(65010L));
      assertThat(neighbor.getLocalAs(), equalTo(65001L));
    }
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
                .setConfigurationFiles(SNAPSHOTS_PREFIX + "bgp_e2e", "r1", "r2")
                .build(),
            _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    Set<AbstractRoute> routes1 =
        dp.getRibs().get("r1").get(Configuration.DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> routes2 =
        dp.getRibs().get("r2").get(Configuration.DEFAULT_VRF_NAME).getRoutes();

    // kernel routes should be installed
    assertThat(routes1, hasItem(isKernelRouteThat(hasPrefix(Prefix.strict("10.0.0.1/32")))));
    assertThat(routes2, hasItem(isKernelRouteThat(hasPrefix(Prefix.strict("10.0.0.2/32")))));

    // kernel routes should be redistributed
    assertThat(routes1, hasItem(isBgpv4RouteThat(hasPrefix(Prefix.strict("10.0.0.2/32")))));
    assertThat(routes2, hasItem(isBgpv4RouteThat(hasPrefix(Prefix.strict("10.0.0.1/32")))));
  }

  @Test
  public void testBgpKernelRouteRedistributionNoRouteMap() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_redistribute_kernel_no_route_map");
    Ip peerAddress = Ip.parse("192.0.2.2");
    String rpName = computeBgpPeerExportPolicyName("1", peerAddress);

    assertThat(c.getRoutingPolicies(), hasKey(rpName));

    RoutingPolicy rp = c.getRoutingPolicies().get(rpName);

    assertAcceptsKernelRoute(rp);
  }

  @Test
  public void testBgpKernelRouteRedistributionRouteMapAccept() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_redistribute_kernel_route_map_accept");
    Ip peerAddress = Ip.parse("192.0.2.2");
    String rpName = computeBgpPeerExportPolicyName("1", peerAddress);

    assertThat(c.getRoutingPolicies(), hasKey(rpName));

    RoutingPolicy rp = c.getRoutingPolicies().get(rpName);

    assertAcceptsKernelRoute(rp);
  }

  @Test
  public void testBgpKernelRouteRedistributionRouteMapReject() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_redistribute_kernel_route_map_reject");
    Ip peerAddress = Ip.parse("192.0.2.2");
    String rpName = computeBgpPeerExportPolicyName("1", peerAddress);

    assertThat(c.getRoutingPolicies(), hasKey(rpName));

    RoutingPolicy rp = c.getRoutingPolicies().get(rpName);

    assertRejectsKernelRoute(rp);
  }

  @Test
  public void testBgpConnectedRouteRedistributionNoRouteMap() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_redistribute_connected_no_route_map");
    Ip peerAddress = Ip.parse("192.0.2.2");
    String rpName = computeBgpPeerExportPolicyName("1", peerAddress);

    assertThat(c.getRoutingPolicies(), hasKey(rpName));

    RoutingPolicy rp = c.getRoutingPolicies().get(rpName);

    Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
    assertTrue(rp.process(new ConnectedRoute(Prefix.ZERO, "iface1"), outputBuilder, Direction.OUT));
  }

  @Test
  public void testBgpConnectedRouteRedistributionRouteMapAccept() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_redistribute_connected_route_map_accept");
    Ip peerAddress = Ip.parse("192.0.2.2");
    String rpName = computeBgpPeerExportPolicyName("1", peerAddress);

    assertThat(c.getRoutingPolicies(), hasKey(rpName));

    RoutingPolicy rp = c.getRoutingPolicies().get(rpName);

    Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
    assertTrue(rp.process(new ConnectedRoute(Prefix.ZERO, "iface1"), outputBuilder, Direction.OUT));
  }

  @Test
  public void testBgpConnectedRouteRedistributionRouteMapReject() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_redistribute_connected_route_map_reject");
    Ip peerAddress = Ip.parse("192.0.2.2");
    String rpName = computeBgpPeerExportPolicyName("1", peerAddress);

    assertThat(c.getRoutingPolicies(), hasKey(rpName));

    RoutingPolicy rp = c.getRoutingPolicies().get(rpName);

    Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
    assertFalse(
        rp.process(new ConnectedRoute(Prefix.ZERO, "iface1"), outputBuilder, Direction.OUT));
  }

  @Test
  public void testBgpNextHopSelfConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_next_hop_self");

    Ip localIp = Ip.parse("192.0.2.254");
    Ip peer1Ip = Ip.parse("192.0.2.1");
    Ip peer2Ip = Ip.parse("192.0.2.2");
    Ip peer3Ip = Ip.parse("192.0.2.3");
    Prefix peer1Prefix = peer1Ip.toPrefix();
    Prefix peer2Prefix = peer2Ip.toPrefix();
    Prefix peer3Prefix = peer3Ip.toPrefix();

    assertThat(
        c,
        hasDefaultVrf(hasBgpProcess(hasNeighbors(hasKeys(peer1Prefix, peer2Prefix, peer3Prefix)))));
    RoutingPolicy rp1 =
        c.getRoutingPolicies().get(computeBgpPeerExportPolicyName("65501", peer1Ip));
    RoutingPolicy rp2 =
        c.getRoutingPolicies().get(computeBgpPeerExportPolicyName("65501", peer2Ip));
    RoutingPolicy rp3 =
        c.getRoutingPolicies().get(computeBgpPeerExportPolicyName("65501", peer3Ip));

    BgpSessionProperties.Builder sessionProps =
        BgpSessionProperties.builder().setHeadAs(65501).setTailAs(65501).setHeadIp(localIp);
    BgpSessionProperties fromPeer1 = sessionProps.setTailIp(peer1Ip).build();
    BgpSessionProperties fromPeer2 = sessionProps.setTailIp(peer2Ip).build();
    BgpSessionProperties fromPeer3 = sessionProps.setTailIp(peer3Ip).build();

    // 192.0.2.1 with next-hop-self should use next-hop-ip of interface
    assertThat(processBgpRoute(rp1, fromPeer1), hasNextHopIp(equalTo(localIp)));

    // 192.0.2.2 with next-hop-self inherited from pg1 should use next-hop-ip of interface
    assertThat(processBgpRoute(rp2, fromPeer2), hasNextHopIp(equalTo(localIp)));

    // 192.0.2.3 without next-hop-self should leave next-hop-ip unset for dp engine to handle
    assertThat(processBgpRoute(rp3, fromPeer3), hasNextHopIp(equalTo(UNSET_ROUTE_NEXT_HOP_IP)));
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
    assertThat(
        "Ensure next-hop-self is non-inherited in VS for ip neighbor",
        vc.getBgpProcesses().get("65501").getNeighbors().get("192.0.2.2").getNextHopSelf(),
        nullValue());
    assertThat(
        "Ensure next-hop-self is unset",
        vc.getBgpProcesses().get("65501").getNeighbors().get("192.0.2.3").getNextHopSelf(),
        nullValue());
  }

  @Test
  public void testBgpProcessExtractionGh5721() {
    String hostname = "f5_bigip_imish_bgp_gh_5721";
    F5BigipConfiguration c = parseVendorConfig(hostname);
    assertThat(c.getBgpProcesses(), hasKey("11111"));
    org.batfish.representation.f5_bigip.BgpProcess p = c.getBgpProcesses().get("11111");
    assertThat(p.getPeerGroups().keySet(), containsInAnyOrder("spines"));
    BgpPeerGroup spines = p.getPeerGroups().get("spines");
    assertThat(spines.getRemoteAs(), equalTo(22222L));
    BgpNeighborIpv4AddressFamily spinesv4 = spines.getIpv4AddressFamily();
    assertThat(spinesv4.getRouteMapIn(), equalTo("V4-SPINE-TO-EBIGIP"));
    assertThat(spinesv4.getRouteMapOut(), equalTo("V4-EBIGIP-TO-SPINE"));
    assertThat(
        p.getNeighbors().keySet(),
        containsInAnyOrder(
            "10.10.13.16", "10.10.106.36", "10.10.106.100", "10.10.106.164", "10.10.106.228"));
    BgpNeighbor neighbor16 = p.getNeighbors().get("10.10.13.16");
    assertThat(neighbor16.getAddress(), equalTo(Ip.parse("10.10.13.16")));
    assertThat(neighbor16.getPeerGroup(), nullValue());
    assertThat(neighbor16.getIpv4AddressFamily().getRouteMapIn(), nullValue());
    assertThat(neighbor16.getIpv4AddressFamily().getRouteMapOut(), equalTo("ebigip-to-agg"));
    BgpNeighbor neighbor36 = p.getNeighbors().get("10.10.106.36");
    assertThat(neighbor36.getAddress(), equalTo(Ip.parse("10.10.106.36")));
    assertThat(neighbor36.getPeerGroup(), equalTo("spines"));
    assertThat(neighbor36.getIpv4AddressFamily().getRouteMapIn(), nullValue());
    assertThat(neighbor36.getIpv4AddressFamily().getRouteMapOut(), nullValue());
  }

  @Test
  public void testBgpProcessConversionGh5721() throws IOException {
    String hostname = "f5_bigip_imish_bgp_gh_5721";
    Configuration c = parseConfig(hostname);
    BgpProcess p = c.getDefaultVrf().getBgpProcess();
    assertThat(p, notNullValue());
    assertThat(
        p.getActiveNeighbors(),
        hasKeys(
            Prefix.parse("10.10.13.16/32"),
            Prefix.parse("10.10.106.36/32"),
            Prefix.parse("10.10.106.100/32"),
            Prefix.parse("10.10.106.164/32"),
            Prefix.parse("10.10.106.228/32")));
    BgpActivePeerConfig neighbor16 = p.getActiveNeighbors().get(Prefix.parse("10.10.13.16/32"));
    assertThat(neighbor16.getRemoteAsns(), equalTo(LongSpace.of(33333)));
    assertThat(neighbor16.getDescription(), equalTo("agg1-dc1"));
    assertThat(neighbor16.getIpv4UnicastAddressFamily(), notNullValue());
    assertThat(neighbor16.getIpv4UnicastAddressFamily().getImportPolicy(), nullValue());
    assertThat(neighbor16.getIpv4UnicastAddressFamily().getExportPolicy(), notNullValue());
    BgpActivePeerConfig neighbor36 = p.getActiveNeighbors().get(Prefix.parse("10.10.106.36/32"));
    assertThat(neighbor36.getRemoteAsns(), equalTo(LongSpace.of(22222)));
    assertThat(neighbor36.getDescription(), equalTo("spine5-dc1"));
    assertThat(neighbor36.getIpv4UnicastAddressFamily(), notNullValue());
    assertThat(
        neighbor36.getIpv4UnicastAddressFamily().getImportPolicy(), equalTo("V4-SPINE-TO-EBIGIP"));
    assertThat(neighbor36.getIpv4UnicastAddressFamily().getExportPolicy(), notNullValue());
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
                    allOf(
                        hasDescription("Cool IPv4 BGP neighbor description"),
                        hasIpv4UnicastAddressFamily(hasImportPolicy("MY_IPV4_IN")))))));
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

    Bgpv4Route.Builder bgpRouteBuilder =
        Bgpv4Route.testBuilder()
            .setAdmin(10)
            .setMetric(10)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("1.2.3.4"));
    Bgpv4Route bgpv4RouteAllowedByPeerPolicy =
        bgpRouteBuilder.setNetwork(Prefix.strict("10.0.0.0/24")).build();
    Bgpv4Route bgpv4RouteAllowedOnlyByCommonPolicy =
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
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          commonExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(bgpv4RouteAllowedByPeerPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      Bgpv4Route outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities());
    }

    {
      // BGP input route acceptable to peer export policy
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          peerExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(bgpv4RouteAllowedByPeerPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      Bgpv4Route outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities(StandardCommunity.parse("2:2")));
    }

    {
      // With below test, BGP input route acceptable ONLY to common export policy
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          commonExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(bgpv4RouteAllowedOnlyByCommonPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // BGP input route unacceptable to peer export policy
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertFalse(
          peerExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(bgpv4RouteAllowedOnlyByCommonPolicy)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Connected input route unacceptable to common export policy
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertFalse(
          commonExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(connectedRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Connected input route unacceptable to peer export policy
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertFalse(
          peerExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(connectedRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Kernel input route acceptable to common export policy
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          commonExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(kernelRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      Bgpv4Route outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities());
    }

    {
      // Kernel input route acceptable to peer export policy
      Bgpv4Route.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          peerExportPolicy
              .call(
                  Environment.builder(c)
                      .setOriginalRoute(kernelRoute)
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
      Bgpv4Route outputRoute = outputBuilder.build();
      assertThat(outputRoute, hasCommunities(StandardCommunity.parse("2:2")));
    }
  }

  @Test
  public void testBgpProcessReferences() throws IOException {
    String hostname = "f5_bigip_imish_bgp_process_references";
    String file = "configs/" + hostname;
    String used = "123";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ans =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

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
  public void testOspfExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_ospf");

    assertThat(vc.getImishInterfaces(), hasKeys("vlan_active", "vlan_active_nbma", "vlan_passive"));
    {
      ImishInterface iface = vc.getImishInterfaces().get("vlan_active");
      assertThat(iface.getName(), equalTo("vlan_active"));
      OspfInterface ospf = iface.getOspf();
      assertNull(ospf);
    }
    {
      ImishInterface iface = vc.getImishInterfaces().get("vlan_active_nbma");
      assertThat(iface.getName(), equalTo("vlan_active_nbma"));
      OspfInterface ospf = iface.getOspf();
      assertNotNull(ospf);
      assertThat(ospf.getNetwork(), equalTo(OspfNetworkType.NON_BROADCAST));
    }
    {
      ImishInterface iface = vc.getImishInterfaces().get("vlan_passive");
      assertNull(iface.getOspf());
    }

    assertThat(vc.getOspfProcesses(), hasKeys("1"));
    {
      OspfProcess proc = vc.getOspfProcesses().get("1");
      assertThat(proc.getName(), equalTo("1"));
      assertThat(proc.getRouterId(), equalTo(Ip.parse("10.0.1.1")));
      assertThat(proc.getPassiveInterfaces(), contains("vlan_passive"));
      assertThat(
          proc.getNetworks(),
          equalTo(
              ImmutableMap.of(
                  Prefix.strict("10.0.1.0/30"),
                  0L,
                  Prefix.strict("10.0.2.0/30"),
                  0L,
                  Prefix.strict("10.0.3.0/30"),
                  0L)));
      assertThat(proc.getNeighbors(), contains(Ip.parse("10.0.1.2")));
    }
  }

  @Test
  public void testOspfConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_ospf");

    assertThat(c.getDefaultVrf().getOspfProcesses(), hasKeys("1"));

    org.batfish.datamodel.ospf.OspfProcess proc = c.getDefaultVrf().getOspfProcesses().get("1");
    assertNotNull(proc);
    assertThat(proc.getRouterId(), equalTo(Ip.parse("10.0.1.1")));
    assertThat(proc.getAreas(), hasKeys(0L));
    {
      OspfArea area = proc.getAreas().get(0L);
      assertThat(
          area.getInterfaces(),
          containsInAnyOrder(
              "/Common/vlan_active", "/Common/vlan_active_nbma", "/Common/vlan_passive"));
    }

    assertThat(
        c.getAllInterfaces(),
        hasKeys("/Common/vlan_active", "/Common/vlan_active_nbma", "/Common/vlan_passive"));
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("/Common/vlan_active");
      OspfInterfaceSettings ospf = iface.getOspfSettings();
      assertNotNull(ospf);
      assertTrue(ospf.getEnabled());
      assertThat(ospf.getAreaName(), equalTo(0L));
      assertThat(
          ospf.getNetworkType(), equalTo(org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST));
      assertFalse(ospf.getPassive());
      assertThat(ospf.getHelloInterval(), equalTo(10));
      assertThat(ospf.getDeadInterval(), equalTo(40));
    }
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("/Common/vlan_active_nbma");
      OspfInterfaceSettings ospf = iface.getOspfSettings();
      assertNotNull(ospf);
      assertTrue(ospf.getEnabled());
      assertThat(ospf.getAreaName(), equalTo(0L));
      assertThat(
          ospf.getNetworkType(),
          equalTo(org.batfish.datamodel.ospf.OspfNetworkType.NON_BROADCAST_MULTI_ACCESS));
      assertFalse(ospf.getPassive());
      assertThat(ospf.getHelloInterval(), equalTo(30));
      assertThat(ospf.getDeadInterval(), equalTo(120));
      assertThat(ospf.getNbmaNeighbors(), contains(Ip.parse("10.0.1.2")));
    }
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("/Common/vlan_passive");
      OspfInterfaceSettings ospf = iface.getOspfSettings();
      assertNotNull(ospf);
      assertTrue(ospf.getEnabled());
      assertThat(ospf.getAreaName(), equalTo(0L));
      // network type does not matter
      assertTrue(ospf.getPassive());
    }
  }

  @Test
  public void testPrefixListExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_prefix_list");

    // check all lists are extracted
    assertThat(vc.getPrefixLists(), hasKeys("pl_simple", "pl_le", "pl_ge", "pl_ge_le", "pl_deny"));
    // check lists know their own names
    vc.getPrefixLists()
        .forEach((name, prefixList) -> assertThat(prefixList.getName(), equalTo(name)));

    PrefixList plSimple = vc.getPrefixLists().get("pl_simple");
    PrefixList plLe = vc.getPrefixLists().get("pl_le");
    PrefixList plGe = vc.getPrefixLists().get("pl_ge");
    PrefixList plGeLe = vc.getPrefixLists().get("pl_ge_le");
    PrefixList plDeny = vc.getPrefixLists().get("pl_deny");

    // check presence of entries
    assertThat(plSimple.getEntries(), hasKeys(10L));
    assertThat(plLe.getEntries(), hasKeys(10L));
    assertThat(plGe.getEntries(), hasKeys(10L));
    assertThat(plGeLe.getEntries(), hasKeys(10L));
    assertThat(plDeny.getEntries(), hasKeys(10L, 20L));

    PrefixListEntry plSimple10 = plSimple.getEntries().get(10L);
    PrefixListEntry plLe10 = plLe.getEntries().get(10L);
    PrefixListEntry plGe10 = plGe.getEntries().get(10L);
    PrefixListEntry plGeLe10 = plGeLe.getEntries().get(10L);
    PrefixListEntry plDeny10 = plDeny.getEntries().get(10L);
    PrefixListEntry plDeny20 = plDeny.getEntries().get(20L);

    // check entry actions
    assertThat(plSimple10.getAction(), equalTo(LineAction.PERMIT));
    assertThat(plLe10.getAction(), equalTo(LineAction.PERMIT));
    assertThat(plGe10.getAction(), equalTo(LineAction.PERMIT));
    assertThat(plGeLe10.getAction(), equalTo(LineAction.PERMIT));
    assertThat(plDeny10.getAction(), equalTo(LineAction.DENY));
    assertThat(plDeny20.getAction(), equalTo(LineAction.PERMIT));

    // check entry prefixes
    assertThat(plSimple10.getPrefix(), equalTo(Prefix.parse("10.0.0.0/24")));
    assertThat(plLe10.getPrefix(), equalTo(Prefix.parse("10.0.0.0/16")));
    assertThat(plGe10.getPrefix(), equalTo(Prefix.parse("10.0.0.0/16")));
    assertThat(plGeLe10.getPrefix(), equalTo(Prefix.parse("10.0.0.0/16")));
    assertThat(plDeny10.getPrefix(), equalTo(Prefix.parse("10.0.0.0/32")));
    assertThat(plDeny20.getPrefix(), equalTo(Prefix.parse("10.0.0.0/16")));

    // check entry length-ranges
    assertThat(plSimple10.getLengthRange(), equalTo(SubRange.singleton(24)));
    assertThat(plLe10.getLengthRange(), equalTo(new SubRange(16, 24)));
    assertThat(plGe10.getLengthRange(), equalTo(new SubRange(24, 32)));
    assertThat(plGeLe10.getLengthRange(), equalTo(new SubRange(24, 28)));
    assertThat(plDeny10.getLengthRange(), equalTo(SubRange.singleton(32)));
    assertThat(plDeny20.getLengthRange(), equalTo(SubRange.singleton(16)));
  }

  @Test
  public void testRecovery() throws IOException {
    String hostname = "f5_bigip_imish_recovery";
    String filename = String.format("configs/%s", hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.getSettings().setDisableUnrecognized(false);
    batfish.getSettings().setThrowOnLexerError(false);
    batfish.getSettings().setThrowOnParserError(false);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    assertThat(c, hasIpAccessLists(hasKey("acl2")));
    InitInfoAnswerElement initAns = batfish.initInfo(batfish.getSnapshot(), false, true);
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
                Environment.builder(c)
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
                Environment.builder(c)
                    .setDirection(Direction.OUT)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.0.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());

    ConnectedRoute acceptedRoute =
        new ConnectedRoute(Prefix.strict("10.0.1.0/24"), "/Common/outint");
    Bgpv4Route.Builder outputRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(acceptedRoute.getNetwork())
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP);
    Environment acceptedPrefixEnvironment =
        Environment.builder(c)
            .setDirection(Direction.OUT)
            .setOutputRoute(outputRoute)
            .setOriginalRoute(acceptedRoute)
            .build();
    Result acceptedBy20 = rm1.call(acceptedPrefixEnvironment);

    assertTrue("rm1 accepts prefix 10.0.1.0/24 (via 20)", acceptedBy20.getBooleanValue());
    assertThat(
        "rm1 sets communities 1:2 and 33:44 on the output route",
        outputRoute.build().getCommunities().getCommunities(),
        equalTo(ImmutableSet.of(StandardCommunity.of(1, 2), StandardCommunity.of(33, 44))));

    assertTrue(
        "rm1 rejects prefix 10.0.2.0/24 (no matching entry)",
        !rm1.call(
                Environment.builder(c)
                    .setDirection(Direction.OUT)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.2.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());
  }

  @Test
  public void testRouteMapMatchIpAddressPrefixListConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_route_map_match_ip_address_prefix_list");
    String rp1Name = "rm1";
    String rp2Name = "rm2";

    assertThat(c.getRoutingPolicies(), hasKeys(rp1Name, rp2Name));

    // (default route) accepted
    assertAcceptsKernelRoute(c.getRoutingPolicies().get(rp1Name));
    // (default route) rejected
    assertRejectsKernelRoute(c.getRoutingPolicies().get(rp2Name));
  }

  @Test
  public void testRouteMapMatchIpAddressPrefixListExtraction() {
    F5BigipConfiguration vc =
        parseVendorConfig("f5_bigip_imish_route_map_match_ip_address_prefix_list");
    String rm1Name = "rm1";
    String rm2Name = "rm2";

    assertThat(vc.getRouteMaps(), hasKeys(rm1Name, rm2Name));

    RouteMap rm1 = vc.getRouteMaps().get(rm1Name);
    RouteMap rm2 = vc.getRouteMaps().get(rm2Name);

    assertThat(rm1.getEntries(), hasKeys(10L));
    assertThat(rm2.getEntries(), hasKeys(10L));

    RouteMapEntry entry1 = rm1.getEntries().get(10L);
    RouteMapEntry entry2 = rm2.getEntries().get(10L);

    RouteMapMatchPrefixList match1 = entry1.getMatchPrefixList();
    RouteMapMatchPrefixList match2 = entry2.getMatchPrefixList();

    assertThat(match1, notNullValue());
    assertThat(match1.getPrefixList(), equalTo("pl1"));
    assertThat(entry1.getMatches().collect(ImmutableList.toImmutableList()), contains(match1));
    assertThat(match2, notNullValue());
    assertThat(match2.getPrefixList(), equalTo("pl2"));
    assertThat(entry2.getMatches().collect(ImmutableList.toImmutableList()), contains(match2));
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
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // detect undefined reference
    assertThat(ans, hasUndefinedReference(file, ROUTE_MAP, undefined));

    // detected unused structure
    assertThat(ans, hasNumReferrers(file, ROUTE_MAP, unused, 0));

    // detect all structure references
    assertThat(ans, hasNumReferrers(file, ROUTE_MAP, used, 2));
  }

  @Test
  public void testRouteMapSetMetricConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_route_map_set_metric");
    String rpName = "rm1";

    assertThat(c.getRoutingPolicies(), hasKeys(rpName));

    assertThat(processBgpRouteNoPeerContext(c.getRoutingPolicies().get(rpName)), hasMetric(50L));
  }

  @Test
  public void testRouteMapSetMetricExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_route_map_set_metric");
    String rmName = "rm1";

    assertThat(vc.getRouteMaps(), hasKeys(rmName));

    RouteMap rm = vc.getRouteMaps().get(rmName);

    assertThat(rm.getEntries(), hasKeys(10L));

    RouteMapEntry entry = rm.getEntries().get(10L);

    RouteMapSetMetric set = entry.getSetMetric();

    assertThat(set, notNullValue());
    assertThat(set.getMetric(), equalTo(50L));
    assertThat(entry.getSets().collect(ImmutableList.toImmutableList()), contains(set));
  }

  @Test
  public void testRouteMapSetOriginConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_route_map_set_origin");
    String rpName = "rm1";

    assertThat(c.getRoutingPolicies(), hasKeys(rpName));

    assertThat(
        processBgpRouteNoPeerContext(c.getRoutingPolicies().get(rpName)),
        hasOriginType(OriginType.IGP));
  }

  @Test
  public void testRouteMapSetOriginExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_route_map_set_origin");
    String rmName = "rm1";

    assertThat(vc.getRouteMaps(), hasKeys(rmName));

    RouteMap rm = vc.getRouteMaps().get(rmName);

    assertThat(rm.getEntries(), hasKeys(10L));

    RouteMapEntry entry = rm.getEntries().get(10L);

    RouteMapSetOrigin set = entry.getSetOrigin();

    assertThat(set, notNullValue());
    assertThat(set.getOrigin(), equalTo(OriginType.IGP));
    assertThat(entry.getSets().collect(ImmutableList.toImmutableList()), contains(set));
  }

  @Test
  public void testBgpNeighborNull() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_neighbor_null");
    assertNotNull(vc);
  }

  @Test
  public void testBgpNeighborUpdateSourceExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_bgp_neighbor_update_source");
    assertNotNull(vc.getBgpProcesses().get("65001"));
    assertNotNull(vc.getBgpProcesses().get("65001").getNeighbors().get("10.0.1.10"));
    UpdateSource updateSource =
        vc.getBgpProcesses().get("65001").getNeighbors().get("10.0.1.10").getUpdateSource();
    assertThat(updateSource, instanceOf(UpdateSourceIp.class));
    assertThat(((UpdateSourceIp) updateSource).getIp(), equalTo(Ip.parse("10.0.1.1")));

    assertNotNull(vc.getBgpProcesses().get("65001").getNeighbors().get("10.0.2.10"));
    updateSource =
        vc.getBgpProcesses().get("65001").getNeighbors().get("10.0.2.10").getUpdateSource();
    assertThat(updateSource, instanceOf(UpdateSourceInterface.class));
    assertThat(((UpdateSourceInterface) updateSource).getName(), equalTo("vlan1"));
  }

  @Test
  public void testBgpNeighborUpdateSourceConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_bgp_neighbor_update_source");
    BgpActivePeerConfig peer1 =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Prefix.parse("10.0.1.10/32"));
    assertNotNull(peer1);
    assertThat(peer1.getLocalIp(), equalTo(Ip.parse("10.0.1.1")));

    BgpActivePeerConfig peer2 =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Prefix.parse("10.0.2.10/32"));
    assertNotNull(peer2);
    assertThat(peer2.getLocalIp(), equalTo(Ip.parse("10.0.2.1")));

    BgpActivePeerConfig peer3 =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Prefix.parse("10.0.3.10/32"));
    assertNotNull(peer3);
    // cannot infer a local IP for this neighbor since no interface in this subnet
    assertNull(peer3.getLocalIp());

    BgpActivePeerConfig peer4 =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Prefix.parse("10.0.4.10/32"));
    assertNotNull(peer4);
    // get the default IP (i.e., an interface address in the same subnet)
    assertThat(peer4.getLocalIp(), equalTo(Ip.parse("10.0.4.1")));
  }

  private @Nonnull IpAccessListToBdd toBDD() {
    BDDPacket pkt = new BDDPacket();
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(pkt, ImmutableSet.of("dummy"));
    return new IpAccessListToBddImpl(pkt, mgr, ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testRouteMapSetNextHopExtraction() {
    F5BigipConfiguration vc = parseVendorConfig("f5_bigip_imish_route_map_set_next_hop");
    assertNotNull(vc.getRouteMaps().get("rm1"));

    RouteMapEntry entry = vc.getRouteMaps().get("rm1").getEntries().get(10L);
    assertNotNull(entry);
    RouteMapSetIpNextHop set = entry.getSetIpNextHop();
    assertNotNull(set);
    assertThat(set.getNextHop(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(entry.getSets().collect(ImmutableList.toImmutableList()), contains(set));
  }

  @Test
  public void testRouteMapSetNextHopConversion() throws IOException {
    Configuration c = parseConfig("f5_bigip_imish_route_map_set_next_hop");
    String rpName = "rm1";

    assertThat(c.getRoutingPolicies(), hasKeys(rpName));

    assertThat(
        processBgpRouteNoPeerContext(c.getRoutingPolicies().get(rpName)),
        hasNextHopIp(Ip.parse("1.2.3.4")));
  }
}
