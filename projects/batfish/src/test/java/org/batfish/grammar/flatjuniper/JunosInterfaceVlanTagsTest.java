package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.InterfaceVlanTag;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceVlanTagsTest {

  private static final String HOSTNAME = "interface-vlan-tags";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtraction() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);
    Interface physical = configuration.getMasterLogicalSystem().getInterfaces().get("xe-0/0/0");
    Interface numeric = physical.getUnits().get("xe-0/0/0.100");
    Interface qualified = physical.getUnits().get("xe-0/0/0.200");

    assertThat(numeric.getOuterVlanTag(), equalTo(new InterfaceVlanTag(null, 100)));
    assertThat(numeric.getInnerVlanTag(), equalTo(new InterfaceVlanTag(null, 101)));
    assertThat(qualified.getOuterVlanTag(), equalTo(new InterfaceVlanTag(0x88A8, 200)));
    assertThat(qualified.getInnerVlanTag(), equalTo(new InterfaceVlanTag(0x8100, 201)));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("vlan-tags outer 100 inner 101"),
            isTodo("vlan-tags outer 0x88a8.200 inner 0x8100.201")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
