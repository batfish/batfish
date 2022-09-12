package org.batfish.question.routes;

public interface RouteRowSecondaryKeyVisitor<R> {
  R visitBgpRouteRowSecondaryKey(BgpRouteRowSecondaryKey bgpRouteRowSecondaryKey);

  R visitEvpnRouteRowSecondaryKey(EvpnRouteRowSecondaryKey evpnRouteRowSecondaryKey);

  R visitMainRibRouteRowSecondaryKey(MainRibRouteRowSecondaryKey mainRibRouteRowSecondaryKey);
}
