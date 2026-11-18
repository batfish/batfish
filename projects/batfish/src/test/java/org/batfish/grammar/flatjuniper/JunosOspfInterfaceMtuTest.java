package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.OspfInterfaceSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosOspfInterfaceMtuTest {

  private static final String HOSTNAME = "junos-ospf-interface-mtu";

  @TempDir public File _folder;

  @Test
  void testInterfaceMtu() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    OspfInterfaceSettings settings =
        jc.getMasterLogicalSystem()
            .getInterfaces()
            .get("ge-0/0/0")
            .getUnits()
            .get("ge-0/0/0.0")
            .getOspfSettings();

    assertThat(settings.getOspfMtu(), equalTo(1500));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("mtu 1500")));
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
