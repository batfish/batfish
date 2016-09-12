package org.batfish.answerer;

import java.util.ArrayList;
import java.util.Collections;
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
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.ReachabilityType;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NodAnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.ReachabilityQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.z3.BlacklistDstIpQuerySynthesizer;
import org.batfish.z3.CompositeNodJob;
import org.batfish.z3.MultipathInconsistencyQuerySynthesizer;
import org.batfish.z3.NodJob;
import org.batfish.z3.QuerySynthesizer;
import org.batfish.z3.ReachEdgeQuerySynthesizer;
import org.batfish.z3.ReachabilityQuerySynthesizer;
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
         return multipath(question, testrigSettings);
      case STANDARD:
         return standard(question, testrigSettings);
      case PATH_DIFF:
      case REDUCED_REACHABILITY:
      case INCREASED:
      case MULTIPATH_DIFF:
      default:
         throw new BatfishException(
               "Unsupported non-differential reachabilty type: "
                     + type.reachabilityTypeName());
      }
   }

   @Override
   public AnswerElement answerDiff() {
      ReachabilityQuestion question = (ReachabilityQuestion) _question;
      ReachabilityType type = question.getReachabilityType();
      switch (type) {
      case PATH_DIFF:
         return pathDiff(question);
      case REDUCED_REACHABILITY:
         return reducedReachability(question);
      case STANDARD:
      case MULTIPATH:
      case INCREASED:
      case MULTIPATH_DIFF:
      default:
         throw new BatfishException(
               "Unsupported differential reachabilty type: "
                     + type.reachabilityTypeName());
      }
   }

   private AnswerElement multipath(ReachabilityQuestion question,
         TestrigSettings testrigSettings) {
      Settings settings = _batfish.getSettings();
      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      String tag = _batfish.getFlowTag(testrigSettings);
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = _batfish
            .synthesizeDataPlane(testrigSettings);
      List<NodJob> jobs = new ArrayList<>();
      for (String node : configurations.keySet()) {
         MultipathInconsistencyQuerySynthesizer query = new MultipathInconsistencyQuerySynthesizer(
               node, question.getHeaderSpace());
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         NodJob job = new NodJob(settings, dataPlaneSynthesizer, query, nodes,
               tag);
         jobs.add(job);
      }

      flows = _batfish.computeNodOutput(jobs);

      _batfish.getDataPlanePlugin().processFlows(flows, testrigSettings);

      AnswerElement answerElement = _batfish.getHistory(testrigSettings);
      return answerElement;
   }

   private AnswerElement pathDiff(ReachabilityQuestion question) {
      Settings settings = _batfish.getSettings();
      _batfish.checkDifferentialDataPlaneQuestionDependencies();
      String tag = _batfish.getDifferentialFlowTag();

      // load base configurations and generate base data plane
      Map<String, Configuration> baseConfigurations = _batfish
            .loadConfigurations(_batfish.getBaseTestrigSettings());
      Synthesizer baseDataPlaneSynthesizer = _batfish
            .synthesizeDataPlane(_batfish.getBaseTestrigSettings());

      // load diff configurations and generate diff data plane
      Map<String, Configuration> diffConfigurations = _batfish
            .loadConfigurations(_batfish.getDeltaTestrigSettings());
      Synthesizer diffDataPlaneSynthesizer = _batfish
            .synthesizeDataPlane(_batfish.getDeltaTestrigSettings());

      Set<String> commonNodes = new TreeSet<>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      NodeSet blacklistNodes = _batfish
            .getNodeBlacklist(_batfish.getDeltaTestrigSettings());
      Set<NodeInterfacePair> blacklistInterfaces = _batfish
            .getInterfaceBlacklist(_batfish.getDeltaTestrigSettings());
      EdgeSet blacklistEdges = _batfish
            .getEdgeBlacklist(_batfish.getDeltaTestrigSettings());

      BlacklistDstIpQuerySynthesizer blacklistQuery = new BlacklistDstIpQuerySynthesizer(
            null, blacklistNodes, blacklistInterfaces, blacklistEdges,
            baseConfigurations);

      // compute composite program and flows
      List<Synthesizer> commonEdgeSynthesizers = new ArrayList<>();
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(diffDataPlaneSynthesizer);
      commonEdgeSynthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<>();

      // generate local edge reachability and black hole queries
      Topology diffTopology = _batfish
            .loadTopology(_batfish.getDeltaTestrigSettings());
      EdgeSet diffEdges = diffTopology.getEdges();
      for (Edge edge : diffEdges) {
         String ingressNode = edge.getNode1();
         ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, true, question.getHeaderSpace());
         ReachEdgeQuerySynthesizer noReachQuery = new ReachEdgeQuerySynthesizer(
               ingressNode, edge, true, new HeaderSpace());
         noReachQuery.setNegate(true);
         List<QuerySynthesizer> queries = new ArrayList<>();
         queries.add(reachQuery);
         queries.add(noReachQuery);
         queries.add(blacklistQuery);
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         CompositeNodJob job = new CompositeNodJob(settings,
               commonEdgeSynthesizers, queries, nodes, tag);
         jobs.add(job);
      }

      // we also need queries for nodes next to edges that are now missing,
      // in the case that those nodes still exist
      List<Synthesizer> missingEdgeSynthesizers = new ArrayList<>();
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      missingEdgeSynthesizers.add(baseDataPlaneSynthesizer);
      Topology baseTopology = _batfish
            .loadTopology(_batfish.getBaseTestrigSettings());
      EdgeSet baseEdges = baseTopology.getEdges();
      EdgeSet missingEdges = new EdgeSet();
      missingEdges.addAll(baseEdges);
      missingEdges.removeAll(diffEdges);
      for (Edge missingEdge : missingEdges) {
         String ingressNode = missingEdge.getNode1();
         if (diffConfigurations.containsKey(ingressNode)) {
            ReachEdgeQuerySynthesizer reachQuery = new ReachEdgeQuerySynthesizer(
                  ingressNode, missingEdge, true, question.getHeaderSpace());
            List<QuerySynthesizer> queries = new ArrayList<>();
            queries.add(reachQuery);
            queries.add(blacklistQuery);
            NodeSet nodes = new NodeSet();
            nodes.add(ingressNode);
            CompositeNodJob job = new CompositeNodJob(settings,
                  missingEdgeSynthesizers, queries, nodes, tag);
            jobs.add(job);
         }

      }

      // TODO: maybe do something with nod answer element
      Set<Flow> flows = _batfish.computeCompositeNodOutput(jobs,
            new NodAnswerElement());
      _batfish.getDataPlanePlugin().processFlows(flows,
            _batfish.getBaseTestrigSettings());
      _batfish.getDataPlanePlugin().processFlows(flows,
            _batfish.getDeltaTestrigSettings());

      AnswerElement answerElement = _batfish.getHistory();
      return answerElement;
   }

   private AnswerElement reducedReachability(ReachabilityQuestion question) {
      Settings settings = _batfish.getSettings();
      _batfish.checkDifferentialDataPlaneQuestionDependencies();
      String tag = _batfish.getDifferentialFlowTag();

      // load base configurations and generate base data plane
      Map<String, Configuration> baseConfigurations = _batfish
            .loadConfigurations(_batfish.getBaseTestrigSettings());
      Synthesizer baseDataPlaneSynthesizer = _batfish
            .synthesizeDataPlane(_batfish.getBaseTestrigSettings());

      // load diff configurations and generate diff data plane
      Map<String, Configuration> diffConfigurations = _batfish
            .loadConfigurations(_batfish.getDeltaTestrigSettings());
      Synthesizer diffDataPlaneSynthesizer = _batfish
            .synthesizeDataPlane(_batfish.getDeltaTestrigSettings());

      Set<String> commonNodes = new TreeSet<>();
      commonNodes.addAll(baseConfigurations.keySet());
      commonNodes.retainAll(diffConfigurations.keySet());

      NodeSet blacklistNodes = _batfish
            .getNodeBlacklist(_batfish.getDeltaTestrigSettings());
      Set<NodeInterfacePair> blacklistInterfaces = _batfish
            .getInterfaceBlacklist(_batfish.getDeltaTestrigSettings());
      EdgeSet blacklistEdges = _batfish
            .getEdgeBlacklist(_batfish.getDeltaTestrigSettings());

      BlacklistDstIpQuerySynthesizer blacklistQuery = new BlacklistDstIpQuerySynthesizer(
            null, blacklistNodes, blacklistInterfaces, blacklistEdges,
            baseConfigurations);

      // compute composite program and flows
      List<Synthesizer> synthesizers = new ArrayList<>();
      synthesizers.add(baseDataPlaneSynthesizer);
      synthesizers.add(diffDataPlaneSynthesizer);
      synthesizers.add(baseDataPlaneSynthesizer);

      List<CompositeNodJob> jobs = new ArrayList<>();

      // generate base reachability and diff blackhole and blacklist queries
      for (String node : commonNodes) {
         ReachabilityQuerySynthesizer acceptQuery = new ReachabilityQuerySynthesizer(
               Collections.singleton(ForwardingAction.ACCEPT),
               question.getHeaderSpace(), Collections.<String> emptySet(),
               Collections.singleton(node));
         ReachabilityQuerySynthesizer notAcceptQuery = new ReachabilityQuerySynthesizer(
               Collections.singleton(ForwardingAction.ACCEPT),
               new HeaderSpace(), Collections.<String> emptySet(),
               Collections.singleton(node));
         notAcceptQuery.setNegate(true);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         List<QuerySynthesizer> queries = new ArrayList<>();
         queries.add(acceptQuery);
         queries.add(notAcceptQuery);
         queries.add(blacklistQuery);
         CompositeNodJob job = new CompositeNodJob(settings, synthesizers,
               queries, nodes, tag);
         jobs.add(job);
      }

      // TODO: maybe do something with nod answer element
      Set<Flow> flows = _batfish.computeCompositeNodOutput(jobs,
            new NodAnswerElement());
      _batfish.getDataPlanePlugin().processFlows(flows,
            _batfish.getBaseTestrigSettings());
      _batfish.getDataPlanePlugin().processFlows(flows,
            _batfish.getDeltaTestrigSettings());

      AnswerElement answerElement = _batfish.getHistory();
      return answerElement;
   }

   private AnswerElement standard(ReachabilityQuestion question,
         TestrigSettings testrigSettings) {
      Settings settings = _batfish.getSettings();
      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      String tag = _batfish.getFlowTag(testrigSettings);
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = _batfish
            .synthesizeDataPlane(testrigSettings);

      // collect ingress nodes
      Pattern ingressNodeRegex = Pattern
            .compile(question.getIngressNodeRegex());
      Pattern notIngressNodeRegex = Pattern
            .compile(question.getNotIngressNodeRegex());
      Set<String> activeIngressNodes = new TreeSet<>();
      for (String node : configurations.keySet()) {
         Matcher ingressNodeMatcher = ingressNodeRegex.matcher(node);
         Matcher notIngressNodeMatcher = notIngressNodeRegex.matcher(node);
         if (ingressNodeMatcher.matches() && !notIngressNodeMatcher.matches()) {
            activeIngressNodes.add(node);
         }
      }
      if (activeIngressNodes.isEmpty()) {
         return new StringAnswerElement(
               "NOTHING TO DO: No nodes both match ingressNodeRegex: '"
                     + question.getIngressNodeRegex()
                     + "' and fail to match notIngressNodeRegex: '"
                     + question.getNotIngressNodeRegex() + "'");
      }

      // collect final nodes
      Pattern finalNodeRegex = Pattern.compile(question.getFinalNodeRegex());
      Pattern notFinalNodeRegex = Pattern
            .compile(question.getNotFinalNodeRegex());
      Set<String> activeFinalNodes = new TreeSet<>();
      for (String node : configurations.keySet()) {
         Matcher finalNodeMatcher = finalNodeRegex.matcher(node);
         Matcher notFinalNodeMatcher = notFinalNodeRegex.matcher(node);
         if (finalNodeMatcher.matches() && !notFinalNodeMatcher.matches()) {
            activeFinalNodes.add(node);
         }
      }
      if (activeFinalNodes.isEmpty()) {
         return new StringAnswerElement(
               "NOTHING TO DO: No nodes both match finalNodeRegex: '"
                     + question.getFinalNodeRegex()
                     + "' and fail to match notFinalNodeRegex: '"
                     + question.getNotFinalNodeRegex() + "'");
      }

      // build query jobs
      List<NodJob> jobs = new ArrayList<>();
      for (String ingressNode : activeIngressNodes) {
         ReachabilityQuerySynthesizer query = new ReachabilityQuerySynthesizer(
               question.getActions(), question.getHeaderSpace(),
               activeFinalNodes, Collections.singleton(ingressNode));
         NodeSet nodes = new NodeSet();
         nodes.add(ingressNode);
         NodJob job = new NodJob(settings, dataPlaneSynthesizer, query, nodes,
               tag);
         jobs.add(job);
      }

      // run jobs and get resulting flows
      flows = _batfish.computeNodOutput(jobs);

      _batfish.getDataPlanePlugin().processFlows(flows, testrigSettings);

      AnswerElement answerElement = _batfish.getHistory(testrigSettings);
      return answerElement;
   }
}
