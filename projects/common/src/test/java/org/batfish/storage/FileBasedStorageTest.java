package org.batfish.storage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.storage.FileBasedStorage.ISP_CONFIGURATION_KEY;
import static org.batfish.storage.FileBasedStorage.getWorkLogPath;
import static org.batfish.storage.FileBasedStorage.keyInDir;
import static org.batfish.storage.FileBasedStorage.objectKeyToRelativePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.InterfaceForwardingBehavior;
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
import org.batfish.datamodel.VrfForwardingBehavior;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspFilter;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.Id;
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

  /** Deleting a network puts its data out of reach at once; the trash reclaims it later. */
  @Test
  public void testDeleteNetwork() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    NetworkId otherNetworkId = new NetworkId("other-network-id");
    _storage.storeNetworkBlob(new ByteArrayInputStream("blob".getBytes()), networkId, "key");
    _storage.storeNetworkBlob(new ByteArrayInputStream("blob".getBytes()), otherNetworkId, "key");

    _storage.deleteNetwork(networkId);

    // unreachable before anything has been deleted
    assertFalse(Files.exists(_storage.getNetworkDir(networkId)));
    assertThat(listTrash(), hasSize(1));

    assertThat(_storage.drainTrash(), equalTo(0));
    assertThat(listTrash(), empty());
    assertTrue(Files.exists(_storage.getNetworkDir(otherNetworkId)));
  }

  @Test
  public void testDeleteNetworkAbsent() throws IOException {
    _storage.deleteNetwork(new NetworkId("network-id"));

    assertThat(listTrash(), empty());
  }

  @Test
  public void testDeleteSnapshot() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");
    SnapshotId otherSnapshotId = new SnapshotId("other-snapshot-id");
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(Instant.now(), null), networkId, snapshotId);
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(Instant.now(), null), networkId, otherSnapshotId);

    _storage.deleteSnapshot(new NetworkSnapshot(networkId, snapshotId));

    assertFalse(Files.exists(_storage.getSnapshotDir(networkId, snapshotId)));
    assertThat(_storage.drainTrash(), equalTo(0));
    assertThat(listTrash(), empty());
    assertTrue(Files.exists(_storage.getSnapshotDir(networkId, otherSnapshotId)));
  }

  /** Data that could not be moved into the trash is named by a tombstone and deleted in place. */
  @Test
  public void testDrainTrashTombstone() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    _storage.storeNetworkBlob(new ByteArrayInputStream("blob".getBytes()), networkId, "key");
    Path tombstone = _storage.getTrashDir().resolve("entry.path");
    _storage.mkdirs(_storage.getTrashDir());
    _storage.writeStringToFile(tombstone, _storage.getNetworkDir(networkId).toString(), UTF_8);

    assertThat(_storage.drainTrash(), equalTo(0));
    assertFalse(Files.exists(_storage.getNetworkDir(networkId)));
    assertFalse(Files.exists(tombstone));
  }

  /** A stray file in the trash is deleted rather than retried forever. */
  @Test
  public void testDrainTrashStrayFile() throws IOException {
    Path stray = _storage.getTrashDir().resolve("stray");
    _storage.mkdirs(_storage.getTrashDir());
    _storage.writeStringToFile(stray, "stray", UTF_8);

    assertThat(_storage.drainTrash(), equalTo(0));
    assertFalse(Files.exists(stray));
  }

  /** An entry that cannot be deleted is counted and left for the next pass. */
  @Test
  public void testDrainTrashKeepsFailures() throws IOException {
    Path tombstone = _storage.getTrashDir().resolve("entry.path");
    _storage.mkdirs(_storage.getTrashDir());
    // outside the base dir: refused rather than deleted
    _storage.writeStringToFile(tombstone, "/not-under-the-batfish-base-dir", UTF_8);

    assertThat(_storage.drainTrash(), equalTo(1));
    assertTrue(Files.exists(tombstone));
  }

  @Test
  public void testRecoverOrphansNetwork() throws IOException {
    NetworkId extantNetworkId = new NetworkId("network-id");
    NetworkId orphanedNetworkId = new NetworkId("orphaned-network-id");
    _storage.writeId(extantNetworkId, "network"); // make the network extant
    _storage.initNetwork(extantNetworkId);
    _storage.initNetwork(orphanedNetworkId);

    _storage.recoverOrphans();
    assertThat(_storage.drainTrash(), equalTo(0));

    assertTrue(Files.exists(_storage.getNetworkDir(extantNetworkId)));
    assertFalse(Files.exists(_storage.getNetworkDir(orphanedNetworkId)));
  }

  @Test
  public void testRecoverOrphansSnapshot() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId extantSnapshotId = new SnapshotId("snapshot-id");
    SnapshotId orphanedSnapshotId = new SnapshotId("orphaned-snapshot-id");
    _storage.writeId(networkId, "network"); // make the network extant
    _storage.writeId(extantSnapshotId, "snapshot", networkId); // only for the extant snapshot
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(Instant.now(), null), networkId, extantSnapshotId);
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(Instant.now(), null), networkId, orphanedSnapshotId);

    _storage.recoverOrphans();
    assertThat(_storage.drainTrash(), equalTo(0));

    assertTrue(Files.exists(_storage.getSnapshotDir(networkId, extantSnapshotId)));
    assertFalse(Files.exists(_storage.getSnapshotDir(networkId, orphanedSnapshotId)));
  }

  @Test
  public void testRecoverOrphansQuestion() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    QuestionId extantQuestionId = new QuestionId("question-id");
    QuestionId orphanedQuestionId = new QuestionId("orphaned-question-id");
    _storage.writeId(networkId, "network"); // make the network extant
    _storage.writeId(extantQuestionId, "question", networkId); // only for the extant question
    _storage.storeQuestion("{}", networkId, extantQuestionId);
    _storage.storeQuestion("{}", networkId, orphanedQuestionId);

    _storage.recoverOrphans();
    assertThat(_storage.drainTrash(), equalTo(0));

    assertTrue(_storage.checkQuestionExists(networkId, extantQuestionId));
    assertFalse(_storage.checkQuestionExists(networkId, orphanedQuestionId));
  }

  @Test
  public void testRecoverOrphansFreshStartup() throws IOException {
    // Should not throw
    _storage.recoverOrphans();
  }

  /** Maintenance reclaims deleted data without anything else prompting it. */
  @Test
  public void testMaintenanceReclaimsDeletedData() throws IOException, InterruptedException {
    NetworkId networkId = new NetworkId("network-id");
    _storage.writeId(networkId, "network"); // extant, so startup recovery leaves it alone
    _storage.storeNetworkBlob(new ByteArrayInputStream("blob".getBytes()), networkId, "key");

    _storage.startMaintenance();
    try {
      _storage.deleteNetwork(networkId);
      Instant deadline = Instant.now().plus(30, ChronoUnit.SECONDS);
      while (!listTrash().isEmpty() && Instant.now().isBefore(deadline)) {
        Thread.sleep(10);
      }
    } finally {
      _storage.stopMaintenance();
    }

    assertThat(listTrash(), empty());
  }

  /** Check that stale parse cache entries are evicted */
  @Test
  public void testEvictStaleBlobs() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");

    _storage.writeId(networkId, "network"); // make the network extant
    _storage.writeId(snapshotId, "snapshot", networkId); // make the snapshot extant
    Instant snapshotTime = Instant.now();
    _storage.storeSnapshotMetadata(new SnapshotMetadata(snapshotTime, null), networkId, snapshotId);

    // create blobs newer and older than the oldest snapshot
    _storage.storeNetworkBlob(new ByteArrayInputStream(new byte[] {}), networkId, "older");
    Files.setLastModifiedTime(
        _storage.getNetworkBlobPath(networkId, "older"),
        FileTime.from(snapshotTime.minus(1, ChronoUnit.MINUTES)));
    _storage.storeNetworkBlob(new ByteArrayInputStream(new byte[] {}), networkId, "newer");
    Files.setLastModifiedTime(
        _storage.getNetworkBlobPath(networkId, "newer"),
        FileTime.from(snapshotTime.plus(1, ChronoUnit.MINUTES)));

    _storage.evictStaleBlobs();

    assertFalse(Files.exists(_storage.getNetworkBlobPath(networkId, "older")));
    assertTrue(Files.exists(_storage.getNetworkBlobPath(networkId, "newer")));
  }

  /**
   * Deleting a network takes the mappings of everything inside it, which nothing else can reach.
   */
  @Test
  public void testDeleteNetworkDeletesItsIdMappings() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");
    _storage.writeId(networkId, "network");
    _storage.writeId(snapshotId, "snapshot", networkId);
    _storage.initNetwork(networkId);
    assertTrue(Files.exists(_storage.getNetworkIdsDir(networkId)));

    _storage.deleteNameIdMapping(NetworkId.class, "network");
    _storage.deleteNetwork(networkId);
    assertThat(_storage.drainTrash(), equalTo(0));

    assertFalse(Files.exists(_storage.getNetworkIdsDir(networkId)));
  }

  @Test
  public void testRecoverOrphansIdMappings() throws IOException {
    NetworkId extantNetworkId = new NetworkId("network-id");
    NetworkId orphanedNetworkId = new NetworkId("orphaned-network-id");
    _storage.writeId(extantNetworkId, "network"); // make the network extant
    _storage.writeId(new SnapshotId("snapshot-id"), "snapshot", extantNetworkId);
    _storage.writeId(new SnapshotId("orphaned-snapshot-id"), "snapshot", orphanedNetworkId);

    _storage.recoverOrphans();
    assertThat(_storage.drainTrash(), equalTo(0));

    assertTrue(Files.exists(_storage.getNetworkIdsDir(extantNetworkId)));
    assertFalse(Files.exists(_storage.getNetworkIdsDir(orphanedNetworkId)));
    // the extant network's own mapping is a sibling of those directories
    assertThat(_storage.readId(NetworkId.class, "network"), equalTo(Optional.of("network-id")));
  }

  /**
   * Dying between deleting a name-to-ID mapping and deleting the data it pointed at leaves data
   * that nothing can reach, which the next startup reclaims.
   */
  @Test
  public void testRecoverAfterCrashBeforeDeletingData() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId deletedSnapshotId = new SnapshotId("deleted-snapshot-id");
    SnapshotId liveSnapshotId = new SnapshotId("live-snapshot-id");
    _storage.writeId(networkId, "network");
    _storage.writeId(liveSnapshotId, "live", networkId);
    _storage.writeId(deletedSnapshotId, "deleted", networkId);
    storeSnapshot(networkId, liveSnapshotId);
    storeSnapshot(networkId, deletedSnapshotId);

    // the delete got as far as the mapping, and no further
    _storage.deleteNameIdMapping(SnapshotId.class, "deleted", networkId);
    assertTrue(Files.exists(_storage.getSnapshotDir(networkId, deletedSnapshotId)));

    restartStorage();

    assertFalse(Files.exists(_storage.getSnapshotDir(networkId, deletedSnapshotId)));
    assertThat(loadSnapshotInput(networkId, liveSnapshotId), equalTo("configs"));
  }

  /**
   * Dying part way through creating a snapshot leaves data that no mapping points at, even though
   * the snapshot never existed as far as any client saw.
   */
  @Test
  public void testRecoverAfterCrashDuringCreate() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId snapshotId = new SnapshotId("snapshot-id");
    _storage.writeId(networkId, "network");
    // inputs copied, but neither the metadata nor the mapping written yet
    _storage.storeSnapshotInputObject(
        new ByteArrayInputStream("configs".getBytes(UTF_8)),
        "configs/c.cfg",
        new NetworkSnapshot(networkId, snapshotId));

    restartStorage();

    assertFalse(Files.exists(_storage.getSnapshotDir(networkId, snapshotId)));
    assertTrue(Files.exists(_storage.getNetworkDir(networkId)));
  }

  /** Data already in the trash when the process died is reclaimed by the next startup. */
  @Test
  public void testRecoverDrainsTrashLeftByCrash() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    storeSnapshot(networkId, new SnapshotId("snapshot-id"));

    // deleted, but the process died before the maintenance thread emptied the trash
    _storage.deleteNetwork(networkId);
    assertThat(listTrash(), hasSize(1));

    restartStorage();

    assertThat(listTrash(), empty());
  }

  /**
   * Creation and deletion race each other and the maintenance thread. Data reachable through a
   * name-to-ID mapping must survive, and everything else must be reclaimed.
   */
  @Test
  public void testConcurrentCreateAndDeleteWithMaintenance() throws Exception {
    int numThreads = 4;
    int numOpsPerThread = 150;

    List<Thread> threads = new ArrayList<>();
    List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
    // Every network and snapshot that is currently reachable, by ID, with the name it is reachable
    // under. Threads share the map but only touch their own networks.
    Map<NetworkId, Map<SnapshotId, String>> live = new ConcurrentHashMap<>();
    CyclicBarrier barrier = new CyclicBarrier(numThreads);
    AtomicInteger networksCreated = new AtomicInteger();
    AtomicInteger networksDeleted = new AtomicInteger();
    AtomicInteger snapshotsDeleted = new AtomicInteger();

    _storage.startMaintenance();
    try {
      for (int t = 0; t < numThreads; t++) {
        String prefix = "t" + t;
        // Seeded, so that a failing interleaving has a chance of repeating.
        Random random = new Random(t);
        Thread thread =
            new Thread(
                () -> {
                  try {
                    List<String> mine = new ArrayList<>();
                    barrier.await();
                    for (int i = 0; i < numOpsPerThread; i++) {
                      switch (random.nextInt(4)) {
                        case 0 -> {
                          mine.add(createNetwork(prefix + "-" + i, live));
                          networksCreated.incrementAndGet();
                        }
                        case 1 ->
                            snapshotsDeleted.addAndGet(deleteSomeSnapshot(random, mine, live));
                        case 2 -> networksDeleted.addAndGet(deleteSomeNetwork(random, mine, live));
                        // Deleting data that is already gone must be harmless: two clients can
                        // delete the same network, and one of them loses the race.
                        default -> _storage.deleteNetwork(new NetworkId("never-existed"));
                      }
                    }
                  } catch (Throwable e) {
                    failures.add(e);
                  }
                });
        threads.add(thread);
        thread.start();
      }
      for (Thread thread : threads) {
        thread.join();
      }
    } finally {
      _storage.stopMaintenance();
    }
    assertThat(failures, empty());
    // The interleaving must have done all three things, or the invariants below are vacuous.
    assertThat(networksCreated.get(), greaterThan(0));
    assertThat(networksDeleted.get(), greaterThan(0));
    assertThat(snapshotsDeleted.get(), greaterThan(0));

    // Everything still reachable is intact, by name rather than by ID ...
    for (String network : _storage.listResolvableNames(NetworkId.class)) {
      NetworkId networkId = new NetworkId(_storage.readId(NetworkId.class, network).get());
      assertThat(live, hasKey(networkId));
      for (String snapshot : _storage.listResolvableNames(SnapshotId.class, networkId)) {
        SnapshotId snapshotId =
            new SnapshotId(_storage.readId(SnapshotId.class, snapshot, networkId).get());
        assertThat(live.get(networkId), hasKey(snapshotId));
        assertThat(loadSnapshotInput(networkId, snapshotId), equalTo("configs"));
      }
    }
    // ... nothing reachable was missed ...
    assertThat(_storage.listResolvableNames(NetworkId.class), hasSize(live.size()));
    // ... and nothing unreachable is left behind.
    assertThat(_storage.drainTrash(), equalTo(0));
    assertThat(listTrash(), empty());
    assertThat(unreferencedDirs(), empty());
  }

  /**
   * Creates a network with two snapshots, writing data before the mapping that makes it reachable,
   * as the coordinator does. Returns the network's name.
   */
  private String createNetwork(String name, Map<NetworkId, Map<SnapshotId, String>> live)
      throws IOException {
    NetworkId networkId = new NetworkId(name + "-id");
    _storage.initNetwork(networkId);
    Map<SnapshotId, String> snapshots = new ConcurrentHashMap<>();
    live.put(networkId, snapshots);
    _storage.writeId(networkId, name);
    for (int i = 0; i < 2; i++) {
      String snapshot = name + "-snapshot-" + i;
      SnapshotId snapshotId = new SnapshotId(snapshot + "-id");
      storeSnapshot(networkId, snapshotId);
      // give cache eviction something to walk
      _storage.storeNetworkBlob(
          new ByteArrayInputStream("blob".getBytes(UTF_8)), networkId, snapshotId.getId());
      snapshots.put(snapshotId, snapshot);
      _storage.writeId(snapshotId, snapshot, networkId);
    }
    return name;
  }

  /** Returns the number of snapshots deleted, which is zero if there was nothing to delete. */
  private int deleteSomeSnapshot(
      Random random, List<String> mine, Map<NetworkId, Map<SnapshotId, String>> live)
      throws IOException {
    String network = pick(random, mine);
    if (network == null) {
      return 0;
    }
    NetworkId networkId = new NetworkId(network + "-id");
    Map<SnapshotId, String> snapshots = live.get(networkId);
    if (snapshots == null) {
      return 0;
    }
    SnapshotId snapshotId = pick(random, ImmutableList.copyOf(snapshots.keySet()));
    if (snapshotId == null) {
      return 0;
    }
    String snapshot = snapshots.remove(snapshotId);
    if (snapshot == null) {
      return 0;
    }
    _storage.deleteNameIdMapping(SnapshotId.class, snapshot, networkId);
    _storage.deleteSnapshot(new NetworkSnapshot(networkId, snapshotId));
    return 1;
  }

  /** Returns the number of networks deleted, which is zero if there was nothing to delete. */
  private int deleteSomeNetwork(
      Random random, List<String> mine, Map<NetworkId, Map<SnapshotId, String>> live)
      throws IOException {
    String network = pick(random, mine);
    if (network == null) {
      return 0;
    }
    NetworkId networkId = new NetworkId(network + "-id");
    if (live.remove(networkId) == null) {
      return 0;
    }
    mine.remove(network);
    _storage.deleteNameIdMapping(NetworkId.class, network);
    _storage.deleteNetwork(networkId);
    return 1;
  }

  private static <T> @Nullable T pick(Random random, List<T> items) {
    return items.isEmpty() ? null : items.get(random.nextInt(items.size()));
  }

  /** Directories holding data or mappings that no name-to-ID mapping can reach. */
  private List<Path> unreferencedDirs() throws IOException {
    ImmutableList.Builder<Path> unreferenced = ImmutableList.builder();
    Set<String> extantNetworkIds = extantIds(NetworkId.class);
    for (Path networkDir : listDir(_storage.getNetworksDir())) {
      NetworkId networkId = new NetworkId(networkDir.getFileName().toString());
      if (!extantNetworkIds.contains(networkId.getId())) {
        unreferenced.add(networkDir);
        continue;
      }
      Set<String> extantSnapshotIds = extantIds(SnapshotId.class, networkId);
      listDir(_storage.getSnapshotsDir(networkId)).stream()
          .filter(dir -> !extantSnapshotIds.contains(dir.getFileName().toString()))
          .forEach(unreferenced::add);
    }
    // The per-network mapping directories live alongside the network name mappings.
    Path networkIdsDir = _storage.getNetworkIdsDir(new NetworkId("any")).getParent();
    listDir(networkIdsDir).stream()
        .filter(Files::isDirectory)
        .filter(dir -> !extantNetworkIds.contains(dir.getFileName().toString()))
        .forEach(unreferenced::add);
    return unreferenced.build();
  }

  private Set<String> extantIds(Class<? extends Id> type, Id... ancestors) throws IOException {
    ImmutableSet.Builder<String> ids = ImmutableSet.builder();
    for (String name : _storage.listResolvableNames(type, ancestors)) {
      _storage.readId(type, name, ancestors).ifPresent(ids::add);
    }
    return ids.build();
  }

  private static List<Path> listDir(Path dir) throws IOException {
    if (!Files.exists(dir)) {
      return ImmutableList.of();
    }
    try (Stream<Path> children = Files.list(dir)) {
      return children.collect(ImmutableList.toImmutableList());
    }
  }

  private void storeSnapshot(NetworkId networkId, SnapshotId snapshotId) throws IOException {
    _storage.storeSnapshotInputObject(
        new ByteArrayInputStream("configs".getBytes(UTF_8)),
        "configs/c.cfg",
        new NetworkSnapshot(networkId, snapshotId));
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(Instant.now(), null), networkId, snapshotId);
  }

  private String loadSnapshotInput(NetworkId networkId, SnapshotId snapshotId) throws IOException {
    try (InputStream in =
        _storage.loadSnapshotInputObject(networkId, snapshotId, "configs/c.cfg")) {
      return new String(ByteStreams.toByteArray(in), UTF_8);
    }
  }

  /** Replaces the storage with a new one on the same data, and runs its startup recovery. */
  private void restartStorage() throws IOException {
    _storage =
        new FileBasedStorage(_containerDir.getParent(), _logger, (m, n) -> new AtomicInteger());
    _storage.recoverOrphans();
    assertThat(_storage.drainTrash(), equalTo(0));
  }

  private List<Path> listTrash() throws IOException {
    if (!Files.exists(_storage.getTrashDir())) {
      return ImmutableList.of();
    }
    try (Stream<Path> entries = Files.list(_storage.getTrashDir())) {
      return entries.collect(ImmutableList.toImmutableList());
    }
  }

  @Test
  public void testGetOldestSnapshotCreationTime() throws IOException {
    NetworkId networkId = new NetworkId("network-id");
    SnapshotId extantSnapshotId = new SnapshotId("snapshot-id");
    SnapshotId orphanedSnapshotId = new SnapshotId("orphaned-snapshot-id");

    _storage.writeId(networkId, "network"); // make the network extant
    _storage.writeId(extantSnapshotId, "snapshot", networkId); // only for the extant snapshot

    Instant extantSnapshotTime = Instant.now();
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(extantSnapshotTime, null), networkId, extantSnapshotId);

    // date the orphaned snapshot older than the extant snapshot
    Instant orphanedSnapshotTime = extantSnapshotTime.minus(10, ChronoUnit.MINUTES);
    _storage.storeSnapshotMetadata(
        new SnapshotMetadata(orphanedSnapshotTime, null), networkId, orphanedSnapshotId);

    // Should use extant snapshot time even though orphaned snapshot has older creation time
    assertThat(
        _storage.getOldestSnapshotCreationTime(networkId),
        equalTo(Optional.of(extantSnapshotTime)));
  }

  @Test
  public void testListSnapshotInputObjectKeysMissingSnapshot() throws IOException {
    _thrown.expect(FileNotFoundException.class);
    try (Stream<String> keys =
        _storage.listSnapshotInputObjectKeys(
            new NetworkSnapshot(new NetworkId("n1"), new SnapshotId("s1")))) {
      // silence warning; shouldn't be hit because of expected exception
      assertThat(keys, notNullValue());
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
    VrfForwardingBehavior vfb =
        new VrfForwardingBehavior(
            ImmutableMap.of(),
            ImmutableMap.of(
                "i",
                new InterfaceForwardingBehavior(
                    Prefix.parse("10.0.0.0/8").toIpSpace(),
                    EmptyIpSpace.INSTANCE,
                    EmptyIpSpace.INSTANCE,
                    EmptyIpSpace.INSTANCE,
                    EmptyIpSpace.INSTANCE)),
            ImmutableMap.of(),
            EmptyIpSpace.INSTANCE,
            UniverseIpSpace.INSTANCE);
    ForwardingAnalysis fa =
        MockForwardingAnalysis.builder()
            .setArpReplies(ImmutableMap.of("n", ImmutableMap.of("i", UniverseIpSpace.INSTANCE)))
            .setVrfForwardingBehavior(ImmutableMap.of("n", ImmutableMap.of("v", vfb)))
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
    assertThat(dp2.getForwardingAnalysis().getArpReplies(), equalTo(fa.getArpReplies()));
    assertThat(
        dp2.getForwardingAnalysis().getVrfForwardingBehavior(),
        equalTo(fa.getVrfForwardingBehavior()));
    assertThat(dp2.getLayer2Vnis().rowMap(), hasEntry(equalTo("n"), hasKey("v2")));
    assertThat(dp2.getLayer3Vnis().rowMap(), hasEntry(equalTo("n"), hasKey("v3")));
    assertThat(dp2.getPrefixTracingInfoSummary(), hasEntry(equalTo("n"), hasKey("vp")));
    assertThat(dp2.getRibs().rowMap(), hasEntry(equalTo("n"), hasKey("vr")));
  }
}
