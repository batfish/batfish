package org.batfish.question.tracefilters;

import static org.batfish.question.tracefilters.TraceFiltersAnswerElement.createRow;

import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;

public class TraceFiltersAnswerer extends Answerer {

  public TraceFiltersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TraceFiltersAnswerElement answer() {
    TraceFiltersQuestion question = (TraceFiltersQuestion) _question;

    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(_batfish);

    TraceFiltersAnswerElement answer = TraceFiltersAnswerElement.create(question);
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
        FilterResult result = trace.computeFilterResult();
        Integer matchLine = result.getMatchLine();
        String lineDesc = "no-match";
        if (matchLine != null) {
          lineDesc = filter.getLines().get(matchLine).getName();
          if (lineDesc == null) {
            lineDesc = "line:" + matchLine;
          }
        }
        Row row =
            createRow(
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
}
