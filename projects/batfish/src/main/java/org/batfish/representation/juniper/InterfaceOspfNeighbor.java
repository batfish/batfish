package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Ospf interface neighbor information */
public final class InterfaceOspfNeighbor implements Serializable {
  public InterfaceOspfNeighbor(@Nonnull Ip ip) {
    _ip = ip;
  }

  @Nonnull
  public Ip getIp() {
    return _ip;
  }

  @Nullable
  public Boolean getDesignated() {
    return _isDesignated;
  }

  public void setDesignated(@Nullable Boolean designated) {
    _isDesignated = designated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof InterfaceOspfNeighbor)) {
      return false;
    }

    InterfaceOspfNeighbor other = (InterfaceOspfNeighbor) o;

    return other.getIp().equals(_ip) && Objects.equals(_isDesignated, other.getDesignated());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _isDesignated);
  }

  private final @Nonnull Ip _ip;
  private @Nullable Boolean _isDesignated;
}
