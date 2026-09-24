package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_FROM_INTERFACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyInterfaceNameTest {

  private static final String HOSTNAME = "policy-statement-from-generic-interface-name";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testReferencesAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("INTERFACE-POLICY"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasUndefinedReference(
            "configs/" + HOSTNAME, INTERFACE, "primary", POLICY_STATEMENT_FROM_INTERFACE));
    assertThat(
        ccae,
        hasUndefinedReference(
            "configs/" + HOSTNAME, INTERFACE, "eth-example-vrf", POLICY_STATEMENT_FROM_INTERFACE));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
