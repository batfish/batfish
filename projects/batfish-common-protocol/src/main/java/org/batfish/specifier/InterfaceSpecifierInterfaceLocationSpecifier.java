package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link LocationSpecifier} that yields {@link InterfaceLocation}s based on an {@code
 * InterfaceSpecifier}. The {@link InterfaceSpecifier} helps select the Interfaces, which are
 * converted to InterfaceLocations.
 */
@ParametersAreNonnullByDefault
public class InterfaceSpecifierInterfaceLocationSpecifier implements LocationSpecifier {
  private InterfaceSpecifier _interfaceSpecifier;

  public InterfaceSpecifierInterfaceLocationSpecifier(InterfaceSpecifier interfaceSpecifier) {
    _interfaceSpecifier = interfaceSpecifier;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfaceSpecifierInterfaceLocationSpecifier)) {
      return false;
    }
    return Objects.equals(
        _interfaceSpecifier,
        ((InterfaceSpecifierInterfaceLocationSpecifier) o)._interfaceSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_interfaceSpecifier);
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return _interfaceSpecifier.resolve(ctxt.getConfigs().keySet(), ctxt).stream()
        .map(iface -> new InterfaceLocation(iface.getHostname(), iface.getInterface()))
        .collect(ImmutableSet.toImmutableSet());
  }
}
