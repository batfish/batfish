package org.batfish.grammar.palo_alto;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE;

import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_hostnameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_ntp_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsd_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsn_ntp_server_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_line_config_devicesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sn_virtual_routerContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sni_ethernetContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snie_commentContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snie_link_statusContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_ipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_mtuContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvr_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvr_routing_tableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_admin_distContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_nexthopContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ssl_syslogContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ssls_serverContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sslss_serverContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Variable_list_itemContext;
import org.batfish.representation.palo_alto.Interface;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.batfish.representation.palo_alto.StaticRoute;
import org.batfish.representation.palo_alto.SyslogServer;
import org.batfish.representation.palo_alto.VirtualRouter;
import org.batfish.vendor.StructureType;

public class PaloAltoConfigurationBuilder extends PaloAltoParserBaseListener {
  private PaloAltoConfiguration _configuration;

  private String _currentDeviceName;

  private Interface _currentInterface;

  private boolean _currentNtpServerPrimary;

  private StaticRoute _currentStaticRoute;

  private SyslogServer _currentSyslogServer;

  private String _currentSyslogServerGroupName;

  private VirtualRouter _currentVirtualRouter;

  private PaloAltoCombinedParser _parser;

  private final String _text;

  private final Set<String> _unimplementedFeatures;

  private final boolean _unrecognizedAsRedFlag;

  private final Warnings _w;

  public PaloAltoConfigurationBuilder(
      PaloAltoCombinedParser parser,
      String text,
      Warnings warnings,
      Set<String> unimplementedFeatures,
      boolean unrecognizedAsRedFlag) {
    _configuration = new PaloAltoConfiguration(unimplementedFeatures);
    _parser = parser;
    _text = text;
    _unimplementedFeatures = unimplementedFeatures;
    _unrecognizedAsRedFlag = unrecognizedAsRedFlag;
    _w = warnings;
  }

  @SuppressWarnings("unused")
  private BatfishException convError(Class<?> type, ParserRuleContext ctx) {
    String typeName = type.getSimpleName();
    String txt = getFullText(ctx);
    return new BatfishException("Could not convert to " + typeName + ": " + txt);
  }

  /** Mark the specified structure as defined on each line in the supplied context */
  private void defineStructure(StructureType type, String name, RuleContext ctx) {
    /* Recursively process children to find all relevant definition lines for the specified context */
    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree child = ctx.getChild(i);
      if (child instanceof TerminalNode) {
        _configuration.defineStructure(type, name, getLine(((TerminalNode) child).getSymbol()));
      } else if (child instanceof RuleContext) {
        defineStructure(type, name, (RuleContext) child);
      }
    }
  }

  private String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  /** Return original line number for specified token */
  private int getLine(Token t) {
    return _parser.getLine(t);
  }

  /** Return token text with enclosing quotes removed, if applicable */
  private String getText(ParserRuleContext ctx) {
    return unquote(ctx.getText());
  }

  private String unquote(String text) {
    if (text.length() < 2) {
      return text;
    }
    char leading = text.charAt(0);
    char trailing = text.charAt(text.length() - 1);
    if (leading == '\'' || leading == '"') {
      if (leading == trailing) {
        return text.substring(1, text.length() - 1);
      } else {
        _w.redFlag("Improperly-quoted string: " + text);
      }
    }
    return text;
  }

  @Override
  public void enterPalo_alto_configuration(Palo_alto_configurationContext ctx) {
    _configuration = new PaloAltoConfiguration(_unimplementedFeatures);
  }

  @Override
  public void exitSds_hostname(Sds_hostnameContext ctx) {
    _configuration.setHostname(ctx.name.getText());
  }

  @Override
  public void enterSds_ntp_servers(Sds_ntp_serversContext ctx) {
    _currentNtpServerPrimary = ctx.PRIMARY_NTP_SERVER() != null;
  }

  @Override
  public void exitSdsn_ntp_server_address(Sdsn_ntp_server_addressContext ctx) {
    if (_currentNtpServerPrimary) {
      _configuration.setNtpServerPrimary(ctx.address.getText());
    } else {
      _configuration.setNtpServerSecondary(ctx.address.getText());
    }
  }

  @Override
  public void exitSdsd_servers(Sdsd_serversContext ctx) {
    if (ctx.primary_name != null) {
      _configuration.setDnsServerPrimary(ctx.primary_name.getText());
    } else if (ctx.secondary_name != null) {
      _configuration.setDnsServerSecondary(ctx.secondary_name.getText());
    }
  }

  @Override
  public void enterSet_line_config_devices(Set_line_config_devicesContext ctx) {
    if (ctx.name != null) {
      String deviceName = ctx.name.getText();
      _currentDeviceName = firstNonNull(_currentDeviceName, deviceName);
      if (!_currentDeviceName.equals(deviceName)) {
        /* Do not currently handle multiple device names, which presumably happens only if multiple
         * physical devices are configured from a single config */
        _w.redFlag("Multiple devices encountered: " + deviceName);
      }
    }
  }

  @Override
  public void enterSn_virtual_router(Sn_virtual_routerContext ctx) {
    _currentVirtualRouter =
        _configuration.getVirtualRouters().computeIfAbsent(ctx.name.getText(), VirtualRouter::new);
  }

  @Override
  public void exitSn_virtual_router(Sn_virtual_routerContext ctx) {
    _currentVirtualRouter = null;
  }

  @Override
  public void enterSni_ethernet(Sni_ethernetContext ctx) {
    String name = ctx.name.getText();
    _currentInterface = _configuration.getInterfaces().computeIfAbsent(name, Interface::new);
    defineStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSni_ethernet(Sni_ethernetContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitSnie_comment(Snie_commentContext ctx) {
    _currentInterface.setComment(getText(ctx.text));
  }

  @Override
  public void exitSnie_link_status(Snie_link_statusContext ctx) {
    _currentInterface.setActive((ctx.DOWN() == null));
  }

  @Override
  public void exitSniel3_ip(Sniel3_ipContext ctx) {
    InterfaceAddress address = new InterfaceAddress(ctx.address.getText());
    _currentInterface.setAddress(address);
    _currentInterface.getAllAddresses().add(address);
  }

  @Override
  public void exitSniel3_mtu(Sniel3_mtuContext ctx) {
    _currentInterface.setMtu(Integer.parseInt(ctx.mtu.getText()));
  }

  @Override
  public void enterSnvr_routing_table(Snvr_routing_tableContext ctx) {
    _currentStaticRoute =
        _currentVirtualRouter
            .getStaticRoutes()
            .computeIfAbsent(ctx.name.getText(), StaticRoute::new);
  }

  @Override
  public void exitSnvr_interface(Snvr_interfaceContext ctx) {
    for (Variable_list_itemContext var : ctx.variable_list().variable_list_item()) {
      String name = var.getText();
      _currentVirtualRouter.getInterfaceNames().add(name);
      _configuration.referenceStructure(
          INTERFACE, name, VIRTUAL_ROUTER_INTERFACE, getLine(var.start));
    }
  }

  @Override
  public void exitSnvr_routing_table(Snvr_routing_tableContext ctx) {
    _currentStaticRoute = null;
  }

  @Override
  public void exitSnvrrt_admin_dist(Snvrrt_admin_distContext ctx) {
    _currentStaticRoute.setAdminDistance(Integer.parseInt(ctx.distance.getText()));
  }

  @Override
  public void exitSnvrrt_destination(Snvrrt_destinationContext ctx) {
    _currentStaticRoute.setDestination(Prefix.parse(ctx.destination.getText()));
  }

  @Override
  public void exitSnvrrt_interface(Snvrrt_interfaceContext ctx) {
    _currentStaticRoute.setNextHopInterface(ctx.iface.getText());
  }

  @Override
  public void exitSnvrrt_metric(Snvrrt_metricContext ctx) {
    _currentStaticRoute.setMetric(Integer.parseInt(ctx.metric.getText()));
  }

  @Override
  public void exitSnvrrt_nexthop(Snvrrt_nexthopContext ctx) {
    _currentStaticRoute.setNextHopIp(new Ip(ctx.address.getText()));
  }

  @Override
  public void enterSsl_syslog(Ssl_syslogContext ctx) {
    _currentSyslogServerGroupName = getText(ctx.name);
  }

  @Override
  public void exitSsl_syslog(Ssl_syslogContext ctx) {
    _currentSyslogServerGroupName = null;
  }

  @Override
  public void enterSsls_server(Ssls_serverContext ctx) {
    _currentSyslogServer =
        _configuration.getSyslogServer(_currentSyslogServerGroupName, getText(ctx.name));
  }

  @Override
  public void exitSsls_server(Ssls_serverContext ctx) {
    _currentSyslogServer = null;
  }

  @Override
  public void exitSslss_server(Sslss_serverContext ctx) {
    _currentSyslogServer.setAddress(ctx.address.getText());
  }

  public PaloAltoConfiguration getConfiguration() {
    return _configuration;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    int line = getLine(token);
    String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
    if (_unrecognizedAsRedFlag) {
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
      _configuration.setUnrecognized(true);
    } else {
      _parser.getErrors().add(msg);
    }
  }
}
