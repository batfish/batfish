package org.batfish.question.routes;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.batfish.datamodel.table.TableDiff.COL_BASE_PREFIX;
import static org.batfish.datamodel.table.TableDiff.COL_DELTA_PREFIX;
import static org.batfish.question.routes.RoutesAnswerer.COL_ADMIN_DISTANCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_AS_PATH;
import static org.batfish.question.routes.RoutesAnswerer.COL_COMMUNITIES;
import static org.batfish.question.routes.RoutesAnswerer.COL_LOCAL_PREF;
import static org.batfish.question.routes.RoutesAnswerer.COL_METRIC;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NODE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGIN_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_NETWORK_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_TAG;
import static org.batfish.question.routes.RoutesAnswerer.COL_VRF_NAME;
import static org.batfish.question.routes.RoutesAnswerer.getTableMetadata;

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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.question.routes.DiffRoutesOutput.PresenceStatus;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;

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
   * Returns a {@link Multiset} of {@link Row}s for all routes present in all RIBs
   *
   * @param ribs {@link Map} representing all RIBs of all nodes
   * @param matchingNodes {@link Set} of hostnames of nodes whose routes are to be returned
   * @param network {@link Prefix} of the network used to filter the routes
   * @param protocolRegex protocols used to filter the routes
   * @param vrfRegex Regex used to filter the VRF of routes
   * @param ipOwners {@link Map} of {@link Ip} to {@link Set} of owner nodes
   * @return {@link Multiset} of {@link Row}s representing the routes
   */
  static Multiset<Row> getMainRibRoutes(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Set<String> matchingNodes,
      @Nullable Prefix network,
      String protocolRegex,
      String vrfRegex,
      @Nullable Map<Ip, Set<String>> ipOwners) {
    Multiset<Row> rows = HashMultiset.create();
    Pattern compiledProtocolRegex = Pattern.compile(protocolRegex, Pattern.CASE_INSENSITIVE);
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    Map<String, ColumnMetadata> columnMetadataMap =
        getTableMetadata(RibProtocol.MAIN).toColumnMap();
    ribs.forEach(
        (node, vrfMap) -> {
          if (matchingNodes.contains(node)) {
            vrfMap.forEach(
                (vrfName, rib) -> {
                  if (compiledVrfRegex.matcher(vrfName).matches()) {
                    rib.getRoutes()
                        .stream()
                        .filter(
                            route ->
                                (network == null || network.equals(route.getNetwork()))
                                    && compiledProtocolRegex
                                        .matcher(route.getProtocol().protocolName())
                                        .matches())
                        .forEach(
                            route ->
                                rows.add(
                                    abstractRouteToRow(
                                        node, vrfName, route, columnMetadataMap, ipOwners)));
                  }
                });
          }
        });
    return rows;
  }

  /**
   * Filters a {@link Table} of {@link BgpRoute}s to produce a {@link Multiset} of rows
   *
   * @param bgpRoutes {@link Table} of all {@link BgpRoute}s
   * @param ribProtocol {@link RibProtocol}, either {@link RibProtocol#BGP} or {@link
   *     RibProtocol#BGPMP}
   * @param matchingNodes {@link Set} of nodes from which {@link BgpRoute}s are to be selected
   * @param network {@link Prefix} of the network used to filter the routes
   * @param protocolRegex protocols used to filter the {@link BgpRoute}s
   * @param vrfRegex Regex used to filter the routes based on {@link org.batfish.datamodel.Vrf}
   * @return {@link Multiset} of {@link Row}s representing the routes
   */
  static Multiset<Row> getBgpRibRoutes(
      Table<String, String, Set<BgpRoute>> bgpRoutes,
      RibProtocol ribProtocol,
      Set<String> matchingNodes,
      @Nullable Prefix network,
      String protocolRegex,
      String vrfRegex) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, ColumnMetadata> columnMetadataMap = getTableMetadata(ribProtocol).toColumnMap();
    Pattern compiledProtocolRegex = Pattern.compile(protocolRegex, Pattern.CASE_INSENSITIVE);
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    matchingNodes.forEach(
        hostname ->
            bgpRoutes
                .row(hostname)
                .forEach(
                    (vrfName, routes) -> {
                      if (compiledVrfRegex.matcher(vrfName).matches()) {
                        routes
                            .stream()
                            .filter(
                                route ->
                                    (network == null || network.equals(route.getNetwork()))
                                        && compiledProtocolRegex
                                            .matcher(route.getProtocol().protocolName())
                                            .matches())
                            .forEach(
                                route ->
                                    rows.add(
                                        bgpRouteToRow(
                                            hostname, vrfName, route, columnMetadataMap)));
                      }
                    }));
    return rows;
  }

  /**
   * Converts a {@link AbstractRoute} to a {@link Row}
   *
   * @param hostName {@link String} host-name of the node containing the route
   * @param vrfName {@link String} name of the VRF containing the route
   * @param abstractRoute {@link AbstractRoute} to convert
   * @param columnMetadataMap Column metadata of the columns for this {@link Row} c
   * @return {@link Row} representing the {@link AbstractRoute}
   */
  private static Row abstractRouteToRow(
      String hostName,
      String vrfName,
      AbstractRoute abstractRoute,
      Map<String, ColumnMetadata> columnMetadataMap,
      @Nullable Map<Ip, Set<String>> ipOwners) {
    return Row.builder(columnMetadataMap)
        .put(COL_NODE, new Node(hostName))
        .put(COL_VRF_NAME, vrfName)
        .put(COL_NETWORK, abstractRoute.getNetwork())
        .put(COL_NEXT_HOP_IP, abstractRoute.getNextHopIp())
        .put(COL_NEXT_HOP, computeNextHopNode(abstractRoute.getNextHopIp(), ipOwners))
        .put(COL_PROTOCOL, abstractRoute.getProtocol())
        .put(
            COL_TAG, abstractRoute.getTag() == AbstractRoute.NO_TAG ? null : abstractRoute.getTag())
        .put(COL_ADMIN_DISTANCE, abstractRoute.getAdministrativeCost())
        .put(COL_METRIC, abstractRoute.getMetric())
        .build();
  }

  /**
   * Converts a {@link BgpRoute} to a {@link Row}
   *
   * @param hostName {@link String} host-name of the node containing the bgpRoute
   * @param vrfName {@link String} name of the VRF containing the bgpRoute
   * @param bgpRoute {@link BgpRoute} BGP route to convert
   * @param columnMetadataMap Column metadata of the columns for this {@link Row}
   * @return {@link Row} representing the {@link BgpRoute}
   */
  static Row bgpRouteToRow(
      String hostName,
      String vrfName,
      BgpRoute bgpRoute,
      Map<String, ColumnMetadata> columnMetadataMap) {
    return Row.builder(columnMetadataMap)
        .put(COL_NODE, new Node(hostName))
        .put(COL_VRF_NAME, vrfName)
        .put(COL_NETWORK, bgpRoute.getNetwork())
        .put(COL_NEXT_HOP_IP, bgpRoute.getNextHopIp())
        .put(COL_PROTOCOL, bgpRoute.getProtocol())
        .put(COL_AS_PATH, bgpRoute.getAsPath().getAsPathString())
        .put(COL_METRIC, bgpRoute.getMetric())
        .put(COL_LOCAL_PREF, bgpRoute.getLocalPreference())
        .put(
            COL_COMMUNITIES,
            bgpRoute
                .getCommunities()
                .stream()
                .map(CommonUtil::longToCommunity)
                .collect(toImmutableList()))
        .put(COL_ORIGIN_PROTOCOL, bgpRoute.getSrcProtocol())
        .put(COL_TAG, bgpRoute.getTag() == Route.UNSET_ROUTE_TAG ? null : bgpRoute.getTag())
        .build();
  }

  /**
   * Converts {@link List} of {@link DiffRoutesOutput} to {@link Row}s with one row corresponding to
   * each {@link DiffRoutesOutput#_diffInAttributes} of the {@link DiffRoutesOutput}
   *
   * @param diffRoutesList {@link List} of {@link DiffRoutesOutput} for {@link BgpRoute}s
   * @return {@link Multiset} of {@link Row}s
   */
  static Multiset<Row> getBgpRouteRowsDiff(List<DiffRoutesOutput> diffRoutesList) {
    Multiset<Row> rows = HashMultiset.create();
    for (DiffRoutesOutput diffRoutesOutput : diffRoutesList) {
      RouteRowKey routeRowKey = diffRoutesOutput.getRouteRowKey();
      String hostName = routeRowKey.getHostName();
      String vrfName = routeRowKey.getVrfName();
      Prefix network = routeRowKey.getPrefix();
      PresenceStatus networkPresenceStatus = diffRoutesOutput.getNetworkPresenceStatus();

      for (List<RouteRowAttribute> routeRowAttributeInBaseAndRef :
          diffRoutesOutput.getDiffInAttributes()) {
        Row.RowBuilder rowBuilder = Row.builder();
        rowBuilder
            .put(COL_NODE, new Node(hostName))
            .put(COL_VRF_NAME, vrfName)
            .put(COL_NETWORK, network)
            .put(COL_ROUTE_NETWORK_PRESENCE, networkPresenceStatus);

        RouteRowAttribute routeRowAttributeBase = routeRowAttributeInBaseAndRef.get(0);
        RouteRowAttribute routeRowAttributeRef = routeRowAttributeInBaseAndRef.get(1);
        PresenceStatus routePathPresenceStatus =
            routeRowAttributeBase != null && routeRowAttributeRef != null
                ? PresenceStatus.IN_BOTH
                : routeRowAttributeBase != null
                    ? PresenceStatus.ONLY_IN_SNAPSHOT
                    : PresenceStatus.ONLY_IN_REFERENCE;
        rowBuilder.put(COL_ROUTE_PRESENCE, routePathPresenceStatus);
        populateBgpRouteAttributes(rowBuilder, routeRowAttributeBase, true);
        populateBgpRouteAttributes(rowBuilder, routeRowAttributeRef, false);

        rows.add(rowBuilder.build());
      }
    }
    return rows;
  }

  private static void populateBgpRouteAttributes(
      RowBuilder rowBuilder, @Nullable RouteRowAttribute routeRowAttribute, boolean base) {
    rowBuilder
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_NEXT_HOP_IP,
            routeRowAttribute != null ? routeRowAttribute.getNextHopIp() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_PROTOCOL,
            routeRowAttribute != null ? routeRowAttribute.getProtocol() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_AS_PATH,
            routeRowAttribute != null ? routeRowAttribute.getAsPath() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_METRIC,
            routeRowAttribute != null ? routeRowAttribute.getMetric() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_LOCAL_PREF,
            routeRowAttribute != null ? routeRowAttribute.getLocalPreference() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_COMMUNITIES,
            routeRowAttribute != null ? routeRowAttribute.getCommunities() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_ORIGIN_PROTOCOL,
            routeRowAttribute != null ? routeRowAttribute.getOriginProtocol() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_TAG,
            routeRowAttribute != null ? routeRowAttribute.getTag() : null);
  }

  /**
   * Converts {@link List} of {@link DiffRoutesOutput} to {@link Row}s with one row corresponding to
   * each {@link DiffRoutesOutput#_diffInAttributes} of the {@link DiffRoutesOutput}
   *
   * @param diffRoutesList {@link List} of {@link DiffRoutesOutput} for routes in Main RIB
   * @return {@link Multiset} of {@link Row}s
   */
  static Multiset<Row> getAbstractRouteRowsDiff(List<DiffRoutesOutput> diffRoutesList) {
    Multiset<Row> rows = HashMultiset.create();
    for (DiffRoutesOutput diffRoutesOutput : diffRoutesList) {
      RouteRowKey routeRowKey = diffRoutesOutput.getRouteRowKey();
      String hostName = routeRowKey.getHostName();
      String vrfName = routeRowKey.getVrfName();
      Prefix network = routeRowKey.getPrefix();

      PresenceStatus networkPresenceStatus = diffRoutesOutput.getNetworkPresenceStatus();

      for (List<RouteRowAttribute> routeRowAttributeInBaseAndRef :
          diffRoutesOutput.getDiffInAttributes()) {
        Row.RowBuilder rowBuilder = Row.builder();
        rowBuilder
            .put(COL_NODE, new Node(hostName))
            .put(COL_VRF_NAME, vrfName)
            .put(COL_NETWORK, network)
            .put(COL_ROUTE_NETWORK_PRESENCE, networkPresenceStatus);

        RouteRowAttribute routeRowAttributeBase = routeRowAttributeInBaseAndRef.get(0);
        RouteRowAttribute routeRowAttributeRef = routeRowAttributeInBaseAndRef.get(1);

        PresenceStatus routePathPresenceStatus =
            routeRowAttributeBase != null && routeRowAttributeRef != null
                ? PresenceStatus.IN_BOTH
                : routeRowAttributeBase != null
                    ? PresenceStatus.ONLY_IN_SNAPSHOT
                    : PresenceStatus.ONLY_IN_REFERENCE;
        rowBuilder.put(COL_ROUTE_PRESENCE, routePathPresenceStatus);
        populateRouteAttributes(rowBuilder, routeRowAttributeBase, true);
        populateRouteAttributes(rowBuilder, routeRowAttributeRef, false);
        rows.add(rowBuilder.build());
      }
    }
    return rows;
  }

  private static void populateRouteAttributes(
      RowBuilder rowBuilder, @Nullable RouteRowAttribute routeRowAttribute, boolean base) {
    rowBuilder
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_NEXT_HOP,
            routeRowAttribute != null ? routeRowAttribute.getNextHop() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_NEXT_HOP_IP,
            routeRowAttribute != null ? routeRowAttribute.getNextHopIp() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_PROTOCOL,
            routeRowAttribute != null ? routeRowAttribute.getProtocol() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_METRIC,
            routeRowAttribute != null ? routeRowAttribute.getMetric() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_ADMIN_DISTANCE,
            routeRowAttribute != null ? routeRowAttribute.getAdminDistance() : null)
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_TAG,
            routeRowAttribute != null ? routeRowAttribute.getTag() : null);
  }

  /**
   * Given a {@link Set} of {@link AbstractRoute}s, groups the routes by the fields of {@link
   * RouteRowKey} and for routes with the same key, sorts them according to {@link
   * RouteRowAttribute}s
   *
   * @param ribs {@link Map} of the RIBs
   * @param matchingNodes {@link Set} of nodes to be matched
   * @param network {@link Prefix}
   * @param vrfRegex Regex to filter the VRF
   * @param protocolRegex Regex to filter the protocols of the routes
   * @param ipOwners {@link Map} of {@link Ip} to {@link Set} of owner nodes
   * @return {@link Map} with {@link RouteRowKey}s and corresponding {@link SortedSet} of {@link
   *     RouteRowAttribute}s
   */
  static Map<RouteRowKey, SortedSet<RouteRowAttribute>> groupMatchingRoutesByPrefix(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Set<String> matchingNodes,
      @Nullable Prefix network,
      String vrfRegex,
      String protocolRegex,
      @Nullable Map<Ip, Set<String>> ipOwners) {
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> routesGroupedByPrefix = new HashMap<>();
    Pattern compiledProtocolRegex = Pattern.compile(protocolRegex, Pattern.CASE_INSENSITIVE);
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    ribs.forEach(
        (node, vrfMap) -> {
          if (matchingNodes.contains(node)) {
            vrfMap.forEach(
                (vrfName, rib) -> {
                  if (compiledVrfRegex.matcher(vrfName).matches()) {
                    rib.getRoutes()
                        .stream()
                        .filter(
                            route ->
                                (network == null || network.equals(route.getNetwork()))
                                    && compiledProtocolRegex
                                        .matcher(route.getProtocol().protocolName())
                                        .matches())
                        .forEach(
                            route ->
                                routesGroupedByPrefix
                                    .computeIfAbsent(
                                        new RouteRowKey(node, vrfName, route.getNetwork()),
                                        k -> new TreeSet<>())
                                    .add(
                                        RouteRowAttribute.builder()
                                            .setProtocol(
                                                route.getProtocol() != null
                                                    ? route.getProtocol().protocolName()
                                                    : null)
                                            .setNextHopIp(route.getNextHopIp())
                                            .setNextHop(
                                                computeNextHopNode(route.getNextHopIp(), ipOwners))
                                            .setAdminDistance(route.getAdministrativeCost())
                                            .setMetric(route.getMetric())
                                            .setTag(route.getTag())
                                            .build()));
                  }
                });
          }
        });
    return routesGroupedByPrefix;
  }

  /**
   * Given a {@link Table} of {@link BgpRoute}s indexed by Node name and VRF name, applies given
   * filters groups the routes by {@link RouteRowKey} and for the routes with the same key, sorts
   * them according to {@link RouteRowAttribute}
   *
   * @param bgpRoutes {@link Table} of BGP routes with rows per node and columns per VRF
   * @return {@link Map} with {@link RouteRowKey}s and corresponding {@link SortedSet} of {@link
   *     RouteRowAttribute}s
   */
  static Map<RouteRowKey, SortedSet<RouteRowAttribute>> groupMatchingBgpRoutesByPrefix(
      Table<String, String, Set<BgpRoute>> bgpRoutes,
      Set<String> matchingNodes,
      String vrfRegex,
      @Nullable Prefix network,
      String protocolRegex) {
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> bgpRoutesGroupedByPrefix = new HashMap<>();
    Pattern compiledProtocolRegex = Pattern.compile(protocolRegex, Pattern.CASE_INSENSITIVE);
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    matchingNodes.forEach(
        hostname ->
            bgpRoutes
                .row(hostname)
                .forEach(
                    (vrfName, routes) -> {
                      if (compiledVrfRegex.matcher(vrfName).matches()) {
                        routes
                            .stream()
                            .filter(
                                route ->
                                    (network == null || network.equals(route.getNetwork()))
                                        && compiledProtocolRegex
                                            .matcher(route.getProtocol().protocolName())
                                            .matches())
                            .forEach(
                                route ->
                                    bgpRoutesGroupedByPrefix
                                        .computeIfAbsent(
                                            new RouteRowKey(hostname, vrfName, route.getNetwork()),
                                            k -> new TreeSet<>())
                                        .add(
                                            RouteRowAttribute.builder()
                                                .setNextHopIp(route.getNextHopIp())
                                                .setProtocol(route.getProtocol().protocolName())
                                                .setAdminDistance(route.getAdministrativeCost())
                                                .setMetric(route.getMetric())
                                                .setAsPath(route.getAsPath())
                                                .setLocalPreference(route.getLocalPreference())
                                                .setCommunities(
                                                    route
                                                        .getCommunities()
                                                        .stream()
                                                        .map(CommonUtil::longToCommunity)
                                                        .collect(toImmutableList()))
                                                .setTag(
                                                    route.getTag() == Route.UNSET_ROUTE_TAG
                                                        ? null
                                                        : route.getTag())
                                                .build()));
                      }
                    }));

    return bgpRoutesGroupedByPrefix;
  }

  /**
   * Given two sorted {@link List}s of {@link RouteRowAttribute}s, outputs a 2-dimensional list
   * showing the diff in the two input lists
   *
   * <p>In each row of the output 2-dimensional {@link List} the two columns contain matching {@link
   * RouteRowAttribute}s and for {@link RouteRowAttribute}s with no matching values the other column
   * contains a null.
   *
   * @param routeRowAttributes1 First sorted {@link List} of {@link RouteRowAttribute}s
   * @param routeRowAttributes2 Second sorted {@link List} of {@link RouteRowAttribute}s
   * @return A 2-dimensional {@link List} of columns size two showing the diff
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
                  PresenceStatus.IN_BOTH));
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
            new DiffRoutesOutput(routeRowKey, diffMatrix, PresenceStatus.ONLY_IN_SNAPSHOT));
      } else {
        SortedSet<RouteRowAttribute> routeRowAttributesInRef = routesInRef.get(routeRowKey);
        // base route attribute in the 2 column diff Matrix will be unset for elements
        List<List<RouteRowAttribute>> diffMatrix =
            routeRowAttributesInRef
                .stream()
                .map(routeRowAttribute -> Lists.newArrayList(null, routeRowAttribute))
                .collect(Collectors.toList());
        listOfDiffPerKeys.add(
            new DiffRoutesOutput(routeRowKey, diffMatrix, PresenceStatus.ONLY_IN_REFERENCE));
      }
    }
    return listOfDiffPerKeys;
  }
}
