package org.batfish.main;

import static org.batfish.common.BfConsts.RELPATH_AWS_CONFIGS_DIR;
import static org.batfish.common.BfConsts.RELPATH_AZURE_CONFIGS_DIR;
import static org.batfish.common.BfConsts.RELPATH_CHECKPOINT_MANAGEMENT_DIR;
import static org.batfish.common.BfConsts.RELPATH_CONFIGURATIONS_DIR;
import static org.batfish.common.BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES;
import static org.batfish.common.BfConsts.RELPATH_HOST_CONFIGS_DIR;
import static org.batfish.common.BfConsts.RELPATH_SONIC_CONFIGS_DIR;
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
import java.util.concurrent.atomic.AtomicInteger;
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

  /** Used to generate a guaranteed different snapshot name every time. */
  private static final AtomicInteger SNAPSHOT_COUNTER = new AtomicInteger();

  /** Not for use in this file, but used in other tests as a convenience. */
  public static final NetworkSnapshot DUMMY_SNAPSHOT_1 =
      new NetworkSnapshot(new NetworkId("testnet"), new SnapshotId("testss"));

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

  private static void setNextTestNetworkSnapshot(Settings settings) {
    int cur = SNAPSHOT_COUNTER.incrementAndGet();
    NetworkId net = new NetworkId("net" + cur);
    settings.setContainer(net.getId());
    NetworkSnapshot snap = new NetworkSnapshot(net, new SnapshotId("snap" + cur));
    settings.setSnapshotName(snap.getSnapshot().getId() + "name");
    settings.setTestrig(snap.getSnapshot().getId());
    NetworkSnapshot ref = new NetworkSnapshot(net, new SnapshotId("ref" + cur));
    settings.setDeltaTestrig(ref.getSnapshot());
  }

  private static Batfish initBatfish(
      SortedMap<String, Configuration> configurations, @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    final Cache<NetworkSnapshot, SortedMap<String, Configuration>> testrigs = makeTestrigCache();

    settings.setStorageBase(tempFolder.newFolder().toPath());
    setNextTestNetworkSnapshot(settings);
    if (!configurations.isEmpty()) {
      testrigs.put(
          new NetworkSnapshot(settings.getContainer(), settings.getTestrig()), configurations);
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
    setNextTestNetworkSnapshot(settings);
    if (!baseConfigs.isEmpty()) {
      testrigs.put(
          new NetworkSnapshot(settings.getContainer(), settings.getTestrig()), baseConfigs);
      testrigs.put(
          new NetworkSnapshot(settings.getContainer(), settings.getDeltaTestrig()), deltaConfigs);
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
      batfish.initializeTopology(batfish.getSnapshot());
    }
    if (!deltaConfigs.isEmpty()) {
      batfish.initializeTopology(batfish.getReferenceSnapshot());
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
    return getBatfishFromTestrigText(testrigText, tempFolder.newFolder().toPath());
  }

  /**
   * Get a new Batfish instance with given configurations, tempFolder should be present for
   * non-empty configurations
   *
   * @param testrigText Structure containing names and content of testrig input files
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return Batfish instance pointing at new testrig comprising testrigText
   */
  public static Batfish getBatfishFromTestrigText(TestrigText testrigText, Path tempFolder)
      throws IOException {
    Map<String, byte[]> awsBytes = testrigText.getAwsBytes();
    Map<String, byte[]> azureBytes = testrigText.getAzureBytes();
    Map<String, byte[]> bgpTablesBytes = testrigText.getBgpTablesBytes();
    Map<String, byte[]> checkpointMgmtBytes = testrigText.getCheckpointMgmtBytes();
    Map<String, byte[]> configurationBytes = testrigText.getConfigurationBytes();
    byte[] externalBgpAnnouncementsBytes = testrigText.getExternalBgpAnnouncementBytes();
    Map<String, byte[]> hostsBytes = testrigText.getHostsBytes();
    Map<String, byte[]> iptablesFilesBytes = testrigText.getIptablesFilesBytes();
    byte[] ispConfigBytes = testrigText.getIspConfigBytes();
    byte[] layer1TopologyBytes = testrigText.getLayer1TopologyBytes();
    byte[] runtimeDataBytes = testrigText.getRuntimeDataBytes();
    Map<String, byte[]> sonicConfigDbBytes = testrigText.getSonicConfigBytes();
    ConversionContext conversionContext = testrigText.getConversionContext();

    Settings settings = new Settings(new String[] {});
    configureBatfishTestSettings(settings);
    settings.setStorageBase(tempFolder);
    setNextTestNetworkSnapshot(settings);
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
        checkpointMgmtBytes, RELPATH_CHECKPOINT_MANAGEMENT_DIR, storage, batfish.getSnapshot());
    writeTemporarySnapshotInputFiles(
        configurationBytes, RELPATH_CONFIGURATIONS_DIR, storage, batfish.getSnapshot());
    writeTemporarySnapshotInputFiles(
        awsBytes, RELPATH_AWS_CONFIGS_DIR, storage, batfish.getSnapshot());
    writeTemporarySnapshotInputFiles(
        azureBytes, RELPATH_AZURE_CONFIGS_DIR, storage, batfish.getSnapshot());
    writeTemporarySnapshotInputFiles(
        bgpTablesBytes, RELPATH_ENVIRONMENT_BGP_TABLES, storage, batfish.getSnapshot());
    if (externalBgpAnnouncementsBytes != null) {
      writeTemporarySnapshotInputFiles(
          ImmutableMap.of(
              BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS, externalBgpAnnouncementsBytes),
          "",
          storage,
          batfish.getSnapshot());
    }
    writeTemporarySnapshotInputFiles(
        hostsBytes, RELPATH_HOST_CONFIGS_DIR, storage, batfish.getSnapshot());
    writeTemporarySnapshotInputFiles(
        iptablesFilesBytes, "iptables", storage, batfish.getSnapshot());
    if (ispConfigBytes != null) {
      writeTemporarySnapshotInputFiles(
          ImmutableMap.of(BfConsts.RELPATH_ISP_CONFIG_FILE, ispConfigBytes),
          "batfish",
          storage,
          batfish.getSnapshot());
    }
    if (layer1TopologyBytes != null) {
      writeTemporarySnapshotInputFiles(
          ImmutableMap.of(BfConsts.RELPATH_L1_TOPOLOGY_PATH, layer1TopologyBytes),
          "",
          storage,
          batfish.getSnapshot());
    }
    if (runtimeDataBytes != null) {
      writeTemporarySnapshotInputFiles(
          ImmutableMap.of(BfConsts.RELPATH_RUNTIME_DATA_FILE, runtimeDataBytes),
          BfConsts.RELPATH_BATFISH,
          storage,
          batfish.getSnapshot());
    }
    writeTemporarySnapshotInputFiles(
        sonicConfigDbBytes, RELPATH_SONIC_CONFIGS_DIR, storage, batfish.getSnapshot());
    if (conversionContext != null) {
      // Note: only works when the snapshot input does not contain anything that would populate
      // conversion context.
      writeTemporaryConversionContext(conversionContext, storage, batfish.getSnapshot());
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
    setNextTestNetworkSnapshot(settings);
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
