package org.batfish.common.plugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentRoutingTablesAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.questions.Question;
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

public interface IBatfish extends IPluginConsumer {

  DifferentialReachabilityResult bddDifferentialReachability(
      DifferentialReachabilityParameters parameters);

  /**
   * Given a {@link Set} of {@link Flow}s it populates the {@link List} of {@link Trace}s for them
   *
   * @param flows {@link Set} of {@link Flow}s for which {@link Trace}s are to be found
   * @param ignoreFilters if true, filters/ACLs encountered while building the {@link Flow}s are
   *     ignored
   * @return {@link SortedMap} of {@link Flow} to {@link List} of {@link Trace}s
   */
  SortedMap<Flow, List<Trace>> buildFlows(Set<Flow> flows, boolean ignoreFilters);

  void checkSnapshotOutputReady();

  /** Compute the dataplane for the current {@link NetworkSnapshot} */
  DataPlaneAnswerElement computeDataPlane();

  boolean debugFlagEnabled(String flag);

  ReferenceLibrary getReferenceLibraryData();

  @Nullable
  Answerer createAnswerer(@Nonnull Question question);

  NetworkId getContainerName();

  DataPlanePlugin getDataPlanePlugin();

  String getDifferentialFlowTag();

  Environment getEnvironment();

  String getFlowTag();

  /** Get the configuration of the major issue type {@code majorIssueType} if its present */
  MajorIssueConfig getMajorIssueConfig(String majorIssueType);

  @Nonnull
  NetworkSnapshot getNetworkSnapshot();

  NodeRolesData getNodeRolesData();

  Optional<NodeRoleDimension> getNodeRoleDimension(String roleDimension);

  @Nonnull
  TopologyProvider getTopologyProvider();

  Map<String, String> getQuestionTemplates(boolean verbose);

  /**
   * Get batfish settings
   *
   * @return the {@link ImmutableConfiguration} that represents batfish settings.
   */
  ImmutableConfiguration getSettingsConfiguration();

  String getTaskId();

  SnapshotId getTestrigName();

  InitInfoAnswerElement initInfo(boolean summary, boolean verboseError);

  InitInfoAnswerElement initInfoBgpAdvertisements(boolean summary, boolean verboseError);

  InitInfoAnswerElement initInfoRoutes(boolean summary, boolean verboseError);

  SortedMap<String, Configuration> loadConfigurations();

  /** Returns the configurations for given snapshot. */
  SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot);

  ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse();

  DataPlane loadDataPlane();

  SortedMap<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables();

  SortedMap<String, RoutesByVrf> loadEnvironmentRoutingTables();

  ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement();

  ParseEnvironmentRoutingTablesAnswerElement loadParseEnvironmentRoutingTablesAnswerElement();

  ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement();

  AtomicInteger newBatch(String description, int jobs);

  void popSnapshot();

  Set<BgpAdvertisement> loadExternalBgpAnnouncements(Map<String, Configuration> configurations);

  /** @return a {@link TracerouteEngine} for the current snapshot. */
  TracerouteEngine getTracerouteEngine();

  void pushBaseSnapshot();

  void pushDeltaSnapshot();

  @Nullable
  String readExternalBgpAnnouncementsFile();

  void registerAnswerer(
      String questionName,
      String questionClassName,
      BiFunction<Question, IBatfish, Answerer> answererCreator);

  void registerBgpTablePlugin(BgpTableFormat format, BgpTablePlugin bgpTablePlugin);

  /**
   * Register a new dataplane plugin
   *
   * @param plugin a {@link DataPlanePlugin} capable of computing a dataplane
   * @param name name of the plugin, will be used to register the plugin and prefixed to all
   *     plugin-specific settings (and hence command line arguments)
   */
  void registerDataPlanePlugin(DataPlanePlugin plugin, String name);

  void registerExternalBgpAdvertisementPlugin(
      ExternalBgpAdvertisementPlugin externalBgpAdvertisementPlugin);

  /** Use more explicit {@link #specifierContext(NetworkSnapshot)} if possible. */
  SpecifierContext specifierContext();

  /** Return a {@link SpecifierContext} for a given {@link NetworkSnapshot} */
  SpecifierContext specifierContext(NetworkSnapshot networkSnapshot);

  AnswerElement standard(ReachabilityParameters reachabilityParameters);

  Set<Flow> bddLoopDetection();

  Set<Flow> bddMultipathConsistency(MultipathConsistencyParameters parameters);

  @Nullable
  String loadQuestionSettings(@Nonnull Question question);

  /** Performs bidirectional reachability analysis. */
  @Nonnull
  BidirectionalReachabilityResult bidirectionalReachability(
      BDDPacket bddPacket, ReachabilityParameters parameters);
}
