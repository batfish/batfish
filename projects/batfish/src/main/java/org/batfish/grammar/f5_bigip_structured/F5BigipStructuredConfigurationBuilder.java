package org.batfish.grammar.f5_bigip_structured;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.BGP_PROCESS;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.INTERFACE;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.MONITOR;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.MONITOR_HTTP;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.MONITOR_HTTPS;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.NODE;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PERSISTENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PERSISTENCE_SOURCE_ADDR;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PERSISTENCE_SSL;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.POOL;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PREFIX_LIST;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PROFILE;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PROFILE_CLIENT_SSL;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PROFILE_HTTP;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PROFILE_OCSP_STAPLING_PARAMS;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PROFILE_ONE_CONNECT;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PROFILE_SERVER_SSL;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PROFILE_TCP;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.ROUTE_MAP;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.RULE;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.SELF;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.SNAT;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.SNATPOOL;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.VIRTUAL;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.VIRTUAL_ADDRESS;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.VLAN;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.BGP_ADDRESS_FAMILY_REDISTRIBUTE_KERNEL_ROUTE_MAP;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.BGP_NEIGHBOR_IPV4_ROUTE_MAP_OUT;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.BGP_NEIGHBOR_IPV6_ROUTE_MAP_OUT;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.BGP_PROCESS_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.INTERFACE_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.MONITOR_HTTPS_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.MONITOR_HTTPS_SSL_PROFILE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.MONITOR_HTTP_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PERSISTENCE_SOURCE_ADDR_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PERSISTENCE_SSL_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.POOL_MEMBER;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.POOL_MONITOR;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PROFILE_CLIENT_SSL_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PROFILE_HTTP_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PROFILE_OCSP_STAPLING_PARAMS_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PROFILE_ONE_CONNECT_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PROFILE_SERVER_SSL_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.PROFILE_TCP_DEFAULTS_FROM;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.ROUTE_MAP_MATCH_IPV4_ADDRESS_PREFIX_LIST;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SELF_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SELF_VLAN;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SNAT_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SNAT_SNATPOOL;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VIRTUAL_DESTINATION;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VIRTUAL_PERSIST_PERSISTENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VIRTUAL_POOL;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VIRTUAL_PROFILE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VIRTUAL_RULES_RULE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VIRTUAL_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VIRTUAL_SOURCE_ADDRESS_TRANSLATION_POOL;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VLAN_INTERFACE;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.SubRange;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Bundle_speedContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.F5_bigip_structured_configurationContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.L_nodeContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.L_poolContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.L_ruleContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.L_snatContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.L_snatpoolContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.L_virtualContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.L_virtual_addressContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lm_httpContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lm_httpsContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lmh_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lmhs_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lmhs_ssl_profileContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lp_monitorContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lper_source_addrContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lper_sslContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lpersa_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lperss_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lpm_memberContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprof_client_sslContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprof_httpContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprof_ocsp_stapling_paramsContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprof_one_connectContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprof_server_sslContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprof_tcpContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprofcs_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprofh_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprofoc_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprofon_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lprofss_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lproft_defaults_fromContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ls_snatpoolContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lv_destinationContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lv_poolContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lv_profiles_profileContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lvp_persistenceContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lvr_ruleContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Lvsat_poolContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_interfaceContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_selfContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_vlanContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ni_bundleContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nr_bgpContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nr_prefix_listContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nr_route_mapContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrbaf_ipv4Context;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrbaf_ipv6Context;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrbafcrk_route_mapContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrbnnafcr_outContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nreem4a_prefix_listContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nreesc_valueContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrp_route_domainContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpe_entryContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpee_actionContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpee_prefixContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpee_prefix_len_rangeContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrr_route_domainContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrre_entryContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrree_actionContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ns_addressContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ns_allow_serviceContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ns_traffic_groupContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ns_vlanContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nv_tagContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nvi_interfaceContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Prefix_list_actionContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Route_map_actionContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Sgs_hostnameContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Standard_communityContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.UnrecognizedContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.WordContext;
import org.batfish.representation.f5_bigip.BgpAddressFamily;
import org.batfish.representation.f5_bigip.BgpIpv4AddressFamily;
import org.batfish.representation.f5_bigip.BgpProcess;
import org.batfish.representation.f5_bigip.BuiltinMonitor;
import org.batfish.representation.f5_bigip.BuiltinPersistence;
import org.batfish.representation.f5_bigip.BuiltinProfile;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
import org.batfish.representation.f5_bigip.F5BigipStructureUsage;
import org.batfish.representation.f5_bigip.Interface;
import org.batfish.representation.f5_bigip.PrefixList;
import org.batfish.representation.f5_bigip.PrefixListEntry;
import org.batfish.representation.f5_bigip.RouteMap;
import org.batfish.representation.f5_bigip.RouteMapEntry;
import org.batfish.representation.f5_bigip.RouteMapMatchPrefixList;
import org.batfish.representation.f5_bigip.RouteMapSetCommunity;
import org.batfish.representation.f5_bigip.Self;
import org.batfish.representation.f5_bigip.Vlan;
import org.batfish.representation.f5_bigip.VlanInterface;
import org.batfish.vendor.StructureType;

@ParametersAreNonnullByDefault
public class F5BigipStructuredConfigurationBuilder extends F5BigipStructuredParserBaseListener {

  static String unquote(String text) {
    if (text.length() == 0) {
      return text;
    }
    if (text.charAt(0) != '"') {
      return text;
    } else if (text.charAt(text.length() - 1) != '"') {
      throw new BatfishException("Improperly-quoted string");
    } else {
      return text.substring(1, text.length() - 1);
    }
  }

  private @Nullable F5BigipConfiguration _c;
  private @Nullable BgpAddressFamily _currentBgpAddressFamily;
  private @Nullable BgpProcess _currentBgpProcess;
  private @Nullable Interface _currentInterface;
  private @Nullable PrefixList _currentPrefixList;
  private @Nullable PrefixListEntry _currentPrefixListEntry;
  private @Nullable RouteMap _currentRouteMap;
  private @Nullable RouteMapEntry _currentRouteMapEntry;
  private @Nullable Self _currentSelf;
  private @Nullable UnrecognizedContext _currentUnrecognized;
  private @Nullable Vlan _currentVlan;
  private final @Nonnull F5BigipStructuredCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;

  public F5BigipStructuredConfigurationBuilder(
      F5BigipStructuredCombinedParser parser, String text, Warnings w) {
    _parser = parser;
    _text = text;
    _w = w;
  }

  private String convErrorMessage(Class<?> type, ParserRuleContext ctx) {
    return String.format("Could not convert to %s: %s", type.getSimpleName(), getFullText(ctx));
  }

  private @Nullable <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, @Nullable U defaultReturnValue) {
    _w.redFlag(convErrorMessage(returnType, ctx));
    return defaultReturnValue;
  }

  /** Mark the specified structure as defined on each line in the supplied context */
  private void defineStructure(StructureType type, String name, RuleContext ctx) {
    /* Recursively process children to find all relevant definition lines for the specified context */
    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree child = ctx.getChild(i);
      if (child instanceof TerminalNode) {
        _c.defineStructure(type, name, ((TerminalNode) child).getSymbol().getLine());
      } else if (child instanceof RuleContext) {
        defineStructure(type, name, (RuleContext) child);
      }
    }
  }

  @Override
  public void enterF5_bigip_structured_configuration(F5_bigip_structured_configurationContext ctx) {
    _c = new F5BigipConfiguration();
  }

  @Override
  public void enterL_node(L_nodeContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(NODE, name, ctx);
  }

  @Override
  public void enterL_pool(L_poolContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(POOL, name, ctx);
  }

  @Override
  public void enterL_rule(L_ruleContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(RULE, name, ctx);
  }

  @Override
  public void enterL_snat(L_snatContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(SNAT, name, ctx);
    _c.referenceStructure(SNAT, name, SNAT_SELF_REFERENCE, ctx.name.getStart().getLine());
  }

  @Override
  public void enterL_snatpool(L_snatpoolContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(SNATPOOL, name, ctx);
  }

  @Override
  public void enterL_virtual(L_virtualContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(VIRTUAL, name, ctx);
    _c.referenceStructure(VIRTUAL, name, VIRTUAL_SELF_REFERENCE, ctx.name.getStart().getLine());
  }

  @Override
  public void enterL_virtual_address(L_virtual_addressContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(VIRTUAL_ADDRESS, name, ctx);
  }

  @Override
  public void enterLm_http(Lm_httpContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(MONITOR_HTTP, name, ctx);
  }

  @Override
  public void enterLm_https(Lm_httpsContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(MONITOR_HTTPS, name, ctx);
  }

  @Override
  public void enterLper_source_addr(Lper_source_addrContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PERSISTENCE_SOURCE_ADDR, name, ctx);
  }

  @Override
  public void enterLper_ssl(Lper_sslContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PERSISTENCE_SSL, name, ctx);
  }

  @Override
  public void enterLpm_member(Lpm_memberContext ctx) {
    String name = toName(unquote(ctx.name.getText()), ctx);
    if (name != null) {
      _c.referenceStructure(NODE, name, POOL_MEMBER, ctx.name.getStart().getLine());
    }
  }

  @Override
  public void enterLprof_client_ssl(Lprof_client_sslContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PROFILE_CLIENT_SSL, name, ctx);
  }

  @Override
  public void enterLprof_http(Lprof_httpContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PROFILE_HTTP, name, ctx);
  }

  @Override
  public void enterLprof_ocsp_stapling_params(Lprof_ocsp_stapling_paramsContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PROFILE_OCSP_STAPLING_PARAMS, name, ctx);
  }

  @Override
  public void enterLprof_one_connect(Lprof_one_connectContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PROFILE_ONE_CONNECT, name, ctx);
  }

  @Override
  public void enterLprof_server_ssl(Lprof_server_sslContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PROFILE_SERVER_SSL, name, ctx);
  }

  @Override
  public void enterLprof_tcp(Lprof_tcpContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PROFILE_TCP, name, ctx);
  }

  @Override
  public void enterLv_profiles_profile(Lv_profiles_profileContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(PROFILE, name, VIRTUAL_PROFILE, ctx.name.getStart().getLine());
  }

  @Override
  public void enterNet_interface(Net_interfaceContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(INTERFACE, name, ctx);
    _c.referenceStructure(INTERFACE, name, INTERFACE_SELF_REFERENCE, ctx.name.getStart().getLine());
    _currentInterface = _c.getInterfaces().computeIfAbsent(name, Interface::new);
  }

  @Override
  public void enterNet_self(Net_selfContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(SELF, name, ctx);
    _c.referenceStructure(SELF, name, SELF_SELF_REFERENCE, ctx.name.getStart().getLine());
    _currentSelf = _c.getSelves().computeIfAbsent(name, Self::new);
  }

  @Override
  public void enterNet_vlan(Net_vlanContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(VLAN, name, ctx);
    _currentVlan = _c.getVlans().computeIfAbsent(name, Vlan::new);
  }

  @Override
  public void enterNr_bgp(Nr_bgpContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(BGP_PROCESS, name, ctx);
    _c.referenceStructure(
        BGP_PROCESS, name, BGP_PROCESS_SELF_REFERENCE, ctx.name.getStart().getLine());
    _currentBgpProcess = _c.getBgpProcesses().computeIfAbsent(name, BgpProcess::new);
  }

  @Override
  public void enterNr_prefix_list(Nr_prefix_listContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(PREFIX_LIST, name, ctx);
    _currentPrefixList = _c.getPrefixLists().computeIfAbsent(name, PrefixList::new);
  }

  @Override
  public void enterNr_route_map(Nr_route_mapContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(ROUTE_MAP, name, ctx);
    _currentRouteMap = _c.getRouteMaps().computeIfAbsent(name, RouteMap::new);
  }

  @Override
  public void enterNrbaf_ipv4(Nrbaf_ipv4Context ctx) {
    _currentBgpAddressFamily = _currentBgpProcess.getIpv4AddressFamily();
  }

  @Override
  public void enterNrbaf_ipv6(Nrbaf_ipv6Context ctx) {
    _currentBgpAddressFamily = _currentBgpProcess.getIpv6AddressFamily();
  }

  @Override
  public void enterNrpe_entry(Nrpe_entryContext ctx) {
    _currentPrefixListEntry =
        _currentPrefixList.getEntries().computeIfAbsent(toInteger(ctx.num), PrefixListEntry::new);
  }

  @Override
  public void enterNrre_entry(Nrre_entryContext ctx) {
    _currentRouteMapEntry =
        _currentRouteMap.getEntries().computeIfAbsent(toInteger(ctx.num), RouteMapEntry::new);
  }

  @Override
  public void enterUnrecognized(UnrecognizedContext ctx) {
    if (_currentUnrecognized == null) {
      _currentUnrecognized = ctx;
    }
  }

  @Override
  public void exitBundle_speed(Bundle_speedContext ctx) {
    Double speed = toSpeed(ctx);
    _currentInterface.setSpeed(speed);
  }

  @Override
  public void exitLmh_defaults_from(Lmh_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        MONITOR_HTTP, name, MONITOR_HTTP_DEFAULTS_FROM, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLmhs_defaults_from(Lmhs_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        MONITOR_HTTPS, name, MONITOR_HTTPS_DEFAULTS_FROM, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLmhs_ssl_profile(Lmhs_ssl_profileContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PROFILE_SERVER_SSL, name, MONITOR_HTTPS_SSL_PROFILE, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLp_monitor(Lp_monitorContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(MONITOR, name, POOL_MONITOR, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLpersa_defaults_from(Lpersa_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PERSISTENCE_SOURCE_ADDR,
        name,
        PERSISTENCE_SOURCE_ADDR_DEFAULTS_FROM,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitLperss_defaults_from(Lperss_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PERSISTENCE_SSL, name, PERSISTENCE_SSL_DEFAULTS_FROM, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLprofcs_defaults_from(Lprofcs_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PROFILE_CLIENT_SSL, name, PROFILE_CLIENT_SSL_DEFAULTS_FROM, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLprofh_defaults_from(Lprofh_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PROFILE_HTTP, name, PROFILE_HTTP_DEFAULTS_FROM, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLprofoc_defaults_from(Lprofoc_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PROFILE_OCSP_STAPLING_PARAMS,
        name,
        PROFILE_OCSP_STAPLING_PARAMS_DEFAULTS_FROM,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitLprofon_defaults_from(Lprofon_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PROFILE_ONE_CONNECT,
        name,
        PROFILE_ONE_CONNECT_DEFAULTS_FROM,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitLprofss_defaults_from(Lprofss_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PROFILE_SERVER_SSL, name, PROFILE_SERVER_SSL_DEFAULTS_FROM, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLproft_defaults_from(Lproft_defaults_fromContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PROFILE_TCP, name, PROFILE_TCP_DEFAULTS_FROM, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLs_snatpool(Ls_snatpoolContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(SNATPOOL, name, SNAT_SNATPOOL, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLv_destination(Lv_destinationContext ctx) {
    String name = toName(unquote(ctx.name.getText()), ctx);
    if (name != null) {
      _c.referenceStructure(
          VIRTUAL_ADDRESS, name, VIRTUAL_DESTINATION, ctx.name.getStart().getLine());
    }
  }

  @Override
  public void exitLv_pool(Lv_poolContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(POOL, name, VIRTUAL_POOL, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLvp_persistence(Lvp_persistenceContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PERSISTENCE, name, VIRTUAL_PERSIST_PERSISTENCE, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLvr_rule(Lvr_ruleContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(RULE, name, VIRTUAL_RULES_RULE, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLvsat_pool(Lvsat_poolContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        SNATPOOL, name, VIRTUAL_SOURCE_ADDRESS_TRANSLATION_POOL, ctx.name.getStart().getLine());
  }

  @Override
  public void exitNet_interface(Net_interfaceContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitNet_self(Net_selfContext ctx) {
    _currentSelf = null;
  }

  @Override
  public void exitNet_vlan(Net_vlanContext ctx) {
    _currentVlan = null;
  }

  @Override
  public void exitNi_bundle(Ni_bundleContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitNr_bgp(Nr_bgpContext ctx) {
    _currentBgpProcess = null;
  }

  @Override
  public void exitNr_prefix_list(Nr_prefix_listContext ctx) {
    _currentPrefixList = null;
  }

  @Override
  public void exitNr_route_map(Nr_route_mapContext ctx) {
    _currentRouteMap = null;
  }

  @Override
  public void exitNrbaf_ipv4(Nrbaf_ipv4Context ctx) {
    _currentBgpAddressFamily = null;
  }

  @Override
  public void exitNrbaf_ipv6(Nrbaf_ipv6Context ctx) {
    _currentBgpAddressFamily = null;
  }

  @Override
  public void exitNrbafcrk_route_map(Nrbafcrk_route_mapContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        ROUTE_MAP,
        name,
        BGP_ADDRESS_FAMILY_REDISTRIBUTE_KERNEL_ROUTE_MAP,
        ctx.name.getStart().getLine());
    todo(ctx);
  }

  @Override
  public void exitNrbnnafcr_out(Nrbnnafcr_outContext ctx) {
    String name = unquote(ctx.name.getText());
    F5BigipStructureUsage usage =
        _currentBgpAddressFamily instanceof BgpIpv4AddressFamily
            ? BGP_NEIGHBOR_IPV4_ROUTE_MAP_OUT
            : BGP_NEIGHBOR_IPV6_ROUTE_MAP_OUT;
    _c.referenceStructure(ROUTE_MAP, name, usage, ctx.name.getStart().getLine());
    todo(ctx);
  }

  @Override
  public void exitNreem4a_prefix_list(Nreem4a_prefix_listContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(
        PREFIX_LIST, name, ROUTE_MAP_MATCH_IPV4_ADDRESS_PREFIX_LIST, ctx.name.getStart().getLine());
    _currentRouteMapEntry.setMatchPrefixList(new RouteMapMatchPrefixList(name));
  }

  @Override
  public void exitNreesc_value(Nreesc_valueContext ctx) {
    _currentRouteMapEntry.setSetCommunity(
        new RouteMapSetCommunity(
            ctx.communities.stream()
                .map(this::toCommunity)
                .filter(Objects::nonNull)
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Override
  public void exitNrp_route_domain(Nrp_route_domainContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitNrpe_entry(Nrpe_entryContext ctx) {
    _currentPrefixListEntry = null;
  }

  @Override
  public void exitNrpee_action(Nrpee_actionContext ctx) {
    _currentPrefixListEntry.setAction(toLineAction(ctx.action));
  }

  @Override
  public void exitNrpee_prefix(Nrpee_prefixContext ctx) {
    String text = ctx.prefix.getText();
    Optional<Prefix> prefix = Prefix.tryParse(text);
    if (prefix.isPresent()) {
      _currentPrefixListEntry.setPrefix(prefix.get());
      return;
    }
    Optional<Prefix6> prefix6 = Prefix6.tryParse(text);
    if (prefix6.isPresent()) {
      _currentPrefixListEntry.setPrefix6(prefix6.get());
      return;
    }
    _w.redFlag(
        String.format("'%s' is neither IPv4 nor IPv6 prefix in: %s", text, getFullText(ctx)));
  }

  @Override
  public void exitNrpee_prefix_len_range(Nrpee_prefix_len_rangeContext ctx) {
    _currentPrefixListEntry.setLengthRange(toSubRange(ctx.range));
  }

  @Override
  public void exitNrr_route_domain(Nrr_route_domainContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitNrre_entry(Nrre_entryContext ctx) {
    _currentRouteMapEntry = null;
  }

  @Override
  public void exitNrree_action(Nrree_actionContext ctx) {
    _currentRouteMapEntry.setAction(toLineAction(ctx.action));
  }

  @Override
  public void exitNs_address(Ns_addressContext ctx) {
    _currentSelf.setAddress(new InterfaceAddress(ctx.interface_address.getText()));
  }

  @Override
  public void exitNs_allow_service(Ns_allow_serviceContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitNs_traffic_group(Ns_traffic_groupContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitNs_vlan(Ns_vlanContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(VLAN, name, SELF_VLAN, ctx.name.getStart().getLine());
    _currentSelf.setVlan(name);
  }

  @Override
  public void exitNv_tag(Nv_tagContext ctx) {
    _currentVlan.setTag(toInteger(ctx.tag));
  }

  @Override
  public void exitNvi_interface(Nvi_interfaceContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(INTERFACE, name, VLAN_INTERFACE, ctx.name.getStart().getLine());
    _currentVlan.getInterfaces().computeIfAbsent(name, VlanInterface::new);
  }

  @Override
  public void exitSgs_hostname(Sgs_hostnameContext ctx) {
    String hostname = unquote(ctx.hostname.getText());
    _c.setHostname(hostname);
  }

  @Override
  public void exitUnrecognized(UnrecognizedContext ctx) {
    if (_currentUnrecognized != ctx) {
      return;
    }
    unrecognized(ctx);
    _currentUnrecognized = null;
  }

  public F5BigipConfiguration getConfiguration() {
    return _c;
  }

  private @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  private @Nullable Long toCommunity(Standard_communityContext ctx) {
    if (ctx.word() != null) {
      return CommonUtil.communityStringToLong(ctx.getText());
    } else {
      return convProblem(Long.class, ctx, null);
    }
  }

  private void todo(ParserRuleContext ctx) {
    _w.todo(ctx, getFullText(ctx), _parser);
  }

  private int toInteger(ParserRuleContext ctx) {
    return Integer.parseUnsignedInt(ctx.getText(), 10);
  }

  private @Nullable LineAction toLineAction(Prefix_list_actionContext ctx) {
    if (ctx.PERMIT() != null) {
      return LineAction.PERMIT;
    } else if (ctx.DENY() != null) {
      return LineAction.DENY;
    } else {
      return convProblem(LineAction.class, ctx, null);
    }
  }

  private @Nullable LineAction toLineAction(Route_map_actionContext ctx) {
    if (ctx.PERMIT() != null) {
      return LineAction.PERMIT;
    } else if (ctx.DENY() != null) {
      return LineAction.DENY;
    } else {
      return convProblem(LineAction.class, ctx, null);
    }
  }

  private @Nullable String toName(String nameWithPort, ParserRuleContext ctx) {
    String[] parts = nameWithPort.split(":", -1);
    if (parts.length != 2) {
      _w.redFlag(
          String.format(
              "Expected node name with :port suffix but got '%s' in: %s",
              nameWithPort, getFullText(ctx)));
      return null;
    }
    return parts[0];
  }

  private @Nullable Double toSpeed(Bundle_speedContext ctx) {
    if (ctx.FORTY_G() != null) {
      return 40E9D;
    } else if (ctx.ONE_HUNDRED_G() != null) {
      return 100E9D;
    } else {
      return convProblem(Double.class, ctx, null);
    }
  }

  private @Nullable SubRange toSubRange(WordContext ctx) {
    String[] parts = ctx.getText().split(":", -1);
    try {
      checkArgument(parts.length == 2);
      int low = Integer.parseInt(parts[0], 10);
      int high = Integer.parseInt(parts[1], 10);
      return new SubRange(low, high);
    } catch (IllegalArgumentException e) {
      return convProblem(SubRange.class, ctx, null);
    }
  }

  private void unrecognized(UnrecognizedContext ctx) {
    Token start = ctx.getStart();
    int line = start.getLine();
    _w.getParseWarnings()
        .add(
            new ParseWarning(
                line,
                start.getText(),
                ctx.toString(Arrays.asList(_parser.getParser().getRuleNames())),
                "This syntax is unrecognized"));
  }
}
