package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureType.MAC_VRF_VLAN;
import static org.batfish.representation.juniper.JuniperStructureUsage.MAC_VRF_VLAN_L3_INTERFACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.MacVrfServiceType;
import org.batfish.representation.juniper.MacVrfVlan;
import org.batfish.representation.juniper.RoutingInstance;
import org.batfish.representation.juniper.RoutingInstanceType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosMacVrfTest {

  private static final String HOSTNAME = "mac-vrf";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testMacVrf() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance evpn = configuration.getMasterLogicalSystem().getRoutingInstances().get("EVPN");
    RoutingInstance vlanBased =
        configuration.getMasterLogicalSystem().getRoutingInstances().get("EVPN-VLAN");
    RoutingInstance vlanBundle =
        configuration.getMasterLogicalSystem().getRoutingInstances().get("EVPN-BUNDLE");

    assertThat(evpn.getInstanceType(), equalTo(RoutingInstanceType.MAC_VRF));
    assertThat(evpn.getMacVrfServiceType(), equalTo(MacVrfServiceType.VLAN_AWARE));
    assertThat(evpn.getMacVrfVlans(), hasKey("USERS"));
    MacVrfVlan users = evpn.getMacVrfVlans().get("USERS");
    assertThat(users.getDescription(), equalTo("User VLAN"));
    assertThat(users.getL3Interface(), equalTo("irb.10"));
    assertThat(users.getVlanId(), equalTo(10));
    assertThat(users.getVniId(), equalTo(10010));
    assertThat(vlanBased.getInstanceType(), equalTo(RoutingInstanceType.MAC_VRF));
    assertThat(vlanBased.getMacVrfServiceType(), equalTo(MacVrfServiceType.VLAN_BASED));
    assertThat(vlanBundle.getInstanceType(), equalTo(RoutingInstanceType.MAC_VRF));
    assertThat(vlanBundle.getMacVrfServiceType(), equalTo(MacVrfServiceType.VLAN_BUNDLE));

    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("instance-type mac-vrf"),
            isTodo("service-type vlan-aware"),
            isTodo("vxlan vni 10010"),
            isTodo("instance-type mac-vrf"),
            isTodo("service-type vlan-based"),
            isTodo("instance-type mac-vrf"),
            isTodo("service-type vlan-bundle")));

    assertThat(
        batfish
            .loadConfigurations(batfish.getSnapshot())
            .get(HOSTNAME)
            .getAllInterfaces()
            .get("irb.10")
            .getVlan(),
        equalTo(10));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(ccae, hasDefinedStructure(filename, MAC_VRF_VLAN, "EVPN USERS"));
    assertThat(
        ccae, hasReferencedStructure(filename, INTERFACE, "irb.10", MAC_VRF_VLAN_L3_INTERFACE));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
