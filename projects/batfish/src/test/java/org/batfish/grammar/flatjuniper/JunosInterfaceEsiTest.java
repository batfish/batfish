package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.Interface.EthernetSegmentRedundancyMode.ALL_ACTIVE;
import static org.batfish.representation.juniper.Interface.EthernetSegmentRedundancyMode.SINGLE_ACTIVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceEsiTest {

  private static final String HOSTNAME = "junos-interface-esi";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testInterfaceEsi() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    Interface identifier = jc.getMasterLogicalSystem().getInterfaces().get("ae0");
    Interface allActive = jc.getMasterLogicalSystem().getInterfaces().get("ae1");
    Interface singleActive = jc.getMasterLogicalSystem().getInterfaces().get("ae2");

    assertThat(identifier.getEthernetSegmentIdentifier(), equalTo("00:00:00:00:00:00:00:00:00:01"));
    assertThat(allActive.getEthernetSegmentRedundancyMode(), equalTo(ALL_ACTIVE));
    assertThat(singleActive.getEthernetSegmentRedundancyMode(), equalTo(SINGLE_ACTIVE));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("esi 00:00:00:00:00:00:00:00:00:01"),
            isTodo("esi all-active"),
            isTodo("esi single-active")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
