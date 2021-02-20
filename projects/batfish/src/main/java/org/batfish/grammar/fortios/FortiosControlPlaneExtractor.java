package org.batfish.grammar.fortios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.fortios.FortiosConfiguration;
import org.batfish.vendor.VendorConfiguration;

/**
 * Extracts a {@link FortiosConfiguration} from a corresponding parse tree.
 *
 * <p>First, preprocess the parse tree with {@link FortiosPreprocessor} to get final config lines.
 * Then, build the configuration with {@link FortiosConfigurationBuilder} taking those properties
 * into account.
 */
public final class FortiosControlPlaneExtractor implements ControlPlaneExtractor {

  public FortiosControlPlaneExtractor(
      String text, FortiosCombinedParser parser, Warnings warnings) {
    _text = text;
    _parser = parser;
    _w = warnings;
    _configuration = new FortiosConfiguration();
  }

  @Override
  public @Nullable VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    // extract metadata and set defaults
    walker.walk(new FortiosPreprocessor(_parser, _w), tree);
    // build the configuration
    walker.walk(new FortiosConfigurationBuilder(_text, _parser, _w, _configuration), tree);
  }

  private final @Nonnull FortiosConfiguration _configuration;
  private final @Nonnull FortiosCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
}
