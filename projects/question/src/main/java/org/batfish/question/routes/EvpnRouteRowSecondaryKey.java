package org.batfish.question.routes;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ReceivedFrom;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.route.nh.NextHop;

/** Class representing the secondary key used for grouping {@link Bgpv4Route}s */
@ParametersAreNonnullByDefault
public class EvpnRouteRowSecondaryKey extends RouteRowSecondaryKey {
  private final @Nonnull ReceivedFrom _receivedFrom;
  private final @Nullable Integer _pathId;
  private final @Nonnull RouteDistinguisher _routeDistinguisher;

  public EvpnRouteRowSecondaryKey(
      NextHop nextHop,
      String protocol,
      ReceivedFrom receivedFrom,
      @Nullable Integer pathId,
      RouteDistinguisher routeDistinguisher) {
    super(nextHop, protocol);
    _pathId = pathId;
    _receivedFrom = receivedFrom;
    _routeDistinguisher = routeDistinguisher;
  }

  @Override
  public <R> R accept(RouteRowSecondaryKeyVisitor<R> visitor) {
    return visitor.visitEvpnRouteRowSecondaryKey(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EvpnRouteRowSecondaryKey that = (EvpnRouteRowSecondaryKey) o;
    return _nextHop.equals(that._nextHop)
        && Objects.equals(_pathId, that._pathId)
        && _protocol.equals(that._protocol)
        && _receivedFrom.equals(that._receivedFrom)
        && _routeDistinguisher.equals(that._routeDistinguisher);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nextHop, _pathId, _protocol, _receivedFrom, _routeDistinguisher);
  }

  public @Nullable Integer getPathId() {
    return _pathId;
  }

  public @Nonnull ReceivedFrom getReceivedFrom() {
    return _receivedFrom;
  }

  public @Nonnull RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }
}
