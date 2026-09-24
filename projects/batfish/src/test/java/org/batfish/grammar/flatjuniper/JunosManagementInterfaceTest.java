package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasIncomingFilter;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.hasName;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.Interface.InterfaceType.MANAGEMENT;
import static org.batfish.representation.juniper.Interface.InterfaceType.MANAGEMENT_UNIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.NodeDevice;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosManagementInterfaceTest {

  private static final String HOSTNAME = "junos-management-interface";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testManagementInterface() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    NodeDevice node = jc.getNodeDevices().get("re0");

    assertThat(node.getInterfaces().get("re0:mgmt-0").getType(), equalTo(MANAGEMENT));
    assertThat(
        node.getInterfaces().get("re0:mgmt-0").getUnits().get("re0:mgmt-0.0").getType(),
        equalTo(MANAGEMENT_UNIT));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        configuration,
        hasInterface("re0:mgmt-0", hasDescription("Out-of-band management interface")));
    assertThat(
        configuration,
        hasInterface(
            "re0:mgmt-0.0",
            allOf(hasDescription("Management unit"), hasIncomingFilter(hasName("CONTROL_PLANE")))));
    assertThat(
        configuration.getAllInterfaces().get("re0:mgmt-0.0").getConcreteAddress(),
        equalTo(ConcreteInterfaceAddress.parse("192.0.2.1/24")));
  }
}
