package org.batfish.question.reducedreachability;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.table.TableMetadata;

/** An {@link Answerer} for {@link ReducedReachabilityQuestion}. */
public class ReducedReachabilityAnswerer extends Answerer {
  static final String COL_FLOW = "flow";

  static final String COL_TRACES = "traces";

  static final String COL_BASE_TRACES = TableDiff.baseColumnName(COL_TRACES);

  static final String COL_DELTA_TRACES = TableDiff.deltaColumnName(COL_TRACES);

  public ReducedReachabilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    return answerDiff();
  }

  @Override
  public AnswerElement answerDiff() {
    Set<Flow> flows = _batfish.bddReducedReachability();
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
  private static Multiset<Row> flowHistoryToRows(FlowHistory flowHistory) {
    Multiset<Row> rows = LinkedHashMultiset.create();
    for (FlowHistoryInfo historyInfo : flowHistory.getTraces().values()) {
      rows.add(
          Row.of(
              COL_FLOW,
              historyInfo.getFlow(),
              COL_BASE_TRACES,
              historyInfo.getPaths().get("BASE"),
              COL_DELTA_TRACES,
              historyInfo.getPaths().get("DELTA")));
    }
    return rows;
  }
}
