package org.batfish.question.routes;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.batfish.datamodel.table.TableDiff.COL_KEY_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ADMIN_DISTANCES;
import static org.batfish.question.routes.RoutesAnswerer.COL_AS_PATHS;
import static org.batfish.question.routes.RoutesAnswerer.COL_BASE_PREFIX;
import static org.batfish.question.routes.RoutesAnswerer.COL_COMMUNITIES;
import static org.batfish.question.routes.RoutesAnswerer.COL_DELTA_PREFIX;
import static org.batfish.question.routes.RoutesAnswerer.COL_LOCAL_PREFS;
import static org.batfish.question.routes.RoutesAnswerer.COL_METRICS;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOPS;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_IPS;
import static org.batfish.question.routes.RoutesAnswerer.COL_NODE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGIN_PROTOCOLS;
import static org.batfish.question.routes.RoutesAnswerer.COL_PROTOCOLS;
import static org.batfish.question.routes.RoutesAnswerer.COL_TAGs;
import static org.batfish.question.routes.RoutesAnswerer.COL_VRF_NAME;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.question.routes.DiffRoutesOutput.KeyPresenceStatus;

public class RoutesAnswererUtil {

  /** Compute the next hop node for a given next hop IP. */
  @Nullable
  static String computeNextHopNode(
      @Nullable Ip nextHopIp, @Nullable Map<Ip, Set<String>> ipOwners) {
    if (nextHopIp == null || ipOwners == null) {
      return null;
    }
    // TODO: https://github.com/batfish/batfish/issues/1862
    return ipOwners
        .getOrDefault(nextHopIp, ImmutableSet.of())
        .stream()
        .min(Comparator.naturalOrder())
        .orElse(null);
  }

  /**
   * Takes in a {@link Map} of {@link RouteRowKey}s and a {@link SortedSet} of {@link
   * RouteRowAttribute}s and forms one {@link Row} per {@link RouteRowKey}
   *
   * @param bgpRawRows {@link Map} containing BGP routes in the form of {@link RouteRowKey} and
   *     {@link SortedSet} of {@link RouteRowAttribute}s
   * @return {@link Multiset} of {@link Row}s formed for the BGP routes
   */
  static Multiset<Row> getBgpRouteRows(Map<RouteRowKey, SortedSet<RouteRowAttribute>> bgpRawRows) {
    Multiset<Row> rows = HashMultiset.create();
    for (Entry<RouteRowKey, SortedSet<RouteRowAttribute>> entry : bgpRawRows.entrySet()) {
      Row.RowBuilder rowBuilder = Row.builder();
      RouteRowKey routeRowKey = entry.getKey();
      rowBuilder
          .put(COL_NODE, new Node(routeRowKey.getHostName()))
          .put(COL_VRF_NAME, routeRowKey.getVrfName())
          .put(COL_NETWORK, routeRowKey.getPrefix());

      List<String> nextHopsList = new ArrayList<>();
      List<Ip> nextHopsIpsList = new ArrayList<>();
      List<String> protocolsList = new ArrayList<>();
      List<String> asPathsList = new ArrayList<>();
      List<Long> metricsList = new ArrayList<>();
      List<Integer> localPrefsList = new ArrayList<>();
      List<String> communitiesList = new ArrayList<>();
      List<String> originProtocolsList = new ArrayList<>();
      List<Integer> tagsList = new ArrayList<>();

      for (RouteRowAttribute routeRowAttribute : entry.getValue()) {
        nextHopsList.add(routeRowAttribute.getNextHop());
        nextHopsIpsList.add(routeRowAttribute.getNextHopIp());
        protocolsList.add(routeRowAttribute.getProtocol());
        asPathsList.add(
            routeRowAttribute.getAsPath() == null
                ? null
                : routeRowAttribute.getAsPath().getAsPathString());
        metricsList.add(routeRowAttribute.getMetric());
        localPrefsList.add(routeRowAttribute.getLocalPreference());
        communitiesList.add(routeRowAttribute.getCommunities());
        originProtocolsList.add(routeRowAttribute.getOriginProtocol());
        tagsList.add(routeRowAttribute.getTag());
      }
      rowBuilder
          .put(COL_NEXT_HOPS, nextHopsList)
          .put(COL_NEXT_HOP_IPS, nextHopsIpsList)
          .put(COL_PROTOCOLS, protocolsList)
          .put(COL_AS_PATHS, asPathsList)
          .put(COL_METRICS, metricsList)
          .put(COL_LOCAL_PREFS, localPrefsList)
          .put(COL_COMMUNITIES, communitiesList)
          .put(COL_ORIGIN_PROTOCOLS, originProtocolsList)
          .put(COL_TAGs, tagsList);

      rows.add(rowBuilder.build());
    }
    return rows;
  }

  /**
   * Converts {@link List} of {@link DiffRoutesOutput} to {@link Row}s with one row corresponding to
   * each {@link DiffRoutesOutput}
   *
   * @param diffRoutesList {@link List} of {@link DiffRoutesOutput} for {@link BgpRoute}s
   * @return {@link Multiset} of {@link Row}s with a row generated for each {@link DiffRoutesOutput}
   *     in the input
   */
  static Multiset<Row> getBgpRouteRowsDiff(List<DiffRoutesOutput> diffRoutesList) {
    Multiset<Row> rows = HashMultiset.create();
    for (DiffRoutesOutput diffRoutesOutput : diffRoutesList) {
      Row.RowBuilder rowBuilder = Row.builder();
      RouteRowKey routeRowKey = diffRoutesOutput.getRouteRowKey();
      rowBuilder
          .put(COL_NODE, new Node(routeRowKey.getHostName()))
          .put(COL_VRF_NAME, routeRowKey.getVrfName())
          .put(COL_NETWORK, routeRowKey.getPrefix());

      List<String> nextHopsListBase = new ArrayList<>();
      List<String> nextHopsListRef = new ArrayList<>();
      List<Ip> nextHopsIpsListBase = new ArrayList<>();
      List<Ip> nextHopsIpsListRef = new ArrayList<>();
      List<String> protocolsListBase = new ArrayList<>();
      List<String> protocolsListRef = new ArrayList<>();
      List<String> asPathsListBase = new ArrayList<>();
      List<String> asPathsListRef = new ArrayList<>();
      List<Long> metricsListBase = new ArrayList<>();
      List<Long> metricsListRef = new ArrayList<>();
      List<Integer> localPrefsListBase = new ArrayList<>();
      List<Integer> localPrefsListRef = new ArrayList<>();
      List<String> communitiesListBase = new ArrayList<>();
      List<String> communitiesListRef = new ArrayList<>();
      List<String> originProtocolsListBase = new ArrayList<>();
      List<String> originProtocolsListRef = new ArrayList<>();
      List<Integer> tagsListBase = new ArrayList<>();
      List<Integer> tagsListRef = new ArrayList<>();

      for (List<RouteRowAttribute> routeRowAttributeInBaseAndRef :
          diffRoutesOutput.getDiffInAttributes()) {
        RouteRowAttribute routeRowAttributeBase = routeRowAttributeInBaseAndRef.get(0);
        RouteRowAttribute routeRowAttributeRef = routeRowAttributeInBaseAndRef.get(1);
        if (diffRoutesOutput.getKeyPresenceStatus().equals(KeyPresenceStatus.IN_BOTH)
            || diffRoutesOutput.getKeyPresenceStatus().equals(KeyPresenceStatus.ONLY_IN_SNAPSHOT)) {
          nextHopsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getNextHop());
          nextHopsIpsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getNextHopIp());
          protocolsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getProtocol());
          asPathsListBase.add(
              routeRowAttributeBase == null
                  ? null
                  : routeRowAttributeBase.getAsPath() == null
                      ? null
                      : routeRowAttributeBase.getAsPath().getAsPathString());
          metricsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getMetric());
          localPrefsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getLocalPreference());
          communitiesListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getCommunities());
          originProtocolsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getOriginProtocol());
          tagsListBase.add(routeRowAttributeBase == null ? null : routeRowAttributeBase.getTag());
        }
        if (diffRoutesOutput.getKeyPresenceStatus().equals(KeyPresenceStatus.IN_BOTH)
            || diffRoutesOutput
                .getKeyPresenceStatus()
                .equals(KeyPresenceStatus.ONLY_IN_REFERENCE)) {
          nextHopsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getNextHop());
          nextHopsIpsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getNextHopIp());
          protocolsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getProtocol());
          asPathsListRef.add(
              routeRowAttributeRef == null
                  ? null
                  : routeRowAttributeRef.getAsPath() == null
                      ? null
                      : routeRowAttributeRef.getAsPath().getAsPathString());
          metricsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getMetric());
          localPrefsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getLocalPreference());
          communitiesListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getCommunities());
          originProtocolsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getOriginProtocol());
          tagsListRef.add(routeRowAttributeRef == null ? null : routeRowAttributeRef.getTag());
        }
      }
      rowBuilder
          .put(COL_KEY_PRESENCE, diffRoutesOutput.getKeyPresenceStatus())
          .put(COL_BASE_PREFIX + COL_NEXT_HOPS, nextHopsListBase)
          .put(COL_DELTA_PREFIX + COL_NEXT_HOPS, nextHopsListRef)
          .put(COL_BASE_PREFIX + COL_NEXT_HOP_IPS, nextHopsIpsListBase)
          .put(COL_DELTA_PREFIX + COL_NEXT_HOP_IPS, nextHopsIpsListRef)
          .put(COL_BASE_PREFIX + COL_PROTOCOLS, protocolsListBase)
          .put(COL_DELTA_PREFIX + COL_PROTOCOLS, protocolsListRef)
          .put(COL_BASE_PREFIX + COL_AS_PATHS, asPathsListBase)
          .put(COL_DELTA_PREFIX + COL_AS_PATHS, asPathsListRef)
          .put(COL_BASE_PREFIX + COL_METRICS, metricsListBase)
          .put(COL_DELTA_PREFIX + COL_METRICS, metricsListRef)
          .put(COL_BASE_PREFIX + COL_LOCAL_PREFS, localPrefsListBase)
          .put(COL_DELTA_PREFIX + COL_LOCAL_PREFS, localPrefsListRef)
          .put(COL_BASE_PREFIX + COL_COMMUNITIES, communitiesListBase)
          .put(COL_DELTA_PREFIX + COL_COMMUNITIES, communitiesListRef)
          .put(COL_BASE_PREFIX + COL_ORIGIN_PROTOCOLS, originProtocolsListBase)
          .put(COL_DELTA_PREFIX + COL_ORIGIN_PROTOCOLS, originProtocolsListRef)
          .put(COL_BASE_PREFIX + COL_TAGs, tagsListBase)
          .put(COL_DELTA_PREFIX + COL_TAGs, tagsListRef);

      rows.add(rowBuilder.build());
    }
    return rows;
  }

  /**
   * Groups the matching BGP routes as per the input filters to a {@link Map} of {@link RouteRowKey}
   * and {@link SortedSet} of {@link RouteRowAttribute}s
   *
   * @param bgpRoutes all {@link BgpRoute}s
   * @param matchingNodes {@link Set} of nodes from which {@link BgpRoute}s are to be selected
   * @param network {@link Prefix} of the network used to filter the routees
   * @param protocolRegex protocols used to filter the {@link BgpRoute}s
   * @param vrfRegex Regex used to filter the routes based on {@link org.batfish.datamodel.Vrf}
   * @param ipOwners {@link Map} of {@link Ip} to {@link Set} of owner nodes
   * @return {@link Map} of {@link RouteRowKey} to a {@link SortedSet} of {@link RouteRowAttribute}s
   */
  static Map<RouteRowKey, SortedSet<RouteRowAttribute>> getBgpRibRoutes(
      Table<String, String, Set<BgpRoute>> bgpRoutes,
      Set<String> matchingNodes,
      @Nullable Prefix network,
      String protocolRegex,
      String vrfRegex,
      @Nullable Map<Ip, Set<String>> ipOwners) {
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> bgpRoutesGroupedByKey = new HashMap<>();
    Pattern compiledProtocolRegex = Pattern.compile(protocolRegex, Pattern.CASE_INSENSITIVE);
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    matchingNodes.forEach(
        hostname ->
            bgpRoutes
                .row(hostname)
                .forEach(
                    (vrfName, routes) -> {
                      if (compiledVrfRegex.matcher(vrfName).matches()) {
                        bgpRoutesGroupedByKey.putAll(
                            groupBgpRoutesByPrefix(
                                hostname,
                                vrfName,
                                routes,
                                ipOwners,
                                network,
                                compiledProtocolRegex));
                      }
                    }));
    return bgpRoutesGroupedByKey;
  }

  /**
   * Takes in a {@link Map} of {@link RouteRowKey}s and a {@link SortedSet} of {@link
   * RouteRowAttribute}s and forms one {@link Row} per {@link RouteRowKey}
   *
   * @param abstractRouteRawRows {@link Map} containing Main RIB routes in the form of {@link
   *     RouteRowKey} and {@link SortedSet} of {@link RouteRowAttribute}s
   * @return {@link Multiset} of {@link Row}s formed for the Main RIB routes
   */
  @Nonnull
  static Multiset<Row> getAbstractRouteRows(
      Map<RouteRowKey, SortedSet<RouteRowAttribute>> abstractRouteRawRows) {
    Multiset<Row> rows = HashMultiset.create();

    for (Entry<RouteRowKey, SortedSet<RouteRowAttribute>> entry : abstractRouteRawRows.entrySet()) {
      RouteRowKey routeRowKey = entry.getKey();
      Row.RowBuilder rowBuilder = Row.builder();
      rowBuilder
          .put(COL_NODE, new Node(routeRowKey.getHostName()))
          .put(COL_VRF_NAME, routeRowKey.getVrfName())
          .put(COL_NETWORK, routeRowKey.getPrefix());

      List<String> nextHopsList = new ArrayList<>();
      List<Ip> nextHopsIpsList = new ArrayList<>();
      List<String> protocolsList = new ArrayList<>();
      List<Integer> tagsList = new ArrayList<>();
      List<Integer> adminDistancesList = new ArrayList<>();
      List<Long> metricsList = new ArrayList<>();

      for (RouteRowAttribute routeRowAttribute : entry.getValue()) {
        nextHopsList.add(routeRowAttribute.getNextHop());
        nextHopsIpsList.add(routeRowAttribute.getNextHopIp());
        protocolsList.add(routeRowAttribute.getProtocol());
        tagsList.add(routeRowAttribute.getTag());
        adminDistancesList.add(routeRowAttribute.getAdminDistance());
        metricsList.add(routeRowAttribute.getMetric());
      }
      rowBuilder
          .put(COL_NEXT_HOPS, nextHopsList)
          .put(COL_NEXT_HOP_IPS, nextHopsIpsList)
          .put(COL_PROTOCOLS, protocolsList)
          .put(COL_TAGs, tagsList)
          .put(COL_ADMIN_DISTANCES, adminDistancesList)
          .put(COL_METRICS, metricsList);
      rows.add(rowBuilder.build());
    }
    return rows;
  }

  /**
   * Converts {@link List} of {@link DiffRoutesOutput} to {@link Row}s with one row corresponding to
   * each {@link DiffRoutesOutput}
   *
   * @param diffRoutesList {@link List} of {@link DiffRoutesOutput} for routes in Main RIB
   * @return {@link Multiset} of {@link Row}s with a row generated for each {@link DiffRoutesOutput}
   *     in the input
   */
  static Multiset<Row> getAbstractRouteRowsDiff(List<DiffRoutesOutput> diffRoutesList) {
    Multiset<Row> rows = HashMultiset.create();

    for (DiffRoutesOutput diffRoutesOutput : diffRoutesList) {
      RouteRowKey routeRowKey = diffRoutesOutput.getRouteRowKey();
      Row.RowBuilder rowBuilder = Row.builder();
      rowBuilder
          .put(COL_NODE, new Node(routeRowKey.getHostName()))
          .put(COL_VRF_NAME, routeRowKey.getVrfName())
          .put(COL_NETWORK, routeRowKey.getPrefix());

      List<String> nextHopsListBase = new ArrayList<>();
      List<String> nextHopsListRef = new ArrayList<>();
      List<Ip> nextHopsIpsListBase = new ArrayList<>();
      List<Ip> nextHopsIpsListRef = new ArrayList<>();
      List<String> protocolsListBase = new ArrayList<>();
      List<String> protocolsListRef = new ArrayList<>();
      List<Integer> adminDistancesListBase = new ArrayList<>();
      List<Integer> adminDistancesListRef = new ArrayList<>();
      List<Long> metricsListBase = new ArrayList<>();
      List<Long> metricsListRef = new ArrayList<>();
      List<Integer> tagsListBase = new ArrayList<>();
      List<Integer> tagsListRef = new ArrayList<>();

      for (List<RouteRowAttribute> routeRowAttributeInBaseAndRef :
          diffRoutesOutput.getDiffInAttributes()) {
        RouteRowAttribute routeRowAttributeBase = routeRowAttributeInBaseAndRef.get(0);
        RouteRowAttribute routeRowAttributeRef = routeRowAttributeInBaseAndRef.get(1);
        if (diffRoutesOutput.getKeyPresenceStatus().equals(KeyPresenceStatus.IN_BOTH)
            || diffRoutesOutput.getKeyPresenceStatus().equals(KeyPresenceStatus.ONLY_IN_SNAPSHOT)) {
          nextHopsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getNextHop());
          nextHopsIpsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getNextHopIp());
          protocolsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getProtocol());
          metricsListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getMetric());
          adminDistancesListBase.add(
              routeRowAttributeBase == null ? null : routeRowAttributeBase.getAdminDistance());
          tagsListBase.add(routeRowAttributeBase == null ? null : routeRowAttributeBase.getTag());
        }
        if (diffRoutesOutput.getKeyPresenceStatus().equals(KeyPresenceStatus.IN_BOTH)
            || diffRoutesOutput
                .getKeyPresenceStatus()
                .equals(KeyPresenceStatus.ONLY_IN_REFERENCE)) {
          nextHopsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getNextHop());
          nextHopsIpsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getNextHopIp());
          protocolsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getProtocol());
          metricsListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getMetric());
          adminDistancesListRef.add(
              routeRowAttributeRef == null ? null : routeRowAttributeRef.getAdminDistance());
          tagsListRef.add(routeRowAttributeRef == null ? null : routeRowAttributeRef.getTag());
        }
      }
      rowBuilder
          .put(COL_KEY_PRESENCE, diffRoutesOutput.getKeyPresenceStatus())
          .put(COL_BASE_PREFIX + COL_NEXT_HOPS, nextHopsListBase)
          .put(COL_DELTA_PREFIX + COL_NEXT_HOPS, nextHopsListRef)
          .put(COL_BASE_PREFIX + COL_NEXT_HOP_IPS, nextHopsIpsListBase)
          .put(COL_DELTA_PREFIX + COL_NEXT_HOP_IPS, nextHopsIpsListRef)
          .put(COL_BASE_PREFIX + COL_PROTOCOLS, protocolsListBase)
          .put(COL_DELTA_PREFIX + COL_PROTOCOLS, protocolsListRef)
          .put(COL_BASE_PREFIX + COL_METRICS, metricsListBase)
          .put(COL_DELTA_PREFIX + COL_METRICS, metricsListRef)
          .put(COL_BASE_PREFIX + COL_ADMIN_DISTANCES, adminDistancesListBase)
          .put(COL_DELTA_PREFIX + COL_ADMIN_DISTANCES, adminDistancesListRef)
          .put(COL_BASE_PREFIX + COL_TAGs, tagsListBase)
          .put(COL_DELTA_PREFIX + COL_TAGs, tagsListRef);

      rows.add(rowBuilder.build());
    }
    return rows;
  }

  /**
   * Groups the matching routes as per the input filters to a {@link Map} of {@link RouteRowKey} and
   * {@link SortedSet} of {@link RouteRowAttribute}s
   *
   * @param ribs {@link SortedMap} of routes in all RIBs
   * @param matchingNodes {@link Set} of nodes from which {@link AbstractRoute}s are to be selected
   * @param network {@link Prefix} of the network used to filter the routes
   * @param protocolRegex protocols used to filter the {@link AbstractRoute}s
   * @param vrfRegex Regex used to filter the routes based on {@link org.batfish.datamodel.Vrf}
   * @param ipOwners {@link Map} of {@link Ip} to {@link Set} of owner nodes
   * @return {@link Map} of {@link RouteRowKey} to a {@link SortedSet} of {@link RouteRowAttribute}s
   */
  static Map<RouteRowKey, SortedSet<RouteRowAttribute>> getMainRibRoutes(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Set<String> matchingNodes,
      @Nullable Prefix network,
      String protocolRegex,
      String vrfRegex,
      @Nullable Map<Ip, Set<String>> ipOwners) {
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> abstractRoutesGroupedByKey = new HashMap<>();
    Pattern compiledProtocolRegex = Pattern.compile(protocolRegex, Pattern.CASE_INSENSITIVE);
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    ribs.forEach(
        (node, vrfMap) -> {
          if (matchingNodes.contains(node)) {
            vrfMap.forEach(
                (vrfName, rib) -> {
                  if (compiledVrfRegex.matcher(vrfName).matches()) {
                    abstractRoutesGroupedByKey.putAll(
                        groupRoutesByPrefix(
                            node,
                            vrfName,
                            rib.getRoutes(),
                            ipOwners,
                            network,
                            compiledProtocolRegex));
                  }
                });
          }
        });
    return abstractRoutesGroupedByKey;
  }

  /**
   * Given a {@link Set} of {@link AbstractRoute}s, groups the routes by the fields of {@link
   * RouteRowKey} and for the routes with the same key, sorts them according to {@link
   * RouteRowAttribute}
   *
   * @param hostName Hostname for the node containing the routes
   * @param vrfName VRF name of the host containing the routes
   * @param routes {@link Set} of {@link AbstractRoute}s which need to be processed
   * @return {@link Map} with {@link RouteRowKey}s and corresponding {@link SortedSet} of {@link
   *     RouteRowAttribute}s
   */
  static Map<RouteRowKey, SortedSet<RouteRowAttribute>> groupRoutesByPrefix(
      String hostName,
      String vrfName,
      Set<AbstractRoute> routes,
      @Nullable Map<Ip, Set<String>> ipOwners,
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
   * Given a {@link Set} of {@link BgpRoute}s, groups the routes by the fields of {@link
   * RouteRowKey} and for the routes with the same key, sorts them according to {@link
   * RouteRowAttribute}
   *
   * @param hostName Hostname for the node containing the routes
   * @param vrfName VRF name of the host containing the routes
   * @param routes {@link Set} of {@link BgpRoute}s which need to be processed
   * @return {@link Map} with {@link RouteRowKey}s and corresponding {@link SortedSet} of {@link
   *     RouteRowAttribute}s
   */
  static Map<RouteRowKey, SortedSet<RouteRowAttribute>> groupBgpRoutesByPrefix(
      String hostName,
      String vrfName,
      Set<BgpRoute> routes,
      @Nullable Map<Ip, Set<String>> ipOwners,
      @Nullable Prefix network,
      Pattern compiledProtocolRegex) {
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> bgpRoutesGroupedByPrefix = new HashMap<>();
    for (BgpRoute bgpRoute : routes) {
      if ((network == null || network.equals(bgpRoute.getNetwork()))
          && compiledProtocolRegex.matcher(bgpRoute.getProtocol().protocolName()).matches()) {

        RouteRowKey routeRowKey = new RouteRowKey(hostName, vrfName, bgpRoute.getNetwork());
        RouteRowAttribute routeRowAttribute =
            RouteRowAttribute.builder()
                .setProtocol(bgpRoute.getProtocol().protocolName())
                .setNextHopIp(bgpRoute.getNextHopIp())
                .setNextHop(computeNextHopNode(bgpRoute.getNextHopIp(), ipOwners))
                .setAsPath(bgpRoute.getAsPath())
                .setMetric(bgpRoute.getMetric())
                .setLocalPreference(bgpRoute.getLocalPreference())
                .setCommunities(
                    String.join(
                        ", ",
                        bgpRoute
                            .getCommunities()
                            .stream()
                            .map(CommonUtil::longToCommunity)
                            .collect(toImmutableList())))
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
    }
    return bgpRoutesGroupedByPrefix;
  }

  /**
   * Given two {@link List}s of {@link RouteRowAttribute}s (which should be sorted), outputs a
   * 2-dimensional list with two columns
   *
   * <p>In each row of the output 2-dimensional {@link List} the two columns contain matching {@link
   * RouteRowAttribute}s and for {@link RouteRowAttribute}s with no matching value in the other
   * routeRowAttributes {@link List} the other column contains a null.
   *
   * @param routeRowAttributes1 Sorted {@link List} of first {@link RouteRowAttribute}s
   * @param routeRowAttributes2 Sorted {@link List} of second {@link RouteRowAttribute}s
   * @return A 2-dimensional (nested) {@link List} with the inner list of size 2
   */
  static List<List<RouteRowAttribute>> alignRouteRowAttributes(
      List<RouteRowAttribute> routeRowAttributes1, List<RouteRowAttribute> routeRowAttributes2) {
    List<List<RouteRowAttribute>> alignedRouteRowAttrs = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < routeRowAttributes1.size() && j < routeRowAttributes2.size()) {
      RouteRowAttribute routeRowAttribute1 = routeRowAttributes1.get(i);
      RouteRowAttribute routeRowAttribute2 = routeRowAttributes2.get(j);
      if (routeRowAttribute1.compareTo(routeRowAttribute2) < 0) {
        i++;
        alignedRouteRowAttrs.add(Lists.newArrayList(routeRowAttribute1, null));
      } else if (routeRowAttribute1.compareTo(routeRowAttribute2) > 0) {
        j++;
        alignedRouteRowAttrs.add(Lists.newArrayList(null, routeRowAttribute2));
      } else {
        i++;
        j++;
        alignedRouteRowAttrs.add(Lists.newArrayList(routeRowAttribute1, routeRowAttribute2));
      }
    }

    // adding one sided pairs for whichever list remains un-traversed
    // only one of these loops will execute
    while (i < routeRowAttributes1.size()) {
      alignedRouteRowAttrs.add(Lists.newArrayList(routeRowAttributes1.get(i++), null));
    }
    while (j < routeRowAttributes2.size()) {
      alignedRouteRowAttrs.add(Lists.newArrayList(null, routeRowAttributes2.get(j++)));
    }

    return alignedRouteRowAttrs;
  }

  /**
   * Given two {@link Map}s containing mapping from {@link RouteRowKey} to {@link SortedSet} of
   * {@link RouteRowAttribute}s produces diff in the form of a {@link List} of {@link
   * DiffRoutesOutput}
   *
   * @param routesInBase {@link Map} containing mapping from {@link RouteRowKey} to {@link
   *     SortedSet} of {@link RouteRowAttribute}s in the base snapshot
   * @param routesInRef {@link Map} containing mapping from {@link RouteRowKey} to {@link SortedSet}
   *     of {@link RouteRowAttribute}s in the reference snapshot
   * @return {@link List} of {@link DiffRoutesOutput}
   */
  static List<DiffRoutesOutput> getRoutesDiff(
      Map<RouteRowKey, SortedSet<RouteRowAttribute>> routesInBase,
      Map<RouteRowKey, SortedSet<RouteRowAttribute>> routesInRef) {
    Set<RouteRowKey> allRouteKeys = new HashSet<>(routesInBase.keySet());
    allRouteKeys.addAll(routesInRef.keySet());

    List<DiffRoutesOutput> listOfDiffPerKeys = new ArrayList<>();
    for (RouteRowKey routeRowKey : allRouteKeys) {
      if (routesInBase.containsKey(routeRowKey) && routesInRef.containsKey(routeRowKey)) {
        SortedSet<RouteRowAttribute> routeRowAttributesInBase = routesInBase.get(routeRowKey);
        SortedSet<RouteRowAttribute> routeRowAttributesInRef = routesInRef.get(routeRowKey);
        if (!routeRowAttributesInBase.equals(routeRowAttributesInRef)) {
          listOfDiffPerKeys.add(
              new DiffRoutesOutput(
                  routeRowKey,
                  alignRouteRowAttributes(
                      new ArrayList<>(routeRowAttributesInBase),
                      new ArrayList<>(routeRowAttributesInRef)),
                  KeyPresenceStatus.IN_BOTH));
        }
      } else if (routesInBase.containsKey(routeRowKey)) {
        SortedSet<RouteRowAttribute> routeRowAttributesInBase = routesInBase.get(routeRowKey);
        // reference route attribute in the 2 column diff Matrix will be unset for elements
        List<List<RouteRowAttribute>> diffMatrix =
            routeRowAttributesInBase
                .stream()
                .map(routeRowAttribute -> Lists.newArrayList(routeRowAttribute, null))
                .collect(Collectors.toList());
        listOfDiffPerKeys.add(
            new DiffRoutesOutput(routeRowKey, diffMatrix, KeyPresenceStatus.ONLY_IN_SNAPSHOT));
      } else {
        SortedSet<RouteRowAttribute> routeRowAttributesInRef = routesInRef.get(routeRowKey);
        // base route attribute in the 2 column diff Matrix will be unset for elements
        List<List<RouteRowAttribute>> diffMatrix =
            routeRowAttributesInRef
                .stream()
                .map(routeRowAttribute -> Lists.newArrayList(null, routeRowAttribute))
                .collect(Collectors.toList());
        listOfDiffPerKeys.add(
            new DiffRoutesOutput(routeRowKey, diffMatrix, KeyPresenceStatus.ONLY_IN_REFERENCE));
      }
    }
    return listOfDiffPerKeys;
  }
}
