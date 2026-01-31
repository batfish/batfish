package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.LongSpace;

/**
 * A {@link RouteMapMatch} that "provide a list of AS numbers or an AS-path access list using a
 * regular expression. BGP uses this match criteria to determine which BGP peers to create a BGP
 * session with"
 * (https://www.cisco.com/c/m/en_us/techdoc/dc/reference/cli/nxos/commands/bgp/match-as-number.html)
 *
 * <p>AFAICT this does not have any effect when a route-map is used for import/export policy, etc.
 */
public final class RouteMapMatchAsNumber implements RouteMapMatch {

  private final @Nonnull LongSpace _asns;

  public RouteMapMatchAsNumber(LongSpace asns) {
    _asns = asns;
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchAsNumber(this);
  }

  public @Nonnull LongSpace getAsns() {
    return _asns;
  }
}
