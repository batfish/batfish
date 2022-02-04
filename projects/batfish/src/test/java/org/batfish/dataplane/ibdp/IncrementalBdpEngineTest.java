package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.RoutingProtocol.CONNECTED;
import static org.batfish.datamodel.RoutingProtocol.HMM;
import static org.batfish.dataplane.ibdp.IncrementalBdpEngine.evaluateTrackRoute;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.IpOwnersBaseImpl;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackRoute;
import org.batfish.dataplane.rib.Rib;
import org.junit.Test;

/** Test of {@link IncrementalBdpEngine}. */
@ParametersAreNonnullByDefault
public final class IncrementalBdpEngineTest {
  @Test
  public void testEvaluateTrackRoute() {
    Configuration c =
        Configuration.builder().setHostname("foo").setConfigurationFormat(CISCO_IOS).build();
    Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Node node = new Node(c);
    Rib rib = node.getVirtualRouter(DEFAULT_VRF_NAME).get().getMainRib();
    Prefix prefix = Prefix.parse("192.0.2.0/24");
    rib.mergeRoute(new AnnotatedRoute<>(new ConnectedRoute(prefix, "foo"), DEFAULT_VRF_NAME));
    TrackRoute trMatchWithoutProtocol = TrackRoute.of(prefix, ImmutableSet.of(), DEFAULT_VRF_NAME);
    TrackRoute trMatchWithProtocol =
        TrackRoute.of(prefix, ImmutableSet.of(CONNECTED), DEFAULT_VRF_NAME);
    TrackRoute trPrefixMismatch = TrackRoute.of(Prefix.ZERO, ImmutableSet.of(), DEFAULT_VRF_NAME);
    TrackRoute trProtocolMismatch = TrackRoute.of(prefix, ImmutableSet.of(HMM), DEFAULT_VRF_NAME);

    assertTrue(evaluateTrackRoute(trMatchWithoutProtocol, node));
    assertTrue(evaluateTrackRoute(trMatchWithProtocol, node));
    assertFalse(evaluateTrackRoute(trPrefixMismatch, node));
    assertFalse(evaluateTrackRoute(trProtocolMismatch, node));
  }

  @Test
  public void testComputeDataPlane_track() {
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
            TrackRoute.of(Prefix.ZERO, ImmutableSet.of(RoutingProtocol.STATIC), v2.getName()),
            failingTrackId,
            TrackRoute.of(Prefix.ZERO, ImmutableSet.of(RoutingProtocol.OSPF), v2.getName())));
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
        dp._dataPlane.getRibs().get(c.getHostname()).get(v1.getName()).getRoutes().stream()
            .filter(StaticRoute.class::isInstance)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(installedStaticRoutes, contains(srWithPassingTrack));
  }

  private static class TestIpOwners extends IpOwnersBaseImpl {
    protected TestIpOwners(Map<String, Configuration> configurations) {
      super(
          configurations,
          GlobalBroadcastNoPointToPoint.instance(),
          PreDataPlaneTrackMethodEvaluator::new);
    }
  }
}
