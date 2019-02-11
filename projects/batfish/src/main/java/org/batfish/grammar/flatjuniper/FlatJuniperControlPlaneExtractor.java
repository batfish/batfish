package org.batfish.grammar.flatjuniper;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Throwables;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class FlatJuniperControlPlaneExtractor implements ControlPlaneExtractor {

  private final FlatJuniperCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private JuniperConfiguration _configuration;

  public FlatJuniperControlPlaneExtractor(
      String fileText, FlatJuniperCombinedParser combinedParser, Warnings warnings) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(ParserRuleContext tree) {
    Hierarchy hierarchy = new Hierarchy();
    BatfishParseTreeWalker walker = new BatfishParseTreeWalker();

    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivateTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      DeactivateTreeBuilder dtb = new DeactivateTreeBuilder(hierarchy);
      walk(walker, dtb, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivateLinePruner").startActive()) {
      assert span != null; // avoid unused warning
      DeactivateLinePruner dp = new DeactivateLinePruner();
      walk(walker, dp, tree);
    }
    DeactivatedLinePruner dlp = new DeactivatedLinePruner(hierarchy);
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivatedLinePruner").startActive()) {
      assert span != null; // avoid unused warning
      walk(walker, dlp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::InitialTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      InitialTreeBuilder tb = new InitialTreeBuilder(hierarchy);
      walk(walker, tb, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::GroupTreeBuilder").startActive()) {
      assert span != null; // avoid unused warning
      GroupTreeBuilder gb = new GroupTreeBuilder(_parser, hierarchy);
      walk(walker, gb, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::ApplyGroupsApplicator").startActive()) {
      assert span != null; // avoid unused warning
      ApplyGroupsApplicator hb;
      do {
        hb = new ApplyGroupsApplicator(hierarchy, _w);
        walk(walker, hb, tree);
      } while (hb.getChanged());
    }
    try (ActiveSpan span = GlobalTracer.get().buildSpan("FlatJuniper::GroupPruner").startActive()) {
      assert span != null; // avoid unused warning
      GroupPruner gp = new GroupPruner();
      walk(walker, gp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::WildcardApplicator").startActive()) {
      assert span != null; // avoid unused warning
      WildcardApplicator wa = new WildcardApplicator(hierarchy);
      walk(walker, wa, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::WildcardPruner").startActive()) {
      assert span != null; // avoid unused warning
      WildcardPruner wp = new WildcardPruner();
      walk(walker, wp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::DeactivatedLinePruner again").startActive()) {
      assert span != null; // avoid unused warning
      walk(walker, dlp, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::ApplyPathApplicator").startActive()) {
      assert span != null; // avoid unused warning
      ApplyPathApplicator ap = new ApplyPathApplicator(hierarchy, _w);
      walk(walker, ap, tree);
    }
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::ConfigurationBuilder").startActive()) {
      assert span != null; // avoid unused warning
      ConfigurationBuilder cb =
          new ConfigurationBuilder(_parser, _text, _w, hierarchy.getTokenInputs());
      walk(walker, cb, tree);
      _configuration = cb.getConfiguration();
    }
  }

  private void walk(BatfishParseTreeWalker walker, ParseTreeListener listener, ParseTree tree) {
    try {
      walker.walk(listener, tree);
    } catch (Exception e) {
      _w.setErrorDetails(
          new ErrorDetails(
              Throwables.getStackTraceAsString(firstNonNull(e.getCause(), e)),
              new ParseExceptionContext(walker.getCurrentCtx(), _parser)));
      throw e;
    }
  }
}
