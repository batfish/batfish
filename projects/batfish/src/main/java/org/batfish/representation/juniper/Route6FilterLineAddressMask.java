package org.batfish.representation.juniper;

import java.util.Objects;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;

/** Representation of a route-filter with a IPv6 prefix and an address mask */
public final class Route6FilterLineAddressMask extends Route6FilterLine {

  private final Ip6 _addressMask;

  public Route6FilterLineAddressMask(Prefix6 prefix6, Ip6 addressMask) {
    super(prefix6);
    _addressMask = addressMask;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route6FilterLineAddressMask)) {
      return false;
    }

    Route6FilterLineAddressMask rhs = (Route6FilterLineAddressMask) o;
    return _prefix6.equals(rhs._prefix6) && _addressMask.equals(rhs._addressMask);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix6, _addressMask);
  }
}
