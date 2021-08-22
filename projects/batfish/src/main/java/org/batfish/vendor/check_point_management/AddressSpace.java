package org.batfish.vendor.check_point_management;

/** Parent class for objects that may used to represent an address space. */
public abstract class AddressSpace extends TypedManagementObject {

  protected AddressSpace(String name, Uid uid) {
    super(name, uid);
  }
}
