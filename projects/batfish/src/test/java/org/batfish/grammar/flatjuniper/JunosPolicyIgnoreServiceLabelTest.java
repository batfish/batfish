package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
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

public final class JunosPolicyIgnoreServiceLabelTest {

  private static final String HOSTNAME = "policy-ignore-service-label";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testParsingAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("IGNORE-LABEL"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
