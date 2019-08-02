package org.batfish.grammar.cumulus_concatenated;

import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Arrays;
import java.util.SortedMap;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.grammar.GrammarSettings;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class CumulusConcatenatedGrammarTest {
  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/grammar/cumulus_concatenated/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static CumulusNcluConfiguration parseVendorConfig(String filename) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    return parseVendorConfig(filename, settings);
  }

  private static CumulusNcluConfiguration parseVendorConfig(
      String filename, GrammarSettings settings) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + filename);
    CumulusConcatenatedCombinedParser parser = new CumulusConcatenatedCombinedParser(src, settings);
    CumulusConcatenatedControlPlaneExtractor extractor =
        new CumulusConcatenatedControlPlaneExtractor(
            src, new Warnings(), filename, parser.getSettings(), null, false);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(tree);
    CumulusNcluConfiguration vendorConfiguration =
        (CumulusNcluConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + filename);
    return vendorConfiguration;
  }

  private SortedMap<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    return BatfishTestUtils.getBatfish(parseTextConfigs(configurationNames), _folder);
  }

  @Test
  public void testConcatenation() {
    CumulusNcluConfiguration cfg = parseVendorConfig("concatenation");
    assertThat(cfg.getHostname(), equalTo("hostname"));
  }

  @Test
  public void testPortsUnrecognized() {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    settings.setDisableUnrecognized(false);
    settings.setThrowOnLexerError(false);
    settings.setThrowOnParserError(false);
    CumulusNcluConfiguration cfg = parseVendorConfig("ports_unrecognized", settings);
    assertThat(cfg.getHostname(), equalTo("hostname"));
  }
}
