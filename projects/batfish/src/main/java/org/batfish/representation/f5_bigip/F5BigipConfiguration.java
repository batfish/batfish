package org.batfish.representation.f5_bigip;

import static com.google.common.base.Predicates.notNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific configuration for F5 BIG-IP device */
@ParametersAreNonnullByDefault
public class F5BigipConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Map<String, BgpProcess> _bgpProcesses;
  private transient Configuration _c;
  private ConfigurationFormat _format;
  private String _hostname;
  private final @Nonnull Map<String, Interface> _interfaces;
  private final @Nonnull Map<String, PrefixList> _prefixLists;
  private final @Nonnull Map<String, RouteMap> _routeMaps;
  private final @Nonnull Map<String, Self> _selves;
  private final @Nonnull Map<String, Vlan> _vlans;

  public F5BigipConfiguration() {
    _bgpProcesses = new HashMap<>();
    _interfaces = new HashMap<>();
    _prefixLists = new HashMap<>();
    _routeMaps = new HashMap<>();
    _selves = new HashMap<>();
    _vlans = new HashMap<>();
  }

  public @Nonnull Map<String, BgpProcess> getBgpProcesses() {
    return _bgpProcesses;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public @Nonnull Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull Map<String, PrefixList> getPrefixLists() {
    return _prefixLists;
  }

  public @Nonnull Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  public @Nonnull Map<String, Self> getSelves() {
    return _selves;
  }

  public @Nonnull Map<String, Vlan> getVlans() {
    return _vlans;
  }

  private void markStructures() {
    markConcreteStructure(
        F5BigipStructureType.BGP_PROCESS, F5BigipStructureUsage.BGP_PROCESS_SELF_REFERENCE);
    markConcreteStructure(
        F5BigipStructureType.INTERFACE,
        F5BigipStructureUsage.INTERFACE_SELF_REFERENCE,
        F5BigipStructureUsage.BGP_NEIGHBOR_UPDATE_SOURCE,
        F5BigipStructureUsage.VLAN_INTERFACE);
    markConcreteStructure(
        F5BigipStructureType.PREFIX_LIST,
        F5BigipStructureUsage.ROUTE_MAP_MATCH_IPV4_ADDRESS_PREFIX_LIST);
    markConcreteStructure(
        F5BigipStructureType.ROUTE_MAP,
        F5BigipStructureUsage.BGP_ADDRESS_FAMILY_REDISTRIBUTE_KERNEL_ROUTE_MAP,
        F5BigipStructureUsage.BGP_NEIGHBOR_IPV4_ROUTE_MAP_OUT,
        F5BigipStructureUsage.BGP_NEIGHBOR_IPV6_ROUTE_MAP_OUT);
    markConcreteStructure(F5BigipStructureType.SELF, F5BigipStructureUsage.SELF_SELF_REFERENCE);
    markConcreteStructure(F5BigipStructureType.VLAN, F5BigipStructureUsage.SELF_VLAN);
  }

  private void processSelf(Self self) {
    // Add addresses to appropriate VLAN interfaces.
    String vlanName = self.getVlan();
    if (vlanName == null) {
      return;
    }
    org.batfish.datamodel.Interface vlanIface = _c.getAllInterfaces().get(vlanName);
    if (vlanIface == null) {
      return;
    }
    InterfaceAddress address = self.getAddress();
    vlanIface.setAddress(address);
    vlanIface.setAllAddresses(
        address != null ? ImmutableSortedSet.of(address) : ImmutableSortedSet.of());
  }

  private void processVlanSettings(Vlan vlan) {
    // Configure interface switchport parameters.
    Integer tag = vlan.getTag();
    if (tag == null) {
      return;
    }
    _interfaces.keySet().stream()
        .map(ifaceName -> _c.getAllInterfaces().get(ifaceName))
        .filter(notNull())
        .forEach(
            iface -> {
              iface.setSwitchport(true);
              iface.setSwitchportMode(SwitchportMode.TRUNK);
              // TODO: something else for configs with no concept of native VLAN
              iface.setNativeVlan(null);
              iface.setAllowedVlans(iface.getAllowedVlans().union(IntegerSpace.of(tag)));
            });
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _format = format;
  }

  private @Nonnull List<Statement> toActions(RouteMapEntry entry) {
    ImmutableList.Builder<Statement> builder = ImmutableList.builder();
    entry.getSets().flatMap(set -> set.toStatements(_c, this, _w)).forEach(builder::add);
    return builder.add(toStatement(entry.getAction())).build();
  }

  private @Nonnull BooleanExpr toGuard(RouteMapEntry entry) {
    return new Conjunction(
        entry
            .getMatches()
            .map(match -> match.toBooleanExpr(_c, this, _w))
            .collect(ImmutableList.toImmutableList()));
  }

  private @Nonnull org.batfish.datamodel.Interface toInterface(Interface iface) {
    org.batfish.datamodel.Interface newIface =
        new org.batfish.datamodel.Interface(iface.getName(), _c);
    Double speed = iface.getSpeed();
    newIface.setSpeed(speed);
    newIface.setBandwidth(firstNonNull(iface.getBandwidth(), speed, Interface.DEFAULT_BANDWIDTH));
    // Assume all interfaces are in default VRF for now
    newIface.setVrf(_c.getDefaultVrf());
    return newIface;
  }

  private @Nonnull org.batfish.datamodel.Interface toInterface(Vlan vlan) {
    org.batfish.datamodel.Interface newIface =
        new org.batfish.datamodel.Interface(vlan.getName(), _c, InterfaceType.VLAN);
    // TODO: Possibly add dependencies on ports allowing this VLAN
    newIface.setVlan(vlan.getTag());
    newIface.setBandwidth(Interface.DEFAULT_BANDWIDTH);
    newIface.setVrf(_c.getDefaultVrf());
    return newIface;
  }

  /**
   * Converts {@code prefixList} to {@link Route6FilterList}. If {@code prefixList} contains IPv4
   * information, returns {@code null}.
   */
  private @Nullable Route6FilterList toRoute6FilterList(PrefixList prefixList) {
    Collection<PrefixListEntry> entries = prefixList.getEntries().values();
    if (entries.stream().map(PrefixListEntry::getPrefix).anyMatch(Objects::nonNull)) {
      return null;
    }
    String name = prefixList.getName();
    Route6FilterList output = new Route6FilterList(name);
    entries.stream()
        .map(entry -> entry.toRoute6FilterLine(_w, name))
        .filter(Objects::nonNull)
        .forEach(output::addLine);
    return output;
  }

  /**
   * Converts {@code prefixList} to {@link RouteFilterList}. If {@code prefixList} contains IPv6
   * information, returns {@code null}.
   */
  private @Nullable RouteFilterList toRouteFilterList(PrefixList prefixList) {
    Collection<PrefixListEntry> entries = prefixList.getEntries().values();
    if (entries.stream().map(PrefixListEntry::getPrefix6).anyMatch(Objects::nonNull)) {
      return null;
    }
    String name = prefixList.getName();
    RouteFilterList output = new RouteFilterList(name);
    entries.stream()
        .map(entry -> entry.toRouteFilterLine(_w, name))
        .filter(Objects::nonNull)
        .forEach(output::addLine);
    return output;
  }

  private @Nonnull RoutingPolicy toRoutingPolicy(RouteMap routeMap) {
    RoutingPolicy.Builder builder =
        RoutingPolicy.builder().setName(routeMap.getName()).setOwner(_c);
    // Warn about entries missing an action.
    routeMap.getEntries().values().stream()
        .filter(entry -> entry.getAction() == null)
        .forEach(
            entry ->
                _w.redFlag(
                    String.format(
                        "route-map: '%s' entry '%d' has no action",
                        routeMap.getName(), entry.getNum())));
    routeMap.getEntries().values().stream()
        .filter(entry -> entry.getAction() != null)
        .map(entry -> toRoutingPolicyStatement(entry))
        .forEach(builder::addStatement);
    return builder.addStatement(Statements.ReturnFalse.toStaticStatement()).build();
  }

  private @Nonnull Statement toRoutingPolicyStatement(RouteMapEntry entry) {
    return new If(toGuard(entry), toActions(entry));
  }

  private @Nonnull Statement toStatement(LineAction action) {
    switch (action) {
      case PERMIT:
        return Statements.ReturnTrue.toStaticStatement();
      case DENY:
        return Statements.ReturnFalse.toStaticStatement();
      default:
        throw new IllegalArgumentException(String.format("Invalid action: %s", action));
    }
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    _c = new Configuration(_hostname, _format);

    // TODO: alter as behavior fleshed out
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Add default VRF
    _c.getVrfs().computeIfAbsent(DEFAULT_VRF_NAME, Vrf::new);

    // Add interfaces
    _interfaces.forEach(
        (name, iface) -> {
          org.batfish.datamodel.Interface newIface = toInterface(iface);
          _c.getAllInterfaces().put(name, newIface);
          // Assume all interfaces are in default VRF for now
          _c.getDefaultVrf().getInterfaces().put(name, newIface);
        });

    // Add VLAN interfaces
    _vlans.forEach(
        (name, vlan) -> {
          org.batfish.datamodel.Interface newIface = toInterface(vlan);
          _c.getAllInterfaces().put(name, newIface);
          // Assume all interfaces are in default VRF for now
          _c.getDefaultVrf().getInterfaces().put(name, newIface);
        });
    // Process vlans:
    _vlans.values().forEach(this::processVlanSettings);

    // Process selves:
    _selves.values().forEach(this::processSelf);

    // Convert valid IPv4 prefix-lists to RouteFilterLists
    _prefixLists.forEach(
        (name, prefixList) -> {
          RouteFilterList converted = toRouteFilterList(prefixList);
          if (converted != null) {
            _c.getRouteFilterLists().put(name, converted);
          }
        });

    // Convert valid IPv6 prefix-lists to Route6FilterLists
    _prefixLists.forEach(
        (name, prefixList) -> {
          Route6FilterList converted = toRoute6FilterList(prefixList);
          if (converted != null) {
            _c.getRoute6FilterLists().put(name, converted);
          }
        });

    // Warn about invalid prefix-lists
    _prefixLists.values().forEach(this::warnInvalidPrefixList);

    // Convert route-maps to RoutingPolicies
    _routeMaps.forEach(
        (name, routeMap) -> _c.getRoutingPolicies().put(name, toRoutingPolicy(routeMap)));

    markStructures();

    return _c;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  private void warnInvalidPrefixList(PrefixList prefixList) {
    if (prefixList.getEntries().values().stream()
        .anyMatch(entry -> entry.getPrefix() != null && entry.getPrefix6() != null)) {
      _w.redFlag(
          String.format(
              "prefix-list '%s' is invalid since it contains both IPv4 and IPv6 information",
              prefixList.getName()));
    }
  }
}
