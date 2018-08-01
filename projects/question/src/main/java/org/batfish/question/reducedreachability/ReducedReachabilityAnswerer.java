package org.batfish.question.reducedreachability;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** An {@link Answerer} for {@link ReducedReachabilityQuestion}. */
public class ReducedReachabilityAnswerer extends Answerer {
  static final String COL_ENVIRONMENT = "environment";
  static final String COL_DST_IP = "dstIp";
  static final String COL_FLOW = "flow";
  static final String COL_NODE = "node";
  static final String COL_NUM_PATHS = "numPaths";
  static final String COL_PATHS = "paths";
  static final String COL_RESULTS = "results";

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
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_ENVIRONMENT, Schema.STRING, "Environment", true, false),
            new ColumnMetadata(COL_NODE, Schema.NODE, "Ingress node", true, false),
            new ColumnMetadata(COL_DST_IP, Schema.IP, "Destination IP of the packet", true, false),
            new ColumnMetadata(COL_FLOW, Schema.FLOW, "The flow", true, false),
            new ColumnMetadata(COL_NUM_PATHS, Schema.INTEGER, "The number of paths", false, true),
            new ColumnMetadata(
                COL_RESULTS, Schema.set(Schema.STRING), "The set of outcomes", false, true),
            new ColumnMetadata(COL_PATHS, Schema.set(Schema.FLOW_TRACE), "The paths", false, true));

    return new TableMetadata(columnMetadata, String.format("Paths for flow ${%s}", COL_FLOW));
  }

  /**
   * Converts a flowHistory object into a set of Rows. Expects that the traces correspond to only
   * one environment.
   */
  private static Multiset<Row> flowHistoryToRows(FlowHistory flowHistory) {
    Multiset<Row> rows = LinkedHashMultiset.create();
    for (FlowHistoryInfo historyInfo : flowHistory.getTraces().values()) {
      for (String environment : historyInfo.getEnvironments().keySet()) {
        rows.add(flowHistoryToRow(environment, historyInfo));
      }
    }
    return rows;
  }

  /**
   * Converts {@code FlowHistoryInfo} into {@link Row}. Expects that the history object contains
   * traces for only one environment
   */
  private static Row flowHistoryToRow(String environment, FlowHistoryInfo historyInfo) {
    // there should be only environment in this object
    checkArgument(
        historyInfo.getPaths().containsKey(environment),
        String.format("Environments %s not in flow history info.", environment));
    Set<FlowTrace> paths = historyInfo.getPaths().get(environment);
    Set<String> results =
        paths.stream().map(path -> path.getDisposition().toString()).collect(Collectors.toSet());
    return Row.of(
        COL_ENVIRONMENT,
        environment,
        COL_NODE,
        new Node(historyInfo.getFlow().getIngressNode()),
        COL_DST_IP,
        historyInfo.getFlow().getDstIp(),
        COL_FLOW,
        historyInfo.getFlow(),
        COL_NUM_PATHS,
        paths.size(),
        COL_RESULTS,
        results,
        COL_PATHS,
        paths);
  }
}
