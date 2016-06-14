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

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.ReachabilityType;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NodAnswerElement;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.ReachabilityQuestion;
import org.batfish.main.Batfish;
import org.batfish.z3.BlacklistDstIpQuerySynthesizer;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.MultipathInconsistencyQuerySynthesizer;
import org.batfish.z3.NodJob;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachEdgeQuerySynthesizer;
import org.batfish.z3.ReachabilityQuerySynthesizer;
import org.batfish.z3.ReachableQuerySynthesizer;
import org.batfish.z3.Synthesizer;

public class ReachabilityAnswer extends Answer {

   public ReachabilityAnswer(Batfish batfish, ReachabilityQuestion question) {
      ReachabilityType type = question.getReachabilityType();
      switch (type) {
      case MULTIPATH:
         multipath(batfish, question);
         break;

      case PATH_DIFF:
         pathDiff(batfish, question);
         break;

      case REDUCED_REACHABILITY:
         reducedReachability(batfish, question);
         break;

      case STANDARD:
         standard(batfish, question);
         break;

      case INCREASED:
      case MULTIPATH_DIFF:
      default:
         throw new BatfishException("Unsupported reachabilty type: "
               + type.reachabilityTypeName());
      }
   }

   private void multipath(Batfish batfish, ReachabilityQuestion question) {
      batfish.checkDataPlaneQuestionDependencies();
      String tag = batfish.getFlowTag();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      File dataPlanePath = new File(batfish.getEnvSettings().getDataPlanePath());
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = batfish.synthesizeDataPlane(
            configurations, dataPlanePath);
      List<NodJob> jobs = new ArrayList<NodJob>();
      for (String node : configurations.keySet()) {
         MultipathInconsistencyQuerySynthesizer query = new MultipathInconsistencyQuerySynthesizer(
               node);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         NodJob job = new NodJob(dataPlaneSynthesizer, query, nodes, tag);
         jobs.add(job);
      }

      flows = batfish.computeNodOutput(jobs);
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

   private void pathDiff(Batfish batfish, ReachabilityQuestion question) {
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

      // we also need queries for nodes next to edges that are now missing,
      // in
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

      // TODO: maybe do something with nod answer element
      Set<Flow> flows = batfish.computeCompositeNodOutput(jobs,
            new NodAnswerElement());

      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
         batfish.getLogger().output(flow.toString() + "\n");
      }
      batfish.dumpTrafficFacts(trafficFactBins, batfish.getBaseEnvSettings());
      batfish.dumpTrafficFacts(trafficFactBins, batfish.getDiffEnvSettings());
      batfish.nlsTraffic();
      AnswerElement answerElement = batfish.getHistory();
      addAnswerElement(answerElement);
   }

   private void reducedReachability(Batfish batfish,
         ReachabilityQuestion question) {
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

   private void standard(Batfish batfish, ReachabilityQuestion question) {
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
