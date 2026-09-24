package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.ConcreteFirewallFilter;
import org.batfish.representation.juniper.FwFromLearnVlanId;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosFirewallLearnVlanIdTest {

  private static final String HOSTNAME = "junos-firewall-learn-vlan-id";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testLearnVlanId() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    Object match =
        ((ConcreteFirewallFilter)
                jc.getMasterLogicalSystem().getFirewallFilters().get("VLAN-FILTER"))
            .getTerms()
            .get("BLOCK")
            .getFroms()
            .get(0);
    assertThat(match, instanceOf(FwFromLearnVlanId.class));
    assertThat(((FwFromLearnVlanId) match).getVlanId(), equalTo(100));
    assertThat(
        ((FwFromLearnVlanId) match).toAclLineMatchExpr(jc, null, new Warnings()),
        instanceOf(FalseExpr.class));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("learn-vlan-id 100"),
            hasComment("Expected vlan number in range 1-4094, but got '4095'")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
