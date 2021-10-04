package org.batfish.vendor.a10.grammar;

import static org.batfish.vendor.a10.grammar.A10Lexer.WORD;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
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
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.a10.grammar.A10Parser.A10_configurationContext;
import org.batfish.vendor.a10.grammar.A10Parser.HostnameContext;
import org.batfish.vendor.a10.grammar.A10Parser.S_hostnameContext;
import org.batfish.vendor.a10.grammar.A10Parser.WordContext;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.batfish.vendor.a10.representation.A10StructureType;
import org.batfish.vendor.a10.representation.A10StructureUsage;
import org.batfish.vendor.a10.representation.Interface;
import org.batfish.vendor.a10.representation.InterfaceReference;
import org.batfish.vendor.a10.representation.NatPool;
import org.batfish.vendor.a10.representation.StaticRoute;
import org.batfish.vendor.a10.representation.StaticRouteManager;
import org.batfish.vendor.a10.representation.TrunkGroup;
import org.batfish.vendor.a10.representation.TrunkInterface;
import org.batfish.vendor.a10.representation.Vlan;

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
      _w.redFlag(
          String.format(
              "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
              line, lineText));
    }
  }

  @Nonnull
  public A10Configuration getConfiguration() {
    return _c;
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
  public SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Nonnull
  @Override
  public Warnings getWarnings() {
    return _w;
  }

  @Override
  public void enterA10_configuration(A10_configurationContext ctx) {}

  @Override
  public void exitA10_configuration(A10_configurationContext ctx) {
    _c.finalizeStructures();
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    toString(ctx, ctx.hostname()).ifPresent(_c::setHostname);
  }

  @Override
  public void enterSid_ethernet(A10Parser.Sid_ethernetContext ctx) {
    Optional<Integer> num = toInteger(ctx.num);
    num.ifPresent(
        n -> {
          _currentInterface =
              _c.getInterfacesEthernet()
                  .computeIfAbsent(n, number -> new Interface(Interface.Type.ETHERNET, n));
          String ifaceName = getInterfaceName(_currentInterface);
          _c.defineStructure(A10StructureType.INTERFACE, ifaceName, ctx);
          _c.referenceStructure(
              A10StructureType.INTERFACE,
              ifaceName,
              A10StructureUsage.INTERFACE_SELF_REF,
              ctx.start.getLine());
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
    Optional<Integer> num = toInteger(ctx.num);
    num.ifPresent(
        n -> {
          _currentInterface =
              _c.getInterfacesLoopback()
                  .computeIfAbsent(n, number -> new Interface(Interface.Type.LOOPBACK, n));
          String ifaceName = getInterfaceName(_currentInterface);
          _c.defineStructure(A10StructureType.INTERFACE, ifaceName, ctx);
          _c.referenceStructure(
              A10StructureType.INTERFACE,
              ifaceName,
              A10StructureUsage.INTERFACE_SELF_REF,
              ctx.start.getLine());
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
    Optional<Integer> num = toInteger(ctx.num);
    num.ifPresent(
        n ->
            _currentInterface =
                _c.getInterfacesTrunk().computeIfAbsent(n, number -> new TrunkInterface(n, null)));
    _c.defineStructure(A10StructureType.INTERFACE, getInterfaceName(_currentInterface), ctx);
    if (!num.isPresent()) {
      _currentInterface = new TrunkInterface(-1, null); // dummy
    }
  }

  @Override
  public void exitSid_loopback(A10Parser.Sid_loopbackContext ctx) {
    _currentInterface = null;
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
    toInteger(ctx.interface_mtu()).ifPresent(mtu -> _currentInterface.setMtu(mtu));
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
  public void exitSid_name(A10Parser.Sid_nameContext ctx) {
    toString(ctx, ctx.interface_name_str()).ifPresent(n -> _currentInterface.setName(n));
  }

  @Override
  public void exitSid_ports_threshold(A10Parser.Sid_ports_thresholdContext ctx) {
    if (!(_currentInterface instanceof TrunkInterface)) {
      warn(ctx, "Ports-threshold can only be configured on trunk interfaces.");
      return;
    }
    toInteger(ctx.ports_threshold())
        .ifPresent(n -> ((TrunkInterface) _currentInterface).setPortsThreshold(n));
  }

  @Override
  public void enterSid_ve(A10Parser.Sid_veContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx.num);
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
    _c.defineStructure(A10StructureType.INTERFACE, getInterfaceName(_currentInterface), ctx);
  }

  @Override
  public void exitSid_ve(A10Parser.Sid_veContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void enterSidl_trunk(A10Parser.Sidl_trunkContext ctx) {
    TrunkGroup.Type type = TrunkGroup.Type.LACP;
    toInteger(ctx.num)
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
      maybeDistance = toInteger(distCtx);
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
    Optional<Integer> maybeVlanNum = toInteger(ctx.vlan_number());
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
    Optional<Integer> maybeNum = toInteger(ctx.vlan_number());
    if (maybeNum.isPresent()) {
      if (!maybeNum.get().equals(_currentVlan.getNumber())) {
        warn(ctx, "Virtual Ethernet interface number must be the same as VLAN ID.");
        return;
      }
      String routerInterfaceName = getInterfaceName(Interface.Type.VE, maybeNum.get());
      _c.defineStructure(A10StructureType.INTERFACE, routerInterfaceName, ctx);
      _c.referenceStructure(
          A10StructureType.INTERFACE,
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
        .ifPresent(refs -> _currentVlan.addTagged(refs));
  }

  @Override
  public void exitSvd_untagged(A10Parser.Svd_untaggedContext ctx) {
    // TODO enforce interface restrictions
    //  e.g. untagged iface cannot be reused, cannot attach trunk members directly, etc.
    toInterfaceReferences(ctx.vlan_iface_references(), A10StructureUsage.VLAN_UNTAGGED_INTERFACE)
        .ifPresent(refs -> _currentVlan.addUntagged(refs));
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
    _c.defineStructure(A10StructureType.INTERFACE, trunkName, ctx);
    _c.referenceStructure(
        A10StructureType.INTERFACE,
        trunkName,
        A10StructureUsage.INTERFACE_TRUNK_GROUP,
        ctx.start.getLine());
  }

  @Override
  public void enterSid_trunk_group(A10Parser.Sid_trunk_groupContext ctx) {
    TrunkGroup.Type type = ctx.trunk_type() != null ? toType(ctx.trunk_type()) : null;
    Optional<Integer> maybeNum = toInteger(ctx.trunk_number());
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
  private Optional<NatPool> createNatPool(
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
    Optional<NatPool> overlappinngPool =
        _c.getNatPools().values().stream()
            .filter(
                otherPool -> natRangesOverlap(start, end, otherPool.getStart(), otherPool.getEnd()))
            .findAny();
    if (overlappinngPool.isPresent()) {
      warn(
          ctx,
          String.format(
              "Invalid NAT pool range, overlaps with existing NAT pool '%s'",
              overlappinngPool.get().getName()));
      return Optional.empty();
    }

    return Optional.of(new NatPool(name, start, end, netmask));
  }

  /** Return boolean indicating if the specified NAT pool ranges overlap. */
  private static boolean natRangesOverlap(Ip lhsStart, Ip lhsEnd, Ip rhsStart, Ip rhsEnd) {
    assert lhsEnd.compareTo(lhsStart) > 0;
    assert rhsEnd.compareTo(rhsStart) > 0;
    return lhsStart.compareTo(rhsEnd) <= 0 && rhsStart.compareTo(lhsEnd) <= 0;
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
      _c.getNatPools().put(_currentNatPool.getName(), _currentNatPool);
    }
    _currentNatPool = null;
  }

  @Override
  public void exitSinpp_gateway(A10Parser.Sinpp_gatewayContext ctx) {
    _currentNatPool.setGateway(toIp(ctx.gateway));
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
    Optional<Integer> scaleoutDeviceId = toInteger(ctx.scaleout_device_id());
    if (scaleoutDeviceId.isPresent()) {
      _currentNatPool.setScaleoutDeviceId(scaleoutDeviceId.get());
    } else {
      _currentNatPoolValid = false;
    }
  }

  @Override
  public void exitSinpp_vrid(A10Parser.Sinpp_vridContext ctx) {
    // TODO enforce existence check when VRRP is supported
    Optional<Integer> vrid = toInteger(ctx.vrid());
    if (vrid.isPresent()) {
      _currentNatPool.setVrid(vrid.get());
    } else {
      _currentNatPoolValid = false;
    }
  }

  @Override
  public void enterS_lacp_trunk(A10Parser.S_lacp_trunkContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx.trunk_number());
    _currentTrunk =
        maybeNum
            .map(
                n -> {
                  TrunkInterface trunkInterface =
                      _c.getInterfacesTrunk()
                          .computeIfAbsent(n, num -> new TrunkInterface(num, TrunkGroup.Type.LACP));
                  String trunkName = getInterfaceName(trunkInterface);
                  _c.defineStructure(A10StructureType.INTERFACE, trunkName, ctx);
                  _c.referenceStructure(
                      A10StructureType.INTERFACE,
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
    toInteger(ctx.ports_threshold()).ifPresent(n -> _currentTrunk.setPortsThreshold(n));
  }

  @Override
  public void enterS_trunk(A10Parser.S_trunkContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx.trunk_number());
    _currentTrunk =
        maybeNum
            .map(
                n -> {
                  TrunkInterface trunkInterface =
                      _c.getInterfacesTrunk()
                          .computeIfAbsent(
                              n, num -> new TrunkInterface(num, TrunkGroup.Type.STATIC));
                  String trunkName = getInterfaceName(trunkInterface);
                  _c.defineStructure(A10StructureType.INTERFACE, trunkName, ctx);
                  _c.referenceStructure(
                      A10StructureType.INTERFACE,
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
                _c.referenceStructure(
                    A10StructureType.INTERFACE,
                    getInterfaceName(iface),
                    A10StructureUsage.TRUNK_INTERFACE,
                    line);
                _currentTrunk.getMembers().add(iface);
              });
        });
  }

  Optional<List<InterfaceReference>> toInterfaces(A10Parser.Std_ethernetContext ctx) {
    ImmutableList.Builder<InterfaceReference> ifaces = ImmutableList.builder();
    for (A10Parser.Trunk_ethernet_interfaceContext iface : ctx.trunk_ethernet_interface()) {
      Optional<Integer> maybeNum = toInteger(iface.num);
      if (!maybeNum.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      ifaces.add(new InterfaceReference(Interface.Type.ETHERNET, maybeNum.get()));
    }
    return Optional.of(ifaces.build());
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

  private Optional<List<InterfaceReference>> toInterfaceReferences(
      A10Parser.Vlan_ifaces_rangeContext ctx) {
    Interface.Type type = toInterfaceType(ctx);
    return toSubRange(ctx)
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
                ctx.vlan_ifaces_list().stream().map(this::toInterfaces),
                ctx.vlan_ifaces_range().stream().map(this::toInterfaceReferences))
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
                          _c.referenceStructure(
                              A10StructureType.INTERFACE, getInterfaceName(iface), usage, line));
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

  Optional<SubRange> toSubRange(A10Parser.Vlan_ifaces_rangeContext ctx) {
    Optional<Integer> maybeFrom;
    Optional<Integer> maybeTo;
    if (ctx.vlan_iface_ethernet_range() != null) {
      maybeFrom = toInteger(ctx.vlan_iface_ethernet_range().num);
      maybeTo = toInteger(ctx.vlan_iface_ethernet_range().to);
    } else {
      assert ctx.vlan_iface_trunk_range() != null;
      maybeFrom = toInteger(ctx.vlan_iface_trunk_range().num);
      maybeTo = toInteger(ctx.vlan_iface_trunk_range().to);
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

  Optional<List<InterfaceReference>> toInterfaces(A10Parser.Vlan_ifaces_listContext ctx) {
    ImmutableList.Builder<InterfaceReference> ifaces = ImmutableList.builder();
    for (A10Parser.Vlan_iface_ethernetContext iface : ctx.vlan_iface_ethernet()) {
      Optional<Integer> maybeNum = toInteger(iface.num);
      if (!maybeNum.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      ifaces.add(new InterfaceReference(Interface.Type.ETHERNET, maybeNum.get()));
    }
    for (A10Parser.Vlan_iface_trunkContext iface : ctx.vlan_iface_trunk()) {
      Optional<Integer> maybeNum = toInteger(iface.num);
      if (!maybeNum.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      ifaces.add(new InterfaceReference(Interface.Type.TRUNK, maybeNum.get()));
    }
    return Optional.of(ifaces.build());
  }

  Optional<Integer> toInteger(A10Parser.Trunk_numberContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint16(), TRUNK_NUMBER_RANGE, "trunk number");
  }

  Optional<Integer> toInteger(A10Parser.Ports_thresholdContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint8(), TRUNK_PORTS_THRESHOLD_RANGE, "trunk ports-threshold");
  }

  Optional<Integer> toInteger(A10Parser.Vlan_numberContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint16(), VLAN_NUMBER_RANGE, "vlan number");
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

  private @Nonnull Optional<Integer> toInteger(A10Parser.Interface_mtuContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint16(), INTERFACE_MTU_RANGE, "interface mtu");
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.Static_route_distanceContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint8(), IP_ROUTE_DISTANCE_RANGE, "ip route distance");
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.Ethernet_numberContext ctx) {
    return toIntegerInSpace(
        ctx, ctx.uint8(), INTERFACE_NUMBER_ETHERNET_RANGE, "interface ethernet number");
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.Loopback_numberContext ctx) {
    return toIntegerInSpace(
        ctx, ctx.uint8(), INTERFACE_NUMBER_LOOPBACK_RANGE, "interface loopback number");
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.Scaleout_device_idContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint8(), SCALEOUT_DEVICE_ID_RANGE, "scaleout-device-id");
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.VridContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint8(), VRID_RANGE, "vrid");
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

  /**
   * Convert a {@link String} to an {@link Integer} if it represents a number that is contained in
   * the provided {@code space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, String str, IntegerSpace space, String name) {
    Integer num = Ints.tryParse(str);
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
      ParserRuleContext messageCtx, A10Parser.Static_route_descriptionContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx,
        ctx.route_description().word(),
        IP_ROUTE_DESCRIPTION_LENGTH_RANGE,
        "ip route description");
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
  private static final IntegerSpace SCALEOUT_DEVICE_ID_RANGE = IntegerSpace.of(Range.closed(1, 16));
  private static final IntegerSpace TRUNK_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 4096));
  private static final IntegerSpace TRUNK_PORTS_THRESHOLD_RANGE =
      IntegerSpace.of(Range.closed(2, 8));
  private static final IntegerSpace USER_TAG_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace VLAN_NAME_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace VLAN_NUMBER_RANGE = IntegerSpace.of(Range.closed(2, 4094));
  private static final IntegerSpace VRID_RANGE = IntegerSpace.of(Range.closed(1, 31));

  private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

  @Nonnull private A10Configuration _c;

  private Interface _currentInterface;

  private NatPool _currentNatPool;

  /**
   * Boolean indicating if the {@code _currentNatPool} is valid (i.e. configured properties are
   * valid)
   */
  private boolean _currentNatPoolValid;

  // Current trunk for ACOS v2 trunk stanza
  private TrunkInterface _currentTrunk;

  private TrunkGroup _currentTrunkGroup;

  private Vlan _currentVlan;

  @Nonnull private A10CombinedParser _parser;

  @Nonnull private final String _text;

  @Nonnull private final Warnings _w;

  @Nonnull private final SilentSyntaxCollection _silentSyntax;
}
