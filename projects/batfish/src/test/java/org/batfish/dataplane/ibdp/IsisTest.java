package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.RoutingProtocol.ISIS_L1;
import static org.batfish.datamodel.RoutingProtocol.ISIS_L2;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.batfish.datamodel.matchers.IsisRouteMatchers.hasDown;
import static org.batfish.datamodel.matchers.IsisRouteMatchers.isIsisRouteThat;
import static org.batfish.datamodel.matchers.IsisRouteMatchers.isisRouteWith;
import static org.batfish.dataplane.ibdp.TestUtils.assertIsisRoute;
import static org.batfish.dataplane.ibdp.TestUtils.assertNoRoute;
import static org.batfish.dataplane.ibdp.TestUtils.assertRoute;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.isis.IsisTopology;
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
  private static final Ip R3_LOOPBACK_IP = Ip.parse("10.3.3.3");
  private static final Ip R4_LOOPBACK_IP = Ip.parse("10.4.4.4");
  private static final Ip R5_LOOPBACK_IP = Ip.parse("10.5.5.5");

  private static final int INTERFACE_PREFIX_LENGTH = 24;

  private static final Ip R1_TO_R2_IP = Ip.parse("10.1.2.1");
  private static final Ip R2_TO_R1_IP = Ip.parse("10.1.2.2");
  private static final Prefix R1_R2_PREFIX = Prefix.create(R1_TO_R2_IP, INTERFACE_PREFIX_LENGTH);

  private static final Ip R1_TO_R3_IP = Ip.parse("10.1.3.1");
  private static final Ip R3_TO_R1_IP = Ip.parse("10.1.3.3");
  private static final Prefix R1_R3_PREFIX = Prefix.create(R1_TO_R3_IP, INTERFACE_PREFIX_LENGTH);

  private static final Ip R2_TO_R3_IP = Ip.parse("10.2.3.2");
  private static final Ip R3_TO_R2_IP = Ip.parse("10.2.3.3");
  private static final Prefix R2_R3_PREFIX = Prefix.create(R2_TO_R3_IP, INTERFACE_PREFIX_LENGTH);

  private static final Ip R2_TO_R4_IP = Ip.parse("10.2.4.2");
  private static final Ip R4_TO_R2_IP = Ip.parse("10.2.4.4");

  private static final Ip R3_TO_R4_IP = Ip.parse("10.3.4.3");
  private static final Ip R4_TO_R3_IP = Ip.parse("10.3.4.4");
  private static final Prefix R3_R4_PREFIX = Prefix.create(R3_TO_R4_IP, INTERFACE_PREFIX_LENGTH);

  private static final Ip R3_TO_R5_IP = Ip.parse("10.3.5.3");
  private static final Ip R5_TO_R3_IP = Ip.parse("10.3.5.5");
  private static final Prefix R3_R5_PREFIX = Prefix.create(R3_TO_R5_IP, INTERFACE_PREFIX_LENGTH);

  private static final Ip R4_TO_R5_IP = Ip.parse("10.4.5.4");
  private static final Ip R5_TO_R4_IP = Ip.parse("10.4.5.5");
  private static final Prefix R4_R5_PREFIX = Prefix.create(R4_TO_R5_IP, INTERFACE_PREFIX_LENGTH);

  private static void assertInterAreaRoute(
      SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routesByNode,
      String hostname,
      Prefix prefix,
      long expectedCost) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, Set<AbstractRoute>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(DEFAULT_VRF_NAME));
    Set<AbstractRoute> routes = routesByVrf.get(DEFAULT_VRF_NAME);
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
  private IbdpResult setUpPassiveIsis(boolean r1Level1Passive, boolean r1Level2Passive) {
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
    ib.setAddress(ConcreteInterfaceAddress.create(R1_LOOPBACK_IP, Prefix.MAX_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(passiveIls).setLevel2(passiveIls).build())
        .build();
    // r1 interface to r2
    ib.setAddress(ConcreteInterfaceAddress.create(R1_TO_R2_IP, INTERFACE_PREFIX_LENGTH))
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
    ib.setAddress(ConcreteInterfaceAddress.create(R2_LOOPBACK_IP, Prefix.MAX_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(passiveIls).setLevel2(passiveIls).build())
        .build();
    // r2 interface to r1
    ib.setAddress(ConcreteInterfaceAddress.create(R2_TO_R1_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(activeIls).setLevel2(activeIls).build())
        .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(r1.getHostname(), r1, r2.getHostname(), r2);
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    Topology topology = TopologyUtil.synthesizeL3Topology(configurations);
    return (IbdpResult)
        engine.computeDataPlane(
            configurations,
            TopologyContext.builder()
                .setLayer3Topology(topology)
                .setIsisTopology(IsisTopology.initIsisTopology(configurations, topology))
                .build(),
            Collections.emptySet());
  }

  @Test
  public void testIsisPassiveL1() {
    // Test with passive level 1 on r1. Should see each other's loopback addresses in L2 RIB, but
    // not L1, and both should have routes to the other's loopback with ISIS_L2 protocol.
    IbdpResult dp = setUpPassiveIsis(true, false);
    Prefix r1LoopbackPrefix = R1_LOOPBACK_IP.toPrefix();
    Prefix r2LoopbackPrefix = R2_LOOPBACK_IP.toPrefix();

    Set<IsisRoute> r1L1RibRoutes =
        dp.getNodes().get(R1).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL1Rib.getTypedRoutes();
    Set<IsisRoute> r2L1RibRoutes =
        dp.getNodes().get(R2).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL1Rib.getTypedRoutes();
    Set<IsisRoute> r1L2RibRoutes =
        dp.getNodes().get(R1).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL2Rib.getTypedRoutes();
    Set<IsisRoute> r2L2RibRoutes =
        dp.getNodes().get(R2).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL2Rib.getTypedRoutes();

    // L1 RIBs lack the other's loopback, but have default route (see VirtualRouter.initIsisImports)
    assertThat(
        r1L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteWith(R1_R2_PREFIX, R1_TO_R2_IP, 0), // 0 because passive
                isisRouteWith(Prefix.ZERO, Route.UNSET_ROUTE_NEXT_HOP_IP, 0))));
    assertThat(
        r2L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteWith(R1_R2_PREFIX, R2_TO_R1_IP, 10),
                isisRouteWith(Prefix.ZERO, Route.UNSET_ROUTE_NEXT_HOP_IP, 0))));

    // L2 RIBs have the other's loopback
    assertThat(
        r1L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteWith(r2LoopbackPrefix, R2_TO_R1_IP, 10),
                isisRouteWith(R1_R2_PREFIX, R1_TO_R2_IP, 10))));
    assertThat(
        r2L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r1LoopbackPrefix, R1_TO_R2_IP, 10),
                isisRouteWith(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteWith(R1_R2_PREFIX, R2_TO_R1_IP, 10),
                // This route comes from r1's passive L1 interface level. It starts with cost 0,
                // then gets upgraded to L2 and sent to r2 with cost 10 (same as previous route).
                isisRouteWith(R1_R2_PREFIX, R1_TO_R2_IP, 10))));

    // Both nodes have an L2 route to the other's loopback
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes((IncrementalDataPlane) dp._dataPlane);
    assertRoute(routes, ISIS_L2, R1, r2LoopbackPrefix, 10L);
    assertRoute(routes, ISIS_L2, R2, r1LoopbackPrefix, 10L);
  }

  @Test
  public void testIsisPassiveL2() {
    // Test with passive level 2 on r1. Should see each other's loopback addresses in L1 RIB, but
    // not L2, and both should have routes to the other's loopback with ISIS_L1 protocol.
    IbdpResult dp = setUpPassiveIsis(false, true);
    Prefix r1LoopbackPrefix = R1_LOOPBACK_IP.toPrefix();
    Prefix r2LoopbackPrefix = R2_LOOPBACK_IP.toPrefix();

    Set<IsisRoute> r1L1RibRoutes =
        dp.getNodes().get(R1).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL1Rib.getTypedRoutes();
    Set<IsisRoute> r2L1RibRoutes =
        dp.getNodes().get(R2).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL1Rib.getTypedRoutes();
    Set<IsisRoute> r1L2RibRoutes =
        dp.getNodes().get(R1).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL2Rib.getTypedRoutes();
    Set<IsisRoute> r2L2RibRoutes =
        dp.getNodes().get(R2).getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL2Rib.getTypedRoutes();

    // L1 RIBs have the other's loopback and default route (see VirtualRouter.initIsisImports)
    assertThat(
        r1L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteWith(r2LoopbackPrefix, R2_TO_R1_IP, 10),
                isisRouteWith(R1_R2_PREFIX, R1_TO_R2_IP, 10),
                isisRouteWith(Prefix.ZERO, Route.UNSET_ROUTE_NEXT_HOP_IP, 0))));
    assertThat(
        r2L1RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r1LoopbackPrefix, R1_TO_R2_IP, 10),
                isisRouteWith(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteWith(R1_R2_PREFIX, R2_TO_R1_IP, 10),
                isisRouteWith(Prefix.ZERO, Route.UNSET_ROUTE_NEXT_HOP_IP, 0))));

    // L2 RIBs lack the other's loopback
    assertThat(
        r1L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r1LoopbackPrefix, R1_LOOPBACK_IP, 0),
                isisRouteWith(R1_R2_PREFIX, R1_TO_R2_IP, 0)))); // 0 because passive
    assertThat(
        r2L2RibRoutes,
        containsInAnyOrder(
            ImmutableList.of(
                isisRouteWith(r2LoopbackPrefix, R2_LOOPBACK_IP, 0),
                isisRouteWith(R1_R2_PREFIX, R2_TO_R1_IP, 10))));

    // Both nodes have an L1 route to the other's loopback
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes((IncrementalDataPlane) dp._dataPlane);
    assertRoute(routes, ISIS_L1, R1, r2LoopbackPrefix, 10L);
    assertRoute(routes, ISIS_L1, R2, r1LoopbackPrefix, 10L);
  }

  /*
  Setup:
          R1 - R3 - R4
           \  /  \  /
            R2    R5

   R1 and R2 are L2 only, R4 and R5 are L1 only. R3 interfaces to R1 and R2 are L2 only, to R4 and
   R5 are L1 only.
   */
  private IbdpResult computeDataPlane() {
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
    ib.setAddress(ConcreteInterfaceAddress.create(R1_LOOPBACK_IP, 32))
        .setIsis(iib.setLevel2(passiveIls).build())
        .build();
    // r1r2
    ib.setAddress(ConcreteInterfaceAddress.create(R1_TO_R2_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel2(activeIls).build())
        .build();
    // r1r3
    ib.setAddress(ConcreteInterfaceAddress.create(R1_TO_R3_IP, INTERFACE_PREFIX_LENGTH))
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
    ib.setAddress(ConcreteInterfaceAddress.create(R2_LOOPBACK_IP, 32))
        .setIsis(iib.setLevel2(passiveIls).build())
        .build();
    // r2r1
    ib.setAddress(ConcreteInterfaceAddress.create(R2_TO_R1_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel2(activeIls).build())
        .build();
    // r2r3
    ib.setAddress(ConcreteInterfaceAddress.create(R2_TO_R3_IP, INTERFACE_PREFIX_LENGTH))
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
    ib.setAddress(ConcreteInterfaceAddress.create(R3_LOOPBACK_IP, 32))
        .setIsis(iib.setLevel1(passiveIls).setLevel2(passiveIls).build())
        .build();
    // r3r1
    ib.setAddress(ConcreteInterfaceAddress.create(R3_TO_R1_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(null).setLevel2(activeIls).build())
        .build();
    // r3r2
    ib.setAddress(ConcreteInterfaceAddress.create(R3_TO_R2_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(null).setLevel2(activeIls).build())
        .build();
    // r3r4
    ib.setAddress(ConcreteInterfaceAddress.create(R3_TO_R4_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(activeIls).setLevel2(null).build())
        .build();
    // r3r5
    ib.setAddress(ConcreteInterfaceAddress.create(R3_TO_R5_IP, INTERFACE_PREFIX_LENGTH))
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
    ib.setAddress(ConcreteInterfaceAddress.create(R4_LOOPBACK_IP, 32))
        .setIsis(iib.setLevel1(passiveIls).build())
        .build();
    // r4r3
    ib.setAddress(ConcreteInterfaceAddress.create(R4_TO_R3_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(activeIls).build())
        .build();
    // r4r5
    ib.setAddress(ConcreteInterfaceAddress.create(R4_TO_R5_IP, INTERFACE_PREFIX_LENGTH))
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
    ib.setAddress(ConcreteInterfaceAddress.create(R5_LOOPBACK_IP, 32))
        .setIsis(iib.setLevel1(passiveIls).build())
        .build();
    // r5r3
    ib.setAddress(ConcreteInterfaceAddress.create(R5_TO_R3_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(iib.setLevel1(activeIls).build())
        .build();
    // r5r4
    ib.setAddress(ConcreteInterfaceAddress.create(R5_TO_R4_IP, INTERFACE_PREFIX_LENGTH))
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
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    Topology topology = TopologyUtil.synthesizeL3Topology(configurations);
    return (IbdpResult)
        engine.computeDataPlane(
            configurations,
            TopologyContext.builder()
                .setLayer3Topology(topology)
                .setIsisTopology(IsisTopology.initIsisTopology(configurations, topology))
                .build(),
            Collections.emptySet());
  }

  @Test
  public void testL1AndL2Routes() {
    IbdpResult dp = computeDataPlane();
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes((IncrementalDataPlane) dp._dataPlane);

    // R1 is directly connected to R2 and R3, can reach R4 and R5 through R3
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, ISIS_L2, R1, R2_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L2, R1, R2_R3_PREFIX, 20L);
    assertRoute(routes, ISIS_L2, R1, R3_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L2, R1, R3_R4_PREFIX, 20L);
    assertRoute(routes, ISIS_L2, R1, R3_R5_PREFIX, 20L);
    assertRoute(routes, ISIS_L2, R1, R4_LOOPBACK_IP.toPrefix(), 20L);
    assertRoute(routes, ISIS_L2, R1, R4_R5_PREFIX, 30L);
    assertRoute(routes, ISIS_L2, R1, R5_LOOPBACK_IP.toPrefix(), 20L);

    // R2 is directly connected to R1 and R3, can reach R4 and R5 through R3
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, ISIS_L2, R2, R1_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L2, R2, R1_R3_PREFIX, 20L);
    assertRoute(routes, ISIS_L2, R2, R3_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L2, R2, R3_R4_PREFIX, 20L);
    assertRoute(routes, ISIS_L2, R2, R3_R5_PREFIX, 20L);
    assertRoute(routes, ISIS_L2, R2, R4_LOOPBACK_IP.toPrefix(), 20L);
    assertRoute(routes, ISIS_L2, R2, R4_R5_PREFIX, 30L);
    assertRoute(routes, ISIS_L2, R2, R5_LOOPBACK_IP.toPrefix(), 20L);

    // R3 is directly connected to all other nodes
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, ISIS_L2, R3, R1_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L2, R3, R1_R2_PREFIX, 20L);
    assertRoute(routes, ISIS_L2, R3, R2_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L1, R3, R4_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L1, R3, R4_R5_PREFIX, 20L);
    assertRoute(routes, ISIS_L1, R3, R5_LOOPBACK_IP.toPrefix(), 10L);

    // R4 is directly connected to R3 and R5; no R1/R2 routes because those are L2 and R4 is L1 only
    assertRoute(routes, ISIS_L1, R4, Prefix.ZERO, 10L);
    assertRoute(routes, ISIS_L1, R4, R3_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L1, R4, R3_R5_PREFIX, 20L);
    assertRoute(routes, ISIS_L1, R4, R5_LOOPBACK_IP.toPrefix(), 10L);

    // R5 is directly connected to R3 and R4; no R1/R2 routes because those are L2 and R5 is L1 only
    assertRoute(routes, ISIS_L1, R5, Prefix.ZERO, 10L);
    assertRoute(routes, ISIS_L1, R5, R3_LOOPBACK_IP.toPrefix(), 10L);
    assertRoute(routes, ISIS_L1, R5, R3_R4_PREFIX, 20L);
    assertRoute(routes, ISIS_L1, R5, R4_LOOPBACK_IP.toPrefix(), 10L);

    // Assert r1/r2 have empty l1 rib
    assertThat(
        dp.getNodes().get("r1").getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes(),
        empty());
    assertThat(
        dp.getNodes().get("r2").getVirtualRouterOrThrow(DEFAULT_VRF_NAME)._isisL1Rib.getRoutes(),
        empty());

    // Assert r3 has disjoint l1/l2 ribs
    VirtualRouter r3Vr = dp.getNodes().get("r3").getVirtualRouterOrThrow(DEFAULT_VRF_NAME);
    assertThat(
        Sets.intersection(r3Vr._isisL1Rib.getRoutes(), r3Vr._isisL2Rib.getRoutes()), empty());
  }

  /* Sets up a 4-node network. See details in testIsisOverload() */
  private IncrementalDataPlane setUpOverloadIsis() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Vrf.Builder vb = nf.vrfBuilder().setName(DEFAULT_VRF_NAME);
    Interface.Builder ib = nf.interfaceBuilder();
    IsisInterfaceLevelSettings active =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.ACTIVE).build();
    IsisInterfaceLevelSettings passive =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.PASSIVE).build();
    IsisInterfaceSettings loopbackIfaceSettings =
        IsisInterfaceSettings.builder()
            .setPointToPoint(true)
            .setLevel1(passive)
            .setLevel2(passive)
            .build();
    IsisInterfaceSettings nonLoopbackIfaceSettings =
        IsisInterfaceSettings.builder()
            .setPointToPoint(true)
            .setLevel1(active)
            .setLevel2(active)
            .build();
    IsisLevelSettings levelSettings = IsisLevelSettings.builder().build();
    IsisProcess.Builder ipb = IsisProcess.builder().setLevel1(levelSettings);

    // r1
    Configuration r1 = cb.setHostname(R1).build();
    Vrf v1 = vb.setOwner(r1).build();
    ib.setOwner(r1).setVrf(v1);
    ipb.setVrf(v1).setNetAddress(new IsoAddress("49.0001.0100.0100.1001.00")).build();
    // r1l
    ib.setAddress(ConcreteInterfaceAddress.create(R1_LOOPBACK_IP, 32))
        .setIsis(loopbackIfaceSettings)
        .build();
    // r1r2
    ib.setAddress(ConcreteInterfaceAddress.create(R1_TO_R2_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(nonLoopbackIfaceSettings)
        .build();
    // r1r3
    ib.setAddress(ConcreteInterfaceAddress.create(R1_TO_R3_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(nonLoopbackIfaceSettings)
        .build();

    // r2 with overload set
    Configuration r2 = cb.setHostname(R2).build();
    Vrf v2 = vb.setOwner(r2).build();
    ib.setOwner(r2).setVrf(v2);
    IsisProcess.builder()
        .setVrf(v2)
        .setNetAddress(new IsoAddress("49.0001.0100.0200.2002.00"))
        .setLevel1(levelSettings)
        .setLevel2(levelSettings)
        .setOverload(true)
        .build();
    // r2l
    ib.setAddress(ConcreteInterfaceAddress.create(R2_LOOPBACK_IP, 32))
        .setIsis(loopbackIfaceSettings)
        .build();
    // r2r1
    ib.setAddress(ConcreteInterfaceAddress.create(R2_TO_R1_IP, 24))
        .setIsis(nonLoopbackIfaceSettings)
        .build();
    // r2r4
    ib.setAddress(ConcreteInterfaceAddress.create(R2_TO_R4_IP, 24))
        .setIsis(nonLoopbackIfaceSettings)
        .build();

    // r3
    Configuration r3 = cb.setHostname(R3).build();
    Vrf v3 = vb.setOwner(r3).build();
    ib.setOwner(r3).setVrf(v3);
    ipb.setVrf(v3).setNetAddress(new IsoAddress("49.0001.0100.0300.3003.00")).build();
    IsisInterfaceLevelSettings cost20LevelSettings =
        IsisInterfaceLevelSettings.builder().setMode(IsisInterfaceMode.ACTIVE).setCost(20L).build();
    IsisInterfaceSettings cost20InterfaceSettings =
        IsisInterfaceSettings.builder()
            .setPointToPoint(true)
            .setLevel1(cost20LevelSettings)
            .build();
    // r3l
    ib.setAddress(ConcreteInterfaceAddress.create(R3_LOOPBACK_IP, 32))
        .setIsis(loopbackIfaceSettings)
        .build();
    // r3r1
    ib.setAddress(ConcreteInterfaceAddress.create(R3_TO_R1_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(cost20InterfaceSettings)
        .build();
    // r3r4
    ib.setAddress(ConcreteInterfaceAddress.create(R3_TO_R4_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(cost20InterfaceSettings)
        .build();

    // r4
    Configuration r4 = cb.setHostname(R4).build();
    Vrf v4 = vb.setOwner(r4).build();
    ib.setOwner(r4).setVrf(v4);
    ipb.setVrf(v4).setNetAddress(new IsoAddress("49.0001.0100.0400.4004.00")).build();
    // r4l
    ib.setAddress(ConcreteInterfaceAddress.create(R4_LOOPBACK_IP, 32))
        .setIsis(loopbackIfaceSettings)
        .build();
    // r4r2
    ib.setAddress(ConcreteInterfaceAddress.create(R4_TO_R2_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(nonLoopbackIfaceSettings)
        .build();
    // r4r3
    ib.setAddress(ConcreteInterfaceAddress.create(R4_TO_R3_IP, INTERFACE_PREFIX_LENGTH))
        .setIsis(nonLoopbackIfaceSettings)
        .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(
            r1.getHostname(), r1, r2.getHostname(), r2, r3.getHostname(), r3, r4.getHostname(), r4);
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    Topology topology = TopologyUtil.synthesizeL3Topology(configurations);
    return (IncrementalDataPlane)
        engine.computeDataPlane(
                configurations,
                TopologyContext.builder()
                    .setLayer3Topology(topology)
                    .setIsisTopology(IsisTopology.initIsisTopology(configurations, topology))
                    .build(),
                Collections.emptySet())
            ._dataPlane;
  }

  @Test
  public void testIsisOverload() {
    /*
     All routers have IS-IS configured on level 1 on all interfaces; R2 also has level 2 configured.
     R2 is overloaded; R3 has outgoing IS-IS metric of 20 on both interfaces.
     R1 and R4 should prefer to traverse R3 despite higher metric because R2 is overloaded.
     R1-R2 prefix: 10.1.2.0/24
     R1-R3 prefix: 10.1.3.0/24
     R2-R4 prefix: 10.1.4.0/24
     R3-R4 prefix: 10.1.5.0/24

       +-- R2 --+
      /          \
    R1            R4
      \          /
       +-- R3 --+
    */

    IncrementalDataPlane dp = setUpOverloadIsis();
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);
    Prefix r1LoopbackPrefix = R1_LOOPBACK_IP.toPrefix();
    Prefix r2LoopbackPrefix = R2_LOOPBACK_IP.toPrefix();
    Prefix r3LoopbackPrefix = R3_LOOPBACK_IP.toPrefix();
    Prefix r4LoopbackPrefix = R4_LOOPBACK_IP.toPrefix();

    // r2 is overloaded. Other three routers should all have routes to each other's loopbacks that
    // don't traverse r2.
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R1, r3LoopbackPrefix, 10, R3_TO_R1_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R1, r4LoopbackPrefix, 30, R3_TO_R1_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R3, r1LoopbackPrefix, 20, R1_TO_R3_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R3, r4LoopbackPrefix, 20, R4_TO_R3_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R4, r1LoopbackPrefix, 30, R3_TO_R4_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R4, r3LoopbackPrefix, 10, R3_TO_R4_IP, false);

    // r1, r3, and r4 should all have an overloaded route to r2's loopback. r3 should have routes
    // through both r1 and r4.
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R1, r2LoopbackPrefix, 10, R2_TO_R1_IP, true);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R3, r2LoopbackPrefix, 30, R1_TO_R3_IP, true);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R3, r2LoopbackPrefix, 30, R4_TO_R3_IP, true);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R4, r2LoopbackPrefix, 10, R2_TO_R4_IP, true);

    // r2 is a Level 1-2 router, but it should not have advertised a default route due to overload.
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertNoRoute(routes, R4, Prefix.ZERO);

    // r2's own routes shouldn't be overloaded.
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R2, r1LoopbackPrefix, 10, R1_TO_R2_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R2, r3LoopbackPrefix, 20, R1_TO_R2_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R2, r3LoopbackPrefix, 20, R4_TO_R2_IP, false);
    assertIsisRoute(routes, RoutingProtocol.ISIS_L1, R2, r4LoopbackPrefix, 10, R4_TO_R2_IP, false);
  }

  @Ignore("https://github.com/batfish/batfish/issues/1703")
  @Test
  public void testLeakedRoutes() {
    IbdpResult dp = computeDataPlane();
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes((IncrementalDataPlane) dp._dataPlane);

    assertInterAreaRoute(routes, R4, R1_LOOPBACK_IP.toPrefix(), 148L);
    assertInterAreaRoute(routes, R4, R1_R2_PREFIX, 158L);
    assertInterAreaRoute(routes, R4, R1_R3_PREFIX, 148L);
    assertInterAreaRoute(routes, R4, R2_LOOPBACK_IP.toPrefix(), 148L);
    assertInterAreaRoute(routes, R4, R2_R3_PREFIX, 148L);

    assertInterAreaRoute(routes, R5, R1_LOOPBACK_IP.toPrefix(), 148L);
    assertInterAreaRoute(routes, R5, R1_R2_PREFIX, 158L);
    assertInterAreaRoute(routes, R5, R1_R3_PREFIX, 148L);
    assertInterAreaRoute(routes, R5, R2_LOOPBACK_IP.toPrefix(), 148L);
    assertInterAreaRoute(routes, R5, R2_R3_PREFIX, 148L);
  }

  @Ignore("https://github.com/batfish/batfish/issues/1703")
  @Test
  public void testRedistributedRoutes() {
    IbdpResult dp = computeDataPlane();
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes((IncrementalDataPlane) dp._dataPlane);

    assertRoute(routes, ISIS_L2, R1, Prefix.parse("10.3.3.100/32"), 10L);
    assertRoute(routes, ISIS_L2, R2, Prefix.parse("10.3.3.100/32"), 10L);
    assertRoute(routes, ISIS_L1, R4, Prefix.parse("10.3.3.100/32"), 10L);
    assertRoute(routes, ISIS_L1, R5, Prefix.parse("10.3.3.100/32"), 10L);
  }
}
