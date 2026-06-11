package org.batfish.vendor.sros.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.junit.Test;

/**
 * Tests that {@link SrosSilentSyntax} reports the statements {@link SrosFeatureExtractor} does not
 * read as silently ignored, and reports nothing for the statements it does read. This is the
 * regression net for the automatic visit-tracking in {@link SrosStatementTree}: a future extractor
 * change that stops reading a path must surface it as ignored, and one that starts reading a path
 * must stop reporting it.
 */
public final class SrosSilentSyntaxTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/sros/grammar/testconfigs/";

  /**
   * The captured r1 config models hardware, interfaces, BGP, and routing-policy, but not the {@code
   * system security} subtree or the top-level {@code persistent-indices} block. Both — and only
   * both — are reported as silently ignored; modeled leaves are not.
   */
  @Test
  public void testR1SilentlyIgnored() {
    List<String> ignored = silentlyIgnored("r1_admin_show_configuration.txt");

    // The two unmodeled subtrees are each reported once, at their block header, and nothing else.
    assertThat(ignored, containsInAnyOrder("configure system security", "persistent-indices"));

    // None of the modeled keywords appears in any reported element (no false positives).
    for (String modeled :
        List.of(
            "name",
            "card",
            "card-type",
            "autonomous-system",
            "router-id",
            "prefix-list",
            "entry",
            "policy-statement",
            "interface",
            "port")) {
      assertThat(
          "no silently-ignored element mentions modeled keyword: " + modeled,
          ignored.stream().noneMatch(s -> s.contains(modeled)),
          equalTo(true));
    }
  }

  /** A config that is entirely modeled reports nothing as silently ignored. */
  @Test
  public void testFullyModeledReportsNothing() {
    assertThat(silentlyIgnored("hostname.txt"), empty());
  }

  /**
   * Parse {@code filename} with parse-tree printing enabled (the mode the annotate tool uses) and
   * return the text of each silently-ignored element.
   */
  private static @Nonnull List<String> silentlyIgnored(String filename) {
    String src = readResource(TESTCONFIGS_PREFIX + filename, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    // The silent-syntax sweep, like the grammar-driven _null mechanism, only runs when printing the
    // parse tree (i.e. under the annotate tool).
    settings.setPrintParseTree(true);
    SrosCombinedParser parser = new SrosCombinedParser(src, settings);
    Warnings warnings = new Warnings(true, true, true);
    SilentSyntaxCollection silentSyntax = new SilentSyntaxCollection();
    SrosControlPlaneExtractor extractor =
        new SrosControlPlaneExtractor(src, parser, warnings, silentSyntax);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    return silentSyntax.getElements().stream()
        .map(SilentSyntaxCollection.SilentSyntaxElem::getText)
        .collect(Collectors.toList());
  }
}
