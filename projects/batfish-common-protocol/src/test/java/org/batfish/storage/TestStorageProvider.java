package org.batfish.storage;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.AnalysisMetadata;
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
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.Id;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.vendor.VendorConfiguration;

public class TestStorageProvider implements StorageProvider {

  @Override
  public SortedMap<String, Configuration> loadConfigurations(
      NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Nullable
  @Override
  public SortedSet<NodeInterfacePair> loadInterfaceBlacklist(
      NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Nullable
  @Override
  public IspConfiguration loadIspConfiguration(NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Nullable
  @Override
  public SortedSet<String> loadNodeBlacklist(NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public Layer1Topology loadLayer1Topology(NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Nullable
  @Override
  public SnapshotRuntimeData loadRuntimeData(NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Nonnull
  @Override
  public String loadWorkLog(NetworkId network, SnapshotId snapshot, String workId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Nonnull
  @Override
  public String loadWorkJson(NetworkId network, SnapshotId snapshot, String workId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      Layer1Topology synthesizedLayer1Topology,
      NetworkId network,
      SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadQuestion(NetworkId network, QuestionId analysis, AnalysisId question) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean checkQuestionExists(NetworkId network, QuestionId question, AnalysisId analysis) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeQuestion(
      String questionStr, NetworkId network, QuestionId question, AnalysisId analysis) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean checkNetworkExists(NetworkId network) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeAnswer(String answerStr, AnswerId answerId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeAnswerMetadata(AnswerMetadata answerMetadata, AnswerId answerId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadAnswer(AnswerId answerId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public AnswerMetadata loadAnswerMetadata(AnswerId answerId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean hasAnswerMetadata(AnswerId answerId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadQuestionClassId(
      NetworkId networkId, QuestionId questionId, AnalysisId analysisId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean hasAnalysisMetadata(NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeAnalysisMetadata(
      AnalysisMetadata analysisMetadata, NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadAnalysisMetadata(NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeSnapshotMetadata(
      SnapshotMetadata snapshotMetadata, NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadSnapshotMetadata(NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeNodeRoles(NodeRolesData nodeRolesData, NodeRolesId nodeRolesId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadNodeRoles(NodeRolesId nodeRolesId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean hasNodeRoles(NodeRolesId nodeRolesId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void initNetwork(NetworkId networkId) {}

  @Override
  public void deleteAnswerMetadata(AnswerId answerId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public InputStream loadNetworkBlob(NetworkId networkId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeNetworkBlob(InputStream inputStream, NetworkId networkId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream loadNetworkObject(NetworkId networkId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeNetworkObject(InputStream inputStream, NetworkId networkId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteNetworkObject(NetworkId networkId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream loadSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeSnapshotObject(
      InputStream inputStream, NetworkId networkId, SnapshotId snapshotId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream loadSnapshotInputObject(
      NetworkId networkId, SnapshotId snapshotId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSnapshotInputObject(String key, NetworkSnapshot snapshot) throws IOException {
    return false;
  }

  @Override
  public @Nonnull List<StoredObjectMetadata> getSnapshotInputObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull List<StoredObjectMetadata> getSnapshotExtendedObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String loadPojoTopology(NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String loadInitialTopology(NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeInitialTopology(Topology topology, NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storePojoTopology(
      org.batfish.datamodel.pojo.Topology topology, NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeWorkLog(
      String logOutput, NetworkId network, SnapshotId snapshot, String workId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeWorkJson(
      String jsonOutput, NetworkId network, SnapshotId snapshot, String workId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompletionMetadata loadCompletionMetadata(NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeCompletionMetadata(
      CompletionMetadata completionMetadata, NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BgpTopology loadBgpTopology(NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public EigrpTopology loadEigrpTopology(NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Layer1Topology> loadSynthesizedLayer1Topology(NetworkSnapshot snapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Layer2Topology> loadLayer2Topology(NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Topology loadLayer3Topology(NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OspfTopology loadOspfTopology(NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VxlanTopology loadVxlanTopology(NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeBgpTopology(BgpTopology bgpTopology, NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeEigrpTopology(EigrpTopology eigrpTopology, NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeLayer2Topology(
      Optional<Layer2Topology> layer2Topology, NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeLayer3Topology(Topology layer3Topology, NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeOspfTopology(OspfTopology ospfTopology, NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeVxlanTopology(VxlanTopology vxlanTopology, NetworkSnapshot networkSnapshot) {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Optional<String> readId(Class<? extends Id> type, String name, Id... ancestors)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeId(Id id, String name, Id... ancestors) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean deleteNameIdMapping(Class<? extends Id> type, String name, Id... ancestors)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasId(Class<? extends Id> type, String name, Id... ancestors) {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Set<String> listResolvableNames(Class<? extends Id> type, Id... ancestors)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Optional<ReferenceLibrary> loadReferenceLibrary(NetworkId network) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeReferenceLibrary(ReferenceLibrary referenceLibrary, NetworkId network)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeUploadSnapshotZip(InputStream inputStream, String key, NetworkId network)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeForkSnapshotRequest(String forkSnapshotRequest, String key, NetworkId network)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public InputStream loadUploadSnapshotZip(String key, NetworkId network) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeSnapshotInputObject(
      InputStream inputStream, String key, NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Stream<String> listSnapshotInputObjectKeys(NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public DataPlane loadDataPlane(NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeDataPlane(DataPlane dataPlane, NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasDataPlane(NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Stream<String> listInputEnvironmentBgpTableKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
      NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeParseEnvironmentBgpTablesAnswerElement(
      ParseEnvironmentBgpTablesAnswerElement parseEnvironmentBgpTablesAnswerElement,
      NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasParseEnvironmentBgpTablesAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteParseEnvironmentBgpTablesAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Map<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeEnvironmentBgpTables(
      Map<String, BgpAdvertisementsByVrf> environmentBgpTables, NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteEnvironmentBgpTables(NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Optional<String> loadExternalBgpAnnouncementsFile(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
      NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeParseVendorConfigurationAnswerElement(
      ParseVendorConfigurationAnswerElement parseVendorConfigurationAnswerElement,
      NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasParseVendorConfigurationAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteParseVendorConfigurationAnswerElement(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Map<String, VendorConfiguration> loadVendorConfigurations(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeVendorConfigurations(
      Map<String, VendorConfiguration> vendorConfigurations, NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteVendorConfigurations(NetworkSnapshot snapshot) throws IOException {}

  @Nonnull
  @Override
  public Stream<String> listInputHostConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Stream<String> listInputNetworkConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Stream<String> listInputAwsMultiAccountKeys(NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Stream<String> listInputAwsSingleAccountKeys(NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void runGarbageCollection(Instant expungeBeforeDate) {}
}
