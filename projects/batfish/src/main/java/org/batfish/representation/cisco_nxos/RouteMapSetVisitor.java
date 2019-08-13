package org.batfish.representation.cisco_nxos;

/** A visitor of {@link RouteMapSet}. */
public interface RouteMapSetVisitor<T> {

  T visitRouteMapSetAsPathPrependLastAs(
      RouteMapSetAsPathPrependLastAs routeMapSetAsPathPrependLastAs);

  T visitRouteMapSetAsPathPrependLiteralAs(
      RouteMapSetAsPathPrependLiteralAs routeMapSetAsPathPrependLiteralAs);

  T visitRouteMapSetCommunity(RouteMapSetCommunity routeMapSetCommunity);

  T visitRouteMapSetIpNextHopLiteral(RouteMapSetIpNextHopLiteral routeMapSetIpNextHopLiteral);

  T visitRouteMapSetIpNextHopUnchanged(RouteMapSetIpNextHopUnchanged routeMapSetIpNextHopUnchanged);

  T visitRouteMapSetLocalPreference(RouteMapSetLocalPreference routeMapSetLocalPreference);

  T visitRouteMapSetMetric(RouteMapSetMetric routeMapSetMetric);

  T visitRouteMapSetMetricType(RouteMapSetMetricType routeMapSetMetricType);

  T visitRouteMapSetOrigin(RouteMapSetOrigin routeMapSetOrigin);

  T visitRouteMapSetTag(RouteMapSetTag routeMapSetTag);
}
