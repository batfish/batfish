package org.batfish.grammar.cumulus_nclu;

import static org.batfish.datamodel.BgpPeerConfig.ALL_AS_NUMBERS;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasExportPolicy;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasInterfaceNeighbors;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.BgpUnnumberedPeerConfigMatchers.hasPeerInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDefaultVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAccessVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAdditionalArpIps;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllowedVlans;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDependencies;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasEncapsulationVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMlagId;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasNativeVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isSwitchport;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasInterfaces;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.batfish.representation.cumulus.BgpProcess.BGP_UNNUMBERED_IP;
import static org.batfish.representation.cumulus.CumulusNcluConfiguration.CUMULUS_CLAG_DOMAIN_ID;
import static org.batfish.representation.cumulus.CumulusNcluConfiguration.computeBgpPeerExportPolicyName;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.Mlag;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.vendor_family.cumulus.InterfaceClagSettings;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cumulus.BgpInterfaceNeighbor;
import org.batfish.representation.cumulus.BgpL2vpnEvpnAddressFamily;
import org.batfish.representation.cumulus.BgpProcess;
import org.batfish.representation.cumulus.BgpRedistributionPolicy;
import org.batfish.representation.cumulus.BgpVrf;
import org.batfish.representation.cumulus.Bond;
import org.batfish.representation.cumulus.CumulusInterfaceType;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusRoutingProtocol;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.Interface;
import org.batfish.representation.cumulus.RemoteAsType;
import org.batfish.representation.cumulus.RouteMap;
import org.batfish.representation.cumulus.RouteMapMatchInterface;
import org.batfish.representation.cumulus.StaticRoute;
import org.batfish.vendor.VendorConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class CumulusNcluGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cumulus_nclu/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private void assertRoutingPolicyDeniesNetwork(RoutingPolicy routingPolicy, Prefix network) {
    assertFalse(
        routingPolicy.process(
            new ConnectedRoute(network, "dummy"),
            BgpRoute.builder().setNetwork(network),
            Ip.parse("192.0.2.1"),
            DEFAULT_VRF_NAME,
            Direction.OUT));
  }

  private void assertRoutingPolicyPermitsNetwork(RoutingPolicy routingPolicy, Prefix network) {
    assertTrue(
        routingPolicy.process(
            new ConnectedRoute(network, "dummy"),
            BgpRoute.builder().setNetwork(network),
            Ip.parse("192.0.2.1"),
            DEFAULT_VRF_NAME,
            Direction.OUT));
  }

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull BgpRoute.Builder makeBgpOutputRouteBuilder() {
    return BgpRoute.builder()
        .setNetwork(Prefix.ZERO)
        .setOriginType(OriginType.INCOMPLETE)
        .setOriginatorIp(Ip.ZERO)
        .setProtocol(RoutingProtocol.BGP);
  }

  private @Nonnull BgpRoute makeBgpRoute(Prefix prefix) {
    return BgpRoute.builder()
        .setNetwork(prefix)
        .setOriginType(OriginType.INCOMPLETE)
        .setOriginatorIp(Ip.ZERO)
        .setProtocol(RoutingProtocol.BGP)
        .build();
  }

  private Configuration parseConfig(String hostname) throws IOException {
    Configuration c = parseTextConfigs(hostname).get(hostname);
    assertThat(c, notNullValue());
    return c;
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  private @Nonnull CumulusNcluConfiguration parseVendorConfig(String hostname) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + hostname);
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);
    CumulusNcluCombinedParser parser = new CumulusNcluCombinedParser(src, settings);
    CumulusNcluControlPlaneExtractor extractor =
        new CumulusNcluControlPlaneExtractor(src, parser, new Warnings());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(tree);
    assertThat(
        String.format("Ensure '%s' was successfully parsed", hostname),
        extractor.getVendorConfiguration(),
        notNullValue());
    VendorConfiguration vc = extractor.getVendorConfiguration();
    assertThat(vc, instanceOf(CumulusNcluConfiguration.class));
    return (CumulusNcluConfiguration) vc;
  }

  @Test
  public void testBgpConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_bgp");
    String peerInterface = "swp1";
    String peerExportPolicyName = computeBgpPeerExportPolicyName(DEFAULT_VRF_NAME, peerInterface);
    String commonExportPolicyName =
        CumulusNcluConfiguration.computeBgpCommonExportPolicyName(DEFAULT_VRF_NAME);

    assertThat(c, hasDefaultVrf(hasBgpProcess(hasRouterId(Ip.parse("192.0.2.2")))));
    assertThat(c, hasDefaultVrf(hasBgpProcess(hasInterfaceNeighbors(hasKey(peerInterface)))));

    BgpUnnumberedPeerConfig pc =
        c.getDefaultVrf().getBgpProcess().getInterfaceNeighbors().get(peerInterface);

    assertThat(pc, hasPeerInterface(peerInterface));
    assertThat(pc, hasLocalAs(65500L));
    assertThat(pc, hasRemoteAs(equalTo(ALL_AS_NUMBERS.difference(LongSpace.of(65500L)))));
    assertThat(pc, hasExportPolicy(peerExportPolicyName));

    // ARP response for link-local address for BGP unnumbered interface
    assertThat(c, hasInterface(peerInterface, hasAdditionalArpIps(containsIp(BGP_UNNUMBERED_IP))));

    //// generated routing policies

    // common export policy
    assertThat(c.getRoutingPolicies(), hasKey(commonExportPolicyName));
    // TODO: tests that differentiate common and peer export policy when possibility is implemented

    // peer export policy
    assertThat(c.getRoutingPolicies(), hasKey(peerExportPolicyName));
    RoutingPolicy peerExportPolicy = c.getRoutingPolicies().get(peerExportPolicyName);

    {
      // Redistribute connected route matching lo's interface address
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          peerExportPolicy
              .call(
                  Environment.builder(c, DEFAULT_VRF_NAME)
                      .setOriginalRoute(new ConnectedRoute(Prefix.parse("10.0.0.1/32"), "foo"))
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Reject connected route not matching lo's interface address
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertFalse(
          peerExportPolicy
              .call(
                  Environment.builder(c, DEFAULT_VRF_NAME)
                      .setOriginalRoute(new ConnectedRoute(Prefix.parse("10.0.0.2/32"), "foo"))
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Advertise route for explicitly advertised network 192.0.2.1/32
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          peerExportPolicy
              .call(
                  Environment.builder(c, DEFAULT_VRF_NAME)
                      .setOriginalRoute(new ConnectedRoute(Prefix.parse("192.0.2.1/32"), "foo"))
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }

    {
      // Forward BGP route
      BgpRoute.Builder outputBuilder = makeBgpOutputRouteBuilder();
      assertTrue(
          peerExportPolicy
              .call(
                  Environment.builder(c, DEFAULT_VRF_NAME)
                      .setOriginalRoute(makeBgpRoute(Prefix.parse("10.0.0.5/32")))
                      .setOutputRoute(outputBuilder)
                      .build())
              .getBooleanValue());
    }
  }

  @Test
  public void testBgpExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_bgp");

    // bgp process should be created
    assertThat(vc.getBgpProcess(), notNullValue());

    BgpProcess proc = vc.getBgpProcess();

    // autonomous system
    assertThat(
        "Ensure autonomous-sytem is set",
        proc.getDefaultVrf().getAutonomousSystem(),
        equalTo(65500L));

    // ipv4 unicast
    assertThat(
        "Ensure ipv4 unicast is enabled", proc.getDefaultVrf().getIpv4Unicast(), notNullValue());

    assertThat(
        "Ensure ipv4 unicast network is extracted",
        proc.getDefaultVrf().getIpv4Unicast().getNetworks().keySet(),
        contains(Prefix.strict("192.0.2.1/32")));

    assertThat(
        "Ensure ipv4 unicast redistribution of connected routes",
        proc.getDefaultVrf().getIpv4Unicast().getRedistributionPolicies().keySet(),
        contains(CumulusRoutingProtocol.CONNECTED));
    BgpRedistributionPolicy rc =
        proc.getDefaultVrf()
            .getIpv4Unicast()
            .getRedistributionPolicies()
            .get(CumulusRoutingProtocol.CONNECTED);
    assertThat(
        "Ensure redistribution policy has correct protocol",
        rc.getProtocol(),
        equalTo(CumulusRoutingProtocol.CONNECTED));
    assertThat(
        "Ensure redistribution policy uses correct route-map", rc.getRouteMap(), equalTo("rm1"));

    // l2vpn evpn
    assertThat("Ensure l2vpn evpn is enabled", proc.getDefaultVrf().getL2VpnEvpn(), notNullValue());
    BgpL2vpnEvpnAddressFamily l2vpnEvpn = proc.getDefaultVrf().getL2VpnEvpn();

    assertTrue("Ensure l2vpn evpn advertise-all-vni is extracted", l2vpnEvpn.getAdvertiseAllVni());
    assertTrue(
        "Ensure l2vpn evpn advertise-default-gw is extracted", l2vpnEvpn.getAdvertiseDefaultGw());
    assertThat(
        "Ensure l2vpn evpn advertise ipv4 unicast is extracted",
        l2vpnEvpn.getAdvertiseIpv4Unicast(),
        notNullValue());

    // interface neighbor
    assertThat(
        "Ensure interface neighbor is extracted",
        proc.getDefaultVrf().getInterfaceNeighbors().keySet(),
        contains("swp1"));
    BgpInterfaceNeighbor in = proc.getDefaultVrf().getInterfaceNeighbors().get("swp1");
    assertThat("Ensure interface neighbor has correct name", in.getName(), equalTo("swp1"));
    assertThat(
        "Ensure interface uses correct remote-as type",
        in.getRemoteAsType(),
        equalTo(RemoteAsType.EXTERNAL));

    // router-id
    assertThat(
        "Ensure router-id is extracted",
        proc.getDefaultVrf().getRouterId(),
        equalTo(Ip.parse("192.0.2.2")));

    //// VRF settings
    assertThat(proc.getVrfs().keySet(), contains("vrf1"));
    BgpVrf vrf = proc.getVrfs().get("vrf1");
    assertThat("Ensure vrf uses correct name", vrf.getVrfName(), equalTo("vrf1"));

    // autonomous-system
    assertThat("Ensure autonomous-sytem is set", vrf.getAutonomousSystem(), equalTo(65501L));

    // redistribution
    assertThat(
        "Ensure ipv4 unicast redistribution of connected and static routes for vrf",
        vrf.getIpv4Unicast().getRedistributionPolicies().keySet(),
        containsInAnyOrder(CumulusRoutingProtocol.CONNECTED, CumulusRoutingProtocol.STATIC));
    assertThat(
        "Ensure connected redistribution policy has correct protocol for vrf",
        vrf.getIpv4Unicast()
            .getRedistributionPolicies()
            .get(CumulusRoutingProtocol.CONNECTED)
            .getProtocol(),
        equalTo(CumulusRoutingProtocol.CONNECTED));
    assertThat(
        "Ensure static redistribution policy has correct protocol for vrf",
        vrf.getIpv4Unicast()
            .getRedistributionPolicies()
            .get(CumulusRoutingProtocol.STATIC)
            .getProtocol(),
        equalTo(CumulusRoutingProtocol.STATIC));

    // l2vpn evpn
    assertThat("Ensure l2vpn evpn is enabled for vrf", vrf.getL2VpnEvpn(), notNullValue());
    assertThat(
        "Ensure l2vpn evpn advertise ipv4 unicast is extracted for vrf",
        vrf.getL2VpnEvpn().getAdvertiseIpv4Unicast(),
        notNullValue());
  }

  @Test
  public void testBondConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_bond");

    // bond1
    assertThat(
        c,
        hasInterface(
            "bond1",
            (hasDependencies(
                containsInAnyOrder(
                    new Dependency("swp1", DependencyType.AGGREGATE),
                    new Dependency("swp2", DependencyType.AGGREGATE),
                    new Dependency("swp3", DependencyType.AGGREGATE),
                    new Dependency("swp4", DependencyType.AGGREGATE),
                    new Dependency("swp5", DependencyType.AGGREGATE),
                    new Dependency("swp6", DependencyType.AGGREGATE),
                    new Dependency("swp7", DependencyType.AGGREGATE),
                    new Dependency("swp8", DependencyType.AGGREGATE))))));
    assertThat(c, hasInterface("bond1", hasMlagId(1)));
    assertThat(c, hasInterface("bond1", isSwitchport()));
    assertThat(c, hasInterface("bond1", hasSwitchPortMode(SwitchportMode.ACCESS)));
    assertThat(c, hasInterface("bond1", hasAccessVlan(2)));
    assertThat(c, hasInterface("bond1", isActive()));
    assertThat(c, hasInterface("bond1", hasVrfName(DEFAULT_VRF_NAME)));

    // bond2
    assertThat(c, hasInterface("bond2", isSwitchport()));
    assertThat(c, hasInterface("bond2", hasSwitchPortMode(SwitchportMode.TRUNK)));
    assertThat(
        c,
        hasInterface(
            "bond2",
            hasAllowedVlans(
                IntegerSpace.builder().including(1).including(new SubRange(3, 5)).build())));
    assertThat(c, hasInterface("bond2", isActive(false)));
    assertThat(c, hasInterface("bond2", hasVrfName(DEFAULT_VRF_NAME)));

    // bond3
    assertThat(c, hasInterface("bond3", isSwitchport(false)));
    assertThat(c, hasInterface("bond3", isActive(false)));
    assertThat(c, hasInterface("bond3", hasAddress(new InterfaceAddress("192.0.2.1/24"))));
    assertThat(c, hasInterface("bond3", hasVrfName("vrf1")));
  }

  @Test
  public void testBondExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_bond");
    String bond1Name = "bond1";
    String bond2Name = "bond2";
    String bond3Name = "bond3";

    String[] expectedSlaves =
        new String[] {
          "swp1", "swp2", "swp3", "swp4", "swp5", "swp6", "swp7", "swp8",
        };

    // referenced interfaces should have been created
    assertThat(vc.getInterfaces().keySet(), containsInAnyOrder(expectedSlaves));

    assertThat(
        "Ensure bonds were extracted",
        vc.getBonds().keySet(),
        containsInAnyOrder(bond1Name, bond2Name, bond3Name));

    Bond bond1 = vc.getBonds().get(bond1Name);
    Bond bond2 = vc.getBonds().get(bond2Name);
    Bond bond3 = vc.getBonds().get(bond3Name);

    assertThat("Ensure access VLAN ID was set", bond1.getBridge().getAccess(), equalTo(2));
    assertThat("Ensure CLAG ID was set", bond1.getClagId(), equalTo(1));
    assertThat("Ensure slaves were set", bond1.getSlaves(), containsInAnyOrder(expectedSlaves));
    assertThat(
        "Ensure trunk VLAN IDs were set",
        bond2.getBridge().getVids(),
        equalTo(IntegerSpace.of(Range.closed(3, 5))));
    assertThat(
        "Ensure IP address was extracted",
        bond3.getIpAddresses(),
        contains(new InterfaceAddress("192.0.2.1/24")));
    assertThat("Ensure VRF was extracted", bond3.getVrf(), equalTo("vrf1"));
  }

  @Test
  public void testBondReferences() throws IOException {
    String hostname = "cumulus_nclu_bond_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.BOND, "bond1", 3));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "bond2.4094", 2));
  }

  @Test
  public void testBridgeConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_bridge");

    IntegerSpace bridgeVlans =
        IntegerSpace.builder().including(Range.closed(1, 7)).including(1000).build();

    //// bond
    org.batfish.datamodel.Interface bond1 = c.getAllInterfaces().get("bond1");
    org.batfish.datamodel.Interface bond2 = c.getAllInterfaces().get("bond2");
    org.batfish.datamodel.Interface bond3 = c.getAllInterfaces().get("bond3");
    org.batfish.datamodel.Interface bond4 = c.getAllInterfaces().get("bond4");

    // bond1 is in access mode using VLAN 1000
    assertThat(bond1, isSwitchport());
    assertThat(bond1, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(bond1, hasAccessVlan(1000));

    // bond2 is in trunk mode with native vlan 1, allowed vlans 1-7,1000 (inherited)
    assertThat(bond2, isSwitchport());
    assertThat(bond2, hasSwitchPortMode(SwitchportMode.TRUNK));
    assertThat(bond2, hasNativeVlan(1));
    assertThat(bond2, hasAllowedVlans(bridgeVlans));

    // bond3 is in trunk mode with native vlan 2 (inherited), allowed vlans 2,3 (pvid 2, vids 3)
    assertThat(bond3, isSwitchport());
    assertThat(bond3, hasSwitchPortMode(SwitchportMode.TRUNK));
    assertThat(bond3, hasNativeVlan(2));
    assertThat(bond3, hasAllowedVlans(IntegerSpace.of(Range.closed(2, 3))));

    // bond4 is not a switchport
    assertThat(bond4, isSwitchport(false));
    assertThat(bond4, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(bond4, hasNativeVlan(nullValue()));
    assertThat(bond4, hasAllowedVlans(equalTo(IntegerSpace.EMPTY)));

    // swp
    org.batfish.datamodel.Interface swp5 = c.getAllInterfaces().get("swp5");
    org.batfish.datamodel.Interface swp6 = c.getAllInterfaces().get("swp6");
    org.batfish.datamodel.Interface swp7 = c.getAllInterfaces().get("swp7");
    org.batfish.datamodel.Interface swp8 = c.getAllInterfaces().get("swp8");

    // swp5 is in access mode using VLAN 1000
    assertThat(swp5, isSwitchport());
    assertThat(swp5, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(swp5, hasAccessVlan(1000));

    // swp6 is in trunk mode with native vlan 1, allowed vlans 1-7,1000 (inherited)
    assertThat(swp6, isSwitchport());
    assertThat(swp6, hasSwitchPortMode(SwitchportMode.TRUNK));
    assertThat(swp6, hasNativeVlan(1));
    assertThat(swp6, hasAllowedVlans(bridgeVlans));

    // swp7 is in trunk mode with native vlan 2 (inherited), allowed vlans 2,7 (pvid 2, vids 7)
    assertThat(swp7, isSwitchport());
    assertThat(swp7, hasSwitchPortMode(SwitchportMode.TRUNK));
    assertThat(swp7, hasNativeVlan(2));
    assertThat(swp7, hasAllowedVlans(IntegerSpace.builder().including(2).including(7).build()));

    // swp8 is not a switchport
    assertThat(swp8, isSwitchport(false));
    assertThat(swp8, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(swp8, hasNativeVlan(nullValue()));
    assertThat(swp8, hasAllowedVlans(equalTo(IntegerSpace.EMPTY)));
  }

  @Test
  public void testBridgeExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_bridge");

    // bridge
    assertThat(
        vc.getBridge().getPorts(),
        containsInAnyOrder("bond1", "bond2", "bond3", "swp5", "swp6", "swp7", "vni10001"));
    assertThat(vc.getBridge().getPvid(), equalTo(2));
    assertThat(
        vc.getBridge().getVids(),
        equalTo(IntegerSpace.builder().including(Range.closed(1, 7)).including(1000).build()));

    // bond
    Bond bond1 = vc.getBonds().get("bond1");
    Bond bond2 = vc.getBonds().get("bond2");
    Bond bond3 = vc.getBonds().get("bond3");
    Bond bond4 = vc.getBonds().get("bond4");

    assertThat(
        "bond1 is in access mode using VLAN 1000", bond1.getBridge().getAccess(), equalTo(1000));
    assertTrue("bond1 has no vids", bond1.getBridge().getVids().isEmpty());
    assertThat("bond1 has no pvid", bond1.getBridge().getPvid(), nullValue());
    assertThat("bond2 has no access VLAN", bond2.getBridge().getAccess(), nullValue());
    assertTrue("bond2 has no vids (inherits from bridge)", bond2.getBridge().getVids().isEmpty());
    assertThat("bond2 has manual pvid 1", bond2.getBridge().getPvid(), equalTo(1));
    assertThat("bond3 has no access VLAN", bond3.getBridge().getAccess(), nullValue());
    assertThat(
        "bond3 has has manual vids 3", bond3.getBridge().getVids(), equalTo(IntegerSpace.of(3)));
    assertThat(
        "bond3 has no pvid (inherits from bridge)", bond3.getBridge().getPvid(), nullValue());
    assertThat("bond4 has no access VLAN", bond4.getBridge().getAccess(), nullValue());
    assertTrue("bond4 has no vids", bond4.getBridge().getVids().isEmpty());
    assertThat("bond4 has no pvid", bond4.getBridge().getPvid(), nullValue());

    // swp
    Interface swp5 = vc.getInterfaces().get("swp5");
    Interface swp6 = vc.getInterfaces().get("swp6");
    Interface swp7 = vc.getInterfaces().get("swp7");
    Interface swp8 = vc.getInterfaces().get("swp8");

    assertThat(
        "swp5 is in access mode using VLAN 1000", swp5.getBridge().getAccess(), equalTo(1000));
    assertTrue("swp5 has no vids", swp5.getBridge().getVids().isEmpty());
    assertThat("swp5 has no pvid", swp5.getBridge().getPvid(), nullValue());
    assertThat("swp6 has no access VLAN", swp6.getBridge().getAccess(), nullValue());
    assertTrue("swp6 has no vids (inherits from bridge)", swp6.getBridge().getVids().isEmpty());
    assertThat("swp6 has manual pvid 1", swp6.getBridge().getPvid(), equalTo(1));
    assertThat("swp7 has no access VLAN", swp7.getBridge().getAccess(), nullValue());
    assertThat(
        "swp7 has has manual vids 7", swp7.getBridge().getVids(), equalTo(IntegerSpace.of(7)));
    assertThat("swp7 has no pvid (inherits from bridge)", swp7.getBridge().getPvid(), nullValue());
    assertThat("swp8 has no access VLAN", swp8.getBridge().getAccess(), nullValue());
    assertTrue("swp8 has no vids", swp8.getBridge().getVids().isEmpty());
    assertThat("swp8 has no pvid", swp8.getBridge().getPvid(), nullValue());
  }

  @Test
  public void testClagConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_clag");

    assertThat(
        c.getMlags(),
        equalTo(
            ImmutableMap.of(
                CUMULUS_CLAG_DOMAIN_ID,
                Mlag.builder()
                    .setId(CUMULUS_CLAG_DOMAIN_ID)
                    .setLocalInterface("peerlink.4094")
                    .setPeerAddress(Ip.parse("192.0.2.2"))
                    .setPeerInterface("peerlink")
                    .build())));
  }

  @Test
  public void testDnsConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_dns");

    assertThat(c.getDnsServers(), containsInAnyOrder("192.0.2.3", "192.0.2.4"));
  }

  @Test
  public void testDnsExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_dns");

    assertThat(vc.getIpv4Nameservers(), contains(Ip.parse("192.0.2.3"), Ip.parse("192.0.2.4")));
    assertThat(vc.getIpv6Nameservers(), contains(Ip6.parse("1::1"), Ip6.parse("1::2")));
  }

  @Test
  public void testHostname() throws IOException {
    String filename = "cumulus_nclu_hostname";
    String hostname = "custom_hostname";
    Batfish batfish = getBatfishForConfigurationNames(filename);
    assertThat(batfish.loadConfigurations(), hasEntry(equalTo(hostname), hasHostname(hostname)));
  }

  @Test
  public void testInterfaceConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_interface");

    assertThat(
        "Ensure interfaces are created",
        c.getAllInterfaces().keySet(),
        containsInAnyOrder(
            "bond1",
            "bond2",
            "bond2.4094",
            "bond3",
            "bond3.4094",
            "eth0",
            "lo",
            "mgmt",
            "swp1",
            "swp2",
            "swp3",
            "swp4",
            "swp5",
            "swp5.1",
            "vrf1"));

    assertThat(
        c,
        hasDefaultVrf(
            hasInterfaces(
                containsInAnyOrder(
                    "bond1",
                    "bond2",
                    "bond2.4094",
                    "bond3",
                    "bond3.4094",
                    "eth0",
                    "lo",
                    "swp1",
                    "swp2",
                    "swp3",
                    "swp4",
                    "swp5"))));
    assertThat(c, hasVrf("mgmt", hasInterfaces(containsInAnyOrder("mgmt"))));
    assertThat(c, hasVrf("vrf1", hasInterfaces(containsInAnyOrder("vrf1", "swp5.1"))));

    // encapsulation vlan
    assertThat(c, hasInterface("bond2.4094", hasEncapsulationVlan(4094)));
    assertThat(c, hasInterface("bond3.4094", hasEncapsulationVlan(4094)));
    assertThat(c, hasInterface("swp5.1", hasEncapsulationVlan(1)));

    // ip address
    assertThat(
        c,
        hasInterface(
            "bond2.4094",
            both(hasAllAddresses(
                    containsInAnyOrder(
                        new InterfaceAddress("10.0.1.1/24"),
                        new InterfaceAddress("172.16.0.1/24"))))
                .and(hasAddress(new InterfaceAddress("10.0.1.1/24")))));
    assertThat(
        c,
        hasInterface(
            "eth0",
            both(hasAllAddresses(containsInAnyOrder(new InterfaceAddress("10.0.2.1/24"))))
                .and(hasAddress(new InterfaceAddress("10.0.2.1/24")))));
    assertThat(
        c,
        hasInterface(
            "swp4",
            both(hasAllAddresses(containsInAnyOrder(new InterfaceAddress("10.0.3.1/24"))))
                .and(hasAddress(new InterfaceAddress("10.0.3.1/24")))));

    // bandwidth
    assertThat(c, hasInterface("bond1", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("bond2", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("bond3", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("eth0", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("swp1", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("swp2", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("swp3", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("swp4", hasBandwidth(10E9D)));
    assertThat(c, hasInterface("swp5", hasBandwidth(10E9D)));

    // channel group
    assertThat(c.getAllInterfaces().get("swp1").getChannelGroup(), equalTo("bond1"));
    assertThat(c.getAllInterfaces().get("swp2").getChannelGroup(), equalTo("bond2"));
    assertThat(c.getAllInterfaces().get("swp3").getChannelGroup(), equalTo("bond3"));

    // channel group members
    assertThat(c.getAllInterfaces().get("bond1").getChannelGroupMembers(), contains("swp1"));
    assertThat(c.getAllInterfaces().get("bond2").getChannelGroupMembers(), contains("swp2"));
    assertThat(c.getAllInterfaces().get("bond3").getChannelGroupMembers(), contains("swp3"));
  }

  @Test
  public void testInterfaceExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_interface");

    assertThat(
        "Ensure interfaces are created",
        vc.getInterfaces().keySet(),
        containsInAnyOrder(
            "bond2.4094", "bond3.4094", "eth0", "swp1", "swp2", "swp3", "swp4", "swp5.1"));

    // encapsulation vlan
    assertThat(
        "Ensure encapsulation VLAN is extracted",
        vc.getInterfaces().get("bond2.4094").getEncapsulationVlan(),
        equalTo(4094));
    assertThat(
        "Ensure encapsulation VLAN is extracted",
        vc.getInterfaces().get("bond3.4094").getEncapsulationVlan(),
        equalTo(4094));
    assertThat(
        "Ensure encapsulation VLAN is extracted",
        vc.getInterfaces().get("swp5.1").getEncapsulationVlan(),
        equalTo(1));

    // ip address
    assertThat(
        "Ensure ip addresses are extracted",
        vc.getInterfaces().get("bond2.4094").getIpAddresses(),
        contains(new InterfaceAddress("10.0.1.1/24"), new InterfaceAddress("172.16.0.1/24")));

    // clag backup-ip
    assertThat(
        "Ensure clag backup-ip extracted",
        vc.getInterfaces().get("bond2.4094").getClag().getBackupIp(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        "Ensure clag backup-ip is extracted",
        vc.getInterfaces().get("bond3.4094").getClag().getBackupIp(),
        equalTo(Ip.parse("192.168.0.1")));

    // clag backup-ip vrf
    assertThat(
        "Ensure clag backup-ip vrf is extracted",
        vc.getInterfaces().get("bond2.4094").getClag().getBackupIpVrf(),
        equalTo("mgmt"));
    assertThat(
        "Ensure clag backup-ip vrf is extracted",
        vc.getInterfaces().get("bond3.4094").getClag().getBackupIpVrf(),
        nullValue());

    // clag peer-ip
    assertThat(
        "Ensure clag peer-ip is extracted",
        vc.getInterfaces().get("bond2.4094").getClag().getPeerIp(),
        equalTo(Ip.parse("10.0.0.2")));

    // clag priority
    assertThat(
        "Ensure clag priority is extracted",
        vc.getInterfaces().get("bond2.4094").getClag().getPriority(),
        equalTo(1000));

    // clag sys-mac
    assertThat(
        "Ensure clag sys-mac is extracted",
        vc.getInterfaces().get("bond2.4094").getClag().getSysMac(),
        equalTo(MacAddress.parse("00:11:22:33:44:55")));

    // vrf
    assertThat(
        "Ensure vrf is extracted", vc.getInterfaces().get("swp5.1").getVrf(), equalTo("vrf1"));

    // interface type (computed)
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("bond2.4094").getType(),
        equalTo(CumulusInterfaceType.BOND_SUBINTERFACE));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("bond3.4094").getType(),
        equalTo(CumulusInterfaceType.BOND_SUBINTERFACE));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("eth0").getType(),
        equalTo(CumulusInterfaceType.PHYSICAL));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("swp1").getType(),
        equalTo(CumulusInterfaceType.PHYSICAL));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("swp2").getType(),
        equalTo(CumulusInterfaceType.PHYSICAL));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("swp3").getType(),
        equalTo(CumulusInterfaceType.PHYSICAL));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("swp4").getType(),
        equalTo(CumulusInterfaceType.PHYSICAL));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("swp5.1").getType(),
        equalTo(CumulusInterfaceType.PHYSICAL_SUBINTERFACE));
  }

  @Test
  public void testInterfaceReferences() throws IOException {
    String hostname = "cumulus_nclu_interface_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "swp1", 4));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "swp2", 1));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "swp3", 1));
  }

  @Test
  public void testLoopbackConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_loopback");

    assertThat(c, hasInterface("lo", hasAddress(new InterfaceAddress("10.0.0.1/32"))));
    assertThat(
        c,
        hasInterface(
            "lo",
            hasAllAddresses(
                containsInAnyOrder(
                    new InterfaceAddress("10.0.0.1/32"), new InterfaceAddress("10.0.1.1/24")))));
    assertThat(c, hasInterface("lo", hasVrfName(DEFAULT_VRF_NAME)));
    assertThat(c, hasDefaultVrf(hasInterfaces(contains("lo"))));
  }

  @Test
  public void testLoopbackExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_loopback");

    assertTrue("Ensure loopback is configured", vc.getLoopback().getConfigured());
    assertThat(
        "Ensure clag vxlan-anycast-ip is extracted",
        vc.getLoopback().getClagVxlanAnycastIp(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        "Ensure ip addresses are extracted",
        vc.getLoopback().getAddresses(),
        contains(new InterfaceAddress("10.0.0.1/32"), new InterfaceAddress("10.0.1.1/24")));
  }

  @Test
  public void testLoopbackMissingExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_loopback_missing");

    assertFalse("Ensure loopback is disabled", vc.getLoopback().getConfigured());
  }

  @Test
  public void testLoopbackReferences() throws IOException {
    String hostname = "cumulus_nclu_loopback_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(
        ans,
        hasNumReferrers(
            filename,
            CumulusStructureType.LOOPBACK,
            CumulusNcluConfiguration.LOOPBACK_INTERFACE_NAME,
            2));
  }

  @Test
  public void testRouteMapReferences() throws IOException {
    String hostname = "cumulus_nclu_route_map_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.ROUTE_MAP, "rm1", 2));
  }

  @Test
  public void testRoutingConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_routing");

    org.batfish.datamodel.StaticRoute.Builder builder =
        org.batfish.datamodel.StaticRoute.builder().setAdmin(1).setMetric(0);

    // static routes in default VRF
    builder.setNetwork(Prefix.strict("10.0.1.0/24"));
    assertThat(
        c,
        hasDefaultVrf(
            hasStaticRoutes(
                containsInAnyOrder(
                    builder.setNextHopIp(Ip.parse("10.1.0.1")).build(),
                    builder.setNextHopIp(Ip.parse("10.1.0.2")).build()))));

    // static routes in vrf1
    builder.setNetwork(Prefix.strict("10.0.2.0/24"));
    assertThat(
        c,
        hasVrf(
            "vrf1",
            hasStaticRoutes(
                containsInAnyOrder(
                    builder.setNextHopIp(Ip.parse("192.0.2.1")).build(),
                    builder.setNextHopIp(Ip.parse("192.0.2.2")).build()))));

    // route-maps
    assertThat(
        "Ensure route-maps are converted",
        c.getRoutingPolicies().keySet(),
        containsInAnyOrder("rm1", "rm2"));

    Prefix bond1Prefix = Prefix.strict("10.1.0.0/24");
    Prefix eth04094Prefix = Prefix.strict("10.1.1.0/24");
    Prefix swp1Prefix = Prefix.strict("10.1.2.0/24");
    Prefix swp2Prefix = Prefix.strict("10.1.3.0/24");
    Prefix loPrefix = Prefix.strict("10.1.4.0/24");
    Prefix vrf1Prefix1 = Prefix.strict("10.1.5.0/24");
    Prefix vrf1Prefix2 = Prefix.strict("10.1.6.0/24");
    Prefix otherPrefix = Prefix.strict("10.10.10.10/32");

    RoutingPolicy rm1 = c.getRoutingPolicies().get("rm1");
    RoutingPolicy rm2 = c.getRoutingPolicies().get("rm2");

    assertRoutingPolicyPermitsNetwork(rm1, bond1Prefix);
    assertRoutingPolicyPermitsNetwork(rm1, eth04094Prefix);
    assertRoutingPolicyPermitsNetwork(rm1, swp1Prefix);
    assertRoutingPolicyPermitsNetwork(rm1, swp2Prefix);
    assertRoutingPolicyPermitsNetwork(rm1, loPrefix);
    assertRoutingPolicyDeniesNetwork(rm1, vrf1Prefix1);
    assertRoutingPolicyDeniesNetwork(rm1, vrf1Prefix2);
    assertRoutingPolicyDeniesNetwork(rm1, otherPrefix);

    assertRoutingPolicyPermitsNetwork(rm2, bond1Prefix);
    assertRoutingPolicyPermitsNetwork(rm2, eth04094Prefix);
    assertRoutingPolicyPermitsNetwork(rm2, swp1Prefix);
    assertRoutingPolicyPermitsNetwork(rm2, swp2Prefix);
    assertRoutingPolicyPermitsNetwork(rm2, loPrefix);
    assertRoutingPolicyDeniesNetwork(rm2, vrf1Prefix1);
    assertRoutingPolicyDeniesNetwork(rm2, vrf1Prefix2);
    assertRoutingPolicyPermitsNetwork(rm2, otherPrefix);
  }

  @Test
  public void testRoutingExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_routing");

    // static route (main vrf)
    assertThat(
        vc.getStaticRoutes(),
        containsInAnyOrder(
            new StaticRoute(Prefix.strict("10.0.1.0/24"), Ip.parse("10.1.0.1")),
            new StaticRoute(Prefix.strict("10.0.1.0/24"), Ip.parse("10.1.0.2"))));

    // static route (alternate vrf)
    assertThat(
        vc.getVrfs().get("vrf1").getStaticRoutes(),
        containsInAnyOrder(
            new StaticRoute(Prefix.strict("10.0.2.0/24"), Ip.parse("192.0.2.1")),
            new StaticRoute(Prefix.strict("10.0.2.0/24"), Ip.parse("192.0.2.2"))));

    // route-map keys
    assertThat(vc.getRouteMaps().keySet(), containsInAnyOrder("rm1", "rm2"));
    RouteMap rm1 = vc.getRouteMaps().get("rm1");
    RouteMap rm2 = vc.getRouteMaps().get("rm2");

    // route-map entries
    assertThat(rm1.getEntries().keySet(), contains(1, 2, 3, 4));
    assertThat(rm2.getEntries().keySet(), contains(1, 2));
    // route-map entry num
    assertThat(rm1.getEntries().get(1).getNumber(), equalTo(1));
    assertThat(rm1.getEntries().get(2).getNumber(), equalTo(2));
    assertThat(rm1.getEntries().get(3).getNumber(), equalTo(3));
    assertThat(rm1.getEntries().get(4).getNumber(), equalTo(4));
    assertThat(rm2.getEntries().get(1).getNumber(), equalTo(1));
    assertThat(rm2.getEntries().get(2).getNumber(), equalTo(2));
    // route-map entry action
    assertThat(rm1.getEntries().get(1).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm1.getEntries().get(2).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm1.getEntries().get(3).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm1.getEntries().get(4).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm2.getEntries().get(1).getAction(), equalTo(LineAction.DENY));
    assertThat(rm2.getEntries().get(2).getAction(), equalTo(LineAction.PERMIT));

    // route-map match
    assertThat(
        rm1.getEntries().get(1).getMatches().collect(ImmutableList.toImmutableList()),
        contains(new RouteMapMatchInterface(ImmutableSet.of("bond1"))));

    // route-map match interface
    assertThat(
        rm1.getEntries().get(1).getMatchInterface(),
        equalTo(new RouteMapMatchInterface(ImmutableSet.of("bond1"))));
    assertThat(
        rm1.getEntries().get(1).getMatchInterface(),
        equalTo(new RouteMapMatchInterface(ImmutableSet.of("bond1"))));
    assertThat(
        rm1.getEntries().get(2).getMatchInterface(),
        equalTo(
            new RouteMapMatchInterface(
                ImmutableSet.of(CumulusNcluConfiguration.LOOPBACK_INTERFACE_NAME))));
    assertThat(
        rm1.getEntries().get(3).getMatchInterface(),
        equalTo(new RouteMapMatchInterface(ImmutableSet.of("eth0.4094"))));
    assertThat(
        rm1.getEntries().get(4).getMatchInterface(),
        equalTo(new RouteMapMatchInterface(ImmutableSet.of("swp1", "swp2"))));
    assertThat(
        rm2.getEntries().get(1).getMatchInterface(),
        equalTo(new RouteMapMatchInterface(ImmutableSet.of("vrf1"))));
  }

  @Test
  public void testVendorFamilyClag() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_clag");

    assertThat(c.getVendorFamily().getCumulus(), notNullValue());

    Map<String, InterfaceClagSettings> clagByInterface =
        c.getVendorFamily().getCumulus().getInterfaceClagSettings();

    assertThat(
        clagByInterface,
        equalTo(
            ImmutableMap.of(
                "peerlink.4094",
                InterfaceClagSettings.builder()
                    .setBackupIp(Ip.parse("10.0.0.2"))
                    .setBackupIpVrf("mgmt")
                    .setPeerIp(Ip.parse("192.0.2.2"))
                    .setPriority(1000)
                    .setSysMac(MacAddress.parse("00:11:22:33:44:55"))
                    .build())));
  }

  @Test
  public void testVlanConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_vlan");

    // vlan interfaces should be put in correct vrfs
    assertThat(
        c, hasDefaultVrf(hasInterfaces(containsInAnyOrder("vlan3", "vlan4", "vlan5", "lo"))));
    assertThat(c, hasVrf("vrf1", hasInterfaces(containsInAnyOrder("vrf1", "vlan2"))));
    assertThat(c, hasInterface("vlan2", hasVrfName("vrf1")));
    assertThat(c, hasInterface("vlan3", hasVrfName(DEFAULT_VRF_NAME)));
    assertThat(c, hasInterface("vlan4", hasVrfName(DEFAULT_VRF_NAME)));
    assertThat(c, hasInterface("vlan5", hasVrfName(DEFAULT_VRF_NAME)));

    // vlan-id
    assertThat(c, hasInterface("vlan2", hasVlan(2)));
    assertThat(c, hasInterface("vlan3", hasVlan(nullValue())));
    assertThat(c, hasInterface("vlan4", hasVlan(nullValue())));
    assertThat(c, hasInterface("vlan5", hasVlan(6)));

    // ip address
    assertThat(c, hasInterface("vlan2", hasAddress(new InterfaceAddress("10.0.0.1/24"))));
    assertThat(
        c,
        hasInterface(
            "vlan2",
            hasAllAddresses(
                containsInAnyOrder(
                    new InterfaceAddress("10.0.0.1/24"),
                    new InterfaceAddress("10.0.1.1/24"),
                    new InterfaceAddress("10.0.2.1/24"),
                    new InterfaceAddress("10.0.3.1/24"),
                    new InterfaceAddress("10.0.4.1/24")))));
  }

  @Test
  public void testVlanExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_vlan");

    // vlan interfaces
    assertThat(vc.getVlans().keySet(), containsInAnyOrder("vlan2", "vlan3", "vlan4", "vlan5"));

    // name
    assertThat(vc.getVlans().get("vlan2").getName(), equalTo("vlan2"));
    assertThat(vc.getVlans().get("vlan3").getName(), equalTo("vlan3"));
    assertThat(vc.getVlans().get("vlan4").getName(), equalTo("vlan4"));
    assertThat(vc.getVlans().get("vlan5").getName(), equalTo("vlan5"));

    // vlan-id
    assertThat(vc.getVlans().get("vlan2").getVlanId(), equalTo(2));
    assertThat(vc.getVlans().get("vlan3").getVlanId(), nullValue());
    assertThat(vc.getVlans().get("vlan4").getVlanId(), nullValue());
    assertThat(vc.getVlans().get("vlan5").getVlanId(), equalTo(6)); // intentional 6

    // ip address
    assertThat(
        vc.getVlans().get("vlan2").getAddresses(),
        contains(new InterfaceAddress("10.0.0.1/24"), new InterfaceAddress("10.0.1.1/24")));
    assertThat(vc.getVlans().get("vlan3").getAddresses(), empty());
    assertThat(vc.getVlans().get("vlan4").getAddresses(), empty());
    assertThat(vc.getVlans().get("vlan5").getAddresses(), empty());

    // ip address-virtual
    assertThat(
        vc.getVlans().get("vlan2").getAddressVirtuals(),
        equalTo(
            ImmutableMap.of(
                MacAddress.parse("00:00:00:00:00:01"),
                ImmutableSet.of(
                    new InterfaceAddress("10.0.2.1/24"), new InterfaceAddress("10.0.3.1/24")),
                MacAddress.parse("00:00:00:00:00:02"),
                ImmutableSet.of(new InterfaceAddress("10.0.4.1/24")))));
    assertThat(vc.getVlans().get("vlan3").getAddressVirtuals(), anEmptyMap());
    assertThat(vc.getVlans().get("vlan4").getAddressVirtuals(), anEmptyMap());
    assertThat(vc.getVlans().get("vlan5").getAddressVirtuals(), anEmptyMap());

    // vrf
    assertThat(vc.getVlans().get("vlan2").getVrf(), equalTo("vrf1"));
    assertThat(vc.getVlans().get("vlan3").getVrf(), nullValue());
    assertThat(vc.getVlans().get("vlan4").getVrf(), nullValue());
    assertThat(vc.getVlans().get("vlan5").getVrf(), nullValue());
  }

  @Test
  public void testVrfConversion() throws IOException {
    Configuration c = parseConfig("cumulus_nclu_vrf");

    // vrf presence and loopbacks
    assertThat(c, hasVrf("vrf1", hasInterfaces(contains("vrf1"))));
    assertThat(c, hasVrf("vrf2", hasInterfaces(contains("vrf2"))));

    // ip address
    assertThat(c, hasInterface("vrf1", hasAddress(new InterfaceAddress("10.0.0.1/24"))));
    assertThat(
        c,
        hasInterface(
            "vrf1",
            hasAllAddresses(
                containsInAnyOrder(
                    new InterfaceAddress("10.0.0.1/24"), new InterfaceAddress("10.0.1.1/24")))));
  }

  @Test
  public void testVrfExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_vrf");

    // vrfs
    assertThat(
        "Ensure vrfs are created", vc.getVrfs().keySet(), containsInAnyOrder("vrf1", "vrf2"));

    // name
    assertThat("Ensure name is extracted", vc.getVrfs().get("vrf1").getName(), equalTo("vrf1"));
    assertThat("Ensure name is extracted", vc.getVrfs().get("vrf2").getName(), equalTo("vrf2"));

    // ip address
    assertThat(
        "Ensure ip addresses are extracted",
        vc.getVrfs().get("vrf1").getAddresses(),
        contains(new InterfaceAddress("10.0.0.1/24"), new InterfaceAddress("10.0.1.1/24")));
    assertThat(
        "Ensure ip addresses are extracted", vc.getVrfs().get("vrf2").getAddresses(), empty());

    // vni
    assertThat("Ensure vni is extracted", vc.getVrfs().get("vrf1").getVni(), equalTo(10001));
    assertThat("Ensure vni is extracted", vc.getVrfs().get("vrf2").getVni(), nullValue());
  }

  @Test
  public void testVrfReferences() throws IOException {
    String hostname = "cumulus_nclu_vrf_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.VRF, "vrf1", 7));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.VRF, "vrf2", 1));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.VRF, "vrf3", 1));
  }

  @Test
  public void testVxlanExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_vxlan");

    // vxlan interfaces
    assertThat(
        vc.getVxlans().keySet(),
        containsInAnyOrder("v2", "v3", "v5", "v6", "v7", "v8", "v9", "v10"));

    // name
    assertThat(vc.getVxlans().get("v2").getName(), equalTo("v2"));
    assertThat(vc.getVxlans().get("v3").getName(), equalTo("v3"));
    // v4 is missing
    assertThat(vc.getVxlans().get("v5").getName(), equalTo("v5"));
    assertThat(vc.getVxlans().get("v6").getName(), equalTo("v6"));
    assertThat(vc.getVxlans().get("v7").getName(), equalTo("v7"));
    assertThat(vc.getVxlans().get("v8").getName(), equalTo("v8"));
    assertThat(vc.getVxlans().get("v9").getName(), equalTo("v9"));
    assertThat(vc.getVxlans().get("v10").getName(), equalTo("v10"));

    // vxlan id
    assertThat(vc.getVxlans().get("v2").getId(), equalTo(10002));
    assertThat(vc.getVxlans().get("v3").getId(), equalTo(10003));
    // v4 is missing
    assertThat(vc.getVxlans().get("v5").getId(), equalTo(10005));
    assertThat(vc.getVxlans().get("v6").getId(), equalTo(10005)); // dumb
    assertThat(vc.getVxlans().get("v7").getId(), equalTo(10007));
    assertThat(vc.getVxlans().get("v8").getId(), equalTo(10008));
    assertThat(vc.getVxlans().get("v9").getId(), equalTo(10009));
    assertThat(vc.getVxlans().get("v10").getId(), equalTo(10010));

    // bridge access
    assertThat(vc.getVxlans().get("v2").getBridgeAccessVlan(), equalTo(2));
    assertThat(vc.getVxlans().get("v3").getBridgeAccessVlan(), nullValue()); // out of order
    // v4 is missing
    assertThat(vc.getVxlans().get("v5").getBridgeAccessVlan(), equalTo(5));
    assertThat(vc.getVxlans().get("v6").getBridgeAccessVlan(), equalTo(5)); // dumb
    assertThat(vc.getVxlans().get("v7").getBridgeAccessVlan(), equalTo(7));
    assertThat(vc.getVxlans().get("v8").getBridgeAccessVlan(), equalTo(8));
    assertThat(vc.getVxlans().get("v9").getBridgeAccessVlan(), equalTo(9));
    assertThat(vc.getVxlans().get("v10").getBridgeAccessVlan(), nullValue()); // missing

    // vxlan local-tunnelip
    Ip expectedLocalTunnelip = Ip.parse("192.0.2.1");
    assertThat(vc.getVxlans().get("v2").getLocalTunnelip(), equalTo(expectedLocalTunnelip));
    assertThat(vc.getVxlans().get("v3").getLocalTunnelip(), nullValue()); // out of order
    // v4 is missing
    assertThat(vc.getVxlans().get("v5").getLocalTunnelip(), equalTo(expectedLocalTunnelip));
    assertThat(vc.getVxlans().get("v6").getLocalTunnelip(), equalTo(expectedLocalTunnelip));
    assertThat(vc.getVxlans().get("v7").getLocalTunnelip(), equalTo(expectedLocalTunnelip));
    assertThat(vc.getVxlans().get("v8").getLocalTunnelip(), equalTo(expectedLocalTunnelip));
    assertThat(vc.getVxlans().get("v9").getLocalTunnelip(), nullValue()); // missing
    assertThat(vc.getVxlans().get("v10").getLocalTunnelip(), equalTo(expectedLocalTunnelip));
  }

  @Test
  public void testVxlanReferences() throws IOException {
    String hostname = "cumulus_nclu_vxlan_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.VXLAN, "v2", 1));
  }
}
