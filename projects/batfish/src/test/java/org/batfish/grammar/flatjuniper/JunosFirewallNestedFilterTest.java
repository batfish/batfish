package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_FILTER;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_INET6_FILTER;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_TERM_FILTER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.ConcreteFirewallFilter;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosFirewallNestedFilterTest {

  private static final String HOSTNAME = "firewall-nested-filter";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionReferencesAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    ConcreteFirewallFilter parent =
        (ConcreteFirewallFilter)
            juniperConfiguration.getMasterLogicalSystem().getFirewallFilters().get("PARENT");
    ConcreteFirewallFilter parent6 =
        (ConcreteFirewallFilter)
            juniperConfiguration.getMasterLogicalSystem().getFirewallFilters().get("PARENT6");

    assertThat(parent.getTerms().get("NESTED").getFilter(), equalTo("CHILD"));
    assertThat(parent.getTerms().get("UNDEFINED").getFilter(), equalTo("UNKNOWN"));
    assertThat(parent6.getTerms().get("NESTED").getFilter(), equalTo("CHILD6"));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        configuration.getIpAccessLists().get("PARENT").getLines().get(0),
        instanceOf(AclAclLine.class));
    assertThat(
        ((AclAclLine) configuration.getIpAccessLists().get("PARENT").getLines().get(0))
            .getAclName(),
        equalTo("CHILD"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae,
        hasReferencedStructure(filename, FIREWALL_FILTER, "CHILD", FIREWALL_FILTER_TERM_FILTER));
    assertThat(ccae, hasUndefinedReference(filename, FIREWALL_FILTER, "UNKNOWN"));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, FIREWALL_INET6_FILTER, "CHILD6", FIREWALL_FILTER_TERM_FILTER));
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }
}
