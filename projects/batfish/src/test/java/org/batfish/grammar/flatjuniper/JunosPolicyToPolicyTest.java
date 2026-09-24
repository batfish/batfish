package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.POLICY_STATEMENT;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_TO_POLICY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.PsToPolicyStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyToPolicyTest {

  private static final String HOSTNAME = "policy-statement-to-policy";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionReferencesAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("OUTER")
            .getTerms()
            .get("CALL")
            .getTos()
            .getToPolicyStatement(),
        equalTo(new PsToPolicyStatement("SUBROUTINE")));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("policy SUBROUTINE")));

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("OUTER"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, POLICY_STATEMENT, "SUBROUTINE", POLICY_STATEMENT_TO_POLICY));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
