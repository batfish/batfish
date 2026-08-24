package org.batfish.vendor.fastpath.grammar;

import com.google.common.collect.Range;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Host_valueContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.HostnameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Interface_nameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ip_addressContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ipd_lookupContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ipd_nameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ipn_serverContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ipn_source_interfaceContext;
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
import org.batfish.vendor.fastpath.grammar.FastpathParser.Noipd_lookupContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Nol_consoleContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Quoted_textContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.S_hostnameContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Set_promptContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Sntp_client_modeContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Sntp_source_interfaceContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Sntpc_modeContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Sntpc_portContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ss_hostContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.WordContext;
import org.batfish.vendor.fastpath.representation.FastpathConfiguration;
import org.batfish.vendor.fastpath.representation.LoggingBuffered;
import org.batfish.vendor.fastpath.representation.LoggingServer;
import org.batfish.vendor.fastpath.representation.Sntp;

/** Populates a {@link FastpathConfiguration} by walking a FastPath parse tree. */
public final class FastpathConfigurationBuilder extends FastpathParserBaseListener
    implements SilentSyntaxListener {

  /** Valid range for a logging severity level (emergency=0 ... debug=7). */
  private static final IntegerSpace SEVERITY_RANGE = IntegerSpace.of(Range.closed(0, 7));

  /** Valid range for a logging destination UDP port. */
  private static final IntegerSpace LOGGING_PORT_RANGE = IntegerSpace.of(Range.closed(1, 65535));

  /** Valid range for the SNTP client source port ({@code sntp client port}). */
  private static final IntegerSpace SNTP_CLIENT_PORT_RANGE =
      IntegerSpace.of(Range.closed(1, 65535));

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
  public void exitSet_prompt(Set_promptContext ctx) {
    _c.setHostname(toString(ctx.hostname()));
  }

  @Override
  public void exitIpn_server(Ipn_serverContext ctx) {
    ctx.ip_address().forEach(addr -> _c.getDns().addServer(toString(addr)));
  }

  @Override
  public void exitIpn_source_interface(Ipn_source_interfaceContext ctx) {
    _c.getDns().setSourceInterface(toInterfaceName(ctx.iface));
  }

  @Override
  public void exitIpd_name(Ipd_nameContext ctx) {
    _c.getDns().setDomainName(toString(ctx.domain_name().word()));
  }

  @Override
  public void exitIpd_lookup(Ipd_lookupContext ctx) {
    _c.getDns().setLookupEnabled(true);
  }

  @Override
  public void exitNoipd_lookup(Noipd_lookupContext ctx) {
    _c.getDns().setLookupEnabled(false);
  }

  @Override
  public void exitSs_host(Ss_hostContext ctx) {
    _c.getSntp().addServer(toString(ctx.host_value()));
  }

  @Override
  public void exitSntp_source_interface(Sntp_source_interfaceContext ctx) {
    _c.getSntp().setSourceInterface(toInterfaceName(ctx.iface));
  }

  @Override
  public void exitSntpc_mode(Sntpc_modeContext ctx) {
    if (ctx.sntp_client_mode() != null) {
      _c.getSntp().setClientMode(toClientMode(ctx.sntp_client_mode()));
    }
  }

  @Override
  public void exitSntpc_port(Sntpc_portContext ctx) {
    toIntegerInSpace(ctx, ctx.port, SNTP_CLIENT_PORT_RANGE, "sntp client port")
        .ifPresent(_c.getSntp()::setClientPort);
  }

  private static Sntp.ClientMode toClientMode(Sntp_client_modeContext ctx) {
    if (ctx.BROADCAST() != null) {
      return Sntp.ClientMode.BROADCAST;
    }
    assert ctx.UNICAST() != null;
    return Sntp.ClientMode.UNICAST;
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
  public void exitNol_console(Nol_consoleContext ctx) {
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

  private static @Nonnull String toInterfaceName(Interface_nameContext ctx) {
    // TODO: once FastPath models interfaces, track this as a reference to an interface structure
    // (for undefined-reference detection) and reconcile the name format across the branches below.
    if (ctx.LOOPBACK() != null) {
      return "loopback " + ctx.uint16().getText();
    } else if (ctx.TUNNEL() != null) {
      return "tunnel " + ctx.uint16().getText();
    } else if (ctx.VLAN() != null) {
      return "vlan " + ctx.uint16().getText();
    } else if (ctx.SERVICEPORT() != null) {
      return "serviceport";
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
    if (ctx.ip_address() != null) {
      return toString(ctx.ip_address());
    }
    return toString(ctx.word());
  }

  private static @Nonnull String toString(Ip_addressContext ctx) {
    return ctx.getText();
  }

  private static @Nonnull String toString(HostnameContext ctx) {
    return toString(ctx.word());
  }

  private static @Nonnull String toString(WordContext ctx) {
    if (ctx.double_quoted_string() != null) {
      return toString(ctx.double_quoted_string().text);
    }
    return ctx.WORD().getText();
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
