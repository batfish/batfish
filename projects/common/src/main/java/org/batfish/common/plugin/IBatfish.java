package org.batfish.common.plugin;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.flow.Trace;
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
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.batfish.specifier.SpecifierContext;
import org.batfish.vendor.VendorConfiguration;

public interface IBatfish extends IPluginConsumer {

  DifferentialReachabilityResult bddDifferentialReachability(
      NetworkSnapshot snapshot,
      NetworkSnapshot reference,
      DifferentialReachabilityParameters parameters);

  /**
   * Given a {@link Set} of {@link Flow}s it populates the {@link List} of {@link Trace}s for them
   *
   * @param flows {@link Set} of {@link Flow}s for which {@link Trace}s are to be found
   * @param ignoreFilters if true, filters/ACLs encountered while building the {@link Flow}s are
   *     ignored
   * @return {@link SortedMap} of {@link Flow} to {@link List} of {@link Trace}s
   */
  SortedMap<Flow, List<Trace>> buildFlows(
      NetworkSnapshot snapshot, Set<Flow> flows, boolean ignoreFilters);

  /** Compute the dataplane for the given {@link NetworkSnapshot} */
  DataPlaneAnswerElement computeDataPlane(NetworkSnapshot snapshot);

  boolean debugFlagEnabled(String flag);

  /** Return the {@link LocationInfo} of each {@link Location} in the {@link NetworkSnapshot}. */
  Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot snapshot);

  ReferenceLibrary getReferenceLibraryData();

  @Nullable
  Answerer createAnswerer(@Nonnull Question question);

  NetworkId getContainerName();

  DataPlanePlugin getDataPlanePlugin();

  @Nonnull
  NetworkSnapshot getSnapshot();

  @Nonnull
  NetworkSnapshot getReferenceSnapshot();

  NodeRolesData getNodeRolesData();

  Optional<NodeRoleDimension> getNodeRoleDimension(String roleDimension);

  @Nonnull
  TopologyProvider getTopologyProvider();

  /**
   * Get batfish settings
   *
   * @return the {@link ImmutableConfiguration} that represents batfish settings.
   */
  ImmutableConfiguration getSettingsConfiguration();

  /**
   * Get a network extended object for the given key
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @MustBeClosed
  @Nonnull
  InputStream getNetworkObject(NetworkId networkId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Get a snapshot extended object for the given key
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @MustBeClosed
  @Nonnull
  InputStream getSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Put a snapshot extended object for the given key
   *
   * @throws IOException if there is an error writing the object
   */
  void putSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key, InputStream stream)
      throws IOException;

  /**
   * Get a snapshot input object for the given key
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @MustBeClosed
  @Nonnull
  InputStream getSnapshotInputObject(NetworkSnapshot snapshot, String key)
      throws FileNotFoundException, IOException;

  String getTaskId();

  InitInfoAnswerElement initInfo(NetworkSnapshot snapshot, boolean summary, boolean verboseError);

  InitInfoAnswerElement initInfoBgpAdvertisements(
      NetworkSnapshot snapshot, boolean summary, boolean verboseError);

  /**
   * Returns the configurations for given snapshot. Parses and serializes them from snapshot input
   * if needed.
   */
  SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot);

  /**
   * Returns the configurations for given snapshot that have been processed (parsed, serialized,
   * etc.) before. The returned optional is empty if the snapshot configurations have not been
   * processed.
   */
  Optional<SortedMap<String, Configuration>> getProcessedConfigurations(NetworkSnapshot snapshot);

  /** Returns the vendor configurations of a given snapshot */
  Map<String, VendorConfiguration> loadVendorConfigurations(NetworkSnapshot snapshot);

  ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse(
      NetworkSnapshot snapshot);

  DataPlane loadDataPlane(NetworkSnapshot snapshot);

  SortedMap<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables(NetworkSnapshot snapshot);

  ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
      NetworkSnapshot snapshot);

  AtomicInteger newBatch(String description, int jobs);

  Set<BgpAdvertisement> loadExternalBgpAnnouncements(
      NetworkSnapshot snapshot, Map<String, Configuration> configurations);

  /**
   * @return a {@link TracerouteEngine} for the given snapshot.
   */
  TracerouteEngine getTracerouteEngine(NetworkSnapshot snapshot);

  @Nullable
  String readExternalBgpAnnouncementsFile(NetworkSnapshot snapshot);

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

  /** Return a {@link SpecifierContext} for a given {@link NetworkSnapshot} */
  SpecifierContext specifierContext(NetworkSnapshot networkSnapshot);

  AnswerElement standard(NetworkSnapshot snapshot, ReachabilityParameters reachabilityParameters);

  Set<Flow> bddLoopDetection(NetworkSnapshot snapshot);

  Set<Flow> bddMultipathConsistency(
      NetworkSnapshot snapshot, MultipathConsistencyParameters parameters);

  /** Performs bidirectional reachability analysis. */
  @Nonnull
  BidirectionalReachabilityResult bidirectionalReachability(
      NetworkSnapshot snapshot, BDDPacket bddPacket, ReachabilityParameters parameters);
}
