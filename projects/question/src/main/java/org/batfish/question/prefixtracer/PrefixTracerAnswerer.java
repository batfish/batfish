package org.batfish.question.prefixtracer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Computes the answer for a PrefixTracer question. */
class PrefixTracerAnswerer extends Answerer {
  static final String COL_NODE = "Node";
  static final String COL_VRF = "VRF";
  static final String COL_PREFIX = "Prefix";
  static final String COL_ACTION = "Action";
  static final String COL_PEER = "Peer";

  PrefixTracerAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    PrefixTracerQuestion question = (PrefixTracerQuestion) _question;
    TableAnswerElement answer = new TableAnswerElement(getTableMetadata());
    DataPlane dp = _batfish.loadDataPlane(snapshot);

    SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>> prefixTracingInfo =
        dp.getPrefixTracingInfoSummary();
    answer.postProcessAnswer(
        question,
        getRows(
            prefixTracingInfo,
            question.getPrefix(),
            question.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot))));

    return answer;
  }

  @VisibleForTesting
  static Multiset<Row> getRows(
      SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>> prefixTracingInfo,
      @Nullable Prefix desiredPrefix,
      @Nonnull Set<String> nodes) {

    HashMultiset<Row> rows = HashMultiset.create();

    prefixTracingInfo.keySet().stream()
        .filter(nodes::contains)
        .forEach(
            node -> {
              SortedMap<String, Map<Prefix, Map<String, Set<String>>>> vrfs =
                  prefixTracingInfo.get(node);
              vrfs.forEach(
                  (vrf, prefixes) ->
                      prefixes.forEach(
                          (prefix, actions) -> {
                            if (desiredPrefix == null || prefix.equals(desiredPrefix)) {
                              actions.forEach(
                                  (action, neighbors) ->
                                      neighbors.forEach(
                                          neighbor ->
                                              rows.add(
                                                  Row.builder()
                                                      .put(COL_NODE, new Node(node))
                                                      .put(COL_VRF, vrf)
                                                      .put(COL_PEER, new Node(neighbor))
                                                      .put(COL_ACTION, action)
                                                      .put(COL_PREFIX, prefix)
                                                      .build())));
                            }
                          }));
            });
    return rows;
  }

  static TableMetadata getTableMetadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, Schema.NODE, "The node where action takes place"),
            new ColumnMetadata(COL_VRF, Schema.STRING, "The VRF where action takes place"),
            new ColumnMetadata(
                COL_PEER, Schema.NODE, "The node's neighbor to which the action applies"),
            new ColumnMetadata(COL_ACTION, Schema.STRING, "The action that takes place"),
            new ColumnMetadata(COL_PREFIX, Schema.PREFIX, "The prefix in question"));

    return new TableMetadata(columnMetadata, TEXT_DESC);
  }

  private static final String TEXT_DESC =
      String.format(
          "For prefix ${%s}, on node ${%s} action ${%s} was taken for peer {%s}",
          COL_PREFIX, COL_NODE, COL_ACTION, COL_PEER);
}
