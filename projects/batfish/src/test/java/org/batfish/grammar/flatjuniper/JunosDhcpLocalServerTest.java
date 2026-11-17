package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.DHCP_LOCAL_SERVER_GROUP_INTERFACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.File;
import java.io.IOException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosDhcpLocalServerTest {

  private static final String HOSTNAME = "routing-instance-dhcp-local-server";

  @TempDir public File _folder;

  @Test
  void testGroupInterface() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement answer =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    assertThat(
        answer,
        hasReferencedStructure(
            "configs/" + HOSTNAME, INTERFACE, "irb.100", DHCP_LOCAL_SERVER_GROUP_INTERFACE));
  }
}
