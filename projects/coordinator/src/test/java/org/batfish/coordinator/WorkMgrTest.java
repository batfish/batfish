package org.batfish.coordinator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.CommonUtil.writeFile;
import static org.batfish.coordinator.WorkMgr.addToSerializedList;
import static org.batfish.coordinator.WorkMgr.deserializeAndDeleteInterfaceBlacklist;
import static org.batfish.coordinator.WorkMgr.generateFileDateString;
import static org.batfish.coordinator.WorkMgr.removeFromSerializedList;
import static org.batfish.coordinator.WorkMgr.updateRuntimeData;
import static org.batfish.coordinator.WorkMgrTestUtils.createSnapshot;
import static org.batfish.coordinator.WorkMgrTestUtils.setupQuestionAndAnswer;
import static org.batfish.identifiers.NodeRolesId.DEFAULT_NETWORK_NODE_ROLES_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import javax.ws.rs.BadRequestException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.ColumnFilter;
import org.batfish.common.ColumnSortOption;
import org.batfish.common.Container;
import org.batfish.common.WorkItem;
import org.batfish.common.runtime.RuntimeData;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.id.IdManager;
import org.batfish.coordinator.resources.ForkSnapshotBean;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.SnapshotMetadataEntry;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AnswerMetadataUtil;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.Issue;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.LoopStep;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.TestQuestion;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.table.TableView;
import org.batfish.datamodel.table.TableViewRow;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.role.RoleDimensionMapping;
import org.batfish.storage.StorageProvider;
import org.batfish.storage.StoredObjectMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link WorkMgr}. */
public final class WorkMgrTest {

  private static TableMetadata MOCK_TABLE_METADATA =
      new TableMetadata(ImmutableList.of(new ColumnMetadata("col", Schema.STRING, "desc")));

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private WorkMgr _manager;
  private StorageProvider _storage;
  private IdManager _idManager;
  private SnapshotMetadataMgr _snapshotMetadataManager;

  @Before
  public void initManager() {
    WorkMgrTestUtils.initWorkManager(_folder);
    _manager = Main.getWorkMgr();
    _idManager = _manager.getIdManager();
    _snapshotMetadataManager = _manager.getSnapshotMetadataManager();
    _storage = _manager.getStorage();
  }

  private void createSnapshotWithMetadata(String network, String snapshot) throws IOException {
    createSnapshotWithMetadata(network, snapshot, Instant.now());
  }

  private void createSnapshotWithMetadata(String network, String snapshot, Instant creationTime)
      throws IOException {
    NetworkId networkId = _idManager.getNetworkId(network).get();
    SnapshotId snapshotId = _idManager.generateSnapshotId();
    _idManager.assignSnapshot(snapshot, networkId, snapshotId);
    _snapshotMetadataManager.writeMetadata(
        new SnapshotMetadata(creationTime, null), networkId, snapshotId);
  }

  @Test
  public void testAddToSerializedList() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();

    NodeInterfacePair baseInterface = NodeInterfacePair.of("n1", "iface1");
    NodeInterfacePair additionalInterface = NodeInterfacePair.of("n2", "iface2");

    // Write base serialized list
    List<NodeInterfacePair> interfaces = new ArrayList<>();
    interfaces.add(baseInterface);
    writeFile(serializedListPath, BatfishObjectMapper.writePrettyString(interfaces));

    addToSerializedList(
        serializedListPath,
        ImmutableList.of(additionalInterface),
        new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm the additional and original interfaces show up in the merged list
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        containsInAnyOrder(baseInterface, additionalInterface));
  }

  @Test
  public void testAddToSerializedListNoAddition() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();

    NodeInterfacePair baseInterface = NodeInterfacePair.of("n1", "iface1");

    // Write base serialized list
    List<NodeInterfacePair> interfaces = new ArrayList<>();
    interfaces.add(baseInterface);
    writeFile(serializedListPath, BatfishObjectMapper.writePrettyString(interfaces));

    addToSerializedList(
        serializedListPath, ImmutableList.of(), new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm original interface shows up in the merged list, even if there are no additions
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        containsInAnyOrder(baseInterface));
  }

  @Test
  public void testAddToSerializedListNullAddition() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();

    NodeInterfacePair baseInterface = NodeInterfacePair.of("n1", "iface1");

    // Write base serialized list
    List<NodeInterfacePair> interfaces = new ArrayList<>();
    interfaces.add(baseInterface);
    writeFile(serializedListPath, BatfishObjectMapper.writePrettyString(interfaces));

    addToSerializedList(serializedListPath, null, new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm original interface shows up in the merged list, even if addition is null
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        containsInAnyOrder(baseInterface));
  }

  @Test
  public void testAddToSerializedListNoList() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();
    serializedList.delete();

    NodeInterfacePair additionalInterface = NodeInterfacePair.of("n2", "iface2");

    addToSerializedList(
        serializedListPath,
        ImmutableList.of(additionalInterface),
        new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm the additional interface shows up in the serialized list, even if the serialized list
    // didn't exist in the first place
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        containsInAnyOrder(additionalInterface));
  }

  @Test
  public void testAddToSerializedListNoListNoAddition() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();
    serializedList.delete();

    addToSerializedList(
        serializedListPath, ImmutableList.of(), new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm no file was created (since there was no list to begin with and nothing was added)
    assertThat(serializedList, not(anExistingFile()));
  }

  @Test
  public void testRemoveFromSerializedList() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();

    NodeInterfacePair baseInterface1 = NodeInterfacePair.of("n1", "iface1");
    NodeInterfacePair baseInterface2 = NodeInterfacePair.of("n2", "iface2");

    // Write base serialized list
    List<NodeInterfacePair> interfaces = new ArrayList<>();
    interfaces.add(baseInterface1);
    interfaces.add(baseInterface2);
    writeFile(serializedListPath, BatfishObjectMapper.writePrettyString(interfaces));

    removeFromSerializedList(
        serializedListPath,
        ImmutableList.of(baseInterface1),
        new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm only one interface shows up
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        contains(baseInterface2));
  }

  @Test
  public void testRemoveFromSerializedListBadSubtraction() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();

    NodeInterfacePair baseInterface1 = NodeInterfacePair.of("n1", "iface1");
    NodeInterfacePair baseInterface2 = NodeInterfacePair.of("n2", "iface2");
    NodeInterfacePair subtraction1 = NodeInterfacePair.of("n2", "iface2");
    NodeInterfacePair subtraction2 = NodeInterfacePair.of("n3", "iface3");

    // Write base serialized list
    List<NodeInterfacePair> interfaces = new ArrayList<>();
    interfaces.add(baseInterface1);
    interfaces.add(baseInterface2);
    writeFile(serializedListPath, BatfishObjectMapper.writePrettyString(interfaces));

    // Removing non-existent element should throw
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(
        String.format(
            "Existing blacklist does not contain element(s) specified for removal: '%s'",
            ImmutableList.of(subtraction2)));
    removeFromSerializedList(
        serializedListPath,
        ImmutableList.of(subtraction1, subtraction2),
        new TypeReference<List<NodeInterfacePair>>() {});
  }

  @Test
  public void testRemoveFromSerializedListBadSubtractionMissingBaseList() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();
    Files.delete(serializedListPath);

    NodeInterfacePair subtraction = NodeInterfacePair.of("n2", "iface2");

    // Removing non-existent element should throw
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Cannot remove element(s) from non-existent blacklist.");
    removeFromSerializedList(
        serializedListPath,
        ImmutableList.of(subtraction),
        new TypeReference<List<NodeInterfacePair>>() {});
  }

  @Test
  public void testRemoveFromSerializedListEmptyBaseList() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();

    // Write empty base serialized list
    List<NodeInterfacePair> interfaces = new ArrayList<>();
    writeFile(serializedListPath, BatfishObjectMapper.writePrettyString(interfaces));

    removeFromSerializedList(
        serializedListPath, ImmutableList.of(), new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm no issue if base list didn't exist
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        iterableWithSize(0));
  }

  @Test
  public void testRemoveFromSerializedListMissingBaseList() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();
    Files.delete(serializedListPath);

    // Remove nothing from a non-existent list
    removeFromSerializedList(
        serializedListPath, ImmutableList.of(), new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm no issue if base list didn't exist
    assertThat(serializedList, not(anExistingFile()));
  }

  @Test
  public void testRemoveFromSerializedListNoSubtraction() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File serializedList = tmp.newFile();
    Path serializedListPath = serializedList.toPath();

    NodeInterfacePair baseInterface1 = NodeInterfacePair.of("n1", "iface1");

    // Write base serialized list
    List<NodeInterfacePair> interfaces = new ArrayList<>();
    interfaces.add(baseInterface1);
    writeFile(serializedListPath, BatfishObjectMapper.writePrettyString(interfaces));

    removeFromSerializedList(
        serializedListPath, ImmutableList.of(), new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm nothing changes with no subtraction
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        contains(baseInterface1));

    removeFromSerializedList(
        serializedListPath, null, new TypeReference<List<NodeInterfacePair>>() {});

    // Confirm nothing changes with null subtraction
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readFile(serializedListPath),
                new TypeReference<List<NodeInterfacePair>>() {}),
        contains(baseInterface1));
  }

  @Test
  public void testDeserializeAndDeleteInterfaceBlacklist_noBlacklist() throws IOException {
    // No blacklist file: Should return an empty list
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    Path blacklistPath = tmp.newFile().toPath();
    CommonUtil.delete(blacklistPath);
    assertThat(deserializeAndDeleteInterfaceBlacklist(blacklistPath), empty());
  }

  @Test
  public void testDeserializeAndDeleteInterfaceBlacklist_emptyBlacklist() throws IOException {
    // Empty blacklist: Should return an empty list and delete file
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File blacklistFile = tmp.newFile();
    assertTrue(blacklistFile.exists());
    assertThat(deserializeAndDeleteInterfaceBlacklist(blacklistFile.toPath()), empty());
    assertFalse(blacklistFile.exists());
  }

  @Test
  public void testDeserializeAndDeleteInterfaceBlacklist_nonEmptyBlacklist() throws IOException {
    // Non-empty blacklist: Should return blacklist contents and delete file
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    NodeInterfacePair nip = NodeInterfacePair.of("n1", "i1");
    File blacklistFile = tmp.newFile();
    Path blacklistPath = blacklistFile.toPath();
    writeFile(blacklistPath, BatfishObjectMapper.writePrettyString(ImmutableList.of(nip)));
    assertThat(deserializeAndDeleteInterfaceBlacklist(blacklistPath), contains(nip));
    assertFalse(blacklistFile.exists());
  }

  @Test
  public void testDeserializeAndDeleteInterfaceBlacklist_invalidBlacklist() throws IOException {
    // Invalid blacklist: Should return an empty list and delete file
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    File blacklistFile = tmp.newFile();
    Path blacklistPath = blacklistFile.toPath();
    writeFile(blacklistPath, "invalid json");
    assertThat(deserializeAndDeleteInterfaceBlacklist(blacklistPath), empty());
    assertFalse(blacklistFile.exists());
  }

  @Test
  public void testUpdateRuntimeData() throws IOException {
    NodeInterfacePair nip1 = NodeInterfacePair.of("n1", "i1");
    NodeInterfacePair nip2 = NodeInterfacePair.of("n2", "i2");
    NodeInterfacePair nip3 = NodeInterfacePair.of("n3", "i3");
    TemporaryFolder tmp = new TemporaryFolder();

    // No runtime data and no changes: Shouldn't create runtime data
    {
      tmp.create();
      Path runtimeDataPath = tmp.newFile().toPath();
      CommonUtil.delete(runtimeDataPath);
      updateRuntimeData(runtimeDataPath, ImmutableSet.of(), ImmutableSet.of());
      assertFalse(runtimeDataPath.toFile().exists());
    }

    // No runtime data: Should create it with the given line up data
    {
      tmp.create();
      Path runtimeDataPath = tmp.newFile().toPath();
      updateRuntimeData(runtimeDataPath, ImmutableSet.of(nip1), ImmutableSet.of());
      assertThat(
          BatfishObjectMapper.mapper()
              .readValue(CommonUtil.readFile(runtimeDataPath), SnapshotRuntimeData.class),
          equalTo(SnapshotRuntimeData.builder().setInterfacesLineDown(nip1).build()));
    }

    // Invalid snapshot runtime data: should get replaced
    {
      tmp.create();
      Path runtimeDataPath = tmp.newFile().toPath();
      CommonUtil.writeFile(runtimeDataPath, "invalid json");
      updateRuntimeData(runtimeDataPath, ImmutableSet.of(nip1), ImmutableSet.of());
      assertThat(
          BatfishObjectMapper.mapper()
              .readValue(CommonUtil.readFile(runtimeDataPath), SnapshotRuntimeData.class),
          equalTo(SnapshotRuntimeData.builder().setInterfacesLineDown(nip1).build()));
    }

    // Existing empty runtime data: Should add data
    {
      tmp.create();
      Path runtimeDataPath = tmp.newFile().toPath();
      CommonUtil.writeFile(
          runtimeDataPath,
          BatfishObjectMapper.writePrettyString(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA));

      // nip1 line down, nip2 line up
      updateRuntimeData(runtimeDataPath, ImmutableSet.of(nip1), ImmutableSet.of(nip2));
      assertThat(
          BatfishObjectMapper.mapper()
              .readValue(CommonUtil.readFile(runtimeDataPath), SnapshotRuntimeData.class),
          equalTo(
              SnapshotRuntimeData.builder()
                  .setInterfacesLineDown(nip1)
                  .setInterfacesLineUp(nip2)
                  .build()));
    }

    // Existing non-empty runtime data: Should apply updates without changing other data
    {
      tmp.create();
      Path runtimeDataPath = tmp.newFile().toPath();

      // Initially: nip1 up, nip2 down, nip3 down. Reverse states of first 2; nip3 should not change
      SnapshotRuntimeData existingData =
          SnapshotRuntimeData.builder()
              .setInterfacesLineUp(nip1)
              .setInterfacesLineDown(nip2, nip3)
              .build();
      CommonUtil.writeFile(runtimeDataPath, BatfishObjectMapper.writePrettyString(existingData));
      updateRuntimeData(runtimeDataPath, ImmutableSet.of(nip1), ImmutableSet.of(nip2));
      assertThat(
          BatfishObjectMapper.mapper()
              .readValue(CommonUtil.readFile(runtimeDataPath), SnapshotRuntimeData.class),
          equalTo(
              SnapshotRuntimeData.builder()
                  .setInterfacesLineUp(nip2)
                  .setInterfacesLineDown(nip1, nip3)
                  .build()));
    }

    // Same interface in deactivate and restore lists; should be up
    {
      tmp.create();
      Path runtimeDataPath = tmp.newFile().toPath();
      CommonUtil.writeFile(
          runtimeDataPath,
          BatfishObjectMapper.writePrettyString(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA));
      updateRuntimeData(runtimeDataPath, ImmutableSet.of(nip1), ImmutableSet.of(nip1));
      assertThat(
          BatfishObjectMapper.mapper()
              .readValue(CommonUtil.readFile(runtimeDataPath), SnapshotRuntimeData.class),
          equalTo(SnapshotRuntimeData.builder().setInterfacesLineUp(nip1).build()));
    }
  }

  @Test
  public void initNetworkWithContainerName() {
    String initResult = _manager.initNetwork("container", null);
    assertThat(initResult, equalTo("container"));
  }

  @Test
  public void initNetworkWithContainerPrefix() {
    String initResult = _manager.initNetwork(null, "containerPrefix");
    assertThat(initResult, startsWith("containerPrefix"));
  }

  @Test
  public void initNetworkWithNullInput() {
    String initResult = _manager.initNetwork(null, null);
    assertThat(initResult, startsWith("null_"));
  }

  @Test
  public void initExistingContainer() {
    _manager.initNetwork("container", null);
    String expectedMessage = "Network 'container' already exists!";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo(expectedMessage));
    _manager.initNetwork("container", null);
  }

  @Test
  public void listEmptyQuestion() {
    _manager.initNetwork("container", null);
    SortedSet<String> questions = _manager.listQuestions("container", false);
    assertThat(questions.isEmpty(), is(true));
  }

  @Test
  public void listQuestionNames() {
    String questionName = "publicquestion";
    // Leading __ means this question is an internal question
    // And should be hidden from listQuestions when verbose is false
    String internalQuestionName = "__internalquestion";
    String network = "container";
    _manager.initNetwork(network, null);
    NetworkId networkId = _idManager.getNetworkId(network).get();
    // Make sure the questions are assigned
    _idManager.assignQuestion(questionName, networkId, _idManager.generateQuestionId());
    _idManager.assignQuestion(internalQuestionName, networkId, _idManager.generateQuestionId());

    SortedSet<String> questionsNotVerbose = _manager.listQuestions(network, false);
    SortedSet<String> questionsVerbose = _manager.listQuestions(network, true);

    // Only the public question should show up when verbose is false
    assertThat(questionsNotVerbose, equalTo(Sets.newHashSet(questionName)));

    // Both questions should show up when verbose is true
    assertThat(questionsVerbose, equalTo(Sets.newHashSet(questionName, internalQuestionName)));
  }

  @Test
  public void listQuestionWithNonExistContainer() {
    assertThat(_manager.listQuestions("container", false), nullValue());
  }

  @Test
  public void listSortedQuestionNames() {
    String network = "container";
    _manager.initNetwork(network, null);
    NetworkId networkId = _idManager.getNetworkId(network).get();
    _idManager.assignQuestion("nodes", networkId, _idManager.generateQuestionId());
    _idManager.assignQuestion("access", networkId, _idManager.generateQuestionId());
    _idManager.assignQuestion("initinfo", networkId, _idManager.generateQuestionId());
    SortedSet<String> questions = _manager.listQuestions(network, false);

    assertThat(questions, equalTo(ImmutableSet.of("access", "initinfo", "nodes")));
  }

  @Test
  public void getEmptyContainer() {
    _manager.initNetwork("container", null);
    Container container = _manager.getContainer("container");
    assertThat(container, equalTo(Container.of("container", new TreeSet<>())));
  }

  @Test
  public void getLatestTestrig() throws IOException {
    _manager.initNetwork("container", null);

    // empty should be returned if no testrigs exist
    assertThat(_manager.getLatestSnapshot("container"), equalTo(Optional.empty()));

    // create testrig1, which should be returned
    createSnapshotWithMetadata("container", "testrig1");
    assertThat(_manager.getLatestSnapshot("container"), equalTo(Optional.of("testrig1")));

    // create a second testrig, which should be returned
    createSnapshotWithMetadata("container", "testrig2");
    assertThat(_manager.getLatestSnapshot("container"), equalTo(Optional.of("testrig2")));
  }

  @Test
  public void getNodes() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    _manager.initNetwork(network, null);
    NetworkId networkId = _idManager.getNetworkId(network).get();

    // create a snapshot and write a topology object for it
    createSnapshotWithMetadata(network, snapshot);
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshot, networkId).get();
    Topology topology = new Topology(snapshot);
    topology.setNodes(ImmutableSet.of(new Node("a1"), new Node("b1")));
    _storage.storePojoTopology(topology, networkId, snapshotId);

    // should get the nodes of the topology when we ask for it
    assertThat(_manager.getNodes(network, snapshot), equalTo(ImmutableSet.of("a1", "b1")));
  }

  @Test
  public void getNonEmptyContainer() {
    String network = "container";
    String snapshot = "testrig";
    _manager.initNetwork(network, null);
    NetworkId networkId = _idManager.getNetworkId(network).get();
    _idManager.assignSnapshot(snapshot, networkId, _idManager.generateSnapshotId());
    Container container = _manager.getContainer(network);
    assertThat(
        container,
        equalTo(Container.of(network, Sets.newTreeSet(Collections.singleton(snapshot)))));
  }

  @Test
  public void getNonExistContainer() {
    _thrown.expect(Exception.class);
    _thrown.expectMessage(equalTo("Network 'container' does not exist"));
    _manager.getContainer("container");
  }

  @Test
  public void testForkSnapshot() throws Exception {
    String networkName = "network";
    String snapshotBaseName = "snapshotBase";
    String snapshotNewName = "snapshotNew";

    _manager.initNetwork(networkName, null);
    uploadTestSnapshot(networkName, snapshotBaseName);
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotBaseName, snapshotNewName, null, null, null, null, null, null, null));

    // Confirm the forked snapshot exists
    assertThat(_manager.getLatestSnapshot(networkName), equalTo(Optional.of(snapshotNewName)));
  }

  @Test
  public void testForkSnapshotBlacklists() throws Exception {
    String networkName = "network";
    String snapshotBaseName = "snapshotBase";
    String snapshotNewName1 = "snapshotNew1";
    String snapshotNewName2 = "snapshotNew2";

    List<NodeInterfacePair> interfaces = ImmutableList.of(NodeInterfacePair.of("n1", "iface1"));
    List<Edge> links = ImmutableList.of(Edge.of("n2", "iface2", "n3", "iface3"));
    List<String> nodes = ImmutableList.of("n4", "n5");

    _manager.initNetwork(networkName, null);
    uploadTestSnapshot(networkName, snapshotBaseName);
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotBaseName, snapshotNewName1, interfaces, links, nodes, null, null, null, null));
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId1 = _idManager.getSnapshotId(snapshotNewName1, networkId).get();

    // Confirm the forked snapshot exists
    assertThat(_manager.getLatestSnapshot(networkName), equalTo(Optional.of(snapshotNewName1)));
    // Confirm the blacklists are correct
    // Blacklisted interfaces should be represented as runtime data, not interface blacklist
    assertNull(_storage.loadInterfaceBlacklist(networkId, snapshotId1));
    assertThat(
        _storage.loadRuntimeData(networkId, snapshotId1),
        equalTo(SnapshotRuntimeData.builder().setInterfacesLineDown(interfaces).build()));
    assertThat(
        _storage.loadNodeBlacklist(networkId, snapshotId1), containsInAnyOrder(nodes.toArray()));

    // Remove blacklisted items from a fork of the first fork
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotNewName1, snapshotNewName2, null, null, null, interfaces, links, nodes, null));
    SnapshotId snapshotId2 = _idManager.getSnapshotId(snapshotNewName2, networkId).get();

    // Confirm the forked snapshot exists
    assertThat(_manager.getLatestSnapshot(networkName), equalTo(Optional.of(snapshotNewName2)));
    // Confirm the blacklists are empty
    // Interface blacklist still shouldn't exist; runtime data should now specify interfaces are up
    assertNull(_storage.loadInterfaceBlacklist(networkId, snapshotId2));
    assertThat(
        _storage.loadRuntimeData(networkId, snapshotId2),
        equalTo(SnapshotRuntimeData.builder().setInterfacesLineUp(interfaces).build()));
    assertThat(_storage.loadNodeBlacklist(networkId, snapshotId2), iterableWithSize(0));
  }

  @Test
  public void testForkSnapshot_runtimeDataInBase() throws IOException {
    /*
    Setup:
    4 devices (n1, n2, n3, n4)
    3 interfaces each (i1, i2, i3)
    Original snapshot runtime data says all i1 are line down, all i2 are line up, nothing about i3
    Fork deactivates all n1 and n2 interfaces
    Fork activates all n1 and n3 interfaces
     */
    // Create base snapshot runtime data
    String n1 = "n1";
    String n2 = "n2";
    String n3 = "n3";
    String n4 = "n4";
    String i1 = "i1";
    String i2 = "i2";
    String i3 = "i3";
    RuntimeData deviceData =
        RuntimeData.builder().setInterfaceLineUp(i1, false).setInterfaceLineUp(i2, true).build();
    SnapshotRuntimeData runtimeData =
        SnapshotRuntimeData.builder()
            .setRuntimeData(
                ImmutableMap.of(n1, deviceData, n2, deviceData, n3, deviceData, n4, deviceData))
            .build();

    // Create activate/deactivate lists
    Set<NodeInterfacePair> n1Ifaces =
        ImmutableSet.of(
            NodeInterfacePair.of(n1, i1),
            NodeInterfacePair.of(n1, i2),
            NodeInterfacePair.of(n1, i3));
    Set<NodeInterfacePair> n2Ifaces =
        ImmutableSet.of(
            NodeInterfacePair.of(n2, i1),
            NodeInterfacePair.of(n2, i2),
            NodeInterfacePair.of(n2, i3));
    Set<NodeInterfacePair> n3Ifaces =
        ImmutableSet.of(
            NodeInterfacePair.of(n3, i1),
            NodeInterfacePair.of(n3, i2),
            NodeInterfacePair.of(n3, i3));
    List<NodeInterfacePair> deactivate =
        Streams.concat(n1Ifaces.stream(), n2Ifaces.stream())
            .collect(ImmutableList.toImmutableList());
    List<NodeInterfacePair> activate =
        Streams.concat(n1Ifaces.stream(), n3Ifaces.stream())
            .collect(ImmutableList.toImmutableList());

    /*
    Expected runtime data in fork:
    - All activated interfaces (n1:i1, n1:i2, n1:i3, n3:i1, n3:i2, n3:i3) should be up
    - All deactivated interfaces that were not activated (n2:i1, n2:i2, n2:i3) should be down
    - n4:i1 should be line down because runtime data says so
    - n4:i2 should be line up because runtime data says so
    - n4:i3 should not be in runtime data because no info was given about it (will interpret as up)
     */
    Set<NodeInterfacePair> expectedDown =
        Sets.union(n2Ifaces, ImmutableSet.of(NodeInterfacePair.of(n4, i1)));
    Set<NodeInterfacePair> expectedUp =
        Sets.union(ImmutableSet.copyOf(activate), ImmutableSet.of(NodeInterfacePair.of(n4, i2)));

    // Create base snapshot
    String networkName = "network";
    String baseSnapshotName = "snapshotName";
    Path srcDir = createSnapshot(baseSnapshotName, "file.type", "! empty config", _folder);
    Path snapshotDir = srcDir.resolve(baseSnapshotName);
    Path batfishDir = snapshotDir.resolve(BfConsts.RELPATH_BATFISH);
    batfishDir.toFile().mkdir();
    CommonUtil.writeFile(
        batfishDir.resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        BatfishObjectMapper.writePrettyString(runtimeData));
    _manager.initNetwork(networkName, null);
    _manager.initSnapshot(networkName, baseSnapshotName, srcDir, Instant.now());

    // Create fork
    String forkName = "fork";
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            baseSnapshotName, forkName, deactivate, null, null, activate, null, null, null));
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId forkId = _idManager.getSnapshotId(forkName, networkId).get();

    // Interface blacklist should not exist in fork
    assertNull(_storage.loadInterfaceBlacklist(networkId, forkId));
    assertThat(
        _storage.loadRuntimeData(networkId, forkId),
        equalTo(
            SnapshotRuntimeData.builder()
                .setInterfacesLineDown(expectedDown)
                .setInterfacesLineUp(expectedUp)
                .build()));
  }

  /**
   * We handle runtime_data.json at the snapshot root directory as well as in its preferred location
   * under batfish/.
   */
  @Test
  public void testForkSnapshot_runtimeDataInBase_oldLocation() throws IOException {
    /*
    Setup:
    4 devices (n1, n2, n3, n4)
    3 interfaces each (i1, i2, i3)
    Original snapshot runtime data says all i1 are line down, all i2 are line up, nothing about i3
    Fork deactivates all n1 and n2 interfaces
    Fork activates all n1 and n3 interfaces
     */
    // Create base snapshot runtime data
    String n1 = "n1";
    String n2 = "n2";
    String n3 = "n3";
    String n4 = "n4";
    String i1 = "i1";
    String i2 = "i2";
    String i3 = "i3";
    RuntimeData deviceData =
        RuntimeData.builder().setInterfaceLineUp(i1, false).setInterfaceLineUp(i2, true).build();
    SnapshotRuntimeData runtimeData =
        SnapshotRuntimeData.builder()
            .setRuntimeData(
                ImmutableMap.of(n1, deviceData, n2, deviceData, n3, deviceData, n4, deviceData))
            .build();

    // Create activate/deactivate lists
    Set<NodeInterfacePair> n1Ifaces =
        ImmutableSet.of(
            NodeInterfacePair.of(n1, i1),
            NodeInterfacePair.of(n1, i2),
            NodeInterfacePair.of(n1, i3));
    Set<NodeInterfacePair> n2Ifaces =
        ImmutableSet.of(
            NodeInterfacePair.of(n2, i1),
            NodeInterfacePair.of(n2, i2),
            NodeInterfacePair.of(n2, i3));
    Set<NodeInterfacePair> n3Ifaces =
        ImmutableSet.of(
            NodeInterfacePair.of(n3, i1),
            NodeInterfacePair.of(n3, i2),
            NodeInterfacePair.of(n3, i3));
    List<NodeInterfacePair> deactivate =
        Streams.concat(n1Ifaces.stream(), n2Ifaces.stream())
            .collect(ImmutableList.toImmutableList());
    List<NodeInterfacePair> activate =
        Streams.concat(n1Ifaces.stream(), n3Ifaces.stream())
            .collect(ImmutableList.toImmutableList());

    /*
    Expected runtime data in fork:
    - All activated interfaces (n1:i1, n1:i2, n1:i3, n3:i1, n3:i2, n3:i3) should be up
    - All deactivated interfaces that were not activated (n2:i1, n2:i2, n2:i3) should be down
    - n4:i1 should be line down because runtime data says so
    - n4:i2 should be line up because runtime data says so
    - n4:i3 should not be in runtime data because no info was given about it (will interpret as up)
     */
    Set<NodeInterfacePair> expectedDown =
        Sets.union(n2Ifaces, ImmutableSet.of(NodeInterfacePair.of(n4, i1)));
    Set<NodeInterfacePair> expectedUp =
        Sets.union(ImmutableSet.copyOf(activate), ImmutableSet.of(NodeInterfacePair.of(n4, i2)));

    // Create base snapshot
    String networkName = "network";
    String baseSnapshotName = "snapshotName";
    Path srcDir = createSnapshot(baseSnapshotName, "file.type", "! empty config", _folder);
    Path snapshotDir = srcDir.resolve(baseSnapshotName);
    CommonUtil.writeFile(
        snapshotDir.resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        BatfishObjectMapper.writePrettyString(runtimeData));
    _manager.initNetwork(networkName, null);
    _manager.initSnapshot(networkName, baseSnapshotName, srcDir, Instant.now());

    // Create fork
    String forkName = "fork";
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            baseSnapshotName, forkName, deactivate, null, null, activate, null, null, null));
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId forkId = _idManager.getSnapshotId(forkName, networkId).get();

    // Interface blacklist should not exist in fork
    assertNull(_storage.loadInterfaceBlacklist(networkId, forkId));
    assertThat(
        _storage.loadRuntimeData(networkId, forkId),
        equalTo(
            SnapshotRuntimeData.builder()
                .setInterfacesLineDown(expectedDown)
                .setInterfacesLineUp(expectedUp)
                .build()));
  }

  @Test
  public void testForkSnapshot_interfaceBlacklistInBase() throws IOException {
    /*
    Setup:
    4 devices (n1, n2, n3, n4)
    2 interfaces each (i1, i2)
    Original snapshot interface blacklist says all i1 are line down
    Fork deactivates all n1 and n2 interfaces
    Fork activates all n1 and n3 interfaces
     */
    // Create base snapshot interface blacklist
    String n1 = "n1";
    String n2 = "n2";
    String n3 = "n3";
    String n4 = "n4";
    String i1 = "i1";
    String i2 = "i2";
    Set<NodeInterfacePair> baseBlacklist =
        ImmutableSet.of(
            NodeInterfacePair.of(n1, i1),
            NodeInterfacePair.of(n2, i1),
            NodeInterfacePair.of(n3, i1),
            NodeInterfacePair.of(n4, i1));

    // Create activate/deactivate lists
    Set<NodeInterfacePair> n1Ifaces =
        ImmutableSet.of(NodeInterfacePair.of(n1, i1), NodeInterfacePair.of(n1, i2));
    Set<NodeInterfacePair> n2Ifaces =
        ImmutableSet.of(NodeInterfacePair.of(n2, i1), NodeInterfacePair.of(n2, i2));
    Set<NodeInterfacePair> n3Ifaces =
        ImmutableSet.of(NodeInterfacePair.of(n3, i1), NodeInterfacePair.of(n3, i2));
    List<NodeInterfacePair> deactivate =
        Streams.concat(n1Ifaces.stream(), n2Ifaces.stream())
            .collect(ImmutableList.toImmutableList());
    List<NodeInterfacePair> activate =
        Streams.concat(n1Ifaces.stream(), n3Ifaces.stream())
            .collect(ImmutableList.toImmutableList());

    /*
    Expected runtime data in fork:
    - All activated interfaces (n1:i1, n1:i2, n3:i1, n3:i2) should be up
    - All deactivated interfaces that were not activated (n2:i1, n2:i2) should be down
    - n4:i1 should be line down because interface blacklist says so
    - n4:i2 should not be in runtime data because no info was given about it (will interpret as up)
     */
    Set<NodeInterfacePair> expectedDown =
        Sets.union(n2Ifaces, ImmutableSet.of(NodeInterfacePair.of(n4, i1)));
    Collection<NodeInterfacePair> expectedUp = activate;

    // Create base snapshot
    String networkName = "network";
    String baseSnapshotName = "snapshotName";
    Path srcDir = createSnapshot(baseSnapshotName, "file.type", "! empty config", _folder);
    Path snapshotDir = srcDir.resolve(baseSnapshotName);
    CommonUtil.writeFile(
        snapshotDir.resolve(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE),
        BatfishObjectMapper.writePrettyString(baseBlacklist));
    _manager.initNetwork(networkName, null);
    _manager.initSnapshot(networkName, baseSnapshotName, srcDir, Instant.now());

    // Create fork
    String forkName = "fork";
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            baseSnapshotName, forkName, deactivate, null, null, activate, null, null, null));
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId forkId = _idManager.getSnapshotId(forkName, networkId).get();

    // Interface blacklist should not exist in fork
    assertNull(_storage.loadInterfaceBlacklist(networkId, forkId));
    assertThat(
        _storage.loadRuntimeData(networkId, forkId),
        equalTo(
            SnapshotRuntimeData.builder()
                .setInterfacesLineDown(expectedDown)
                .setInterfacesLineUp(expectedUp)
                .build()));
  }

  @Test
  public void testForkSnapshotDuplicateName() throws Exception {
    String networkName = "network";
    String snapshotBaseName = "snapshotBase";
    String snapshotNewName = "snapshotNew";

    _manager.initNetwork(networkName, null);
    uploadTestSnapshot(networkName, snapshotBaseName);
    uploadTestSnapshot(networkName, snapshotNewName);

    // Fork should fail due to duplicate/conflicting new snapshot name
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(equalTo("Snapshot with name: '" + snapshotNewName + "' already exists"));
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotBaseName, snapshotNewName, null, null, null, null, null, null, null));
  }

  @Test
  public void testForkSnapshotFileUpload() throws Exception {
    String networkName = "network";
    String snapshotBaseName = "snapshotBase";
    String snapshotNewName = "snapshotNew";
    String fileName = "file.type";
    String fileContents = "new";

    _manager.initNetwork(networkName, null);
    uploadTestSnapshot(networkName, snapshotBaseName);

    // Create zip with a new file to add to the forked snapshot
    byte[] zipFile = createSnapshotZip(snapshotNewName, fileName, fileContents);

    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotBaseName, snapshotNewName, null, null, null, null, null, null, zipFile));

    // Confirm the forked snapshot exists
    assertThat(_manager.getLatestSnapshot(networkName), equalTo(Optional.of(snapshotNewName)));

    // Confirm the new file exists in the forked snapshot, with the right contents
    String readFileContents = readSnapshotConfig(networkName, snapshotNewName, fileName);
    assertThat(readFileContents, equalTo(fileContents));
  }

  @Test
  public void testForkSnapshotFileUploadOverwrite() throws Exception {
    String networkName = "network";
    String snapshotBaseName = "snapshotBase";
    String snapshotNewName = "snapshotNew";
    String fileName = "file.type";
    String fileContents = "contents";
    String fileContentsNew = "new";

    _manager.initNetwork(networkName, null);
    // Create base snapshot with a file: fileName, containing: fileContents
    uploadTestSnapshot(networkName, snapshotBaseName, fileName, fileContents);

    // Create zip with a file to overwrite the original file in the forked snapshot
    byte[] zipFile = createSnapshotZip(snapshotNewName, fileName, fileContentsNew);

    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotBaseName, snapshotNewName, null, null, null, null, null, null, zipFile));

    // Confirm the forked snapshot exists
    assertThat(_manager.getLatestSnapshot(networkName), equalTo(Optional.of(snapshotNewName)));

    // Confirm the file was overwritten with the new contents
    String readFileContents = readSnapshotConfig(networkName, snapshotNewName, fileName);
    assertThat(readFileContents, equalTo(fileContentsNew));
  }

  private byte[] createSnapshotZip(String snapshot, String fileName, String fileContents)
      throws IOException {
    Path zipPath =
        WorkMgrTestUtils.createSingleFileSnapshotZip(snapshot, fileName, fileContents, _folder);
    return FileUtils.readFileToByteArray(zipPath.toFile());
  }

  private String readSnapshotConfig(String network, String snapshot, String fileName)
      throws IOException {
    try (InputStream inputStream =
        _manager.getSnapshotInputObject(
            network,
            snapshot,
            Paths.get(BfConsts.RELPATH_CONFIGURATIONS_DIR, fileName).toString())) {
      assertThat(inputStream, not(nullValue()));
      return new String(IOUtils.toByteArray(inputStream), UTF_8);
    }
  }

  @Test
  public void testForkSnapshotMissingBaseSnapshot() throws Exception {
    String networkName = "network";
    String snapshotBaseName = "snapshotBase";
    String snapshotNewName = "snapshotNew";

    _manager.initNetwork(networkName, null);

    // Fork should fail because base snapshot does not exist
    _thrown.expect(FileNotFoundException.class);
    _thrown.expectMessage(
        equalTo("Base snapshot with name: '" + snapshotBaseName + "' does not exist"));
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotBaseName, snapshotNewName, null, null, null, null, null, null, null));
  }

  @Test
  public void testForkSnapshotMissingNetwork() throws Exception {
    String networkName = "network";
    String snapshotBaseName = "snapshotBase";
    String snapshotNewName = "snapshotNew";

    // Fork should fail because network does not exist
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(equalTo("Network '" + networkName + "' does not exist"));
    _manager.forkSnapshot(
        networkName,
        new ForkSnapshotBean(
            snapshotBaseName, snapshotNewName, null, null, null, null, null, null, null));
  }

  @Test
  public void testGetAnswer() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String questionName = "question";

    Answer expectedAnswer = new Answer();
    expectedAnswer.addAnswerElement(new StringAnswerElement("foo1"));
    String expectedAnswerString = BatfishObjectMapper.writeString(expectedAnswer);
    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);
    setupQuestionAndAnswer(network, snapshot, questionName, expectedAnswer);
    Answer ans = _manager.getAnswer(network, snapshot, questionName, null);

    // Confirm the getAnswer returns the answer we setup
    String ansString = BatfishObjectMapper.writeString(ans);
    assertThat(ansString, equalTo(expectedAnswerString));
  }

  @Test
  public void testGetAnswerNotFound() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String questionName = "question";

    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);
    setupQuestionAndAnswer(network, snapshot, questionName, null, null);

    // Confirm we get null calling getAnswer before the question is answered
    assertThat(_manager.getAnswer(network, snapshot, questionName, null), nullValue());
  }

  @Test
  public void testGetAnswerReferenceSnapshot() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String referenceSnapshot = "referenceSnapshot";
    String questionName = "question";

    Answer expectedAnswer = new Answer();
    expectedAnswer.addAnswerElement(new StringAnswerElement("foo1"));
    String expectedAnswerString = BatfishObjectMapper.writeString(expectedAnswer);
    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);
    uploadTestSnapshot(network, referenceSnapshot);
    setupQuestionAndAnswer(network, snapshot, questionName, expectedAnswer, referenceSnapshot);
    Answer ans = _manager.getAnswer(network, snapshot, questionName, referenceSnapshot);

    // Confirm the getAnswer returns the answer we setup
    String ansString = BatfishObjectMapper.writeString(ans);
    assertThat(ansString, equalTo(expectedAnswerString));
  }

  @Test
  public void testGetAnswerReferenceSnapshotBadRef() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String referenceSnapshot = "referenceSnapshot";
    String bogusReferenceSnapshot = "bogusReferenceSnapshot";
    String questionName = "question";

    Answer expectedAnswer = new Answer();
    expectedAnswer.addAnswerElement(new StringAnswerElement("foo1"));
    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);
    uploadTestSnapshot(network, referenceSnapshot);
    setupQuestionAndAnswer(network, snapshot, questionName, expectedAnswer, referenceSnapshot);

    // Confirm we get an illegal arg exception calling getAnswer with a bad reference snapshot
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(
        containsString(
            String.format(
                "Missing snapshot '%s' for network '%s'", bogusReferenceSnapshot, network)));
    _manager.getAnswer(network, snapshot, questionName, bogusReferenceSnapshot);
  }

  @Test
  public void testGetAnswerReferenceSnapshotNotFound() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String referenceSnapshot = "referenceSnapshot";
    String questionName = "question";

    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);
    uploadTestSnapshot(network, referenceSnapshot);
    setupQuestionAndAnswer(network, snapshot, questionName, null, referenceSnapshot);

    // Confirm we get null calling getAnswer before the question is answered
    assertThat(_manager.getAnswer(network, snapshot, questionName, referenceSnapshot), nullValue());
  }

  @Test
  public void testGetAnswerMetadataAdHocSuccess() throws IOException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    Question question = new TestQuestion();
    String questionContent = BatfishObjectMapper.writeString(question);
    String questionName = "question2Name";
    _manager.initNetwork(networkName, null);
    _manager.uploadQuestion(networkName, questionName, questionContent, false);
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId = _idManager.generateSnapshotId();
    _idManager.assignSnapshot(snapshotName, networkId, snapshotId);
    QuestionId questionId = _idManager.getQuestionId(questionName, networkId).get();
    AnswerId baseAnswerId =
        _idManager.getAnswerId(
            networkId, snapshotId, questionId, DEFAULT_NETWORK_NODE_ROLES_ID, null);
    Answer answer = new Answer();
    answer.addAnswerElement(new TableAnswerElement(MOCK_TABLE_METADATA));
    AnswerMetadata answerMetadata =
        AnswerMetadataUtil.computeAnswerMetadata(answer, Main.getLogger());
    _storage.storeAnswerMetadata(networkId, snapshotId, answerMetadata, baseAnswerId);
    String answerStr = BatfishObjectMapper.writeString(answer);
    _storage.storeAnswer(networkId, snapshotId, answerStr, baseAnswerId);
    AnswerMetadata answerResult =
        _manager.getAnswerMetadata(networkName, snapshotName, questionName, null);

    assertThat(answerResult, equalTo(answerMetadata));
  }

  @Test
  public void testGetAnswerMetadataAdHocMissingAnswerMetadata() throws IOException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    Question question = new TestQuestion();
    String questionContent = BatfishObjectMapper.writeString(question);
    String questionName = "question2Name";
    _manager.initNetwork(networkName, null);
    _manager.uploadQuestion(networkName, questionName, questionContent, false);
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId = _idManager.generateSnapshotId();
    _idManager.assignSnapshot(snapshotName, networkId, snapshotId);
    QuestionId questionId = _idManager.getQuestionId(questionName, networkId).get();
    AnswerId baseAnswerId =
        _idManager.getAnswerId(
            networkId, snapshotId, questionId, DEFAULT_NETWORK_NODE_ROLES_ID, null);
    Answer answer = new Answer();
    answer.addAnswerElement(new TableAnswerElement(MOCK_TABLE_METADATA));
    AnswerMetadata answerMetadata =
        AnswerMetadataUtil.computeAnswerMetadata(answer, Main.getLogger());
    _storage.storeAnswerMetadata(networkId, snapshotId, answerMetadata, baseAnswerId);
    String answerStr = BatfishObjectMapper.writeString(answer);
    _storage.storeAnswer(networkId, snapshotId, answerStr, baseAnswerId);
    // remove answer metadata
    _storage.deleteAnswerMetadata(networkId, snapshotId, baseAnswerId);

    assertThat(
        _manager.getAnswerMetadata(networkName, snapshotName, questionName, null),
        equalTo(AnswerMetadata.forStatus(AnswerStatus.NOTFOUND)));
  }

  @Test
  public void testGetAnswerMetadataAdHocMissingQuestion() throws IOException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    Question question = new TestQuestion();
    String questionContent = BatfishObjectMapper.writeString(question);
    String questionName = "question2Name";
    _manager.initNetwork(networkName, null);
    _manager.uploadQuestion(networkName, questionName, questionContent, false);
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId = _idManager.generateSnapshotId();
    _idManager.assignSnapshot(snapshotName, networkId, snapshotId);
    QuestionId questionId = _idManager.getQuestionId(questionName, networkId).get();
    AnswerId answerId =
        _idManager.getAnswerId(
            networkId, snapshotId, questionId, DEFAULT_NETWORK_NODE_ROLES_ID, null);
    Answer answer = new Answer();
    answer.addAnswerElement(new TableAnswerElement(MOCK_TABLE_METADATA));
    AnswerMetadata answerMetadata =
        AnswerMetadataUtil.computeAnswerMetadata(answer, Main.getLogger());
    _storage.storeAnswerMetadata(networkId, snapshotId, answerMetadata, answerId);
    String answerStr = BatfishObjectMapper.writeString(answer);
    _storage.storeAnswer(networkId, snapshotId, answerStr, answerId);
    // remove question
    _idManager.deleteQuestion(questionName, networkId);

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(containsString(questionName));
    _manager.getAnswerMetadata(networkName, snapshotName, questionName, null);
  }

  @Test
  public void testGenerateDateString() {
    assertThat(
        generateFileDateString("foo", Instant.parse("2018-04-19T12:34:56Z")),
        equalTo("foo_2018-04-19T12-34-56.000"));

    assertThat(
        generateFileDateString(
            "foo",
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .parse("2018-04-19T12:34:56-08:00")
                .query(Instant::from)),
        equalTo("foo_2018-04-19T20-34-56.000"));
  }

  @Test
  public void testInitSnapshot() {
    String networkName = "network";
    String snapshotName = "snapshotName";
    String fileName = "file.type";
    String fileContents = "! empty config";

    _manager.initNetwork(networkName, null);

    // Create snapshot dir to pass into init
    Path srcDir = createSnapshot(snapshotName, fileName, fileContents, _folder);

    _manager.initSnapshot(networkName, snapshotName, srcDir, Instant.now());

    // Confirm the new snapshot exists
    assertThat(_manager.getLatestSnapshot(networkName), equalTo(Optional.of(snapshotName)));
  }

  /**
   * We handle runtime_data.json at the snapshot root directory as well as in its preferred location
   * under batfish/.
   */
  @Test
  public void testInitSnapshot_interfaceBlacklistAndRuntimeData_oldLocation()
      throws JsonProcessingException {
    /*
    Interface runtime data setup for two devices n1 and n2:
    - i1 is down on both
    - i2 is up on both
    - i3 is unspecified on both
    Interface blacklist setup: Everything on n1 is blacklisted
     */
    String n1 = "n1";
    String n2 = "n2";
    String i1 = "i1";
    String i2 = "i2";
    String i3 = "i3";
    RuntimeData deviceData =
        RuntimeData.builder().setInterfaceLineUp(i1, false).setInterfaceLineUp(i2, true).build();
    SnapshotRuntimeData runtimeData =
        SnapshotRuntimeData.builder()
            .setRuntimeData(ImmutableMap.of(n1, deviceData, n2, deviceData))
            .build();
    Set<NodeInterfacePair> blacklist =
        ImmutableSet.of(
            NodeInterfacePair.of(n1, i1),
            NodeInterfacePair.of(n1, i2),
            NodeInterfacePair.of(n1, i3));

    /*
    Expected runtime data after init:
    - All blacklisted interfaces (n1:i1, n1:i2, n1:i3) should be down
    - n2:i1 should be line down because runtime data says so
    - n2:i2 should be line up because runtime data says so
    - n2:i3 should not be in runtime data because no info was given about it (will interpret as up)
     */
    Set<NodeInterfacePair> expectedDown =
        Sets.union(blacklist, ImmutableSet.of(NodeInterfacePair.of(n2, i1)));
    Set<NodeInterfacePair> expectedUp = ImmutableSet.of(NodeInterfacePair.of(n2, i2));

    // Create snapshot
    String networkName = "network";
    String snapshotName = "snapshotName";
    Path srcDir = createSnapshot(snapshotName, "file.type", "! empty config", _folder);
    Path snapshotDir = srcDir.resolve(snapshotName);
    CommonUtil.writeFile(
        snapshotDir.resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        BatfishObjectMapper.writePrettyString(runtimeData));
    CommonUtil.writeFile(
        snapshotDir.resolve(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE),
        BatfishObjectMapper.writePrettyString(blacklist));
    _manager.initNetwork(networkName, null);
    _manager.initSnapshot(networkName, snapshotName, srcDir, Instant.now());

    // Interface blacklist should not exist in new snapshot
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshotName, networkId).get();
    assertNull(_storage.loadInterfaceBlacklist(networkId, snapshotId));
    assertThat(
        _storage.loadRuntimeData(networkId, snapshotId),
        equalTo(
            SnapshotRuntimeData.builder()
                .setInterfacesLineDown(expectedDown)
                .setInterfacesLineUp(expectedUp)
                .build()));
  }

  @Test
  public void testInitSnapshot_interfaceBlacklistAndRuntimeData() throws JsonProcessingException {
    /*
    Interface runtime data setup for two devices n1 and n2:
    - i1 is down on both
    - i2 is up on both
    - i3 is unspecified on both
    Interface blacklist setup: Everything on n1 is blacklisted
     */
    String n1 = "n1";
    String n2 = "n2";
    String i1 = "i1";
    String i2 = "i2";
    String i3 = "i3";
    RuntimeData deviceData =
        RuntimeData.builder().setInterfaceLineUp(i1, false).setInterfaceLineUp(i2, true).build();
    SnapshotRuntimeData runtimeData =
        SnapshotRuntimeData.builder()
            .setRuntimeData(ImmutableMap.of(n1, deviceData, n2, deviceData))
            .build();
    Set<NodeInterfacePair> blacklist =
        ImmutableSet.of(
            NodeInterfacePair.of(n1, i1),
            NodeInterfacePair.of(n1, i2),
            NodeInterfacePair.of(n1, i3));

    /*
    Expected runtime data after init:
    - All blacklisted interfaces (n1:i1, n1:i2, n1:i3) should be down
    - n2:i1 should be line down because runtime data says so
    - n2:i2 should be line up because runtime data says so
    - n2:i3 should not be in runtime data because no info was given about it (will interpret as up)
     */
    Set<NodeInterfacePair> expectedDown =
        Sets.union(blacklist, ImmutableSet.of(NodeInterfacePair.of(n2, i1)));
    Set<NodeInterfacePair> expectedUp = ImmutableSet.of(NodeInterfacePair.of(n2, i2));

    // Create snapshot
    String networkName = "network";
    String snapshotName = "snapshotName";
    Path srcDir = createSnapshot(snapshotName, "file.type", "! empty config", _folder);
    Path snapshotDir = srcDir.resolve(snapshotName);
    Path batfishDir = snapshotDir.resolve(BfConsts.RELPATH_BATFISH);
    batfishDir.toFile().mkdir();
    CommonUtil.writeFile(
        batfishDir.resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        BatfishObjectMapper.writePrettyString(runtimeData));
    CommonUtil.writeFile(
        snapshotDir.resolve(BfConsts.RELPATH_INTERFACE_BLACKLIST_FILE),
        BatfishObjectMapper.writePrettyString(blacklist));
    _manager.initNetwork(networkName, null);
    _manager.initSnapshot(networkName, snapshotName, srcDir, Instant.now());

    // Interface blacklist should not exist in new snapshot
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshotName, networkId).get();
    assertNull(_storage.loadInterfaceBlacklist(networkId, snapshotId));
    assertThat(
        _storage.loadRuntimeData(networkId, snapshotId),
        equalTo(
            SnapshotRuntimeData.builder()
                .setInterfacesLineDown(expectedDown)
                .setInterfacesLineUp(expectedUp)
                .build()));
  }

  /**
   * If runtime_data.json exists at the root snapshot directory and under batfish/ discard the
   * former.
   */
  @Test
  public void testInitSnapshot_runtimeData_bothLocations() throws JsonProcessingException {
    String n1 = "n1";
    String i1 = "i1";
    String i2 = "i2";
    SnapshotRuntimeData outerRuntimeData =
        SnapshotRuntimeData.builder()
            .setRuntimeData(
                ImmutableMap.of(n1, RuntimeData.builder().setInterfaceLineUp(i1, false).build()))
            .build();
    SnapshotRuntimeData innerRuntimeData =
        SnapshotRuntimeData.builder()
            .setRuntimeData(
                ImmutableMap.of(n1, RuntimeData.builder().setInterfaceLineUp(i2, false).build()))
            .build();

    // Create snapshot
    String networkName = "network";
    String snapshotName = "snapshotName";
    Path srcDir = createSnapshot(snapshotName, "file.type", "! empty config", _folder);
    Path snapshotDir = srcDir.resolve(snapshotName);
    CommonUtil.writeFile(
        snapshotDir.resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        BatfishObjectMapper.writePrettyString(outerRuntimeData));
    Path batfishDir = snapshotDir.resolve(BfConsts.RELPATH_BATFISH);
    batfishDir.toFile().mkdir();
    CommonUtil.writeFile(
        batfishDir.resolve(BfConsts.RELPATH_RUNTIME_DATA_FILE),
        BatfishObjectMapper.writePrettyString(innerRuntimeData));
    _manager.initNetwork(networkName, null);
    _manager.initSnapshot(networkName, snapshotName, srcDir, Instant.now());

    // Interface blacklist should not exist in new snapshot
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId = _idManager.getSnapshotId(snapshotName, networkId).get();
    assertNull(_storage.loadInterfaceBlacklist(networkId, snapshotId));
    assertThat(_storage.loadRuntimeData(networkId, snapshotId), equalTo(innerRuntimeData));
  }

  @Test
  public void testInitSnapshotBadPackaging() {
    String networkName = "network";
    String snapshotName = "snapshotName";
    String fileName = "file.type";
    String fileContents = "! empty config";

    _manager.initNetwork(networkName, null);

    // Create snapshot dir to pass into init
    Path srcDir = createSnapshot(snapshotName, fileName, fileContents, _folder);

    // Init should fail with improperly formatted source dir
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(containsString("Unexpected packaging of snapshot"));

    // Pass in path to subdir so init sees improperly formatted dir
    // i.e. sees dir containing 'configs/' instead of 'snapshotName/configs/'
    _manager.initSnapshot(networkName, snapshotName, srcDir.resolve(snapshotName), Instant.now());
  }

  @Test
  public void testInitSnapshotWithCiscoAciConfigsDirOnly() {
    String networkName = "network";
    String snapshotName = "snapshotName";

    _manager.initNetwork(networkName, null);

    Path srcDir = _folder.getRoot().toPath().resolve(snapshotName);
    Path ciscoAciConfig =
        srcDir
            .resolve(snapshotName)
            .resolve(BfConsts.RELPATH_CISCO_ACI_CONFIGS_DIR)
            .resolve("fabric.json");
    ciscoAciConfig.getParent().toFile().mkdirs();
    CommonUtil.writeFile(ciscoAciConfig, "{\"polUni\": {\"children\": []}}");

    _manager.initSnapshot(networkName, snapshotName, srcDir, Instant.now());

    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    assertThat(_idManager.getSnapshotId(snapshotName, networkId).isPresent(), equalTo(true));
  }

  @Test
  public void testProcessAnswerRows() throws IOException {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.ISSUE, "foobar"))));
    table.addRow(Row.of(columnName, new Issue("blah", 5, new Issue.Type("m", "n"))));
    Answer answer = new Answer();
    answer.addAnswerElement(table);
    answer.setStatus(AnswerStatus.SUCCESS);
    String answerStr = BatfishObjectMapper.writePrettyString(answer);
    AnswerRowsOptions options =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);

    List<Row> processedRows =
        ((TableAnswerElement)
                _manager.processAnswerRows(answerStr, options).getAnswerElements().get(0))
            .getRowsList();

    assertThat(processedRows, equalTo(table.getRowsList()));
  }

  @Test
  public void testProcessAnswerRows2() throws IOException {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.ISSUE, "foobar"))));
    table.addRow(Row.of(columnName, new Issue("blah", 5, new Issue.Type("m", "n"))));
    Answer answer = new Answer();
    answer.addAnswerElement(table);
    answer.setStatus(AnswerStatus.SUCCESS);
    String answerStr = BatfishObjectMapper.writePrettyString(answer);
    AnswerRowsOptions options =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);

    List<Row> processedRows =
        ((TableView) _manager.processAnswerRows2(answerStr, options).getAnswerElements().get(0))
            .getInnerRows();

    assertThat(processedRows, equalTo(table.getRowsList()));
  }

  @Test
  public void testProcessAnswerRowsFailure() throws IOException {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    AnswerRowsOptions options =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);
    Answer badInput = new Answer();
    badInput.setStatus(AnswerStatus.SUCCESS);
    String rawAnswerStr = BatfishObjectMapper.writePrettyString(badInput);
    Answer processedAnswer = _manager.processAnswerRows(rawAnswerStr, options);

    assertThat(processedAnswer.getStatus(), equalTo(AnswerStatus.FAILURE));
  }

  @Test
  public void testProcessAnswerRowsNotFound() {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    AnswerRowsOptions options =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);
    Answer processedAnswer = _manager.processAnswerRows(null, options);

    assertThat(processedAnswer.getStatus(), equalTo(AnswerStatus.NOTFOUND));
  }

  @Test
  public void testProcessAnswerRowsStale() throws IOException {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    AnswerRowsOptions options =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);
    Answer badInput = new Answer();
    badInput.setStatus(AnswerStatus.STALE);
    String rawAnswerStr = BatfishObjectMapper.writePrettyString(badInput);
    Answer processedAnswer = _manager.processAnswerRows(rawAnswerStr, options);

    assertThat(processedAnswer.getStatus(), equalTo(AnswerStatus.STALE));
  }

  @Test
  public void testProcessAnswerTableSorting() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 2);
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsSorting =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(new ColumnSortOption(columnName, false)),
            false);
    AnswerRowsOptions optionsSortingReverse =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);

    assertThat(
        _manager.processAnswerTable(table, optionsSorting).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnswerTable(table, optionsSortingReverse).getRowsList(),
        equalTo(ImmutableList.of(row2, row1)));
  }

  @Test
  public void testProcessAnswerTableOffset() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 2);
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNoOffset =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);
    AnswerRowsOptions optionsOffset =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            1,
            ImmutableList.of(),
            false);

    assertThat(
        _manager.processAnswerTable(table, optionsNoOffset).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnswerTable(table, optionsOffset).getRowsList(),
        equalTo(ImmutableList.of(row2)));
  }

  @Test
  public void testProcessAnswerTableFiltered() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.STRING, "foobar"))));
    String whitelistedValue = "hello";
    Row row1 = Row.of(columnName, whitelistedValue);
    Row row2 = Row.of(columnName, "goodbye");
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNotFiltered =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnswerRowsOptions optionsFiltered =
        new AnswerRowsOptions(
            ImmutableSet.of(),
            ImmutableList.of(new ColumnFilter(columnName, whitelistedValue, false)),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    TableAnswerElement notFiltered = _manager.processAnswerTable(table, optionsNotFiltered);
    TableAnswerElement filtered = _manager.processAnswerTable(table, optionsFiltered);

    assertThat(notFiltered.getRowsList(), equalTo(ImmutableList.of(row1, row2)));
    assertThat(filtered.getRowsList(), equalTo(ImmutableList.of(row1)));

    assertThat(notFiltered.getSummary().getNumResults(), equalTo(2));
    assertThat(filtered.getSummary().getNumResults(), equalTo(1));
  }

  /** Test that you can filter by a value column. */
  @Test
  public void testProcessAnswerTable2FilteredValue() {
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(
                    new ColumnMetadata("key", Schema.STRING, "the key column", true, false),
                    new ColumnMetadata("value", Schema.STRING, "the value column", false, true))));
    Row row1 = Row.of("key", "key1", "value", "value1");
    Row row2 = Row.of("key", "key2", "value", "value2");
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNotFiltered =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnswerRowsOptions optionsFiltered =
        new AnswerRowsOptions(
            ImmutableSet.of("value"), // project onto the value column
            ImmutableList.of(new ColumnFilter("value", "2", false)),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    TableView notFiltered = _manager.processAnswerTable2(table, optionsNotFiltered);
    TableView filtered = _manager.processAnswerTable2(table, optionsFiltered);

    assertThat(notFiltered.getInnerRows(), equalTo(ImmutableList.of(row1, row2)));
    assertThat(filtered.getInnerRows(), equalTo(ImmutableList.of(Row.of("value", "value2"))));

    assertThat(notFiltered.getSummary().getNumResults(), equalTo(2));
    assertThat(filtered.getSummary().getNumResults(), equalTo(1));
  }

  @Test
  public void testProcessAnswerTable2Filtered() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.STRING, "foobar"))));
    String whitelistedValue = "hello";
    Row row1 = Row.of(columnName, whitelistedValue);
    Row row2 = Row.of(columnName, "goodbye");
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNotFiltered =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnswerRowsOptions optionsFiltered =
        new AnswerRowsOptions(
            ImmutableSet.of(),
            ImmutableList.of(new ColumnFilter(columnName, whitelistedValue, false)),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    TableView notFiltered = _manager.processAnswerTable2(table, optionsNotFiltered);
    TableView filtered = _manager.processAnswerTable2(table, optionsFiltered);

    assertThat(notFiltered.getInnerRows(), equalTo(ImmutableList.of(row1, row2)));
    assertThat(filtered.getInnerRows(), equalTo(ImmutableList.of(row1)));

    assertThat(notFiltered.getSummary().getNumResults(), equalTo(2));
    assertThat(filtered.getSummary().getNumResults(), equalTo(1));
  }

  @Test
  public void testProcessAnswerTableMaxRows() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 2);
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNoLimit =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnswerRowsOptions optionsLimit =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), 1, 0, ImmutableList.of(), false);

    assertThat(
        _manager.processAnswerTable(table, optionsNoLimit).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnswerTable(table, optionsLimit).getRowsList(),
        equalTo(ImmutableList.of(row1)));
  }

  @Test
  public void testProcessAnswerTableProject() {
    String columnName = "val";
    String otherColumnName = "val2";
    TableMetadata originalMetadata =
        new TableMetadata(
            ImmutableList.of(
                new ColumnMetadata(columnName, Schema.INTEGER, "foobar"),
                new ColumnMetadata(otherColumnName, Schema.INTEGER, "foobaz")));
    TableAnswerElement table = new TableAnswerElement(originalMetadata);
    Row row1 = Row.of(columnName, 1, otherColumnName, 3);
    Row row2 = Row.of(columnName, 2, otherColumnName, 4);
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNoProject =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnswerRowsOptions optionsProject =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    Row row1Projected = Row.of(columnName, 1);
    Row row2Projected = Row.of(columnName, 2);

    assertThat(
        _manager.processAnswerTable(table, optionsNoProject).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));

    List<Row> projectedRows = _manager.processAnswerTable(table, optionsProject).getRowsList();

    assertThat(projectedRows, equalTo(ImmutableList.of(row1Projected, row2Projected)));
  }

  @Test
  public void testProcessAnswerTable2Project() {
    String columnName = "val";
    String otherColumnName = "val2";
    TableMetadata originalMetadata =
        new TableMetadata(
            ImmutableList.of(
                new ColumnMetadata(columnName, Schema.INTEGER, "foobar"),
                new ColumnMetadata(otherColumnName, Schema.INTEGER, "foobaz")));
    TableAnswerElement table = new TableAnswerElement(originalMetadata);
    Row row1 = Row.of(columnName, 1, otherColumnName, 3);
    Row row2 = Row.of(columnName, 2, otherColumnName, 4);
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNoProject =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnswerRowsOptions optionsProject =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    TableViewRow row1Projected = new TableViewRow(0, Row.of(columnName, 1));
    TableViewRow row2Projected = new TableViewRow(1, Row.of(columnName, 2));

    assertThat(
        _manager.processAnswerTable(table, optionsNoProject).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));

    List<TableViewRow> projectedRows =
        _manager.processAnswerTable2(table, optionsProject).getRows();

    assertThat(projectedRows, equalTo(ImmutableList.of(row1Projected, row2Projected)));
    assertThat(projectedRows.get(0).getId(), equalTo(0));
    assertThat(projectedRows.get(1).getId(), equalTo(1));
  }

  @Test
  public void testProcessAnswerTable2ProjectWrongColumn() {
    TableMetadata metadata =
        new TableMetadata(
            ImmutableList.of(
                // Also tests that the columns are sorted in error message.
                new ColumnMetadata("zcol", Schema.INTEGER, "first column but lex last"),
                new ColumnMetadata("col", Schema.INTEGER, "last column but lex first")));
    TableAnswerElement table = new TableAnswerElement(metadata);
    AnswerRowsOptions optionsProject =
        new AnswerRowsOptions(
            ImmutableSet.of("missing"),
            ImmutableList.of(),
            Integer.MAX_VALUE,
            0,
            ImmutableList.of(),
            false);

    _thrown.expectMessage("Column missing is not in the answer: [col, zcol]");
    _thrown.expect(IllegalArgumentException.class);
    _manager.processAnswerTable2(table, optionsProject);
  }

  @Test
  public void testProcessAnswerTableUniqueRows() {
    String columnName = "val";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar"))));
    Row row1 = Row.of(columnName, 1);
    Row row2 = Row.of(columnName, 1);
    table.addRow(row1);
    table.addRow(row2);
    AnswerRowsOptions optionsNonUnique =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);
    AnswerRowsOptions optionsUnique =
        new AnswerRowsOptions(
            ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), true);

    assertThat(
        _manager.processAnswerTable(table, optionsNonUnique).getRowsList(),
        equalTo(ImmutableList.of(row1, row2)));
    assertThat(
        _manager.processAnswerTable(table, optionsUnique).getRowsList(),
        equalTo(ImmutableList.of(row1)));
  }

  @Test
  public void testBuildComparator() {
    String col1 = "col1";
    String col2 = "col2";
    Map<String, ColumnMetadata> rawColumnMap =
        ImmutableMap.of(
            col1,
            new ColumnMetadata(col1, Schema.INTEGER, "blah"),
            col2,
            new ColumnMetadata(col2, Schema.INTEGER, "bloop"));
    Comparator<Row> comCol1 =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col1, false)));
    Comparator<Row> comCol1Reversed =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col1, true)));
    Comparator<Row> comCol2 =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col2, false)));
    Comparator<Row> comCol2Reversed =
        _manager.buildComparator(rawColumnMap, ImmutableList.of(new ColumnSortOption(col2, true)));
    Comparator<Row> comCol1Then2 =
        _manager.buildComparator(
            rawColumnMap,
            ImmutableList.of(new ColumnSortOption(col1, false), new ColumnSortOption(col2, false)));
    Comparator<Row> comCol2Then1 =
        _manager.buildComparator(
            rawColumnMap,
            ImmutableList.of(new ColumnSortOption(col2, false), new ColumnSortOption(col1, false)));

    Row row1 = Row.of(col1, 1, col2, 10);
    Row row2 = Row.of(col1, 1, col2, 20);
    Row row3 = Row.of(col1, 2, col2, 10);
    Row row4 = Row.of(col1, 2, col2, 20);

    assertThat(comCol1.compare(row1, row2), equalTo(0));
    assertThat(comCol1.compare(row1, row3), equalTo(-1));
    assertThat(comCol1.compare(row1, row4), equalTo(-1));
    assertThat(comCol1.compare(row2, row3), equalTo(-1));
    assertThat(comCol1.compare(row2, row4), equalTo(-1));
    assertThat(comCol1.compare(row3, row4), equalTo(0));

    assertThat(comCol1Reversed.compare(row1, row2), equalTo(0));
    assertThat(comCol1Reversed.compare(row1, row3), equalTo(1));
    assertThat(comCol1Reversed.compare(row1, row4), equalTo(1));
    assertThat(comCol1Reversed.compare(row2, row3), equalTo(1));
    assertThat(comCol1Reversed.compare(row2, row4), equalTo(1));
    assertThat(comCol1Reversed.compare(row3, row4), equalTo(0));

    assertThat(comCol2.compare(row1, row2), equalTo(-1));
    assertThat(comCol2.compare(row1, row3), equalTo(0));
    assertThat(comCol2.compare(row1, row4), equalTo(-1));
    assertThat(comCol2.compare(row2, row3), equalTo(1));
    assertThat(comCol2.compare(row2, row4), equalTo(0));
    assertThat(comCol2.compare(row3, row4), equalTo(-1));

    assertThat(comCol2Reversed.compare(row1, row2), equalTo(1));
    assertThat(comCol2Reversed.compare(row1, row3), equalTo(0));
    assertThat(comCol2Reversed.compare(row1, row4), equalTo(1));
    assertThat(comCol2Reversed.compare(row2, row3), equalTo(-1));
    assertThat(comCol2Reversed.compare(row2, row4), equalTo(0));
    assertThat(comCol2Reversed.compare(row3, row4), equalTo(1));

    assertThat(comCol1Then2.compare(row1, row2), equalTo(-1));
    assertThat(comCol1Then2.compare(row1, row3), equalTo(-1));
    assertThat(comCol1Then2.compare(row1, row4), equalTo(-1));
    assertThat(comCol1Then2.compare(row2, row3), equalTo(-1));
    assertThat(comCol1Then2.compare(row2, row4), equalTo(-1));
    assertThat(comCol1Then2.compare(row3, row4), equalTo(-1));

    assertThat(comCol2Then1.compare(row1, row2), equalTo(-1));
    assertThat(comCol2Then1.compare(row1, row3), equalTo(-1));
    assertThat(comCol2Then1.compare(row1, row4), equalTo(-1));
    assertThat(comCol2Then1.compare(row2, row3), equalTo(1));
    assertThat(comCol2Then1.compare(row2, row4), equalTo(-1));
    assertThat(comCol2Then1.compare(row3, row4), equalTo(-1));
  }

  @Test
  public void testColumnComparator() {
    String colInteger = "colInteger";
    String colIssue = "colIssue";
    String colString = "colString";

    ColumnMetadata columnMetadataInteger =
        new ColumnMetadata(colInteger, Schema.INTEGER, "colIntegerDesc");
    ColumnMetadata columnMetadataIssue = new ColumnMetadata(colIssue, Schema.ISSUE, "colIssueDesc");
    ColumnMetadata columnMetadataString =
        new ColumnMetadata(colString, Schema.STRING, "colStringDesc");

    Comparator<Row> comInteger = _manager.columnComparator(columnMetadataInteger);
    Comparator<Row> comIssue = _manager.columnComparator(columnMetadataIssue);
    Comparator<Row> comString = _manager.columnComparator(columnMetadataString);

    Row r1 =
        Row.of(
            colInteger,
            1,
            colIssue,
            new Issue("blah", 1, new Issue.Type("major", "minor")),
            colString,
            "a");
    Row r2 =
        Row.of(
            colInteger,
            2,
            colIssue,
            new Issue("blah", 2, new Issue.Type("major", "minor")),
            colString,
            "b");

    assertThat(comInteger.compare(r1, r2), lessThan(0));
    assertThat(comIssue.compare(r1, r2), lessThan(0));
    assertThat(comString.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorBoolean() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.BOOLEAN, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, false);
    Row r2 = Row.of(col, true);

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorFlow() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.FLOW, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, Flow.builder().setDstIp(Ip.ZERO).setIngressNode("a").build());
    Row r2 = Row.of(col, Flow.builder().setDstIp(Ip.MAX).setIngressNode("a").build());

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorInteger() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.INTEGER, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, 0);
    Row r2 = Row.of(col, 1);

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorInterface() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.INTERFACE, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, NodeInterfacePair.of("a", "a"));
    Row r2 = Row.of(col, NodeInterfacePair.of("a", "b"));
    Row r3 = Row.of(col, NodeInterfacePair.of("b", "a"));

    assertThat(comparator.compare(r1, r2), lessThan(0));
    assertThat(comparator.compare(r1, r3), lessThan(0));
    assertThat(comparator.compare(r2, r3), lessThan(0));
  }

  @Test
  public void testColumnComparatorIp() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.IP, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, Ip.ZERO);
    Row r2 = Row.of(col, Ip.MAX);

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorIssue() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.ISSUE, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, new Issue("a", 1, new Issue.Type("major", "minor")));
    Row r2 = Row.of(col, new Issue("a", 2, new Issue.Type("major", "minor")));
    Row r3 = Row.of(col, new Issue("b", 1, new Issue.Type("major", "minor")));

    assertThat(comparator.compare(r1, r2), lessThan(0));
    assertThat(comparator.compare(r1, r3), equalTo(0));
    assertThat(comparator.compare(r2, r3), greaterThan(0));
  }

  @Test
  public void testColumnComparatorList() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.list(Schema.STRING), "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, ImmutableList.of());
    Row r2 = Row.of(col, ImmutableList.of("a"));
    Row r3 = Row.of(col, ImmutableList.of("a", "b"));
    Row r4 = Row.of(col, ImmutableList.of("b"));
    Row r5 = Row.of(col, ImmutableList.of("b", "a"));

    assertThat(comparator.compare(r1, r2), lessThan(0));
    assertThat(comparator.compare(r1, r3), lessThan(0));
    assertThat(comparator.compare(r1, r4), lessThan(0));
    assertThat(comparator.compare(r1, r5), lessThan(0));
    assertThat(comparator.compare(r2, r3), lessThan(0));
    assertThat(comparator.compare(r2, r4), lessThan(0));
    assertThat(comparator.compare(r2, r5), lessThan(0));
    assertThat(comparator.compare(r3, r4), lessThan(0));
    assertThat(comparator.compare(r3, r5), lessThan(0));
    assertThat(comparator.compare(r4, r5), lessThan(0));
  }

  @Test
  public void testColumnComparatorLong() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.LONG, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, 0L);
    Row r2 = Row.of(col, 1L);
    Row r3 = Row.of(col, Long.MAX_VALUE);

    assertThat(comparator.compare(r1, r2), lessThan(0));
    assertThat(comparator.compare(r1, r3), lessThan(0));
    assertThat(comparator.compare(r2, r3), lessThan(0));
  }

  @Test
  public void testColumnComparatorNode() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.NODE, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, new Node("a"));
    Row r2 = Row.of(col, new Node("b"));

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorPrefix() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.PREFIX, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, Prefix.parse("1.1.2.1/32"));
    Row r2 = Row.of(col, Prefix.parse("1.1.11.1/32"));

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorSet() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.set(Schema.STRING), "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r0 = Row.of(col, null);
    Row r1 = Row.of(col, ImmutableSet.of());
    Row r2 = Row.of(col, ImmutableSet.of("a"));
    Row r3 = Row.of(col, ImmutableSet.of("b"));
    Row r4 = Row.of(col, ImmutableSet.of("a", "b"));
    Row r5 = Row.of(col, ImmutableSet.of("b", "a"));
    Row r6 = Row.of(col, Collections.singleton(null));

    assertThat(comparator.compare(r0, r1), lessThan(0));
    assertThat(comparator.compare(r1, r2), lessThan(0));
    assertThat(comparator.compare(r1, r3), lessThan(0));
    assertThat(comparator.compare(r1, r4), lessThan(0));
    assertThat(comparator.compare(r1, r5), lessThan(0));
    assertThat(comparator.compare(r2, r3), lessThan(0));
    assertThat(comparator.compare(r2, r4), not(equalTo(0)));
    assertThat(comparator.compare(r2, r5), not(equalTo(0)));
    assertThat(comparator.compare(r6, r2), lessThan(0));
    assertThat(comparator.compare(r3, r4), not(equalTo(0)));
    assertThat(comparator.compare(r3, r5), not(equalTo(0)));
    // sets in r4 and r5 might end up in same order, so no guarantee on comparison order
  }

  @Test
  public void testColumnComparatorString() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.STRING, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, "a");
    Row r2 = Row.of(col, "b");

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorSelfDescribingObject() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.SELF_DESCRIBING, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, new SelfDescribingObject(Schema.STRING, "a"));
    Row r2 = Row.of(col, new SelfDescribingObject(Schema.STRING, "b"));

    assertThat(comparator.compare(r1, r2), lessThan(0));
  }

  @Test
  public void testColumnComparatorTrace() {
    String col = "col1";
    ColumnMetadata columnMetadata = new ColumnMetadata(col, Schema.TRACE, "colDesc");
    Comparator<Row> comparator = _manager.columnComparator(columnMetadata);
    Row r1 = Row.of(col, new Trace(FlowDisposition.ACCEPTED, ImmutableList.of()));
    Row r2 =
        Row.of(
            col,
            new Trace(
                FlowDisposition.ACCEPTED,
                ImmutableList.of(new Hop(new Node("a"), ImmutableList.of(LoopStep.INSTANCE)))));
    Row r3 = Row.of(col, new Trace(FlowDisposition.DELIVERED_TO_SUBNET, ImmutableList.of()));

    assertThat(comparator.compare(r1, r2), lessThan(0));
    assertThat(comparator.compare(r1, r3), lessThan(0));
    assertThat(comparator.compare(r2, r3), lessThan(0));
  }

  @Test
  public void testDeleteSnapshotAbsent() {
    String network = "network1";
    String snapshot = "snapshot1";
    _manager.initNetwork(network, null);

    // should not be able to delete non-existent snapshot
    assertFalse(_manager.delSnapshot(network, snapshot));
  }

  @Test
  public void testDeleteSnapshotPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);

    // snapshot should exist
    assertThat(_manager.listSnapshots(network), contains(snapshot));

    _manager.delSnapshot(network, snapshot);

    // snapshot should no longer exist
    assertThat(_manager.listSnapshots(network), emptyIterable());
  }

  @Test
  public void testDeleteNetworkAbsent() {
    String network = "network1";

    // deletion should do no work
    assertFalse(_manager.delNetwork(network));
  }

  @Test
  public void testDeleteNetworkPresent() {
    String network = "network1";
    _manager.initNetwork(network, null);

    // network should exist
    assertThat(_manager.getNetworkNames(), contains(network));

    // deletion should be successful
    assertTrue(_manager.delNetwork(network));

    // network  should not exist
    assertThat(_manager.getNetworkNames(), emptyIterable());
  }

  @Test
  public void testGetNetworkNames() {
    String network = "network1";

    // should start out empty
    assertThat(_manager.getNetworkNames(), emptyIterable());

    _manager.initNetwork(network, null);

    // should contain initialized network
    assertThat(_manager.getNetworkNames(), contains(network));
  }

  @Test
  public void testGetNetworkNodeRolesEmpty() throws IOException {
    String network = "network1";
    _manager.initNetwork(network, null);

    // should return empty node roles
    assertThat(_manager.getNetworkNodeRoles(network), equalTo(NodeRolesData.builder().build()));
  }

  @Test
  public void testGetNetworkNodeRolesPresent() throws IOException {
    String network = "network1";
    String node = "node1";
    _manager.initNetwork(network, null);
    NodeRolesId networkNodeRolesId = new NodeRolesId("nr");
    NodeRolesData networkNodeRoles =
        NodeRolesData.builder()
            .setRoleDimensions(
                ImmutableList.of(
                    NodeRoleDimension.builder()
                        .setName("dim1")
                        .setRoleDimensionMappings(
                            ImmutableList.of(
                                new RoleDimensionMapping(
                                    "(" + node + ")", null, ImmutableMap.of(node, "role1"))))
                        .build()))
            .build();

    NetworkId networkId = _idManager.getNetworkId(network).get();
    _manager.getStorage().storeNodeRoles(networkId, networkNodeRoles, networkNodeRolesId);
    _idManager.assignNetworkNodeRolesId(networkId, networkNodeRolesId);

    // inferred roles for first snapshot should have been set network-wide
    assertThat(_manager.getNetworkNodeRoles(network), equalTo(networkNodeRoles));
  }

  @Test
  public void testUploadSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    _manager.initNetwork(network, null);
    assertTrue(uploadTestSnapshot(network, snapshot));

    // snapshot should exist
    assertThat(_manager.listSnapshots(network), contains(snapshot));

    // should be able to delete and recreate
    _manager.delSnapshot(network, snapshot);
    assertTrue(uploadTestSnapshot(network, snapshot));

    // snapshot should exist again
    assertThat(_manager.listSnapshots(network), contains(snapshot));

    // should not be able to upload again with same name
    assertFalse(uploadTestSnapshot(network, snapshot));
  }

  private boolean uploadTestSnapshot(String network, String snapshot) throws IOException {
    return WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);
  }

  private void uploadTestSnapshot(String network, String snapshot, String fileName)
      throws IOException {
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, fileName, _folder);
  }

  private void uploadTestSnapshot(String network, String snapshot, String fileName, String content)
      throws IOException {
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, fileName, content, _folder);
  }

  @Test
  public void testComputeWorkDetailsAdHocQuestion() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    String question = "question1";
    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);
    _manager.uploadQuestion(network, question, BatfishObjectMapper.writeString(new TestQuestion()));
    WorkItem workItem =
        new WorkItem(
            UUID.randomUUID(),
            network,
            snapshot,
            ImmutableMap.of(BfConsts.COMMAND_ANSWER, "", BfConsts.ARG_QUESTION_NAME, question));
    WorkDetails workDetails = _manager.computeWorkDetails(workItem);

    assertThat(
        workDetails.getSnapshotId(),
        equalTo(_idManager.getSnapshotId(snapshot, _idManager.getNetworkId(network).get()).get()));
    assertThat(workDetails.getWorkType(), equalTo(WorkType.PARSING_DEPENDENT_ANSWERING));
  }

  @Test
  public void testGetAnswerStringNotFoundAfterNodeRolesUpdate() throws IOException {
    String networkName = "network1";
    String snapshotName = "snapshot1";
    Question question = new TestQuestion();
    String questionContent = BatfishObjectMapper.writeString(question);
    String questionName = "question2Name";
    _manager.initNetwork(networkName, null);
    _manager.uploadQuestion(networkName, questionName, questionContent, false);
    NetworkId networkId = _idManager.getNetworkId(networkName).get();
    SnapshotId snapshotId = _idManager.generateSnapshotId();
    _idManager.assignSnapshot(snapshotName, networkId, snapshotId);
    QuestionId questionId = _idManager.getQuestionId(questionName, networkId).get();
    AnswerId baseAnswerId =
        _idManager.getAnswerId(
            networkId, snapshotId, questionId, DEFAULT_NETWORK_NODE_ROLES_ID, null);
    Answer answer = new Answer();
    answer.setStatus(AnswerStatus.SUCCESS);
    answer.addAnswerElement(new TableAnswerElement(MOCK_TABLE_METADATA));
    AnswerMetadata answerMetadata =
        AnswerMetadataUtil.computeAnswerMetadata(answer, Main.getLogger());
    _storage.storeAnswerMetadata(networkId, snapshotId, answerMetadata, baseAnswerId);
    String answerStr = BatfishObjectMapper.writeString(answer);
    _storage.storeAnswer(networkId, snapshotId, answerStr, baseAnswerId);
    Answer answerBeforeUpdate =
        BatfishObjectMapper.mapper()
            .readValue(
                _manager.getAnswerString(networkName, snapshotName, questionName, null),
                Answer.class);

    // answer should be found at first
    assertThat(answerBeforeUpdate.getStatus(), equalTo(AnswerStatus.SUCCESS));

    boolean updated =
        _manager.putNetworkNodeRoles(
            NodeRolesData.builder()
                .setRoleDimensions(
                    ImmutableList.of(NodeRoleDimension.builder().setName("foo").build()))
                .build(),
            networkName);

    // updating node roles should succeed
    assertTrue(updated);

    Answer answerAfterUpdate =
        BatfishObjectMapper.mapper()
            .readValue(
                _manager.getAnswerString(networkName, snapshotName, questionName, null),
                Answer.class);

    // answer should no longer be available since node roles input id changed
    assertThat(answerAfterUpdate.getStatus(), equalTo(AnswerStatus.NOTFOUND));
  }

  @Test
  public void testListSnapshotsWithMetadataMissingNetwork() throws IOException {
    String network = "network1";

    assertThat(Main.getWorkMgr().listSnapshotsWithMetadata(network), nullValue());
  }

  @Test
  public void testListSnapshotsWithMetadataSuccess() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);

    assertThat(
        Main.getWorkMgr().listSnapshotsWithMetadata(network),
        equalTo(
            ImmutableList.of(
                new SnapshotMetadataEntry(
                    snapshot, Main.getWorkMgr().getSnapshotMetadata(network, snapshot)))));
  }

  @Test
  public void testGetSnapshotSubdirInvalid() {
    Path root = _folder.getRoot().toPath();
    Path s1Path = root.resolve("s1");
    s1Path.toFile().mkdirs();
    Path s2Path = root.resolve("s2");
    s2Path.toFile().mkdirs();
    CommonUtil.writeFile(s1Path.resolve("file1"), "content");
    CommonUtil.writeFile(s2Path.resolve("file2"), "content");

    // invalid because there are two top-level dirs
    _thrown.expect(BatfishException.class);
    WorkMgr.getSnapshotSubdir(root);
  }

  @Test
  public void testGetSnapshotSubdirValid() {
    Path root = _folder.getRoot().toPath();
    Path s1Path = root.resolve("s1");
    s1Path.toFile().mkdirs();
    Path s2Path = root.resolve("__MACOSX"); // ignored dir
    s2Path.toFile().mkdirs();
    CommonUtil.writeFile(s1Path.resolve("file1"), "content");
    CommonUtil.writeFile(s2Path.resolve("file2"), "content");

    // extra dir should be ignored, and s1Path should be considered the snapshot subdir
    assertThat(WorkMgr.getSnapshotSubdir(root), equalTo(s1Path));
  }

  @Test
  public void testGetSnapshotInputKeysInvalidNetwork() throws IOException {
    assertThat(_manager.getSnapshotInputObjectsMetadata("network", "snapshot"), nullValue());
  }

  @Test
  public void testGetSnapshotInputKeysInvalidSnapshot() throws IOException {
    String network = "network";
    NetworkId networkId = _idManager.generateNetworkId();
    _idManager.assignNetwork(network, networkId);
    assertThat(_manager.getSnapshotInputObjectsMetadata(network, "snapshot"), nullValue());
  }

  @Test
  public void testGetSnapshotInputKeysIOException() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    NetworkId networkId = _idManager.generateNetworkId();
    SnapshotId snapshotId = _idManager.generateSnapshotId();

    _idManager.assignNetwork(network, networkId);
    _idManager.assignSnapshot(snapshot, networkId, snapshotId);

    _thrown.expect(IOException.class);
    _manager.getSnapshotInputObjectsMetadata(network, snapshot);
  }

  @Test
  public void testGetSnapshotInputKeys() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String fileName = "fileName";
    String content = "some content";

    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot, fileName, content);

    assertThat(
        _manager.getSnapshotInputObjectsMetadata(network, snapshot),
        equalTo(
            ImmutableList.of(
                new StoredObjectMetadata("configs/" + fileName, content.getBytes().length))));
  }

  @Test
  public void testGetSnapshotExtendedObjectsMetadataInvalidNetwork() throws IOException {
    assertThat(_manager.getSnapshotExtendedObjectsMetadata("network", "snapshot"), nullValue());
  }

  @Test
  public void testGetSnapshotExtendedObjectsMetadataInvalidSnapshot() throws IOException {
    String network = "network";
    NetworkId networkId = _idManager.generateNetworkId();
    _idManager.assignNetwork(network, networkId);
    assertThat(_manager.getSnapshotExtendedObjectsMetadata(network, "snapshot"), nullValue());
  }

  @Test
  public void testGetSnapshotExtendedObjectsMetadataIOException() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    NetworkId networkId = _idManager.generateNetworkId();
    SnapshotId snapshotId = _idManager.generateSnapshotId();

    _idManager.assignNetwork(network, networkId);
    _idManager.assignSnapshot(snapshot, networkId, snapshotId);

    _thrown.expect(IOException.class);
    _manager.getSnapshotExtendedObjectsMetadata(network, snapshot);
  }

  @Test
  public void testGetSnapshotExtendedObjectsMetadata() throws IOException {
    String network = "network";
    String snapshot = "snapshot";
    String fileName = "fileName";
    String content = "some content";

    _manager.initNetwork(network, null);
    uploadTestSnapshot(network, snapshot);

    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    _manager.putSnapshotExtendedObject(inputStream, network, snapshot, fileName);

    assertThat(
        _manager.getSnapshotExtendedObjectsMetadata(network, snapshot),
        contains(new StoredObjectMetadata(fileName, content.getBytes().length)));
  }

  @Test
  public void testCheckQuestionExists() {
    String network = "network";
    String question = "question";

    // No network or question should result in questionExists being false
    assertFalse(_manager.checkQuestionExists(network, question));

    _manager.initNetwork(network, null);
    // No question should result in questionExists being false
    assertFalse(_manager.checkQuestionExists(network, question));

    NetworkId networkId = _idManager.getNetworkId(network).get();
    _idManager.assignQuestion(question, networkId, _idManager.generateQuestionId());
    // After creating both network and question, questionExists should be true
    assertTrue(_manager.checkQuestionExists(network, question));
  }

  @Test
  public void testUploadQuestionWithInvalidJson() throws IOException {
    String network = "network";
    String question = "question";
    String invalidQuestionJson = "{\"invalid\": \"json\", \"missing\": \"required fields\"}";

    _manager.initNetwork(network, null);

    _thrown.expect(BadRequestException.class);
    _thrown.expectMessage(containsString("Invalid question"));
    _manager.uploadQuestion(network, question, invalidQuestionJson);
  }

  @Test
  public void testFilterAnswer() {
    String columnName = "issue";
    int maxRows = 1;
    int rowOffset = 0;
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.ISSUE, "foobar"))));
    table.addRow(Row.of(columnName, new Issue("blah", 5, new Issue.Type("m", "n"))));
    Answer answer = new Answer();
    answer.addAnswerElement(table);
    answer.setStatus(AnswerStatus.SUCCESS);
    AnswerRowsOptions options =
        new AnswerRowsOptions(
            ImmutableSet.of(columnName),
            ImmutableList.of(),
            maxRows,
            rowOffset,
            ImmutableList.of(new ColumnSortOption(columnName, true)),
            false);

    List<Row> processedRows =
        ((TableView) _manager.filterAnswer(answer, options).getAnswerElements().get(0))
            .getInnerRows();

    // Confirm filter options were applied correctly
    assertThat(processedRows, equalTo(table.getRowsList()));
  }
}
