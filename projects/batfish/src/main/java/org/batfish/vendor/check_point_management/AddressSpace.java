package org.batfish.vendor.check_point_management;

/** An object that may be used to represent an address space. */
public interface AddressSpace extends HasName, NatTranslatedAddress {

  @Override
  default <T> T accept(NatTranslatedAddressVisitor<T> visitor) {
    // TODO: may want to implement solely in implementing classes instead
    return visitor.visitAddressSpace(this);
  }

  <T> T accept(AddressSpaceVisitor<T> visitor);
}
