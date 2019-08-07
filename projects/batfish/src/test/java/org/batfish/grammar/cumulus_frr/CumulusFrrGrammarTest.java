package org.batfish.grammar.cumulus_frr;

import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.main.Batfish;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class CumulusFrrGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cumulus_frr/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static CumulusNcluConfiguration parseVendorConfig(String filename) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    return parseVendorConfig(filename, settings);
  }

  private static CumulusNcluConfiguration parseVendorConfig(String filename, Settings settings) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + filename);
    return parseFromTextWithSettings(src, settings);
  }

  private static CumulusNcluConfiguration parse(String src) {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);

    return parseFromTextWithSettings(src, settings);
  }

  @Nonnull
  private static CumulusNcluConfiguration parseFromTextWithSettings(String src, Settings settings) {
    CumulusNcluConfiguration configuration = new CumulusNcluConfiguration();
    CumulusFrrCombinedParser parser = new CumulusFrrCombinedParser(src, settings, 1, 0);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    CumulusFrrConfigurationBuilder cb = new CumulusFrrConfigurationBuilder(configuration);
    walker.walk(cb, tree);
    return cb.getVendorConfiguration();
  }

  @Test
  public void testCumulusFrrVrf() {
    CumulusNcluConfiguration config = parse("vrf NAME\n exit-vrf");
    assertThat(config.getVrfs().keySet(), equalTo(ImmutableSet.of("NAME")));
  }
}
