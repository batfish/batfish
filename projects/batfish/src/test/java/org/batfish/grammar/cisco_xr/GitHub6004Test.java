package org.batfish.grammar.cisco_xr;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.junit.Assert.assertThat;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.main.Batfish;
import org.batfish.representation.cisco_xr.CiscoXrConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for https://github.com/batfish/batfish/issues/6004. */
@ParametersAreNonnullByDefault
public final class GitHub6004Test {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_xr/testconfigs/";

  @Rule public ExpectedException _thrown = ExpectedException.none();

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
  public void testGitHub6004() {
    CiscoXrConfiguration c = parseVendorConfig("gh6004");
    assertThat(c.getIpv4Acls(), hasKeys("FOO", "BAR", "BAZ"));
  }
}
