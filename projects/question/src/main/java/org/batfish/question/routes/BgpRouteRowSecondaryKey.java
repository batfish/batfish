package org.batfish.question.routes;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHop;

/** Class representing the secondary key used for grouping {@link Bgpv4Route}s */
@ParametersAreNonnullByDefault
public class BgpRouteRowSecondaryKey extends RouteRowSecondaryKey {
  private final @Nonnull Ip _receivedFromIp;
  private final @Nullable Integer _pathId;

  public BgpRouteRowSecondaryKey(
      NextHop nextHop, String protocol, Ip receivedFromIp, @Nullable Integer pathId) {
    super(nextHop, protocol);
    _pathId = pathId;
    _receivedFromIp = receivedFromIp;
  }

  @Override
  public <R> R accept(RouteRowSecondaryKeyVisitor<R> visitor) {
    return visitor.visitBgpRouteRowSecondaryKey(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BgpRouteRowSecondaryKey that = (BgpRouteRowSecondaryKey) o;
    return _nextHop.equals(that._nextHop)
        && Objects.equals(_pathId, that._pathId)
        && _protocol.equals(that._protocol)
        && _receivedFromIp.equals(that._receivedFromIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nextHop, _pathId, _protocol, _receivedFromIp);
  }

  public @Nullable Integer getPathId() {
    return _pathId;
  }

  public @Nonnull Ip getReceivedFromIp() {
    return _receivedFromIp;
  }
}
