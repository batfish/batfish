package org.batfish.specifier;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Vrf;

/**
 * A {@link LocationSpecifier} specifying interfaces that belong to VRFs with names matching the
 * input regex.
 */
public final class VrfNameRegexInterfaceLocationSpecifier extends VrfNameRegexLocationSpecifier {
  public VrfNameRegexInterfaceLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Stream<Location> getVrfLocations(Vrf vrf) {
    return vrf.getInterfaces()
        .values()
        .stream()
        .map(iface -> new InterfaceLocation(iface.getOwner().getHostname(), iface.getName()));
  }
}
