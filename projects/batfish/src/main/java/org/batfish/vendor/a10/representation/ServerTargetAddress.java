package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Datamodel class representing an IP address for a load balancer server */
public final class ServerTargetAddress extends ServerTarget {
  @Override
  public int hashCode() {
    return _address.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof ServerTargetAddress)) {
      return false;
    }
    ServerTargetAddress o = (ServerTargetAddress) obj;
    return _address.equals(o._address);
  }

  public @Nonnull Ip getAddress() {
    return _address;
  }

  public ServerTargetAddress(Ip address) {
    _address = address;
  }

  private final @Nonnull Ip _address;

  @Override
  public <T> T accept(ServerTargetVisitor<T> visitor) {
    return visitor.visitAddress(this);
  }
}
