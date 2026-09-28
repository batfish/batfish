package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.LOGICAL_SYSTEM;
import static org.batfish.representation.juniper.JuniperStructureType.RADSEC_DESTINATION;
import static org.batfish.representation.juniper.JuniperStructureType.ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.ACCESS_RADIUS_SERVER_RADSEC_DESTINATION;
import static org.batfish.representation.juniper.JuniperStructureUsage.ACCESS_RADIUS_SERVER_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.ACCESS_RADSEC_DYNAMIC_REQUESTS_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.ACCESS_RADSEC_LOGICAL_SYSTEM;
import static org.batfish.representation.juniper.JuniperStructureUsage.ACCESS_RADSEC_ROUTING_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosAccessRadiusRadsecTest {

  private static final String CONFIG_NAME = "access-radius-radsec";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testParsingAndReferences() throws IOException {
    Batfish batfish = getBatfish(_folder, CONFIG_NAME);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + CONFIG_NAME;

    assertThat(ccae, hasDefinedStructure(filename, RADSEC_DESTINATION, "1"));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, RADSEC_DESTINATION, "1", ACCESS_RADIUS_SERVER_RADSEC_DESTINATION));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ROUTING_INSTANCE, "MGMT", ACCESS_RADIUS_SERVER_ROUTING_INSTANCE));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ROUTING_INSTANCE, "MGMT", ACCESS_RADSEC_DYNAMIC_REQUESTS_ROUTING_INSTANCE));
    assertThat(
        ccae,
        hasReferencedStructure(filename, LOGICAL_SYSTEM, "TENANT", ACCESS_RADSEC_LOGICAL_SYSTEM));
    assertThat(
        ccae,
        hasReferencedStructure(filename, ROUTING_INSTANCE, "MGMT", ACCESS_RADSEC_ROUTING_INSTANCE));
    assertThat(getParseWarnings(batfish, CONFIG_NAME), empty());
    Warnings warnings = ccae.getWarnings().getOrDefault(CONFIG_NAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
