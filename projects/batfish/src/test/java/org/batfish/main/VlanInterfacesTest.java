package org.batfish.main;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class VlanInterfacesTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/main/testconfigs/";
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration parseConfig(String hostname) {
    try {
      IBatfish batfish =
          BatfishTestUtils.getBatfishForTextConfigs(_folder, TESTCONFIGS_PREFIX + hostname);
      Map<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());
      assertThat(configs, hasKey(hostname.toLowerCase()));
      return configs.get(hostname.toLowerCase());
    } catch (IOException e) {
      throw new AssertionError("Failed to parse " + hostname, e);
    }
  }

  /** Tests Batfish#disableUnusableVlanInterfaces. */
  @Test
  public void testDisableUnusableInterfaces() {
    Configuration c = parseConfig("vlan");
    assertThat(
        c.getAllInterfaces().keySet(),
        containsInAnyOrder("Ethernet1", "Ethernet2", "Vlan1", "Vlan2"));
    assertThat(c.getActiveInterfaces().keySet(), containsInAnyOrder("Ethernet1", "Vlan1"));
  }
}
