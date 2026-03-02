package org.batfish.storage;

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

public class TestStorageProvider implements StorageProvider {

  @Override
  public SortedMap<String, Configuration> loadConfigurations(
      NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public @Nullable ConversionContext loadConversionContext(NetworkSnapshot snapshot)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public @Nullable SortedSet<NodeInterfacePair> loadInterfaceBlacklist(
      NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public @Nullable IspConfiguration loadIspConfiguration(NetworkId network, SnapshotId snapshot)
      throws IspConfigurationException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public @Nullable SortedSet<String> loadNodeBlacklist(NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public Layer1Topology loadLayer1Topology(NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public @Nullable SnapshotRuntimeData loadRuntimeData(NetworkId network, SnapshotId snapshot) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public @Nonnull String loadWorkLog(NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public @Nonnull String loadWorkJson(NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      Layer1Topology synthesizedLayer1Topology,
      NetworkId network,
      SnapshotId snapshot)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeConversionContext(ConversionContext conversionContext, NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadQuestion(NetworkId network, QuestionId question) throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean checkQuestionExists(NetworkId network, QuestionId question) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeQuestion(String questionStr, NetworkId network, QuestionId question)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean checkNetworkExists(NetworkId network) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeAnswer(
      NetworkId network, SnapshotId snapshot, String answerStr, AnswerId answerId)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeAnswerMetadata(
      NetworkId network, SnapshotId snapshot, AnswerMetadata answerMetadata, AnswerId answerId)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadAnswer(NetworkId network, SnapshotId snapshot, AnswerId answerId)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public AnswerMetadata loadAnswerMetadata(
      NetworkId network, SnapshotId snapshot, AnswerId answerId)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean hasAnswerMetadata(NetworkId network, SnapshotId snapshot, AnswerId answerId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadQuestionClassId(NetworkId networkId, QuestionId questionId)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeSnapshotMetadata(
      SnapshotMetadata snapshotMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadSnapshotMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void storeNodeRoles(
      NetworkId network, NodeRolesData nodeRolesData, NodeRolesId nodeRolesId) throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public String loadNodeRoles(NetworkId network, NodeRolesId nodeRolesId)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean hasNodeRoles(NetworkId network, NodeRolesId nodeRolesId) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public void initNetwork(NetworkId networkId) {}

  @Override
  public void deleteAnswerMetadata(NetworkId networkId, SnapshotId snapshotId, AnswerId answerId)
      throws IOException {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public InputStream loadNetworkBlob(NetworkId networkId, String key)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeNetworkBlob(InputStream inputStream, NetworkId networkId, String key)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream loadNetworkObject(NetworkId networkId, String key)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeNetworkObject(InputStream inputStream, NetworkId networkId, String key)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteNetworkObject(NetworkId networkId, String key)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream loadSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeSnapshotObject(
      InputStream inputStream, NetworkId networkId, SnapshotId snapshotId, String key)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteSnapshotObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream loadSnapshotInputObject(NetworkId networkId, SnapshotId snapshotId, String key)
      throws FileNotFoundException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSnapshotInputObject(String key, NetworkSnapshot snapshot) throws IOException {
    return false;
  }

  @Override
  public @Nonnull List<StoredObjectMetadata> getSnapshotInputObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull List<StoredObjectMetadata> getSnapshotExtendedObjectsMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String loadPojoTopology(NetworkId networkId, SnapshotId snapshotId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String loadInitialTopology(NetworkId networkId, SnapshotId snapshotId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeInitialTopology(Topology topology, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storePojoTopology(
      org.batfish.datamodel.pojo.Topology topology, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeWorkLog(String logOutput, NetworkId network, SnapshotId snapshot, String workId)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeWorkJson(
      String jsonOutput, NetworkId network, SnapshotId snapshot, String workId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompletionMetadata loadCompletionMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeCompletionMetadata(
      CompletionMetadata completionMetadata, NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public BgpTopology loadBgpTopology(NetworkSnapshot networkSnapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public EigrpTopology loadEigrpTopology(NetworkSnapshot networkSnapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Layer1Topology> loadSynthesizedLayer1Topology(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Topology loadLayer3Topology(NetworkSnapshot networkSnapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull L3Adjacencies loadL3Adjacencies(NetworkSnapshot networkSnapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public OspfTopology loadOspfTopology(NetworkSnapshot networkSnapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public VxlanTopology loadVxlanTopology(NetworkSnapshot networkSnapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeBgpTopology(BgpTopology bgpTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeEigrpTopology(EigrpTopology eigrpTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeLayer3Topology(Topology layer3Topology, NetworkSnapshot networkSnapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeL3Adjacencies(L3Adjacencies adjacencies, NetworkSnapshot networkSnapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeOspfTopology(OspfTopology ospfTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeVxlanTopology(VxlanTopology vxlanTopology, NetworkSnapshot networkSnapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Optional<String> readId(Class<? extends Id> type, String name, Id... ancestors)
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

  @Override
  public @Nonnull Set<String> listResolvableNames(Class<? extends Id> type, Id... ancestors)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Optional<ReferenceLibrary> loadReferenceLibrary(NetworkId network)
      throws IOException {
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

  @Override
  public @Nonnull InputStream loadUploadSnapshotZip(String key, NetworkId network)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeSnapshotInputObject(
      InputStream inputStream, String key, NetworkSnapshot snapshot) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listSnapshotInputObjectKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull DataPlane loadDataPlane(NetworkSnapshot snapshot) throws IOException {
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

  @Override
  public @Nonnull Stream<String> listInputEnvironmentBgpTableKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull ParseEnvironmentBgpTablesAnswerElement loadParseEnvironmentBgpTablesAnswerElement(
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

  @Override
  public @Nonnull Map<String, BgpAdvertisementsByVrf> loadEnvironmentBgpTables(
      NetworkSnapshot snapshot) throws IOException {
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

  @Override
  public @Nonnull Optional<String> loadExternalBgpAnnouncementsFile(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
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

  @Override
  public @Nonnull Map<String, VendorConfiguration> loadVendorConfigurations(
      NetworkSnapshot snapshot) throws IOException {
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

  @Override
  public @Nonnull Stream<String> listInputHostConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listInputNetworkConfigurationsKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listInputCheckpointManagementKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listInputSonicConfigsKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listInputCiscoAciConfigsKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listInputAwsMultiAccountKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listInputAwsSingleAccountKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Stream<String> listInputAzureSingleAccountKeys(NetworkSnapshot snapshot)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void runGarbageCollection() {}
}
