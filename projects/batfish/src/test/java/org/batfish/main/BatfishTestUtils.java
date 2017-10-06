package org.batfish.main;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.apache.commons.collections4.map.LRUMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.config.Settings.EnvironmentSettings;
import org.batfish.config.Settings.TestrigSettings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.junit.rules.TemporaryFolder;

public class BatfishTestUtils {

  private static Cache<TestrigSettings, SortedMap<String, Configuration>> makeTestrigCache() {
    return CacheBuilder.newBuilder().maximumSize(5).weakValues().build();
  }

  private static Map<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>>
      makeEnvBgpCache() {
    return Collections.synchronizedMap(new LRUMap<>(4));
  }

  private static Map<EnvironmentSettings, SortedMap<String, RoutesByVrf>> makeEnvRouteCache() {
    return Collections.synchronizedMap(new LRUMap<>(4));
  }

  private static Cache<TestrigSettings, DataPlane> makeDataPlaneCache() {
    return CacheBuilder.newBuilder().maximumSize(2).weakValues().build();
  }

  private static Batfish initBatfish(
      SortedMap<String, Configuration> configurations, @Nullable TemporaryFolder tempFolder)
      throws IOException {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    final Cache<TestrigSettings, SortedMap<String, Configuration>> testrigs = makeTestrigCache();

    if (!configurations.isEmpty()) {
      Path containerDir = tempFolder.newFolder("container").toPath();
      settings.setContainerDir(containerDir);
      settings.setTestrig("tempTestrig");
      settings.setEnvironmentName("tempEnvironment");
      Batfish.initTestrigSettings(settings);
      settings.getBaseTestrigSettings().getSerializeIndependentPath().toFile().mkdirs();
      settings.getBaseTestrigSettings().getEnvironmentSettings().getEnvPath().toFile().mkdirs();
      testrigs.put(settings.getBaseTestrigSettings(), configurations);
      settings.setActiveTestrigSettings(settings.getBaseTestrigSettings());
    }

    return new Batfish(
        settings, testrigs, makeDataPlaneCache(), makeEnvBgpCache(), makeEnvRouteCache());
  }

  private static Batfish initBatfishFromConfigurationText(
      SortedMap<String, String> configurationText,
      SortedMap<String, String> hostsText,
      SortedMap<String, String> iptablesFilesText,
      @Nullable TemporaryFolder tempFolder)
      throws IOException {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    settings.setDisableUnrecognized(true);
    Path containerDir = tempFolder.newFolder("container").toPath();
    settings.setContainerDir(containerDir);
    settings.setTestrig("tempTestrig");
    settings.setEnvironmentName("tempEnvironment");
    Batfish.initTestrigSettings(settings);
    Path testrigPath = settings.getBaseTestrigSettings().getTestRigPath();
    testrigPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).toFile().mkdirs();
    testrigPath.resolve(BfConsts.RELPATH_AWS_VPC_CONFIGS_DIR).toFile().mkdirs();
    testrigPath.resolve(BfConsts.RELPATH_HOST_CONFIGS_DIR).toFile().mkdirs();
    testrigPath.resolve("iptables").toFile().mkdirs();
    settings.getBaseTestrigSettings().getEnvironmentSettings().getEnvPath().toFile().mkdirs();
    settings.setActiveTestrigSettings(settings.getBaseTestrigSettings());
    configurationText.forEach(
        (filename, content) -> {
          Path filePath =
              testrigPath.resolve(Paths.get(BfConsts.RELPATH_CONFIGURATIONS_DIR, filename));
          CommonUtil.writeFile(filePath, content);
        });
    hostsText.forEach(
        (filename, content) -> {
          Path filePath =
              testrigPath.resolve(Paths.get(BfConsts.RELPATH_HOST_CONFIGS_DIR, filename));
          CommonUtil.writeFile(filePath, content);
        });
    iptablesFilesText.forEach(
        (filename, content) -> {
          Path filePath =
              testrigPath.resolve(Paths.get("iptables", filename));
          CommonUtil.writeFile(filePath, content);
        });

    Batfish batfish =
        new Batfish(
            settings,
            makeTestrigCache(),
            makeDataPlaneCache(),
            makeEnvBgpCache(),
            makeEnvRouteCache());
    batfish.serializeVendorConfigs(
        testrigPath, settings.getBaseTestrigSettings().getSerializeVendorPath());
    batfish.serializeIndependentConfigs(
        settings.getBaseTestrigSettings().getSerializeVendorPath(),
        settings.getBaseTestrigSettings().getSerializeIndependentPath());
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
    Configuration config = new Configuration(nodeName);
    config.setConfigurationFormat(configFormat);
    for (String interfaceName : interfaceNames) {
      config.getInterfaces().put(interfaceName, new Interface(interfaceName, config));
    }
    return config;
  }

  /**
   * Prepares a default scenario from provided configs and returns a Batfish pointing to it.
   *
   * @param configsResourcePrefix Denotes the resource path to the directory containing the
   *     configurations
   * @param configFilenames The names of the configuration files. The filename for each
   *     configuration should be identical to hostname declared therein.
   * @param tempFolder
   * @return A Batfish pointing to the newly prepared scenario.
   */
  public static Batfish getBatfishFromTestrigResource(
      String configsResourcePrefix, String[] configFilenames, @Nullable TemporaryFolder tempFolder)
      throws IOException {
    SortedMap<String, String> configurationsText = new TreeMap<>();
    for (String configurationName : configFilenames) {
      String configurationPath =
          String.format(
              "%s/%s/%s",
              configsResourcePrefix, BfConsts.RELPATH_CONFIGURATIONS_DIR, configurationName);
      String configurationText = CommonUtil.readResource(configurationPath);
      configurationsText.put(configurationName, configurationText);
    }
    Batfish batfish =
        BatfishTestUtils.getBatfishFromConfigurationText(
            configurationsText,
            Collections.emptySortedMap(),
            Collections.emptySortedMap(),
            tempFolder);
    return batfish;
  }

  /**
   * Get a new Batfish instance with given configurations, tempFolder should be present for
   * non-empty configurations
   *
   * @param configurations Map of all Configuration Name -> Configuration Object
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return New Batfish instance
   */
  public static Batfish getBatfishFromConfigurationText(
      SortedMap<String, String> configurationText,
      SortedMap<String, String> hostText,
      SortedMap<String, String> iptablesText,
      @Nullable TemporaryFolder tempFolder)
      throws IOException {
    if (!configurationText.isEmpty() && tempFolder == null) {
      throw new BatfishException("tempFolder must be set for non-empty configurations");
    }
    return initBatfishFromConfigurationText(configurationText, hostText, iptablesText, tempFolder);
  }

  /**
   * Get a new Batfish instance with given configurations, tempFolder should be present for
   * non-empty configurations
   *
   * @param configurations Map of all Configuration Name -> Configuration Object
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return New Batfish instance
   */
  public static Batfish getBatfish(
      SortedMap<String, Configuration> configurations, @Nullable TemporaryFolder tempFolder)
      throws IOException {
    if (!configurations.isEmpty() && tempFolder == null) {
      throw new BatfishException("tempFolder must be set for non-empty configurations");
    }
    return initBatfish(configurations, tempFolder);
  }
}
