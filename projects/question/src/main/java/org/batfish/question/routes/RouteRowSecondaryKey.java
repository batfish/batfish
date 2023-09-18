package org.batfish.question.routes;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * Class representing the secondary key used for grouping {@link
 * org.batfish.datamodel.AbstractRoute}s and {@link BgpRoute}s
 */
@ParametersAreNonnullByDefault
public abstract class RouteRowSecondaryKey {
  protected final @Nonnull NextHop _nextHop;
  protected final @Nonnull String _protocol;

  protected RouteRowSecondaryKey(NextHop nextHop, String protocol) {
    _nextHop = nextHop;
    _protocol = protocol;
  }

  public final @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  public final @Nonnull String getProtocol() {
    return _protocol;
  }

  public abstract <R> R accept(RouteRowSecondaryKeyVisitor<R> visitor);
}
