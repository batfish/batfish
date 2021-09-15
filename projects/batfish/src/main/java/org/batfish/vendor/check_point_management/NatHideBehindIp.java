package org.batfish.vendor.check_point_management;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** IP to hide behind for a NAT hide rule. */
public class NatHideBehindIp extends NatHideBehind {
  @Nonnull private final Ip _ip;

  public NatHideBehindIp(@Nonnull Ip ip) {
    _ip = ip;
  }

  @Override
  <T> T accept(NatHideBehindVisitor<T> visitor) {
    return visitor.visitNatHideBehindIp(this);
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NatHideBehindIp)) {
      return false;
    }
    NatHideBehindIp that = (NatHideBehindIp) o;
    return _ip.equals(that._ip);
  }

  @Override
  public int hashCode() {
    return _ip.hashCode();
  }
}
