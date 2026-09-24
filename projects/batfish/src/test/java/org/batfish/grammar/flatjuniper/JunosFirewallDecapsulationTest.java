package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.FwThenDecapsulate.Type.GRE;
import static org.batfish.representation.juniper.FwThenDecapsulate.Type.GRE_IN_UDP;
import static org.batfish.representation.juniper.FwThenDecapsulate.Type.MPLS_IN_UDP;
import static org.batfish.representation.juniper.JuniperStructureType.ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_THEN_ROUTING_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.ConcreteFirewallFilter;
import org.batfish.representation.juniper.FwThenDecapsulate;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosFirewallDecapsulationTest {

  private static final String HOSTNAME = "firewall-decapsulation";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionReferencesAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);
    ConcreteFirewallFilter filter =
        (ConcreteFirewallFilter)
            configuration.getMasterLogicalSystem().getFirewallFilters().get("DECAP");

    assertThat(
        filter.getTerms().get("GRE").getThens(), contains(new FwThenDecapsulate(GRE, "DECAP-VRF")));
    assertThat(
        filter.getTerms().get("GRE-UDP").getThens(),
        contains(new FwThenDecapsulate(GRE_IN_UDP, null)));
    assertThat(
        filter.getTerms().get("MPLS-UDP").getThens(),
        contains(new FwThenDecapsulate(MPLS_IN_UDP, null)));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("decapsulate gre routing-instance DECAP-VRF"),
            isTodo("decapsulate gre-in-udp"),
            isTodo("decapsulate mpls-in-udp")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME,
            ROUTING_INSTANCE,
            "DECAP-VRF",
            FIREWALL_FILTER_THEN_ROUTING_INSTANCE));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
