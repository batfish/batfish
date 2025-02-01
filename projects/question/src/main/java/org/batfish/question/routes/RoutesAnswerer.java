package org.batfish.question.routes;

import static org.batfish.datamodel.questions.BgpRouteStatus.BACKUP;
import static org.batfish.datamodel.questions.BgpRouteStatus.BEST;
import static org.batfish.datamodel.table.TableDiff.COL_BASE_PREFIX;
import static org.batfish.datamodel.table.TableDiff.COL_DELTA_PREFIX;
import static org.batfish.question.routes.RoutesAnswererUtil.getAbstractRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getEvpnRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getEvpnRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getMainRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getRoutesDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.groupBgpRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.groupEvpnRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.groupRoutes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.NextHopComparator;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.BgpRouteStatus;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Answerer for {@link RoutesQuestion} */
@ParametersAreNonnullByDefault
public class RoutesAnswerer extends Answerer {
  // Global (always present) columns
  static final String COL_NODE = "Node";
  static final String COL_VRF_NAME = "VRF";
  static final String COL_NETWORK = "Network";
  static final String COL_NEXT_HOP = "Next_Hop";
  static final String COL_NEXT_HOP_INTERFACE = "Next_Hop_Interface";
  static final String COL_NEXT_HOP_IP = "Next_Hop_IP";
  static final String COL_PROTOCOL = "Protocol";
  static final String COL_TAG = "Tag";

  // Present sometimes
  static final String COL_METRIC = "Metric";

  // Main RIB
  static final String COL_ADMIN_DISTANCE = "Admin_Distance";

  // BGP only
  static final String COL_AS_PATH = "AS_Path";
  static final String COL_LOCAL_PREF = "Local_Pref";
  static final String COL_COMMUNITIES = "Communities";
  static final String COL_ORIGIN_PROTOCOL = "Origin_Protocol";
  static final String COL_ORIGIN_TYPE = "Origin_Type";
  static final String COL_CLUSTER_LIST = "Cluster_List";
  static final String COL_ORIGINATOR_ID = "Originator_Id";
  static final String COL_PATH_ID = "Received_Path_Id";
  static final String COL_RECEIVED_FROM_IP = "Received_From_IP";
  static final String COL_STATUS = "Status";
  static final String COL_TUNNEL_ENCAPSULATION_ATTRIBUTE = "Tunnel_Encapsulation_Attribute";
  static final String COL_WEIGHT = "Weight";

  // EVPN BGP only
  static final String COL_ROUTE_DISTINGUISHER = "Route_Distinguisher";

  // Diff Only
  static final String COL_ROUTE_ENTRY_PRESENCE = "Entry_Presence";

  static final String WARNING_NO_MATCHING_NODES = "No matching nodes found.";
  static final String WARNING_NO_MATCHING_VRFS = "No matching VRFs found on matching nodes.";

  RoutesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    RoutesQuestion question = (RoutesQuestion) _question;
    Set<BgpRouteStatus> expandedBgpRouteStatuses =
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                question.getBgpRouteStatus(),
                Grammar.BGP_ROUTE_STATUS_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.of(BgpRouteStatus.BEST)))
            .resolve();

    TableAnswerElement answer = new TableAnswerElement(getTableMetadata(question.getRib()));

    DataPlane dp = _batfish.loadDataPlane(snapshot);
    Set<String> matchingNodes =
        question.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot));

    Prefix network = question.getNetwork();
    RoutingProtocolSpecifier protocolSpec = question.getRoutingProtocolSpecifier();
    String vrfRegex = question.getVrfs();

    Multimap<String, String> matchingVrfsByNode =
        computeMatchingVrfsByNode(_batfish.loadConfigurations(snapshot), matchingNodes, vrfRegex);

    if (matchingNodes.isEmpty()) {
      answer.addWarning(WARNING_NO_MATCHING_NODES);
    } else if (matchingVrfsByNode.isEmpty()) {
      answer.addWarning(WARNING_NO_MATCHING_VRFS);
    }

    List<Row> rows = new ArrayList<>();

    switch (question.getRib()) {
      case BGP -> {
        rows.addAll(
            getBgpRibRoutes(
                dp.getBgpRoutes(),
                dp.getBgpBackupRoutes(),
                matchingVrfsByNode,
                network,
                protocolSpec,
                expandedBgpRouteStatuses,
                question.getPrefixMatchType()));
        rows.sort(BGP_COMPARATOR);
      }
      case EVPN -> {
        rows.addAll(
            getEvpnRoutes(
                dp.getEvpnRoutes(),
                dp.getEvpnBackupRoutes(),
                matchingVrfsByNode,
                network,
                protocolSpec,
                ImmutableSet.of(BEST, BACKUP),
                question.getPrefixMatchType()));
        rows.sort(EVPN_COMPARATOR);
      }
      case MAIN -> {
        rows.addAll(
            getMainRibRoutes(
                dp.getRibs(),
                matchingVrfsByNode,
                network,
                protocolSpec,
                question.getPrefixMatchType()));
        rows.sort(MAIN_RIB_COMPARATOR);
      }
    }

    answer.postProcessAnswer(_question, rows);
    return answer;
  }

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {
    RoutesQuestion question = (RoutesQuestion) _question;
    TableAnswerElement diffAnswer = new TableAnswerElement(getDiffTableMetadata(question.getRib()));

    Set<String> matchingNodes =
        question.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot));
    Prefix network = question.getNetwork();
    RoutingProtocolSpecifier protocolSpec = question.getRoutingProtocolSpecifier();
    String vrfRegex = question.getVrfs();
    Set<BgpRouteStatus> expandedBgpRouteStatuses =
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                question.getBgpRouteStatus(),
                Grammar.BGP_ROUTE_STATUS_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.of(BgpRouteStatus.BEST)))
            .resolve();

    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>>
        routesGroupedByKeyInBase;
    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>>
        routesGroupedByKeyInDelta;
    DataPlane dp;

    List<DiffRoutesOutput> routesDiffRaw;

    List<Row> rows =
        switch (question.getRib()) {
          case BGP -> {
            dp = _batfish.loadDataPlane(snapshot);
            routesGroupedByKeyInBase =
                groupBgpRoutes(
                    expandedBgpRouteStatuses.contains(BEST) ? dp.getBgpRoutes() : null,
                    expandedBgpRouteStatuses.contains(BACKUP) ? dp.getBgpBackupRoutes() : null,
                    matchingNodes,
                    vrfRegex,
                    network,
                    protocolSpec);

            dp = _batfish.loadDataPlane(reference);
            routesGroupedByKeyInDelta =
                groupBgpRoutes(
                    expandedBgpRouteStatuses.contains(BEST) ? dp.getBgpRoutes() : null,
                    expandedBgpRouteStatuses.contains(BACKUP) ? dp.getBgpBackupRoutes() : null,
                    matchingNodes,
                    vrfRegex,
                    network,
                    protocolSpec);
            routesDiffRaw = getRoutesDiff(routesGroupedByKeyInBase, routesGroupedByKeyInDelta);
            yield new ArrayList<>(getBgpRouteRowsDiff(routesDiffRaw));
          }
          case EVPN -> {
            dp = _batfish.loadDataPlane(snapshot);
            routesGroupedByKeyInBase =
                groupEvpnRoutes(
                    expandedBgpRouteStatuses.contains(BEST) ? dp.getEvpnRoutes() : null,
                    expandedBgpRouteStatuses.contains(BACKUP) ? dp.getEvpnBackupRoutes() : null,
                    matchingNodes,
                    vrfRegex,
                    network,
                    protocolSpec);

            dp = _batfish.loadDataPlane(reference);
            routesGroupedByKeyInDelta =
                groupEvpnRoutes(
                    expandedBgpRouteStatuses.contains(BEST) ? dp.getEvpnRoutes() : null,
                    expandedBgpRouteStatuses.contains(BACKUP) ? dp.getEvpnBackupRoutes() : null,
                    matchingNodes,
                    vrfRegex,
                    network,
                    protocolSpec);
            routesDiffRaw = getRoutesDiff(routesGroupedByKeyInBase, routesGroupedByKeyInDelta);
            yield new ArrayList<>(getEvpnRouteRowsDiff(routesDiffRaw));
          }
          case MAIN -> {
            dp = _batfish.loadDataPlane(snapshot);
            routesGroupedByKeyInBase =
                groupRoutes(dp.getRibs(), matchingNodes, network, vrfRegex, protocolSpec);

            dp = _batfish.loadDataPlane(reference);
            routesGroupedByKeyInDelta =
                groupRoutes(dp.getRibs(), matchingNodes, network, vrfRegex, protocolSpec);

            routesDiffRaw = getRoutesDiff(routesGroupedByKeyInBase, routesGroupedByKeyInDelta);
            yield new ArrayList<>(getAbstractRouteRowsDiff(routesDiffRaw));
          }
        };

    rows.sort(DIFF_COMPARATOR);
    diffAnswer.postProcessAnswer(_question, rows);
    return diffAnswer;
  }

  @VisibleForTesting
  static final Comparator<Row> MAIN_RIB_COMPARATOR =
      Comparator.<Row, String>comparing(row -> row.getNode(COL_NODE).getName())
          .thenComparing(row -> row.getString(COL_VRF_NAME))
          .thenComparing(row -> row.getPrefix(COL_NETWORK))
          .thenComparing(row -> row.getNextHop(COL_NEXT_HOP), NextHopComparator.instance());

  @VisibleForTesting
  static final Comparator<Row> BGP_COMPARATOR =
      Comparator.<Row, String>comparing(row -> row.getNode(COL_NODE).getName())
          .thenComparing(row -> row.getString(COL_VRF_NAME))
          .thenComparing(row -> row.getPrefix(COL_NETWORK))
          .thenComparing(row -> row.getNextHop(COL_NEXT_HOP), NextHopComparator.instance())
          .thenComparing(row -> row.getIp(COL_RECEIVED_FROM_IP))
          .thenComparing(
              row -> row.getInteger(COL_PATH_ID), Comparator.nullsFirst(Comparator.naturalOrder()));

  @VisibleForTesting
  static final Comparator<Row> EVPN_COMPARATOR =
      Comparator.<Row, String>comparing(row -> row.getNode(COL_NODE).getName())
          .thenComparing(row -> row.getString(COL_VRF_NAME))
          .thenComparing(row -> row.getPrefix(COL_NETWORK))
          .thenComparing(row -> row.getNextHop(COL_NEXT_HOP), NextHopComparator.instance())
          .thenComparing(
              row -> row.getInteger(COL_PATH_ID), Comparator.nullsFirst(Comparator.naturalOrder()));

  // TODO: Finer-grained sorting using diffed columns.
  @VisibleForTesting
  static final Comparator<Row> DIFF_COMPARATOR =
      Comparator.<Row, String>comparing(row -> row.getNode(COL_NODE).getName())
          .thenComparing(row -> row.getString(COL_VRF_NAME))
          .thenComparing(row -> row.getPrefix(COL_NETWORK))
          .thenComparing(row -> row.getString(COL_ROUTE_ENTRY_PRESENCE));

  /** Generate the table metadata based on the {@code rib} we are pulling */
  @VisibleForTesting
  static TableMetadata getTableMetadata(RibProtocol rib) {
    ImmutableList.Builder<ColumnMetadata> columnBuilder = ImmutableList.builder();
    addCommonTableColumnsAtStart(columnBuilder);
    switch (rib) {
      case EVPN:
        columnBuilder
            .add(
                new ColumnMetadata(
                    COL_STATUS,
                    Schema.list(Schema.STRING),
                    "Route's statuses",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ROUTE_DISTINGUISHER,
                    Schema.STRING,
                    "Route distinguisher",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP, Schema.NEXT_HOP, "Route's Next Hop", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP_IP, Schema.IP, "Route's Next Hop IP", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP_INTERFACE,
                    Schema.STRING,
                    "Route's Next Hop Interface",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_PROTOCOL, Schema.STRING, "Route's Protocol", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_AS_PATH, Schema.STRING, "Route's AS path", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_METRIC, Schema.LONG, "Route's Metric", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_LOCAL_PREF,
                    Schema.LONG,
                    "Route's Local Preference",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_COMMUNITIES,
                    Schema.list(Schema.STRING),
                    "Route's List of communities",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ORIGIN_PROTOCOL,
                    Schema.STRING,
                    "Route's Origin protocol",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ORIGIN_TYPE,
                    Schema.STRING,
                    "Route's Origin type",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ORIGINATOR_ID,
                    Schema.STRING,
                    "Route's Originator ID",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(new ColumnMetadata(COL_PATH_ID, Schema.INTEGER, "Route's Received Path ID"))
            .add(
                new ColumnMetadata(
                    COL_CLUSTER_LIST,
                    Schema.set(Schema.LONG),
                    "Route's Cluster List",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                    Schema.STRING,
                    "Route's BGP Tunnel Encapsulation Attribute",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_WEIGHT, Schema.INTEGER, "Route's BGP Weight", Boolean.FALSE, Boolean.TRUE));
        break;
      case BGP:
        columnBuilder
            .add(
                new ColumnMetadata(
                    COL_STATUS,
                    Schema.list(Schema.STRING),
                    "Route's statuses",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP, Schema.NEXT_HOP, "Route's Next Hop", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP_IP, Schema.IP, "Route's Next Hop IP", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP_INTERFACE,
                    Schema.STRING,
                    "Route's Next Hop Interface",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_PROTOCOL, Schema.STRING, "Route's Protocol", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_AS_PATH, Schema.STRING, "Route's AS path", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_METRIC, Schema.LONG, "Route's Metric", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_LOCAL_PREF,
                    Schema.LONG,
                    "Route's Local Preference",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_COMMUNITIES,
                    Schema.list(Schema.STRING),
                    "Route's List of communities",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ORIGIN_PROTOCOL,
                    Schema.STRING,
                    "Route's Origin protocol",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ORIGIN_TYPE,
                    Schema.STRING,
                    "Route's Origin type",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ORIGINATOR_ID,
                    Schema.STRING,
                    "Route's Originator ID",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_RECEIVED_FROM_IP, Schema.IP, "IP of the neighbor who sent this route"))
            .add(new ColumnMetadata(COL_PATH_ID, Schema.INTEGER, "Route's Received Path ID"))
            .add(
                new ColumnMetadata(
                    COL_CLUSTER_LIST,
                    Schema.set(Schema.LONG),
                    "Route's Cluster List",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                    Schema.STRING,
                    "Route's BGP Tunnel Encapsulation Attribute",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_WEIGHT, Schema.INTEGER, "Route's BGP Weight", Boolean.FALSE, Boolean.TRUE));
        break;
      case MAIN:
        columnBuilder
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP, Schema.NEXT_HOP, "Route's Next Hop", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP_IP, Schema.IP, "Route's Next Hop IP", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_NEXT_HOP_INTERFACE,
                    Schema.STRING,
                    "Route's Next Hop Interface",
                    Boolean.FALSE,
                    Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_PROTOCOL, Schema.STRING, "Route's Protocol", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_METRIC, Schema.LONG, "Route's Metric", Boolean.FALSE, Boolean.TRUE))
            .add(
                new ColumnMetadata(
                    COL_ADMIN_DISTANCE,
                    Schema.INTEGER,
                    "Route's Admin distance",
                    Boolean.FALSE,
                    Boolean.TRUE));
    }
    addCommonTableColumnsAtEnd(columnBuilder);
    return new TableMetadata(columnBuilder.build(), "Display RIB routes");
  }

  /** Generate table columns that should be always present, at the end of table. */
  private static void addCommonTableColumnsAtEnd(
      ImmutableList.Builder<ColumnMetadata> columnBuilder) {
    columnBuilder.add(
        new ColumnMetadata(
            COL_TAG, Schema.LONG, "Tag for this route", Boolean.FALSE, Boolean.TRUE));
  }

  /** Generate table columns that should be always present, at the start of table. */
  private static void addCommonTableColumnsAtStart(
      ImmutableList.Builder<ColumnMetadata> columnBuilder) {
    columnBuilder.add(
        new ColumnMetadata(COL_NODE, Schema.NODE, "Node", Boolean.TRUE, Boolean.FALSE));
    columnBuilder.add(
        new ColumnMetadata(COL_VRF_NAME, Schema.STRING, "VRF name", Boolean.TRUE, Boolean.FALSE));
    columnBuilder.add(
        new ColumnMetadata(
            COL_NETWORK, Schema.PREFIX, "Network for this route", Boolean.TRUE, Boolean.TRUE));
  }

  /** Generate the table metadata based on the {@code rib} we are pulling */
  @VisibleForTesting
  static TableMetadata getDiffTableMetadata(RibProtocol rib) {
    ImmutableList.Builder<ColumnMetadata> columnBuilder = ImmutableList.builder();
    addCommonTableColumnsAtStart(columnBuilder);
    columnBuilder.add(
        new ColumnMetadata(
            COL_ROUTE_ENTRY_PRESENCE,
            Schema.STRING,
            "Presence of a Route for the given Network (Prefix)",
            Boolean.FALSE,
            Boolean.TRUE));
    switch (rib) {
      case BGP:
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_STATUS,
                Schema.list(Schema.STRING),
                "Route's statuses",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_STATUS,
                Schema.list(Schema.STRING),
                "Route's statuses",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_NEXT_HOP,
                Schema.NEXT_HOP,
                "Route's Next Hop",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_NEXT_HOP,
                Schema.NEXT_HOP,
                "Route's Next Hop",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_NEXT_HOP_IP,
                Schema.IP,
                "Route's Next Hop IP",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_NEXT_HOP_IP,
                Schema.IP,
                "Route's Next Hop IP",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_PROTOCOL,
                Schema.STRING,
                "Route's Protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_PROTOCOL,
                Schema.STRING,
                "Route's Protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_AS_PATH,
                Schema.STRING,
                "Route's AS path",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_AS_PATH,
                Schema.STRING,
                "Route's AS path",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_METRIC,
                Schema.LONG,
                "Route's Metric",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_METRIC,
                Schema.LONG,
                "Route's Metric",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_LOCAL_PREF,
                Schema.LONG,
                "Route's Local Preference",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_LOCAL_PREF,
                Schema.LONG,
                "Route's Local Preference",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_COMMUNITIES,
                Schema.list(Schema.STRING),
                "Route's List of communities",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_COMMUNITIES,
                Schema.list(Schema.STRING),
                "Route's List of communities",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ORIGIN_PROTOCOL,
                Schema.STRING,
                "Route's Origin protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ORIGIN_PROTOCOL,
                Schema.STRING,
                "Route's Origin protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ORIGIN_TYPE,
                Schema.STRING,
                "Route's Origin type",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ORIGIN_TYPE,
                Schema.STRING,
                "Route's Origin type",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ORIGINATOR_ID,
                Schema.STRING,
                "Route's Originator ID",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ORIGINATOR_ID,
                Schema.STRING,
                "Route's Originator ID",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_RECEIVED_FROM_IP,
                Schema.IP,
                "Route's Received from IP",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_RECEIVED_FROM_IP,
                Schema.IP,
                "Route's Received from IP",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_PATH_ID, Schema.INTEGER, "Route's Received Path ID"));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_PATH_ID, Schema.INTEGER, "Route's Received Path ID"));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_CLUSTER_LIST,
                Schema.set(Schema.LONG),
                "Route's Cluster List",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_CLUSTER_LIST,
                Schema.set(Schema.LONG),
                "Route's Cluster List",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                Schema.STRING,
                "Route's BGP Tunnel Encapsulation Attribute",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                Schema.STRING,
                "Route's BGP Tunnel Encapsulation Attribute",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_WEIGHT,
                Schema.INTEGER,
                "Route's BGP weight",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_WEIGHT,
                Schema.INTEGER,
                "Route's BGP weight",
                Boolean.FALSE,
                Boolean.TRUE));
        break;
      case EVPN:
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_STATUS,
                Schema.list(Schema.STRING),
                "Route's statuses",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_STATUS,
                Schema.list(Schema.STRING),
                "Route's statuses",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ROUTE_DISTINGUISHER,
                Schema.STRING,
                "Route distinguisher",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ROUTE_DISTINGUISHER,
                Schema.STRING,
                "Route distinguisher",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_NEXT_HOP,
                Schema.NEXT_HOP,
                "Route's Next Hop",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_NEXT_HOP,
                Schema.NEXT_HOP,
                "Route's Next Hop",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_PROTOCOL,
                Schema.STRING,
                "Route's Protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_PROTOCOL,
                Schema.STRING,
                "Route's Protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_AS_PATH,
                Schema.STRING,
                "Route's AS path",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_AS_PATH,
                Schema.STRING,
                "Route's AS path",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_METRIC,
                Schema.LONG,
                "Route's Metric",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_METRIC,
                Schema.LONG,
                "Route's Metric",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_LOCAL_PREF,
                Schema.LONG,
                "Route's Local Preference",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_LOCAL_PREF,
                Schema.LONG,
                "Route's Local Preference",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_COMMUNITIES,
                Schema.list(Schema.STRING),
                "Route's List of communities",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_COMMUNITIES,
                Schema.list(Schema.STRING),
                "Route's List of communities",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ORIGIN_PROTOCOL,
                Schema.STRING,
                "Route's Origin protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ORIGIN_PROTOCOL,
                Schema.STRING,
                "Route's Origin protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ORIGIN_TYPE,
                Schema.STRING,
                "Route's Origin type",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ORIGIN_TYPE,
                Schema.STRING,
                "Route's Origin type",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ORIGINATOR_ID,
                Schema.STRING,
                "Route's Originator ID",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ORIGINATOR_ID,
                Schema.STRING,
                "Route's Originator ID",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_PATH_ID, Schema.INTEGER, "Route's Received Path ID"));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_PATH_ID, Schema.INTEGER, "Route's Received Path ID"));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_CLUSTER_LIST,
                Schema.set(Schema.LONG),
                "Route's Cluster List",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_CLUSTER_LIST,
                Schema.set(Schema.LONG),
                "Route's Cluster List",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                Schema.STRING,
                "Route's BGP Tunnel Encapsulation Attribute",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                Schema.STRING,
                "Route's BGP Tunnel Encapsulation Attribute",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_WEIGHT,
                Schema.INTEGER,
                "Route's BGP weight",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_WEIGHT,
                Schema.INTEGER,
                "Route's BGP weight",
                Boolean.FALSE,
                Boolean.TRUE));
        break;
      case MAIN:
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_NEXT_HOP,
                Schema.NEXT_HOP,
                "Route's Next Hop",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_NEXT_HOP,
                Schema.NEXT_HOP,
                "Route's Next Hop",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_NEXT_HOP_IP,
                Schema.IP,
                "Route's Next Hop IP",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_NEXT_HOP_IP,
                Schema.IP,
                "Route's Next Hop IP",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_NEXT_HOP_INTERFACE,
                Schema.STRING,
                "Route's Next Hop Interface",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_NEXT_HOP_INTERFACE,
                Schema.STRING,
                "Route's Next Hop Interface",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_PROTOCOL,
                Schema.STRING,
                "Route's Protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_PROTOCOL,
                Schema.STRING,
                "Route's Protocol",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_METRIC,
                Schema.LONG,
                "Route's Metric",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_METRIC,
                Schema.LONG,
                "Route's Metric",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_BASE_PREFIX + COL_ADMIN_DISTANCE,
                Schema.INTEGER,
                "Route's Admin distance",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_DELTA_PREFIX + COL_ADMIN_DISTANCE,
                Schema.INTEGER,
                "Route's Admin distance",
                Boolean.FALSE,
                Boolean.TRUE));
        break;
    }
    columnBuilder.add(
        new ColumnMetadata(
            COL_BASE_PREFIX + COL_TAG,
            Schema.LONG,
            "Tag for this route",
            Boolean.FALSE,
            Boolean.TRUE));
    columnBuilder.add(
        new ColumnMetadata(
            COL_DELTA_PREFIX + COL_TAG,
            Schema.LONG,
            "Tag for this route",
            Boolean.FALSE,
            Boolean.TRUE));

    return new TableMetadata(columnBuilder.build(), "Display diff of RIB routes");
  }

  static Multimap<String, String> computeMatchingVrfsByNode(
      Map<String, Configuration> configs, Set<String> matchingNodes, String vrfRegex) {
    Predicate<String> vrfPredicate = Pattern.compile(vrfRegex).asPredicate();
    ImmutableMultimap.Builder<String, String> vrfsByNode = ImmutableMultimap.builder();
    matchingNodes.forEach(
        nodeName ->
            configs.get(nodeName).getVrfs().keySet().stream()
                .filter(vrfPredicate)
                .forEach(vrfName -> vrfsByNode.put(nodeName, vrfName)));
    return vrfsByNode.build();
  }
}
