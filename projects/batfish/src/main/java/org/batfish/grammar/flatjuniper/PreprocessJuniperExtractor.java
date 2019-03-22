package org.batfish.grammar.flatjuniper;

import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;

@ParametersAreNonnullByDefault
public final class PreprocessJuniperExtractor {

  private static final String HEADER = "####BATFISH PRE-PROCESSED JUNIPER CONFIG####\n";

  static void preprocess(
      Hierarchy hierarchy,
      String fileText,
      FlatJuniperCombinedParser parser,
      Warnings w,
      ParseTreeWalker walker,
      ParserRuleContext tree) {
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

  public void processParseTree(ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    preprocess(new Hierarchy(), _text, _parser, _w, walker, tree);
    Hierarchy finalHierarchy = new Hierarchy();
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::InitialTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      InitialTreeBuilder tb = new InitialTreeBuilder(finalHierarchy);
      walker.walk(tb, tree);
    }
    _preprocessedConfigurationText = finalHierarchy.dump(HEADER);
  }
}
