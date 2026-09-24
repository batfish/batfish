package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** An {@code nlri-route-type} match condition in a Junos routing policy. */
public final class PsFromNlriRouteType extends PsFrom {

  public PsFromNlriRouteType(int routeType) {
    _routeType = routeType;
  }

  public int getRouteType() {
    return _routeType;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    // TODO: Implement matching BGP NLRI route types.
    // https://www.juniper.net/documentation/us/en/software/junos/routing-policy/bgp/topics/example/mbpg-mvpn-family-based-damping.html
    return BooleanExprs.FALSE;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PsFromNlriRouteType && _routeType == ((PsFromNlriRouteType) o)._routeType;
  }

  @Override
  public int hashCode() {
    return _routeType;
  }

  private final int _routeType;
}
