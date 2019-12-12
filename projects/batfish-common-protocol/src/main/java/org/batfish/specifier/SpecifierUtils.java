package org.batfish.specifier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility methods for specifiers. */
public final class SpecifierUtils {
  private SpecifierUtils() {}

  /** Returns {@code true} iff the given {@link Location} is active (aka, interface is up). */
  @VisibleForTesting
  static boolean isActive(Location l, Map<String, Configuration> configs) {
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
    return configs
        .get(iface.getHostname())
        .getAllInterfaces()
        .get(iface.getInterface())
        .getActive();
  }

  public static Set<Location> resolveActiveLocations(
      LocationSpecifier locationSpecifier, SpecifierContext context) {
    return locationSpecifier.resolve(context).stream()
        .filter(l -> isActive(l, context.getConfigs()))
        .collect(ImmutableSet.toImmutableSet());
  }
}
