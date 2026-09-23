package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.IpBgpGroup;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpDynamicNeighborTest {

  private static final String HOSTNAME = "junos-bgp-dynamic-neighbor";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDynamicNeighbor() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    IpBgpGroup dynamicNeighbor =
        juniperConfiguration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getIpBgpGroups()
            .get(Prefix.parse("198.51.100.0/24"));

    assertThat(dynamicNeighbor.getDynamic(), equalTo(true));
    assertThat(dynamicNeighbor.getDynamicNeighborName(), equalTo("CLIENTS"));
    assertThat(dynamicNeighbor.getParent().getGroupName(), equalTo("PEERS"));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration config = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);

    assertThat(
        config.getDefaultVrf().getBgpProcess().getPassiveNeighbors().keySet(),
        containsInAnyOrder(Prefix.parse("198.51.100.0/24"), Prefix.parse("203.0.113.0/25")));
    BgpPassivePeerConfig peer =
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getPassiveNeighbors()
            .get(Prefix.parse("198.51.100.0/24"));
    assertThat(peer.getLocalAs(), equalTo(65000L));
    assertThat(peer.getRemoteAsns(), equalTo(LongSpace.of(65100L)));
  }
}
