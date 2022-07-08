package org.batfish.question.routes;

public interface RouteRowSecondaryKeyVisitor<R> {
  R visitBgpRouteRowSecondaryKey(BgpRouteRowSecondaryKey bgpRouteRowSecondaryKey);

  R visitMainRibRouteRowSecondaryKey(MainRibRouteRowSecondaryKey mainRibRouteRowSecondaryKey);
}
