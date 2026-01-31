package org.batfish.vendor.arista.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.UniverseIpSpace;

/** Used iff a user specifies the address {@code any} for an IPv4 address in an ACL. */
public final class AnyAddressSpecifier implements AccessListAddressSpecifier {

  public static final AnyAddressSpecifier INSTANCE = new AnyAddressSpecifier();

  private AnyAddressSpecifier() {} // prevent instantiation

  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof AnyAddressSpecifier;
  }

  @Override
  public int hashCode() {
    /* Randomly generated. */
    return 0xA840F6CD;
  }

  @Override
  public @Nonnull IpSpace toIpSpace() {
    return UniverseIpSpace.INSTANCE;
  }
}
