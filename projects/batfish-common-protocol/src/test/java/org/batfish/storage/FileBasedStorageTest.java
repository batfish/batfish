package org.batfish.storage;

import static org.batfish.common.Version.INCOMPATIBLE_VERSION;
import static org.batfish.storage.FileBasedStorage.objectKeyToRelativePath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Version;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.MajorIssueConfig;
import org.batfish.datamodel.answers.MinorIssueConfig;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FileBasedStorageTest {

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
  public void roundTripConfigurationsSucceeds() {
    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");

    Map<String, Configuration> configs = new HashMap<>();
    configs.put("node1", new Configuration("node1", ConfigurationFormat.CISCO_IOS));

    _storage.storeConfigurations(
        configs, new ConvertConfigurationAnswerElement(), network, snapshot);
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
  public void loadOldConfigurationsReturnsNull() {
    ConvertConfigurationAnswerElement oldConvertAnswer = new ConvertConfigurationAnswerElement();
    oldConvertAnswer.setVersion(INCOMPATIBLE_VERSION);
    assertThat(
        "should not be compatible with current code",
        Version.isCompatibleVersion("current", "old test", oldConvertAnswer.getVersion()),
        equalTo(false));

    NetworkId network = new NetworkId("network");
    SnapshotId snapshot = new SnapshotId("snapshot");
    Map<String, Configuration> configs = new HashMap<>();
    configs.put("node1", new Configuration("node1", ConfigurationFormat.CISCO_IOS));
    _storage.storeConfigurations(configs, oldConvertAnswer, network, snapshot);

    assertThat(_storage.loadConfigurations(network, snapshot), nullValue());
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
    _storage.getDirectoryProvider().getNetworkDir(network).toFile().mkdirs();

    assertThat(_storage.checkNetworkExists(network), equalTo(true));
  }

  @Test
  public void testCheckNetworkExistsFalse() {
    NetworkId network = new NetworkId("network");

    assertThat(_storage.checkNetworkExists(network), equalTo(false));
  }

  @Test
  public void testObjectKeyToRelativePathRejectsAbsolute() throws IOException {
    _thrown.expect(IllegalArgumentException.class);
    objectKeyToRelativePath("/foo/bar");
  }

  @Test
  public void testObjectKeyToRelativePathRejectsNonNormalized() throws IOException {
    _thrown.expect(IllegalArgumentException.class);
    objectKeyToRelativePath("foo/../../bar");
  }

  @Test
  public void testObjectKeyToRelativePathValid() throws IOException {
    // no exception should be thrown
    objectKeyToRelativePath("foo/bar");
  }
}
