package org.batfish.question;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NodAnswerElement;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.ReducedReachabilityQuestion;
import org.batfish.main.Batfish;
import org.batfish.z3.BlacklistDstIpQuerySynthesizer;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachableQuerySynthesizer;
import org.batfish.z3.Synthesizer;

public class ReducedReachabilityAnswer extends Answer {

   public ReducedReachabilityAnswer(Batfish batfish,
         ReducedReachabilityQuestion question) {
      batfish.checkDifferentialDataPlaneQuestionDependencies();
      String tag = batfish.getDifferentialFlowTag();

      // load base configurations and generate base data plane
      Map<String, Configuration> baseConfigurations = batfish
            .loadConfigurations(batfish.getBaseEnvSettings());
      File baseDataPlanePath = new File(batfish.getBaseEnvSettings()
            .getDataPlanePath());
      Synthesizer baseDataPlaneSynthesizer = batfish.synthesizeDataPlane(
            baseConfigurations, baseDataPlanePath);

      // load diff configurations and generate diff data plane
      Map<String, Configuration> diffConfigurations = batfish
            .loadConfigurations(batfish.getDiffEnvSettings());
      File diffDataPlanePath = new File(batfish.getDiffEnvSettings()
            .getDataPlanePath());
      Synthesizer diffDataPlaneSynthesizer = batfish.synthesizeDataPlane(
            diffConfigurations, diffDataPlanePath);

      Set<String> commonNodes = new TreeSet<String>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      NodeSet blacklistNodes = batfish.getNodeBlacklist(batfish
            .getDiffEnvSettings());
      Set<NodeInterfacePair> blacklistInterfaces = batfish
            .getInterfaceBlacklist(batfish.getDiffEnvSettings());
      EdgeSet blacklistEdges = batfish.getEdgeBlacklist(batfish
            .getDiffEnvSettings());

      BlacklistDstIpQuerySynthesizer blacklistQuery = new BlacklistDstIpQuerySynthesizer(
            null, blacklistNodes, blacklistInterfaces, blacklistEdges,
            baseConfigurations);

      // compute composite program and flows
      List<Synthesizer> synthesizers = new ArrayList<Synthesizer>();
      synthesizers.add(baseDataPlaneSynthesizer);
      synthesizers.add(diffDataPlaneSynthesizer);
      synthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<CompositeNodJob>();

      // generate base reachability and diff blackhole and blacklist queries
      for (String node : commonNodes) {
         ReachableQuerySynthesizer reachableQuery = new ReachableQuerySynthesizer(
               node, null);
         ReachableQuerySynthesizer blackHoleQuery = new ReachableQuerySynthesizer(
               node, null);
         blackHoleQuery.setNegate(true);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         List<QuerySynthesizer> queries = new ArrayList<QuerySynthesizer>();
         queries.add(reachableQuery);
         queries.add(blackHoleQuery);
         queries.add(blacklistQuery);
         CompositeNodJob job = new CompositeNodJob(synthesizers, queries,
               nodes, tag);
         jobs.add(job);
      }

      // TODO: maybe do something with nod answer element
      Set<Flow> flows = batfish.computeCompositeNodOutput(jobs,
            new NodAnswerElement());

      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
         batfish.getLogger().debug("Found: " + flow.toString() + "\n");
      }
      batfish.dumpTrafficFacts(trafficFactBins, batfish.getBaseEnvSettings());
      batfish.dumpTrafficFacts(trafficFactBins, batfish.getDiffEnvSettings());
      batfish.nlsTraffic();
      AnswerElement answerElement = batfish.getHistory();
      addAnswerElement(answerElement);
   }

}
