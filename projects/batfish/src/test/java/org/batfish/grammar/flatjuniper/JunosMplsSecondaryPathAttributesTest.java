package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosMplsSecondaryPathAttributesTest {

  private static final String HOSTNAME = "mpls-secondary-path-attributes";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);

    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("secondary BACKUP exclude-srlg"),
            isTodo("secondary BACKUP hop-limit 8"),
            hasComment("Expected MPLS LSP hop-limit in range 2-255, but got '1'"),
            isTodo("secondary BACKUP standby")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
