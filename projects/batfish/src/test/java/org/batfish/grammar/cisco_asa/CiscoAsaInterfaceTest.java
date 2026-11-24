package org.batfish.grammar.cisco_asa;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_ASA;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.main.BatfishTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CiscoAsaInterfaceTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_asa/testconfigs/";

  @TempDir public File _folder;

  private Configuration parseConfig(String hostname) throws IOException {
    String[] names = new String[] {TESTCONFIGS_PREFIX + hostname};
    Map<String, Configuration> configs = BatfishTestUtils.parseTextConfigs(_folder, names);
    assertThat(configs, hasKey(hostname.toLowerCase()));
    Configuration c = configs.get(hostname.toLowerCase());
    assertThat(c.getConfigurationFormat(), equalTo(CISCO_ASA));
    return c;
  }

  @Test
  void testPortChannel() throws IOException {
    Configuration c = parseConfig("port_channel");
    assertThat(c, hasInterface("Port-channel2", isActive()));
  }

  @Test
  void testPortChannelNameif() throws IOException {
    Configuration c = parseConfig("port_channel_nameif");
    assertThat(c, hasInterface("inside", isActive()));
  }
}
