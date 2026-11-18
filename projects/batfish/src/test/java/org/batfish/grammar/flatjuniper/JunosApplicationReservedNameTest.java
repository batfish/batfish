package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosApplicationReservedNameTest {

  private static final String CONFIG_NAME = "application-reserved-name";

  @TempDir public File _folder;

  @Test
  void testAnyInactivityTimeout() throws IOException {
    Batfish batfish = getBatfish(_folder, CONFIG_NAME);
    assertThat(
        getVendorConfiguration(batfish, CONFIG_NAME).getMasterLogicalSystem().getApplications(),
        not(hasKey("any")));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae, not(hasDefinedStructure("configs/" + CONFIG_NAME, APPLICATION, "any")));
    assertThat(getParseWarnings(batfish, CONFIG_NAME), empty());
    Warnings warnings = ccae.getWarnings().getOrDefault(CONFIG_NAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
