package org.batfish.answerer;

import java.nio.file.Path;
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
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NodAnswerElement;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.ReachabilityQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.z3.BlacklistDstIpQuerySynthesizer;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.MultipathInconsistencyQuerySynthesizer;
import org.batfish.z3.NodJob;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachEdgeQuerySynthesizer;
import org.batfish.z3.ReachabilityQuerySynthesizer;
import org.batfish.z3.ReachableQuerySynthesizer;
import org.batfish.z3.Synthesizer;

public class ReachabilityAnswerer extends Answerer {

   public ReachabilityAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      ReachabilityQuestion question = (ReachabilityQuestion) _question;
      ReachabilityType type = question.getReachabilityType();
      switch (type) {
      case MULTIPATH:
         return multipath(question,testrigSettings);
      case PATH_DIFF:
         return pathDiff(question,testrigSettings);
      case REDUCED_REACHABILITY:
         return reducedReachability(question,testrigSettings);
      case STANDARD:
         return standard(question,testrigSettings);
      case INCREASED:
      case MULTIPATH_DIFF:
      default:
         throw new BatfishException("Unsupported reachabilty type: "
               + type.reachabilityTypeName());
      }
   }

   private AnswerElement multipath(ReachabilityQuestion question, 
         TestrigSettings testrigSettings) {
      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      String tag = _batfish.getFlowTag(testrigSettings);
      Map<String, Configuration> configurations = _batfish.loadConfigurations(testrigSettings);
      Path dataPlanePath = testrigSettings.getEnvironmentSettings().getDataPlanePath();
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = _batfish.synthesizeDataPlane(
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

      flows = _batfish.computeNodOutput(jobs);
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      _batfish.dumpTrafficFacts(trafficFactBins, testrigSettings);
      _batfish.nlsTraffic(testrigSettings);
      
      AnswerElement answerElement = _batfish.getHistory(testrigSettings);
      return answerElement;
   }

   private AnswerElement pathDiff(ReachabilityQuestion question,
         TestrigSettings testrigSettings) {
      _batfish.checkDifferentialDataPlaneQuestionDependencies();
      String tag = _batfish.getDifferentialFlowTag();

      // load base configurations and generate base data plane
      Map<String, Configuration> baseConfigurations = _batfish
            .loadConfigurations(_batfish.getBaseTestrigSettings());
      Path baseDataPlanePath = _batfish.getBaseTestrigSettings()
            .getEnvironmentSettings().getDataPlanePath();
      Synthesizer baseDataPlaneSynthesizer = _batfish.synthesizeDataPlane(
            baseConfigurations, baseDataPlanePath);

      // load diff configurations and generate diff data plane
      Map<String, Configuration> diffConfigurations = _batfish
            .loadConfigurations(_batfish.getDeltaTestrigSettings());
      Path diffDataPlanePath = _batfish.getDeltaTestrigSettings()
            .getEnvironmentSettings().getDataPlanePath();
      Synthesizer diffDataPlaneSynthesizer = _batfish.synthesizeDataPlane(
            diffConfigurations, diffDataPlanePath);

      Set<String> commonNodes = new TreeSet<String>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      NodeSet blacklistNodes = _batfish.getNodeBlacklist(_batfish
            .getDeltaTestrigSettings());
      Set<NodeInterfacePair> blacklistInterfaces = _batfish
            .getInterfaceBlacklist(_batfish.getDeltaTestrigSettings());
      EdgeSet blacklistEdges = _batfish.getEdgeBlacklist(_batfish
            .getDeltaTestrigSettings());

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
      Topology diffTopology = _batfish.loadTopology(_batfish
            .getDeltaTestrigSettings());
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
      Topology baseTopology = _batfish.loadTopology(_batfish
            .getBaseTestrigSettings());
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
      Set<Flow> flows = _batfish.computeCompositeNodOutput(jobs,
            new NodAnswerElement());

      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
         _logger.output(flow.toString() + "\n");
      }
      _batfish.dumpTrafficFacts(trafficFactBins,
            _batfish.getBaseTestrigSettings());
      _batfish.dumpTrafficFacts(trafficFactBins,
            _batfish.getDeltaTestrigSettings());
      _batfish.nlsTraffic();
      
      AnswerElement answerElement = _batfish.getHistory();
      return answerElement;
   }

   private AnswerElement reducedReachability(ReachabilityQuestion question,
         TestrigSettings testrigSettings) {
      _batfish.checkDifferentialDataPlaneQuestionDependencies();
      String tag = _batfish.getDifferentialFlowTag();

      // load base configurations and generate base data plane
      Map<String, Configuration> baseConfigurations = _batfish
            .loadConfigurations(_batfish.getBaseTestrigSettings());
      Path baseDataPlanePath = _batfish.getBaseTestrigSettings()
            .getEnvironmentSettings().getDataPlanePath();
      Synthesizer baseDataPlaneSynthesizer = _batfish.synthesizeDataPlane(
            baseConfigurations, baseDataPlanePath);

      // load diff configurations and generate diff data plane
      Map<String, Configuration> diffConfigurations = _batfish
            .loadConfigurations(_batfish.getDeltaTestrigSettings());
      Path diffDataPlanePath = _batfish.getDeltaTestrigSettings()
            .getEnvironmentSettings().getDataPlanePath();
      Synthesizer diffDataPlaneSynthesizer = _batfish.synthesizeDataPlane(
            diffConfigurations, diffDataPlanePath);

      Set<String> commonNodes = new TreeSet<String>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      NodeSet blacklistNodes = _batfish.getNodeBlacklist(_batfish
            .getDeltaTestrigSettings());
      Set<NodeInterfacePair> blacklistInterfaces = _batfish
            .getInterfaceBlacklist(_batfish.getDeltaTestrigSettings());
      EdgeSet blacklistEdges = _batfish.getEdgeBlacklist(_batfish
            .getDeltaTestrigSettings());

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
      Set<Flow> flows = _batfish.computeCompositeNodOutput(jobs,
            new NodAnswerElement());

      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
         _logger.debug("Found: " + flow.toString() + "\n");
      }
      _batfish.dumpTrafficFacts(trafficFactBins,
            _batfish.getBaseTestrigSettings());
      _batfish.dumpTrafficFacts(trafficFactBins,
            _batfish.getDeltaTestrigSettings());
      _batfish.nlsTraffic();
      AnswerElement answerElement = _batfish.getHistory();
      return answerElement;
   }

   private AnswerElement standard(ReachabilityQuestion question,
         TestrigSettings testrigSettings) {
      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      String tag = _batfish.getFlowTag(testrigSettings);
      Map<String, Configuration> configurations = _batfish.loadConfigurations(testrigSettings);
      Path dataPlanePath = testrigSettings.getEnvironmentSettings().getDataPlanePath();
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = _batfish.synthesizeDataPlane(
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
      flows = _batfish.computeNodOutput(jobs);

      // dump flows to disk
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      _batfish.dumpTrafficFacts(trafficFactBins, testrigSettings);
      _batfish.nlsTraffic(testrigSettings);
      
      AnswerElement answerElement = _batfish.getHistory(testrigSettings);
      return answerElement;
   }

}
