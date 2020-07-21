package org.batfish.grammar.cisco_xr;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cisco_xr.CiscoXrConfiguration;
import org.batfish.representation.cisco_xr.Interface;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for https://github.com/batfish/batfish/issues/6018. */
@ParametersAreNonnullByDefault
public final class GitHub6018Test {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_xr/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      String[] names = new String[] {TESTCONFIGS_PREFIX + hostname};
      Map<String, Configuration> configs = BatfishTestUtils.parseTextConfigs(_folder, names);
      assertThat(configs, hasKey(hostname.toLowerCase()));
      Configuration c = configs.get(hostname.toLowerCase());
      assertThat(c, hasConfigurationFormat(ConfigurationFormat.CISCO_IOS_XR));
      // Ensure that we used the CiscoXr parser.
      assertThat(c.getVendorFamily().getCiscoXr(), notNullValue());
      return c;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nonnull CiscoXrConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    CiscoXrCombinedParser ciscoXrParser = new CiscoXrCombinedParser(src, settings);
    CiscoXrControlPlaneExtractor extractor =
        new CiscoXrControlPlaneExtractor(
            src, ciscoXrParser, ConfigurationFormat.CISCO_IOS_XR, new Warnings());
    ParserRuleContext tree =
        Batfish.parse(
            ciscoXrParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    CiscoXrConfiguration vendorConfiguration =
        (CiscoXrConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }

  @Test
  public void testGitHub6018Extraction() {
    CiscoXrConfiguration c = parseVendorConfig("gh6018");
    assertThat(c.getInterfaces(), hasKeys("TenGigE0/0/0/24/0", "Bundle-Ether5"));
    Interface tenGE = c.getInterfaces().get("TenGigE0/0/0/24/0");
    assertThat(tenGE.getBundleId(), equalTo(5));
  }

  @Test
  public void testGitHub6018Conversion() {
    Configuration c = parseConfig("gh6018");
    assertThat(c.getAllInterfaces(), hasKeys("TenGigE0/0/0/24/0", "Bundle-Ether5"));
    org.batfish.datamodel.Interface tenGE = c.getAllInterfaces().get("TenGigE0/0/0/24/0");
    assertThat(tenGE.getChannelGroup(), equalTo("Bundle-Ether5"));
    assertThat(tenGE.getBandwidth(), equalTo(10e9));
    org.batfish.datamodel.Interface bundle = c.getAllInterfaces().get("Bundle-Ether5");
    assertThat(bundle.getChannelGroupMembers(), containsInAnyOrder("TenGigE0/0/0/24/0"));
    assertThat(bundle.getBandwidth(), equalTo(10e9));
  }
}
