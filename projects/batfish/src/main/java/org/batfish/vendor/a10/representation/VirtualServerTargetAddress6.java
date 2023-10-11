package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip6;

/** Datamodel class representing an IPv6 address for a load balancer virtual-server */
public final class VirtualServerTargetAddress6 implements VirtualServerTarget {

  public VirtualServerTargetAddress6(Ip6 address6) {
    _address6 = address6;
  }

  @Override
  public <T> T accept(VirtualServerTargetVisitor<T> visitor) {
    return visitor.visitVirtualServerTargetAddress6(this);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof VirtualServerTargetAddress6)) {
      return false;
    }
    VirtualServerTargetAddress6 o = (VirtualServerTargetAddress6) obj;
    return _address6.equals(o._address6);
  }

  @Override
  public int hashCode() {
    return _address6.hashCode();
  }

  public @Nonnull Ip6 getAddress6() {
    return _address6;
  }

  private final @Nonnull Ip6 _address6;
}
