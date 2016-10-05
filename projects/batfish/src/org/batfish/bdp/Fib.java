package org.batfish.bdp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class Fib implements Serializable {

   private static final int MAX_DEPTH = 10;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Map<AbstractRoute, Map<String, Set<AbstractRoute>>> _nextHopInterfaces;

   private final Rib _rib;

   public Fib(Rib rib) {
      _rib = rib;
      _nextHopInterfaces = new HashMap<>();
      for (AbstractRoute route : rib.getRoutes()) {
         Map<String, Set<AbstractRoute>> nextHopInterfaces = new TreeMap<>();
         collectNextHopInterfaces(route, nextHopInterfaces, new HashSet<>(), 0);
         _nextHopInterfaces.put(route, nextHopInterfaces);
      }
   }

   private void collectNextHopInterfaces(AbstractRoute route,
         Map<String, Set<AbstractRoute>> nextHopInterfaces,
         Set<Prefix> seenNetworks, int depth) {
      Prefix network = route.getNetwork();
      if (seenNetworks.contains(network)) {
         return;
      }
      Set<Prefix> newSeenNetworks = new HashSet<>(seenNetworks);
      newSeenNetworks.add(network);
      if (depth > MAX_DEPTH) {
         throw new BatfishException(
               "Exceeded max route recursion depth: " + MAX_DEPTH);
      }
      Ip nextHopIp = route.getNextHopIp();
      if (nextHopIp != null) {
         Set<AbstractRoute> nextHopLongestPrefixMatchRoutes = _rib
               .longestPrefixMatch(nextHopIp);
         for (AbstractRoute nextHopLongestPrefixMatchRoute : nextHopLongestPrefixMatchRoutes) {
            collectNextHopInterfaces(nextHopLongestPrefixMatchRoute,
                  nextHopInterfaces, newSeenNetworks, depth + 1);
         }
      }
      else {
         String nextHopInterface = route.getNextHopInterface();
         if (nextHopInterface != null) {
            Set<AbstractRoute> nextHopInterfaceRoutes = nextHopInterfaces
                  .get(nextHopInterface);
            if (nextHopInterfaceRoutes == null) {
               nextHopInterfaceRoutes = new TreeSet<>();
               nextHopInterfaces.put(nextHopInterface, nextHopInterfaceRoutes);
            }
            nextHopInterfaceRoutes.add(route);
         }
         else {
            throw new BatfishException(
                  "Encountered route with neither nextHopIp nor nextHopInterface");
         }
      }
   }

   public Map<String, Set<AbstractRoute>> getNextHopInterfaces(Ip ip) {
      Map<String, Set<AbstractRoute>> nextHopInterfaces = new TreeMap<>();
      Set<AbstractRoute> nextHopRoutes = _rib.longestPrefixMatch(ip);
      for (AbstractRoute nextHopRoute : nextHopRoutes) {
         Map<String, Set<AbstractRoute>> currentNextHopInterfaces = _nextHopInterfaces
               .get(nextHopRoute);
         currentNextHopInterfaces
               .forEach((nextHopInterface, currentNextHopInterfaceRoutes) -> {
                  Set<AbstractRoute> nextHopInterfaceRoutes = nextHopInterfaces
                        .get(nextHopInterface);
                  if (nextHopInterfaceRoutes == null) {
                     nextHopInterfaceRoutes = new TreeSet<>();
                     nextHopInterfaces.put(nextHopInterface,
                           nextHopInterfaceRoutes);
                  }
                  nextHopInterfaceRoutes.addAll(currentNextHopInterfaceRoutes);
               });
      }
      return nextHopInterfaces;
   }

   public Map<AbstractRoute, Set<String>> getNextHopInterfacesByRoute(
         Ip dstIp) {
      Map<AbstractRoute, Set<String>> nextHopInterfacesByRoute = new HashMap<>();
      Set<AbstractRoute> nextHopRoutes = _rib.longestPrefixMatch(dstIp);
      for (AbstractRoute nextHopRoute : nextHopRoutes) {
         Set<String> nextHopInterfaces = _nextHopInterfaces.get(nextHopRoute)
               .keySet();
         nextHopInterfacesByRoute.put(nextHopRoute, nextHopInterfaces);
      }
      return nextHopInterfacesByRoute;
   }

}
