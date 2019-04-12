package org.batfish.main;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
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
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.dataplane.ibdp.IncrementalDataPlanePlugin;
import org.batfish.identifiers.FileBasedIdResolver;
import org.batfish.identifiers.IdResolver;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.StorageProvider;
import org.junit.rules.TemporaryFolder;

public class BatfishTestUtils {

  private static class TestFileBasedIdResolver extends FileBasedIdResolver {

    public TestFileBasedIdResolver(Path storageBase) {
      super(storageBase);
    }
  }

  private static Cache<NetworkSnapshot, SortedMap<String, Configuration>> makeTestrigCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(5).build();
  }

  private static Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>> makeEnvBgpCache() {
    return Collections.synchronizedMap(new LRUMap<>(4));
  }

  private static Map<NetworkSnapshot, SortedMap<String, RoutesByVrf>> makeEnvRouteCache() {
    return Collections.synchronizedMap(new LRUMap<>(4));
  }

  private static Cache<NetworkSnapshot, DataPlane> makeDataPlaneCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(2).build();
  }

  private static Batfish initBatfish(
      SortedMap<String, Configuration> configurations, @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    final Cache<NetworkSnapshot, SortedMap<String, Configuration>> testrigs = makeTestrigCache();

    settings.setStorageBase(tempFolder.newFolder().toPath());
    settings.setContainer("tempNetworkId");
    if (!configurations.isEmpty()) {
      settings.setTestrig("tempSnapshotId");
      settings.setSnapshotName("tempSnapshot");
      Batfish.initTestrigSettings(settings);
      settings.getBaseTestrigSettings().getInputPath().toFile().mkdirs();
      settings.getBaseTestrigSettings().getOutputPath().toFile().mkdirs();
      testrigs.put(
          new NetworkSnapshot(new NetworkId("tempNetworkId"), new SnapshotId("tempSnapshotId")),
          configurations);
      settings.setActiveTestrigSettings(settings.getBaseTestrigSettings());
    }
    Batfish batfish =
        new Batfish(
            settings,
            testrigs,
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeEnvRouteCache(),
            null,
            new TestFileBasedIdResolver(settings.getStorageBase()));
    if (!configurations.isEmpty()) {
      batfish.initializeTopology(batfish.getNetworkSnapshot());
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
    settings.setContainer("tempNetworkId");
    NetworkSnapshot networkSnapshot =
        new NetworkSnapshot(new NetworkId("tempNetworkId"), new SnapshotId("tempSnapshotId"));
    NetworkSnapshot referenceNetworkSnapshot =
        new NetworkSnapshot(
            new NetworkId("tempNetworkId"), new SnapshotId("tempReferenceSnapshotId"));
    if (!baseConfigs.isEmpty()) {
      settings.setTestrig("tempSnapshotId");
      settings.setSnapshotName("tempSnapshot");
      settings.setDeltaTestrig(new SnapshotId("tempReferenceSnapshotId"));
      Batfish.initTestrigSettings(settings);
      settings.getBaseTestrigSettings().getOutputPath().toFile().mkdirs();
      settings.getDeltaTestrigSettings().getOutputPath().toFile().mkdirs();
      testrigs.put(networkSnapshot, baseConfigs);
      testrigs.put(referenceNetworkSnapshot, deltaConfigs);
      settings.setActiveTestrigSettings(settings.getBaseTestrigSettings());
    }
    Batfish batfish =
        new Batfish(
            settings,
            testrigs,
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeEnvRouteCache(),
            null,
            new TestFileBasedIdResolver(settings.getStorageBase()));
    batfish.getSettings().setDiffQuestion(true);
    if (!baseConfigs.isEmpty()) {
      batfish.initializeTopology(networkSnapshot);
    }
    if (!deltaConfigs.isEmpty()) {
      batfish.pushDeltaSnapshot();
      batfish.initializeTopology(referenceNetworkSnapshot);
      batfish.popSnapshot();
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
    Map<String, String> routingTablesText = testrigText.getRoutingTablesText();

    Settings settings = new Settings(new String[] {});
    configureBatfishTestSettings(settings);
    settings.setStorageBase(tempFolder.newFolder().toPath());
    settings.setContainer("tempNetworkId");
    settings.setTestrig("tempSnapshotId");
    settings.setSnapshotName("tempSnapshot");
    Batfish.initTestrigSettings(settings);
    Path testrigPath = settings.getBaseTestrigSettings().getInputPath();
    settings.getBaseTestrigSettings().getOutputPath().toFile().mkdirs();
    settings.setActiveTestrigSettings(settings.getBaseTestrigSettings());
    writeTemporaryTestrigFiles(
        configurationText, testrigPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR));
    writeTemporaryTestrigFiles(awsText, testrigPath.resolve(BfConsts.RELPATH_AWS_CONFIGS_DIR));
    writeTemporaryTestrigFiles(
        bgpTablesText, settings.getBaseTestrigSettings().getEnvironmentBgpTablesPath());
    writeTemporaryTestrigFiles(hostsText, testrigPath.resolve(BfConsts.RELPATH_HOST_CONFIGS_DIR));
    writeTemporaryTestrigFiles(iptablesFilesText, testrigPath.resolve("iptables"));
    if (layer1TopologyText != null) {
      writeTemporaryTestrigFiles(
          ImmutableMap.of(BfConsts.RELPATH_L1_TOPOLOGY_PATH, layer1TopologyText), testrigPath);
    }
    writeTemporaryTestrigFiles(
        routingTablesText, settings.getBaseTestrigSettings().getEnvironmentRoutingTablesPath());
    Batfish batfish =
        new Batfish(
            settings,
            makeTestrigCache(),
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeEnvRouteCache(),
            null,
            new TestFileBasedIdResolver(settings.getStorageBase()));
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
      config.getAllInterfaces().put(interfaceName, new Interface(interfaceName, config));
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
    settings.setContainer("tempContainer");
    Batfish batfish =
        new Batfish(
            settings,
            makeTestrigCache(),
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeEnvRouteCache(),
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
    return getBatfishForTextConfigs(folder, configurationNames).loadConfigurations();
  }

  private static void writeTemporaryTestrigFiles(
      @Nullable Map<String, String> filesText, Path outputDirectory) {
    if (filesText != null) {
      filesText.forEach(
          (filename, text) -> {
            outputDirectory.toFile().mkdirs();
            CommonUtil.writeFile(outputDirectory.resolve(filename), text);
          });
    }
  }
}
