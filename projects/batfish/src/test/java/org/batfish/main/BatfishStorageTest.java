package org.batfish.main;

import static org.batfish.common.Version.INCOMPATIBLE_VERSION;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

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
import org.batfish.storage.FileBasedStorage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatfishStorageTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

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
    String network = "network";
    String snapshot = "snapshot";

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
    assertThat(_storage.loadConfigurations("nonexistent", "nonexistent"), nullValue());
  }

  @Test
  public void loadOldConfigurationsReturnsNull() {
    ConvertConfigurationAnswerElement oldConvertAnswer = new ConvertConfigurationAnswerElement();
    oldConvertAnswer.setVersion(INCOMPATIBLE_VERSION);
    assertThat(
        "should not be compatible with current code",
        Version.isCompatibleVersion("current", "old test", oldConvertAnswer.getVersion()),
        equalTo(false));

    String network = "network";
    String snapshot = "snapshot";
    Map<String, Configuration> configs = new HashMap<>();
    configs.put("node1", new Configuration("node1", ConfigurationFormat.CISCO_IOS));
    _storage.storeConfigurations(configs, oldConvertAnswer, network, snapshot);

    assertThat(_storage.loadConfigurations(network, snapshot), nullValue());
  }
}
