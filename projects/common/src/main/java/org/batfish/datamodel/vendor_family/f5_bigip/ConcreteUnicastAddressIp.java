package org.batfish.datamodel.vendor_family.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A concrete IP address within a {@link UnicastAddress}. */
public final class ConcreteUnicastAddressIp implements UnicastAddressIp {

  public ConcreteUnicastAddressIp(Ip ip) {
    _ip = ip;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConcreteUnicastAddressIp)) {
      return false;
    }
    return _ip.equals(((ConcreteUnicastAddressIp) obj)._ip);
  }

  @Override
  public int hashCode() {
    return _ip.hashCode();
  }

  private final @Nonnull Ip _ip;
}
