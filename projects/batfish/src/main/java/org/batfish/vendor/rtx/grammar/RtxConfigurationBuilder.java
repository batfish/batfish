package org.batfish.vendor.rtx.grammar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.rtx.grammar.RtxParser.Bgp_filter_match_typeContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ip_interfaceContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ip_routeContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ip_tunnel_nullContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ipf_definitionContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ipi_addressContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ipi_natContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ipi_secure_filterContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ipiv_groupContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Ipr_gatewayContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Nat_address_itemContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Nat_outer_mnemonicContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Nat_port_rangeContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Nat_typeContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Route_gatewayContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Route_networkContext;
import org.batfish.vendor.rtx.grammar.RtxParser.S_natContext;
import org.batfish.vendor.rtx.grammar.RtxParser.S_vlanContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sb_autonomous_systemContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sb_useContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sbe_applyContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sbe_filterContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sbi_apply_bgpContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sbi_apply_staticContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sbi_filterContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sbn_peerContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Sc_promptContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Snd_masquerade_port_rangeContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Snd_typeContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Snda_innerContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Snda_outerContext;
import org.batfish.vendor.rtx.grammar.RtxParser.St_enableContext;
import org.batfish.vendor.rtx.grammar.RtxParser.St_selectContext;
import org.batfish.vendor.rtx.grammar.RtxParser.Vrrp_paramContext;
import org.batfish.vendor.rtx.representation.RtxBgpExportRule;
import org.batfish.vendor.rtx.representation.RtxBgpImportRule;
import org.batfish.vendor.rtx.representation.RtxBgpNeighbor;
import org.batfish.vendor.rtx.representation.RtxBgpProcess;
import org.batfish.vendor.rtx.representation.RtxBgpRouteFilter;
import org.batfish.vendor.rtx.representation.RtxConfiguration;
import org.batfish.vendor.rtx.representation.RtxFilter;
import org.batfish.vendor.rtx.representation.RtxFilterSpecs;
import org.batfish.vendor.rtx.representation.RtxInterface;
import org.batfish.vendor.rtx.representation.RtxNatDescriptor;
import org.batfish.vendor.rtx.representation.RtxStaticRoute;
import org.batfish.vendor.rtx.representation.RtxStructureType;
import org.batfish.vendor.rtx.representation.RtxStructureUsage;
import org.batfish.vendor.rtx.representation.RtxVrrpGroup;

/** Builds an {@link RtxConfiguration} from a Yamaha RTX parse tree. */
@ParametersAreNonnullByDefault
public final class RtxConfigurationBuilder extends RtxParserBaseListener
    implements SilentSyntaxListener {

  /** The wildcard accepted in place of most {@code ip filter} arguments. */
  private static final String WILDCARD = "*";

  /** Number of positional arguments an {@code ip filter} definition can take. */
  private static final int FILTER_ARGUMENT_COUNT = 5;

  private static final IntegerSpace FILTER_NUMBERS =
      IntegerSpace.builder().including(com.google.common.collect.Range.closed(1, 21474836)).build();
  private static final IntegerSpace BGP_FILTER_NUMBERS =
      IntegerSpace.builder()
          .including(com.google.common.collect.Range.closed(1, Integer.MAX_VALUE))
          .build();
  private static final IntegerSpace VLAN_IDS =
      IntegerSpace.builder().including(com.google.common.collect.Range.closed(1, 4094)).build();
  private static final IntegerSpace VRIDS =
      IntegerSpace.builder().including(com.google.common.collect.Range.closed(1, 255)).build();
  private static final IntegerSpace VRRP_PRIORITIES =
      IntegerSpace.builder().including(com.google.common.collect.Range.closed(1, 254)).build();
  private static final IntegerSpace NAT_DESCRIPTOR_NUMBERS =
      IntegerSpace.builder()
          .including(com.google.common.collect.Range.closed(1, 2147483647))
          .build();
  private static final IntegerSpace PORTS =
      IntegerSpace.builder().including(com.google.common.collect.Range.closed(0, 65535)).build();
  private static final IntegerSpace TUNNEL_NUMBERS =
      IntegerSpace.builder().including(com.google.common.collect.Range.closed(1, 4000)).build();

  private final @Nonnull RtxCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull RtxConfiguration _c;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  /** The interface named by the {@code ip <interface> ...} command being processed. */
  private @Nullable RtxInterface _currentInterface;

  public RtxConfigurationBuilder(
      RtxCombinedParser parser,
      String text,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _c = new RtxConfiguration();
    _c.setExtraLines(_parser.getExtraLines());
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  public @Nonnull RtxConfiguration getConfiguration() {
    return _c;
  }

  // ---------------------------------------------------------------- hostname

  @Override
  public void exitSc_prompt(Sc_promptContext ctx) {
    // RTX has no host-name command; the console prompt is the device's name.
    _c.setHostname(ctx.name.getText());
  }

  // --------------------------------------------------------------- interfaces

  @Override
  public void enterIp_interface(Ip_interfaceContext ctx) {
    _currentInterface = getOrCreateInterface(ctx.iface.getText());
  }

  @Override
  public void exitIp_interface(Ip_interfaceContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitIpi_address(Ipi_addressContext ctx) {
    assert _currentInterface != null;
    _currentInterface.getAddresses().add(ConcreteInterfaceAddress.parse(ctx.addr.getText()));
  }

  @Override
  public void exitS_vlan(S_vlanContext ctx) {
    RtxInterface iface = getOrCreateInterface(ctx.iface.getText());
    // The token text is `vid=<number>`.
    String vidText = ctx.vid.getText().substring("vid=".length());
    toIntegerInSpace(ctx, vidText, VLAN_IDS, "VLAN ID").ifPresent(iface::setVlanId);
  }

  // ------------------------------------------------------------------ filters

  @Override
  public void exitIpf_definition(Ipf_definitionContext ctx) {
    Optional<Integer> maybeNumber =
        toIntegerInSpace(ctx, ctx.num.getText(), FILTER_NUMBERS, "filter number");
    if (!maybeNumber.isPresent()) {
      return;
    }
    long number = maybeNumber.get();

    if (ctx.action.FILTER_RESTRICT() != null) {
      // `restrict` passes traffic only while the WAN line is connected, which has no analogue in a
      // static analysis.
      warn(ctx, "restrict filters are not supported and will be ignored");
      return;
    }
    LineAction action = ctx.action.FILTER_PASS() != null ? LineAction.PERMIT : LineAction.DENY;

    List<String> args =
        RtxFilterSpecs.padArguments(
            ctx.args.stream().map(Token::getText).collect(ImmutableList.toImmutableList()),
            FILTER_ARGUMENT_COUNT);
    if (ctx.args.size() > FILTER_ARGUMENT_COUNT) {
      warn(
          ctx,
          String.format(
              "Expected at most %d arguments, but got %d; ignoring this filter",
              FILTER_ARGUMENT_COUNT, ctx.args.size()));
      return;
    }
    String srcText = args.get(0);
    String dstText = args.get(1);
    String protocolText = args.get(2);
    String srcPortText = args.get(3);
    String dstPortText = args.get(4);

    IpSpace srcIps = null;
    if (!srcText.equals(WILDCARD)) {
      Optional<IpSpace> parsed = RtxFilterSpecs.parseAddress(srcText);
      if (!parsed.isPresent()) {
        warn(ctx, addressWarning("source", srcText));
        return;
      }
      srcIps = parsed.get();
    }

    IpSpace dstIps = null;
    if (!dstText.equals(WILDCARD)) {
      Optional<IpSpace> parsed = RtxFilterSpecs.parseAddress(dstText);
      if (!parsed.isPresent()) {
        warn(ctx, addressWarning("destination", dstText));
        return;
      }
      dstIps = parsed.get();
    }

    Set<IpProtocol> protocols = ImmutableSet.of();
    boolean established = false;
    if (!protocolText.equals(WILDCARD)) {
      Optional<Set<IpProtocol>> parsed = RtxFilterSpecs.parseProtocols(protocolText);
      if (!parsed.isPresent()) {
        warn(
            ctx,
            String.format(
                "Cannot interpret '%s' as a protocol; ignoring this filter", protocolText));
        return;
      }
      protocols = parsed.get();
      established = RtxFilterSpecs.isEstablished(protocolText);
    }

    IntegerSpace srcPorts = null;
    IntegerSpace dstPorts = null;
    IntegerSpace icmpTypes = null;
    IntegerSpace icmpCodes = null;
    if (protocols.equals(ImmutableSet.of(IpProtocol.ICMP))) {
      // For a filter whose only protocol is ICMP, the two port arguments are the ICMP type and
      // code instead.
      if (!srcPortText.equals(WILDCARD)) {
        Optional<IntegerSpace> parsed = RtxFilterSpecs.parseIcmpTypesOrCodes(srcPortText);
        if (!parsed.isPresent()) {
          warn(ctx, icmpWarning("type", srcPortText));
          return;
        }
        icmpTypes = parsed.get();
      }
      if (!dstPortText.equals(WILDCARD)) {
        Optional<IntegerSpace> parsed = RtxFilterSpecs.parseIcmpTypesOrCodes(dstPortText);
        if (!parsed.isPresent()) {
          warn(ctx, icmpWarning("code", dstPortText));
          return;
        }
        icmpCodes = parsed.get();
      }
    } else {
      if (!srcPortText.equals(WILDCARD)) {
        Optional<IntegerSpace> parsed = RtxFilterSpecs.parsePorts(srcPortText);
        if (!parsed.isPresent()) {
          warn(ctx, portWarning("source", srcPortText));
          return;
        }
        srcPorts = parsed.get();
      }
      if (!dstPortText.equals(WILDCARD)) {
        Optional<IntegerSpace> parsed = RtxFilterSpecs.parsePorts(dstPortText);
        if (!parsed.isPresent()) {
          warn(ctx, portWarning("destination", dstPortText));
          return;
        }
        dstPorts = parsed.get();
      }
      if (protocols.isEmpty() && (srcPorts != null || dstPorts != null)) {
        // Port numbers only exist in TCP and UDP headers. The device documents port arguments as
        // applying when the protocol includes TCP or UDP, so a wildcard protocol combined with a
        // port constraint is narrowed to those two.
        protocols = ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP);
      }
    }

    _c.getFilters()
        .put(
            number,
            new RtxFilter(
                number,
                action,
                srcIps,
                dstIps,
                protocols,
                established,
                srcPorts,
                dstPorts,
                icmpTypes,
                icmpCodes));
    _c.defineStructure(RtxStructureType.FILTER, Long.toString(number), ctx);
  }

  @Override
  public void exitIpi_secure_filter(Ipi_secure_filterContext ctx) {
    assert _currentInterface != null;
    boolean inbound = ctx.direction.IN() != null;
    RtxStructureUsage usage =
        inbound ? RtxStructureUsage.SECURE_FILTER_IN : RtxStructureUsage.SECURE_FILTER_OUT;
    ImmutableList.Builder<Long> numbers = ImmutableList.builder();
    for (Token token : ctx.nums) {
      Optional<Integer> number =
          toIntegerInSpace(ctx, token.getText(), FILTER_NUMBERS, "filter number");
      if (!number.isPresent()) {
        continue;
      }
      numbers.add((long) number.get());
      _c.referenceStructure(
          RtxStructureType.FILTER, Integer.toString(number.get()), usage, token.getLine());
    }
    if (inbound) {
      _currentInterface.setInboundFilters(numbers.build());
    } else {
      _currentInterface.setOutboundFilters(numbers.build());
    }
  }

  // --------------------------------------------------------------------- VRRP

  @Override
  public void exitIpiv_group(Ipiv_groupContext ctx) {
    assert _currentInterface != null;
    Optional<Integer> vrid = toIntegerInSpace(ctx, ctx.vrid.getText(), VRIDS, "VRRP group ID");
    if (!vrid.isPresent()) {
      return;
    }
    RtxVrrpGroup group = new RtxVrrpGroup(vrid.get(), Ip.parse(ctx.vip.getText()));
    for (Vrrp_paramContext param : ctx.params) {
      if (param.VRRP_PRIORITY() != null) {
        String value = valueOf(param.VRRP_PRIORITY().getText());
        Optional<Integer> priority = toIntegerInSpace(ctx, value, VRRP_PRIORITIES, "VRRP priority");
        if (!priority.isPresent()) {
          return;
        }
        group.setPriority(priority.get());
      } else if (param.VRRP_PREEMPT() != null) {
        group.setPreempt(valueOf(param.VRRP_PREEMPT().getText()).equals("on"));
      }
      // The authentication string and the advertisement and down intervals do not change which
      // router owns the virtual address in a static analysis.
    }
    _currentInterface.getVrrpGroups().put(group.getVrid(), group);
  }

  // ------------------------------------------------------------------- routes

  @Override
  public void exitIp_route(Ip_routeContext ctx) {
    Prefix network = toPrefix(ctx.network);
    for (Ipr_gatewayContext gatewayCtx : ctx.gateways) {
      Route_gatewayContext gateway = gatewayCtx.route_gateway();
      if (gateway.ip != null) {
        _c.getStaticRoutes().add(RtxStaticRoute.ofIp(network, Ip.parse(gateway.ip.getText())));
      } else {
        Optional<Integer> num =
            toIntegerInSpace(ctx, gateway.num.getText(), TUNNEL_NUMBERS, "tunnel number");
        if (!num.isPresent()) {
          continue;
        }
        String tunnelName = tunnelInterfaceName(num.get());
        getOrCreateInterface(tunnelName);
        _c.getStaticRoutes().add(RtxStaticRoute.ofInterface(network, tunnelName));
      }
      if (!gatewayCtx.route_gateway_param().isEmpty()) {
        // filter-based gateway selection and metric/weight/keepalive preferences are not modeled.
        todo(gatewayCtx);
      }
    }
  }

  // ------------------------------------------------------------------ tunnels

  @Override
  public void exitSt_select(St_selectContext ctx) {
    createTunnelInterface(ctx, ctx.num);
  }

  @Override
  public void exitSt_enable(St_enableContext ctx) {
    createTunnelInterface(ctx, ctx.num);
  }

  @Override
  public void exitIp_tunnel_null(Ip_tunnel_nullContext ctx) {
    // `ip tunnel secure filter ...` applies a filter that the model will not have. The other
    // `ip tunnel` sub-commands (mtu, tcp mss limit) do not change what Batfish computes.
    if (!ctx.IGNORED_TEXT().isEmpty() && ctx.IGNORED_TEXT(0).getText().equals("secure")) {
      todo(ctx);
    }
  }

  private void createTunnelInterface(ParserRuleContext ctx, Token num) {
    toIntegerInSpace(ctx, num.getText(), TUNNEL_NUMBERS, "tunnel number")
        .ifPresent(n -> getOrCreateInterface(tunnelInterfaceName(n)));
  }

  // ---------------------------------------------------------------------- BGP

  @Override
  public void exitSb_use(Sb_useContext ctx) {
    _c.getOrCreateBgpProcess().setEnabled(ctx.ON() != null);
  }

  @Override
  public void exitSb_autonomous_system(Sb_autonomous_systemContext ctx) {
    Optional<Long> as = toAsNumber(ctx, ctx.as);
    as.ifPresent(value -> _c.getOrCreateBgpProcess().setLocalAs(value));
  }

  @Override
  public void exitSbn_peer(Sbn_peerContext ctx) {
    Optional<Long> remoteAs = toAsNumber(ctx, ctx.remote_as);
    if (!remoteAs.isPresent()) {
      return;
    }
    Optional<Integer> id = toIntegerInSpace(ctx, ctx.id.getText(), VRIDS, "BGP neighbor ID");
    if (!id.isPresent()) {
      return;
    }
    RtxBgpProcess process = _c.getOrCreateBgpProcess();
    process
        .getNeighbors()
        .put(
            id.get(),
            new RtxBgpNeighbor(id.get(), remoteAs.get(), Ip.parse(ctx.remote_ip.getText())));
  }

  @Override
  public void exitSbi_filter(Sbi_filterContext ctx) {
    Optional<Integer> number = toBgpFilterNumber(ctx, ctx.number);
    if (!number.isPresent()) {
      return;
    }
    RtxBgpRouteFilter filter =
        toBgpRouteFilter(number.get(), ctx.reject, ctx.match_type, ctx.prefixes);
    _c.getOrCreateBgpProcess().getImportFilters().put(number.get(), filter);
    _c.defineStructure(RtxStructureType.BGP_IMPORT_FILTER, Integer.toString(number.get()), ctx);
  }

  @Override
  public void exitSbe_filter(Sbe_filterContext ctx) {
    Optional<Integer> number = toBgpFilterNumber(ctx, ctx.number);
    if (!number.isPresent()) {
      return;
    }
    RtxBgpRouteFilter filter =
        toBgpRouteFilter(number.get(), ctx.reject, ctx.match_type, ctx.prefixes);
    _c.getOrCreateBgpProcess().getExportFilters().put(number.get(), filter);
    _c.defineStructure(RtxStructureType.BGP_EXPORT_FILTER, Integer.toString(number.get()), ctx);
  }

  @Override
  public void exitSbi_apply_static(Sbi_apply_staticContext ctx) {
    Optional<Long> remoteAs = toAsNumber(ctx, ctx.remote_as);
    List<Integer> filters =
        toBgpFilterNumbers(
            ctx,
            ctx.filters,
            RtxStructureType.BGP_IMPORT_FILTER,
            RtxStructureUsage.BGP_IMPORT_FILTER);
    if (remoteAs.isPresent()) {
      _c.getOrCreateBgpProcess()
          .getImportRules()
          .add(
              new RtxBgpImportRule(
                  remoteAs.get(), RtxBgpImportRule.Protocol.STATIC, null, filters));
    }
  }

  @Override
  public void exitSbi_apply_bgp(Sbi_apply_bgpContext ctx) {
    Optional<Long> remoteAs = toAsNumber(ctx, ctx.remote_as);
    Optional<Long> fromAs = toAsNumber(ctx, ctx.from_as);
    List<Integer> filters =
        toBgpFilterNumbers(
            ctx,
            ctx.filters,
            RtxStructureType.BGP_IMPORT_FILTER,
            RtxStructureUsage.BGP_IMPORT_FILTER);
    if (remoteAs.isPresent() && fromAs.isPresent()) {
      _c.getOrCreateBgpProcess()
          .getImportRules()
          .add(
              new RtxBgpImportRule(
                  remoteAs.get(), RtxBgpImportRule.Protocol.BGP, fromAs.get(), filters));
    }
  }

  @Override
  public void exitSbe_apply(Sbe_applyContext ctx) {
    Optional<Long> remoteAs = toAsNumber(ctx, ctx.remote_as);
    List<Integer> filters =
        toBgpFilterNumbers(
            ctx,
            ctx.filters,
            RtxStructureType.BGP_EXPORT_FILTER,
            RtxStructureUsage.BGP_EXPORT_FILTER);
    if (remoteAs.isPresent()) {
      _c.getOrCreateBgpProcess()
          .getExportRules()
          .add(new RtxBgpExportRule(remoteAs.get(), filters));
    }
  }

  private @Nonnull RtxBgpRouteFilter toBgpRouteFilter(
      int number,
      @Nullable Token reject,
      Bgp_filter_match_typeContext matchType,
      List<Token> prefixes) {
    LineAction action = reject == null ? LineAction.PERMIT : LineAction.DENY;
    RtxBgpRouteFilter.MatchType type;
    if (matchType.INCLUDE() != null) {
      type = RtxBgpRouteFilter.MatchType.INCLUDE;
    } else if (matchType.REFINES() != null) {
      type = RtxBgpRouteFilter.MatchType.REFINES;
    } else {
      assert matchType.EQUAL() != null;
      type = RtxBgpRouteFilter.MatchType.EQUAL;
    }
    return new RtxBgpRouteFilter(
        number,
        action,
        type,
        prefixes.stream()
            .map(Token::getText)
            .map(Prefix::parse)
            .collect(ImmutableList.toImmutableList()));
  }

  private @Nonnull Optional<Integer> toBgpFilterNumber(ParserRuleContext ctx, Token token) {
    return toIntegerInSpace(ctx, token.getText(), BGP_FILTER_NUMBERS, "BGP filter number");
  }

  private @Nonnull List<Integer> toBgpFilterNumbers(
      ParserRuleContext ctx, List<Token> tokens, RtxStructureType type, RtxStructureUsage usage) {
    ImmutableList.Builder<Integer> numbers = ImmutableList.builder();
    for (Token token : tokens) {
      Optional<Integer> number = toBgpFilterNumber(ctx, token);
      if (!number.isPresent()) {
        continue;
      }
      numbers.add(number.get());
      _c.referenceStructure(type, Integer.toString(number.get()), usage, token.getLine());
    }
    return numbers.build();
  }

  // ---------------------------------------------------------------------- NAT

  @Override
  public void exitS_nat(S_natContext ctx) {
    if (ctx.snd_null() != null) {
      // `nat descriptor log|timer|static|masquerade session limit|...`. None of these change the
      // translation Batfish models.
      return;
    }
    _c.defineStructure(RtxStructureType.NAT_DESCRIPTOR, natDescriptorName(ctx), ctx);
  }

  @Override
  public void exitSnd_type(Snd_typeContext ctx) {
    Optional<Integer> number = toNatDescriptorNumber(ctx, ctx.num);
    if (!number.isPresent()) {
      return;
    }
    _c.getOrCreateNatDescriptor(number.get()).setType(toNatType(ctx.type));
    if (ctx.HAIRPIN() != null) {
      // Hairpin NAT lets an inside host reach another inside host by its outer address.
      todo(ctx);
    }
  }

  @Override
  public void exitSnda_outer(Snda_outerContext ctx) {
    Optional<Integer> number = toNatDescriptorNumber(ctx, ctx.num);
    if (!number.isPresent()) {
      return;
    }
    RtxNatDescriptor descriptor = _c.getOrCreateNatDescriptor(number.get());
    if (ctx.mnemonic != null) {
      descriptor.setOuterMnemonic(toOuterMnemonic(ctx.mnemonic));
      return;
    }
    boolean first = true;
    for (Nat_address_itemContext item : ctx.items) {
      Ip low = Ip.parse(item.low.getText());
      Ip high = item.high == null ? low : Ip.parse(item.high.getText());
      if (low.asLong() > high.asLong()) {
        warn(ctx, String.format("Invalid address range %s-%s; ignoring it", low, high));
        continue;
      }
      descriptor.getOuterAddresses().add(Range.closed(low, high));
      if (first) {
        // Masquerade uses only the first outer address, however large the pool is.
        descriptor.setFirstOuterAddress(low);
        first = false;
      }
    }
  }

  @Override
  public void exitSnda_inner(Snda_innerContext ctx) {
    Optional<Integer> number = toNatDescriptorNumber(ctx, ctx.num);
    if (!number.isPresent()) {
      return;
    }
    if (ctx.AUTO() != null) {
      // `auto` is the default and means every inner address is eligible.
      return;
    }
    ImmutableList.Builder<IpSpace> spaces = ImmutableList.builder();
    for (Nat_address_itemContext item : ctx.items) {
      Ip low = Ip.parse(item.low.getText());
      Ip high = item.high == null ? low : Ip.parse(item.high.getText());
      if (low.asLong() > high.asLong()) {
        warn(ctx, String.format("Invalid address range %s-%s; ignoring it", low, high));
        continue;
      }
      spaces.add(item.high == null ? low.toIpSpace() : IpRange.range(low, high));
    }
    IpSpace inner = AclIpSpace.union(spaces.build());
    if (inner != null) {
      _c.getOrCreateNatDescriptor(number.get()).setInnerAddresses(inner);
    }
  }

  @Override
  public void exitSnd_masquerade_port_range(Snd_masquerade_port_rangeContext ctx) {
    Optional<Integer> number = toNatDescriptorNumber(ctx, ctx.num);
    if (!number.isPresent()) {
      return;
    }
    IntegerSpace.Builder ports = IntegerSpace.builder();
    for (Nat_port_rangeContext range : ctx.ranges) {
      Optional<Integer> low = toIntegerInSpace(ctx, range.low.getText(), PORTS, "port");
      Optional<Integer> high = toIntegerInSpace(ctx, range.high.getText(), PORTS, "port");
      if (!low.isPresent() || !high.isPresent()) {
        return;
      }
      if (low.get() > high.get()) {
        warn(
            ctx,
            String.format(
                "Invalid port range %d-%d; ignoring this command", low.get(), high.get()));
        return;
      }
      ports.including(Range.closed(low.get(), high.get()));
    }
    _c.getOrCreateNatDescriptor(number.get()).setMasqueradePorts(ports.build());
  }

  @Override
  public void exitIpi_nat(Ipi_natContext ctx) {
    assert _currentInterface != null;
    for (Token token : ctx.nums) {
      Optional<Integer> number = toNatDescriptorNumber(ctx, token);
      if (!number.isPresent()) {
        continue;
      }
      _currentInterface.getNatDescriptors().add(number.get());
      _c.referenceStructure(
          RtxStructureType.NAT_DESCRIPTOR,
          Integer.toString(number.get()),
          RtxStructureUsage.INTERFACE_NAT_DESCRIPTOR,
          token.getLine());
    }
    if (ctx.reverse != null && !ctx.reverse.isEmpty()) {
      // Descriptors after `reverse` translate the opposite direction, which is not modeled.
      todo(ctx);
      for (Token token : ctx.reverse) {
        toNatDescriptorNumber(ctx, token)
            .ifPresent(number -> _currentInterface.getReverseNatDescriptors().add(number));
      }
    }
  }

  /** The structure name of the descriptor a {@code nat descriptor} command configures. */
  private static @Nonnull String natDescriptorName(S_natContext ctx) {
    if (ctx.snd_type() != null) {
      return ctx.snd_type().num.getText();
    }
    if (ctx.snd_masquerade_port_range() != null) {
      return ctx.snd_masquerade_port_range().num.getText();
    }
    Snda_outerContext outer = ctx.snd_address().snda_outer();
    return outer != null ? outer.num.getText() : ctx.snd_address().snda_inner().num.getText();
  }

  private static @Nonnull RtxNatDescriptor.Type toNatType(Nat_typeContext ctx) {
    if (ctx.NAT_MASQUERADE() != null) {
      return RtxNatDescriptor.Type.NAT_MASQUERADE;
    }
    if (ctx.MASQUERADE() != null) {
      return RtxNatDescriptor.Type.MASQUERADE;
    }
    if (ctx.NAT() != null) {
      return RtxNatDescriptor.Type.NAT;
    }
    assert ctx.NONE() != null;
    return RtxNatDescriptor.Type.NONE;
  }

  private static @Nonnull RtxNatDescriptor.OuterAddressMnemonic toOuterMnemonic(
      Nat_outer_mnemonicContext ctx) {
    if (ctx.PRIMARY() != null) {
      return RtxNatDescriptor.OuterAddressMnemonic.PRIMARY;
    }
    if (ctx.SECONDARY() != null) {
      return RtxNatDescriptor.OuterAddressMnemonic.SECONDARY;
    }
    if (ctx.MAP_E() != null) {
      return RtxNatDescriptor.OuterAddressMnemonic.MAP_E;
    }
    if (ctx.HB46PP() != null) {
      return RtxNatDescriptor.OuterAddressMnemonic.HB46PP;
    }
    assert ctx.IPCP() != null;
    return RtxNatDescriptor.OuterAddressMnemonic.IPCP;
  }

  private @Nonnull Optional<Integer> toNatDescriptorNumber(ParserRuleContext ctx, Token token) {
    return toIntegerInSpace(ctx, token.getText(), NAT_DESCRIPTOR_NUMBERS, "NAT descriptor number");
  }

  // ------------------------------------------------------------------ helpers

  /**
   * Returns the interface named {@code name}, creating it if no command has named it yet. Creating
   * an 802.1Q sub-interface also creates its parent, which RTX configurations do not have to
   * mention.
   */
  private @Nonnull RtxInterface getOrCreateInterface(String name) {
    RtxInterface existing = _c.getInterfaces().get(name);
    if (existing != null) {
      return existing;
    }
    RtxInterface iface;
    int slash = name.indexOf('/');
    if (name.startsWith("tunnel")) {
      iface = new RtxInterface(name, RtxInterface.Type.TUNNEL);
    } else if (slash > 0) {
      iface = new RtxInterface(name, RtxInterface.Type.LAN_SUBINTERFACE);
      String parent = name.substring(0, slash);
      getOrCreateInterface(parent);
      iface.setParentInterface(parent);
    } else {
      iface = new RtxInterface(name, RtxInterface.Type.LAN);
    }
    _c.getInterfaces().put(name, iface);
    return iface;
  }

  private static @Nonnull String tunnelInterfaceName(int number) {
    return "tunnel" + number;
  }

  /** Returns the part of a {@code key=value} token after the {@code =}. */
  private static @Nonnull String valueOf(String assignment) {
    return assignment.substring(assignment.indexOf('=') + 1);
  }

  /** The destination of an {@code ip route} command. Every form the grammar accepts is valid. */
  private static @Nonnull Prefix toPrefix(Route_networkContext network) {
    if (network.DEFAULT() != null) {
      return Prefix.ZERO;
    }
    if (network.IPV4_PREFIX() != null) {
      return Prefix.parse(network.IPV4_PREFIX().getText());
    }
    // A bare address means a host route.
    return Prefix.create(Ip.parse(network.IPV4_ADDRESS().getText()), Prefix.MAX_PREFIX_LENGTH);
  }

  private @Nonnull Optional<Long> toAsNumber(ParserRuleContext ctx, Token token) {
    long value;
    try {
      value = Long.parseLong(token.getText());
    } catch (NumberFormatException e) {
      warn(ctx, String.format("Expected an AS number, but got '%s'", token.getText()));
      return Optional.empty();
    }
    if (value < 1 || value > 4294967295L) {
      warn(ctx, String.format("Expected an AS number in range 1-4294967295, but got '%d'", value));
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /**
   * Converts {@code text} to an integer if it falls within {@code space}, otherwise warns
   * (attributing the warning to {@code messageCtx}) and returns {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, String text, IntegerSpace space, String name) {
    long value;
    try {
      value = Long.parseLong(text);
    } catch (NumberFormatException e) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%s'", name, space, text));
      return Optional.empty();
    }
    if (value > Integer.MAX_VALUE || !space.contains((int) value)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%s'", name, space, text));
      return Optional.empty();
    }
    return Optional.of((int) value);
  }

  private static @Nonnull String addressWarning(String which, String text) {
    return String.format(
        "Cannot interpret '%s' as a %s address; ignoring this filter", text, which);
  }

  private static @Nonnull String portWarning(String which, String text) {
    return String.format(
        "Cannot interpret '%s' as a %s port list; ignoring this filter", text, which);
  }

  private static @Nonnull String icmpWarning(String which, String text) {
    return String.format("Cannot interpret '%s' as an ICMP %s; ignoring this filter", text, which);
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
