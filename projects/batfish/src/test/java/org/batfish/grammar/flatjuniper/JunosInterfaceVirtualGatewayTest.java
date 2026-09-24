package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceVirtualGatewayTest {

  private static final String HOSTNAME = "junos-interface-virtual-gateway";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testVirtualGateway() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    Interface iface =
        jc.getMasterLogicalSystem().getInterfaces().get("irb").getUnits().get("irb.10");

    assertThat(iface.getVirtualGatewayAcceptData(), is(true));
    assertThat(iface.getVirtualGatewayV4Mac(), equalTo(MacAddress.parse("00:00:5e:00:01:01")));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("virtual-gateway-accept-data"),
            isTodo("virtual-gateway-v4-mac 00:00:5e:00:01:01")));
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
