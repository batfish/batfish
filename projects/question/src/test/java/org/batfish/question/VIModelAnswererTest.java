package org.batfish.question;

import static org.batfish.question.VIModelQuestionPlugin.VIModelAnswerer.getBgpEdges;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Iterator;
import java.util.SortedSet;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.IpEdge;
import org.batfish.datamodel.collections.VerboseBgpEdge;
import org.junit.Test;

public class VIModelAnswererTest {

  @Test
  public void testGetBgpEdgesWithNullLocalIp() {
    /*
    Can have edges in BGP topology where one of the peers is missing its local IP; this should not
    cause problems for creating or sorting the VerboseBgpEdge.
     */

    // Set up two correctly configured active peers, except second is missing local IP
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Prefix prefix1 = Prefix.create(ip1, Prefix.MAX_PREFIX_LENGTH);
    Prefix prefix2 = Prefix.create(ip2, Prefix.MAX_PREFIX_LENGTH);

    BgpPeerConfigId id1 = new BgpPeerConfigId("c1", "vrf1", prefix2, false);
    BgpActivePeerConfig peer1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setPeerAddress(ip2)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .build();
    BgpPeerConfigId id2 = new BgpPeerConfigId("c2", "vrf2", prefix1, false);
    BgpActivePeerConfig peer2 =
        BgpActivePeerConfig.builder().setPeerAddress(ip1).setLocalAs(2L).setRemoteAs(1L).build();

    // Create topology with edges between the peers
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> topology =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    topology.putEdgeValue(id1, id2, BgpSessionProperties.from(peer1, peer2, false));
    topology.putEdgeValue(id2, id1, BgpSessionProperties.from(peer1, peer2, true));

    // Create configurations
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    BgpProcess.Builder pb =
        nf.bgpProcessBuilder().setAdminCostsToVendorDefaults(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.setHostname("c1").build();
    Configuration c2 = cb.setHostname("c2").build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).setName("vrf1").build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).setName("vrf2").build();
    BgpProcess proc1 = pb.setVrf(vrf1).setRouterId(Ip.ZERO).build();
    BgpProcess proc2 = pb.setVrf(vrf2).setRouterId(Ip.ZERO).build();
    proc1.setNeighbors(ImmutableSortedMap.of(prefix2, peer1));
    proc2.setNeighbors(ImmutableSortedMap.of(prefix1, peer2));

    // Get BGP edges
    SortedSet<VerboseBgpEdge> edges =
        getBgpEdges(topology, NetworkConfigurations.of(ImmutableSortedMap.of("c1", c1, "c2", c2)));
    assertThat(edges, hasSize(2));
    Iterator<VerboseBgpEdge> edgeIterator = edges.iterator();

    // Compare edge from 1 to 2
    VerboseBgpEdge edge1To2 = edgeIterator.next();
    VerboseBgpEdge expected1To2 =
        new VerboseBgpEdge(peer1, peer2, id1, id2, new IpEdge("c1", ip1, "c2", ip2));
    assertEdgesEqual(edge1To2, expected1To2);

    // Compare edge from 2 to 1
    VerboseBgpEdge edge2To1 = edgeIterator.next();
    VerboseBgpEdge expected2To1 =
        new VerboseBgpEdge(peer2, peer1, id2, id1, new IpEdge("c2", ip2, "c1", ip1));
    assertEdgesEqual(edge2To1, expected2To1);
  }

  private static void assertEdgesEqual(VerboseBgpEdge e1, VerboseBgpEdge e2) {
    assertThat(e1.getEdgeSummary(), equalTo(e2.getEdgeSummary()));
    assertThat(e1.getSession1Id(), equalTo(e2.getSession1Id()));
    assertThat(e1.getSession2Id(), equalTo(e2.getSession2Id()));
    assertThat(e1.getNode1Session(), equalTo(e2.getNode1Session()));
    assertThat(e1.getNode2Session(), equalTo(e2.getNode2Session()));
  }
}
