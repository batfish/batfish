package org.batfish.storage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.storage.FileBasedStorage.GC_SKEW_ALLOWANCE;
import static org.batfish.storage.FileBasedStorage.ISP_CONFIGURATION_KEY;
import static org.batfish.storage.FileBasedStorage.getWorkLogPath;
import static org.batfish.storage.FileBasedStorage.objectKeyToRelativePath;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
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
import javax.annotation.Nonnull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.autocomplete.IpCompletionMetadata;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.UnzipUtility;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.answers.MinorIssueConfig;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspFilter;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.specifier.InterfaceLocation;
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
  public void testMajorIssueConfigRoundTrip() throws IOException {
    String majorIssue = "majorIssue";
    IssueSettingsId issueSettingsId = new IssueSettingsId("issueSettingsId");
    NetworkId network = new NetworkId("network");
    MinorIssueConfig minorIssueConfig = new MinorIssueConfig("minorIssue", 100, "www.google.com");
    MajorIssueConfig majorIssueConfig =
        new MajorIssueConfig(majorIssue, ImmutableList.of(minorIssueConfig));

    _storage.storeMajorIssueConfig(network, issueSettingsId, majorIssueConfig);
    assertThat(_storage.loadMajorIssueConfig(network, issueSettingsId), equalTo(majorIssueConfig));
  }

  @Test
  public void testLoadMissingMajorIssueConfig() {
    IssueSettingsId majorIssue = new IssueSettingsId("majorIssue");
    NetworkId network = new NetworkId("network");
    assertThat(_storage.loadMajorIssueConfig(network, majorIssue), nullValue());
  }

  @Test
  public void testStoreQuestionSettingsThenLoad() throws IOException {
    NetworkId network = new NetworkId("network");
    QuestionSettingsId questionSettingsId = new QuestionSettingsId("q1");
    String settings = "{}";
    _storage.storeQuestionSettings(settings, network, questionSettingsId);

    assertThat(_storage.loadQuestionSettings(network, questionSettingsId), equalTo(settings));
  }

  @Test
  public void testLoadQuestionSettingsMissing() throws IOException {
    NetworkId network = new NetworkId("network");
    QuestionSettingsId questionSettingsId = new QuestionSettingsId("q1");

    assertThat(_storage.loadQuestionSettings(network, questionSettingsId), nullValue());
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
    final boolean mkdirs = logFile.getParent().toFile().mkdirs();
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

    final Path dir = _containerDir.resolve("testDir");
    final CyclicBarrier barrier = new CyclicBarrier(numThreads);
    final AtomicInteger exceptions = new AtomicInteger(0);
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

  @Test
  public void testCompletionMetadataRoundtrip() throws IOException {
    NetworkId networkId = new NetworkId("network");
    SnapshotId snapshotId = new SnapshotId("snapshot");

    CompletionMetadata completionMetadata =
        new CompletionMetadata(
            ImmutableSet.of("filter1"),
            ImmutableSet.of(NodeInterfacePair.of("node", "iface")),
            ImmutableMap.of(Ip.parse("1.1.1.1"), new IpCompletionMetadata()),
            ImmutableSet.of(new InterfaceLocation("node", "iface")),
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
  public void testStoreLayer2TopologyMissing() throws IOException {
    NetworkSnapshot networkSnapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));
    _storage.storeLayer2Topology(Optional.empty(), networkSnapshot);

    assertEquals(_storage.loadLayer2Topology(networkSnapshot), Optional.empty());
  }

  @Test
  public void testStoreLayer2TopologyPresent() throws IOException {
    NetworkSnapshot networkSnapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));
    _storage.storeLayer2Topology(Optional.of(Layer2Topology.EMPTY), networkSnapshot);

    assertEquals(_storage.loadLayer2Topology(networkSnapshot), Optional.of(Layer2Topology.EMPTY));
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

    // different ancestors
    _storage.writeId(
        new SnapshotId("snapshot1_id_other"),
        "snapshot3",
        new NetworkId("net1_id"),
        new AnalysisId("analysis1_id")); //

    // different ID type
    _storage.writeId(new QuestionId("question1_id"), "snapshot4", new NetworkId("net1_id"));

    assertThat(
        _storage.listResolvableNames(SnapshotId.class, new NetworkId("net1_id")),
        containsInAnyOrder("snapshot1", "snapshot2"));
  }

  private interface IOExceptionThrower {
    void run() throws IOException;
  }

  private static void expectFileNotFoundException(IOExceptionThrower r) {
    boolean thrown = false;
    try {
      r.run();
    } catch (FileNotFoundException e) {
      thrown = true;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    assertTrue("Expected FileNotFoundException", thrown);
  }

  @Test
  public void testExpungeOldEntriesDirectories() throws IOException {
    Instant newTime = Instant.now();
    Instant expungBeforeTime = newTime.minus(GC_SKEW_ALLOWANCE);
    Instant oldTime = expungBeforeTime.minus(1, ChronoUnit.MINUTES);

    Path newDir = _storage.getStorageBase().resolve("newDir");
    Path newFile = _storage.getStorageBase().resolve("newFile");
    Path oldDir = _storage.getStorageBase().resolve("oldDir");
    Path oldFile = _storage.getStorageBase().resolve("oldFile");

    _storage.mkdirs(_storage.getStorageBase());
    Files.createDirectory(_storage.getStorageBase().resolve("newDir"));
    Files.createFile(_storage.getStorageBase().resolve("newFile"));
    Files.createDirectory(_storage.getStorageBase().resolve("oldDir"));
    Files.createFile(oldFile);
    Files.setLastModifiedTime(newDir, FileTime.from(newTime));
    Files.setLastModifiedTime(newFile, FileTime.from(newTime));
    Files.setLastModifiedTime(oldDir, FileTime.from(oldTime));
    Files.setLastModifiedTime(oldFile, FileTime.from(oldTime));

    _storage.expungeOldEntries(expungBeforeTime, _storage.getStorageBase(), true);

    assertTrue(Files.exists(newDir));
    assertTrue(Files.exists(newFile));
    assertFalse(Files.exists(oldDir));
    assertTrue(Files.exists(oldFile));
  }

  @Test
  public void testExpungeOldEntriesFiles() throws IOException {
    Instant newTime = Instant.now();
    Instant expungBeforeTime = newTime.minus(GC_SKEW_ALLOWANCE);
    Instant oldTime = expungBeforeTime.minus(1, ChronoUnit.MINUTES);

    Path newDir = _storage.getStorageBase().resolve("newDir");
    Path newFile = _storage.getStorageBase().resolve("newFile");
    Path oldDir = _storage.getStorageBase().resolve("oldDir");
    Path oldFile = _storage.getStorageBase().resolve("oldFile");

    _storage.mkdirs(_storage.getStorageBase());
    Files.createDirectory(_storage.getStorageBase().resolve("newDir"));
    Files.createFile(_storage.getStorageBase().resolve("newFile"));
    Files.createDirectory(_storage.getStorageBase().resolve("oldDir"));
    Files.createFile(oldFile);
    Files.setLastModifiedTime(newDir, FileTime.from(newTime));
    Files.setLastModifiedTime(newFile, FileTime.from(newTime));
    Files.setLastModifiedTime(oldDir, FileTime.from(oldTime));
    Files.setLastModifiedTime(oldFile, FileTime.from(oldTime));

    _storage.expungeOldEntries(expungBeforeTime, _storage.getStorageBase(), false);

    assertTrue(Files.exists(newDir));
    assertTrue(Files.exists(newFile));
    assertTrue(Files.exists(oldDir));
    assertFalse(Files.exists(oldFile));
  }

  // doesn't realize close is explicitly called
  @SuppressWarnings({"MustBeClosedChecker", "PMD.CloseResource"})
  @Test
  public void testRunGarbageCollection() throws IOException {
    String network = "network1";
    String networkToDelete = "networkToDelete";
    String snapshotNew = "snapshotNew";
    String snapshotOld = "snapshotOld";
    String oldUploadKey = "oldUpload";
    String newUploadKey = "newUpload";

    NetworkId networkId = new NetworkId("network1-id");
    NetworkId networkToDeleteId = new NetworkId("networkToDelete-id");
    SnapshotId snapshotNewId = new SnapshotId("snapshotNew-id");
    SnapshotId snapshotOldId = new SnapshotId("snapshotOld-id");
    AnswerId oldAnswerId = new AnswerId("answerOld-id");
    AnswerId newAnswerId = new AnswerId("answerNew-id");

    Instant oldTime = Instant.now();
    Instant newTime = oldTime.plus(GC_SKEW_ALLOWANCE).plus(Duration.ofMinutes(1L));

    // mock modified times for test
    FileBasedStorage storage =
        new FileBasedStorage(_containerDir.getParent(), _logger, (m, n) -> new AtomicInteger()) {
          @Nonnull
          @Override
          Instant getLastModifiedTime(Path path) throws IOException {
            if (path.equals(getAnswerDir(oldAnswerId))
                || path.equals(getOriginalDir(oldUploadKey, networkId))
                || path.equals(getOriginalDir(oldUploadKey, networkToDeleteId))
                || path.startsWith(getSnapshotDir(networkId, snapshotOldId))
                || path.startsWith(getSnapshotDir(networkToDeleteId, snapshotOldId))) {
              return oldTime;
            } else if (path.equals(getAnswerDir(newAnswerId))
                || path.equals(getOriginalDir(newUploadKey, networkId))
                || path.startsWith(getSnapshotDir(networkId, snapshotNewId))) {
              return newTime;
            } else {
              throw new IllegalArgumentException(String.format("Unhandled path: %s", path));
            }
          }
        };

    storage.writeId(networkId, network);
    storage.writeId(networkToDeleteId, networkToDelete);

    // write old answer
    storage.storeAnswer("", oldAnswerId);

    // write new answer
    storage.storeAnswer("", newAnswerId);

    // write old original upload
    storage.storeUploadSnapshotZip(new NullInputStream(0), oldUploadKey, networkId);

    // write old original upload in network to be deleted
    storage.storeUploadSnapshotZip(new NullInputStream(0), oldUploadKey, networkToDeleteId);

    // write new original upload
    storage.storeUploadSnapshotZip(new NullInputStream(0), newUploadKey, networkId);

    // write old snapshot
    storage.storeSnapshotMetadata(new SnapshotMetadata(oldTime, null), networkId, snapshotOldId);
    storage.writeId(snapshotOldId, snapshotOld, networkId);

    // write old snapshot in network to be deleted
    storage.storeSnapshotMetadata(
        new SnapshotMetadata(oldTime, null), networkToDeleteId, snapshotOldId);
    storage.writeId(snapshotOldId, snapshotOld, networkToDeleteId);

    // write new snapshot
    storage.storeSnapshotMetadata(new SnapshotMetadata(newTime, null), networkId, snapshotNewId);
    storage.writeId(snapshotNewId, snapshotNew, networkId);

    // unlink old snapshot
    storage.deleteNameIdMapping(SnapshotId.class, snapshotOld, networkId);

    // unlink network to be deleted
    storage.deleteNameIdMapping(NetworkId.class, networkToDelete);

    // should exist before garbage collection
    storage.loadAnswer(oldAnswerId);
    storage.loadAnswer(newAnswerId);
    storage.loadUploadSnapshotZip(oldUploadKey, networkId).close();
    storage.loadUploadSnapshotZip(oldUploadKey, networkToDeleteId).close();
    storage.loadUploadSnapshotZip(newUploadKey, networkId).close();
    storage.loadSnapshotMetadata(networkId, snapshotNewId);
    storage.loadSnapshotMetadata(networkId, snapshotOldId);

    storage.runGarbageCollection(newTime);

    // should have survived garbage collection, so should not throw
    storage.loadAnswer(newAnswerId);
    storage.loadUploadSnapshotZip(newUploadKey, networkId).close();
    storage.loadSnapshotMetadata(networkId, snapshotNewId);

    // should throw because data should have been garbage collected
    expectFileNotFoundException(() -> storage.loadAnswer(oldAnswerId));
    expectFileNotFoundException(() -> storage.loadUploadSnapshotZip(oldUploadKey, networkId));
    expectFileNotFoundException(
        () -> storage.loadUploadSnapshotZip(oldUploadKey, networkToDeleteId));
    expectFileNotFoundException(() -> storage.loadSnapshotMetadata(networkId, snapshotOldId));
    expectFileNotFoundException(
        () -> storage.loadSnapshotMetadata(networkToDeleteId, snapshotOldId));
  }

  @Test
  public void testRunGarbageCollectionFreshStartup() throws IOException {
    // Should not throw
    _storage.runGarbageCollection(Instant.MAX);
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
}
