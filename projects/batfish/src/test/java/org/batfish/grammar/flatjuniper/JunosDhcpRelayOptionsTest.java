package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.DHCP_RELAY_SERVER_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.DHCP_RELAY_OPTION_RELAY_SERVER_GROUP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosDhcpRelayOptionsTest {

  private static final String HOSTNAME = "junos-dhcp-relay-options";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDhcpRelayOptions() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, DHCP_RELAY_SERVER_GROUP, "FALLBACK", DHCP_RELAY_OPTION_RELAY_SERVER_GROUP));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            DHCP_RELAY_SERVER_GROUP,
            "VENDOR-MISSING",
            DHCP_RELAY_OPTION_RELAY_SERVER_GROUP));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
