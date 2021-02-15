package org.batfish.representation.cumulus;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.cumulus.CumulusStructureType.BOND;
import static org.batfish.representation.cumulus.CumulusStructureType.INTERFACE;
import static org.batfish.representation.cumulus.CumulusStructureType.VLAN;
import static org.batfish.representation.cumulus.CumulusStructureType.VRF;
import static org.batfish.representation.cumulus.CumulusStructureType.VXLAN;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/** Converter from cumulus interfaces file model to Cumulus VS model. */
public final class InterfaceConverter {
  @VisibleForTesting public static final String BRIDGE_NAME = "bridge";

  private static final Pattern ENCAPSULATION_VLAN_PATTERN = Pattern.compile("^.*\\.([0-9]+)$");

  private static final Pattern SUBINTERFACE_PATTERN = Pattern.compile("^(.*)\\.([0-9]+)$");

  public static final Set<String> DEFAULT_BRIDGE_PORTS = ImmutableSet.of();
  public static final int DEFAULT_BRIDGE_PVID = 1;

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

  private InterfaceConverter() {} // cannot instantiate utility class
}
