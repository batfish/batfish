package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

/** A {@link RouteMapSet} that deletes from a route's community attribute. */
public final class RouteMapSetCommListDelete implements RouteMapSet {

  private @Nonnull String _name;

  public RouteMapSetCommListDelete(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetCommListDelete(this);
  }

  public @Nonnull String getName() {
    return _name;
  }
}
