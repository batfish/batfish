package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.POLICY_STATEMENT;
import static org.batfish.representation.juniper.JuniperStructureUsage.DYNAMIC_TUNNELS_INET_IMPORT_POLICY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.DynamicTunnel;
import org.batfish.representation.juniper.DynamicTunnel.Type;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosDynamicTunnelsTest {

  private static final String HOSTNAME = "junos-dynamic-tunnels";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDynamicTunnels() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance ri = jc.getMasterLogicalSystem().getDefaultRoutingInstance();
    DynamicTunnel ipip = ri.getDynamicTunnels().get("IP-TUNNEL");
    DynamicTunnel udp = ri.getDynamicTunnels().get("UDP-TUNNEL");

    assertThat(ri.getDynamicTunnelsForwardingRib(), equalTo("inet.0"));
    assertThat(ri.getDynamicTunnelsInetImportPolicy(), equalTo("IMPORT-POLICY"));
    assertThat(ipip.getBgpSignal(), is(true));
    assertThat(
        ipip.getDestinationNetworks().get(Prefix.parse("192.0.2.0/24")).getPreference(),
        nullValue());
    assertThat(
        ipip.getDestinationNetworks().get(Prefix.parse("198.51.100.0/24")).getPreference(),
        equalTo(5L));
    assertThat(ipip.getSourceAddress(), equalTo(Ip.parse("192.0.2.1")));
    assertThat(ipip.getType(), equalTo(Type.IPIP));
    assertThat(udp.getType(), equalTo(Type.UDP));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("forwarding-rib inet.0 inet-import IMPORT-POLICY"),
            isTodo("bgp-signal"),
            isTodo("destination-networks 192.0.2.0/24"),
            isTodo("destination-networks 198.51.100.0/24 preference 5"),
            isTodo("ipip"),
            isTodo("source-address 192.0.2.1"),
            isTodo("udp")));
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME,
            POLICY_STATEMENT,
            "IMPORT-POLICY",
            DYNAMIC_TUNNELS_INET_IMPORT_POLICY));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
