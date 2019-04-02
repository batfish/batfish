package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A route-map condition that matches a route whose network is assigned to one of a set of provided
 * interfaces.
 */
public class RouteMapMatchInterface implements RouteMapMatch {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Set<String> _interfaces;

  public RouteMapMatchInterface(Set<String> interfaces) {
    _interfaces = ImmutableSet.copyOf(interfaces);
  }

  public @Nonnull Set<String> getInterfaces() {
    return _interfaces;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RouteMapMatchInterface)) {
      return false;
    }
    return _interfaces.equals(((RouteMapMatchInterface) obj)._interfaces);
  }

  @Override
  public int hashCode() {
    return _interfaces.hashCode();
  }
}
