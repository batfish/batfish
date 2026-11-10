package org.batfish.vendor.nec_ix.grammar;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Acl_addressContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Acl_dstContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Acl_optionContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Acl_srcContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.CommunityContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Dev_shutdownContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.If_bridge_groupContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.If_descriptionContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.If_shutdownContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ife_dot1qContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifip_addressContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifip_filterContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifip_mtuContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifip_vrfContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipa_dynamicContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnapt_addressContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnapt_enableContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnapt_insideContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnapt_port_rangeContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnapt_unimplementedContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnat_enableContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnat_nullContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifipnat_staticContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifno_ip_addressContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ifno_shutdownContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ip_access_listContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ip_prefix_listContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ip_routeContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ipal_entryContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ipale_icmpContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ipale_otherContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ipale_tcpContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ipale_udpContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Ipr_optionContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Port_specContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_address_familyContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_default_local_preferenceContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_neighborContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_networkContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_originate_defaultContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_redistributeContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_router_idContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rb_vrfContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbn_ebgp_multihopContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbn_next_hop_selfContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbn_remote_asContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbn_route_mapContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbn_send_defaultContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbn_shutdownContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbn_update_sourceContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rbno_send_defaultContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rmm_prefix_listContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rmm_unimplementedContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rms_as_path_prependContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rms_communityContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rms_local_preferenceContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rms_metricContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Rms_unimplementedContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.S_deviceContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.S_hostnameContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.S_interfaceContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.S_route_mapContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.S_router_bgpContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.Tcp_flagContext;
import org.batfish.vendor.nec_ix.grammar.NecIxParser.UintContext;
import org.batfish.vendor.nec_ix.representation.NecIxAccessList;
import org.batfish.vendor.nec_ix.representation.NecIxAccessListLine;
import org.batfish.vendor.nec_ix.representation.NecIxBgpNeighbor;
import org.batfish.vendor.nec_ix.representation.NecIxBgpProcess;
import org.batfish.vendor.nec_ix.representation.NecIxBgpVrf;
import org.batfish.vendor.nec_ix.representation.NecIxConfiguration;
import org.batfish.vendor.nec_ix.representation.NecIxInterface;
import org.batfish.vendor.nec_ix.representation.NecIxPrefixList;
import org.batfish.vendor.nec_ix.representation.NecIxRouteMap;
import org.batfish.vendor.nec_ix.representation.NecIxStaticRoute;
import org.batfish.vendor.nec_ix.representation.NecIxStructureType;
import org.batfish.vendor.nec_ix.representation.NecIxStructureUsage;

/** Builds an {@link NecIxConfiguration} from an NEC IX parse tree. */
@ParametersAreNonnullByDefault
public final class NecIxConfigurationBuilder extends NecIxParserBaseListener
    implements SilentSyntaxListener {

  private static final IntegerSpace ACCESS_LIST_SEQUENCES =
      IntegerSpace.of(Range.closed(1, Integer.MAX_VALUE));
  private static final IntegerSpace BGP_AS_NUMBERS =
      IntegerSpace.of(Range.closed(1, Integer.MAX_VALUE));
  private static final IntegerSpace BRIDGE_GROUPS = IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace DSCPS = IntegerSpace.of(Range.closed(0, 63));
  private static final IntegerSpace FILTER_SEQUENCES = IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace ICMP_VALUES = IntegerSpace.of(Range.closed(0, 255));
  private static final IntegerSpace IP_MTUS = IntegerSpace.of(Range.closed(576, 65535));
  private static final IntegerSpace IP_PROTOCOLS = IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace NAPT_PORTS = IntegerSpace.of(Range.closed(1024, 65535));
  private static final IntegerSpace PORTS = IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace PRECEDENCES = IntegerSpace.of(Range.closed(0, 7));
  private static final IntegerSpace PREFIX_LENGTHS = IntegerSpace.of(Range.closed(0, 32));
  private static final IntegerSpace PREFIX_LIST_SEQUENCES = IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace ROUTE_DISTANCES = IntegerSpace.of(Range.closed(0, 255));
  private static final IntegerSpace ROUTE_MAP_SEQUENCES = IntegerSpace.of(Range.closed(0, 65535));
  private static final IntegerSpace ROUTE_METRICS = IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace VLAN_IDS = IntegerSpace.of(Range.closed(1, 4094));

  /** Sequence number of a {@code route-map} entry whose sequence number is omitted. */
  private static final int DEFAULT_ROUTE_MAP_SEQUENCE = 10;

  /**
   * ICMP message names accepted in place of {@code type <t> [code <c>]}: type, then code or {@code
   * -1} for any code.
   */
  private static final Map<String, int[]> ICMP_MESSAGES =
      ImmutableMap.<String, int[]>builder()
          .put("administratively-prohibited", new int[] {3, 13})
          .put("dod-host-prohibited", new int[] {3, 10})
          .put("dod-net-prohibited", new int[] {3, 9})
          .put("echo", new int[] {8, -1})
          .put("echo-reply", new int[] {0, -1})
          .put("general-parameter-problem", new int[] {12, 0})
          .put("host-isolated", new int[] {3, 8})
          .put("host-precedence-unreachable", new int[] {3, 14})
          .put("host-redirect", new int[] {5, 1})
          .put("host-tos-redirect", new int[] {5, 3})
          .put("host-tos-unreachable", new int[] {3, 12})
          .put("host-unknown", new int[] {3, 7})
          .put("host-unreachable", new int[] {3, 1})
          .put("information-reply", new int[] {16, -1})
          .put("information-request", new int[] {15, -1})
          .put("mask-reply", new int[] {18, -1})
          .put("mask-request", new int[] {17, -1})
          .put("net-redirect", new int[] {5, 0})
          .put("net-tos-redirect", new int[] {5, 2})
          .put("net-tos-unreachable", new int[] {3, 11})
          .put("net-unreachable", new int[] {3, 0})
          .put("network-unknown", new int[] {3, 6})
          .put("option-missing", new int[] {12, 1})
          .put("packet-too-big", new int[] {3, 4})
          .put("parameter-problem", new int[] {12, -1})
          .put("port-unreachable", new int[] {3, 3})
          .put("precedence-unreachable", new int[] {3, 15})
          .put("protocol-unreachable", new int[] {3, 2})
          .put("reassembly-timeout", new int[] {11, 1})
          .put("redirect", new int[] {5, -1})
          .put("router-advertisement", new int[] {9, -1})
          .put("router-solicitation", new int[] {10, -1})
          .put("source-quench", new int[] {4, -1})
          .put("source-route-failed", new int[] {3, 5})
          .put("time-exceeded", new int[] {11, -1})
          .put("timestamp-reply", new int[] {14, -1})
          .put("timestamp-request", new int[] {13, -1})
          .put("traceroute", new int[] {30, -1})
          .put("ttl-exceeded", new int[] {11, 0})
          .put("unreachable", new int[] {3, -1})
          .build();

  private final @Nonnull NecIxCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull NecIxConfiguration _c;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  private @Nullable String _currentDevice;
  private @Nullable NecIxInterface _currentInterface;
  private @Nullable String _currentAccessList;
  private @Nullable NecIxRouteMap.Entry _currentRouteMapEntry;
  private @Nullable NecIxBgpProcess _currentBgpProcess;
  private @Nullable NecIxBgpVrf _currentBgpVrf;
  private @Nullable NecIxBgpNeighbor _currentBgpNeighbor;

  /** Whether the current BGP address-family is IPv6, whose configuration is not modeled. */
  private boolean _inIpv6AddressFamily;

  public NecIxConfigurationBuilder(
      NecIxCombinedParser parser,
      String text,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _c = new NecIxConfiguration();
    _c.setExtraLines(_parser.getExtraLines());
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  public @Nonnull NecIxConfiguration getConfiguration() {
    return _c;
  }

  // ----------------------------------------------------------------------------- top level

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _c.setHostname(ctx.name.getText());
  }

  @Override
  public void enterS_device(S_deviceContext ctx) {
    _currentDevice = ctx.name.getText();
  }

  @Override
  public void exitS_device(S_deviceContext ctx) {
    _currentDevice = null;
  }

  @Override
  public void exitDev_shutdown(Dev_shutdownContext ctx) {
    assert _currentDevice != null;
    _c.getShutdownDevices().add(_currentDevice);
  }

  // ----------------------------------------------------------------------------- interfaces

  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    String name = ctx.name.getText();
    _currentInterface = _c.getInterfaces().computeIfAbsent(name, NecIxInterface::new);
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    _currentInterface = null;
  }

  private @Nonnull NecIxInterface currentInterface() {
    assert _currentInterface != null;
    return _currentInterface;
  }

  @Override
  public void exitIf_description(If_descriptionContext ctx) {
    currentInterface()
        .setDescription(ctx.description == null ? null : getFullText(ctx.description));
  }

  @Override
  public void exitIf_shutdown(If_shutdownContext ctx) {
    currentInterface().setShutdown(true);
  }

  @Override
  public void exitIfno_shutdown(Ifno_shutdownContext ctx) {
    currentInterface().setShutdown(false);
  }

  @Override
  public void exitIfip_address(Ifip_addressContext ctx) {
    if (ctx.address == null) {
      return;
    }
    ConcreteInterfaceAddress address = ConcreteInterfaceAddress.parse(ctx.address.getText());
    if (ctx.SECONDARY() != null) {
      currentInterface().getSecondaryAddresses().add(address);
    } else {
      currentInterface().setAddress(address);
    }
  }

  @Override
  public void exitIfipa_dynamic(Ifipa_dynamicContext ctx) {
    // dhcp, ipcp, map-e, v6pv: the address is only known at run time.
    todo(ctx);
  }

  @Override
  public void exitIfno_ip_address(Ifno_ip_addressContext ctx) {
    currentInterface().setAddress(null);
    currentInterface().getSecondaryAddresses().clear();
  }

  @Override
  public void exitIfip_mtu(Ifip_mtuContext ctx) {
    toInteger(ctx, ctx.mtu, IP_MTUS, "IP MTU").ifPresent(currentInterface()::setMtu);
  }

  @Override
  public void exitIfip_vrf(Ifip_vrfContext ctx) {
    currentInterface().setVrf(ctx.name.getText());
  }

  @Override
  public void exitIfe_dot1q(Ife_dot1qContext ctx) {
    toInteger(ctx, ctx.vlan, VLAN_IDS, "VLAN ID").ifPresent(currentInterface()::setVlan);
  }

  @Override
  public void exitIf_bridge_group(If_bridge_groupContext ctx) {
    if (ctx.null_rest_of_line() != null) {
      return;
    }
    toInteger(ctx, ctx.group, BRIDGE_GROUPS, "bridge group")
        .ifPresent(currentInterface()::setBridgeGroup);
  }

  @Override
  public void exitIfip_filter(Ifip_filterContext ctx) {
    String name = ctx.name.getText();
    Optional<Integer> seq = toInteger(ctx, ctx.seq, FILTER_SEQUENCES, "filter sequence number");
    if (!seq.isPresent()) {
      return;
    }
    boolean in = ctx.direction.getType() == NecIxLexer.IN;
    (in ? currentInterface().getInboundFilters() : currentInterface().getOutboundFilters())
        .put(seq.get(), name);
    _c.referenceStructure(
        NecIxStructureType.ACCESS_LIST,
        name,
        in
            ? NecIxStructureUsage.INTERFACE_IP_FILTER_IN
            : NecIxStructureUsage.INTERFACE_IP_FILTER_OUT,
        ctx.getStart().getLine());
  }

  @Override
  public void exitIfipnapt_enable(Ifipnapt_enableContext ctx) {
    currentInterface().setNaptEnabled(true);
  }

  @Override
  public void exitIfipnapt_address(Ifipnapt_addressContext ctx) {
    if (ctx.ip != null) {
      currentInterface().setNaptAddress(Ip.parse(ctx.ip.getText()));
    } else {
      currentInterface().setNaptAddressInterface(ctx.iface.getText());
    }
  }

  @Override
  public void exitIfipnapt_inside(Ifipnapt_insideContext ctx) {
    String acl = ctx.acl.getText();
    currentInterface()
        .getNaptInsides()
        .add(
            new NecIxInterface.NaptInside(
                acl, ctx.outside == null ? null : Ip.parse(ctx.outside.getText())));
    _c.referenceStructure(
        NecIxStructureType.ACCESS_LIST,
        acl,
        NecIxStructureUsage.INTERFACE_IP_NAPT_INSIDE_LIST,
        ctx.getStart().getLine());
  }

  @Override
  public void exitIfipnapt_port_range(Ifipnapt_port_rangeContext ctx) {
    ImmutableList.Builder<SubRange> ranges = ImmutableList.builder();
    for (Token token : ctx.ranges) {
      Optional<SubRange> range = toNaptPortRange(ctx, token.getText());
      if (!range.isPresent()) {
        return;
      }
      ranges.add(range.get());
    }
    currentInterface().setNaptPortRanges(ranges.build());
  }

  /** {@code <port>} or {@code <low>-<high>}, each in 1024-65535. */
  private @Nonnull Optional<SubRange> toNaptPortRange(ParserRuleContext ctx, String text) {
    String[] parts = text.split("-", -1);
    if (parts.length > 2) {
      warn(ctx, String.format("Cannot interpret '%s' as a port range", text));
      return Optional.empty();
    }
    Optional<Integer> low = toInteger(ctx, parts[0], NAPT_PORTS, "NAPT port");
    Optional<Integer> high =
        parts.length == 1 ? low : toInteger(ctx, parts[1], NAPT_PORTS, "NAPT port");
    if (!low.isPresent() || !high.isPresent()) {
      return Optional.empty();
    }
    if (low.get() > high.get()) {
      warn(ctx, String.format("Cannot interpret '%s' as a port range", text));
      return Optional.empty();
    }
    return Optional.of(new SubRange(low.get(), high.get()));
  }

  @Override
  public void exitIfipnapt_unimplemented(Ifipnapt_unimplementedContext ctx) {
    // Port forwarding (`service`, `static`), hairpinning, ...
    todo(ctx);
  }

  @Override
  public void exitIfipnat_enable(Ifipnat_enableContext ctx) {
    currentInterface().setNatEnabled(true);
  }

  @Override
  public void exitIfipnat_static(Ifipnat_staticContext ctx) {
    Prefix inside;
    Prefix outside;
    if (ctx.inside != null) {
      inside = Prefix.create(Ip.parse(ctx.inside.getText()), Prefix.MAX_PREFIX_LENGTH);
      outside = Prefix.create(Ip.parse(ctx.outside.getText()), Prefix.MAX_PREFIX_LENGTH);
    } else {
      inside = Prefix.parse(ctx.inside_prefix.getText());
      outside = Prefix.parse(ctx.outside_prefix.getText());
      if (inside.getPrefixLength() != outside.getPrefixLength()) {
        warn(ctx, "Inside and outside networks must have the same length; ignoring this mapping");
        return;
      }
    }
    currentInterface().getStaticNats().add(new NecIxInterface.StaticNat(inside, outside));
  }

  @Override
  public void exitIfipnat_null(Ifipnat_nullContext ctx) {
    // Dynamic NAT pools, cache sizes and timeouts.
    todo(ctx);
  }

  // ----------------------------------------------------------------------------- access-lists

  @Override
  public void enterIp_access_list(Ip_access_listContext ctx) {
    _currentAccessList = ctx.name.getText();
  }

  @Override
  public void exitIp_access_list(Ip_access_listContext ctx) {
    _currentAccessList = null;
  }

  @Override
  public void exitIpal_entry(Ipal_entryContext ctx) {
    assert _currentAccessList != null;
    String name = _currentAccessList;
    NecIxAccessList acl = _c.getAccessLists().computeIfAbsent(name, NecIxAccessList::new);
    _c.defineStructure(NecIxStructureType.ACCESS_LIST, name, (ParserRuleContext) ctx.getParent());
    Long seq = null;
    if (ctx.seq != null) {
      Optional<Integer> parsed =
          toInteger(ctx, ctx.seq, ACCESS_LIST_SEQUENCES, "access-list sequence number");
      if (!parsed.isPresent()) {
        return;
      }
      seq = (long) parsed.get();
    }
    HeaderSpace.Builder headerSpace = HeaderSpace.builder();
    if (!(toProtocolMatch(ctx, headerSpace) && toOptionMatch(ctx, headerSpace))) {
      return;
    }
    acl.getLines()
        .add(
            new NecIxAccessListLine(
                seq,
                ctx.action.getType() == NecIxLexer.PERMIT ? LineAction.PERMIT : LineAction.DENY,
                headerSpace.build(),
                _text
                    .substring(
                        ctx.getStart().getStartIndex(), ctx.NEWLINE().getSymbol().getStartIndex())
                    .trim()));
  }

  /**
   * Fills in {@code headerSpace} from the protocol-specific part of an access-list entry. Returns
   * {@code false} (having warned) if the entry cannot be represented.
   */
  private boolean toProtocolMatch(Ipal_entryContext ctx, HeaderSpace.Builder headerSpace) {
    Acl_srcContext src;
    Acl_dstContext dst;
    if (ctx.ipale_tcp() != null) {
      Ipale_tcpContext tcp = ctx.ipale_tcp();
      headerSpace.setIpProtocols(IpProtocol.TCP);
      src = tcp.src;
      dst = tcp.dst;
      if (!(toPorts(ctx, tcp.sport, headerSpace::setSrcPorts)
          && toPorts(ctx, tcp.dport, headerSpace::setDstPorts))) {
        return false;
      }
      toTcpFlags(tcp.flags).ifPresent(headerSpace::setTcpFlags);
    } else if (ctx.ipale_udp() != null) {
      Ipale_udpContext udp = ctx.ipale_udp();
      headerSpace.setIpProtocols(IpProtocol.UDP);
      src = udp.src;
      dst = udp.dst;
      if (!(toPorts(ctx, udp.sport, headerSpace::setSrcPorts)
          && toPorts(ctx, udp.dport, headerSpace::setDstPorts))) {
        return false;
      }
    } else if (ctx.ipale_icmp() != null) {
      Ipale_icmpContext icmp = ctx.ipale_icmp();
      headerSpace.setIpProtocols(IpProtocol.ICMP);
      src = icmp.src;
      dst = icmp.dst;
      if (!toIcmpMatch(ctx, icmp, headerSpace)) {
        return false;
      }
    } else {
      Ipale_otherContext other = ctx.ipale_other();
      src = other.src;
      dst = other.dst;
      if (other.protocol != null) {
        Optional<Integer> number = toInteger(ctx, other.protocol, IP_PROTOCOLS, "IP protocol");
        if (!number.isPresent()) {
          return false;
        }
        headerSpace.setIpProtocols(IpProtocol.fromNumber(number.get()));
      }
    }
    Optional<IpWildcard> srcIps = toIpWildcard(ctx, src.address, src.domain);
    Optional<IpWildcard> dstIps = toIpWildcard(ctx, dst.address, dst.domain);
    if (!srcIps.isPresent() || !dstIps.isPresent()) {
      return false;
    }
    headerSpace.setSrcIps(srcIps.get().toIpSpace());
    headerSpace.setDstIps(dstIps.get().toIpSpace());
    return true;
  }

  private @Nonnull Optional<IpWildcard> toIpWildcard(
      ParserRuleContext ctx, @Nullable Acl_addressContext address, @Nullable ParserRuleContext domain) {
    if (address == null) {
      assert domain != null;
      warn(ctx, "Batfish cannot match on domain names; ignoring this access-list entry");
      return Optional.empty();
    }
    if (address.ANY() != null) {
      return Optional.of(IpWildcard.ANY);
    } else if (address.prefix != null) {
      return Optional.of(IpWildcard.create(Prefix.parse(address.prefix.getText())));
    }
    return Optional.of(
        IpWildcard.ipWithWildcardMask(
            Ip.parse(address.ip.getText()), Ip.parse(address.wildcard.getText())));
  }

  private boolean toPorts(
      ParserRuleContext ctx,
      @Nullable Port_specContext spec,
      java.util.function.Consumer<Iterable<SubRange>> setter) {
    if (spec == null || spec.ANY() != null) {
      return true;
    }
    IntegerSpace ports;
    if (spec.RANGE() != null) {
      Optional<Integer> low = toInteger(ctx, spec.low, PORTS, "port");
      Optional<Integer> high = toInteger(ctx, spec.high, PORTS, "port");
      if (!low.isPresent() || !high.isPresent()) {
        return false;
      }
      ports = IntegerSpace.of(new SubRange(low.get(), high.get()));
    } else {
      Optional<Integer> port = toInteger(ctx, spec.port, PORTS, "port");
      if (!port.isPresent()) {
        return false;
      }
      int p = port.get();
      ports =
          switch (spec.op.getType()) {
            case NecIxLexer.EQ -> IntegerSpace.of(p);
            case NecIxLexer.NEQ ->
                IntegerSpace.builder()
                    .including(new SubRange(0, 65535))
                    .excluding(new SubRange(p, p))
                    .build();
            case NecIxLexer.LT -> IntegerSpace.of(new SubRange(0, p - 1));
            case NecIxLexer.GT -> IntegerSpace.of(new SubRange(p + 1, 65535));
            default -> throw new IllegalStateException("Unexpected port operator " + spec.op);
          };
    }
    setter.accept(ports.getSubRanges());
    return true;
  }

  /**
   * {@code ack|fin|psh|rst|syn|urg}: the flag is set. {@code established}: ACK or RST is set.
   * Several flags must all be set.
   */
  private static @Nonnull Optional<List<TcpFlagsMatchConditions>> toTcpFlags(
      List<Tcp_flagContext> flags) {
    if (flags.isEmpty()) {
      return Optional.empty();
    }
    if (flags.size() == 1 && flags.get(0).ESTABLISHED() != null) {
      return Optional.of(
          ImmutableList.of(TcpFlagsMatchConditions.ACK_TCP_FLAG, TcpFlagsMatchConditions.RST_TCP_FLAG));
    }
    TcpFlags.Builder value = TcpFlags.builder();
    TcpFlagsMatchConditions.Builder use = TcpFlagsMatchConditions.builder();
    for (Tcp_flagContext flag : flags) {
      if (flag.ACK() != null) {
        value.setAck(true);
        use.setUseAck(true);
      } else if (flag.FIN() != null) {
        value.setFin(true);
        use.setUseFin(true);
      } else if (flag.PSH() != null) {
        value.setPsh(true);
        use.setUsePsh(true);
      } else if (flag.RST() != null) {
        value.setRst(true);
        use.setUseRst(true);
      } else if (flag.SYN() != null) {
        value.setSyn(true);
        use.setUseSyn(true);
      } else if (flag.URG() != null) {
        value.setUrg(true);
        use.setUseUrg(true);
      } else {
        // `established` combined with other flags: ACK or RST, plus the others. Approximate by
        // requiring ACK.
        value.setAck(true);
        use.setUseAck(true);
      }
    }
    return Optional.of(ImmutableList.of(use.setTcpFlags(value.build()).build()));
  }

  private boolean toIcmpMatch(
      Ipal_entryContext ctx, Ipale_icmpContext icmp, HeaderSpace.Builder headerSpace) {
    if (icmp.icmp_type != null) {
      Optional<Integer> type = toInteger(ctx, icmp.icmp_type, ICMP_VALUES, "ICMP type");
      if (!type.isPresent()) {
        return false;
      }
      headerSpace.setIcmpTypes(type.get());
      if (icmp.icmp_code != null) {
        Optional<Integer> code = toInteger(ctx, icmp.icmp_code, ICMP_VALUES, "ICMP code");
        if (!code.isPresent()) {
          return false;
        }
        headerSpace.setIcmpCodes(code.get());
      }
    } else if (icmp.icmp_message != null) {
      int[] typeAndCode = ICMP_MESSAGES.get(icmp.icmp_message.getText());
      if (typeAndCode == null) {
        warn(
            ctx,
            String.format(
                "Unknown ICMP message '%s'; ignoring this access-list entry",
                icmp.icmp_message.getText()));
        return false;
      }
      headerSpace.setIcmpTypes(typeAndCode[0]);
      if (typeAndCode[1] >= 0) {
        headerSpace.setIcmpCodes(typeAndCode[1]);
      }
    }
    return true;
  }

  private boolean toOptionMatch(Ipal_entryContext ctx, HeaderSpace.Builder headerSpace) {
    for (Acl_optionContext option : ctx.acl_option()) {
      if (option.dscp != null) {
        Optional<Integer> dscp = toInteger(ctx, option.dscp, DSCPS, "DSCP");
        if (!dscp.isPresent()) {
          return false;
        }
        headerSpace.setDscps(ImmutableList.of(dscp.get()));
      } else if (option.precedence != null) {
        Optional<Integer> precedence = toInteger(ctx, option.precedence, PRECEDENCES, "precedence");
        if (!precedence.isPresent()) {
          return false;
        }
        // IP precedence is the top 3 bits of the DSCP field.
        int low = precedence.get() << 3;
        headerSpace.setDscps(
            IntegerSpace.of(new SubRange(low, low + 7)).enumerate().stream()
                .collect(ImmutableList.toImmutableList()));
      } else if (option.FRAGMENTS() != null) {
        headerSpace.setFragmentOffsets(ImmutableList.of(new SubRange(1, 8191)));
      } else if (option.TOS() != null) {
        warn(ctx, "Batfish cannot match on TOS; ignoring this access-list entry");
        return false;
      }
      // `linkmgr` conditions on link-manager groups, which do not exist in the static model.
    }
    return true;
  }

  // ----------------------------------------------------------------------------- prefix-lists

  @Override
  public void exitIp_prefix_list(Ip_prefix_listContext ctx) {
    // "Names are not case-sensitive and are all registered in lower case."
    String name = ctx.name.getText().toLowerCase();
    Optional<Integer> seq =
        toInteger(ctx, ctx.seq, PREFIX_LIST_SEQUENCES, "prefix-list sequence number");
    if (!seq.isPresent()) {
      return;
    }
    Prefix prefix;
    SubRange lengths;
    if (ctx.ANY() != null) {
      // "any: same as 0.0.0.0/0 min 0 max 32"
      prefix = Prefix.ZERO;
      lengths = new SubRange(0, Prefix.MAX_PREFIX_LENGTH);
    } else {
      prefix = Prefix.parse(ctx.prefix.getText());
      int length = prefix.getPrefixLength();
      Optional<Integer> min =
          ctx.min == null
              ? Optional.of(length)
              : toInteger(ctx, ctx.min, PREFIX_LENGTHS, "minimum prefix length");
      // Without `max`, a `min` opens the range up to /32 and otherwise only the prefix itself
      // matches.
      Optional<Integer> max =
          ctx.max != null
              ? toInteger(ctx, ctx.max, PREFIX_LENGTHS, "maximum prefix length")
              : Optional.of(ctx.min != null ? Prefix.MAX_PREFIX_LENGTH : length);
      if (!min.isPresent() || !max.isPresent()) {
        return;
      }
      if (min.get() > max.get()) {
        warn(ctx, "min is larger than max; ignoring this prefix-list entry");
        return;
      }
      lengths = new SubRange(min.get(), max.get());
    }
    _c.getPrefixLists()
        .computeIfAbsent(name, NecIxPrefixList::new)
        .getLines()
        .put(
            seq.get(),
            new NecIxPrefixList.Line(
                ctx.action.getType() == NecIxLexer.PERMIT ? LineAction.PERMIT : LineAction.DENY,
                prefix,
                lengths));
    _c.defineStructure(NecIxStructureType.PREFIX_LIST, name, ctx);
  }

  // ----------------------------------------------------------------------------- static routes

  @Override
  public void exitIp_route(Ip_routeContext ctx) {
    if (ctx.ipr_next_hop().DHCP() != null) {
      // The gateway is learned from DHCP at run time.
      todo(ctx);
      return;
    }
    Prefix network = ctx.prefix != null ? Prefix.parse(ctx.prefix.getText()) : Prefix.ZERO;
    Ip nextHopIp =
        ctx.ipr_next_hop().next_hop_ip == null
            ? null
            : Ip.parse(ctx.ipr_next_hop().next_hop_ip.getText());
    String nextHopInterface =
        ctx.ipr_next_hop().iface == null ? null : ctx.ipr_next_hop().iface.getText();
    NecIxStaticRoute route =
        new NecIxStaticRoute(
            ctx.vrf == null ? DEFAULT_VRF_NAME : ctx.vrf.getText(),
            network,
            nextHopIp,
            nextHopInterface);
    for (Ipr_optionContext option : ctx.ipr_option()) {
      if (option.distance != null) {
        Optional<Integer> distance =
            toInteger(ctx, option.distance, ROUTE_DISTANCES, "route distance");
        if (!distance.isPresent()) {
          return;
        }
        route.setDistance(distance.get());
      } else if (option.metric != null) {
        Optional<Integer> metric = toInteger(ctx, option.metric, ROUTE_METRICS, "route metric");
        if (!metric.isPresent()) {
          return;
        }
        route.setMetric(metric.get());
      } else if (option.tag != null) {
        Optional<Long> tag = toLong(ctx, option.tag, 0L, 4294967295L, "route tag");
        if (!tag.isPresent()) {
          return;
        }
        route.setTag(tag.get());
      }
      // `connected` only applies to dial-on-demand interfaces.
    }
    _c.getStaticRoutes().add(route);
  }

  // ----------------------------------------------------------------------------- route-maps

  @Override
  public void enterS_route_map(S_route_mapContext ctx) {
    String name = ctx.name.getText();
    int seq = DEFAULT_ROUTE_MAP_SEQUENCE;
    if (ctx.seq != null) {
      Optional<Integer> parsed =
          toInteger(ctx, ctx.seq, ROUTE_MAP_SEQUENCES, "route-map sequence number");
      if (!parsed.isPresent()) {
        _currentRouteMapEntry = new NecIxRouteMap.Entry(0, LineAction.DENY); // discarded
        return;
      }
      seq = parsed.get();
    }
    LineAction action =
        ctx.action == null || ctx.action.getType() == NecIxLexer.PERMIT
            ? LineAction.PERMIT
            : LineAction.DENY;
    int entrySeq = seq;
    _currentRouteMapEntry =
        _c.getRouteMaps()
            .computeIfAbsent(name, NecIxRouteMap::new)
            .getEntries()
            .computeIfAbsent(entrySeq, s -> new NecIxRouteMap.Entry(entrySeq, action));
  }

  @Override
  public void exitS_route_map(S_route_mapContext ctx) {
    if (_c.getRouteMaps().containsKey(ctx.name.getText())) {
      _c.defineStructure(NecIxStructureType.ROUTE_MAP, ctx.name.getText(), ctx);
    }
    _currentRouteMapEntry = null;
  }

  private @Nonnull NecIxRouteMap.Entry currentRouteMapEntry() {
    assert _currentRouteMapEntry != null;
    return _currentRouteMapEntry;
  }

  @Override
  public void exitRmm_prefix_list(Rmm_prefix_listContext ctx) {
    String name = ctx.name.getText().toLowerCase();
    currentRouteMapEntry().getMatchPrefixLists().add(name);
    _c.referenceStructure(
        NecIxStructureType.PREFIX_LIST,
        name,
        NecIxStructureUsage.ROUTE_MAP_MATCH_PREFIX_LIST,
        ctx.getStart().getLine());
  }

  @Override
  public void exitRmm_unimplemented(Rmm_unimplementedContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRms_local_preference(Rms_local_preferenceContext ctx) {
    toLong(ctx, ctx.value, 0L, 4294967295L, "local preference")
        .ifPresent(currentRouteMapEntry()::setSetLocalPreference);
  }

  @Override
  public void exitRms_metric(Rms_metricContext ctx) {
    toLong(ctx, ctx.value, 0L, 4294967295L, "metric")
        .ifPresent(currentRouteMapEntry()::setSetMetric);
  }

  @Override
  public void exitRms_community(Rms_communityContext ctx) {
    ImmutableList.Builder<StandardCommunity> communities = ImmutableList.builder();
    for (CommunityContext community : ctx.communities) {
      Optional<StandardCommunity> parsed = toStandardCommunity(ctx, community);
      if (!parsed.isPresent()) {
        return;
      }
      communities.add(parsed.get());
    }
    currentRouteMapEntry().setSetCommunities(communities.build(), ctx.ADDITIVE() != null);
  }

  private @Nonnull Optional<StandardCommunity> toStandardCommunity(
      ParserRuleContext ctx, CommunityContext community) {
    if (community.NO_ADVERTISE() != null) {
      return Optional.of(StandardCommunity.NO_ADVERTISE);
    } else if (community.NO_EXPORT() != null) {
      return Optional.of(StandardCommunity.NO_EXPORT);
    } else if (community.NO_EXPORT_SUBCONFED() != null) {
      return Optional.of(StandardCommunity.NO_EXPORT_SUBCONFED);
    }
    String text = community.getText();
    String[] parts = text.split(":", -1);
    Optional<Long> high = toLong(ctx, parts[0], 0L, 65535L, "community AS number");
    Optional<Long> low = toLong(ctx, parts[1], 0L, 65535L, "community value");
    if (!high.isPresent() || !low.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(StandardCommunity.of(high.get().intValue(), low.get().intValue()));
  }

  @Override
  public void exitRms_as_path_prepend(Rms_as_path_prependContext ctx) {
    ImmutableList.Builder<Long> asns = ImmutableList.builder();
    for (UintContext asn : ctx.asns) {
      Optional<Integer> parsed = toInteger(ctx, asn, BGP_AS_NUMBERS, "AS number");
      if (!parsed.isPresent()) {
        return;
      }
      asns.add((long) parsed.get());
    }
    currentRouteMapEntry().getAsPathPrepend().addAll(asns.build());
  }

  @Override
  public void exitRms_unimplemented(Rms_unimplementedContext ctx) {
    todo(ctx);
  }

  // ----------------------------------------------------------------------------- BGP

  @Override
  public void enterS_router_bgp(S_router_bgpContext ctx) {
    Optional<Integer> asn = toInteger(ctx, ctx.asn, BGP_AS_NUMBERS, "AS number");
    NecIxBgpProcess proc = _c.getBgpProcess();
    if (proc == null) {
      proc = new NecIxBgpProcess(asn.isPresent() ? asn.get() : 0L);
      if (asn.isPresent()) {
        _c.setBgpProcess(proc);
      }
    } else if (asn.isPresent() && proc.getAsn() != asn.get()) {
      warn(ctx, "Only one BGP process is allowed; ignoring this one");
      proc = new NecIxBgpProcess(asn.get());
    }
    _currentBgpProcess = proc;
    _currentBgpVrf = proc.getOrCreateVrf(DEFAULT_VRF_NAME);
    _inIpv6AddressFamily = false;
  }

  @Override
  public void exitS_router_bgp(S_router_bgpContext ctx) {
    _currentBgpProcess = null;
    _currentBgpVrf = null;
  }

  private @Nonnull NecIxBgpVrf currentBgpVrf() {
    assert _currentBgpVrf != null;
    return _currentBgpVrf;
  }

  @Override
  public void exitRb_vrf(Rb_vrfContext ctx) {
    assert _currentBgpProcess != null;
    _currentBgpVrf = _currentBgpProcess.getOrCreateVrf(ctx.name.getText());
    _inIpv6AddressFamily = false;
  }

  @Override
  public void exitRb_address_family(Rb_address_familyContext ctx) {
    _inIpv6AddressFamily = ctx.family.getType() == NecIxLexer.IPV6;
  }

  @Override
  public void exitRb_router_id(Rb_router_idContext ctx) {
    assert _currentBgpProcess != null;
    _currentBgpProcess.setRouterId(Ip.parse(ctx.id.getText()));
  }

  @Override
  public void exitRb_default_local_preference(Rb_default_local_preferenceContext ctx) {
    assert _currentBgpProcess != null;
    toLong(ctx, ctx.value, 0L, 4294967295L, "local preference")
        .ifPresent(_currentBgpProcess::setDefaultLocalPreference);
  }

  @Override
  public void enterRb_neighbor(Rb_neighborContext ctx) {
    _currentBgpNeighbor =
        _inIpv6AddressFamily
            ? new NecIxBgpNeighbor(Ip.parse(ctx.ip.getText())) // discarded
            : currentBgpVrf().getOrCreateNeighbor(Ip.parse(ctx.ip.getText()));
  }

  @Override
  public void exitRb_neighbor(Rb_neighborContext ctx) {
    _currentBgpNeighbor = null;
  }

  private @Nonnull NecIxBgpNeighbor currentBgpNeighbor() {
    assert _currentBgpNeighbor != null;
    return _currentBgpNeighbor;
  }

  @Override
  public void exitRbn_remote_as(Rbn_remote_asContext ctx) {
    toInteger(ctx, ctx.asn, BGP_AS_NUMBERS, "AS number")
        .ifPresent(asn -> currentBgpNeighbor().setRemoteAs((long) asn));
  }

  @Override
  public void exitRbn_route_map(Rbn_route_mapContext ctx) {
    String name = ctx.name.getText();
    boolean in = ctx.direction.getType() == NecIxLexer.IN;
    if (in) {
      currentBgpNeighbor().setRouteMapIn(name);
    } else {
      currentBgpNeighbor().setRouteMapOut(name);
    }
    _c.referenceStructure(
        NecIxStructureType.ROUTE_MAP,
        name,
        in
            ? NecIxStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_IN
            : NecIxStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_OUT,
        ctx.getStart().getLine());
  }

  @Override
  public void exitRbn_send_default(Rbn_send_defaultContext ctx) {
    currentBgpNeighbor().setSendDefault(true);
  }

  @Override
  public void exitRbno_send_default(Rbno_send_defaultContext ctx) {
    if (_inIpv6AddressFamily) {
      return;
    }
    currentBgpVrf().getOrCreateNeighbor(Ip.parse(ctx.ip.getText())).setSendDefault(false);
  }

  @Override
  public void exitRbn_shutdown(Rbn_shutdownContext ctx) {
    currentBgpNeighbor().setShutdown(true);
  }

  @Override
  public void exitRbn_update_source(Rbn_update_sourceContext ctx) {
    currentBgpNeighbor().setUpdateSource(ctx.iface.getText());
  }

  @Override
  public void exitRbn_ebgp_multihop(Rbn_ebgp_multihopContext ctx) {
    if (ctx.ttl == null) {
      currentBgpNeighbor().setEbgpMultihop(255);
      return;
    }
    toInteger(ctx, ctx.ttl, IntegerSpace.of(Range.closed(1, 254)), "TTL")
        .ifPresent(currentBgpNeighbor()::setEbgpMultihop);
  }

  @Override
  public void exitRbn_next_hop_self(Rbn_next_hop_selfContext ctx) {
    currentBgpNeighbor().setNextHopSelf(true);
  }

  @Override
  public void exitRb_network(Rb_networkContext ctx) {
    if (_inIpv6AddressFamily) {
      return;
    }
    currentBgpVrf().getNetworks().add(Prefix.parse(ctx.prefix.getText()));
  }

  @Override
  public void exitRb_originate_default(Rb_originate_defaultContext ctx) {
    if (_inIpv6AddressFamily) {
      return;
    }
    currentBgpVrf()
        .setOriginateDefault(
            ctx.ALWAYS() != null
                ? NecIxBgpVrf.OriginateDefault.ALWAYS
                : NecIxBgpVrf.OriginateDefault.CONDITIONAL);
  }

  @Override
  public void exitRb_redistribute(Rb_redistributeContext ctx) {
    if (_inIpv6AddressFamily) {
      return;
    }
    Long metric = null;
    if (ctx.metric != null) {
      Optional<Long> parsed = toLong(ctx, ctx.metric, 0L, 4294967295L, "metric");
      if (!parsed.isPresent()) {
        return;
      }
      metric = parsed.get();
    }
    String routeMap = ctx.route_map == null ? null : ctx.route_map.getText();
    if (routeMap != null) {
      _c.referenceStructure(
          NecIxStructureType.ROUTE_MAP,
          routeMap,
          NecIxStructureUsage.BGP_REDISTRIBUTE_ROUTE_MAP,
          ctx.getStart().getLine());
    }
    currentBgpVrf()
        .getRedistributions()
        .add(
            new NecIxBgpVrf.Redistribution(
                ctx.protocol.getType() == NecIxLexer.CONNECTED
                    ? RoutingProtocol.CONNECTED
                    : RoutingProtocol.STATIC,
                metric,
                routeMap));
  }

  // ----------------------------------------------------------------------------- helpers

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext ctx, Token token, IntegerSpace space, String name) {
    return toInteger(ctx, token.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext ctx, UintContext uint, IntegerSpace space, String name) {
    return toInteger(ctx, uint.getText(), space, name);
  }

  /**
   * Converts {@code text} to an integer if it falls within {@code space}, otherwise warns
   * (attributing the warning to {@code ctx}) and returns {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext ctx, String text, IntegerSpace space, String name) {
    long value;
    try {
      value = Long.parseLong(text);
    } catch (NumberFormatException e) {
      warn(ctx, String.format("Expected %s in range %s, but got '%s'", name, space, text));
      return Optional.empty();
    }
    if (value > Integer.MAX_VALUE || !space.contains((int) value)) {
      warn(ctx, String.format("Expected %s in range %s, but got '%s'", name, space, text));
      return Optional.empty();
    }
    return Optional.of((int) value);
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext ctx, UintContext uint, long min, long max, String name) {
    return toLong(ctx, uint.getText(), min, max, name);
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext ctx, String text, long min, long max, String name) {
    long value;
    try {
      value = Long.parseLong(text);
    } catch (NumberFormatException e) {
      value = -1L;
    }
    if (value < min || value > max) {
      warn(
          ctx, String.format("Expected %s in range %d-%d, but got '%s'", name, min, max, text));
      return Optional.empty();
    }
    return Optional.of(value);
  }

  // ------------------------------------------------- BatfishListener overrides

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
