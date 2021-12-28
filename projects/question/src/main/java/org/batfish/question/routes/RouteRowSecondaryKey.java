package org.batfish.question.routes;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * Class representing the secondary key used for grouping {@link
 * org.batfish.datamodel.AbstractRoute}s and {@link BgpRoute}s
 */
@ParametersAreNonnullByDefault
public class RouteRowSecondaryKey {

  @Nonnull private final NextHop _nextHop;

  @Nonnull private final String _protocol;

  public RouteRowSecondaryKey(NextHop nextHop, String protocol) {
    _nextHop = nextHop;
    _protocol = protocol;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RouteRowSecondaryKey that = (RouteRowSecondaryKey) o;
    return _nextHop.equals(that._nextHop) && _protocol.equals(that._protocol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nextHop, _protocol);
  }

  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  public @Nonnull String getProtocol() {
    return _protocol;
  }
}
