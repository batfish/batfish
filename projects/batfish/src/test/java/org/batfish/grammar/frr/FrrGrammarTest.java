package org.batfish.grammar.frr;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.bgpNeighborStructureName;
import static org.batfish.datamodel.Names.generatedBgpMainRibIndependentNetworkPolicyName;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.isNonRouting;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasWeight;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isBgpv4RouteThat;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.routing_policy.Environment.Direction.OUT;
import static org.batfish.grammar.frr.FrrConfigurationBuilder.nextMultipleOfFive;
import static org.batfish.representation.frr.FrrConversions.computeOspfAreaRangeFilterName;
import static org.batfish.representation.frr.FrrConversions.computeRouteMapEntryName;
import static org.batfish.representation.frr.FrrRoutingProtocol.CONNECTED;
import static org.batfish.representation.frr.FrrRoutingProtocol.OSPF;
import static org.batfish.representation.frr.FrrRoutingProtocol.STATIC;
import static org.batfish.representation.frr.FrrStructureType.BGP_AS_PATH_ACCESS_LIST;
import static org.batfish.representation.frr.FrrStructureType.BGP_COMMUNITY_LIST;
import static org.batfish.representation.frr.FrrStructureType.BGP_LISTEN_RANGE;
import static org.batfish.representation.frr.FrrStructureType.BGP_NEIGHBOR;
import static org.batfish.representation.frr.FrrStructureType.BGP_NEIGHBOR_INTERFACE;
import static org.batfish.representation.frr.FrrStructureType.ROUTE_MAP;
import static org.batfish.representation.frr.FrrStructureType.VRF;
import static org.batfish.representation.frr.FrrStructureUsage.BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF;
import static org.batfish.representation.frr.FrrStructureUsage.BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF;
import static org.batfish.representation.frr.FrrStructureUsage.BGP_LISTEN_RANGE_SELF_REF;
import static org.batfish.representation.frr.FrrStructureUsage.BGP_NEIGHBOR_INTERFACE_SELF_REF;
import static org.batfish.representation.frr.FrrStructureUsage.BGP_NEIGHBOR_SELF_REF;
import static org.batfish.representation.frr.FrrStructureUsage.ROUTE_MAP_MATCH_AS_PATH;
import static org.batfish.representation.frr.FrrStructureUsage.ROUTE_MAP_MATCH_COMMUNITY_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.graph.ValueGraph;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute.Builder;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfAreaSummary.SummaryRouteBehavior;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.representation.cumulus_concatenated.CumulusConcatenatedConfiguration;
import org.batfish.representation.cumulus_concatenated.InterfacesInterface;
import org.batfish.representation.frr.BgpAsPathAccessList;
import org.batfish.representation.frr.BgpAsPathAccessListLine;
import org.batfish.representation.frr.BgpCommunityListExpanded;
import org.batfish.representation.frr.BgpCommunityListExpandedLine;
import org.batfish.representation.frr.BgpInterfaceNeighbor;
import org.batfish.representation.frr.BgpIpNeighbor;
import org.batfish.representation.frr.BgpNeighbor;
import org.batfish.representation.frr.BgpNeighbor.RemoteAs;
import org.batfish.representation.frr.BgpNeighborIpv4UnicastAddressFamily;
import org.batfish.representation.frr.BgpNeighborIpv4UnicastAddressFamily.RemovePrivateAsMode;
import org.batfish.representation.frr.BgpNeighborL2vpnEvpnAddressFamily;
import org.batfish.representation.frr.BgpNeighborSourceAddress;
import org.batfish.representation.frr.BgpNeighborSourceInterface;
import org.batfish.representation.frr.BgpNetwork;
import org.batfish.representation.frr.BgpPeerGroupNeighbor;
import org.batfish.representation.frr.BgpRedistributionPolicy;
import org.batfish.representation.frr.BgpVrfAddressFamilyAggregateNetworkConfiguration;
import org.batfish.representation.frr.FrrConfiguration;
import org.batfish.representation.frr.FrrInterface;
import org.batfish.representation.frr.FrrRoutingProtocol;
import org.batfish.representation.frr.FrrStructureType;
import org.batfish.representation.frr.FrrStructureUsage;
import org.batfish.representation.frr.IpPrefixList;
import org.batfish.representation.frr.IpPrefixListLine;
import org.batfish.representation.frr.Ipv6PrefixList;
import org.batfish.representation.frr.Ipv6PrefixListLine;
import org.batfish.representation.frr.OspfArea;
import org.batfish.representation.frr.OspfNetworkArea;
import org.batfish.representation.frr.OspfNetworkType;
import org.batfish.representation.frr.OspfVrf;
import org.batfish.representation.frr.RedistributionPolicy;
import org.batfish.representation.frr.RouteMap;
import org.batfish.representation.frr.RouteMapEntry;
import org.batfish.representation.frr.RouteMapMatchSourceProtocol.Protocol;
import org.batfish.representation.frr.RouteMapMetricType;
import org.batfish.representation.frr.StaticRoute;
import org.batfish.representation.frr.Vrf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link FrrParser}. */
public class FrrGrammarTest {
  private static final String FILENAME = "";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  // TODO: Shouldn't depend directly on Cumulus concatenated
  private static CumulusConcatenatedConfiguration _config;
  private static FrrConfiguration _frr;
  private static ConvertConfigurationAnswerElement _ccae;
  private static Warnings _warnings;

  private static final String SNAPSHOTS_PREFIX = "org/batfish/grammar/frr/snapshots/";

  @Before
  public void setup() {
    _config = new CumulusConcatenatedConfiguration();
    _config.setHostname("c");
    _frr = _config.getFrrConfiguration();
    _ccae = new ConvertConfigurationAnswerElement();
    _warnings = new Warnings(true, true, true);
    _config.setFilename(FILENAME);
    _config.setWarnings(_warnings);
  }

  private static DefinedStructureInfo getDefinedStructureInfo(FrrStructureType type, String name) {
    return _ccae
        .getDefinedStructures()
        .get(_config.getFilename())
        .getOrDefault(type.getDescription(), ImmutableSortedMap.of())
        .get(name);
  }

  private Set<Integer> getStructureReferences(
      FrrStructureType type, String name, FrrStructureUsage usage) {
    return _ccae
        .getReferencedStructures()
        .get(FILENAME)
        .get(type.getDescription())
        .get(name)
        .get(usage.getDescription());
  }

  /**
   * NB: the parse function can only be used once per test, as it breaks state that is setup cleanly
   * once per test.
   */
  private static void parse(String src) {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);

    parseFromTextWithSettings(src, settings);
    _config.getStructureManager().saveInto(_ccae, _config.getFilename());
  }

  /**
   * NB: the parseLines function can only be used once per test, as it breaks state that is setup
   * cleanly once per test.
   */
  private static void parseLines(String... lines) {
    parse(String.join("\n", lines) + "\n");
  }

  /**
   * NB: the parseFromTextWithSettings function can only be used once per test, as it breaks state
   * that is setup cleanly once per test.
   */
  private static void parseFromTextWithSettings(String src, Settings settings) {
    FrrCombinedParser parser = new FrrCombinedParser(src, settings, 1, 0);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    FrrConfigurationBuilder cb =
        new FrrConfigurationBuilder(_config, parser, _warnings, src, new SilentSyntaxCollection());
    walker.walk(cb, tree);

    // SerializationUtils.clone will clear transient state, which we save and restore.
    // Or populate with default values for things that supplied by Batfish pre-conversion.
    Warnings w = _config.getWarnings();
    _config = SerializationUtils.clone(_config);
    _config.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    _config.setWarnings(w);
  }

  @Test
  public void testBgp_defaultVrf() {
    parse("router bgp 12345\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getAutonomousSystem(), equalTo(12345L));
  }

  @Test
  public void testBgp_vrf() {
    parse("router bgp 12345 vrf foo\n");
    assertThat(_frr.getBgpProcess().getVrfs().get("foo").getAutonomousSystem(), equalTo(12345L));
    assertThat(
        getStructureReferences(FrrStructureType.VRF, "foo", FrrStructureUsage.BGP_VRF),
        contains(1));
  }

  @Test
  public void testBgpAddressFamily_ipv4Unicast() {
    parse("router bgp 1\n address-family ipv4 unicast\n exit-address-family\n");
    assertNotNull(_frr.getBgpProcess().getDefaultVrf().getIpv4Unicast());
  }

  @Test
  public void testBgpAddressFamily_ipv4UnicastMaximumPaths() {
    // do not crash
    parse("router bgp 1\n address-family ipv4 unicast\n maximum-paths 4\nexit-address-family\n");
  }

  @Test
  public void testBgpAddressFamily_ipv6UnicastMaximumPaths() {
    parse("router bgp 1\n address-family ipv6 unicast\n maximum-paths 4\nexit-address-family\n");
  }

  @Test
  public void testBgpAddressFamily_ipv4UnicastMaximumPathsIbgp() {
    parse(
        "router bgp 1\n address-family ipv4 unicast\n maximum-paths ibgp 4\nexit-address-family\n");
  }

  @Test
  public void testBgpAddressFamily_ipv6UnicastMaximumPathsIbgp() {
    parse(
        "router bgp 1\n address-family ipv6 unicast\n maximum-paths ibgp 4\nexit-address-family\n");
  }

  /** Make sure that we warn when import statements are ignored but still do reference counting */
  @Test
  public void testBgpAddressFamilyIpv4UnicastImport() {
    parseLines(
        "router bgp 1",
        " address-family ipv4 unicast",
        "  import vrf Vrf_storage1",
        "  import vrf route-map import6-vrf-deny",
        " exit-address-family");
    assertThat(
        _warnings.getParseWarnings(),
        contains(
            hasText(equalTo("import vrf Vrf_storage1")),
            hasText(equalTo("import vrf route-map import6-vrf-deny"))));
    assertThat(
        getStructureReferences(VRF, "Vrf_storage1", BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF),
        contains(3));
    assertThat(
        getStructureReferences(ROUTE_MAP, "import6-vrf-deny", BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF),
        contains(4));
  }

  /** Make sure we do reference counting for import statements */
  @Test
  public void testBgpAddressFamilyIpv6UnicastImport() {
    parseLines(
        "router bgp 1",
        " address-family ipv6 unicast",
        "  import vrf Vrf_storage1",
        "  import vrf route-map import6-vrf-deny",
        " exit-address-family");
    assertThat(
        getStructureReferences(VRF, "Vrf_storage1", BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF),
        contains(3));
    assertThat(
        getStructureReferences(ROUTE_MAP, "import6-vrf-deny", BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF),
        contains(4));
  }

  /** Make sure that we warn when no statements are ignored */
  @Test
  public void testBgpAddressFamilyIpv4UnicastNo() {
    parseLines(
        "router bgp 1",
        "neighbor N interface description N",
        "address-family ipv4 unicast",
        "redistribute connected",
        "neighbor N activate",
        "no neighbor N activate",
        "exit-address-family");

    assertFalse(
        _frr.getBgpProcess().getDefaultVrf().getIpv4UnicastConfiguration("N").getActivated());
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastNetwork() {
    parseLines(
        "router bgp 1", "address-family ipv4 unicast", "network 1.2.3.4/24", "exit-address-family");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getNetworks().keySet(),
        contains(Prefix.parse("1.2.3.4/24")));
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastNetworkWithRouteMap() {
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "network 1.2.3.4/24 route-map FOO",
        "exit-address-family");
    Prefix prefix = Prefix.parse("1.2.3.4/24");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getNetworks().get(prefix),
        equalTo(new BgpNetwork(prefix, "FOO")));
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastRedistributeConnected() {
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "redistribute connected",
        "exit-address-family");
    BgpRedistributionPolicy policy =
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4Unicast()
            .getRedistributionPolicies()
            .get(CONNECTED);
    assertNotNull(policy);
    assertNull(policy.getRouteMap());
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastRedistributeRouteMap() {
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "redistribute connected route-map foo",
        "exit-address-family");
    BgpRedistributionPolicy policy =
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4Unicast()
            .getRedistributionPolicies()
            .get(CONNECTED);
    assertNotNull(policy);
    assertThat(policy.getRouteMap(), equalTo("foo"));
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastMultipleDefinitions() {
    parseLines(
        "router bgp 1",
        " address-family ipv4 unicast",
        "  network 1.2.3.4/24",
        " exit-address-family",
        " address-family ipv4 unicast",
        "  network 2.2.3.4/24",
        " exit-address-family");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getNetworks().keySet(),
        contains(Prefix.parse("1.2.3.4/24"), Prefix.parse("2.2.3.5/24")));
  }

  @Test
  public void testBgpRedistributeOspfRouteMap() {
    parseLines("router bgp 1", "redistribute ospf route-map foo");
    BgpRedistributionPolicy policy =
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getRedistributionPolicies().get(OSPF);
    assertNotNull(policy);
    assertThat(policy.getRouteMap(), equalTo("foo"));
  }

  @Test
  public void testBgpRedistributeOspf() {
    parseLines("router bgp 1", "redistribute ospf");
    BgpRedistributionPolicy policy =
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getRedistributionPolicies().get(OSPF);
    assertNotNull(policy);
    assertNull(policy.getRouteMap());
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastRedistributeOspfRouteMap() {
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "redistribute ospf route-map foo",
        "exit-address-family");
    BgpRedistributionPolicy policy =
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getRedistributionPolicies().get(OSPF);
    assertNotNull(policy);
    assertThat(policy.getRouteMap(), equalTo("foo"));
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastRedistributeOspf() {
    parseLines(
        "router bgp 1", "address-family ipv4 unicast", "redistribute ospf", "exit-address-family");
    BgpRedistributionPolicy policy =
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getRedistributionPolicies().get(OSPF);
    assertNotNull(policy);
    assertNull(policy.getRouteMap());
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastRedistributeStatic() {
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "redistribute static",
        "exit-address-family");
    BgpRedistributionPolicy policy =
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4Unicast()
            .getRedistributionPolicies()
            .get(STATIC);
    assertNotNull(policy);
    assertNull(policy.getRouteMap());
  }

  @Test
  public void testBgpAddressFamilyIpv4UnicastAggregateAddress() {
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "aggregate-address 1.2.3.0/24",
        "exit-address-family");
    Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> aggregateNetworks =
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getAggregateNetworks();
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    assertThat(aggregateNetworks, hasKey(prefix));
    assertFalse(aggregateNetworks.get(prefix).isSummaryOnly());
  }

  @Test
  public void testBgpAddressFamily_l2vpn_evpn() {
    parse("router bgp 1\n address-family l2vpn evpn\n exit-address-family\n");
    assertNotNull(_frr.getBgpProcess().getDefaultVrf().getL2VpnEvpn());
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnAdvertiseAllVni() {
    parseLines(
        "router bgp 1", "address-family l2vpn evpn", "advertise-all-vni", "exit-address-family");
    assertTrue(_frr.getBgpProcess().getDefaultVrf().getL2VpnEvpn().getAdvertiseAllVni());
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnAdvertiseDefaultGw() {
    parseLines(
        "router bgp 1", "address-family l2vpn evpn", "advertise-default-gw", "exit-address-family");
    assertTrue(_frr.getBgpProcess().getDefaultVrf().getL2VpnEvpn().getAdvertiseDefaultGw());
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnAdvertiseIpv4Unicast() {
    parseLines(
        "router bgp 1",
        "address-family l2vpn evpn",
        "advertise ipv4 unicast",
        "exit-address-family");
    assertNotNull(_frr.getBgpProcess().getDefaultVrf().getL2VpnEvpn().getAdvertiseIpv4Unicast());
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnAdvertiseIpv4UnicastRouteMap() {
    parseLines(
        "router bgp 1",
        "address-family l2vpn evpn",
        "advertise ipv4 unicast route-map RM",
        "exit-address-family");
    assertThat(
        _warnings.getParseWarnings(),
        contains(hasComment("Route maps in 'advertise ipv4 unicast' are not supported")));
    assertThat(
        getStructureReferences(
            FrrStructureType.ROUTE_MAP,
            "RM",
            FrrStructureUsage.BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV4_UNICAST),
        contains(3));
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnAdvertiseIpv6Unicast() {
    parseLines(
        "router bgp 1",
        "address-family l2vpn evpn",
        "advertise ipv6 unicast",
        "advertise ipv6 unicast route-map RM",
        "exit-address-family");
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(
        getStructureReferences(
            FrrStructureType.ROUTE_MAP,
            "RM",
            FrrStructureUsage.BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV6_UNICAST),
        contains(4));
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnNeighborActivate() {
    parseLines(
        "router bgp 1",
        "neighbor n interface description a",
        "neighbor 1.2.3.4 description a",
        "address-family l2vpn evpn",
        "neighbor n activate",
        "neighbor 1.2.3.4 activate",
        "exit-address-family");
    Map<String, BgpNeighborL2vpnEvpnAddressFamily> neighbors =
        _frr.getBgpProcess().getDefaultVrf().getL2VpnEvpn().getNeighbors();
    assertTrue(neighbors.get("n").getActivated());
    assertTrue(neighbors.get("1.2.3.4").getActivated());
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnNeighborRouteMap() {
    parseLines(
        "router bgp 1",
        " neighbor n interface description a",
        " address-family l2vpn evpn",
        "  neighbor n route-map rm in",
        "  neighbor n route-map rm out",
        " exit-address-family");
    assertThat(
        getStructureReferences(
            ROUTE_MAP, "rm", FrrStructureUsage.BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_IN),
        contains(4));
    assertThat(
        getStructureReferences(
            ROUTE_MAP, "rm", FrrStructureUsage.BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_OUT),
        contains(5));
    assertThat(
        _warnings.getParseWarnings(),
        contains(
            hasComment(
                "Routes maps on neighbors in address-family  'l2vpn evpn' are not supported"),
            hasComment(
                "Routes maps on neighbors in address-family  'l2vpn evpn' are not supported")));
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnNeighborRouteReflectorClient() {
    parseLines(
        "router bgp 1",
        "neighbor n interface description a",
        "address-family l2vpn evpn",
        "neighbor n route-reflector-client",
        "exit-address-family");
    assertTrue(
        _frr.getBgpProcess().getDefaultVrf().getL2EvpnConfiguration("n").getRouteReflectorClient());
  }

  @Test
  public void testBgpAddressFamilyL2vpnEvpnMultipleDefinitions() {
    parseLines(
        "router bgp 1",
        " neighbor n interface description a",
        " neighbor 1.2.3.4 description a",
        " address-family l2vpn evpn",
        "   neighbor n activate",
        " address-family l2vpn evpn",
        "   neighbor 1.2.3.4 activate");
    Map<String, BgpNeighborL2vpnEvpnAddressFamily> neighbors =
        _frr.getBgpProcess().getDefaultVrf().getL2VpnEvpn().getNeighbors();
    assertTrue(neighbors.get("n").getActivated());
    assertTrue(neighbors.get("1.2.3.4").getActivated());
  }

  @Test
  public void testBgpAddressFamilyNeighborNextHopSelf() {
    parseLines(
        "router bgp 1",
        "neighbor 10.0.0.1 description x",
        "address-family ipv4 unicast",
        "neighbor 10.0.0.1 next-hop-self",
        "exit-address-family");
    assertTrue(
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4UnicastConfiguration("10.0.0.1")
            .getNextHopSelf());
  }

  @Test
  public void testBgpAlwaysCompareMed() {
    parse("router bgp 1\n bgp always-compare-med\n");
  }

  @Test
  public void testBgpAddressFamilyNeighborActivate() {
    parseLines(
        "router bgp 1",
        "neighbor N interface description N",
        "address-family ipv4 unicast",
        "neighbor N activate",
        "exit-address-family");
    assertTrue(
        _frr.getBgpProcess().getDefaultVrf().getIpv4UnicastConfiguration("N").getActivated());
  }

  @Test
  public void testBgpAddressFamilyNeighborAllowAsIn() {
    parseLines(
        "router bgp 1",
        "neighbor N interface description N",
        "address-family ipv4 unicast",
        "neighbor N allowas-in 5",
        "exit-address-family");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4UnicastConfiguration("N").getAllowAsIn(),
        equalTo(5));
  }

  @Test
  public void testBgpNeighborDoesntCheckLocalIp() {
    parseLines(
        "router bgp 1",
        "neighbor 10.0.0.1 remote-as 1",
        "neighbor PG peer-group",
        "neighbor PG remote-as 3",
        "bgp listen range 20.0.0.0/8 peer-group PG");
    Configuration c = _config.toVendorIndependentConfigurations().get(0);
    BgpProcess p = c.getDefaultVrf().getBgpProcess();
    assertThat(
        p.getActiveNeighbors().get(Ip.parse("10.0.0.1")).getCheckLocalIpOnAccept(), equalTo(false));
    assertThat(
        p.getPassiveNeighbors().get(Prefix.parse("20.0.0.0/8")).getCheckLocalIpOnAccept(),
        equalTo(false));
  }

  @Test
  public void testBgpAddressFamilyNeighborAllowAsInDefault() {
    parseLines(
        "router bgp 1",
        "neighbor N interface description N",
        "address-family ipv4 unicast",
        "neighbor N allowas-in",
        "exit-address-family");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4UnicastConfiguration("N").getAllowAsIn(),
        equalTo(3));
  }

  @Test
  public void testBgpAddressFamilyNeighborDefaultOriginate_parsing() {
    parseLines(
        "router bgp 1",
        "  neighbor N interface description N",
        "  neighbor N2 interface description N2",
        "  address-family ipv4 unicast",
        "    neighbor N default-originate",
        "    neighbor N2 default-originate route-map RM",
        "  exit-address-family");
    Map<String, BgpNeighborIpv4UnicastAddressFamily> bgpNeighbors =
        _frr.getBgpProcess().getDefaultVrf().getIpv4Unicast().getNeighbors();
    assertTrue(bgpNeighbors.get("N").getDefaultOriginate());
    assertNull(bgpNeighbors.get("N").getDefaultOriginateRouteMap());
    assertTrue(bgpNeighbors.get("N2").getDefaultOriginate());
    assertEquals("RM", bgpNeighbors.get("N2").getDefaultOriginateRouteMap());
  }

  @Test
  public void testBgpAddressFamilyNeighborDefaultOriginate_behavior() throws IOException {
    /*
     There are four nodes in the topology arranged in a line.
       - frr-originator -- frr-reoriginator -- frr-propagator -- ios-listener

     frr-originator has default-originate.

     frr-reoriginator also has default-originate. it should generate a fresh route not propagate
        the one it got from frr-originator. it also has a route map toward frr-propagator that
        drops default (but shouldn't interfere with default-originate)

     frr-propagator does not default-originate. it should propagate the default it got from
         frr-reoriginator to ios-listener
    */

    String snapshotName = "default-originate";
    List<String> configurationNames =
        ImmutableList.of("frr-originator", "frr-reoriginator", "frr-propagator", "ios-listener");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);

    // frr-reoriginator should get a default route from frr-originator
    assertThat(
        dp.getRibs().get("frr-reoriginator", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopIp(Ip.parse("10.1.1.1"))
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.1.1.1")))
                    .setOriginatorIp(Ip.parse("1.1.1.1"))
                    .setOriginType(OriginType.INCOMPLETE)
                    .setProtocol(RoutingProtocol.BGP)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .setAsPath(AsPath.ofSingletonAsSets(1L))
                    .setAdmin(20)
                    .setLocalPreference(100)
                    .build())));

    // frr-propagator should get a fresh default route from frr-originator
    assertThat(
        dp.getRibs().get("frr-propagator", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopIp(Ip.parse("20.1.1.2"))
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("20.1.1.2")))
                    .setOriginatorIp(Ip.parse("2.2.2.2"))
                    .setOriginType(OriginType.INCOMPLETE)
                    .setProtocol(RoutingProtocol.BGP)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .setAsPath(AsPath.ofSingletonAsSets(2L)) // fresh route with only one AS
                    .setAdmin(20)
                    .setLocalPreference(100)
                    .build())));

    // ios-listener should get a propagated route from frr-propagator
    assertThat(
        dp.getRibs().get("ios-listener", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopIp(Ip.parse("30.1.1.3"))
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("30.1.1.3")))
                    .setOriginatorIp(Ip.parse("3.3.3.3"))
                    .setOriginType(OriginType.INCOMPLETE)
                    .setProtocol(RoutingProtocol.BGP)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .setAsPath(AsPath.ofSingletonAsSets(3L, 2L)) // propagated route
                    .setAdmin(20)
                    .setLocalPreference(100)
                    .build())));
  }

  /**
   * Test that default routes originated via default-originate are subjected to configured route
   * maps.
   */
  @Test
  public void testBgpAddressFamilyNeighborDefaultOriginate_routeMap() throws IOException {
    /*
     The topology has one originator and three listeners: 1, 3, 4
      - listener1 has default-originate with empty match conditions.
      - listener3 has default-originate with a match condition that succeeds.
      - listener4 has default-originate with a match condition that fails.
     All route-maps are prepending, so we can test if attributes are being set.
    */

    String snapshotName = "default-originate-route-map";
    List<String> configurationNames =
        ImmutableList.of("frr-originator", "frr-listener1", "frr-listener3", "frr-listener4");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);

    assertThat(
        dp.getRibs().get("frr-listener1", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopIp(Ip.parse("10.1.1.2"))
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.1.1.2")))
                    .setOriginatorIp(Ip.parse("2.2.2.2"))
                    .setOriginType(OriginType.INCOMPLETE)
                    .setProtocol(RoutingProtocol.BGP)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .setAsPath(AsPath.ofSingletonAsSets(2L, 23L))
                    .setAdmin(20)
                    .setLocalPreference(100)
                    .build())));

    assertThat(
        dp.getRibs().get("frr-listener3", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopIp(Ip.parse("10.1.1.2"))
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.1.1.2")))
                    .setOriginatorIp(Ip.parse("2.2.2.2"))
                    .setOriginType(OriginType.INCOMPLETE)
                    .setProtocol(RoutingProtocol.BGP)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .setAsPath(AsPath.ofSingletonAsSets(2L, 23L))
                    .setAdmin(20)
                    .setLocalPreference(100)
                    .build())));
    assertThat(
        dp.getRibs().get("frr-listener4", DEFAULT_VRF_NAME).getRoutes(),
        not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  @Test
  public void testBgpAddressFamilyNeighborRemovePrivateAs() {
    parseLines(
        "router bgp 1",
        "  neighbor N1 interface description N",
        "  neighbor N2 interface description N",
        "  neighbor N3 interface description N",
        "  address-family ipv4 unicast",
        "    neighbor N1 remove-private-AS",
        "    neighbor N2 remove-private-AS all",
        "    neighbor N3 remove-private-AS all replace-as",
        "  exit-address-family");
    assertThat(
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4UnicastConfiguration("N1")
            .getRemovePrivateAsMode(),
        equalTo(RemovePrivateAsMode.BASIC));
    assertThat(
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4UnicastConfiguration("N2")
            .getRemovePrivateAsMode(),
        equalTo(RemovePrivateAsMode.ALL));
    assertThat(
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4UnicastConfiguration("N3")
            .getRemovePrivateAsMode(),
        equalTo(RemovePrivateAsMode.REPLACE_AS));
  }

  @Test
  public void testBgpAddressFamilyNeighborRouteReflectorClient() {
    parseLines(
        "router bgp 1",
        "neighbor N interface description N",
        "address-family ipv4 unicast",
        "neighbor N route-reflector-client",
        "exit-address-family");
    assertTrue(
        _frr.getBgpProcess()
            .getDefaultVrf()
            .getIpv4UnicastConfiguration("N")
            .getRouteReflectorClient());
  }

  @Test
  public void testBgpAddressFamilyNeighborSendCommunity() {
    // No extraction because these are already enabled by default
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "neighbor N send-community",
        "exit-address-family");
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "neighbor N send-community extended",
        "exit-address-family");
  }

  @Test
  public void testBgpAddressFamilyNeighborSoftReconfiguration() {
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "neighbor N soft-reconfiguration inbound",
        "exit-address-family");
    parseLines(
        "router bgp 1",
        "address-family ipv4 unicast",
        "neighbor 10.0.0.1 soft-reconfiguration inbound",
        "exit-address-family");
  }

  @Test
  public void testBgpAddressFamilyNoExit() {
    parseLines(
        "router bgp 1", "address-family ipv4 unicast", "neighbor N soft-reconfiguration inbound");
  }

  @Test
  public void testBgpNeighbor_peerGroup() {
    parse("router bgp 1\n neighbor foo peer-group\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("foo"));
    assertThat(neighbors.get("foo"), isA(BgpPeerGroupNeighbor.class));
  }

  @Test
  public void testBgpNeighbor_peerGroup_remote_as() {
    parse("router bgp 1\n neighbor foo peer-group\n neighbor foo remote-as 2\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("foo"));
    BgpNeighbor foo = neighbors.get("foo");
    assertThat(foo, isA(BgpPeerGroupNeighbor.class));
    assertThat(foo.getRemoteAs(), equalTo(RemoteAs.explicit(2L)));
  }

  @Test
  public void testBgpNeighbor_interface() {
    parse("router bgp 1\n neighbor foo interface remote-as 2\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("foo"));
    assertThat(neighbors.get("foo"), isA(BgpInterfaceNeighbor.class));
  }

  @Test
  public void testBgpNeighborProperty_descrption() {
    parse("router bgp 1\n neighbor n interface description a b c! d\n");
    BgpNeighbor neighbor = _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("n");
    assertThat(neighbor.getDescription(), equalTo("a b c! d"));
  }

  @Test
  public void testBgpNeighborProperty_remoteAs_explicit() {
    parse("router bgp 1\n neighbor n interface remote-as 2\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("n"));
    BgpNeighbor foo = neighbors.get("n");
    assertThat(foo.getRemoteAs(), equalTo(RemoteAs.explicit(2)));
  }

  @Test
  public void testBgpNeighborProperty_remoteAs_external() {
    parse("router bgp 1\n neighbor n interface remote-as external\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("n"));
    BgpNeighbor foo = neighbors.get("n");
    assertThat(foo.getRemoteAs(), equalTo(RemoteAs.external()));
  }

  @Test
  public void testBgpNeighborProperty_remoteAs_internal() {
    parse("router bgp 1\n neighbor n interface remote-as internal\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("n"));
    BgpNeighbor foo = neighbors.get("n");
    assertThat(foo.getRemoteAs(), equalTo(RemoteAs.internal()));
  }

  @Test
  public void testBgpNeighborProperty_peerGroup() {
    parse("router bgp 1\n neighbor n interface peer-group pg\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("n"));
    BgpNeighbor foo = neighbors.get("n");
    assertThat(foo.getPeerGroup(), equalTo("pg"));
  }

  @Test
  public void testBgpNeighbor_ip() {
    parse("router bgp 1\n neighbor 1.2.3.4 remote-as 2\n");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("1.2.3.4"));
    BgpNeighbor neighbor = neighbors.get("1.2.3.4");
    assertThat(neighbor, isA(BgpIpNeighbor.class));
    assertThat(neighbor.getRemoteAs(), equalTo(RemoteAs.explicit(2L)));
  }

  @Test
  public void testBgpNeighborCompatiblity() throws IOException {
    String snapshotName = "bgp-neighbor-compatibility";
    /*
    There are two nodes in the snapshot, each with XX interfaces
      u swp1: both nodes have an interface neighbor with no IP address
      n swp2: both nodes have an interface neighbor with a /31 address (same subnet)
      u swp3: both nodes have an interface neighbor with a /24 address (same subnet)
      u swp4: both nodes have an interface neighbor with a /31 and a /24 address
      - swp5: node1 has an interface neighbor with /31 and node2 has an interface neighbor with no IP address
      u swp6: node1 has an interface neighbor with /24 and node2 has an interface neighbor with no IP address
      u swp7: node1 has an interface neighbor with /31 and /24 addresses and node 2 has an interface neighbor with no IP address
      n swp8: node1 has an interface neighbor with /31 and node2 has an IP neighbor in the same subnet
      - swp9: node1 has an interface neighbor with /24 and node2 has an IP neighbor in the same subnet
      n swp10: both nodes have an interface neighbor with /30 addresses (host addresses in same subnet)

    The layer1 topology file connects matching swpX interfaces on each node (swp1<>swp1, ...)

    Combinations marked 'u' should be unnumbered sessions, combinations marked 'n' should be numbered sessions, and those marked '-' are invalid combinations.
     */
    List<String> configurationNames = ImmutableList.of("node1", "node2");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .setLayer1TopologyPrefix(SNAPSHOTS_PREFIX + snapshotName)
                .build(),
            _folder);

    batfish.computeDataPlane(batfish.getSnapshot());

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpGraph =
        batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot()).getGraph();

    // unnumbered sessions
    assertThat(
        bgpGraph.edges().stream()
            .map(e -> e.nodeU().getPeerInterface())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        containsInAnyOrder("swp1", "swp3", "swp4", "swp6", "swp7"));

    // numbered sessions: swp2 and swp8
    assertThat(
        bgpGraph.edges().stream()
            .map(e -> e.nodeU().getRemotePeerPrefix())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        containsInAnyOrder(
            Prefix.parse("2.2.2.0/32"),
            Prefix.parse("2.2.2.1/32"),
            Prefix.parse("8.8.8.0/32"),
            Prefix.parse("8.8.8.1/32"),
            Prefix.parse("10.10.10.1/32"),
            Prefix.parse("10.10.10.2/32")));
  }

  /** Test that passive neighbors can establish sessions. */
  @Test
  public void testBgpDynamicNeighbor() throws IOException {
    String snapshotName = "bgp-passive";
    List<String> configurationNames = ImmutableList.of("frr-passive", "frr-active");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    batfish.computeDataPlane(batfish.getSnapshot());

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpGraph =
        batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot()).getGraph();

    assertThat(
        bgpGraph.edges().stream()
            .map(e -> e.nodeU().getRemotePeerPrefix())
            .collect(Collectors.toSet()),
        containsInAnyOrder(Prefix.parse("10.1.1.0/24"), Prefix.parse("10.1.1.5/32")));
  }

  @Test
  public void testBgpListenParsing() {
    parseLines(
        "router bgp 1",
        "  neighbor PG peer-group",
        "  bgp listen range 172.19.0.0/24 peer-group PG",
        "  bgp listen range 2001:100:1:31::2/64 peer-group PG",
        "  bgp listen limit 42");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(
        neighbors.keySet(),
        containsInAnyOrder("PG", "172.19.0.0/24", Prefix6.parse("2001:100:1:31::2/64").toString()));
    assertThat(neighbors.get("172.19.0.0/24").getPeerGroup(), equalTo("PG"));
    assertThat(
        neighbors.get(Prefix6.parse("2001:100:1:31::2/64").toString()).getPeerGroup(),
        equalTo("PG"));
    assertThat(
        _warnings.getParseWarnings(),
        contains(
            hasComment("Batfish does not limit the number sessions for dynamic BGP neighbors")));
  }

  @Test
  public void testBgpNeighbor_refs() {
    parseLines(
        "router bgp 1",
        "neighbor 1.1.1.1 description ip-neighbor",
        "neighbor 2001:db8:85a3:0:0:8a2e:0370:7334 description ipv6-neighbor",
        "neighbor swp1 interface description interface-neighbor",
        "bgp listen range 1.2.3.0/24 peer-group PG",
        "bgp listen range 2001:db8:85a3::/48 peer-group PG");

    String neighborIp = bgpNeighborStructureName("1.1.1.1", "default");
    String neighborIp6 = bgpNeighborStructureName("2001:db8:85a3:0:0:8a2e:370:7334", "default");
    String neighborPrefix = bgpNeighborStructureName("1.2.3.0/24", "default");
    String neighborPrefix6 = bgpNeighborStructureName("2001:db8:85a3:0:0:0:0:0/48", "default");
    String neighborInterface = bgpNeighborStructureName("swp1", "default");

    assertThat(
        getDefinedStructureInfo(BGP_NEIGHBOR, neighborIp).getDefinitionLines().enumerate(),
        contains(2));
    assertThat(
        getDefinedStructureInfo(BGP_NEIGHBOR, neighborIp6).getDefinitionLines().enumerate(),
        contains(3));
    assertThat(
        getDefinedStructureInfo(BGP_NEIGHBOR_INTERFACE, neighborInterface)
            .getDefinitionLines()
            .enumerate(),
        contains(4));
    assertThat(
        getDefinedStructureInfo(BGP_LISTEN_RANGE, neighborPrefix).getDefinitionLines().enumerate(),
        contains(5));
    assertThat(
        getDefinedStructureInfo(BGP_LISTEN_RANGE, neighborPrefix6).getDefinitionLines().enumerate(),
        contains(6));

    assertThat(
        getStructureReferences(BGP_NEIGHBOR, neighborIp, BGP_NEIGHBOR_SELF_REF), contains(2));
    assertThat(
        getStructureReferences(BGP_NEIGHBOR, neighborIp6, BGP_NEIGHBOR_SELF_REF), contains(3));
    assertThat(
        getStructureReferences(
            BGP_NEIGHBOR_INTERFACE, neighborInterface, BGP_NEIGHBOR_INTERFACE_SELF_REF),
        contains(4));
    assertThat(
        getStructureReferences(BGP_LISTEN_RANGE, neighborPrefix, BGP_LISTEN_RANGE_SELF_REF),
        contains(5));
    assertThat(
        getStructureReferences(BGP_LISTEN_RANGE, neighborPrefix6, BGP_LISTEN_RANGE_SELF_REF),
        contains(6));
  }

  @Test
  public void testBgp_Ipv6() {
    parseLines(
        "router bgp 1",
        "  neighbor 2001:100:1:31::2 remote-as 2",
        "  neighbor 2001:100:1:31::2 timers connect 15",
        "  neighbor 2001:100:1:31::2 advertisement-interval 0",
        "  neighbor 2001:100:1:31::2 local-as 65534 no-prepend replace-as",
        "  address-family ipv6 unicast",
        "    redistribute connected",
        "    import vrf Vrf_tenant1",
        "    neighbor 2001:100:1:31::2 route-map wanguard6-any-out out",
        "    neighbor 2001:100:1:31::2 next-hop-self",
        "    neighbor 2001:100:1:31::2 activate",
        "    aggregate-address 2a02:4780:9::/48",
        "    network 2a02:4780:9::/48 route-map internal",
        "  address-family ipv4 unicast",
        "    neighbor 2001:100:1:31::2 remove-private-AS",
        "    neighbor 2001:100:1:31::2 next-hop-self",
        "    neighbor 2001:100:1:31::2 allowas-in",
        "    neighbor 2001:100:1:31::2 route-map rm-out out",
        "    neighbor 2001:100:1:31::2 route-map rm-in in");
    Map<String, BgpNeighbor> neighbors = _frr.getBgpProcess().getDefaultVrf().getNeighbors();
    assertThat(neighbors.keySet(), contains("2001:100:1:31:0:0:0:2"));
    BgpNeighbor foo = neighbors.get("2001:100:1:31:0:0:0:2");
    assertThat(foo.getRemoteAs(), equalTo(RemoteAs.explicit(2)));
    assertThat(_warnings.getParseWarnings(), empty());
  }

  @Test
  public void testBgpBestpathAsPathMultipathRelax() {
    parse("router bgp 1\n bgp bestpath as-path multipath-relax\n");
    assertTrue(_frr.getBgpProcess().getDefaultVrf().getAsPathMultipathRelax());
  }

  @Test
  public void testBgpMaxMedAdministrative_value() {
    parse("router bgp 1\n bgp max-med administrative 12345\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getMaxMedAdministrative(), equalTo(12345L));
  }

  @Test
  public void testBgpMaxMedAdministrative_default() {
    parse("router bgp 1\n bgp max-med administrative\n");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getMaxMedAdministrative(), equalTo(4294967294L));
  }

  @Test
  public void testBgpMaxMedAdministrative_unset() {
    parse("router bgp 1\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getMaxMedAdministrative(), equalTo(null));
  }

  @Test
  public void testBgpMaxMedOnStartup_timeonly() {
    parse("router bgp 1\n bgp max-med on-startup 15\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getMaxMedAdministrative(), nullValue());
  }

  @Test
  public void testBgpMaxMedOnStartup_timeandmed() {
    parse("router bgp 1\n bgp max-med on-startup 15 4000000000\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getMaxMedAdministrative(), nullValue());
  }

  @Test
  public void testBgpRouterId() {
    parse("router bgp 1\n bgp router-id 1.2.3.4\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getRouterId(), equalTo(Ip.parse("1.2.3.4")));
  }

  @Test
  public void testBgpClusterId_set() {
    parse("router bgp 1\n bgp router-id 1.2.3.4\n bgp cluster-id 2.2.2.2\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getClusterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testBgpClusterId_unset() {
    parse("router bgp 1\n bgp router-id 1.2.3.4\n");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getClusterId(), equalTo(null));
  }

  @Test
  public void testBgpNoDefaultIpv4Unicast() {
    parse("router bgp 1\n no bgp default ipv4-unicast\n");
    assertFalse(_frr.getBgpProcess().getDefaultVrf().getDefaultIpv4Unicast());
  }

  @Test
  public void testHostname() {
    parse("hostname asdf235jgij981\n");
  }

  @Test
  public void testFrrVrf() {
    _frr.getVrfs().put("NAME", new Vrf("NAME"));
    parse("vrf NAME\n exit-vrf\n");
    assertThat(
        getDefinedStructureInfo(FrrStructureType.VRF, "NAME").getDefinitionLines().enumerate(),
        contains(1, 2));
  }

  @Test
  public void testFrrVrfVni() {
    Vrf vrf = new Vrf("NAME");
    _frr.getVrfs().put("NAME", vrf);
    parse("vrf NAME\n vni 170000\n exit-vrf\n");
    assertThat(vrf.getVni(), equalTo(170000));
  }

  @Test
  public void testFrrVrfIpRoutes() {
    Vrf vrf = new Vrf("NAME");
    _frr.getVrfs().put("NAME", vrf);
    parse("vrf NAME\n ip route 1.0.0.0/8 10.0.2.1\n ip route 0.0.0.0/0 10.0.0.1\n exit-vrf\n");
    assertThat(
        vrf.getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                new StaticRoute(Prefix.parse("1.0.0.0/8"), Ip.parse("10.0.2.1"), null, null),
                new StaticRoute(Prefix.parse("0.0.0.0/0"), Ip.parse("10.0.0.1"), null, null))));
  }

  @Test
  public void testFrrVrfIpRoutes_blackhole() {
    Vrf vrf = new Vrf("NAME");
    _frr.getVrfs().put("NAME", vrf);
    parse("vrf NAME\n ip route 1.0.0.0/8 blackhole\n exit-vrf\n");
    assertThat(
        vrf.getStaticRoutes(),
        equalTo(
            ImmutableSet.of(new StaticRoute(Prefix.parse("1.0.0.0/8"), null, "blackhole", null))));
  }

  @Test
  public void testFrrVrf_noExit() {
    Vrf vrf = new Vrf("NAME");
    _frr.getVrfs().put("NAME", vrf);
    parse("vrf NAME\n vni 170000\n");
    assertTrue(_warnings.getParseWarnings().isEmpty());
  }

  @Test
  public void testFrrRouteMap() {
    String name = "ROUTE-MAP-NAME";
    parse(String.format("route-map %s permit 10\nroute-map %s deny 20\n", name, name));
    assertThat(_frr.getRouteMaps().keySet(), equalTo(ImmutableSet.of(name)));

    RouteMap rm = _frr.getRouteMaps().get(name);
    assertThat(rm.getEntries().keySet(), equalTo(ImmutableSet.of(10, 20)));

    RouteMapEntry entry1 = rm.getEntries().get(10);
    assertThat(entry1.getAction(), equalTo(LineAction.PERMIT));

    RouteMapEntry entry2 = rm.getEntries().get(20);
    assertThat(entry2.getAction(), equalTo(LineAction.DENY));

    assertThat(
        getDefinedStructureInfo(FrrStructureType.ROUTE_MAP, name).getDefinitionLines().enumerate(),
        equalTo(ImmutableSet.of(1, 2)));
    assertThat(
        getDefinedStructureInfo(
                FrrStructureType.ROUTE_MAP_ENTRY, computeRouteMapEntryName(name, 10))
            .getDefinitionLines()
            .enumerate(),
        equalTo(ImmutableSet.of(1)));
    assertThat(
        getStructureReferences(
            FrrStructureType.ROUTE_MAP_ENTRY,
            computeRouteMapEntryName(name, 10),
            FrrStructureUsage.ROUTE_MAP_ENTRY_SELF_REFERENCE),
        contains(1));
  }

  @Test
  public void testFrrVrfRouteMapDescription() {
    String name = "ROUTE-MAP-NAME";
    String description = "PERmit Xxx Yy_+!@#$%^&*()";

    parse(String.format("route-map %s permit 10\ndescription %s\n", name, description));

    RouteMap rm = _frr.getRouteMaps().get(name);
    RouteMapEntry entry1 = rm.getEntries().get(10);
    assertThat(entry1.getDescription(), equalTo(description));
  }

  @Test
  public void testRouteMapMatchAsPathAccessList() {
    parseLines("route-map ROUTE_MAP permit 10", "match as-path AS_PATH_ACCESS_LIST");
  }

  @Test
  public void testRouteMapMatchIpv6() {
    parseLines("route-map ROUTE_MAP permit 10", "match ipv6 address prefix-list pfx6-vrf-deny");
    assertTrue(_warnings.getParseWarnings().isEmpty());
  }

  @Test
  public void testFrrVrfRouteMapMatchCallExtraction() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\ncall SUB-MAP\n", name));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getCall().getRouteMapName(), equalTo("SUB-MAP"));
    assertThat(
        getStructureReferences(
            FrrStructureType.ROUTE_MAP, "SUB-MAP", FrrStructureUsage.ROUTE_MAP_CALL),
        contains(2));
  }

  @Test
  public void testFrrVrfRouteMapOnMatchNext() {
    String name = "ROUTE-MAP-NAME";
    parse(String.format("route-map %s permit 10\non-match next\n", name));
    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertNotNull(entry.getContinue());
    assertNull(entry.getContinue().getNext());
  }

  @Test
  public void testFrrVrfRouteMapOnMatchGoto() {
    String name = "ROUTE-MAP-NAME";
    parse(String.format("route-map %s permit 10\non-match goto 20\n", name));
    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertNotNull(entry.getContinue());
    assertThat(entry.getContinue().getNext(), equalTo(20));
  }

  @Test
  public void testFrrVrfRouteMapMatchAsPath() {
    String routeMapName = "ROUTE-MAP-NAME";
    String asPathName1 = "AS-PATH-1";
    String asPathName2 = "AS-PATH-2";

    // Second match as-path line should overwrite first.
    parseLines(
        String.format("route-map %s permit 10", routeMapName),
        String.format("match as-path %s", asPathName1),
        String.format("match as-path %s", asPathName2));

    RouteMapEntry entry = _frr.getRouteMaps().get(routeMapName).getEntries().get(10);
    assertThat(entry.getMatchAsPath().getName(), equalTo(asPathName2));

    // Both AS paths should be referenced.
    assertThat(
        getStructureReferences(BGP_AS_PATH_ACCESS_LIST, asPathName1, ROUTE_MAP_MATCH_AS_PATH),
        contains(2));
    assertThat(
        getStructureReferences(BGP_AS_PATH_ACCESS_LIST, asPathName2, ROUTE_MAP_MATCH_AS_PATH),
        contains(3));
  }

  @Test
  public void testFrrVrfRouteMapMatchCommunity() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nmatch community CN1 CN2\n", name));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getMatchCommunity().getNames(), equalTo(ImmutableList.of("CN1", "CN2")));

    assertThat(
        getStructureReferences(BGP_COMMUNITY_LIST, "CN1", ROUTE_MAP_MATCH_COMMUNITY_LIST),
        contains(2));
    assertThat(
        getStructureReferences(BGP_COMMUNITY_LIST, "CN2", ROUTE_MAP_MATCH_COMMUNITY_LIST),
        contains(2));
  }

  @Test
  public void testFrrVrfRouteMapMatchCommunity_multiline() {
    parseLines("route-map RM permit 10", "match community CN1", "match community CN2");

    RouteMapEntry entry = _frr.getRouteMaps().get("RM").getEntries().get(10);
    assertThat(entry.getMatchCommunity().getNames(), equalTo(ImmutableList.of("CN1", "CN2")));

    assertThat(
        getStructureReferences(BGP_COMMUNITY_LIST, "CN1", ROUTE_MAP_MATCH_COMMUNITY_LIST),
        contains(2));
    assertThat(
        getStructureReferences(BGP_COMMUNITY_LIST, "CN2", ROUTE_MAP_MATCH_COMMUNITY_LIST),
        contains(3));
  }

  @Test
  public void testFrrVrfRouteMapMatchPrefixLen() {
    String name = "ROUTE-MAP-NAME";
    String match1 = "match ip address prefix-len 7";
    String match2 = "match ip address prefix-len 8";

    parse(String.format("route-map %s permit 10\n%s\n%s\n", name, match1, match2));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getMatchIpAddressPrefixLen(), notNullValue());
    // Multiple commands overwrite - last one wins.
    assertThat(entry.getMatchIpAddressPrefixLen().getLen(), equalTo(8));
  }

  @Test
  public void testFrrVrfRouteMapMatchPrefixList() {
    String name = "ROUTE-MAP-NAME";
    String match1 = "match ip address prefix-list PREFIX_LIST1";
    String match2 = "match ip address prefix-list PREFIX_LIST2";

    parse(String.format("route-map %s permit 10\n%s\n%s\n", name, match1, match2));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(
        entry.getMatchIpAddressPrefixList().getNames(),
        equalTo(ImmutableList.of("PREFIX_LIST1", "PREFIX_LIST2")));

    Set<Integer> reference =
        getStructureReferences(
            FrrStructureType.IP_PREFIX_LIST,
            "PREFIX_LIST1",
            FrrStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST);
    assertThat(reference, equalTo(ImmutableSet.of(2)));

    reference =
        getStructureReferences(
            FrrStructureType.IP_PREFIX_LIST,
            "PREFIX_LIST2",
            FrrStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST);
    assertThat(reference, equalTo(ImmutableSet.of(3)));
  }

  @Test
  public void testFrrRouteMapMatchIpv6PrefixListRefTracking() {
    parseLines("route-map ROUTE_MAP1 permit 10", "match ipv6 address prefix-list PREFIX_LIST1");
    Set<Integer> reference =
        getStructureReferences(
            FrrStructureType.IPV6_PREFIX_LIST,
            "PREFIX_LIST1",
            FrrStructureUsage.ROUTE_MAP_MATCH_IPV6_ADDRESS_PREFIX_LIST);
    assertThat(reference, equalTo(ImmutableSet.of(2)));
  }

  @Test
  public void testFrrVrfRouteMapMatchSourceProtocol() {
    String name = "ROUTE-MAP-NAME";
    StringBuilder sb = new StringBuilder();
    List<String> protocols =
        ImmutableList.of("bgp", "connected", "eigrp", "isis", "kernel", "ospf", "rip", "static");
    for (int i = 0; i < protocols.size(); ++i) {
      int seq = 10 * (i + 1);
      sb.append("route-map ").append(name).append(" permit ").append(seq).append('\n');
      sb.append(" match source-protocol").append(protocols.get(i)).append('\n');
    }
    parse(sb.toString());

    SortedMap<Integer, RouteMapEntry> entries = _frr.getRouteMaps().get(name).getEntries();
    assertThat(entries, aMapWithSize(protocols.size()));
    assertThat(entries.get(10).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.BGP));
    assertThat(entries.get(20).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.CONNECTED));
    assertThat(entries.get(30).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.EIGRP));
    assertThat(entries.get(40).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.ISIS));
    assertThat(entries.get(50).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.KERNEL));
    assertThat(entries.get(60).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.OSPF));
    assertThat(entries.get(70).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.RIP));
    assertThat(entries.get(80).getMatchSourceProtocol().getProtocol(), equalTo(Protocol.STATIC));
  }

  @Test
  public void testFrrVrfRouteMapSetAsPath() {
    String name = "ROUTE-MAP-NAME";
    parse(String.format("route-map %s permit 10\nset as-path prepend 11111 22222 33333\n", name));
    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getSetAsPath().getAsns(), contains(11111L, 22222L, 33333L));
  }

  @Test
  public void testFrrVrfRouteMapSetExcludeAsPath() {
    String name = "ROUTE-MAP-NAME";
    parse(String.format("route-map %s permit 10\nset as-path exclude 11111 22222 33333\n", name));
    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getSetExcludeAsPath().getAsns(), contains(11111L, 22222L, 33333L));
  }

  @Test
  public void testFrrVrfRouteMapSetMetric() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nset metric 30\n", name));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getSetMetric().getMetric(), equalTo(new LiteralLong(30)));
  }

  @Test
  public void testFrrRouteMapSetMetricType() {
    String name = "ROUTE-MAP-NAME";

    parse(
        String.format(
            "route-map %s permit 10\nset metric-type type-1\n"
                + "route-map %s permit 20\nset metric-type type-2\n",
            name, name));
    {
      RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
      assertThat(entry.getSetMetricType().getMetricType(), equalTo(RouteMapMetricType.TYPE_1));
    }
    {
      RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(20);
      assertThat(entry.getSetMetricType().getMetricType(), equalTo(RouteMapMetricType.TYPE_2));
    }
  }

  @Test
  public void testFrrRouteMapSetOrigin() {
    String name = "ROUTE-MAP-NAME";
    parseLines(
        "route-map ROUTE-MAP-NAME permit 10",
        "set origin egp",
        "route-map ROUTE-MAP-NAME permit 20",
        "set origin igp",
        "route-map ROUTE-MAP-NAME permit 30",
        "set origin incomplete");
    {
      RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
      assertThat(entry.getSetOrigin().getOriginType(), equalTo(OriginType.EGP));
    }
    {
      RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(20);
      assertThat(entry.getSetOrigin().getOriginType(), equalTo(OriginType.IGP));
    }
    {
      RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(30);
      assertThat(entry.getSetOrigin().getOriginType(), equalTo(OriginType.INCOMPLETE));
    }
  }

  @Test
  public void testFrrRouteMapSetSrc() {
    _warnings = new Warnings(false, true, false);
    parseLines("route-map RM permit 10", "set src 1.1.1.1", "set src 2a02:4780:9:ffff::1");
    // there should be only one warning, for v4 line
    ParseWarning warning = Iterables.getOnlyElement(_warnings.getParseWarnings());
    assertThat(warning, hasText("src 1.1.1.1"));
  }

  @Test
  public void testFrrVrfRouteMapSetWeight() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nset weight 30\n", name));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getSetWeight().getWeight(), equalTo(30));
  }

  @Test
  public void testFrrBgpCommunityListExpanded() {
    String name = "NAME";

    parse(String.format("bgp community-list expanded %s permit 10000:10 20000:20\n", name));

    BgpCommunityListExpanded communityList =
        (BgpCommunityListExpanded) _frr.getBgpCommunityLists().get(name);

    List<BgpCommunityListExpandedLine> expected =
        Lists.newArrayList(
            new BgpCommunityListExpandedLine(LineAction.PERMIT, "10000:10 20000:20"));
    List<BgpCommunityListExpandedLine> actual = communityList.getLines();
    assertThat(expected.size(), equalTo(actual.size()));
    assertThat(expected.get(0).getAction(), equalTo(actual.get(0).getAction()));
    assertThat(expected.get(0).getRegex(), equalTo(actual.get(0).getRegex()));
  }

  @Test
  public void testFrrIpCommunityListExpanded() {
    // check that the old syntax still parses into a BGP community list
    String name = "NAME";
    parse(String.format("ip community-list expanded %s permit 10000:10 20000:20\n", name));
    BgpCommunityListExpanded communityList =
        (BgpCommunityListExpanded) _frr.getBgpCommunityLists().get(name);
    assertNotNull(communityList);
  }

  @Test
  public void testFrrVrfRouteMapMatchInterface() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nmatch interface lo\n", name));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getMatchInterface().getInterfaces(), equalTo(ImmutableSet.of("lo")));

    assertThat(
        getStructureReferences(
            FrrStructureType.ABSTRACT_INTERFACE, "lo", FrrStructureUsage.ROUTE_MAP_MATCH_INTERFACE),
        equalTo(ImmutableSet.of(2)));
  }

  @Test
  public void testFrrRouteMapMatchTagExtraction() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nmatch tag 65555\n", name));

    RouteMapEntry c = _frr.getRouteMaps().get(name).getEntries().get(10);

    assertThat(c.getMatchTag().getTag(), equalTo(65555L));
  }

  @Test
  public void testFrrRouteMapMatchTagConversion() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nmatch tag 65555\n", name));
    Configuration c = _config.toVendorIndependentConfigurations().get(0);
    RoutingPolicy policy = c.getRoutingPolicies().get(name);

    Builder routeBuilder =
        org.batfish.datamodel.StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setAdmin(1)
            .setNextHopInterface("iface");
    // NOTE: re-using the builder, order of asserts may matter
    // With missing tag -- no match
    assertFalse(policy.process(routeBuilder.build(), Bgpv4Route.testBuilder(), OUT));
    // With tag -- match
    assertTrue(policy.process(routeBuilder.setTag(65555L).build(), Bgpv4Route.testBuilder(), OUT));
    // With different tag -- no match
    assertFalse(policy.process(routeBuilder.setTag(65554L).build(), Bgpv4Route.testBuilder(), OUT));
  }

  @Test
  public void testRouteMapSetAsPath() {
    parseLines("route-map ROUTE_MAP permit 10", "set as-path prepend 11111 22222");
  }

  @Test
  public void testFrrRouteMapSetLocalPref() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nset local-preference 200\n", name));

    RouteMapEntry c = _frr.getRouteMaps().get(name).getEntries().get(10);

    assertThat(c.getSetLocalPreference().getLocalPreference(), equalTo(200L));
  }

  @Test
  public void testFrrRouteMapSetTagPref() {
    String name = "ROUTE-MAP-NAME";

    parse(String.format("route-map %s permit 10\nset tag 999\n", name));

    RouteMapEntry c = _frr.getRouteMaps().get(name).getEntries().get(10);

    assertThat(c.getSetTag().getTag(), equalTo(999L));
  }

  @Test
  public void testRouteMapSetIpv6() {
    // parsing only
    parseLines(
        "route-map ROUTE_MAP permit 10",
        " set ipv6 next-hop peer-address",
        " set ipv6 next-hop prefer-global",
        " set ipv6 next-hop global 2001:db8:1::1",
        " set ipv6 next-hop local 2001:db8:1::1");
  }

  @Test
  public void testFrrVrfRouteMapSetIpNextHop() {
    String name = "ROUTE-MAP-NAME";
    String clause1 = "set ip next-hop 10.0.0.1";

    parse(String.format("route-map %s permit 10\n%s\n", name, clause1));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(entry.getSetIpNextHop().getNextHop(), equalTo(Ip.parse("10.0.0.1")));
  }

  @Test
  public void testFrrVrfRouteMapSetCommunity() {
    String name = "ROUTE-MAP-NAME";

    parse(
        String.format(
            "route-map %s permit 10\n"
                + "set community 10000:1 20000:2 local-AS no-advertise no-export internet\n",
            name));

    RouteMapEntry entry = _frr.getRouteMaps().get(name).getEntries().get(10);
    assertThat(
        entry.getSetCommunity().getCommunities(),
        equalTo(
            ImmutableList.of(
                StandardCommunity.of(10000, 1),
                StandardCommunity.of(20000, 2),
                StandardCommunity.NO_EXPORT_SUBCONFED,
                StandardCommunity.NO_ADVERTISE,
                StandardCommunity.NO_EXPORT,
                StandardCommunity.INTERNET)));
  }

  @Test
  public void testFrrBgpAsPathAccessList() {
    String name = "NAME";
    String as1 = "^11111$";
    String as2 = "_1_";
    String as3 = "^1[1-2]";
    String as4 = "^1(1)";
    parse(
        String.format(
            "bgp as-path access-list %s permit %s\n"
                + "bgp as-path access-list %s permit %s\n"
                + "bgp as-path access-list %s permit %s\n"
                + "bgp as-path access-list %s permit %s\n"
                + "bgp as-path access-list %s deny %s\n"
                + "bgp as-path access-list %s deny %s\n"
                + "bgp as-path access-list %s deny %s\n"
                + "bgp as-path access-list %s deny %s\n",
            name, as1, name, as2, name, as3, name, as4, name, as1, name, as2, name, as3, name,
            as4));

    // Check that config has the expected AS-path access list with the expected name and num lines
    assertThat(_frr.getBgpAsPathAccessLists().keySet(), contains(name));
    BgpAsPathAccessList asPathAccessList = _frr.getBgpAsPathAccessLists().get(name);
    assertThat(asPathAccessList.getName(), equalTo(name));
    assertThat(asPathAccessList.getLines(), hasSize(8));

    // Check that lines look as expected
    BgpAsPathAccessListLine line0 = asPathAccessList.getLines().get(0);
    BgpAsPathAccessListLine line1 = asPathAccessList.getLines().get(1);
    BgpAsPathAccessListLine line2 = asPathAccessList.getLines().get(2);
    BgpAsPathAccessListLine line3 = asPathAccessList.getLines().get(3);
    BgpAsPathAccessListLine line4 = asPathAccessList.getLines().get(4);
    BgpAsPathAccessListLine line5 = asPathAccessList.getLines().get(5);
    BgpAsPathAccessListLine line6 = asPathAccessList.getLines().get(6);
    BgpAsPathAccessListLine line7 = asPathAccessList.getLines().get(7);

    assertThat(line0.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line1.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line2.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line3.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line4.getAction(), equalTo(LineAction.DENY));
    assertThat(line5.getAction(), equalTo(LineAction.DENY));
    assertThat(line6.getAction(), equalTo(LineAction.DENY));
    assertThat(line7.getAction(), equalTo(LineAction.DENY));

    assertThat(line0.getRegex(), equalTo(as1));
    assertThat(line1.getRegex(), equalTo(as2));
    assertThat(line2.getRegex(), equalTo(as3));
    assertThat(line3.getRegex(), equalTo(as4));
    assertThat(line4.getRegex(), equalTo(as1));
    assertThat(line5.getRegex(), equalTo(as2));
    assertThat(line6.getRegex(), equalTo(as3));
    assertThat(line7.getRegex(), equalTo(as4));

    // Check that the AS-path access list definition was registered
    DefinedStructureInfo definedStructureInfo =
        getDefinedStructureInfo(FrrStructureType.BGP_AS_PATH_ACCESS_LIST, name);
    assertThat(
        definedStructureInfo.getDefinitionLines().enumerate(), contains(1, 2, 3, 4, 5, 6, 7, 8));
  }

  @Test
  public void testFrrIpAsPathAccessList() {
    // check that old syntax parses into BgpAsPathAccessList
    String name = "NAME";
    parse(String.format("ip as-path access-list %s permit ^$\n", name));
    assertThat(_frr.getBgpAsPathAccessLists().keySet(), contains(name));
  }

  @Test
  public void testFrrIpPrefixList() {
    String name = "PREFIX_10.0.0.0/23";
    String prefix1 = "10.0.0.1/24";
    String prefix2 = "10.0.1.2/24";
    parse(
        String.format(
            "ip prefix-list %s seq 10 permit %s\n"
                + "ip prefix-list %s seq 20 deny %s ge 27 le 30 \n",
            name, prefix1, name, prefix2));

    assertThat(_frr.getIpPrefixLists().keySet(), equalTo(ImmutableSet.of(name)));
    IpPrefixList prefixList = _frr.getIpPrefixLists().get(name);
    assertThat(prefixList.getName(), equalTo(name));
    assertThat(prefixList.getLines().keySet(), equalTo(ImmutableSet.of(10L, 20L)));

    IpPrefixListLine line1 = prefixList.getLines().get(10L);
    assertThat(line1.getLine(), equalTo(10L));
    assertThat(line1.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line1.getLengthRange(), equalTo(SubRange.singleton(24)));
    assertThat(line1.getPrefix(), equalTo(Prefix.parse("10.0.0.1/24")));

    IpPrefixListLine line2 = prefixList.getLines().get(20L);
    assertThat(line2.getLine(), equalTo(20L));
    assertThat(line2.getAction(), equalTo(LineAction.DENY));
    assertThat(line2.getLengthRange(), equalTo(new SubRange(27, 30)));
    assertThat(line2.getPrefix(), equalTo(Prefix.parse("10.0.1.2/24")));
  }

  @Test
  public void testFrrIpPrefixListIPv6() {
    String name = "2001:db8::/32";
    String prefix = "2001:db8:1::/48";
    parse(String.format("ipv6 prefix-list %s seq 10 permit %s\n", name, prefix));

    assertThat(_frr.getIpv6PrefixLists().keySet(), equalTo(ImmutableSet.of(name)));
    Ipv6PrefixList prefixList = _frr.getIpv6PrefixLists().get(name);
    assertThat(prefixList.getName(), equalTo(name));

    Ipv6PrefixListLine line = prefixList.getLines().get(10L);
    assertThat(line.getLine(), equalTo(10L));
    assertThat(line.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line.getLengthRange(), equalTo(SubRange.singleton(48)));
    assertThat(line.getPrefix(), equalTo(Prefix6.parse(prefix)));
  }

  @Test
  public void testFrrIpPrefixListNoSeqOnFirstEntry() {
    String name = "NAME";
    String prefix1 = "10.0.0.1/24";
    parse(String.format("ip prefix-list %s permit %s\n", name, prefix1));
    assertThat(_frr.getIpPrefixLists().keySet(), equalTo(ImmutableSet.of(name)));
    IpPrefixList prefixList = _frr.getIpPrefixLists().get(name);
    IpPrefixListLine line1 = prefixList.getLines().get(5L);
    assertThat(line1.getLine(), equalTo(5L));
    assertThat(line1.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line1.getLengthRange(), equalTo(SubRange.singleton(24)));
    assertThat(line1.getPrefix(), equalTo(Prefix.parse("10.0.0.1/24")));
  }

  @Test
  public void testFrrIpPrefixListNoSeqOnLaterEntry() {
    String name = "NAME";
    String prefix1 = "10.0.0.1/24";
    String prefix2 = "10.0.1.2/24";
    parse(
        String.format(
            "ip prefix-list %s seq 4 permit %s\n" + "ip prefix-list %s deny %s ge 27 le 30 \n",
            name, prefix1, name, prefix2));
    assertThat(_frr.getIpPrefixLists().keySet(), equalTo(ImmutableSet.of(name)));
    IpPrefixList prefixList = _frr.getIpPrefixLists().get(name);
    IpPrefixListLine line1 = prefixList.getLines().get(4L);
    assertThat(line1.getLine(), equalTo(4L));
    assertThat(line1.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line1.getLengthRange(), equalTo(SubRange.singleton(24)));
    assertThat(line1.getPrefix(), equalTo(Prefix.parse("10.0.0.1/24")));

    IpPrefixListLine line2 = prefixList.getLines().get(5L);
    assertThat(line2.getLine(), equalTo(5L));
    assertThat(line2.getAction(), equalTo(LineAction.DENY));
    assertThat(line2.getLengthRange(), equalTo(new SubRange(27, 30)));
    assertThat(line2.getPrefix(), equalTo(Prefix.parse("10.0.1.2/24")));
  }

  @Test
  public void testFrrIpPrefixListAny() {
    String name = "NAME";
    parse(String.format("ip prefix-list %s seq 5 permit any\n", name));
    assertThat(_frr.getIpPrefixLists().keySet(), equalTo(ImmutableSet.of(name)));
    IpPrefixList prefixList = _frr.getIpPrefixLists().get(name);
    IpPrefixListLine line1 = prefixList.getLines().get(5L);
    assertThat(line1.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line1.getLengthRange(), equalTo(new SubRange(0, Prefix.MAX_PREFIX_LENGTH)));
    assertThat(line1.getPrefix(), equalTo(Prefix.ZERO));
  }

  @Test
  public void testFrrIpPrefixListDescription() {
    String name = "NAME";
    // Don't crash
    parse(String.format("ip prefix-list %s description FOO\n", name));
  }

  @Test
  public void testFrrAgentx() {
    parse("agentx\n");
  }

  @Test
  public void testCumulusService() {
    parse("service integrated-vtysh-config\n");
  }

  @Test
  public void testFrrSyslog() {
    parse("log syslog\n");
    parse("log syslog alerts\n");
    parse("log syslog critical\n");
    parse("log syslog debugging\n");
    parse("log syslog errors\n");
    parse("log syslog informational\n");
    parse("log syslog notifications\n");
    parse("log syslog warnings\n");
  }

  @Test
  public void testLineVty() {
    parse("line vty\n");
  }

  @Test
  public void testFrrUsername() {
    parse("username cumulus nopassword\n");
  }

  @Test
  public void testFrrVersion() {
    parse("frr version\n");
    parse("frr version sV4@%)!@#$%^&**()_+|\n");
  }

  @Test
  public void testFrrBgpNeighborBfd() {
    parse("router bgp 10000 vrf VRF\nneighbor N bfd 1 10 20\n");
    parse("router bgp 10000 vrf VRF\nneighbor N bfd\n");
  }

  @Test
  public void testFrrNeightborPassword() {
    parse("router bgp 10000\nneighbor N password sV4@%)!@#$%^&**()_+|\n");
  }

  @Test
  public void testBgpNeighborEbgpMultihopPeerGroup() {
    _warnings = new Warnings(false, true, false);
    parseLines(
        "router bgp 10000",
        "neighbor N peer-group",
        "neighbor N ebgp-multihop 3",
        "neighbor M peer-group",
        "neighbor M ebgp-multihop",
        "neighbor L peer-group");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("N").getEbgpMultihop(),
        equalTo(true));
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("M").getEbgpMultihop(),
        equalTo(true));
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("L").getEbgpMultihop(),
        equalTo(null));
    assertThat(
        Iterables.getOnlyElement(_warnings.getParseWarnings()),
        hasComment("Neighbor recognized as ebgp-multihop, but distance limit is not enforced"));
  }

  @Test
  public void testBgpNeighborEbgpMultihopPeer() {
    _warnings = new Warnings(false, true, false);
    parseLines(
        "router bgp 10000",
        "neighbor 10.0.0.1 ebgp-multihop 3",
        "neighbor 10.0.0.2 ebgp-multihop",
        "neighbor 10.0.0.3 bfd");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("10.0.0.1").getEbgpMultihop(),
        equalTo(true));
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("10.0.0.2").getEbgpMultihop(),
        equalTo(true));
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("10.0.0.3").getEbgpMultihop(),
        equalTo(null));
    assertThat(
        Iterables.getOnlyElement(_warnings.getParseWarnings()),
        hasComment("Neighbor recognized as ebgp-multihop, but distance limit is not enforced"));
  }

  /**
   * Test that we warn (and not crash) when we encounter an interface neighbor on an undefined
   * interface
   */
  @Test
  public void testBgpNeighborMissingInterface() {
    parseLines(
        "router bgp 65003",
        "  neighbor leaf peer-group",
        "  neighbor leaf remote-as external",
        "  neighbor Ethernet8 interface peer-group leaf");
    _config.toVendorIndependentConfigurations();
    assertThat(
        _config.getWarnings().getRedFlagWarnings(),
        contains(hasToString(containsString("Ethernet8"))));
  }

  @Test
  public void testBgpNetwork() {
    Prefix network = Prefix.parse("10.0.0.0/8");
    parseLines("router bgp 10000", "network 10.0.0.0/8");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNetworks(),
        equalTo(ImmutableMap.of(network, new BgpNetwork(network))));
  }

  @Test
  public void testNetworkConversion() throws IOException {
    parseLines("router bgp 10000", "network 10.0.0.0/24");
    Configuration c = _config.toVendorIndependentConfigurations().get(0);
    assertThat(
        c.getDefaultVrf().getBgpProcess().getUnconditionalNetworkStatements(),
        contains(Prefix.strict("10.0.0.0/24")));

    String networkPolicyName = generatedBgpMainRibIndependentNetworkPolicyName(DEFAULT_VRF_NAME);

    Prefix networkMatching = Prefix.strict("10.0.0.0/24");
    Prefix networkNotMatching = Prefix.strict("10.0.1.0/24");

    assertThat(c.getRoutingPolicies(), hasKey(networkPolicyName));
    assertThat(
        c.getDefaultVrf().getBgpProcess().getMainRibIndependentNetworkPolicy(),
        equalTo(networkPolicyName));

    KernelRoute routeNetworkMatch =
        org.batfish.datamodel.KernelRoute.builder().setNetwork(networkMatching).build();
    KernelRoute routeNetworkNoMatch =
        org.batfish.datamodel.KernelRoute.builder().setNetwork(networkNotMatching).build();

    Bgpv4Route.Builder bgpRoute = Bgpv4Route.testBuilder().setNetwork(networkMatching);
    RoutingPolicy networkPolicy = c.getRoutingPolicies().get(networkPolicyName);
    assertTrue(
        networkPolicy.processBgpRoute(
            routeNetworkMatch, bgpRoute, null, Environment.Direction.OUT, null));
    // check properties set by the policy
    assertThat(
        bgpRoute.build(),
        isBgpv4RouteThat(allOf(hasWeight(32768), hasNextHop(NextHopDiscard.instance()))));

    assertFalse(networkPolicy.processReadOnly(routeNetworkNoMatch));
  }

  @Test
  public void testBgpNetworkWithRouteMap() {
    Prefix network = Prefix.parse("10.0.0.0/8");
    parseLines("router bgp 10000", "network 10.0.0.0/8 route-map FOO");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNetworks(),
        equalTo(ImmutableMap.of(network, new BgpNetwork(network, "FOO"))));
  }

  /** FRR does not crash if undeclared peer-group has a route-map configured. */
  @Test
  public void testIp4vUnicastRoutemap_error() {
    parseLines(
        "router bgp 10000",
        "address-family ipv4 unicast",
        "neighbor N route-map R in",
        "exit-address-family");
  }

  /** FRR allows route-map to be configured before neighbor exists. */
  @Test
  public void testIp4vUnicastRoutemap_out_of_order() {
    parseLines(
        "router bgp 10000",
        "address-family ipv4 unicast",
        "neighbor N route-map R in",
        "exit-address-family",
        "neighbor N peer-group");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4UnicastConfiguration("N").getRouteMapIn(),
        equalTo("R"));
    assertThat(
        getStructureReferences(
            FrrStructureType.ROUTE_MAP,
            "R",
            FrrStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_IN),
        contains(3));
  }

  @Test
  public void testIp4vUnicastRoutemap_in() {
    parseLines(
        "router bgp 10000",
        "neighbor N peer-group",
        "address-family ipv4 unicast",
        "neighbor N route-map R in",
        "exit-address-family");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4UnicastConfiguration("N").getRouteMapIn(),
        equalTo("R"));
    assertThat(
        getStructureReferences(
            FrrStructureType.ROUTE_MAP,
            "R",
            FrrStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_IN),
        contains(4));
  }

  @Test
  public void testIp4vUnicastRoutemap_out() {
    parseLines(
        "router bgp 10000",
        "neighbor N2 peer-group",
        "address-family ipv4 unicast",
        "neighbor N2 route-map R out",
        "exit-address-family");
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getIpv4UnicastConfiguration("N2").getRouteMapOut(),
        equalTo("R"));
    assertThat(
        getStructureReferences(
            FrrStructureType.ROUTE_MAP,
            "R",
            FrrStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_OUT),
        contains(4));
  }

  @Test
  public void testInterface_IPv6() {
    parseLines("interface swp1", "ipv6 nd ra-interval 10", "ipv6 address 172:65:1::3/128");
    assertTrue(_warnings.getParseWarnings().isEmpty());
  }

  @Test
  public void testInterface_No() {
    parseLines("interface swp1", "no ipv6 nd suppress-ra");
    assertTrue(_warnings.getParseWarnings().isEmpty());
  }

  /**
   * Interface vrf is configured in FRR for a new interface that we haven't seen before, and we
   * haven't seen the configured VRF before.
   */
  @Test
  public void testInterface_InterfaceVrfWithoutVrfDefinition() {
    parseLines("interface swp1 vrf VRF", "description rt1010svc01 swp1s1");
    assertThat(
        Iterables.getOnlyElement(_warnings.getParseWarnings()).getComment(),
        equalTo("vrf VRF of interface swp1 has not been defined in FRR configuration file"));
    assertFalse(_frr.getInterfaces().containsKey("swp1"));
  }

  /**
   * Interface vrf is configured in FRR for a new interface that we haven't seen before but using a
   * vrf that we have seen before in interfaces file (since we do allow for definitions in frr
   * file).
   */
  @Test
  public void testInterface_InterfaceVrfWithVrfDefinition() {
    _frr.getVrfs().put("VRF", new Vrf("VRF"));
    parseLines("interface swp1 vrf VRF", "description rt1010svc01 swp1s1");
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(_frr.getInterfaces().keySet(), contains("swp1"));
    assertThat(_frr.getInterfaces().get("swp1").getVrfName(), equalTo("VRF"));
  }

  /** Interface has a vrf definition that does not match what we saw earlier */
  @Test
  public void testInterface_InterfaceVrfNotMatch() {
    // has vrf but not match
    FrrInterface i1 = new FrrInterface("swp1", "VRF2");
    i1.setAlias("old alias");
    _frr.getInterfaces().put("swp1", i1);
    parseLines("interface swp1 vrf VRF", "description rt1010svc01 swp1s1");
    assertThat(_warnings.getParseWarnings(), hasSize(1));
    assertThat(
        _warnings.getParseWarnings().get(0).getComment(),
        equalTo("vrf VRF of interface swp1 does not match previously-defined vrf VRF2"));
    assertThat(_frr.getInterfaces().get("swp1").getAlias(), equalTo("old alias"));
  }

  /** Two interface definitions in FRR with different VRFs */
  @Test
  public void testInterface_InterfaceVrfNotMatchWithinFrr() {
    _frr.getVrfs().put("VRF1", new Vrf("VRF1"));
    _frr.getVrfs().put("VRF2", new Vrf("VRF2"));
    parseLines(
        "interface swp1 vrf VRF1",
        "description first",
        "interface swp1 vrf VRF2",
        "description second");
    assertThat(_warnings.getParseWarnings(), hasSize(1));
    assertThat(
        _warnings.getParseWarnings().get(0).getComment(),
        equalTo("vrf VRF2 of interface swp1 does not match previously-defined vrf VRF1"));
    assertThat(_frr.getInterfaces().get("swp1").getAlias(), equalTo("first"));
  }

  /** Two interface definitions in FRR, with the second one missing explicit VRF */
  @Test
  public void testInterface_InterfaceVrfNotMatchWithinFrrSecondDefault() {
    _frr.getVrfs().put("VRF", new Vrf("VRF"));
    parseLines(
        "interface swp1 vrf VRF", "description first", "interface swp1", "description second");
    assertThat(_warnings.getParseWarnings(), hasSize(1));
    assertThat(
        _warnings.getParseWarnings().get(0).getComment(),
        equalTo("vrf default of interface swp1 does not match previously-defined vrf VRF"));
  }

  /** Interface vrf is configured in the interfaces file and is not explicitly configured in FRR */
  @Test
  public void testInterface_InterfaceDefaultVrf() {
    InterfacesInterface i2 = new InterfacesInterface("swp2");
    i2.setVrf("VRF2");
    _config.getInterfacesConfiguration().getInterfaces().put("swp2", i2);
    parseLines("interface swp2", "description rt1010svc01 swp1s1");
    assertThat(_warnings.getParseWarnings(), hasSize(0));
    assertThat(_frr.getInterfaces().get("swp2").getAlias(), equalTo("rt1010svc01 swp1s1"));
  }

  /**
   * Interface vrf is defined in FRR and it matches what was defined for the interface earlier in
   * interfaces or in FRR.
   */
  @Test
  public void testInterface_InterfaceVrfMatch() {
    FrrInterface i1 = new FrrInterface("swp1", "VRF");
    i1.setAlias("rt1010svc01 swp1s1");
    _frr.getInterfaces().put("swp1", i1);
    parseLines("interface swp1 vrf VRF", "description rt1010svc01 swp1s1");
    assertThat(_warnings.getParseWarnings(), empty());
  }

  @Test
  public void testIpv6PrefixList() {
    parseLines("ipv6 prefix-list allow-loc-storage-v6 seq 10 permit 2001:100:100:20::/64");
    assertTrue(_warnings.getParseWarnings().isEmpty());
  }

  @Test
  public void testRouterOspf() {
    parse("router ospf\n log-adjacency-changes detail\n");
    assertThat(_warnings.getParseWarnings(), empty());
    assertNotNull(_frr.getOspfProcess());
  }

  @Test
  public void testInterface_ospf_area() {
    parse("interface swp1\n ip ospf area 0.0.0.0\n");
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(_frr.getInterfaces().get("swp1").getOspf().getOspfArea(), equalTo(0L));
  }

  @Test
  public void testInterface_ospf_area_subnet_mask() {
    parse("interface swp1\n ip ospf area 255.0.0.0\n");
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(
        _frr.getInterfaces().get("swp1").getOspf().getOspfArea(),
        equalTo(Ip.parse("255.0.0.0").asLong()));
  }

  @Test
  public void testInterface_ospf_area_num() {
    parse("interface swp1\n ip ospf area 0\n");
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(_frr.getInterfaces().get("swp1").getOspf().getOspfArea(), equalTo(0L));
  }

  @Test
  public void testInterface_ospf_area_num32bit() {
    parse("interface swp1\n ip ospf area 4000000000\n");
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(_frr.getInterfaces().get("swp1").getOspf().getOspfArea(), equalTo(4000000000L));
  }

  @Test
  public void testInterface_ospf_authentication() {
    FrrInterface i1 = new FrrInterface("swp1");
    _frr.getInterfaces().put("swp1", i1);
    parse("interface swp1\n ip ospf authentication message-digest\n");
  }

  @Test
  public void testInterface_ospf_authentication_key() {
    FrrInterface i1 = new FrrInterface("swp1");
    _frr.getInterfaces().put("swp1", i1);
    parse("interface swp1\n ip ospf message-digest-key 1 md5 <SCRUBBED>\n");
  }

  @Test
  public void testInterface_ospf_p2p() {
    parse("interface swp1\n ip ospf network point-to-point\n");
    assertThat(
        _frr.getInterfaces().get("swp1").getOspf().getNetwork(),
        equalTo(OspfNetworkType.POINT_TO_POINT));
  }

  @Test
  public void testInterface_ospf_cost() {
    parse("interface swp1\n ip ospf cost 100\n");
    assertThat(_frr.getInterfaces().get("swp1").getOspf().getCost(), equalTo(100));
  }

  @Test
  public void testInterface_ospf_cost_null() {
    parse("interface swp1\n ip ospf area 0\n");
    assertThat(_frr.getInterfaces().get("swp1").getOspf().getCost(), equalTo(null));
  }

  @Test
  public void testRouterOspfAreaRange() {
    parse(
        "router ospf\n"
            + " area 1.1.1.0 range 1.255.0.0/17 cost 10\n"
            + " area 5 range 1.255.0.0/17\n");
    Prefix prefix = Prefix.parse("1.255.0.0/17");
    OspfVrf vrf = _frr.getOspfProcess().getDefaultVrf();
    OspfArea area1110 = vrf.getArea(Ip.parse("1.1.1.0").asLong());
    assertThat(area1110, notNullValue());
    assertThat(area1110.getRanges(), hasKeys(prefix));
    assertThat(area1110.getRange(prefix).getCost(), equalTo(10));
    OspfArea area5 = vrf.getArea(5L);
    assertThat(area5, notNullValue());
    assertThat(area5.getRanges(), hasKeys(prefix));
    assertThat(area5.getRange(prefix).getCost(), nullValue());
  }

  @Test
  public void testRouterOspfAreaRangeConversion() {
    parse(
        "interface swp1\n"
            + " ip address 1.2.3.4/5\n"
            + " ip ospf area 1.1.1.0\n"
            + "\n"
            + "interface swp2\n"
            + " ip address 2.3.4.5/6\n"
            + " ip ospf area 5\n"
            + "\n"
            + "router ospf\n"
            + " area 1.1.1.0 range 1.255.0.0/17 cost 10\n");
    long area1110 = Ip.parse("1.1.1.0").asLong();
    Configuration c = _config.toVendorIndependentConfigurations().get(0);
    assertThat(c.getDefaultVrf().getOspfProcesses(), aMapWithSize(1));
    OspfProcess proc = Iterables.getOnlyElement(c.getDefaultVrf().getOspfProcesses().values());
    assertThat(proc.getAreas(), hasKeys(5L, area1110));
    {
      org.batfish.datamodel.ospf.OspfArea area = proc.getAreas().get(5L);
      assertThat(area.getSummaries(), anEmptyMap());
      assertThat(area.getSummaryFilter(), nullValue());
    }
    {
      org.batfish.datamodel.ospf.OspfArea area = proc.getAreas().get(area1110);
      assertThat(area.getSummaries(), hasKeys(Prefix.parse("1.255.0.0/17")));
      OspfAreaSummary summary = Iterables.getOnlyElement(area.getSummaries().values());
      assertThat(
          summary.getBehavior(), equalTo(SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD));
      assertThat(summary.getMetric(), equalTo(10L));
      String name = computeOspfAreaRangeFilterName(c.getDefaultVrf().getName(), area1110);
      assertThat(area.getSummaryFilter(), equalTo(name));
      assertThat(c.getRouteFilterLists(), hasKey(name));
      RouteFilterList filter = c.getRouteFilterLists().get(name);
      assertTrue(filter.permits(Prefix.parse("1.255.0.0/17"))); // prefix itself should be permitted
      assertFalse(
          filter.permits(
              Prefix.parse("1.255.64.0/18"))); // more specific, even with diff prefix, denied
      assertTrue(filter.permits(Prefix.parse("10.0.0.0/18"))); // more specific, not overlapping
      assertTrue(filter.permits(Prefix.parse("1.255.0.0/16"))); // less specific
    }
  }

  @Test
  public void testRouterOspfNetwork() {
    parse(
        "router ospf\n"
            + " network 10.0.0.0/8 area 0.0.0.0\n"
            + " network 20.0.0.0/8 area 4000000000\n"
            + " network 30.0.0.0/8 area 1.2.3.4\n");
    Prefix ten8 = Prefix.parse("10.0.0.0/8");
    Prefix twenty8 = Prefix.parse("20.0.0.0/8");
    Prefix thirty8 = Prefix.parse("30.0.0.0/8");
    assertThat(_frr.getOspfProcess().getNetworkAreas(), hasKeys(ten8, twenty8, thirty8));
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(
        _frr.getOspfProcess().getNetworkAreas().get(ten8), equalTo(new OspfNetworkArea(ten8, 0)));
    assertThat(
        _frr.getOspfProcess().getNetworkAreas().get(twenty8),
        equalTo(new OspfNetworkArea(twenty8, 4000000000L)));
    assertThat(
        _frr.getOspfProcess().getNetworkAreas().get(thirty8),
        equalTo(new OspfNetworkArea(thirty8, Ip.parse("1.2.3.4").asLong())));
  }

  @Test
  public void testRouterOspfNoNetwork() {
    parse(
        "router ospf\n"
            + " network 10.0.0.0/8 area 0.0.0.0\n"
            + " network 20.0.0.0/8 area 4000000000\n"
            + " no network 10.0.0.0/8 area 1.2.3.4\n"
            + " no network 10.0.0.0/8 area 0\n"
            + " no network 30.0.0.0/8 area 0\n");
    assertThat(_frr.getOspfProcess().getNetworkAreas(), hasKeys(Prefix.parse("20.0.0.0/8")));
    assertThat(
        _warnings.getParseWarnings(),
        contains(
            allOf(
                hasText("no network 10.0.0.0/8 area 1.2.3.4"),
                hasComment(
                    "The area already defined for network 10.0.0.0/8 is 0.0.0.0 (0), not 1.2.3.4"
                        + " (16909060)")),
            allOf(
                hasText("no network 30.0.0.0/8 area 0"),
                hasComment("There is no area already defined for network 30.0.0.0/8"))));
  }

  @Test
  public void testRouterOspfPassiveInterface_NoInterface() {
    parse("router ospf\n passive-interface lo\n");
    assertThat(_warnings.getParseWarnings(), hasSize(1));
    assertThat(
        _warnings.getParseWarnings().get(0).getComment(), equalTo("interface lo is not defined"));
  }

  @Test
  public void testRouterOspfPassiveInterface() {
    FrrInterface iface = new FrrInterface("lo");
    _frr.getInterfaces().put("lo", iface);
    parse("router ospf\n passive-interface lo\n");
    assertTrue(iface.getOspf().getPassive());
  }

  @Test
  public void testRouterOspfPassiveInterfaceDefault() {
    _frr.getOrCreateInterface("ifaceTrue").getOrCreateOspf().setPassive(true);
    _frr.getOrCreateInterface("ifaceFalse").getOrCreateOspf().setPassive(false);
    parse("router ospf\n passive-interface default\n");
    assertTrue(_frr.getOspfProcess().getDefaultPassiveInterface());
    assertNull(_frr.getInterfaces().get("ifaceTrue").getOspf().getPassive());
    assertNull(_frr.getInterfaces().get("ifaceFalse").getOspf().getPassive());
  }

  @Test
  public void testRouterOspfNoPassiveInterfaceDefault() {
    _frr.getOrCreateInterface("ifaceTrue").getOrCreateOspf().setPassive(true);
    _frr.getOrCreateInterface("ifaceFalse").getOrCreateOspf().setPassive(false);
    parse("router ospf\n no passive-interface default\n");
    assertFalse(_frr.getOspfProcess().getDefaultPassiveInterface());
    assertNull(_frr.getInterfaces().get("ifaceTrue").getOspf().getPassive());
    assertNull(_frr.getInterfaces().get("ifaceFalse").getOspf().getPassive());
  }

  @Test
  public void testRouterOspfRouterId() {
    parse("router ospf\n ospf router-id 1.1.1.3\n");
    assertThat(_frr.getOspfProcess().getDefaultVrf().getRouterId(), equalTo(Ip.parse("1.1.1.3")));
  }

  @Test
  public void testRouterOspfRouterId_noOspf() {
    // router-id without preceding OSPF is legal
    parse("router ospf\n router-id 1.1.1.3\n");
    assertThat(_frr.getOspfProcess().getDefaultVrf().getRouterId(), equalTo(Ip.parse("1.1.1.3")));
  }

  @Test
  public void testOspfRedistributeConnected() {
    parseLines("router ospf", "redistribute connected");
    RedistributionPolicy policy =
        _frr.getOspfProcess().getRedistributionPolicies().get(FrrRoutingProtocol.CONNECTED);
    assertNotNull(policy);
    assertNull(policy.getRouteMap());
  }

  @Test
  public void testOspfRedistributeConnectedRouteMap() {
    parseLines("router ospf", "redistribute connected route-map foo");
    RedistributionPolicy policy =
        _frr.getOspfProcess().getRedistributionPolicies().get(FrrRoutingProtocol.CONNECTED);
    assertNotNull(policy);
    assertThat(policy.getRouteMap(), equalTo("foo"));
  }

  @Test
  public void testOspfRedistributeStatic() {
    parseLines("router ospf", "redistribute static");
    RedistributionPolicy policy =
        _frr.getOspfProcess().getRedistributionPolicies().get(FrrRoutingProtocol.STATIC);
    assertNotNull(policy);
    assertNull(policy.getRouteMap());
  }

  @Test
  public void testOspfRedistributeStaticRouteMap() {
    parseLines("router ospf", "redistribute static route-map foo");
    RedistributionPolicy policy =
        _frr.getOspfProcess().getRedistributionPolicies().get(FrrRoutingProtocol.STATIC);
    assertNotNull(policy);
    assertThat(policy.getRouteMap(), equalTo("foo"));
  }

  @Test
  public void testOspfRedistributeBgp() {
    parseLines("router ospf", "redistribute bgp");
    RedistributionPolicy policy =
        _frr.getOspfProcess().getRedistributionPolicies().get(FrrRoutingProtocol.BGP);
    assertNotNull(policy);
    assertNull(policy.getRouteMap());
  }

  @Test
  public void testOspfRedistributeBgpRouteMap() {
    parseLines("router ospf", "redistribute bgp route-map foo");
    RedistributionPolicy policy =
        _frr.getOspfProcess().getRedistributionPolicies().get(FrrRoutingProtocol.BGP);
    assertNotNull(policy);
    assertThat(policy.getRouteMap(), equalTo("foo"));
  }

  @Test
  public void testOspfMaxMetricRouterLsaAdministrativeDefault() {
    parseLines("router ospf");
    assertThat(_frr.getOspfProcess().getMaxMetricRouterLsa(), equalTo(null));
  }

  @Test
  public void testOspfMaxMetricRouterLsaAdministrative() {
    parseLines("router ospf", "max-metric router-lsa administrative");
    assertThat(_frr.getOspfProcess().getMaxMetricRouterLsa(), equalTo(true));
  }

  @Test
  public void testOspfRedistributeBgpSetMetric_behavior() throws IOException {
    /*
      Three nodes in a line - frr-originator spawns a route into BGP, redistributor redistributes this route into OSPF and sets metric of 10k
      We expect to see 10.2.2.2/32 with a Metric Type of E2 and Ospf Cost of 10k in frr-listener.
    */

    String snapshotName = "ospf-bgp-redist-set-metric";
    List<String> configurationNames =
        ImmutableList.of("frr-originator", "frr-listener", "frr-redistributor");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);

    // frr-reoriginator should get a default route from frr-originator
    assertThat(
        dp.getRibs().get("frr-listener", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                OspfExternalType2Route.builder()
                    .setNetwork(Prefix.parse("10.2.2.2/32"))
                    .setNextHop(NextHopInterface.of("swp1", Ip.parse("10.1.1.2")))
                    .setAdvertiser("frr-redistributor")
                    .setAdmin(110)
                    .setLsaMetric(10000L)
                    .setArea(0L)
                    .setCostToAdvertiser(10L)
                    .setMetric(10000L)
                    .build())));
  }

  @Test
  public void testCreatePhysicalInterfaceInFRR() {
    String name = "eth1";
    parse(String.format("interface %s\n", name));
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(_frr.getInterfaces().keySet(), contains(name));
  }

  @Test
  public void testCreateLoopbackInterfaceInFRR() {
    String name = "lo";
    parse(String.format("interface %s\n", name));
    assertThat(_warnings.getParseWarnings(), empty());
    assertThat(_frr.getInterfaces().keySet(), contains(name));
  }

  @Test
  public void testSetInterfaceIpAddress() {
    parseLines("interface eth1", "ip address 1.1.1.1/30");
    assertThat(
        _frr.getInterfaces().get("eth1").getIpAddresses(),
        equalTo(ImmutableList.of(ConcreteInterfaceAddress.parse("1.1.1.1/30"))));
  }

  @Test
  public void testConvertSetInterfaceIpAddress() {
    parseLines("interface eth1", "ip address 1.1.1.1/24");
    Configuration c = _config.toVendorIndependentConfigurations().get(0);
    assertThat(c.getAllInterfaces(), hasKey("eth1"));
    Interface e1 = c.getAllInterfaces().get("eth1");
    Map<ConcreteInterfaceAddress, ConnectedRouteMetadata> metadata = e1.getAddressMetadata();
    assertThat(metadata, hasKey(ConcreteInterfaceAddress.parse("1.1.1.1/24")));
    ConnectedRouteMetadata e1Metadata = metadata.get(ConcreteInterfaceAddress.parse("1.1.1.1/24"));
    assertThat(e1Metadata.getGenerateLocalRoute(), equalTo(Boolean.FALSE));
  }

  @Test
  public void testSetInterfaceShutdown() {
    parseLines("interface eth1", "shutdown");
    assertTrue(_frr.getInterfaces().get("eth1").getShutdown());
  }

  @Test
  public void testGetInterfaceShutdown() {
    parseLines("interface eth1", "ip address 1.1.1.1/30");
    assertFalse(_frr.getInterfaces().get("eth1").getShutdown());
  }

  @Test
  public void testFRRDefaultTraditional() {
    parse("frr defaults traditional\n");
  }

  @Test
  public void testIpForwarding() {
    // does not crash
    parseLines("ip forwarding\n");
  }

  @Test
  public void testNoIpForwarding() {
    parseLines("no ip forwarding\n");
    assertThat(
        Iterables.getOnlyElement(_warnings.getParseWarnings()).getComment(),
        equalTo("This feature is not currently supported"));
  }

  @Test
  public void testNoIpv6Forwarding() {
    parse("no ipv6 forwarding\n");
  }

  @Test
  public void testPasswordEncryption() {
    parse("service password-encryption\n");
  }

  @Test
  public void testLog() {
    parse("log file /var/log/frr/frr.log\n");
    parse("log commands\n");
    parse("log timestamp precision 6\n");
  }

  @Test
  public void testEnd() {
    parseLines("frr version", "end");
  }

  @Test
  public void testPasswordParsing() {
    parseLines("password 8 geC4x9Mm5HYDE", "enable password 8 <SCRUBBED>");
  }

  @Test
  public void testNextMultipleOfFive() {
    assertThat(nextMultipleOfFive(null), equalTo(5L));
    assertThat(nextMultipleOfFive(0L), equalTo(5L));
    assertThat(nextMultipleOfFive(1L), equalTo(5L));
    assertThat(nextMultipleOfFive(4L), equalTo(5L));
    assertThat(nextMultipleOfFive(5L), equalTo(10L));
    assertThat(nextMultipleOfFive(6L), equalTo(10L));
  }

  @Test
  public void testBgpLogNeighborChanges() {
    parseLines("router bgp 1", "bgp log-neighbor-changes");
  }

  @Test
  public void testBgpConfederationId() {
    parseLines("router bgp 65001", "bgp confederation identifier 100");
    assertThat(_frr.getBgpProcess().getDefaultVrf().getConfederationId(), equalTo(100L));
  }

  @Test
  public void testStaticRoute_vrf_noDefinition() {
    _warnings = new Warnings(false, true, false);
    parseLines("ip route 1.2.3.4/24 1.1.1.1 vrf VRF");

    assertThat(
        _warnings.getRedFlagWarnings(),
        contains(
            new Warning(
                "the static route is ignored since vrf VRF is not defined",
                Warnings.TAG_RED_FLAG)));
  }

  @Test
  public void testStaticRoute_vrf_withDefinition() {
    _warnings = new Warnings(false, true, false);

    _frr.getVrfs().put("VRF", new Vrf("VRF"));
    parseLines("ip route 1.2.3.0/24 1.1.1.1 vrf VRF");

    assertThat(_warnings.getRedFlagWarnings(), empty());

    assertThat(
        _frr.getVrfs().get("VRF").getStaticRoutes(),
        contains(new StaticRoute(Prefix.parse("1.2.3.0/24"), Ip.parse("1.1.1.1"), null, null)));
  }

  @Test
  public void testStaticRoute_vrf_withDefinition_withDistance() {
    _warnings = new Warnings(false, true, false);

    _frr.getVrfs().put("VRF", new Vrf("VRF"));
    parseLines("ip route 1.2.3.0/24 1.1.1.1 200 vrf VRF");

    assertThat(_warnings.getRedFlagWarnings(), empty());

    assertThat(
        _frr.getVrfs().get("VRF").getStaticRoutes(),
        contains(new StaticRoute(Prefix.parse("1.2.3.0/24"), Ip.parse("1.1.1.1"), null, 200)));
  }

  @Test
  public void testStaticRoute_defaultVrf() {
    parseLines("ip route 1.2.3.4/24 1.1.1.1");
    assertThat(
        _frr.getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                new StaticRoute(Prefix.parse("1.2.3.4/24"), Ip.parse("1.1.1.1"), null, null))));
  }

  @Test
  public void testStaticRoute_defaultVrf_withDistance() {
    parseLines("ip route 1.2.3.4/24 1.1.1.1 75");
    assertThat(
        _frr.getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                new StaticRoute(Prefix.parse("1.2.3.4/24"), Ip.parse("1.1.1.1"), null, 75))));
  }

  @Test
  public void testBgpNeighborUpdateSource_Address() {
    parseLines("router bgp 65001", "neighbor 1.1.1.1 update-source 2.2.2.2");

    assertNotNull(_frr.getBgpProcess());
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("1.1.1.1").getBgpNeighborSource(),
        equalTo(new BgpNeighborSourceAddress(Ip.parse("2.2.2.2"))));
  }

  @Test
  public void testBgpNeighborUpdateSource_Interface() {
    parseLines("router bgp 65001", "neighbor 1.1.1.1 update-source lo");

    assertNotNull(_frr.getBgpProcess());
    assertThat(
        _frr.getBgpProcess().getDefaultVrf().getNeighbors().get("1.1.1.1").getBgpNeighborSource(),
        equalTo(new BgpNeighborSourceInterface("lo")));
  }

  @Test
  public void testStaticRouteNull0_defaultVrf() {
    parseLines("ip route 1.2.3.4/24 Null0");
    assertThat(
        _frr.getStaticRoutes(),
        equalTo(ImmutableSet.of(new StaticRoute(Prefix.parse("1.2.3.4/24"), null, "Null0", null))));
  }

  @Test
  public void testStaticRouteReject_defaultVrf() {
    parseLines("ip route 1.2.3.4/24 reject");
    assertThat(
        _frr.getStaticRoutes(),
        equalTo(
            ImmutableSet.of(new StaticRoute(Prefix.parse("1.2.3.4/24"), null, "reject", null))));
  }

  @Test
  public void testStaticRouteBlackhole_defaultVrf() {
    parseLines("ip route 1.2.3.4/24 blackhole");
    assertThat(
        _frr.getStaticRoutes(),
        equalTo(
            ImmutableSet.of(new StaticRoute(Prefix.parse("1.2.3.4/24"), null, "blackhole", null))));
  }

  @Test
  public void testStaticRouteInterface_defaultVrf() {
    parseLines("ip route 1.2.3.4/24 eth0 100");
    assertThat(
        _frr.getStaticRoutes(),
        equalTo(ImmutableSet.of(new StaticRoute(Prefix.parse("1.2.3.4/24"), null, "eth0", 100))));
  }

  @Test
  public void testStaticRouteInterface_vrf_withDefinition() {
    _warnings = new Warnings(false, true, false);

    _frr.getVrfs().put("VRF", new Vrf("VRF"));
    parseLines("ip route 1.2.3.0/24 eth0 vrf VRF");

    assertThat(_warnings.getRedFlagWarnings(), empty());

    assertThat(
        _frr.getVrfs().get("VRF").getStaticRoutes(),
        contains(new StaticRoute(Prefix.parse("1.2.3.0/24"), null, "eth0", null)));
  }

  @Test
  public void testBgpcommunityMatchSetTagAndWeight_behavior() throws IOException {
    /*
     There are two nodes in the topology arranged in a line.
       - frr-originator -- frr-listener

     Features tested:
     1) Set comm
     2) Match comm + set tag + set weight

     frr-originator originates routes via network statements and applies a community outbound.

     frr-listener matches on that community and sets a weight and tag.

    */

    String snapshotName = "bgp-comm-match-set-tag-weight";
    List<String> configurationNames = ImmutableList.of("frr-originator", "frr-listener");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);

    assertThat(
        dp.getRibs().get("frr-listener", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.parse("10.2.2.2/32"))
                    .setNextHopIp(Ip.parse("10.1.1.1"))
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.1.1.1")))
                    .setOriginatorIp(Ip.parse("1.1.1.1"))
                    .setOriginType(OriginType.IGP)
                    .setProtocol(RoutingProtocol.BGP)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .setAsPath(AsPath.ofSingletonAsSets(1L))
                    .setCommunities(ImmutableSet.of(StandardCommunity.of(12345, 123)))
                    .setAdmin(20)
                    .setLocalPreference(100)
                    .setWeight(500)
                    .setTag(10000L)
                    .build())));
  }

  @Test
  public void testBgpRedistribution_behavior() throws IOException {
    /*
     There are four nodes in the topology arranged in a line.
       - frr-ospf-originator -- frr-redistributor -- frr-listener

    frr-ospf-originator spawns routes into ospf and is ospf adjacent with frr-redistributor

    frr-redistributor will redist a single OSPF route into BGP and advertise to frr-listener
    */

    String snapshotName = "redistribution-test";
    List<String> configurationNames =
        ImmutableList.of("frr-ospf-originator", "frr-redistributor", "frr-listener");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);

    assertThat(
        dp.getRibs().get("frr-listener", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(
            equalTo(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.parse("2.2.2.2/32"))
                    .setNextHopIp(Ip.parse("10.1.1.1"))
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.1.1.1")))
                    .setOriginatorIp(Ip.parse("1.1.1.1"))
                    .setOriginType(OriginType.INCOMPLETE)
                    .setProtocol(RoutingProtocol.BGP)
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .setAsPath(AsPath.ofSingletonAsSets(1L))
                    .setAdmin(20)
                    .setLocalPreference(100)
                    .setMetric(10)
                    .build())));
  }

  @Test
  public void testStaticRouteNetworkStatementInteraction_behavior() throws IOException {
    String snapshotName = "static-route-network-statement";
    List<String> configurationNames =
        ImmutableList.of("frr-t1-r1", "frr-t1-r2", "frr-t2-r1", "frr-t2-r2");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    assertThat(
        batfish.getTopologyProvider().getBgpTopology(snapshot).getGraph().edges(), hasSize(8));

    assertThat(
        dp.getRibs().get("frr-t2-r1", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(isBgpv4RouteThat(hasPrefix(Prefix.parse("99.13.80.0/21")))));

    assertThat(
        dp.getRibs().get("frr-t2-r2", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(isBgpv4RouteThat(hasPrefix(Prefix.parse("99.13.80.0/21")))));

    assertThat(
        dp.getRibs().get("frr-t2-r1", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(isBgpv4RouteThat(hasPrefix(Prefix.parse("99.8.0.0/20")))));

    assertThat(
        dp.getRibs().get("frr-t2-r2", DEFAULT_VRF_NAME).getRoutes(),
        hasItem(isBgpv4RouteThat(hasPrefix(Prefix.parse("99.8.0.0/20")))));
  }

  @Test
  public void testMainRibIndependentNetworkStatements() throws IOException {
    String snapshotName = "main-rib-independent-network-statement";
    List<String> configurationNames = ImmutableList.of("frr-network");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);

    assertThat(dp.getRibs().get("frr-network", DEFAULT_VRF_NAME).getRoutes(), empty());

    assertThat(
        dp.getBgpRoutes().get("frr-network", DEFAULT_VRF_NAME),
        containsInAnyOrder(
            isBgpv4RouteThat(
                allOf(
                    hasPrefix(Prefix.parse("10.2.2.2/32")),
                    hasWeight(32768),
                    hasNextHop(NextHopDiscard.instance()),
                    hasMetric(0L),
                    isNonRouting(true))),
            isBgpv4RouteThat(
                allOf(
                    hasPrefix(Prefix.parse("10.3.3.3/32")),
                    hasWeight(32768),
                    hasNextHop(NextHopDiscard.instance()),
                    hasMetric(10000L),
                    isNonRouting(true)))));
  }

  @Test
  public void testInterfaceInitOrderNoVrfs() {
    parseLines("interface swp1", "interface swp2", "interface swp1", "interface swp3");

    // init from the back
    assertThat(_frr.getInterfaceInitOrder(), contains("swp3", "swp1", "swp2"));
  }

  @Test
  public void testInterfaceInitOrderVrfs() {
    _frr.getVrfs().put("v1", new Vrf("v1"));
    parseLines(
        "interface swp1 vrf v1",
        "interface swp2 vrf v1",
        "interface swp1 vrf v1",
        "interface swp3 vrf v1");

    // init from the back
    assertThat(_frr.getInterfaceInitOrder(), contains("swp3", "swp1", "swp2"));
  }

  @Test
  public void testBgpTimers() {
    // does not crash
    parseLines("router bgp 65432", "  timers bgp 3 15");
  }

  @Test
  public void testBgpNeighborTimers() {
    // does not crash
    parseLines(
        "router bgp 65432",
        "  neighbor PEERS timers connect 15888",
        "  neighbor PEERS timers delayopen 15",
        "  neighbor PEERS timers 15 23");
  }

  @Test
  public void testBgpNeighborAdvertisementInterval() {
    // does not crash
    parseLines("router bgp 65432", "  neighbor swp49 advertisement-interval 0");
  }

  @Test
  public void testIpv6_noWarnings() {
    // top-level ipv6 commands are not warned
    _warnings = new Warnings(false, true, false);
    parseLines("ipv6 protocol route-map set-src-address");
    assertThat(_warnings.getRedFlagWarnings(), empty());
  }
}
