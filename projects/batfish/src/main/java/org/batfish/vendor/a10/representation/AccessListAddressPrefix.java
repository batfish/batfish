package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** An access-list address, representing a specific prefix. */
public class AccessListAddressPrefix implements AccessListAddress {
  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }

  public AccessListAddressPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  @Nonnull private final Prefix _prefix;

  @Override
  public <T> T accept(AccessListAddressVisitor<T> visitor) {
    return visitor.visitPrefix(this);
  }
}
