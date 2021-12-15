package org.batfish.grammar.cisco_asa;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_ASA;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CiscoAsaInterfaceTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_asa/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration parseConfig(String hostname) throws IOException {
    String[] names = new String[] {TESTCONFIGS_PREFIX + hostname};
    Map<String, Configuration> configs = BatfishTestUtils.parseTextConfigs(_folder, names);
    assertThat(configs, hasKey(hostname.toLowerCase()));
    Configuration c = configs.get(hostname.toLowerCase());
    assertThat(c.getConfigurationFormat(), equalTo(CISCO_ASA));
    return c;
  }

  @Test
  public void testPortChannel() throws IOException {
    Configuration c = parseConfig("port_channel");
    assertThat(c, hasInterface("Port-channel2", isActive()));
  }

  @Test
  public void testPortChannelNameif() throws IOException {
    Configuration c = parseConfig("port_channel_nameif");
    assertThat(c, hasInterface("inside", isActive()));
  }
}
