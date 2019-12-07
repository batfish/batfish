package org.batfish.grammar.arista;

import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of router ID computation on Arista. */
public class AristaRouterIdTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/arista/testconfigs/";
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Batfish batfish = BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
    batfish
        .getSettings()
        .setDebugFlags(ImmutableList.of(CiscoCombinedParser.DEBUG_FLAG_USE_ARISTA_BGP));
    return batfish;
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    Configuration configuration = configs.get(canonicalHostname);
    assertThat(configuration.getConfigurationFormat(), equalTo(ConfigurationFormat.ARISTA));
    return configuration;
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  @Test
  public void testRouterId() throws IOException {
    Configuration c = parseConfig("arista_router_id");
    // default VRF uses the highest loopback IP
    assertThat(c, hasVrf("default", hasBgpProcess(hasRouterId(Ip.parse("4.4.4.5")))));
    // VRF A uses the highest ACTIVE loopback IP
    assertThat(c, hasVrf("A", hasBgpProcess(hasRouterId(Ip.parse("2.2.2.2")))));
    // VRF B has no active interfaces.
    assertThat(c, hasVrf("B", hasBgpProcess(hasRouterId(Ip.ZERO))));
  }
}
