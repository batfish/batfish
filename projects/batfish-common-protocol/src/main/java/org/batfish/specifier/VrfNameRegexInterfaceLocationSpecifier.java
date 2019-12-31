package org.batfish.specifier;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;

/**
 * A {@link LocationSpecifier} specifying interfaces that belong to VRFs with names matching the
 * input regex.
 */
public final class VrfNameRegexInterfaceLocationSpecifier extends VrfNameRegexLocationSpecifier {
  public VrfNameRegexInterfaceLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Stream<Location> getVrfLocations(Configuration c, String vrfName) {
    return c.getAllInterfaces(vrfName).values().stream()
        .map(iface -> new InterfaceLocation(iface.getOwner().getHostname(), iface.getName()));
  }
}
