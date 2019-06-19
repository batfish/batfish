package org.batfish.representation.cisco;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasOspfAreaName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isOspfPassive;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Simple unit tests of various OSPF configurations. */
@RunWith(JUnit4.class)
public class CiscoOspfAreaTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/representation/cisco/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration parseConfig(String hostname) throws IOException {
    return parseTextConfigs(hostname).get(hostname.toLowerCase());
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  @Test
  public void testOspfPassiveInInterface() throws IOException {
    Configuration c = parseConfig("cisco-nxos-passive");
    assertThat(c, hasInterface("Ethernet2/1", allOf(isOspfPassive(), hasOspfAreaName(1L))));
  }

  @Test
  public void testOspfAreaInInterface() throws IOException {
    Configuration c = parseConfig("cisco-nxos-passive");
    assertThat(c, hasInterface("Ethernet2/2", allOf(not(isOspfPassive()), hasOspfAreaName(3L))));
  }
}
