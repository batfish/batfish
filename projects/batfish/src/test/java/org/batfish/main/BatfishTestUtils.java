package org.batfish.main;

import static org.batfish.common.BfConsts.RELPATH_AWS_CONFIGS_DIR;
import static org.batfish.common.BfConsts.RELPATH_CONFIGURATIONS_DIR;
import static org.batfish.common.BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES;
import static org.batfish.common.BfConsts.RELPATH_HOST_CONFIGS_DIR;
import static org.batfish.common.util.Resources.readResourceBytes;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
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
import org.batfish.storage.FileBasedStorage;
import org.batfish.storage.StorageProvider;
import org.batfish.vendor.ConversionContext;
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
    Map<String, byte[]> awsBytes = testrigText.getAwsBytes();
    Map<String, byte[]> bgpTablesBytes = testrigText.getBgpTablesBytes();
    Map<String, byte[]> configurationBytes = testrigText.getConfigurationBytes();
    byte[] externalBgpAnnouncementsBytes = testrigText.getExternalBgpAnnouncementBytes();
    Map<String, byte[]> hostsBytes = testrigText.getHostsBytes();
    Map<String, byte[]> iptablesFilesBytes = testrigText.getIptablesFilesBytes();
    byte[] layer1TopologyBytes = testrigText.getLayer1TopologyBytes();
    byte[] runtimeDataBytes = testrigText.getRuntimeDataBytes();
    ConversionContext conversionContext = testrigText.getConversionContext();

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
    StorageProvider storage = new FileBasedStorage(settings.getStorageBase(), batfish.getLogger());
    writeTemporarySnapshotInputFiles(
        configurationBytes, RELPATH_CONFIGURATIONS_DIR, storage, TEST_SNAPSHOT);
    writeTemporarySnapshotInputFiles(awsBytes, RELPATH_AWS_CONFIGS_DIR, storage, TEST_SNAPSHOT);
    writeTemporarySnapshotInputFiles(
        bgpTablesBytes, RELPATH_ENVIRONMENT_BGP_TABLES, storage, TEST_SNAPSHOT);
    if (externalBgpAnnouncementsBytes != null) {
      writeTemporarySnapshotInputFiles(
          ImmutableMap.of(
              BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS, externalBgpAnnouncementsBytes),
          "",
          storage,
          TEST_SNAPSHOT);
    }
    writeTemporarySnapshotInputFiles(hostsBytes, RELPATH_HOST_CONFIGS_DIR, storage, TEST_SNAPSHOT);
    writeTemporarySnapshotInputFiles(iptablesFilesBytes, "iptables", storage, TEST_SNAPSHOT);
    if (layer1TopologyBytes != null) {
      writeTemporarySnapshotInputFiles(
          ImmutableMap.of(BfConsts.RELPATH_L1_TOPOLOGY_PATH, layer1TopologyBytes),
          "",
          storage,
          TEST_SNAPSHOT);
    }
    if (runtimeDataBytes != null) {
      writeTemporarySnapshotInputFiles(
          ImmutableMap.of(BfConsts.RELPATH_RUNTIME_DATA_FILE, runtimeDataBytes),
          "",
          storage,
          TEST_SNAPSHOT);
    }
    if (conversionContext != null) {
      // Note: only works when the snapshot input does not contain anything that would populate
      // conversion context.
      writeTemporaryConversionContext(conversionContext, storage, TEST_SNAPSHOT);
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
    SortedMap<String, byte[]> configurationBytesMap = new TreeMap<>();
    for (String configName : configurationNames) {
      byte[] configurationBytes = readResourceBytes(configName);
      configurationBytesMap.put(new File(configName).getName(), configurationBytes);
    }
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder().setConfigurationBytes(configurationBytesMap).build(), folder);
  }

  public static Batfish getBatfishForTextConfigsAndConversionContext(
      TemporaryFolder folder, ConversionContext conversionContext, String... configurationNames)
      throws IOException {
    SortedMap<String, byte[]> configurationBytesMap = new TreeMap<>();
    for (String configName : configurationNames) {
      byte[] configurationBytes = readResourceBytes(configName);
      configurationBytesMap.put(new File(configName).getName(), configurationBytes);
    }
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder()
            .setConfigurationBytes(configurationBytesMap)
            .setConversionContext(conversionContext)
            .build(),
        folder);
  }

  public static SortedMap<String, Configuration> parseTextConfigs(
      TemporaryFolder folder, String... configurationNames) throws IOException {
    IBatfish iBatfish = getBatfishForTextConfigs(folder, configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private static void writeTemporarySnapshotInputFiles(
      @Nullable Map<String, byte[]> filesBytes,
      String keyPrefix,
      StorageProvider storage,
      NetworkSnapshot snapshot) {
    if (filesBytes != null) {
      filesBytes.forEach(
          (filename, bytes) -> {
            String key =
                keyPrefix.isEmpty() ? filename : String.format("%s/%s", keyPrefix, filename);
            try {
              storage.storeSnapshotInputObject(new ByteArrayInputStream(bytes), key, snapshot);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    }
  }

  private static void writeTemporaryConversionContext(
      ConversionContext conversionContext, StorageProvider storage, NetworkSnapshot snapshot) {
    try {
      storage.storeConversionContext(conversionContext, snapshot);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
