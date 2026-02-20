package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Represents a destination NAT translation in a Cisco FTD NAT rule. Destination NAT is always
 * static.
 */
public class FtdNatDestination implements Serializable {
  private final @Nonnull FtdNatAddress _real;
  private final @Nonnull FtdNatAddress _mapped;

  public FtdNatDestination(@Nonnull FtdNatAddress real, @Nonnull FtdNatAddress mapped) {
    _real = real;
    _mapped = mapped;
  }

  public @Nonnull FtdNatAddress getReal() {
    return _real;
  }

  public @Nonnull FtdNatAddress getMapped() {
    return _mapped;
  }

  @Override
  public String toString() {
    return String.format("FtdNatDestination[%s -> %s]", _real, _mapped);
  }
}
