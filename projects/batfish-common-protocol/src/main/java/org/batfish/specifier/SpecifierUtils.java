package org.batfish.specifier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;

/** Utility methods for specifiers. */
public final class SpecifierUtils {
  private SpecifierUtils() {}

  /** Returns {@code true} iff the given {@link Location} is active (aka, interface is up). */
  @VisibleForTesting
  static boolean isActive(Location l, Map<String, Configuration> configs) {
    Configuration c = configs.get(l.getNodeName());
    if (l instanceof InterfaceLocation) {
      return c.getAllInterfaces()
          .get(((InterfaceLocation) l).getInterfaceName())
          .canOriginateIpTraffic();
    } else {
      assert l instanceof InterfaceLinkLocation;
      return c.getAllInterfaces()
          .get(((InterfaceLinkLocation) l).getInterfaceName())
          .canReceiveIpTraffic();
    }
  }

  public static Set<Location> resolveActiveStartLocations(
      LocationSpecifier locationSpecifier, SpecifierContext context) {
    return locationSpecifier.resolve(context).stream()
        .filter(l -> isActive(l, context.getConfigs()))
        .collect(ImmutableSet.toImmutableSet());
  }
}
