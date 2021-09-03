package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedAddress} */
public interface NatTranslatedAddressVisitor<T> extends AddressSpaceVisitor<T> {
  T visitOriginal(Original original);

  T visitAddressSpace(AddressSpace addressSpace);
}
