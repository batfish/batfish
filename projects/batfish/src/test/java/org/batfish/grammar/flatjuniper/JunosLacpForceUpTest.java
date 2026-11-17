package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.LogicalSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosLacpForceUpTest {

  private static final String HOSTNAME = "junos-lacp-force-up";

  @TempDir public File _folder;

  @Test
  void testLacpForceUp() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    LogicalSystem logicalSystem = jc.getMasterLogicalSystem();
    Interface forced = logicalSystem.getInterfaces().get("xe-0/0/0");
    Interface member = logicalSystem.getInterfaces().get("xe-0/0/1");

    assertThat(forced.getLacpForceUp(), is(true));
    assertThat(member.get8023adInterface(), equalTo("ae0"));
    assertThat(logicalSystem.getInterfaceRanges().get("FORCE-UP").getLacpForceUp(), is(true));
    logicalSystem.expandInterfaceRanges();
    Interface inherited = logicalSystem.getInterfaces().get("xe-0/0/2");
    assertThat(inherited.getLacpForceUp(), is(true));
    assertThat(getParseWarnings(batfish, HOSTNAME), hasItem(isTodo("lacp force-up")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
