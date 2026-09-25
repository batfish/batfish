package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.RSTP_INTERFACE;
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

public final class JunosRstpTest {

  private static final String HOSTNAME = "junos-rstp";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testRstp() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(jc.getMasterLogicalSystem().getRstpBridgePriority(), equalTo(61440));
    assertThat(jc.getMasterLogicalSystem().getXstpInterfaceNames(), contains("ge-0/0/0.0"));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("rstp"),
            isTodo("bridge-priority 60k"),
            isTodo("interface all"),
            isTodo("interface ge-0/0/0")));
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure("configs/" + HOSTNAME, INTERFACE, "ge-0/0/0.0", RSTP_INTERFACE));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
