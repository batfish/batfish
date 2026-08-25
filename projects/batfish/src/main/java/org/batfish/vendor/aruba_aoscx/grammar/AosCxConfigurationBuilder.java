package org.batfish.vendor.aruba_aoscx.grammar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_authenticationContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_encryptionContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ipv6_ospfv3_bfdContext;
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
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_match_tagContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_match_source_protocolContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_match_route_typeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_route_redistribute_active_routes_onlyContext;
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
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_area_authenticationContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_area_encryptionContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_active_backboneContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_bfd_all_interfacesContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_area_rangeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_area_default_metricContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_default_informationContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_default_metricContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_distanceContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_distribute_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_graceful_restartContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_timers_throttle_spfContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_timers_throttle_lsaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_timers_lsa_arrivalContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_max_metric_router_lsaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_maximum_pathsContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_summary_addressContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_virtual_linkContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_virtual_link_authenticationContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_virtual_link_encryptionContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_virtual_link_hello_intervalContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_virtual_link_dead_intervalContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_virtual_link_retransmit_intervalContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_virtual_link_transit_delayContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ospfv3_process_stateContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_redistribute_connectedContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_redistribute_local_loopbackContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_redistribute_ospfContext;
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
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfv3Authentication;
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfv3Encryption;
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
  public void
      exitS_route_redistribute_active_routes_only(
          S_route_redistribute_active_routes_onlyContext ctx) {

    if (ctx.getStart().getCharPositionInLine() != 0) {

      warn(
          ctx,
          "Ignoring route-redistribute active-routes-only outside global context");

      return;
    }

    _configuration
        .setRouteRedistributeActiveRoutesOnly(
            ctx.NO() == null);
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

  private @Nullable AosCxOspfv3Authentication
      toOspfv3Authentication(
          ParserRuleContext owner,
          AosCxParser.Ospfv3_ipsec_authenticationContext ctx) {

    long spi;

    try {
      spi =
          Long.parseLong(
              ctx.WORD(0).getText());

    } catch (NumberFormatException e) {

      warn(
          owner,
          "Ignoring OSPFv3 IPsec authentication with invalid SPI");

      return null;
    }

    if (spi < 256L
        || spi > 0xFFFFFFFFL) {

      warn(
          owner,
          "Ignoring OSPFv3 IPsec authentication SPI outside 256-4294967295");

      return null;
    }

    AosCxOspfv3Authentication.AuthType authType =
        ctx.ospfv3_auth_type().MD5() != null
            ? AosCxOspfv3Authentication.AuthType.MD5
            : AosCxOspfv3Authentication.AuthType.SHA1;

    AosCxOspfv3Authentication.KeyType keyType =
        null;

    String key =
        null;

    if (ctx.ospfv3_key_type() != null) {

      if (ctx.ospfv3_key_type().PLAINTEXT() != null) {
        keyType =
            AosCxOspfv3Authentication.KeyType.PLAINTEXT;

      } else if (
          ctx.ospfv3_key_type().HEX_STRING() != null) {

        keyType =
            AosCxOspfv3Authentication.KeyType.HEX_STRING;

      } else {

        keyType =
            AosCxOspfv3Authentication.KeyType.CIPHERTEXT;
      }

      key =
          ctx.WORD(1).getText();
    }

    return new AosCxOspfv3Authentication(
        spi,
        authType,
        keyType,
        key);
  }

  private @Nullable AosCxOspfv3Encryption
      toOspfv3Encryption(
          ParserRuleContext owner,
          AosCxParser.Ospfv3_ipsec_encryptionContext ctx) {

    long spi;

    try {
      spi =
          Long.parseLong(
              ctx.WORD(0).getText());

    } catch (NumberFormatException e) {

      warn(
          owner,
          "Ignoring OSPFv3 IPsec ESP with invalid SPI");

      return null;
    }

    if (spi < 256L
        || spi > 0xFFFFFFFFL) {

      warn(
          owner,
          "Ignoring OSPFv3 IPsec ESP SPI outside 256-4294967295");

      return null;
    }

    AosCxOspfv3Encryption.AuthType authType =
        ctx.ospfv3_auth_type().MD5() != null
            ? AosCxOspfv3Encryption.AuthType.MD5
            : AosCxOspfv3Encryption.AuthType.SHA1;

    AosCxOspfv3Encryption.KeyType authKeyType =
        null;

    String authKey =
        null;

    if (ctx.ospfv3_key_type() != null) {

      if (ctx.ospfv3_key_type().PLAINTEXT() != null) {

        authKeyType =
            AosCxOspfv3Encryption.KeyType.PLAINTEXT;

      } else if (
          ctx.ospfv3_key_type().HEX_STRING() != null) {

        authKeyType =
            AosCxOspfv3Encryption.KeyType.HEX_STRING;

      } else {

        authKeyType =
            AosCxOspfv3Encryption.KeyType.CIPHERTEXT;
      }

      authKey =
          ctx.WORD(1).getText();
    }

    AosCxOspfv3Encryption.EncryptionType
        encryptionType =
            null;

    AosCxOspfv3Encryption.KeyType
        encryptionKeyType =
            null;

    String encryptionKey =
        null;

    AosCxParser.Ospfv3_encryption_specContext spec =
        ctx.ospfv3_encryption_spec();

    if (spec != null) {

      if (spec.NULL() != null) {

        encryptionType =
            AosCxOspfv3Encryption.EncryptionType.NULL;

      } else {

        if (spec.ospfv3_encr_type().DES() != null) {

          encryptionType =
              AosCxOspfv3Encryption.EncryptionType.DES;

        } else if (
            spec.ospfv3_encr_type().THREE_DES()
                != null) {

          encryptionType =
              AosCxOspfv3Encryption.EncryptionType.THREE_DES;

        } else {

          encryptionType =
              AosCxOspfv3Encryption.EncryptionType.AES;
        }

        if (spec.ospfv3_key_type() != null) {

          if (spec
                  .ospfv3_key_type()
                  .PLAINTEXT()
              != null) {

            encryptionKeyType =
                AosCxOspfv3Encryption.KeyType.PLAINTEXT;

          } else if (
              spec
                      .ospfv3_key_type()
                      .HEX_STRING()
                  != null) {

            encryptionKeyType =
                AosCxOspfv3Encryption.KeyType.HEX_STRING;

          } else {

            encryptionKeyType =
                AosCxOspfv3Encryption.KeyType.CIPHERTEXT;
          }

          encryptionKey =
              spec.WORD().getText();
        }
      }
    }

    return new AosCxOspfv3Encryption(
        spi,
        authType,
        authKeyType,
        authKey,
        encryptionType,
        encryptionKeyType,
        encryptionKey);
  }

  @Override
  public void exitS_ipv6_ospfv3_authentication(
      S_ipv6_ospfv3_authenticationContext ctx) {

    if (_currentInterface == null) {

      warn(
          ctx,
          "Ignoring OSPFv3 authentication outside interface context");

      return;
    }

    if (ctx.NO() != null) {

      _currentInterface
          .clearOspfv3Authentication();

      return;
    }

    if (ctx.NULL() != null) {

      _currentInterface
          .setOspfv3AuthenticationNull();

      return;
    }

    AosCxOspfv3Authentication authentication =
        toOspfv3Authentication(
            ctx,
            ctx.ospfv3_ipsec_authentication());

    if (authentication == null) {
      return;
    }

    _currentInterface
        .setOspfv3Authentication(
            authentication);
  }

  @Override
  public void exitS_ipv6_ospfv3_encryption(
      S_ipv6_ospfv3_encryptionContext ctx) {

    if (_currentInterface == null) {

      warn(
          ctx,
          "Ignoring OSPFv3 encryption outside interface context");

      return;
    }

    if (ctx.NO() != null) {

      _currentInterface
          .clearOspfv3Encryption();

      return;
    }

    /*
     * This is "ipv6 ospfv3 encryption null", which disables ESP.
     * It is distinct from "encryption ipsec ... null", which retains
     * authenticated ESP with no payload encryption.
     */
    if (ctx.NULL() != null) {

      _currentInterface
          .setOspfv3EncryptionNull();

      return;
    }

    AosCxOspfv3Encryption encryption =
        toOspfv3Encryption(
            ctx,
            ctx.ospfv3_ipsec_encryption());

    if (encryption == null) {
      return;
    }

    _currentInterface
        .setOspfv3Encryption(
            encryption);
  }

  @Override
  public void exitS_ipv6_ospfv3_bfd(
      S_ipv6_ospfv3_bfdContext ctx) {

    if (_currentInterface == null) {

      warn(
          ctx,
          "Ignoring OSPFv3 BFD outside interface context");

      return;
    }

    /*
     * The no form removes the interface override. Effective state then
     * inherits from process-level bfd all-interfaces.
     */
    if (ctx.NO() != null) {

      _currentInterface
          .clearOspfv3Bfd();

      return;
    }

    /*
     * "ipv6 ospfv3 bfd disable" is an explicit false override.
     * Plain "ipv6 ospfv3 bfd" is an explicit true override.
     */
    _currentInterface
        .setOspfv3Bfd(
            ctx.DISABLE() == null);
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
  public void exitS_ospfv3_bfd_all_interfaces(
      S_ospfv3_bfd_all_interfacesContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring bfd all-interfaces outside OSPFv3 context");

      return;
    }

    _currentOspfv3Process
        .setBfdAllInterfaces(
            ctx.NO() == null);
  }

  @Override
  public void exitS_ospfv3_graceful_restart(
      S_ospfv3_graceful_restartContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring graceful-restart outside OSPFv3 context");

      return;
    }

    /*
     * restart-interval
     */
    if (ctx.RESTART_INTERVAL() != null) {

      if (ctx.NO() != null) {

        _currentOspfv3Process
            .resetGracefulRestartInterval();

        return;
      }

      int seconds;

      try {
        seconds =
            Integer.parseInt(
                ctx.WORD().getText());

      } catch (NumberFormatException e) {

        warn(
            ctx,
            "Ignoring invalid OSPFv3 graceful-restart interval");

        return;
      }

      if (seconds < 5
          || seconds > 1800) {

        warn(
            ctx,
            "Ignoring OSPFv3 graceful-restart interval outside 5-1800");

        return;
      }

      _currentOspfv3Process
          .setGracefulRestartIntervalSeconds(
              seconds);

      return;
    }

    /*
     * helper [strict-lsa-check]
     *
     * Disabling helper also clears strict-lsa-check because strict checking
     * has no meaning when this router is not acting as a GR helper.
     */
    if (ctx.HELPER() != null) {

      _currentOspfv3Process
          .setGracefulRestartHelper(
              ctx.NO() == null,
              ctx.NO() == null
                  && ctx.STRICT_LSA_CHECK()
                      != null);

      return;
    }

    /*
     * ignore-lost-interface
     */
    _currentOspfv3Process
        .setGracefulRestartIgnoreLostInterface(
            ctx.NO() == null);
  }

  @Override
  public void exitS_ospfv3_timers_throttle_spf(
      S_ospfv3_timers_throttle_spfContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring timers throttle spf outside OSPFv3 context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .resetSpfThrottleTimers();

      return;
    }

    int startTime;
    int holdTime;
    int maxWaitTime;

    try {

      startTime =
          Integer.parseInt(
              ctx.WORD(0).getText());

      holdTime =
          Integer.parseInt(
              ctx.WORD(1).getText());

      maxWaitTime =
          Integer.parseInt(
              ctx.WORD(2).getText());

    } catch (NumberFormatException e) {

      warn(
          ctx,
          "Ignoring invalid OSPFv3 SPF throttle timers");

      return;
    }

    if (startTime < 1
        || startTime > 600000
        || holdTime < 1
        || holdTime > 600000
        || maxWaitTime < 1
        || maxWaitTime > 600000) {

      warn(
          ctx,
          "Ignoring OSPFv3 SPF throttle timers outside 1-600000 ms");

      return;
    }

    _currentOspfv3Process
        .setSpfThrottleTimers(
            startTime,
            holdTime,
            maxWaitTime);
  }

  @Override
  public void exitS_ospfv3_timers_throttle_lsa(
      S_ospfv3_timers_throttle_lsaContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring timers throttle lsa outside OSPFv3 context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .resetLsaThrottleTimers();

      return;
    }

    int startTime;
    int holdTime;
    int maxWaitTime;

    try {

      startTime =
          Integer.parseInt(
              ctx.WORD(0).getText());

      holdTime =
          Integer.parseInt(
              ctx.WORD(1).getText());

      maxWaitTime =
          Integer.parseInt(
              ctx.WORD(2).getText());

    } catch (NumberFormatException e) {

      warn(
          ctx,
          "Ignoring invalid OSPFv3 LSA throttle timers");

      return;
    }

    if (startTime < 0
        || startTime > 600000
        || holdTime < 0
        || holdTime > 600000
        || maxWaitTime < 0
        || maxWaitTime > 600000) {

      warn(
          ctx,
          "Ignoring OSPFv3 LSA throttle timers outside 0-600000 ms");

      return;
    }

    _currentOspfv3Process
        .setLsaThrottleTimers(
            startTime,
            holdTime,
            maxWaitTime);
  }

  @Override
  public void exitS_ospfv3_timers_lsa_arrival(
      S_ospfv3_timers_lsa_arrivalContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring timers lsa-arrival outside OSPFv3 context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .resetLsaArrivalTime();

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
          "Ignoring invalid OSPFv3 LSA arrival timer");

      return;
    }

    if (delay < 0
        || delay > 600000) {

      warn(
          ctx,
          "Ignoring OSPFv3 LSA arrival timer outside 0-600000 ms");

      return;
    }

    _currentOspfv3Process
        .setLsaArrivalTimeMs(
            delay);
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
  public void exitS_match_tag(
      S_match_tagContext ctx) {

    if (_currentRouteMapEntry == null) {

      warn(
          ctx,
          "Ignoring route-map match tag outside route-map context");

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
          "Ignoring invalid route-map match tag");

      return;
    }

    if (tag < 0L
        || tag > 0xFFFFFFFFL) {

      warn(
          ctx,
          "Ignoring route-map match tag outside 0-4294967295");

      return;
    }

    if (ctx.NO() != null) {

      _currentRouteMapEntry
          .clearMatchTag(tag);

      return;
    }

    _currentRouteMapEntry
        .setMatchTag(tag);
  }

  @Override
  public void exitS_match_source_protocol(
      S_match_source_protocolContext ctx) {

    if (_currentRouteMapEntry == null) {

      warn(
          ctx,
          "Ignoring route-map match source-protocol outside route-map context");

      return;
    }

    if (ctx.NO() != null) {

      _currentRouteMapEntry
          .clearMatchSourceProtocol();

      return;
    }

    AosCxRouteMapEntry.SourceProtocol sourceProtocol;

    if (ctx.route_map_source_protocol().BGP() != null) {

      sourceProtocol =
          AosCxRouteMapEntry.SourceProtocol.BGP;

    } else if (
        ctx.route_map_source_protocol().CONNECTED()
            != null) {

      sourceProtocol =
          AosCxRouteMapEntry.SourceProtocol.CONNECTED;

    } else if (
        ctx.route_map_source_protocol().OSPF()
            != null) {

      sourceProtocol =
          AosCxRouteMapEntry.SourceProtocol.OSPF;

    } else {

      sourceProtocol =
          AosCxRouteMapEntry.SourceProtocol.STATIC;
    }

    _currentRouteMapEntry
        .setMatchSourceProtocol(
            sourceProtocol);
  }

  @Override
  public void exitS_match_route_type(
      S_match_route_typeContext ctx) {

    if (_currentRouteMapEntry == null) {

      warn(
          ctx,
          "Ignoring route-map match route-type outside route-map context");

      return;
    }

    /*
     * Aruba's no form restores the default of not matching external
     * metric type. The optional type following "no" does not create a
     * different stored state.
     */
    if (ctx.NO() != null) {

      _currentRouteMapEntry
          .clearMatchOspfMetricType();

      return;
    }

    _currentRouteMapEntry
        .setMatchOspfMetricType(
            ctx.ospf_external_metric_type()
                    .TYPE_1()
                != null
                ? OspfMetricType.E1
                : OspfMetricType.E2);
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
  public void exitS_ospfv3_area_authentication(
      S_ospfv3_area_authenticationContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 area authentication outside OSPFv3 context");

      return;
    }

    String area =
        ctx.WORD().getText();

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .clearAreaAuthentication(
              area);

      return;
    }

    AosCxOspfv3Authentication authentication =
        toOspfv3Authentication(
            ctx,
            ctx.ospfv3_ipsec_authentication());

    if (authentication == null) {
      return;
    }

    _currentOspfv3Process
        .setAreaAuthentication(
            area,
            authentication);
  }

  @Override
  public void exitS_ospfv3_area_encryption(
      S_ospfv3_area_encryptionContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 area encryption outside OSPFv3 context");

      return;
    }

    String area =
        ctx.WORD().getText();

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .clearAreaEncryption(
              area);

      return;
    }

    AosCxOspfv3Encryption encryption =
        toOspfv3Encryption(
            ctx,
            ctx.ospfv3_ipsec_encryption());

    if (encryption == null) {
      return;
    }

    _currentOspfv3Process
        .setAreaEncryption(
            area,
            encryption);
  }

  @Override
  public void exitS_ospfv3_active_backbone(
      S_ospfv3_active_backboneContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring active-backbone outside OSPFv3 context");

      return;
    }

    _currentOspfv3Process
        .setActiveBackboneStubDefaultRoute(
            ctx.NO() == null);
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
  public void exitS_ospfv3_max_metric_router_lsa(
      S_ospfv3_max_metric_router_lsaContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring max-metric router-lsa outside OSPFv3 context");

      return;
    }

    if (ctx.NO() != null) {

      if (ctx.ON_STARTUP() != null) {
        _currentOspfv3Process
            .clearMaxMetricRouterLsaOnStartup();
      } else {
        _currentOspfv3Process
            .setMaxMetricRouterLsa(false);
      }

      return;
    }

    if (ctx.ON_STARTUP() == null) {
      _currentOspfv3Process
          .setMaxMetricRouterLsa(true);
      return;
    }

    int seconds =
        AosCxOspfv3Process
            .DEFAULT_MAX_METRIC_ROUTER_LSA_ON_STARTUP_SECONDS;

    if (ctx.WORD() != null) {

      try {
        seconds =
            Integer.parseInt(
                ctx.WORD().getText());

      } catch (NumberFormatException e) {

        warn(
            ctx,
            "Ignoring invalid OSPFv3 max-metric router-lsa on-startup interval");

        return;
      }

      if (seconds < 5
          || seconds > 86400) {

        warn(
            ctx,
            "Ignoring OSPFv3 max-metric router-lsa on-startup interval outside 5-86400");

        return;
      }
    }

    _currentOspfv3Process
        .setMaxMetricRouterLsaOnStartupSeconds(
            seconds);
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
  public void exitS_ospfv3_virtual_link(
      S_ospfv3_virtual_linkContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      _currentOspfv3VirtualLinkProcess =
          null;
      _currentOspfv3VirtualLinkArea =
          null;
      _currentOspfv3VirtualLinkPeer =
          null;

      warn(
          ctx,
          "Ignoring virtual-link outside OSPFv3 context");

      return;
    }

    String transitArea =
        ctx.WORD(0).getText();

    String peerText =
        ctx.WORD(1).getText();

    if (Ip.tryParse(peerText).isEmpty()) {

      _currentOspfv3VirtualLinkProcess =
          null;
      _currentOspfv3VirtualLinkArea =
          null;
      _currentOspfv3VirtualLinkPeer =
          null;

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link with invalid peer router ID");

      return;
    }

    Ip peer =
        Ip.parse(peerText);

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .removeVirtualLink(
              transitArea,
              peer);

      _currentOspfv3VirtualLinkProcess =
          null;
      _currentOspfv3VirtualLinkArea =
          null;
      _currentOspfv3VirtualLinkPeer =
          null;

      return;
    }

    _currentOspfv3Process
        .setVirtualLink(
            transitArea,
            peer);

    _currentOspfv3VirtualLinkProcess =
        _currentOspfv3Process;

    _currentOspfv3VirtualLinkArea =
        transitArea;

    _currentOspfv3VirtualLinkPeer =
        peer;
  }

  @Override
  public void exitS_ospfv3_virtual_link_authentication(
      S_ospfv3_virtual_link_authenticationContext ctx) {

    if (_currentOspfv3Process == null
        || _currentOspfv3VirtualLinkProcess
            != _currentOspfv3Process
        || _currentOspfv3VirtualLinkArea == null
        || _currentOspfv3VirtualLinkPeer == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link authentication outside virtual-link context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .clearVirtualLinkAuthentication(
              _currentOspfv3VirtualLinkArea,
              _currentOspfv3VirtualLinkPeer);

      return;
    }

    AosCxOspfv3Authentication authentication =
        toOspfv3Authentication(
            ctx,
            ctx.ospfv3_ipsec_authentication());

    if (authentication == null) {
      return;
    }

    _currentOspfv3Process
        .setVirtualLinkAuthentication(
            _currentOspfv3VirtualLinkArea,
            _currentOspfv3VirtualLinkPeer,
            authentication);
  }

  @Override
  public void exitS_ospfv3_virtual_link_encryption(
      S_ospfv3_virtual_link_encryptionContext ctx) {

    if (_currentOspfv3Process == null
        || _currentOspfv3VirtualLinkProcess
            != _currentOspfv3Process
        || _currentOspfv3VirtualLinkArea == null
        || _currentOspfv3VirtualLinkPeer == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link encryption outside virtual-link context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .clearVirtualLinkEncryption(
              _currentOspfv3VirtualLinkArea,
              _currentOspfv3VirtualLinkPeer);

      return;
    }

    AosCxOspfv3Encryption encryption =
        toOspfv3Encryption(
            ctx,
            ctx.ospfv3_ipsec_encryption());

    if (encryption == null) {
      return;
    }

    _currentOspfv3Process
        .setVirtualLinkEncryption(
            _currentOspfv3VirtualLinkArea,
            _currentOspfv3VirtualLinkPeer,
            encryption);
  }

  @Override
  public void exitS_ospfv3_virtual_link_hello_interval(
      S_ospfv3_virtual_link_hello_intervalContext ctx) {

    if (_currentOspfv3Process == null
        || _currentOspfv3VirtualLinkProcess
            != _currentOspfv3Process
        || _currentOspfv3VirtualLinkArea == null
        || _currentOspfv3VirtualLinkPeer == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link hello-interval outside virtual-link context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .resetVirtualLinkHelloInterval(
              _currentOspfv3VirtualLinkArea,
              _currentOspfv3VirtualLinkPeer);

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
          "Ignoring invalid OSPFv3 virtual-link hello-interval");

      return;
    }

    if (interval < 1
        || interval > 65535) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link hello-interval outside 1-65535 seconds");

      return;
    }

    _currentOspfv3Process
        .setVirtualLinkHelloIntervalSeconds(
            _currentOspfv3VirtualLinkArea,
            _currentOspfv3VirtualLinkPeer,
            interval);
  }

  @Override
  public void exitS_ospfv3_virtual_link_dead_interval(
      S_ospfv3_virtual_link_dead_intervalContext ctx) {

    if (_currentOspfv3Process == null
        || _currentOspfv3VirtualLinkProcess
            != _currentOspfv3Process
        || _currentOspfv3VirtualLinkArea == null
        || _currentOspfv3VirtualLinkPeer == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link dead-interval outside virtual-link context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .resetVirtualLinkDeadInterval(
              _currentOspfv3VirtualLinkArea,
              _currentOspfv3VirtualLinkPeer);

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
          "Ignoring invalid OSPFv3 virtual-link dead-interval");

      return;
    }

    if (interval < 1
        || interval > 65535) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link dead-interval outside 1-65535 seconds");

      return;
    }

    _currentOspfv3Process
        .setVirtualLinkDeadIntervalSeconds(
            _currentOspfv3VirtualLinkArea,
            _currentOspfv3VirtualLinkPeer,
            interval);
  }

  @Override
  public void
      exitS_ospfv3_virtual_link_retransmit_interval(
          S_ospfv3_virtual_link_retransmit_intervalContext ctx) {

    if (_currentOspfv3Process == null
        || _currentOspfv3VirtualLinkProcess
            != _currentOspfv3Process
        || _currentOspfv3VirtualLinkArea == null
        || _currentOspfv3VirtualLinkPeer == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link retransmit-interval outside virtual-link context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .resetVirtualLinkRetransmitInterval(
              _currentOspfv3VirtualLinkArea,
              _currentOspfv3VirtualLinkPeer);

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
          "Ignoring invalid OSPFv3 virtual-link retransmit-interval");

      return;
    }

    if (interval < 1
        || interval > 3600) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link retransmit-interval outside 1-3600 seconds");

      return;
    }

    _currentOspfv3Process
        .setVirtualLinkRetransmitIntervalSeconds(
            _currentOspfv3VirtualLinkArea,
            _currentOspfv3VirtualLinkPeer,
            interval);
  }

  @Override
  public void exitS_ospfv3_virtual_link_transit_delay(
      S_ospfv3_virtual_link_transit_delayContext ctx) {

    if (_currentOspfv3Process == null
        || _currentOspfv3VirtualLinkProcess
            != _currentOspfv3Process
        || _currentOspfv3VirtualLinkArea == null
        || _currentOspfv3VirtualLinkPeer == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link transit-delay outside virtual-link context");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .resetVirtualLinkTransitDelay(
              _currentOspfv3VirtualLinkArea,
              _currentOspfv3VirtualLinkPeer);

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
          "Ignoring invalid OSPFv3 virtual-link transit-delay");

      return;
    }

    if (delay < 1
        || delay > 3600) {

      warn(
          ctx,
          "Ignoring OSPFv3 virtual-link transit-delay outside 1-3600 seconds");

      return;
    }

    _currentOspfv3Process
        .setVirtualLinkTransitDelaySeconds(
            _currentOspfv3VirtualLinkArea,
            _currentOspfv3VirtualLinkPeer,
            delay);
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
  public void exitS_redistribute_ospf(
      S_redistribute_ospfContext ctx) {

    if (_currentOspfv3Process == null
        || ctx.getStart().getCharPositionInLine() == 0) {

      warn(
          ctx,
          "Ignoring redistribute ospf outside OSPFv3 context");

      return;
    }

    int sourceProcessId;

    try {
      sourceProcessId =
          Integer.parseInt(
              ctx.WORD().getText());

    } catch (NumberFormatException e) {

      warn(
          ctx,
          "Ignoring redistribute ospf with invalid process ID");

      return;
    }

    /*
     * AOS-CX 10.13 supports OSPFv3 process IDs 1-63 for this
     * redistribution source.
     */
    if (sourceProcessId < 1
        || sourceProcessId > 63) {

      warn(
          ctx,
          "Ignoring redistribute ospf process ID outside 1-63");

      return;
    }

    if (ctx.NO() != null) {

      _currentOspfv3Process
          .removeRedistributeOspf(
              sourceProcessId);

      return;
    }

    String routeMap =
        ctx.redistribute_route_map() == null
            ? null
            : ctx.redistribute_route_map()
                .WORD()
                .getText();

    _currentOspfv3Process
        .setRedistributeOspf(
            sourceProcessId,
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

    /*
     * "no router ospfv3" removes the process rather than entering router
     * configuration context.
     */
    if (ctx.NO() != null) {

      _configuration.removeOspfv3Process(
          processId,
          vrfName);

      _currentOspfv3Process =
          null;

      _currentOspfv3VirtualLinkProcess =
          null;

      _currentOspfv3VirtualLinkArea =
          null;

      _currentOspfv3VirtualLinkPeer =
          null;

      _currentOspfProcess =
          null;

      _currentRouteMapEntry =
          null;

      _currentInterface =
          null;

      _currentIpAccessList =
          null;

      _currentIpv6AccessList =
          null;

      return;
    }

    if (vrfName != null) {
      _configuration.addVrf(
          vrfName);
    }

    _currentOspfv3Process =
        _configuration.getOrCreateOspfv3Process(
            processId,
            vrfName);

    /*
     * Entering another process invalidates any previously selected
     * virtual-link subcontext.
     */
    _currentOspfv3VirtualLinkProcess =
        null;

    _currentOspfv3VirtualLinkArea =
        null;

    _currentOspfv3VirtualLinkPeer =
        null;

    _currentOspfProcess =
        null;

    _currentRouteMapEntry =
        null;

    _currentInterface =
        null;

    _currentIpAccessList =
        null;

    _currentIpv6AccessList =
        null;
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

  /*
   * The AOS-CX grammar is intentionally a flat statement stream. These
   * fields preserve the selected config-router-vlink6 context so nested
   * authentication commands can be associated with the immediately
   * selected virtual link.
   */
  private AosCxOspfv3Process
      _currentOspfv3VirtualLinkProcess;
  private String _currentOspfv3VirtualLinkArea;
  private Ip _currentOspfv3VirtualLinkPeer;

  private AosCxBgpProcess _currentBgpProcess;
  private Long _currentBgpLocalAs;
  private AosCxRouteMapEntry _currentRouteMapEntry;
  private AosCxIpAccessList _currentIpAccessList;
  private AosCxIpv6AccessList _currentIpv6AccessList;
  private boolean _inBgpIpv4Unicast;
}
