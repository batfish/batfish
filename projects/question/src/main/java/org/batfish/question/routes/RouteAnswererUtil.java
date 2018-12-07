package org.batfish.question.routes;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.batfish.question.routes.RoutesAnswerer.computeNextHopNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class RouteAnswererUtil {

  /**
   * Groups and sorts {@link RouteRowAttribute}s for {@link AbstractRoute}s in the main RIB by
   * {@link RouteRowKey}
   *
   * @param hostName Hostname for the node containing the routes
   * @param vrfName VRF name of the host containing the routes
   * @param routes {@link AbstractRoute}s which need to be grouped by fields of {@link RouteRowKey}
   * @return {@link Map} containing mapping from {@link RouteRowKey} to {@link SortedSet} of {@link
   *     RouteRowAttribute}s
   */
  public static Map<RouteRowKey, SortedSet<RouteRowAttribute>> groupRoutesByPrefix(
      String hostName,
      String vrfName,
      Set<AbstractRoute> routes,
      Map<Ip, Set<String>> ipOwners,
      @Nullable Prefix network,
      Pattern compiledProtocolRegex) {
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> routesGroupedByPrefix = new HashMap<>();
    for (AbstractRoute route : routes) {
      if ((network == null || network.equals(route.getNetwork()))
          && compiledProtocolRegex.matcher(route.getProtocol().protocolName()).matches()) {

        RouteRowKey routeRowKey = new RouteRowKey(hostName, vrfName, route.getNetwork());
        RouteRowAttribute routeRowAttribute =
            RouteRowAttribute.builder()
                .setProtocol(
                    route.getProtocol() != null ? route.getProtocol().protocolName() : null)
                .setNextHopIp(route.getNextHopIp())
                .setNextHop(computeNextHopNode(route.getNextHopIp(), ipOwners))
                .setAdminDistance(route.getAdministrativeCost())
                .setMetric(route.getMetric())
                .setTag(route.getTag())
                .build();
        routesGroupedByPrefix
            .computeIfAbsent(routeRowKey, k -> new TreeSet<>())
            .add(routeRowAttribute);
      }
    }
    return routesGroupedByPrefix;
  }

  /**
   * Groups and sorts {@link RouteRowAttribute}s for {@link org.batfish.datamodel.BgpRoute}s in the
   * BGP RIB by {@link RouteRowKey}
   *
   * @param hostName Hostname for the node containing the routes
   * @param vrfName VRF name of the host containing the routes
   * @param routes {@link Set} of {@link BgpRoute}s which need to be grouped by fields of {@link
   *     RouteRowKey}
   * @return {@link Map} containing mapping from {@link RouteRowKey} to {@link SortedSet} of {@link
   *     RouteRowAttribute}s
   */
  public Map<RouteRowKey, SortedSet<RouteRowAttribute>> groupBgpRoutesByPrefix(
      String hostName, String vrfName, Set<BgpRoute> routes) {
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> bgpRoutesGroupedByPrefix = new HashMap<>();
    for (BgpRoute bgpRoute : routes) {
      RouteRowKey routeRowKey = new RouteRowKey(hostName, vrfName, bgpRoute.getNetwork());
      RouteRowAttribute routeRowAttribute =
          RouteRowAttribute.builder()
              .setProtocol(bgpRoute.getProtocol().protocolName())
              .setNextHopIp(bgpRoute.getNextHopIp())
              .setAsPath(bgpRoute.getAsPath().toString())
              .setMetric(bgpRoute.getMetric())
              .setLocalPreference(bgpRoute.getLocalPreference())
              .setCommunities(
                  bgpRoute
                      .getCommunities()
                      .stream()
                      .map(CommonUtil::longToCommunity)
                      .collect(toImmutableList()))
              .setOriginProtocol(
                  bgpRoute.getSrcProtocol() != null
                      ? bgpRoute.getSrcProtocol().protocolName()
                      : null)
              .setTag(bgpRoute.getTag())
              .build();
      bgpRoutesGroupedByPrefix
          .computeIfAbsent(routeRowKey, k -> new TreeSet<>())
          .add(routeRowAttribute);
    }
    return bgpRoutesGroupedByPrefix;
  }
}
