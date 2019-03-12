package org.batfish.bddreachability;

import static org.batfish.bddreachability.TestNetwork.LINK_1_NETWORK;
import static org.batfish.bddreachability.TestNetwork.LINK_2_NETWORK;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BDDReachabilityAnalysisDetectLoopsTest {
  private static final String DST_NODE = "dest";
  private static final String SRC_NODE = "src";
  private static final Prefix DST_PREFIX_1 = Prefix.parse("1.1.0.0/32");
  private static final Prefix DST_PREFIX_2 = Prefix.parse("2.1.0.0/32");

  public @Rule TemporaryFolder _temporaryFolder = new TemporaryFolder();

  @Test
  public void testDetectLoops() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    Configuration dstNode = cb.setHostname(DST_NODE).build();
    Configuration srcNode = cb.setHostname(SRC_NODE).build();
    Vrf dstVrf = vb.setOwner(dstNode).build();
    Vrf srcVrf = vb.setOwner(srcNode).build();

    // first link
    ib.setOwner(srcNode)
        .setVrf(srcVrf)
        .setAddress(
            new InterfaceAddress(LINK_1_NETWORK.getStartIp(), LINK_1_NETWORK.getPrefixLength()))
        .build();
    ib.setOwner(dstNode)
        .setVrf(dstVrf)
        .setAddress(
            new InterfaceAddress(LINK_1_NETWORK.getEndIp(), LINK_1_NETWORK.getPrefixLength()))
        .build();

    // second link
    ib.setOwner(srcNode)
        .setVrf(srcVrf)
        .setAddress(
            new InterfaceAddress(LINK_2_NETWORK.getStartIp(), LINK_2_NETWORK.getPrefixLength()))
        .build();
    ib.setOwner(dstNode)
        .setVrf(dstVrf)
        .setAddress(
            new InterfaceAddress(LINK_2_NETWORK.getEndIp(), LINK_2_NETWORK.getPrefixLength()))
        .build();

    StaticRoute.Builder bld = StaticRoute.builder().setAdministrativeCost(1);
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
    batfish.computeDataPlane();

    BDDPacket pkt = new BDDPacket();

    Set<Location> allLocations =
        LocationSpecifier.ALL_LOCATIONS.resolve(batfish.specifierContext());
    IpSpaceAssignment srcIpSpaceAssignment =
        IpSpaceAssignment.builder().assign(allLocations, UniverseIpSpace.INSTANCE).build();
    BDDReachabilityAnalysis analysis =
        new BDDReachabilityAnalysisFactory(
                pkt, configs, batfish.loadDataPlane().getForwardingAnalysis(), false, false)
            .bddReachabilityAnalysis(srcIpSpaceAssignment);
    analysis.detectLoops();
  }
}
