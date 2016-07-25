package org.batfish.answerer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowBuilder;
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
      TracerouteQuestion question = (TracerouteQuestion) _question;
      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      Set<FlowBuilder> flowBuilders = question.getFlowBuilders();
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      String tag = _batfish.getFlowTag(testrigSettings);
      Map<String, Configuration> configurations = null;
      for (FlowBuilder flowBuilder : flowBuilders) {
         if (flowBuilder.getSrcIp().equals(Ip.AUTO)) {
            if (configurations == null) {
               _batfish.checkConfigurations();
               configurations = _batfish.loadConfigurations(testrigSettings);
            }
            String hostname = flowBuilder.getIngressNode();
            Configuration node = (hostname == null) ? null : configurations
                  .get(hostname);
            if (node != null) {
               Set<Ip> ips = new TreeSet<Ip>();
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
         wSetFlowOriginate.append(flow.toLBLine());
      }
      _batfish.dumpTrafficFacts(trafficFactBins);
      _batfish.nlsTraffic(testrigSettings);
      AnswerElement answerElement = _batfish.getHistory(testrigSettings);
      return answerElement;
   }
}
