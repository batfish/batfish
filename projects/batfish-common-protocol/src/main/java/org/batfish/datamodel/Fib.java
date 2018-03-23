package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface Fib extends Serializable {

  /** Mapping: route -> nexthopinterface -> nextHopIp -> interfaceRoutes */
  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfaces();

  Map<String, Map<Ip, Set<AbstractRoute>>> getNextHopInterfaces(Ip ip);

  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfacesByRoute(
      Ip dstIp);

  Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface();
}
