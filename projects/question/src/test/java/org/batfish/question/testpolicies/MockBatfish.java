package org.batfish.question.testpolicies;

import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishLogger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.BgpTablePlugin;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.ExternalBgpAdvertisementPlugin;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentRoutingTablesAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.datamodel.questions.smt.HeaderQuestion;
import org.batfish.datamodel.questions.smt.RoleQuestion;
import org.batfish.grammar.BgpTableFormat;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.bidirectionalreachability.BidirectionalReachabilityResult;
import org.batfish.question.differentialreachability.DifferentialReachabilityParameters;
import org.batfish.question.differentialreachability.DifferentialReachabilityResult;
import org.batfish.question.multipath.MultipathConsistencyParameters;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.SpecifierContext;

final class MockBatfish implements IBatfish {
  private final SortedMap<String, Configuration> _configs;

  MockBatfish(SortedMap<String, Configuration> configs) {
    _configs = ImmutableSortedMap.copyOf(configs);
  }

  @Override
  public DifferentialReachabilityResult bddDifferentialReachability(
      DifferentialReachabilityParameters parameters) {
    return null;
  }

  @Override
  public SortedMap<Flow, List<Trace>> buildFlows(Set<Flow> flows, boolean ignoreFilters) {
    return null;
  }

  @Override
  public void checkSnapshotOutputReady() {}

  @Override
  public DataPlaneAnswerElement computeDataPlane() {
    return null;
  }

  @Deprecated
  @Override
  public DataPlaneAnswerElement computeDataPlane(boolean differentialContext) {
    return null;
  }

  @Override
  public boolean debugFlagEnabled(String flag) {
    return false;
  }

  @Override
  public ReferenceLibrary getReferenceLibraryData() {
    return null;
  }

  @Deprecated
  @Override
  public Map<String, BiFunction<Question, IBatfish, Answerer>> getAnswererCreators() {
    return null;
  }

  @Nullable
  @Override
  public Answerer createAnswerer(@Nonnull Question question) {
    return null;
  }

  @Override
  public NetworkId getContainerName() {
    return null;
  }

  @Override
  public DataPlanePlugin getDataPlanePlugin() {
    return null;
  }

  @Override
  public String getDifferentialFlowTag() {
    return null;
  }

  @Override
  public Environment getEnvironment() {
    return null;
  }

  @Override
  public Topology getEnvironmentTopology() {
    return null;
  }

  @Override
  public String getFlowTag() {
    return null;
  }

  @Override
  public MajorIssueConfig getMajorIssueConfig(String majorIssueType) {
    return null;
  }

  @Nullable
  @Override
  public Layer1Topology getLayer1Topology() {
    return null;
  }

  @Nullable
  @Override
  public Layer2Topology getLayer2Topology() {
    return null;
  }

  @Nonnull
  @Override
  public NetworkSnapshot getNetworkSnapshot() {
    return null;
  }

  @Override
  public NodeRolesData getNodeRolesData() {
    return null;
  }

  @Override
  public Optional<NodeRoleDimension> getNodeRoleDimension(String roleDimension) {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public TopologyProvider getTopologyProvider() {
    return null;
  }

  @Override
  public Map<String, String> getQuestionTemplates(boolean verbose) {
    return null;
  }

  @Override
  public SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes(
      boolean useCompression) {
    return null;
  }

  @Override
  public ImmutableConfiguration getSettingsConfiguration() {
    return null;
  }

  @Override
  public String getTaskId() {
    return null;
  }

  @Override
  public SnapshotId getTestrigName() {
    return null;
  }

  @Override
  public InitInfoAnswerElement initInfo(boolean summary, boolean verboseError) {
    return null;
  }

  @Override
  public InitInfoAnswerElement initInfoBgpAdvertisements(boolean summary, boolean verboseError) {
    return null;
  }

  @Override
  public InitInfoAnswerElement initInfoRoutes(boolean summary, boolean verboseError) {
    return null;
  }

  @Override
  public void initRemoteRipNeighbors(
      Map<String, Configuration> configurations,
      Map<Ip, Set<String>> ipOwners,
      Topology topology) {}

  @Override
  public SortedMap<String, Configuration> loadConfigurations() {
    return _configs;
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
    return null;
  }

  @Override
  public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse() {
    return null;
  }

  @Override
  public DataPlane loadDataPlane() {
    return null;
  }

  @Override
  public SortedMap<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables() {
    return null;
  }

  @Override
  public SortedMap<String, RoutesByVrf> loadEnvironmentRoutingTables() {
    return null;
  }

  @Override
  public ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement() {
    return null;
  }

  @Override
  public ParseEnvironmentRoutingTablesAnswerElement
      loadParseEnvironmentRoutingTablesAnswerElement() {
    return null;
  }

  @Override
  public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement() {
    return null;
  }

  @Override
  public AtomicInteger newBatch(String description, int jobs) {
    return null;
  }

  @Override
  public void popSnapshot() {}

  @Override
  public Set<BgpAdvertisement> loadExternalBgpAnnouncements(
      Map<String, Configuration> configurations) {
    return null;
  }

  @Override
  public TracerouteEngine getTracerouteEngine() {
    return null;
  }

  @Override
  public void pushBaseSnapshot() {}

  @Override
  public void pushDeltaSnapshot() {}

  @Nullable
  @Override
  public String readExternalBgpAnnouncementsFile() {
    return null;
  }

  @Override
  public void registerAnswerer(
      String questionName,
      String questionClassName,
      BiFunction<Question, IBatfish, Answerer> answererCreator) {}

  @Override
  public void registerBgpTablePlugin(BgpTableFormat format, BgpTablePlugin bgpTablePlugin) {}

  @Override
  public void registerDataPlanePlugin(DataPlanePlugin plugin, String name) {}

  @Override
  public void registerExternalBgpAdvertisementPlugin(
      ExternalBgpAdvertisementPlugin externalBgpAdvertisementPlugin) {}

  @Override
  public AnswerElement smtBlackhole(HeaderQuestion q) {
    return null;
  }

  @Override
  public AnswerElement smtBoundedLength(HeaderLocationQuestion q, Integer bound) {
    return null;
  }

  @Override
  public AnswerElement smtDeterminism(HeaderQuestion q) {
    return null;
  }

  @Override
  public AnswerElement smtEqualLength(HeaderLocationQuestion q) {
    return null;
  }

  @Override
  public AnswerElement smtForwarding(HeaderQuestion q) {
    return null;
  }

  @Override
  public AnswerElement smtLoadBalance(HeaderLocationQuestion q, int threshold) {
    return null;
  }

  @Override
  public AnswerElement smtLocalConsistency(Pattern routerRegex, boolean strict, boolean fullModel) {
    return null;
  }

  @Override
  public AnswerElement smtMultipathConsistency(HeaderLocationQuestion q) {
    return null;
  }

  @Override
  public AnswerElement smtReachability(HeaderLocationQuestion q) {
    return null;
  }

  @Override
  public AnswerElement smtRoles(RoleQuestion q) {
    return null;
  }

  @Override
  public AnswerElement smtRoutingLoop(HeaderQuestion q) {
    return null;
  }

  @Override
  public SpecifierContext specifierContext() {
    return null;
  }

  @Override
  public SpecifierContext specifierContext(NetworkSnapshot networkSnapshot) {
    return null;
  }

  @Override
  public AnswerElement standard(ReachabilityParameters reachabilityParameters) {
    return null;
  }

  @Override
  public Set<Flow> bddLoopDetection() {
    return null;
  }

  @Override
  public Set<Flow> bddMultipathConsistency(MultipathConsistencyParameters parameters) {
    return null;
  }

  @Nullable
  @Override
  public String loadQuestionSettings(@Nonnull Question question) {
    return null;
  }

  @Nullable
  @Override
  public Layer1Topology loadRawLayer1PhysicalTopology(@Nonnull NetworkSnapshot networkSnapshot) {
    return null;
  }

  @Nonnull
  @Override
  public SortedSet<Edge> getEdgeBlacklist(@Nonnull NetworkSnapshot networkSnapshot) {
    return null;
  }

  @Nonnull
  @Override
  public SortedSet<NodeInterfacePair> getInterfaceBlacklist(
      @Nonnull NetworkSnapshot networkSnapshot) {
    return null;
  }

  @Nonnull
  @Override
  public SortedSet<String> getNodeBlacklist(@Nonnull NetworkSnapshot networkSnapshot) {
    return null;
  }

  @Nonnull
  @Override
  public BidirectionalReachabilityResult bidirectionalReachability(
      BDDPacket bddPacket, ReachabilityParameters parameters) {
    return null;
  }

  @Override
  public BatfishLogger getLogger() {
    return null;
  }
}
