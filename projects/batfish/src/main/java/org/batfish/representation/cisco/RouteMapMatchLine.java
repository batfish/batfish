package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

public abstract class RouteMapMatchLine implements Serializable {

  public abstract BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w);

  public abstract <T> T accept(RouteMapMatchLineVisitor<T> visitor);

  public interface RouteMapMatchLineVisitor<T> {
    default T visit(RouteMapMatchLine line) {
      return line.accept(this);
    }

    T visitRouteMapMatchAsPathAccessListLine(RouteMapMatchAsPathAccessListLine line);

    T visitRouteMapMatchCommunityListLine(RouteMapMatchCommunityListLine line);

    T visitRouteMapMatchExtcommunityLine(RouteMapMatchExtcommunityLine line);

    T visitRouteMapMatchIpAccessListLine(RouteMapMatchIpAccessListLine line);

    T visitRouteMapMatchIpPrefixListLine(RouteMapMatchIpPrefixListLine line);

    T visitRouteMapMatchIpv6AccessListLine(RouteMapMatchIpv6AccessListLine line);

    T visitRouteMapMatchIpv6PrefixListLine(RouteMapMatchIpv6PrefixListLine line);

    T visitRouteMapMatchSourceProtocolLine(RouteMapMatchSourceProtocolLine line);

    T visitRouteMapMatchTagLine(RouteMapMatchTagLine line);
  }
}
