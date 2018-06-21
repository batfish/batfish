package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
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
  static final String COL_NODE = "Node";
  static final String COL_VRF_NAME = "VRF";
  static final String COL_NETWORK = "Network";
  static final String COL_NEXT_HOP = "NextHop";
  static final String COL_NEXT_HOP_IP = "NextHopIp";
  static final String COL_PROTOCOL = "Protocol";

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
      case ALL:
      default:
        return getMainRibRoutes(dp.getRibs(), matchingNodes, vrfRegex);
    }
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
                    .put(COL_NEXT_HOP, firstNonNull(route.getNextHop(), "N/A"))
                    .put(COL_PROTOCOL, route.getProtocol())
                    .build())
        .collect(ImmutableList.toImmutableList());
  }

  /** Generate the table metadata based on the protocol for which we are examining the RIBs */
  @VisibleForTesting
  static TableMetadata getTableMetadata(RibProtocol protocol) {
    switch (protocol) {
      case ALL:
      default:
        ImmutableList.Builder<ColumnMetadata> columnBuilder = ImmutableList.builder();
        columnBuilder.add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node"));
        columnBuilder.add(new ColumnMetadata(COL_VRF_NAME, Schema.STRING, "VRF name"));
        columnBuilder.add(new ColumnMetadata(COL_NETWORK, Schema.PREFIX, "Route network (prefix)"));
        columnBuilder.add(new ColumnMetadata(COL_PROTOCOL, Schema.STRING, "Route protocol"));
        columnBuilder.add(
            new ColumnMetadata(COL_NEXT_HOP, Schema.STRING, "Route's next hop (as node hostname)"));
        columnBuilder.add(new ColumnMetadata(COL_NEXT_HOP_IP, Schema.IP, "Route's next hop IP"));
        return new TableMetadata(columnBuilder.build(), new DisplayHints());
    }
  }
}
