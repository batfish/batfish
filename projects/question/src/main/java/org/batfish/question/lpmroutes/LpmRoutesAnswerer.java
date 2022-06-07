package org.batfish.question.lpmroutes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** An answerer for {@link LpmRoutesQuestion} */
@ParametersAreNonnullByDefault
public class LpmRoutesAnswerer extends Answerer {

  static final String COL_NODE = "Node";
  static final String COL_VRF = "VRF";
  static final String COL_IP = "Ip";
  static final String COL_NETWORK = "Network";
  static final String COL_NUM_ROUTES = "Num_Routes";

  private final Map<String, ColumnMetadata> _columnMap;

  public LpmRoutesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
    _columnMap = getMetadata().toColumnMap();
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    LpmRoutesQuestion q = (LpmRoutesQuestion) _question;
    TableAnswerElement answer = new TableAnswerElement(getMetadata());
    answer.postProcessAnswer(
        _question,
        getRows(
            _batfish.loadDataPlane(snapshot).getRibs(),
            q.getIp(),
            q.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot)),
            Pattern.compile(q.getVrfs()),
            _columnMap));
    return answer;
  }

  @VisibleForTesting
  static List<Row> getRows(
      Table<String, String, FinalMainRib> ribs,
      Ip ip,
      Set<String> nodes,
      Pattern vrfRegex,
      Map<String, ColumnMetadata> columnMap) {

    ImmutableList.Builder<Row> builder = ImmutableList.builder();
    Set<String> sortedNodes =
        ImmutableSortedSet.copyOf(Ordering.natural(), Sets.intersection(nodes, ribs.rowKeySet()));
    for (String node : sortedNodes) {
      Set<String> nodeVrfs = ImmutableSortedSet.copyOf(ribs.row(node).keySet());
      for (String vrf : nodeVrfs) {
        if (!vrfRegex.matcher(vrf).matches()) {
          continue;
        }

        FinalMainRib rib = ribs.get(node, vrf);
        assert rib != null; // iterating over map's keys.

        // TODO: implement resolution restriction
        toRow(rib.longestPrefixMatch(ip), node, vrf, ip, columnMap).ifPresent(builder::add);
      }
    }
    return builder.build();
  }

  @VisibleForTesting
  static <T extends AbstractRouteDecorator> Optional<Row> toRow(
      Set<T> routes, String node, String vrf, Ip ip, Map<String, ColumnMetadata> columnMap) {
    if (routes.isEmpty()) {
      return Optional.empty();
    }
    Prefix network = routes.iterator().next().getNetwork();
    return Optional.of(
        Row.builder(columnMap)
            .put(COL_NODE, new Node(node))
            .put(COL_VRF, vrf)
            .put(COL_IP, ip)
            .put(COL_NETWORK, network)
            .put(COL_NUM_ROUTES, routes.size())
            .build());
  }

  private static TableMetadata getMetadata() {
    return new TableMetadata(getColumnMetadata());
  }

  @VisibleForTesting
  static List<ColumnMetadata> getColumnMetadata() {
    return ImmutableList.<ColumnMetadata>builder()
        .add(
            new ColumnMetadata(
                COL_NODE, Schema.NODE, "Node where the route is present", true, false))
        .add(
            new ColumnMetadata(
                COL_VRF, Schema.STRING, "VRF where the route is present", true, false))
        .add(new ColumnMetadata(COL_IP, Schema.IP, "IP that was being matched on", true, false))
        .add(
            new ColumnMetadata(
                COL_NETWORK, Schema.PREFIX, "The longest-prefix network that matched", false, true))
        .add(
            new ColumnMetadata(
                COL_NUM_ROUTES,
                Schema.INTEGER,
                "Number of routes that matched (in case of ECMP)",
                false,
                true))
        .build();
  }
}
