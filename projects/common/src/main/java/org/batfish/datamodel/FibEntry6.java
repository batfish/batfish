package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * One resolved IPv6 forwarding entry.
 *
 * <p>The next-hop IP is the neighbor-discovery target when present. For a
 * directly connected route it is absent, meaning the destination itself is
 * resolved on the outgoing interface.
 */
@ParametersAreNonnullByDefault
public final class FibEntry6 implements Serializable {

  public FibEntry6(
      AbstractRoute6 topLevelRoute,
      String interfaceName,
      @Nullable Ip6 nextHopIp) {
    _topLevelRoute = topLevelRoute;
    _interfaceName = interfaceName;
    _nextHopIp = nextHopIp;
  }

  public @Nonnull AbstractRoute6 getTopLevelRoute() {
    return _topLevelRoute;
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public @Nonnull Optional<Ip6> getNextHopIp() {
    return Optional.ofNullable(_nextHopIp);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FibEntry6)) {
      return false;
    }

    FibEntry6 rhs = (FibEntry6) o;

    return _topLevelRoute.equals(rhs._topLevelRoute)
        && _interfaceName.equals(rhs._interfaceName)
        && Objects.equals(_nextHopIp, rhs._nextHopIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _topLevelRoute,
        _interfaceName,
        _nextHopIp);
  }

  @Override
  public String toString() {
    return "FibEntry6<"
        + _topLevelRoute.getNetwork()
        + ",iface:"
        + _interfaceName
        + ",nh:"
        + _nextHopIp
        + ">";
  }

  private final @Nonnull String _interfaceName;
  private final @Nullable Ip6 _nextHopIp;
  private final @Nonnull AbstractRoute6 _topLevelRoute;
}
