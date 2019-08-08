package org.batfish.representation.cumulus_interfaces;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.representation.cumulus.Bridge;
import org.batfish.representation.cumulus.CumulusInterfaceType;
import org.batfish.representation.cumulus.InterfaceBridgeSettings;
import org.batfish.representation.cumulus.Vlan;
import org.batfish.representation.cumulus.Vrf;

/** Converter from cumulus interfaces file model to Cumulus VS model. */
public final class Converter {
  @VisibleForTesting static final String BRIDGE_NAME = "bridge";

  private static final Pattern ENCAPSULATION_VLAN_PATTERN = Pattern.compile("^.*\\.([0-9]+)$");
  private static final Pattern PHYSICAL_INTERFACE_PATTERN =
      Pattern.compile("^(swp[0-9]+(s[0-9])?)|(eth[0-9]+)$");
  private static final Pattern PHYSICAL_SUBINTERFACE_PATTERN =
      Pattern.compile("^((swp[0-9]+(s[0-9])?)|(eth[0-9]+))\\.([0-9]+)$");
  private static final Pattern VLAN_INTERFACE_PATTERN = Pattern.compile("^vlan([0-9]+)$");
  private final Interfaces _interfaces;

  private static final Set<String> DEFAULT_BRIDGE_PORTS = ImmutableSet.of();
  private static final int DEFAULT_BRIDGE_PVID = 1;

  public Converter(Interfaces interfaces) {
    _interfaces = interfaces;
  }

  /** Get Cumulus VS model {@link Bridge}. */
  public @Nullable Bridge convertBridge() {
    Interface bridgeIface = _interfaces.getInterfaces().get(BRIDGE_NAME);
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
        .filter(Converter::isInterface)
        .map(this::convertInterface)
        .collect(
            toImmutableMap(
                org.batfish.representation.cumulus.Interface::getName, Function.identity()));
  }

  /** Get Cumulus VS model {@link Vlan Vlans}. */
  public Map<String, Vlan> convertVlans() {
    return _interfaces.getInterfaces().values().stream()
        .filter(Converter::isVlan)
        .map(Converter::convertVlan)
        .collect(toImmutableMap(Vlan::getName, Function.identity()));
  }

  /** Get Cumulus VS model {@link Vrf Vrfs}. */
  public Map<String, Vrf> convertVrfs() {
    return _interfaces.getInterfaces().values().stream()
        .filter(Interface::getIsVrf)
        .map(Converter::convertVrf)
        .collect(toImmutableMap(Vrf::getName, Function.identity()));
  }

  @VisibleForTesting
  CumulusInterfaceType getInterfaceType(Interface iface) {
    String name = iface.getName();
    if (_interfaces.getBondSlaveParents().containsKey(name)) {
      return CumulusInterfaceType.BOND_SUBINTERFACE;
    } else if (PHYSICAL_INTERFACE_PATTERN.matcher(name).matches()) {
      return CumulusInterfaceType.PHYSICAL;
    } else if (PHYSICAL_SUBINTERFACE_PATTERN.matcher(name).matches()) {
      return CumulusInterfaceType.PHYSICAL_SUBINTERFACE;
    } else {
      throw new BatfishException("cannot determine interface type for " + name);
    }
  }

  @VisibleForTesting
  @Nullable
  String getSuperInterfaceName(Interface iface) {
    String name = iface.getName();
    String superIfaceName = _interfaces.getBondSlaveParents().get(name);
    if (superIfaceName != null) {
      return superIfaceName;
    }
    Matcher matcher = PHYSICAL_SUBINTERFACE_PATTERN.matcher(name);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
  }

  @VisibleForTesting
  @Nullable
  static Integer getEncapsulationVlan(Interface iface) {
    Matcher matcher = ENCAPSULATION_VLAN_PATTERN.matcher(iface.getName());
    return matcher.matches() ? Integer.parseInt(matcher.group(1)) : null;
  }

  @VisibleForTesting
  org.batfish.representation.cumulus.Interface convertInterface(Interface iface) {
    checkArgument(!iface.getIsVrf(), "input is a vrf");
    checkArgument(!isVlan(iface), "input is a vlan");
    String name = iface.getName();
    org.batfish.representation.cumulus.Interface vsIface =
        new org.batfish.representation.cumulus.Interface(
            name,
            getInterfaceType(iface),
            getSuperInterfaceName(iface),
            getEncapsulationVlan(iface));
    vsIface.setAlias(iface.getDescription());
    vsIface.setBridgeSettings(iface.getBridgeSettings());
    vsIface.setClagSettings(iface.getClagSettings());
    vsIface.getIpAddresses().addAll(firstNonNull(iface.getAddresses(), ImmutableList.of()));
    vsIface.setSpeed(iface.getLinkSpeed());
    vsIface.setVrf(iface.getVrf());
    return vsIface;
  }

  @VisibleForTesting
  static Vlan convertVlan(Interface iface) {
    checkArgument(!iface.getIsVrf(), "input is a vrf, not a vlan");
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
  static Vrf convertVrf(Interface iface) {
    checkArgument(iface.getIsVrf(), "not a vrf");
    Vrf vrf = new Vrf(iface.getName());
    vrf.getAddresses().addAll(firstNonNull(iface.getAddresses(), ImmutableList.of()));
    return vrf;
  }

  @VisibleForTesting
  static boolean isBridge(Interface iface) {
    return iface.getName().equals(BRIDGE_NAME);
  }

  @VisibleForTesting
  static boolean isVlan(Interface iface) {
    return VLAN_INTERFACE_PATTERN.matcher(iface.getName()).matches();
  }

  @VisibleForTesting
  static boolean isInterface(Interface iface) {
    return !isBridge(iface) && !isVlan(iface) && !iface.getIsVrf();
  }
}
