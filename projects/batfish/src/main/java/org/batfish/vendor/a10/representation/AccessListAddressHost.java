package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** An access-list address, representing a specific host address. */
public class AccessListAddressHost implements AccessListAddress {
  @Nonnull
  public Ip getHost() {
    return _host;
  }

  public AccessListAddressHost(Ip host) {
    _host = host;
  }

  @Nonnull private final Ip _host;
}
