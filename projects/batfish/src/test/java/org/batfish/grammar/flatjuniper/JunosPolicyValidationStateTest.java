package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.PsThenValidationState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyValidationStateTest {

  private static final String HOSTNAME = "policy-statement-then-validation-state";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("RPKI-POLICY")
            .getTerms()
            .get("UNKNOWN")
            .getThens()
            .getAllThens(),
        contains(new PsThenValidationState(PsThenValidationState.State.UNKNOWN)));
    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("RPKI-POLICY")
            .getTerms()
            .get("VALID")
            .getThens()
            .getAllThens(),
        contains(new PsThenValidationState(PsThenValidationState.State.VALID)));
    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("RPKI-POLICY")
            .getTerms()
            .get("INVALID")
            .getThens()
            .getAllThens(),
        contains(new PsThenValidationState(PsThenValidationState.State.INVALID)));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("validation-state unknown"),
            isTodo("validation-state valid"),
            isTodo("validation-state invalid")));

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("RPKI-POLICY"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
