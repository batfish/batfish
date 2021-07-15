package org.batfish.vendor.check_point_gateway.representation;

import java.util.Objects;
import javax.annotation.Nonnull;

/** Data model class containing configuration of a nexthop for a route, pointing to an interface. */
public class NexthopLogical implements NexthopTarget {
  public NexthopLogical(String iface) {
    _interface = iface;
  }

  @Nonnull
  public String getInterface() {
    return _interface;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NexthopLogical)) {
      return false;
    }
    NexthopLogical other = (NexthopLogical) o;
    return Objects.equals(_interface, other._interface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_interface);
  }

  @Nonnull private final String _interface;
}
