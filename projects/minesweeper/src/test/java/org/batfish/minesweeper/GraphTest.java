package org.batfish.minesweeper;

import static org.batfish.minesweeper.Graph.generateIbgpNeighbors;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Map;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.minesweeper.Graph} */
@RunWith(JUnit4.class)
public class GraphTest {

  NetworkFactory _nf;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
  }

  /*

  3-node Ibgp network, using interface (!) and not loopback peerings

                        +-------+  r2
                        |
    r1 +----------------+
                        |
                        +-------+  r3

   */
  private SortedMap<String, Configuration> threeNodeIbgpNet() {
    Builder cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    // Non-default vrfs are not handled yet
    Vrf.Builder vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Interface.Builder ib = _nf.interfaceBuilder();
    BgpProcess.Builder bpb =
        _nf.bgpProcessBuilder().setAdminCostsToVendorDefaults(ConfigurationFormat.CISCO_IOS);
    BgpActivePeerConfig.Builder bnb = _nf.bgpNeighborBuilder().setLocalAs(1L).setRemoteAs(1L);

    Configuration c1 = cb.setHostname("r1").build();

    Vrf v1 = vb.setOwner(c1).build();
    ib.setOwner(c1).setVrf(v1).setAddress(ConcreteInterfaceAddress.parse("1.1.2.1/30"));
    ib.setOwner(c1).setVrf(v1).setAddress(ConcreteInterfaceAddress.parse("1.1.3.1/30"));
    BgpProcess bp1 = bpb.setVrf(v1).setRouterId(Ip.parse("1.1.1.1")).build();
    bnb.setLocalIp(Ip.parse("1.1.2.1"))
        .setPeerAddress(Ip.parse("1.1.2.2"))
        .setBgpProcess(bp1)
        .build();
    bnb.setLocalIp(Ip.parse("1.1.3.1"))
        .setPeerAddress(Ip.parse("1.1.3.3"))
        .setBgpProcess(bp1)
        .build();

    Configuration c2 = cb.setHostname("r2").build();
    Vrf v2 = vb.setOwner(c2).build();
    ib.setOwner(c2).setVrf(v2).setAddress(ConcreteInterfaceAddress.parse("1.1.2.2/30"));
    BgpProcess bp2 = bpb.setVrf(v2).setRouterId(Ip.parse("2.2.2.2")).build();
    bnb.setLocalIp(Ip.parse("1.1.2.2"))
        .setPeerAddress(Ip.parse("1.1.2.1"))
        .setBgpProcess(bp2)
        .build();

    Configuration c3 = cb.setHostname("r3").build();
    Vrf v3 = vb.setOwner(c3).build();
    ib.setOwner(c3).setVrf(v3).setAddress(ConcreteInterfaceAddress.parse("1.1.3.3/30"));
    BgpProcess bp3 = bpb.setVrf(v3).setRouterId(Ip.parse("3.3.3.3")).build();
    bnb.setLocalIp(Ip.parse("1.1.3.3"))
        .setPeerAddress(Ip.parse("1.1.3.1"))
        .setBgpProcess(bp3)
        .build();

    return ImmutableSortedMap.of("r1", c1, "r2", c2, "r3", c3);
  }

  @Test
  public void testNeighborComputation() {
    Map<String, Configuration> configs = threeNodeIbgpNet();
    Map<String, Map<String, BgpActivePeerConfig>> neighborMap =
        generateIbgpNeighbors(configs).asMap();

    assertThat(neighborMap, hasEntry(equalTo("r1"), allOf(hasKey("r2"), hasKey("r3"))));
    assertThat(neighborMap, hasEntry(equalTo("r2"), hasKey("r1")));
    assertThat(neighborMap, hasEntry(equalTo("r3"), hasKey("r1")));

    // Assert right configs have been put in the map (based on IPs)
    assertThat(neighborMap.get("r1").get("r2").getLocalIp(), equalTo(Ip.parse("1.1.2.1")));
    assertThat(neighborMap.get("r1").get("r3").getLocalIp(), equalTo(Ip.parse("1.1.3.1")));
  }

  @Test
  public void testCommunityAtomicPredicates() {
    IBatfish batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return threeNodeIbgpNet();
          }

          @Override
          public TopologyProvider getTopologyProvider() {
            return new TopologyProviderTestAdapter(this) {
              @Override
              public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
                return Topology.EMPTY;
              }
            };
          }

          @Override
          public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
            return ImmutableMap.of();
          }
        };
    Graph g =
        new Graph(
            batfish,
            batfish.getSnapshot(),
            null,
            null,
            ImmutableSet.of(
                new RegexCommunitySet("^2[0-3]:40$"),
                new RegexCommunitySet("^21:4[0-3]$"),
                new LiteralCommunity(StandardCommunity.parse("20:40")),
                new LiteralCommunity(StandardCommunity.parse("22:22"))));

    assertEquals(g.getNumAtomicPredicates(), 5);

    Automaton a1 = new RegExp("20:40").toAutomaton();
    Automaton a2 = new RegExp("21:40").toAutomaton();
    Automaton a3 = new RegExp("2[2-3]:40").toAutomaton();
    Automaton a4 = new RegExp("21:4[1-3]").toAutomaton();
    Automaton a5 = new RegExp("22:22").toAutomaton();

    assertEquals(g.getAtomicPredicateAutomata().size(), 5);
    assertThat(g.getAtomicPredicateAutomata().values(), hasItem(a1));
    assertThat(g.getAtomicPredicateAutomata().values(), hasItem(a2));
    assertThat(g.getAtomicPredicateAutomata().values(), hasItem(a3));
    assertThat(g.getAtomicPredicateAutomata().values(), hasItem(a4));
    assertThat(g.getAtomicPredicateAutomata().values(), hasItem(a5));

    assertEquals(g.getCommunityAtomicPredicates().size(), 4);
    assertThat(
        g.getCommunityAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from("^2[0-3]:40$")), iterableWithSize(3)));
    assertThat(
        g.getCommunityAtomicPredicates(),
        hasEntry(equalTo(CommunityVar.from("^21:4[0-3]$")), iterableWithSize(2)));
    assertThat(
        g.getCommunityAtomicPredicates(),
        hasEntry(
            equalTo(CommunityVar.from(StandardCommunity.parse("20:40"))), iterableWithSize(1)));
    assertThat(
        g.getCommunityAtomicPredicates(),
        hasEntry(
            equalTo(CommunityVar.from(StandardCommunity.parse("22:22"))), iterableWithSize(1)));
  }
}
