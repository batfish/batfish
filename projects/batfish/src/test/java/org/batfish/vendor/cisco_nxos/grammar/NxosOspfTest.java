package org.batfish.vendor.cisco_nxos.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDefaultVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrfs;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasOspfAreaName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isOspfPassive;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isOspfPointToPoint;
import static org.batfish.datamodel.matchers.OspfProcessMatchers.hasAreas;
import static org.batfish.datamodel.matchers.VrfMatchers.hasOspfProcess;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.matchers.OspfAreaMatchers;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.cisco_nxos.representation.CiscoNxosConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests of the NX-OS parser dedicated to specifics of OSPF. */
@ParametersAreNonnullByDefault
public final class NxosOspfTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/cisco_nxos/grammar/ospf/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Batfish batfish = BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
    return batfish;
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    return configs.get(canonicalHostname);
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private @Nonnull CiscoNxosConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    CiscoNxosCombinedParser ciscoNxosParser = new CiscoNxosCombinedParser(src, settings);
    NxosControlPlaneExtractor extractor =
        new NxosControlPlaneExtractor(
            src, ciscoNxosParser, new Warnings(), new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(
            ciscoNxosParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    CiscoNxosConfiguration vendorConfiguration =
        (CiscoNxosConfiguration) extractor.getVendorConfiguration();
    assertThat(vendorConfiguration, notNullValue());
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }

  @Test
  public void testNxosOspfAreaParameters() throws IOException {
    Configuration c = parseConfig("nxos-ospf-area");
    String ifaceName = "Ethernet1";
    long areaNum = 1L;

    /* Ensure bidirectional references between OSPF area and interface */ assertThat(
        c, hasDefaultVrf(hasOspfProcess("1", hasAreas(hasKey(areaNum)))));
    OspfArea area = c.getDefaultVrf().getOspfProcesses().get("1").getAreas().get(areaNum);
    assertThat(area, OspfAreaMatchers.hasInterfaces(hasItem(ifaceName)));
    assertThat(c, hasInterface(ifaceName, hasOspfAreaName(areaNum)));
    assertThat(c, hasInterface(ifaceName, isOspfPassive(equalTo(false))));
    assertThat(c, hasInterface(ifaceName, isOspfPointToPoint()));
  }

  @Test
  public void testNxosOspfNonDefaultVrf() throws IOException {
    Configuration c = parseConfig("nxos-ospf-iface-in-vrf");
    String vrfName = "OTHER-VRF";
    String ifaceName = "Ethernet1";
    long areaNum = 1;

    /* Ensure bidirectional references between OSPF area and interface */
    assertThat(c, hasVrfs(hasKey(vrfName)));
    Vrf vrf = c.getVrfs().get(vrfName);
    assertThat(vrf, hasOspfProcess("1", hasAreas(hasKey(areaNum))));
    OspfArea area = vrf.getOspfProcesses().get("1").getAreas().get(areaNum);
    assertThat(area, OspfAreaMatchers.hasInterfaces(hasItem(ifaceName)));
    assertThat(c, hasInterface(ifaceName, hasVrf(sameInstance(vrf))));
    assertThat(c, hasInterface(ifaceName, hasOspfAreaName(areaNum)));
    assertThat(c, hasInterface(ifaceName, isOspfPassive(equalTo(false))));
    assertThat(c, hasInterface(ifaceName, isOspfPointToPoint()));
  }

  @Test
  public void testOspfPassiveInInterface() throws IOException {
    Configuration c = parseConfig("nxos-ospf-passive");
    assertThat(c, hasInterface("Ethernet2/1", allOf(isOspfPassive(), hasOspfAreaName(1L))));
  }

  @Test
  public void testOspfAreaInInterface() throws IOException {
    Configuration c = parseConfig("nxos-ospf-passive");
    assertThat(c, hasInterface("Ethernet2/2", allOf(not(isOspfPassive()), hasOspfAreaName(3L))));
  }

  @Test
  public void testDontCrashMultipleImplicitAs() throws IOException {
    // Don't crash.
    parseVendorConfig("nxos_ospf_multiple_implicit");
    parseConfig("nxos_ospf_multiple_implicit");
  }
}
