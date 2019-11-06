package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
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
  public @Nonnull org.batfish.datamodel.vendor_family.f5_bigip.ConcreteUnicastAddressIp
      toUnicastAddressIp() {
    return new org.batfish.datamodel.vendor_family.f5_bigip.ConcreteUnicastAddressIp(_ip);
  }

  private final @Nonnull Ip _ip;
}
