package org.batfish.storage;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspConfigurationException;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.Id;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.VendorConfiguration;

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
   * @throws IspConfigurationException if there is an error reading the log file.
   * @returns The IspConfiguration object or null if the file does not exist.
   */
  @Nullable
  IspConfiguration loadIspConfiguration(NetworkId network, SnapshotId snapshot)
      throws IspConfigurationException;

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
   * Returns the {@link SnapshotRuntimeData} of the network provided in the given snapshot
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  SnapshotRuntimeData loadRuntimeData(NetworkId network, SnapshotId snapshot);

  /**
   * Load the log file for a given work item ID.
   *
   * @throws FileNotFoundException if the log file is not found.
   * @throws IOException if there is an error reading the log file.
   */
  @Nonnull
  String loadWorkLog(NetworkId network, SnapshotId snapshot, String workId) throws IOException;

  /**
   * Load the answer JSON file for a given work item ID.
   *
   * @throws FileNotFoundException if the log file is not found.
   * @throws IOException if there is an error reading the log file.
   */
  @Nonnull
  String loadWorkJson(NetworkId network, SnapshotId snapshot, String workId) throws IOException;

  /**
   * Stores the configuration information into the given snapshot. Will replace any
   * previously-stored configurations.
   */
  void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      @Nullable Layer1Topology layer1Topology,
      NetworkId network,
      SnapshotId snapshot)
      throws IOException;

  /**
   * Store the answer to an ad-hoc question.
   *
   * @param network The id of the network
   * @param snapshot The id of the snapshot
   * @param answerStr The text of the answer
   * @param answerId The ID of the answer
   * @throws IOException if there is an error
   */
  void storeAnswer(NetworkId network, SnapshotId snapshot, String answerStr, AnswerId answerId)
      throws IOException;

  /**
   * Store the metadata for the answer to an ad-hoc question.
   *
   * @param network The id of the network
   * @param snapshot The id of the snapshot
   * @param answerMetadata The metadata to store
   * @param answerId The ID of the answer
   * @throws IOException if there is an error
   */
  void storeAnswerMetadata(
      NetworkId network, SnapshotId snapshot, AnswerMetadata answerMetadata, AnswerId answerId)
      throws IOException;

  /**
   * Load the text of a JSON-serialized ad-hoc question
   *
   * @param network The name of the network
   * @param question The name of the question
   * @throws IOException if there is some other error
   */
  @Nonnull
  String loadQuestion(NetworkId network, QuestionId question) throws IOException;

  /**
   * Returns {@code true} iff the specified question exists.
   *
   * @param network The name of the network
   * @param question The name of the question
   */
  boolean checkQuestionExists(NetworkId network, QuestionId question);

  /**
   * Load the JSON-serialized answer to an ad-hoc question.
   *
   * @param network The id of the network
   * @param snapshot The id of the snapshot
   * @param answerId The ID of the answer
   * @throws FileNotFoundException if answer does not exist; {@link IOException} if there is an
   *     error reading the answer.
   */
  @Nonnull
  String loadAnswer(NetworkId network, SnapshotId snapshot, AnswerId answerId)
      throws FileNotFoundException, IOException;

  /**
   * Load the metadata for the answer to an ad-hoc question.
   *
   * @param network The id of the network
   * @param snapshot The id of the snapshot
   * @param answerId The ID of the answer
   * @throws FileNotFoundException if answer metadata does not exist; {@link IOException} if there
   *     is an error reading the answer metadata.
   */
  @Nonnull
  AnswerMetadata loadAnswerMetadata(NetworkId network, SnapshotId snapshot, AnswerId answerId)
      throws FileNotFoundException, IOException;

  /**
   * Returns {@code true} iff the answer metadata for the specified ID exists.
   *
   * @param network The id of the network
   * @param snapshot The id of the snapshot
   * @param answerId The ID of the answer
   */
  @Nonnull
  boolean hasAnswerMetadata(NetworkId network, SnapshotId snapshot, AnswerId answerId);

  /**
   * Stores a question with the specified name and text.
   *
   * @param questionStr The JSON-serialized text of the question
   * @param network The name of the network
   * @param question The name of the question
   * @throws IOException if there is an error
   */
  void storeQuestion(String questionStr, NetworkId network, QuestionId question) throws IOException;

  /** Returns {@code true} iff the specified network question exists. */
  boolean checkNetworkExists(NetworkId network);

  /**
   * Retrieve the question class ID associated with the given question.
   *
   * @throws FileNotFoundException if question does not exist
   * @throws IOException if there is an error reading the question class ID
   */
  @Nonnull
  String loadQuestionClassId(NetworkId networkId, QuestionId questionId)
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
   * @param network The id of the network
   * @throws IOException if there is an error
   */
  void storeNodeRoles(NetworkId network, NodeRolesData nodeRolesData, NodeRolesId nodeRolesId)
      throws IOException;

  /**
   * Read the node roles data with the given ID.
   *
   * @param network The id of the network
   * @throws FileNotFoundException if the roles do not exist
   * @throws IOException if there is an error reading the roles.
   */
  @Nonnull
  String loadNodeRoles(NetworkId network, NodeRolesId nodeRolesId)
      throws FileNotFoundException, IOException;

  /** Returns true iff the network with the specified ID has node roles */
  boolean hasNodeRoles(NetworkId network, NodeRolesId nodeRolesId);

  /** Initialize an empty network */
  void initNetwork(NetworkId networkId);

  /** Delete answer metadata for given ID */
  void deleteAnswerMetadata(NetworkId network, SnapshotId snapshot, AnswerId answerId)
      throws IOException;

  /**
   * Provide a stream from which a network-wide extended object for the given key may be read
   *
   * @throws FileNotFoundException if the object for the given key does not exist
   * @throws IOException if there is an error reading the object
   */
  @MustBeClosed
  @Nonnull
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
  @MustBeClosed
  @Nonnull
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
  @MustBeClosed
  @Nonnull
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
  @MustBeClosed
  @Nonnull
  InputStream loadSnapshotInputObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException;

  boolean hasSnapshotInputObject(String key, NetworkSnapshot snapshot) throws IOException;

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

  /** Store a given string as an answer JSON file for a given work item ID. */
  void storeWorkJson(String jsonOutput, NetworkId network, SnapshotId snapshot, String workId)
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
   * Loads the {@link Layer1Topology} synthesized internally (e.g., for AWS).
   *
   * @throws IOException if there is an error reading the {@link Layer1Topology}
   */
  @Nonnull
  Optional<Layer1Topology> loadSynthesizedLayer1Topology(NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Loads the layer-3 {@link Topology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the layer-3 {@link Topology}
   */
  @Nonnull
  Topology loadLayer3Topology(NetworkSnapshot networkSnapshot) throws IOException;

  /**
   * Loads the {@link L3Adjacencies} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error reading the {@link L3Adjacencies}
   */
  @Nonnull
  L3Adjacencies loadL3Adjacencies(NetworkSnapshot networkSnapshot) throws IOException;

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
   * Stores the provided {@code layer3Topology} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code layer3Topology}
   */
  void storeLayer3Topology(Topology layer3Topology, NetworkSnapshot networkSnapshot)
      throws IOException;

  /**
   * Stores the provided {@link L3Adjacencies} corresponding to the converged {@link
   * org.batfish.datamodel.DataPlane} for the provided {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error writing the {@code layer3Topology}
   */
  void storeL3Adjacencies(L3Adjacencies l3Adjacencies, NetworkSnapshot networkSnapshot)
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

  /**
   * Read the value of an ID corresponding to given ancestor IDs, ID type, and user-provided name.
   * Returns {@link Optional#empty} if there is no such ID.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  Optional<String> readId(Class<? extends Id> idType, String name, Id... ancestors)
      throws IOException;

  /**
   * Write an name-ID mapping corresponding to given ancestor IDs and user-provided name.
   *
   * @throws IOException if there is an error
   */
  void writeId(Id id, String name, Id... ancestors) throws IOException;

  /**
   * Delete the name-ID mapping corresponding to the given ancestor IDs, ID type, and user-provided
   * name. Returns {@code true} iff a mapping for the provided name was successfully deleted.
   *
   * @throws IOException if there is an error
   */
  boolean deleteNameIdMapping(Class<? extends Id> idType, String name, Id... ancestors)
      throws IOException;

  /**
   * Returns true iff there is a name-ID mapping corresponding to given ancestor IDs and
   * user-provided name.
   */
  boolean hasId(Class<? extends Id> idType, String name, Id... ancestors);

  /**
   * Lists the resolvable names corresponding to the given ancestor IDs and ID type.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  Set<String> listResolvableNames(Class<? extends Id> idType, Id... ancestors) throws IOException;

  /**
   * Loads the network-wide reference library for the given network if it exists, or else returns
   * {@link Optional#empty}.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  Optional<ReferenceLibrary> loadReferenceLibrary(NetworkId network) throws IOException;

  /**
   * Stores the network-wide reference library for the given network
   *
   * @throws IOException if there is an error
   */
  void storeReferenceLibrary(ReferenceLibrary referenceLibrary, NetworkId network)
      throws IOException;

  /**
   * Stores the original zip for a snapshot upload request for the given network, associating it
   * with the given key.
   *
   * @throws IOException if there is an error
   */
  void storeUploadSnapshotZip(InputStream inputStream, String key, NetworkId network)
      throws IOException;

  /**
   * Stores the a fork-snapshot request for the given network, associating it with the given key.
   *
   * @throws IOException if there is an error
   */
  void storeForkSnapshotRequest(String forkSnapshotRequest, String key, NetworkId network)
      throws IOException;

  /**
   * Loads the original zip for a snapshot upload request associated with the given key.
   *
   * @throws FileNotFoundException if the zip is not found.
   * @throws IOException if there is any other error
   */
  @MustBeClosed
  @Nonnull
  InputStream loadUploadSnapshotZip(String key, NetworkId network) throws IOException;

  /**
   * Stores an input object with the given key whose contents are accessible via the given
   * inputStream for the given snapshot.
   *
   * @throws IOException if there is an error
   */
  void storeSnapshotInputObject(InputStream inputStream, String key, NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Returns a stream of the keys of all input objects for the given snapshot.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listSnapshotInputObjectKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Loads the stored data plane for the given snapshot.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  DataPlane loadDataPlane(NetworkSnapshot snapshot) throws IOException;

  /**
   * Stores the data plane for the given snapshot.
   *
   * @throws IOException if there is an error
   */
  void storeDataPlane(DataPlane dataPlane, NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns {@code true} iff a data plane has been stored for the given snapshot
   *
   * @throws IOException if there is an error
   */
  boolean hasDataPlane(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to environment BGP tables.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputEnvironmentBgpTableKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Loads the answer element that is the result of parsing environment BGP tables for the given
   * snapshot.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
      NetworkSnapshot snapshot) throws IOException;

  /**
   * Stores the answer element that is the result of parsing environment BGP tables for the given
   * snapshot.
   *
   * @throws IOException if there is an error
   */
  void storeParseEnvironmentBgpTablesAnswerElement(
      ParseEnvironmentBgpTablesAnswerElement parseEnvironmentBgpTablesAnswerElement,
      NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Returns true iff environment BGP tables have been parsed for the given snapshot.
   *
   * @throws IOException if there is an error
   */
  boolean hasParseEnvironmentBgpTablesAnswerElement(NetworkSnapshot snapshot) throws IOException;

  /**
   * Deletes the answer element that is the result of parsing environment BGP tables for the given
   * snapshot if it exists.
   *
   * @throws IOException if there is an error
   */
  void deleteParseEnvironmentBgpTablesAnswerElement(NetworkSnapshot snapshot) throws IOException;

  /**
   * Loads the compiled environment BGP tables for the given snapshot if they exist. Returns an
   * empty map if none were compiled.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  Map<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables(NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Stores the compiled environment BGP tables for the given snapshot if they exist.
   *
   * @throws IOException if there is an error
   */
  void storeEnvironmentBgpTables(
      Map<String, BgpAdvertisementsByVrf> environmentBgpTables, NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Deletes the compiled environment BGP tables for the given snapshot if they exist.
   *
   * @throws IOException if there is an error
   */
  void deleteEnvironmentBgpTables(NetworkSnapshot snapshot) throws IOException;

  /**
   * Loads the content of the external BGP announcements input file for the given snapshot, or
   * returns {@link Optional#empty} if the snapshot does not contain one.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  Optional<String> loadExternalBgpAnnouncementsFile(NetworkSnapshot snapshot) throws IOException;

  /**
   * Loads the {@link ConversionContext} for the given {@link NetworkSnapshot}, if present.
   *
   * @throws FileNotFoundException if there is no serialized {@link ConversionContext}
   * @throws IOException if there is an error deserializing
   */
  @Nonnull
  ConversionContext loadConversionContext(NetworkSnapshot snapshot) throws IOException;

  /**
   * Stores the {@link ConversionContext} for the given {@link NetworkSnapshot}.
   *
   * @throws IOException if there is an error
   */
  void storeConversionContext(ConversionContext conversionContext, NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Loads the answer element that is the result of parsing vendor configurations for the given
   * snapshot.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
      NetworkSnapshot snapshot) throws IOException;

  /**
   * Stores the answer element that is the result of parsing vendor configurations for the given
   * snapshot.
   *
   * @throws IOException if there is an error
   */
  void storeParseVendorConfigurationAnswerElement(
      ParseVendorConfigurationAnswerElement parseVendorConfigurationAnswerElement,
      NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Returns true iff vendor configurations have been parsed for the given snapshot.
   *
   * @throws IOException if there is an error
   */
  boolean hasParseVendorConfigurationAnswerElement(NetworkSnapshot snapshot) throws IOException;

  /**
   * Deletes the answer element that is the result of parsing vendor configurations for the given
   * snapshot if it exists.
   *
   * @throws IOException if there is an error
   */
  void deleteParseVendorConfigurationAnswerElement(NetworkSnapshot snapshot) throws IOException;

  /**
   * Loads the compiled vendor configurations for the given snapshot if they exist. Returns an empty
   * map if none were compiled.
   *
   * @throws IOException if there is an error
   */
  @Nonnull
  Map<String, VendorConfiguration> loadVendorConfigurations(NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Stores the compiled vendor configurations for the given snapshot if they exist. Merges with any
   * existing stored vendor configurations.
   *
   * @throws IOException if there is an error
   */
  void storeVendorConfigurations(
      Map<String, VendorConfiguration> vendorConfigurations, NetworkSnapshot snapshot)
      throws IOException;

  /**
   * Deletes the compiled vendor configurations for the given snapshot if they exist.
   *
   * @throws IOException if there is an error
   */
  void deleteVendorConfigurations(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to Checkpoint management servers.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputCheckpointManagementKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to SONiC config files.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputSonicConfigsKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to host configurations.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputHostConfigurationsKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to network configurations.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputNetworkConfigurationsKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to AWS multi-account configuration
   * data.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputAwsMultiAccountKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to AWS single-account configuration
   * data.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputAwsSingleAccountKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Returns a list of snapshot input object keys corresponding to Azure single-account
   * configuration data.
   *
   * @throws IOException if there is an error
   */
  @MustBeClosed
  @Nonnull
  Stream<String> listInputAzureSingleAccountKeys(NetworkSnapshot snapshot) throws IOException;

  /**
   * Run implementation-specific garbage collection.
   *
   * <p>Expunge stored data for networks and snapshot that have been deleted by the users. An
   * individual call to this function may not expunge all such data. Implementations need to only
   * guarantee that data is eventually deleted.
   *
   * @throws IOException if there is an error
   */
  void runGarbageCollection() throws IOException;
}
