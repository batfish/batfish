package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpSocketBuffersTest {

  private static final String HOSTNAME = "junos-bgp-socket-buffers";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance ri = jc.getMasterLogicalSystem().getDefaultRoutingInstance();
    BgpGroup master = ri.getMasterBgpGroup();
    BgpGroup group = ri.getNamedBgpGroups().get("TEST");
    BgpGroup receiveOverride = ri.getIpBgpGroups().get(Prefix.parse("192.0.2.2/32"));
    BgpGroup sendOverride = ri.getIpBgpGroups().get(Prefix.parse("192.0.2.3/32"));

    assertThat(master.getReceiveBufferBytes(), equalTo(64L * 1024));
    assertThat(master.getSendBufferBytes(), equalTo(64L * 1024));
    assertThat(group.getReceiveBufferBytes(), equalTo(1024L * 1024));
    assertThat(group.getSendBufferBytes(), nullValue());
    assertThat(receiveOverride.getReceiveBufferBytes(), equalTo(256L * 1024));
    assertThat(receiveOverride.getSendBufferBytes(), nullValue());
    assertThat(sendOverride.getReceiveBufferBytes(), nullValue());
    assertThat(sendOverride.getSendBufferBytes(), equalTo(1024L * 1024 * 1024));

    receiveOverride.cascadeInheritance();
    sendOverride.cascadeInheritance();
    assertThat(receiveOverride.getSendBufferBytes(), equalTo(64L * 1024));
    assertThat(sendOverride.getReceiveBufferBytes(), equalTo(1024L * 1024));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors(), hasKey(Ip.parse("192.0.2.2")));
    assertThat(
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors(), hasKey(Ip.parse("192.0.2.3")));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault("configs/" + HOSTNAME, new Warnings()).getRedFlagWarnings(),
        empty());
  }
}
