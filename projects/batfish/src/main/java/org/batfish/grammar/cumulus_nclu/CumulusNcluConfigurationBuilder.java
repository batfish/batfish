package org.batfish.grammar.cumulus_nclu;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.MacAddress;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bgpContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bondContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bridgeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_hostnameContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_interfaceContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_loopbackContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_routingContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_timeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vlanContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vrfContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vxlanContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bob_accessContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bob_vidsContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bobo_slavesContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bond_clag_idContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Cumulus_nclu_configurationContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Dn4Context;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Dn6Context;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.GlobContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Glob_range_setContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.I_ip_addressContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Ic_backup_ipContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Ic_peer_ipContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Ic_priorityContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Ic_sys_macContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Interface_addressContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Ip_addressContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Ipv6_addressContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Mac_addressContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.RangeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Range_setContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.S_extra_configurationContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.S_net_add_unrecognizedContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Uint16Context;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vlan_idContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vlan_rangeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vlan_range_setContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vni_numberContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vrf_ip_addressContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vrf_vniContext;
import org.batfish.representation.cumulus.Bond;
import org.batfish.representation.cumulus.CumulusInterfaceType;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.CumulusStructureUsage;
import org.batfish.representation.cumulus.Interface;
import org.batfish.representation.cumulus.Vrf;

/**
 * A listener that builds a {@link CumulusNcluConfiguration} while walking a parse tree produced by
 * {@link CumulusNcluCombinedParser#parse}.
 */
public class CumulusNcluConfigurationBuilder extends CumulusNcluParserBaseListener {

  private static final String LOOPBACK_INTERFACE_NAME = "lo";
  private static final Pattern NUMBERED_WORD_PATTERN = Pattern.compile("^(.*[^0-9])([0-9]+)$");
  private static final Pattern PHYSICAL_INTERFACE_PATTERN = Pattern.compile("(swp|eth)[0-9]+");
  private static final Pattern SUBINTERFACE_PATTERN = Pattern.compile("^(.*)\\.([0-9]+)$");

  private static int toInteger(Uint16Context ctx) {
    return Integer.parseInt(ctx.getText(), 10);
  }

  private static int toInteger(Vlan_idContext ctx) {
    return Integer.parseInt(ctx.getText(), 10);
  }

  private static int toInteger(Vni_numberContext ctx) {
    return Integer.parseInt(ctx.getText(), 10);
  }

  private static @Nonnull InterfaceAddress toInterfaceAddress(Interface_addressContext ctx) {
    return new InterfaceAddress(ctx.getText());
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Ip6 toIp6(Ipv6_addressContext ctx) {
    return Ip6.parse(ctx.getText());
  }

  private static @Nonnull MacAddress toMacAddress(Mac_addressContext ctx) {
    return MacAddress.parse(ctx.getText());
  }

  private static @Nonnull Range<Integer> toRange(RangeContext ctx) {
    int low = toInteger(ctx.low);
    int high = ctx.high != null ? toInteger(ctx.high) : low;
    return Range.closed(low, high);
  }

  private static @Nonnull Range<Integer> toRange(Vlan_rangeContext ctx) {
    int low = toInteger(ctx.low);
    int high = ctx.high != null ? toInteger(ctx.high) : low;
    return Range.closed(low, high);
  }

  private static @Nonnull RangeSet<Integer> toRangeSet(Range_setContext ctx) {
    return ctx.range().stream()
        .map(CumulusNcluConfigurationBuilder::toRange)
        .collect(ImmutableRangeSet.toImmutableRangeSet());
  }

  private static @Nonnull RangeSet<Integer> toRangeSet(Vlan_range_setContext ctx) {
    return ctx.vlan_range().stream()
        .map(CumulusNcluConfigurationBuilder::toRange)
        .collect(ImmutableRangeSet.toImmutableRangeSet());
  }

  private static @Nonnull Set<String> toStrings(Glob_range_setContext ctx) {
    if (ctx.unnumbered != null) {
      return ImmutableSet.of(ctx.unnumbered.getText());
    }
    String baseWord = ctx.base_word.getText();
    if (ctx.first_interval_end == null && ctx.other_numeric_ranges == null) {
      return ImmutableSet.of(baseWord);
    }
    Matcher matcher = NUMBERED_WORD_PATTERN.matcher(baseWord);
    matcher.matches(); // parser+lexer guarantee match
    String prefix = matcher.group(1);
    int firstIntervalStart = Integer.parseInt(matcher.group(2), 10);
    int firstIntervalEnd =
        ctx.first_interval_end != null
            ? Integer.parseInt(ctx.first_interval_end.getText(), 10)
            : firstIntervalStart;
    // add first interval
    ImmutableRangeSet.Builder<Integer> builder =
        ImmutableRangeSet.<Integer>builder()
            .add(Range.closed(firstIntervalStart, firstIntervalEnd));
    if (ctx.other_numeric_ranges != null) {
      // add other intervals
      builder.addAll(toRangeSet(ctx.other_numeric_ranges));
    }
    return builder.build().asRanges().stream()
        .flatMapToInt(r -> IntStream.rangeClosed(r.lowerEndpoint(), r.upperEndpoint()))
        .mapToObj(i -> String.format("%s%d", prefix, i))
        .collect(ImmutableSet.toImmutableSet());
  }

  private static @Nonnull Set<String> toStrings(GlobContext ctx) {
    return ctx.glob_range_set().stream()
        .flatMap(grs -> toStrings(grs).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  private @Nullable CumulusNcluConfiguration _c;

  private @Nullable Bond _currentBond;
  private @Nullable List<Interface> _currentInterfaces;
  private @Nullable List<Vrf> _currentVrfs;
  private final @Nonnull CumulusNcluCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;

  public CumulusNcluConfigurationBuilder(
      CumulusNcluCombinedParser parser, String text, Warnings w) {
    _parser = parser;
    _text = text;
    _w = w;
  }

  @SuppressWarnings("unused")
  private @Nullable <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, @Nullable U defaultReturnValue) {
    _w.redFlag(
        String.format("Could not convert to %s: %s", returnType.getSimpleName(), getFullText(ctx)));
    return defaultReturnValue;
  }

  /**
   * Returns a newly-created {@link Bond} with given {@code name}, or {@code null} if {@code name}
   * is invalid.
   */
  private @Nullable Bond createBond(String name, A_bondContext ctx) {
    if (name.equals(LOOPBACK_INTERFACE_NAME)
        || PHYSICAL_INTERFACE_PATTERN.matcher(name).matches()
        || SUBINTERFACE_PATTERN.matcher(name).matches()) {
      _w.redFlag(String.format("Invalid name '%s' for bond in: %s", name, getFullText(ctx)));
      return null;
    }
    if (_c.getVrfs().containsKey(name)) {
      _w.redFlag(
          String.format(
              "Invalid name '%s' for bond clashes with existing vrf in: %s",
              name, getFullText(ctx)));
      return null;
    }

    return new Bond(name);
  }

  /**
   * Returns a newly-created {@link Interface} with given {@code name}, or {@code null} if {@code
   * name} is invalid.
   */
  private @Nullable Interface createInterface(String name, ParserRuleContext ctx) {
    Matcher subinterfaceMatcher = SUBINTERFACE_PATTERN.matcher(name);
    Integer encapsulationVlan = null;
    CumulusInterfaceType type;
    if (name.equals(LOOPBACK_INTERFACE_NAME)) {
      _w.redFlag(
          String.format(
              "Loopback interface can only be configured via 'net add loopback' family of commands; following is invalid: %s",
              getFullText(ctx)));
      return null;
    } else if (_c.getVrfs().containsKey(name)) {
      _w.redFlag(
          String.format(
              "Invalid name '%s' for interface clashes with existing vrf in: %s",
              name, getFullText(ctx)));
      return null;
    } else if (PHYSICAL_INTERFACE_PATTERN.matcher(name).matches()) {
      type = CumulusInterfaceType.PHYSICAL;
    } else if (subinterfaceMatcher.matches()) {
      String layer1LogicalInterfaceName = subinterfaceMatcher.group(1);
      String vlanStr = subinterfaceMatcher.group(2);
      if (!PHYSICAL_INTERFACE_PATTERN.matcher(layer1LogicalInterfaceName).matches()
          && !_c.getBonds().containsKey(layer1LogicalInterfaceName)) {
        _w.redFlag(
            String.format(
                "Subinterface name '%s' is invalid since '%s' is neither a physical nor a bond interface in: %s",
                name, layer1LogicalInterfaceName, getFullText(ctx)));
        return null;
      }
      try {
        encapsulationVlan = Integer.parseInt(vlanStr, 10);
        checkArgument(1 <= encapsulationVlan && encapsulationVlan <= 4094);
      } catch (IllegalArgumentException e) {
        _w.redFlag(
            String.format(
                "Subinterface name '%s' is invalid since '%s' is not a valid VLAN number in: %s",
                name, vlanStr, getFullText(ctx)));
        return null;
      }
      type = CumulusInterfaceType.SUBINTERFACE;
    } else if (_c.getBonds().containsKey(name)) {
      type = CumulusInterfaceType.BOND;
    } else {
      _w.redFlag(String.format("Interface name '%s' is invalid in: %s", name, getFullText(ctx)));
      return null;
    }
    Interface iface = new Interface(name, type);
    iface.setEncapsulationVlan(encapsulationVlan);
    return iface;
  }

  /**
   * Returns a newly-created {@link Vrf} with given {@code name}, or {@code null} if {@code name} is
   * invalid.
   */
  private @Nullable Vrf createVrf(String name, ParserRuleContext ctx) {
    if (name.equals(LOOPBACK_INTERFACE_NAME)
        || PHYSICAL_INTERFACE_PATTERN.matcher(name).matches()
        || SUBINTERFACE_PATTERN.matcher(name).matches()) {
      _w.redFlag(String.format("Invalid name '%s' for vrf in: %s", name, getFullText(ctx)));
      return null;
    }
    if (_c.getBonds().containsKey(name)) {
      _w.redFlag(
          String.format(
              "Invalid name '%s' for vrf clashes with existing bond interface in: %s",
              name, getFullText(ctx)));
      return null;
    }
    if (_c.getInterfaces().containsKey(name)) {
      _w.redFlag(
          String.format(
              "Invalid name '%s' for vrf clashes with existing interface in: %s",
              name, getFullText(ctx)));
      return null;
    }
    return new Vrf(name);
  }

  @Override
  public void enterA_bond(A_bondContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    Bond bond = _c.getBonds().get(name);
    if (bond == null) {
      bond = createBond(name, ctx);
      if (bond == null) {
        bond = new Bond("dummy");
      } else {
        _c.defineStructure(CumulusStructureType.BOND, name, line);
        _c.referenceStructure(
            CumulusStructureType.BOND, name, CumulusStructureUsage.BOND_SELF_REFERENCE, line);
        _c.getBonds().put(name, bond);
      }
    }
    _currentBond = bond;
  }

  @Override
  public void enterA_interface(A_interfaceContext ctx) {
    Set<String> interfaceNames = toStrings(ctx.interfaces);
    _currentInterfaces =
        initInterfacesIfAbsent(interfaceNames, ctx, CumulusStructureUsage.INTERFACE_SELF_REFERENCE);
  }

  @Override
  public void enterA_vrf(A_vrfContext ctx) {
    Set<String> vrfNames = toStrings(ctx.names);
    _currentVrfs = initVrfsIfAbsent(vrfNames, ctx, CumulusStructureUsage.VRF_SELF_REFERENCE);
  }

  @Override
  public void enterCumulus_nclu_configuration(Cumulus_nclu_configurationContext ctx) {
    _c = new CumulusNcluConfiguration();
  }

  @Override
  public void exitA_bgp(A_bgpContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_bond(A_bondContext ctx) {
    _currentBond = null;
  }

  @Override
  public void exitA_bridge(A_bridgeContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_hostname(A_hostnameContext ctx) {
    _c.setHostname(ctx.hostname.getText());
  }

  @Override
  public void exitA_interface(A_interfaceContext ctx) {
    _currentInterfaces = null;
  }

  @Override
  public void exitA_loopback(A_loopbackContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_routing(A_routingContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_time(A_timeContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_vlan(A_vlanContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_vrf(A_vrfContext ctx) {
    _currentVrfs = null;
  }

  @Override
  public void exitA_vxlan(A_vxlanContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitBob_access(Bob_accessContext ctx) {
    _currentBond.getBridge().setAccess(toInteger(ctx.vlan));
  }

  @Override
  public void exitBob_vids(Bob_vidsContext ctx) {
    _currentBond.getBridge().setVids(IntegerSpace.of(toRangeSet(ctx.vlans)));
  }

  @Override
  public void exitBobo_slaves(Bobo_slavesContext ctx) {
    Set<String> slaves = toStrings(ctx.slaves);
    _currentBond.setSlaves(
        initInterfacesIfAbsent(slaves, ctx, CumulusStructureUsage.BOND_SLAVE).isEmpty()
            ? ImmutableSet.of()
            : slaves);
  }

  @Override
  public void exitBond_clag_id(Bond_clag_idContext ctx) {
    _currentBond.setClagId(toInteger(ctx.id));
  }

  @Override
  public void exitDn4(Dn4Context ctx) {
    _c.getIpv4Nameservers().add(toIp(ctx.address));
  }

  @Override
  public void exitDn6(Dn6Context ctx) {
    _c.getIpv6Nameservers().add(toIp6(ctx.address6));
  }

  @Override
  public void exitI_ip_address(I_ip_addressContext ctx) {
    InterfaceAddress address = toInterfaceAddress(ctx.address);
    _currentInterfaces.forEach(iface -> iface.getIpAddresses().add(address));
  }

  @Override
  public void exitIc_backup_ip(Ic_backup_ipContext ctx) {
    Ip backupIp = toIp(ctx.backup_ip);
    String vrf = ctx.vrf != null ? ctx.vrf.getText() : null;
    _currentInterfaces.forEach(
        iface -> {
          iface.setClagBackupIp(backupIp);
          iface.setClagBackupIpVrf(vrf);
        });
  }

  @Override
  public void exitIc_peer_ip(Ic_peer_ipContext ctx) {
    Ip peerIp = toIp(ctx.peer_ip);
    _currentInterfaces.forEach(
        iface -> {
          iface.setClagPeerIp(peerIp);
        });
  }

  @Override
  public void exitIc_priority(Ic_priorityContext ctx) {
    int priority = toInteger(ctx.priority);
    _currentInterfaces.forEach(
        iface -> {
          iface.setClagPriority(priority);
        });
  }

  @Override
  public void exitIc_sys_mac(Ic_sys_macContext ctx) {
    MacAddress macAddress = toMacAddress(ctx.mac);
    _currentInterfaces.forEach(
        iface -> {
          iface.setClagSysMac(macAddress);
        });
  }

  @Override
  public void exitS_extra_configuration(S_extra_configurationContext ctx) {
    unrecognized(ctx);
  }

  @Override
  public void exitS_net_add_unrecognized(S_net_add_unrecognizedContext ctx) {
    unrecognized(ctx);
  }

  @Override
  public void exitVrf_ip_address(Vrf_ip_addressContext ctx) {
    InterfaceAddress address = toInterfaceAddress(ctx.address);
    _currentVrfs.forEach(vrf -> vrf.getAddresses().add(address));
  }

  @Override
  public void exitVrf_vni(Vrf_vniContext ctx) {
    int vni = toInteger(ctx.vni);
    _currentVrfs.forEach(vrf -> vrf.setVni(vni));
  }

  /**
   * Returns built {@link CumulusNcluConfiguration}.
   *
   * @throws IllegalStateException if called before walking parse tree produced by {@link
   *     CumulusNcluCombinedParser#parse}
   */
  public @Nonnull CumulusNcluConfiguration getConfiguration() {
    checkState(
        _c != null,
        "Cannot return vendor configuration before walking valid Cumulus NCLU parse tree");
    return _c;
  }

  private @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  /**
   * Returns already-present or newly-created {@link Interface}s with given {@code names}, or an
   * empty {@link List} if any name in {@code names} is invalid.
   */
  private @Nonnull List<Interface> initInterfacesIfAbsent(
      Set<String> names, ParserRuleContext ctx, CumulusStructureUsage usage) {
    ImmutableList.Builder<Interface> interfacesBuilder = ImmutableList.builder();
    ImmutableList.Builder<String> newInterfaces = ImmutableList.builder();
    for (String name : names) {
      Interface iface = _c.getInterfaces().get(name);
      if (iface == null) {
        iface = createInterface(name, ctx);
        if (iface == null) {
          return ImmutableList.of();
        }
        newInterfaces.add(name);
      }
      interfacesBuilder.add(iface);
    }
    List<Interface> interfaces = interfacesBuilder.build();
    interfaces.forEach(iface -> _c.getInterfaces().computeIfAbsent(iface.getName(), n -> iface));
    int line = ctx.getStart().getLine();
    newInterfaces
        .build()
        .forEach(name -> _c.defineStructure(CumulusStructureType.INTERFACE, name, line));
    names.forEach(name -> _c.referenceStructure(CumulusStructureType.INTERFACE, name, usage, line));
    return interfaces;
  }

  /**
   * Returns already-present or newly-created {@link Vrf}s with given {@code names}, or an empty
   * {@link List} if any name in {@code names} is invalid.
   */
  private @Nonnull List<Vrf> initVrfsIfAbsent(
      Set<String> names, ParserRuleContext ctx, CumulusStructureUsage usage) {
    ImmutableList.Builder<Vrf> vrfsBuilder = ImmutableList.builder();
    ImmutableList.Builder<String> newVrfs = ImmutableList.builder();
    for (String name : names) {
      Vrf vrf = _c.getVrfs().get(name);
      if (vrf == null) {
        vrf = createVrf(name, ctx);
        if (vrf == null) {
          return ImmutableList.of();
        }
        newVrfs.add(name);
      }
      vrfsBuilder.add(vrf);
    }
    List<Vrf> vrfs = vrfsBuilder.build();
    vrfs.forEach(vrf -> _c.getVrfs().computeIfAbsent(vrf.getName(), n -> vrf));
    int line = ctx.getStart().getLine();
    newVrfs.build().forEach(name -> _c.defineStructure(CumulusStructureType.VRF, name, line));
    names.forEach(name -> _c.referenceStructure(CumulusStructureType.VRF, name, usage, line));
    return vrfs;
  }

  @SuppressWarnings("unused")
  private void todo(ParserRuleContext ctx) {
    _w.todo(ctx, getFullText(ctx), _parser);
  }

  private void unrecognized(ParserRuleContext ctx) {
    _w.getParseWarnings()
        .add(
            new ParseWarning(
                ctx.getStart().getLine(),
                getFullText(ctx),
                ctx.toString(Arrays.asList(_parser.getParser().getRuleNames())),
                "This syntax is unrecognized"));
    _c.setUnrecognized(true);
  }
}
