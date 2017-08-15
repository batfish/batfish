package org.batfish.bdp;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections4.map.LRUMap;
import org.batfish.common.BatfishLogger;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link BdpDataPlanePlugin}. */
public class BdpDataPlanePluginTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static Configuration createConfiguration(String hostname, String interfaceName) {
    Configuration config = new Configuration(hostname);
    config.setConfigurationFormat(ConfigurationFormat.HOST);
    config.getInterfaces().put(interfaceName, new Interface(interfaceName, config));
    return config;
  }

  private Batfish initBatfish(SortedMap<String, Configuration> configurations) throws IOException {
    Settings settings = new Settings(new String[] {});

    settings.setLogger(new BatfishLogger("debug", false));
    settings.getBaseTestrigSettings().setNodeRolesPath(Paths.get("/fakepath"));
    settings.getBaseTestrigSettings().setInferredNodeRolesPath(Paths.get("/fakepath"));
    settings.getBaseTestrigSettings().setTestRigPath(Paths.get("/fakepath"));
    settings
        .getBaseTestrigSettings()
        .getEnvironmentSettings()
        .setDataPlanePath(_folder.newFile("dataplane").toPath());
    settings
        .getBaseTestrigSettings()
        .getEnvironmentSettings()
        .setDataPlaneAnswerPath(_folder.newFile("dataplaneanswer").toPath());
    final Map<TestrigSettings, SortedMap<String, Configuration>> CACHED_TESTRIGS =
        Collections.synchronizedMap(
            new LRUMap<TestrigSettings, SortedMap<String, Configuration>>(5));
    settings.setActiveTestrigSettings(settings.getBaseTestrigSettings());
    CACHED_TESTRIGS.put(settings.getBaseTestrigSettings(), configurations);
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

  @Test(timeout = 5000)
  public void testComputeFixedPoint() throws IOException {
    SortedMap<String, Configuration> configurations = new TreeMap<>();
    //creating configurations with no vrfs
    configurations.put("h1", createConfiguration("h1", "eth0"));
    configurations.put("h2", createConfiguration("h2", "e0"));
    Batfish batfish = initBatfish(configurations);
    BdpDataPlanePlugin dataPlanePlugin = new BdpDataPlanePlugin();
    dataPlanePlugin.initialize(batfish);

    //Test that compute Data Plane finishes in a finite time
    dataPlanePlugin.computeDataPlane(false);
  }
}
