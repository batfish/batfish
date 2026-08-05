package org.batfish.vendor.fastpath.grammar;

import static org.batfish.vendor.fastpath.grammar.FastpathLexer.WORD;

import com.google.common.collect.Range;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Double_quoted_stringContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Host_valueContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.HostnameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ip_addressContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ip_domain_nameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ip_name_serverContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Lb_enableContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Lb_wrapContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Lh_serverContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Logging_addr_typeContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Logging_cli_commandContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Logging_consoleContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Logging_persistentContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Logging_severityContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Logging_severity_keywordContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ls_enableContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ls_source_interfaceContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Nl_consoleContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Quoted_textContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.S_hostnameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.S_set_promptContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Sntp_serverContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Source_interfaceContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.WordContext;
import org.batfish.vendor.fastpath.representation.FastpathConfiguration;
import org.batfish.vendor.fastpath.representation.LoggingBuffered;
import org.batfish.vendor.fastpath.representation.LoggingServer;

/** Populates a {@link FastpathConfiguration} by walking a FastPath parse tree. */
public final class FastpathConfigurationBuilder extends FastpathParserBaseListener
    implements SilentSyntaxListener {

  /** Valid range for a logging severity level (emergency=0 ... debug=7). */
  private static final IntegerSpace SEVERITY_RANGE = IntegerSpace.of(Range.closed(0, 7));

  /** Valid range for a logging destination UDP port. */
  private static final IntegerSpace LOGGING_PORT_RANGE = IntegerSpace.of(Range.closed(1, 65535));

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
  public void exitLh_server(Lh_serverContext ctx) {
    String host = toString(ctx.host_value());
    LoggingServer server = _c.getLogging().getServers().computeIfAbsent(host, LoggingServer::new);
    if (ctx.logging_addr_type() != null) {
      server.setAddressType(toAddressType(ctx.logging_addr_type()));
    }
    if (ctx.port != null) {
      toIntegerInSpace(ctx, ctx.port, LOGGING_PORT_RANGE, "logging host port")
          .ifPresent(server::setPort);
    }
    if (ctx.severity != null) {
      toSeverity(ctx, ctx.severity).ifPresent(server::setSeverityLevel);
    }
  }

  @Override
  public void exitLb_enable(Lb_enableContext ctx) {
    LoggingBuffered buffered = _c.getLogging().getOrCreateBuffered();
    buffered.setEnabled(true);
    if (ctx.severity != null) {
      toSeverity(ctx, ctx.severity).ifPresent(buffered::setSeverity);
    }
  }

  @Override
  public void exitLb_wrap(Lb_wrapContext ctx) {
    _c.getLogging().getOrCreateBuffered().setWrap(true);
  }

  @Override
  public void exitLogging_cli_command(Logging_cli_commandContext ctx) {
    _c.getLogging().setCliCommand(true);
  }

  @Override
  public void exitLogging_console(Logging_consoleContext ctx) {
    _c.getLogging().setConsoleEnabled(true);
    if (ctx.severity != null) {
      toSeverity(ctx, ctx.severity).ifPresent(_c.getLogging()::setConsoleSeverity);
    }
  }

  @Override
  public void exitNl_console(Nl_consoleContext ctx) {
    _c.getLogging().setConsoleEnabled(false);
  }

  @Override
  public void exitLogging_persistent(Logging_persistentContext ctx) {
    toSeverity(ctx, ctx.severity).ifPresent(_c.getLogging()::setPersistentSeverity);
  }

  @Override
  public void exitLs_enable(Ls_enableContext ctx) {
    _c.getLogging().setSyslogEnabled(true);
  }

  @Override
  public void exitLs_source_interface(Ls_source_interfaceContext ctx) {
    _c.getLogging().setSourceInterface(toInterfaceName(ctx.iface));
  }

  private static @Nonnull String toInterfaceName(Source_interfaceContext ctx) {
    // TODO: once FastPath models interfaces, track this as a reference to an interface structure
    // (for undefined-reference detection) and reconcile the name format across the branches below.
    if (ctx.LOOPBACK() != null) {
      return "loopback " + ctx.uint16().getText();
    } else if (ctx.TUNNEL() != null) {
      return "tunnel " + ctx.uint16().getText();
    } else if (ctx.VLAN() != null) {
      return "vlan " + ctx.uint16().getText();
    }
    assert ctx.interface_slot_port() != null;
    // Physical interface, e.g. "0/1" or "1/0/1".
    return ctx.interface_slot_port().getText();
  }

  private static @Nonnull LoggingServer.AddressType toAddressType(Logging_addr_typeContext ctx) {
    if (ctx.DNS() != null) {
      return LoggingServer.AddressType.DNS;
    } else if (ctx.IPV6() != null) {
      return LoggingServer.AddressType.IPV6;
    }
    assert ctx.IPV4() != null;
    return LoggingServer.AddressType.IPV4;
  }

  private @Nonnull Optional<Integer> toSeverity(
      ParserRuleContext messageCtx, Logging_severityContext ctx) {
    if (ctx.logging_severity_keyword() != null) {
      return Optional.of(toSeverity(ctx.logging_severity_keyword()));
    }
    // Numeric severity.
    return toIntegerInSpace(messageCtx, ctx, SEVERITY_RANGE, "logging severity");
  }

  /**
   * Convert the text of {@code ctx} to an integer if it falls within {@code space}, otherwise warn
   * (attributing the warning to {@code messageCtx}) and return {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace space, String name) {
    int num = Integer.parseInt(ctx.getText());
    if (!space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private static int toSeverity(Logging_severity_keywordContext ctx) {
    if (ctx.EMERGENCY() != null) {
      return 0;
    } else if (ctx.ALERT() != null) {
      return 1;
    } else if (ctx.CRITICAL() != null) {
      return 2;
    } else if (ctx.ERROR() != null) {
      return 3;
    } else if (ctx.WARNING() != null) {
      return 4;
    } else if (ctx.NOTICE() != null) {
      return 5;
    } else if (ctx.INFO() != null) {
      return 6;
    }
    assert ctx.DEBUG() != null;
    return 7;
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
