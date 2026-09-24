package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.Interface.DependencyType.BIND;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_DEMUX_UNDERLYING_INTERFACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosDemuxUnderlyingInterfaceTest {

  private static final String HOSTNAME = "demux-underlying-interface";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionReferencesAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration vendorConfiguration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        vendorConfiguration
            .getMasterLogicalSystem()
            .getInterfaces()
            .get("demux0")
            .getUnits()
            .get("demux0.100")
            .getDemuxUnderlyingInterface(),
        equalTo("ae0.0"));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        configuration.getAllInterfaces().get("demux0.100").getDependencies(),
        contains(new Dependency("ae0.0", BIND)));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, INTERFACE, "ae0.0", INTERFACE_DEMUX_UNDERLYING_INTERFACE));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
