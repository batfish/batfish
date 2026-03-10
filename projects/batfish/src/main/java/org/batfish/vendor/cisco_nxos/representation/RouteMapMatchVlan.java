package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IntegerSpace;

/**
 * A {@link RouteMapMatch} that matches routes for networks belonging to an IRB interface for a VLAN
 * in a provided range.
 */
public final class RouteMapMatchVlan implements RouteMapMatch {

  private final @Nonnull IntegerSpace _vlans;

  public RouteMapMatchVlan(IntegerSpace vlans) {
    _vlans = vlans;
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchVlan(this);
  }

  public @Nonnull IntegerSpace getVlans() {
    return _vlans;
  }
}
