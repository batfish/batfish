package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** A statically-configured route */
public class StaticRoute implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Prefix _network;
  private final @Nullable Ip _nextHopIp;

  public StaticRoute(Prefix network, Ip nextHopIp) {
    _network = network;
    _nextHopIp = nextHopIp;
  }

  public @Nonnull Prefix getNetwork() {
    return _network;
  }

  public @Nullable Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StaticRoute)) {
      return false;
    }
    StaticRoute rhs = (StaticRoute) obj;
    return _network.equals(rhs._network) && Objects.equals(_nextHopIp, rhs._nextHopIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _nextHopIp);
  }
}
