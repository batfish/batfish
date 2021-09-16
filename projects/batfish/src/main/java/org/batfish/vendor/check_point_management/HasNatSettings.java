package org.batfish.vendor.check_point_management;

/** Interface for objects that have {@link NatSettings} */
public interface HasNatSettings extends AddressSpace {
  NatSettings getNatSettings();
}
