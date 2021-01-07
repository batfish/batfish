package org.batfish.datamodel.route.nh;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;

/**
 * Represent a generic routing next hop. There are many types of next hops: IPv4 concrete address,
 * interface, next hops to be looked up in a different VRF, null-routing next hops. Consumers of
 * this interface <em>should</em> be prepared to deal with different types of next hops.
 */
public interface NextHop extends Serializable {
  <T> T accept(NextHopVisitor<T> visitor);

  /**
   * Returns a {@link NextHop} based on next hop interface and next hop ip, both of which can be
   * nullable. If both are null, a {@link DiscardNextHop} is returned
   */
  static NextHop legacyConverter(@Nullable String nextHopInterface, @Nullable Ip nextHopIp) {
    if (nextHopInterface != null && !Route.UNSET_NEXT_HOP_INTERFACE.equals(nextHopInterface)) {
      if (nextHopInterface.equals(Interface.NULL_INTERFACE_NAME)) {
        return NextHopDiscard.instance();
      }
      if (nextHopIp != null && !Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp)) {
        return NextHopInterface.of(nextHopInterface, nextHopIp);
      } else {
        return NextHopInterface.of(nextHopInterface);
      }
    } else {
      if (nextHopIp != null && !Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp)) {
        return NextHopIp.of(nextHopIp);
      } else {
        throw new IllegalArgumentException(
            "Cannot construct a next hop when missing both a next hop IP and interface");
      }
    }
  }
}
