package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrfs;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.LogicalSystem.MANAGEMENT_ROUTING_INSTANCE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosManagementInstanceTest {

  private static final String HOSTNAME = "system-management-instance";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testManagementInstance() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    assertThat(jc.getMasterLogicalSystem().getManagementInstance(), equalTo(true));

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(c, hasVrfs(hasKey(MANAGEMENT_ROUTING_INSTANCE_NAME)));
    assertThat(c, hasInterface("fxp0", hasVrfName(MANAGEMENT_ROUTING_INSTANCE_NAME)));
    assertThat(c, hasInterface("fxp0.0", hasVrfName(MANAGEMENT_ROUTING_INSTANCE_NAME)));
    assertThat(c, hasInterface("re0:mgmt-0", hasVrfName(MANAGEMENT_ROUTING_INSTANCE_NAME)));
    assertThat(c, hasInterface("re0:mgmt-0.0", hasVrfName(MANAGEMENT_ROUTING_INSTANCE_NAME)));
    assertThat(c, hasInterface("ge-0/0/0.0", hasVrfName(DEFAULT_VRF_NAME)));
  }
}
