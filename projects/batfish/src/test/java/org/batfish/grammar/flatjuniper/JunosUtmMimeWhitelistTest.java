package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosUtmMimeWhitelistTest {

  private static final String CONFIG_NAME = "utm-mime-whitelist";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testParsing() throws IOException {
    Batfish batfish = getBatfish(_folder, CONFIG_NAME);
    assertThat(getParseWarnings(batfish, CONFIG_NAME), empty());

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    Warnings warnings = ccae.getWarnings().getOrDefault(CONFIG_NAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
