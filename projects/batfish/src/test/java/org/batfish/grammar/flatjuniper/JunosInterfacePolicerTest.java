package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_POLICER;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_ARP_POLICER;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_INPUT_POLICER;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_OUTPUT_POLICER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfacePolicerTest {

  private static final String HOSTNAME = "junos-interface-policer";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testInterfacePolicer() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    Interface iface =
        juniperConfiguration
            .getMasterLogicalSystem()
            .getInterfaces()
            .get("ae0")
            .getUnits()
            .get("ae0.0");

    assertThat(iface.getArpPolicer(), equalTo("ARP-POLICER"));
    assertThat(iface.getIncomingPolicer(), equalTo("INPUT4-POLICER"));
    assertThat(iface.getOutgoingPolicer(), equalTo("OUTPUT4-POLICER"));
    assertThat(iface.getIncomingPolicer6(), equalTo("INPUT6-POLICER"));
    assertThat(iface.getOutgoingPolicer6(), equalTo("OUTPUT6-MISSING"));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("policer arp ARP-POLICER"),
            isTodo("policer input INPUT4-POLICER"),
            isTodo("policer output OUTPUT4-POLICER"),
            isTodo("policer input INPUT6-POLICER"),
            isTodo("policer output OUTPUT6-MISSING")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae,
        hasReferencedStructure(filename, FIREWALL_POLICER, "ARP-POLICER", INTERFACE_ARP_POLICER));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, FIREWALL_POLICER, "INPUT4-POLICER", INTERFACE_INPUT_POLICER));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, FIREWALL_POLICER, "OUTPUT4-POLICER", INTERFACE_OUTPUT_POLICER));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, FIREWALL_POLICER, "INPUT6-POLICER", INTERFACE_INPUT_POLICER));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FIREWALL_POLICER, "OUTPUT6-MISSING", INTERFACE_OUTPUT_POLICER));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
