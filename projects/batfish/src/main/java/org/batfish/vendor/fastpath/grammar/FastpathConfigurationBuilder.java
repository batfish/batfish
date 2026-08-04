package org.batfish.vendor.fastpath.grammar;

import static org.batfish.vendor.fastpath.grammar.FastpathLexer.WORD;

import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Double_quoted_stringContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Fastpath_configurationContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Host_valueContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.HostnameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ip_addressContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ip_domain_nameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ip_name_serverContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Logging_hostContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Quoted_textContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.S_hostnameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.S_set_promptContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Sntp_serverContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.WordContext;
import org.batfish.vendor.fastpath.representation.FastpathConfiguration;

/** Populates a {@link FastpathConfiguration} by walking a FastPath parse tree. */
public final class FastpathConfigurationBuilder extends FastpathParserBaseListener
    implements SilentSyntaxListener {

  public FastpathConfigurationBuilder(
      FastpathCombinedParser parser,
      String text,
      Warnings warnings,
      FastpathConfiguration configuration,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _c = configuration;
    _silentSyntax = silentSyntax;
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _c.setHostname(toString(ctx.hostname()));
  }

  @Override
  public void exitS_set_prompt(S_set_promptContext ctx) {
    _c.setHostname(toString(ctx.hostname()));
  }

  @Override
  public void exitIp_name_server(Ip_name_serverContext ctx) {
    ctx.ip_address().forEach(addr -> _c.addDnsServer(toString(addr)));
  }

  @Override
  public void exitIp_domain_name(Ip_domain_nameContext ctx) {
    _c.setDomainName(toString(ctx.domain_name().double_quoted_string().text));
  }

  @Override
  public void exitSntp_server(Sntp_serverContext ctx) {
    _c.addNtpServer(toString(ctx.host_value()));
  }

  @Override
  public void exitLogging_host(Logging_hostContext ctx) {
    _c.addLoggingServer(toString(ctx.host_value()));
  }

  private static @Nonnull String toString(Host_valueContext ctx) {
    if (ctx.double_quoted_string() != null) {
      return toString(ctx.double_quoted_string().text);
    }
    return toString(ctx.ip_address());
  }

  private static @Nonnull String toString(Ip_addressContext ctx) {
    return ctx.getText();
  }

  private static @Nonnull String toString(HostnameContext ctx) {
    return toString(ctx.word());
  }

  private static @Nonnull String toString(WordContext ctx) {
    return ctx.word_content().children.stream()
        .map(
            child -> {
              if (child instanceof Double_quoted_stringContext) {
                return toString(((Double_quoted_stringContext) child).text);
              } else {
                assert child instanceof TerminalNode;
                int type = ((TerminalNode) child).getSymbol().getType();
                assert type == WORD;
                return child.getText();
              }
            })
        .collect(Collectors.joining(""));
  }

  private static @Nonnull String toString(@Nullable Quoted_textContext text) {
    if (text == null) {
      return "";
    }
    // The device removes backslashes from quoted strings.
    return text.getText().replaceAll("\\\\", "");
  }

  @Override
  public void exitFastpath_configuration(Fastpath_configurationContext ctx) {
    _c.finalizeStructures();
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _c.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      _w.redFlagf(
          "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
          line, lineText);
    }
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
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Override
  public @Nonnull Warnings getWarnings() {
    return _w;
  }

  private final @Nonnull FastpathConfiguration _c;

  private final @Nonnull FastpathCombinedParser _parser;

  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  private final @Nonnull String _text;

  private final @Nonnull Warnings _w;
}
