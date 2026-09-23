package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
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
import org.batfish.representation.juniper.PsFromState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyStateTest {

  private static final String HOSTNAME = "policy-statement-from-state";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("STATE-POLICY")
            .getTerms()
            .get("ACTIVE")
            .getFroms()
            .getFromState(),
        equalTo(new PsFromState(PsFromState.State.ACTIVE)));
    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("STATE-POLICY")
            .getTerms()
            .get("INACTIVE")
            .getFroms()
            .getFromState(),
        equalTo(new PsFromState(PsFromState.State.INACTIVE)));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(isTodo("state active"), isTodo("state inactive")));

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("STATE-POLICY"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
