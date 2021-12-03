package org.batfish.grammar.frr;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Long.parseLong;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.grammar.frr.FrrParser.Int_exprContext;
import static org.batfish.representation.frr.CumulusStructureType.ABSTRACT_INTERFACE;
import static org.batfish.representation.frr.CumulusStructureType.IPV6_PREFIX_LIST;
import static org.batfish.representation.frr.CumulusStructureType.IP_AS_PATH_ACCESS_LIST;
import static org.batfish.representation.frr.CumulusStructureType.IP_COMMUNITY_LIST;
import static org.batfish.representation.frr.CumulusStructureType.IP_COMMUNITY_LIST_EXPANDED;
import static org.batfish.representation.frr.CumulusStructureType.IP_COMMUNITY_LIST_STANDARD;
import static org.batfish.representation.frr.CumulusStructureType.IP_PREFIX_LIST;
import static org.batfish.representation.frr.CumulusStructureType.ROUTE_MAP;
import static org.batfish.representation.frr.CumulusStructureType.ROUTE_MAP_ENTRY;
import static org.batfish.representation.frr.CumulusStructureType.VRF;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV4_UNICAST;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV6_UNICAST;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_IPV4_UNICAST_REDISTRIBUTE_CONNECTED_ROUTE_MAP;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_IPV4_UNICAST_REDISTRIBUTE_OSPF_ROUTE_MAP;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_IPV4_UNICAST_REDISTRIBUTE_STATIC_ROUTE_MAP;
import static org.batfish.representation.frr.CumulusStructureUsage.BGP_NETWORK;
import static org.batfish.representation.frr.CumulusStructureUsage.OSPF_REDISTRIBUTE_BGP_ROUTE_MAP;
import static org.batfish.representation.frr.CumulusStructureUsage.OSPF_REDISTRIBUTE_CONNECTED_ROUTE_MAP;
import static org.batfish.representation.frr.CumulusStructureUsage.OSPF_REDISTRIBUTE_STATIC_ROUTE_MAP;
import static org.batfish.representation.frr.CumulusStructureUsage.ROUTE_MAP_CALL;
import static org.batfish.representation.frr.CumulusStructureUsage.ROUTE_MAP_ENTRY_SELF_REFERENCE;
import static org.batfish.representation.frr.CumulusStructureUsage.ROUTE_MAP_MATCH_COMMUNITY_LIST;
import static org.batfish.representation.frr.CumulusStructureUsage.ROUTE_MAP_SET_COMM_LIST_DELETE;
import static org.batfish.representation.frr.FrrConversions.DEFAULT_MAX_MED;
import static org.batfish.representation.frr.FrrConversions.computeRouteMapEntryName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.frr.FrrParser.Access_list_actionContext;
import org.batfish.grammar.frr.FrrParser.Agg_feature_as_setContext;
import org.batfish.grammar.frr.FrrParser.Agg_feature_matching_med_onlyContext;
import org.batfish.grammar.frr.FrrParser.Agg_feature_originContext;
import org.batfish.grammar.frr.FrrParser.Agg_feature_route_mapContext;
import org.batfish.grammar.frr.FrrParser.Agg_feature_summary_onlyContext;
import org.batfish.grammar.frr.FrrParser.Agg_feature_suppress_mapContext;
import org.batfish.grammar.frr.FrrParser.Bgp_redist_typeContext;
import org.batfish.grammar.frr.FrrParser.Frr_configurationContext;
import org.batfish.grammar.frr.FrrParser.Icl_expandedContext;
import org.batfish.grammar.frr.FrrParser.Icl_standardContext;
import org.batfish.grammar.frr.FrrParser.Interface_ospf_costContext;
import org.batfish.grammar.frr.FrrParser.Ip_addressContext;
import org.batfish.grammar.frr.FrrParser.Ip_as_pathContext;
import org.batfish.grammar.frr.FrrParser.Ip_community_list_nameContext;
import org.batfish.grammar.frr.FrrParser.Ip_prefix_lengthContext;
import org.batfish.grammar.frr.FrrParser.Ip_prefix_listContext;
import org.batfish.grammar.frr.FrrParser.Ip_routeContext;
import org.batfish.grammar.frr.FrrParser.Ipv6_prefix_listContext;
import org.batfish.grammar.frr.FrrParser.Line_actionContext;
import org.batfish.grammar.frr.FrrParser.Literal_standard_communityContext;
import org.batfish.grammar.frr.FrrParser.Origin_typeContext;
import org.batfish.grammar.frr.FrrParser.Ospf_areaContext;
import org.batfish.grammar.frr.FrrParser.Ospf_area_range_costContext;
import org.batfish.grammar.frr.FrrParser.Ospf_redist_typeContext;
import org.batfish.grammar.frr.FrrParser.Pl6_line_actionContext;
import org.batfish.grammar.frr.FrrParser.Pl_line_actionContext;
import org.batfish.grammar.frr.FrrParser.Prefix6Context;
import org.batfish.grammar.frr.FrrParser.PrefixContext;
import org.batfish.grammar.frr.FrrParser.Rm_callContext;
import org.batfish.grammar.frr.FrrParser.Rm_descriptionContext;
import org.batfish.grammar.frr.FrrParser.Rmm_as_pathContext;
import org.batfish.grammar.frr.FrrParser.Rmm_communityContext;
import org.batfish.grammar.frr.FrrParser.Rmm_interfaceContext;
import org.batfish.grammar.frr.FrrParser.Rmm_source_protocolContext;
import org.batfish.grammar.frr.FrrParser.Rmm_tagContext;
import org.batfish.grammar.frr.FrrParser.Rmmipa_prefix_lenContext;
import org.batfish.grammar.frr.FrrParser.Rmmipa_prefix_listContext;
import org.batfish.grammar.frr.FrrParser.Rmom_gotoContext;
import org.batfish.grammar.frr.FrrParser.Rmom_nextContext;
import org.batfish.grammar.frr.FrrParser.Rms_as_path_excludeContext;
import org.batfish.grammar.frr.FrrParser.Rms_as_path_prependContext;
import org.batfish.grammar.frr.FrrParser.Rms_comm_listContext;
import org.batfish.grammar.frr.FrrParser.Rms_communityContext;
import org.batfish.grammar.frr.FrrParser.Rms_local_preferenceContext;
import org.batfish.grammar.frr.FrrParser.Rms_metricContext;
import org.batfish.grammar.frr.FrrParser.Rms_metric_typeContext;
import org.batfish.grammar.frr.FrrParser.Rms_srcContext;
import org.batfish.grammar.frr.FrrParser.Rms_tagContext;
import org.batfish.grammar.frr.FrrParser.Rms_weightContext;
import org.batfish.grammar.frr.FrrParser.Rmsipnh_literalContext;
import org.batfish.grammar.frr.FrrParser.Ro_areaContext;
import org.batfish.grammar.frr.FrrParser.Ro_max_metric_router_lsa_administrativeContext;
import org.batfish.grammar.frr.FrrParser.Ro_networkContext;
import org.batfish.grammar.frr.FrrParser.Ro_redistributeContext;
import org.batfish.grammar.frr.FrrParser.Ro_router_idContext;
import org.batfish.grammar.frr.FrrParser.Roa_rangeContext;
import org.batfish.grammar.frr.FrrParser.Rono_networkContext;
import org.batfish.grammar.frr.FrrParser.Ronopi_defaultContext;
import org.batfish.grammar.frr.FrrParser.Ronopi_interface_nameContext;
import org.batfish.grammar.frr.FrrParser.Ropi_defaultContext;
import org.batfish.grammar.frr.FrrParser.Ropi_interface_nameContext;
import org.batfish.grammar.frr.FrrParser.Route_map_nameContext;
import org.batfish.grammar.frr.FrrParser.S_bgpContext;
import org.batfish.grammar.frr.FrrParser.S_interfaceContext;
import org.batfish.grammar.frr.FrrParser.S_routemapContext;
import org.batfish.grammar.frr.FrrParser.S_router_ospfContext;
import org.batfish.grammar.frr.FrrParser.S_vrfContext;
import org.batfish.grammar.frr.FrrParser.Sb_neighborContext;
import org.batfish.grammar.frr.FrrParser.Sb_networkContext;
import org.batfish.grammar.frr.FrrParser.Sb_redistributeContext;
import org.batfish.grammar.frr.FrrParser.Sbaf_ipv4_unicastContext;
import org.batfish.grammar.frr.FrrParser.Sbaf_l2vpn_evpnContext;
import org.batfish.grammar.frr.FrrParser.Sbafi6_importContext;
import org.batfish.grammar.frr.FrrParser.Sbafi_aggregate_addressContext;
import org.batfish.grammar.frr.FrrParser.Sbafi_importContext;
import org.batfish.grammar.frr.FrrParser.Sbafi_neighborContext;
import org.batfish.grammar.frr.FrrParser.Sbafi_networkContext;
import org.batfish.grammar.frr.FrrParser.Sbafi_redistributeContext;
import org.batfish.grammar.frr.FrrParser.Sbafin_activateContext;
import org.batfish.grammar.frr.FrrParser.Sbafin_allowas_inContext;
import org.batfish.grammar.frr.FrrParser.Sbafin_default_originateContext;
import org.batfish.grammar.frr.FrrParser.Sbafin_next_hop_selfContext;
import org.batfish.grammar.frr.FrrParser.Sbafin_remove_private_asContext;
import org.batfish.grammar.frr.FrrParser.Sbafin_route_mapContext;
import org.batfish.grammar.frr.FrrParser.Sbafin_route_reflector_clientContext;
import org.batfish.grammar.frr.FrrParser.Sbafino_neighborContext;
import org.batfish.grammar.frr.FrrParser.Sbafinon_activateContext;
import org.batfish.grammar.frr.FrrParser.Sbafl_advertise_all_vniContext;
import org.batfish.grammar.frr.FrrParser.Sbafl_advertise_default_gwContext;
import org.batfish.grammar.frr.FrrParser.Sbafl_neighborContext;
import org.batfish.grammar.frr.FrrParser.Sbafla_ipv4_unicastContext;
import org.batfish.grammar.frr.FrrParser.Sbafla_ipv6_unicastContext;
import org.batfish.grammar.frr.FrrParser.Sbafln_activateContext;
import org.batfish.grammar.frr.FrrParser.Sbafln_route_mapContext;
import org.batfish.grammar.frr.FrrParser.Sbafln_route_reflector_clientContext;
import org.batfish.grammar.frr.FrrParser.Sbb_cluster_idContext;
import org.batfish.grammar.frr.FrrParser.Sbb_confederationContext;
import org.batfish.grammar.frr.FrrParser.Sbb_max_med_administrativeContext;
import org.batfish.grammar.frr.FrrParser.Sbb_router_idContext;
import org.batfish.grammar.frr.FrrParser.Sbbb_aspath_multipath_relaxContext;
import org.batfish.grammar.frr.FrrParser.Sbbl_limitContext;
import org.batfish.grammar.frr.FrrParser.Sbbl_rangeContext;
import org.batfish.grammar.frr.FrrParser.Sbn_interfaceContext;
import org.batfish.grammar.frr.FrrParser.Sbn_ip6Context;
import org.batfish.grammar.frr.FrrParser.Sbn_ipContext;
import org.batfish.grammar.frr.FrrParser.Sbn_nameContext;
import org.batfish.grammar.frr.FrrParser.Sbn_peer_group_declContext;
import org.batfish.grammar.frr.FrrParser.Sbnobd_ipv4_unicastContext;
import org.batfish.grammar.frr.FrrParser.Sbnp_descriptionContext;
import org.batfish.grammar.frr.FrrParser.Sbnp_ebgp_multihopContext;
import org.batfish.grammar.frr.FrrParser.Sbnp_local_asContext;
import org.batfish.grammar.frr.FrrParser.Sbnp_peer_groupContext;
import org.batfish.grammar.frr.FrrParser.Sbnp_remote_asContext;
import org.batfish.grammar.frr.FrrParser.Sbnp_update_sourceContext;
import org.batfish.grammar.frr.FrrParser.Si_descriptionContext;
import org.batfish.grammar.frr.FrrParser.Si_shutdownContext;
import org.batfish.grammar.frr.FrrParser.Siip_addressContext;
import org.batfish.grammar.frr.FrrParser.Siipo_areaContext;
import org.batfish.grammar.frr.FrrParser.Siipo_costContext;
import org.batfish.grammar.frr.FrrParser.Siipo_network_p2pContext;
import org.batfish.grammar.frr.FrrParser.Snoip_forwardingContext;
import org.batfish.grammar.frr.FrrParser.Standard_communityContext;
import org.batfish.grammar.frr.FrrParser.Sv_routeContext;
import org.batfish.grammar.frr.FrrParser.Sv_vniContext;
import org.batfish.grammar.frr.FrrParser.Uint32Context;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.frr.BgpDynamic6Neighbor;
import org.batfish.representation.frr.BgpDynamicNeighbor;
import org.batfish.representation.frr.BgpInterfaceNeighbor;
import org.batfish.representation.frr.BgpIpNeighbor;
import org.batfish.representation.frr.BgpIpv4UnicastAddressFamily;
import org.batfish.representation.frr.BgpIpv6Neighbor;
import org.batfish.representation.frr.BgpL2VpnEvpnIpv4Unicast;
import org.batfish.representation.frr.BgpL2vpnEvpnAddressFamily;
import org.batfish.representation.frr.BgpNeighbor;
import org.batfish.representation.frr.BgpNeighbor.RemoteAs;
import org.batfish.representation.frr.BgpNeighborIpv4UnicastAddressFamily;
import org.batfish.representation.frr.BgpNeighborIpv4UnicastAddressFamily.RemovePrivateAsMode;
import org.batfish.representation.frr.BgpNeighborL2vpnEvpnAddressFamily;
import org.batfish.representation.frr.BgpNeighborSourceAddress;
import org.batfish.representation.frr.BgpNeighborSourceInterface;
import org.batfish.representation.frr.BgpNetwork;
import org.batfish.representation.frr.BgpPeerGroupNeighbor;
import org.batfish.representation.frr.BgpProcess;
import org.batfish.representation.frr.BgpRedistributionPolicy;
import org.batfish.representation.frr.BgpVrf;
import org.batfish.representation.frr.BgpVrfAddressFamilyAggregateNetworkConfiguration;
import org.batfish.representation.frr.CumulusRoutingProtocol;
import org.batfish.representation.frr.CumulusStructureType;
import org.batfish.representation.frr.CumulusStructureUsage;
import org.batfish.representation.frr.FrrConfiguration;
import org.batfish.representation.frr.FrrInterface;
import org.batfish.representation.frr.FrrVendorConfiguration;
import org.batfish.representation.frr.IpAsPathAccessList;
import org.batfish.representation.frr.IpAsPathAccessListLine;
import org.batfish.representation.frr.IpCommunityList;
import org.batfish.representation.frr.IpCommunityListExpanded;
import org.batfish.representation.frr.IpCommunityListExpandedLine;
import org.batfish.representation.frr.IpCommunityListStandard;
import org.batfish.representation.frr.IpCommunityListStandardLine;
import org.batfish.representation.frr.IpPrefixList;
import org.batfish.representation.frr.IpPrefixListLine;
import org.batfish.representation.frr.Ipv6PrefixList;
import org.batfish.representation.frr.Ipv6PrefixListLine;
import org.batfish.representation.frr.OspfArea;
import org.batfish.representation.frr.OspfAreaRange;
import org.batfish.representation.frr.OspfNetworkArea;
import org.batfish.representation.frr.OspfNetworkType;
import org.batfish.representation.frr.OspfProcess;
import org.batfish.representation.frr.OspfVrf;
import org.batfish.representation.frr.RedistributionPolicy;
import org.batfish.representation.frr.RouteMap;
import org.batfish.representation.frr.RouteMapCall;
import org.batfish.representation.frr.RouteMapContinue;
import org.batfish.representation.frr.RouteMapEntry;
import org.batfish.representation.frr.RouteMapMatchAsPath;
import org.batfish.representation.frr.RouteMapMatchCommunity;
import org.batfish.representation.frr.RouteMapMatchInterface;
import org.batfish.representation.frr.RouteMapMatchIpAddressPrefixLen;
import org.batfish.representation.frr.RouteMapMatchIpAddressPrefixList;
import org.batfish.representation.frr.RouteMapMatchSourceProtocol;
import org.batfish.representation.frr.RouteMapMatchSourceProtocol.Protocol;
import org.batfish.representation.frr.RouteMapMatchTag;
import org.batfish.representation.frr.RouteMapMetricType;
import org.batfish.representation.frr.RouteMapSetCommListDelete;
import org.batfish.representation.frr.RouteMapSetCommunity;
import org.batfish.representation.frr.RouteMapSetExcludeAsPath;
import org.batfish.representation.frr.RouteMapSetIpNextHopLiteral;
import org.batfish.representation.frr.RouteMapSetLocalPreference;
import org.batfish.representation.frr.RouteMapSetMetric;
import org.batfish.representation.frr.RouteMapSetMetricType;
import org.batfish.representation.frr.RouteMapSetPrependAsPath;
import org.batfish.representation.frr.RouteMapSetTag;
import org.batfish.representation.frr.RouteMapSetWeight;
import org.batfish.representation.frr.StaticRoute;
import org.batfish.representation.frr.Vrf;

public class FrrConfigurationBuilder extends FrrParserBaseListener implements SilentSyntaxListener {
  private static final IntegerSpace OSPF_AREA_RANGE_COST_SPACE =
      IntegerSpace.of(Range.closed(0, 16777215));
  private static final IntegerSpace PREFIX_LENGTH_SPACE =
      IntegerSpace.of(Range.closed(0, Prefix.MAX_PREFIX_LENGTH));

  // The vendor config (e.g., cumulus) of which this FRR config is part of
  private final FrrVendorConfiguration _vc;
  private final FrrConfiguration _frr;
  private final FrrCombinedParser _parser;
  private final Warnings _w;
  private final String _text;
  @Nonnull private final SilentSyntaxCollection _silentSyntax;

  private @Nullable Vrf _currentVrf;
  private RouteMapEntry _currentRouteMapEntry;
  private @Nullable BgpVrf _currentBgpVrf;
  private @Nullable BgpNeighbor _currentBgpNeighbor;
  private @Nullable IpPrefixList _currentIpPrefixList;
  private @Nullable Ipv6PrefixList _currentIpv6PrefixList;
  private @Nullable BgpNeighborIpv4UnicastAddressFamily _currentBgpNeighborIpv4UnicastAddressFamily;
  private @Nullable BgpNeighborL2vpnEvpnAddressFamily _currentBgpNeighborL2vpnEvpnAddressFamily;
  private @Nullable FrrInterface _currentInterface;
  private OspfArea _currentOspfArea;
  private OspfVrf _currentOspfVrf;
  private BgpVrfAddressFamilyAggregateNetworkConfiguration _currentAggregate;
  // Interfaces in the frr file are initialized from bottom to top, where bottommost occurrence of a
  // given interface is the only one that matters for determining order.
  private Set<String> _reverseInterfaceInitOrder;

  public FrrConfigurationBuilder(
      FrrVendorConfiguration configuration,
      FrrCombinedParser parser,
      Warnings w,
      String fullText,
      SilentSyntaxCollection silentSyntax) {
    _vc = configuration;
    _frr = configuration.getFrrConfiguration();
    _parser = parser;
    _w = w;
    _text = fullText;
    _silentSyntax = silentSyntax;
    _reverseInterfaceInitOrder = new LinkedHashSet<>();
  }

  @Override
  @Nonnull
  public SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  private @Nonnull StandardCommunity toStandardCommunity(Literal_standard_communityContext ctx) {
    return StandardCommunity.of(
        Integer.parseInt(ctx.high.getText()), Integer.parseInt(ctx.low.getText()));
  }

  private @Nonnull Optional<StandardCommunity> toStandardCommunity(Standard_communityContext ctx) {
    if (ctx.literal != null) {
      return Optional.of(toStandardCommunity(ctx.literal));
    } else if (ctx.INTERNET() != null) {
      return Optional.of(StandardCommunity.INTERNET);
    } else if (ctx.LOCAL_AS() != null) {
      return Optional.of(StandardCommunity.NO_EXPORT_SUBCONFED);
    } else if (ctx.NO_ADVERTISE() != null) {
      return Optional.of(StandardCommunity.NO_ADVERTISE);
    } else if (ctx.NO_EXPORT() != null) {
      return Optional.of(StandardCommunity.NO_EXPORT);
    } else {
      // assume valid but unsupported
      todo(ctx);
      return Optional.empty();
    }
  }

  @Nonnull
  private Long toLong(Uint32Context ctx) {
    return Long.parseUnsignedLong(ctx.getText());
  }

  private long toLong(Ospf_areaContext ctx) {
    if (ctx.ip != null) {
      return toIp(ctx.ip).asLong();
    }
    return toLong(ctx.num);
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Prefix toPrefix(PrefixContext ctx) {
    return Prefix.parse(ctx.getText());
  }

  private static @Nonnull Prefix6 toPrefix6(Prefix6Context ctx) {
    return Prefix6.parse(ctx.getText());
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _vc.setUnrecognized(true);

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
  public Warnings getWarnings() {
    return _w;
  }

  private LongExpr toMetricLongExpr(Int_exprContext ctx) {
    if (ctx.uint32() != null) {
      long val = toLong(ctx.uint32());
      if (ctx.PLUS() != null) {
        return new IncrementMetric(val);
      } else if (ctx.DASH() != null) {
        return new DecrementMetric(val);
      } else {
        return new LiteralLong(val);
      }
    } else {
      /*
       * Unsupported metric long expression - do not add cases unless you
       * know what you are doing
       */
      throw new BatfishException(String.format("Invalid BGP MED metric : " + getFullText(ctx)));
    }
  }

  private static long toLong(TerminalNode t) {
    return Long.parseLong(t.getText());
  }

  private @Nonnull String toString(Ip_community_list_nameContext ctx) {
    return ctx.getText();
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 32-bit
   * decimal integer to an {@link Integer} if it is contained in the provided {@code space}, or else
   * {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace space, String name) {
    int num = Integer.parseInt(ctx.getText());
    if (!space.contains(num)) {
      _w.addWarning(
          messageCtx,
          getFullText(messageCtx),
          _parser,
          String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private Integer toInteger(Interface_ospf_costContext ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, Ip_prefix_lengthContext len) {
    return toIntegerInSpace(ctx, len, PREFIX_LENGTH_SPACE, "prefix length");
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, Ospf_area_range_costContext cost) {
    return toIntegerInSpace(ctx, cost, OSPF_AREA_RANGE_COST_SPACE, "ospf area range cost");
  }

  private void clearOspfPassiveInterface() {
    _frr.getInterfaces()
        .values()
        .forEach(
            iface -> {
              if (iface.getOspf() != null) {
                iface.getOspf().setPassive(null);
              }
            });
  }

  @Override
  public void enterS_bgp(S_bgpContext ctx) {
    if (_frr.getBgpProcess() == null) {
      _frr.setBgpProcess(new BgpProcess());
    }

    if (ctx.vrf_name() == null) {
      _currentBgpVrf = _frr.getBgpProcess().getDefaultVrf();
    } else {
      String vrfName = ctx.vrf_name().getText();
      _currentBgpVrf = new BgpVrf(vrfName);
      _frr.getBgpProcess().getVrfs().put(vrfName, _currentBgpVrf);
      _vc.referenceStructure(
          VRF, vrfName, CumulusStructureUsage.BGP_VRF, ctx.vrf_name().getStart().getLine());
    }
    _currentBgpVrf.setAutonomousSystem(parseLong(ctx.autonomous_system().getText()));
  }

  @Override
  public void exitS_bgp(S_bgpContext ctx) {
    _currentBgpVrf = null;
  }

  @Override
  public void enterSbaf_ipv4_unicast(Sbaf_ipv4_unicastContext ctx) {
    if (_currentBgpVrf.getIpv4Unicast() == null) {
      _currentBgpVrf.setIpv4Unicast(new BgpIpv4UnicastAddressFamily());
    }
  }

  @Override
  public void enterSbaf_l2vpn_evpn(Sbaf_l2vpn_evpnContext ctx) {
    if (_currentBgpVrf.getL2VpnEvpn() == null) {
      _currentBgpVrf.setL2VpnEvpn(new BgpL2vpnEvpnAddressFamily());
    }
  }

  @Override
  public void exitSb_redistribute(Sb_redistributeContext ctx) {
    String routeMap = ctx.route_map_name() == null ? null : ctx.route_map_name().getText();
    handleOspfToBgpRedistribution(ctx, ctx.bgp_redist_type(), routeMap);
  }

  @Override
  public void exitSbafi_redistribute(Sbafi_redistributeContext ctx) {
    String routeMap = ctx.route_map_name() == null ? null : ctx.route_map_name().getText();
    handleOspfToBgpRedistribution(ctx, ctx.bgp_redist_type(), routeMap);
  }

  public void handleOspfToBgpRedistribution(
      ParserRuleContext ctx,
      Bgp_redist_typeContext bgpRedistTypeContext,
      @Nullable String routeMap) {
    CumulusRoutingProtocol srcProtocol;
    CumulusStructureUsage usage;

    if (bgpRedistTypeContext.STATIC() != null) {
      usage = BGP_IPV4_UNICAST_REDISTRIBUTE_STATIC_ROUTE_MAP;
      srcProtocol = CumulusRoutingProtocol.STATIC;
    } else if (bgpRedistTypeContext.CONNECTED() != null) {
      usage = BGP_IPV4_UNICAST_REDISTRIBUTE_CONNECTED_ROUTE_MAP;
      srcProtocol = CumulusRoutingProtocol.CONNECTED;
    } else if (bgpRedistTypeContext.OSPF() != null) {
      usage = BGP_IPV4_UNICAST_REDISTRIBUTE_OSPF_ROUTE_MAP;
      srcProtocol = CumulusRoutingProtocol.OSPF;
    } else {
      throw new BatfishException("Unexpected redistribution protocol");
    }

    if (routeMap != null) {
      _vc.referenceStructure(ROUTE_MAP, routeMap, usage, ctx.getStart().getLine());
    }

    BgpRedistributionPolicy oldRedistributionPolicy;

    _currentBgpVrf.getOrCreateIpv4Unicast();

    oldRedistributionPolicy =
        _currentBgpVrf
            .getIpv4Unicast()
            .getRedistributionPolicies()
            .put(srcProtocol, new BgpRedistributionPolicy(srcProtocol, routeMap));

    if (oldRedistributionPolicy != null) {
      _w.addWarning(
          ctx,
          getFullText(ctx),
          _parser,
          String.format(
              "overwriting BgpRedistributionPolicy for vrf %s, protocol %s",
              _currentBgpVrf.getVrfName(), srcProtocol));
    }
  }

  @Override
  public void exitSbafi_import(Sbafi_importContext ctx) {
    todo(ctx);
    if (ctx.vrf_name() != null) {
      _vc.referenceStructure(
          VRF,
          ctx.vrf_name().getText(),
          BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF,
          ctx.getStart().getLine());
    }
    if (ctx.route_map_name() != null) {
      _vc.referenceStructure(
          ROUTE_MAP,
          ctx.route_map_name().getText(),
          BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitSbafi6_import(Sbafi6_importContext ctx) {
    if (ctx.vrf_name() != null) {
      _vc.referenceStructure(
          VRF,
          ctx.vrf_name().getText(),
          BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF,
          ctx.getStart().getLine());
    }
    if (ctx.route_map_name() != null) {
      _vc.referenceStructure(
          ROUTE_MAP,
          ctx.route_map_name().getText(),
          BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitSbafi_network(Sbafi_networkContext ctx) {
    @Nullable String routeMap = null;
    if (ctx.rm != null) {
      routeMap = getFullText(ctx.rm);
      _vc.referenceStructure(ROUTE_MAP, routeMap, BGP_NETWORK, ctx.getStart().getLine());
    }
    Prefix prefix = toPrefix(ctx.network);
    _currentBgpVrf.getIpv4Unicast().getNetworks().put(prefix, new BgpNetwork(prefix, routeMap));
  }

  @Override
  public void exitSbafl_advertise_all_vni(Sbafl_advertise_all_vniContext ctx) {
    _currentBgpVrf.getL2VpnEvpn().setAdvertiseAllVni(true);
  }

  @Override
  public void exitSbafl_advertise_default_gw(Sbafl_advertise_default_gwContext ctx) {
    _currentBgpVrf.getL2VpnEvpn().setAdvertiseDefaultGw(true);
  }

  @Override
  public void enterSbafla_ipv4_unicast(Sbafla_ipv4_unicastContext ctx) {
    // setting in enter instead of exit since in future we can attach a routemap
    _currentBgpVrf.getL2VpnEvpn().setAdvertiseIpv4Unicast(new BgpL2VpnEvpnIpv4Unicast());
    if (ctx.rm != null) {
      _w.addWarning(
          ctx,
          getFullText(ctx),
          _parser,
          "Route maps in 'advertise ipv4 unicast' are not supported");
      _vc.referenceStructure(
          ROUTE_MAP,
          ctx.rm.getText(),
          BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV4_UNICAST,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void enterSbafla_ipv6_unicast(Sbafla_ipv6_unicastContext ctx) {
    if (ctx.rm != null) {
      _vc.referenceStructure(
          ROUTE_MAP,
          ctx.rm.getText(),
          BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV6_UNICAST,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void enterSbafi_aggregate_address(Sbafi_aggregate_addressContext ctx) {
    _currentAggregate = new BgpVrfAddressFamilyAggregateNetworkConfiguration();
  }

  @Override
  public void exitSbafi_aggregate_address(Sbafi_aggregate_addressContext ctx) {
    Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> aggregateNetworks =
        _currentBgpVrf.getIpv4Unicast().getAggregateNetworks();
    Prefix prefix = Prefix.parse(ctx.IP_PREFIX().getText());
    if (aggregateNetworks.put(prefix, _currentAggregate) != null) {
      _w.addWarning(
          ctx, getFullText(ctx), _parser, "Overwriting aggregate-address for " + prefix.toString());
    }
    _currentAggregate = null;
  }

  @Override
  public void exitAgg_feature_as_set(Agg_feature_as_setContext ctx) {
    todo(ctx);
    _currentAggregate.setAsSet(true);
  }

  @Override
  public void exitAgg_feature_matching_med_only(Agg_feature_matching_med_onlyContext ctx) {
    todo(ctx);
    _currentAggregate.setMatchingMedOnly(true);
  }

  @Override
  public void exitAgg_feature_origin(Agg_feature_originContext ctx) {
    todo(ctx);
    _currentAggregate.setOrigin(toOriginType(ctx.origin_type()));
  }

  private static @Nonnull OriginType toOriginType(Origin_typeContext ctx) {
    if (ctx.EGP() != null) {
      return OriginType.EGP;
    } else if (ctx.IGP() != null) {
      return OriginType.IGP;
    } else {
      assert ctx.INCOMPLETE() != null;
      return OriginType.INCOMPLETE;
    }
  }

  @Override
  public void exitAgg_feature_route_map(Agg_feature_route_mapContext ctx) {
    String routeMapName = toString(ctx.route_map_name());
    _currentAggregate.setRouteMap(routeMapName);
    _vc.referenceStructure(
        CumulusStructureType.ROUTE_MAP,
        routeMapName,
        CumulusStructureUsage.BGP_AGGREGATE_ADDRESS_ROUTE_MAP,
        ctx.getStart().getLine());
  }

  @Override
  public void exitAgg_feature_summary_only(Agg_feature_summary_onlyContext ctx) {
    _currentAggregate.setSummaryOnly(true);
  }

  @Override
  public void exitAgg_feature_suppress_map(Agg_feature_suppress_mapContext ctx) {
    todo(ctx);
    String routeMapName = toString(ctx.route_map_name());
    _currentAggregate.setSuppressMap(routeMapName);
    _vc.referenceStructure(
        CumulusStructureType.ROUTE_MAP,
        routeMapName,
        CumulusStructureUsage.BGP_AGGREGATE_ADDRESS_SUPPRESS_MAP,
        ctx.getStart().getLine());
  }

  private static @Nonnull String toString(Route_map_nameContext ctx) {
    // TODO: validation and return optional
    return ctx.getText();
  }

  @Override
  public void exitSbnp_local_as(Sbnp_local_asContext ctx) {
    long asn = Long.parseLong(ctx.autonomous_system().getText());
    if (_currentBgpNeighbor == null) {
      _w.addWarning(ctx, getFullText(ctx), _parser, "cannot find bgp neighbor");
      return;
    }
    _currentBgpNeighbor.setLocalAs(asn);
    if (ctx.NO_PREPEND() == null || ctx.REPLACE_AS() == null) {
      warn(ctx, "local-as is supported only in 'no-prepend replace-as' mode");
    }
  }

  @Override
  public void exitSbnp_update_source(Sbnp_update_sourceContext ctx) {
    if (_currentBgpNeighbor == null) {
      _w.addWarning(ctx, getFullText(ctx), _parser, "cannot find bgp neighbor");
      return;
    }

    if (ctx.ip != null) {
      _currentBgpNeighbor.setBgpNeighborSource(
          new BgpNeighborSourceAddress(Ip.parse(ctx.ip.getText())));
    } else if (ctx.name != null) {
      _currentBgpNeighbor.setBgpNeighborSource(new BgpNeighborSourceInterface(ctx.name.getText()));
    } else {
      _w.addWarning(ctx, getFullText(ctx), _parser, "either Ip or Interface is needed");
    }
  }

  @Override
  public void enterSbafl_neighbor(Sbafl_neighborContext ctx) {
    String neighborName = ctx.neighbor.getText();
    BgpNeighbor neighbor = _currentBgpVrf.getNeighbors().get(neighborName);
    if (neighbor == null) {
      _w.addWarning(
          ctx,
          getFullText(ctx),
          _parser,
          String.format("neighbor %s does not exist", neighborName));
    } else {
      _currentBgpNeighborL2vpnEvpnAddressFamily = neighbor.getL2vpnEvpnAddressFamily();
      if (_currentBgpNeighborL2vpnEvpnAddressFamily == null) {
        _currentBgpNeighborL2vpnEvpnAddressFamily = new BgpNeighborL2vpnEvpnAddressFamily();
        neighbor.setL2vpnEvpnAddressFamily(_currentBgpNeighborL2vpnEvpnAddressFamily);
      }
    }
  }

  @Override
  public void exitSbafl_neighbor(Sbafl_neighborContext ctx) {
    _currentBgpNeighborL2vpnEvpnAddressFamily = null;
  }

  @Override
  public void enterSbafi_neighbor(Sbafi_neighborContext ctx) {
    String name;
    if (ctx.ip != null) {
      name = ctx.ip.getText();
    } else if (ctx.name != null) {
      name = ctx.name.getText();
    } else {
      throw new BatfishException("neighbor name or address");
    }

    BgpNeighbor bgpNeighbor = _currentBgpVrf.getNeighbors().get(name);
    if (bgpNeighbor == null) {
      _w.addWarning(
          ctx, getFullText(ctx), _parser, String.format("neighbor %s does not exist", name));
    } else {
      _currentBgpNeighborIpv4UnicastAddressFamily = bgpNeighbor.getIpv4UnicastAddressFamily();
      if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
        _currentBgpNeighborIpv4UnicastAddressFamily = new BgpNeighborIpv4UnicastAddressFamily();
        bgpNeighbor.setIpv4UnicastAddressFamily(_currentBgpNeighborIpv4UnicastAddressFamily);
      }
    }
  }

  @Override
  public void exitSbafi_neighbor(Sbafi_neighborContext ctx) {
    _currentBgpNeighborIpv4UnicastAddressFamily = null;
  }

  @Override
  public void enterSbafino_neighbor(Sbafino_neighborContext ctx) {
    String name;
    if (ctx.ipv4 != null) {
      name = ctx.ipv4.getText();
    } else if (ctx.name != null) {
      name = ctx.name.getText();
    } else if (ctx.ipv6 != null) {
      name = ctx.ipv6.getText();
    } else {
      throw new BatfishException("neightbor name or address");
    }

    BgpNeighbor bgpNeighbor = _currentBgpVrf.getNeighbors().get(name);
    if (bgpNeighbor == null) {
      _w.addWarning(
          ctx, getFullText(ctx), _parser, String.format("neighbor %s does not exist", name));
    } else {
      _currentBgpNeighborIpv4UnicastAddressFamily = bgpNeighbor.getIpv4UnicastAddressFamily();
      if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
        _currentBgpNeighborIpv4UnicastAddressFamily = new BgpNeighborIpv4UnicastAddressFamily();
        bgpNeighbor.setIpv4UnicastAddressFamily(_currentBgpNeighborIpv4UnicastAddressFamily);
      }
    }
  }

  @Override
  public void exitSbafino_neighbor(Sbafino_neighborContext ctx) {
    _currentBgpNeighborIpv4UnicastAddressFamily = null;
  }

  @Override
  public void exitSb_network(Sb_networkContext ctx) {
    @Nullable String routeMap = null;
    if (ctx.rm != null) {
      routeMap = getFullText(ctx.rm);
      _vc.referenceStructure(ROUTE_MAP, routeMap, BGP_NETWORK, ctx.getStart().getLine());
    }
    Prefix prefix = toPrefix(ctx.network);
    _currentBgpVrf.addNetwork(new BgpNetwork(prefix, routeMap));
  }

  @Override
  public void exitSbnobd_ipv4_unicast(Sbnobd_ipv4_unicastContext ctx) {
    if (_currentBgpVrf == null) {
      _w.addWarning(ctx, getFullText(ctx), _parser, "cannot find bgp vrf");
      return;
    }
    _currentBgpVrf.setDefaultIpv4Unicast(false);
  }

  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    String name = ctx.name.getText();

    if (_frr.getInterfaces().containsKey(name)) {
      // this interface was already defined in the FRR file
      String newVrfName = ctx.VRF() != null ? ctx.vrf.getText() : DEFAULT_VRF_NAME;
      String oldVrfName = _frr.getInterfaces().get(name).getVrfName();
      if (!newVrfName.equals(oldVrfName)) {
        warn(
            ctx,
            String.format(
                "vrf %s of interface %s does not match previously-defined vrf %s",
                newVrfName, name, oldVrfName));
        _currentInterface = new FrrInterface("dummy", "dummy");
        return;
      }

      setRealCurrentInterface(_frr.getInterfaces().get(name));
      return;
    }

    if (ctx.VRF() != null) {
      // interface with non-default vrf
      // this is OK only if the VRF matches prior definition in the interfaces file or, if no prior
      // definition exists, the vrf is defined in FRR file
      String vrfName = ctx.vrf.getText();
      if (_vc.hasInterface(name)) {
        if (!vrfName.equals(_vc.getInterfaceVrf(name))) {
          warn(
              ctx,
              String.format(
                  "vrf %s of interface %s does not match previously-defined vrf %s",
                  vrfName, name, _vc.getInterfaceVrf(name)));
          _currentInterface = new FrrInterface("dummy", "dummy");
          return;
        }
        setRealCurrentInterface(_frr.getOrCreateInterface(name, vrfName));
        return;
      }

      if (!_frr.getVrfs().containsKey(vrfName)) {
        warn(
            ctx,
            String.format(
                "vrf %s of interface %s has not been defined in FRR configuration file",
                vrfName, name));
        _currentInterface = new FrrInterface("dummy", "dummy");
        return;
      }
      setRealCurrentInterface(_frr.getOrCreateInterface(name, vrfName));
      return;
    }

    // interface with default vrf and defined for the first time in FRR
    setRealCurrentInterface(_frr.getOrCreateInterface(name, DEFAULT_VRF_NAME));
  }

  private void setRealCurrentInterface(FrrInterface newCurrentInterface) {
    _currentInterface = newCurrentInterface;
    String name = newCurrentInterface.getName();
    _reverseInterfaceInitOrder.remove(name);
    _reverseInterfaceInitOrder.add(name);
  }

  @Override
  public void exitFrr_configuration(Frr_configurationContext ctx) {
    _frr.setInterfaceInitOrder(Lists.reverse(ImmutableList.copyOf(_reverseInterfaceInitOrder)));
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitSi_shutdown(Si_shutdownContext ctx) {
    _currentInterface.setShutdown(true);
  }

  @Override
  public void exitSiip_address(Siip_addressContext ctx) {
    if (_currentInterface == null) {
      _w.addWarning(ctx, getFullText(ctx), _parser, "no interfaces found for address declaration");
      return;
    }

    _currentInterface.getIpAddresses().add(ConcreteInterfaceAddress.parse(ctx.ip_prefix.getText()));
  }

  @Override
  public void enterS_router_ospf(S_router_ospfContext ctx) {
    _currentOspfVrf = _frr.getOrCreateOspfProcess().getDefaultVrf();
  }

  @Override
  public void exitS_router_ospf(S_router_ospfContext ctx) {
    _currentOspfVrf = null;
  }

  @Override
  public void enterRo_area(Ro_areaContext ctx) {
    long area = toLong(ctx.area);
    _currentOspfArea = _currentOspfVrf.getOrCreateArea(area);
  }

  @Override
  public void exitRo_area(Ro_areaContext ctx) {
    _currentOspfArea = null;
  }

  @Override
  public void exitRo_max_metric_router_lsa_administrative(
      Ro_max_metric_router_lsa_administrativeContext ctx) {
    _frr.getOspfProcess().setMaxMetricRouterLsa(true);
  }

  @Override
  public void exitRo_network(Ro_networkContext ctx) {
    Prefix p = toPrefix(ctx.pfx);
    long area = toLong(ctx.area);
    _frr.getOspfProcess().getNetworkAreas().put(p, new OspfNetworkArea(p, area));
  }

  @Override
  public void exitRo_router_id(Ro_router_idContext ctx) {
    if (_frr.getOspfProcess() == null) {
      _w.addWarning(ctx, getFullText(ctx), _parser, "No OSPF process configured");
      return;
    }

    _frr.getOspfProcess().getDefaultVrf().setRouterId(Ip.parse(ctx.ip.getText()));
  }

  @Override
  public void exitRo_redistribute(Ro_redistributeContext ctx) {
    String routeMap = ctx.route_map_name() == null ? null : ctx.route_map_name().getText();
    handleToOspfRedistribution(ctx, ctx.ospf_redist_type(), routeMap);
  }

  public void handleToOspfRedistribution(
      ParserRuleContext ctx,
      Ospf_redist_typeContext ospfRedistTypeContext,
      @Nullable String routeMap) {
    CumulusRoutingProtocol srcProtocol;
    CumulusStructureUsage usage;

    if (ospfRedistTypeContext.STATIC() != null) {
      usage = OSPF_REDISTRIBUTE_STATIC_ROUTE_MAP;
      srcProtocol = CumulusRoutingProtocol.STATIC;
    } else if (ospfRedistTypeContext.CONNECTED() != null) {
      usage = OSPF_REDISTRIBUTE_CONNECTED_ROUTE_MAP;
      srcProtocol = CumulusRoutingProtocol.CONNECTED;
    } else if (ospfRedistTypeContext.BGP() != null) {
      usage = OSPF_REDISTRIBUTE_BGP_ROUTE_MAP;
      srcProtocol = CumulusRoutingProtocol.BGP;
    } else {
      throw new BatfishException("Unexpected redistribution protocol");
    }

    if (routeMap != null) {
      _vc.referenceStructure(ROUTE_MAP, routeMap, usage, ctx.getStart().getLine());
    }

    OspfProcess proc = _frr.getOspfProcess();

    RedistributionPolicy oldRedistributionPolicy;
    oldRedistributionPolicy =
        proc.getRedistributionPolicies()
            .put(srcProtocol, new RedistributionPolicy(srcProtocol, routeMap));

    if (oldRedistributionPolicy != null) {
      _w.addWarning(
          ctx,
          getFullText(ctx),
          _parser,
          String.format(
              "overwriting BgpRedistributionPolicy for vrf %s, protocol %s",
              proc.getDefaultVrf().getVrfName(), srcProtocol));
    }
  }

  @Override
  public void exitRoa_range(Roa_rangeContext ctx) {
    Prefix pfx = toPrefix(ctx.pfx);
    OspfAreaRange range = _currentOspfArea.getOrCreateRange(pfx);
    if (ctx.cost != null) {
      toInteger(ctx.getParent(), ctx.cost).ifPresent(range::setCost);
    }
  }

  @Override
  public void exitSi_description(Si_descriptionContext ctx) {
    if (_currentInterface == null) {
      return;
    }
    if (_currentInterface.getAlias() == null) {
      _currentInterface.setAlias(ctx.description.getText());
    }
  }

  @Override
  public void exitSiipo_area(Siipo_areaContext ctx) {
    if (_currentInterface == null) {
      return;
    }
    _currentInterface.getOrCreateOspf().setOspfArea(toLong(ctx.area));
  }

  @Override
  public void exitSbafin_activate(Sbafin_activateContext ctx) {
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      return;
    }
    _currentBgpNeighborIpv4UnicastAddressFamily.setActivated(true);
  }

  @Override
  public void exitSbafinon_activate(Sbafinon_activateContext ctx) {
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      return;
    }
    _currentBgpNeighborIpv4UnicastAddressFamily.setActivated(false);
  }

  @Override
  public void exitSbafin_allowas_in(Sbafin_allowas_inContext ctx) {
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      // TODO: remove this silent ignore from here and other places
      return;
    }
    if (ctx.count != null) {
      _currentBgpNeighborIpv4UnicastAddressFamily.setAllowAsIn(
          Integer.parseInt(ctx.count.getText()));
    } else {
      // If `allowas-in <num>` <num> is ommited, then
      // use a default value which is currently 3 in FRR.
      _currentBgpNeighborIpv4UnicastAddressFamily.setAllowAsIn(3);
    }
  }

  @Override
  public void exitSbafin_default_originate(Sbafin_default_originateContext ctx) {
    // TODO: handle address-family l2vpn-evpn
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      return;
    }
    _currentBgpNeighborIpv4UnicastAddressFamily.setDefaultOriginate(true);
  }

  @Override
  public void exitSbafin_next_hop_self(Sbafin_next_hop_selfContext ctx) {
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      return;
    }
    _currentBgpNeighborIpv4UnicastAddressFamily.setNextHopSelf(true);
    // "force" and "all" are aliases. See https://github.com/FRRouting/frr/pull/4200.
    // The functionality is described at
    // http://docs.frrouting.org/en/latest/bgp.html#clicmd-[no]neighborPEERnext-hop-self[all].
    if (ctx.FORCE() != null || ctx.ALL() != null) {
      _currentBgpNeighborIpv4UnicastAddressFamily.setNextHopSelfAll(true);
    }
  }

  @Override
  public void exitSbafin_remove_private_as(Sbafin_remove_private_asContext ctx) {
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      return;
    }
    if (ctx.REPLACE_AS() != null) {
      _currentBgpNeighborIpv4UnicastAddressFamily.setRemovePrivateAsMode(
          RemovePrivateAsMode.REPLACE_AS);
    } else if (ctx.ALL() != null) {
      _currentBgpNeighborIpv4UnicastAddressFamily.setRemovePrivateAsMode(RemovePrivateAsMode.ALL);
    } else {
      _currentBgpNeighborIpv4UnicastAddressFamily.setRemovePrivateAsMode(RemovePrivateAsMode.BASIC);
    }
  }

  @Override
  public void exitSbafin_route_reflector_client(Sbafin_route_reflector_clientContext ctx) {
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      return;
    }
    _currentBgpNeighborIpv4UnicastAddressFamily.setRouteReflectorClient(true);
  }

  @Override
  public void exitSbafln_activate(Sbafln_activateContext ctx) {
    if (_currentBgpNeighborL2vpnEvpnAddressFamily == null) {
      return;
    }
    _currentBgpNeighborL2vpnEvpnAddressFamily.setActivated(true);
  }

  @Override
  public void exitSbafln_route_map(Sbafln_route_mapContext ctx) {
    String name = ctx.name.getText();
    CumulusStructureUsage usage;
    if (ctx.IN() != null) {
      usage = CumulusStructureUsage.BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_IN;
    } else if (ctx.OUT() != null) {
      usage = CumulusStructureUsage.BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_OUT;
    } else {
      throw new IllegalStateException("only support in and out in route map");
    }
    _w.addWarning(
        ctx,
        getFullText(ctx.getParent()),
        _parser,
        "Routes maps on neighbors in address-family  'l2vpn evpn' are not supported");
    _vc.referenceStructure(ROUTE_MAP, name, usage, ctx.name.getStart().getLine());
  }

  @Override
  public void exitSbafln_route_reflector_client(Sbafln_route_reflector_clientContext ctx) {
    if (_currentBgpNeighborL2vpnEvpnAddressFamily == null) {
      return;
    }
    _currentBgpNeighborL2vpnEvpnAddressFamily.setRouteReflectorClient(true);
  }

  @Override
  public void exitSiipo_network_p2p(Siipo_network_p2pContext ctx) {
    _currentInterface.getOrCreateOspf().setNetwork(OspfNetworkType.POINT_TO_POINT);
  }

  @Override
  public void exitSiipo_cost(Siipo_costContext ctx) {
    _currentInterface.getOrCreateOspf().setCost(toInteger(ctx.interface_ospf_cost()));
  }

  @Override
  public void exitSbbb_aspath_multipath_relax(Sbbb_aspath_multipath_relaxContext ctx) {
    _currentBgpVrf.setAsPathMultipathRelax(true);
  }

  @Override
  public void exitSbb_router_id(Sbb_router_idContext ctx) {
    _currentBgpVrf.setRouterId(Ip.parse(ctx.IP_ADDRESS().getText()));
  }

  @Override
  public void exitSbb_cluster_id(Sbb_cluster_idContext ctx) {
    _currentBgpVrf.setClusterId(
        Ip.parse(ctx.IP_ADDRESS() != null ? ctx.IP_ADDRESS().getText() : null));
  }

  @Override
  public void exitSbb_confederation(Sbb_confederationContext ctx) {
    Long id = toLong(ctx.id);
    _currentBgpVrf.setConfederationId(id);
  }

  @Override
  public void exitSbb_max_med_administrative(Sbb_max_med_administrativeContext ctx) {
    if (ctx.med != null) {
      _currentBgpVrf.setMaxMedAdministrative(Long.parseLong(ctx.med.getText()));
    } else {
      _currentBgpVrf.setMaxMedAdministrative(DEFAULT_MAX_MED);
    }
  }

  @Override
  public void exitSbbl_range(Sbbl_rangeContext ctx) {
    BgpNeighbor bgpNeighbor;
    if (ctx.prefix() != null) {
      Prefix listenRange = toPrefix(ctx.prefix());
      bgpNeighbor =
          _currentBgpVrf
              .getNeighbors()
              .computeIfAbsent(
                  listenRange.toString(), (name) -> new BgpDynamicNeighbor(name, listenRange));
    } else {
      Prefix6 listenRange = toPrefix6(ctx.prefix6());
      bgpNeighbor =
          _currentBgpVrf
              .getNeighbors()
              .computeIfAbsent(
                  listenRange.toString(), (name) -> new BgpDynamic6Neighbor(name, listenRange));
    }
    bgpNeighbor.setPeerGroup(ctx.name.getText());
  }

  @Override
  public void exitSbbl_limit(Sbbl_limitContext ctx) {
    warn(ctx, "Batfish does not limit the number sessions for dynamic BGP neighbors");
  }

  @Override
  public void enterSbn_ip(Sbn_ipContext ctx) {
    _currentBgpNeighbor =
        _currentBgpVrf
            .getNeighbors()
            .computeIfAbsent(
                ctx.ip.getText(), (ipStr) -> new BgpIpNeighbor(ipStr, Ip.parse(ipStr)));
  }

  @Override
  public void enterSbn_ip6(Sbn_ip6Context ctx) {
    _currentBgpNeighbor =
        _currentBgpVrf
            .getNeighbors()
            .computeIfAbsent(
                ctx.ip6.getText(), (ipStr) -> new BgpIpv6Neighbor(ipStr, Ip6.parse(ipStr)));
  }

  @Override
  public void enterSbn_name(Sbn_nameContext ctx) {
    // if neighbor does not already exist, get will return null. That's ok -- the listener for
    // child parse node will create it.
    _currentBgpNeighbor = _currentBgpVrf.getNeighbors().get(ctx.name.getText());
  }

  @Override
  public void exitSb_neighbor(Sb_neighborContext ctx) {
    _currentBgpNeighbor = null;
  }

  @Override
  public void enterSbn_interface(Sbn_interfaceContext ctx) {
    if (_currentBgpNeighbor != null) {
      // warn if it's not an interface
      if (!(_currentBgpNeighbor instanceof BgpInterfaceNeighbor)) {
        String line = getFullText(ctx.getParent()) + getFullText(ctx);
        _w.addWarning(
            ctx,
            line,
            _parser,
            String.format(
                "neighbor %s not declared to be an interface", _currentBgpNeighbor.getName()));
      }
    } else {
      Sbn_nameContext parentCtx = (Sbn_nameContext) ctx.getParent();
      String ifaceName = parentCtx.name.getText();
      _currentBgpNeighbor = new BgpInterfaceNeighbor(ifaceName);
      checkState(
          _currentBgpVrf.getNeighbors().put(ifaceName, _currentBgpNeighbor) == null,
          "neighbor should not already exist since _currentBgpNeighbor was null");
    }
  }

  @Override
  public void exitSbn_peer_group_decl(Sbn_peer_group_declContext ctx) {
    if (_currentBgpNeighbor != null) {
      String line = getFullText(ctx.getParent()) + getFullText(ctx);
      _w.addWarning(
          ctx,
          line,
          _parser,
          String.format("neighbor %s already defined", _currentBgpNeighbor.getName()));
    }

    Sbn_nameContext parentCtx = (Sbn_nameContext) ctx.getParent();
    String peerGroupName = parentCtx.name.getText();
    checkState(
        _currentBgpVrf.getNeighbors().put(peerGroupName, new BgpPeerGroupNeighbor(peerGroupName))
            == null,
        "neighbor should not already exist since _currentBgpNeighbor was null");
  }

  @Override
  public void exitSbnp_description(Sbnp_descriptionContext ctx) {
    _currentBgpNeighbor.setDescription(ctx.REMARK_TEXT().getText());
  }

  @Override
  public void exitSbnp_peer_group(Sbnp_peer_groupContext ctx) {
    _currentBgpNeighbor.setPeerGroup(ctx.name.getText());
  }

  @Override
  public void exitSbnp_remote_as(Sbnp_remote_asContext ctx) {
    assert _currentBgpNeighbor != null;
    if (ctx.autonomous_system() != null) {
      _currentBgpNeighbor.setRemoteAs(
          RemoteAs.explicit(parseLong(ctx.autonomous_system().getText())));
    } else if (ctx.EXTERNAL() != null) {
      _currentBgpNeighbor.setRemoteAs(RemoteAs.external());
    } else {
      assert ctx.INTERNAL() != null;
      _currentBgpNeighbor.setRemoteAs(RemoteAs.internal());
    }
  }

  @Override
  public void exitSbnp_ebgp_multihop(Sbnp_ebgp_multihopContext ctx) {
    if (ctx.num != null) {
      warn(
          ctx.getParent(),
          "Neighbor recognized as ebgp-multihop, but distance limit is not enforced");
    }
    _currentBgpNeighbor.setEbgpMultihop(true);
  }

  @Override
  public void enterS_vrf(S_vrfContext ctx) {
    String name = ctx.name.getText();

    // VRFs are declared in /etc/network/interfaces file, but this is part of the definition
    _currentVrf = _frr.getOrCreateVrf(name);
    _vc.defineStructure(VRF, name, ctx);
  }

  @Override
  public void exitS_vrf(S_vrfContext ctx) {
    _currentVrf = null;
  }

  @Override
  public void exitSbafin_route_map(Sbafin_route_mapContext ctx) {
    if (_currentBgpNeighborIpv4UnicastAddressFamily == null) {
      return;
    }
    String name = ctx.name.getText();
    CumulusStructureUsage usage;
    if (ctx.IN() != null) {
      usage = CumulusStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_IN;
      _currentBgpNeighborIpv4UnicastAddressFamily.setRouteMapIn(name);
    } else if (ctx.OUT() != null) {
      usage = CumulusStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_OUT;
      _currentBgpNeighborIpv4UnicastAddressFamily.setRouteMapOut(name);
    } else {
      throw new IllegalStateException("only support in and out in route map");
    }
    _vc.referenceStructure(ROUTE_MAP, name, usage, ctx.name.getStart().getLine());
  }

  @Override
  public void exitSv_vni(Sv_vniContext ctx) {
    int vni = Integer.parseInt(ctx.vni.v.getText());
    _currentVrf.setVni(vni);
  }

  @Override
  public void exitSv_route(Sv_routeContext ctx) {
    // If an interface name is parsed, use it.
    final String next_hop_interface =
        ctx.next_hop_interface != null ? ctx.next_hop_interface.getText() : null;
    // If user provides an IP next-hop instead of an interface, use that
    final Ip next_hop_ip = ctx.next_hop_ip != null ? Ip.parse(ctx.next_hop_ip.getText()) : null;
    Prefix network = Prefix.parse(ctx.prefix().getText());

    final Integer distance = ctx.distance != null ? Integer.parseInt(ctx.distance.getText()) : null;

    _currentVrf
        .getStaticRoutes()
        .add(new StaticRoute(network, next_hop_ip, next_hop_interface, distance));
  }

  @Override
  public void enterS_routemap(S_routemapContext ctx) {
    int sequence = Integer.parseInt(ctx.sequence.getText());
    String name = ctx.name.getText();
    LineAction action;
    if (ctx.action.permit != null) {
      action = LineAction.PERMIT;
    } else if (ctx.action.deny != null) {
      action = LineAction.DENY;
    } else {
      throw new IllegalStateException("only support permit and deny in route map");
    }
    _currentRouteMapEntry =
        _frr.getRouteMaps()
            .computeIfAbsent(name, RouteMap::new)
            .getEntries()
            .computeIfAbsent(sequence, k -> new RouteMapEntry(sequence, action));
    _vc.defineStructure(ROUTE_MAP, name, ctx);

    String routeMapEntryName = computeRouteMapEntryName(name, sequence);
    _vc.defineStructure(ROUTE_MAP_ENTRY, routeMapEntryName, ctx);
    _vc.referenceStructure(
        ROUTE_MAP_ENTRY,
        routeMapEntryName,
        ROUTE_MAP_ENTRY_SELF_REFERENCE,
        ctx.getStart().getLine());
  }

  @Override
  public void exitRono_network(Rono_networkContext ctx) {
    Prefix prefix = toPrefix(ctx.pfx);
    Map<Prefix, OspfNetworkArea> networkAreas = _frr.getOspfProcess().getNetworkAreas();
    OspfNetworkArea na = networkAreas.get(prefix);
    if (na == null) {
      warn(
          ctx.getParent(),
          String.format("There is no area already defined for network %s", prefix));
      return;
    }
    long area = toLong(ctx.area);
    if (na.getArea() != area) {
      warn(
          ctx.getParent(),
          String.format(
              "The area already defined for network %s is %s (%d), not %s (%d)",
              prefix, Ip.create(na.getArea()), na.getArea(), Ip.create(area), area));
      return;
    }
    // Correctly remove the setting.
    networkAreas.remove(prefix);
  }

  @Override
  public void exitRonopi_default(Ronopi_defaultContext ctx) {
    clearOspfPassiveInterface();
    _frr.getOspfProcess().setDefaultPassiveInterface(false);
  }

  @Override
  public void exitRonopi_interface_name(Ronopi_interface_nameContext ctx) {
    String ifaceName = ctx.name.getText();
    if (!_vc.hasInterface(ifaceName) && !_frr.getInterfaces().containsKey(ifaceName)) {
      _w.addWarning(
          ctx, getFullText(ctx), _parser, String.format("interface %s is not defined", ifaceName));
      return;
    }
    _frr.getOrCreateInterface(ifaceName).getOrCreateOspf().setPassive(false);
  }

  @Override
  public void exitRopi_default(Ropi_defaultContext ctx) {
    clearOspfPassiveInterface();
    _frr.getOspfProcess().setDefaultPassiveInterface(true);
  }

  @Override
  public void exitRopi_interface_name(Ropi_interface_nameContext ctx) {
    String ifaceName = ctx.name.getText();
    if (!_vc.hasInterface(ifaceName) && !_frr.getInterfaces().containsKey(ifaceName)) {
      _w.addWarning(
          ctx, getFullText(ctx), _parser, String.format("interface %s is not defined", ifaceName));
      return;
    }
    _frr.getOrCreateInterface(ifaceName).getOrCreateOspf().setPassive(true);
  }

  @Override
  public void exitRm_description(Rm_descriptionContext ctx) {
    _currentRouteMapEntry.setDescription(ctx.route_map_description().getText());
  }

  @Override
  public void exitS_routemap(S_routemapContext ctx) {
    _currentRouteMapEntry = null;
  }

  @Override
  public void exitRm_call(Rm_callContext ctx) {
    String name = ctx.name.getText();
    _currentRouteMapEntry.setCall(new RouteMapCall(name));
    _vc.referenceStructure(ROUTE_MAP, name, ROUTE_MAP_CALL, ctx.getStart().getLine());
  }

  @Override
  public void exitRmm_as_path(Rmm_as_pathContext ctx) {
    String name = ctx.name.getText();
    _currentRouteMapEntry.setMatchAsPath(new RouteMapMatchAsPath(name));
    _vc.referenceStructure(
        IP_AS_PATH_ACCESS_LIST,
        name,
        CumulusStructureUsage.ROUTE_MAP_MATCH_AS_PATH,
        ctx.getStart().getLine());
  }

  @Override
  public void exitRmm_community(Rmm_communityContext ctx) {
    ctx.names.forEach(
        name ->
            _vc.referenceStructure(
                IP_COMMUNITY_LIST, name.getText(),
                ROUTE_MAP_MATCH_COMMUNITY_LIST, name.getStart().getLine()));

    _currentRouteMapEntry.setMatchCommunity(
        new RouteMapMatchCommunity(
            ImmutableList.<String>builder()
                // add old names
                .addAll(
                    Optional.ofNullable(_currentRouteMapEntry.getMatchCommunity())
                        .map(RouteMapMatchCommunity::getNames)
                        .orElse(ImmutableList.of()))
                // add new names
                .addAll(ctx.names.stream().map(RuleContext::getText).iterator())
                .build()));
  }

  @Override
  public void exitRmm_interface(Rmm_interfaceContext ctx) {
    String name = ctx.name.getText();
    _currentRouteMapEntry.setMatchInterface(new RouteMapMatchInterface(ImmutableSet.of(name)));
    _vc.referenceStructure(
        ABSTRACT_INTERFACE,
        name,
        CumulusStructureUsage.ROUTE_MAP_MATCH_INTERFACE,
        ctx.getStart().getLine());
  }

  @Override
  public void exitRmmipa_prefix_len(Rmmipa_prefix_lenContext ctx) {
    Optional<Integer> maybeLen = toInteger(ctx, ctx.len);
    maybeLen.ifPresent(
        len ->
            _currentRouteMapEntry.setMatchIpAddressPrefixLen(
                new RouteMapMatchIpAddressPrefixLen(len)));
  }

  @Override
  public void exitRmmipa_prefix_list(Rmmipa_prefix_listContext ctx) {
    String name = ctx.name.getText();
    RouteMapMatchIpAddressPrefixList matchPrefixList =
        _currentRouteMapEntry.getMatchIpAddressPrefixList();
    List<String> prefixNameList =
        matchPrefixList == null ? new ArrayList<>() : new ArrayList<>(matchPrefixList.getNames());
    prefixNameList.add(name);

    _currentRouteMapEntry.setMatchIpAddressPrefixList(
        new RouteMapMatchIpAddressPrefixList(prefixNameList));

    _vc.referenceStructure(
        IP_PREFIX_LIST,
        name,
        CumulusStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST,
        ctx.getStart().getLine());
  }

  @Override
  public void exitRmm_source_protocol(Rmm_source_protocolContext ctx) {
    RouteMapMatchSourceProtocol p = null;
    if (ctx.BGP() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.BGP);
    } else if (ctx.CONNECTED() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.CONNECTED);
    } else if (ctx.EIGRP() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.EIGRP);
    } else if (ctx.ISIS() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.ISIS);
    } else if (ctx.KERNEL() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.KERNEL);
    } else if (ctx.OSPF() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.OSPF);
    } else if (ctx.RIP() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.RIP);
    } else if (ctx.STATIC() != null) {
      p = new RouteMapMatchSourceProtocol(Protocol.STATIC);
    }
    assert p != null; // or else we're missing something in the if statement.
    _currentRouteMapEntry.setMatchSourceProtocol(p);
  }

  @Override
  public void exitRmm_tag(Rmm_tagContext ctx) {
    _currentRouteMapEntry.setMatchTag(new RouteMapMatchTag(toLong(ctx.tag)));
  }

  @Override
  public void exitRmom_next(Rmom_nextContext ctx) {
    _currentRouteMapEntry.setContinue(new RouteMapContinue(null));
  }

  @Override
  public void exitRmom_goto(Rmom_gotoContext ctx) {
    int seq = Integer.parseInt(ctx.seq.getText());
    _currentRouteMapEntry.setContinue(new RouteMapContinue(seq));
  }

  @Override
  public void exitRms_as_path_exclude(Rms_as_path_excludeContext ctx) {
    List<Long> asns =
        ctx.as_path.asns.stream().map(this::toLong).collect(ImmutableList.toImmutableList());
    _currentRouteMapEntry.setSetExcludeAsPath(new RouteMapSetExcludeAsPath(asns));
  }

  @Override
  public void exitRms_as_path_prepend(Rms_as_path_prependContext ctx) {
    List<Long> asns =
        ctx.as_path.asns.stream().map(this::toLong).collect(ImmutableList.toImmutableList());
    _currentRouteMapEntry.setSetAsPath(new RouteMapSetPrependAsPath(asns));
  }

  @Override
  public void exitRms_metric(Rms_metricContext ctx) {
    LongExpr val = toMetricLongExpr(ctx.metric);
    _currentRouteMapEntry.setSetMetric(new RouteMapSetMetric(val));
  }

  @Override
  public void exitRms_metric_type(Rms_metric_typeContext ctx) {
    RouteMapMetricType type;
    if (ctx.TYPE_1() != null) {
      type = RouteMapMetricType.TYPE_1;
    } else if (ctx.TYPE_2() != null) {
      type = RouteMapMetricType.TYPE_2;
    } else {
      // assume valid but unsupported
      todo(ctx);
      return;
    }
    _currentRouteMapEntry.setSetMetricType(new RouteMapSetMetricType(type));
  }

  @Override
  public void exitRms_src(Rms_srcContext ctx) {
    if (ctx.ip_address() != null) {
      todo(ctx);
    }
    // no warning for v6
  }

  @Override
  public void exitRms_weight(Rms_weightContext ctx) {
    Integer val = Integer.parseInt(ctx.weight.getText());
    _currentRouteMapEntry.setSetWeight(new RouteMapSetWeight(val));
  }

  @Override
  public void exitRmsipnh_literal(Rmsipnh_literalContext ctx) {
    Ip ip = Ip.parse(ctx.next_hop.getText());
    RouteMapSetIpNextHopLiteral setNextHop = _currentRouteMapEntry.getSetIpNextHop();
    if (setNextHop != null) {
      _w.addWarning(
          ctx, getFullText(ctx), _parser, "next-hop already exists will be replaced by this one");
    }

    _currentRouteMapEntry.setSetIpNextHop(new RouteMapSetIpNextHopLiteral(ip));
  }

  @Override
  public void exitRms_community(Rms_communityContext ctx) {
    RouteMapSetCommunity old = _currentRouteMapEntry.getSetCommunity();
    if (old != null) {
      _w.addWarning(ctx, getFullText(ctx), _parser, "overwriting set community");
    }
    Optional<Set<StandardCommunity>> communities = toStandardCommunitySet(ctx.communities);
    if (!communities.isPresent()) {
      return;
    }
    boolean additive = ctx.ADDITIVE() != null;
    _currentRouteMapEntry.setSetCommunity(new RouteMapSetCommunity(communities.get(), additive));
  }

  @Override
  public void exitRms_comm_list(Rms_comm_listContext ctx) {
    String name = toString(ctx.name);
    if (Strings.isNullOrEmpty(name)) {
      return;
    }
    _currentRouteMapEntry.setSetCommListDelete(new RouteMapSetCommListDelete(name));
    _vc.referenceStructure(
        IP_COMMUNITY_LIST, name, ROUTE_MAP_SET_COMM_LIST_DELETE, ctx.getStart().getLine());
  }

  @Override
  public void exitRms_local_preference(Rms_local_preferenceContext ctx) {
    _currentRouteMapEntry.setSetLocalPreference(new RouteMapSetLocalPreference(toLong(ctx.pref)));
  }

  @Override
  public void exitRms_tag(Rms_tagContext ctx) {
    _currentRouteMapEntry.setSetTag(new RouteMapSetTag(toLong(ctx.tag)));
  }

  @Override
  public void exitSnoip_forwarding(Snoip_forwardingContext ctx) {
    _w.todo(ctx, "no ip forwarding", _parser);
  }

  private static @Nonnull LineAction toLineAction(Line_actionContext ctx) {
    if (ctx.deny != null) {
      return LineAction.DENY;
    } else {
      return LineAction.PERMIT;
    }
  }

  @Override
  public void enterIcl_expanded(Icl_expandedContext ctx) {
    String name = toString(ctx.name);
    if (Strings.isNullOrEmpty(name)) {
      return;
    }
    String regex =
        ctx.quoted != null
            ? ctx.quoted.text != null ? ctx.quoted.text.getText() : ""
            : ctx.regex.getText();
    IpCommunityList communityList =
        _frr.getIpCommunityLists().computeIfAbsent(name, IpCommunityListExpanded::new);
    if (!(communityList instanceof IpCommunityListExpanded)) {
      warn(
          ctx,
          String.format(
              "Cannot define expanded community-list '%s' because another community-list with that"
                  + " name but a different type already exists.",
              name));
      return;
    }
    IpCommunityListExpanded communityListExpanded = (IpCommunityListExpanded) communityList;
    communityListExpanded
        .getLines()
        .add(new IpCommunityListExpandedLine(toLineAction(ctx.action), regex));
    _vc.defineStructure(IP_COMMUNITY_LIST_EXPANDED, name, ctx);
  }

  @Override
  public void enterIcl_standard(Icl_standardContext ctx) {
    Optional<Set<StandardCommunity>> communities = toStandardCommunitySet(ctx.communities);
    if (!communities.isPresent()) {
      return;
    }
    String name = toString(ctx.name);
    if (Strings.isNullOrEmpty(name)) {
      return;
    }
    IpCommunityList communityList =
        _frr.getIpCommunityLists().computeIfAbsent(name, IpCommunityListStandard::new);
    if (!(communityList instanceof IpCommunityListStandard)) {
      warn(
          ctx,
          String.format(
              "Cannot define standard community-list '%s' because another community-list with that"
                  + " name but a different type already exists.",
              name));
      return;
    }
    IpCommunityListStandard communityListStandard = (IpCommunityListStandard) communityList;
    communityListStandard
        .getLines()
        .add(new IpCommunityListStandardLine(toLineAction(ctx.action), communities.get()));
    _vc.defineStructure(IP_COMMUNITY_LIST_STANDARD, name, ctx);
  }

  private @Nonnull Optional<Set<StandardCommunity>> toStandardCommunitySet(
      Iterable<Standard_communityContext> communities) {
    ImmutableSet.Builder<StandardCommunity> builder = ImmutableSet.builder();
    for (Standard_communityContext communityCtx : communities) {
      Optional<StandardCommunity> community = toStandardCommunity(communityCtx);
      if (!community.isPresent()) {
        return Optional.empty();
      }
      builder.add(community.get());
    }
    return Optional.of(builder.build());
  }

  @Override
  public void enterIp_prefix_list(Ip_prefix_listContext ctx) {
    String name = ctx.name.getText();
    _currentIpPrefixList = _frr.getIpPrefixLists().computeIfAbsent(name, IpPrefixList::new);
    _vc.defineStructure(IP_PREFIX_LIST, name, ctx);
  }

  @Override
  public void exitIp_prefix_list(Ip_prefix_listContext ctx) {
    _currentIpPrefixList = null;
  }

  @Override
  public void enterIpv6_prefix_list(Ipv6_prefix_listContext ctx) {
    String name = ctx.name.getText();
    _currentIpv6PrefixList = _frr.getIpv6PrefixLists().computeIfAbsent(name, Ipv6PrefixList::new);
    _vc.defineStructure(IPV6_PREFIX_LIST, name, ctx);
  }

  @Override
  public void exitIpv6_prefix_list(Ipv6_prefix_listContext ctx) {
    _currentIpv6PrefixList = null;
  }

  @Override
  public void exitIp_route(Ip_routeContext ctx) {
    // If an interface name is parsed, use it.
    final String next_hop_interface =
        ctx.next_hop_interface != null ? ctx.next_hop_interface.getText() : null;
    // If user provides an IP next-hop instead of an interface, use that
    final Ip next_hop_ip = ctx.next_hop_ip != null ? Ip.parse(ctx.next_hop_ip.getText()) : null;
    // If admin distance is specified, set it
    final Integer distance = ctx.distance != null ? Integer.parseInt(ctx.distance.getText()) : null;

    StaticRoute route =
        new StaticRoute(
            Prefix.parse(ctx.network.getText()), next_hop_ip, next_hop_interface, distance);
    if (ctx.vrf == null) {
      _frr.getStaticRoutes().add(route);
    } else {
      String vrfName = ctx.vrf.getText();
      if (!_vc.hasVrf(vrfName) && !_frr.getVrfs().containsKey(vrfName)) {
        _w.redFlag(
            String.format("the static route is ignored since vrf %s is not defined", vrfName));
        return;
      }
      _frr.getOrCreateVrf(vrfName).getStaticRoutes().add(route);
      _vc.referenceStructure(
          VRF, vrfName, CumulusStructureUsage.STATIC_ROUTE_VRF, ctx.vrf.getStart().getLine());
    }
  }

  private LineAction toLineAction(Access_list_actionContext ctx) {
    if (ctx.PERMIT() != null) {
      return LineAction.PERMIT;
    } else {
      assert ctx.DENY() != null;
      return LineAction.DENY;
    }
  }

  @Override
  public void exitIp_as_path(Ip_as_pathContext ctx) {
    String name = ctx.name.getText();
    LineAction action = toLineAction(ctx.action);
    String regex = ctx.as_path_regex.getText();
    _frr.getIpAsPathAccessLists()
        .computeIfAbsent(name, IpAsPathAccessList::new)
        .addLine(new IpAsPathAccessListLine(action, regex));
    _vc.defineStructure(IP_AS_PATH_ACCESS_LIST, name, ctx);
  }

  @Override
  public void exitPl_line_action(Pl_line_actionContext ctx) {
    long num;
    if (ctx.num != null) {
      num = toLong(ctx.num);
    } else {
      // Round up to the next multiple of 5
      // http://docs.frrouting.org/en/latest/filter.html#clicmd-ipprefix-listNAMEseqNUMBER(permit|deny)PREFIX[leLEN][geLEN]
      Long lastNum =
          _currentIpPrefixList.getLines().isEmpty()
              ? 0L
              : _currentIpPrefixList.getLines().lastKey();
      num = nextMultipleOfFive(lastNum);
    }
    LineAction action = ctx.action.permit != null ? LineAction.PERMIT : LineAction.DENY;

    if (ctx.ANY() != null) {
      _currentIpPrefixList.addLine(
          new IpPrefixListLine(
              action, num, Prefix.ZERO, new SubRange(0, Prefix.MAX_PREFIX_LENGTH)));
      return;
    }

    Prefix prefix = Prefix.parse(ctx.ip_prefix.getText());
    int prefixLength = prefix.getPrefixLength();
    SubRange range;
    if (ctx.le == null && ctx.ge == null) {
      range = SubRange.singleton(prefixLength);
    } else {
      int low = ctx.ge != null ? Integer.parseInt(ctx.ge.getText()) : prefixLength;
      int high = ctx.le != null ? Integer.parseInt(ctx.le.getText()) : Prefix.MAX_PREFIX_LENGTH;
      range = new SubRange(low, high);
    }
    IpPrefixListLine pll = new IpPrefixListLine(action, num, prefix, range);
    _currentIpPrefixList.getLines().put(num, pll);
  }

  @Override
  public void exitPl6_line_action(Pl6_line_actionContext ctx) {
    long num;
    if (ctx.num != null) {
      num = toLong(ctx.num);
    } else {
      // Round up to the next multiple of 5
      // http://docs.frrouting.org/en/latest/filter.html#ip-prefix-list
      Long lastNum =
          _currentIpv6PrefixList.getLines().isEmpty()
              ? 0L
              : _currentIpv6PrefixList.getLines().lastKey();
      num = nextMultipleOfFive(lastNum);
    }
    LineAction action = ctx.action.permit != null ? LineAction.PERMIT : LineAction.DENY;

    if (ctx.ANY() != null) {
      _currentIpv6PrefixList.addLine(
          new Ipv6PrefixListLine(
              action, num, Prefix6.ZERO, new SubRange(0, Prefix6.MAX_PREFIX_LENGTH)));
      return;
    }

    Prefix6 prefix = Prefix6.parse(ctx.ip_prefix.getText());
    int prefixLength = prefix.getPrefixLength();
    SubRange range;
    if (ctx.le == null && ctx.ge == null) {
      range = SubRange.singleton(prefixLength);
    } else {
      int low = ctx.ge != null ? Integer.parseInt(ctx.ge.getText()) : prefixLength;
      int high = ctx.le != null ? Integer.parseInt(ctx.le.getText()) : Prefix6.MAX_PREFIX_LENGTH;
      range = new SubRange(low, high);
    }
    Ipv6PrefixListLine pll = new Ipv6PrefixListLine(action, num, prefix, range);
    _currentIpv6PrefixList.getLines().put(num, pll);
  }

  @VisibleForTesting
  static long nextMultipleOfFive(@Nullable Long lastNum) {
    return (long) (Math.ceil((Optional.ofNullable(lastNum).orElse(0L) + 1) * 1.0 / 5) * 5);
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }
}
