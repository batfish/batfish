package org.batfish.vendor.a10.representation;

/** An access-list address, representing any possible address. */
public class AccessListAddressAny implements AccessListAddress {
  public static final AccessListAddressAny INSTANCE = new AccessListAddressAny();

  private AccessListAddressAny() {}

  @Override
  public <T> T accept(AccessListAddressVisitor<T> visitor) {
    return visitor.visitAny(this);
  }
}
