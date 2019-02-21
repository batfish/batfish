package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public interface Fib extends Serializable {

  /** Mapping: route -&gt; nexthopinterface -&gt; resolved nextHopIp -&gt; interfaceRoutes */
  @Nonnull
  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfaces();

  /** Set of interfaces used to forward traffic destined to this IP. */
  @Nonnull
  Set<String> getNextHopInterfaces(Ip ip);

  /**
   * Mapping: matching route -&gt; nexthopinterface -&gt; resolved nextHopIP -&gt; interfaceRoutes
   */
  @Nonnull
  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfacesByRoute(
      Ip dstIp);

  @Nonnull
  Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface();
}
