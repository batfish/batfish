package org.batfish.grammar.cumulus_nclu;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.grammar.cumulus_nclu.CumulusNcluConfigurationBuilder.LOOPBACK_INTERFACE_NAME;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
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
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cumulus.Bond;
import org.batfish.representation.cumulus.CumulusInterfaceType;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.RouteMap;
import org.batfish.representation.cumulus.RouteMapMatchInterface;
import org.batfish.representation.cumulus.StaticRoute;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class CumulusNcluGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cumulus_nclu/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @SuppressWarnings("unused")
  private Configuration parseConfig(String hostname) throws IOException {
    return parseTextConfigs(hostname).get(hostname);
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
    return (CumulusNcluConfiguration) extractor.getVendorConfiguration();
  }

  @Test
  public void testBondExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_bond");
    String bond1Name = "bond1";
    String bond2Name = "bond2";

    String[] expectedSlaves =
        new String[] {
          "swp1", "swp2", "swp3", "swp4", "swp5", "swp6", "swp7", "swp8",
        };

    // referenced interfaces should have been created
    assertThat(vc.getInterfaces().keySet(), containsInAnyOrder(expectedSlaves));

    assertThat(
        "Ensure bonds were extracted",
        vc.getBonds().keySet(),
        containsInAnyOrder(bond1Name, bond2Name));

    Bond bond1 = vc.getBonds().get(bond1Name);
    Bond bond2 = vc.getBonds().get(bond2Name);

    assertThat("Ensure access VLAN ID was set", bond1.getBridge().getAccess(), equalTo(2));
    assertThat("Ensure CLAG ID was set", bond1.getClagId(), equalTo(1));
    assertThat("Ensure slaves were set", bond1.getSlaves(), containsInAnyOrder(expectedSlaves));

    assertThat(
        "Ensure trunk VLAN IDs were set",
        bond2.getBridge().getVids(),
        equalTo(IntegerSpace.of(Range.closed(3, 5))));
  }

  @Test
  public void testBondReferences() throws IOException {
    String hostname = "cumulus_nclu_bond_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.BOND, "bond1", 2));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "bond2.4094", 2));
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
  public void testInterfaceExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_interface");

    assertThat(
        "Ensure interfaces are created",
        vc.getInterfaces().keySet(),
        containsInAnyOrder(
            "bond1", "bond2.4094", "bond3.4094", "eth0", "swp1", "swp2", "swp3", "swp4", "swp5.1"));

    // ip address
    assertThat(
        "Ensure ip addresses are extracted",
        vc.getInterfaces().get("bond2.4094").getIpAddresses(),
        contains(new InterfaceAddress("10.0.1.1/24"), new InterfaceAddress("172.16.0.1/24")));

    // clag backup-ip
    assertThat(
        "Ensure clag backup-ip extracted",
        vc.getInterfaces().get("bond2.4094").getClagBackupIp(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        "Ensure clag backup-ip is extracted",
        vc.getInterfaces().get("bond3.4094").getClagBackupIp(),
        equalTo(Ip.parse("192.168.0.1")));

    // clag backup-ip vrf
    assertThat(
        "Ensure clag backup-ip vrf is extracted",
        vc.getInterfaces().get("bond2.4094").getClagBackupIpVrf(),
        equalTo("mgmt"));
    assertThat(
        "Ensure clag backup-ip vrf is extracted",
        vc.getInterfaces().get("bond3.4094").getClagBackupIpVrf(),
        nullValue());

    // clag peer-ip
    assertThat(
        "Ensure clag peer-ip is extracted",
        vc.getInterfaces().get("bond2.4094").getClagPeerIp(),
        equalTo(Ip.parse("10.0.0.2")));

    // clag priority
    assertThat(
        "Ensure clag priority is extracted",
        vc.getInterfaces().get("bond2.4094").getClagPriority(),
        equalTo(1000));

    // clag sys-mac
    assertThat(
        "Ensure clag sys-mac is extracted",
        vc.getInterfaces().get("bond2.4094").getClagSysMac(),
        equalTo(MacAddress.parse("00:11:22:33:44:55")));

    // vrf
    assertThat(
        "Ensure vrf is extracted", vc.getInterfaces().get("swp5.1").getVrf(), equalTo("vrf1"));

    // interface type (computed)
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("bond1").getType(),
        equalTo(CumulusInterfaceType.BOND));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("bond2.4094").getType(),
        equalTo(CumulusInterfaceType.SUBINTERFACE));
    assertThat(
        "Ensure type is correctly calculated",
        vc.getInterfaces().get("bond3.4094").getType(),
        equalTo(CumulusInterfaceType.SUBINTERFACE));
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
        equalTo(CumulusInterfaceType.SUBINTERFACE));
  }

  @Test
  public void testInterfaceReferences() throws IOException {
    String hostname = "cumulus_nclu_interface_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "swp1", 3));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "swp2", 1));
    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "swp3", 1));
  }

  @Test
  public void testLoopbackExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_loopback");

    assertTrue("Ensure loopback is enabled", vc.getLoopback().getEnabled());
    assertThat(
        "Ensure clag vxlan-anycast-ip is extracted",
        vc.getLoopback().getClagVxlanAnycastIp(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        "Ensure clag vxlan-anycast-ip is extracted",
        vc.getLoopback().getAddresses(),
        contains(new InterfaceAddress("10.0.0.1/32"), new InterfaceAddress("10.0.1.1/24")));
  }

  @Test
  public void testLoopbackMissingExtraction() throws IOException {
    CumulusNcluConfiguration vc = parseVendorConfig("cumulus_nclu_loopback_missing");

    assertFalse("Ensure loopback is disabled", vc.getLoopback().getEnabled());
  }

  @Test
  public void testLoopbackReferences() throws IOException {
    String hostname = "cumulus_nclu_loopback_references";
    String filename = String.format("configs/%s", hostname);
    ConvertConfigurationAnswerElement ans =
        getBatfishForConfigurationNames(hostname).loadConvertConfigurationAnswerElementOrReparse();

    assertThat(
        ans, hasNumReferrers(filename, CumulusStructureType.LOOPBACK, LOOPBACK_INTERFACE_NAME, 2));
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
    assertThat(rm2.getEntries().keySet(), contains(1));
    // route-map entry num
    assertThat(rm1.getEntries().get(1).getNumber(), equalTo(1));
    assertThat(rm1.getEntries().get(2).getNumber(), equalTo(2));
    assertThat(rm1.getEntries().get(3).getNumber(), equalTo(3));
    assertThat(rm1.getEntries().get(4).getNumber(), equalTo(4));
    assertThat(rm2.getEntries().get(1).getNumber(), equalTo(1));
    // route-map entry action
    assertThat(rm1.getEntries().get(1).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm1.getEntries().get(2).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm1.getEntries().get(3).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm1.getEntries().get(4).getAction(), equalTo(LineAction.PERMIT));
    assertThat(rm2.getEntries().get(1).getAction(), equalTo(LineAction.DENY));

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
        equalTo(new RouteMapMatchInterface(ImmutableSet.of(LOOPBACK_INTERFACE_NAME))));
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

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.VRF, "vrf1", 5));
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
}
