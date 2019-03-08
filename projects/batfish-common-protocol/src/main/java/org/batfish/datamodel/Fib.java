package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public interface Fib extends Serializable {

  /** Mapping: route -&gt; nexthopinterface -&gt; resolved nextHopIp -&gt; interfaceRoutes */
  @Nonnull
  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfaces();

  /**
   * Set of interfaces used to forward traffic destined to this IP.
   *
   * @deprecated in favor of more general {@link Fib#get(Ip)}
   */
  @Nonnull
  @Deprecated
  Set<String> getNextHopInterfaces(Ip ip);

  /**
   * Return a set of {@link FibEntry fib entries} that match a given IP (using longest prefix match)
   */
  @Nonnull
  Set<FibEntry> get(Ip ip);

  /** Return the set of all entries */
  @Nonnull
  Set<FibEntry> allEntries();

  /**
   * Mapping: matching route -&gt; nexthopinterface -&gt; resolved nextHopIP -&gt; interfaceRoutes
   *
   * @deprecated in favor of more general {@link Fib#get(Ip)}
   */
  @Nonnull
  @Deprecated
  Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfacesByRoute(
      Ip dstIp);

  @Nonnull
  Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface();
}
