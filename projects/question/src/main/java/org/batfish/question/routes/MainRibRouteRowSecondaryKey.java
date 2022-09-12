package org.batfish.question.routes;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * Class representing the secondary key used for grouping {@link
 * org.batfish.datamodel.AbstractRoute}s
 */
@ParametersAreNonnullByDefault
public final class MainRibRouteRowSecondaryKey extends RouteRowSecondaryKey {
  public MainRibRouteRowSecondaryKey(NextHop nextHop, String protocol) {
    super(nextHop, protocol);
  }

  @Override
  public <R> R accept(RouteRowSecondaryKeyVisitor<R> visitor) {
    return visitor.visitMainRibRouteRowSecondaryKey(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MainRibRouteRowSecondaryKey that = (MainRibRouteRowSecondaryKey) o;
    return _nextHop.equals(that._nextHop) && _protocol.equals(that._protocol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nextHop, _protocol);
  }
}
