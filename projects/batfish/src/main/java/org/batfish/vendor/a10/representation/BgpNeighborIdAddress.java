package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** Data model class representing an IP-address-based BGP neighbor identifier. */
public class BgpNeighborIdAddress implements BgpNeighborId {
  @Nonnull
  public Ip getAddress() {
    return _address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpNeighborIdAddress)) {
      return false;
    }
    BgpNeighborIdAddress that = (BgpNeighborIdAddress) o;
    return _address.equals(that._address);
  }

  @Override
  public int hashCode() {
    return _address.hashCode();
  }

  public BgpNeighborIdAddress(Ip address) {
    _address = address;
  }

  @Nonnull private Ip _address;
}
