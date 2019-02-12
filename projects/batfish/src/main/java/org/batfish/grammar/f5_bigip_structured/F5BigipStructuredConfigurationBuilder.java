package org.batfish.grammar.f5_bigip_structured;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.BGP_PROCESS;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.INTERFACE;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.PREFIX_LIST;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.ROUTE_MAP;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.SELF;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.VLAN;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.BGP_PROCESS_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.INTERFACE_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.ROUTE_MAP_MATCH_IPV4_ADDRESS_PREFIX_LIST;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SELF_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SELF_VLAN;
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
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_interfaceContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_selfContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_vlanContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nr_bgpContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nr_prefix_listContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nr_route_mapContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrbaf_ipv4Context;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrbaf_ipv6Context;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nreem4a_prefix_listContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nreesc_valueContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpe_entryContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpee_actionContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpee_prefixContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrpee_prefix_len_rangeContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrre_entryContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nrree_actionContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ns_addressContext;
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
import org.batfish.representation.f5_bigip.BgpProcess;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
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

  @SuppressWarnings("unused") // temporary
  private BgpAddressFamily _currentBgpAddressFamily;

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
