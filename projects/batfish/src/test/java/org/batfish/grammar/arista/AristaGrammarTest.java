package org.batfish.grammar.arista;

import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.junit.Test;

@ParametersAreNonnullByDefault
public class AristaGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/arista/testconfigs/";

  private @Nonnull CiscoConfiguration parseVendorConfig(String hostname) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + hostname);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    settings.setDebugFlags(ImmutableList.of(CiscoCombinedParser.DEBUG_FLAG_USE_ARISTA_BGP));
    CiscoCombinedParser ciscoParser =
        new CiscoCombinedParser(src, settings, ConfigurationFormat.ARISTA);
    CiscoControlPlaneExtractor extractor =
        new CiscoControlPlaneExtractor(
            src, ciscoParser, ConfigurationFormat.ARISTA, new Warnings());
    ParserRuleContext tree =
        Batfish.parse(
            ciscoParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(tree);
    CiscoConfiguration vendorConfiguration =
        (CiscoConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    SerializationUtils.clone(vendorConfiguration);
    return vendorConfiguration;
  }

  @Test
  public void testParseBgpFile() {
    // Don't crash
    parseVendorConfig("arista_bgp");
  }
}
