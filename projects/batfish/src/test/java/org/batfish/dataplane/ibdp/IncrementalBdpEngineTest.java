package org.batfish.dataplane.ibdp;

import static org.batfish.common.topology.TopologyUtil.synthesizeL3Topology;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.Prefix.MAX_PREFIX_LENGTH;
import static org.batfish.datamodel.RoutingProtocol.BGP;
import static org.batfish.datamodel.RoutingProtocol.CONNECTED;
import static org.batfish.datamodel.RoutingProtocol.HMM;
import static org.batfish.datamodel.tracking.TrackMethods.bgpRoute;
import static org.batfish.datamodel.tracking.TrackMethods.negated;
import static org.batfish.datamodel.tracking.TrackMethods.reachability;
import static org.batfish.datamodel.tracking.TrackMethods.route;
import static org.batfish.dataplane.ibdp.IncrementalBdpEngine.compareTracks;
import static org.batfish.dataplane.ibdp.IncrementalBdpEngine.evaluateTrackRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.IpOwnersBaseImpl;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.HmmRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackRoute;
import org.batfish.dataplane.rib.Bgpv4Rib;
import org.batfish.dataplane.rib.Rib;
import org.junit.Test;

/** Test of {@link IncrementalBdpEngine}. */
@ParametersAreNonnullByDefault
public final class IncrementalBdpEngineTest {
  @Test
  public void testCompareTracksAndLogDifference() {
    Table<String, Integer, Boolean> hasA =
        ImmutableTable.<String, Integer, Boolean>builder()
            .put("A", 1, true)
            .put("B", 1, false)
            .build();
    Table<String, Integer, Boolean> hasB =
        ImmutableTable.<String, Integer, Boolean>builder()
            .put("A", 1, false)
            .put("B", 1, true)
            .build();
    Table<String, Integer, Boolean> hasAB =
        ImmutableTable.<String, Integer, Boolean>builder()
            .put("A", 1, true)
            .put("B", 1, true)
            .build();
    assertThat(compareTracks(hasA, hasA).orElse("equal"), equalTo("equal"));
    assertThat(compareTracks(hasAB, hasA).orElse("equal"), equalTo("lost 1 including [B > 1]"));
    assertThat(compareTracks(hasAB, hasB).orElse("equal"), equalTo("lost 1 including [A > 1]"));
    assertThat(compareTracks(hasA, hasAB).orElse("equal"), equalTo("gained 1 including [B > 1]"));
    assertThat(
        compareTracks(hasA, hasB).orElse("equal"),
        equalTo("gained 1 including [B > 1], lost 1 including [A > 1]"));
  }

  @Test
  public void testEvaluateTrackRoute() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Node node = new Node(c);
    Rib rib = node.getVirtualRouter(DEFAULT_VRF_NAME).get().getMainRib();
    Prefix prefix = Prefix.parse("192.0.2.0/24");
    rib.mergeRoute(new AnnotatedRoute<>(new ConnectedRoute(prefix, "foo"), DEFAULT_VRF_NAME));
    TrackRoute trMatchWithoutProtocol =
        (TrackRoute) route(prefix, ImmutableSet.of(), DEFAULT_VRF_NAME);
    TrackRoute trMatchWithProtocol =
        (TrackRoute) route(prefix, ImmutableSet.of(CONNECTED), DEFAULT_VRF_NAME);
    TrackRoute trPrefixMismatch =
        (TrackRoute) route(Prefix.ZERO, ImmutableSet.of(), DEFAULT_VRF_NAME);
    TrackRoute trProtocolMismatch =
        (TrackRoute) route(prefix, ImmutableSet.of(HMM), DEFAULT_VRF_NAME);
    TrackRoute trBgpButNoBgpRib = (TrackRoute) bgpRoute(prefix, DEFAULT_VRF_NAME);

    assertTrue(evaluateTrackRoute(trMatchWithoutProtocol, node));
    assertTrue(evaluateTrackRoute(trMatchWithProtocol, node));
    assertFalse(evaluateTrackRoute(trPrefixMismatch, node));
    assertFalse(evaluateTrackRoute(trProtocolMismatch, node));

    // don't crash, should just return false
    assertFalse(evaluateTrackRoute(trBgpButNoBgpRib, node));
  }

  @Test
  public void testEvaluateTrackRoute_bgp() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    Vrf v = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    BgpProcess.builder()
        .setEbgpAdminCost(100)
        .setIbgpAdminCost(100)
        .setLocalAdminCost(100)
        .setVrf(v)
        .setRouterId(Ip.ZERO)
        .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
        .build();
    Node node = new Node(c);
    Bgpv4Rib rib =
        node.getVirtualRouter(DEFAULT_VRF_NAME).get().getBgpRoutingProcess()._bgpv4RibEbgp;
    Prefix prefix = Prefix.parse("192.0.2.0/24");
    rib.mergeRoute(
        Bgpv4Route.builder()
            .setNetwork(prefix)
            .setProtocol(BGP)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.2.3.4")))
            .setNextHop(NextHopDiscard.instance())
            .build());
    TrackRoute trBgp = (TrackRoute) bgpRoute(prefix, DEFAULT_VRF_NAME);
    TrackRoute trPrefixMismatch = (TrackRoute) bgpRoute(Prefix.ZERO, DEFAULT_VRF_NAME);

    assertTrue(evaluateTrackRoute(trBgp, node));
    assertFalse(evaluateTrackRoute(trPrefixMismatch, node));
  }

  @Test
  public void testComputeDataPlane_trackReachability() {
    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Configuration c2 =
        Configuration.builder()
            .setHostname("c2")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf v1 = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c1).build();
    Vrf v2 = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c2).build();
    TestInterface.builder()
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
        .setVrf(v1)
        .setOwner(c1)
        .build();
    TestInterface.builder()
        .setName("i2")
        .setAddress(ConcreteInterfaceAddress.parse("10.0.0.2/24"))
        .setVrf(v2)
        .setOwner(c2)
        .build();
    c1.setTrackingGroups(
        ImmutableMap.of(
            "succeedsNoIp",
            reachability(Ip.parse("10.0.0.2"), DEFAULT_VRF_NAME),
            "succeedsIp",
            reachability(Ip.parse("10.0.0.2"), DEFAULT_VRF_NAME, Ip.parse("10.0.0.1")),
            "fails",
            reachability(Ip.parse("192.0.2.1"), DEFAULT_VRF_NAME),
            "succeedsExitsNetwork",
            reachability(Ip.parse("10.1.0.1"), DEFAULT_VRF_NAME, Ip.parse("10.0.0.1")),
            "succeedsDeliveredToSubnet",
            reachability(Ip.parse("10.0.0.254"), DEFAULT_VRF_NAME, Ip.parse("10.0.0.1"))));

    StaticRoute srSucceedsNoIp =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.10.0.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setTrack("succeedsNoIp")
            .setAdmin(1)
            .build();
    StaticRoute srSucceedsIp =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.20.0.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setTrack("succeedsIp")
            .setAdmin(1)
            .build();
    StaticRoute srFails =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.30.0.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setAdmin(1)
            .setTrack("fails")
            .build();
    StaticRoute srSucceedsExitsNetwork =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.40.0.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setTrack("succeedsExitsNetwork")
            .setAdmin(1)
            .build();
    StaticRoute srSucceedsDeliveredToSubnet =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.50.0.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setTrack("succeedsDeliveredToSubnet")
            .setAdmin(1)
            .build();
    StaticRoute resolvesTrackSucceedsExitsNetwork =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.1.0.1/32"))
            .setNextHop(NextHopInterface.of("i1"))
            .setAdmin(1)
            .build();
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            srSucceedsNoIp,
            srSucceedsIp,
            srFails,
            srSucceedsExitsNetwork,
            srSucceedsDeliveredToSubnet,
            resolvesTrackSucceedsExitsNetwork));

    Map<String, Configuration> configurations =
        ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    TopologyContext initialTopologyContext =
        TopologyContext.builder().setLayer3Topology(synthesizeL3Topology(configurations)).build();
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    ComputeDataPlaneResult dp =
        engine.computeDataPlane(
            configurations,
            initialTopologyContext,
            ImmutableSet.of(),
            new TestIpOwners(configurations));
    Set<AbstractRoute> installedStaticRoutes =
        dp._dataPlane.getRibs().get(c1.getHostname(), v1.getName()).getRoutes().stream()
            .filter(StaticRoute.class::isInstance)
            .collect(ImmutableSet.toImmutableSet());

    // Only the static routes tracking the reachable IP should be present.
    assertThat(
        installedStaticRoutes,
        containsInAnyOrder(
            srSucceedsNoIp,
            srSucceedsIp,
            srSucceedsExitsNetwork,
            srSucceedsDeliveredToSubnet,
            resolvesTrackSucceedsExitsNetwork));
  }

  @Test
  public void testComputeDataPlane_trackRoute() {
    Configuration c =
        Configuration.builder()
            .setHostname("foo")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    // v1 has static routes that track route presence in v2
    Vrf v1 = Vrf.builder().setName("v1").setOwner(c).build();
    // v2 has tracked static route
    Vrf v2 = Vrf.builder().setName("v2").setOwner(c).build();

    String passingTrackId = "1";
    String failingTrackId = "2";
    c.setTrackingGroups(
        ImmutableMap.of(
            passingTrackId,
            route(Prefix.ZERO, ImmutableSet.of(RoutingProtocol.STATIC), v2.getName()),
            failingTrackId,
            route(Prefix.ZERO, ImmutableSet.of(RoutingProtocol.OSPF), v2.getName())));
    StaticRoute srWithPassingTrack =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.0.0.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setTrack(passingTrackId)
            .setAdmin(1)
            .build();
    StaticRoute srWithFailingTrack =
        StaticRoute.builder()
            .setNetwork(Prefix.strict("10.0.1.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setTrack(failingTrackId)
            .setAdmin(1)
            .build();
    v1.setStaticRoutes(ImmutableSortedSet.of(srWithFailingTrack, srWithPassingTrack));
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.ZERO)
                .setNextHop(NextHopDiscard.instance())
                .setAdmin(1)
                .build()));
    Map<String, Configuration> configurations = ImmutableMap.of(c.getHostname(), c);
    TopologyContext initialTopologyContext = TopologyContext.builder().build();
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    ComputeDataPlaneResult dp =
        engine.computeDataPlane(
            configurations,
            initialTopologyContext,
            ImmutableSet.of(),
            new TestIpOwners(configurations));
    Set<AbstractRoute> installedStaticRoutes =
        dp._dataPlane.getRibs().get(c.getHostname(), v1.getName()).getRoutes().stream()
            .filter(StaticRoute.class::isInstance)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(installedStaticRoutes, contains(srWithPassingTrack));
  }

  @Test
  public void testComputeDataPlane_hmmWithVrrp() {
    // Test that HMM routes reflect post-dataplane VRRP winners.
    // Topology:
    // - h1:i1 <=> vrrp1:i1
    // - h1:i2 <=> vrrp2:i1
    // - vrrp1:i2 <=> vrrp2:i2
    // Notes:
    // - h is running hmm on i1 and i2.
    // - vrrp1 and vrrp2 participate in vrrp on i2, with virtual address 10.0.0.1 installed on i1.
    // - vrrp1 has higher configured priority, but would have lower priority if its dataplane-based
    //   track succeeded.
    // - Since dataplane tracks fail with initial IpOwners, h having hmm route for 10.0.0.1 with
    //   next hop interface i2 means hmm computation is correctly using dataplane IpOwners.
    Ip virtualAddress = Ip.parse("10.0.0.1");
    Configuration h =
        Configuration.builder().setHostname("h").setConfigurationFormat(CISCO_IOS).build();
    Configuration vrrp1 =
        Configuration.builder().setHostname("vrrp1").setConfigurationFormat(CISCO_IOS).build();
    String trackIndex = "1";
    Configuration vrrp2 =
        Configuration.builder().setHostname("vrrp2").setConfigurationFormat(CISCO_IOS).build();
    Vrf hVrf1 = Vrf.builder().setName("v1").setOwner(h).build();
    Vrf hVrf2 = Vrf.builder().setName("v2").setOwner(h).build();
    Vrf vrrp1Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(vrrp1).build();
    Vrf vrrp2Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(vrrp2).build();
    // h interfaces
    Interface hI1 =
        TestInterface.builder()
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("10.1.0.1/24"))
            .setHmm(true)
            .setVrf(hVrf1)
            .setOwner(h)
            .build();
    Interface hI2 =
        TestInterface.builder()
            .setName("i2")
            .setAddress(ConcreteInterfaceAddress.parse("10.2.0.1/24"))
            .setHmm(true)
            .setVrf(hVrf2)
            .setOwner(h)
            .build();
    // vrrp1 interfaces
    ConcreteInterfaceAddress i1HAddress = ConcreteInterfaceAddress.parse("10.1.0.2/24");
    TestInterface.builder()
        .setAddress(i1HAddress)
        .setName("i1")
        .setVrf(vrrp1Vrf)
        .setOwner(vrrp1)
        .build();
    ConcreteInterfaceAddress vrrp1Source = ConcreteInterfaceAddress.parse("192.168.0.1/24");
    vrrp1.setTrackingGroups(
        ImmutableMap.of(
            trackIndex,
            route(vrrp1Source.getPrefix(), ImmutableSet.of(CONNECTED), DEFAULT_VRF_NAME)));
    TestInterface.builder()
        .setAddress(vrrp1Source)
        .setName("i2")
        .setVrrpGroups(
            ImmutableSortedMap.of(
                1,
                VrrpGroup.builder()
                    .setPriority(110)
                    .setSourceAddress(vrrp1Source)
                    .addVirtualAddress("i1", virtualAddress)
                    .setTrackActions(ImmutableMap.of(trackIndex, new DecrementPriority(50)))
                    .build()))
        .setVrf(vrrp1Vrf)
        .setOwner(vrrp1)
        .build();

    // vrrp2 interfaces
    ConcreteInterfaceAddress i2HAddress = ConcreteInterfaceAddress.parse("10.2.0.2/24");
    TestInterface.builder()
        .setAddress(i2HAddress)
        .setName("i1")
        .setVrf(vrrp2Vrf)
        .setOwner(vrrp2)
        .build();
    ConcreteInterfaceAddress vrrp2Source = ConcreteInterfaceAddress.parse("192.168.0.2/24");
    TestInterface.builder()
        .setAddress(vrrp2Source)
        .setName("i2")
        .setVrrpGroups(
            ImmutableSortedMap.of(
                1,
                VrrpGroup.builder()
                    .setPriority(100)
                    .setSourceAddress(vrrp2Source)
                    .addVirtualAddress("i1", virtualAddress)
                    .build()))
        .setVrf(vrrp2Vrf)
        .setOwner(vrrp2)
        .build();

    Map<String, Configuration> configurations =
        ImmutableMap.of(h.getHostname(), h, vrrp1.getHostname(), vrrp1, vrrp2.getHostname(), vrrp2);
    TopologyContext initialTopologyContext =
        TopologyContext.builder().setLayer3Topology(synthesizeL3Topology(configurations)).build();
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    ComputeDataPlaneResult dp =
        engine.computeDataPlane(
            configurations,
            initialTopologyContext,
            ImmutableSet.of(),
            new TestIpOwners(configurations));
    Set<AbstractRoute> h1Vrf1HmmRoutes =
        dp._dataPlane.getRibs().get(h.getHostname(), hVrf1.getName()).getRoutes().stream()
            .filter(HmmRoute.class::isInstance)
            .collect(ImmutableSet.toImmutableSet());
    Set<AbstractRoute> h1Vrf2HmmRoutes =
        dp._dataPlane.getRibs().get(h.getHostname(), hVrf2.getName()).getRoutes().stream()
            .filter(HmmRoute.class::isInstance)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(
        h1Vrf1HmmRoutes,
        containsInAnyOrder(
            HmmRoute.builder()
                .setNetwork(Prefix.create(i1HAddress.getIp(), MAX_PREFIX_LENGTH))
                .setNextHop(NextHopInterface.of(hI1.getName()))
                .build()));
    assertThat(
        h1Vrf2HmmRoutes,
        containsInAnyOrder(
            HmmRoute.builder()
                .setNetwork(Prefix.create(i2HAddress.getIp(), MAX_PREFIX_LENGTH))
                .setNextHop(NextHopInterface.of(hI2.getName()))
                .build(),
            HmmRoute.builder()
                .setNetwork(Prefix.create(virtualAddress, MAX_PREFIX_LENGTH))
                .setNextHop(NextHopInterface.of(hI2.getName()))
                .build()));
  }

  @Test
  public void testKernelRoutesUpdatedMidDataPlane() {
    // Topology: r1 <=> r2
    // - r1 has a kernel route that is activated if it wins VRRP virtual address 192.0.2.10
    // - r1 and r2 connected on interface i1 with IPs 192.0.2.{1,2}
    // - r1 and r2 run VRRP group 1 on i1
    // - r1 has higher initial priority than r2
    // - r1 has track that reduces priority to below that of r2 if reachability to itself fails
    //   (will fail at start of data plane, but not end)
    Ip r1Ip = Ip.parse("192.0.2.1");
    Ip r2Ip = Ip.parse("192.0.2.2");
    ConcreteInterfaceAddress r1Address = ConcreteInterfaceAddress.create(r1Ip, 24);
    ConcreteInterfaceAddress r2Address = ConcreteInterfaceAddress.create(r2Ip, 24);
    Ip virtualIp = Ip.parse("192.0.2.10");
    Configuration r1 =
        Configuration.builder().setHostname("r1").setConfigurationFormat(CISCO_IOS).build();
    Configuration r2 =
        Configuration.builder().setHostname("r2").setConfigurationFormat(CISCO_IOS).build();
    Vrf v1 = Vrf.builder().setOwner(r1).setName(DEFAULT_VRF_NAME).build();
    Vrf v2 = Vrf.builder().setOwner(r2).setName(DEFAULT_VRF_NAME).build();
    Prefix kernelRoutePrefix = Prefix.create(virtualIp, MAX_PREFIX_LENGTH);
    KernelRoute kernelRoute =
        KernelRoute.builder().setNetwork(kernelRoutePrefix).setRequiredOwnedIp(virtualIp).build();
    v1.setKernelRoutes(ImmutableSortedSet.of(kernelRoute));
    v2.setKernelRoutes(ImmutableSortedSet.of(kernelRoute));
    r1.setTrackingGroups(
        ImmutableMap.of("failReach", negated(reachability(r1Ip, DEFAULT_VRF_NAME))));
    VrrpGroup g1 =
        VrrpGroup.builder()
            .setPriority(100)
            .setTrackActions(ImmutableMap.of("failReach", new DecrementPriority(100)))
            .addVirtualAddress("i1", virtualIp)
            .setSourceAddress(r1Address)
            .build();
    VrrpGroup g2 =
        VrrpGroup.builder()
            .setPriority(50)
            .addVirtualAddress("i1", virtualIp)
            .setSourceAddress(r2Address)
            .build();
    TestInterface.builder()
        .setName("i1")
        .setOwner(r1)
        .setVrf(v1)
        .setAddress(r1Address)
        .setVrrpGroups(ImmutableSortedMap.of(1, g1))
        .build();
    TestInterface.builder()
        .setName("i1")
        .setOwner(r2)
        .setVrf(v2)
        .setAddress(r2Address)
        .setVrrpGroups(ImmutableSortedMap.of(1, g2))
        .build();
    Map<String, Configuration> configurations =
        ImmutableMap.of(r1.getHostname(), r1, r2.getHostname(), r2);
    TopologyContext initialTopologyContext =
        TopologyContext.builder().setLayer3Topology(synthesizeL3Topology(configurations)).build();
    IncrementalBdpEngine engine = new IncrementalBdpEngine(new IncrementalDataPlaneSettings());
    IncrementalDataPlane dp =
        (IncrementalDataPlane)
            engine.computeDataPlane(
                    configurations,
                    initialTopologyContext,
                    ImmutableSet.of(),
                    new TestIpOwners(configurations))
                ._dataPlane;

    // r1 should have the kernel route, but r2 should not.
    assertThat(
        dp.getRibsForTesting().get("r1").get(DEFAULT_VRF_NAME).getRoutes(kernelRoutePrefix),
        contains(new AnnotatedRoute<>(kernelRoute, DEFAULT_VRF_NAME)));
    assertThat(
        dp.getRibsForTesting().get("r2").get(DEFAULT_VRF_NAME).getRoutes(kernelRoutePrefix),
        empty());
  }

  private static class TestIpOwners extends IpOwnersBaseImpl {
    protected TestIpOwners(Map<String, Configuration> configurations) {
      super(
          configurations,
          GlobalBroadcastNoPointToPoint.instance(),
          PreDataPlaneTrackMethodEvaluator::new,
          false);
    }
  }
}
