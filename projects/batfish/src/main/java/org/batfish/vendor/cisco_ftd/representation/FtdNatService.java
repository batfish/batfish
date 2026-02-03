package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** Represents a service (port/protocol) NAT translation in a Cisco FTD NAT rule. */
public class FtdNatService implements Serializable {
  private final @Nonnull String _realService;
  private final @Nonnull String _mappedService;

  public FtdNatService(@Nonnull String realService, @Nonnull String mappedService) {
    _realService = realService;
    _mappedService = mappedService;
  }

  public @Nonnull String getRealService() {
    return _realService;
  }

  public @Nonnull String getMappedService() {
    return _mappedService;
  }
}
