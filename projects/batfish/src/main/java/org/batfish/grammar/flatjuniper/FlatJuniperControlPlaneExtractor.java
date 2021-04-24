package org.batfish.grammar.flatjuniper;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.SilentSyntax;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class FlatJuniperControlPlaneExtractor implements ControlPlaneExtractor {

  private JuniperConfiguration _configuration;
  private final FlatJuniperCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  @Nonnull private final SilentSyntax _silentSyntax;

  @Override
  public Set<String> implementedRuleNames() {
    // TODO: do we need to add things like InsertDeleteApplicator? I think not, since those don't
    // make it to the final output.
    return ImplementedRules.getImplementedRules(ConfigurationBuilder.class);
  }

  public FlatJuniperControlPlaneExtractor(
      String fileText,
      FlatJuniperCombinedParser combinedParser,
      Warnings warnings,
      SilentSyntax silentSyntax) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
    _silentSyntax = silentSyntax;
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
          new ConfigurationBuilder(_parser, _text, _w, hierarchy.getTokenInputs(), _silentSyntax);
      new BatfishParseTreeWalker(_parser).walk(cb, tree);
      _configuration = cb.getConfiguration();
    } finally {
      span.finish();
    }
  }
}
