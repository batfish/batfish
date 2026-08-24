package org.batfish.vendor.aruba_aoscx.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.Interface_nameContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_access_list_ipContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_access_list_ipv6Context;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_acl_entryContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_apply_access_list_ipContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_apply_access_list_ipv6Context;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_default_gatewayContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_descriptionContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_hostnameContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_interfaceContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_lag_memberContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_addressContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_addressContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_areaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_costContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_dead_intervalContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_hello_intervalContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_networkContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_passiveContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_priorityContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_retransmit_intervalContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_transit_delayContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_shutdownContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_routeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_ospfv3Context;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospf_areaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_passive_defaultContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_reference_bandwidthContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_staticContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_mtuContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_areaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_costContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_networkContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_prefix_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_prefix_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_routeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_mtuContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_no_routingContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_vlan_trunk_nativeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_vlan_trunk_allowedContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_no_shutdownContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_idContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_bgpContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_match_ip_address_prefix_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_match_ipv6_address_prefix_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_route_mapContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_set_local_preferenceContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_set_metricContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_set_metric_typeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_set_tagContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_router_idContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_address_family_ipv4Context;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_neighbor_activateContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_neighbor_remote_asContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_neighbor_route_mapContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospf_area_stubContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospf_area_nssaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_area_rangeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_area_default_metricContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_default_informationContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_default_metricContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_distanceContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_distribute_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_maximum_pathsContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_summary_addressContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_process_stateContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_redistribute_connectedContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_redistribute_local_loopbackContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_redistribute_staticContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_ospfContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_shutdownContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_speedContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_vrfContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_vrf_attachContext;
import org.batfish.vendor.aruba_aoscx.representation.AosCxConfiguration;
import org.batfish.vendor.aruba_aoscx.representation.AosCxBgpProcess;
import org.batfish.vendor.aruba_aoscx.representation.AosCxInterface;
import org.batfish.vendor.aruba_aoscx.representation.AosCxIpAccessList;
import org.batfish.vendor.aruba_aoscx.representation.AosCxIpAccessListEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxIpv6AccessList;
import org.batfish.vendor.aruba_aoscx.representation.AosCxIpv6AccessListEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxIpv6PrefixList;
import org.batfish.vendor.aruba_aoscx.representation.AosCxIpv6PrefixListEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPortSpec;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPortSpec.Operator;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPrefixList;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPrefixListEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMapEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxInterface.OspfNetworkType;
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfProcess;
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfv3Process;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute6;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute.NextHopType;

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
  public void exitS_access_list_ip(S_access_list_ipContext ctx) {
    _currentIpAccessList =
        _configuration.getOrCreateIpAccessList(ctx.WORD().getText());
    _currentIpv6AccessList = null;
    _currentInterface = null;
    _currentRouteMapEntry = null;
    _currentOspfProcess = null;
    _currentBgpProcess = null;
    _inBgpIpv4Unicast = false;
  }

  @Override
  public void exitS_access_list_ipv6(
      S_access_list_ipv6Context ctx) {
    _currentIpv6AccessList =
        _configuration.getOrCreateIpv6AccessList(
            ctx.WORD().getText());
    _currentIpAccessList = null;
    _currentInterface = null;
    _currentRouteMapEntry = null;
    _currentOspfProcess = null;
    _currentOspfv3Process = null;
    _currentBgpProcess = null;
    _inBgpIpv4Unicast = false;
  }

  @Override
  public void exitS_acl_entry(S_acl_entryContext ctx) {
    if (_currentIpAccessList == null
        && _currentIpv6AccessList == null) {
      warn(
          ctx,
          "Ignoring ACL entry outside ACL context");
      return;
    }

    long sequence =
        ctx.WORD() != null
            ? Long.parseLong(
                ctx.WORD().getText())
            : _currentIpv6AccessList != null
                ? _currentIpv6AccessList.getNextSequence()
                : _currentIpAccessList.getNextSequence();

    LineAction action =
        ctx.acl_action().PERMIT() != null
            ? LineAction.PERMIT
            : LineAction.DENY;

    AosCxPortSpec sourcePort =
        ctx.acl_src_port_spec() == null
            ? null
            : toPortSpec(
                ctx.acl_src_port_spec()
                    .acl_port_spec());

    AosCxPortSpec destinationPort =
        ctx.acl_dst_port_spec() == null
            ? null
            : toPortSpec(
                ctx.acl_dst_port_spec()
                    .acl_port_spec());

    if (_currentIpv6AccessList != null) {
      _currentIpv6AccessList.addEntry(
          new AosCxIpv6AccessListEntry(
              sequence,
              action,
              ctx.acl_protocol().getText(),
              ctx.acl_address(0).getText(),
              sourcePort,
              ctx.acl_address(1).getText(),
              destinationPort));
      return;
    }

    _currentIpAccessList.addEntry(
        new AosCxIpAccessListEntry(
            sequence,
            action,
            ctx.acl_protocol().getText(),
            ctx.acl_address(0).getText(),
            sourcePort,
            ctx.acl_address(1).getText(),
            destinationPort));
  }

  private static AosCxPortSpec toPortSpec(AosCxParser.Acl_port_specContext ctx) {
    Operator operator;
    if (ctx.EQ() != null) {
      operator = Operator.EQ;
    } else if (ctx.GT() != null) {
      operator = Operator.GT;
    } else if (ctx.LT() != null) {
      operator = Operator.LT;
    } else {
      operator = Operator.RANGE;
    }

    int first = Integer.parseInt(ctx.WORD(0).getText());
    Integer second =
        ctx.RANGE() == null ? null : Integer.parseInt(ctx.WORD(1).getText());

    return new AosCxPortSpec(operator, first, second);
  }

  @Override
  public void exitS_apply_access_list_ip(S_apply_access_list_ipContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring apply access-list outside interface context");
      return;
    }

    String aclName = ctx.WORD().getText();
    String direction = ctx.acl_direction().getText();

    if (direction.equals("in") || direction.equals("routed-in")) {
      _currentInterface.setIncomingAcl(aclName);
    } else {
      _currentInterface.setOutgoingAcl(aclName);
    }
  }

  @Override
  public void exitS_apply_access_list_ipv6(
      S_apply_access_list_ipv6Context ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring apply access-list ipv6 outside interface context");
      return;
    }

    String aclName =
        ctx.WORD().getText();
    String direction =
        ctx.acl_direction().getText();

    if (direction.equals("in")
        || direction.equals("routed-in")) {
      _currentInterface.setIncomingIpv6Acl(
          aclName);
    } else {
      _currentInterface.setOutgoingIpv6Acl(
          aclName);
    }
  }

  @Override
  public void exitS_default_gateway(S_default_gatewayContext ctx) {
    if (_currentInterface == null
        || !_currentInterface.getName().equals("mgmt")) {
      warn(ctx, "Ignoring default-gateway outside management interface context");
      return;
    }

    String gateway = ctx.WORD().getText();

    if (Ip.tryParse(gateway).isEmpty()) {
      warn(ctx, "Ignoring non-IPv4 management default gateway");
      return;
    }

    _configuration
        .getStaticRoutes()
        .add(
            new AosCxStaticRoute(
                Prefix.ZERO,
                NextHopType.IP,
                gateway,
                "mgmt"));
  }

  @Override
  public void exitS_description(S_descriptionContext ctx) {
    if (_currentInterface == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(ctx, "Ignoring description outside interface context");
      return;
    }

    String line =
        _text.substring(
                ctx.getStart().getStartIndex(),
                ctx.getStop().getStopIndex() + 1)
            .trim();

    _currentInterface.setDescription(
        line.substring("description".length()).trim());
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _configuration.setHostname(ctx.WORD().getText());
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    String name = toInterfaceName(ctx.interface_name());
    _currentInterface = _configuration.getOrCreateInterface(name);

    if (name.equals("mgmt")) {
      _configuration.addVrf("mgmt");
      _currentInterface.setVrfName("mgmt");
    }

    _currentRouteMapEntry = null;
    _currentOspfProcess = null;
    _currentOspfv3Process = null;
    _currentBgpProcess = null;
    _currentBgpLocalAs = null;
    _inBgpIpv4Unicast = false;
    _currentIpAccessList = null;
    _currentIpv6AccessList = null;
  }

  @Override
  public void exitS_lag_member(S_lag_memberContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring LAG member command outside interface context");
      return;
    }
    _currentInterface.setLagName("lag " + ctx.WORD().getText());
  }

  @Override
  public void exitS_vrf_attach(S_vrf_attachContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring vrf attach outside interface context");
      return;
    }

    String vrfName = ctx.WORD().getText();
    _configuration.addVrf(vrfName);
    _currentInterface.setVrfName(vrfName);
  }

  @Override
  public void exitS_vrf(S_vrfContext ctx) {
    String vrfName = ctx.WORD().getText();

    /*
     * AOS-CX uses "vrf NAME" both as a top-level VRF command and
     * as a nested router-bgp command. show running-config indents
     * the nested form, so use the token column to distinguish them
     * while this parser still uses a flat statement stream.
     */
    if (_currentBgpLocalAs != null
        && ctx.getStart().getCharPositionInLine() > 0) {
      _configuration.addVrf(vrfName);
      _currentBgpProcess =
          _configuration.getOrCreateBgpProcess(
              _currentBgpLocalAs, vrfName);
      _currentInterface = null;
      _currentRouteMapEntry = null;
      _currentOspfProcess = null;
      _currentIpAccessList = null;
    _currentIpv6AccessList = null;
      _inBgpIpv4Unicast = false;
      return;
    }

    _configuration.addVrf(vrfName);
    _currentInterface = null;
    _currentRouteMapEntry = null;
    _currentOspfProcess = null;
    _currentBgpProcess = null;
    _currentBgpLocalAs = null;
    _currentIpAccessList = null;
    _currentIpv6AccessList = null;
    _inBgpIpv4Unicast = false;
  }

  @Override
  public void exitS_ip_address(S_ip_addressContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring IP address outside interface context");
      return;
    }
    _currentInterface.setAddress(ConcreteInterfaceAddress.parse(ctx.WORD().getText()));
  }

  @Override
  public void exitS_ipv6_address(S_ipv6_addressContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring IPv6 address outside interface context");
      return;
    }

    if (ctx.LINK_LOCAL() != null) {
      // AOS-CX generates the actual link-local address. Preserve the
      // configured state without fabricating an fe80:: address.
      _currentInterface.setIpv6LinkLocalEnabled(true);
      return;
    }

    _currentInterface.addIpv6Address(
        ConcreteInterfaceAddress6.parse(ctx.WORD().getText()));
  }

  @Override
  public void exitS_ip_static(S_ip_staticContext ctx) {
    if (_currentInterface == null
        || !_currentInterface.getName().equals("mgmt")) {
      warn(ctx, "Ignoring ip static outside management interface context");
      return;
    }

    String address = ctx.WORD().getText();

    if (address.contains(":")) {
      warn(ctx, "Ignoring IPv6 management static address until IPv6 support is implemented");
      return;
    }

    _currentInterface.setAddress(
        ConcreteInterfaceAddress.parse(address));
  }

  @Override
  public void exitS_ip_mtu(S_ip_mtuContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring IP MTU outside interface context");
      return;
    }
    _currentInterface.setIpMtu(Integer.parseInt(ctx.WORD().getText()));
  }

  @Override
  public void exitS_ip_ospf_area(S_ip_ospf_areaContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring OSPF area command outside interface context");
      return;
    }
    _currentInterface.setOspfProcessId(Integer.parseInt(ctx.WORD(0).getText()));
    _currentInterface.setOspfArea(ctx.WORD(1).getText());
  }

  @Override
  public void exitS_ip_ospf_cost(S_ip_ospf_costContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring OSPF cost command outside interface context");
      return;
    }

    _currentInterface.setOspfCost(
        Integer.parseInt(ctx.WORD().getText()));
  }

  @Override
  public void exitS_ip_ospf_network(S_ip_ospf_networkContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring OSPF network command outside interface context");
      return;
    }
    _currentInterface.setOspfNetworkType(OspfNetworkType.POINT_TO_POINT);
  }

  @Override
  public void exitS_ipv6_ospfv3_area(S_ipv6_ospfv3_areaContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring OSPFv3 area command outside interface context");
      return;
    }

    _currentInterface.setOspfv3ProcessId(
        Integer.parseInt(ctx.WORD(0).getText()));
    _currentInterface.setOspfv3Area(ctx.WORD(1).getText());
  }

  @Override
  public void exitS_ipv6_ospfv3_cost(
      S_ipv6_ospfv3_costContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 cost command outside interface context");
      return;
    }

    if (ctx.NO() != null) {
      _currentInterface.clearOspfv3Cost();
      return;
    }

    int cost;
    try {
      cost =
          Integer.parseInt(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(ctx, "Ignoring invalid OSPFv3 cost");
      return;
    }

    if (cost < 1 || cost > 65535) {
      warn(
          ctx,
          "Ignoring OSPFv3 cost outside 1-65535");
      return;
    }

    _currentInterface.setOspfv3Cost(cost);
  }

  @Override
  public void exitS_ipv6_ospfv3_hello_interval(
      S_ipv6_ospfv3_hello_intervalContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 hello-interval outside interface context");
      return;
    }

    if (ctx.NO() != null) {
      _currentInterface
          .clearOspfv3HelloInterval();
      return;
    }

    int interval;
    try {
      interval =
          Integer.parseInt(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 hello interval");
      return;
    }

    if (interval < 1 || interval > 65535) {
      warn(
          ctx,
          "Ignoring OSPFv3 hello interval outside 1-65535");
      return;
    }

    _currentInterface
        .setOspfv3HelloInterval(interval);
  }

  @Override
  public void exitS_ipv6_ospfv3_dead_interval(
      S_ipv6_ospfv3_dead_intervalContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 dead-interval outside interface context");
      return;
    }

    if (ctx.NO() != null) {
      _currentInterface
          .clearOspfv3DeadInterval();
      return;
    }

    int interval;
    try {
      interval =
          Integer.parseInt(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 dead interval");
      return;
    }

    if (interval < 1 || interval > 65535) {
      warn(
          ctx,
          "Ignoring OSPFv3 dead interval outside 1-65535");
      return;
    }

    _currentInterface
        .setOspfv3DeadInterval(interval);
  }

  @Override
  public void exitS_ipv6_ospfv3_network(
      S_ipv6_ospfv3_networkContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 network command outside interface context");
      return;
    }

    if (ctx.NO() != null) {
      _currentInterface
          .clearOspfv3NetworkType();
      return;
    }

    _currentInterface.setOspfv3NetworkType(
        ctx.POINT_TO_POINT() != null
            ? OspfNetworkType.POINT_TO_POINT
            : OspfNetworkType.BROADCAST);
  }

  @Override
  public void exitS_ipv6_ospfv3_priority(
      S_ipv6_ospfv3_priorityContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 priority outside interface context");
      return;
    }

    if (ctx.NO() != null) {
      _currentInterface
          .clearOspfv3Priority();
      return;
    }

    int priority;

    try {
      priority =
          Integer.parseInt(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 priority");
      return;
    }

    if (priority < 0 || priority > 255) {
      warn(
          ctx,
          "Ignoring OSPFv3 priority outside 0-255");
      return;
    }

    _currentInterface
        .setOspfv3Priority(priority);
  }

  @Override
  public void exitS_ipv6_ospfv3_retransmit_interval(
      S_ipv6_ospfv3_retransmit_intervalContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 retransmit-interval outside interface context");
      return;
    }

    if (ctx.NO() != null) {
      _currentInterface
          .clearOspfv3RetransmitInterval();
      return;
    }

    int interval;

    try {
      interval =
          Integer.parseInt(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 retransmit interval");
      return;
    }

    if (interval < 1 || interval > 3600) {
      warn(
          ctx,
          "Ignoring OSPFv3 retransmit interval outside 1-3600");
      return;
    }

    _currentInterface
        .setOspfv3RetransmitInterval(
            interval);
  }

  @Override
  public void exitS_ipv6_ospfv3_transit_delay(
      S_ipv6_ospfv3_transit_delayContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 transit-delay outside interface context");
      return;
    }

    if (ctx.NO() != null) {
      _currentInterface
          .clearOspfv3TransitDelay();
      return;
    }

    int delay;

    try {
      delay =
          Integer.parseInt(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 transit delay");
      return;
    }

    if (delay < 1 || delay > 3600) {
      warn(
          ctx,
          "Ignoring OSPFv3 transit delay outside 1-3600");
      return;
    }

    _currentInterface
        .setOspfv3TransitDelay(delay);
  }

  @Override
  public void exitS_ipv6_ospfv3_passive(
      S_ipv6_ospfv3_passiveContext ctx) {
    if (_currentInterface == null) {
      warn(
          ctx,
          "Ignoring OSPFv3 passive command outside interface context");
      return;
    }

    _currentInterface.setOspfv3Passive(
        ctx.NO() == null);
  }

  @Override
  public void exitS_ipv6_ospfv3_shutdown(
      S_ipv6_ospfv3_shutdownContext ctx) {
    if (_currentInterface == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPFv3 shutdown outside interface context");
      return;
    }

    _currentInterface.setOspfv3Enabled(
        ctx.NO() != null);
  }

  @Override
  public void exitS_ospfv3_passive_default(
      S_ospfv3_passive_defaultContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring passive-interface outside OSPFv3 context");
      return;
    }

    _currentOspfv3Process
        .setPassiveInterfaceDefault(
            ctx.NO() == null);
  }

  @Override
  public void exitS_ospfv3_reference_bandwidth(
      S_ospfv3_reference_bandwidthContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring reference-bandwidth outside OSPFv3 context");
      return;
    }

    if (ctx.NO() != null) {
      _currentOspfv3Process
          .resetReferenceBandwidth();
      return;
    }

    long bandwidthMbps;

    try {
      bandwidthMbps =
          Long.parseLong(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 reference bandwidth");
      return;
    }

    if (bandwidthMbps < 1L
        || bandwidthMbps > 4_000_000L) {
      warn(
          ctx,
          "Ignoring OSPFv3 reference bandwidth outside 1-4000000 Mbps");
      return;
    }

    _currentOspfv3Process
        .setReferenceBandwidthMbps(
            bandwidthMbps);
  }

  @Override
  public void exitS_ip_prefix_list(S_ip_prefix_listContext ctx) {
    String name = ctx.WORD(0).getText();
    AosCxPrefixList prefixList = _configuration.getOrCreatePrefixList(name);

    long sequence =
        ctx.prefix_list_seq() != null
            ? Long.parseLong(ctx.prefix_list_seq().WORD().getText())
            : prefixList.getNextSequence();

    LineAction action =
        ctx.prefix_list_action().PERMIT() != null ? LineAction.PERMIT : LineAction.DENY;

    Prefix prefix = Prefix.parse(ctx.WORD(1).getText());

    Integer ge =
        ctx.prefix_list_ge() != null
            ? Integer.parseInt(ctx.prefix_list_ge().WORD().getText())
            : null;

    Integer le =
        ctx.prefix_list_le() != null
            ? Integer.parseInt(ctx.prefix_list_le().WORD().getText())
            : null;

    prefixList.addEntry(new AosCxPrefixListEntry(sequence, action, prefix, ge, le));
  }

  @Override
  public void exitS_ipv6_prefix_list(
      S_ipv6_prefix_listContext ctx) {

    String name =
        ctx.WORD(0).getText();

    AosCxIpv6PrefixList prefixList =
        _configuration
            .getOrCreateIpv6PrefixList(name);

    long sequence =
        ctx.prefix_list_seq() != null
            ? Long.parseLong(
                ctx.prefix_list_seq()
                    .WORD()
                    .getText())
            : prefixList.getNextSequence();

    LineAction action =
        ctx.prefix_list_action()
                    .PERMIT()
                != null
            ? LineAction.PERMIT
            : LineAction.DENY;

    Prefix6 prefix;

    try {
      prefix =
          Prefix6.parse(
              ctx.WORD(1).getText());
    } catch (IllegalArgumentException e) {
      warn(
          ctx,
          "Ignoring invalid IPv6 prefix-list prefix");
      return;
    }

    Integer ge =
        ctx.prefix_list_ge() != null
            ? Integer.parseInt(
                ctx.prefix_list_ge()
                    .WORD()
                    .getText())
            : null;

    Integer le =
        ctx.prefix_list_le() != null
            ? Integer.parseInt(
                ctx.prefix_list_le()
                    .WORD()
                    .getText())
            : null;

    int prefixLength =
        prefix.getPrefixLength();

    int minLength =
        ge != null
            ? ge
            : prefixLength;

    int maxLength =
        le != null
            ? le
            : ge != null
                ? Prefix6.MAX_PREFIX_LENGTH
                : prefixLength;

    if (minLength < prefixLength
        || minLength
            > Prefix6.MAX_PREFIX_LENGTH
        || maxLength < prefixLength
        || maxLength
            > Prefix6.MAX_PREFIX_LENGTH
        || minLength > maxLength) {
      warn(
          ctx,
          "Ignoring IPv6 prefix-list entry with invalid ge/le range");
      return;
    }

    prefixList.addEntry(
        new AosCxIpv6PrefixListEntry(
            sequence,
            action,
            prefix,
            ge,
            le));
  }

  @Override
  public void exitS_ip_route(S_ip_routeContext ctx) {
    Prefix prefix = Prefix.parse(ctx.WORD(0).getText());
    String nextHop = ctx.static_route_next_hop().getText();
    String vrfName = ctx.VRF() != null ? ctx.WORD(1).getText() : null;

    if (vrfName != null) {
      _configuration.addVrf(vrfName);
    }

    NextHopType nextHopType;
    if (ctx.static_route_next_hop().NULLROUTE() != null) {
      nextHopType = NextHopType.NULL_ROUTE;
    } else if (ctx.static_route_next_hop().REJECT() != null) {
      nextHopType = NextHopType.REJECT;
    } else if (Ip.tryParse(nextHop).isPresent()) {
      nextHopType = NextHopType.IP;
    } else {
      nextHopType = NextHopType.INTERFACE;
    }

    _configuration
        .getStaticRoutes()
        .add(new AosCxStaticRoute(prefix, nextHopType, nextHop, vrfName));
  }

  @Override
  public void exitS_ipv6_route(
      S_ipv6_routeContext ctx) {
    Prefix6 prefix =
        Prefix6.parse(
            ctx.WORD().getText());

    String nextHop =
        ctx.static_route_next_hop()
            .getText();

    String vrfName = null;
    long administrativeDistance =
        AosCxStaticRoute6
            .DEFAULT_ADMINISTRATIVE_DISTANCE;
    long tag =
        Route.UNSET_ROUTE_TAG;

    for (AosCxParser.Ipv6_static_route_optionContext option :
        ctx.ipv6_static_route_option()) {

      if (option.VRF() != null) {
        vrfName =
            option.WORD().getText();
        continue;
      }

      if (option.DISTANCE() != null) {
        long value;

        try {
          value =
              Long.parseLong(
                  option.WORD().getText());
        } catch (NumberFormatException e) {
          warn(
              ctx,
              "Ignoring IPv6 static route with invalid distance");
          return;
        }

        if (value < 1L || value > 255L) {
          warn(
              ctx,
              "Ignoring IPv6 static route with distance outside 1-255");
          return;
        }

        administrativeDistance = value;
        continue;
      }

      if (option.TAG() != null) {
        long value;

        try {
          value =
              Long.parseLong(
                  option.WORD().getText());
        } catch (NumberFormatException e) {
          warn(
              ctx,
              "Ignoring IPv6 static route with invalid tag");
          return;
        }

        if (value < 1L
            || value > 0xFFFFFFFFL) {
          warn(
              ctx,
              "Ignoring IPv6 static route with tag outside 1-4294967295");
          return;
        }

        tag = value;
      }
    }

    if (vrfName != null) {
      _configuration.addVrf(vrfName);
    }

    NextHopType nextHopType;

    if (ctx.static_route_next_hop()
        .NULLROUTE() != null) {
      nextHopType =
          NextHopType.NULL_ROUTE;
    } else if (
        ctx.static_route_next_hop()
            .REJECT() != null) {
      nextHopType =
          NextHopType.REJECT;
    } else if (
        Ip6.tryParse(nextHop).isPresent()) {
      nextHopType =
          NextHopType.IP;
    } else {
      nextHopType =
          NextHopType.INTERFACE;
    }

    _configuration
        .getStaticRoutes6()
        .add(
            new AosCxStaticRoute6(
                prefix,
                nextHopType,
                nextHop,
                vrfName,
                administrativeDistance,
                tag));
  }

  @Override
  public void exitS_route_map(S_route_mapContext ctx) {
    String name = ctx.WORD(0).getText();
    long sequence = Long.parseLong(ctx.WORD(1).getText());
    LineAction action =
        ctx.route_map_action().PERMIT() != null ? LineAction.PERMIT : LineAction.DENY;

    _currentRouteMapEntry =
        _configuration.getOrCreateRouteMap(name).getOrCreateEntry(sequence, action);

    _currentInterface = null;
    _currentOspfProcess = null;
    _currentOspfv3Process = null;
    _currentBgpProcess = null;
    _currentIpAccessList = null;
    _currentIpv6AccessList = null;
  }

  @Override
  public void exitS_match_ip_address_prefix_list(
      S_match_ip_address_prefix_listContext ctx) {
    if (_currentRouteMapEntry == null) {
      warn(ctx, "Ignoring route-map match outside route-map context");
      return;
    }
    _currentRouteMapEntry.setMatchPrefixList(ctx.WORD().getText());
  }

  @Override
  public void exitS_match_ipv6_address_prefix_list(
      S_match_ipv6_address_prefix_listContext ctx) {
    if (_currentRouteMapEntry == null) {
      warn(
          ctx,
          "Ignoring IPv6 route-map match outside route-map context");
      return;
    }

    _currentRouteMapEntry
        .setMatchIpv6PrefixList(
            ctx.WORD().getText());
  }

  @Override
  public void exitS_set_local_preference(S_set_local_preferenceContext ctx) {
    if (_currentRouteMapEntry == null) {
      warn(ctx, "Ignoring route-map set outside route-map context");
      return;
    }
    _currentRouteMapEntry.setSetLocalPreference(Long.parseLong(ctx.WORD().getText()));
  }

  @Override
  public void exitS_set_metric(
      S_set_metricContext ctx) {
    if (_currentRouteMapEntry == null) {
      warn(
          ctx,
          "Ignoring route-map set metric outside route-map context");
      return;
    }

    long metric;

    try {
      metric =
          Long.parseLong(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid route-map metric");
      return;
    }

    if (metric < 0L
        || metric > 0xFFFFFFFFL) {
      warn(
          ctx,
          "Ignoring route-map metric outside 0-4294967295");
      return;
    }

    _currentRouteMapEntry
        .setSetMetric(metric);
  }

  @Override
  public void exitS_set_metric_type(
      S_set_metric_typeContext ctx) {
    if (_currentRouteMapEntry == null) {
      warn(
          ctx,
          "Ignoring route-map set metric-type outside route-map context");
      return;
    }

    _currentRouteMapEntry.setSetOspfMetricType(
        ctx.ospf_external_metric_type().TYPE_1() != null
            ? OspfMetricType.E1
            : OspfMetricType.E2);
  }

  @Override
  public void exitS_set_tag(
      S_set_tagContext ctx) {
    if (_currentRouteMapEntry == null) {
      warn(
          ctx,
          "Ignoring route-map set tag outside route-map context");
      return;
    }

    long tag;

    try {
      tag =
          Long.parseLong(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid route-map tag");
      return;
    }

    if (tag < 0L
        || tag > 0xFFFFFFFFL) {
      warn(
          ctx,
          "Ignoring route-map tag outside 0-4294967295");
      return;
    }

    _currentRouteMapEntry
        .setSetTag(tag);
  }

  @Override
  public void exitS_router_bgp(S_router_bgpContext ctx) {
    long localAs = toAsNumber(ctx.WORD().getText());
    _currentBgpLocalAs = localAs;
    _currentBgpProcess =
        _configuration.getOrCreateBgpProcess(localAs);
    _currentRouteMapEntry = null;
    _inBgpIpv4Unicast = false;
    _currentOspfProcess = null;
    _currentInterface = null;
    _currentIpAccessList = null;
    _currentIpv6AccessList = null;
  }

  private static long toAsNumber(String text) {
    if (!text.contains(".")) {
      return Long.parseLong(text);
    }
    String[] parts = text.split("\\.", -1);
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid AS number: " + text);
    }
    long high = Long.parseLong(parts[0]);
    long low = Long.parseLong(parts[1]);
    return (high << 16) | low;
  }

  @Override
  public void exitS_bgp_neighbor_remote_as(S_bgp_neighbor_remote_asContext ctx) {
    if (_currentBgpProcess == null) {
      warn(ctx, "Ignoring BGP neighbor outside BGP context");
      return;
    }

    String neighborText = ctx.WORD(0).getText();
    if (Ip.tryParse(neighborText).isEmpty()) {
      warn(ctx, "BGP unnumbered/interface neighbors are not supported yet");
      return;
    }

    Ip neighborIp = Ip.parse(neighborText);
    _currentBgpProcess
        .getOrCreateNeighbor(neighborIp)
        .setRemoteAs(toAsNumber(ctx.WORD(1).getText()));
  }

  @Override
  public void exitS_bgp_address_family_ipv4(S_bgp_address_family_ipv4Context ctx) {
    if (_currentBgpProcess == null) {
      warn(ctx, "Ignoring BGP address-family outside BGP context");
      return;
    }
    _inBgpIpv4Unicast = true;
  }

  @Override
  public void exitS_bgp_neighbor_activate(S_bgp_neighbor_activateContext ctx) {
    if (_currentBgpProcess == null || !_inBgpIpv4Unicast) {
      warn(ctx, "Ignoring BGP neighbor activate outside IPv4 unicast address-family");
      return;
    }

    String neighborText = ctx.WORD().getText();
    if (Ip.tryParse(neighborText).isEmpty()) {
      warn(ctx, "BGP unnumbered/interface neighbors are not supported yet");
      return;
    }

    _currentBgpProcess
        .getOrCreateNeighbor(Ip.parse(neighborText))
        .setIpv4UnicastActive(true);
  }

  @Override
  public void exitS_bgp_neighbor_route_map(S_bgp_neighbor_route_mapContext ctx) {
    if (_currentBgpProcess == null || !_inBgpIpv4Unicast) {
      warn(ctx, "Ignoring BGP neighbor route-map outside IPv4 unicast address-family");
      return;
    }

    String neighborText = ctx.WORD(0).getText();
    if (Ip.tryParse(neighborText).isEmpty()) {
      warn(ctx, "BGP unnumbered/interface neighbor route-maps are not supported yet");
      return;
    }

    String routeMapName = ctx.WORD(1).getText();
    if (ctx.IN() != null) {
      _currentBgpProcess
          .getOrCreateNeighbor(Ip.parse(neighborText))
          .setRouteMapIn(routeMapName);
    } else {
      _currentBgpProcess
          .getOrCreateNeighbor(Ip.parse(neighborText))
          .setRouteMapOut(routeMapName);
    }
  }

  @Override
  public void exitS_bgp_router_id(S_bgp_router_idContext ctx) {
    if (_currentBgpProcess == null) {
      warn(ctx, "Ignoring BGP router-id outside BGP context");
      return;
    }
    _currentBgpProcess.setRouterId(Ip.parse(ctx.WORD().getText()));
  }

  @Override
  public void exitS_ospf_area(S_ospf_areaContext ctx) {
    if (_currentOspfv3Process != null
        && ctx.getStart().getCharPositionInLine() > 0) {
      _currentOspfv3Process
          .setNormalArea(
              ctx.WORD().getText());
    }
  }

  @Override
  public void exitS_ospf_area_stub(
      S_ospf_area_stubContext ctx) {
    if (ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPF stub area outside OSPF context");
      return;
    }

    String area =
        ctx.WORD().getText();

    if (_currentOspfProcess != null) {
      if (ctx.NO() == null) {
        _currentOspfProcess.setStubArea(
            area,
            ctx.NO_SUMMARY() != null);
      } else if (ctx.NO_SUMMARY() != null) {
        if (_currentOspfProcess
            .getStubAreas()
            .containsKey(area)) {
          _currentOspfProcess.setStubArea(
              area,
              false);
        }
      } else {
        _currentOspfProcess
            .getStubAreas()
            .remove(area);
      }
      return;
    }

    if (_currentOspfv3Process != null) {
      if (ctx.NO() == null) {
        _currentOspfv3Process.setStubArea(
            area,
            ctx.NO_SUMMARY() != null);
      } else if (ctx.NO_SUMMARY() != null) {
        _currentOspfv3Process
            .clearStubNoSummary(area);
      } else {
        _currentOspfv3Process
            .clearStubArea(area);
      }
      return;
    }

    warn(
        ctx,
        "Ignoring OSPF stub area outside OSPF context");
  }

  @Override
  public void exitS_ospf_area_nssa(
      S_ospf_area_nssaContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPFv3 NSSA area outside OSPFv3 context");
      return;
    }

    String area =
        ctx.WORD().getText();

    if (ctx.NO() == null) {
      _currentOspfv3Process
          .setNssaArea(
              area,
              ctx.NO_SUMMARY() != null);
      return;
    }

    if (ctx.NO_SUMMARY() != null) {
      /*
       * AOS-CX:
       * no area X nssa no-summary
       * keeps NSSA type and restores inter-area summaries.
       */
      _currentOspfv3Process
          .clearNssaNoSummary(area);
      return;
    }

    /*
     * no area X nssa changes the configured area back to normal.
     */
    _currentOspfv3Process
        .clearNssaArea(area);
  }

  @Override
  public void exitS_ospfv3_area_range(
      S_ospfv3_area_rangeContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 area range outside OSPFv3 context");

      return;
    }

    String area =
        ctx.WORD(0).getText();

    String prefixText =
        ctx.WORD(1).getText();

    if (Prefix6.tryParse(prefixText).isEmpty()) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 area-range IPv6 prefix");

      return;
    }

    Prefix6 prefix =
        Prefix6.parse(prefixText);

    boolean nssa =
        ctx
                .ospfv3_area_range_type()
                .NSSA()
            != null;

    if (ctx.NO() != null) {

      if (ctx.NO_ADVERTISE() != null) {

        if (nssa) {
          _currentOspfv3Process
              .enableNssaRangeAdvertisement(
                  area,
                  prefix);
        } else {
          _currentOspfv3Process
              .enableInterAreaRangeAdvertisement(
                  area,
                  prefix);
        }

      } else if (nssa) {

        _currentOspfv3Process
            .removeNssaRange(
                area,
                prefix);

      } else {

        _currentOspfv3Process
            .removeInterAreaRange(
                area,
                prefix);
      }

      return;
    }

    boolean advertise =
        ctx.NO_ADVERTISE() == null;

    if (nssa) {

      _currentOspfv3Process
          .setNssaRange(
              area,
              prefix,
              advertise);

    } else {

      _currentOspfv3Process
          .setInterAreaRange(
              area,
              prefix,
              advertise);
    }
  }

  @Override
  public void exitS_ospfv3_area_default_metric(
      S_ospfv3_area_default_metricContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPFv3 area default-metric outside OSPFv3 context");
      return;
    }

    String area =
        ctx.WORD(0).getText();

    if (ctx.NO() != null) {
      _currentOspfv3Process
          .clearAreaDefaultMetric(area);
      return;
    }

    long metric;

    try {
      metric =
          Long.parseLong(
              ctx.WORD(1).getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 area default metric");
      return;
    }

    if (metric < 0L
        || metric > 0xFFFFFFL) {
      warn(
          ctx,
          "Ignoring OSPFv3 area default metric outside 0-16777215");
      return;
    }

    _currentOspfv3Process
        .setAreaDefaultMetric(
            area,
            metric);
  }

  @Override
  public void exitS_ospfv3_default_metric(
      S_ospfv3_default_metricContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPFv3 default-metric outside OSPFv3 context");
      return;
    }

    if (ctx.NO() != null) {
      _currentOspfv3Process
          .resetRedistributionMetric();
      return;
    }

    long metric;

    try {
      metric =
          Long.parseLong(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 default metric");
      return;
    }

    if (metric < 1L
        || metric > 0xFFFFFEL) {
      warn(
          ctx,
          "Ignoring OSPFv3 default metric outside 1-16777214");
      return;
    }

    _currentOspfv3Process
        .setRedistributionMetric(metric);
  }

  @Override
  public void exitS_ospfv3_distance(
      S_ospfv3_distanceContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPFv3 distance outside OSPFv3 context");
      return;
    }

    if (ctx.NO() != null) {
      AosCxParser.Ospfv3_distance_typeContext type =
          ctx.ospfv3_distance_type();

      if (type == null) {
        _currentOspfv3Process
            .resetDistance();
      } else if (type.INTRA_AREA() != null) {
        _currentOspfv3Process
            .resetIntraAreaDistance();
      } else if (type.INTER_AREA() != null) {
        _currentOspfv3Process
            .resetInterAreaDistance();
      } else {
        _currentOspfv3Process
            .resetExternalDistance();
      }

      return;
    }

    if (ctx.ospfv3_distance_value().isEmpty()) {
      int distance;

      try {
        distance =
            Integer.parseInt(
                ctx.WORD().getText());
      } catch (NumberFormatException e) {
        warn(
            ctx,
            "Ignoring invalid OSPFv3 administrative distance");
        return;
      }

      if (distance < 1 || distance > 255) {
        warn(
            ctx,
            "Ignoring OSPFv3 administrative distance outside 1-255");
        return;
      }

      _currentOspfv3Process
          .setDistance(distance);
      return;
    }

    for (AosCxParser.Ospfv3_distance_valueContext value :
        ctx.ospfv3_distance_value()) {

      int distance;

      try {
        distance =
            Integer.parseInt(
                value.WORD().getText());
      } catch (NumberFormatException e) {
        warn(
            ctx,
            "Ignoring invalid OSPFv3 administrative distance");
        return;
      }

      if (distance < 1 || distance > 255) {
        warn(
            ctx,
            "Ignoring OSPFv3 administrative distance outside 1-255");
        return;
      }

      if (value
              .ospfv3_distance_type()
              .INTRA_AREA()
          != null) {
        _currentOspfv3Process
            .setIntraAreaDistance(distance);
      } else if (
          value
                  .ospfv3_distance_type()
                  .INTER_AREA()
              != null) {
        _currentOspfv3Process
            .setInterAreaDistance(distance);
      } else {
        _currentOspfv3Process
            .setExternalDistance(distance);
      }
    }
  }

  @Override
  public void exitS_ospfv3_distribute_list(
      S_ospfv3_distribute_listContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPFv3 distribute-list outside OSPFv3 context");
      return;
    }

    boolean inbound =
        ctx.IN() != null;

    if (ctx.NO() != null) {
      if (inbound) {
        _currentOspfv3Process
            .clearDistributeListIn();
      } else {
        _currentOspfv3Process
            .clearDistributeListOut();
      }
      return;
    }

    String prefixList =
        ctx.WORD().getText();

    if (inbound) {
      _currentOspfv3Process
          .setDistributeListIn(prefixList);
    } else {
      _currentOspfv3Process
          .setDistributeListOut(prefixList);
    }
  }

  @Override
  public void exitS_ospfv3_maximum_paths(
      S_ospfv3_maximum_pathsContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring maximum-paths outside OSPFv3 context");
      return;
    }

    if (ctx.NO() != null) {
      _currentOspfv3Process
          .resetMaximumPaths();
      return;
    }

    int maximumPaths;

    try {
      maximumPaths =
          Integer.parseInt(
              ctx.WORD().getText());
    } catch (NumberFormatException e) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 maximum-paths value");
      return;
    }

    if (maximumPaths < 1
        || maximumPaths > 32) {
      warn(
          ctx,
          "Ignoring OSPFv3 maximum-paths outside 1-32");
      return;
    }

    _currentOspfv3Process
        .setMaximumPaths(
            maximumPaths);
  }

  @Override
  public void exitS_ospfv3_summary_address(
      S_ospfv3_summary_addressContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring summary-address outside OSPFv3 context");

      return;
    }

    String prefixText =
        ctx.WORD().getText();

    if (Prefix6.tryParse(prefixText).isEmpty()) {
      warn(
          ctx,
          "Ignoring invalid OSPFv3 summary-address IPv6 prefix");
      return;
    }

    Prefix6 prefix =
        Prefix6.parse(prefixText);

    if (ctx.NO() != null) {
      _currentOspfv3Process
          .removeExternalSummary(prefix);
      return;
    }

    boolean advertise =
        true;

    Long tag =
        null;

    AosCxParser.Ospfv3_summary_address_optionContext option =
        ctx.ospfv3_summary_address_option();

    if (option != null) {

      if (option.NO_ADVERTISE() != null) {
        advertise =
            false;

      } else {

        long parsedTag;

        try {
          parsedTag =
              Long.parseLong(
                  option.WORD().getText());
        } catch (NumberFormatException e) {
          warn(
              ctx,
              "Ignoring invalid OSPFv3 summary-address tag");
          return;
        }

        if (parsedTag < 0L
            || parsedTag > 0xFFFFFFFFL) {

          warn(
              ctx,
              "Ignoring OSPFv3 summary-address tag outside 0-4294967295");
          return;
        }

        tag =
            parsedTag;
      }
    }

    _currentOspfv3Process
        .setExternalSummary(
            prefix,
            advertise,
            tag);
  }

  @Override
  public void exitS_ospfv3_process_state(
      S_ospfv3_process_stateContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring OSPFv3 enable/disable outside OSPFv3 context");
      return;
    }

    _currentOspfv3Process.setEnabled(
        ctx.ENABLE() != null);
  }

  @Override
  public void exitS_ospfv3_default_information(
      S_ospfv3_default_informationContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring default-information outside OSPFv3 context");
      return;
    }

    if (ctx.NO() != null) {
      _currentOspfv3Process
          .disableDefaultInformationOriginate();
      return;
    }

    long metric =
        AosCxOspfv3Process
            .DEFAULT_INFORMATION_METRIC;

    if (ctx.ospfv3_metric_option() != null) {
      try {
        metric =
            Long.parseLong(
                ctx.ospfv3_metric_option()
                    .WORD()
                    .getText());
      } catch (NumberFormatException e) {
        warn(
            ctx,
            "Ignoring invalid OSPFv3 default-information metric");
        return;
      }

      if (metric < 1L
          || metric > 0xFFFFFEL) {
        warn(
            ctx,
            "Ignoring OSPFv3 default-information metric outside 1-16777214");
        return;
      }
    }

    _currentOspfv3Process
        .setDefaultInformationOriginate(
            ctx.ALWAYS() != null,
            metric);
  }

  @Override
  public void exitS_redistribute_connected(
      S_redistribute_connectedContext ctx) {
    if (ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring redistribute connected outside OSPF context");
      return;
    }

    boolean enabled =
        ctx.NO() == null;

    String routeMap =
        ctx.redistribute_route_map() == null
            ? null
            : ctx.redistribute_route_map()
                .WORD()
                .getText();

    if (_currentOspfv3Process != null) {
      _currentOspfv3Process
          .setRedistributeConnected(
              enabled,
              routeMap);
      return;
    }

    if (_currentOspfProcess != null) {
      if (routeMap != null) {
        warn(
            ctx,
            "Ignoring OSPFv2 redistribute connected with route-map");
        return;
      }

      _currentOspfProcess
          .setRedistributeConnected(enabled);
      return;
    }

    warn(
        ctx,
        "Ignoring redistribute connected outside OSPF context");
  }

  @Override
  public void exitS_redistribute_local_loopback(
      S_redistribute_local_loopbackContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring redistribute local loopback outside OSPFv3 context");

      return;
    }

    boolean enabled =
        ctx.NO() == null;

    String routeMap =
        ctx.redistribute_route_map() == null
            ? null
            : ctx.redistribute_route_map()
                .WORD()
                .getText();

    _currentOspfv3Process
        .setRedistributeLocalLoopback(
            enabled,
            routeMap);
  }

  @Override
  public void exitS_redistribute_static(
      S_redistribute_staticContext ctx) {
    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {
      warn(
          ctx,
          "Ignoring redistribute static outside OSPFv3 context");
      return;
    }

    boolean enabled =
        ctx.NO() == null;

    String routeMap =
        ctx.redistribute_route_map() == null
            ? null
            : ctx.redistribute_route_map()
                .WORD()
                .getText();

    _currentOspfv3Process
        .setRedistributeStatic(
            enabled,
            routeMap);
  }

  @Override
  public void exitS_router_ospfv3(
      S_router_ospfv3Context ctx) {
    int processId =
        Integer.parseInt(
            ctx.WORD(0).getText());

    String vrfName =
        ctx.VRF() == null
            ? null
            : ctx.WORD(1).getText();

    if (vrfName != null) {
      _configuration.addVrf(vrfName);
    }

    _currentOspfv3Process =
        _configuration.getOrCreateOspfv3Process(
            processId,
            vrfName);

    _currentOspfProcess = null;
    _currentRouteMapEntry = null;
    _currentInterface = null;
    _currentIpAccessList = null;
    _currentIpv6AccessList = null;
  }

  @Override
  public void exitS_router_ospf(S_router_ospfContext ctx) {
    int processId = Integer.parseInt(ctx.WORD(0).getText());
    String vrfName = ctx.VRF() == null ? null : ctx.WORD(1).getText();

    if (vrfName != null) {
      _configuration.addVrf(vrfName);
    }

    _currentOspfProcess =
        _configuration.getOrCreateOspfProcess(processId, vrfName);
    _currentOspfv3Process = null;
    _currentRouteMapEntry = null;
    _currentInterface = null;
    _currentIpAccessList = null;
    _currentIpv6AccessList = null;
  }

  @Override
  public void exitS_router_id(S_router_idContext ctx) {
    Ip routerId = Ip.parse(ctx.WORD().getText());

    if (_currentOspfProcess != null) {
      _currentOspfProcess.setRouterId(routerId);
      return;
    }

    if (_currentOspfv3Process != null) {
      _currentOspfv3Process.setRouterId(routerId);
      return;
    }

    warn(ctx, "Ignoring router-id outside OSPF context");
  }

  @Override
  public void exitS_mtu(S_mtuContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring MTU outside interface context");
      return;
    }
    _currentInterface.setMtu(Integer.parseInt(ctx.WORD().getText()));
  }

  @Override
  public void exitS_no_routing(S_no_routingContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring no routing outside interface context");
      return;
    }
    _currentInterface.setSwitchport(true);
  }

  @Override
  public void exitS_vlan_trunk_native(S_vlan_trunk_nativeContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring VLAN trunk native outside interface context");
      return;
    }
    _currentInterface.setSwitchport(true);
    _currentInterface.setNativeVlan(Integer.parseInt(ctx.WORD().getText()));
    _currentInterface.setNativeVlanTagged(ctx.TAG() != null);
  }

  @Override
  public void exitS_vlan_trunk_allowed(S_vlan_trunk_allowedContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring VLAN trunk allowed outside interface context");
      return;
    }
    _currentInterface.setSwitchport(true);
    _currentInterface.setAllowedVlans(
        IntegerSpace.parse(ctx.WORD().getText()));
  }

  @Override
  public void exitS_no_shutdown(S_no_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setEnabled(true);
    }
  }

  @Override
  public void exitS_shutdown(S_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setEnabled(false);
    }
  }

  @Override
  public void exitS_speed(S_speedContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring speed command outside interface context");
      return;
    }

    String speed = ctx.WORD(0).getText();

    // Auto-negotiation does not tell us the actual negotiated bandwidth.
    if (speed.equals("auto")) {
      return;
    }

    Double bandwidth =
        switch (speed) {
          case "10-half", "10-full" -> 10_000_000D;
          case "100-half", "100-full" -> 100_000_000D;
          case "1000-full", "1g" -> 1_000_000_000D;
          case "2.5g" -> 2_500_000_000D;
          case "5g" -> 5_000_000_000D;
          case "10g" -> 10_000_000_000D;
          case "25g" -> 25_000_000_000D;
          case "40g" -> 40_000_000_000D;
          case "50g" -> 50_000_000_000D;
          case "100g" -> 100_000_000_000D;
          case "200g" -> 200_000_000_000D;
          case "400g" -> 400_000_000_000D;
          default -> null;
        };

    if (bandwidth != null) {
      _currentInterface.setBandwidth(bandwidth);
    }
  }

  private static String toInterfaceName(Interface_nameContext ctx) {
    String id = ctx.WORD().getText();
    if (ctx.LOOPBACK() != null) {
      return "loopback " + id;
    }
    if (ctx.VLAN() != null) {
      return "vlan " + id;
    }
    if (ctx.LAG() != null) {
      return "lag " + id;
    }
    return id;
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
  private AosCxInterface _currentInterface;
  private AosCxOspfProcess _currentOspfProcess;
  private AosCxOspfv3Process _currentOspfv3Process;
  private AosCxBgpProcess _currentBgpProcess;
  private Long _currentBgpLocalAs;
  private AosCxRouteMapEntry _currentRouteMapEntry;
  private AosCxIpAccessList _currentIpAccessList;
  private AosCxIpv6AccessList _currentIpv6AccessList;
  private boolean _inBgpIpv4Unicast;
}
