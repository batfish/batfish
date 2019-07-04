package org.batfish.grammar.cisco_nxos;

import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.getCanonicalInterfaceNamePrefix;
import static org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType.ETHERNET;
import static org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType.LOOPBACK;
import static org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType.MGMT;
import static org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType.PORT_CHANNEL;
import static org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType.VLAN;
import static org.batfish.representation.cisco_nxos.Interface.VLAN_RANGE;
import static org.batfish.representation.cisco_nxos.Interface.newNonVlanInterface;
import static org.batfish.representation.cisco_nxos.Interface.newVlanInterface;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Channel_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cisco_nxos_configurationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_bandwidthContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_channel_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_encapsulationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_autostateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_accessContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_trunk_allowedContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_trunk_nativeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_vrf_memberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_bandwidth_kbpsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_prefixContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_hostnameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_vrf_contextContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Uint16Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vc_no_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vc_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vlan_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vlan_id_rangeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vlan_vlanContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vrf_nameContext;
import org.batfish.representation.cisco_nxos.CiscoNxosConfiguration;
import org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType;
import org.batfish.representation.cisco_nxos.CiscoNxosStructureType;
import org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage;
import org.batfish.representation.cisco_nxos.Interface;
import org.batfish.representation.cisco_nxos.Vlan;
import org.batfish.representation.cisco_nxos.Vrf;
import org.batfish.vendor.VendorConfiguration;

@ParametersAreNonnullByDefault
public final class CiscoNxosControlPlaneExtractor extends CiscoNxosParserBaseListener
    implements ControlPlaneExtractor {

  private static final IntegerSpace BANDWIDTH_RANGE = IntegerSpace.of(Range.closed(1, 100_000_000));
  private static final int MAX_VRF_NAME_LENGTH = 32;
  private static final IntegerSpace PORT_CHANNEL_RANGE = IntegerSpace.of(Range.closed(1, 4096));

  private static int toInteger(Uint16Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static @Nonnull InterfaceAddress toInterfaceAddress(Interface_addressContext ctx) {
    // TODO: support exotic address types
    return ctx.iaddress != null
        ? ConcreteInterfaceAddress.parse(ctx.getText())
        : ConcreteInterfaceAddress.create(toIp(ctx.address), toIp(ctx.mask));
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private @Nullable CiscoNxosConfiguration _configuration;
  private @Nullable List<Interface> _currentInterfaces;
  private @Nullable IntegerSpace _currentValidVlanRange;

  @SuppressWarnings("unused")
  private @Nullable List<Vlan> _currentVlans;

  private Vrf _currentVrf;
  private final CiscoNxosCombinedParser _parser;
  private @Nonnull final String _text;
  private @Nonnull final Warnings _w;

  public CiscoNxosControlPlaneExtractor(
      String text, CiscoNxosCombinedParser parser, Warnings warnings) {
    _text = text;
    _parser = parser;
    _w = warnings;
  }

  private boolean checkPortChannelCompatibilitySettings(Interface referenceIface, Interface iface) {
    return Objects.equals(iface.getAccessVlan(), referenceIface.getAccessVlan())
        && Objects.equals(iface.getAllowedVlans(), referenceIface.getAllowedVlans())
        && Objects.equals(iface.getNativeVlan(), referenceIface.getNativeVlan())
        && iface.getSwitchportMode() == referenceIface.getSwitchportMode();
  }

  /**
   * Clears layer-3 configuration of an interface to enable safe assignment to a new VRF.
   *
   * <p>NX-OS switches clear all layer-3 configuration from interfaces when an interface is assigned
   * to a VRF, presumably to prevent accidental leakage of any connected routes from the old VRF
   * into the new one.
   */
  private void clearLayer3Configuration(Interface iface) {
    iface.setAddress(null);
    iface.getSecondaryAddresses().clear();
  }

  private @Nonnull String convErrorMessage(Class<?> type, ParserRuleContext ctx) {
    return String.format("Could not convert to %s: %s", type.getSimpleName(), getFullText(ctx));
  }

  private @Nullable <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, @Nullable U defaultReturnValue) {
    _w.redFlag(convErrorMessage(returnType, ctx));
    return defaultReturnValue;
  }

  private void copyPortChannelCompatibilitySettings(Interface referenceIface, Interface iface) {
    iface.setAccessVlan(referenceIface.getAccessVlan());
    iface.setAllowedVlans(referenceIface.getAllowedVlans());
    iface.setNativeVlan(referenceIface.getNativeVlan());
    iface.setSwitchportMode(referenceIface.getSwitchportMode());
  }

  @Override
  public void enterCisco_nxos_configuration(Cisco_nxos_configurationContext ctx) {
    _configuration = new CiscoNxosConfiguration();
    _currentValidVlanRange = VLAN_RANGE.difference(_configuration.getReservedVlanRange());
  }

  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    int line = ctx.getStart().getLine();
    String declaredName = getFullText(ctx.irange);
    String prefix = ctx.irange.iname.prefix.getText();
    CiscoNxosInterfaceType type = toType(ctx.irange.iname.prefix);
    if (type == null) {
      _w.redFlag(String.format("Unsupported interface type: %s", prefix));
      _currentInterfaces = ImmutableList.of();
      return;
    }
    String canonicalPrefix = getCanonicalInterfaceNamePrefix(prefix);
    if (canonicalPrefix == null) {
      _w.redFlag(String.format("Unsupported interface name/range: %s", declaredName));
      _currentInterfaces = ImmutableList.of();
      return;
    }
    String middle = ctx.irange.iname.middle != null ? ctx.irange.iname.middle.getText() : "";
    String parentSuffix =
        ctx.irange.iname.parent_suffix != null ? ctx.irange.iname.parent_suffix.getText() : "";
    String lead = String.format("%s%s%s", canonicalPrefix, middle, parentSuffix);
    String parentInterface =
        parentSuffix.isEmpty()
            ? null
            : String.format(
                "%s%s%s", canonicalPrefix, middle, ctx.irange.iname.parent_suffix.num.getText());
    int first = toInteger(ctx.irange.iname.first);
    int last = ctx.irange.last != null ? toInteger(ctx.irange.last) : first;

    // flip first and last if range is backwards
    if (last < first) {
      int tmp = last;
      last = first;
      first = tmp;
    }

    // disallow subinterfaces except for physical and port-channel interfaces
    if (type != ETHERNET && type != PORT_CHANNEL && parentInterface != null) {
      _w.redFlag(
          String.format(
              "Cannot construct subinterface for interface type '%s' in: %s",
              type, getFullText(ctx)));
      _currentInterfaces = ImmutableList.of();
      return;
    }

    // Validate VLAN numbers
    if (type == VLAN
        && !_currentValidVlanRange.contains(IntegerSpace.of(Range.closed(first, last)))) {
      _w.redFlag(
          String.format(
              "Vlan number(s) outside of range %s in Vlan interface declaration: %s",
              _currentValidVlanRange, getFullText(ctx)));
      _currentInterfaces = ImmutableList.of();
      return;
    }

    // Validate port-channel numbers
    if (type == PORT_CHANNEL
        && !PORT_CHANNEL_RANGE.contains(IntegerSpace.of(Range.closed(first, last)))) {
      _w.redFlag(
          String.format(
              "port-channel number(s) outside of range %s in port-channel interface declaration: %s",
              PORT_CHANNEL_RANGE, getFullText(ctx)));
      _currentInterfaces = ImmutableList.of();
      return;
    }

    _currentInterfaces =
        IntStream.range(first, last + 1)
            .mapToObj(
                i -> {
                  String ifaceName = lead + i;
                  return _configuration
                      .getInterfaces()
                      .computeIfAbsent(
                          ifaceName,
                          n -> {
                            _configuration.defineStructure(
                                CiscoNxosStructureType.INTERFACE, n, line);
                            _configuration.referenceStructure(
                                CiscoNxosStructureType.INTERFACE,
                                n,
                                CiscoNxosStructureUsage.INTERFACE_SELF_REFERENCE,
                                line);
                            if (type == VLAN) {
                              _configuration.referenceStructure(
                                  CiscoNxosStructureType.VLAN,
                                  Integer.toString(i),
                                  CiscoNxosStructureUsage.INTERFACE_VLAN,
                                  line);
                              return newVlanInterface(n, i);
                            } else {
                              if (type == PORT_CHANNEL) {
                                _configuration.defineStructure(
                                    CiscoNxosStructureType.PORT_CHANNEL, n, line);
                              }
                              return newNonVlanInterface(n, parentInterface, type);
                            }
                          });
                })
            .collect(ImmutableList.toImmutableList());
    _currentInterfaces.forEach(i -> i.getDeclaredNames().add(declaredName));
  }

  @Override
  public void enterS_vrf_context(S_vrf_contextContext ctx) {
    String name = toVrfName(ctx, ctx.name);
    if (name == null) {
      _currentVrf = new Vrf("dummy");
      return;
    }
    _currentVrf =
        _configuration
            .getVrfs()
            .computeIfAbsent(
                name,
                n -> {
                  _configuration.defineStructure(
                      CiscoNxosStructureType.VRF, name, ctx.getStart().getLine());
                  return new Vrf(n);
                });
  }

  @Override
  public void enterVlan_vlan(Vlan_vlanContext ctx) {
    IntegerSpace vlans = toVlanIdRange(ctx, ctx.vlans);
    int line = ctx.getStart().getLine();
    if (vlans == null) {
      _currentVlans = ImmutableList.of();
      return;
    }
    _currentVlans =
        vlans.stream()
            .map(
                vlanId ->
                    _configuration
                        .getVlans()
                        .computeIfAbsent(
                            vlanId,
                            id -> {
                              _configuration.defineStructure(
                                  CiscoNxosStructureType.VLAN, Integer.toString(id), line);
                              return new Vlan(id);
                            }))
            .collect(ImmutableList.toImmutableList());
  }

  @Override
  public void exitI_bandwidth(I_bandwidthContext ctx) {
    Integer bandwidth = toBandwidth(ctx, ctx.bw);
    if (bandwidth == null) {
      return;
    }
    _currentInterfaces.forEach(iface -> iface.setBandwidth(bandwidth));
  }

  @Override
  public void exitI_channel_group(I_channel_groupContext ctx) {
    int line = ctx.getStart().getLine();
    String channelGroup = toPortChannel(ctx, ctx.id);
    if (channelGroup == null) {
      return;
    }
    // To be added to a channel-group, all interfaces in range must be:
    // - compatible with each other
    // - compatible with the port-channel if it already exists
    // If the port-channel does not exist, it is created with compatible settings.

    // However, if force flag is set, then compatibility is forced as follows:
    // - If port-channel already exists, all interfaces in range copy its settings
    // - Otherwise, the new port-channel and interfaces beyond the first copy settings
    //   from the first interface in the range.

    boolean channelExists = _configuration.getInterfaces().containsKey(channelGroup);
    boolean force = ctx.force != null;

    _configuration.referenceStructure(
        CiscoNxosStructureType.PORT_CHANNEL,
        channelGroup,
        CiscoNxosStructureUsage.INTERFACE_CHANNEL_GROUP,
        line);

    if (_currentInterfaces.isEmpty()) {
      // Stop now, since later logic requires non-empty list
      return;
    }

    Interface referenceIface =
        channelExists
            ? _configuration.getInterfaces().get(channelGroup)
            : _currentInterfaces.iterator().next();

    if (!force) {
      Optional<Interface> incompatibleInterface =
          _currentInterfaces.stream()
              .filter(iface -> iface != referenceIface)
              .filter(iface -> !checkPortChannelCompatibilitySettings(referenceIface, iface))
              .findFirst();
      if (incompatibleInterface.isPresent()) {
        _w.redFlag(
            String.format(
                "Cannot set channel-group because interface '%s' has settings that do not conform to those of interface '%s' in: %s",
                incompatibleInterface.get().getName(), referenceIface.getName(), getFullText(ctx)));
        return;
      }
    }

    Interface portChannelIface;
    if (channelExists) {
      portChannelIface = referenceIface;
    } else {
      portChannelIface =
          newNonVlanInterface(channelGroup, null, CiscoNxosInterfaceType.PORT_CHANNEL);
      copyPortChannelCompatibilitySettings(referenceIface, portChannelIface);
      _configuration.getInterfaces().put(channelGroup, portChannelIface);
      _configuration.defineStructure(CiscoNxosStructureType.INTERFACE, channelGroup, line);
      _configuration.referenceStructure(
          CiscoNxosStructureType.INTERFACE,
          channelGroup,
          CiscoNxosStructureUsage.INTERFACE_SELF_REFERENCE,
          line);
      _configuration.defineStructure(CiscoNxosStructureType.PORT_CHANNEL, channelGroup, line);
    }

    _currentInterfaces.forEach(
        iface -> {
          iface.setChannelGroup(channelGroup);
          copyPortChannelCompatibilitySettings(referenceIface, iface);
        });
  }

  @Override
  public void exitI_encapsulation(I_encapsulationContext ctx) {
    Integer vlanId = toVlanId(ctx, ctx.vlan);
    if (vlanId == null) {
      return;
    }
    _currentInterfaces.forEach(iface -> iface.setEncapsulationVlan(vlanId));
  }

  @Override
  public void exitI_ip_address(I_ip_addressContext ctx) {
    InterfaceAddress address = toInterfaceAddress(ctx.addr);
    if (ctx.SECONDARY() != null) {
      // secondary addresses are appended
      _currentInterfaces.forEach(iface -> iface.getSecondaryAddresses().add(address));
    } else {
      // primary address is replaced
      _currentInterfaces.forEach(iface -> iface.setAddress(address));
    }
  }

  @Override
  public void exitI_no_autostate(I_no_autostateContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setAutostate(false));
  }

  @Override
  public void exitI_no_shutdown(I_no_shutdownContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setShutdown(false));
  }

  @Override
  public void exitI_no_switchport(I_no_switchportContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(SwitchportMode.NONE));
  }

  @Override
  public void exitI_shutdown(I_shutdownContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setShutdown(true));
  }

  @Override
  public void exitI_switchport_access(I_switchport_accessContext ctx) {
    Integer vlanId = toVlanId(ctx, ctx.vlan);
    if (vlanId == null) {
      return;
    }
    _currentInterfaces.forEach(
        iface -> {
          iface.setSwitchportMode(SwitchportMode.ACCESS);
          iface.setAccessVlan(vlanId);
        });
  }

  @Override
  public void exitI_switchport_trunk_allowed(I_switchport_trunk_allowedContext ctx) {
    IntegerSpace vlans;
    if (ctx.vlans != null) {
      vlans = ctx.vlans != null ? toVlanIdRange(ctx, ctx.vlans) : null;
      if (vlans == null) {
        // invalid VLAN in range
        return;
      }
    } else if (ctx.except != null) {
      Integer except = toVlanId(ctx, ctx.except);
      if (except == null) {
        // invalid VLAN to exclude
        return;
      }
      vlans = _currentValidVlanRange.difference(IntegerSpace.of(except));
    } else if (ctx.NONE() != null) {
      vlans = IntegerSpace.EMPTY;
    } else {
      todo(ctx);
      return;
    }
    _currentInterfaces.forEach(
        iface -> {
          iface.setSwitchportMode(SwitchportMode.TRUNK);
          if (ctx.ADD() != null) {
            iface.setAllowedVlans(iface.getAllowedVlans().union(vlans));
          } else if (ctx.REMOVE() != null) {
            iface.setAllowedVlans(iface.getAllowedVlans().difference(vlans));
          } else {
            iface.setAllowedVlans(vlans);
          }
        });
  }

  @Override
  public void exitI_switchport_trunk_native(I_switchport_trunk_nativeContext ctx) {
    Integer vlanId = toVlanId(ctx, ctx.vlan);
    if (vlanId == null) {
      return;
    }
    _currentInterfaces.forEach(
        iface -> {
          iface.setSwitchportMode(SwitchportMode.TRUNK);
          iface.setNativeVlan(vlanId);
        });
  }

  @Override
  public void exitI_vrf_member(I_vrf_memberContext ctx) {
    String name = toVrfName(ctx, ctx.name);
    if (name == null) {
      return;
    }
    if (_currentInterfaces.stream()
        .anyMatch(iface -> iface.getSwitchportMode() != SwitchportMode.NONE)) {
      _w.redFlag(
          String.format("Cannot assign VRF to switchport interface(s) in: %s", getFullText(ctx)));
      return;
    }
    _configuration.referenceStructure(
        CiscoNxosStructureType.VRF,
        name,
        CiscoNxosStructureUsage.INTERFACE_VRF_MEMBER,
        ctx.getStart().getLine());
    _currentInterfaces.forEach(
        iface -> {
          clearLayer3Configuration(iface);
          iface.setVrfMember(name);
        });
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _configuration.setHostname(ctx.hostname.getText());
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    _currentInterfaces = null;
  }

  @Override
  public void exitS_vrf_context(S_vrf_contextContext ctx) {
    _currentVrf = null;
  }

  @Override
  public void exitVc_no_shutdown(Vc_no_shutdownContext ctx) {
    _currentVrf.setShutdown(false);
  }

  @Override
  public void exitVc_shutdown(Vc_shutdownContext ctx) {
    _currentVrf.setShutdown(true);
  }

  @Override
  public void exitVlan_vlan(Vlan_vlanContext ctx) {
    _currentVlans = null;
  }

  private @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    return _text.substring(start, end + 1);
  }

  @Override
  public @Nullable VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(this, tree);
  }

  private @Nullable Integer toBandwidth(
      ParserRuleContext messageCtx, Interface_bandwidth_kbpsContext ctx) {
    int bandwidth = Integer.parseInt(ctx.getText());
    if (!BANDWIDTH_RANGE.contains(bandwidth)) {
      _w.redFlag(
          String.format(
              "Expected bandwidth in range %s, but got '%d' in: %s",
              BANDWIDTH_RANGE, bandwidth, getFullText(messageCtx)));
      return null;
    }
    return bandwidth;
  }

  private void todo(ParserRuleContext ctx) {
    _w.todo(ctx, getFullText(ctx), _parser);
  }

  private @Nullable String toPortChannel(ParserRuleContext messageCtx, Channel_idContext ctx) {
    int id = Integer.parseInt(ctx.getText());
    // not a mistake; range is 1-4096 (not zero-based).
    if (!PORT_CHANNEL_RANGE.contains(id)) {
      _w.redFlag(
          String.format(
              "Expected port-channel id in range %s, but got '%d' in: %s",
              PORT_CHANNEL_RANGE, id, getFullText(messageCtx)));
      return null;
    }
    return "port-channel" + id;
  }

  private @Nullable CiscoNxosInterfaceType toType(Interface_prefixContext ctx) {
    if (ctx.ETHERNET() != null) {
      return ETHERNET;
    } else if (ctx.LOOPBACK() != null) {
      return LOOPBACK;
    } else if (ctx.MGMT() != null) {
      return MGMT;
    } else if (ctx.PORT_CHANNEL() != null) {
      return PORT_CHANNEL;
    } else if (ctx.VLAN() != null) {
      return VLAN;
    }
    return convProblem(CiscoNxosInterfaceType.class, ctx, null);
  }

  private @Nullable Integer toVlanId(ParserRuleContext messageCtx, Vlan_idContext ctx) {
    int vlan = Integer.parseInt(ctx.getText());
    if (!_currentValidVlanRange.contains(vlan)) {
      _w.redFlag(
          String.format(
              "Expected VLAN in range %s, but got '%d' in: %s",
              _currentValidVlanRange, vlan, getFullText(messageCtx)));
      return null;
    }
    return vlan;
  }

  private @Nullable IntegerSpace toVlanIdRange(
      ParserRuleContext messageCtx, Vlan_id_rangeContext ctx) {
    String rangeText = ctx.getText();
    IntegerSpace value = IntegerSpace.parse(rangeText);
    if (!_currentValidVlanRange.contains(value)) {
      _w.redFlag(
          String.format(
              "Expected VLANs in range %s, but got '%s' in: %s",
              _currentValidVlanRange, rangeText, getFullText(messageCtx)));
      return null;
    }
    return value;
  }

  private @Nullable String toVrfName(ParserRuleContext messageCtx, Vrf_nameContext ctx) {
    String name = ctx.getText();
    if (name.length() > MAX_VRF_NAME_LENGTH) {
      _w.redFlag(
          String.format(
              "VRF name cannot exceed %d chars, but was '%s' in: %s",
              MAX_VRF_NAME_LENGTH, name, getFullText(messageCtx)));
      return null;
    }
    // Case-insensitive, so just canonicalize as lower-case
    return name.toLowerCase();
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _configuration.setUnrecognized(true);

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
}
