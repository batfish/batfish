package org.batfish.grammar.flatjuniper;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.PreprocessExtractor;

/**
 * Parse tree extractor used for generating pre-processed Juniper configuration text from an initial
 * unprocessed flat Juniper parse tree.
 */
@ParametersAreNonnullByDefault
public final class PreprocessJuniperExtractor implements PreprocessExtractor {

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
   *   <li>Applying insertions (moves) and deleteions
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
   * @param parser The parser that produced the parse {@code tree}.
   * @param w The store for warnings produced during pre-processing
   */
  static void preprocess(
      ParserRuleContext tree, Hierarchy hierarchy, FlatJuniperCombinedParser parser, Warnings w) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    Span deleterSpan = GlobalTracer.get().buildSpan("FlatJuniper::InsertDeleteApplicator").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(deleterSpan)) {
      assert scope != null; // avoid unused warning
      InsertDeleteApplicator d = new InsertDeleteApplicator(parser, w);
      walker.walk(d, tree);
    } finally {
      deleterSpan.finish();
    }
    Span treeBuilderSpan =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivateTreeBuilder").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(treeBuilderSpan)) {
      assert scope != null; // avoid unused warning
      DeactivateTreeBuilder dtb = new DeactivateTreeBuilder(hierarchy);
      walker.walk(dtb, tree);
    } finally {
      treeBuilderSpan.finish();
    }
    Span linePrunerSpan = GlobalTracer.get().buildSpan("FlatJuniper::DeactivateLinePruner").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(linePrunerSpan)) {
      assert scope != null; // avoid unused warning
      DeactivateLinePruner dp = new DeactivateLinePruner();
      walker.walk(dp, tree);
    } finally {
      linePrunerSpan.finish();
    }
    DeactivatedLinePruner dlp = new DeactivatedLinePruner(hierarchy);
    Span dlpSpan = GlobalTracer.get().buildSpan("FlatJuniper::DeactivatedLinePruner").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(dlpSpan)) {
      assert scope != null; // avoid unused warning
      walker.walk(dlp, tree);
    } finally {
      dlpSpan.finish();
    }
    Span treeSpan = GlobalTracer.get().buildSpan("FlatJuniper::InitialTreeBuilder").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(treeSpan)) {
      assert scope != null; // avoid unused warning
      InitialTreeBuilder tb = new InitialTreeBuilder(hierarchy);
      walker.walk(tb, tree);
    } finally {
      treeSpan.finish();
    }
    Span groupTreeSpan = GlobalTracer.get().buildSpan("FlatJuniper::GroupTreeBuilder").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(groupTreeSpan)) {
      assert scope != null; // avoid unused warning
      GroupTreeBuilder gb = new GroupTreeBuilder(hierarchy);
      walker.walk(gb, tree);
    } finally {
      groupTreeSpan.finish();
    }
    Span applicatorSpan =
        GlobalTracer.get().buildSpan("FlatJuniper::ApplyGroupsApplicator").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(applicatorSpan)) {
      assert scope != null; // avoid unused warning
      ApplyGroupsApplicator hb;
      do {
        hb = new ApplyGroupsApplicator(hierarchy, w);
        walker.walk(hb, tree);
      } while (hb.getChanged());
    } finally {
      applicatorSpan.finish();
    }
    Span prunerSpan = GlobalTracer.get().buildSpan("FlatJuniper::GroupPruner").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(prunerSpan)) {
      assert scope != null; // avoid unused warning
      GroupPruner gp = new GroupPruner();
      walker.walk(gp, tree);
    } finally {
      prunerSpan.finish();
    }
    Span wildcardAppSpan = GlobalTracer.get().buildSpan("FlatJuniper::WildcardApplicator").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(wildcardAppSpan)) {
      assert scope != null; // avoid unused warning
      WildcardApplicator wa = new WildcardApplicator(hierarchy);
      walker.walk(wa, tree);
    } finally {
      wildcardAppSpan.finish();
    }
    Span wdPrunerSpan = GlobalTracer.get().buildSpan("FlatJuniper::WildcardPruner").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(wdPrunerSpan)) {
      assert scope != null; // avoid unused warning
      WildcardPruner wp = new WildcardPruner();
      walker.walk(wp, tree);
    } finally {
      wdPrunerSpan.finish();
    }
    Span prunerAgainSpan =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivatedLinePruner again").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(prunerAgainSpan)) {
      assert scope != null; // avoid unused warning
      walker.walk(dlp, tree);
    } finally {
      prunerAgainSpan.finish();
    }
    Span apApplictorSpan = GlobalTracer.get().buildSpan("FlatJuniper::ApplyPathApplicator").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(applicatorSpan)) {
      assert scope != null; // avoid unused warning
      ApplyPathApplicator ap = new ApplyPathApplicator(hierarchy, w);
      walker.walk(ap, tree);
    } finally {
      apApplictorSpan.finish();
    }
  }

  private final FlatJuniperCombinedParser _parser;
  private String _preprocessedConfigurationText;
  private final Warnings _w;

  public PreprocessJuniperExtractor(FlatJuniperCombinedParser combinedParser, Warnings warnings) {
    _parser = combinedParser;
    _w = warnings;
  }

  @Override
  public @Nonnull String getPreprocessedConfigurationText() {
    return _preprocessedConfigurationText;
  }

  /**
   * Pre-process a flat Juniper parse {@code tree}, after which pre-processed configuration text
   * will be available via {@link #getPreprocessedConfigurationText}.
   */
  @Override
  public void processParseTree(ParserRuleContext tree) {
    preprocess(tree, new Hierarchy(), _parser, _w);
    Hierarchy finalHierarchy = new Hierarchy();
    Span span = GlobalTracer.get().buildSpan("FlatJuniper::InitialTreeBuilder").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      InitialTreeBuilder tb = new InitialTreeBuilder(finalHierarchy);
      new BatfishParseTreeWalker(_parser).walk(tb, tree);
    } finally {
      span.finish();
    }
    _preprocessedConfigurationText = finalHierarchy.toSetLines(HEADER);
  }
}
