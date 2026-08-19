package org.batfish.vendor.aruba_aoscx.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.Interface_nameContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_access_list_ipContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_acl_entryContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_apply_access_list_ipContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_hostnameContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_interfaceContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_addressContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_areaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_costContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_networkContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_prefix_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_routeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_no_shutdownContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_idContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_bgpContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_match_ip_address_prefix_listContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_route_mapContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_set_local_preferenceContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_router_idContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_address_family_ipv4Context;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_neighbor_activateContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_neighbor_remote_asContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_bgp_neighbor_route_mapContext;
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
import org.batfish.vendor.aruba_aoscx.representation.AosCxPortSpec;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPortSpec.Operator;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPrefixList;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPrefixListEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMapEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxInterface.OspfNetworkType;
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfProcess;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute;
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
    _currentInterface = null;
    _currentRouteMapEntry = null;
    _currentOspfProcess = null;
    _currentBgpProcess = null;
    _inBgpIpv4Unicast = false;
  }

  @Override
  public void exitS_acl_entry(S_acl_entryContext ctx) {
    if (_currentIpAccessList == null) {
      warn(ctx, "Ignoring ACL entry outside IPv4 ACL context");
      return;
    }

    long sequence =
        ctx.WORD() != null
            ? Long.parseLong(ctx.WORD().getText())
            : _currentIpAccessList.getNextSequence();

    LineAction action =
        ctx.acl_action().PERMIT() != null ? LineAction.PERMIT : LineAction.DENY;

    AosCxPortSpec sourcePort =
        ctx.acl_src_port_spec() == null
            ? null
            : toPortSpec(ctx.acl_src_port_spec().acl_port_spec());

    AosCxPortSpec destinationPort =
        ctx.acl_dst_port_spec() == null
            ? null
            : toPortSpec(ctx.acl_dst_port_spec().acl_port_spec());

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
  public void exitS_hostname(S_hostnameContext ctx) {
    _configuration.setHostname(ctx.WORD().getText());
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    String name = toInterfaceName(ctx.interface_name());
    _currentInterface = _configuration.getOrCreateInterface(name);
    _currentRouteMapEntry = null;
    _currentIpAccessList = null;
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
  public void exitS_route_map(S_route_mapContext ctx) {
    String name = ctx.WORD(0).getText();
    long sequence = Long.parseLong(ctx.WORD(1).getText());
    LineAction action =
        ctx.route_map_action().PERMIT() != null ? LineAction.PERMIT : LineAction.DENY;

    _currentRouteMapEntry =
        _configuration.getOrCreateRouteMap(name).getOrCreateEntry(sequence, action);

    _currentInterface = null;
    _currentOspfProcess = null;
    _currentBgpProcess = null;
    _currentIpAccessList = null;
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
  public void exitS_set_local_preference(S_set_local_preferenceContext ctx) {
    if (_currentRouteMapEntry == null) {
      warn(ctx, "Ignoring route-map set outside route-map context");
      return;
    }
    _currentRouteMapEntry.setSetLocalPreference(Long.parseLong(ctx.WORD().getText()));
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
  public void exitS_router_ospf(S_router_ospfContext ctx) {
    int processId = Integer.parseInt(ctx.WORD(0).getText());
    String vrfName = ctx.VRF() == null ? null : ctx.WORD(1).getText();

    if (vrfName != null) {
      _configuration.addVrf(vrfName);
    }

    _currentOspfProcess =
        _configuration.getOrCreateOspfProcess(processId, vrfName);
    _currentRouteMapEntry = null;
    _currentInterface = null;
    _currentIpAccessList = null;
  }

  @Override
  public void exitS_router_id(S_router_idContext ctx) {
    if (_currentOspfProcess == null) {
      warn(ctx, "Ignoring router-id outside OSPF context");
      return;
    }
    _currentOspfProcess.setRouterId(Ip.parse(ctx.WORD().getText()));
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
  private AosCxBgpProcess _currentBgpProcess;
  private Long _currentBgpLocalAs;
  private AosCxRouteMapEntry _currentRouteMapEntry;
  private AosCxIpAccessList _currentIpAccessList;
  private boolean _inBgpIpv4Unicast;
}
