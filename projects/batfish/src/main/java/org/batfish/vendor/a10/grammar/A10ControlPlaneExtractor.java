package org.batfish.vendor.a10.grammar;

import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.a10.representation.A10Configuration;

/** Extracts a {@link A10Configuration} from a corresponding parse tree. */
public class A10ControlPlaneExtractor extends A10ParserBaseListener
    implements ControlPlaneExtractor {

  public A10ControlPlaneExtractor(
      String text,
      A10CombinedParser parser,
      Warnings warnings,
      @Nonnull SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _w = warnings;
    _configuration = new A10Configuration();
    _silentSyntax = silentSyntax;
  }

  @Nonnull
  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    // extract preprocessing info/metadata from configuration
    walker.walk(new A10Preprocessor(_configuration), tree);
    // build the configuration
    walker.walk(
        new A10ConfigurationBuilder(_parser, _text, _w, _configuration, _silentSyntax), tree);
  }

  @Nonnull private A10Configuration _configuration;

  @Nonnull private final A10CombinedParser _parser;

  @Nonnull private final SilentSyntaxCollection _silentSyntax;

  @Nonnull private final String _text;

  @Nonnull private final Warnings _w;
}
