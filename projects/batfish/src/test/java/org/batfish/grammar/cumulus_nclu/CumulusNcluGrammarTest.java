package org.batfish.grammar.cumulus_nclu;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cumulus.Bond;
import org.batfish.representation.cumulus.CumulusInterfaceType;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
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

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.BOND, "bond1", 1));
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

    assertThat(ans, hasNumReferrers(filename, CumulusStructureType.INTERFACE, "swp1", 1));
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
}
