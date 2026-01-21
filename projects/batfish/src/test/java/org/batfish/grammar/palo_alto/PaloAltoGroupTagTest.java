package org.batfish.grammar.palo_alto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.junit.Test;

public final class PaloAltoGroupTagTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";

  private PaloAltoConfiguration parsePaloAltoConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(src, settings, null);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    Warnings parseWarnings = new Warnings();
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(src, parser, parseWarnings, new SilentSyntaxCollection());
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    PaloAltoConfiguration pac = (PaloAltoConfiguration) extractor.getVendorConfiguration();
    pac.setVendor(ConfigurationFormat.PALO_ALTO);
    pac.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    pac = SerializationUtils.clone(pac);
    pac.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    pac.setWarnings(parseWarnings);
    return pac;
  }

  @Test
  public void testGroupTagQuotes() {
    PaloAltoConfiguration c = parsePaloAltoConfig("repro-group-tag");
    assertThat(c, notNullValue());
    // Should have no warnings
    assertThat(c.getWarnings().getParseWarnings(), empty());

    // Verify the group-tag was extracted
    // Structure: vsys -> vsys1 -> rulebase -> security -> rules -> test-rule
    assertThat(
        c.getVirtualSystems()
            .get("vsys1")
            .getRulebase()
            .getSecurityRules()
            .get("test-rule")
            .getGroupTag(),
        org.hamcrest.Matchers.equalTo("REDACTED TAG"));
  }
}
