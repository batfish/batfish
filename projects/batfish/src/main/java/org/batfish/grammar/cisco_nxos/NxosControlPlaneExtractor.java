package org.batfish.grammar.cisco_nxos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.cisco_nxos.CiscoNxosConfiguration;
import org.batfish.vendor.VendorConfiguration;

/**
 * Extracts a {@link CiscoNxosConfiguration} from a corresponding parse tree.
 *
 * <p>First, preprocess the parse tree with {@link CiscoNxosPreprocessor} to determine platform,
 * version, and some default values. Then, build the configuration with {@link
 * CiscoNxosControlPlaneExtractor} taking those properties into account.
 */
@ParametersAreNonnullByDefault
public final class NxosControlPlaneExtractor implements ControlPlaneExtractor {

  public NxosControlPlaneExtractor(String text, CiscoNxosCombinedParser parser, Warnings warnings) {
    _text = text;
    _parser = parser;
    _w = warnings;
    _configuration = new CiscoNxosConfiguration();
  }

  @Override
  public @Nullable VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    // extract metadata and set defaults
    walker.walk(new CiscoNxosPreprocessor(_text, _parser, _w, _configuration), tree);
    // build the configuration
    walker.walk(new CiscoNxosControlPlaneExtractor(_text, _parser, _w, _configuration), tree);
  }

  private final @Nonnull CiscoNxosConfiguration _configuration;
  private final @Nonnull CiscoNxosCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
}
