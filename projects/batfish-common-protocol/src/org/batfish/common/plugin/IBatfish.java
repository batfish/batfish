package org.batfish.common.plugin;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.batfish.common.Answerer;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.Question;

public interface IBatfish extends IPluginConsumer {

   AnswerElement answerAclReachability(String aclNameRegexStr,
         NamedStructureEquivalenceSets<?> aclEqSets);

   String answerProtocolDependencies();

   void checkConfigurations();

   void checkDataPlane();

   void checkDataPlaneQuestionDependencies();

   void checkEnvironmentExists();

   Map<Ip, Set<String>> computeIpOwners(
         Map<String, Configuration> configurations);

   Topology computeTopology(Map<String, Configuration> configurations);

   AnswerElement createEnvironment(String environmentName,
         NodeSet nodeBlacklist, Set<NodeInterfacePair> interfaceBlacklist,
         boolean dp);

   Map<String, BiFunction<Question, IBatfish, Answerer>> getAnswererCreators();

   ConvertConfigurationAnswerElement getConvertConfigurationAnswerElement();

   String getDifferentialFlowTag();

   String getFlowTag();

   FlowHistory getHistory();

   void initBgpAdvertisements(Map<String, Configuration> configurations);

   void initBgpOriginationSpaceExplicit(
         Map<String, Configuration> configurations);

   void initRemoteBgpNeighbors(Map<String, Configuration> configurations,
         Map<Ip, Set<String>> ipOwners);

   void initRemoteIpsecVpns(Map<String, Configuration> configurations);

   void initRoutes(Map<String, Configuration> configurations);

   Map<String, Configuration> loadConfigurations();

   AnswerElement multipath(HeaderSpace headerSpace);

   AnswerElement pathDiff(HeaderSpace headerSpace);

   void popEnvironment();

   void processFlows(Set<Flow> flows);

   void pushBaseEnvironment();

   void pushDeltaEnvironment();

   AnswerElement reducedReachability(HeaderSpace headerSpace);

   void registerAnswerer(String questionClassName,
         BiFunction<Question, IBatfish, Answerer> answererCreator);

   AnswerElement standard(HeaderSpace headerSpace,
         Set<ForwardingAction> actions, String ingressNodeRegexStr,
         String notIngressNodeRegexStr, String finalNodeRegexStr,
         String notFinalNodeRegexStr);

}
