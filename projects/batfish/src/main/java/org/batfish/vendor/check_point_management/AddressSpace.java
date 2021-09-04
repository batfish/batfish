package org.batfish.vendor.check_point_management;

/** An object that may be used to represent an address space. */
public abstract class AddressSpace extends TypedManagementObject implements NatTranslatedAddress {

  protected AddressSpace(String name, Uid uid) {
    super(name, uid);
  }

  @Override
  public <T> T accept(NatTranslatedAddressVisitor<T> visitor) {
    // TODO: may want to implement solely in implementing classes instead
    return visitor.visitAddressSpace(this);
  }

  public abstract <T> T accept(AddressSpaceVisitor<T> visitor);
}
