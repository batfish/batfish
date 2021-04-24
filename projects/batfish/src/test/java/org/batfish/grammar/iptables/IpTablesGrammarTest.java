package org.batfish.grammar.iptables;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarning;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.grammar.SilentSyntax;
import org.batfish.main.Batfish;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.junit.Test;

public class IpTablesGrammarTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/iptables/testconfigs/";

  @Nonnull
  private static IptablesVendorConfiguration parseVendorConfig(String filename) {
    String filepath = TESTCONFIGS_PREFIX + filename;
    String src = readResource(filepath, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    IptablesCombinedParser parser = new IptablesCombinedParser(src, settings);
    Warnings w = new Warnings(true, true, true);
    IptablesControlPlaneExtractor extractor =
        new IptablesControlPlaneExtractor(src, parser, w, filepath, new SilentSyntax());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    IptablesVendorConfiguration vendorConfiguration =
        (IptablesVendorConfiguration) extractor.getVendorConfiguration();
    // crash if not serializable
    IptablesVendorConfiguration vcClone = SerializationUtils.clone(vendorConfiguration);
    vcClone.setWarnings(w);
    return vcClone;
  }

  @Test
  public void testUnsupportedCommand() {
    IptablesVendorConfiguration c = parseVendorConfig("unsupported_command");
    // warnings for unsupported commands
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Check command is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Delete command is not supported")));
    assertThat(
        c.getWarnings(), hasParseWarning(hasComment("Delete Chain command is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Flush command is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Help command is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("List command is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("List Rules command is not supported")));
    assertThat(
        c.getWarnings(), hasParseWarning(hasComment("Rename Chain command is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Replace command is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Zero command is not supported")));
  }

  @Test
  public void testUnsupportedRuleSpecOptions() {
    IptablesVendorConfiguration c = parseVendorConfig("unsupported_rule_spec_options");
    // warnings about unknown options
    assertThat(c.getWarnings().getParseWarnings().size(), equalTo(3));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Option '-4' is not supported")));
    assertThat(c.getWarnings(), hasParseWarning(hasComment("Option '-6' is not supported")));
    assertThat(
        c.getWarnings(),
        hasParseWarning(hasComment("Option '-m tcp' is supported only with '-p tcp'")));

    // rules are created for the matches we do understand
    assertThat(c.getTables().get("filter").getChains().get("INPUT").getRules().size(), equalTo(4));
  }
}
