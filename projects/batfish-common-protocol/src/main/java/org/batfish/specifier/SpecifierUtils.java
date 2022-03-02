package org.batfish.specifier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility methods for specifiers. */
public final class SpecifierUtils {
  private SpecifierUtils() {}

  /**
   * Returns {@code true} iff the given {@link Location} is active (aka, interface is up) and L3
   * (aka, not switchport AND has addresses).
   */
  @VisibleForTesting
  static boolean isActiveL3Interface(Location l, Map<String, Configuration> configs) {
    NodeInterfacePair iface;
    if (l instanceof InterfaceLocation) {
      iface =
          NodeInterfacePair.of(
              ((InterfaceLocation) l).getNodeName(), ((InterfaceLocation) l).getInterfaceName());
    } else {
      assert l instanceof InterfaceLinkLocation;
      iface =
          NodeInterfacePair.of(
              ((InterfaceLinkLocation) l).getNodeName(),
              ((InterfaceLinkLocation) l).getInterfaceName());
    }
    Interface i = configs.get(iface.getHostname()).getAllInterfaces().get(iface.getInterface());
    return i.getActive() && !i.getSwitchport() && !i.getAllAddresses().isEmpty();
  }

  public static Set<Location> resolveActiveL3Locations(
      LocationSpecifier locationSpecifier, SpecifierContext context) {
    return locationSpecifier.resolve(context).stream()
        .filter(l -> isActiveL3Interface(l, context.getConfigs()))
        .collect(ImmutableSet.toImmutableSet());
  }
}
