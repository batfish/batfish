package org.batfish.grammar.cisco_xr;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.representation.cisco_xr.CiscoXrConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for https://github.com/batfish/batfish/issues/10062. */
public final class GitHub10062Test {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_xr/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull CiscoXrConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    CiscoXrCombinedParser ciscoXrParser = new CiscoXrCombinedParser(src, settings);
    CiscoXrControlPlaneExtractor extractor =
        new CiscoXrControlPlaneExtractor(
            src,
            ciscoXrParser,
            ConfigurationFormat.CISCO_IOS_XR,
            new Warnings(),
            new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(
            ciscoXrParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    CiscoXrConfiguration vendorConfiguration =
        (CiscoXrConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }

  @Test
  public void testBgpVrfRdExtraction() {
    CiscoXrConfiguration c = parseVendorConfig("gh10062");
    assertThat(c.getVrfs(), hasKeys("default", "two_byte", "four_byte", "dotted", "ip_admin"));
    // type 0: 2-byte ASN administrator, 4-byte assigned number
    assertThat(
        c.getVrfs().get("two_byte").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from(65000, 4294967295L)));
    // type 2: 4-byte asplain ASN administrator, 2-byte assigned number
    assertThat(
        c.getVrfs().get("four_byte").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from(4200000001L, 200)));
    // type 2: 4-byte asdot ASN administrator (1.100 = 65636)
    assertThat(
        c.getVrfs().get("dotted").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from((1L << 16) + 100, 200)));
    // type 1: IP address administrator
    assertThat(
        c.getVrfs().get("ip_admin").getRouteDistinguisher(),
        equalTo(RouteDistinguisher.from(Ip.parse("10.0.0.1"), 200)));
  }
}
