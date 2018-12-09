package org.batfish.question.routes;

import static org.batfish.common.topology.TopologyUtil.computeIpNodeOwners;
import static org.batfish.question.routes.RoutesAnswererUtil.getAbstractRouteRows;
import static org.batfish.question.routes.RoutesAnswererUtil.getAbstractRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRouteRows;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getMainRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getRoutesDiff;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;

/** Answerer for {@link RoutesQuestion} */
@ParametersAreNonnullByDefault
public class RoutesAnswerer extends Answerer {
  // Global (always present) columns
  static final String COL_NODE = "Node";
  static final String COL_VRF_NAME = "VRF";
  static final String COL_NETWORK = "Network";
  static final String COL_NEXT_HOPS = "Next_Hops";
  static final String COL_NEXT_HOP_IPS = "Next_Hop_IPs";
  static final String COL_PROTOCOLS = "Protocols";
  static final String COL_TAGs = "Tags";

  // Present sometimes
  static final String COL_METRICS = "Metrics";

  // Main RIB
  static final String COL_ADMIN_DISTANCES = "Admin_Distances";

  // BGP only
  static final String COL_AS_PATHS = "AS_Paths";
  static final String COL_LOCAL_PREFS = "Local_Prefs";
  static final String COL_COMMUNITIES = "Communities";
  static final String COL_ORIGIN_PROTOCOLS = "Origin_Protocols";

  // Diff prefixes
  static final String COL_BASE_PREFIX = "Snapshot_";
  static final String COL_DELTA_PREFIX = "Reference_";

  RoutesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    RoutesQuestion question = (RoutesQuestion) _question;
    TableAnswerElement answer = new TableAnswerElement(getTableMetadata(question.getRib()));

    DataPlane dp = _batfish.loadDataPlane();
    Set<String> matchingNodes = question.getNodes().getMatchingNodes(_batfish);
    Prefix network = question.getNetwork();
    String protocolRegex = question.getProtocols();
    String vrfRegex = question.getVrfs();
    Map<Ip, Set<String>> ipOwners = computeIpNodeOwners(_batfish.loadConfigurations(), true);

    Multiset<Row> rows;

    switch (question.getRib()) {
      case BGP:
        rows =
            getBgpRouteRows(
                getBgpRibRoutes(
                    dp.getBgpRoutes(false),
                    matchingNodes,
                    network,
                    protocolRegex,
                    vrfRegex,
                    ipOwners));
        break;

      case BGPMP:
        rows =
            getBgpRouteRows(
                getBgpRibRoutes(
                    dp.getBgpRoutes(true),
                    matchingNodes,
                    network,
                    protocolRegex,
                    vrfRegex,
                    ipOwners));
        break;
      case MAIN:
      default:
        rows =
            getAbstractRouteRows(
                getMainRibRoutes(
                    dp.getRibs(), matchingNodes, network, protocolRegex, vrfRegex, ipOwners));
    }

    answer.postProcessAnswer(_question, rows);
    return answer;
  }

  @Override
  public AnswerElement answerDiff() {
    RoutesQuestion question = (RoutesQuestion) _question;
    TableAnswerElement diffAnswer =
        new TableAnswerElement(TableDiff.diffMetadata(getTableMetadata(question.getRib())));

    Set<String> matchingNodes = question.getNodes().getMatchingNodes(_batfish);
    Prefix network = question.getNetwork();
    String protocolRegex = question.getProtocols();
    String vrfRegex = question.getVrfs();

    Multiset<Row> rows;
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> routesGroupedByKeyInBase;
    Map<RouteRowKey, SortedSet<RouteRowAttribute>> routesGroupedByKeyInDelta;
    Map<Ip, Set<String>> ipOwners;
    DataPlane dp;

    List<DiffRoutesOutput> routesDiffRaw;

    switch (question.getRib()) {
      case BGP:
        _batfish.pushBaseSnapshot();
        dp = _batfish.loadDataPlane();
        ipOwners = computeIpNodeOwners(_batfish.loadConfigurations(), true);
        routesGroupedByKeyInBase =
            getBgpRibRoutes(
                dp.getBgpRoutes(false), matchingNodes, network, protocolRegex, vrfRegex, ipOwners);
        _batfish.popSnapshot();

        _batfish.pushDeltaSnapshot();
        dp = _batfish.loadDataPlane();
        ipOwners = computeIpNodeOwners(_batfish.loadConfigurations(), true);
        routesGroupedByKeyInDelta =
            getBgpRibRoutes(
                dp.getBgpRoutes(false), matchingNodes, network, protocolRegex, vrfRegex, ipOwners);
        _batfish.popSnapshot();
        routesDiffRaw = getRoutesDiff(routesGroupedByKeyInBase, routesGroupedByKeyInDelta);
        rows = getBgpRouteRowsDiff(routesDiffRaw);
        break;

      case BGPMP:
        _batfish.pushBaseSnapshot();
        dp = _batfish.loadDataPlane();
        ipOwners = computeIpNodeOwners(_batfish.loadConfigurations(), true);
        routesGroupedByKeyInBase =
            getBgpRibRoutes(
                dp.getBgpRoutes(true), matchingNodes, network, protocolRegex, vrfRegex, ipOwners);
        _batfish.popSnapshot();

        _batfish.pushDeltaSnapshot();
        dp = _batfish.loadDataPlane();
        ipOwners = computeIpNodeOwners(_batfish.loadConfigurations(), true);
        routesGroupedByKeyInDelta =
            getBgpRibRoutes(
                dp.getBgpRoutes(true), matchingNodes, network, protocolRegex, vrfRegex, ipOwners);
        _batfish.popSnapshot();
        routesDiffRaw = getRoutesDiff(routesGroupedByKeyInBase, routesGroupedByKeyInDelta);
        rows = getBgpRouteRowsDiff(routesDiffRaw);
        break;

      case MAIN:
      default:
        _batfish.pushBaseSnapshot();
        dp = _batfish.loadDataPlane();
        ipOwners = computeIpNodeOwners(_batfish.loadConfigurations(), true);
        routesGroupedByKeyInBase =
            getMainRibRoutes(
                dp.getRibs(), matchingNodes, network, protocolRegex, vrfRegex, ipOwners);
        _batfish.popSnapshot();

        _batfish.pushDeltaSnapshot();
        dp = _batfish.loadDataPlane();
        ipOwners = computeIpNodeOwners(_batfish.loadConfigurations(), true);
        routesGroupedByKeyInDelta =
            getMainRibRoutes(
                dp.getRibs(), matchingNodes, network, protocolRegex, vrfRegex, ipOwners);
        _batfish.popSnapshot();
        routesDiffRaw = getRoutesDiff(routesGroupedByKeyInBase, routesGroupedByKeyInDelta);
        rows = getAbstractRouteRowsDiff(routesDiffRaw);
    }

    diffAnswer.postProcessAnswer(_question, rows);
    return diffAnswer;
  }

  /** Generate the table metadata based on the {@code rib} we are pulling */
  @VisibleForTesting
  static TableMetadata getTableMetadata(RibProtocol rib) {
    ImmutableList.Builder<ColumnMetadata> columnBuilder = ImmutableList.builder();
    addCommonTableColumnsAtStart(columnBuilder);
    switch (rib) {
      case BGP:
      case BGPMP:
        columnBuilder.add(
            new ColumnMetadata(
                COL_AS_PATHS,
                Schema.list(Schema.STRING),
                "AS paths of routes for this prefix",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_METRICS,
                Schema.list(Schema.INTEGER),
                "Metrics of routes for this prefix",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_LOCAL_PREFS,
                Schema.list(Schema.INTEGER),
                "Local Preferences of routes for this prefix",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_COMMUNITIES,
                Schema.list(Schema.STRING),
                "List list of BGP communities of routes for this prefix",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_ORIGIN_PROTOCOLS,
                Schema.list(Schema.STRING),
                "Origin protocols of routes for this prefix",
                Boolean.FALSE,
                Boolean.TRUE));
        break;
      case MAIN:
      default:
        columnBuilder.add(
            new ColumnMetadata(
                COL_ADMIN_DISTANCES,
                Schema.list(Schema.INTEGER),
                "Admin distances of routes for this prefix",
                Boolean.FALSE,
                Boolean.TRUE));
        columnBuilder.add(
            new ColumnMetadata(
                COL_METRICS,
                Schema.list(Schema.INTEGER),
                "Metrics of routes for this prefix",
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
            COL_TAGs, Schema.list(Schema.INTEGER), "Route tag", Boolean.FALSE, Boolean.TRUE));
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
            COL_NETWORK, Schema.PREFIX, "Route network (prefix)", Boolean.TRUE, Boolean.TRUE));
    columnBuilder.add(
        new ColumnMetadata(
            COL_NEXT_HOPS,
            Schema.list(Schema.STRING),
            "Route's next hop (as node hostname)",
            Boolean.FALSE,
            Boolean.TRUE));
    columnBuilder.add(
        new ColumnMetadata(
            COL_NEXT_HOP_IPS,
            Schema.list(Schema.IP),
            "Next Hop IPs for the given prefix",
            Boolean.FALSE,
            Boolean.TRUE));
    columnBuilder.add(
        new ColumnMetadata(
            COL_PROTOCOLS,
            Schema.list(Schema.STRING),
            "Protocols of all routes for this prefix",
            Boolean.FALSE,
            Boolean.TRUE));
  }
}
