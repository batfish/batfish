package org.batfish.question;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.ReachabilityQuestion;
import org.batfish.main.Batfish;
import org.batfish.z3.NodJob;
import org.batfish.z3.ReachabilityQuerySynthesizer;
import org.batfish.z3.Synthesizer;

public class ReachabilityAnswer extends Answer {

   public ReachabilityAnswer(Batfish batfish, ReachabilityQuestion question) {
      batfish.checkDataPlaneQuestionDependencies();
      String tag = batfish.getFlowTag();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      File dataPlanePath = new File(batfish.getEnvSettings().getDataPlanePath());
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = batfish.synthesizeDataPlane(
            configurations, dataPlanePath);

      // collect ingress nodes
      Pattern ingressNodeRegex = Pattern
            .compile(question.getIngressNodeRegex());
      Set<String> activeIngressNodes = new TreeSet<String>();
      if (ingressNodeRegex != null) {
         for (String node : configurations.keySet()) {
            Matcher ingressNodeMatcher = ingressNodeRegex.matcher(node);
            if (ingressNodeMatcher.matches()) {
               activeIngressNodes.add(node);
            }
         }
      }
      else {
         activeIngressNodes.addAll(configurations.keySet());
      }

      // collect final nodes
      Pattern finalNodeRegex = Pattern.compile(question.getFinalNodeRegex());
      Set<String> activeFinalNodes = new TreeSet<String>();
      if (finalNodeRegex != null) {
         for (String node : configurations.keySet()) {
            Matcher finalNodeMatcher = finalNodeRegex.matcher(node);
            if (finalNodeMatcher.matches()) {
               activeFinalNodes.add(node);
            }
         }
      }
      else {
         activeFinalNodes.addAll(configurations.keySet());
      }

      // build query jobs
      List<NodJob> jobs = new ArrayList<NodJob>();
      for (String ingressNode : activeIngressNodes) {
         ReachabilityQuerySynthesizer query = new ReachabilityQuerySynthesizer(
               question.getActions(), question.getDstPrefixes(),
               question.getDstPortRange(), activeFinalNodes,
               Collections.singleton(ingressNode),
               question.getIpProtocolRange(), question.getSrcPrefixes(),
               question.getSrcPortRange(), question.getIcmpType(),
               question.getIcmpCode(), 0 /*
                                          * TODO: allow constraining tcpFlags
                                          * question.getTcpFlags()
                                          */);
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         NodJob job = new NodJob(dataPlaneSynthesizer, query, nodes, tag);
         jobs.add(job);
      }

      // run jobs and get resulting flows
      flows = batfish.computeNodOutput(jobs);

      // dump flows to disk
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      batfish.dumpTrafficFacts(trafficFactBins);
      batfish.nlsTraffic();
      AnswerElement answerElement = batfish.getHistory();
      addAnswerElement(answerElement);
   }

}
