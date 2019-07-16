package org.batfish.representation.cisco_nxos;

/** A visitor of {@link RouteMapMatch}. */
public interface RouteMapMatchVisitor<T> {

  T visitRouteMapMatchAsPath(RouteMapMatchAsPath routeMapMatchAsPath);

  T visitRouteMapMatchCommunity(RouteMapMatchCommunity routeMapMatchCommunity);

  T visitRouteMapMatchInterface(RouteMapMatchInterface routeMapMatchInterface);

  T visitRouteMapMatchIpAddress(RouteMapMatchIpAddress routeMapMatchIpAddress);

  T visitRouteMapMatchIpAddressPrefixList(
      RouteMapMatchIpAddressPrefixList routeMapMatchIpAddressPrefixList);

  T visitRouteMapMatchMetric(RouteMapMatchMetric routeMapMatchMetric);

  T visitRouteMapMatchTag(RouteMapMatchTag routeMapMatchTag);
}
