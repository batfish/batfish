package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.STATIC_NDP_L2_INTERFACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosStaticNdpTest {

  private static final String HOSTNAME = "junos-static-ndp";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testStaticNdp() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, INTERFACE, "ae0.80", STATIC_NDP_L2_INTERFACE));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("ndp 2001:db8::2 mac 00:de:ad:be:ef:00"),
            isTodo("ndp 2001:db8::3 l2-interface ae0.80 mac 00:de:ad:be:ef:01")));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
