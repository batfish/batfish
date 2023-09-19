package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** An access-list address, representing a specific host address. */
public class AccessListAddressHost implements AccessListAddress {
  public @Nonnull Ip getHost() {
    return _host;
  }

  public AccessListAddressHost(Ip host) {
    _host = host;
  }

  private final @Nonnull Ip _host;

  @Override
  public <T> T accept(AccessListAddressVisitor<T> visitor) {
    return visitor.visitHost(this);
  }
}
