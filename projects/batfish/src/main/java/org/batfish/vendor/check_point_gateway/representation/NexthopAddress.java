package org.batfish.vendor.check_point_gateway.representation;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/**
 * Data model class containing configuration of a nexthop for a route, pointing to an IP address.
 */
public class NexthopAddress implements NexthopTarget {
  public NexthopAddress(Ip address) {
    _address = address;
  }

  @Nonnull
  public Ip getAddress() {
    return _address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NexthopAddress)) {
      return false;
    }
    NexthopAddress other = (NexthopAddress) o;
    return Objects.equals(_address, other._address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_address);
  }

  @Nonnull private final Ip _address;
}
