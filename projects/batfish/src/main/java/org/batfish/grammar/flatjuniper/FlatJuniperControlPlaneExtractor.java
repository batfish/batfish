package org.batfish.grammar.flatjuniper;

import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
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
  public void processParseTree(ParserRuleContext tree) {
    Hierarchy hierarchy = new Hierarchy();
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);

    PreprocessJuniperExtractor.preprocess(hierarchy, _text, _parser, _w, walker, tree);
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("FlatJuniper::ConfigurationBuilder").startActive()) {
      assert span != null; // avoid unused warning
      ConfigurationBuilder cb =
          new ConfigurationBuilder(_parser, _text, _w, hierarchy.getTokenInputs());
      walker.walk(cb, tree);
      _configuration = cb.getConfiguration();
    }
  }
}
