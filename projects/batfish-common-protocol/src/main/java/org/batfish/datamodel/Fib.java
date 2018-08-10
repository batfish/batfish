package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public interface Fib extends Serializable {

  /** Mapping: route -&gt; nexthopinterface -&gt; nextHopIp -&gt; interfaceRoutes */
  @Nonnull
  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfaces();

  /** Mapping: nextHopInterface -&gt; nextHopIp -&gt; interfaceRoutes */
  @Nonnull
  Map<String, Map<Ip, Set<AbstractRoute>>> getNextHopInterfaces(Ip ip);

  @Nonnull
  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfacesByRoute(
      Ip dstIp);

  @Nonnull
  Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface();
}
