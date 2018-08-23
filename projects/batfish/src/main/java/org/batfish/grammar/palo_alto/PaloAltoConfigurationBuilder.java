package org.batfish.grammar.palo_alto;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.CATCHALL_APPLICATION_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.CATCHALL_SERVICE_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.CATCHALL_ZONE_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.DEFAULT_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.SHARED_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeObjectName;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.RULE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE_OR_SERVICE_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.RULEBASE_SERVICE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.RULE_FROM_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.RULE_SELF_REF;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.RULE_TO_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SERVICE_GROUP_MEMBER;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.ZONE_INTERFACE;

import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_serviceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_service_groupContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_sharedContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_vsysContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_zoneContext;
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
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_unitsContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3u_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvr_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvr_routing_tableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_admin_distContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snvrrt_nexthopContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sr_securityContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Src_or_dst_list_itemContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_actionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_applicationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_disabledContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_fromContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_serviceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_sourceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_toContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_protocolContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_source_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sservgrp_membersContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ssl_syslogContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ssls_serverContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sslss_serverContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Szn_layer3Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Variable_list_itemContext;
import org.batfish.representation.palo_alto.Interface;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.batfish.representation.palo_alto.PaloAltoStructureType;
import org.batfish.representation.palo_alto.Rule;
import org.batfish.representation.palo_alto.Service;
import org.batfish.representation.palo_alto.ServiceGroup;
import org.batfish.representation.palo_alto.ServiceOrServiceGroupReference;
import org.batfish.representation.palo_alto.StaticRoute;
import org.batfish.representation.palo_alto.SyslogServer;
import org.batfish.representation.palo_alto.VirtualRouter;
import org.batfish.representation.palo_alto.Vsys;
import org.batfish.representation.palo_alto.Zone;
import org.batfish.vendor.StructureType;

public class PaloAltoConfigurationBuilder extends PaloAltoParserBaseListener {
  private PaloAltoConfiguration _configuration;

  private String _currentDeviceName;

  private Interface _currentInterface;

  private boolean _currentNtpServerPrimary;

  private Interface _currentParentInterface;

  private Rule _currentRule;

  private Service _currentService;

  private ServiceGroup _currentServiceGroup;

  private StaticRoute _currentStaticRoute;

  private SyslogServer _currentSyslogServer;

  private String _currentSyslogServerGroupName;

  private VirtualRouter _currentVirtualRouter;

  private Vsys _currentVsys;

  private Zone _currentZone;

  private Vsys _defaultVsys;

  private PaloAltoCombinedParser _parser;

  private final String _text;

  private final Warnings _w;

  public PaloAltoConfigurationBuilder(
      PaloAltoCombinedParser parser, String text, Warnings warnings) {
    _configuration = new PaloAltoConfiguration();
    _parser = parser;
    _text = text;
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

  private static int toInteger(Token t) {
    return Integer.parseInt(t.getText());
  }

  /** Convert source or destination list item into an appropriate IpSpace */
  private @Nullable IpSpace toIpSpace(Src_or_dst_list_itemContext ctx) {
    if (ctx.ANY() != null) {
      return UniverseIpSpace.INSTANCE;
    } else if (ctx.IP_ADDRESS() != null) {
      return new Ip(ctx.IP_ADDRESS().getText()).toIpSpace();
    } else if (ctx.IP_PREFIX() != null) {
      return Prefix.parse(ctx.IP_PREFIX().getText()).toIpSpace();
    }
    _w.redFlag("Unhandled source/destination item conversion: " + getFullText(ctx));
    return null;
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
    _configuration = new PaloAltoConfiguration();
    _configuration.getVirtualSystems().computeIfAbsent(SHARED_VSYS_NAME, Vsys::new);
    _defaultVsys = _configuration.getVirtualSystems().computeIfAbsent(DEFAULT_VSYS_NAME, Vsys::new);
    _currentVsys = _defaultVsys;
  }

  @Override
  public void exitPalo_alto_configuration(Palo_alto_configurationContext ctx) {
    // Assign the appropriate zone to each interface
    for (Vsys vsys : _configuration.getVirtualSystems().values()) {
      for (Zone zone : vsys.getZones().values()) {
        for (String ifname : zone.getInterfaceNames()) {
          Interface iface = _configuration.getInterfaces().get(ifname);
          if (iface != null) {
            iface.setZone(zone);
          }
        }
      }
    }
  }

  @Override
  public void enterS_zone(S_zoneContext ctx) {
    String name = ctx.name.getText();
    _currentZone = _currentVsys.getZones().computeIfAbsent(name, n -> new Zone(n, _currentVsys));

    // Use constructed zone name so same-named zone defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys.getName(), name);
    defineStructure(ZONE, uniqueName, ctx);
  }

  @Override
  public void exitS_zone(S_zoneContext ctx) {
    _currentZone = null;
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
    _currentParentInterface = _configuration.getInterfaces().computeIfAbsent(name, Interface::new);
    _currentInterface = _currentParentInterface;
    defineStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSni_ethernet(Sni_ethernetContext ctx) {
    _currentParentInterface = null;
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
  public void enterSniel3_units(Sniel3_unitsContext ctx) {
    String name = ctx.name.getText();
    _currentInterface = _configuration.getInterfaces().computeIfAbsent(name, Interface::new);
    _currentInterface.setParent(_currentParentInterface);
    _currentParentInterface.getUnits().put(name, _currentInterface);
    defineStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSniel3u_tag(Sniel3u_tagContext ctx) {
    _currentInterface.setTag(toInteger(ctx.tag));
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
  public void enterSr_security(Sr_securityContext ctx) {
    String name = ctx.name.getText();
    _currentRule = _currentVsys.getRules().computeIfAbsent(name, n -> new Rule(n, _currentVsys));

    // Use constructed name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys.getName(), name);
    defineStructure(RULE, uniqueName, ctx);
    _configuration.referenceStructure(RULE, uniqueName, RULE_SELF_REF, getLine(ctx.name.start));
  }

  @Override
  public void exitSr_security(Sr_securityContext ctx) {
    _currentRule = null;
  }

  @Override
  public void exitSrs_action(Srs_actionContext ctx) {
    if (ctx.ALLOW() != null) {
      _currentRule.setAction(LineAction.PERMIT);
    } else {
      _currentRule.setAction(LineAction.DENY);
    }
  }

  @Override
  public void exitSrs_application(Srs_applicationContext ctx) {
    for (Variable_list_itemContext var : ctx.variable_list().variable_list_item()) {
      String name = var.getText();
      if (!name.equals(CATCHALL_APPLICATION_NAME)) {
        _w.redFlag("Unhandled application: " + name);
      }
    }
  }

  @Override
  public void exitSrs_description(Srs_descriptionContext ctx) {
    _currentRule.setDescription(ctx.description.getText());
  }

  @Override
  public void exitSrs_destination(Srs_destinationContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      IpSpace destinationIpSpace = toIpSpace(var);
      if (destinationIpSpace != null) {
        _currentRule.getDestination().add(destinationIpSpace);
      }
    }
  }

  @Override
  public void exitSrs_disabled(Srs_disabledContext ctx) {
    _currentRule.setDisabled(ctx.YES() != null);
  }

  @Override
  public void exitSrs_from(Srs_fromContext ctx) {
    for (Variable_list_itemContext var : ctx.variable_list().variable_list_item()) {
      String zoneName = var.getText();
      _currentRule.getFrom().add(zoneName);

      if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys.getName(), zoneName);
        _configuration.referenceStructure(ZONE, uniqueName, RULE_FROM_ZONE, getLine(var.start));
      }
    }
  }

  @Override
  public void exitSrs_service(Srs_serviceContext ctx) {
    for (Variable_list_itemContext var : ctx.variable_list().variable_list_item()) {
      String serviceName = var.getText();
      _currentRule.getService().add(new ServiceOrServiceGroupReference(serviceName));

      if (!serviceName.equals(CATCHALL_SERVICE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys.getName(), serviceName);
        _configuration.referenceStructure(
            SERVICE_OR_SERVICE_GROUP, uniqueName, RULEBASE_SERVICE, getLine(var.start));
      }
    }
  }

  @Override
  public void exitSrs_source(Srs_sourceContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      IpSpace sourceIpSpace = toIpSpace(var);
      if (sourceIpSpace != null) {
        _currentRule.getSource().add(sourceIpSpace);
      }
    }
  }

  @Override
  public void exitSrs_to(Srs_toContext ctx) {
    for (Variable_list_itemContext var : ctx.variable_list().variable_list_item()) {
      String zoneName = var.getText();
      _currentRule.getTo().add(zoneName);

      if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys.getName(), zoneName);
        _configuration.referenceStructure(ZONE, uniqueName, RULE_TO_ZONE, getLine(var.start));
      }
    }
  }

  @Override
  public void enterS_service(S_serviceContext ctx) {
    String name = ctx.name.getText();
    _currentService = _currentVsys.getServices().computeIfAbsent(name, Service::new);

    // Use constructed service name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys.getName(), name);
    defineStructure(PaloAltoStructureType.SERVICE, uniqueName, ctx);
  }

  @Override
  public void exitS_service(S_serviceContext ctx) {
    _currentService = null;
  }

  @Override
  public void exitSserv_description(Sserv_descriptionContext ctx) {
    _currentService.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSserv_port(Sserv_portContext ctx) {
    for (TerminalNode item : ctx.variable_comma_separated_dec().DEC()) {
      _currentService.getPorts().add(toInteger(item.getSymbol()));
    }
  }

  @Override
  public void exitSserv_protocol(Sserv_protocolContext ctx) {
    if (ctx.SCTP() != null) {
      _currentService.setProtocol(IpProtocol.SCTP);
    } else if (ctx.TCP() != null) {
      _currentService.setProtocol(IpProtocol.TCP);
    } else if (ctx.UDP() != null) {
      _currentService.setProtocol(IpProtocol.UDP);
    }
  }

  @Override
  public void exitSserv_source_port(Sserv_source_portContext ctx) {
    for (TerminalNode item : ctx.variable_comma_separated_dec().DEC()) {
      _currentService.getSourcePorts().add(toInteger(item.getSymbol()));
    }
  }

  @Override
  public void enterS_service_group(S_service_groupContext ctx) {
    String name = ctx.name.getText();
    _currentServiceGroup = _currentVsys.getServiceGroups().computeIfAbsent(name, ServiceGroup::new);

    // Use constructed service-group name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys.getName(), name);
    defineStructure(SERVICE_GROUP, uniqueName, ctx);
  }

  @Override
  public void exitS_service_group(S_service_groupContext ctx) {
    _currentServiceGroup = null;
  }

  @Override
  public void exitSservgrp_members(Sservgrp_membersContext ctx) {
    for (Variable_list_itemContext var : ctx.variable_list().variable_list_item()) {
      String name = getText(var);
      _currentServiceGroup.getReferences().add(new ServiceOrServiceGroupReference(name));

      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys.getName(), name);
      _configuration.referenceStructure(
          SERVICE_OR_SERVICE_GROUP, uniqueName, SERVICE_GROUP_MEMBER, getLine(var.getStart()));
    }
  }

  @Override
  public void enterS_shared(S_sharedContext ctx) {
    _currentVsys = _configuration.getVirtualSystems().computeIfAbsent(SHARED_VSYS_NAME, Vsys::new);
  }

  @Override
  public void exitS_shared(S_sharedContext ctx) {
    _currentVsys = _defaultVsys;
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
        _currentVsys.getSyslogServer(_currentSyslogServerGroupName, getText(ctx.name));
  }

  @Override
  public void exitSsls_server(Ssls_serverContext ctx) {
    _currentSyslogServer = null;
  }

  @Override
  public void exitSslss_server(Sslss_serverContext ctx) {
    _currentSyslogServer.setAddress(ctx.address.getText());
  }

  @Override
  public void enterS_vsys(S_vsysContext ctx) {
    _currentVsys =
        _configuration.getVirtualSystems().computeIfAbsent(ctx.name.getText(), Vsys::new);
  }

  @Override
  public void exitS_vsys(S_vsysContext ctx) {
    _currentVsys = _defaultVsys;
  }

  @Override
  public void exitSzn_layer3(Szn_layer3Context ctx) {
    if (ctx.variable_list() != null) {
      for (Variable_list_itemContext var : ctx.variable_list().variable_list_item()) {
        String name = var.getText();
        _currentZone.getInterfaceNames().add(name);
        _configuration.referenceStructure(INTERFACE, name, ZONE_INTERFACE, getLine(var.start));
      }
    }
  }

  public PaloAltoConfiguration getConfiguration() {
    return _configuration;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    int line = getLine(token);
    _configuration.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
    }
  }
}
