package org.batfish.vendor.a10.grammar;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.vendor.a10.grammar.A10Lexer.WORD;
import static org.batfish.vendor.a10.representation.A10Configuration.arePortTypesCompatible;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceName;
import static org.batfish.vendor.a10.representation.A10StructureType.ACCESS_LIST;
import static org.batfish.vendor.a10.representation.A10StructureType.HEALTH_MONITOR;
import static org.batfish.vendor.a10.representation.A10StructureType.INTERFACE;
import static org.batfish.vendor.a10.representation.A10StructureType.NAT_POOL;
import static org.batfish.vendor.a10.representation.A10StructureType.SERVER;
import static org.batfish.vendor.a10.representation.A10StructureType.SERVICE_GROUP;
import static org.batfish.vendor.a10.representation.A10StructureType.VIRTUAL_SERVER;
import static org.batfish.vendor.a10.representation.A10StructureType.VRRP_A_FAIL_OVER_POLICY_TEMPLATE;
import static org.batfish.vendor.a10.representation.A10StructureType.VRRP_A_VRID;
import static org.batfish.vendor.a10.representation.A10StructureUsage.HA_INTERFACE;
import static org.batfish.vendor.a10.representation.A10StructureUsage.INTERFACE_ACCESS_LIST;
import static org.batfish.vendor.a10.representation.A10StructureUsage.IP_NAT_POOL_VRID;
import static org.batfish.vendor.a10.representation.A10StructureUsage.SERVER_HEALTH_CHECK;
import static org.batfish.vendor.a10.representation.A10StructureUsage.SERVER_PORT_HEALTH_CHECK;
import static org.batfish.vendor.a10.representation.A10StructureUsage.SERVICE_GROUP_HEALTH_CHECK;
import static org.batfish.vendor.a10.representation.A10StructureUsage.SERVICE_GROUP_MEMBER;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VIRTUAL_SERVER_SELF_REF;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VIRTUAL_SERVER_SERVICE_GROUP;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VIRTUAL_SERVER_SOURCE_NAT_POOL;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VIRTUAL_SERVER_VRID;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VRRP_A_INTERFACE;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VRRP_A_VRID_BLADE_PARAMETERS_FAIL_OVER_POLICY_TEMPLATE;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VRRP_A_VRID_DEFAULT_SELF_REFERENCE;
import static org.batfish.vendor.a10.representation.VrrpA.DEFAULT_VRID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.a10.grammar.A10Parser.A10_configurationContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ethernet_numberContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ethernet_or_trunk_referenceContext;
import org.batfish.vendor.a10.grammar.A10Parser.Fail_over_policy_template_nameContext;
import org.batfish.vendor.a10.grammar.A10Parser.Fip_optionContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_check_gatewayContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_check_routeContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_check_vlanContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_conn_mirrorContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_groupContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_idContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_id_numberContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_interfaceContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_preemption_enableContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_priority_numberContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ha_set_id_numberContext;
import org.batfish.vendor.a10.grammar.A10Parser.HostnameContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ipv6_addressContext;
import org.batfish.vendor.a10.grammar.A10Parser.Non_default_vridContext;
import org.batfish.vendor.a10.grammar.A10Parser.S_floating_ipContext;
import org.batfish.vendor.a10.grammar.A10Parser.S_hostnameContext;
import org.batfish.vendor.a10.grammar.A10Parser.Snha_preemption_enableContext;
import org.batfish.vendor.a10.grammar.A10Parser.Sssdno_health_checkContext;
import org.batfish.vendor.a10.grammar.A10Parser.Sssgdt_portContext;
import org.batfish.vendor.a10.grammar.A10Parser.Ssvs_ha_groupContext;
import org.batfish.vendor.a10.grammar.A10Parser.Trunk_numberContext;
import org.batfish.vendor.a10.grammar.A10Parser.Uint8Context;
import org.batfish.vendor.a10.grammar.A10Parser.VridContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpa_device_id_numberContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpa_fail_over_policy_templateContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpa_interfaceContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpa_set_id_numberContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpa_vrid_idContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpa_vrid_leadContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpac_device_idContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpac_disable_default_vridContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpac_enableContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpac_set_idContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpaf_gatewayContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpaf_gateway_weightContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpapg_peerContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpavi_floating_ipContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpavi_preempt_mode_disableContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpavi_preempt_mode_thresholdContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpavib_fail_over_policy_templateContext;
import org.batfish.vendor.a10.grammar.A10Parser.Vrrpavib_priorityContext;
import org.batfish.vendor.a10.grammar.A10Parser.WordContext;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.batfish.vendor.a10.representation.A10StructureType;
import org.batfish.vendor.a10.representation.A10StructureUsage;
import org.batfish.vendor.a10.representation.AccessList;
import org.batfish.vendor.a10.representation.AccessListAddress;
import org.batfish.vendor.a10.representation.AccessListAddressAny;
import org.batfish.vendor.a10.representation.AccessListAddressHost;
import org.batfish.vendor.a10.representation.AccessListAddressPrefix;
import org.batfish.vendor.a10.representation.AccessListAddressWildcard;
import org.batfish.vendor.a10.representation.AccessListRule;
import org.batfish.vendor.a10.representation.AccessListRuleIcmp;
import org.batfish.vendor.a10.representation.AccessListRuleIp;
import org.batfish.vendor.a10.representation.AccessListRuleTcp;
import org.batfish.vendor.a10.representation.AccessListRuleUdp;
import org.batfish.vendor.a10.representation.BgpNeighbor;
import org.batfish.vendor.a10.representation.BgpNeighborId;
import org.batfish.vendor.a10.representation.BgpNeighborIdAddress;
import org.batfish.vendor.a10.representation.BgpNeighborUpdateSource;
import org.batfish.vendor.a10.representation.BgpNeighborUpdateSourceAddress;
import org.batfish.vendor.a10.representation.BgpProcess;
import org.batfish.vendor.a10.representation.FloatingIp;
import org.batfish.vendor.a10.representation.Ha;
import org.batfish.vendor.a10.representation.Interface;
import org.batfish.vendor.a10.representation.Interface.Type;
import org.batfish.vendor.a10.representation.InterfaceLldp;
import org.batfish.vendor.a10.representation.InterfaceReference;
import org.batfish.vendor.a10.representation.NatPool;
import org.batfish.vendor.a10.representation.Server;
import org.batfish.vendor.a10.representation.ServerPort;
import org.batfish.vendor.a10.representation.ServerTarget;
import org.batfish.vendor.a10.representation.ServerTargetAddress;
import org.batfish.vendor.a10.representation.ServiceGroup;
import org.batfish.vendor.a10.representation.ServiceGroupMember;
import org.batfish.vendor.a10.representation.StaticRoute;
import org.batfish.vendor.a10.representation.StaticRouteManager;
import org.batfish.vendor.a10.representation.TrunkGroup;
import org.batfish.vendor.a10.representation.TrunkInterface;
import org.batfish.vendor.a10.representation.VirtualServer;
import org.batfish.vendor.a10.representation.VirtualServerPort;
import org.batfish.vendor.a10.representation.VirtualServerTarget;
import org.batfish.vendor.a10.representation.VirtualServerTargetAddress;
import org.batfish.vendor.a10.representation.VirtualServerTargetAddress6;
import org.batfish.vendor.a10.representation.Vlan;
import org.batfish.vendor.a10.representation.VrrpAFailOverPolicyTemplate;
import org.batfish.vendor.a10.representation.VrrpAVrid;

/** Given a parse tree, builds a {@link A10Configuration}. */
public final class A10ConfigurationBuilder extends A10ParserBaseListener
    implements SilentSyntaxListener {

  public A10ConfigurationBuilder(
      A10CombinedParser parser,
      String text,
      Warnings warnings,
      A10Configuration configuration,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _c = configuration;
    _silentSyntax = silentSyntax;
  }

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

  public @Nonnull A10Configuration getConfiguration() {
    return _c;
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

  @Override
  public void enterA10_configuration(A10_configurationContext ctx) {}

  @Override
  public void exitA10_configuration(A10_configurationContext ctx) {
    _c.finalizeStructures();
  }

  @Override
  public void exitS_health_monitor(A10Parser.S_health_monitorContext ctx) {
    toString(ctx, ctx.health_check_name())
        .ifPresent(
            name -> {
              _c.defineStructure(HEALTH_MONITOR, name, ctx);
              _c.createHealthMonitorIfAbsent(name);
            });
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    toString(ctx, ctx.hostname()).ifPresent(_c::setHostname);
  }

  @Override
  public void exitS_access_list(A10Parser.S_access_listContext ctx) {
    toInteger(ctx, ctx.access_list_number())
        .ifPresent(
            accessListNum -> {
              if (accessListNum < 100) {
                // TODO support standard access lists
                todo(ctx);
                return;
              }
              // Will not clash with named access lists; they can't start with a digit
              String aclName = String.valueOf(accessListNum);
              _c.defineStructure(A10StructureType.ACCESS_LIST, aclName, ctx);
              _c.getOrCreateAccessList(aclName).addRule(toAclRule(ctx.sal_rule_definition()));
            });
  }

  @Override
  public void enterSi_access_list(A10Parser.Si_access_listContext ctx) {
    _currentAccessList =
        toString(ctx, ctx.access_list_name())
            .map(
                n -> {
                  _c.defineStructure(A10StructureType.ACCESS_LIST, n, ctx);
                  return _c.getOrCreateAccessList(n);
                })
            .orElseGet(() -> new AccessList(ctx.access_list_name().getText())); // dummy
  }

  @Override
  public void exitSi_access_list(A10Parser.Si_access_listContext ctx) {
    _currentAccessList = null;
  }

  @Override
  public void exitSial_rule_definition(A10Parser.Sial_rule_definitionContext ctx) {
    toAclRule(ctx).ifPresent(_currentAccessList::addRule);
  }

  private AccessListRule toAclRule(A10Parser.Sal_rule_definitionContext ctx) {
    String lineText = getFullText(ctx);
    AccessListRule.Action action = toAclAction(ctx.access_list_action());
    AccessListAddress source = toAclAddress(ctx.source);
    AccessListAddress destination = toAclAddress(ctx.destination);
    return toAclRule(lineText, action, source, destination, ctx.access_list_protocol(), null);
  }

  /**
   * Convert a rule definition context into an {@link AccessListRule}. Warns and returns {@link
   * Optional#empty()} if the definition is not valid.
   */
  private Optional<AccessListRule> toAclRule(A10Parser.Sial_rule_definitionContext ctx) {
    String lineText = getFullText(ctx);
    AccessListRule.Action action = toAclAction(ctx.access_list_action());
    AccessListAddress source = toAclAddress(ctx.source);
    AccessListAddress destination = toAclAddress(ctx.destination);

    Optional<SubRange> maybeDestRange = Optional.empty();
    if (ctx.dest_range != null) {
      maybeDestRange = toSubRange(ctx, ctx.dest_range);
      if (!maybeDestRange.isPresent()) {
        // Already warned
        return Optional.empty();
      }
    }
    return Optional.of(
        toAclRule(
            lineText,
            action,
            source,
            destination,
            ctx.access_list_protocol(),
            maybeDestRange.orElse(null)));
  }

  private AccessListRule toAclRule(
      String lineText,
      AccessListRule.Action action,
      AccessListAddress source,
      AccessListAddress destination,
      A10Parser.Access_list_protocolContext protocolCtx,
      @Nullable SubRange destRange) {
    if (protocolCtx.ICMP() != null) {
      return new AccessListRuleIcmp(action, source, destination, lineText);
    } else if (protocolCtx.IP() != null) {
      return new AccessListRuleIp(action, source, destination, lineText);
    } else if (protocolCtx.TCP() != null) {
      AccessListRuleTcp tcp = new AccessListRuleTcp(action, source, destination, lineText);
      tcp.setDestinationRange(destRange);
      return tcp;
    }
    assert protocolCtx.UDP() != null;
    AccessListRuleUdp udp = new AccessListRuleUdp(action, source, destination, lineText);
    udp.setDestinationRange(destRange);
    return udp;
  }

  private @Nonnull AccessListAddress toAclAddress(A10Parser.Access_list_addressContext ctx) {
    if (ctx.access_list_address_any() != null) {
      return AccessListAddressAny.INSTANCE;
    }
    assert ctx.access_list_address_host() != null;
    return new AccessListAddressHost(toIp(ctx.access_list_address_host().address));
  }

  private @Nonnull AccessListAddress toAclAddress(A10Parser.Sal_addressContext ctx) {
    if (ctx.access_list_address_any() != null) {
      return AccessListAddressAny.INSTANCE;
    } else if (ctx.access_list_address_host() != null) {
      return new AccessListAddressHost(toIp(ctx.access_list_address_host().address));
    }
    assert ctx.ip_wildcard() != null;
    A10Parser.Ip_wildcard_maskContext mask = ctx.ip_wildcard().ip_wildcard_mask();
    if (mask.ip_slash_prefix() != null) {
      return new AccessListAddressPrefix(
          Prefix.parse(
              ctx.ip_wildcard().ip_address().getText() + mask.ip_slash_prefix().getText()));
    }
    assert mask.ip_address() != null;
    return new AccessListAddressWildcard(
        IpWildcard.ipWithWildcardMask(
            toIp(ctx.ip_wildcard().ip_address()), toIp(mask.ip_address())));
  }

  private AccessListRule.Action toAclAction(A10Parser.Access_list_actionContext ctx) {
    if (ctx.PERMIT() != null) {
      return AccessListRule.Action.PERMIT;
    }
    assert ctx.DENY() != null;
    return AccessListRule.Action.DENY;
  }

  private Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, A10Parser.Access_list_port_rangeContext ctx) {
    Optional<Integer> maybeFrom = toInteger(messageCtx, ctx.from);
    Optional<Integer> maybeTo = toInteger(messageCtx, ctx.to);
    if (!maybeFrom.isPresent() || !maybeTo.isPresent()) {
      // Already warned
      return Optional.empty();
    }
    Integer from = maybeFrom.get();
    Integer to = maybeTo.get();
    if (to < from) {
      warn(messageCtx, "Port range is invalid, to must not be lower than from.");
      return Optional.empty();
    }
    return Optional.of(new SubRange(from, to));
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Acl_port_numberContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint16(), ACL_PORT_NUMBER_RANGE, "ip access-list range");
  }

  private static final IntegerSpace ACL_PORT_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 65535));

  @Override
  public void enterSid_ethernet(A10Parser.Sid_ethernetContext ctx) {
    Optional<Integer> num = toInteger(ctx, ctx.num);
    num.ifPresent(
        n -> {
          _currentInterface =
              _c.getInterfacesEthernet()
                  .computeIfAbsent(n, number -> new Interface(Interface.Type.ETHERNET, n));
          String ifaceName = getInterfaceName(_currentInterface);
          _c.defineStructure(INTERFACE, ifaceName, ctx);
          _c.referenceStructure(
              INTERFACE, ifaceName, A10StructureUsage.INTERFACE_SELF_REF, ctx.start.getLine());
        });
    if (!num.isPresent()) {
      _currentInterface = new Interface(Interface.Type.ETHERNET, -1); // dummy
    }
  }

  @Override
  public void exitSid_ethernet(A10Parser.Sid_ethernetContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void enterSid_loopback(A10Parser.Sid_loopbackContext ctx) {
    Optional<Integer> num = toInteger(ctx, ctx.num);
    num.ifPresent(
        n -> {
          _currentInterface =
              _c.getInterfacesLoopback()
                  .computeIfAbsent(n, number -> new Interface(Interface.Type.LOOPBACK, n));
          String ifaceName = getInterfaceName(_currentInterface);
          _c.defineStructure(INTERFACE, ifaceName, ctx);
          _c.referenceStructure(
              INTERFACE, ifaceName, A10StructureUsage.INTERFACE_SELF_REF, ctx.start.getLine());
        });
    if (!num.isPresent()) {
      _currentInterface = new Interface(Interface.Type.LOOPBACK, -1); // dummy
    }
  }

  @Override
  public void exitSid_trunk(A10Parser.Sid_trunkContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void enterSid_trunk(A10Parser.Sid_trunkContext ctx) {
    Optional<Integer> num = toInteger(ctx, ctx.num);
    num.ifPresent(
        n ->
            _currentInterface =
                _c.getInterfacesTrunk().computeIfAbsent(n, number -> new TrunkInterface(n, null)));
    _c.defineStructure(INTERFACE, getInterfaceName(_currentInterface), ctx);
    if (!num.isPresent()) {
      _currentInterface = new TrunkInterface(-1, null); // dummy
    }
  }

  @Override
  public void exitSid_loopback(A10Parser.Sid_loopbackContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitSid_access_list(A10Parser.Sid_access_listContext ctx) {
    String accessList;
    if (ctx.access_list_name() != null) {
      Optional<String> nameOptional = toString(ctx, ctx.access_list_name());
      if (!nameOptional.isPresent()) {
        return;
      }
      accessList = nameOptional.get();
    } else {
      assert ctx.access_list_number() != null;
      Optional<Integer> numOptional = toInteger(ctx, ctx.access_list_number());
      if (!numOptional.isPresent()) {
        return;
      } else if (numOptional.get() < 100) {
        warn(ctx, "A10 standard access lists are not yet supported");
        return;
      }
      accessList = String.valueOf(numOptional.get());
    }
    _c.referenceStructure(ACCESS_LIST, accessList, INTERFACE_ACCESS_LIST, ctx.start.getLine());
    _currentInterface.setAccessListIn(accessList);
  }

  @Override
  public void exitSid_enable(A10Parser.Sid_enableContext ctx) {
    _currentInterface.setEnabled(true);
  }

  @Override
  public void exitSid_disable(A10Parser.Sid_disableContext ctx) {
    _currentInterface.setEnabled(false);
  }

  @Override
  public void exitSid_mtu(A10Parser.Sid_mtuContext ctx) {
    toInteger(ctx, ctx.interface_mtu()).ifPresent(mtu -> _currentInterface.setMtu(mtu));
  }

  @Override
  public void exitSidi_address(A10Parser.Sidi_addressContext ctx) {
    if (_currentInterface.getTrunkGroup() != null) {
      warn(ctx, "Cannot configure an IP address on a trunk-group member");
      return;
    }
    _currentInterface.setIpAddress(toInterfaceAddress(ctx.ip_prefix()));
  }

  @Override
  public void exitSidll_enable(A10Parser.Sidll_enableContext ctx) {
    boolean enableRx = false;
    boolean enableTx = false;
    for (A10Parser.SidlleContext enable : ctx.sidlle()) {
      if (enable.RX() != null) {
        enableRx = true;
        continue;
      }
      assert enable.TX() != null;
      enableTx = true;
    }
    InterfaceLldp lldp = _currentInterface.getOrCreateLldp();
    lldp.setEnableRx(enableRx);
    lldp.setEnableTx(enableTx);
  }

  @Override
  public void exitSid_name(A10Parser.Sid_nameContext ctx) {
    toString(ctx, ctx.interface_name_str()).ifPresent(n -> _currentInterface.setName(n));
  }

  @Override
  public void exitSid_ports_threshold(A10Parser.Sid_ports_thresholdContext ctx) {
    if (!(_currentInterface instanceof TrunkInterface)) {
      warn(ctx, "Ports-threshold can only be configured on trunk interfaces.");
      return;
    }
    toInteger(ctx, ctx.ports_threshold())
        .ifPresent(n -> ((TrunkInterface) _currentInterface).setPortsThreshold(n));
  }

  @Override
  public void exitSid_speed(A10Parser.Sid_speedContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterSid_ve(A10Parser.Sid_veContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx, ctx.num);
    if (!maybeNum.isPresent()) {
      _currentInterface = new Interface(Interface.Type.VE, -1); // dummy
      return;
    }
    int num = maybeNum.get();
    Vlan vlan = _c.getVlans().get(num);
    if (vlan == null || !Objects.equals(num, vlan.getRouterInterface())) {
      warn(ctx, "Cannot create a ve interface for a non-existent or unassociated VLAN.");
      _currentInterface = new Interface(Interface.Type.VE, -1); // dummy
      return;
    }
    _currentInterface =
        _c.getInterfacesVe().computeIfAbsent(num, n -> new Interface(Interface.Type.VE, n));
    _c.defineStructure(INTERFACE, getInterfaceName(_currentInterface), ctx);
  }

  @Override
  public void exitSid_ve(A10Parser.Sid_veContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void enterSidl_trunk(A10Parser.Sidl_trunkContext ctx) {
    TrunkGroup.Type type = TrunkGroup.Type.LACP;
    toInteger(ctx, ctx.num)
        .ifPresent(
            n -> {
              Optional<String> maybeInvalidReason = isTrunkValidForCurrentIface(n, type);
              if (maybeInvalidReason.isPresent()) {
                warn(ctx, maybeInvalidReason.get());
              } else {
                setCurrentTrunkGroupAndReferences(n, type, ctx);
              }
            });

    if (_currentTrunkGroup == null) {
      _currentTrunkGroup = new TrunkGroup(-1, type); // dummy
    }
  }

  @Override
  public void exitSidl_trunk(A10Parser.Sidl_trunkContext ctx) {
    _currentTrunkGroup = null;
  }

  @Override
  public void exitSidl_timeout(A10Parser.Sidl_timeoutContext ctx) {
    TrunkGroup tg = _currentInterface.getTrunkGroup();
    if (tg == null) {
      warn(ctx, "Cannot configure timeout for non-existent trunk-group");
      return;
    }
    tg.setTimeout(toTimeout(ctx.trunk_timeout()));
  }

  @Override
  public void exitSidlt_mode(A10Parser.Sidlt_modeContext ctx) {
    _currentTrunkGroup.setMode(toMode(ctx.trunk_mode()));
  }

  @Override
  public void exitSi_route(A10Parser.Si_routeContext ctx) {
    Optional<Prefix> maybePrefix = toRoutePrefix(ctx, ctx.ip_prefix());
    if (!maybePrefix.isPresent()) {
      // Already warned
      return;
    }

    Optional<StaticRoute> maybeStaticRoute = toStaticRoute(ctx.static_route_definition());
    maybeStaticRoute.ifPresent(
        sr ->
            _c.getStaticRoutes()
                .computeIfAbsent(maybePrefix.get(), p -> new StaticRouteManager())
                .add(sr));
  }

  @Override
  public void exitSni_route(A10Parser.Sni_routeContext ctx) {
    Optional<Prefix> maybePrefix = toRoutePrefix(ctx, ctx.ip_prefix());
    if (!maybePrefix.isPresent()) {
      // Already warned
      return;
    }

    Optional<StaticRoute> maybeStaticRoute = toStaticRoute(ctx.static_route_definition());
    maybeStaticRoute.ifPresent(
        sr -> {
          StaticRouteManager srm = _c.getStaticRoutes().get(maybePrefix.get());
          if (srm == null) {
            warn(ctx, String.format("No routes exist for prefix %s", maybePrefix.get()));
            return;
          }
          Optional<String> maybeInvalidReason = srm.delete(sr);
          maybeInvalidReason.ifPresent(reason -> warn(ctx, reason));
        });
  }

  private Optional<StaticRoute> toStaticRoute(A10Parser.Static_route_definitionContext ctx) {
    A10Parser.Static_route_distanceContext distCtx = ctx.static_route_distance();
    A10Parser.Static_route_descriptionContext descrCtx = ctx.static_route_description();
    Optional<Integer> maybeDistance = Optional.empty();
    Optional<String> maybeDescription = Optional.empty();
    if (distCtx != null) {
      maybeDistance = toInteger(ctx, distCtx);
      if (!maybeDistance.isPresent()) {
        // Already warned
        return Optional.empty();
      }
    }
    if (descrCtx != null) {
      maybeDescription = toString(ctx, descrCtx);
      if (!maybeDescription.isPresent()) {
        // Already warned
        return Optional.empty();
      }
    }
    Ip forwardingRouterAddr = toIp(ctx.ip_address());
    return Optional.of(
        new StaticRoute(
            forwardingRouterAddr, maybeDescription.orElse(null), maybeDistance.orElse(null)));
  }

  @Override
  public void enterS_vlan(A10Parser.S_vlanContext ctx) {
    Optional<Integer> maybeVlanNum = toInteger(ctx, ctx.vlan_number());
    if (maybeVlanNum.isPresent()) {
      _currentVlan = _c.getVlans().computeIfAbsent(maybeVlanNum.get(), Vlan::new);
      return;
    }
    _currentVlan = new Vlan(0); // dummy
  }

  @Override
  public void exitS_vlan(A10Parser.S_vlanContext ctx) {
    _currentVlan = null;
  }

  @Override
  public void exitSvd_name(A10Parser.Svd_nameContext ctx) {
    toString(ctx, ctx.vlan_name()).ifPresent(n -> _currentVlan.setName(n));
  }

  @Override
  public void exitSvd_router_interface(A10Parser.Svd_router_interfaceContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx, ctx.vlan_number());
    if (maybeNum.isPresent()) {
      if (!maybeNum.get().equals(_currentVlan.getNumber())) {
        warn(ctx, "Virtual Ethernet interface number must be the same as VLAN ID.");
        return;
      }
      String routerInterfaceName = getInterfaceName(Interface.Type.VE, maybeNum.get());
      _c.defineStructure(INTERFACE, routerInterfaceName, ctx);
      _c.referenceStructure(
          INTERFACE,
          routerInterfaceName,
          A10StructureUsage.VLAN_ROUTER_INTERFACE,
          ctx.start.getLine());
      _currentVlan.setRouterInterface(maybeNum.get());
    }
  }

  @Override
  public void exitSvd_tagged(A10Parser.Svd_taggedContext ctx) {
    // TODO enforce interface restrictions
    //  e.g. untagged iface cannot be reused, cannot attach trunk members directly, etc.
    toInterfaceReferences(ctx.vlan_iface_references(), A10StructureUsage.VLAN_TAGGED_INTERFACE)
        .ifPresent(
            refs -> {
              refs.forEach(
                  ref -> {
                    if (ref.getType() == Type.ETHERNET) {
                      // Ethernet interfaces may not show up elsewhere if members of a vlan
                      _c.getInterfacesEthernet()
                          .computeIfAbsent(ref.getNumber(), n -> new Interface(Type.ETHERNET, n));
                    }
                    _c.defineStructure(INTERFACE, getInterfaceName(ref), ctx);
                  });
              _currentVlan.addTagged(refs);
            });
  }

  @Override
  public void exitSvd_untagged(A10Parser.Svd_untaggedContext ctx) {
    // TODO enforce interface restrictions
    //  e.g. untagged iface cannot be reused, cannot attach trunk members directly, etc.
    toInterfaceReferences(ctx.vlan_iface_references(), A10StructureUsage.VLAN_UNTAGGED_INTERFACE)
        .ifPresent(
            refs -> {
              refs.forEach(
                  ref -> {
                    if (ref.getType() == Type.ETHERNET) {
                      // Ethernet interfaces may not show up elsewhere if members of a vlan
                      _c.getInterfacesEthernet()
                          .computeIfAbsent(ref.getNumber(), n -> new Interface(Type.ETHERNET, n));
                    }
                    _c.defineStructure(INTERFACE, getInterfaceName(ref), ctx);
                  });
              _currentVlan.addUntagged(refs);
            });
  }

  /**
   * Check if specified trunk number and type are valid for the current interface.
   *
   * <p>If so, return {@link Optional#empty()}, otherwise returns a string indicating why it is not
   * valid.
   *
   * <ul>
   *   If {@link Optional#empty()} is returned, this function indicates that:
   *   <li>If a trunk group already exists on the interface, it is compatible with the supplied
   *       number and type.
   *   <li>If a trunk interface exists with the supplied number, it is compatible with the supplied
   *       number and type.
   *   <li>The {@code _currentInterface} can be added to the trunk group.
   * </ul>
   */
  private Optional<String> isTrunkValidForCurrentIface(int num, @Nullable TrunkGroup.Type type) {
    if (_currentInterface.getIpAddress() != null) {
      return Optional.of("Cannot add an interface with a configured IP address to a trunk-group");
    }

    TrunkGroup currentTrunk = _currentInterface.getTrunkGroup();
    if (currentTrunk != null) {
      // Existing TrunkGroup for this interface
      if (currentTrunk.getNumber() != num) {
        return Optional.of(
            String.format(
                "This interface is already a member of trunk-group %s", currentTrunk.getNumber()));
      } else if (type != null && currentTrunk.getTypeEffective() != type) {
        return Optional.of(
            String.format(
                "Trunk-group already exists as a different type (%s)",
                currentTrunk.getTypeEffective().toString().toLowerCase()));
      }
    } else {
      // New TrunkGroup for this interface
      currentTrunk = new TrunkGroup(num, type);
      TrunkInterface existingTrunk = _c.getInterfacesTrunk().get(num);
      if (existingTrunk != null
          && existingTrunk.getTrunkTypeEffective() != currentTrunk.getTypeEffective()) {
        return Optional.of(
            String.format(
                "Trunk-group already exists as a different type (%s)",
                existingTrunk.getTrunkTypeEffective().toString().toLowerCase()));
      }
      // TODO enforce invariants regarding trunk members
      //  e.g. must not be associated with different VLANs, must have same MTUs at least for LACP,
      // etc.
    }
    return Optional.empty();
  }

  /**
   * Set the current trunk-group and add it as the trunk-group for the current interface, creating
   * it if necessary. Also, perform related datamodel updates, like creating a corresponding trunk
   * interface if needed and updating its members.
   */
  private void setCurrentTrunkGroupAndReferences(
      int num, @Nullable TrunkGroup.Type type, ParserRuleContext ctx) {
    _currentTrunkGroup =
        Optional.ofNullable(_currentInterface.getTrunkGroup())
            .orElseGet(() -> new TrunkGroup(num, type));
    _currentInterface.setTrunkGroup(_currentTrunkGroup);
    TrunkInterface trunkInterface =
        _c.getInterfacesTrunk().computeIfAbsent(num, n -> new TrunkInterface(n, type));
    trunkInterface
        .getMembers()
        .add(new InterfaceReference(_currentInterface.getType(), _currentInterface.getNumber()));
    String trunkName = getInterfaceName(trunkInterface);
    _c.defineStructure(INTERFACE, trunkName, ctx);
    _c.referenceStructure(
        INTERFACE, trunkName, A10StructureUsage.INTERFACE_TRUNK_GROUP, ctx.start.getLine());
  }

  @Override
  public void enterSid_trunk_group(A10Parser.Sid_trunk_groupContext ctx) {
    TrunkGroup.Type type = ctx.trunk_type() != null ? toType(ctx.trunk_type()) : null;
    Optional<Integer> maybeNum = toInteger(ctx, ctx.trunk_number());
    if (!maybeNum.isPresent()) {
      _currentTrunkGroup = new TrunkGroup(-1, type); // dummy
      return;
    }

    int num = maybeNum.get();
    Optional<String> maybeInvalidReason = isTrunkValidForCurrentIface(num, type);
    if (maybeInvalidReason.isPresent()) {
      warn(ctx, maybeInvalidReason.get());
      _currentTrunkGroup = new TrunkGroup(-1, type); // dummy
      return;
    }
    setCurrentTrunkGroupAndReferences(num, type, ctx);
  }

  @Override
  public void exitSid_trunk_group(A10Parser.Sid_trunk_groupContext ctx) {
    _currentTrunkGroup = null;
  }

  @Override
  public void exitSidtg_mode(A10Parser.Sidtg_modeContext ctx) {
    _currentTrunkGroup.setMode(toMode(ctx.trunk_mode()));
  }

  @Override
  public void exitSidtg_timeout(A10Parser.Sidtg_timeoutContext ctx) {
    _currentTrunkGroup.setTimeout(toTimeout(ctx.trunk_timeout()));
  }

  @Override
  public void exitSidtg_user_tag(A10Parser.Sidtg_user_tagContext ctx) {
    toString(ctx, ctx.user_tag()).ifPresent(ut -> _currentTrunkGroup.setUserTag(ut));
  }

  /**
   * Creates and returns a NAT pool based on the specified settings. Adds a warning and returns
   * {@link Optional#empty()} if the NAT pool cannot be created.
   */
  private @Nonnull Optional<NatPool> createNatPool(
      ParserRuleContext ctx, Optional<String> maybeName, Ip start, Ip end, int netmask) {
    if (!maybeName.isPresent()) {
      // Already warned
      return Optional.empty();
    }
    String name = maybeName.get();
    if (_c.getNatPools().containsKey(name)) {
      warn(ctx, "Cannot modify an existing NAT pool");
      return Optional.empty();
    }
    if (start.compareTo(end) > 0) {
      warn(ctx, "Invalid NAT pool range, the end address cannot be lower than the start address");
      return Optional.empty();
    }
    if (netmask == 0) {
      warn(ctx, "Invalid NAT pool netmask, must be > 0");
      return Optional.empty();
    }
    if (!start.getNetworkAddress(netmask).equals(end.getNetworkAddress(netmask))) {
      warn(ctx, "Invalid NAT pool range, all addresses must fit in specified netmask");
      return Optional.empty();
    }
    if (!_allNatPools
        .intersection(LongSpace.of(Range.closed(start.asLong(), end.asLong())))
        .isEmpty()) {
      warn(ctx, "Invalid NAT pool range, overlaps with existing NAT pool");
      return Optional.empty();
    }

    return Optional.of(new NatPool(name, start, end, netmask));
  }

  @Override
  public void enterSin_pool(A10Parser.Sin_poolContext ctx) {
    Optional<String> name = toString(ctx, ctx.nat_pool_name());
    Ip start = toIp(ctx.start);
    Ip end = toIp(ctx.end);
    int netmask = toInteger(ctx.ip_netmask());
    _currentNatPoolValid = true;
    _currentNatPool =
        createNatPool(ctx, name, start, end, netmask)
            .orElseGet(
                () -> {
                  _currentNatPoolValid = false;
                  return new NatPool(ctx.nat_pool_name().getText(), start, end, netmask); // dummy
                });
  }

  @Override
  public void exitSin_pool(A10Parser.Sin_poolContext ctx) {
    // Only add the NAT pool if all its properties were valid
    if (_currentNatPoolValid) {
      String name = _currentNatPool.getName();
      _c.getNatPools().put(name, _currentNatPool);
      _allNatPools =
          _allNatPools.union(
              LongSpace.of(
                  Range.closed(
                      _currentNatPool.getStart().asLong(), _currentNatPool.getEnd().asLong())));
      _c.defineStructure(NAT_POOL, name, ctx);
    }
    _currentNatPool = null;
  }

  @Override
  public void exitSinpp_gateway(A10Parser.Sinpp_gatewayContext ctx) {
    _currentNatPool.setGateway(toIp(ctx.gateway));
  }

  @Override
  public void exitSinpp_ha_group_id(A10Parser.Sinpp_ha_group_idContext ctx) {
    toInteger(ctx, ctx.ha_group_id()).ifPresent(_currentNatPool::setHaGroupId);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Ha_group_idContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), HA_GROUP_ID_RANGE, "ha-group-id");
  }

  @Override
  public void exitSinpp_ip_rr(A10Parser.Sinpp_ip_rrContext ctx) {
    _currentNatPool.setIpRr(true);
  }

  @Override
  public void exitSinpp_port_overload(A10Parser.Sinpp_port_overloadContext ctx) {
    _currentNatPool.setPortOverload(true);
  }

  @Override
  public void exitSinpp_scaleout_device_id(A10Parser.Sinpp_scaleout_device_idContext ctx) {
    Optional<Integer> scaleoutDeviceId = toInteger(ctx, ctx.scaleout_device_id());
    if (scaleoutDeviceId.isPresent()) {
      _currentNatPool.setScaleoutDeviceId(scaleoutDeviceId.get());
    } else {
      _currentNatPoolValid = false;
    }
  }

  @Override
  public void exitSinpp_vrid(A10Parser.Sinpp_vridContext ctx) {
    Optional<Integer> maybeVrid = toInteger(ctx, ctx.non_default_vrid());
    if (!maybeVrid.isPresent()) {
      _currentNatPoolValid = false;
      return;
    }
    int vrid = maybeVrid.get();
    assert vrid != DEFAULT_VRID;
    if (!Optional.ofNullable(_c.getVrrpA())
        .map(vrrpA -> vrrpA.getVrids().containsKey(vrid))
        .orElse(false)) {
      _currentNatPoolValid = false;
      warn(ctx, String.format("Cannot assign nat pool to undefined non-default vrid: %d", vrid));
      return;
    }
    _c.referenceStructure(
        VRRP_A_VRID, Integer.toString(vrid), IP_NAT_POOL_VRID, ctx.getStart().getLine());
    _currentNatPool.setVrid(vrid);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Non_default_vridContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint8(), NON_DEFAULT_VRID_RANGE, "non-default vrid number");
  }

  @Override
  public void enterS_lacp_trunk(A10Parser.S_lacp_trunkContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx, ctx.trunk_number());
    _currentTrunk =
        maybeNum
            .map(
                n -> {
                  TrunkInterface trunkInterface =
                      _c.getInterfacesTrunk()
                          .computeIfAbsent(n, num -> new TrunkInterface(num, TrunkGroup.Type.LACP));
                  String trunkName = getInterfaceName(trunkInterface);
                  _c.defineStructure(INTERFACE, trunkName, ctx);
                  _c.referenceStructure(
                      INTERFACE,
                      trunkName,
                      A10StructureUsage.INTERFACE_SELF_REF,
                      ctx.start.getLine());
                  return trunkInterface;
                })
            .orElseGet(() -> new TrunkInterface(-1, TrunkGroup.Type.LACP)); // dummy
  }

  @Override
  public void exitS_lacp_trunk(A10Parser.S_lacp_trunkContext ctx) {
    _currentTrunk = null;
  }

  @Override
  public void exitSltd_ports_threshold(A10Parser.Sltd_ports_thresholdContext ctx) {
    toInteger(ctx, ctx.ports_threshold()).ifPresent(n -> _currentTrunk.setPortsThreshold(n));
  }

  @Override
  public void enterSr_bgp(A10Parser.Sr_bgpContext ctx) {
    Optional<Long> maybeAsn = toLong(ctx, ctx.bgp_asn());
    if (!maybeAsn.isPresent()) {
      _currentBgpProcess = new BgpProcess(-1); // dummy
      return;
    }

    _currentBgpProcess = _c.getOrCreateBgpProcess(maybeAsn.get());
    if (_currentBgpProcess.getAsn() != maybeAsn.get()) {
      warn(
          ctx, String.format("BGP is already configured with asn %d", _currentBgpProcess.getAsn()));
      _currentBgpProcess = new BgpProcess(-1); // dummy
    }
  }

  @Override
  public void exitSr_bgp(A10Parser.Sr_bgpContext ctx) {
    _currentBgpProcess = null;
  }

  @Override
  public void exitSrb_maximum_paths(A10Parser.Srb_maximum_pathsContext ctx) {
    toInteger(ctx, ctx.bgp_max_paths()).ifPresent(_currentBgpProcess::setMaximumPaths);
  }

  @Override
  public void exitSrbb_default_local_preference(
      A10Parser.Srbb_default_local_preferenceContext ctx) {
    toLong(ctx, ctx.bgp_local_preference())
        .ifPresent(_currentBgpProcess::setDefaultLocalPreference);
  }

  @Override
  public void exitSrbb_router_id(A10Parser.Srbb_router_idContext ctx) {
    _currentBgpProcess.setRouterId(toIp(ctx.ip_address()));
  }

  private @Nonnull BgpNeighborId toBgpNeighborId(A10Parser.Bgp_neighborContext ctx) {
    assert ctx.ip_address() != null;
    return new BgpNeighborIdAddress(toIp(ctx.ip_address()));
  }

  @Override
  public void enterSrb_neighbor(A10Parser.Srb_neighborContext ctx) {
    BgpNeighborId id = toBgpNeighborId(ctx.bgp_neighbor());

    BgpNeighbor existingNeighbor = _currentBgpProcess.getNeighbor(id);

    // The first neighbor statement must be remote-as or peer-group
    // TODO handle peer-group check
    if (ctx.srbn().srbn_remote_as() == null && existingNeighbor == null) {
      warn(ctx, "Must specify neighbor remote-as or peer-group first");
      _currentBgpNeighbor = new BgpNeighbor(id); // dummy
      return;
    }

    _currentBgpNeighbor = _currentBgpProcess.getOrCreateNeighbor(id);
  }

  @Override
  public void exitSrb_neighbor(A10Parser.Srb_neighborContext ctx) {
    _currentBgpNeighbor = null;
  }

  @Override
  public void exitSrbn_activate(A10Parser.Srbn_activateContext ctx) {
    _currentBgpNeighbor.setActivate(true);
  }

  @Override
  public void exitSrbn_description(A10Parser.Srbn_descriptionContext ctx) {
    toString(ctx, ctx.bgp_neighbor_description()).ifPresent(_currentBgpNeighbor::setDescription);
  }

  @Override
  public void exitSrbn_maximum_prefix(A10Parser.Srbn_maximum_prefixContext ctx) {
    Optional<Integer> maybeMaxPrefix = toInteger(ctx, ctx.bgp_neighbor_max_prefix());
    if (!maybeMaxPrefix.isPresent()) {
      // Already warned
      return;
    }

    Optional<Integer> maybeThreshold = Optional.empty();
    if (ctx.bgp_neighbor_max_prefix_threshold() != null) {
      maybeThreshold = toInteger(ctx, ctx.bgp_neighbor_max_prefix_threshold());
      if (!maybeThreshold.isPresent()) {
        // Already warned
        return;
      }
    }

    _currentBgpNeighbor.setMaximumPrefix(maybeMaxPrefix.get());
    maybeThreshold.ifPresent(_currentBgpNeighbor::setMaximumPrefixThreshold);
  }

  @Override
  public void exitSrbn_remote_as(A10Parser.Srbn_remote_asContext ctx) {
    toLong(ctx, ctx.bgp_asn()).ifPresent(_currentBgpNeighbor::setRemoteAs);
  }

  @Override
  public void exitSrbn_send_community(A10Parser.Srbn_send_communityContext ctx) {
    _currentBgpNeighbor.setSendCommunity(toSendCommunity(ctx.send_community()));
  }

  private @Nonnull BgpNeighbor.SendCommunity toSendCommunity(A10Parser.Send_communityContext ctx) {
    if (ctx.BOTH() != null) {
      return BgpNeighbor.SendCommunity.BOTH;
    } else if (ctx.EXTENDED() != null) {
      return BgpNeighbor.SendCommunity.EXTENDED;
    } else if (ctx.NONE() != null) {
      return BgpNeighbor.SendCommunity.NONE;
    }
    assert ctx.STANDARD() != null;
    return BgpNeighbor.SendCommunity.STANDARD;
  }

  @Override
  public void exitSrbn_weight(A10Parser.Srbn_weightContext ctx) {
    toInteger(ctx, ctx.bgp_neighbor_weight()).ifPresent(_currentBgpNeighbor::setWeight);
  }

  @Override
  public void exitSrbn_update_source(A10Parser.Srbn_update_sourceContext ctx) {
    toUpdateSource(ctx.bgp_neighbor_update_source())
        .ifPresent(_currentBgpNeighbor::setUpdateSource);
  }

  @Nonnull
  Optional<BgpNeighborUpdateSource> toUpdateSource(
      A10Parser.Bgp_neighbor_update_sourceContext ctx) {
    assert ctx.ip_address() != null;
    return Optional.of(new BgpNeighborUpdateSourceAddress(toIp(ctx.ip_address())));
  }

  @Override
  public void exitSrbr_connected(A10Parser.Srbr_connectedContext ctx) {
    _currentBgpProcess.setRedistributeConnected(true);
  }

  @Override
  public void exitSrbr_floating_ip(A10Parser.Srbr_floating_ipContext ctx) {
    _currentBgpProcess.setRedistributeFloatingIp(true);
  }

  @Override
  public void exitSrbr_ip_nat(A10Parser.Srbr_ip_natContext ctx) {
    _currentBgpProcess.setRedistributeIpNat(true);
  }

  @Override
  public void exitSrbrv_only_flagged(A10Parser.Srbrv_only_flaggedContext ctx) {
    _currentBgpProcess.setRedistributeVipOnlyFlagged(true);
  }

  @Override
  public void exitSrbrv_only_not_flagged(A10Parser.Srbrv_only_not_flaggedContext ctx) {
    _currentBgpProcess.setRedistributeVipOnlyNotFlagged(true);
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext messageCtx, A10Parser.Bgp_asnContext ctx) {
    return toLongInSpace(messageCtx, ctx.uint32(), BGP_ASN_RANGE, "bgp asn");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Bgp_max_pathsContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), BGP_MAX_PATHS_RANGE, "maximum-paths");
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext messageCtx, A10Parser.Bgp_local_preferenceContext ctx) {
    return toLongInSpace(
        messageCtx, ctx.uint32(), BGP_DEFAULT_LOCAL_PREFERENCE_RANGE, "default local-preference");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Bgp_neighbor_weightContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), BGP_NEIGHBOR_WEIGHT_RANGE, "neighbor weight");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Bgp_neighbor_max_prefixContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint32(), BGP_MAX_PREFIX_RANGE, "neighbor maximum-prefix");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Bgp_neighbor_max_prefix_thresholdContext ctx) {
    return toIntegerInSpace(
        messageCtx,
        ctx.uint8(),
        BGP_MAX_PREFIX_THRESHOLD_RANGE,
        "neighbor maximum-prefix threshold");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Bgp_neighbor_descriptionContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), BGP_NEIGHBOR_DESCRIPTION_LENGTH_RANGE, "neighbor description");
  }

  private static final LongSpace BGP_ASN_RANGE = LongSpace.of(Range.closed(1L, 4294967295L));
  private static final IntegerSpace BGP_MAX_PATHS_RANGE = IntegerSpace.of(Range.closed(1, 64));
  private static final LongSpace BGP_DEFAULT_LOCAL_PREFERENCE_RANGE =
      LongSpace.of(Range.closed(0L, 4294967295L));
  private static final IntegerSpace BGP_NEIGHBOR_WEIGHT_RANGE =
      IntegerSpace.of(Range.closed(0, 65535));
  private static final IntegerSpace BGP_MAX_PREFIX_RANGE = IntegerSpace.of(Range.closed(1, 65536));
  private static final IntegerSpace BGP_MAX_PREFIX_THRESHOLD_RANGE =
      IntegerSpace.of(Range.closed(1, 100));
  private static final IntegerSpace BGP_NEIGHBOR_DESCRIPTION_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 80));

  @Override
  public void enterSs_service_group(A10Parser.Ss_service_groupContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.service_group_name());
    ServerPort.Type type = ctx.tcp_or_udp() == null ? null : toType(ctx.tcp_or_udp());
    if (!maybeName.isPresent()) {
      _currentServiceGroup =
          new ServiceGroup(ctx.service_group_name().getText(), ServerPort.Type.TCP); // dummy
      return;
    }

    // Can only omit type/protocol for an already-existing group
    _currentServiceGroup = _c.getServiceGroups().get(maybeName.get());
    if (_currentServiceGroup == null) {
      if (type == null) {
        warn(ctx, "New service-group must have a protocol specified.");
        _currentServiceGroup = new ServiceGroup(maybeName.get(), ServerPort.Type.TCP); // dummy
        return;
      }
      _currentServiceGroup = _c.getOrCreateServiceGroup(maybeName.get(), type);
    }

    if (type != null && type != _currentServiceGroup.getType()) {
      warn(
          ctx,
          "Cannot modify the service-group type field at runtime, ignoring this service-group"
              + " block.");
      _currentServiceGroup =
          new ServiceGroup(ctx.service_group_name().getText(), ServerPort.Type.TCP); // dummy
      return;
    }

    _c.defineStructure(SERVICE_GROUP, maybeName.get(), ctx);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Service_group_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), SERVICE_GROUP_NAME_LENGTH_RANGE, "service-group name");
  }

  @Override
  public void exitSs_service_group(A10Parser.Ss_service_groupContext ctx) {
    _currentServiceGroup = null;
  }

  @Override
  public void exitSssgd_health_check(A10Parser.Sssgd_health_checkContext ctx) {
    toString(ctx, ctx.health_check_name())
        .ifPresent(
            name -> {
              if (!_c.getHealthMonitors().containsKey(name)) {
                warn(
                    ctx,
                    String.format(
                        "Cannot reference non-existent health monitor %s for service-group %s",
                        name, _currentServiceGroup.getName()));
                return;
              }
              _c.referenceStructure(
                  HEALTH_MONITOR, name, SERVICE_GROUP_HEALTH_CHECK, ctx.start.getLine());
              _currentServiceGroup.setHealthCheck(name);
            });
  }

  @Override
  public void exitSssgd_health_check_disable(A10Parser.Sssgd_health_check_disableContext ctx) {
    _currentServiceGroup.setHealthCheckDisable(true);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Health_check_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), HEALTH_CHECK_NAME_LENGTH_RANGE, "health-check name");
  }

  @Override
  public void exitSssgd_method(A10Parser.Sssgd_methodContext ctx) {
    _currentServiceGroup.setMethod(toMethod(ctx.service_group_method()));
  }

  public ServiceGroup.Method toMethod(A10Parser.Service_group_methodContext ctx) {
    if (ctx.LEAST_CONNECTION() != null) {
      return ServiceGroup.Method.LEAST_CONNECTION;
    } else if (ctx.LEAST_REQUEST() != null) {
      return ServiceGroup.Method.LEAST_REQUEST;
    } else if (ctx.SERVICE_LEAST_CONNECTION() != null) {
      return ServiceGroup.Method.SERVICE_LEAST_CONNECTION;
    } else if (ctx.ROUND_ROBIN() != null) {
      return ServiceGroup.Method.ROUND_ROBIN;
    }
    assert ctx.ROUND_ROBIN_STRICT() != null;
    return ServiceGroup.Method.ROUND_ROBIN_STRICT;
  }

  @Override
  public void exitSssgd_min_active_member(A10Parser.Sssgd_min_active_memberContext ctx) {
    toInteger(ctx, ctx.minimum_active_member()).ifPresent(_currentServiceGroup::setMinActiveMember);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Minimum_active_memberContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint16(), MINIMUM_ACTIVE_MEMBER_RANGE, "min-active-member");
  }

  private static final IntegerSpace MINIMUM_ACTIVE_MEMBER_RANGE =
      IntegerSpace.of(Range.closed(1, 1024));

  @Override
  public void exitSssgd_stats_data_disable(A10Parser.Sssgd_stats_data_disableContext ctx) {
    _currentServiceGroup.setStatsDataEnable(false);
  }

  @Override
  public void exitSssgd_stats_data_enable(A10Parser.Sssgd_stats_data_enableContext ctx) {
    _currentServiceGroup.setStatsDataEnable(true);
  }

  @Override
  public void exitSssgdt_port(Sssgdt_portContext ctx) {
    toString(ctx, ctx.name).ifPresent(_currentServiceGroup::setTemplatePort);
  }

  @Override
  public void enterSssgd_member(A10Parser.Sssgd_memberContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.slb_server_name());
    if (!maybeName.isPresent()) {
      _currentServiceGroupMember =
          new ServiceGroupMember(ctx.slb_server_name().getText(), -1); // dummy
      return;
    }
    String name = maybeName.get();

    // Extract port number
    Optional<Integer> maybePort;
    // ACOS v2 - port is embedded in the reference "word"
    if (ctx.sssgd_member_tail().sssgd_member_acos2_tail() != null) {
      if (!name.contains(":")) {
        warn(ctx, "Member reference must include port when not specified separately");
        return;
      }
      maybePort = toPortNumber(ctx, name.substring(name.lastIndexOf(":") + 1));
      name = name.substring(0, name.lastIndexOf(":"));
    } else {
      // ACOS v4+ - port is specified separately
      assert ctx.sssgd_member_tail().sssgd_member_port_number() != null;
      maybePort = toInteger(ctx, ctx.sssgd_member_tail().sssgd_member_port_number().port_number());
    }
    if (!maybePort.isPresent()) {
      _currentServiceGroupMember = new ServiceGroupMember(name, -1); // dummy
      return;
    }

    int port = maybePort.get();
    Server server = _c.getServers().get(name);

    if (server == null) {
      warn(ctx, String.format("Specified server '%s' does not exist.", name));
      _currentServiceGroupMember = new ServiceGroupMember(name, port); // dummy
      return;
    }

    _currentServiceGroupMember = _currentServiceGroup.getOrCreateMember(name, port);

    // Create port for specified server if it doesn't already exist
    if (!server
        .getPorts()
        .containsKey(new ServerPort.ServerPortAndType(port, _currentServiceGroup.getType()))) {
      _c.defineStructure(SERVER, name, ctx);
      server.createPort(port, _currentServiceGroup.getType());
    }
    _c.referenceStructure(SERVER, name, SERVICE_GROUP_MEMBER, ctx.start.getLine());
  }

  @Override
  public void exitSssgd_member_disable(A10Parser.Sssgd_member_disableContext ctx) {
    _currentServiceGroupMember.setEnable(false);
  }

  @Override
  public void exitSssgd_member_priority(A10Parser.Sssgd_member_priorityContext ctx) {
    toInteger(ctx, ctx.service_group_member_priority())
        .ifPresent(_currentServiceGroupMember::setPriority);
  }

  @Override
  public void exitSssgd_member(A10Parser.Sssgd_memberContext ctx) {
    _currentServiceGroupMember = null;
  }

  @Override
  public void exitSssgdmd_disable(A10Parser.Sssgdmd_disableContext ctx) {
    _currentServiceGroupMember.setEnable(false);
  }

  @Override
  public void exitSssgdmd_enable(A10Parser.Sssgdmd_enableContext ctx) {
    _currentServiceGroupMember.setEnable(true);
  }

  @Override
  public void exitSssgdmd_priority(A10Parser.Sssgdmd_priorityContext ctx) {
    toInteger(ctx, ctx.service_group_member_priority())
        .ifPresent(_currentServiceGroupMember::setPriority);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Service_group_member_priorityContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint8(), SERVICE_GROUP_MEMBER_PRIORITY_RANGE, "member priority");
  }

  @Override
  public void enterS_trunk(A10Parser.S_trunkContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx, ctx.trunk_number());
    _currentTrunk =
        maybeNum
            .map(
                n -> {
                  TrunkInterface trunkInterface =
                      _c.getInterfacesTrunk()
                          .computeIfAbsent(
                              n, num -> new TrunkInterface(num, TrunkGroup.Type.STATIC));
                  String trunkName = getInterfaceName(trunkInterface);
                  _c.defineStructure(INTERFACE, trunkName, ctx);
                  _c.referenceStructure(
                      INTERFACE,
                      trunkName,
                      A10StructureUsage.INTERFACE_SELF_REF,
                      ctx.start.getLine());
                  return trunkInterface;
                })
            .orElseGet(() -> new TrunkInterface(-1, TrunkGroup.Type.STATIC)); // dummy
  }

  @Override
  public void exitS_trunk(A10Parser.S_trunkContext ctx) {
    _currentTrunk = null;
  }

  @Override
  public void exitVrrpac_device_id(Vrrpac_device_idContext ctx) {
    toInteger(ctx, ctx.vrrpa_device_id_number())
        .ifPresent(_c.getOrCreateVrrpA().getOrCreateCommon()::setDeviceId);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vrrpa_device_id_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), VRRP_A_DEVICE_ID_RANGE, "vrrp-a device-id");
  }

  @Override
  public void exitVrrpac_disable_default_vrid(Vrrpac_disable_default_vridContext ctx) {
    _c.getOrCreateVrrpA().getOrCreateCommon().setDisableDefaultVrid(true);
  }

  @Override
  public void exitVrrpac_set_id(Vrrpac_set_idContext ctx) {
    toInteger(ctx, ctx.vrrpa_set_id_number())
        .ifPresent(_c.getOrCreateVrrpA().getOrCreateCommon()::setSetId);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vrrpa_set_id_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), VRRP_A_SET_ID_RANGE, "vrrp-a set-id");
  }

  @Override
  public void exitVrrpac_enable(Vrrpac_enableContext ctx) {
    _c.getOrCreateVrrpA().getOrCreateCommon().setEnable(true);
  }

  @Override
  public void enterVrrpa_fail_over_policy_template(Vrrpa_fail_over_policy_templateContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.name);
    if (!maybeName.isPresent()) {
      // dummy
      _currentVrrpAFailOverPolicyTemplate = new VrrpAFailOverPolicyTemplate();
      return;
    }
    String name = maybeName.get();
    _currentVrrpAFailOverPolicyTemplate =
        _c.getOrCreateVrrpA().getOrCreateFailOverPolicyTemplate(name);
    _c.defineStructure(VRRP_A_FAIL_OVER_POLICY_TEMPLATE, name, ctx);
  }

  @Override
  public void exitVrrpa_fail_over_policy_template(Vrrpa_fail_over_policy_templateContext ctx) {
    _currentVrrpAFailOverPolicyTemplate = null;
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Fail_over_policy_template_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx,
        ctx.word(),
        FAIL_OVER_POLICY_TEMPLATE_NAME_LENGTH_RANGE,
        "fail-over-policy-template name");
  }

  @Override
  public void exitVrrpaf_gateway(Vrrpaf_gatewayContext ctx) {
    toInteger(ctx, ctx.weight)
        .ifPresent(
            weight ->
                _currentVrrpAFailOverPolicyTemplate.addOrReplaceGateway(toIp(ctx.gwip), weight));
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vrrpaf_gateway_weightContext ctx) {
    return toIntegerInSpace(
        messageCtx,
        ctx.uint8(),
        FAIL_OVER_POLICY_TEMPLATE_GATEWAY_WEIGHT_RANGE,
        "fail-over-policy-template gateway weight");
  }

  @Override
  public void exitVrrpa_interface(Vrrpa_interfaceContext ctx) {
    Optional<InterfaceReference> maybeRef = toInterfaceReference(ctx, ctx.ref);
    if (!maybeRef.isPresent()) {
      return;
    }
    InterfaceReference ref = maybeRef.get();
    _c.getOrCreateVrrpA().setInterface(ref);
    _c.referenceStructure(
        INTERFACE, getInterfaceName(ref), VRRP_A_INTERFACE, ctx.getStart().getLine());
  }

  private @Nonnull Optional<InterfaceReference> toInterfaceReference(
      ParserRuleContext messageCtx, Ethernet_or_trunk_referenceContext ctx) {
    if (ctx.ETHERNET() != null) {
      return toInterfaceReference(messageCtx, ctx.ethnum);
    } else {
      assert ctx.TRUNK() != null;
      return toInterfaceReference(messageCtx, ctx.trunknum);
    }
  }

  private @Nonnull Optional<InterfaceReference> toInterfaceReference(
      ParserRuleContext messageCtx, Ethernet_numberContext ctx) {
    return toInteger(messageCtx, ctx).map(num -> new InterfaceReference(Type.ETHERNET, num));
  }

  private @Nonnull Optional<InterfaceReference> toInterfaceReference(
      ParserRuleContext messageCtx, Trunk_numberContext ctx) {
    return toInteger(messageCtx, ctx).map(num -> new InterfaceReference(Type.TRUNK, num));
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ethernet_numberContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint8(), INTERFACE_NUMBER_ETHERNET_RANGE, "ethernet interface number");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Trunk_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), TRUNK_NUMBER_RANGE, "trunk number");
  }

  @Override
  public void exitVrrpapg_peer(Vrrpapg_peerContext ctx) {
    _c.getOrCreateVrrpA().addPeerGroupPeer(toIp(ctx.ip));
  }

  @Override
  public void enterVrrpa_vrid_id(Vrrpa_vrid_idContext ctx) {
    Optional<Integer> maybeId = toInteger(ctx, ctx.vrid());
    if (!maybeId.isPresent()) {
      _currentVrid = new VrrpAVrid();
      return;
    }
    int id = maybeId.get();
    _currentVrid = _c.getOrCreateVrrpA().getOrCreateVrid(id);
    String name = Integer.toString(id);
    _c.defineStructure(VRRP_A_VRID, name, ctx);
    if (id == 0) {
      // VRID 0 always exists and is not referenced explicitly, so add self-reference.
      _c.referenceStructure(
          VRRP_A_VRID, name, VRRP_A_VRID_DEFAULT_SELF_REFERENCE, ctx.getStart().getLine());
    }
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, VridContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), VRID_RANGE, "vrrp-a vrid number");
  }

  @Override
  public void exitVrrpa_vrid_id(Vrrpa_vrid_idContext ctx) {
    _currentVrid = null;
  }

  @Override
  public void exitVrrpavi_floating_ip(Vrrpavi_floating_ipContext ctx) {
    _currentVrid.addFloatingIp(toIp(ctx.ip));
  }

  @Override
  public void exitVrrpavi_preempt_mode_disable(Vrrpavi_preempt_mode_disableContext ctx) {
    _currentVrid.setPreemptModeDisable(true);
  }

  @Override
  public void exitVrrpavi_preempt_mode_threshold(Vrrpavi_preempt_mode_thresholdContext ctx) {
    _currentVrid.setPreemptModeThreshold(toInteger(ctx.threshold));
  }

  private int toInteger(Uint8Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  @Override
  public void exitVrrpavib_priority(Vrrpavib_priorityContext ctx) {
    toIntegerInSpace(
            ctx,
            ctx.vrrpa_priority_number().uint8(),
            VRRP_A_PRIORITY_RANGE,
            "vrrp-a vrid blade-paramters priority")
        .ifPresent(_currentVrid.getOrCreateBladeParameters()::setPriority);
  }

  @Override
  public void exitVrrpavib_fail_over_policy_template(
      Vrrpavib_fail_over_policy_templateContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.name);
    if (!maybeName.isPresent()) {
      return;
    }
    String name = maybeName.get();
    assert _c.getVrrpA() != null;
    if (!_c.getVrrpA().getFailOverPolicyTemplates().containsKey(name)) {
      warn(ctx, String.format("Cannot assign non-existent fail-over-policy-template '%s'", name));
      return;
    }
    _c.referenceStructure(
        VRRP_A_FAIL_OVER_POLICY_TEMPLATE,
        name,
        VRRP_A_VRID_BLADE_PARAMETERS_FAIL_OVER_POLICY_TEMPLATE,
        ctx.getStart().getLine());
    _currentVrid.getOrCreateBladeParameters().setFailOverPolicyTemplate(name);
  }

  @Override
  public void exitVrrpa_vrid_lead(Vrrpa_vrid_leadContext ctx) {
    toStringWithLengthInSpace(ctx, ctx.name.word(), VRID_LEAD_NAME_LENGTH_RANGE, "vrrp-a vrid-lead")
        .ifPresent(_c.getOrCreateVrrpA()::setVridLead);
  }

  @Override
  public void exitHa_conn_mirror(Ha_conn_mirrorContext ctx) {
    _c.getOrCreateHa().setConnMirror(toIp(ctx.ip));
  }

  @Override
  public void exitHa_group(Ha_groupContext ctx) {
    Optional<Integer> maybeId = toInteger(ctx, ctx.id);
    Optional<Integer> maybePriority = toInteger(ctx, ctx.priority);
    if (!maybeId.isPresent() || !maybePriority.isPresent()) {
      // already warned
      return;
    }
    _c.getOrCreateHa().getOrCreateHaGroup(maybeId.get()).setPriority(maybePriority.get());
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ha_priority_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), HA_PRIORITY_RANGE, "ha priority");
  }

  @Override
  public void exitHa_id(Ha_idContext ctx) {
    Optional<Integer> maybeId = toInteger(ctx, ctx.id);
    Optional<Integer> maybeSetId = toInteger(ctx, ctx.set_id);
    if (!maybeId.isPresent() || !maybeSetId.isPresent()) {
      // already warned
      return;
    }
    Ha ha = _c.getOrCreateHa();
    ha.setId(maybeId.get());
    ha.setSetId(maybeSetId.get());
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ha_set_id_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), HA_SET_ID_RANGE, "ha set-id");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ha_id_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), HA_ID_RANGE, "ha id");
  }

  @Override
  public void exitHa_interface(Ha_interfaceContext ctx) {
    Optional<InterfaceReference> maybeRef = toInterfaceReference(ctx, ctx.ref);
    if (!maybeRef.isPresent()) {
      return;
    }
    InterfaceReference ref = maybeRef.get();
    _c.getOrCreateHa();
    _c.referenceStructure(INTERFACE, getInterfaceName(ref), HA_INTERFACE, ctx.getStart().getLine());
  }

  @Override
  public void exitHa_preemption_enable(Ha_preemption_enableContext ctx) {
    _c.getOrCreateHa().setPreemptionEnable(true);
  }

  @Override
  public void exitSnha_preemption_enable(Snha_preemption_enableContext ctx) {
    _c.getOrCreateHa().setPreemptionEnable(false);
  }

  @Override
  public void exitS_floating_ip(S_floating_ipContext ctx) {
    Integer haGroupId = null;
    for (Fip_optionContext optionCtx : ctx.fip_option()) {
      assert optionCtx.fipo_ha_group() != null;
      Optional<Integer> maybeHaGroupId = toInteger(ctx, optionCtx.fipo_ha_group().id);
      if (!maybeHaGroupId.isPresent()) {
        // already warned
        return;
      }
      haGroupId = maybeHaGroupId.get();
    }
    FloatingIp floatingIp = new FloatingIp();
    floatingIp.setHaGroup(haGroupId);
    _c.getV2FloatingIps().put(toIp(ctx.ip), floatingIp);
  }

  @Override
  public void exitSsvs_ha_group(Ssvs_ha_groupContext ctx) {
    toInteger(ctx, ctx.id).ifPresent(_currentVirtualServer::setHaGroup);
  }

  @Override
  public void exitStd_name(A10Parser.Std_nameContext ctx) {
    toString(ctx, ctx.name).ifPresent(n -> _currentTrunk.setName(n));
  }

  @Override
  public void exitStd_ethernet(A10Parser.Std_ethernetContext ctx) {
    int line = ctx.start.getLine();
    Optional<List<InterfaceReference>> maybeIfaces = toInterfaces(ctx);
    maybeIfaces.ifPresent(
        ifaces -> {
          ifaces.forEach(
              iface -> {
                assert iface.getType() == Type.ETHERNET;
                // Interfaces may not show up elsewhere if members of a trunk
                _c.getInterfacesEthernet()
                    .computeIfAbsent(iface.getNumber(), n -> new Interface(Type.ETHERNET, n));
                _c.defineStructure(INTERFACE, getInterfaceName(iface), ctx);
                _c.referenceStructure(
                    INTERFACE, getInterfaceName(iface), A10StructureUsage.TRUNK_INTERFACE, line);
                _currentTrunk.getMembers().add(iface);
              });
        });
  }

  @Override
  public void exitHa_check_gateway(Ha_check_gatewayContext ctx) {
    _c.getOrCreateHa().addCheckGateway(toIp(ctx.ip));
  }

  @Override
  public void exitHa_check_route(Ha_check_routeContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitHa_check_vlan(Ha_check_vlanContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterSs_server(A10Parser.Ss_serverContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.slb_server_name());
    if (!maybeName.isPresent()) {
      _currentServer =
          new Server(ctx.slb_server_name().getText(), new ServerTargetAddress(Ip.ZERO)); // dummy
      return;
    }

    String name = maybeName.get();
    if (ctx.slb_server_target() == null) {
      _currentServer = _c.getServers().get(name);
      // No match
      if (_currentServer == null) {
        warn(ctx, "Server target must be specified for a new server");
        _currentServer =
            new Server(ctx.slb_server_name().getText(), new ServerTargetAddress(Ip.ZERO)); // dummy
        return;
      }
      // Updating existing server
      _c.defineStructure(SERVER, name, ctx);
      return;
    }

    // TODO enforce no target reuse
    Optional<ServerTarget> target = toServerTarget(ctx.slb_server_target());
    _c.defineStructure(SERVER, name, ctx);
    if (target.isPresent()) {
      _currentServer = _c.getServers().computeIfAbsent(name, n -> new Server(n, target.get()));
      // Make sure target is up-to-date
      _currentServer.setTarget(target.get());
    } else {
      // dummy for internal fields
      _currentServer = new Server("~dummy~", new ServerTargetAddress(Ip.ZERO));
    }
  }

  @Override
  public void exitSns_server(A10Parser.Sns_serverContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.slb_server_name());
    if (!maybeName.isPresent()) {
      return;
    }
    String name = maybeName.get();
    Server removedServer = _c.getServers().remove(name);
    if (removedServer != null && ctx.slb_server_target() != null) {
      // Ensure that specified target is correct; if it isn't, server should not be deleted.
      Optional<ServerTarget> maybeTarget = toServerTarget(ctx.slb_server_target());
      if (!maybeTarget.isPresent()) {
        // The server was a dummy, no need to clean up.
        return;
      }
      ServerTarget target = maybeTarget.get();
      if (!target.equals(removedServer.getTarget())) {
        _c.getServers().put(name, removedServer);
        removedServer = null;
      }
    }
    if (removedServer == null) {
      warn(ctx, "No matching server to remove");
    }
  }

  @Nonnull
  Optional<ServerTarget> toServerTarget(A10Parser.Slb_server_targetContext ctx) {
    if (ctx.ip_address() != null) {
      return Optional.of(new ServerTargetAddress(toIp(ctx.ip_address())));
    }
    assert ctx.ipv6_address() != null;
    return Optional.empty();
  }

  @Override
  public void exitSs_server(A10Parser.Ss_serverContext ctx) {
    _currentServer = null;
  }

  @Override
  public void exitSssd_conn_limit(A10Parser.Sssd_conn_limitContext ctx) {
    toInteger(ctx, ctx.connection_limit()).ifPresent(_currentServer::setConnLimit);
  }

  @Override
  public void exitSssd_disable(A10Parser.Sssd_disableContext ctx) {
    _currentServer.setEnable(false);
  }

  @Override
  public void exitSssd_enable(A10Parser.Sssd_enableContext ctx) {
    _currentServer.setEnable(true);
  }

  @Override
  public void exitSssd_health_check(A10Parser.Sssd_health_checkContext ctx) {
    toString(ctx, ctx.health_check_name())
        .ifPresent(
            name -> {
              if (!_c.getHealthMonitors().containsKey(name)) {
                warn(
                    ctx,
                    String.format(
                        "Cannot reference non-existent health monitor %s for server %s",
                        name, _currentServer.getName()));
                return;
              }
              _c.referenceStructure(HEALTH_MONITOR, name, SERVER_HEALTH_CHECK, ctx.start.getLine());
              _currentServer.setHealthCheck(name);
            });
  }

  @Override
  public void exitSssd_health_check_disable(A10Parser.Sssd_health_check_disableContext ctx) {
    _currentServer.setHealthCheckDisable(true);
  }

  @Override
  public void exitSssdno_health_check(Sssdno_health_checkContext ctx) {
    _currentServer.setHealthCheckDisable(true);
  }

  @Override
  public void exitSssd_stats_data_disable(A10Parser.Sssd_stats_data_disableContext ctx) {
    _currentServer.setStatsDataEnable(false);
  }

  @Override
  public void exitSssd_stats_data_enable(A10Parser.Sssd_stats_data_enableContext ctx) {
    _currentServer.setStatsDataEnable(true);
  }

  @Override
  public void exitSssdt_server(A10Parser.Sssdt_serverContext ctx) {
    toString(ctx, ctx.template_name()).ifPresent(_currentServer::setServerTemplate);
  }

  @Override
  public void exitSssd_weight(A10Parser.Sssd_weightContext ctx) {
    toInteger(ctx, ctx.connection_weight()).ifPresent(_currentServer::setWeight);
  }

  @Override
  public void enterSssd_port(A10Parser.Sssd_portContext ctx) {
    ServerPort.Type type = toType(ctx.tcp_or_udp());
    Integer range;
    if (ctx.port_range_value() != null) {
      Optional<Integer> maybeRange = toInteger(ctx, ctx.port_range_value());
      if (!maybeRange.isPresent()) {
        // Already warned
        _currentServerPort = new ServerPort(-1, type, null); // dummy
        return;
      }
      range = maybeRange.get();
    } else {
      range = null;
    }
    _currentServerPort =
        toInteger(ctx, ctx.port_number())
            .map(n -> _currentServer.getOrCreatePort(n, type, range))
            .orElseGet(() -> new ServerPort(-1, type, range)); // dummy
    // Make sure range is up-to-date
    _currentServerPort.setRange(range);
  }

  @Override
  public void exitSssd_port(A10Parser.Sssd_portContext ctx) {
    _currentServerPort = null;
  }

  @Override
  public void exitSssdpd_conn_limit(A10Parser.Sssdpd_conn_limitContext ctx) {
    toInteger(ctx, ctx.connection_limit()).ifPresent(_currentServerPort::setConnLimit);
  }

  @Override
  public void exitSssdpd_disable(A10Parser.Sssdpd_disableContext ctx) {
    _currentServerPort.setEnable(false);
  }

  @Override
  public void exitSssdpd_enable(A10Parser.Sssdpd_enableContext ctx) {
    _currentServerPort.setEnable(true);
  }

  @Override
  public void exitSssdpd_health_check(A10Parser.Sssdpd_health_checkContext ctx) {
    toString(ctx, ctx.health_check_name())
        .ifPresent(
            name -> {
              if (!_c.getHealthMonitors().containsKey(name)) {
                warn(
                    ctx,
                    String.format(
                        "Cannot reference non-existent health monitor %s for server port %d in"
                            + " server %s",
                        name, _currentServerPort.getNumber(), _currentServer.getName()));
                return;
              }
              _c.referenceStructure(
                  HEALTH_MONITOR, name, SERVER_PORT_HEALTH_CHECK, ctx.start.getLine());
              _currentServerPort.setHealthCheck(name);
            });
  }

  @Override
  public void exitSssdpd_health_check_disable(A10Parser.Sssdpd_health_check_disableContext ctx) {
    _currentServerPort.setHealthCheckDisable(true);
  }

  @Override
  public void exitSssdpd_stats_data_disable(A10Parser.Sssdpd_stats_data_disableContext ctx) {
    _currentServerPort.setStatsDataEnable(false);
  }

  @Override
  public void exitSssdpd_stats_data_enable(A10Parser.Sssdpd_stats_data_enableContext ctx) {
    _currentServerPort.setStatsDataEnable(true);
  }

  @Override
  public void exitSssdpdt_port(A10Parser.Sssdpdt_portContext ctx) {
    toString(ctx, ctx.template_name()).ifPresent(_currentServerPort::setPortTemplate);
  }

  @Override
  public void exitSssdpd_weight(A10Parser.Sssdpd_weightContext ctx) {
    toInteger(ctx, ctx.connection_weight()).ifPresent(_currentServerPort::setWeight);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Connection_limitContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint32(), CONNECTION_LIMIT_RANGE, "conn-limit");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Connection_weightContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), CONNECTION_WEIGHT_RANGE, "connection weight");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Port_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), PORT_NUMBER_RANGE, "port");
  }

  private @Nonnull Optional<Integer> toPortNumber(ParserRuleContext messageCtx, String str) {
    return toIntegerInSpace(messageCtx, str, PORT_NUMBER_RANGE, "port");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Port_range_valueContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), PORT_RANGE_VALUE_RANGE, "port range");
  }

  private static ServerPort.Type toType(A10Parser.Tcp_or_udpContext ctx) {
    if (ctx.TCP() != null) {
      return ServerPort.Type.TCP;
    }
    assert ctx.UDP() != null;
    return ServerPort.Type.UDP;
  }

  Optional<List<InterfaceReference>> toInterfaces(A10Parser.Std_ethernetContext ctx) {
    ImmutableList.Builder<InterfaceReference> ifaces = ImmutableList.builder();
    for (A10Parser.Trunk_ethernet_interfaceContext iface : ctx.trunk_ethernet_interface()) {
      Optional<Integer> maybeNum = toInteger(ctx, iface.num);
      if (!maybeNum.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      ifaces.add(new InterfaceReference(Interface.Type.ETHERNET, maybeNum.get()));
    }
    for (A10Parser.Trunk_ethernet_interface_rangeContext iface :
        ctx.trunk_ethernet_interface_range()) {
      Optional<SubRange> maybeNumbers = toSubRange(ctx, iface);
      if (!maybeNumbers.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      maybeNumbers
          .get()
          .asStream()
          .forEach(num -> ifaces.add(new InterfaceReference(Type.ETHERNET, num)));
    }
    return Optional.of(ifaces.build());
  }

  /**
   * Convert interface range context to a {@link SubRange}. Returns {@link Optional#empty()} if the
   * context is invalid, e.g. {@code from} is greater than {@code to}.
   */
  Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, A10Parser.Trunk_ethernet_interface_rangeContext ctx) {
    Optional<Integer> maybeFrom;
    Optional<Integer> maybeTo;
    maybeFrom = toInteger(messageCtx, ctx.num);
    maybeTo = toInteger(messageCtx, ctx.to);

    if (!maybeFrom.isPresent() || !maybeTo.isPresent()) {
      // Already warned
      return Optional.empty();
    }
    int from = maybeFrom.get();
    int to = maybeTo.get();
    if (from > to) {
      warn(
          ctx,
          "Invalid range for trunk interface reference, 'from' must not be greater than 'to'.");
      return Optional.empty();
    }
    return Optional.of(new SubRange(from, to));
  }

  TrunkGroup.Mode toMode(A10Parser.Trunk_modeContext ctx) {
    if (ctx.ACTIVE() != null) {
      return TrunkGroup.Mode.ACTIVE;
    }
    assert ctx.PASSIVE() != null;
    return TrunkGroup.Mode.PASSIVE;
  }

  TrunkGroup.Timeout toTimeout(A10Parser.Trunk_timeoutContext ctx) {
    if (ctx.SHORT() != null) {
      return TrunkGroup.Timeout.SHORT;
    }
    assert ctx.LONG() != null;
    return TrunkGroup.Timeout.LONG;
  }

  TrunkGroup.Type toType(A10Parser.Trunk_typeContext ctx) {
    if (ctx.LACP() != null) {
      return TrunkGroup.Type.LACP;
    } else if (ctx.LACP_UDLD() != null) {
      return TrunkGroup.Type.LACP_UDLD;
    }
    assert ctx.STATIC() != null;
    return TrunkGroup.Type.STATIC;
  }

  @Override
  public void enterSs_virtual_server(A10Parser.Ss_virtual_serverContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.virtual_server_name());
    if (!maybeName.isPresent()) {
      _currentVirtualServer =
          new VirtualServer(
              ctx.virtual_server_name().getText(),
              new VirtualServerTargetAddress(Ip.ZERO)); // dummy
      return;
    }

    String name = maybeName.get();
    if (ctx.virtual_server_target() == null) {
      _currentVirtualServer = _c.getVirtualServer(name);
      // No match
      if (_currentVirtualServer == null) {
        warn(ctx, "Server target must be specified for a new virtual-server");
        _currentVirtualServer =
            new VirtualServer(
                ctx.virtual_server_name().getText(),
                new VirtualServerTargetAddress(Ip.ZERO)); // dummy
        return;
      }
      // Updating existing server
      _c.defineStructure(VIRTUAL_SERVER, name, ctx);
      return;
    }

    // TODO enforce no target reuse
    VirtualServerTarget target = toVirtualServerTarget(ctx.virtual_server_target());
    _c.defineStructure(VIRTUAL_SERVER, name, ctx);
    _c.referenceStructure(VIRTUAL_SERVER, name, VIRTUAL_SERVER_SELF_REF, ctx.start.getLine());
    _currentVirtualServer = _c.getOrCreateVirtualServer(name, target);
    // Make sure target is up-to-date
    _currentVirtualServer.setTarget(target);
  }

  @Override
  public void exitSsvs_disable(A10Parser.Ssvs_disableContext ctx) {
    _currentVirtualServer.setEnable(false);
  }

  @Override
  public void exitSsvs_enable(A10Parser.Ssvs_enableContext ctx) {
    _currentVirtualServer.setEnable(true);
  }

  @Override
  public void exitSsvs_redistribution_flagged(A10Parser.Ssvs_redistribution_flaggedContext ctx) {
    _currentVirtualServer.setRedistributionFlagged(true);
  }

  /**
   * Return the vrid for the NAT pool(s) associated with the current virtual-server. Returns {@link
   * Optional#empty()} if there are no NAT pools assigned for the current virtual-server.
   */
  private Optional<Integer> getNatPoolVrid() {
    return _currentVirtualServer.getPorts().values().stream()
        .map(VirtualServerPort::getSourceNat)
        .filter(Objects::nonNull)
        .map(
            name -> {
              NatPool pool = _c.getNatPools().get(name);
              assert pool != null; // sanity check, undefined refs are not allowed
              return pool;
            })
        .map(pool -> Optional.ofNullable(pool.getVrid()).orElse(DEFAULT_VRID))
        .findAny();
  }

  @Override
  public void exitSsvs_vrid(A10Parser.Ssvs_vridContext ctx) {
    toInteger(ctx, ctx.non_default_vrid())
        .ifPresent(
            vrid -> {
              assert vrid != DEFAULT_VRID;

              Optional<Integer> natPoolVrid = getNatPoolVrid();
              if (natPoolVrid.isPresent() && !natPoolVrid.get().equals(vrid)) {
                warn(
                    ctx,
                    String.format(
                        "Cannot assign virtual-server to vrid %d, it contains a NAT pool in vrid"
                            + " %d",
                        vrid, natPoolVrid.get()));
                return;
              }

              if (_c.getVrrpA() == null || !_c.getVrrpA().getVrids().containsKey(vrid)) {
                warn(
                    ctx,
                    String.format(
                        "Cannot assign virtual-server to undefined non-default vrid: %d", vrid));
                return;
              }
              _c.referenceStructure(
                  VRRP_A_VRID,
                  Integer.toString(vrid),
                  VIRTUAL_SERVER_VRID,
                  ctx.getStart().getLine());
              _currentVirtualServer.setVrid(vrid);
            });
  }

  @Override
  public void enterSsvs_port(A10Parser.Ssvs_portContext ctx) {
    VirtualServerPort.Type type = toType(ctx.virtual_server_port_type());
    Integer range;
    if (ctx.port_range_value() != null) {
      Optional<Integer> maybeRange = toInteger(ctx, ctx.port_range_value());
      if (!maybeRange.isPresent()) {
        // Already warned
        _currentVirtualServerPort = new VirtualServerPort(-1, type, null); // dummy
        return;
      }
      range = maybeRange.get();
    } else {
      range = null;
    }
    _currentVirtualServerPort =
        toInteger(ctx, ctx.port_number())
            .map(n -> _currentVirtualServer.getOrCreatePort(n, type, range))
            .orElseGet(() -> new VirtualServerPort(-1, type, range)); // dummy
    // Make sure range is up-to-date
    _currentVirtualServerPort.setRange(range);
  }

  @Override
  public void exitSsvs_port(A10Parser.Ssvs_portContext ctx) {
    _currentVirtualServerPort = null;
  }

  @Override
  public void exitSsvspd_access_list(A10Parser.Ssvspd_access_listContext ctx) {
    toString(ctx, ctx.access_list_name())
        .ifPresent(
            n -> {
              if (!_c.getAccessLists().containsKey(n)) {
                warn(ctx, String.format("Cannot reference non-existent ip access-list '%s'", n));
                return;
              }
              if (_c.getAccessLists().get(n).getRules().isEmpty()) {
                warn(ctx, String.format("Cannot reference empty ip access-list '%s'", n));
                return;
              }
              _c.referenceStructure(
                  A10StructureType.ACCESS_LIST,
                  n,
                  A10StructureUsage.VIRTUAL_SERVER_ACCESS_LIST,
                  ctx.start.getLine());
              _currentVirtualServerPort.setAccessList(n);
            });
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Access_list_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), ACCESS_LIST_NAME_LENGTH_RANGE, "access-list name");
  }

  private static final IntegerSpace ACCESS_LIST_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 16));

  @Override
  public void exitSsvspd_aflex(A10Parser.Ssvspd_aflexContext ctx) {
    toString(ctx, ctx.aflex_name()).ifPresent(_currentVirtualServerPort::setAflex);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Aflex_nameContext ctx) {
    return toStringWithLengthInSpace(messageCtx, ctx.word(), AFLEX_NAME_LENGTH_RANGE, "aflex name");
  }

  private static final IntegerSpace AFLEX_NAME_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 63));

  @Override
  public void exitSsvspd_bucket_count(A10Parser.Ssvspd_bucket_countContext ctx) {
    toInteger(ctx, ctx.traffic_bucket_count()).ifPresent(_currentVirtualServerPort::setBucketCount);
  }

  @Override
  public void exitSsvspd_disable(A10Parser.Ssvspd_disableContext ctx) {
    _currentVirtualServerPort.setEnable(false);
  }

  @Override
  public void exitSsvspd_enable(A10Parser.Ssvspd_enableContext ctx) {
    _currentVirtualServerPort.setEnable(true);
  }

  @Override
  public void exitSsvspd_def_selection_if_pref_failed(
      A10Parser.Ssvspd_def_selection_if_pref_failedContext ctx) {
    _currentVirtualServerPort.setDefSelectionIfPrefFailed(true);
  }

  @Override
  public void exitSsvspd_name(A10Parser.Ssvspd_nameContext ctx) {
    toString(ctx, ctx.virtual_service_name()).ifPresent(_currentVirtualServerPort::setName);
  }

  @Override
  public void exitSsvspd_service_group(A10Parser.Ssvspd_service_groupContext ctx) {
    toString(ctx, ctx.service_group_name())
        .ifPresent(
            sgName -> {
              ServiceGroup sg = _c.getServiceGroups().get(sgName);
              if (sg == null) {
                warn(
                    ctx,
                    String.format(
                        "Cannot add non-existent service-group to virtual-server %s",
                        _currentVirtualServer.getName()));
                return;
              }
              if (!arePortTypesCompatible(sg.getType(), _currentVirtualServerPort.getType())) {
                warn(
                    ctx,
                    String.format(
                        "Service-group port type %s is not compatible with virtual-server port type"
                            + " %s",
                        sg.getType(), _currentVirtualServerPort.getType()));
                return;
              }
              _currentVirtualServerPort.setServiceGroup(sgName);
              _c.referenceStructure(
                  SERVICE_GROUP, sgName, VIRTUAL_SERVER_SERVICE_GROUP, ctx.start.getLine());
            });
  }

  @Override
  public void exitSsvspd_source_nat(A10Parser.Ssvspd_source_natContext ctx) {
    toString(ctx, ctx.nat_pool_name())
        .ifPresent(
            poolName -> {
              NatPool pool = _c.getNatPools().get(poolName);
              if (pool == null) {
                warn(
                    ctx,
                    String.format(
                        "Cannot add non-existent nat pool to virtual-server %s",
                        _currentVirtualServer.getName()));
                return;
              }
              int poolVrid = firstNonNull(pool.getVrid(), DEFAULT_VRID);
              int serverVrid = firstNonNull(_currentVirtualServer.getVrid(), DEFAULT_VRID);
              if (poolVrid != serverVrid) {
                warn(
                    ctx,
                    String.format(
                        "Cannot assign a NAT pool in vrid %d, the virtual-server is in vrid %d",
                        poolVrid, serverVrid));
                return;
              }
              _currentVirtualServerPort.setSourceNat(poolName);
              _c.referenceStructure(
                  NAT_POOL, poolName, VIRTUAL_SERVER_SOURCE_NAT_POOL, ctx.start.getLine());
            });
  }

  @Override
  public void exitSsvspd_use_rcv_hop_for_resp(A10Parser.Ssvspd_use_rcv_hop_for_respContext ctx) {
    _currentVirtualServerPort.setUseRcvHopForResp(true);
  }

  @Nonnull
  VirtualServerTarget toVirtualServerTarget(A10Parser.Virtual_server_targetContext ctx) {
    if (ctx.ip_address() != null) {
      return new VirtualServerTargetAddress(toIp(ctx.ip_address()));
    } else {
      assert ctx.ipv6_address() != null;
      return new VirtualServerTargetAddress6(toIp6(ctx.ipv6_address()));
    }
  }

  private @Nonnull Ip6 toIp6(Ipv6_addressContext ctx) {
    return Ip6.parse(ctx.getText());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Virtual_server_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), VIRTUAL_SERVER_NAME_LENGTH_RANGE, "virtual-server name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Virtual_service_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), VIRTUAL_SERVICE_NAME_LENGTH_RANGE, "virtual service name");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Traffic_bucket_countContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint16(), TRAFFIC_BUCKET_COUNT_RANGE, "traffic bucket-count");
  }

  private static @Nonnull VirtualServerPort.Type toType(
      A10Parser.Virtual_server_port_typeContext ctx) {
    if (ctx.DIAMETER() != null) {
      return VirtualServerPort.Type.DIAMETER;
    } else if (ctx.HTTP() != null) {
      return VirtualServerPort.Type.HTTP;
    } else if (ctx.HTTPS() != null) {
      return VirtualServerPort.Type.HTTPS;
    } else if (ctx.RADIUS() != null) {
      return VirtualServerPort.Type.RADIUS;
    } else if (ctx.SIP() != null) {
      return VirtualServerPort.Type.SIP;
    } else if (ctx.SMTP() != null) {
      return VirtualServerPort.Type.SMTP;
    } else if (ctx.SSL_PROXY() != null) {
      return VirtualServerPort.Type.SSL_PROXY;
    } else if (ctx.TCP() != null) {
      return VirtualServerPort.Type.TCP;
    } else if (ctx.TCP_PROXY() != null) {
      return VirtualServerPort.Type.TCP_PROXY;
    }
    // TODO support more types
    assert ctx.UDP() != null;
    return VirtualServerPort.Type.UDP;
  }

  @Override
  public void exitSs_virtual_server(A10Parser.Ss_virtual_serverContext ctx) {
    _currentVirtualServer = null;
  }

  private Optional<List<InterfaceReference>> toInterfaceReferences(
      ParserRuleContext messageCtx, A10Parser.Vlan_ifaces_rangeContext ctx) {
    Interface.Type type = toInterfaceType(ctx);
    return toSubRange(messageCtx, ctx)
        .map(
            subRange ->
                subRange
                    .asStream()
                    .mapToObj(i -> new InterfaceReference(type, i))
                    .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Convert specified context into a list of {@link InterfaceReference} and add structure
   * references for each interface. Returns {@link Optional#empty()} and adds warnings if the
   * context cannot be converted to a list of {@link InterfaceReference}s.
   */
  private Optional<List<InterfaceReference>> toInterfaceReferences(
      A10Parser.Vlan_iface_referencesContext ctx, A10StructureUsage usage) {
    int line = ctx.start.getLine();

    ImmutableList<Optional<List<InterfaceReference>>> references =
        Stream.concat(
                ctx.vlan_ifaces_list().stream().map(l -> toInterfaces(ctx, l)),
                ctx.vlan_ifaces_range().stream().map(r -> toInterfaceReferences(ctx, r)))
            .collect(ImmutableList.toImmutableList());
    if (references.stream().anyMatch(maybeRefs -> !maybeRefs.isPresent())) {
      // Already warned
      return Optional.empty();
    }
    return Optional.of(
        references.stream()
            .flatMap(
                maybeRef -> {
                  // Guaranteed above
                  assert maybeRef.isPresent();
                  List<InterfaceReference> refs = maybeRef.get();
                  refs.forEach(
                      iface ->
                          _c.referenceStructure(INTERFACE, getInterfaceName(iface), usage, line));
                  return refs.stream();
                })
            .collect(ImmutableList.toImmutableList()));
  }

  Interface.Type toInterfaceType(A10Parser.Vlan_ifaces_rangeContext ctx) {
    if (ctx.vlan_iface_ethernet_range() != null) {
      return Interface.Type.ETHERNET;
    }
    assert ctx.vlan_iface_trunk_range() != null;
    return Interface.Type.TRUNK;
  }

  Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, A10Parser.Vlan_ifaces_rangeContext ctx) {
    Optional<Integer> maybeFrom;
    Optional<Integer> maybeTo;
    if (ctx.vlan_iface_ethernet_range() != null) {
      maybeFrom = toInteger(messageCtx, ctx.vlan_iface_ethernet_range().num);
      maybeTo = toInteger(messageCtx, ctx.vlan_iface_ethernet_range().to);
    } else {
      assert ctx.vlan_iface_trunk_range() != null;
      maybeFrom = toInteger(messageCtx, ctx.vlan_iface_trunk_range().num);
      maybeTo = toInteger(messageCtx, ctx.vlan_iface_trunk_range().to);
    }

    if (!maybeFrom.isPresent() || !maybeTo.isPresent()) {
      // Already warned
      return Optional.empty();
    }
    int from = maybeFrom.get();
    int to = maybeTo.get();
    if (from > to) {
      warn(
          ctx, "Invalid range for VLAN interface reference, 'from' must not be greater than 'to'.");
      return Optional.empty();
    }
    return Optional.of(new SubRange(from, to));
  }

  Optional<List<InterfaceReference>> toInterfaces(
      ParserRuleContext messageCtx, A10Parser.Vlan_ifaces_listContext ctx) {
    ImmutableList.Builder<InterfaceReference> ifaces = ImmutableList.builder();
    for (A10Parser.Vlan_iface_ethernetContext iface : ctx.vlan_iface_ethernet()) {
      Optional<Integer> maybeNum = toInteger(messageCtx, iface.num);
      if (!maybeNum.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      ifaces.add(new InterfaceReference(Interface.Type.ETHERNET, maybeNum.get()));
    }
    for (A10Parser.Vlan_iface_trunkContext iface : ctx.vlan_iface_trunk()) {
      Optional<Integer> maybeNum = toInteger(messageCtx, iface.num);
      if (!maybeNum.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      ifaces.add(new InterfaceReference(Interface.Type.TRUNK, maybeNum.get()));
    }
    return Optional.of(ifaces.build());
  }

  Optional<Integer> toInteger(ParserRuleContext messageCtx, A10Parser.Ports_thresholdContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint8(), TRUNK_PORTS_THRESHOLD_RANGE, "trunk ports-threshold");
  }

  Optional<Integer> toInteger(ParserRuleContext messageCtx, A10Parser.Vlan_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), VLAN_NUMBER_RANGE, "vlan number");
  }

  private @Nonnull ConcreteInterfaceAddress toInterfaceAddress(A10Parser.Ip_prefixContext ctx) {
    A10Parser.Ip_netmaskContext netmask = ctx.ip_netmask();
    if (netmask.subnet_mask() != null) {
      return ConcreteInterfaceAddress.create(toIp(ctx.ip_address()), toIp(netmask.subnet_mask()));
    }
    assert netmask.ip_slash_prefix() != null;
    return ConcreteInterfaceAddress.parse(
        ctx.ip_address().getText() + netmask.ip_slash_prefix().getText());
  }

  /** Convert specified context into a prefix for a route, enforcing IP/mask requirements. */
  private @Nonnull Optional<Prefix> toRoutePrefix(
      ParserRuleContext ctx, A10Parser.Ip_prefixContext prefixCtx) {
    Ip address = toIp(prefixCtx.ip_address());
    Prefix prefix = toPrefix(prefixCtx);
    if (!prefix.getStartIp().equals(address)) {
      warn(ctx, "Incorrect IP/mask specified");
      return Optional.empty();
    }
    return Optional.of(prefix);
  }

  private @Nonnull Prefix toPrefix(A10Parser.Ip_prefixContext ctx) {
    A10Parser.Ip_netmaskContext netmask = ctx.ip_netmask();
    if (netmask.subnet_mask() != null) {
      return Prefix.create(toIp(ctx.ip_address()), toIp(netmask.subnet_mask()));
    }
    assert netmask.ip_slash_prefix() != null;
    return Prefix.parse(ctx.ip_address().getText() + netmask.ip_slash_prefix().getText());
  }

  private @Nonnull Ip toIp(A10Parser.Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private @Nonnull Ip toIp(A10Parser.Subnet_maskContext ctx) {
    return Ip.parse(ctx.getText());
  }

  /**
   * Get the number of subnet bits for an {@link
   * org.batfish.vendor.a10.grammar.A10Parser.Ip_netmaskContext}.
   */
  private int toInteger(A10Parser.Ip_netmaskContext ctx) {
    if (ctx.ip_slash_prefix() != null) {
      // Remove leading slash
      return Integer.parseUnsignedInt(ctx.ip_slash_prefix().getText().substring(1));
    }
    assert ctx.subnet_mask() != null;
    return toIp(ctx.subnet_mask()).numSubnetBits();
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Access_list_numberContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint8(), ACCESS_LIST_NUMBER_RANGE, "access-list number");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Interface_mtuContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), INTERFACE_MTU_RANGE, "interface mtu");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Static_route_distanceContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), IP_ROUTE_DISTANCE_RANGE, "ip route distance");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Loopback_numberContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint8(), INTERFACE_NUMBER_LOOPBACK_RANGE, "loopback interface number");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, A10Parser.Scaleout_device_idContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx.uint8(), SCALEOUT_DEVICE_ID_RANGE, "scaleout-device-id");
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, A10Parser.Uint8Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, A10Parser.Uint16Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, A10Parser.Uint32Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Long> toLongInSpace(
      ParserRuleContext messageCtx, A10Parser.Uint32Context ctx, LongSpace space, String name) {
    return toLongInSpace(messageCtx, ctx.getText(), space, name);
  }

  /**
   * Convert a {@link String} to an {@link Integer} if it represents a number that is contained in
   * the provided {@code space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, String str, IntegerSpace space, String name) {
    Integer num = Ints.tryParse(str);
    if (num == null || !space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%s'", name, space, str));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  /**
   * Convert a {@link String} to an {@link Long} if it represents a number that is contained in the
   * provided {@code space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Long> toLongInSpace(
      ParserRuleContext messageCtx, String str, LongSpace space, String name) {
    Long num = Longs.tryParse(str);
    if (num == null || !space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  /**
   * Return the text of the provided {@code ctx} if its length is within the provided {@link
   * IntegerSpace lengthSpace}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<String> toStringWithLengthInSpace(
      ParserRuleContext messageCtx, WordContext ctx, IntegerSpace lengthSpace, String name) {
    String text = toString(ctx);
    if (!lengthSpace.contains(text.length())) {
      warn(
          messageCtx,
          String.format(
              "Expected %s with length in range %s, but got '%s'", name, lengthSpace, text));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, HostnameContext ctx) {
    return toString(messageCtx, ctx.word(), "hostname", HOSTNAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Slb_server_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), SLB_SERVER_NAME_LENGTH_RANGE, "slb server name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Static_route_descriptionContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx,
        ctx.route_description().word(),
        IP_ROUTE_DESCRIPTION_LENGTH_RANGE,
        "ip route description");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Template_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), TEMPLATE_NAME_LENGTH_RANGE, "template name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Nat_pool_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), NAT_POOL_NAME_LENGTH_RANGE, "nat pool name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.User_tagContext ctx) {
    return toStringWithLengthInSpace(messageCtx, ctx.word(), USER_TAG_LENGTH_RANGE, "user-tag");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Vlan_nameContext ctx) {
    return toStringWithLengthInSpace(messageCtx, ctx.word(), VLAN_NAME_LENGTH_RANGE, "vlan name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Interface_name_strContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), INTERFACE_NAME_LENGTH_RANGE, "interface name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Pattern pattern) {
    return toString(messageCtx, ctx, type, s -> pattern.matcher(s).matches());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Predicate<String> predicate) {
    String text = toString(ctx);
    if (!predicate.test(text)) {
      warn(messageCtx, String.format("Illegal value for %s", type));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private static @Nonnull String toString(WordContext ctx) {
    return ctx.word_content().children.stream()
        .map(
            child -> {
              if (child instanceof A10Parser.Double_quoted_stringContext) {
                return toString(((A10Parser.Double_quoted_stringContext) child).text);
              } else if (child instanceof A10Parser.Single_quoted_stringContext) {
                return toString(((A10Parser.Single_quoted_stringContext) child).text);
              } else {
                assert child instanceof TerminalNode;
                int type = ((TerminalNode) child).getSymbol().getType();
                assert type == WORD;
                return child.getText();
              }
            })
        .collect(Collectors.joining(""));
  }

  private static @Nonnull String toString(@Nullable A10Parser.Quoted_textContext text) {
    if (text == null) {
      return "";
    }
    // Device appears to just remove backslashes from quoted strings
    return text.getText().replaceAll("\\\\", "");
  }

  private static final IntegerSpace ACCESS_LIST_NUMBER_RANGE =
      IntegerSpace.of(Range.closed(1, 199));
  private static final IntegerSpace CONNECTION_LIMIT_RANGE =
      IntegerSpace.of(Range.closed(1, 64000000));
  private static final IntegerSpace CONNECTION_WEIGHT_RANGE =
      IntegerSpace.of(Range.closed(1, 1000));
  private static final IntegerSpace FAIL_OVER_POLICY_TEMPLATE_GATEWAY_WEIGHT_RANGE =
      IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace FAIL_OVER_POLICY_TEMPLATE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace HA_GROUP_ID_RANGE = IntegerSpace.of(Range.closed(1, 31));
  private static final IntegerSpace HA_ID_RANGE = IntegerSpace.of(Range.closed(1, 2));
  private static final IntegerSpace HA_PRIORITY_RANGE = IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace HA_SET_ID_RANGE = IntegerSpace.of(Range.closed(1, 7));
  private static final IntegerSpace HEALTH_CHECK_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace INTERFACE_MTU_RANGE = IntegerSpace.of(Range.closed(434, 1500));
  private static final IntegerSpace INTERFACE_NUMBER_ETHERNET_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace INTERFACE_NUMBER_LOOPBACK_RANGE =
      IntegerSpace.of(Range.closed(0, 10));
  private static final IntegerSpace INTERFACE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace IP_ROUTE_DESCRIPTION_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace IP_ROUTE_DISTANCE_RANGE = IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace NAT_POOL_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace NON_DEFAULT_VRID_RANGE = IntegerSpace.of(Range.closed(1, 31));
  private static final IntegerSpace PORT_NUMBER_RANGE = IntegerSpace.of(Range.closed(0, 65535));
  private static final IntegerSpace PORT_RANGE_VALUE_RANGE = IntegerSpace.of(Range.closed(0, 254));
  private static final IntegerSpace SCALEOUT_DEVICE_ID_RANGE = IntegerSpace.of(Range.closed(1, 16));
  private static final IntegerSpace SERVICE_GROUP_MEMBER_PRIORITY_RANGE =
      IntegerSpace.of(Range.closed(1, 16));
  private static final IntegerSpace SERVICE_GROUP_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace SLB_SERVER_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace TEMPLATE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace TRAFFIC_BUCKET_COUNT_RANGE =
      IntegerSpace.of(Range.closed(1, 256));
  private static final IntegerSpace TRUNK_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 4096));
  private static final IntegerSpace TRUNK_PORTS_THRESHOLD_RANGE =
      IntegerSpace.of(Range.closed(2, 8));
  private static final IntegerSpace USER_TAG_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace VIRTUAL_SERVER_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace VIRTUAL_SERVICE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace VLAN_NAME_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace VLAN_NUMBER_RANGE = IntegerSpace.of(Range.closed(2, 4094));
  private static final IntegerSpace VRID_LEAD_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace VRID_RANGE = IntegerSpace.of(Range.closed(0, 31));
  private static final IntegerSpace VRRP_A_DEVICE_ID_RANGE = IntegerSpace.of(Range.closed(1, 4));
  private static final IntegerSpace VRRP_A_SET_ID_RANGE = IntegerSpace.of(Range.closed(1, 15));
  private static final IntegerSpace VRRP_A_PRIORITY_RANGE = IntegerSpace.of(Range.closed(1, 255));

  private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

  /** Combination of all NAT pools, used for preventing pool overlap */
  private LongSpace _allNatPools = LongSpace.EMPTY;

  private @Nonnull A10Configuration _c;

  private AccessList _currentAccessList;

  private BgpProcess _currentBgpProcess;

  private BgpNeighbor _currentBgpNeighbor;

  private Interface _currentInterface;

  private NatPool _currentNatPool;

  private VrrpAFailOverPolicyTemplate _currentVrrpAFailOverPolicyTemplate;

  /**
   * Boolean indicating if the {@code _currentNatPool} is valid (i.e. configured properties are
   * valid)
   */
  private boolean _currentNatPoolValid;

  private ServiceGroup _currentServiceGroup;

  private ServiceGroupMember _currentServiceGroupMember;

  private Server _currentServer;

  private ServerPort _currentServerPort;

  // Current trunk for ACOS v2 trunk stanza
  private TrunkInterface _currentTrunk;

  private TrunkGroup _currentTrunkGroup;

  private VirtualServer _currentVirtualServer;

  private VirtualServerPort _currentVirtualServerPort;

  private Vlan _currentVlan;

  private VrrpAVrid _currentVrid;

  private @Nonnull A10CombinedParser _parser;

  private final @Nonnull String _text;

  private final @Nonnull Warnings _w;

  private final @Nonnull SilentSyntaxCollection _silentSyntax;
}
