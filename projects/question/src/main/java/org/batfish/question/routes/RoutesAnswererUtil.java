package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.comparingInt;
import static org.batfish.datamodel.questions.BgpRouteStatus.BACKUP;
import static org.batfish.datamodel.questions.BgpRouteStatus.BEST;
import static org.batfish.datamodel.table.TableDiff.COL_BASE_PREFIX;
import static org.batfish.datamodel.table.TableDiff.COL_DELTA_PREFIX;
import static org.batfish.question.routes.RoutesAnswerer.COL_ADMIN_DISTANCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_AS_PATH;
import static org.batfish.question.routes.RoutesAnswerer.COL_CLUSTER_LIST;
import static org.batfish.question.routes.RoutesAnswerer.COL_COMMUNITIES;
import static org.batfish.question.routes.RoutesAnswerer.COL_LOCAL_PREF;
import static org.batfish.question.routes.RoutesAnswerer.COL_METRIC;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_INTERFACE;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NODE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGINATOR_ID;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGIN_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGIN_TYPE;
import static org.batfish.question.routes.RoutesAnswerer.COL_PATH_ID;
import static org.batfish.question.routes.RoutesAnswerer.COL_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_RECEIVED_FROM_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_DISTINGUISHER;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_ENTRY_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_STATUS;
import static org.batfish.question.routes.RoutesAnswerer.COL_TAG;
import static org.batfish.question.routes.RoutesAnswerer.COL_TUNNEL_ENCAPSULATION_ATTRIBUTE;
import static org.batfish.question.routes.RoutesAnswerer.COL_VRF_NAME;
import static org.batfish.question.routes.RoutesAnswerer.COL_WEIGHT;
import static org.batfish.question.routes.RoutesAnswerer.getDiffTableMetadata;
import static org.batfish.question.routes.RoutesAnswerer.getTableMetadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRouteStatus;
import org.batfish.datamodel.route.nh.LegacyNextHops;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.visitors.LegacyReceivedFromToIpConverter;
import org.batfish.question.routes.DiffRoutesOutput.KeyPresenceStatus;
import org.batfish.question.routes.RoutesQuestion.PrefixMatchType;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.batfish.specifier.RoutingProtocolSpecifier;

public class RoutesAnswererUtil {

  /** IPs that are used internally and should not be exposed as next hop IPs */
  private static final Set<Ip> INTERNAL_USE_IPS =
      ImmutableSet.of(
          // BGP unnumbered IP
          Ip.parse("169.254.0.1"));

  public enum RouteEntryPresenceStatus {
    ONLY_IN_SNAPSHOT(TableDiff.COL_KEY_STATUS_ONLY_BASE),
    ONLY_IN_REFERENCE(TableDiff.COL_KEY_STATUS_ONLY_DELTA),
    CHANGED("Changed"),
    UNCHANGED("Unchanged");

    private static final Map<String, RouteEntryPresenceStatus> _map = buildMap();

    private static Map<String, RouteEntryPresenceStatus> buildMap() {
      ImmutableMap.Builder<String, RouteEntryPresenceStatus> map = ImmutableMap.builder();
      for (RouteEntryPresenceStatus value : RouteEntryPresenceStatus.values()) {
        map.put(value._name, value);
      }
      return map.build();
    }

    @JsonCreator
    public static RouteEntryPresenceStatus fromName(String name) {
      RouteEntryPresenceStatus instance = _map.get(name);
      if (instance == null) {
        throw new BatfishException(
            String.format(
                "No %s with name: '%s'", RouteEntryPresenceStatus.class.getSimpleName(), name));
      }
      return instance;
    }

    private final String _name;

    RouteEntryPresenceStatus(String name) {
      _name = name;
    }

    @JsonValue
    public String routeEntryPresenceName() {
      return _name;
    }
  }

  /** Compute the next hop node for a given next hop IP. */
  static @Nullable String computeNextHopNode(
      @Nullable Ip nextHopIp, @Nullable Map<Ip, Set<String>> ipOwners) {
    if (nextHopIp == null || ipOwners == null) {
      return null;
    }
    Set<String> nextNodes = ipOwners.getOrDefault(nextHopIp, ImmutableSet.of());
    if (nextNodes.size() == 1) {
      return Iterables.getOnlyElement(nextNodes);
    }
    // TODO: https://github.com/batfish/batfish/issues/1862 can try harder for multiple outs
    return null;
  }

  /**
   * Returns a {@link Multiset} of {@link Row}s for all routes present in all RIBs
   *
   * @param ribs {@link Map} representing all RIBs of all nodes
   * @param matchingVrfsByNode {@link Multimap} of vrfs grouped by node from which {@link
   *     Bgpv4Route}s are to be selected
   * @param network {@link Prefix} of the network used to filter the routes
   * @param protocolSpec {@link RoutingProtocolSpecifier} used to filter the routes
   * @param prefixMatchType {@link PrefixMatchType} used to select which prefixes are reported
   * @return {@link Multiset} of {@link Row}s representing the routes
   */
  static <T extends AbstractRouteDecorator> Multiset<Row> getMainRibRoutes(
      Table<String, String, FinalMainRib> ribs,
      Multimap<String, String> matchingVrfsByNode,
      @Nullable Prefix network,
      RoutingProtocolSpecifier protocolSpec,
      PrefixMatchType prefixMatchType) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, ColumnMetadata> columnMetadataMap =
        getTableMetadata(RibProtocol.MAIN).toColumnMap();
    matchingVrfsByNode.forEach(
        (hostname, vrfName) ->
            Optional.ofNullable(ribs.get(hostname, vrfName))
                .map(rib -> getMatchingPrefixRoutes(prefixMatchType, network, rib))
                .orElse(Stream.empty())
                .filter(route -> protocolSpec.getProtocols().contains(route.getProtocol()))
                .forEach(
                    route ->
                        rows.add(abstractRouteToRow(hostname, vrfName, route, columnMetadataMap))));
    return rows;
  }

  /**
   * Given the prefixMatchType and network (user input), returns routes from the {@code rib} that
   * match.
   */
  @VisibleForTesting
  static Stream<AbstractRoute> getMatchingPrefixRoutes(
      PrefixMatchType prefixMatchType, @Nullable Prefix network, FinalMainRib rib) {
    if (network == null) {
      // everything matches if there is not user input
      return rib.getRoutes().stream();
    }
    switch (prefixMatchType) {
      case EXACT:
        return rib.getRoutes(network).stream();
      case LONGEST_PREFIX_MATCH:
        return rib.longestPrefixMatch(network).stream();
      default:
        return rib.getRoutes().stream()
            .filter(r -> prefixMatches(prefixMatchType, network, r.getNetwork()));
    }
  }

  @VisibleForTesting
  static boolean prefixMatches(
      PrefixMatchType prefixMatchType, Prefix inputNetwork, Prefix routeNetwork) {
    // handled separately in the caller
    return switch (prefixMatchType) {
      case EXACT -> inputNetwork.equals(routeNetwork);
      case LONGER_PREFIXES -> inputNetwork.containsPrefix(routeNetwork);
      case SHORTER_PREFIXES -> routeNetwork.containsPrefix(inputNetwork);
      case LONGEST_PREFIX_MATCH ->
          throw new IllegalArgumentException("Illegal PrefixMatchType " + prefixMatchType);
    };
  }

  /**
   * Filters {@link Table} of BEST and BACKUP {@link Bgpv4Route}s to produce a {@link Multiset} of
   * rows.
   *
   * @param bgpBestRoutes {@link Table} of all best {@link Bgpv4Route}s
   * @param bgpBackupRoutes {@link Table} of all backup {@link Bgpv4Route}s
   * @param matchingVrfsByNode {@link Multimap} of vrfs grouped by node from which {@link
   *     Bgpv4Route}s are to be selected
   * @param network {@link Prefix} of the network used to filter the routes
   * @param protocolSpec {@link RoutingProtocolSpecifier} used to filter the {@link Bgpv4Route}s
   * @param routeStatuses BGP route statuses to report
   * @param prefixMatchType The type of prefix matching desired
   * @return {@link Multiset} of {@link Row}s representing the routes
   */
  static Multiset<Row> getBgpRibRoutes(
      Table<String, String, Set<Bgpv4Route>> bgpBestRoutes,
      Table<String, String, Set<Bgpv4Route>> bgpBackupRoutes,
      Multimap<String, String> matchingVrfsByNode,
      @Nullable Prefix network,
      RoutingProtocolSpecifier protocolSpec,
      Set<BgpRouteStatus> routeStatuses,
      PrefixMatchType prefixMatchType) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, ColumnMetadata> columnMetadataMap = getTableMetadata(RibProtocol.BGP).toColumnMap();
    matchingVrfsByNode.forEach(
        (hostname, vrfName) ->
            getMatchingRoutes(
                    firstNonNull(bgpBestRoutes.get(hostname, vrfName), ImmutableSet.of()),
                    firstNonNull(bgpBackupRoutes.get(hostname, vrfName), ImmutableSet.of()),
                    network,
                    routeStatuses,
                    prefixMatchType)
                .forEach(
                    (status, routeStream) ->
                        routeStream
                            .filter(r -> protocolSpec.getProtocols().contains(r.getProtocol()))
                            .forEach(
                                route ->
                                    rows.add(
                                        bgpRouteToRow(
                                            hostname,
                                            vrfName,
                                            route,
                                            ImmutableSet.of(status),
                                            columnMetadataMap)))));
    return rows;
  }

  /**
   * Filters {@link Table} of BEST and BACKUP {@link EvpnRoute}s to produce a {@link Multiset} of
   * rows.
   *
   * @param evpnBestRoutes {@link Table} of all best {@link EvpnRoute}s
   * @param evpnBackupRoutes {@link Table} of all backup {@link EvpnRoute}s
   * @param matchingVrfsByNode {@link Multimap} of vrfs grouped by node from which {@link
   *     Bgpv4Route}s are to be selected
   * @param network {@link Prefix} of the network used to filter the routes
   * @param protocolSpec {@link RoutingProtocolSpecifier} used to filter the {@link Bgpv4Route}s
   * @param routeStatuses BGP route statuses to report
   * @param prefixMatchType The type of prefix matching desired
   * @return {@link Multiset} of {@link Row}s representing the routes
   */
  static Multiset<Row> getEvpnRoutes(
      Table<String, String, Set<EvpnRoute<?, ?>>> evpnBestRoutes,
      Table<String, String, Set<EvpnRoute<?, ?>>> evpnBackupRoutes,
      Multimap<String, String> matchingVrfsByNode,
      @Nullable Prefix network,
      RoutingProtocolSpecifier protocolSpec,
      Set<BgpRouteStatus> routeStatuses,
      PrefixMatchType prefixMatchType) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, ColumnMetadata> columnMetadataMap =
        getTableMetadata(RibProtocol.EVPN).toColumnMap();
    matchingVrfsByNode.forEach(
        (hostname, vrfName) ->
            getMatchingRoutes(
                    firstNonNull(evpnBestRoutes.get(hostname, vrfName), ImmutableSet.of()),
                    firstNonNull(evpnBackupRoutes.get(hostname, vrfName), ImmutableSet.of()),
                    network,
                    routeStatuses,
                    prefixMatchType)
                .forEach(
                    (status, routeStream) ->
                        routeStream
                            .filter(r -> protocolSpec.getProtocols().contains(r.getProtocol()))
                            .forEach(
                                route ->
                                    rows.add(
                                        evpnRouteToRow(
                                            hostname,
                                            vrfName,
                                            route,
                                            ImmutableSet.of(status),
                                            columnMetadataMap)))));
    return rows;
  }

  /**
   * Filters best and backup routes to those that match the input network, route statuses, and
   * prefix match type.
   *
   * <p>If the network is null, all routes are returned.
   *
   * <p>It the prefix match type is LONGEST_PREFIX_MATCH, the returned prefix is decided based on
   * LPM on the best routes table. This is done because LPM logic is meaningful only for best routes
   * (as those are the lookup candidates) and also because backup table cannot have a longer
   * matching prefix (for a prefix to exist in backup, there must be something better in best). A
   * consequence of these semantics is that if the user asks only for backup routes (via
   * routeStatus), nothing may be reported in cases where the LPM-based matching on the best table
   * leads to prefix P1 but P1 is not present in the backup table.
   */
  static <T extends AbstractRouteDecorator> Map<BgpRouteStatus, Stream<T>> getMatchingRoutes(
      Set<T> bestRoutes,
      Set<T> backupRoutes,
      @Nullable Prefix network,
      Set<BgpRouteStatus> routeStatuses,
      PrefixMatchType prefixMatchType) {
    ImmutableMap.Builder<BgpRouteStatus, Stream<T>> routes = ImmutableMap.builder();
    if (prefixMatchType == PrefixMatchType.LONGEST_PREFIX_MATCH && network != null) {
      Optional<Prefix> lpmMatch = longestMatchingPrefix(network, bestRoutes);
      if (lpmMatch.isPresent()) {
        if (routeStatuses.contains(BEST)) {
          routes.put(
              BEST, getMatchingPrefixRoutes(bestRoutes, lpmMatch.get(), PrefixMatchType.EXACT));
        }
        if (routeStatuses.contains(BACKUP)) {
          routes.put(
              BACKUP, getMatchingPrefixRoutes(backupRoutes, lpmMatch.get(), PrefixMatchType.EXACT));
        }
      }
      return routes.build();
    }

    if (routeStatuses.contains(BEST)) {
      routes.put(BEST, getMatchingPrefixRoutes(bestRoutes, network, prefixMatchType));
    }
    if (routeStatuses.contains(BACKUP)) {
      routes.put(BACKUP, getMatchingPrefixRoutes(backupRoutes, network, prefixMatchType));
    }
    return routes.build();
  }

  private static <T extends AbstractRouteDecorator> Stream<T> getMatchingPrefixRoutes(
      Set<T> bgpRoutes, Prefix network, PrefixMatchType prefixMatchType) {
    return bgpRoutes.stream()
        .filter(r -> network == null || prefixMatches(prefixMatchType, network, r.getNetwork()));
  }

  @VisibleForTesting
  static <T extends AbstractRouteDecorator> Optional<Prefix> longestMatchingPrefix(
      Prefix network, Set<T> routes) {
    return routes.stream()
        .map(AbstractRouteDecorator::getNetwork)
        .filter(prefix -> prefix.containsPrefix(network))
        .max(comparingInt(Prefix::getPrefixLength));
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
      Map<String, ColumnMetadata> columnMetadataMap) {
    // If the route's next hop IP is for internal use, do not show it in the row
    Ip nextHopIp =
        INTERNAL_USE_IPS.contains(abstractRoute.getNextHopIp())
            ? null
            : abstractRoute.getNextHopIp();
    return Row.builder(columnMetadataMap)
        .put(COL_NODE, new Node(hostName))
        .put(COL_VRF_NAME, vrfName)
        .put(COL_NETWORK, abstractRoute.getNetwork())
        .put(COL_NEXT_HOP, abstractRoute.getNextHop())
        .put(COL_NEXT_HOP_IP, nextHopIp)
        .put(COL_NEXT_HOP_INTERFACE, abstractRoute.getNextHopInterface())
        .put(COL_PROTOCOL, abstractRoute.getProtocol())
        .put(
            COL_TAG,
            abstractRoute.getTag() == Route.UNSET_ROUTE_TAG ? null : abstractRoute.getTag())
        .put(COL_ADMIN_DISTANCE, abstractRoute.getAdministrativeCost())
        .put(COL_METRIC, abstractRoute.getMetric())
        .build();
  }

  /**
   * Converts a {@link Bgpv4Route} to a {@link Row}
   *
   * @param hostName {@link String} host-name of the node containing the bgpv4Route
   * @param vrfName {@link String} name of the VRF containing the bgpv4Route
   * @param bgpv4Route {@link Bgpv4Route} BGP route to convert
   * @param statuses BGP route statuses applicable to the route
   * @param columnMetadataMap Column metadata of the columns for this {@link Row}
   * @return {@link Row} representing the {@link Bgpv4Route}
   */
  static Row bgpRouteToRow(
      String hostName,
      String vrfName,
      Bgpv4Route bgpv4Route,
      Set<BgpRouteStatus> statuses,
      Map<String, ColumnMetadata> columnMetadataMap) {
    // If the route's next hop IP is for internal use, do not show it in the row
    Ip nextHopIp =
        INTERNAL_USE_IPS.contains(bgpv4Route.getNextHopIp()) ? null : bgpv4Route.getNextHopIp();
    return Row.builder(columnMetadataMap)
        .put(COL_NODE, new Node(hostName))
        .put(COL_VRF_NAME, vrfName)
        .put(COL_NETWORK, bgpv4Route.getNetwork())
        .put(COL_NEXT_HOP, bgpv4Route.getNextHop())
        .put(COL_NEXT_HOP_IP, nextHopIp)
        .put(COL_NEXT_HOP_INTERFACE, bgpv4Route.getNextHopInterface())
        .put(COL_PROTOCOL, bgpv4Route.getProtocol())
        .put(COL_AS_PATH, bgpv4Route.getAsPath().getAsPathString())
        .put(COL_METRIC, bgpv4Route.getMetric())
        .put(COL_LOCAL_PREF, bgpv4Route.getLocalPreference())
        .put(
            COL_COMMUNITIES,
            bgpv4Route.getCommunities().getCommunities().stream()
                .sorted()
                .map(Community::toString)
                .collect(toImmutableList()))
        .put(COL_ORIGIN_PROTOCOL, bgpv4Route.getSrcProtocol())
        .put(COL_ORIGIN_TYPE, bgpv4Route.getOriginType())
        .put(COL_ORIGINATOR_ID, bgpv4Route.getOriginatorIp())
        .put(
            COL_RECEIVED_FROM_IP,
            LegacyReceivedFromToIpConverter.convert(bgpv4Route.getReceivedFrom()))
        .put(COL_PATH_ID, bgpv4Route.getPathId())
        .put(
            COL_CLUSTER_LIST,
            bgpv4Route.getClusterList().isEmpty()
                ? null
                : bgpv4Route.getClusterList().stream()
                    .sorted()
                    .collect(ImmutableList.toImmutableList()))
        .put(COL_TAG, bgpv4Route.getTag() == Route.UNSET_ROUTE_TAG ? null : bgpv4Route.getTag())
        .put(COL_STATUS, statuses)
        .put(
            COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
            Optional.ofNullable(bgpv4Route.getTunnelEncapsulationAttribute())
                .map(TunnelEncapsulationAttribute::toString)
                .orElse(null))
        .put(COL_WEIGHT, bgpv4Route.getWeight())
        .build();
  }

  static Row evpnRouteToRow(
      String hostName,
      String vrfName,
      EvpnRoute<?, ?> evpnRoute,
      Set<BgpRouteStatus> statuses,
      Map<String, ColumnMetadata> columnMetadataMap) {
    // If the route's next hop IP is for internal use, do not show it in the row
    Ip nextHopIp =
        INTERNAL_USE_IPS.contains(evpnRoute.getNextHopIp()) ? null : evpnRoute.getNextHopIp();
    return Row.builder(columnMetadataMap)
        .put(COL_NODE, new Node(hostName))
        .put(COL_VRF_NAME, vrfName)
        .put(COL_NETWORK, evpnRoute.getNetwork())
        .put(COL_NEXT_HOP, evpnRoute.getNextHop())
        .put(COL_NEXT_HOP_IP, nextHopIp)
        .put(COL_NEXT_HOP_INTERFACE, evpnRoute.getNextHopInterface())
        .put(COL_PROTOCOL, evpnRoute.getProtocol())
        .put(COL_AS_PATH, evpnRoute.getAsPath().getAsPathString())
        .put(COL_METRIC, evpnRoute.getMetric())
        .put(COL_LOCAL_PREF, evpnRoute.getLocalPreference())
        .put(
            COL_COMMUNITIES,
            evpnRoute.getCommunities().getCommunities().stream()
                .sorted()
                .map(Community::toString)
                .collect(toImmutableList()))
        .put(COL_ORIGIN_PROTOCOL, evpnRoute.getSrcProtocol())
        .put(COL_ORIGIN_TYPE, evpnRoute.getOriginType())
        .put(COL_ORIGINATOR_ID, evpnRoute.getOriginatorIp())
        .put(COL_PATH_ID, evpnRoute.getPathId())
        .put(
            COL_CLUSTER_LIST,
            evpnRoute.getClusterList().isEmpty() ? null : evpnRoute.getClusterList())
        .put(COL_TAG, evpnRoute.getTag() == Route.UNSET_ROUTE_TAG ? null : evpnRoute.getTag())
        .put(COL_ROUTE_DISTINGUISHER, evpnRoute.getRouteDistinguisher())
        .put(COL_STATUS, statuses)
        .put(
            COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
            Optional.ofNullable(evpnRoute.getTunnelEncapsulationAttribute())
                .map(TunnelEncapsulationAttribute::toString)
                .orElse(null))
        .put(COL_WEIGHT, evpnRoute.getWeight())
        .build();
  }

  /**
   * Converts {@link List} of {@link DiffRoutesOutput} to {@link Row}s with one row corresponding to
   * each {@link DiffRoutesOutput#getDiffInAttributes} of the {@link DiffRoutesOutput}
   *
   * @param diffRoutesList {@link List} of {@link DiffRoutesOutput} for {@link Bgpv4Route}s
   * @return {@link Multiset} of {@link Row}s
   */
  static Multiset<Row> getBgpRouteRowsDiff(List<DiffRoutesOutput> diffRoutesList) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, ColumnMetadata> columnMetadataMap =
        getDiffTableMetadata(RibProtocol.BGP).toColumnMap();
    for (DiffRoutesOutput diffRoutesOutput : diffRoutesList) {
      RouteRowKey routeRowKey = diffRoutesOutput.getRouteRowKey();
      String hostName = routeRowKey.getHostName();
      String vrfName = routeRowKey.getVrfName();
      Prefix network = routeRowKey.getPrefix();
      RouteRowSecondaryKey routeRowSecondaryKey = diffRoutesOutput.getRouteRowSecondaryKey();
      KeyPresenceStatus secondaryKeyPresenceStatus =
          diffRoutesOutput.getRouteRowSecondaryKeyStatus();

      for (List<RouteRowAttribute> routeRowAttributeInBaseAndRef :
          diffRoutesOutput.getDiffInAttributes()) {
        Row.RowBuilder rowBuilder = Row.builder(columnMetadataMap);
        rowBuilder
            .put(COL_NODE, new Node(hostName))
            .put(COL_VRF_NAME, vrfName)
            .put(COL_NETWORK, network);

        RouteRowAttribute routeRowAttributeBase = routeRowAttributeInBaseAndRef.get(0);
        RouteRowAttribute routeRowAttributeRef = routeRowAttributeInBaseAndRef.get(1);

        rowBuilder.put(
            COL_ROUTE_ENTRY_PRESENCE,
            getRouteEntryPresence(
                secondaryKeyPresenceStatus, routeRowAttributeBase, routeRowAttributeRef));

        populateSecondaryKeyAttrs(routeRowSecondaryKey, secondaryKeyPresenceStatus, rowBuilder);
        populateBgpRouteAttributes(rowBuilder, routeRowAttributeBase, true);
        populateBgpRouteAttributes(rowBuilder, routeRowAttributeRef, false);

        rows.add(rowBuilder.build());
      }
    }
    return rows;
  }

  /**
   * Converts {@link List} of {@link DiffRoutesOutput} to {@link Row}s with one row corresponding to
   * each {@link DiffRoutesOutput#getDiffInAttributes} of the {@link DiffRoutesOutput}
   *
   * @param diffRoutesList {@link List} of {@link DiffRoutesOutput} for {@link EvpnRoute}s
   * @return {@link Multiset} of {@link Row}s
   */
  static Multiset<Row> getEvpnRouteRowsDiff(List<DiffRoutesOutput> diffRoutesList) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, ColumnMetadata> columnMetadataMap =
        getDiffTableMetadata(RibProtocol.EVPN).toColumnMap();
    for (DiffRoutesOutput diffRoutesOutput : diffRoutesList) {
      RouteRowKey routeRowKey = diffRoutesOutput.getRouteRowKey();
      String hostName = routeRowKey.getHostName();
      String vrfName = routeRowKey.getVrfName();
      Prefix network = routeRowKey.getPrefix();
      RouteRowSecondaryKey routeRowSecondaryKey = diffRoutesOutput.getRouteRowSecondaryKey();
      KeyPresenceStatus secondaryKeyPresenceStatus =
          diffRoutesOutput.getRouteRowSecondaryKeyStatus();

      for (List<RouteRowAttribute> routeRowAttributeInBaseAndRef :
          diffRoutesOutput.getDiffInAttributes()) {
        Row.RowBuilder rowBuilder = Row.builder(columnMetadataMap);
        rowBuilder
            .put(COL_NODE, new Node(hostName))
            .put(COL_VRF_NAME, vrfName)
            .put(COL_NETWORK, network);

        RouteRowAttribute routeRowAttributeBase = routeRowAttributeInBaseAndRef.get(0);
        RouteRowAttribute routeRowAttributeRef = routeRowAttributeInBaseAndRef.get(1);

        rowBuilder.put(
            COL_ROUTE_ENTRY_PRESENCE,
            getRouteEntryPresence(
                secondaryKeyPresenceStatus, routeRowAttributeBase, routeRowAttributeRef));

        populateSecondaryKeyAttrs(routeRowSecondaryKey, secondaryKeyPresenceStatus, rowBuilder);
        populateBgpRouteAttributes(rowBuilder, routeRowAttributeBase, true);
        populateBgpRouteAttributes(rowBuilder, routeRowAttributeRef, false);

        rows.add(rowBuilder.build());
      }
    }
    return rows;
  }

  /**
   * Populates the {@link RowBuilder} for differential answer of {@link RoutesAnswerer} with
   * attributes of {@link RouteRowSecondaryKey} for base and reference snapshot
   *
   * @param routeRowSecondaryKey {@link RouteRowSecondaryKey} for the current {@link Row} of the
   *     differential answer
   * @param secondaryKeyPresence {@link KeyPresenceStatus} for the routeRowSecondaryKey
   * @param rowBuilder {@link RowBuilder} for the current row
   */
  private static void populateSecondaryKeyAttrs(
      RouteRowSecondaryKey routeRowSecondaryKey,
      KeyPresenceStatus secondaryKeyPresence,
      RowBuilder rowBuilder) {
    SecondaryKeyPopulator secondaryKeyPopulator = new SecondaryKeyPopulator();
    // populating base columns for secondary key if it is present in base snapshot or in both
    // snapshots
    if (secondaryKeyPresence == KeyPresenceStatus.IN_BOTH
        || secondaryKeyPresence == KeyPresenceStatus.ONLY_IN_SNAPSHOT) {
      secondaryKeyPopulator.populateSecondaryKeyAttrs(
          routeRowSecondaryKey, rowBuilder, COL_BASE_PREFIX);
    }
    // populating reference columns for secondary key if it is present in reference snapshot or in
    // both snapshots
    if (secondaryKeyPresence == KeyPresenceStatus.IN_BOTH
        || secondaryKeyPresence == KeyPresenceStatus.ONLY_IN_REFERENCE) {
      secondaryKeyPopulator.populateSecondaryKeyAttrs(
          routeRowSecondaryKey, rowBuilder, COL_DELTA_PREFIX);
    }
  }

  private static class SecondaryKeyPopulator implements RouteRowSecondaryKeyVisitor<Void> {
    private String _columnPrefix;
    private RowBuilder _rowBuilder;

    /**
     * Populates the given {@link RowBuilder} with the attributes of the given {@link
     * RouteRowSecondaryKey} with column names prefixed with the given {@code columnPrefix}
     * (expected to be {@link TableDiff#COL_BASE_PREFIX} or {@link TableDiff#COL_DELTA_PREFIX}).
     */
    public void populateSecondaryKeyAttrs(
        RouteRowSecondaryKey routeRowSecondaryKey, RowBuilder rowBuilder, String columnPrefix) {
      _rowBuilder = rowBuilder;
      _columnPrefix = columnPrefix;
      routeRowSecondaryKey.accept(this);
    }

    @Override
    public Void visitBgpRouteRowSecondaryKey(BgpRouteRowSecondaryKey bgpRouteRowSecondaryKey) {
      _rowBuilder
          .put(_columnPrefix + COL_NEXT_HOP, bgpRouteRowSecondaryKey.getNextHop())
          .put(
              // included for backwards compatibility
              _columnPrefix + COL_NEXT_HOP_IP,
              LegacyNextHops.getNextHopIp(bgpRouteRowSecondaryKey.getNextHop())
                  .orElse(Route.UNSET_ROUTE_NEXT_HOP_IP))
          .put(_columnPrefix + COL_PROTOCOL, bgpRouteRowSecondaryKey.getProtocol())
          .put(_columnPrefix + COL_RECEIVED_FROM_IP, bgpRouteRowSecondaryKey.getReceivedFromIp())
          .put(_columnPrefix + COL_PATH_ID, bgpRouteRowSecondaryKey.getPathId());
      return null;
    }

    @Override
    public Void visitEvpnRouteRowSecondaryKey(EvpnRouteRowSecondaryKey evpnRouteRowSecondaryKey) {
      _rowBuilder
          .put(_columnPrefix + COL_NEXT_HOP, evpnRouteRowSecondaryKey.getNextHop())
          .put(_columnPrefix + COL_PROTOCOL, evpnRouteRowSecondaryKey.getProtocol())
          .put(
              _columnPrefix + COL_ROUTE_DISTINGUISHER,
              evpnRouteRowSecondaryKey.getRouteDistinguisher())
          .put(_columnPrefix + COL_PATH_ID, evpnRouteRowSecondaryKey.getPathId());
      return null;
    }

    @Override
    public Void visitMainRibRouteRowSecondaryKey(
        MainRibRouteRowSecondaryKey mainRibRouteRowSecondaryKey) {
      _rowBuilder
          .put(_columnPrefix + COL_NEXT_HOP, mainRibRouteRowSecondaryKey.getNextHop())
          .put(
              // included for backwards compatibility
              _columnPrefix + COL_NEXT_HOP_IP,
              LegacyNextHops.getNextHopIp(mainRibRouteRowSecondaryKey.getNextHop())
                  .orElse(Route.UNSET_ROUTE_NEXT_HOP_IP))
          .put(_columnPrefix + COL_PROTOCOL, mainRibRouteRowSecondaryKey.getProtocol());
      return null;
    }
  }

  /**
   * Computes RoutePresenceEntryStatus using secondary key status and pairs of routeRowAttributes in
   * base and reference
   */
  private static RouteEntryPresenceStatus getRouteEntryPresence(
      KeyPresenceStatus secondaryKeyStatus,
      RouteRowAttribute routeRowAttributeBase,
      RouteRowAttribute routeRowAttributeReference) {
    RouteEntryPresenceStatus routeEntryPresenceStatus;
    if (secondaryKeyStatus == KeyPresenceStatus.IN_BOTH) {
      if (routeRowAttributeBase != null && routeRowAttributeReference != null) {
        routeEntryPresenceStatus =
            routeRowAttributeBase.equals(routeRowAttributeReference)
                ? RouteEntryPresenceStatus.UNCHANGED
                : RouteEntryPresenceStatus.CHANGED;

      } else {
        routeEntryPresenceStatus =
            routeRowAttributeBase != null
                ? RouteEntryPresenceStatus.ONLY_IN_SNAPSHOT
                : RouteEntryPresenceStatus.ONLY_IN_REFERENCE;
      }
    } else {
      routeEntryPresenceStatus =
          secondaryKeyStatus == KeyPresenceStatus.ONLY_IN_REFERENCE
              ? RouteEntryPresenceStatus.ONLY_IN_REFERENCE
              : RouteEntryPresenceStatus.ONLY_IN_SNAPSHOT;
    }
    return routeEntryPresenceStatus;
  }

  @VisibleForTesting
  static void populateBgpRouteAttributes(
      RowBuilder rowBuilder, @Nullable RouteRowAttribute routeRowAttribute, boolean base) {
    String prefix = base ? COL_BASE_PREFIX : COL_DELTA_PREFIX;
    rowBuilder
        .put(
            prefix + COL_AS_PATH,
            routeRowAttribute != null && routeRowAttribute.getAsPath() != null
                ? routeRowAttribute.getAsPath().getAsPathString()
                : null)
        .put(prefix + COL_METRIC, routeRowAttribute != null ? routeRowAttribute.getMetric() : null)
        .put(
            prefix + COL_LOCAL_PREF,
            routeRowAttribute != null ? routeRowAttribute.getLocalPreference() : null)
        .put(
            prefix + COL_CLUSTER_LIST,
            routeRowAttribute == null
                ? null
                : routeRowAttribute.getClusterList().stream()
                    .sorted()
                    .collect(ImmutableList.toImmutableList()))
        .put(
            prefix + COL_COMMUNITIES,
            routeRowAttribute != null ? routeRowAttribute.getCommunities() : null)
        .put(
            prefix + COL_ORIGIN_PROTOCOL,
            routeRowAttribute != null ? routeRowAttribute.getOriginProtocol() : null)
        .put(
            prefix + COL_ORIGIN_TYPE,
            routeRowAttribute != null ? routeRowAttribute.getOriginType() : null)
        .put(
            prefix + COL_ORIGINATOR_ID,
            routeRowAttribute != null ? routeRowAttribute.getOriginatorIp() : null)
        .put(prefix + COL_TAG, routeRowAttribute != null ? routeRowAttribute.getTag() : null)
        .put(
            prefix + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
            routeRowAttribute != null && routeRowAttribute.getTunnelEncapsulationAttribute() != null
                ? routeRowAttribute.getTunnelEncapsulationAttribute().toString()
                : null)
        .put(prefix + COL_WEIGHT, routeRowAttribute != null ? routeRowAttribute.getWeight() : null)
        .put(
            prefix + COL_STATUS,
            routeRowAttribute != null && routeRowAttribute.getStatus() != null
                ? ImmutableList.of(routeRowAttribute.getStatus())
                : null);
  }

  /**
   * Converts {@link List} of {@link DiffRoutesOutput} to {@link Row}s with one row corresponding to
   * each {@link DiffRoutesOutput#getDiffInAttributes} of the {@link DiffRoutesOutput}
   *
   * @param diffRoutesList {@link List} of {@link DiffRoutesOutput} for routes in Main RIB
   * @return {@link Multiset} of {@link Row}s
   */
  static Multiset<Row> getAbstractRouteRowsDiff(List<DiffRoutesOutput> diffRoutesList) {
    Map<String, ColumnMetadata> columnMetadataMap =
        getDiffTableMetadata(RibProtocol.MAIN).toColumnMap();
    Multiset<Row> rows = HashMultiset.create();
    for (DiffRoutesOutput diffRoutesOutput : diffRoutesList) {
      RouteRowKey routeRowKey = diffRoutesOutput.getRouteRowKey();
      String hostName = routeRowKey.getHostName();
      String vrfName = routeRowKey.getVrfName();
      Prefix network = routeRowKey.getPrefix();

      RouteRowSecondaryKey routeRowSecondaryKey = diffRoutesOutput.getRouteRowSecondaryKey();
      KeyPresenceStatus secondaryKeyPresenceStatus =
          diffRoutesOutput.getRouteRowSecondaryKeyStatus();

      for (List<RouteRowAttribute> routeRowAttributeInBaseAndRef :
          diffRoutesOutput.getDiffInAttributes()) {
        Row.RowBuilder rowBuilder = Row.builder(columnMetadataMap);
        rowBuilder
            .put(COL_NODE, new Node(hostName))
            .put(COL_VRF_NAME, vrfName)
            .put(COL_NETWORK, network);

        RouteRowAttribute routeRowAttributeBase = routeRowAttributeInBaseAndRef.get(0);
        RouteRowAttribute routeRowAttributeRef = routeRowAttributeInBaseAndRef.get(1);

        rowBuilder.put(
            COL_ROUTE_ENTRY_PRESENCE,
            getRouteEntryPresence(
                secondaryKeyPresenceStatus, routeRowAttributeBase, routeRowAttributeRef));

        populateSecondaryKeyAttrs(routeRowSecondaryKey, secondaryKeyPresenceStatus, rowBuilder);
        populateRouteAttributes(rowBuilder, routeRowAttributeBase, true);
        populateRouteAttributes(rowBuilder, routeRowAttributeRef, false);
        rows.add(rowBuilder.build());
      }
    }
    return rows;
  }

  static void populateRouteAttributes(
      RowBuilder rowBuilder, @Nullable RouteRowAttribute routeRowAttribute, boolean base) {
    rowBuilder
        .put(
            (base ? COL_BASE_PREFIX : COL_DELTA_PREFIX) + COL_NEXT_HOP_INTERFACE,
            routeRowAttribute != null ? routeRowAttribute.getNextHopInterface() : null)
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
   * Given a {@link Map} of all main RIBs, groups the routes in them by the fields of {@link
   * RouteRowKey} and further sub-groups them by {@link RouteRowSecondaryKey} and for routes in the
   * same sub-group, sorts them according to {@link RouteRowAttribute}s
   *
   * @param ribs {@link Map} of the RIBs
   * @param matchingNodes {@link Set} of nodes to be matched
   * @param network {@link Prefix}
   * @param vrfRegex Regex to filter the VRF
   * @param protocolSpec {@link RoutingProtocolSpecifier} to filter the protocols of the routes
   * @return {@link Map} of {@link RouteRowKey}s to corresponding sub{@link Map}s of {@link
   *     RouteRowSecondaryKey} to {@link SortedSet} of {@link RouteRowAttribute}s
   */
  public static <T extends AbstractRouteDecorator>
      Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> groupRoutes(
          Table<String, String, FinalMainRib> ribs,
          Set<String> matchingNodes,
          @Nullable Prefix network,
          String vrfRegex,
          RoutingProtocolSpecifier protocolSpec) {
    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> routesGroups =
        new HashMap<>();
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    for (String node : ribs.rowKeySet()) {
      if (!matchingNodes.contains(node)) {
        continue;
      }
      for (Map.Entry<String, FinalMainRib> entry : ribs.row(node).entrySet()) {
        String vrfName = entry.getKey();
        if (!compiledVrfRegex.matcher(vrfName).matches()) {
          continue;
        }
        FinalMainRib rib = entry.getValue();
        rib.getRoutes().stream()
            .filter(
                route ->
                    (network == null || network.equals(route.getNetwork()))
                        && protocolSpec.getProtocols().contains(route.getProtocol()))
            .forEach(
                route ->
                    routesGroups
                        .computeIfAbsent(
                            new RouteRowKey(node, vrfName, route.getNetwork()),
                            k -> new HashMap<>())
                        .computeIfAbsent(
                            new MainRibRouteRowSecondaryKey(
                                route.getNextHop(), route.getProtocol().protocolName()),
                            k -> new TreeSet<>())
                        .add(
                            RouteRowAttribute.builder()
                                .setNextHopInterface(route.getNextHopInterface())
                                .setAdminDistance(route.getAdministrativeCost())
                                .setMetric(route.getMetric())
                                .setTag(route.getTag())
                                .build()));
      }
    }
    return routesGroups;
  }

  /**
   * Given a {@link Table} of {@link Bgpv4Route}s indexed by Node name and VRF name, applies given
   * filters and groups the routes by {@link RouteRowKey} and sub-groups them further by {@link
   * RouteRowSecondaryKey} and for the routes in same sub-groups, sorts them according to {@link
   * RouteRowAttribute}
   *
   * @param bgpBestRoutes {@link Table} of best BGP routes with rows per node and columns per VRF
   * @param bgpBackupRoutes {@link Table} of backup BGP routes with rows per node and columns per
   *     VRF
   * @param matchingNodes {@link Set} of nodes to be matched
   * @param vrfRegex Regex to filter the VRF
   * @param network {@link Prefix}
   * @param protocolSpec {@link RoutingProtocolSpecifier} to filter the protocols of the routes
   * @return {@link Map} of {@link RouteRowKey}s to corresponding sub{@link Map}s of {@link
   *     RouteRowSecondaryKey} to {@link SortedSet} of {@link RouteRowAttribute}s
   */
  public static Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>>
      groupBgpRoutes(
          @Nullable Table<String, String, Set<Bgpv4Route>> bgpBestRoutes,
          @Nullable Table<String, String, Set<Bgpv4Route>> bgpBackupRoutes,
          Set<String> matchingNodes,
          String vrfRegex,
          @Nullable Prefix network,
          RoutingProtocolSpecifier protocolSpec) {
    checkArgument(
        bgpBestRoutes != null || bgpBackupRoutes != null,
        "At least one of best routes or backup routes is required.");
    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> routesGroups =
        new HashMap<>();
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);

    Map<BgpRouteStatus, Table<String, String, Set<Bgpv4Route>>> routesByStatus =
        new EnumMap<>(BgpRouteStatus.class);
    if (bgpBestRoutes != null) {
      routesByStatus.put(BEST, bgpBestRoutes);
    }
    if (bgpBackupRoutes != null) {
      routesByStatus.put(BACKUP, bgpBackupRoutes);
    }

    matchingNodes.forEach(
        hostname ->
            routesByStatus.forEach(
                (status, statusRoutes) ->
                    statusRoutes.row(hostname).entrySet().stream()
                        .filter(vrfEntry -> compiledVrfRegex.matcher(vrfEntry.getKey()).matches())
                        .forEach(
                            vrfEntry ->
                                vrfEntry.getValue().stream()
                                    .filter(
                                        route ->
                                            (network == null || network.equals(route.getNetwork()))
                                                && protocolSpec
                                                    .getProtocols()
                                                    .contains(route.getProtocol()))
                                    .forEach(
                                        route ->
                                            routesGroups
                                                .computeIfAbsent(
                                                    new RouteRowKey(
                                                        hostname,
                                                        vrfEntry.getKey(),
                                                        route.getNetwork()),
                                                    k -> new HashMap<>())
                                                .computeIfAbsent(
                                                    new BgpRouteRowSecondaryKey(
                                                        route.getNextHop(),
                                                        route.getProtocol().protocolName(),
                                                        LegacyReceivedFromToIpConverter.convert(
                                                            route.getReceivedFrom()),
                                                        route.getPathId()),
                                                    k -> new TreeSet<>())
                                                .add(bgpRouteToRowAttribute(route, status))))));

    return routesGroups;
  }

  /**
   * Given a {@link Table} of {@link EvpnRoute}s indexed by Node name and VRF name, applies given
   * filters and groups the routes by {@link RouteRowKey} and sub-groups them further by {@link
   * RouteRowSecondaryKey} and for the routes in same sub-groups, sorts them according to {@link
   * RouteRowAttribute}
   *
   * @param evpnBestRoutes {@link Table} of best EVPN routes with rows per node and columns per VRF
   * @param evpnBackupRoutes {@link Table} of backup EVPN routes with rows per node and columns per
   *     VRF
   * @param matchingNodes {@link Set} of nodes to be matched
   * @param vrfRegex Regex to filter the VRF
   * @param network {@link Prefix}
   * @param protocolSpec {@link RoutingProtocolSpecifier} to filter the protocols of the routes
   * @return {@link Map} of {@link RouteRowKey}s to corresponding sub{@link Map}s of {@link
   *     RouteRowSecondaryKey} to {@link SortedSet} of {@link RouteRowAttribute}s
   */
  public static Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>>
      groupEvpnRoutes(
          @Nullable Table<String, String, Set<EvpnRoute<?, ?>>> evpnBestRoutes,
          @Nullable Table<String, String, Set<EvpnRoute<?, ?>>> evpnBackupRoutes,
          Set<String> matchingNodes,
          String vrfRegex,
          @Nullable Prefix network,
          RoutingProtocolSpecifier protocolSpec) {
    checkArgument(
        evpnBestRoutes != null || evpnBackupRoutes != null,
        "At least one of best routes or backup routes is required.");
    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> routesGroups =
        new HashMap<>();
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);

    Map<BgpRouteStatus, Table<String, String, Set<EvpnRoute<?, ?>>>> routesByStatus =
        new EnumMap<>(BgpRouteStatus.class);
    if (evpnBestRoutes != null) {
      routesByStatus.put(BEST, evpnBestRoutes);
    }
    if (evpnBackupRoutes != null) {
      routesByStatus.put(BACKUP, evpnBackupRoutes);
    }

    matchingNodes.forEach(
        hostname ->
            routesByStatus.forEach(
                (status, statusRoutes) ->
                    statusRoutes.row(hostname).entrySet().stream()
                        .filter(vrfEntry -> compiledVrfRegex.matcher(vrfEntry.getKey()).matches())
                        .forEach(
                            vrfEntry ->
                                vrfEntry.getValue().stream()
                                    .filter(
                                        route ->
                                            (network == null || network.equals(route.getNetwork()))
                                                && protocolSpec
                                                    .getProtocols()
                                                    .contains(route.getProtocol()))
                                    .forEach(
                                        route ->
                                            routesGroups
                                                .computeIfAbsent(
                                                    new RouteRowKey(
                                                        hostname,
                                                        vrfEntry.getKey(),
                                                        route.getNetwork()),
                                                    k -> new HashMap<>())
                                                .computeIfAbsent(
                                                    new EvpnRouteRowSecondaryKey(
                                                        route.getNextHop(),
                                                        route.getProtocol().protocolName(),
                                                        route.getReceivedFrom(),
                                                        route.getPathId(),
                                                        route.getRouteDistinguisher()),
                                                    k -> new TreeSet<>())
                                                .add(bgpRouteToRowAttribute(route, status))))));

    return routesGroups;
  }

  /**
   * Converts a BGP route (can be {@link Bgpv4Route} or {@link EvpnRoute}) to {@link
   * RouteRowAttribute}.
   */
  @VisibleForTesting
  static RouteRowAttribute bgpRouteToRowAttribute(BgpRoute<?, ?> route, BgpRouteStatus status) {
    return RouteRowAttribute.builder()
        .setOriginProtocol(
            route.getSrcProtocol() != null ? route.getSrcProtocol().protocolName() : null)
        .setMetric(route.getMetric())
        .setAsPath(route.getAsPath())
        .setLocalPreference(route.getLocalPreference())
        .setClusterList(route.getClusterList())
        .setCommunities(
            route.getCommunities().getCommunities().stream()
                .sorted()
                .map(Community::toString)
                .collect(toImmutableList()))
        .setOriginMechanism(route.getOriginMechanism())
        .setOriginType(route.getOriginType())
        .setOriginatorIp(route.getOriginatorIp())
        .setTag(route.getTag() == Route.UNSET_ROUTE_TAG ? null : route.getTag())
        .setTunnelEncapsulationAttribute(route.getTunnelEncapsulationAttribute())
        .setWeight(route.getWeight())
        .setStatus(status)
        .build();
  }

  /**
   * Given two sorted {@link List}s of {@link RouteRowAttribute}s, outputs a 2-dimensional list
   * showing the diff in the two input lists
   *
   * <p>The aligning is done using two pointers method where we place the two pointers at the
   * beginning of both lists in the beginning and then keep incrementing the one pointing to a
   * smaller element. If the elements at the two pointers are equal, we found a match, otherwise it
   * is a missing route.
   *
   * <p>The output is a nested {@link List} with the inner list of always size 2. The inner list
   * contains {@link RouteRowAttribute} for matching routes and contains nulls for missing routes.
   *
   * @param routeRowAttributes1 First sorted {@link List} of {@link RouteRowAttribute}s
   * @param routeRowAttributes2 Second sorted {@link List} of {@link RouteRowAttribute}s
   * @return A 2-dimensional {@link List} of columns size two showing the diff
   */
  static List<List<RouteRowAttribute>> alignRouteRowAttributes(
      List<RouteRowAttribute> routeRowAttributes1, List<RouteRowAttribute> routeRowAttributes2) {
    List<List<RouteRowAttribute>> alignedRouteRowAttrs = new ArrayList<>();

    // i and j are two pointers initialized to the starting of both lists
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
        // the two pointers are pointing to equal elements, so we found a match
        i++;
        j++;
        alignedRouteRowAttrs.add(Lists.newArrayList(routeRowAttribute1, routeRowAttribute2));
      }
    }

    // adding one sided pairs for whichever list remains un-traversed, the other element in the
    // inner list will be null
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
   * Given two {@link Map}s containing mapping from {@link RouteRowKey} to {@link Map}s of {@link
   * RouteRowSecondaryKey} to {@link SortedSet} of {@link RouteRowAttribute}s, produces diff in the
   * form of a {@link List} of {@link DiffRoutesOutput}
   *
   * @param routesInBase {@link Map} from {@link RouteRowKey} to {@link Map} of {@link
   *     RouteRowSecondaryKey} to {@link SortedSet} of {@link RouteRowAttribute}s in the base
   *     snapshot
   * @param routesInRef {@link Map} from {@link RouteRowKey} to {@link Map} of {@link
   *     RouteRowSecondaryKey} to {@link SortedSet} of {@link RouteRowAttribute}s in the ref
   *     snapshot
   * @return {@link List} of {@link DiffRoutesOutput}
   */
  public static List<DiffRoutesOutput> getRoutesDiff(
      Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> routesInBase,
      Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> routesInRef) {
    Set<RouteRowKey> allRouteKeys = new HashSet<>(routesInBase.keySet());
    allRouteKeys.addAll(routesInRef.keySet());

    List<DiffRoutesOutput> listDiffs = new ArrayList<>();
    for (RouteRowKey routeRowKey : allRouteKeys) {
      Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> baseAttrsForRowKey =
          routesInBase.get(routeRowKey);
      Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> refAttrsForRowKey =
          routesInRef.get(routeRowKey);

      if (baseAttrsForRowKey != null && refAttrsForRowKey != null) {
        // this network is present in routesInBase and routesInRef. check if values are different
        if (!baseAttrsForRowKey.equals(refAttrsForRowKey)) {
          listDiffs.addAll(getDiffPerKey(routeRowKey, baseAttrsForRowKey, refAttrsForRowKey));
        }
      } else if (baseAttrsForRowKey != null) {
        baseAttrsForRowKey.forEach(
            (key, value) -> {
              // the nested list contains list of pairs of RouteRowAttributes with fist element and
              // second element of the pair from base and reference snapshots respectively, second
              // element is null to account for absence of this network in the reference snapshot
              List<List<RouteRowAttribute>> diffMatrix =
                  value.stream()
                      .map(routeRowAttribute -> Lists.newArrayList(routeRowAttribute, null))
                      .collect(Collectors.toList());
              listDiffs.add(
                  new DiffRoutesOutput(
                      routeRowKey,
                      key,
                      KeyPresenceStatus.ONLY_IN_SNAPSHOT,
                      diffMatrix,
                      KeyPresenceStatus.ONLY_IN_SNAPSHOT));
            });
      } else {
        refAttrsForRowKey.forEach(
            (key, value) -> {
              // the nested list contains list of pairs of RouteRowAttributes with fist element and
              // second element of the pair from base and reference snapshots respectively, first
              // element is null to account for absence of this network in the base snapshot
              List<List<RouteRowAttribute>> diffMatrix =
                  value.stream()
                      .map(routeRowAttribute -> Lists.newArrayList(null, routeRowAttribute))
                      .collect(Collectors.toList());
              listDiffs.add(
                  new DiffRoutesOutput(
                      routeRowKey,
                      key,
                      KeyPresenceStatus.ONLY_IN_REFERENCE,
                      diffMatrix,
                      KeyPresenceStatus.ONLY_IN_REFERENCE));
            });
      }
    }
    return listDiffs;
  }

  /**
   * Gets the diff in the form of {@link List} of {@link DiffRoutesOutput} for {@link
   * RouteRowAttribute}s for a given {@link RouteRowKey}
   *
   * @param routeRowKey {@link RouteRowKey} for the {@link RouteRowAttribute}s in innerGroup1 and
   *     innerGroup2
   * @param innerGroup1 {@link Map} from {@link RouteRowSecondaryKey} to {@link SortedSet} of {@link
   *     RouteRowAttribute}s for a given {@link RouteRowKey} in base snapshot
   * @param innerGroup2 {@link Map} from {@link RouteRowSecondaryKey} to {@link SortedSet} of {@link
   *     RouteRowAttribute}s for a given {@link RouteRowKey} in ref snapshot
   * @return {@link List} of {@link DiffRoutesOutput}
   */
  @VisibleForTesting
  static List<DiffRoutesOutput> getDiffPerKey(
      RouteRowKey routeRowKey,
      Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroup1,
      Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroup2) {
    Set<RouteRowSecondaryKey> allRouteKeys = new HashSet<>(innerGroup1.keySet());
    allRouteKeys.addAll(innerGroup2.keySet());
    List<DiffRoutesOutput> diffForThisKey = new ArrayList<>();
    for (RouteRowSecondaryKey secondaryKey : allRouteKeys) {
      // a nested list that contains list of pairs of RouteRowAttributes with fist element and
      // second element from innerGroup1 and innerGroup2 respectively
      List<List<RouteRowAttribute>> diffMatrix;
      KeyPresenceStatus routeRowSecondaryKeyStatus;

      if (innerGroup1.containsKey(secondaryKey) && innerGroup2.containsKey(secondaryKey)) {
        routeRowSecondaryKeyStatus = KeyPresenceStatus.IN_BOTH;
        // both groups have route for this next hop ip and protocol
        SortedSet<RouteRowAttribute> routeRowAttributes1 = innerGroup1.get(secondaryKey);
        SortedSet<RouteRowAttribute> routeRowAttributes2 = innerGroup2.get(secondaryKey);
        if (routeRowAttributes1.size() == 1 && routeRowAttributes2.size() == 1) {
          // no need to align since  only one RouteRowAttribute exists for this secondary key in
          // innerGroup1 and innerGroup2
          diffMatrix =
              ImmutableList.of(
                  ImmutableList.of(routeRowAttributes1.first(), routeRowAttributes2.first()));

        } else {
          // need to align since  number of RowRowAttributes for this secondary key is different in
          // innerGroup1 and innerGroup2
          diffMatrix =
              alignRouteRowAttributes(
                  new ArrayList<>(routeRowAttributes1), new ArrayList<>(routeRowAttributes2));
        }
      } else if (innerGroup1.containsKey(secondaryKey)) {
        routeRowSecondaryKeyStatus = KeyPresenceStatus.ONLY_IN_SNAPSHOT;
        // second element of RouteRowAttribute pairs will be null to account for absence of
        // secondary key in innerGroup2
        diffMatrix =
            innerGroup1.get(secondaryKey).stream()
                .map(routeRowAttribute -> Lists.newArrayList(routeRowAttribute, null))
                .collect(Collectors.toList());
      } else {
        routeRowSecondaryKeyStatus = KeyPresenceStatus.ONLY_IN_REFERENCE;
        // first element of RouteRowAttribute pairs will be null to account for absence of secondary
        // key in innerGroup1
        diffMatrix =
            innerGroup2.get(secondaryKey).stream()
                .map(routeRowAttribute -> Lists.newArrayList(null, routeRowAttribute))
                .collect(Collectors.toList());
      }
      diffForThisKey.add(
          new DiffRoutesOutput(
              routeRowKey,
              secondaryKey,
              routeRowSecondaryKeyStatus,
              diffMatrix,
              KeyPresenceStatus.IN_BOTH));
    }
    return diffForThisKey;
  }
}
