package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpDisableLinkLocalAddressTest {

  private static final String HOSTNAME = "bgp-disable-linklocal-addr";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance defaultRi = jc.getMasterLogicalSystem().getDefaultRoutingInstance();

    assertThat(defaultRi.getMasterBgpGroup().getDisableLinkLocalAddress(), equalTo(true));
    BgpGroup inheritedGroup = defaultRi.getNamedBgpGroups().get("INHERITED");
    assertThat(inheritedGroup.getDisableLinkLocalAddress(), nullValue());
    inheritedGroup.cascadeInheritance();
    assertThat(inheritedGroup.getDisableLinkLocalAddress(), equalTo(true));
    assertThat(
        defaultRi.getNamedBgpGroups().get("DIRECT").getDisableLinkLocalAddress(), equalTo(true));
    assertThat(
        defaultRi.getIpBgpGroups().get(Prefix.parse("192.0.2.2/32")).getDisableLinkLocalAddress(),
        equalTo(true));
    assertThat(
        jc.getMasterLogicalSystem()
            .getRoutingInstances()
            .get("RI")
            .getMasterBgpGroup()
            .getDisableLinkLocalAddress(),
        equalTo(true));

    assertThat(getParseWarnings(batfish, HOSTNAME), hasSize(4));
    assertThat(getParseWarnings(batfish, HOSTNAME), everyItem(isTodo("disable-linklocal-addr")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
