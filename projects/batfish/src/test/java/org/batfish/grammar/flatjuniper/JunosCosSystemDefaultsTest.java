package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.CLASS_OF_SERVICE_CLASSIFIER;
import static org.batfish.representation.juniper.JuniperStructureUsage.CLASS_OF_SERVICE_SYSTEM_DEFAULTS_CLASSIFIERS_EXP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosCosSystemDefaultsTest {

  private static final String HOSTNAME = "cos-system-defaults";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testSystemDefaultExpClassifier() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        configuration.getMasterLogicalSystem().getSystemDefaultExpClassifier(),
        equalTo("EXP-DEFAULT"));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("exp EXP-DEFAULT")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME,
            CLASS_OF_SERVICE_CLASSIFIER,
            "EXP-DEFAULT",
            CLASS_OF_SERVICE_SYSTEM_DEFAULTS_CLASSIFIERS_EXP));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
