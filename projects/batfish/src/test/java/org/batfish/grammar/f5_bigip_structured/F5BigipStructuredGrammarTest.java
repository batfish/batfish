package org.batfish.grammar.f5_bigip_structured;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllowedVlans;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasNativeVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isSwitchport;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class F5BigipStructuredGrammarTest {
  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/grammar/f5_bigip_structured/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

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
  public void testHostname() throws IOException {
    String filename = "f5_bigip_structured_hostname";
    String hostname = "myhostname";
    Map<String, Configuration> configurations = parseTextConfigs(filename);

    assertThat(configurations, hasKey(hostname));
  }

  @Test
  public void testInterfaceSpeed() throws IOException {
    Configuration c = parseConfig("f5_bigip_structured_interface");

    assertThat(c.getAllInterfaces().keySet(), containsInAnyOrder("1.0", "2.0"));
    assertThat(c, hasInterface("1.0", hasSpeed(40E9D)));
    assertThat(c, hasInterface("2.0", hasSpeed(100E9D)));
  }

  @Test
  public void testVlan() throws IOException {
    Configuration c = parseConfig("f5_bigip_structured_vlan");
    String portName = "1.0";
    String selfName = "/Common/MYSELF";

    assertThat(c.getAllInterfaces().keySet(), containsInAnyOrder(portName, selfName));

    // port interface
    assertThat(c, hasInterface(portName, isActive()));
    assertThat(c, hasInterface(portName, isSwitchport()));
    assertThat(c, hasInterface(portName, hasSwitchPortMode(SwitchportMode.TRUNK)));
    assertThat(c, hasInterface(portName, hasAllowedVlans(IntegerSpace.of(123))));
    assertThat(c, hasInterface(portName, hasNativeVlan(nullValue())));

    // self interface
    assertThat(c, hasInterface(selfName, isActive()));
    assertThat(c, hasInterface(selfName, hasVlan(123)));
    assertThat(c, hasInterface(selfName, hasAddress("10.0.0.1/24")));
  }
}
