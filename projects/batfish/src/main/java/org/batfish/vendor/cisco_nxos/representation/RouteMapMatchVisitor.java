package org.batfish.vendor.cisco_nxos.representation;

/** A visitor of {@link RouteMapMatch}. */
public interface RouteMapMatchVisitor<T> {

  T visitRouteMapMatchAsNumber(RouteMapMatchAsNumber routeMapMatchAsNumber);

  T visitRouteMapMatchAsPath(RouteMapMatchAsPath routeMapMatchAsPath);

  T visitRouteMapMatchCommunity(RouteMapMatchCommunity routeMapMatchCommunity);

  T visitRouteMapMatchInterface(RouteMapMatchInterface routeMapMatchInterface);

  T visitRouteMapMatchIpAddress(RouteMapMatchIpAddress routeMapMatchIpAddress);

  T visitRouteMapMatchIpAddressPrefixList(
      RouteMapMatchIpAddressPrefixList routeMapMatchIpAddressPrefixList);

  T visitRouteMapMatchIpMulticast(RouteMapMatchIpMulticast routeMapMatchIpMulticast);

  T visitRouteMapMatchIpv6Address(RouteMapMatchIpv6Address routeMapMatchIpv6Address);

  T visitRouteMapMatchIpv6AddressPrefixList(
      RouteMapMatchIpv6AddressPrefixList routeMapMatchIpv6AddressPrefixList);

  T visitRouteMapMatchMetric(RouteMapMatchMetric routeMapMatchMetric);

  T visitRouteMapMatchRouteType(RouteMapMatchRouteType routeMapMatchRouteType);

  T visitRouteMapMatchSourceProtocol(RouteMapMatchSourceProtocol routeMapMatchSourceProtocol);

  T visitRouteMapMatchTag(RouteMapMatchTag routeMapMatchTag);

  T visitRouteMapMatchVlan(RouteMapMatchVlan routeMapMatchVlan);
}
