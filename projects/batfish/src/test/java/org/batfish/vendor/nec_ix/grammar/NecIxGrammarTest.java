package org.batfish.vendor.nec_ix.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.NEC_IX;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.vendor.nec_ix.representation.NecIxStructureType.ACCESS_LIST;
import static org.batfish.vendor.nec_ix.representation.NecIxStructureType.PREFIX_LIST;
import static org.batfish.vendor.nec_ix.representation.NecIxStructureType.ROUTE_MAP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.transformation.TransformationEvaluator;
import org.batfish.datamodel.transformation.TransformationEvaluator.TransformationResult;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.nec_ix.representation.NecIxAccessList;
import org.batfish.vendor.nec_ix.representation.NecIxBgpNeighbor;
import org.batfish.vendor.nec_ix.representation.NecIxBgpVrf;
import org.batfish.vendor.nec_ix.representation.NecIxConfiguration;
import org.batfish.vendor.nec_ix.representation.NecIxConversions;
import org.batfish.vendor.nec_ix.representation.NecIxInterface;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of the NEC IX grammar, extraction, and conversion. */
@ParametersAreNonnullByDefault
public final class NecIxGrammarTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/nec_ix/grammar/testconfigs/";

  /** The terminal capture of `show running-config` given as the sample configuration. */
  private static final String SAMPLE = "tun-01-venue";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  // -------------------------------------------------------------------- sample configuration

  @Test
  public void testSampleParsesCleanly() {
    // The login banner, the commands typed at the prompt, and the trailing prompt are skipped,
    // and every configuration line is recognized.
    NecIxConfiguration vc = parseVendorConfig(SAMPLE);
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getUnrecognized(), equalTo(false));
    assertThat(vc.getHostname(), equalTo("tun-01-venue"));
  }

  @Test
  public void testSampleFormatDetection() throws IOException {
    assertThat(parseConfig(SAMPLE), hasConfigurationFormat(NEC_IX));
  }

  @Test
  public void testSampleInterfaces() throws IOException {
    Configuration c = parseConfig(SAMPLE);
    Map<String, Interface> ifaces = c.getAllInterfaces();

    Interface upstream = ifaces.get("GigaEthernet1.1");
    assertThat(upstream.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    assertThat(upstream.getConcreteAddress(), equalTo(ConcreteInterfaceAddress.parse("10.33.22.167/31")));
    assertThat(upstream.getEncapsulationVlan(), equalTo(1));
    assertThat(upstream.getMtu(), equalTo(1390));
    assertTrue(upstream.getAdminUp());
    assertThat(upstream.getDescription(), equalTo("Upstream: ge-0/0/0.1@er-01.venue"));
    // The sub-interfaces run on the Ethernet port, which is created implicitly.
    assertThat(ifaces.get("GigaEthernet1").getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(
        upstream.getDependencies(),
        contains(new Interface.Dependency("GigaEthernet1", Interface.DependencyType.BIND)));
    assertThat(
        ifaces.get("GigaEthernet2:7.0").getDependencies(),
        contains(new Interface.Dependency("GigaEthernet2:7", Interface.DependencyType.BIND)));

    // `shutdown` and the default state are down; Loopback is always up.
    assertFalse(ifaces.get("GigaEthernet1.0").getAdminUp());
    assertFalse(ifaces.get("BRI0.0").getAdminUp());
    assertTrue(ifaces.get("Loopback0.0").getAdminUp());
    assertThat(ifaces.get("Loopback0.0").getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(ifaces.get("Null0.0").getInterfaceType(), equalTo(InterfaceType.NULL));
    assertThat(ifaces.get("Tunnel10.0").getInterfaceType(), equalTo(InterfaceType.TUNNEL));

    // The NGN uplink is IPv6-only, and only IPv6 filters are applied anywhere.
    assertThat(ifaces.get("GigaEthernet0.0").getAllAddresses(), empty());
    assertThat(
        ifaces.values().stream().map(Interface::getIncomingFilter).toList(),
        everyItem(nullValue()));
    assertThat(
        ifaces.values().stream().map(Interface::getOutgoingFilter).toList(),
        everyItem(nullValue()));
  }

  @Test
  public void testSampleBgp() throws IOException {
    Configuration c = parseConfig(SAMPLE);
    BgpProcess proc = c.getDefaultVrf().getBgpProcess();
    assertThat(proc.getRouterId(), equalTo(Ip.parse("10.33.0.161")));
    assertThat(proc.getOriginationSpace().getPrefixRanges(), hasSize(1));

    BgpActivePeerConfig er01 = proc.getActiveNeighbors().get(Ip.parse("10.33.22.166"));
    assertThat(er01.getLocalAs(), equalTo(65161L));
    assertThat(er01.getRemoteAsns().enumerate(), contains(65010L));
    assertThat(er01.getLocalIp(), equalTo(Ip.parse("10.33.22.167")));

    // bb-in permits pl-private (10.33.0.0/16 up to /32) with local-preference 100.
    RoutingPolicy in = c.getRoutingPolicies().get(er01.getIpv4UnicastAddressFamily().getImportPolicy());
    Bgpv4Route.Builder accepted = ebgpRoute(Prefix.parse("10.33.1.0/24")).toBuilder();
    assertTrue(in.process(ebgpRoute(Prefix.parse("10.33.1.0/24")), accepted, Direction.IN));
    assertThat(accepted.getLocalPreference(), equalTo(100L));
    assertFalse(in.processReadOnly(ebgpRoute(Prefix.parse("192.0.2.0/24"))));

    // bb-out entries 2 and 3 match the prefix-lists pl-public and default, which the sample does
    // not define. They match nothing, so the default route is not advertised.
    RoutingPolicy out = c.getRoutingPolicies().get(er01.getIpv4UnicastAddressFamily().getExportPolicy());
    assertTrue(out.processReadOnly(ebgpRoute(Prefix.parse("10.33.0.161/32"))));
    assertFalse(out.processReadOnly(ebgpRoute(Prefix.ZERO)));
  }

  @Test
  public void testSampleUndefinedReferences() throws IOException {
    Batfish batfish = getBatfishForConfigurationNames(SAMPLE);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + SAMPLE;
    assertThat(ccae, hasUndefinedReference(filename, PREFIX_LIST, "default"));
    assertThat(ccae, hasUndefinedReference(filename, PREFIX_LIST, "pl-public"));
    assertThat(ccae, not(hasUndefinedReference(filename, PREFIX_LIST, "pl-private")));
  }

  // -------------------------------------------------------------------------- null commands

  @Test
  public void testNullCommandsParseWithoutWarnings() {
    // Every command that does not affect what Batfish models is recognized, including blocks
    // (policy-map, ikev2, ipv6 dhcp client-profile, ip dhcp profile, device) and IPv6.
    NecIxConfiguration vc = parseVendorConfig("ix_null_commands");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getUnrecognized(), equalTo(false));
    assertThat(vc.getInterfaces().keySet(), contains("GigaEthernet0.0"));
    NecIxInterface iface = vc.getInterfaces().get("GigaEthernet0.0");
    assertThat(iface.getDescription(), equalTo("Circuit: NGN, {test}"));
    assertThat(iface.getAddress(), nullValue());
    assertThat(iface.getShutdown(), equalTo(false));
  }

  // ----------------------------------------------------------------------------- interfaces

  @Test
  public void testInterfaceExtraction() {
    NecIxConfiguration vc = parseVendorConfig("ix_interfaces");
    assertThat(vc.getWarnings().getParseWarnings(), empty());

    NecIxInterface ge00 = vc.getInterfaces().get("GigaEthernet0.0");
    assertThat(ge00.getType(), equalTo(NecIxInterface.Type.ETHERNET));
    assertThat(ge00.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.0.1/24")));
    assertThat(
        ge00.getSecondaryAddresses(), contains(ConcreteInterfaceAddress.parse("10.0.1.1/24")));
    assertThat(ge00.getMtu(), equalTo(1390));
    assertThat(vc.getInterfaces().get("GigaEthernet0.10").getVlan(), equalTo(10));
    assertThat(vc.getInterfaces().get("GigaEthernet2:7.0").getBridgeGroup(), equalTo(7));
    assertThat(vc.getInterfaces().get("Tunnel1.0").getBridgeGroup(), equalTo(7));
    assertThat(vc.getInterfaces().get("Tunnel2.0").getVrf(), equalTo("red"));
    assertThat(vc.getInterfaces().get("BVI1").getType(), equalTo(NecIxInterface.Type.BVI));
    assertThat(vc.getShutdownDevices(), containsInAnyOrder("GigaEthernet3", "USB0"));

    assertThat(NecIxInterface.computeParentPort("GigaEthernet2:7.0"), equalTo("GigaEthernet2:7"));
    assertThat(NecIxInterface.computeParentPort("Tunnel1.0"), nullValue());
    assertThat(NecIxInterface.computeDevice("GigaEthernet2:7.0"), equalTo("GigaEthernet2"));
    assertThat(NecIxInterface.computeDevice("USB-Serial0.0"), equalTo("USB0"));
  }

  @Test
  public void testInterfaceConversion() throws IOException {
    Configuration c = parseConfig("ix_interfaces");
    Map<String, Interface> ifaces = c.getAllInterfaces();
    assertThat(
        ifaces.keySet(),
        containsInAnyOrder(
            "GigaEthernet0",
            "GigaEthernet0.0",
            "GigaEthernet0.10",
            "GigaEthernet1",
            "GigaEthernet1.0",
            "GigaEthernet1.1",
            "GigaEthernet2:7",
            "GigaEthernet2:7.0",
            "GigaEthernet3",
            "GigaEthernet3.0",
            "USB-Serial0.0",
            "Loopback0.0",
            "Null0.0",
            "Tunnel1.0",
            "Tunnel2.0",
            "BVI1"));

    Interface ge00 = ifaces.get("GigaEthernet0.0");
    assertThat(
        ge00.getAllConcreteAddresses(),
        containsInAnyOrder(
            ConcreteInterfaceAddress.parse("10.0.0.1/24"),
            ConcreteInterfaceAddress.parse("10.0.1.1/24")));
    assertThat(ge00.getMtu(), equalTo(1390));
    assertThat(ifaces.get("GigaEthernet0.10").getMtu(), equalTo(1500));

    // "Default: the interface is stopped": only `no shutdown` brings an interface up.
    assertTrue(ge00.getAdminUp());
    assertFalse(ifaces.get("GigaEthernet1.0").getAdminUp());
    assertFalse(ifaces.get("GigaEthernet1.1").getAdminUp());
    assertTrue(ifaces.get("Loopback0.0").getAdminUp());
    assertTrue(ifaces.get("Null0.0").getAdminUp());
    // `shutdown` in device mode stops the port and everything on it.
    assertFalse(ifaces.get("GigaEthernet3").getAdminUp());
    assertFalse(ifaces.get("GigaEthernet3.0").getAdminUp());
    assertFalse(ifaces.get("USB-Serial0.0").getAdminUp());

    assertThat(ifaces.get("Tunnel2.0").getVrfName(), equalTo("red"));
    assertThat(c.getVrfs().keySet(), containsInAnyOrder(DEFAULT_VRF_NAME, "red"));
    assertThat(ifaces.get("BVI1").getInterfaceType(), equalTo(InterfaceType.LOGICAL));
  }

  // ----------------------------------------------------------------------------- access-lists

  @Test
  public void testAccessListExtraction() {
    NecIxConfiguration vc = parseVendorConfig("ix_access_lists");
    NecIxAccessList seq = vc.getAccessLists().get("seq");
    // In sequence mode entries are evaluated in sequence-number order, not configuration order.
    assertThat(
        seq.getOrderedLines().stream().map(l -> l.getSequence()).toList(), contains(100L, 200L));
    assertThat(
        vc.getAccessLists().get("p-web").getLines().get(0).getText(),
        equalTo("permit tcp src 10.0.0.0/8 sport any dest 192.0.2.0 0.0.0.255 dport eq 443"));
    assertThat(
        vc.getInterfaces().get("GigaEthernet0.0").getInboundFilters(),
        equalTo(
            ImmutableMap.of(
                10, "p-tcpest", 20, "undefined-acl", 100, "p-web", 9000, "d-all")));
  }

  @Test
  public void testAccessListWarnings() {
    // Domain names and TOS cannot be modeled, and an unknown ICMP message cannot be interpreted.
    // Each entry is dropped with a warning rather than being widened to match more packets.
    NecIxConfiguration vc = parseVendorConfig("ix_access_lists");
    assertThat(
        vc.getWarnings().getParseWarnings(),
        contains(
            hasComment("Batfish cannot match on domain names; ignoring this access-list entry"),
            hasComment("Batfish cannot match on TOS; ignoring this access-list entry"),
            hasComment("Unknown ICMP message 'no-such-message'; ignoring this access-list entry")));
    assertThat(vc.getAccessLists().get("bad").getLines(), empty());
  }

  @Test
  public void testAccessListConversion() throws IOException {
    Configuration c = parseConfig("ix_access_lists");
    Map<String, IpAccessList> acls = c.getIpAccessLists();

    assertPermits(acls, "p-echo", icmp(8, 0));
    assertDenies(acls, "p-echo", icmp(0, 0));
    assertPermits(acls, "p-unreach", icmp(3, 4));
    assertDenies(acls, "p-unreach", icmp(3, 1));
    // `established`: ACK or RST is set.
    assertPermits(acls, "p-tcpest", tcp("1.1.1.1", "2.2.2.2", 80, 1024, TcpFlags.builder().setAck(true).build()));
    assertPermits(acls, "p-tcpest", tcp("1.1.1.1", "2.2.2.2", 80, 1024, TcpFlags.builder().setRst(true).build()));
    assertDenies(acls, "p-tcpest", tcp("1.1.1.1", "2.2.2.2", 1024, 80, TcpFlags.builder().setSyn(true).build()));
    // Address with wildcard bits.
    assertPermits(acls, "p-web", tcp("10.1.1.1", "192.0.2.9", 1024, 443, syn()));
    assertDenies(acls, "p-web", tcp("10.1.1.1", "192.0.3.9", 1024, 443, syn()));
    assertDenies(acls, "p-web", tcp("172.16.1.1", "192.0.2.9", 1024, 443, syn()));
    assertPermits(acls, "p-web", tcp("172.16.1.1", "192.0.2.9", 1024, 8080, syn()));
    // lt, gt, neq.
    assertMatches(acls, "d-low", udp(1023, 9), LineAction.DENY);
    assertNoMatch(acls, "d-low", udp(1024, 9));
    assertMatches(acls, "d-high", udp(60001, 54), LineAction.DENY);
    assertNoMatch(acls, "d-high", udp(60001, 53));
    assertNoMatch(acls, "d-high", udp(60000, 54));
    // Protocol number, DSCP, and precedence (the top three bits of DSCP).
    assertPermits(acls, "p-esp", Flow.builder().setIngressNode("n").setSrcIp(Ip.parse("1.1.1.1")).setDstIp(Ip.parse("2.2.2.2")).setIpProtocol(IpProtocol.ESP).build());
    assertPermits(acls, "p-af41", dscp(34));
    assertNoMatch(acls, "p-af41", dscp(0));
    assertPermits(acls, "p-prec5", dscp(46));
    assertNoMatch(acls, "p-prec5", dscp(34));

    // A sequence-mode list is evaluated in sequence-number order.
    assertMatches(acls, "seq", udp("10.1.2.3", 1024, 53), LineAction.DENY);
    assertMatches(acls, "seq", udp("10.2.3.4", 1024, 53), LineAction.PERMIT);
  }

  @Test
  public void testInterfaceFilterConversion() throws IOException {
    Configuration c = parseConfig("ix_access_lists");
    Interface ge00 = c.getAllInterfaces().get("GigaEthernet0.0");
    IpAccessList in = ge00.getIncomingFilter();
    assertThat(in.getName(), equalTo(NecIxConversions.computeInboundFilterName("GigaEthernet0.0")));
    // Lists in ascending filter sequence number, without the undefined one, then the implicit
    // deny.
    assertThat(
        in.getLines().stream().map(l -> l.getName()).toList(),
        contains(
            "ip filter p-tcpest 10",
            "ip filter p-web 100",
            "ip filter d-all 9000",
            "implicit deny: no access-list matched"));

    assertThat(filter(c, in, tcp("1.1.1.1", "192.0.2.1", 443, 1024, TcpFlags.builder().setAck(true).build())), equalTo(LineAction.PERMIT));
    assertThat(filter(c, in, tcp("10.0.0.1", "192.0.2.1", 1024, 443, syn())), equalTo(LineAction.PERMIT));
    assertThat(filter(c, in, tcp("172.16.0.1", "192.0.2.1", 1024, 443, syn())), equalTo(LineAction.DENY));
    assertThat(ge00.getOutgoingFilter().getLines(), hasSize(2));

    // A packet matching none of the applied lists is discarded, even though no list denies it.
    IpAccessList ge10in = c.getAllInterfaces().get("GigaEthernet1.0").getIncomingFilter();
    assertThat(filter(c, ge10in, icmp(8, 0)), equalTo(LineAction.PERMIT));
    assertThat(filter(c, ge10in, udp(1024, 53)), equalTo(LineAction.DENY));

    // Interfaces without `ip filter` pass everything.
    assertThat(c.getAllInterfaces().get("GigaEthernet0").getIncomingFilter(), nullValue());
  }

  @Test
  public void testAccessListReferences() throws IOException {
    Batfish batfish = getBatfishForConfigurationNames("ix_access_lists");
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/ix_access_lists";
    assertThat(ccae, hasUndefinedReference(filename, ACCESS_LIST, "undefined-acl"));
    assertThat(ccae, not(hasUndefinedReference(filename, ACCESS_LIST, "p-all")));
  }

  // --------------------------------------------------------------- prefix-lists and route-maps

  @Test
  public void testPrefixListConversion() throws IOException {
    Configuration c = parseConfig("ix_route_maps");
    assertThat(lengths(c, "pl-default"), equalTo(new SubRange(0, 0)));
    // `max` alone: from the prefix length up to max.
    assertThat(lengths(c, "pl-private"), equalTo(new SubRange(8, 32)));
    // Neither: the prefix itself.
    assertThat(lengths(c, "pl-exact"), equalTo(new SubRange(16, 16)));
    // `min` alone: from min up to /32.
    assertThat(lengths(c, "pl-min"), equalTo(new SubRange(24, 32)));
    assertThat(lengths(c, "pl-range"), equalTo(new SubRange(20, 24)));
    // `any` is 0.0.0.0/0 min 0 max 32.
    assertThat(
        c.getRouteFilterLists().get("pl-any").getLines().get(1).getLengthRange(),
        equalTo(new SubRange(0, 32)));
    // Names are case-insensitive.
    assertThat(c.getRouteFilterLists().keySet(), hasItem("pl-upper"));
  }

  @Test
  public void testRouteMapConversion() throws IOException {
    Configuration c = parseConfig("ix_route_maps");
    RoutingPolicy rm = c.getRoutingPolicies().get("rm");
    CommunitySet input = CommunitySet.of(StandardCommunity.of(1, 1));

    // Entries are evaluated in sequence order, so deny 5 is tried before permit 10.
    assertFalse(rm.processReadOnly(ebgpRoute(Prefix.parse("10.1.0.0/16"))));

    // permit 10 sets local-preference, metric, and replaces the communities.
    Bgpv4Route private10 = ebgpRoute(Prefix.parse("10.9.0.0/16")).toBuilder().setCommunities(input).build();
    Bgpv4Route.Builder out = private10.toBuilder();
    assertTrue(rm.process(private10, out, Direction.IN));
    assertThat(out.getLocalPreference(), equalTo(200L));
    assertThat(out.getMetric(), equalTo(50L));
    assertThat(
        out.build().getCommunities().getCommunities(),
        containsInAnyOrder(StandardCommunity.of(65000, 100), StandardCommunity.of(65000, 200)));

    // permit 20 adds a community and prepends the AS path.
    Bgpv4Route defaultRoute = ebgpRoute(Prefix.ZERO).toBuilder().setCommunities(input).build();
    // An outbound policy starts from a route without a next hop.
    out = defaultRoute.toBuilder().setNextHop(NextHopDiscard.instance());
    assertTrue(rm.process(defaultRoute, out, Direction.OUT));
    Bgpv4Route defaultOut = out.build();
    assertThat(
        defaultOut.getCommunities().getCommunities(),
        containsInAnyOrder(StandardCommunity.of(1, 1), StandardCommunity.of(65000, 300)));
    assertThat(defaultOut.getAsPath(), equalTo(AsPath.ofSingletonAsSets(65000L, 65000L, 65100L)));

    // A route matching no entry is rejected.
    assertFalse(rm.processReadOnly(ebgpRoute(Prefix.parse("192.0.2.0/24"))));

    // An entry without match commands matches everything.
    assertTrue(c.getRoutingPolicies().get("rm-all").processReadOnly(ebgpRoute(Prefix.parse("192.0.2.0/24"))));
    // Prefix-list names are case-insensitive.
    assertTrue(c.getRoutingPolicies().get("rm-upper").processReadOnly(ebgpRoute(Prefix.parse("10.5.0.0/16"))));
    // An undefined prefix-list matches nothing.
    assertFalse(c.getRoutingPolicies().get("rm-undefined").processReadOnly(ebgpRoute(Prefix.parse("10.0.0.0/8"))));
  }

  @Test
  public void testRouteMapReferences() throws IOException {
    Batfish batfish = getBatfishForConfigurationNames("ix_route_maps");
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/ix_route_maps";
    assertThat(ccae, hasUndefinedReference(filename, PREFIX_LIST, "no-such-list"));
    assertThat(ccae, not(hasUndefinedReference(filename, PREFIX_LIST, "pl-upper")));
    assertThat(ccae, not(hasUndefinedReference(filename, ROUTE_MAP, "rm")));
  }

  // ---------------------------------------------------------------------------- static routes

  @Test
  public void testStaticRouteConversion() throws IOException {
    Configuration c = parseConfig("ix_static_routes");
    Set<StaticRoute> routes = c.getDefaultVrf().getStaticRoutes();
    assertThat(routes, hasSize(4));
    assertThat(route(routes, Prefix.ZERO).getNextHop(), equalTo(NextHopIp.of(Ip.parse("10.0.0.254"))));
    assertThat(route(routes, Prefix.parse("10.10.0.0/16")).getNextHop(), equalTo(NextHopDiscard.instance()));
    assertThat(
        route(routes, Prefix.parse("10.20.0.0/16")).getNextHop(),
        equalTo(NextHopInterface.of("GigaEthernet0.0")));
    StaticRoute withOptions = route(routes, Prefix.parse("10.30.0.0/16"));
    assertThat(
        withOptions.getNextHop(),
        equalTo(NextHopInterface.of("GigaEthernet0.0", Ip.parse("10.0.0.253"))));
    assertThat(withOptions.getAdministrativeCost(), equalTo(5L));
    assertThat(withOptions.getMetric(), equalTo(2L));
    assertThat(withOptions.getTag(), equalTo(100L));
    // Defaults: distance 1, metric 1.
    assertThat(route(routes, Prefix.ZERO).getAdministrativeCost(), equalTo(1L));
    assertThat(route(routes, Prefix.ZERO).getMetric(), equalTo(1L));

    assertThat(
        c.getVrfs().get("red").getStaticRoutes().stream().map(StaticRoute::getNetwork).toList(),
        contains(Prefix.parse("172.16.10.0/24")));
  }

  // ------------------------------------------------------------------------------------ NAT

  @Test
  public void testNaptTranslatesInsideSourcesToNaptAddress() throws IOException {
    Configuration c = parseConfig("ix_nat");
    Interface outside = c.getAllInterfaces().get("GigaEthernet1.0");
    Flow translated =
        eval(c, outside.getOutgoingTransformation(), tcp("10.0.0.5", "8.8.8.8", 12345, 443, syn()));
    assertThat(translated.getSrcIp(), equalTo(Ip.parse("203.0.113.100")));
    assertTrue(translated.getSrcPort() >= 1025);
    assertThat(translated.getDstIp(), equalTo(Ip.parse("8.8.8.8")));

    // Sources outside `ip napt inside list` are not translated.
    Flow notInside = tcp("10.1.0.5", "8.8.8.8", 12345, 443, syn());
    assertThat(eval(c, outside.getOutgoingTransformation(), notInside), equalTo(notInside));

    // Return traffic is handled by a session.
    assertThat(outside.getFirewallSessionInterfaceInfo(), notNullValue());
  }

  @Test
  public void testNaptDefaults() throws IOException {
    // Without `ip napt address` and `ip napt inside list`, every source is translated to the
    // interface address, using ports 49152-65535.
    Configuration c = parseConfig("ix_nat");
    Flow translated =
        eval(
            c,
            c.getAllInterfaces().get("GigaEthernet2.0").getOutgoingTransformation(),
            tcp("172.16.0.5", "8.8.8.8", 12345, 443, syn()));
    assertThat(translated.getSrcIp(), equalTo(Ip.parse("198.51.100.1")));
    assertTrue(translated.getSrcPort() >= 49152);
  }

  @Test
  public void testNaptWarnings() {
    // Port forwarding is not modeled and says so; session timers are silently ignored.
    NecIxConfiguration vc = parseVendorConfig("ix_nat");
    assertThat(
        vc.getWarnings().getParseWarnings(),
        contains(hasComment("This feature is not currently supported")));
    assertThat(
        vc.getInterfaces().get("GigaEthernet1.0").getNaptPortRanges(),
        contains(new SubRange(1025, 65535)));
  }

  @Test
  public void testStaticNat() throws IOException {
    Configuration c = parseConfig("ix_nat");
    Interface outside = c.getAllInterfaces().get("GigaEthernet3.0");
    // Outgoing: the inside address becomes the outside address.
    assertThat(
        eval(c, outside.getOutgoingTransformation(), tcp("10.0.0.10", "8.8.8.8", 1024, 80, syn()))
            .getSrcIp(),
        equalTo(Ip.parse("192.0.2.10")));
    assertThat(
        eval(c, outside.getOutgoingTransformation(), tcp("10.0.1.7", "8.8.8.8", 1024, 80, syn()))
            .getSrcIp(),
        equalTo(Ip.parse("198.18.1.7")));
    // Incoming: the outside address is mapped back to the inside address.
    assertThat(
        eval(c, outside.getIncomingTransformation(), tcp("8.8.8.8", "192.0.2.10", 1024, 80, syn()))
            .getDstIp(),
        equalTo(Ip.parse("10.0.0.10")));
  }

  // ------------------------------------------------------------------------------------ BGP

  @Test
  public void testBgpExtraction() {
    NecIxConfiguration vc = parseVendorConfig("ix_bgp_a");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    NecIxBgpVrf bgp = vc.getBgpProcess().getVrfs().get(DEFAULT_VRF_NAME);
    assertThat(bgp.getOriginateDefault(), equalTo(NecIxBgpVrf.OriginateDefault.CONDITIONAL));
    assertThat(bgp.getNetworks(), hasSize(4));
    NecIxBgpNeighbor b = bgp.getNeighbors().get(Ip.parse("192.168.0.0"));
    assertThat(b.getRemoteAs(), equalTo(65002L));
    assertThat(b.getRouteMapOut(), equalTo("to-b"));
    assertTrue(b.getSendDefault());
    assertFalse(bgp.getNeighbors().get(Ip.parse("192.168.1.0")).getSendDefault());
  }

  @Test
  public void testBgpVrf() throws IOException {
    // The sample's neighbors are all in the default VRF; tun-01.nrt style BGP-VRF mode is
    // exercised here with the router ID inherited from the global setting.
    String text =
        "! IX Series IX2215 (magellan-sec) Software, Version 10.9.11, RELEASE SOFTWARE\n"
            + "hostname ix_bgp_vrf\n"
            + "router bgp 65000\n"
            + "  router-id 10.255.0.1\n"
            + "  neighbor 10.0.0.2 remote-as 65001\n"
            + "  vrf blue\n"
            + "    neighbor 10.1.0.2 remote-as 65002\n"
            + "    address-family ipv4 unicast\n"
            + "      neighbor 10.1.0.2 route-map rm in\n"
            + "      network 10.9.0.0/16\n"
            + "!\n"
            + "interface GigaEthernet0.0\n"
            + "  ip address 10.0.0.1/30\n"
            + "  no shutdown\n"
            + "!\n"
            + "interface GigaEthernet1.0\n"
            + "  ip vrf forwarding blue\n"
            + "  ip address 10.1.0.1/30\n"
            + "  no shutdown\n"
            + "!\n";
    NecIxConfiguration vc = parseVendorConfigText(text, "ix_bgp_vrf");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getBgpProcess().getVrfs().get(DEFAULT_VRF_NAME).getNeighbors().keySet(), contains(Ip.parse("10.0.0.2")));
    NecIxBgpVrf blue = vc.getBgpProcess().getVrfs().get("blue");
    assertThat(blue.getNeighbors().keySet(), contains(Ip.parse("10.1.0.2")));
    assertThat(blue.getNeighbors().get(Ip.parse("10.1.0.2")).getRouteMapIn(), equalTo("rm"));
    assertThat(blue.getNetworks(), contains(Prefix.parse("10.9.0.0/16")));

    Configuration c = vc.toVendorIndependentConfigurations().get(0);
    BgpProcess blueProc = c.getVrfs().get("blue").getBgpProcess();
    assertThat(blueProc.getRouterId(), equalTo(Ip.parse("10.255.0.1")));
    assertThat(blueProc.getActiveNeighbors().get(Ip.parse("10.1.0.2")).getLocalIp(), equalTo(Ip.parse("10.1.0.1")));
  }

  @Test
  public void testBgpInDataPlane() throws IOException {
    Batfish batfish = getBatfishForConfigurationNames("ix_bgp_a", "ix_bgp_b", "ix_bgp_c");
    batfish.computeDataPlane(batfish.getSnapshot());
    var bgpRoutes = batfish.loadDataPlane(batfish.getSnapshot()).getBgpRoutes();

    // B: `network` prefixes present in A's routing table, and the default route from
    // `originate-default`. 10.2.0.0/16 is denied by A's outbound route-map, and 10.99.0.0/16 is
    // not in A's routing table.
    Map<Prefix, Bgpv4Route> b = routesByPrefix(bgpRoutes.get("ix_bgp_b", DEFAULT_VRF_NAME));
    assertThat(
        b.keySet(),
        containsInAnyOrder(Prefix.parse("10.0.0.1/32"), Prefix.parse("10.1.0.0/16"), Prefix.ZERO));
    // B's inbound route-map sets local-preference 200 on the loopback; other routes get B's
    // default-local-preference.
    assertThat(b.get(Prefix.parse("10.0.0.1/32")).getLocalPreference(), equalTo(200L));
    assertThat(b.get(Prefix.parse("10.1.0.0/16")).getLocalPreference(), equalTo(99L));
    // A's outbound route-map adds a community.
    assertThat(
        b.get(Prefix.parse("10.1.0.0/16")).getCommunities().getCommunities(),
        contains(StandardCommunity.of(65001, 100)));
    assertThat(b.get(Prefix.parse("10.0.0.1/32")).getOriginType(), equalTo(OriginType.IGP));

    // C: no outbound route-map, so 10.2.0.0/16 is advertised; `no neighbor send-default` stops
    // the default route.
    assertThat(
        routesByPrefix(bgpRoutes.get("ix_bgp_c", DEFAULT_VRF_NAME)).keySet(),
        containsInAnyOrder(
            Prefix.parse("10.0.0.1/32"), Prefix.parse("10.1.0.0/16"), Prefix.parse("10.2.0.0/16")));
  }

  // ------------------------------------------------------------------------------ helpers

  private static @Nonnull Map<Prefix, Bgpv4Route> routesByPrefix(Set<Bgpv4Route> routes) {
    return routes.stream()
        .collect(ImmutableMap.toImmutableMap(Bgpv4Route::getNetwork, r -> r, (r1, r2) -> r1));
  }

  private static @Nonnull Bgpv4Route ebgpRoute(Prefix network) {
    return Bgpv4Route.testBuilder()
        .setNetwork(network)
        .setProtocol(RoutingProtocol.BGP)
        .setOriginType(OriginType.IGP)
        .setAsPath(AsPath.ofSingletonAsSets(65100L))
        .setNextHop(NextHopIp.of(Ip.parse("192.0.2.1")))
        .build();
  }

  private static @Nonnull SubRange lengths(Configuration c, String prefixList) {
    return c.getRouteFilterLists().get(prefixList).getLines().get(0).getLengthRange();
  }

  private static @Nonnull StaticRoute route(Set<StaticRoute> routes, Prefix network) {
    return routes.stream().filter(r -> r.getNetwork().equals(network)).findFirst().orElseThrow();
  }

  private static @Nonnull TcpFlags syn() {
    return TcpFlags.builder().setSyn(true).build();
  }

  private static @Nonnull Flow tcp(String src, String dst, int srcPort, int dstPort, TcpFlags flags) {
    return Flow.builder()
        .setIngressNode("n")
        .setSrcIp(Ip.parse(src))
        .setDstIp(Ip.parse(dst))
        .setIpProtocol(IpProtocol.TCP)
        .setSrcPort(srcPort)
        .setDstPort(dstPort)
        .setTcpFlags(flags)
        .build();
  }

  private static @Nonnull Flow udp(int srcPort, int dstPort) {
    return udp("1.1.1.1", srcPort, dstPort);
  }

  private static @Nonnull Flow udp(String src, int srcPort, int dstPort) {
    return Flow.builder()
        .setIngressNode("n")
        .setSrcIp(Ip.parse(src))
        .setDstIp(Ip.parse("2.2.2.2"))
        .setIpProtocol(IpProtocol.UDP)
        .setSrcPort(srcPort)
        .setDstPort(dstPort)
        .build();
  }

  private static @Nonnull Flow icmp(int type, int code) {
    return Flow.builder()
        .setIngressNode("n")
        .setSrcIp(Ip.parse("1.1.1.1"))
        .setDstIp(Ip.parse("2.2.2.2"))
        .setIpProtocol(IpProtocol.ICMP)
        .setIcmpType(type)
        .setIcmpCode(code)
        .build();
  }

  private static @Nonnull Flow dscp(int dscp) {
    return Flow.builder()
        .setIngressNode("n")
        .setSrcIp(Ip.parse("1.1.1.1"))
        .setDstIp(Ip.parse("2.2.2.2"))
        .setIpProtocol(IpProtocol.UDP)
        .setSrcPort(1024)
        .setDstPort(53)
        .setDscp(dscp)
        .build();
  }

  private static @Nonnull LineAction filter(Configuration c, IpAccessList acl, Flow flow) {
    return acl.filter(flow, null, c.getIpAccessLists(), c.getIpSpaces()).getAction();
  }

  private static void assertMatches(
      Map<String, IpAccessList> acls, String name, Flow flow, LineAction action) {
    IpAccessList acl = acls.get(name);
    assertThat(
        acl.filter(flow, null, acls, ImmutableMap.of()).getMatchLine(), notNullValue());
    assertThat(acl.filter(flow, null, acls, ImmutableMap.of()).getAction(), equalTo(action));
  }

  private static void assertNoMatch(Map<String, IpAccessList> acls, String name, Flow flow) {
    assertThat(
        acls.get(name).filter(flow, null, acls, ImmutableMap.of()).getMatchLine(), nullValue());
  }

  private static void assertPermits(Map<String, IpAccessList> acls, String name, Flow flow) {
    assertMatches(acls, name, flow, LineAction.PERMIT);
  }

  private static void assertDenies(Map<String, IpAccessList> acls, String name, Flow flow) {
    // The list itself has no trailing deny, so "denies" means it does not permit.
    assertThat(
        acls.get(name).filter(flow, null, acls, ImmutableMap.of()).getAction(),
        equalTo(LineAction.DENY));
  }

  private static @Nonnull Flow eval(
      Configuration c, org.batfish.datamodel.transformation.Transformation t, Flow flow) {
    TransformationResult result =
        TransformationEvaluator.eval(t, flow, "GigaEthernet0.0", c.getIpAccessLists(), c.getIpSpaces());
    return result.getOutputFlow();
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Map<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());
    assertThat(configs.keySet(), contains(hostname));
    return configs.get(hostname);
  }

  private @Nonnull NecIxConfiguration parseVendorConfig(String filename) {
    return parseVendorConfigText(
        readResource(TESTCONFIGS_PREFIX + filename, UTF_8), TESTCONFIGS_PREFIX + filename);
  }

  /** Parses {@code src} directly as an NEC IX config, attributing it to {@code name}. */
  private @Nonnull NecIxConfiguration parseVendorConfigText(String src, String name) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    NecIxCombinedParser parser = new NecIxCombinedParser(src, settings);
    Warnings warnings = new Warnings(true, true, true);
    NecIxControlPlaneExtractor extractor =
        new NecIxControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    NecIxConfiguration vc = (NecIxConfiguration) extractor.getVendorConfiguration();
    vc.setFilename(name);

    // Crash if the vendor model is not serializable.
    vc = SerializationUtils.clone(vc);
    vc.setWarnings(warnings);
    return vc;
  }
}
