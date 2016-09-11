package org.batfish.answerer;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowBuilder;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.TracerouteQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class TracerouteAnswerer extends Answerer {

   public TracerouteAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      String tag = _batfish.getFlowTag(testrigSettings);
      Set<Flow> flows = getFlows(tag);
      _batfish.getDataPlanePlugin().processFlows(flows, testrigSettings);
      AnswerElement answerElement = _batfish.getHistory(testrigSettings);
      return answerElement;
   }

   @Override
   public AnswerElement answerDiff() {
      String tag = _batfish.getDifferentialFlowTag();
      Set<Flow> flows = getFlows(tag);
      _batfish.getDataPlanePlugin().processFlows(flows,
            _batfish.getBaseTestrigSettings());
      _batfish.getDataPlanePlugin().processFlows(flows,
            _batfish.getDeltaTestrigSettings());
      FlowHistory history = _batfish.getHistory();
      FlowHistory filteredHistory = new FlowHistory();
      for (String flowText : history.getFlowsByText().keySet()) {
         // String baseEnvId = _batfish.getBaseTestrigSettings().getName() + ":"
         // + _batfish.getBaseTestrigSettings().getEnvironmentSettings()
         // .getName();
         String baseEnvId = _batfish
               .getFlowTag(_batfish.getBaseTestrigSettings());
         // String deltaEnvId = _batfish.getDeltaTestrigSettings().getName() +
         // ":"
         // + _batfish.getDeltaTestrigSettings().getEnvironmentSettings()
         // .getName();
         String deltaEnvId = _batfish
               .getFlowTag(_batfish.getDeltaTestrigSettings());
         Set<FlowTrace> baseFlowTraces = history.getTraces().get(flowText)
               .get(baseEnvId);
         Set<FlowTrace> deltaFlowTraces = history.getTraces().get(flowText)
               .get(deltaEnvId);
         if (!baseFlowTraces.toString().equals(deltaFlowTraces.toString())) {
            Flow flow = history.getFlowsByText().get(flowText);
            for (FlowTrace flowTrace : baseFlowTraces) {
               filteredHistory.addFlowTrace(flow, baseEnvId, flowTrace);
            }
            for (FlowTrace flowTrace : deltaFlowTraces) {
               filteredHistory.addFlowTrace(flow, deltaEnvId, flowTrace);
            }
         }
      }
      return filteredHistory;
   }

   private Set<Flow> getFlows(String tag) {
      Set<Flow> flows = new TreeSet<>();
      TracerouteQuestion question = (TracerouteQuestion) _question;
      Set<FlowBuilder> flowBuilders = question.getFlowBuilders();
      Map<String, Configuration> configurations = null;
      for (FlowBuilder flowBuilder : flowBuilders) {
         if (flowBuilder.getSrcIp().equals(Ip.AUTO)) {
            if (configurations == null) {
               _batfish.checkConfigurations();
               configurations = _batfish
                     .loadConfigurations(_batfish.getBaseTestrigSettings());
            }
            String hostname = flowBuilder.getIngressNode();
            Configuration node = (hostname == null) ? null
                  : configurations.get(hostname);
            if (node != null) {
               Set<Ip> ips = new TreeSet<>();
               for (Interface i : node.getInterfaces().values()) {
                  ips.addAll(i.getAllPrefixes().stream()
                        .map(prefix -> prefix.getAddress())
                        .collect(Collectors.toSet()));
               }
               if (!ips.isEmpty()) {
                  Ip lowestIp = ips.toArray(new Ip[] {})[0];
                  flowBuilder.setSrcIp(lowestIp);
               }
               else {
                  throw new BatfishException(
                        "Cannot automatically assign source ip to flow since no there are no ip addresses assigned to any interface on ingress node: '"
                              + hostname + "'");
               }
            }
            else {
               throw new BatfishException(
                     "Cannot create flow with non-existent ingress node: '"
                           + hostname + "'");
            }
         }
         flowBuilder.setTag(tag);
         Flow flow = flowBuilder.build();
         flows.add(flow);
      }
      return flows;
   }
}
