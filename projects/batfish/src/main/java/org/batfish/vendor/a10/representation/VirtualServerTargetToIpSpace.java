package org.batfish.vendor.a10.representation;

import org.batfish.datamodel.IpSpace;

/** Visitor that generates an {@link IpSpace} for a {@link VirtualServerTarget}. */
public class VirtualServerTargetToIpSpace implements VirtualServerTargetVisitor<IpSpace> {
  public static final VirtualServerTargetToIpSpace INSTANCE = new VirtualServerTargetToIpSpace();

  @Override
  public IpSpace visitVirtualServerTargetAddress(
      VirtualServerTargetAddress virtualServerTargetAddress) {
    return virtualServerTargetAddress.getAddress().toIpSpace();
  }

  @Override
  public IpSpace visitVirtualServerTargetAddress6(
      VirtualServerTargetAddress6 virtualServerTargetAddress6) {
    throw new IllegalArgumentException("Cannot convert IPv6 target");
  }

  private VirtualServerTargetToIpSpace() {}
}
