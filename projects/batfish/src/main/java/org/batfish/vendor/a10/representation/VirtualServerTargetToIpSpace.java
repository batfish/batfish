package org.batfish.vendor.a10.representation;

import org.batfish.datamodel.IpSpace;

/** Visitor that generates an {@link IpSpace} for a {@link VirtualServerTarget}. */
public class VirtualServerTargetToIpSpace implements VirtualServerTargetVisitor<IpSpace> {
  public static final VirtualServerTargetToIpSpace INSTANCE = new VirtualServerTargetToIpSpace();

  @Override
  public IpSpace visitAddress(VirtualServerTargetAddress address) {
    return address.getAddress().toIpSpace();
  }

  private VirtualServerTargetToIpSpace() {}
}
