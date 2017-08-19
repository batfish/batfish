package org.batfish.common;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nullable;
import org.apache.commons.collections4.map.LRUMap;
import org.batfish.config.Settings;
import org.batfish.config.Settings.EnvironmentSettings;
import org.batfish.config.Settings.TestrigSettings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.main.Batfish;
import org.junit.rules.TemporaryFolder;

public class BatfishTestUtils {

  private static Batfish _batfish;
  private static Batfish _batfishConfigurations;

  private static Batfish initBatfish(
      @Nullable SortedMap<String, Configuration> configurations,
      @Nullable TemporaryFolder tempFolder)
      throws IOException {
    Settings settings = new Settings(new String[] {});
    settings.setLogger(new BatfishLogger("debug", false));
    final Map<TestrigSettings, SortedMap<String, Configuration>> CACHED_TESTRIGS =
        Collections.synchronizedMap(
            new LRUMap<TestrigSettings, SortedMap<String, Configuration>>(5));
    if (configurations != null) {
      settings.getBaseTestrigSettings().setNodeRolesPath(Paths.get("/fakepath"));
      settings.getBaseTestrigSettings().setInferredNodeRolesPath(Paths.get("/fakepath"));
      settings.getBaseTestrigSettings().setTestRigPath(Paths.get("/fakepath"));
      settings
          .getBaseTestrigSettings()
          .getEnvironmentSettings()
          .setDataPlanePath(tempFolder.newFile("dataplane").toPath());
      settings
          .getBaseTestrigSettings()
          .getEnvironmentSettings()
          .setDataPlaneAnswerPath(tempFolder.newFile("dataplaneanswer").toPath());
      CACHED_TESTRIGS.put(settings.getBaseTestrigSettings(), configurations);
      settings.setActiveTestrigSettings(settings.getBaseTestrigSettings());
    }

    final Map<TestrigSettings, DataPlane> CACHED_DATA_PLANES =
        Collections.synchronizedMap(new LRUMap<TestrigSettings, DataPlane>(2));
    final Map<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>>
        CACHED_ENVIRONMENT_BGP_TABLES =
            Collections.synchronizedMap(
                new LRUMap<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>>(4));
    final Map<EnvironmentSettings, SortedMap<String, RoutesByVrf>>
        CACHED_ENVIRONMENT_ROUTING_TABLES =
            Collections.synchronizedMap(
                new LRUMap<EnvironmentSettings, SortedMap<String, RoutesByVrf>>(4));
    Batfish batfish =
        new Batfish(
            settings,
            CACHED_TESTRIGS,
            CACHED_DATA_PLANES,
            CACHED_ENVIRONMENT_BGP_TABLES,
            CACHED_ENVIRONMENT_ROUTING_TABLES);
    return batfish;
  }

  public static Configuration createConfiguration(String hostname, String interfaceName) {
    Configuration config = new Configuration(hostname);
    config.setConfigurationFormat(ConfigurationFormat.HOST);
    config.getInterfaces().put(interfaceName, new Interface(interfaceName, config));
    return config;
  }

  public static Batfish getBatfishWithConfigurations(
      SortedMap<String, Configuration> configurations, TemporaryFolder tempFolder)
      throws IOException {
    if (_batfishConfigurations == null) {
      _batfishConfigurations = initBatfish(configurations, tempFolder);
    }
    return _batfishConfigurations;
  }

  public static Batfish getBatfish() throws IOException {
    if (_batfish == null) {
      _batfish = initBatfish(null, null);
    }
    return _batfish;
  }
}
