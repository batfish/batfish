package org.batfish.grammar.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.representation.cisco_ftd.FtdBgpNeighbor;
import org.batfish.representation.cisco_ftd.FtdBgpProcess;
import org.batfish.representation.cisco_ftd.FtdConfiguration;
import org.junit.Test;

public class FtdBgpTest extends FtdGrammarTest {

    @Test
    public void testBgpBasic() {
        String config = join(
                "router bgp 65001",
                " bgp router-id 1.1.1.1",
                " bgp log-neighbor-changes",
                " neighbor 2.2.2.2 remote-as 65002",
                " neighbor 2.2.2.2 description PeerA",
                " address-family ipv4 unicast",
                "  neighbor 2.2.2.2 activate",
                "  network 10.0.0.0 mask 255.0.0.0",
                " exit-address-family");

        FtdConfiguration vc = parseVendorConfig(config);
        FtdBgpProcess bgp = vc.getBgpProcess();

        assertThat(bgp, notNullValue());
        assertThat(bgp.getAsn(), equalTo(65001L));
        assertThat(bgp.getRouterId(), equalTo(Ip.parse("1.1.1.1")));

        Map<Ip, FtdBgpNeighbor> neighbors = bgp.getNeighbors();
        assertThat(neighbors.values(), hasSize(1));

        FtdBgpNeighbor n = neighbors.get(Ip.parse("2.2.2.2"));
        assertThat(n.getRemoteAs(), equalTo(65002L));
        assertThat(n.getDescription(), equalTo("PeerA"));
    }

    @Test
    public void testBgpConversion() {
        String config = join(
                "router bgp 65001",
                " bgp router-id 1.1.1.1",
                " neighbor 2.2.2.2 remote-as 65002");

        FtdConfiguration vc = parseVendorConfig(config);
        Configuration c = vc.toVendorIndependentConfigurations().get(0);

        BgpProcess bgpProcess = c.getDefaultVrf().getBgpProcess();
        assertThat(bgpProcess, notNullValue());
        assertThat(bgpProcess.getRouterId(), equalTo(Ip.parse("1.1.1.1")));

        assertThat(bgpProcess.getActiveNeighbors().values(), hasSize(1));
        BgpActivePeerConfig peer = bgpProcess.getActiveNeighbors().get(Ip.parse("2.2.2.2"));
        assertThat(peer.getLocalAs(), equalTo(65001L));
        assertThat(peer.getRemoteAsns(), equalTo(LongSpace.of(65002L)));
        assertThat(peer.getIpv4UnicastAddressFamily(), notNullValue());
    }

    @Test
    public void testBgpNeighborActivationAndTimers() {
        String config = join(
                "router bgp 65001",
                " neighbor 2.2.2.2 remote-as 65002",
                " neighbor 2.2.2.2 timers keepalive 30 holdtime 90",
                " neighbor 2.2.2.2 route-map RM_IN in",
                " neighbor 2.2.2.2 route-map RM_OUT out",
                " address-family ipv4 unicast",
                "  neighbor 2.2.2.2 activate",
                " exit-address-family");

        FtdConfiguration vc = parseVendorConfig(config);
        FtdBgpNeighbor neighbor = vc.getBgpProcess().getNeighbors().get(Ip.parse("2.2.2.2"));
        assertThat(neighbor.getKeepalive(), equalTo(30));
        assertThat(neighbor.getHoldTime(), equalTo(90));
        assertThat(neighbor.getRouteMapIn(), equalTo("RM_IN"));
        assertThat(neighbor.getRouteMapOut(), equalTo("RM_OUT"));
        assertThat(neighbor.isIpv4UnicastActive(), equalTo(true));
    }

    @Test
    public void testBgpConversionRequiresActivationInAf() {
        String config = join(
                "router bgp 65001",
                " bgp router-id 1.1.1.1",
                " neighbor 2.2.2.2 remote-as 65002",
                " address-family ipv4 unicast",
                " exit-address-family");

        FtdConfiguration vc = parseVendorConfig(config);
        Configuration c = vc.toVendorIndependentConfigurations().get(0);
        BgpProcess bgpProcess = c.getVrfs().get(Configuration.DEFAULT_VRF_NAME).getBgpProcess();
        assertThat(bgpProcess.getActiveNeighbors().values(), hasSize(0));
    }
}
