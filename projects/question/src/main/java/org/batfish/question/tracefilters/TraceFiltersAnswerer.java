package org.batfish.question.tracefilters;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class TraceFiltersAnswerer extends Answerer {

  public static final String COLUMN_NODE = "node";
  public static final String COLUMN_FILTER_NAME = "filterName";
  public static final String COLUMN_FLOW = "flow";
  public static final String COLUMN_ACTION = "action";
  public static final String COLUMN_LINE_NUMBER = "lineNumber";
  public static final String COLUMN_LINE_CONTENT = "lineContent";
  public static final String COLUMN_TRACE = "trace";

  public TraceFiltersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates a {@link TableAnswerElement} object the right metadata
   *
   * @param question The question object for which the answer is being created, to borrow its {@link
   *     DisplayHints}
   * @return The creates the answer element object.
   */
  private static TableAnswerElement create(TraceFiltersQuestion question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COLUMN_NODE, Schema.NODE, "Node", true, false),
            new ColumnMetadata(COLUMN_FILTER_NAME, Schema.STRING, "Filter name", true, false),
            new ColumnMetadata(COLUMN_FLOW, Schema.FLOW, "Evaluated flow", true, false),
            new ColumnMetadata(COLUMN_ACTION, Schema.STRING, "Outcome", false, true),
            new ColumnMetadata(COLUMN_LINE_NUMBER, Schema.INTEGER, "Line number", false, true),
            new ColumnMetadata(COLUMN_LINE_CONTENT, Schema.STRING, "Line content", false, true),
            new ColumnMetadata(COLUMN_TRACE, Schema.ACL_TRACE, "ACL trace", false, true));
    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "Filter ${%s} on node ${%s} will ${%s} flow ${%s} at line ${%s} ${%s}",
              COLUMN_FILTER_NAME,
              COLUMN_NODE,
              COLUMN_ACTION,
              COLUMN_FLOW,
              COLUMN_LINE_NUMBER,
              COLUMN_LINE_CONTENT));
    }
    TableMetadata metadata = new TableMetadata(columnMetadata, dhints);
    return new TableAnswerElement(metadata);
  }

  @Override
  public TableAnswerElement answer() {
    TraceFiltersQuestion question = (TraceFiltersQuestion) _question;

    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(_batfish);

    TableAnswerElement answer = create(question);
    for (Configuration c : configurations.values()) {
      if (!includeNodes.contains(c.getHostname())) {
        continue;
      }
      for (IpAccessList filter : c.getIpAccessLists().values()) {
        if (!question.getFilterRegex().matches(filter, c)) {
          continue;
        }
        Flow flow = getFlow(c.getHostname(), question, configurations);
        AclTrace trace =
            AclTracer.trace(
                filter,
                flow,
                question.getIngressInterface(),
                c.getIpAccessLists(),
                c.getIpSpaces());
        FilterResult result =
            filter.filter(
                flow, question.getIngressInterface(), c.getIpAccessLists(), c.getIpSpaces());
        Integer matchLine = result.getMatchLine();
        String lineDesc = "no-match";
        if (matchLine != null) {
          lineDesc = filter.getLines().get(matchLine).getName();
          if (lineDesc == null) {
            lineDesc = "line:" + matchLine;
          }
        }
        Row row =
            getRow(
                c.getHostname(),
                filter.getName(),
                flow,
                result.getAction(),
                matchLine,
                lineDesc,
                trace);

        // exclude or not?
        Exclusion exclusion = Exclusion.covered(row, question.getExclusions());
        if (exclusion != null) {
          answer.addExcludedRow(row, exclusion.getName());
        } else {
          answer.addRow(row);
        }
      }
      // there should be another for loop for v6 filters when we add v6 support
    }
    answer.setSummary(answer.computeSummary(question.getAssertion()));
    return answer;
  }

  private Flow getFlow(
      String ingressNode,
      TraceFiltersQuestion question,
      Map<String, Configuration> configurations) {
    Flow.Builder flowBuilder = question.createBaseFlowBuilder();
    flowBuilder.setTag(_batfish.getFlowTag());
    flowBuilder.setIngressNode(ingressNode);
    if (flowBuilder.getDstIp().equals(Ip.AUTO)) {
      flowBuilder.setDstIp(question.createDstIpFromDst(configurations));
    }
    return flowBuilder.build();
  }

  private static Row getRow(
      String nodeName,
      String filterName,
      Flow flow,
      LineAction action,
      Integer matchLine,
      String lineContent,
      AclTrace trace) {
    RowBuilder row = Row.builder();
    row.put(TraceFiltersAnswerer.COLUMN_NODE, new Node(nodeName))
        .put(TraceFiltersAnswerer.COLUMN_FILTER_NAME, filterName)
        .put(TraceFiltersAnswerer.COLUMN_FLOW, flow)
        .put(TraceFiltersAnswerer.COLUMN_ACTION, action)
        .put(TraceFiltersAnswerer.COLUMN_LINE_NUMBER, matchLine)
        .put(TraceFiltersAnswerer.COLUMN_LINE_CONTENT, lineContent)
        .put(TraceFiltersAnswerer.COLUMN_TRACE, trace);
    return row.build();
  }
}
