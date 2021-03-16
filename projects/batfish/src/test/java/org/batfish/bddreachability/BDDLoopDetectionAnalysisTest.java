package org.batfish.bddreachability;

import static org.batfish.bddreachability.TestNetwork.LINK_1_NETWORK;
import static org.batfish.bddreachability.TestNetwork.LINK_2_NETWORK;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.symbolic.IngressLocation.interfaceLink;
import static org.batfish.symbolic.IngressLocation.vrf;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.StateExpr;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test for {@link BDDLoopDetectionAnalysis}. */
public final class BDDLoopDetectionAnalysisTest {
  private static final String DST_NODE = "dest";
  private static final String SRC_NODE = "src";
  private static final Prefix DST_PREFIX_1 = Prefix.parse("1.1.0.0/32");
  private static final Prefix DST_PREFIX_2 = Prefix.parse("2.1.0.0/32");

  private final BDDPacket _pkt = new BDDPacket();
  private final IpSpaceToBDD _dst = _pkt.getDstIpSpaceToBDD();
  private final BDD _zero = _pkt.getFactory().zero();

  public @Rule TemporaryFolder _temporaryFolder = new TemporaryFolder();

  @Test
  public void testDetectLoopsEndToEnd() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    Configuration dstNode = cb.setHostname(DST_NODE).build();
    Configuration srcNode = cb.setHostname(SRC_NODE).build();
    Vrf dstVrf = vb.setOwner(dstNode).build();
    Vrf srcVrf = vb.setOwner(srcNode).build();

    // first link
    Interface srcNodeIface1 =
        ib.setOwner(srcNode)
            .setVrf(srcVrf)
            .setAddress(
                ConcreteInterfaceAddress.create(
                    LINK_1_NETWORK.getStartIp(), LINK_1_NETWORK.getPrefixLength()))
            .build();
    Interface dstNodeIface1 =
        ib.setOwner(dstNode)
            .setVrf(dstVrf)
            .setAddress(
                ConcreteInterfaceAddress.create(
                    LINK_1_NETWORK.getEndIp(), LINK_1_NETWORK.getPrefixLength()))
            .build();

    // second link
    Interface srcNodeIface2 =
        ib.setOwner(srcNode)
            .setVrf(srcVrf)
            .setAddress(
                ConcreteInterfaceAddress.create(
                    LINK_2_NETWORK.getStartIp(), LINK_2_NETWORK.getPrefixLength()))
            .build();
    Interface dstNodeIface2 =
        ib.setOwner(dstNode)
            .setVrf(dstVrf)
            .setAddress(
                ConcreteInterfaceAddress.create(
                    LINK_2_NETWORK.getEndIp(), LINK_2_NETWORK.getPrefixLength()))
            .build();

    StaticRoute.Builder bld = StaticRoute.testBuilder().setAdministrativeCost(1);
    srcVrf.setStaticRoutes(
        ImmutableSortedSet.of(
            bld.setNetwork(DST_PREFIX_1).setNextHopIp(LINK_1_NETWORK.getEndIp()).build(),
            bld.setNetwork(DST_PREFIX_2).setNextHopIp(LINK_2_NETWORK.getEndIp()).build()));
    dstVrf.setStaticRoutes(
        ImmutableSortedSet.of(
            bld.setNetwork(DST_PREFIX_1).setNextHopIp(LINK_2_NETWORK.getStartIp()).build(),
            bld.setNetwork(DST_PREFIX_2).setNextHopIp(LINK_1_NETWORK.getStartIp()).build()));

    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(srcNode.getHostname(), srcNode, dstNode.getHostname(), dstNode);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _temporaryFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();

    Set<Location> allLocations =
        LocationSpecifier.ALL_LOCATIONS.resolve(batfish.specifierContext(snapshot));
    IpSpaceAssignment srcIpSpaceAssignment =
        IpSpaceAssignment.builder().assign(allLocations, UniverseIpSpace.INSTANCE).build();
    batfish.computeDataPlane(snapshot);
    DataPlane dataPlane = batfish.loadDataPlane(snapshot);
    BDDLoopDetectionAnalysis analysis =
        new BDDReachabilityAnalysisFactory(
                _pkt,
                configs,
                dataPlane.getForwardingAnalysis(),
                new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
                false,
                false)
            .bddLoopDetectionAnalysis(srcIpSpaceAssignment);

    Map<IngressLocation, BDD> actual = analysis.detectLoops();

    BDD loopBdd = _dst.toBDD(DST_PREFIX_1).or(_dst.toBDD(DST_PREFIX_2));
    Map<IngressLocation, BDD> expected =
        ImmutableMap.<IngressLocation, BDD>builder()
            // src node locations
            .put(interfaceLink(SRC_NODE, srcNodeIface1.getName()), loopBdd)
            .put(interfaceLink(SRC_NODE, srcNodeIface2.getName()), loopBdd)
            .put(vrf(SRC_NODE, srcVrf.getName()), loopBdd)

            // dst node locations
            .put(interfaceLink(DST_NODE, dstNodeIface1.getName()), loopBdd)
            .put(interfaceLink(DST_NODE, dstNodeIface2.getName()), loopBdd)
            .put(vrf(DST_NODE, dstVrf.getName()), loopBdd)
            .build();
    assertEquals(expected, actual);
  }

  @Test
  public void testNoFalsePositives() {
    StateExpr state1 = new OriginateVrf("node1", "vrf1");
    StateExpr state2 = new OriginateVrf("node2", "vrf2");

    // there's a topological cycle, but the composed edge constraints don't permit anything
    Map<IngressLocation, BDD> actual =
        new BDDLoopDetectionAnalysis(
                _pkt,
                Stream.of(
                    new Edge(state1, state2, constraint(_dst.toBDD(DST_PREFIX_1))),
                    new Edge(state2, state1, constraint(_dst.toBDD(DST_PREFIX_2)))),
                ImmutableSet.of(state1, state2))
            .detectLoops();

    Map<IngressLocation, BDD> expected =
        ImmutableMap.of(vrf("node1", "vrf1"), _zero, vrf("node2", "vrf2"), _zero);

    assertEquals(expected, actual);
  }
}
