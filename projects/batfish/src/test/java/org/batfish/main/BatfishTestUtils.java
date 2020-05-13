package org.batfish.main;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.collections4.map.LRUMap;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.dataplane.ibdp.IncrementalDataPlanePlugin;
import org.batfish.identifiers.IdResolver;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.identifiers.StorageBasedIdResolver;
import org.batfish.main.Batfish.TestrigSettings;
import org.batfish.storage.FileBasedStorage;
import org.batfish.storage.StorageProvider;
import org.batfish.vendor.VendorConfiguration;
import org.junit.rules.TemporaryFolder;

public class BatfishTestUtils {

  public static final String TEST_SNAPSHOT_NAME = "TestSsName";
  public static final NetworkSnapshot TEST_SNAPSHOT =
      new NetworkSnapshot(new NetworkId("testnet"), new SnapshotId("testss"));
  public static final NetworkSnapshot TEST_REFERENCE_SNAPSHOT =
      new NetworkSnapshot(new NetworkId("testnet"), new SnapshotId("testrefss"));

  private static class TestStorageBasedIdResolver extends StorageBasedIdResolver {

    public TestStorageBasedIdResolver(Path storageBase) {
      super(new FileBasedStorage(storageBase, null));
    }
  }

  private static Cache<NetworkSnapshot, SortedMap<String, Configuration>> makeTestrigCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(5).build();
  }

  private static Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>> makeEnvBgpCache() {
    return Collections.synchronizedMap(new LRUMap<>(4));
  }

  private static Cache<NetworkSnapshot, DataPlane> makeDataPlaneCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(2).build();
  }

  private static Cache<NetworkSnapshot, Map<String, VendorConfiguration>>
      makeVendorConfigurationCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(2).build();
  }

  private static Batfish initBatfish(
      SortedMap<String, Configuration> configurations, @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    final Cache<NetworkSnapshot, SortedMap<String, Configuration>> testrigs = makeTestrigCache();

    settings.setStorageBase(tempFolder.newFolder().toPath());
    settings.setContainer(TEST_SNAPSHOT.getNetwork().getId());
    settings.setTestrig(TEST_SNAPSHOT.getSnapshot().getId());
    settings.setSnapshotName(TEST_SNAPSHOT_NAME);
    if (!configurations.isEmpty()) {
      testrigs.put(TEST_SNAPSHOT, configurations);
    }
    Batfish batfish =
        new Batfish(
            settings,
            testrigs,
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeVendorConfigurationCache(),
            null,
            new TestStorageBasedIdResolver(settings.getStorageBase()));
    if (!configurations.isEmpty()) {
      batfish.initializeTopology(batfish.getSnapshot());
    }
    registerDataPlanePlugins(batfish);
    return batfish;
  }

  private static Batfish initBatfish(
      @Nonnull SortedMap<String, Configuration> baseConfigs,
      @Nonnull SortedMap<String, Configuration> deltaConfigs,
      @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    final Cache<NetworkSnapshot, SortedMap<String, Configuration>> testrigs = makeTestrigCache();

    settings.setStorageBase(tempFolder.newFolder().toPath());
    settings.setContainer(TEST_SNAPSHOT.getNetwork().getId());
    if (!baseConfigs.isEmpty()) {
      settings.setTestrig(TEST_SNAPSHOT.getSnapshot().getId());
      settings.setSnapshotName(TEST_SNAPSHOT_NAME);
      settings.setDeltaTestrig(TEST_REFERENCE_SNAPSHOT.getSnapshot());
      testrigs.put(TEST_SNAPSHOT, baseConfigs);
      testrigs.put(TEST_REFERENCE_SNAPSHOT, deltaConfigs);
    }
    Batfish batfish =
        new Batfish(
            settings,
            testrigs,
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeVendorConfigurationCache(),
            null,
            new TestStorageBasedIdResolver(settings.getStorageBase()));
    batfish.getSettings().setDiffQuestion(true);
    if (!baseConfigs.isEmpty()) {
      batfish.initializeTopology(TEST_SNAPSHOT);
    }
    if (!deltaConfigs.isEmpty()) {
      batfish.initializeTopology(TEST_REFERENCE_SNAPSHOT);
    }
    registerDataPlanePlugins(batfish);
    return batfish;
  }

  private static void registerDataPlanePlugins(Batfish batfish) {
    IncrementalDataPlanePlugin ibdpPlugin = new IncrementalDataPlanePlugin();
    ibdpPlugin.initialize(batfish);
  }

  /** Configure common Batfish settings for tests (e.g. disable recovery, debug level logging) */
  public static void configureBatfishTestSettings(Settings settings) {
    settings.setLogger(new BatfishLogger("debug", false));
    settings.setDisableUnrecognized(true);
    settings.setHaltOnConvertError(true);
    settings.setHaltOnParseError(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);
    settings.setVerboseParse(true);
  }

  /**
   * Get a new Batfish instance with given configurations, tempFolder should be present for
   * non-empty configurations
   *
   * @param testrigText Structure containing names and content of testrig input files
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return Batfish instance pointing at new testrig comprising testrigText
   */
  public static Batfish getBatfishFromTestrigText(
      TestrigText testrigText, TemporaryFolder tempFolder) throws IOException {
    Map<String, String> awsText = testrigText.getAwsText();
    Map<String, String> bgpTablesText = testrigText.getBgpTablesText();
    Map<String, String> configurationText = testrigText.getConfigurationText();
    Map<String, String> hostsText = testrigText.getHostsText();
    Map<String, String> iptablesFilesText = testrigText.getIptablesFilesText();
    String layer1TopologyText = testrigText.getLayer1TopologyText();
    String runtimeDataText = testrigText.getRuntimeDataText();

    Settings settings = new Settings(new String[] {});
    configureBatfishTestSettings(settings);
    settings.setStorageBase(tempFolder.newFolder().toPath());
    settings.setContainer(TEST_SNAPSHOT.getNetwork().getId());
    settings.setTestrig(TEST_SNAPSHOT.getSnapshot().getId());
    settings.setSnapshotName(TEST_SNAPSHOT_NAME);
    Batfish batfish =
        new Batfish(
            settings,
            makeTestrigCache(),
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeVendorConfigurationCache(),
            null,
            new TestStorageBasedIdResolver(settings.getStorageBase()));
    TestrigSettings snapshotTr = batfish.getSnapshotTestrigSettings();
    Path testrigPath = snapshotTr.getInputPath();
    snapshotTr.getOutputPath().toFile().mkdirs();
    writeTemporaryTestrigFiles(
        configurationText, testrigPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR));
    writeTemporaryTestrigFiles(awsText, testrigPath.resolve(BfConsts.RELPATH_AWS_CONFIGS_DIR));
    writeTemporaryTestrigFiles(bgpTablesText, snapshotTr.getEnvironmentBgpTablesPath());
    writeTemporaryTestrigFiles(hostsText, testrigPath.resolve(BfConsts.RELPATH_HOST_CONFIGS_DIR));
    writeTemporaryTestrigFiles(iptablesFilesText, testrigPath.resolve("iptables"));
    if (layer1TopologyText != null) {
      writeTemporaryTestrigFiles(
          ImmutableMap.of(BfConsts.RELPATH_L1_TOPOLOGY_PATH, layer1TopologyText), testrigPath);
    }
    if (runtimeDataText != null) {
      writeTemporaryTestrigFiles(
          ImmutableMap.of(BfConsts.RELPATH_RUNTIME_DATA_FILE, runtimeDataText), testrigPath);
    }
    registerDataPlanePlugins(batfish);
    return batfish;
  }

  /**
   * Get a configuration object with the given interfaces
   *
   * @param nodeName Host name for the configuration
   * @param configFormat Configuration format
   * @param interfaceNames All interface names to be included
   * @return A new configuration
   */
  public static Configuration createTestConfiguration(
      String nodeName, ConfigurationFormat configFormat, String... interfaceNames) {
    Configuration config = new Configuration(nodeName, configFormat);
    for (String interfaceName : interfaceNames) {
      config
          .getAllInterfaces()
          .put(interfaceName, Interface.builder().setName(interfaceName).setOwner(config).build());
    }
    return config;
  }

  /**
   * Get a new Batfish instance with given configurations, tempFolder should be present for
   * non-empty configurations
   *
   * @param configurations Map of all Configuration Name -&gt; Configuration Object
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return New Batfish instance
   */
  public static Batfish getBatfish(
      SortedMap<String, Configuration> configurations, @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    return initBatfish(configurations, tempFolder);
  }

  /** Get a new Batfish instance with given storage provider and id resolver */
  public static Batfish getBatfish(
      @Nonnull StorageProvider storageProvider, @Nonnull IdResolver idResolver) {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    settings.setContainer(TEST_SNAPSHOT.getNetwork().getId());
    Batfish batfish =
        new Batfish(
            settings,
            makeTestrigCache(),
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeVendorConfigurationCache(),
            storageProvider,
            idResolver);
    registerDataPlanePlugins(batfish);
    return batfish;
  }

  /**
   * Get a new Batfish instance with given base and delta configurations, tempFolder should be
   * present for non-empty configurations
   *
   * @param baseConfigs Map of all Configuration Name -&gt; Configuration Object
   * @param deltaConfigs Map of all Configuration Name -&gt; Configuration Object
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return New Batfish instance
   */
  public static Batfish getBatfish(
      @Nonnull SortedMap<String, Configuration> baseConfigs,
      @Nonnull SortedMap<String, Configuration> deltaConfigs,
      @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    return initBatfish(baseConfigs, deltaConfigs, tempFolder);
  }

  public static Batfish getBatfishForTextConfigs(
      TemporaryFolder folder, String... configurationNames) throws IOException {
    SortedMap<String, String> configurationTextMap = new TreeMap<>();
    for (String configName : configurationNames) {
      String configurationText = CommonUtil.readResource(configName);
      configurationTextMap.put(new File(configName).getName(), configurationText);
    }
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder().setConfigurationText(configurationTextMap).build(), folder);
  }

  public static SortedMap<String, Configuration> parseTextConfigs(
      TemporaryFolder folder, String... configurationNames) throws IOException {
    IBatfish iBatfish = getBatfishForTextConfigs(folder, configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private static void writeTemporaryTestrigFiles(
      @Nullable Map<String, String> filesText, Path outputDirectory) {
    if (filesText != null) {
      filesText.forEach(
          (filename, text) -> {
            outputDirectory.toFile().mkdirs();
            Path fpath = Paths.get(filename);
            if (fpath.getNameCount() > 1) {
              outputDirectory.resolve(fpath.subpath(0, fpath.getNameCount() - 1)).toFile().mkdirs();
            }
            CommonUtil.writeFile(outputDirectory.resolve(filename), text);
          });
    }
  }
}
