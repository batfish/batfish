package org.batfish.grammar.flatjuniper;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class FlatJuniperControlPlaneExtractor implements ControlPlaneExtractor {

  private JuniperConfiguration _configuration;
  private final FlatJuniperCombinedParser _parser;
  private final String _text;
  private final Warnings _w;

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
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    Hierarchy hierarchy = new Hierarchy();
    // Pre-process parse tree
    PreprocessJuniperExtractor.preprocess(tree, hierarchy, _parser, _w);
    Span span = GlobalTracer.get().buildSpan("FlatJuniper::ConfigurationBuilder").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      // Build configuration from pre-processed parse tree
      ConfigurationBuilder cb =
          new ConfigurationBuilder(_parser, _text, _w, hierarchy.getTokenInputs());
      new BatfishParseTreeWalker(_parser).walk(cb, tree);
      _configuration = cb.getConfiguration();
    } finally {
      span.finish();
    }
  }
}
