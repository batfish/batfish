package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Wraps another {@link LocationSpecifier}, and converts all its {@link InterfaceLocation
 * InterfaceLocations} to {@link InterfaceLinkLocation InterfaceLinkLocations}.
 */
@ParametersAreNonnullByDefault
public class ToInterfaceLinkLocationSpecifier implements LocationSpecifier {
  private final LocationSpecifier _inner;

  public ToInterfaceLinkLocationSpecifier(LocationSpecifier inner) {
    _inner = inner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ToInterfaceLinkLocationSpecifier)) {
      return false;
    }
    ToInterfaceLinkLocationSpecifier other = (ToInterfaceLinkLocationSpecifier) o;
    return _inner.equals(other._inner);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_inner);
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return _inner.resolve(ctxt).stream()
        .map(ToInterfaceLinkLocationSpecifier::toInterfaceLinkLocation)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Converts {@code loc} to an InterfaceLink location. The function is a no-op if {@code loc} is
   * already an InterfaceLink location.
   */
  static Location toInterfaceLinkLocation(Location loc) {
    return loc.accept(
        new LocationVisitor<Location>() {
          @Override
          public Location visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
            return interfaceLinkLocation;
          }

          @Override
          public Location visitInterfaceLocation(InterfaceLocation interfaceLocation) {
            return new InterfaceLinkLocation(
                interfaceLocation.getNodeName(), interfaceLocation.getInterfaceName());
          }
        });
  }
}
