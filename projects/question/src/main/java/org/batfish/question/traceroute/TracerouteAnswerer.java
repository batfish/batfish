package org.batfish.question.traceroute;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class TracerouteAnswerer extends Answerer {
  static final String COL_DST_IP = "dstIp";
  static final String COL_FLOW = "flow";
  static final String COL_NODE = "node";
  static final String COL_NUM_PATHS = "numPaths";
  static final String COL_PATHS = "paths";
  static final String COL_RESULTS = "results";

  public TracerouteAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    String tag = _batfish.getFlowTag();
    Set<Flow> flows = getFlows(tag);
    _batfish.processFlows(flows, ((TracerouteQuestion) _question).getIgnoreAcls());
    FlowHistory flowHistory = _batfish.getHistory();
    Multiset<Row> rows = flowHistoryToRows(flowHistory);
    TableAnswerElement table = new TableAnswerElement(createMetadata());
    table.postProcessAnswer(_question, rows);
    return table;
  }

  static TableMetadata createMetadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, Schema.NODE, "Ingress node", true, false),
            new ColumnMetadata(COL_DST_IP, Schema.IP, "Destination IP of the packet", true, false),
            new ColumnMetadata(COL_FLOW, Schema.FLOW, "The flow", true, false),
            new ColumnMetadata(COL_NUM_PATHS, Schema.INTEGER, "The number of paths", false, true),
            new ColumnMetadata(
                COL_RESULTS, Schema.set(Schema.STRING), "The set of outcomes", false, true),
            new ColumnMetadata(COL_PATHS, Schema.set(Schema.FLOW_TRACE), "The paths", false, true));

    DisplayHints dhints = new DisplayHints();
    dhints.setTextDesc(String.format("Paths for flow ${%s}", COL_FLOW));

    return new TableMetadata(columnMetadata, dhints);
  }

  /**
   * Converts {@code FlowHistoryInfo} into {@Row}. Expects that the history object contains traces
   * for only one environment
   */
  static Row flowHistoryToRow(FlowHistoryInfo historyInfo) {
    // there should be only environment in this object
    checkArgument(
        historyInfo.getPaths().size() == 1,
        String.format(
            "Expect only one environment in flow history info. Found %d",
            historyInfo.getPaths().size()));
    Set<FlowTrace> paths = historyInfo.getPaths().values().stream().findAny().get();
    Set<String> results =
        paths.stream().map(path -> path.getDisposition().toString()).collect(Collectors.toSet());
    return Row.of(
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

  /**
   * Converts a flowHistory object into a set of Rows. Expects that the traces correspond to only
   * one environment.
   */
  static Multiset<Row> flowHistoryToRows(FlowHistory flowHistory) {
    Multiset<Row> rows = LinkedHashMultiset.create();
    for (FlowHistoryInfo historyInfo : flowHistory.getTraces().values()) {
      rows.add(flowHistoryToRow(historyInfo));
    }
    return rows;
  }

  private Set<Flow> getFlows(String tag) {
    Set<Flow> flows = new TreeSet<>();
    TracerouteQuestion question = (TracerouteQuestion) _question;
    Set<Flow.Builder> flowBuilders = question.getFlowBuilders();
    Map<String, Configuration> configurations = null;
    for (Flow.Builder flowBuilder : flowBuilders) {
      // TODO: better automatic source ip, considering VRFs and routing
      if (flowBuilder.getSrcIp().equals(Ip.AUTO)) {
        if (configurations == null) {
          _batfish.pushBaseEnvironment();
          configurations = _batfish.loadConfigurations();
          _batfish.popEnvironment();
        }
        String hostname = flowBuilder.getIngressNode();
        Configuration node = Strings.isNullOrEmpty(hostname) ? null : configurations.get(hostname);
        if (node != null) {
          Ip canonicalIp = node.getCanonicalIp();
          if (canonicalIp != null) {
            flowBuilder.setSrcIp(canonicalIp);
          } else {
            throw new BatfishException(
                "Cannot automatically assign source ip to flow since no there are no ip "
                    + "addresses assigned to any interface on ingress node: '"
                    + hostname
                    + "'");
          }
        } else {
          throw new BatfishException(
              "Cannot create flow with non-existent ingress node: '" + hostname + "'");
        }
      }
      if (flowBuilder.getDstIp().equals(Ip.AUTO)) {
        if (configurations == null) {
          _batfish.pushBaseEnvironment();
          configurations = _batfish.loadConfigurations();
          _batfish.popEnvironment();
        }
        flowBuilder.setDstIp(question.createDstIpFromDst(configurations));
      }
      flowBuilder.setTag(tag);
      Flow flow = flowBuilder.build();
      flows.add(flow);
    }
    return flows;
  }
}
