package org.batfish.vendor.fastpath.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.fastpath.representation.FastpathConfiguration;

/** Extracts a {@link FastpathConfiguration} from a corresponding parse tree. */
@ParametersAreNonnullByDefault
public final class FastpathControlPlaneExtractor implements ControlPlaneExtractor {

  public FastpathControlPlaneExtractor(
      String text,
      FastpathCombinedParser parser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _w = warnings;
    _configuration = new FastpathConfiguration();
    _silentSyntax = silentSyntax;
  }

  @Override
  public @Nonnull VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    // FastPath has no inheritance/preprocessing, so a single builder pass suffices.
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(
        new FastpathConfigurationBuilder(_parser, _text, _w, _configuration, _silentSyntax), tree);
  }

  private final @Nonnull FastpathConfiguration _configuration;
  private final @Nonnull FastpathCombinedParser _parser;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
}
