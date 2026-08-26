package org.batfish.vendor.fastpath.grammar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
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
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_accounting_commandsContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_accounting_dot1xContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_accounting_execContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_accounting_methodContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_accounting_recordContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_authentication_enableContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_authentication_loginContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_authentication_methodContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_authorization_commandsContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_authorization_execContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_authorization_methodContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_dot1x_accounting_recordContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Aaa_list_nameContext;
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
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ts_hostContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ts_keyContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ts_source_interfaceContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Ts_timeoutContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Tsh_keyContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Tsh_portContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Tsh_priorityContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.Tsh_timeoutContext;
import org.batfish.vendor.fastpath.grammar.FastpathParser.WordContext;
import org.batfish.vendor.fastpath.representation.AaaMethod;
import org.batfish.vendor.fastpath.representation.Accounting;
import org.batfish.vendor.fastpath.representation.AccountingType;
import org.batfish.vendor.fastpath.representation.AuthenticationType;
import org.batfish.vendor.fastpath.representation.AuthorizationType;
import org.batfish.vendor.fastpath.representation.FastpathConfiguration;
import org.batfish.vendor.fastpath.representation.LoggingBuffered;
import org.batfish.vendor.fastpath.representation.LoggingServer;
import org.batfish.vendor.fastpath.representation.Sntp;
import org.batfish.vendor.fastpath.representation.TacacsServer;

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

  private static final IntegerSpace TACACS_TIMEOUT_RANGE = IntegerSpace.of(Range.closed(1, 30));

  /**
   * Name recorded for an AAA method list declared with the {@code default} keyword rather than a
   * user-specified name.
   */
  private static final String DEFAULT_LIST_NAME = "default";

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

  private final @Nonnull FastpathConfiguration _c;
  private final @Nonnull FastpathCombinedParser _parser;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;

  private @Nullable TacacsServer _currentTacacsServer;

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
  public void enterTs_host(Ts_hostContext ctx) {
    _currentTacacsServer =
        _c.getTacacs().getServers().computeIfAbsent(toString(ctx.host_value()), TacacsServer::new);
  }

  @Override
  public void exitTs_host(Ts_hostContext ctx) {
    _currentTacacsServer = null;
  }

  @Override
  public void exitTsh_port(Tsh_portContext ctx) {
    // grammar enforces range (0-65535), so no toIntegerInSpace range check needed
    _currentTacacsServer.setPort(Integer.parseInt(ctx.port.getText()));
  }

  @Override
  public void exitTsh_priority(Tsh_priorityContext ctx) {
    // grammar enforces range (0-65535), so no toIntegerInSpace range check needed
    _currentTacacsServer.setPriority(Integer.parseInt(ctx.priority.getText()));
  }

  @Override
  public void exitTsh_timeout(Tsh_timeoutContext ctx) {
    toIntegerInSpace(ctx, ctx.timeout, TACACS_TIMEOUT_RANGE, "tacacs-server host timeout")
        .ifPresent(_currentTacacsServer::setTimeout);
  }

  @Override
  public void exitTsh_key(Tsh_keyContext ctx) {
    _currentTacacsServer.setKeyEncrypted(ctx.ENCRYPTED() != null);
  }

  @Override
  public void exitTs_key(Ts_keyContext ctx) {
    _c.getTacacs().setKeyEncrypted(ctx.ENCRYPTED() != null);
  }

  @Override
  public void exitTs_timeout(Ts_timeoutContext ctx) {
    toIntegerInSpace(ctx, ctx.timeout, TACACS_TIMEOUT_RANGE, "tacacs-server timeout")
        .ifPresent(_c.getTacacs()::setTimeout);
  }

  @Override
  public void exitTs_source_interface(Ts_source_interfaceContext ctx) {
    _c.getTacacs().setSourceInterface(toInterfaceName(ctx.iface));
  }

  @Override
  public void exitAaa_authentication_login(Aaa_authentication_loginContext ctx) {
    _c.getAaa()
        .defineAuthentication(
            AuthenticationType.LOGIN,
            toString(ctx.name),
            toMethods(ctx.aaa_authentication_method()));
  }

  @Override
  public void exitAaa_authentication_enable(Aaa_authentication_enableContext ctx) {
    _c.getAaa()
        .defineAuthentication(
            AuthenticationType.ENABLE,
            toString(ctx.name),
            toMethods(ctx.aaa_authentication_method()));
  }

  @Override
  public void exitAaa_authorization_commands(Aaa_authorization_commandsContext ctx) {
    _c.getAaa()
        .defineAuthorization(
            AuthorizationType.COMMANDS,
            toString(ctx.name),
            toAuthorizationMethods(ctx.aaa_authorization_method()));
  }

  @Override
  public void exitAaa_authorization_exec(Aaa_authorization_execContext ctx) {
    _c.getAaa()
        .defineAuthorization(
            AuthorizationType.EXEC,
            toString(ctx.name),
            toAuthorizationMethods(ctx.aaa_authorization_method()));
  }

  @Override
  public void exitAaa_accounting_exec(Aaa_accounting_execContext ctx) {
    _c.getAaa()
        .defineAccounting(
            AccountingType.EXEC,
            toString(ctx.name),
            toAccountingRecordType(ctx.record),
            toAccountingMethods(ctx.aaa_accounting_method()));
  }

  @Override
  public void exitAaa_accounting_commands(Aaa_accounting_commandsContext ctx) {
    _c.getAaa()
        .defineAccounting(
            AccountingType.COMMANDS,
            toString(ctx.name),
            toAccountingRecordType(ctx.record),
            toAccountingMethods(ctx.aaa_accounting_method()));
  }

  @Override
  public void exitAaa_accounting_dot1x(Aaa_accounting_dot1xContext ctx) {
    // The grammar hardcodes the `default` list, the only one dot1x accounting supports, and radius
    // is the only method it accepts.
    _c.getAaa()
        .defineAccounting(
            AccountingType.DOT1X,
            DEFAULT_LIST_NAME,
            toAccountingRecordType(ctx.record),
            ctx.RADIUS() != null ? ImmutableList.of(AaaMethod.RADIUS) : ImmutableList.of());
  }

  private static @Nonnull List<AaaMethod> toMethods(List<Aaa_authentication_methodContext> ctxs) {
    return ctxs.stream()
        .map(FastpathConfigurationBuilder::toMethod)
        .collect(ImmutableList.toImmutableList());
  }

  private static @Nonnull List<AaaMethod> toAuthorizationMethods(
      List<Aaa_authorization_methodContext> ctxs) {
    return ctxs.stream()
        .map(FastpathConfigurationBuilder::toMethod)
        .collect(ImmutableList.toImmutableList());
  }

  private static @Nonnull List<AaaMethod> toAccountingMethods(
      List<Aaa_accounting_methodContext> ctxs) {
    return ctxs.stream()
        .map(FastpathConfigurationBuilder::toMethod)
        .collect(ImmutableList.toImmutableList());
  }

  private static @Nonnull AaaMethod toMethod(Aaa_authentication_methodContext ctx) {
    if (ctx.DENY() != null) {
      return AaaMethod.DENY;
    } else if (ctx.ENABLE() != null) {
      return AaaMethod.ENABLE;
    } else if (ctx.LINE() != null) {
      return AaaMethod.LINE;
    } else if (ctx.LOCAL() != null) {
      return AaaMethod.LOCAL;
    } else if (ctx.NONE() != null) {
      return AaaMethod.NONE;
    } else if (ctx.RADIUS() != null) {
      return AaaMethod.RADIUS;
    }
    assert ctx.TACACS() != null;
    return AaaMethod.TACACS;
  }

  private static @Nonnull AaaMethod toMethod(Aaa_authorization_methodContext ctx) {
    if (ctx.LOCAL() != null) {
      return AaaMethod.LOCAL;
    } else if (ctx.NONE() != null) {
      return AaaMethod.NONE;
    } else if (ctx.RADIUS() != null) {
      return AaaMethod.RADIUS;
    }
    assert ctx.TACACS() != null;
    return AaaMethod.TACACS;
  }

  private static @Nonnull AaaMethod toMethod(Aaa_accounting_methodContext ctx) {
    if (ctx.RADIUS() != null) {
      return AaaMethod.RADIUS;
    }
    assert ctx.TACACS() != null;
    return AaaMethod.TACACS;
  }

  private static @Nonnull Accounting.RecordType toAccountingRecordType(
      Aaa_accounting_recordContext ctx) {
    if (ctx.START_STOP() != null) {
      return Accounting.RecordType.START_STOP;
    } else if (ctx.STOP_ONLY() != null) {
      return Accounting.RecordType.STOP_ONLY;
    }
    assert ctx.NONE() != null;
    return Accounting.RecordType.NONE;
  }

  private static @Nonnull Accounting.RecordType toAccountingRecordType(
      Aaa_dot1x_accounting_recordContext ctx) {
    if (ctx.START_STOP() != null) {
      return Accounting.RecordType.START_STOP;
    }
    assert ctx.NONE() != null;
    return Accounting.RecordType.NONE;
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

  /**
   * Returns the name of an AAA method list. The {@code default} keyword is recorded as the literal
   * name {@link #DEFAULT_LIST_NAME}; see {@code aaa_list_name} in the parser grammar.
   */
  private static @Nonnull String toString(Aaa_list_nameContext ctx) {
    if (ctx.DEFAULT() != null) {
      return DEFAULT_LIST_NAME;
    }
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
}
