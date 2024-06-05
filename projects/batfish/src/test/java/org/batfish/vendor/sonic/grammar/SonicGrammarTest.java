package org.batfish.vendor.sonic.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAccessVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDhcpRelayAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.vendor.sonic.representation.SonicConfiguration.DEFAULT_MGMT_VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.vendor.sonic.representation.AclRule;
import org.batfish.vendor.sonic.representation.AclRule.PacketAction;
import org.batfish.vendor.sonic.representation.AclTable;
import org.batfish.vendor.sonic.representation.AclTable.Stage;
import org.batfish.vendor.sonic.representation.AclTable.Type;
import org.batfish.vendor.sonic.representation.DeviceMetadata;
import org.batfish.vendor.sonic.representation.InterfaceKeyProperties;
import org.batfish.vendor.sonic.representation.L3Interface;
import org.batfish.vendor.sonic.representation.MgmtVrf;
import org.batfish.vendor.sonic.representation.Port;
import org.batfish.vendor.sonic.representation.SonicConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SonicGrammarTest {

  private static final String SNAPSHOTS_PREFIX = "org/batfish/vendor/sonic/grammar/snapshots/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfish(String snapshotName, String... files) throws IOException {
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder()
            .setSonicConfigFiles(SNAPSHOTS_PREFIX + snapshotName, Arrays.asList(files))
            .build(),
        _folder);
  }

  /** A basic test that configdb files are read and properly linked to the FRR files. */
  @Test
  public void testBasic() throws IOException {
    String snapshotName = "basic";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("basic");
    vc.setWarnings(new Warnings());
    assertThat(
        vc.getConfigDb().getDeviceMetadata(),
        equalTo(ImmutableMap.of("localhost", new DeviceMetadata("basic"))));
    assertThat(
        vc.getConfigDb().getPorts(),
        equalTo(ImmutableMap.of("Ethernet0", Port.builder().setDescription("basic-port").build())));
    assertThat(
        vc.getConfigDb().getInterfaces(),
        equalTo(
            ImmutableMap.of(
                "Ethernet0",
                new L3Interface(
                    ImmutableMap.of(
                        ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                        InterfaceKeyProperties.builder().build())))));
    assertThat(vc.getFrrConfiguration().getRouteMaps().keySet(), equalTo(ImmutableSet.of("TEST")));

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertEquals("basic", c.getHostname());
    assertThat(
        Iterables.getOnlyElement(c.getAllInterfaces().values()),
        allOf(
            hasName("Ethernet0"),
            hasVrfName(DEFAULT_VRF_NAME),
            hasAddress("1.1.1.1/24"),
            hasDescription("basic-port")));
  }

  /** Test that loopback interfaces are created and put in the right VRF. */
  @Test
  public void testLoopback() throws IOException {
    String snapshotName = "loopback";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("loopback");
    vc.setWarnings(new Warnings());

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertThat(
        Iterables.getOnlyElement(c.getAllInterfaces().values()),
        allOf(hasName("Loopback0"), hasVrfName("default"), hasAddress("172.19.31.1/32")));
  }

  /** Test that management interfaces are created and put in the right VRF. */
  @Test
  public void testMgmt() throws IOException {
    String snapshotName = "mgmt";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("mgmt");
    vc.setWarnings(new Warnings());

    assertEquals(
        ImmutableMap.of("localhost", new DeviceMetadata("mgmt")),
        vc.getConfigDb().getDeviceMetadata());
    assertEquals(
        ImmutableMap.of(
            "eth0",
            new L3Interface(
                ImmutableMap.of(
                    ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                    InterfaceKeyProperties.builder().setGwAddr("10.11.0.1").build()))),
        vc.getConfigDb().getMgmtInterfaces());
    assertEquals(
        ImmutableMap.of("eth0", Port.builder().setAdminStatusUp(true).build()),
        vc.getConfigDb().getMgmtPorts());
    assertEquals(
        ImmutableMap.of(
            "vrf_global_not_default", MgmtVrf.builder().setMgmtVrfEnabled(true).build()),
        vc.getConfigDb().getMgmtVrfs());

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertThat(
        Iterables.getOnlyElement(c.getAllInterfaces().values()),
        allOf(hasName("eth0"), hasVrfName("vrf_global_not_default"), hasAddress("1.1.1.1/24")));
  }

  /**
   * Test that management interfaces are created and put in the default management VRF when none is
   * configured.
   */
  @Test
  public void testMgmtNoVrf() throws IOException {
    Batfish batfish = getBatfish("mgmt-novrf", "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("mgmt");
    vc.setWarnings(new Warnings());

    assertEquals(
        ImmutableMap.of("localhost", new DeviceMetadata("mgmt")),
        vc.getConfigDb().getDeviceMetadata());
    assertEquals(
        ImmutableMap.of(
            "eth0",
            new L3Interface(
                ImmutableMap.of(
                    ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                    InterfaceKeyProperties.builder().setGwAddr("10.11.0.1").build()))),
        vc.getConfigDb().getMgmtInterfaces());
    assertEquals(
        ImmutableMap.of("eth0", Port.builder().setAdminStatusUp(true).build()),
        vc.getConfigDb().getMgmtPorts());
    assertThat(vc.getConfigDb().getMgmtVrfs(), anEmptyMap());

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertThat(
        Iterables.getOnlyElement(c.getAllInterfaces().values()),
        allOf(hasName("eth0"), hasVrfName(DEFAULT_MGMT_VRF_NAME), hasAddress("1.1.1.1/24")));
  }

  @Test
  public void testNtpServer() throws IOException {
    String snapshotName = "ntp_server";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("ntp_server");
    vc.setWarnings(new Warnings());

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertThat(c.getNtpServers(), equalTo(ImmutableSet.of("10.128.255.33", "10.128.255.65")));
  }

  /** Test that TACPLUS related tables are processed. */
  @Test
  public void testTacplus() throws IOException {
    String snapshotName = "tacplus";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("tacplus");
    vc.setWarnings(new Warnings());
    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());

    assertThat(c.getTacacsServers(), equalTo(ImmutableSet.of("10.128.255.35", "10.128.255.67")));
    assertThat(c.getTacacsSourceInterface(), equalTo("Loopback0"));
  }

  @Test
  public void testSyslogServer() throws IOException {
    String snapshotName = "syslog_server";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("syslog_server");
    vc.setWarnings(new Warnings());

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertThat(c.getLoggingServers(), equalTo(ImmutableSet.of("10.128.255.33", "10.128.255.65")));
  }

  /** Test that VLAN related tables are processed. */
  @Test
  public void testVlan() throws IOException {
    String snapshotName = "vlan";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("vlan");
    vc.setWarnings(new Warnings());
    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());

    assertThat(
        c.getAllInterfaces().get("Vlan1"),
        allOf(
            hasName("Vlan1"),
            hasVlan(1),
            hasVrfName("default"),
            hasAddress("172.19.0.1/24"),
            hasInterfaceType(InterfaceType.VLAN),
            hasDhcpRelayAddresses(contains(Ip.parse("10.5.0.138"), Ip.parse("10.5.0.139")))));

    assertThat(
        c.getAllInterfaces().get("Ethernet0"),
        allOf(hasName("Ethernet0"), hasAccessVlan(1), hasSwitchPortMode(SwitchportMode.ACCESS)));
  }

  private static final BddTestbed _bddTestbed =
      new BddTestbed(ImmutableMap.of(), ImmutableMap.of());
  private static final IpAccessListToBdd _aclToBdd = _bddTestbed.getAclToBdd();

  private static @Nonnull BDD toMatchBDD(AclLine aclLine) {
    return _aclToBdd.toPermitAndDenyBdds(aclLine).getMatchBdd();
  }

  @Test
  public void testAcl() throws IOException {
    String snapshotName = "acl";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("acl");
    vc.setWarnings(new Warnings());
    assertThat(
        vc.getConfigDb().getAclTables(),
        equalTo(
            ImmutableMap.of(
                "test-acl",
                AclTable.builder()
                    .setPorts(ImmutableList.of("Ethernet0"))
                    .setStage(Stage.INGRESS)
                    .setType(Type.L3)
                    .build())));
    assertThat(
        vc.getConfigDb().getAclRules(),
        equalTo(
            ImmutableMap.of(
                "test-acl|RULE_1",
                AclRule.builder()
                    .setIpProtocol(17)
                    .setL4DstPort(161)
                    .setPacketAction(PacketAction.ACCEPT)
                    .setPriority(10)
                    .setSrcIp(Prefix.parse("10.1.4.0/22"))
                    .build())));

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    IpAccessList ipAccessList = c.getAllInterfaces().get("Ethernet0").getIncomingFilter();
    assertThat(
        ipAccessList.getLines().stream()
            .map(SonicGrammarTest::toMatchBDD)
            .collect(ImmutableList.toImmutableList()),
        contains(
            toMatchBDD(
                ExprAclLine.accepting(
                    and(
                        matchIpProtocol(17),
                        matchDstPort(161),
                        matchSrc(Prefix.parse("10.1.4.0/22")))))));
  }

  @Test
  public void testCommunityListReferenceTracking() throws IOException {
    String snapshotName = "community_list_reference_tracking";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertTrue(ccae.getUndefinedReferences().get("sonic_configs/device/frr.conf").isEmpty());
  }

  /** Test that resolv.conf files are parsed and its contents are converted. */
  @Test
  public void testResolvConf() throws IOException {
    String snapshotName = "resolv_conf";
    Batfish batfish =
        getBatfish(snapshotName, "device/frr.conf", "device/config_db.json", "device/resolv.conf");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("resolv_conf");
    vc.setWarnings(new Warnings());

    assertNotNull(vc.getResolvConf());
    assertEquals(ImmutableList.of(Ip.parse("1.1.1.1")), vc.getResolvConf().getNameservers());
    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertEquals("resolv_conf", c.getHostname());
    assertEquals(ImmutableSet.of("1.1.1.1"), c.getDnsServers());
  }

  /** Test that resolve.conf files are parsed and its contents are converted. */
  @Test
  public void testSnmpYml() throws IOException {
    String snapshotName = "snmp_yml";
    Batfish batfish =
        getBatfish(snapshotName, "device/frr.conf", "device/config_db.json", "device/snmp.yml");

    Warnings warnings = new Warnings(true, true, true);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("snmp_yml");
    vc.setWarnings(warnings);

    assertNotNull(vc.getSnmpYml());
    assertEquals("public", vc.getSnmpYml().getRoCommunity());
    assertEquals("public6", vc.getSnmpYml().getRoCommunity6());

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertEquals("snmp_yml", c.getHostname());

    SnmpCommunity snmpCommunity =
        Iterables.getOnlyElement(c.getDefaultVrf().getSnmpServer().getCommunities().values());
    assertThat(snmpCommunity.getAccessList(), equalTo("CTRL_PLANE_SNMP_ACL"));
    assertThat(snmpCommunity.getClientIps(), equalTo(Prefix.parse("10.1.4.0/22").toIpSpace()));
  }
}
