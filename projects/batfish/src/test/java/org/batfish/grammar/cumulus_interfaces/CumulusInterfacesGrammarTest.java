package org.batfish.grammar.cumulus_interfaces;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Set;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Cumulus_interfaces_configurationContext;
import org.batfish.representation.cumulus.CumulusConcatenatedConfiguration;
import org.batfish.representation.cumulus.CumulusInterfacesConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.CumulusStructureUsage;
import org.batfish.representation.cumulus.InterfaceBridgeSettings;
import org.batfish.representation.cumulus.InterfaceClagSettings;
import org.batfish.representation.cumulus.InterfacesInterface;
import org.batfish.representation.cumulus.StaticRoute;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link CumulusInterfacesParser}. */
public class CumulusInterfacesGrammarTest {
  private static final String FILENAME = "";
  private static CumulusConcatenatedConfiguration _config;
  private static CumulusInterfacesConfiguration _ic;
  private static ConvertConfigurationAnswerElement _ccae;
  private static Warnings _warnings;

  @Before
  public void setup() {
    _config = new CumulusConcatenatedConfiguration();
    _ic = _config.getInterfacesConfiguration();
    _ccae = new ConvertConfigurationAnswerElement();
    _warnings = new Warnings();
    _config.setFilename(FILENAME);
    _config.setAnswerElement(_ccae);
    _config.setWarnings(_warnings);
  }

  private static DefinedStructureInfo getDefinedStructureInfo(
      CumulusStructureType type, String name) {
    return _ccae
        .getDefinedStructures()
        .get(FILENAME)
        .getOrDefault(type.getDescription(), ImmutableSortedMap.of())
        .get(name);
  }

  private static Set<Integer> getStructureReferences(
      CumulusStructureType type, String name, CumulusStructureUsage usage) {
    // The config keeps reference data in a private variable, and only copies into the answer
    // element when you set it.
    _config.setAnswerElement(new ConvertConfigurationAnswerElement());
    return _config
        .getAnswerElement()
        .getReferencedStructures()
        .get(FILENAME)
        .get(type.getDescription())
        .get(name)
        .get(usage.getDescription());
  }

  private static CumulusInterfacesConfiguration parse(String input) {
    GrammarSettings settings =
        MockGrammarSettings.builder()
            .setDisableUnrecognized(true)
            .setThrowOnLexerError(true)
            .setThrowOnParserError(true)
            .build();
    CumulusInterfacesCombinedParser parser =
        new CumulusInterfacesCombinedParser(input, settings, 1, 0);
    Cumulus_interfaces_configurationContext ctxt = parser.parse();
    CumulusInterfacesConfigurationBuilder configurationBuilder =
        new CumulusInterfacesConfigurationBuilder(_config, parser, input, _warnings);
    new BatfishParseTreeWalker(parser).walk(configurationBuilder, ctxt);
    _config = SerializationUtils.clone(_config);
    return configurationBuilder.getConfig().getInterfacesConfiguration();
  }

  @Test
  public void testAuto() {
    String input = "auto swp1\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    assertThat(interfaces.getAutoIfaces(), contains("swp1"));
  }

  @Test
  public void testBlankLines() {
    String input = "\n\n\n";
    parse(input);
  }

  @Test
  public void testIface() {
    String input = "iface swp1\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    assertThat(interfaces.getInterfaces(), hasKeys("swp1"));
    assertThat(
        getDefinedStructureInfo(CumulusStructureType.INTERFACE, "swp1")
            .getDefinitionLines()
            .enumerate(),
        contains(1));
    assertThat(
        getStructureReferences(
            CumulusStructureType.INTERFACE, "swp1", CumulusStructureUsage.INTERFACE_SELF_REFERENCE),
        contains(1));
  }

  @Test
  public void testIfaceDescription() {
    String description = "foo hey 123!#?<>";
    String input = "iface swp1\n alias " + description + "\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("swp1");
    assertEquals(iface.getDescription(), description);
  }

  @Test
  public void testBondLacpBypassAllow() {
    parse("iface swp1\n bond-lacp-bypass-allow yes\n");
  }

  @Test
  public void testBondMaster() {
    parse("iface swp1\n bond-master bond0\n");
    assertThat(_warnings.getParseWarnings().size(), equalTo(1));
    assertEquals(
        _warnings.getParseWarnings().get(0).getComment(),
        "bond-master command is not supported. use bond-slaves to configure bonds.");
  }

  @Test
  public void testBondMiimon() {
    parse("iface swp1\n bond-miimon 100\n");
  }

  @Test
  public void testBondMode() {
    parse("iface swp1\n bond-mode 802.3ad\n");
  }

  @Test
  public void testBondMinLinks() {
    parse("iface swp1\n bond-min-links 1\n");
  }

  @Test
  public void testIfaceBondSlaves() {
    String input = "iface swp1\n bond-slaves i2 i3 i4\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    assertThat(
        getStructureReferences(
            CumulusStructureType.INTERFACE, "i2", CumulusStructureUsage.BOND_SLAVE),
        contains(2));
    assertThat(
        getStructureReferences(
            CumulusStructureType.INTERFACE, "i3", CumulusStructureUsage.BOND_SLAVE),
        contains(2));
    assertThat(
        getStructureReferences(
            CumulusStructureType.INTERFACE, "i4", CumulusStructureUsage.BOND_SLAVE),
        contains(2));
    assertThat(
        interfaces.getInterfaces().get("swp1").getBondSlaves(),
        containsInAnyOrder("i2", "i3", "i4"));

    // swp1 is inferred to be a bond
    assertThat(_ic.getInterfaces().get("swp1").getType(), equalTo(CumulusStructureType.BOND));
  }

  @Test
  public void testIfaceBondSlaves2() {
    String input = "iface swp1\n bond-slaves s1\n iface swp2\n bond-slaves s2\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    assertThat(
        getStructureReferences(
            CumulusStructureType.INTERFACE, "s1", CumulusStructureUsage.BOND_SLAVE),
        contains(2));
    assertThat(
        getStructureReferences(
            CumulusStructureType.INTERFACE, "s2", CumulusStructureUsage.BOND_SLAVE),
        contains(4));
    assertThat(interfaces.getInterfaces().get("swp1").getBondSlaves(), contains("s1"));
    assertThat(interfaces.getInterfaces().get("swp2").getBondSlaves(), contains("s2"));
  }

  @Test
  public void testBondXmitHashPolicy() {
    parse("iface swp1\n bond-xmit-hash-policy layer3+4\n");
  }

  @Test
  public void testIfaceAddress() {
    String input = "iface swp1\n address 10.12.13.14/24\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("swp1");
    assertThat(iface.getAddresses(), contains(ConcreteInterfaceAddress.parse("10.12.13.14/24")));
  }

  @Test
  public void testIfaceAddress32() {
    String input = "iface swp1\n address 10.12.13.14\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("swp1");
    assertThat(iface.getAddresses(), contains(ConcreteInterfaceAddress.parse("10.12.13.14/32")));
  }

  @Test
  public void testIfaceAddressV6() {
    parse("iface swp1\n address ::1/128\n");
    assertThat(_warnings.getParseWarnings().size(), equalTo(0));
  }

  @Test
  public void testIfaceAddressV6_128() {
    parse("iface swp1\n address ::1\n");
    assertThat(_warnings.getParseWarnings().size(), equalTo(0));
  }

  @Test
  public void testIfaceAddressVirtual() {
    String input = "iface vlan1\n address-virtual 00:00:00:00:00:00 1.2.3.4/24\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("vlan1");
    assertThat(
        iface.getAddressVirtuals(),
        equalTo(
            ImmutableMap.of(
                MacAddress.parse("00:00:00:00:00:00"),
                ImmutableSet.of(ConcreteInterfaceAddress.parse("1.2.3.4/24")))));
  }

  @Test
  public void testIfaceAddressVirtual_32() {
    String input = "iface vlan1\n address-virtual 00:00:00:00:00:00 1.2.3.4\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("vlan1");
    assertThat(
        iface.getAddressVirtuals(),
        equalTo(
            ImmutableMap.of(
                MacAddress.parse("00:00:00:00:00:00"),
                ImmutableSet.of(ConcreteInterfaceAddress.parse("1.2.3.4/32")))));
  }

  @Test
  public void testIfaceBridgeAccess() {
    String input = "iface swp1\n bridge-access 1234\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfaceBridgeSettings bridgeSettings =
        interfaces.getInterfaces().get("swp1").getBridgeSettings();
    assertThat(bridgeSettings.getAccess(), equalTo(1234));
  }

  @Test
  public void testIfaceBridgeArpNdSuppress() {
    parse("iface vni1\n bridge-arp-nd-suppress on\n");
  }

  @Test
  public void testIfaceBridgeLearning() {
    parse("iface vni1\n bridge-learning on\n");
  }

  @Test
  public void testIfaceBridgePorts_multiline() {
    String input = "iface bridge\n bridge-ports i2 \\\n i3 i4\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("bridge");
    assertThat(iface.getBridgePorts(), contains("i2", "i3", "i4"));
  }

  @Test
  public void testIfaceBridgePorts() {
    String input = "iface bridge\n bridge-ports i2 i3 i4\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("bridge");
    assertThat(iface.getBridgePorts(), contains("i2", "i3", "i4"));
  }

  @Test
  public void testIfaceBridgePvid() {
    String input = "iface bridge\n bridge-pvid 1\n";
    InterfaceBridgeSettings bridgeSettings =
        parse(input).getInterfaces().get("bridge").getBridgeSettings();
    assertThat(bridgeSettings.getPvid(), equalTo(1));
  }

  @Test
  public void testIfaceBridgeVids() {
    String input = "iface swp1\n bridge-vids 1 2 5-8 4\n";
    InterfaceBridgeSettings bridgeSettings =
        parse(input).getInterfaces().get("swp1").getBridgeSettings();
    assertThat(bridgeSettings.getVids().enumerate(), contains(1, 2, 4, 5, 6, 7, 8));
  }

  @Test
  public void testIfaceBridgeVlanAware_no() {
    parse("iface bridge\n bridge-vlan-aware no\n");
    assertThat(_warnings.getParseWarnings().size(), equalTo(1));
  }

  @Test
  public void testIfaceBridgeVlanAware_yes() {
    parse("iface bridge\n bridge-vlan-aware yes\n");
    assertThat(_warnings.getParseWarnings(), empty());
  }

  @Test
  public void testIfaceClagId() {
    String input = "iface swp1\n clag-id 123\n";
    InterfacesInterface iface = parse(input).getInterfaces().get("swp1");
    assertThat(iface.getClagId(), equalTo(123));
  }

  @Test
  public void testIfaceClagBackupIpAndVrf() {
    String input = "iface swp1\n clagd-backup-ip 1.2.3.4 vrf v1\n";
    InterfaceClagSettings clag = parse(input).getInterfaces().get("swp1").getClagSettings();
    assertThat(clag.getBackupIp(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(clag.getBackupIpVrf(), equalTo("v1"));
    assertThat(
        getStructureReferences(
            CumulusStructureType.VRF, "v1", CumulusStructureUsage.INTERFACE_CLAG_BACKUP_IP_VRF),
        contains(2));
  }

  @Test
  public void testIfaceClagBackupIpWithoutVrf() {
    String input = "iface swp1\n clagd-backup-ip 1.2.3.4\n";
    InterfaceClagSettings clag = parse(input).getInterfaces().get("swp1").getClagSettings();
    assertThat(clag.getBackupIp(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(clag.getBackupIpVrf(), equalTo(DEFAULT_VRF_NAME));
  }

  @Test
  public void testIfaceClagdPeerIp() {
    String input = "iface swp1\n clagd-peer-ip 1.2.3.4\n";
    InterfaceClagSettings clag = parse(input).getInterfaces().get("swp1").getClagSettings();
    assertThat(clag.getPeerIp(), equalTo(Ip.parse("1.2.3.4")));
  }

  @Test
  public void testIfaceClagdPeerIpLinkLocal() {
    String input = "iface swp1\n clagd-peer-ip linklocal\n";
    InterfaceClagSettings clag = parse(input).getInterfaces().get("swp1").getClagSettings();
    assertTrue(clag.isPeerIpLinkLocal());
  }

  @Test
  public void testIfaceClagdPriority() {
    String input = "iface swp1\n clagd-priority 42\n";
    InterfaceClagSettings clag = parse(input).getInterfaces().get("swp1").getClagSettings();
    assertThat(clag.getPriority(), equalTo(42));
  }

  @Test
  public void testClagdSysMac() {
    String input = "iface swp1\n clagd-sys-mac 00:00:00:00:00:00\n";
    InterfaceClagSettings clag = parse(input).getInterfaces().get("swp1").getClagSettings();
    assertThat(clag.getSysMac(), equalTo(MacAddress.parse("00:00:00:00:00:00")));
  }

  @Test
  public void testIfaceGateway() {
    parse("iface eth0\n gateway 1.2.3.4\n");
  }

  @Test
  public void testIfaceHwaddress() {
    parse("iface vlan1\n hwaddress 00:00:00:00:00:00\n");
  }

  @Test
  public void testIfaceLinkSpeed() {
    String input = "iface swp1\n link-speed 10000\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertThat(iface.getLinkSpeed(), equalTo(10000));
  }

  @Test
  public void testIfaceLinkSpeed_null() {
    String input = "iface swp1\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertNull(iface.getLinkSpeed());
  }

  @Test
  public void testIfaceMtu() {
    String input = "iface swp1\n mtu 9000\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertThat(iface.getMtu(), equalTo(9000));
  }

  @Test
  public void testIfaceMtu_null() {
    String input = "iface swp1\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertNull(iface.getMtu());
  }

  @Test
  public void testMstpctlBpduguard() {
    parse("iface vni1\n mstpctl-bpduguard yes\n");
  }

  @Test
  public void testMstpctlPortadminedge() {
    parse("iface vni1\n mstpctl-portadminedge yes\n");
  }

  @Test
  public void testMstpctlPortpdufilter() {
    parse("iface vni1\n mstpctl-portbpdufilter yes\n");
  }

  @Test
  public void testPostUpLinkPromisc() {
    parse("iface eth0 inet static\npost-up ip link set promisc on dev swp4\n");
    assertThat(_warnings.getParseWarnings().size(), equalTo(0));
    parse("iface eth0 inet static\npost-up ip link set swp1 promisc on\n");
    assertThat(_warnings.getParseWarnings().size(), equalTo(0));
  }

  @Test
  public void testPostUpIpRouteAddDev() {
    String input = "iface eth0 inet static\n post-up ip route add 10.10.10.0/24 dev eth0\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("eth0");
    assertThat(
        iface.getPostUpIpRoutes(),
        equalTo(
            ImmutableList.of(new StaticRoute(Prefix.parse("10.10.10.0/24"), null, "eth0", null))));
  }

  @Test
  public void testPostUpIpRouteAddVia() {
    String input = "iface eth0 inet static\n post-up ip route add 10.10.10.0/24 via 10.1.1.1\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("eth0");
    assertThat(
        iface.getPostUpIpRoutes(),
        equalTo(
            ImmutableList.of(
                new StaticRoute(Prefix.parse("10.10.10.0/24"), Ip.parse("10.1.1.1"), null, null))));
  }

  @Test
  public void testIfaceVlanId() {
    String input = "iface vlan1\n vlan-id 1\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("vlan1");
    assertThat(iface.getVlanId(), equalTo(1));
    // not marked as an interface definition
    assertNull(getDefinedStructureInfo(CumulusStructureType.INTERFACE, "vlan1"));
    assertNotNull(getDefinedStructureInfo(CumulusStructureType.VLAN, "vlan1"));
    assertThat(
        getStructureReferences(
            CumulusStructureType.VLAN, "vlan1", CumulusStructureUsage.VLAN_SELF_REFERENCE),
        contains(1));
  }

  @Test
  public void testIfaceVlanRawDevice() {
    String input = "iface vlan1\n vlan-raw-device bridge\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("vlan1");
    assertThat(iface.getVlanRawDevice(), equalTo("bridge"));
  }

  @Test
  public void testIfaceVrf() {
    String input = "iface swp1\n vrf v1\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertThat(iface.getVrf(), equalTo("v1"));
    assertThat(
        getStructureReferences(CumulusStructureType.VRF, "v1", CumulusStructureUsage.INTERFACE_VRF),
        contains(2));
  }

  @Test
  public void testIfaceVrfTable() {
    String input = "iface vrf1\n vrf-table auto\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("vrf1");
    assertThat(iface.getVrfTable(), equalTo("auto"));
    // not marked as an interface definition
    assertNull(getDefinedStructureInfo(CumulusStructureType.INTERFACE, "vrf1"));
    assertNotNull(getDefinedStructureInfo(CumulusStructureType.VRF, "vrf1"));
    assertThat(
        getStructureReferences(
            CumulusStructureType.VRF, "vrf1", CumulusStructureUsage.VRF_SELF_REFERENCE),
        contains(1));
  }

  @Test
  public void testLoopback() {
    parse("iface lo inet loopback\n");
    assertTrue(_ic.getInterfaces().containsKey("lo"));
  }

  @Test
  public void testLoopbackAddress() {
    parse("iface lo inet loopback\n address 1.2.3.4/24\n");
    assertThat(
        _ic.getInterfaces().get("lo").getAddresses(),
        contains(ConcreteInterfaceAddress.parse("1.2.3.4/24")));
  }

  @Test
  public void testLoopbackAlias() {
    parse("iface lo inet loopback\n alias my aliases\n");
    assertThat(_ic.getInterfaces().get("lo").getDescription(), equalTo("my aliases"));
  }

  @Test
  public void testLoopbackClagdVxlanAnycastIp() {
    parse("iface lo inet loopback\n clagd-vxlan-anycast-ip 1.2.3.4\n");
    assertThat(_ic.getInterfaces().get("lo").getClagVxlanAnycastIp(), equalTo(Ip.parse("1.2.3.4")));
  }

  @Test
  public void testVxlanId() {
    String input = "iface swp1\n vxlan-id 123\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertThat(iface.getVxlanId(), equalTo(123));
    // not marked as an interface definition
    assertNull(getDefinedStructureInfo(CumulusStructureType.INTERFACE, "swp1"));
    assertNotNull(getDefinedStructureInfo(CumulusStructureType.VXLAN, "swp1"));
    assertThat(
        getStructureReferences(
            CumulusStructureType.VXLAN, "swp1", CumulusStructureUsage.VXLAN_SELF_REFERENCE),
        contains(1));
  }

  @Test
  public void testVxlanLocalTunnelIp() {
    String input = "iface swp1\n vxlan-local-tunnelip 1.2.3.4\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertEquals(iface.getVxlanLocalTunnelIp(), Ip.parse("1.2.3.4"));
  }

  @Test
  public void testStaticInterface() {
    String input = "iface swp1 inet static\n link-speed 10000\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("swp1");
    assertEquals(iface.getLinkSpeed(), Integer.valueOf(10000));
  }

  @Test
  public void testDhcpInterface() {
    String input = "iface eth0 inet dhcp\n link-speed 10000\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("eth0");
    assertEquals(iface.getLinkSpeed(), Integer.valueOf(10000));
    assertNull(iface.getAddresses());
  }

  @Test
  public void testManualInterface() {
    String input = "iface eth0 inet manual\n link-speed 10000\n";
    CumulusInterfacesConfiguration interfaces = parse(input);
    InterfacesInterface iface = interfaces.getInterfaces().get("eth0");
    assertEquals(iface.getLinkSpeed(), Integer.valueOf(10000));
    assertNull(iface.getAddresses());
  }

  @Test
  public void testLinkAutoNeg() {
    String input = "iface swp1 inet static\n link-autoneg on\n";
    // assert we can parse the input text
    parse(input);
  }
}
