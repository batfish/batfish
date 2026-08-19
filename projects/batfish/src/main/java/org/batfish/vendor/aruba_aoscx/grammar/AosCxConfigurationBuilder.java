package org.batfish.vendor.aruba_aoscx.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_hostnameContext;
import org.batfish.vendor.aruba_aoscx.representation.AosCxConfiguration;

@ParametersAreNonnullByDefault
public final class AosCxConfigurationBuilder extends AosCxParserBaseListener
    implements SilentSyntaxListener {

  public AosCxConfigurationBuilder(
      AosCxCombinedParser parser,
      String text,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _silentSyntax = silentSyntax;
    _configuration = new AosCxConfiguration();
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _configuration.setHostname(ctx.WORD().getText());
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Override
  public @Nonnull String getInputText() {
    return _text;
  }

  @Override
  public @Nonnull BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public @Nonnull Warnings getWarnings() {
    return _w;
  }

  public @Nonnull AosCxConfiguration getConfiguration() {
    return _configuration;
  }

  private final @Nonnull AosCxConfiguration _configuration;
  private final @Nonnull AosCxCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
}
