package org.batfish.vendor.check_point_gateway.grammar;

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
import org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration;

@ParametersAreNonnullByDefault
public class CheckPointGatewayControlPlaneExtractor extends CheckPointGatewayParserBaseListener
    implements ControlPlaneExtractor {

  public CheckPointGatewayControlPlaneExtractor(
      String text,
      CheckPointGatewayCombinedParser parser,
      Warnings warnings,
      @Nonnull SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _w = warnings;
    _configuration = new CheckPointGatewayConfiguration();
    _silentSyntax = silentSyntax;
  }

  @Override
  public @Nonnull VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    // build the configuration
    walker.walk(
        new CheckPointGatewayConfigurationBuilder(
            _parser, _text, _w, _configuration, _silentSyntax),
        tree);
    _configuration.finalizeStructures();
  }

  private @Nonnull CheckPointGatewayConfiguration _configuration;

  private final @Nonnull CheckPointGatewayCombinedParser _parser;

  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  private final @Nonnull String _text;

  private final @Nonnull Warnings _w;
}
