package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** Data model class representing an IP-address-based update-source for a BGP neighbor. */
public class BgpNeighborUpdateSourceAddress implements BgpNeighborUpdateSource {
  @Nonnull
  public Ip getAddress() {
    return _address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpNeighborUpdateSourceAddress)) {
      return false;
    }
    BgpNeighborUpdateSourceAddress that = (BgpNeighborUpdateSourceAddress) o;
    return _address.equals(that._address);
  }

  @Override
  public int hashCode() {
    return _address.hashCode();
  }

  public BgpNeighborUpdateSourceAddress(Ip address) {
    _address = address;
  }

  @Nonnull private Ip _address;
}
