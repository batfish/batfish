package org.batfish.question;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.LocalPathQuestion;
import org.batfish.main.Batfish;
import org.batfish.z3.BlacklistDstIpQuerySynthesizer;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachEdgeQuerySynthesizer;
import org.batfish.z3.Synthesizer;

public class LocalPathAnswer extends Answer {

   public LocalPathAnswer(Batfish batfish, LocalPathQuestion question) {
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
      List<Synthesizer> commonEdgeSynthesizers = new ArrayList<Synthesizer>();
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(diffDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<CompositeNodJob>();

      // generate local edge reachability and black hole queries
      Topology diffTopology = batfish
            .loadTopology(batfish.getDiffEnvSettings());
      EdgeSet diffEdges = diffTopology.getEdges();
      for (Edge edge : diffEdges) {
         String ingressNode = edge.getNode1();
         ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, true);
         ReachEdgeQuerySynthesizer noReachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, true);
         noReachQuery.setNegate(true);
         List<QuerySynthesizer> queries = new ArrayList<QuerySynthesizer>();
         queries.add(reachQuery);
         queries.add(noReachQuery);
         queries.add(blacklistQuery);
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         CompositeNodJob job = new CompositeNodJob(commonEdgeSynthesizers,
               queries, nodes, tag);
         jobs.add(job);
      }

      // we also need queries for nodes next to edges that are now missing, in
      // the case that those nodes still exist
      List<Synthesizer> missingEdgeSynthesizers = new ArrayList<Synthesizer>();
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      Topology baseTopology = batfish
            .loadTopology(batfish.getBaseEnvSettings());
      EdgeSet baseEdges = baseTopology.getEdges();
      EdgeSet missingEdges = new EdgeSet();
      missingEdges.addAll(baseEdges);
      missingEdges.removeAll(diffEdges);
      for (Edge missingEdge : missingEdges) {
         String ingressNode = missingEdge.getNode1();
         if (diffConfigurations.containsKey(ingressNode)) {
            ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
                  ingressNode, missingEdge, true);
            List<QuerySynthesizer> queries = new ArrayList<QuerySynthesizer>();
            queries.add(reachQuery);
            queries.add(blacklistQuery);
            NodeSet nodes = new NodeSet();
            nodes.add(ingressNode);
            CompositeNodJob job = new CompositeNodJob(missingEdgeSynthesizers,
                  queries, nodes, tag);
            jobs.add(job);
         }

      }

      Set<Flow> flows = batfish.computeCompositeNodOutput(jobs);

      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
         batfish.getLogger().output(flow.toString() + "\n");
      }
      batfish.dumpTrafficFacts(trafficFactBins, batfish.getBaseEnvSettings());
      batfish.dumpTrafficFacts(trafficFactBins, batfish.getDiffEnvSettings());
      batfish.nxtnetTraffic();
      AnswerElement answerElement = batfish.getHistory();
      addAnswerElement(answerElement);
   }

}
