package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Wraps another LocationSpecifer, and converts all its InterfaceLocations to
 * InterfaceLinkLocations.
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
    return Objects.hash(_inner);
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return _inner
        .resolve(ctxt)
        .stream()
        .map(
            loc ->
                loc.accept(
                    new LocationVisitor<Location>() {
                      @Override
                      public Location visitInterfaceLinkLocation(
                          InterfaceLinkLocation interfaceLinkLocation) {
                        return interfaceLinkLocation;
                      }

                      @Override
                      public Location visitInterfaceLocation(InterfaceLocation interfaceLocation) {
                        return new InterfaceLinkLocation(
                            interfaceLocation.getNodeName(), interfaceLocation.getInterfaceName());
                      }
                    }))
        .collect(ImmutableSet.toImmutableSet());
  }
}
