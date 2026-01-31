package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/**
 * In routing context, a {@link RouteMapSetIpNextHop} that sets the NEXT_HOP attribute of the route
 * to the given IP address. In forwarding context, sets the forwarding next-hop(s) based on the
 * given list of IP addresses.
 */
public final class RouteMapSetIpNextHopLiteral implements RouteMapSetIpNextHop {

  private final @Nonnull List<Ip> _nextHops;

  public RouteMapSetIpNextHopLiteral(Iterable<Ip> nextHops) {
    _nextHops = ImmutableList.copyOf(nextHops);
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetIpNextHopLiteral(this);
  }

  public @Nonnull List<Ip> getNextHops() {
    return _nextHops;
  }
}
