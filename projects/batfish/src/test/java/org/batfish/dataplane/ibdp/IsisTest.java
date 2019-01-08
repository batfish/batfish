package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.RoutingProtocol.ISIS_L1;
import static org.batfish.datamodel.RoutingProtocol.ISIS_L2;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasProtocol;
import static org.batfish.datamodel.matchers.IsisRouteMatchers.hasDown;
import static org.batfish.datamodel.matchers.IsisRouteMatchers.isIsisRouteThat;
import static org.batfish.datamodel.matchers.IsisRouteMatchers.isisRouteTo;
import static org.batfish.dataplane.ibdp.TestUtils.assertNoRoute;
import static org.batfish.dataplane.ibdp.TestUtils.assertRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.isis.IsisProcess;
import org.junit.Ignore;
import org.junit.Test;

public class IsisTest {

  private static final String R1 = "r1";
  private static final String R2 = "r2";
  private static final String R3 = "r3";
  private static final String R4 = "r4";
  private static final String R5 = "r5";

  private static final Ip R1_LOOPBACK_IP = Ip.parse("10.1.1.1");
  private static final Ip R2_LOOPBACK_IP = Ip.parse("10.2.2.2");
  private static final Ip R1_INTERFACE_IP = Ip.parse("10.1.2.1");
  private static final Ip R2_INTERFACE_IP = Ip.parse("10.1.2.2");

  private static void assertInterAreaRoute(
      SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByNode,
      String hostname,
      Prefix prefix,
      long expectedCost) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, SortedSet<AbstractRoute>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(DEFAULT_VRF_NAME));
    SortedSet<AbstractRoute> routes = routesByVrf.get(DEFAULT_VRF_NAME);
    assertThat(routes, hasItem(hasPrefix(prefix)));
    AbstractRoute route =
        routes.stream().filter(r -> r.getNetwork().equals(prefix)).findAny().get();
    assertThat(route, hasMetric(expectedCost));
    assertThat(route, hasProtocol(ISIS_L1));
    assertThat(route, isIsisRouteThat(hasDown()));
  }

  /**
   * Builds dataplane for a pair of devices r1 and r2 with IS-IS set up on both. Interface levels on
   * r1 are customizable to be passive or not.
   *
   * @param r1Level1Passive Whether to make IS-IS passive on level 1 of r1's interface to r2
   * @param r1Level2Passive Whether to make IS-IS passive on level 2 of r1's interface to r2
   * @return
   */
  private IncrementalDataPlane setUpPassiveIsis(boolean r1Level1Passive, boolean r1Level2Passive) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Vrf.Builder vb = nf.vrfBuilder().setName(DEFAULT_VRF_NAME);
    IsisProcess.Builder ipb = IsisProcess.builder();
    Interface.Builder ib = nf.interfaceBuilder();
    IsisInterfaceSettings.Builder iib = IsisInterfaceSettings.builder().setPointToPoint(true);
    IsisInterfaceLevelSettings activeIls =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.ACTIVE).build();
    IsisInterfaceLevelSettings passiveIls =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.PASSIVE).build();
    IsisLevelSettings levelSettings = IsisLevelSettings.builder().build();

    // r1
    Configuration r1 = cb.setHostname(R1).build();
    Vrf v1 = vb.setOwner(r1).build();
    ib.setOwner(r1).setVrf(v1);
    ipb.setVrf(v1)
        .setNetAddress(new IsoAddress("49.0001.0100.0100.1001.00"))
        .setLevel1(levelSettings)
        .setLevel2(levelSettings)
        .build();
    // r1 loopback
    ib.setAddress(new InterfaceAddress(R1_LOOPBACK_IP, 32))
        .setIsis(iib.setLevel1(passiveIls).setLevel2(passiveIls).build())
        .build();
    // r1 interface to r2
    ib.setAddress(new InterfaceAddress(R1_INTERFACE_IP, 24))
        .setIsis(
            iib.setLevel1(r1Level1Passive ? passiveIls : activeIls)
                .setLevel2(r1Level2Passive ? passiveIls : activeIls)
                .build())
        .build();

    // r2
    Configuration r2 = cb.setHostname(R2).build();
    Vrf v2 = vb.setOwner(r2).build();
    ib.setOwner(r2).setVrf(v2);
    ipb.setVrf(v2).setNetAddress(new IsoAddress("49.0001.0100.0200.2002.00")).build();
    // r2 loopback
    ib.setAddress(new InterfaceAddress(R2_LOOPBACK_IP, 32))
        .setIsis(iib.setLevel1(passiveIls).setLevel2(passiveIls).build())
        .build();
    // r2 interface to r1
    ib.setAddress(new InterfaceAddress(R2_INTERFACE_IP, 24))
        .setIsis(iib.setLevel1(activeIls).setLevel2(activeIls).build())
        .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(r1.getHostname(), r1, r2.getHostname(), r2);
    IncrementalBdpEngine engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            (s, i) -> new AtomicInteger());
    Topology topology = CommonUtil.synthesizeTopology(configurations);
    return (IncrementalDataPlane)
        engine.computeDataPlane(configurations, topology, Collections.emptySet())._dataPlane;
  }

  @Test
  public void testIsisPassiveL1() {
    // Test with passive level 1 on r1. Should see each other's loopback addresses in L2 RIB, but
    // not L1, and both should have routes to the other's loopback with ISIS_L2 protocol.
    IncrementalDataPlane dp = setUpPassiveIsis(true, false);
    Prefix r1LoopbackPrefix = Prefix.create(R1_LOOPBACK_IP, 32);
    Prefix r2LoopbackPrefix = Prefix.create(R2_LOOPBACK_IP, 32);
    Prefix isisInterfacePrefix = Prefix.create(R1_INTERFACE_IP, 24);

    Set<IsisRoute> r1L1RibRoutes =
        dp.getNodes().get(R1).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes();
    Set<IsisRoute> r2L1RibRoutes =
        dp.getNodes().get(R2).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes();
    Set<IsisRoute> r1L2RibRoutes =
        dp.getNodes().get(R1).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL2Rib.getRoutes();
    Set<IsisRoute> r2L2RibRoutes =
        dp.getNodes().get(R2).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL2Rib.getRoutes();

    // L1 RIBs lack the other's loopback, but have default route (see VirtualRouter.initIsisImports)
    assertThat(
        r1L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteTo(isisInterfacePrefix, R1_INTERFACE_IP, 0), // 0 because passive
                isisRouteTo(Prefix.ZERO, R1_INTERFACE_IP, 0))));
    assertThat(
        r2L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteTo(isisInterfacePrefix, R2_INTERFACE_IP, 10),
                isisRouteTo(Prefix.ZERO, R2_INTERFACE_IP, 0))));

    // L2 RIBs have the other's loopback
    assertThat(
        r1L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteTo(r2LoopbackPrefix, R2_INTERFACE_IP, 10),
                isisRouteTo(isisInterfacePrefix, R1_INTERFACE_IP, 10))));
    assertThat(
        r2L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r1LoopbackPrefix, R1_INTERFACE_IP, 10),
                isisRouteTo(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteTo(isisInterfacePrefix, R2_INTERFACE_IP, 10),
                // This route comes from r1's passive L1 interface level. It starts with cost 0,
                // then gets upgraded to L2 and sent to r2 with cost 10 (same as previous route).
                isisRouteTo(isisInterfacePrefix, R1_INTERFACE_IP, 10))));

    // Both nodes have an L2 route to the other's loopback
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);
    assertRoute(routes, ISIS_L2, R1, r2LoopbackPrefix, 10L);
    assertRoute(routes, ISIS_L2, R2, r1LoopbackPrefix, 10L);
  }

  @Test
  public void testIsisPassiveL2() {
    // Test with passive level 2 on r1. Should see each other's loopback addresses in L1 RIB, but
    // not L2, and both should have routes to the other's loopback with ISIS_L1 protocol.
    IncrementalDataPlane dp = setUpPassiveIsis(false, true);
    Prefix r1LoopbackPrefix = Prefix.create(R1_LOOPBACK_IP, 32);
    Prefix r2LoopbackPrefix = Prefix.create(R2_LOOPBACK_IP, 32);
    Prefix isisInterfacePrefix = Prefix.parse("10.1.2.0/24");

    Set<IsisRoute> r1L1RibRoutes =
        dp.getNodes().get(R1).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes();
    Set<IsisRoute> r2L1RibRoutes =
        dp.getNodes().get(R2).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes();
    Set<IsisRoute> r1L2RibRoutes =
        dp.getNodes().get(R1).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL2Rib.getRoutes();
    Set<IsisRoute> r2L2RibRoutes =
        dp.getNodes().get(R2).getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL2Rib.getRoutes();

    // L1 RIBs have the other's loopback and default route (see VirtualRouter.initIsisImports)
    assertThat(
        r1L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteTo(r2LoopbackPrefix, R2_INTERFACE_IP, 10),
                isisRouteTo(isisInterfacePrefix, R1_INTERFACE_IP, 10),
                isisRouteTo(Prefix.ZERO, R1_INTERFACE_IP, 0))));
    assertThat(
        r2L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r1LoopbackPrefix, R1_INTERFACE_IP, 10),
                isisRouteTo(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteTo(isisInterfacePrefix, R2_INTERFACE_IP, 10),
                isisRouteTo(Prefix.ZERO, R2_INTERFACE_IP, 0))));

    // L2 RIBs lack the other's loopback
    assertThat(
        r1L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteTo(isisInterfacePrefix, R1_INTERFACE_IP, 0)))); // 0 because passive
    assertThat(
        r2L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteTo(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteTo(isisInterfacePrefix, R2_INTERFACE_IP, 10))));

    // Both nodes have an L1 route to the other's loopback
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);
    assertRoute(routes, ISIS_L1, R1, r2LoopbackPrefix, 10L);
    assertRoute(routes, ISIS_L1, R2, r1LoopbackPrefix, 10L);
  }

  private IncrementalDataPlane computeDataPlane() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Vrf.Builder vb = nf.vrfBuilder().setName(DEFAULT_VRF_NAME);
    IsisProcess.Builder ipb = IsisProcess.builder();
    Interface.Builder ib = nf.interfaceBuilder();
    IsisInterfaceSettings.Builder iib = IsisInterfaceSettings.builder().setPointToPoint(true);
    IsisInterfaceLevelSettings activeIls =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.ACTIVE).build();
    IsisInterfaceLevelSettings passiveIls =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.PASSIVE).build();
    IsisLevelSettings levelSettings = IsisLevelSettings.builder().build();

    // r1
    Configuration r1 = cb.setHostname(R1).build();
    Vrf v1 = vb.setOwner(r1).build();
    ib.setOwner(r1).setVrf(v1);
    iib.setLevel1(null);
    ipb.setVrf(v1)
        .setNetAddress(new IsoAddress("49.0001.0100.0100.1001.00"))
        .setLevel1(null)
        .setLevel2(levelSettings)
        .build();
    // r1l
    ib.setAddress(new InterfaceAddress("10.1.1.1/32"))
        .setIsis(iib.setLevel2(passiveIls).build())
        .build();
    // r1r2
    ib.setAddress(new InterfaceAddress("10.1.2.1/24"))
        .setIsis(iib.setLevel2(activeIls).build())
        .build();
    // r1r3
    ib.setAddress(new InterfaceAddress("10.1.3.1/24"))
        .setIsis(iib.setLevel2(activeIls).build())
        .build();

    // r2
    Configuration r2 = cb.setHostname(R2).build();
    Vrf v2 = vb.setOwner(r2).build();
    ib.setOwner(r2).setVrf(v2);
    iib.setLevel1(null);
    ipb.setVrf(v2)
        .setNetAddress(new IsoAddress("49.0001.0100.0200.2002.00"))
        .setLevel1(null)
        .setLevel2(levelSettings)
        .build();
    // r2l
    ib.setAddress(new InterfaceAddress("10.2.2.2/32"))
        .setIsis(iib.setLevel2(passiveIls).build())
        .build();
    // r2r1
    ib.setAddress(new InterfaceAddress("10.1.2.2/24"))
        .setIsis(iib.setLevel2(activeIls).build())
        .build();
    // r2r3
    ib.setAddress(new InterfaceAddress("10.2.3.2/24"))
        .setIsis(iib.setLevel2(activeIls).build())
        .build();

    // r3
    Configuration r3 = cb.setHostname(R3).build();
    Vrf v3 = vb.setOwner(r3).build();
    ib.setOwner(r3).setVrf(v3);
    ipb.setVrf(v3)
        .setNetAddress(new IsoAddress("49.0001.0100.0300.3003.00"))
        .setLevel1(levelSettings)
        .setLevel2(levelSettings)
        .build();
    // r3l
    ib.setAddress(new InterfaceAddress("10.3.3.3/32"))
        .setIsis(iib.setLevel1(passiveIls).setLevel2(passiveIls).build())
        .build();
    // r3r1
    ib.setAddress(new InterfaceAddress("10.1.3.3/24"))
        .setIsis(iib.setLevel1(null).setLevel2(activeIls).build())
        .build();
    // r3r2
    ib.setAddress(new InterfaceAddress("10.2.3.3/24"))
        .setIsis(iib.setLevel1(null).setLevel2(activeIls).build())
        .build();
    // r3r4
    ib.setAddress(new InterfaceAddress("10.3.4.3/24"))
        .setIsis(iib.setLevel1(activeIls).setLevel2(null).build())
        .build();
    // r3r5
    ib.setAddress(new InterfaceAddress("10.3.5.3/24"))
        .setIsis(iib.setLevel1(activeIls).setLevel2(null).build())
        .build();

    // r4
    Configuration r4 = cb.setHostname(R4).build();
    Vrf v4 = vb.setOwner(r4).build();
    ib.setOwner(r4).setVrf(v4);
    iib.setLevel2(null);
    ipb.setVrf(v4)
        .setNetAddress(new IsoAddress("49.0001.0100.0400.4004.00"))
        .setLevel1(levelSettings)
        .setLevel2(null)
        .build();
    // r4l
    ib.setAddress(new InterfaceAddress("10.4.4.4/32"))
        .setIsis(iib.setLevel1(passiveIls).build())
        .build();
    // r4r3
    ib.setAddress(new InterfaceAddress("10.3.4.4/24"))
        .setIsis(iib.setLevel1(activeIls).build())
        .build();
    // r4r5
    ib.setAddress(new InterfaceAddress("10.4.5.4/24"))
        .setIsis(iib.setLevel1(activeIls).build())
        .build();

    // r5
    Configuration r5 = cb.setHostname(R5).build();
    Vrf v5 = vb.setOwner(r5).build();
    ib.setOwner(r5).setVrf(v5);
    iib.setLevel2(null);
    ipb.setVrf(v5)
        .setNetAddress(new IsoAddress("49.0001.0100.0500.5005.00"))
        .setLevel1(levelSettings)
        .setLevel2(null)
        .build();
    // r5l
    ib.setAddress(new InterfaceAddress("10.5.5.5/32"))
        .setIsis(iib.setLevel1(passiveIls).build())
        .build();
    // r5r3
    ib.setAddress(new InterfaceAddress("10.3.5.5/24"))
        .setIsis(iib.setLevel1(activeIls).build())
        .build();
    // r5r4
    ib.setAddress(new InterfaceAddress("10.4.5.5/24"))
        .setIsis(iib.setLevel1(activeIls).build())
        .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(
            r1.getHostname(),
            r1,
            r2.getHostname(),
            r2,
            r3.getHostname(),
            r3,
            r4.getHostname(),
            r4,
            r5.getHostname(),
            r5);
    IncrementalBdpEngine engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            (s, i) -> new AtomicInteger());
    Topology topology = CommonUtil.synthesizeTopology(configurations);
    IncrementalDataPlane dp =
        (IncrementalDataPlane)
            engine.computeDataPlane(configurations, topology, Collections.emptySet())._dataPlane;
    return dp;
  }

  @Test
  public void testL1AndL2Routes() {
    IncrementalDataPlane dp = computeDataPlane();
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    // r1
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.2.2.2/32"), 10L);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.2.3.0/24"), 20L);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.3.3.3/32"), 10L);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.3.4.0/24"), 20L);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.3.5.0/24"), 20L);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.4.4.4/32"), 20L);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.4.5.0/24"), 30L);
    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.5.5.5/32"), 20L);

    // r2
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.1.1.1/32"), 10L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.1.3.0/24"), 20L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.3.3.3/32"), 10L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.3.4.0/24"), 20L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.3.5.0/24"), 20L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.4.4.4/32"), 20L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.4.5.0/24"), 30L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.5.5.5/32"), 20L);

    // r3
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, ISIS_L2, R3, Prefix.parse("10.1.1.1/32"), 10L);
    assertRoute(routes, ISIS_L2, R3, Prefix.parse("10.1.2.0/24"), 20L);
    assertRoute(routes, ISIS_L2, R3, Prefix.parse("10.2.2.2/32"), 10L);
    assertRoute(routes, ISIS_L1, R3, Prefix.parse("10.4.4.4/32"), 10L);
    assertRoute(routes, ISIS_L1, R3, Prefix.parse("10.4.5.0/24"), 20L);
    assertRoute(routes, ISIS_L1, R3, Prefix.parse("10.5.5.5/32"), 10L);

    // r4
    assertRoute(routes, ISIS_L1, R4, Prefix.ZERO, 10L);
    assertRoute(routes, ISIS_L1, R4, Prefix.parse("10.3.3.3/32"), 10L);
    assertRoute(routes, ISIS_L1, R4, Prefix.parse("10.3.5.0/24"), 20L);
    assertRoute(routes, ISIS_L1, R4, Prefix.parse("10.5.5.5/32"), 10L);

    // r5
    assertRoute(routes, ISIS_L1, R5, Prefix.ZERO, 10L);
    assertRoute(routes, ISIS_L1, R5, Prefix.parse("10.3.3.3/32"), 10L);
    assertRoute(routes, ISIS_L1, R5, Prefix.parse("10.3.4.0/24"), 20L);
    assertRoute(routes, ISIS_L1, R5, Prefix.parse("10.4.4.4/32"), 10L);

    // Assert r1/r2 have empty l1 rib
    assertThat(
        dp.getNodes().get("r1").getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes(),
        empty());
    assertThat(
        dp.getNodes().get("r2").getVirtualRouters().get(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes(),
        empty());

    // Assert r3 has disjoint l1/l2 ribs
    VirtualRouter r3Vr = dp.getNodes().get("r3").getVirtualRouters().get(DEFAULT_VRF_NAME);
    assertThat(
        Sets.intersection(r3Vr._isisL1Rib.getRoutes(), r3Vr._isisL2Rib.getRoutes()), empty());
  }

  @Ignore("https://github.com/batfish/batfish/issues/1703")
  @Test
  public void testLeakedRoutes() {
    IncrementalDataPlane dp = computeDataPlane();
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    assertInterAreaRoute(routes, R4, Prefix.parse("10.1.1.1/32"), 148L);
    assertInterAreaRoute(routes, R4, Prefix.parse("10.1.2.0/24"), 158L);
    assertInterAreaRoute(routes, R4, Prefix.parse("10.1.3.0/24"), 148L);
    assertInterAreaRoute(routes, R4, Prefix.parse("10.2.2.2/32"), 148L);
    assertInterAreaRoute(routes, R4, Prefix.parse("10.2.3.0/24"), 148L);

    assertInterAreaRoute(routes, R5, Prefix.parse("10.1.1.1/32"), 148L);
    assertInterAreaRoute(routes, R5, Prefix.parse("10.1.2.0/24"), 158L);
    assertInterAreaRoute(routes, R5, Prefix.parse("10.1.3.0/24"), 148L);
    assertInterAreaRoute(routes, R5, Prefix.parse("10.2.2.2/32"), 148L);
    assertInterAreaRoute(routes, R5, Prefix.parse("10.2.3.0/24"), 148L);
  }

  @Ignore("https://github.com/batfish/batfish/issues/1703")
  @Test
  public void testRedistributedRoutes() {
    IncrementalDataPlane dp = computeDataPlane();
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.3.3.100/32"), 10L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.3.3.100/32"), 10L);
    assertRoute(routes, ISIS_L1, R4, Prefix.parse("10.3.3.100/32"), 10L);
    assertRoute(routes, ISIS_L1, R5, Prefix.parse("10.3.3.100/32"), 10L);
  }
}
