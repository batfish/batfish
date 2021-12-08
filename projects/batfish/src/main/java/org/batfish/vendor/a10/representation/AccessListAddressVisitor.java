package org.batfish.vendor.a10.representation;

/** A visitor of {@link AccessListAddress} that returns a generic value. */
public interface AccessListAddressVisitor<T> {
  default T visit(AccessListAddress address) {
    return address.accept(this);
  }

  T visitAny(AccessListAddressAny address);

  T visitHost(AccessListAddressHost address);
}
