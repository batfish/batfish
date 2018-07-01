package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;

/** Answerer for {@link RoutesQuestion} */
@ParametersAreNonnullByDefault
public class RoutesAnswerer extends Answerer {
  // Global (always present) columns
  static final String COL_NODE = "Node";
  static final String COL_VRF_NAME = "VRF";
  static final String COL_NETWORK = "Network";
  static final String COL_NEXT_HOP_IP = "NextHopIp";

  // Present sometimes
  static final String COL_NEXT_HOP = "NextHop";
  static final String COL_PROTOCOL = "Protocol";

  // BGP only
  static final String COL_AS_PATH = "AsPath";
  static final String COL_METRIC = "Metric";
  static final String COL_LOCAL_PREF = "LocalPref";
  static final String COL_COMMUNITIES = "Communities";
  static final String COL_ORIGIN_PROTOCOL = "OriginProtocol";
  static final String VALUE_NA = "N/A";

  RoutesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    RoutesQuestion question = (RoutesQuestion) _question;
    TableAnswerElement answer = new TableAnswerElement(getTableMetadata(question.getProtocol()));
    DataPlane dp = _batfish.loadDataPlane();
    answer.postProcessAnswer(
        _question,
        generateRows(
            dp,
            question.getProtocol(),
            question.getNodeRegex().getMatchingNodes(_batfish),
            question.getVrfRegex()));
    return answer;
  }

  private static Multiset<Row> generateRows(
      DataPlane dp, RibProtocol protocol, Set<String> matchingNodes, String vrfRegex) {
    switch (protocol) {
      case BGP:
        return getBgpRibRoutes(dp.getBgpRoutes(false), matchingNodes, vrfRegex);
      case BGPMP:
        return getBgpRibRoutes(dp.getBgpRoutes(true), matchingNodes, vrfRegex);
      case ALL:
      default:
        return getMainRibRoutes(dp.getRibs(), matchingNodes, vrfRegex);
    }
  }

  private static Multiset<Row> getBgpRibRoutes(
      Table<String, String, Set<BgpRoute>> bgpRoutes, Set<String> matchingNodes, String vrfRegex) {
    HashMultiset<Row> rows = HashMultiset.create();
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    matchingNodes.forEach(
        hostname ->
            bgpRoutes
                .row(hostname)
                .forEach(
                    (vrfName, routes) -> {
                      if (compiledVrfRegex.matcher(vrfName).matches()) {
                        rows.addAll(getRowsForBgpRoutes(hostname, vrfName, routes));
                      }
                    }));
    return rows;
  }

  /** Get the rows for MainRib routes. */
  @VisibleForTesting
  static Multiset<Row> getMainRibRoutes(
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      Set<String> matchingNodes,
      String vrfRegex) {
    HashMultiset<Row> rows = HashMultiset.create();
    Pattern compiledVrfRegex = Pattern.compile(vrfRegex);
    ribs.forEach(
        (node, vrfMap) -> {
          if (matchingNodes.contains(node)) {
            vrfMap.forEach(
                (vrfName, rib) -> {
                  if (compiledVrfRegex.matcher(vrfName).matches()) {
                    rows.addAll(getRowsForAbstractRoutes(node, vrfName, rib.getRoutes()));
                  }
                });
          }
        });
    return rows;
  }

  /** Convert a {@link Set} of {@link AbstractRoute} into a list of rows. */
  @Nonnull
  private static List<Row> getRowsForAbstractRoutes(
      String node, String vrfName, Set<AbstractRoute> routes) {
    Node nodeObj = new Node(node);
    return routes
        .stream()
        .map(
            route ->
                Row.builder()
                    .put(COL_NODE, nodeObj)
                    .put(COL_VRF_NAME, vrfName)
                    .put(COL_NETWORK, route.getNetwork())
                    .put(COL_NEXT_HOP_IP, route.getNextHopIp())
                    .put(COL_NEXT_HOP, firstNonNull(route.getNextHop(), VALUE_NA))
                    .put(COL_PROTOCOL, route.getProtocol())
                    .build())
        .collect(toImmutableList());
  }

  /** Convert a set of {@link BgpRoute} into a list of rows. */
  @Nonnull
  @VisibleForTesting
  static List<Row> getRowsForBgpRoutes(String hostname, String vrfName, Set<BgpRoute> routes) {
    Node nodeObj = new Node(hostname);
    return routes
        .stream()
        .map(
            route ->
                Row.builder()
                    .put(COL_NODE, nodeObj)
                    .put(COL_VRF_NAME, vrfName)
                    .put(COL_NETWORK, route.getNetwork())
                    .put(COL_NEXT_HOP_IP, route.getNextHopIp())
                    .put(COL_PROTOCOL, route.getProtocol())
                    .put(COL_AS_PATH, route.getAsPath().getAsPathString())
                    .put(COL_METRIC, route.getMetric())
                    .put(COL_LOCAL_PREF, route.getLocalPreference())
                    .put(
                        COL_COMMUNITIES,
                        route
                            .getCommunities()
                            .stream()
                            .map(CommonUtil::longToCommunity)
                            .collect(toImmutableList()))
                    .put(COL_ORIGIN_PROTOCOL, firstNonNull(route.getSrcProtocol(), VALUE_NA))
                    .build())
        .collect(toImmutableList());
  }

  /** Generate the table metadata based on the protocol for which we are examining the RIBs */
  @VisibleForTesting
  static TableMetadata getTableMetadata(RibProtocol protocol) {
    ImmutableList.Builder<ColumnMetadata> columnBuilder = ImmutableList.builder();
    addCommonTableColumns(columnBuilder);
    switch (protocol) {
      case BGP:
        columnBuilder.add(new ColumnMetadata(COL_NEXT_HOP_IP, Schema.IP, "Route's next hop IP"));
        columnBuilder.add(new ColumnMetadata(COL_AS_PATH, Schema.STRING, "AS path"));
        columnBuilder.add(new ColumnMetadata(COL_METRIC, Schema.INTEGER, "Metric"));
        columnBuilder.add(new ColumnMetadata(COL_LOCAL_PREF, Schema.INTEGER, "Local Preference"));
        columnBuilder.add(
            new ColumnMetadata(COL_COMMUNITIES, Schema.list(Schema.STRING), "BGP communities"));
        columnBuilder.add(
            new ColumnMetadata(COL_ORIGIN_PROTOCOL, Schema.STRING, "Origin protocol"));
        break;
      case ALL:
      default:
        columnBuilder.add(
            new ColumnMetadata(COL_NEXT_HOP, Schema.STRING, "Route's next hop (as node hostname)"));
        columnBuilder.add(new ColumnMetadata(COL_NEXT_HOP_IP, Schema.IP, "Route's next hop IP"));
    }
    return new TableMetadata(columnBuilder.build(), new DisplayHints());
  }

  /** Generate table columns that should be always present, at the start of table. */
  private static void addCommonTableColumns(ImmutableList.Builder<ColumnMetadata> columnBuilder) {
    columnBuilder.add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node"));
    columnBuilder.add(new ColumnMetadata(COL_VRF_NAME, Schema.STRING, "VRF name"));
    columnBuilder.add(new ColumnMetadata(COL_NETWORK, Schema.PREFIX, "Route network (prefix)"));
    columnBuilder.add(new ColumnMetadata(COL_PROTOCOL, Schema.STRING, "Route protocol"));
  }
}
