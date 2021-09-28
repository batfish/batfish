package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

/** Datamodel class representing an A10 device configuration. */
public final class A10Configuration extends VendorConfiguration {

  public A10Configuration() {
    _interfacesEthernet = new HashMap<>();
    _interfacesLoopback = new HashMap<>();
    _interfacesTrunk = new HashMap<>();
    _interfacesVe = new HashMap<>();
    _vlans = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<Integer, Interface> getInterfacesEthernet() {
    return _interfacesEthernet;
  }

  public Map<Integer, Interface> getInterfacesLoopback() {
    return _interfacesLoopback;
  }

  public Map<Integer, TrunkInterface> getInterfacesTrunk() {
    return _interfacesTrunk;
  }

  public Map<Integer, Interface> getInterfacesVe() {
    return _interfacesVe;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @VisibleForTesting
  public static boolean getInterfaceEnabledEffective(Interface iface) {
    Boolean enabled = iface.getEnabled();
    if (enabled != null) {
      return enabled;
    }
    switch (iface.getType()) {
      case ETHERNET:
        return false;
      case LOOPBACK:
      case TRUNK:
      case VE:
        return true;
      default:
        assert false;
        return true;
    }
  }

  @VisibleForTesting
  public static int getInterfaceMtuEffective(Interface iface) {
    return firstNonNull(iface.getMtu(), DEFAULT_MTU);
  }

  public Map<Integer, Vlan> getVlans() {
    return _vlans;
  }

  public static InterfaceType getInterfaceType(Interface iface) {
    switch (iface.getType()) {
      case ETHERNET:
        return InterfaceType.PHYSICAL;
      case LOOPBACK:
        return InterfaceType.LOOPBACK;
      case VE:
        return InterfaceType.VLAN;
      case TRUNK:
        return InterfaceType.AGGREGATED;
      default:
        assert false;
        return InterfaceType.UNKNOWN;
    }
  }

  @Nonnull
  public static String getInterfaceName(InterfaceReference ref) {
    return getInterfaceName(ref.getType(), ref.getNumber());
  }

  @Nonnull
  public static String getInterfaceName(Interface iface) {
    return getInterfaceName(iface.getType(), iface.getNumber());
  }

  @Nonnull
  public static String getInterfaceName(Interface.Type type, int num) {
    if (type == Interface.Type.VE) {
      return String.format("VirtualEthernet %s", num);
    }

    String typeStr = type.toString();
    // Only the first letter should be capitalized, like in A10 `show` data
    return String.format(
        "%s%s %s", typeStr.substring(0, 1), typeStr.substring(1).toLowerCase(), num);
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setHumanName(hostname);
    _c.setDeviceModel(DeviceModel.A10);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Generated default VRF
    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    _c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, vrf));

    _ifaceNametoIface = new HashMap<>();
    _interfacesLoopback.forEach(
        (num, iface) -> {
          convertInterface(iface, vrf);
          _ifaceNametoIface.put(getInterfaceName(iface), iface);
        });
    _interfacesEthernet.forEach(
        (num, iface) -> {
          convertInterface(iface, vrf);
          _ifaceNametoIface.put(getInterfaceName(iface), iface);
        });
    _interfacesVe.forEach(
        (num, iface) -> {
          convertInterface(iface, vrf);
          _ifaceNametoIface.put(getInterfaceName(iface), iface);
        });
    _interfacesTrunk.forEach((num, iface) -> convertInterface(iface, vrf));

    markStructures();
    return ImmutableList.of(_c);
  }

  private void markStructures() {
    A10StructureType.CONCRETE_STRUCTURES.forEach(this::markConcreteStructure);
    A10StructureType.ABSTRACT_STRUCTURES.asMap().forEach(this::markAbstractStructureAllUsages);
  }

  /**
   * Convert specified VS {@link Interface} in provided {@link Vrf} to a VI model {@link
   * org.batfish.datamodel.Interface} attached to the VI {@link Configuration}.
   */
  private void convertInterface(Interface iface, Vrf vrf) {
    String name = getInterfaceName(iface);
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setActive(getInterfaceEnabledEffective(iface))
            .setAddress(iface.getIpAddress())
            .setMtu(getInterfaceMtuEffective(iface))
            .setType(getInterfaceType(iface))
            .setName(name)
            .setVrf(vrf)
            .setOwner(_c);
    ImmutableList.Builder<String> names = ImmutableList.<String>builder().add(name);
    if (iface.getName() != null && !name.equals(iface.getName())) {
      names.add(iface.getName());
    }
    newIface.setDeclaredNames(names.build());

    // VLANs
    boolean hasVlanSettings = setVlanSettings(iface, newIface);

    // Aggregates and members - must happen after initial VLAN settings are set
    if (iface instanceof TrunkInterface) {
      TrunkInterface trunkIface = (TrunkInterface) iface;
      String trunkName = getInterfaceName(iface);
      ImmutableSet<String> memberNames =
          trunkIface.getMembers().stream()
              .map(A10Configuration::getInterfaceName)
              .filter(
                  memberName -> {
                    boolean ifaceExists = _ifaceNametoIface.containsKey(memberName);
                    if (!ifaceExists) {
                      // Cannot tell if this missing member would invalidate other members or not
                      // So, optimistically leave other members
                      _w.redFlag(
                          String.format(
                              "Trunk member %s does not exist, cannot add to %s",
                              memberName, trunkName));
                    }
                    return ifaceExists;
                  })
              .collect(ImmutableSet.toImmutableSet());
      if (memberNames.isEmpty()) {
        _w.redFlag(
            String.format(
                "Trunk %s does not contain any member interfaces", trunkIface.getNumber()));
      } else {
        newIface.setChannelGroupMembers(memberNames);
        newIface.setDependencies(
            memberNames.stream()
                .map(
                    member ->
                        new org.batfish.datamodel.Interface.Dependency(
                            member, org.batfish.datamodel.Interface.DependencyType.AGGREGATE))
                .collect(ImmutableSet.toImmutableSet()));

        // If this trunk doesn't have VLAN settings configured directly (e.g. ACOS v2), inherit
        // settings
        if (!hasVlanSettings) {
          if (vlanSettingsDifferent(memberNames)) {
            _w.redFlag(
                String.format(
                    "VLAN settings for members of %s are different, ignoring their VLAN settings",
                    trunkName));
          } else {
            // All members have the same VLAN settings, so just use the first
            String firstMemberName = memberNames.iterator().next();
            setVlanSettings(_ifaceNametoIface.get(firstMemberName), newIface);
          }
        } else {
          org.batfish.datamodel.Interface.Builder dummy = org.batfish.datamodel.Interface.builder();
          if (memberNames.stream()
              .anyMatch(memberName -> setVlanSettings(_ifaceNametoIface.get(memberName), dummy))) {
            _w.redFlag(
                String.format(
                    "Cannot configure VLAN settings on %s as well as its members. Member VLAN"
                        + " settings will be ignored.",
                    trunkName));
          }
        }
      }
    }
    if (iface.getType() == Interface.Type.ETHERNET) {
      InterfaceReference ifaceRef = new InterfaceReference(iface.getType(), iface.getNumber());
      _interfacesTrunk.values().stream()
          .filter(t -> t.getMembers().contains(ifaceRef))
          .findFirst()
          .ifPresent(
              t -> {
                newIface.setChannelGroup(getInterfaceName(Interface.Type.TRUNK, t.getNumber()));
                // TODO determine if switchport settings need to be propagated to member interfaces
              });
    }

    newIface.build();
  }

  /**
   * Check if any VLAN settings for {@link Interface}s (specified by name) are different.
   *
   * <p>All specified interface names must correspond to existent interfaces, in the {@code
   * _ifaceNameToIface} map.
   */
  private boolean vlanSettingsDifferent(Collection<String> names) {
    org.batfish.datamodel.Interface.Builder baseIface =
        org.batfish.datamodel.Interface.builder().setName("");
    Set<org.batfish.datamodel.Interface> distinctVlanSettings =
        names.stream()
            .map(
                name -> {
                  setVlanSettings(_ifaceNametoIface.get(name), baseIface);
                  return baseIface.build();
                })
            .collect(ImmutableSet.toImmutableSet());
    return distinctVlanSettings.size() > 1;
  }

  /**
   * Set VLAN settings of the specified VI {@link org.batfish.datamodel.Interface.Builder} based on
   * the specified {@link Interface}. Returns a boolean indicating if VLAN settings exist for this
   * interface.
   */
  private boolean setVlanSettings(
      Interface iface, org.batfish.datamodel.Interface.Builder viIface) {
    boolean hasVlanSettings = false;
    viIface.setSwitchportMode(SwitchportMode.NONE);
    List<Vlan> taggedVlans = getTaggedVlans(iface);
    Optional<Vlan> untaggedVlan = getUntaggedVlan(iface);
    if (untaggedVlan.isPresent()) {
      viIface.setSwitchportMode(SwitchportMode.TRUNK);
      viIface.setSwitchport(true);
      viIface.setNativeVlan(untaggedVlan.get().getNumber());
      hasVlanSettings = true;
    }
    if (!taggedVlans.isEmpty()) {
      viIface.setSwitchportMode(SwitchportMode.TRUNK);
      viIface.setSwitchport(true);
      viIface.setAllowedVlans(
          IntegerSpace.unionOfSubRanges(
              taggedVlans.stream()
                  .map(v -> new SubRange(v.getNumber()))
                  .collect(ImmutableList.toImmutableList())));
      hasVlanSettings = true;
    }
    if (iface.getType() == Interface.Type.VE) {
      viIface.setVlan(iface.getNumber());
      hasVlanSettings = true;
    }
    return hasVlanSettings;
  }

  /** Get the untagged VLAN for the specified interface, if one exists. */
  public Optional<Vlan> getUntaggedVlan(Interface iface) {
    InterfaceReference ref = new InterfaceReference(iface.getType(), iface.getNumber());
    return _vlans.values().stream().filter(v -> v.getUntagged().contains(ref)).findFirst();
  }

  /** Returns all VLANs associated with the specified tagged interface. */
  private List<Vlan> getTaggedVlans(Interface iface) {
    InterfaceReference ref = new InterfaceReference(iface.getType(), iface.getNumber());
    return _vlans.values().stream()
        .filter(v -> v.getTagged().contains(ref))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Finalize configuration after it is finished being built. Does things like making structures
   * immutable.
   *
   * <p>This should only be called once, at the end of parsing and extraction.
   */
  public void finalizeStructures() {
    _interfacesEthernet = ImmutableMap.copyOf(_interfacesEthernet);
    _interfacesLoopback = ImmutableMap.copyOf(_interfacesLoopback);
    _interfacesVe = ImmutableMap.copyOf(_interfacesVe);
    _interfacesTrunk = ImmutableMap.copyOf(_interfacesTrunk);
    _vlans = ImmutableMap.copyOf(_vlans);
  }

  /** Map of interface names to interface. Used for converting aggregate interfaces. */
  private transient Map<String, Interface> _ifaceNametoIface;

  private Configuration _c;
  private String _hostname;
  private Map<Integer, Interface> _interfacesEthernet;
  private Map<Integer, Interface> _interfacesLoopback;
  private Map<Integer, TrunkInterface> _interfacesTrunk;
  private Map<Integer, Interface> _interfacesVe;
  private Map<Integer, Vlan> _vlans;
  private ConfigurationFormat _vendor;
}
