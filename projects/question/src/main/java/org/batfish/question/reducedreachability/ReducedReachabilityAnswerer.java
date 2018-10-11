package org.batfish.question.reducedreachability;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.table.TableMetadata;

/** An {@link Answerer} for {@link ReducedReachabilityQuestion}. */
public class ReducedReachabilityAnswerer extends Answerer {
  public static final String COL_FLOW = "flow";

  static final String COL_BASE_TRACES = TableDiff.baseColumnName(getTracesColumnName());

  static final String COL_DELTA_TRACES = TableDiff.deltaColumnName(getTracesColumnName());

  public ReducedReachabilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  private static String getTracesColumnName() {
    return "traces";
  }

  @Override
  public TableAnswerElement answer() {
    return answerDiff();
  }

  @Override
  public TableAnswerElement answerDiff() {
    ReducedReachabilityQuestion question = (ReducedReachabilityQuestion) _question;
    ReducedReachabilityResult result = _batfish.bddReducedReachability(question.getActions());

    Set<Flow> flows =
        Sets.union(result.getDecreasedReachabilityFlows(), result.getIncreasedReachabilityFlows());

    _batfish.pushBaseEnvironment();
    _batfish.processFlows(flows, false);
    _batfish.popEnvironment();
    _batfish.pushDeltaEnvironment();
    _batfish.processFlows(flows, false);
    _batfish.popEnvironment();

    FlowHistory flowHistory = _batfish.getHistory();
    Multiset<Row> rows = flowHistoryToRows(flowHistory);
    TableAnswerElement table = new TableAnswerElement(createMetadata());
    table.postProcessAnswer(_question, rows);
    return table;
  }

  private static TableMetadata createMetadata() {
    ImmutableList<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_FLOW, Schema.FLOW, "The flow", true, true),
            new ColumnMetadata(
                COL_BASE_TRACES,
                Schema.set(Schema.FLOW_TRACE),
                "The flow traces in the BASE environment",
                false,
                true),
            new ColumnMetadata(
                COL_DELTA_TRACES,
                Schema.set(Schema.FLOW_TRACE),
                "The flow traces in the DELTA environment",
                false,
                true));
    return new TableMetadata(columnMetadata, "Flows with reduced reachability");
  }

  /**
   * Converts a flowHistory object into a set of Rows. Expects that the traces correspond to only
   * one environment.
   */
  private Multiset<Row> flowHistoryToRows(FlowHistory flowHistory) {
    Multiset<Row> rows = LinkedHashMultiset.create();
    for (FlowHistoryInfo historyInfo : flowHistory.getTraces().values()) {
      rows.add(
          Row.of(
              COL_FLOW,
              historyInfo.getFlow(),
              COL_BASE_TRACES,
              historyInfo.getPaths().get(Flow.BASE_FLOW_TAG),
              COL_DELTA_TRACES,
              historyInfo.getPaths().get(Flow.DELTA_FLOW_TAG)));
    }
    return rows;
  }
}
