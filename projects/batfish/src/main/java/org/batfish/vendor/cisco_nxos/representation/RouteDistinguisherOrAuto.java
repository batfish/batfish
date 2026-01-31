package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;

/** Either {@code auto} or an explicit {@link RouteDistinguisher}. */
public final class RouteDistinguisherOrAuto implements Serializable {

  private static final RouteDistinguisherOrAuto AUTO = new RouteDistinguisherOrAuto(null);

  public static RouteDistinguisherOrAuto auto() {
    return AUTO;
  }

  public static RouteDistinguisherOrAuto of(@Nonnull RouteDistinguisher rd) {
    return new RouteDistinguisherOrAuto(rd);
  }

  public boolean isAuto() {
    return _routeDistinguisher == null;
  }

  public @Nullable RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private RouteDistinguisherOrAuto(@Nullable RouteDistinguisher rd) {
    _routeDistinguisher = rd;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RouteDistinguisherOrAuto)) {
      return false;
    }
    RouteDistinguisherOrAuto that = (RouteDistinguisherOrAuto) o;
    return Objects.equals(_routeDistinguisher, that._routeDistinguisher);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_routeDistinguisher);
  }

  private final @Nullable RouteDistinguisher _routeDistinguisher;
}
