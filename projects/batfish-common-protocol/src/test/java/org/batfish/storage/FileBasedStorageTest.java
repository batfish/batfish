package org.batfish.storage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.storage.FileBasedStorage.GC_SKEW_ALLOWANCE;
import static org.batfish.storage.FileBasedStorage.ISP_CONFIGURATION_KEY;
import static org.batfish.storage.FileBasedStorage.getWorkLogPath;
import static org.batfish.storage.FileBasedStorage.keyInDir;
import static org.batfish.storage.FileBasedStorage.objectKeyToRelativePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.autocomplete.IpCompletionMetadata;
import org.batfish.common.autocomplete.LocationCompletionMetadata;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.UnzipUtility;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MockDataPlane;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.MockForwardingAnalysis;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspFilter;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.vendor.ConversionContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class FileBasedStorageTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Path _containerDir;
  private BatfishLogger _logger;
  private FileBasedStorage _storage;

  @Before
  public void before() throws IOException {
    _containerDir = _folder.newFolder("container").toPath();
    _logger = new BatfishLogger(BatfishLogger.LEVELSTR_DEBUG, false);
    _storage =
        new FileBasedStorage(_containerDir.getParent(), _logger, (m, n) -> new AtomicInteger());
  }

  @Test
  public void roundTripConfigurationsSucceeds() throws IOException {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");

    Map<String, Configuration> configs = new HashMap<>();
    configs.put("node1", new Configuration("node1", ConfigurationFormat.CISCO_IOS));

    _storage.storeConfigurations(
        configs, new ConvertConfigurationAnswerElement(), Layer1Topology.EMPTY, network, snapshot);
    Map<String, Configuration> deserialized = _storage.loadConfigurations(network, snapshot);
    assertThat(deserialized, not(nullValue()));
    assertThat(deserialized.keySet(), equalTo(Sets.newHashSet("node1")));
  }

  @Test
  public void loadMissingConfigurationsReturnsNull() {
    assertThat(
        _storage.loadConfigurations(new NetworkId("nonexistent"), new SnapshotId("nonexistent")),
        nullValue());
  }

  @Test
  public void testStoreAndLoadConversionContext() throws IOException {
    NetworkSnapshot snapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));

    ConversionContext conversionContext = new ConversionContext();
    _storage.storeConversionContext(conversionContext, snapshot);

    ConversionContext loadedContext = _storage.loadConversionContext(snapshot);
    assertThat(loadedContext, instanceOf(ConversionContext.class));
  }

  @Test
  public void testLoadConversionContext_fileNotFound() throws IOException {
    _thrown.expect(FileNotFoundException.class);
    _storage.loadConversionContext(
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot")));
  }

  @Test
  public void testLoadConversionContext_deserializationFailure() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");

    String fooString = "foo"; // not a ConversionContext
    Path conversionContextPath = _storage.getConversionContextPath(networkId, snapshotId);
    _storage.serializeObject(fooString, conversionContextPath);

    _thrown.expect(IOException.class);
    _thrown.expectMessage(containsString("Failed to deserialize ConversionContext"));
    _storage.loadConversionContext(new NetworkSnapshot(networkId, snapshotId));
  }

  @Test
  public void testLoadIspConfiguration() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");

    IspConfiguration ispConfiguration =
        new IspConfiguration(
            ImmutableList.of(new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
            new IspFilter(
                ImmutableList.of(1L, 2L),
                ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"))));
    String ispConfigurationStr = BatfishObjectMapper.writeString(ispConfiguration);
    try (InputStream is = new ByteArrayInputStream(ispConfigurationStr.getBytes(UTF_8))) {
      _storage.storeSnapshotInputObject(
          is, ISP_CONFIGURATION_KEY, new NetworkSnapshot(networkId, snapshotId));
    }

    IspConfiguration readIspConfiguration = _storage.loadIspConfiguration(networkId, snapshotId);
    assertThat(ispConfiguration, equalTo(readIspConfiguration));
  }

  @Test
  public void testCheckNetworkExistsTrue() {
    NetworkId network = new NetworkId("network");
    _storage.getNetworkDir(network).toFile().mkdirs();

    assertThat(_storage.checkNetworkExists(network), equalTo(true));
  }

  @Test
  public void testCheckNetworkExistsFalse() {
    NetworkId network = new NetworkId("network");

    assertThat(_storage.checkNetworkExists(network), equalTo(false));
  }

  @Test
  public void testObjectKeyToRelativePathRejectsAbsolute() {
    _thrown.expect(IllegalArgumentException.class);
    objectKeyToRelativePath("/foo/bar");
  }

  @Test
  public void testObjectKeyToRelativePathRejectsNonNormalized() {
    _thrown.expect(IllegalArgumentException.class);
    objectKeyToRelativePath("foo/../../bar");
  }

  @Test
  public void testObjectKeyToRelativePathValid() {
    // no exception should be thrown
    objectKeyToRelativePath("foo/bar");
  }

  @Test
  public void testLoadWorkLog() throws IOException {
    // setup: pretend a worker logger has written a file
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");
    String workId = "workid";
    Path logFile = getWorkLogPath(_containerDir.getParent(), network, snapshot, workId);
    boolean mkdirs = logFile.getParent().toFile().mkdirs();
    assertThat(mkdirs, equalTo(true));
    CommonUtil.writeFile(logFile, "testoutput");

    // Test: read log using storage API
    assertThat(_storage.loadWorkLog(network, snapshot, workId), equalTo("testoutput"));
  }

  @Test
  public void testLoadWorkLogMissing() throws IOException {
    // setup: pretend a worker logger has written a file
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");

    _thrown.expect(FileNotFoundException.class);
    assertThat(_storage.loadWorkLog(network, snapshot, "workid"), equalTo("testoutput"));
  }

  @Test
  public void testValidatePath() {
    Path validPath = _containerDir.resolve("foo");

    _storage.validatePath(_containerDir);
    _storage.validatePath(validPath);

    Path invalidPath = Paths.get("/dev/null");
    _thrown.expect(IllegalArgumentException.class);
    _storage.validatePath(invalidPath);
  }

  @Test
  public void testMkdirs() throws IOException {
    Path dir = _containerDir.resolve("parentDir").resolve("subDir");

    // Confirm mkdirs creates the non-existent dir
    _storage.mkdirs(dir);
    assertThat(dir.toFile(), anExistingDirectory());
  }

  /**
   * Run multiple threads trying to create the same dir, many times. Goal here is for mkdirs to not
   * throw an exception.
   */
  @Test
  public void testMkdirsConcurrency() throws Exception {
    int numThreads = 2;
    // Try many times, since false negatives are possible
    int numTries = 100;

    Path dir = _containerDir.resolve("testDir");
    CyclicBarrier barrier = new CyclicBarrier(numThreads);
    AtomicInteger exceptions = new AtomicInteger(0);
    List<Thread> threads = new ArrayList<>();

    for (int i = 0; i < numTries; i++) {
      for (int j = 0; j < numThreads; j++) {
        Thread thread =
            new Thread(
                () -> {
                  try {
                    // Wait until all threads are at the barrier
                    barrier.await();
                    _storage.mkdirs(dir);
                  } catch (Exception e) {
                    // Track exceptions with int since they are not directly surfaced
                    exceptions.addAndGet(1);
                    throw new BatfishException(e.getMessage());
                  }
                });
        threads.add(thread);
        thread.start();
      }
      for (Thread thread : threads) {
        thread.join();
      }
      Files.delete(dir);
    }

    // Confirm mkdirs runs successfully even with multiple concurrent calls
    assertThat(exceptions.get(), equalTo(0));
  }

  @Test
  public void testMkdirsExists() throws IOException {
    Path dir = _containerDir;

    // Confirm mkdirs succeeds when the dir already exists
    _storage.mkdirs(dir);
    assertThat(dir.toFile(), anExistingDirectory());
  }

  @Test
  public void testMkdirsFail() throws IOException {
    File parentDir = _containerDir.toFile();
    parentDir.setReadOnly();
    Path dir = parentDir.toPath().resolve("testDir");

    // Confirm mkdirs throws when creating a dir within a read-only dir
    _thrown.expectMessage(containsString("Unable to create directory"));
    _storage.mkdirs(dir);
  }

  @Test
  public void testNetworkBlobRoundTrip() throws IOException {
    NetworkId network = new NetworkId("network");
    String id = "heresanid";

    Boolean found = null;
    try {
      _storage.loadNetworkBlob(network, id);
      found = true;
    } catch (FileNotFoundException e) {
      found = false;
    }
    assertThat("Should have been set", found, notNullValue());
    assertFalse("Should not have been found", found);

    byte[] content = "here's some content".getBytes(UTF_8);
    _storage.storeNetworkBlob(new ByteArrayInputStream(content), network, id);

    byte[] loaded = ByteStreams.toByteArray(_storage.loadNetworkBlob(network, id));
    assertThat(content, equalTo(loaded));
  }

  /**
   * Test that node roles are loaded from the legacy location if nothing is found in the primary
   * location
   */
  @Test
  public void testLoadNodeRoles_old() throws IOException {
    NetworkId networkId = new NetworkId("network");
    NodeRolesId nodeRolesId = new NodeRolesId("nodeRoles");
    String nodeRolesData = "stringData";

    Path nodeRolesPath = _storage.getOldNodeRolesPath(nodeRolesId);
    _storage.mkdirs(nodeRolesPath.getParent());
    _storage.writeStringToFile(nodeRolesPath, nodeRolesData, UTF_8);

    assertThat(_storage.loadNodeRoles(networkId, nodeRolesId), equalTo(nodeRolesData));

    _thrown.expect(FileNotFoundException.class);
    _storage.loadNodeRoles(networkId, new NodeRolesId("missing"));
  }

  /** Test that the response considers the old location of node roles */
  @Test
  public void testHasLoadNodeRoles_old() throws IOException {
    NetworkId networkId = new NetworkId("network");
    NodeRolesId nodeRolesId = new NodeRolesId("nodeRoles");
    String nodeRolesData = "stringData";

    Path nodeRolesPath = _storage.getOldNodeRolesPath(nodeRolesId);
    _storage.mkdirs(nodeRolesPath.getParent());
    _storage.writeStringToFile(nodeRolesPath, nodeRolesData, UTF_8);

    assertTrue(_storage.hasNodeRoles(networkId, nodeRolesId));
    assertFalse(_storage.hasNodeRoles(networkId, new NodeRolesId("missing")));
  }

  @Test
  public void testLoadSnapshotInputObjectFile() throws IOException {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");
    String testSting = "what is life";

    FileUtils.copyInputStreamToFile(
        new ByteArrayInputStream(testSting.getBytes()),
        _storage.getSnapshotInputObjectPath(network, snapshot, "test").toFile());

    try (InputStream inputStream = _storage.loadSnapshotInputObject(network, snapshot, "test")) {
      assertThat(IOUtils.toString(inputStream, UTF_8.name()), equalTo(testSting));
    }
  }

  @Test
  public void testLoadSnapshotInputObjectDirectory() throws IOException {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");
    String testSting = "this is life";

    Path testdir = _storage.getSnapshotInputObjectPath(network, snapshot, "testkey");
    testdir.toFile().mkdirs();
    Files.write(testdir.resolve("testfile"), testSting.getBytes());

    Path tmpzip = _folder.getRoot().toPath().resolve("tmp.zip");
    try (InputStream inputStream = _storage.loadSnapshotInputObject(network, snapshot, "testkey")) {
      FileUtils.copyInputStreamToFile(inputStream, tmpzip.toFile());
    }

    Path unzipDir = _folder.getRoot().toPath().resolve("tmp");
    unzipDir.toFile().mkdirs();
    UnzipUtility.unzip(tmpzip, unzipDir);

    // the top level entry in the zip should be testkey
    String[] toplevel = unzipDir.toFile().list();
    assertThat(toplevel, equalTo(new String[] {"testkey"}));

    // then, there should be testfile
    String[] secondlevel = unzipDir.resolve(toplevel[0]).toFile().list();
    assertThat(secondlevel, equalTo(new String[] {"testfile"}));

    // the content of the testfile should match what we wrote
    assertThat(
        new String(
            Files.readAllBytes(unzipDir.resolve(toplevel[0]).resolve(secondlevel[0])), UTF_8),
        equalTo(testSting));
  }

  @Test
  public void testGetSnapshotInputKeysNonExistentInput() throws IOException {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");
    _thrown.expect(FileNotFoundException.class);
    _storage.getSnapshotInputObjectsMetadata(network, snapshot);
  }

  @Test
  public void testGetSnapshotInputKeys() throws IOException {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");

    String dir1 = "dir1";
    String dir2 = "dir2";

    Path dir1Path = _storage.getSnapshotInputObjectPath(network, snapshot, dir1);
    dir1Path.toFile().mkdirs();

    Path dir2Path = _storage.getSnapshotInputObjectPath(network, snapshot, dir2);
    dir2Path.toFile().mkdirs();

    String file1 = "file1";
    String file1Contents = "some content";
    Files.write(dir1Path.resolve(file1), file1Contents.getBytes());

    String file2 = "file2";
    String file2Contents = "some other content";
    Files.write(dir2Path.resolve(file2), file2Contents.getBytes());

    List<StoredObjectMetadata> keys = _storage.getSnapshotInputObjectsMetadata(network, snapshot);
    assertThat(
        keys.stream().collect(ImmutableSet.toImmutableSet()),
        equalTo(
            ImmutableSet.of(
                new StoredObjectMetadata(dir2 + "/" + file2, file2Contents.getBytes().length),
                new StoredObjectMetadata(dir1 + "/" + file1, file1Contents.getBytes().length))));
  }

  @Test
  public void testGetSnapshotExtendedObjectsMetadataNonExistentInput() throws IOException {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");
    _thrown.expect(FileNotFoundException.class);
    _storage.getSnapshotExtendedObjectsMetadata(network, snapshot);
  }

  @Test
  public void testGetSnapshotExtendedObjectsMetadata() throws IOException {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");

    String key1 = "foo/bar";
    String key2 = "bat/fish";
    String content1 = "some content";
    String content2 = "some other content";

    InputStream inputStream1 = new ByteArrayInputStream(content1.getBytes());
    InputStream inputStream2 = new ByteArrayInputStream(content2.getBytes());

    _storage.storeSnapshotObject(inputStream1, network, snapshot, key1);
    _storage.storeSnapshotObject(inputStream2, network, snapshot, key2);

    List<StoredObjectMetadata> keys =
        _storage.getSnapshotExtendedObjectsMetadata(network, snapshot);
    assertThat(
        keys,
        containsInAnyOrder(
            new StoredObjectMetadata(key1, content1.getBytes().length),
            new StoredObjectMetadata(key2, content2.getBytes().length)));
  }

  /**
   * Test that the answer is loaded from the legacy location if nothing is found in the primary
   * location
   */
  @Test
  public void testLoadAnswer_old() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");
    AnswerId answerId = new AnswerId("answerId");
    String answerStr = "answerStr";

    Path answerPath = _storage.getOldAnswerPath(answerId);
    _storage.mkdirs(answerPath);
    _storage.writeStringToFile(answerPath, answerStr, UTF_8);

    assertThat(_storage.loadAnswer(networkId, snapshotId, answerId), equalTo(answerStr));

    _thrown.expect(FileNotFoundException.class);
    _storage.loadAnswer(networkId, snapshotId, new AnswerId("missing"));
  }

  /**
   * Test that the answer metadata is loaded from the legacy location if nothing is found in the
   * primary location
   */
  @Test
  public void testLoadAnswerMetadata_old() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");
    AnswerId answerId = new AnswerId("answerId");
    AnswerMetadata answerMetadata =
        AnswerMetadata.builder().setStatus(AnswerStatus.SUCCESS).build();

    Path answerMetadataPath = _storage.getOldAnswerMetadataPath(answerId);
    _storage.mkdirs(answerMetadataPath.getParent());
    _storage.writeStringToFile(
        answerMetadataPath, BatfishObjectMapper.writeString(answerMetadata), UTF_8);

    assertThat(
        _storage.loadAnswerMetadata(networkId, snapshotId, answerId), equalTo(answerMetadata));

    _thrown.expect(FileNotFoundException.class);
    _storage.loadAnswerMetadata(networkId, snapshotId, new AnswerId("missing"));
  }

  /**
   * Test that response considers the legacy location if nothing is found in the primary location of
   * the metadata
   */
  @Test
  public void testHasAnswerMetadata_old() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");
    AnswerId answerId = new AnswerId("answerId");
    AnswerMetadata answerMetadata =
        AnswerMetadata.builder().setStatus(AnswerStatus.SUCCESS).build();

    Path answerMetadataPath = _storage.getOldAnswerMetadataPath(answerId);
    _storage.mkdirs(answerMetadataPath.getParent());
    _storage.writeStringToFile(
        answerMetadataPath, BatfishObjectMapper.writeString(answerMetadata), UTF_8);

    assertTrue(_storage.hasAnswerMetadata(networkId, snapshotId, answerId));

    assertFalse(_storage.hasAnswerMetadata(networkId, snapshotId, new AnswerId("missing")));
  }

  @Test
  public void testCompletionMetadataRoundtrip() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");

    PrefixTrieMultiMap<IpCompletionMetadata> ipMetadata = new PrefixTrieMultiMap<>();
    ipMetadata.put(Ip.parse("1.1.1.1").toPrefix(), new IpCompletionMetadata());

    CompletionMetadata completionMetadata =
        new CompletionMetadata(
            ImmutableSet.of("filter1"),
            ImmutableSet.of(NodeInterfacePair.of("node", "iface")),
            ipMetadata,
            ImmutableSet.of(
                new LocationCompletionMetadata(new InterfaceLocation("node", "iface"), true)),
            ImmutableSet.of("mlag"),
            ImmutableMap.of("node", new NodeCompletionMetadata(null)),
            ImmutableSet.of("1.1.1.1/30"),
            ImmutableSet.of("routing1"),
            ImmutableSet.of("structure1"),
            ImmutableSet.of("vrf1"),
            ImmutableSet.of("zone1"));

    _storage.storeCompletionMetadata(completionMetadata, networkId, snapshotId);

    assertThat(_storage.loadCompletionMetadata(networkId, snapshotId), equalTo(completionMetadata));
  }

  @Test
  public void testLoadCompletionMetadataMissing() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");

    // if CompletionMetadata file is missing, should return a CompletionMetadata object with all
    // fields empty
    assertThat(
        _storage.loadCompletionMetadata(networkId, snapshotId), equalTo(CompletionMetadata.EMPTY));
  }

  @Test
  public void testStoreL3Adjacencies() throws IOException {
    NetworkSnapshot networkSnapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));

    try {
      _storage.loadL3Adjacencies(networkSnapshot);
      fail();
    } catch (BatfishException e) {
      assertThat(e.getMessage(), containsString("Failed to deserialize object"));
      assertThat(e.getCause(), instanceOf(FileNotFoundException.class));
    }

    _storage.storeL3Adjacencies(GlobalBroadcastNoPointToPoint.instance(), networkSnapshot);
    assertEquals(
        _storage.loadL3Adjacencies(networkSnapshot), GlobalBroadcastNoPointToPoint.instance());
  }

  @Test
  public void testStoreSynthesizedLayer1TopologyFileMissing() throws IOException {
    NetworkSnapshot networkSnapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));

    assertEquals(_storage.loadSynthesizedLayer1Topology(networkSnapshot), Optional.empty());
  }

  @Test
  public void testStoreSynthesizedLayer1TopologyPresent() throws IOException {
    NetworkSnapshot networkSnapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));
    _storage.storeSynthesizedLayer1Topology(
        Layer1Topology.EMPTY, networkSnapshot.getNetwork(), networkSnapshot.getSnapshot());

    assertEquals(
        _storage.loadSynthesizedLayer1Topology(networkSnapshot), Optional.of(Layer1Topology.EMPTY));
  }

  @Test
  public void testReadId() throws IOException {
    _storage.writeId(new NetworkId("network1_id"), "network1");
    assertThat(_storage.readId(NetworkId.class, "network1"), equalTo(Optional.of("network1_id")));

    assertThat(_storage.readId(NetworkId.class, "network2"), equalTo(Optional.empty()));
  }

  @Test
  public void testHasId() throws IOException {
    assertFalse(_storage.hasId(SnapshotId.class, "snapshot1"));
    assertFalse(_storage.hasId(SnapshotId.class, "snapshot1", new NetworkId("net1_id")));

    _storage.writeId(new SnapshotId("snapshot1_id"), "snapshot1");

    assertTrue(_storage.hasId(SnapshotId.class, "snapshot1"));
    assertFalse(_storage.hasId(SnapshotId.class, "snapshot1", new NetworkId("net1_id")));

    _storage.deleteNameIdMapping(SnapshotId.class, "snapshot1");

    assertFalse(_storage.hasId(SnapshotId.class, "snapshot1"));
  }

  @Test
  public void testListResolvableNames() throws IOException {
    _storage.writeId(new SnapshotId("snapshot1_id"), "snapshot1", new NetworkId("net1_id"));
    _storage.writeId(new SnapshotId("snapshot2_id"), "snapshot2", new NetworkId("net1_id"));

    // different ID type
    _storage.writeId(new QuestionId("question1_id"), "snapshot3", new NetworkId("net1_id"));

    assertThat(
        _storage.listResolvableNames(SnapshotId.class, new NetworkId("net1_id")),
        containsInAnyOrder("snapshot1", "snapshot2"));
  }

  /** Check that extant networks are not expunged even if their content is old */
  @Test
  public void testRunGarbageCollection_extantNetwork() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    _storage.writeId(networkId, "network"); // make the network extant

    // write an "old" blob
    Path blobPath = _storage.getNetworkBlobPath(networkId, "key");
    _storage.storeNetworkBlob(new ByteArrayInputStream("blob".getBytes()), networkId, "key");
    Instant oldTime = Instant.now().minus(GC_SKEW_ALLOWANCE).minus(1, ChronoUnit.MINUTES);
    Files.setLastModifiedTime(blobPath.getParent(), FileTime.from(oldTime));

    _storage.runGarbageCollection();
    assertTrue(Files.exists(_storage.getNetworkDir(networkId)));
  }

  /** Check that orphaned networks are expunged under the right conditions */
  @Test
  public void testRunGarbageCollection_orphanedNetwork() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    Path networkDir = _storage.getNetworkDir(networkId);

    // write a new blob
    Path blobPath = _storage.getNetworkBlobPath(networkId, "key");
    _storage.storeNetworkBlob(new ByteArrayInputStream("blob".getBytes()), networkId, "key");

    // should not be expunged since we have new data
    _storage.runGarbageCollection();
    assertTrue(Files.exists(networkDir));

    // make the blob old
    Instant oldTime = Instant.now().minus(GC_SKEW_ALLOWANCE).minus(1, ChronoUnit.MINUTES);
    Files.setLastModifiedTime(blobPath.getParent(), FileTime.from(oldTime));

    // should not be expunged since network dir is not old enough
    _storage.runGarbageCollection();
    assertTrue(Files.exists(networkDir));

    // make the network dir old
    Files.setLastModifiedTime(networkDir, FileTime.from(oldTime));

    _storage.runGarbageCollection();
    assertFalse(Files.exists(_storage.getNetworkDir(networkId)));
  }

  @Test
  public void testCanExpungeNetwork() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");

    Instant expungeTime = Instant.now();
    FileTime oldFileTime = FileTime.from(expungeTime.minus(1, ChronoUnit.MINUTES));
    FileTime newFileTime = FileTime.from(expungeTime.plus(1, ChronoUnit.MINUTES));

    // confirm behavior for blob
    _storage.mkdirs(_storage.getNetworkBlobsDir(networkId));
    Files.setLastModifiedTime(_storage.getNetworkDir(networkId), oldFileTime);
    Files.setLastModifiedTime(_storage.getNetworkBlobsDir(networkId), newFileTime);
    assertFalse(_storage.canExpungeNetwork(networkId, expungeTime));

    Files.setLastModifiedTime(_storage.getNetworkBlobsDir(networkId), oldFileTime);
    assertTrue(_storage.canExpungeNetwork(networkId, expungeTime));

    // confirm behavior for snapshots
    _storage.mkdirs(_storage.getAnswersDir(networkId, snapshotId));
    // mkdirs creates snapshots dir, which modifies network dir time
    Files.setLastModifiedTime(_storage.getNetworkDir(networkId), oldFileTime);
    Files.setLastModifiedTime(_storage.getSnapshotsDir(networkId), oldFileTime);
    Files.setLastModifiedTime(_storage.getAnswersDir(networkId, snapshotId), newFileTime);
    assertFalse(_storage.canExpungeNetwork(networkId, expungeTime));

    Files.setLastModifiedTime(_storage.getAnswersDir(networkId, snapshotId), oldFileTime);
    setSnapshotLastModifiedTime(networkId, snapshotId, oldFileTime);
    assertTrue(_storage.canExpungeNetwork(networkId, expungeTime));

    // confirm behavior for network dir
    Files.setLastModifiedTime(_storage.getNetworkDir(networkId), newFileTime);
    assertFalse(_storage.canExpungeNetwork(networkId, expungeTime));
    // final sanity check
    Files.setLastModifiedTime(_storage.getNetworkDir(networkId), oldFileTime);
    assertTrue(_storage.canExpungeNetwork(networkId, expungeTime));
  }

  private void setSnapshotLastModifiedTime(
      NetworkId networkId, SnapshotId snapshotId, FileTime time) throws IOException {
    Stream.of(
            _storage.getSnapshotDir(networkId, snapshotId),
            _storage.getSnapshotInputObjectsDir(networkId, snapshotId),
            _storage.getSnapshotOutputDir(networkId, snapshotId),
            _storage.getAnswersDir(networkId, snapshotId))
        .forEach(
            dir -> {
              if (Files.exists(dir)) {
                try {
                  Files.setLastModifiedTime(dir, time);
                } catch (IOException e) {
                  throw new RuntimeException(e.getMessage());
                }
              }
            });
  }

  /** Check that extant snapshots are not expunged even if their content is old */
  @Test
  public void testRunGarbageCollection_extantSnapshot() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");
    AnswerId answerId = new AnswerId("answer-id");

    _storage.writeId(networkId, "network"); // make the network extant
    _storage.writeId(snapshotId, "snapshot", networkId); // make the snapshot extant

    // write an answer and make the snapshot old
    Path answerDir = _storage.getAnswerDir(networkId, snapshotId, answerId);
    _storage.mkdirs(answerDir);
    _storage.writeStringToFile(answerDir.resolve("answer"), "answer", UTF_8);

    Instant oldTime = Instant.now().minus(GC_SKEW_ALLOWANCE).minus(1, ChronoUnit.MINUTES);
    setSnapshotLastModifiedTime(networkId, snapshotId, FileTime.from(oldTime));

    _storage.runGarbageCollection();
    assertTrue(Files.exists(_storage.getSnapshotDir(networkId, snapshotId)));
  }

  /** Check that orphaned snapshots are expunged under the right conditions */
  @Test
  public void testRunGarbageCollection_orphanedSnapshots() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");
    AnswerId answerId = new AnswerId("answer-id");
    Path snapshotDir = _storage.getSnapshotDir(networkId, snapshotId);

    _storage.writeId(networkId, "network"); // make the network extant

    // create a new answer object for the snapshot
    Path answerDir = _storage.getAnswerDir(networkId, snapshotId, answerId);
    _storage.mkdirs(answerDir);
    _storage.writeStringToFile(answerDir.resolve("answer"), "answer", UTF_8);

    // should not be expunged since we have new data
    _storage.runGarbageCollection();
    assertTrue(Files.exists(snapshotDir));

    // should be expunged if we make things old
    Instant oldTime = Instant.now().minus(GC_SKEW_ALLOWANCE).minus(1, ChronoUnit.MINUTES);
    setSnapshotLastModifiedTime(networkId, snapshotId, FileTime.from(oldTime));
    _storage.runGarbageCollection();
    assertFalse(Files.exists(snapshotDir));
  }

  /** Check that network blobs are expunged under the right conditions */
  @Test
  public void testRunGarbageCollection_blobs() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");
    AnswerId answerId = new AnswerId("answer-id");

    _storage.writeId(networkId, "network"); // make the network extant
    _storage.writeId(snapshotId, "snapshot", networkId); // make the snapshot extant

    // write an answer and make the snapshot too new to expunge
    Path answerDir = _storage.getAnswerDir(networkId, snapshotId, answerId);
    _storage.mkdirs(answerDir);
    _storage.writeStringToFile(answerDir.resolve("answer"), "answer", UTF_8);
    Instant snapshotTime = Instant.now();
    setSnapshotLastModifiedTime(networkId, snapshotId, FileTime.from(snapshotTime));
    _storage.storeSnapshotMetadata(new SnapshotMetadata(snapshotTime, null), networkId, snapshotId);

    // create blobs newer and older than the oldest snapshot
    _storage.storeNetworkBlob(new ByteArrayInputStream(new byte[] {}), networkId, "older");
    Files.setLastModifiedTime(
        _storage.getNetworkBlobPath(networkId, "older"),
        FileTime.from(snapshotTime.minus(GC_SKEW_ALLOWANCE).minus(1, ChronoUnit.MINUTES)));
    _storage.storeNetworkBlob(new ByteArrayInputStream(new byte[] {}), networkId, "newer");
    Files.setLastModifiedTime(
        _storage.getNetworkBlobPath(networkId, "newer"),
        FileTime.from(snapshotTime.plus(1, ChronoUnit.MINUTES)));

    _storage.runGarbageCollection();
    assertFalse(Files.exists(_storage.getNetworkBlobPath(networkId, "older")));
    assertTrue(Files.exists(_storage.getNetworkBlobPath(networkId, "newer")));
  }

  @Test
  public void testGetOldestSnapshotCreationTime() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId extantSnapshotId = new SnapshotId("snapshot-id");
    SnapshotId orphanedSnapshotId = new SnapshotId("orphaned-snapshot-id");
    AnswerId answerId = new AnswerId("answer-id");

    _storage.writeId(networkId, "network"); // make the network extant
    _storage.writeId(extantSnapshotId, "snapshot", networkId); // only for the extant snapshot

    // write an answer dir and date the extant snapshot
    Path extantAnswerDir = _storage.getAnswerDir(networkId, extantSnapshotId, answerId);
    _storage.mkdirs(extantAnswerDir);
    Instant extantSnapshotTime = Instant.now();
    setSnapshotLastModifiedTime(networkId, extantSnapshotId, FileTime.from(extantSnapshotTime));
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(extantSnapshotTime, null), networkId, extantSnapshotId);

    // write an answer dir and date the orphaned snapshot older than the extant snapshot
    Path orphanedAnswerDir = _storage.getAnswerDir(networkId, orphanedSnapshotId, answerId);
    _storage.mkdirs(orphanedAnswerDir);
    Instant orphanedSnapshotTime = extantSnapshotTime.minus(10, ChronoUnit.MINUTES);
    setSnapshotLastModifiedTime(networkId, orphanedSnapshotId, FileTime.from(orphanedSnapshotTime));
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(orphanedSnapshotTime, null), networkId, orphanedSnapshotId);

    // Should use extant snapshot time even though orphaned snapshot has older creation time
    assertThat(
        _storage.getOldestSnapshotCreationTime(networkId),
        equalTo(Optional.of(extantSnapshotTime)));
  }

  @Test
  public void testCanExpungeSnapshot() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");

    Instant expungeTime = Instant.now();
    FileTime oldFileTime = FileTime.from(expungeTime.minus(1, ChronoUnit.MINUTES));
    FileTime newFileTime = FileTime.from(expungeTime.plus(1, ChronoUnit.MINUTES));

    _storage.mkdirs(_storage.getSnapshotDir(networkId, snapshotId));
    _storage.mkdirs(_storage.getSnapshotInputObjectsDir(networkId, snapshotId));
    _storage.mkdirs(_storage.getSnapshotOutputDir(networkId, snapshotId));
    _storage.mkdirs(_storage.getAnswersDir(networkId, snapshotId));

    // when everything is old
    setSnapshotLastModifiedTime(networkId, snapshotId, oldFileTime);
    assertTrue(_storage.canExpungeSnapshot(networkId, snapshotId, expungeTime));

    // confirm behavior for snapshot dir itself
    Files.setLastModifiedTime(_storage.getSnapshotDir(networkId, snapshotId), newFileTime);
    assertFalse(_storage.canExpungeSnapshot(networkId, snapshotId, expungeTime));
    Files.setLastModifiedTime(_storage.getSnapshotDir(networkId, snapshotId), oldFileTime);

    // confirm behavior for input dir
    Files.setLastModifiedTime(
        _storage.getSnapshotInputObjectsDir(networkId, snapshotId), newFileTime);
    assertFalse(_storage.canExpungeSnapshot(networkId, snapshotId, expungeTime));
    Files.setLastModifiedTime(
        _storage.getSnapshotInputObjectsDir(networkId, snapshotId), oldFileTime);

    // confirm behavior for output dir
    Files.setLastModifiedTime(_storage.getSnapshotOutputDir(networkId, snapshotId), newFileTime);
    assertFalse(_storage.canExpungeSnapshot(networkId, snapshotId, expungeTime));
    Files.setLastModifiedTime(_storage.getSnapshotOutputDir(networkId, snapshotId), oldFileTime);

    // confirm behavior for answer dir
    Files.setLastModifiedTime(_storage.getAnswersDir(networkId, snapshotId), newFileTime);
    assertFalse(_storage.canExpungeSnapshot(networkId, snapshotId, expungeTime));
    Files.setLastModifiedTime(_storage.getAnswersDir(networkId, snapshotId), oldFileTime);
  }

  @Test
  public void testRunGarbageCollectionFreshStartup() throws IOException {
    // Should not throw
    _storage.runGarbageCollection();
  }

  @Test
  public void testListSnapshotInputObjectKeysMissingSnapshot() throws IOException {
    _thrown.expect(FileNotFoundException.class);
    try (Stream<String> keys =
        _storage.listSnapshotInputObjectKeys(
            new NetworkSnapshot(new NetworkId("n1"), new SnapshotId("s1")))) {
      // silence warning; shouldn't be hit because of expected exception
      assert keys != null;
    }
  }

  @Test
  public void testListSnapshotInputObjectKeysNoInputs() throws IOException {
    NetworkId networkId = new NetworkId("n1");
    SnapshotId snapshotId = new SnapshotId("s1");
    Files.createDirectories(_storage.getSnapshotInputObjectsDir(networkId, snapshotId).getParent());
    try (Stream<String> keys =
        _storage.listSnapshotInputObjectKeys(new NetworkSnapshot(networkId, snapshotId))) {
      assertThat(keys.count(), equalTo(0L));
    }
  }

  @Test
  public void testListSnapshotInputObjectKeys() throws IOException {
    NetworkId networkId = new NetworkId("n1");
    SnapshotId snapshotId = new SnapshotId("s1");
    NetworkSnapshot snapshot = new NetworkSnapshot(networkId, snapshotId);
    _storage.storeSnapshotInputObject(new ByteArrayInputStream(new byte[] {}), "k1", snapshot);
    _storage.storeSnapshotInputObject(new ByteArrayInputStream(new byte[] {}), "k2", snapshot);
    try (Stream<String> keys =
        _storage.listSnapshotInputObjectKeys(new NetworkSnapshot(networkId, snapshotId))) {
      assertThat(keys.collect(ImmutableSet.toImmutableSet()), equalTo(ImmutableSet.of("k1", "k2")));
    }
  }

  @Test
  public void testKeyInDir() {
    assertTrue(keyInDir("configs/rtr.cfg", "configs"));
    assertFalse(keyInDir("configs.ignore", "configs"));
  }

  /** Test that the dataplane serializes and deserializes successfully. */
  @Test
  public void testDataplane() throws IOException {
    NetworkSnapshot snapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));

    // Most fields are tested shallowly. However, since BGP and EVPN have two different fields where
    // a mixup would still be type-correct, actually test them with distinct values.
    Bgpv4Route bgp = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).build();
    Bgpv4Route bgpBackup = Bgpv4Route.testBuilder().setNetwork(Prefix.MULTICAST).build();
    EvpnType5Route evpn =
        EvpnType5Route.builder()
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .setVni(1)
            .build();
    EvpnType5Route evpnBackup = evpn.toBuilder().setNetwork(Prefix.MULTICAST).build();
    ForwardingAnalysis fa =
        MockForwardingAnalysis.builder()
            .setArpReplies(ImmutableMap.of("n", ImmutableMap.of("i", UniverseIpSpace.INSTANCE)))
            .build();

    DataPlane dp =
        MockDataPlane.builder()
            .setBgpRoutes(ImmutableTable.of("n", "v", ImmutableSet.of(bgp)))
            .setBgpBackupRoutes(ImmutableTable.of("n", "v", ImmutableSet.of(bgpBackup)))
            .setEvpnRoutes(ImmutableTable.of("n", "v", ImmutableSet.of(evpn)))
            .setEvpnBackupRoutes(ImmutableTable.of("n", "v", ImmutableSet.of(evpnBackup)))
            .setForwardingAnalysis(fa)
            .setFibs(ImmutableMap.of("n", ImmutableMap.of("v", MockFib.builder().build())))
            .setLayer2VniSettings(ImmutableTable.of("n", "v2", ImmutableSet.of()))
            .setLayer3VniSettings(ImmutableTable.of("n", "v3", ImmutableSet.of()))
            .setPrefixTracingInfoSummary(
                ImmutableSortedMap.of("n", ImmutableSortedMap.of("vp", ImmutableMap.of())))
            .setRibs(ImmutableTable.of("n", "vr", FinalMainRib.of()))
            .build();

    _storage.storeDataPlane(dp, snapshot);
    DataPlane dp2 = _storage.loadDataPlane(snapshot);
    assertThat(Iterables.getOnlyElement(dp2.getBgpRoutes().cellSet()).getValue(), contains(bgp));
    assertThat(
        Iterables.getOnlyElement(dp2.getBgpBackupRoutes().cellSet()).getValue(),
        contains(bgpBackup));
    assertThat(Iterables.getOnlyElement(dp2.getEvpnRoutes().cellSet()).getValue(), contains(evpn));
    assertThat(
        Iterables.getOnlyElement(dp2.getEvpnBackupRoutes().cellSet()).getValue(),
        contains(evpnBackup));
    assertThat(dp2.getFibs(), hasEntry(equalTo("n"), hasKey("v")));
    assertThat(
        dp2.getForwardingAnalysis().getArpReplies().get("n"),
        hasEntry("i", UniverseIpSpace.INSTANCE));
    assertThat(dp2.getLayer2Vnis().rowMap(), hasEntry(equalTo("n"), hasKey("v2")));
    assertThat(dp2.getLayer3Vnis().rowMap(), hasEntry(equalTo("n"), hasKey("v3")));
    assertThat(dp2.getPrefixTracingInfoSummary(), hasEntry(equalTo("n"), hasKey("vp")));
    assertThat(dp2.getRibs().rowMap(), hasEntry(equalTo("n"), hasKey("vr")));
  }
}
