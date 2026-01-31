package org.batfish.vendor.arista.representation;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

/** A Cisco {@link RouteMapMatchLine} that matches routes with the given source protocol. */
public class RouteMapMatchSourceProtocolLine extends RouteMapMatchLine {

  private Set<RoutingProtocol> _protocols;

  public RouteMapMatchSourceProtocolLine(Collection<RoutingProtocol> protocols) {
    _protocols = ImmutableSet.copyOf(protocols);
  }

  public Set<RoutingProtocol> getProtocols() {
    return _protocols;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, AristaConfiguration cc, Warnings w) {
    // When evaluating a Route Policy, the "source protocol" is really the protocol on which the
    // route map is being evaluated.
    return new MatchProtocol(_protocols);
  }
}
