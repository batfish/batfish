package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedAddress} */
public interface NatTranslatedAddressVisitor<T> extends AddressSpaceVisitor<T> {
  default T visit(NatTranslatedAddress natTranslatedAddress) {
    return natTranslatedAddress.accept(this);
  }

  T visitOriginal(Original original);
}
