package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Datamodel class representing an IP address for a load balancer virtual-server */
public final class VirtualServerTargetAddress implements VirtualServerTarget {
  @Override
  public int hashCode() {
    return _address.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof VirtualServerTargetAddress)) {
      return false;
    }
    VirtualServerTargetAddress o = (VirtualServerTargetAddress) obj;
    return _address.equals(o._address);
  }

  @Nonnull
  public Ip getAddress() {
    return _address;
  }

  public VirtualServerTargetAddress(Ip address) {
    _address = address;
  }

  @Nonnull private final Ip _address;

  @Override
  public <T> T accept(VirtualServerTargetVisitor<T> visitor) {
    return visitor.visitAddress(this);
  }
}
