package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosOspfPreferenceTest {

  private static final String HOSTNAME = "ospf-preference";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance routingInstance =
        juniperConfiguration.getMasterLogicalSystem().getDefaultRoutingInstance();
    RoutingInstance highPreferenceRoutingInstance =
        juniperConfiguration.getMasterLogicalSystem().getRoutingInstances().get("HIGH");

    assertThat(routingInstance.getOspfPreference(), equalTo(180L));
    assertThat(routingInstance.getOspfExternalPreference(), equalTo(181L));
    assertThat(highPreferenceRoutingInstance.getOspfPreference(), equalTo(4294967295L));
    assertThat(highPreferenceRoutingInstance.getOspfExternalPreference(), equalTo(4294967295L));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    OspfProcess ospfProcess = configuration.getDefaultVrf().getOspfProcesses().get("default");
    assertThat(ospfProcess.getAdminCosts().get(RoutingProtocol.OSPF), equalTo(180L));
    assertThat(ospfProcess.getAdminCosts().get(RoutingProtocol.OSPF_IA), equalTo(180L));
    assertThat(ospfProcess.getAdminCosts().get(RoutingProtocol.OSPF_IS), equalTo(180L));
    assertThat(ospfProcess.getAdminCosts().get(RoutingProtocol.OSPF_E1), equalTo(181L));
    assertThat(ospfProcess.getAdminCosts().get(RoutingProtocol.OSPF_E2), equalTo(181L));
    assertThat(ospfProcess.getSummaryAdminCost(), equalTo(180L));
    OspfProcess highPreferenceOspfProcess =
        configuration.getVrfs().get("HIGH").getOspfProcesses().get("HIGH");
    assertThat(
        highPreferenceOspfProcess.getAdminCosts().get(RoutingProtocol.OSPF), equalTo(4294967295L));
    assertThat(
        highPreferenceOspfProcess.getAdminCosts().get(RoutingProtocol.OSPF_E1),
        equalTo(4294967295L));
    assertThat(highPreferenceOspfProcess.getSummaryAdminCost(), equalTo(4294967295L));
  }
}
