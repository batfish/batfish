package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A {@link RouteMapMatch} that matches routes based on whether the route's community attribute is
 * matched by named {@link IpCommunityList}s.
 */
public final class RouteMapMatchCommunity implements RouteMapMatch {

  private final @Nonnull List<String> _names;

  public RouteMapMatchCommunity(Iterable<String> names) {
    _names = ImmutableList.copyOf(names);
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchCommunity(this);
  }

  public @Nonnull List<String> getNames() {
    return _names;
  }
}
