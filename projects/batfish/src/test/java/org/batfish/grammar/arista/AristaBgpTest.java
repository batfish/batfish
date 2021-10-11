package org.batfish.grammar.arista;

import static org.batfish.datamodel.ConfigurationFormat.ARISTA;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class AristaBgpTest {
  private static final String DEFAULT_VRF_NAME = "default";
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/arista/testconfigs/";
  private static final String TESTRIGS_PREFIX = "org/batfish/grammar/arista/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      String canonicalHostname = hostname.toLowerCase();
      assertThat(configs, hasKey(canonicalHostname));
      Configuration c = configs.get(canonicalHostname);
      assertThat(c, hasConfigurationFormat(ARISTA));
      return c;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  /** Undefined redistribute map is treated as permitting everything. */
  @Test
  public void testBgpRedistributeUndefined() throws IOException {
    String config = "bgp-redistribute-undefined";
    Batfish batfish = getBatfishForConfigurationNames(config);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    assertThat(
        dp.getBgpRoutes().get(config, DEFAULT_VRF_NAME),
        contains(hasPrefix(Prefix.parse("1.2.3.4/24"))));
  }
}
