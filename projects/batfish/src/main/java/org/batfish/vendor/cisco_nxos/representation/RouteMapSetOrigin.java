package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.OriginType;

/** A {@link RouteMapSet} that sets the origin attribute of a BGP route. */
public final class RouteMapSetOrigin implements RouteMapSet {

  private final @Nonnull OriginType _origin;

  public RouteMapSetOrigin(OriginType origin) {
    _origin = origin;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetOrigin(this);
  }

  public @Nonnull OriginType getOrigin() {
    return _origin;
  }
}
