package org.batfish.vendor.check_point_gateway.grammar;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Check_point_gateway_configurationContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.HostnameContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.S_hostnameContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.WordContext;
import org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration;

@ParametersAreNonnullByDefault
public class CheckPointGatewayConfigurationBuilder extends CheckPointGatewayParserBaseListener
    implements SilentSyntaxListener {

  public CheckPointGatewayConfigurationBuilder(
      CheckPointGatewayCombinedParser parser,
      String text,
      Warnings warnings,
      CheckPointGatewayConfiguration configuration,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _configuration = configuration;
    _silentSyntax = silentSyntax;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _configuration.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      _w.redFlag(
          String.format(
              "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
              line, lineText));
    }
  }

  @Nonnull
  public CheckPointGatewayConfiguration getConfiguration() {
    return _configuration;
  }

  @Nonnull
  @Override
  public String getInputText() {
    return _text;
  }

  @Nonnull
  @Override
  public BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Nonnull
  @Override
  public SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Nonnull
  @Override
  public Warnings getWarnings() {
    return _w;
  }

  @Override
  public void enterCheck_point_gateway_configuration(
      Check_point_gateway_configurationContext ctx) {}

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    toString(ctx, ctx.hostname()).ifPresent(_configuration::setHostname);
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, HostnameContext ctx) {
    return toString(messageCtx, ctx.word(), "device hostname", DEVICE_HOSTNAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Pattern pattern) {
    return toString(messageCtx, ctx, type, s -> pattern.matcher(s).matches());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Predicate<String> predicate) {
    String text = toString(ctx);
    if (!predicate.test(text)) {
      warn(messageCtx, String.format("Illegal value for %s", type));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private static @Nonnull String toString(WordContext ctx) {
    return ctx.WORD().getText();
  }

  private static final Pattern DEVICE_HOSTNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

  @Nonnull private CheckPointGatewayConfiguration _configuration;

  @Nonnull private CheckPointGatewayCombinedParser _parser;

  @Nonnull private final String _text;

  @Nonnull private final Warnings _w;

  @Nonnull private final SilentSyntaxCollection _silentSyntax;
}
