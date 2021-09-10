package org.batfish.vendor.check_point_management;

/** A gateway or server or host, in the context of a NAT translated destination. */
public interface Machine extends NatTranslatedSource {

  <T> T accept(MachineVisitor<T> visitor);
}
