package org.batfish.question.testfilters;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
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
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.SpecifierContext;

public class TestFiltersAnswerer extends Answerer {

  static int DEFAULT_DST_PORT = 80; // HTTP
  static IpProtocol DEFAULT_IP_PROTOCOL = IpProtocol.TCP;
  static int DEFAULT_SRC_PORT = 49152; // lowest ephemeral port

  public static final String COL_NODE = "Node";
  public static final String COL_FILTER_NAME = "Filter_Name";
  public static final String COL_FLOW = "Flow";
  public static final String COL_ACTION = "Action";
  public static final String COL_LINE_NUMBER = "Line_Number";
  public static final String COL_LINE_CONTENT = "Line_Content";
  public static final String COL_TRACE = "Trace";

  public TestFiltersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates a {@link TableAnswerElement} object containing the right metadata
   *
   * @param question The question object for which the answer is being created, to borrow its {@link
   *     DisplayHints}
   * @return The creates the answer element object.
   */
  public static TableAnswerElement create(TestFiltersQuestion question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false),
            new ColumnMetadata(COL_FILTER_NAME, Schema.STRING, "Filter name", true, false),
            new ColumnMetadata(COL_FLOW, Schema.FLOW, "Evaluated flow", true, false),
            new ColumnMetadata(COL_ACTION, Schema.STRING, "Outcome", false, true),
            new ColumnMetadata(COL_LINE_NUMBER, Schema.INTEGER, "Line number", false, true),
            new ColumnMetadata(COL_LINE_CONTENT, Schema.STRING, "Line content", false, true),
            new ColumnMetadata(COL_TRACE, Schema.ACL_TRACE, "ACL trace", false, true));
    String textDesc =
        String.format(
            "Filter ${%s} on node ${%s} will ${%s} flow ${%s} at line ${%s} ${%s}",
            COL_FILTER_NAME, COL_NODE, COL_ACTION, COL_FLOW, COL_LINE_NUMBER, COL_LINE_CONTENT);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    TableMetadata metadata = new TableMetadata(columnMetadata, textDesc);
    return new TableAnswerElement(metadata);
  }

  @Override
  public TableAnswerElement answer() {
    TestFiltersQuestion question = (TestFiltersQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes = question.getNodes().getMatchingNodes(_batfish);
    SpecifierContext specifierContext = _batfish.specifierContext();

    Multiset<Row> rows = rawAnswer(configurations, question, includeNodes, specifierContext);

    TableAnswerElement answer = create(question);
    answer.postProcessAnswer(question, rows);
    return answer;
  }

  private static Flow getFlow(
      String ingressNode, TestFiltersQuestion question, Map<String, Configuration> configurations) {
    Flow.Builder flowBuilder = question.createBaseFlowBuilder();
    flowBuilder.setTag("FlowTag"); // dummy tag; consistent tags enable flow diffs
    flowBuilder.setIngressNode(ingressNode);
    if (flowBuilder.getDstIp().equals(Ip.AUTO)) {
      flowBuilder.setDstIp(question.createDstIpFromDst(configurations));
    }
    applyDefaults(flowBuilder, question);
    return flowBuilder.build();
  }

  /** Applies reasonable default values for fields when left unspecified */
  @VisibleForTesting
  static void applyDefaults(Flow.Builder flowBuilder, TestFiltersQuestion question) {
    if (question.getIpProtocol() == null) {
      flowBuilder.setIpProtocol(DEFAULT_IP_PROTOCOL);
    }
    if (flowBuilder.getIpProtocol() == IpProtocol.TCP
        || flowBuilder.getIpProtocol() == IpProtocol.UDP) {
      if (question.getDstPort() == null) {
        flowBuilder.setSrcPort(DEFAULT_DST_PORT);
      }
      if (question.getSrcPort() == null) {
        flowBuilder.setSrcPort(DEFAULT_SRC_PORT);
      }
    }
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
    row.put(TestFiltersAnswerer.COL_NODE, new Node(nodeName))
        .put(TestFiltersAnswerer.COL_FILTER_NAME, filterName)
        .put(TestFiltersAnswerer.COL_FLOW, flow)
        .put(TestFiltersAnswerer.COL_ACTION, action)
        .put(TestFiltersAnswerer.COL_LINE_NUMBER, matchLine)
        .put(TestFiltersAnswerer.COL_LINE_CONTENT, lineContent)
        .put(TestFiltersAnswerer.COL_TRACE, trace);
    return row.build();
  }

  Multiset<Row> rawAnswer(
      Map<String, Configuration> configurations,
      TestFiltersQuestion question,
      Set<String> includeNodes,
      SpecifierContext specifierContext) {

    FilterSpecifier filterSpecifier = question.getFilterSpecifier();

    Multiset<Row> rows = HashMultiset.create();

    for (Configuration c : configurations.values()) {
      if (!includeNodes.contains(c.getHostname())) {
        continue;
      }
      for (IpAccessList filter : filterSpecifier.resolve(c.getHostname(), specifierContext)) {

        Flow flow = getFlow(c.getHostname(), question, configurations);
        AclTrace trace =
            AclTracer.trace(
                filter,
                flow,
                question.getIngressInterface(),
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());
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
        rows.add(
            getRow(
                c.getHostname(),
                filter.getName(),
                flow,
                result.getAction(),
                matchLine,
                lineDesc,
                trace));
      }
      // there should be another for loop for v6 filters when we add v6 support
    }
    return rows;
  }
}
