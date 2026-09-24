package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_FILTER;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_INET6_FILTER;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_RPF_CHECK_FAIL_FILTER;
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

public final class JunosInterfaceRpfCheckTest {

  private static final String HOSTNAME = "junos-interface-rpf-check";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testRpfCheck() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;

    assertThat(
        ccae,
        hasReferencedStructure(filename, FIREWALL_FILTER, "RPF4", INTERFACE_RPF_CHECK_FAIL_FILTER));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, FIREWALL_INET6_FILTER, "RPF6", INTERFACE_RPF_CHECK_FAIL_FILTER));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("rpf-check"),
            isTodo("rpf-check fail-filter RPF4"),
            isTodo("rpf-check mode strict"),
            isTodo("rpf-check"),
            isTodo("rpf-check fail-filter RPF6"),
            isTodo("rpf-check mode loose")));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
