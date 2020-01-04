package org.batfish.representation.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.batfish.representation.cumulus.CumulusInterfaceType.BOND_SUBINTERFACE;
import static org.batfish.representation.cumulus.CumulusInterfaceType.PHYSICAL;
import static org.batfish.representation.cumulus.CumulusInterfaceType.PHYSICAL_SUBINTERFACE;
import static org.batfish.representation.cumulus.CumulusStructureType.BOND;
import static org.batfish.representation.cumulus.CumulusStructureType.INTERFACE;
import static org.batfish.representation.cumulus.CumulusStructureType.VLAN;
import static org.batfish.representation.cumulus.CumulusStructureType.VRF;
import static org.batfish.representation.cumulus.CumulusStructureType.VXLAN;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;

// TODO: Remove this class entirely now that we don't convert from concatenated to NCLU */

/** Converter from cumulus interfaces file model to Cumulus VS model. */
public final class InterfaceConverter {
  @VisibleForTesting public static final String BRIDGE_NAME = "bridge";

  private static final Pattern ENCAPSULATION_VLAN_PATTERN = Pattern.compile("^.*\\.([0-9]+)$");

  private static final Pattern SUBINTERFACE_PATTERN = Pattern.compile("^(.*)\\.([0-9]+)$");

  public static final Set<String> DEFAULT_BRIDGE_PORTS = ImmutableSet.of();
  public static final int DEFAULT_BRIDGE_PVID = 1;

  private final CumulusInterfacesConfiguration _interfaces;
  private final Warnings _w;

  public InterfaceConverter(CumulusInterfacesConfiguration interfaces, Warnings w) {
    _interfaces = interfaces;
    _w = w;
  }

  /** Get Cumulus VS model {@link Bond Bonds}. */
  public Map<String, Bond> convertBonds() {
    return _interfaces.getInterfaces().values().stream()
        .filter(InterfaceConverter::isBond)
        .map(InterfaceConverter::convertBond)
        .collect(ImmutableMap.toImmutableMap(Bond::getName, Function.identity()));
  }

  @VisibleForTesting
  static Bond convertBond(InterfacesInterface bondIface) {
    Bond bond = new Bond(bondIface.getName());
    bond.setClagId(bondIface.getClagId());
    bond.setSlaves(bondIface.getBondSlaves());
    bond.setVrf(bondIface.getVrf());
    InterfaceBridgeSettings bridgeSettings = bondIface.getBridgeSettings();
    if (bridgeSettings != null) {
      InterfaceBridgeSettings bridge = bond.getBridge();
      bridge.setVids(bridgeSettings.getVids());
      Optional.ofNullable(bridgeSettings.getAccess()).ifPresent(bridge::setAccess);
      Optional.ofNullable(bridgeSettings.getPvid()).ifPresent(bridge::setPvid);
    }
    return bond;
  }

  /** Get Cumulus VS model {@link Bridge}. */
  public @Nullable Bridge convertBridge() {
    InterfacesInterface bridgeIface = _interfaces.getInterfaces().get(BRIDGE_NAME);
    if (bridgeIface == null) {
      return null;
    }
    Bridge bridge = new Bridge();
    bridge.setPorts(firstNonNull(bridgeIface.getBridgePorts(), DEFAULT_BRIDGE_PORTS));
    InterfaceBridgeSettings bridgeSettings = bridgeIface.getBridgeSettings();
    if (bridgeSettings != null) {
      bridge.setVids(bridgeSettings.getVids());
      bridge.setPvid(firstNonNull(bridgeSettings.getPvid(), DEFAULT_BRIDGE_PVID));
    }
    return bridge;
  }

  /** Get Cumulus VS model {@link org.batfish.representation.cumulus.Interface interfaces}. */
  public Map<String, org.batfish.representation.cumulus.Interface> convertInterfaces() {
    return _interfaces.getInterfaces().values().stream()
        .filter(InterfaceConverter::isInterface)
        // interface bridge is handled by convertBridge
        .filter(iface -> !iface.getName().equals(BRIDGE_NAME))
        .map(
            iface -> {
              try {
                return convertInterface(iface);
              } catch (BatfishException e) {
                _w.redFlag(
                    String.format(
                        "failed to convert interface %s to VS model: %s",
                        iface.getName(), e.getMessage()));
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(
            toImmutableMap(
                org.batfish.representation.cumulus.Interface::getName, Function.identity()));
  }

  /** Get Cumulus VS model {@link Vlan Vlans}. */
  public Map<String, Vlan> convertVlans() {
    return _interfaces.getInterfaces().values().stream()
        .filter(InterfaceConverter::isVlan)
        .map(InterfaceConverter::convertVlan)
        .collect(toImmutableMap(Vlan::getName, Function.identity()));
  }

  /** Get Cumulus VS model {@link Vrf Vrfs}. */
  public Map<String, Vrf> convertVrfs() {
    return _interfaces.getInterfaces().values().stream()
        .filter(InterfaceConverter::isVrf)
        .map(InterfaceConverter::convertVrf)
        .collect(toImmutableMap(Vrf::getName, Function.identity()));
  }

  /** Get Cumulus VS model {@link Vxlan Vxlans}. */
  public Map<String, Vxlan> convertVxlans() {
    return _interfaces.getInterfaces().values().stream()
        .filter(InterfaceConverter::isVxlan)
        .map(InterfaceConverter::convertVxlan)
        .collect(toImmutableMap(Vxlan::getName, Function.identity()));
  }

  @VisibleForTesting
  CumulusInterfaceType getInterfaceType(InterfacesInterface iface) {
    String name = iface.getName();
    if (InterfacesInterface.isPhysicalInterfaceType(name)) {
      return CumulusInterfaceType.PHYSICAL;
    }

    String superInterfaceName = getSuperInterfaceName(iface.getName());
    if (superInterfaceName == null) {
      throw new BatfishException("cannot determine interface type for " + name);
    }

    InterfacesInterface superIface = _interfaces.getInterfaces().get(superInterfaceName);
    if (superIface == null) {
      throw new BatfishException("missing superinterface of subinterface " + name);
    } else if (superIface.getType() == BOND) {
      return BOND_SUBINTERFACE;
    } else if (superIface.getType() == INTERFACE && getInterfaceType(superIface) == PHYSICAL) {
      return PHYSICAL_SUBINTERFACE;
    } else {
      throw new BatfishException("invalid superinterface of subinterface " + name);
    }
  }

  @VisibleForTesting
  @Nullable
  public static String getSuperInterfaceName(String ifaceName) {
    Matcher matcher = SUBINTERFACE_PATTERN.matcher(ifaceName);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
  }

  @VisibleForTesting
  @Nullable
  public static Integer getEncapsulationVlan(InterfacesInterface iface) {
    Matcher matcher = ENCAPSULATION_VLAN_PATTERN.matcher(iface.getName());
    return matcher.matches() ? Integer.parseInt(matcher.group(1)) : null;
  }

  @VisibleForTesting
  org.batfish.representation.cumulus.Interface convertInterface(InterfacesInterface iface) {
    checkArgument(isInterface(iface), "input is not an interface");
    checkArgument(!iface.getName().equals(BRIDGE_NAME), "interface bridge is handled separately");
    String name = iface.getName();
    org.batfish.representation.cumulus.Interface vsIface =
        new org.batfish.representation.cumulus.Interface(
            name,
            getInterfaceType(iface),
            getSuperInterfaceName(iface.getName()),
            getEncapsulationVlan(iface));
    vsIface.setAlias(iface.getDescription());
    vsIface.setBridgeSettings(iface.getBridgeSettings());
    vsIface.setClagSettings(iface.getClagSettings());
    vsIface.getIpAddresses().addAll(firstNonNull(iface.getAddresses(), ImmutableList.of()));
    vsIface.setSpeed(iface.getLinkSpeed());
    vsIface.setVrf(iface.getVrf());
    iface.getPostUpIpRoutes().forEach(vsIface::addPostUpIpRoute);
    return vsIface;
  }

  @VisibleForTesting
  static Vlan convertVlan(InterfacesInterface iface) {
    checkArgument(isVlan(iface), "interfaces %s is not a vlan", iface.getName());
    Vlan vlan = new Vlan(iface.getName());
    vlan.setAlias(iface.getDescription());
    vlan.getAddresses().addAll(firstNonNull(iface.getAddresses(), ImmutableList.of()));
    vlan.getAddressVirtuals().putAll(firstNonNull(iface.getAddressVirtuals(), ImmutableMap.of()));
    vlan.setVlanId(iface.getVlanId());
    vlan.setVrf(iface.getVrf());
    return vlan;
  }

  @VisibleForTesting
  static Vrf convertVrf(InterfacesInterface iface) {
    checkArgument(isVrf(iface), "not a vrf");
    Vrf vrf = new Vrf(iface.getName());
    vrf.getAddresses().addAll(firstNonNull(iface.getAddresses(), ImmutableList.of()));
    return vrf;
  }

  @VisibleForTesting
  static Vxlan convertVxlan(InterfacesInterface iface) {
    checkArgument(isVxlan(iface), "not a vxlan");
    Vxlan vxlan = new Vxlan(iface.getName());
    vxlan.setId(iface.getVxlanId());
    vxlan.setLocalTunnelip(iface.getVxlanLocalTunnelIp());
    InterfaceBridgeSettings bridgeSettings = iface.getBridgeSettings();
    if (bridgeSettings != null) {
      vxlan.setBridgeAccessVlan(bridgeSettings.getAccess());
    }
    return vxlan;
  }

  public static boolean isBond(InterfacesInterface iface) {
    return iface.getType() == BOND;
  }

  public static boolean isVlan(InterfacesInterface iface) {
    return iface.getType() == VLAN;
  }

  public static boolean isVxlan(InterfacesInterface iface) {
    return iface.getType() == VXLAN;
  }

  public static boolean isVrf(InterfacesInterface iface) {
    return iface.getType() == VRF;
  }

  public static boolean isInterface(InterfacesInterface iface) {
    return iface.getType() == INTERFACE;
  }
}
