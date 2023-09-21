package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Represents a next-hop for Juniper static routes */
public class NextHop implements Serializable {
  private @Nullable Ip _nextHopIp;
  private @Nullable String _nextHopInterface;

  public NextHop(@Nonnull String nextHopInterface) {
    _nextHopInterface = nextHopInterface;
  }

  public NextHop(@Nonnull Ip nextHopIp) {
    _nextHopIp = nextHopIp;
  }

  public @Nullable String getNextHopInterface() {
    return _nextHopInterface;
  }

  public @Nullable Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NextHop)) {
      return false;
    }
    NextHop that = (NextHop) o;
    return Objects.equals(_nextHopIp, that._nextHopIp)
        && Objects.equals(_nextHopInterface, that._nextHopInterface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nextHopIp, _nextHopInterface);
  }
}
