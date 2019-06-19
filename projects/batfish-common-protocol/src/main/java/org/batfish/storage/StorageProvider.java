package org.batfish.storage;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.AnalysisMetadata;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.role.NodeRolesData;

/** Storage backend for loading and storing persistent data used by Batfish */
@ParametersAreNonnullByDefault
public interface StorageProvider {

  /**
   * Returns the configuration files for the given snapshot. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Nullable
  SortedMap<String, Configuration> loadConfigurations(NetworkId network, SnapshotId snapshot);

  /**
   * Returns the {@link ConvertConfigurationAnswerElement} that is the result of the phase that
   * converts vendor-specific configurations to vendor-independent configurations.
   *
   * @param network The name of the network
   * @param snapshot Then name of the snapshot
   */
  @Nullable
  ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      NetworkId network, SnapshotId snapshot);

  /**
   * Returns the interface blacklist for the specified snapshot.
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  SortedSet<NodeInterfacePair> loadInterfaceBlacklist(NetworkId network, SnapshotId snapshot);

  /**
   * Returns the configuration required to initialize ISPs for the specified snapshot.
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  IspConfiguration loadIspConfiguration(NetworkId network, SnapshotId snapshot);

  /**
   * Returns the node blacklist for the specified snapshot.
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  SortedSet<String> loadNodeBlacklist(NetworkId network, SnapshotId snapshot);

  /**
   * Returns the layer-1 topology of the network provided in the given snapshot
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  Layer1Topology loadLayer1Topology(NetworkId network, SnapshotId snapshot);

  /**
   * Returns the {@link MajorIssueConfig} for the given network and majorIssueType. Returns {@code
   * null} if none exists.
   */
  @Nullable
  MajorIssueConfig loadMajorIssueConfig(NetworkId network, IssueSettingsId majorIssueType);

  /**
   * Load the log file for a given work item ID.
   *
   * @throws FileNotFoundException if the log file is not found.
   * @throws IOException if there is an error reading the log file.
   */
  @Nonnull
  String loadWorkLog(NetworkId network, SnapshotId snapshot, String workId) throws IOException;

  /**
   * Stores the {@link MajorIssueConfig} into the given network. Will replace any previously-stored
   * {@link MajorIssueConfig}s
   *
   * @param network The name of the network
   * @param majorIssueType The type of the {@link MajorIssueConfig}
   * @param majorIssueConfig The {@link MajorIssueConfig} to be stored
   * @throws IOException if there is an error writing writing the config
   */
  void storeMajorIssueConfig(
      NetworkId network, IssueSettingsId majorIssueType, MajorIssueConfig majorIssueConfig)
      throws IOException;

  /**
   * Stores the configuration information into the given snapshot. Will replace any
   * previously-stored configurations.
   */
  void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      NetworkId network,
      SnapshotId snapshot);

  /**
   * Store the answer to an ad-hoc or analysis question.
   *
   * @param answerStr The text of the answer
   * @param answerId The ID of the answer
   */
  void storeAnswer(String answerStr, AnswerId answerId);

  /**
   * Store the metadata for the answer to an ad-hoc or analysis question.
   *
   * @param answerMetadata The metadata to store
   * @param answerId The ID of the answer
   */
  void storeAnswerMetadata(AnswerMetadata answerMetadata, AnswerId answerId);

  /**
   * Load the text of a JSON-serialized ad-hoc or analysis question
   *
   * @param network The name of the network
   * @param question The name of the question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   */
  @Nonnull
  String loadQuestion(NetworkId network, QuestionId question, @Nullable AnalysisId analysis);

  /**
   * Returns {@code true} iff the specified question exists.
   *
   * @param network The name of the network
   * @param question The name of the question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   */
  boolean checkQuestionExists(
      NetworkId network, QuestionId question, @Nullable AnalysisId analysis);

  /**
   * Load the JSON-serialized answer to an ad-hoc or analysis question.
   *
   * @param answerId The ID of the answer
   * @throws FileNotFoundException if answer does not exist; {@link IOException} if there is an
   *     error reading the answer.
   */
  @Nonnull
  String loadAnswer(AnswerId answerId) throws FileNotFoundException, IOException;

  /**
   * Load the metadata for the answer to an ad-hoc or analysis question.
   *
   * @param answerId The ID of the answer
   * @throws FileNotFoundException if answer metadata does not exist; {@link IOException} if there
   *     is an error reading the answer metadata.
   */
  @Nonnull
  AnswerMetadata loadAnswerMetadata(AnswerId answerId) throws FileNotFoundException, IOException;

  /**
   * Returns {@code true} iff the answer metadata for the specified ID exists.
   *
   * @param answerId The ID of the answer
   */
  @Nonnull
  boolean hasAnswerMetadata(AnswerId answerId);

  /**
   * Stores a question with the specified name and text.
   *
   * @param questionStr The JSON-serialized text of the question
   * @param network The name of the network
   * @param question The name of the question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   */
  void storeQuestion(
      String questionStr, NetworkId network, QuestionId question, @Nullable AnalysisId analysis);

  /**
   * Return the JSON-serialized settings for the {@code questionClassId} under the specified {@code
   * networkId}, or {@code null} if no custom settings exist.
   *
   * @param networkId The ID of the network
   * @param questionSettingsId The ID of the question class
   * @throws IOException if there is an error trying to read the settings
   */
  @Nullable
  String loadQuestionSettings(NetworkId networkId, QuestionSettingsId questionSettingsId)
      throws IOException;

  /** Returns {@code true} iff the specified network question exists. */
  boolean checkNetworkExists(NetworkId network);

  /**
   * Write the JSON-serialized settings for the specified question class for the specified network.
   *
   * @param networkId The name of the network
   * @param questionSettingsId The ID of the question class
   * @param settings The settings to write
   * @throws IOException if there is an error writing the settings
   */
  void storeQuestionSettings(
      String settings, NetworkId networkId, QuestionSettingsId questionSettingsId)
      throws IOException;

  /**
   * Retrieve the question class ID associated with the given question.
   *
   * @throws FileNotFoundException if question does not exist
   * @throws IOException if there is an error reading the question class ID
   */
  @Nonnull
  String loadQuestionClassId(NetworkId networkId, QuestionId questionId, AnalysisId analysisId)
      throws FileNotFoundException, IOException;

  /**
   * Returns {@code true} iff metadata for the analysis with specified ID exists.
   *
   * @param networkId The ID of the network
   * @param analysisId The ID of the analysis
   */
  boolean hasAnalysisMetadata(NetworkId networkId, AnalysisId analysisId);

  /**
   * Stores metadata for the analysis in the given network.
   *
   * @param analysisMetadata The metadata to write
   * @param networkId The ID of the network
   * @param analysisId The ID of the analysis
   */
  void storeAnalysisMetadata(
      AnalysisMetadata analysisMetadata, NetworkId networkId, AnalysisId analysisId)
      throws IOException;

  /**
   * Loads metadata for the analysis in the given network.
   *
   * @param networkId The ID of the network
   * @param analysisId The ID of the analysis
   * @throws FileNotFoundException if metadata does not exist
   * @throws IOException if there is an error reading the metadata.
   */
  @Nonnull
  String loadAnalysisMetadata(NetworkId networkId, AnalysisId analysisId)
      throws FileNotFoundException, IOException;

  /**
   * Stores metadata for the snapshot in the given network.
   *
   * @param snapshotMetadata The metadata to write
   * @param networkId The ID of the network
   * @param snapshotId The ID of the snapshot
   * @throws IOException if there is an error
   */
  void storeSnapshotMetadata(
      SnapshotMetadata snapshotMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException;

  /**
   * Loads metadata for the snapshot in the given network.
   *
   * @param networkId The ID of the network
   * @param snapshotId The ID of the snapshot
   * @throws FileNotFoundException if metadata does not exist.
   * @throws IOException if there is an error reading the metadata.
   */
  @Nonnull
  String loadSnapshotMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws FileNotFoundException, IOException;

  /**
   * Write the node roles data for the given ID.
   *
   * @throws IOException if there is an error
   */
  void storeNodeRoles(NodeRolesData nodeRolesData, NodeRolesId nodeRolesId) throws IOException;

  /**
   * Read the node roles data with the given ID.
   *
   * @throws FileNotFoundException if the roles do not exist
   * @throws IOException if there is an error reading the roles.
   */
  @Nonnull
  String loadNodeRoles(NodeRolesId nodeRolesId) throws FileNotFoundException, IOException;

  /** Returns true iff the network with the specified ID has node roles */
  boolean hasNodeRoles(NodeRolesId nodeRolesId);

  /** Initialize an empty network */
  void initNetwork(NetworkId networkId);

  /** Delete answer metadata for given ID */
  void deleteAnswerMetadata(AnswerId answerId) throws FileNotFoundException, IOException;

  /**
   * Provide a stream from which a network-wide extended object for the given key may be read
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @Nonnull
  @MustBeClosed
  InputStream loadNetworkObject(NetworkId networkId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Writes the network-wide extended object at for the given key using the provided input stream.
   *
   * @throws IOException if there is an error writing the object
   */
  void storeNetworkObject(InputStream inputStream, NetworkId networkId, String key)
      throws IOException;

  /**
   * Deletes the network-wide extended object for the given key.
   *
   * @throws FileNotFoundException if the object does not exist
   * @throws IOException if there is an error deleting the object
   */
  void deleteNetworkObject(NetworkId networkId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Provide a stream from which a network-scoped blob object for the given key may be read.
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @Nonnull
  @MustBeClosed
  InputStream loadNetworkBlob(NetworkId networkId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Writes the network-scoped blob object at for the given key using the provided input stream.
   *
   * @throws IOException if there is an error writing the object
   */
  void storeNetworkBlob(InputStream inputStream, NetworkId networkId, String key)
      throws IOException;

  /**
   * Provide a stream from which a snapshot-wide extended object for the given key may be read
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @Nonnull
  @MustBeClosed
  InputStream loadSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Writes the snapshot-wide extended object for the given key using the provided input stream.
   *
   * @throws IOException if there is an error writing the object
   */
  void storeSnapshotObject(
      InputStream inputStream, NetworkId networkId, SnapshotId snapshotId, String key)
      throws IOException;

  /**
   * Deletes the snapshot-wide extended object for the given key.
   *
   * @throws FileNotFoundException if the object does not exist
   * @throws IOException if there is an error deleting the object
   */
  void deleteSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Provide a stream from which a snapshot input object for the given key may be read
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @Nonnull
  InputStream loadSnapshotInputObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException;

  /**
   * Fetch the list of keys in the given snapshot's input directory
   *
   * @throws IOException if there is an error retrieving the metadata
   */
  @Nonnull
  List<StoredObjectMetadata> getSnapshotInputObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException;

  /**
   * Fetch the list of keys in the given snapshot's extended objects store
   *
   * @throws IOException if there is an error retrieving the metadata
   */
  @Nonnull
  List<StoredObjectMetadata> getSnapshotExtendedObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException;

  /**
   * Loads the JSON-serialized POJO topology produced for a snapshot
   *
   * @throws IOException if there is an error reading the topology
   */
  @Nonnull
  String loadPojoTopology(NetworkId networkId, SnapshotId snapshotId) throws IOException;

  /**
   * Loads the JSON-serialized topology produced for a snapshot
   *
   * @throws IOException if there is an error reading the topology
   */
  @Nonnull
  String loadInitialTopology(NetworkId networkId, SnapshotId snapshotId) throws IOException;

  /**
   * Writes the topology for the provided network and snapshot
   *
   * @throws IOException if there is an error writing the topology
   */
  void storeInitialTopology(Topology topology, NetworkId networkId, SnapshotId snapshotId)
      throws IOException;

  /**
   * Writes the pojo topology for the provided network and snapshot
   *
   * @throws IOException if there is an error writing the topology
   */
  void storePojoTopology(
      org.batfish.datamodel.pojo.Topology topology, NetworkId networkId, SnapshotId snapshotId)
      throws IOException;

  /** Store a given string as a log file for a given work item ID. */
  void storeWorkLog(String logOutput, NetworkId network, SnapshotId snapshot, String workId)
      throws IOException;

  /**
   * Loads the JSON-serialized {@link CompletionMetadata} for the provided network and snapshot
   *
   * @param networkId The ID of the network
   * @param snapshotId The ID of the snapshot
   * @return The {@link CompletionMetadata} found for the provided network and snapshot. If none is
   *     found a {@link CompletionMetadata} will be returned with all fields empty
   * @throws IOException if there is an error reading the {@link CompletionMetadata}
   */
  CompletionMetadata loadCompletionMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws IOException;

  /**
   * Writes the {@link CompletionMetadata} produced for the given network and snapshot
   *
   * @param completionMetadata The {@link CompletionMetadata} to write
   * @param networkId The ID of the network
   * @param snapshotId The ID of the snapshot
   * @throws IOException if there is an error writing the {@link CompletionMetadata}
   */
  void storeCompletionMetadata(
      CompletionMetadata completionMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException;

  /**
   * Loads the {@link BgpTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the {@link BgpTopology}
   */
  @Nonnull
  BgpTopology loadBgpTopology(NetworkSnapshot networkSnapshot) throws IOException;

  /**
   * Loads the {@link EigrpTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the {@link EigrpTopology}
   */
  @Nonnull
  EigrpTopology loadEigrpTopology(NetworkSnapshot networkSnapshot) throws IOException;

  /**
   * Loads the optional {@link Layer2Topology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the {@link Layer2Topology}
   */
  @Nonnull
  Optional<Layer2Topology> loadLayer2Topology(NetworkSnapshot networkSnapshot) throws IOException;

  /**
   * Loads the layer-3 {@link Topology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the layer-3 {@link Topology}
   */
  @Nonnull
  Topology loadLayer3Topology(NetworkSnapshot networkSnapshot) throws IOException;

  /**
   * Loads the {@link OspfTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the {@link OspfTopology}
   */
  @Nonnull
  OspfTopology loadOspfTopology(NetworkSnapshot networkSnapshot) throws IOException;

  /**
   * Loads the {@link VxlanTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the {@link VxlanTopology}
   */
  @Nonnull
  VxlanTopology loadVxlanTopology(NetworkSnapshot networkSnapshot) throws IOException;

  /**
   * Stores the provided {@code bgpTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code bgpTopology}
   */
  void storeBgpTopology(BgpTopology bgpTopology, NetworkSnapshot networkSnapshot)
      throws IOException;

  /**
   * Stores the provided {@code eigrpTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code eigrpTopology}
   */
  void storeEigrpTopology(EigrpTopology eigrpTopology, NetworkSnapshot networkSnapshot)
      throws IOException;

  /**
   * Stores the provided optional {@code layer2Topology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code layer2Topology}
   */
  void storeLayer2Topology(Optional<Layer2Topology> layer2Topology, NetworkSnapshot networkSnapshot)
      throws IOException;

  /**
   * Stores the provided {@code layer3Topology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code layer3Topology}
   */
  void storeLayer3Topology(Topology layer3Topology, NetworkSnapshot networkSnapshot)
      throws IOException;

  /**
   * Stores the provided {@code ospfTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code ospfTopology}
   */
  void storeOspfTopology(OspfTopology ospfTopology, NetworkSnapshot networkSnapshot)
      throws IOException;

  /**
   * Stores the provided {@code vxlanTopology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code vxlanTopology}
   */
  void storeVxlanTopology(VxlanTopology vxlanTopology, NetworkSnapshot networkSnapshot)
      throws IOException;
}
