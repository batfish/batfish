package org.batfish.grammar.flatjuniper;

import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;

/**
 * Parse tree extractor used for generating pre-processed Juniper configuration text from an initial
 * unprocessed flat Juniper parse tree.
 */
@ParametersAreNonnullByDefault
public final class PreprocessJuniperExtractor {

  private static final String HEADER = "####BATFISH PRE-PROCESSED JUNIPER CONFIG####\n";

  /**
   * Pre-process a flat Juniper parse tree by generating and pruning parse tree nodes corresponding
   * to various lines in the input configuration.
   *
   * <p>Mutations are made directly to the input parse {@code tree} and serve as the output of this
   * function.
   *
   * <p>Pre-processing consists of:
   *
   * <ol>
   *   <li>Pruning lines deactivated by 'deactivate' lines
   *   <li>Pruning 'deactivate' lines
   *   <li>Generating lines corresponding to 'apply-groups' lines, while respecting
   *       'apply-groups-except' lines;
   *   <li>Pruning 'groups' lines; and 'apply-groups' and 'apply-groups-except' lines
   *   <li>Pruning wildcard lines
   *   <li>Generating lines corresponding to 'apply-path' lines
   *   <li>Pruning 'apply-path' lines
   * </ol>
   *
   * @param tree The flat-Juniper parse tree to be pre-processed in-place.
   * @param hierarchy An empty {@link Hierarchy} that will be populated with trees for regular
   *     configuration lines, groups lines, and deactivate lines
   * @param fileText The original text of the configuration
   * @param parser The parser that produced the parse {@code tree}.
   * @param w The store for warnings produced during pre-processing
   */
  static void preprocess(
      ParserRuleContext tree,
      Hierarchy hierarchy,
      String fileText,
      FlatJuniperCombinedParser parser,
      Warnings w) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivateTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      DeactivateTreeBuilder dtb = new DeactivateTreeBuilder(hierarchy);
      walker.walk(dtb, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivateLinePruner").startActive()) {
      assert span != null; // avoid unused warning
      DeactivateLinePruner dp = new DeactivateLinePruner();
      walker.walk(dp, tree);
    }
    DeactivatedLinePruner dlp = new DeactivatedLinePruner(hierarchy);
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivatedLinePruner").startActive()) {
      assert span != null; // avoid unused warning
      walker.walk(dlp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::InitialTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      InitialTreeBuilder tb = new InitialTreeBuilder(hierarchy);
      walker.walk(tb, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::GroupTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      GroupTreeBuilder gb = new GroupTreeBuilder(parser, hierarchy);
      walker.walk(gb, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::ApplyGroupsApplicator").startActive()) {
      assert span != null; // avoid unused warning
      ApplyGroupsApplicator hb;
      do {
        hb = new ApplyGroupsApplicator(hierarchy, w);
        walker.walk(hb, tree);
      } while (hb.getChanged());
    }
    try (ActiveSpan span = GlobalTracer.get().buildSpan("FlatJuniper::GroupPruner").startActive()) {
      assert span != null; // avoid unused warning
      GroupPruner gp = new GroupPruner();
      walker.walk(gp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::WildcardApplicator").startActive()) {
      assert span != null; // avoid unused warning
      WildcardApplicator wa = new WildcardApplicator(hierarchy);
      walker.walk(wa, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::WildcardPruner").startActive()) {
      assert span != null; // avoid unused warning
      WildcardPruner wp = new WildcardPruner();
      walker.walk(wp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivatedLinePruner again").startActive()) {
      assert span != null; // avoid unused warning
      walker.walk(dlp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::ApplyPathApplicator").startActive()) {
      assert span != null; // avoid unused warning
      ApplyPathApplicator ap = new ApplyPathApplicator(hierarchy, w);
      walker.walk(ap, tree);
    }
  }

  private final FlatJuniperCombinedParser _parser;
  private String _preprocessedConfigurationText;
  private final String _text;
  private final Warnings _w;

  public PreprocessJuniperExtractor(
      String fileText, FlatJuniperCombinedParser combinedParser, Warnings warnings) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
  }

  public @Nonnull String getPreprocessedConfigurationText() {
    return _preprocessedConfigurationText;
  }

  /**
   * Pre-process a flat Juniper parse {@code tree}, after which pre-processed configuration text
   * will be available via {@link #getPreprocessedConfigurationText}.
   */
  public void processParseTree(ParserRuleContext tree) {
    preprocess(tree, new Hierarchy(), _text, _parser, _w);
    Hierarchy finalHierarchy = new Hierarchy();
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::InitialTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      InitialTreeBuilder tb = new InitialTreeBuilder(finalHierarchy);
      new BatfishParseTreeWalker(_parser).walk(tb, tree);
    }
    _preprocessedConfigurationText = finalHierarchy.toSetLines(HEADER);
  }
}
