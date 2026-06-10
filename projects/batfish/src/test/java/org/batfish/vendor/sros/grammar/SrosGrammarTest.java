package org.batfish.vendor.sros.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.vendor.sros.representation.SrosConfiguration;
import org.junit.Test;

/** Tests of the Nokia SR-OS grammar, extraction scaffold, and brace/flat/mixed equivalence. */
public final class SrosGrammarTest {

  /**
   * The captured P0 lab r1 config (full {@code admin show configuration} output) must parse with no
   * FATAL parse errors. {@link #parseVendorConfig} runs with {@code throwOnParserError} + {@code
   * throwOnLexerError} set, so any FATAL would throw; reaching the assertions proves the zero-FATAL
   * P3 gate, and we additionally assert nothing was left unrecognized.
   */
  @Test
  public void testR1ConfigParsesCleanly() {
    SrosConfiguration vc = parseVendorConfig("r1_admin_show_configuration.txt");
    assertThat(vc.getUnrecognized(), equalTo(false));
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    // Spot-check a few canonical statements drawn from across the brace hierarchy.
    assertThat(vc.getStatements(), hasItem("configure router \"Base\" autonomous-system 65001"));
    assertThat(vc.getStatements(), hasItem("configure router \"Base\" bgp router-id 1.1.1.1"));
    assertThat(
        vc.getStatements(),
        hasItem("configure router \"Base\" bgp neighbor \"10.0.0.1\" group \"ebgp\""));
    assertThat(vc.getStatements(), hasItem("configure card 1 card-type iom-1"));
  }

  /**
   * The P3 acceptance test (findings "OPEN ARCH QUESTION", dhalperi 2026-06-04): the brace form,
   * the flat {@code /configure ...} form, and a mix of the two describing the same configuration
   * must all yield the same extracted model. The mixed case is the one that breaks designs assuming
   * one form per file, so it is exercised explicitly.
   */
  @Test
  public void testBraceFlatMixedEquivalence() {
    SrosConfiguration brace = parseVendorConfig("equivalence_brace.txt");
    SrosConfiguration flat = parseVendorConfig("equivalence_flat.txt");
    SrosConfiguration mixed = parseVendorConfig("equivalence_mixed.txt");

    assertThat(brace.getStatements(), equalTo(flat.getStatements()));
    assertThat(brace.getStatements(), equalTo(mixed.getStatements()));

    assertThat(
        brace.getStatements(),
        contains(
            "configure system name \"equivalence\"",
            "configure router \"Base\" autonomous-system 65001",
            "configure router \"Base\" interface \"system\" ipv4 primary address 1.1.1.1",
            "configure router \"Base\" interface \"system\" ipv4 primary prefix-length 32",
            "configure router \"Base\" bgp router-id 1.1.1.1",
            "configure router \"Base\" bgp group \"ebgp\" peer-as 65002",
            "configure router \"Base\" bgp group \"ebgp\" import policy [ \"import-all\" ]"));
  }

  /** Each equivalence config must parse with no FATAL errors and nothing unrecognized. */
  @Test
  public void testEquivalenceConfigsClean() {
    for (String name :
        new String[] {"equivalence_brace.txt", "equivalence_flat.txt", "equivalence_mixed.txt"}) {
      SrosConfiguration vc = parseVendorConfig(name);
      assertThat(vc.getUnrecognized(), equalTo(false));
      assertThat(vc.getWarnings().getParseWarnings(), empty());
      // A non-empty block (e.g. the bgp container) emits no line of its own.
      assertThat(vc.getStatements(), not(hasItem("configure router \"Base\" bgp")));
    }
  }

  @Test
  public void testHostnameExtraction() {
    SrosConfiguration vc = parseVendorConfig("hostname.txt");
    assertThat(vc.getHostname(), equalTo("sros-r1"));
  }

  /**
   * The device renders a list entry with no body as an empty same-line block — {@code <words> { }}
   * (open brace, optional whitespace, close brace, no intervening newline), e.g. the {@code
   * to-prefix}es of a {@code prefix-list ... type to}. This must parse with no FATAL error and the
   * entry must still appear as a canonical statement.
   */
  @Test
  public void testInlineEmptyBlockParses() {
    SrosConfiguration vc = parseVendorConfig("inline_empty_block.txt");
    assertThat(vc.getUnrecognized(), equalTo(false));
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(
        vc.getStatements(),
        hasItem(
            "configure policy-options prefix-list \"to-list\" prefix 10.20.0.0/16 type to to-prefix"
                + " 10.20.0.0/20"));
  }

  private static @Nonnull SrosConfiguration parseVendorConfig(String filename) {
    String src = readResource(TESTCONFIGS_PREFIX + filename, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    SrosCombinedParser parser = new SrosCombinedParser(src, settings);
    Warnings warnings = new Warnings(true, true, true);
    SrosControlPlaneExtractor extractor =
        new SrosControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    SrosConfiguration vc = (SrosConfiguration) extractor.getVendorConfiguration();
    vc.setFilename(TESTCONFIGS_PREFIX + filename);
    // Crash if not serializable.
    vc = SerializationUtils.clone(vc);
    vc.setWarnings(warnings);
    return vc;
  }

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/sros/grammar/testconfigs/";
}
