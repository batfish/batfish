package org.batfish.common.plugin;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.batfish.common.Answerer;
import org.batfish.common.Directory;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentRoutingTablesAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.assertion.AssertionAst;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.datamodel.questions.Question;
import org.batfish.grammar.BgpTableFormat;
import org.batfish.grammar.GrammarSettings;

public interface IBatfish extends IPluginConsumer {

   AnswerElement answerAclReachability(
         String aclNameRegexStr,
         NamedStructureEquivalenceSets<?> aclEqSets);

   void checkConfigurations();

   void checkDataPlane();

   void checkEnvironmentExists();

   InterfaceSet computeFlowSinks(
         Map<String, Configuration> configurations,
         boolean differentialContext, Topology topology);

   Map<Ip, Set<String>> computeIpOwners(
         Map<String, Configuration> configurations, boolean excludeInactive);

   Map<Ip, String> computeIpOwnersSimple(Map<Ip, Set<String>> ipOwners);

   Topology computeTopology(Map<String, Configuration> configurations);

   AnswerElement createEnvironment(
         String environmentName,
         SortedSet<String> nodeBlacklist,
         SortedSet<NodeInterfacePair> interfaceBlacklist,
         SortedSet<Edge> edgeBlacklist, boolean dp);

   Map<String, BiFunction<Question, IBatfish, Answerer>> getAnswererCreators();

   String getDifferentialFlowTag();

   String getFlowTag();

   GrammarSettings getGrammarSettings();

   FlowHistory getHistory();

   SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes();

   Directory getTestrigFileTree();

   void initBgpAdvertisements(Map<String, Configuration> configurations);

   void initBgpOriginationSpaceExplicit(
         Map<String, Configuration> configurations);

   InitInfoAnswerElement initInfo(boolean summary, boolean verboseError, boolean environmentRoutes);

   void initRemoteBgpNeighbors(
         Map<String, Configuration> configurations,
         Map<Ip, Set<String>> ipOwners);

   void initRemoteIpsecVpns(Map<String, Configuration> configurations);

   void initRemoteOspfNeighbors(
         Map<String, Configuration> configurations,
         Map<Ip, Set<String>> ipOwners, Topology topology);

   SortedMap<String, Configuration> loadConfigurations();

   ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement();

   DataPlane loadDataPlane();

   SortedMap<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables();

   SortedMap<String, RoutesByVrf> loadEnvironmentRoutingTables();

   ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement();

   ParseEnvironmentRoutingTablesAnswerElement loadParseEnvironmentRoutingTablesAnswerElement();

   ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement();

   AnswerElement multipath(HeaderSpace headerSpace);

   AtomicInteger newBatch(String description, int jobs);

   AssertionAst parseAssertion(String text);

   AnswerElement pathDiff(HeaderSpace headerSpace);

   void popEnvironment();

   void printElapsedTime();

   AdvertisementSet processExternalBgpAnnouncements(
         Map<String, Configuration> configurations);

   void processFlows(Set<Flow> flows);

   void pushBaseEnvironment();

   void pushDeltaEnvironment();

   String readExternalBgpAnnouncementsFile();

   AnswerElement reducedReachability(HeaderSpace headerSpace);

   void registerAnswerer(
         String questionName, String questionClassName,
         BiFunction<Question, IBatfish, Answerer> answererCreator);

   void registerBgpTablePlugin(
         BgpTableFormat format,
         BgpTablePlugin bgpTablePlugin);

   void registerExternalBgpAdvertisementPlugin(
         ExternalBgpAdvertisementPlugin externalBgpAdvertisementPlugin);

   void resetTimer();

   void setDataPlanePlugin(DataPlanePlugin dataPlanePlugin);

   AnswerElement standard(
         HeaderSpace headerSpace,
         Set<ForwardingAction> actions, String ingressNodeRegexStr,
         String notIngressNodeRegexStr, String finalNodeRegexStr,
         String notFinalNodeRegexStr);

   void writeDataPlane(DataPlane dp, DataPlaneAnswerElement ae);

}
