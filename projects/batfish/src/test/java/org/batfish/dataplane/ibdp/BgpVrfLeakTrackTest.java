package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpVrfLeakConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrfLeakConfig;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.TrackMethods;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * A BGP VRF leak whose import policy reads a track must be re-evaluated when that track changes.
 *
 * <p>Leaks are otherwise driven by the source VRF's per-round deltas, so a route the policy
 * rejected while the track was failing would never be offered again once it held.
 */
public final class BgpVrfLeakTrackTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final Prefix TRACKED = Prefix.parse("10.9.0.0/24");
  private static final Prefix LEAKED = Prefix.parse("10.9.1.0/24");
  private static final String TRACK = "tracked-learned";
  private static final String DST_VRF = "dst";

  private final NetworkFactory _nf = new NetworkFactory();

  private BgpProcess bgpProcess(Vrf vrf, String routerId) {
    return _nf.bgpProcessBuilder()
        .setRouterId(Ip.parse(routerId))
        .setVrf(vrf)
        .setEbgpAdminCost(20)
        .setIbgpAdminCost(200)
        .setLocalAdminCost(200)
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .build();
  }

  /**
   * r1 originates both prefixes to r2 over eBGP. r2 leaks its default VRF into {@link #DST_VRF}
   * only while {@code trackPrefix} is in r2's default BGP RIB, which holds only once the eBGP
   * session has delivered it: the track starts failing and flips during the data plane.
   */
  private Set<Prefix> leakedInto(Prefix trackPrefix) throws IOException {
    Configuration r1 =
        _nf.configurationBuilder()
            .setHostname("r1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf v1 = _nf.vrfBuilder().setOwner(r1).setName(DEFAULT_VRF_NAME).build();
    _nf.interfaceBuilder()
        .setOwner(r1)
        .setVrf(v1)
        .setAddress(ConcreteInterfaceAddress.parse("10.8.0.0/31"))
        .build();
    for (Prefix p : ImmutableList.of(TRACKED, LEAKED)) {
      v1.getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNetwork(p)
                  .setNextHop(NextHopDiscard.instance())
                  .setAdministrativeCost(1)
                  .build());
    }
    _nf.bgpNeighborBuilder()
        .setBgpProcess(bgpProcess(v1, "1.1.1.1"))
        .setPeerAddress(Ip.parse("10.8.0.1"))
        .setLocalIp(Ip.parse("10.8.0.0"))
        .setLocalAs(65000L)
        .setRemoteAs(65001L)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(
                    _nf.routingPolicyBuilder()
                        .setOwner(r1)
                        .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
                        .build()
                        .getName())
                .build())
        .build();

    Configuration r2 =
        _nf.configurationBuilder()
            .setHostname("r2")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf v2 = _nf.vrfBuilder().setOwner(r2).setName(DEFAULT_VRF_NAME).build();
    _nf.interfaceBuilder()
        .setOwner(r2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("10.8.0.1/31"))
        .build();
    _nf.bgpNeighborBuilder()
        .setBgpProcess(bgpProcess(v2, "2.2.2.2"))
        .setPeerAddress(Ip.parse("10.8.0.0"))
        .setLocalIp(Ip.parse("10.8.0.1"))
        .setLocalAs(65001L)
        .setRemoteAs(65000L)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setImportPolicy(
                    _nf.routingPolicyBuilder()
                        .setOwner(r2)
                        .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
                        .build()
                        .getName())
                .setExportPolicy(
                    _nf.routingPolicyBuilder()
                        .setOwner(r2)
                        .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
                        .build()
                        .getName())
                .build())
        .build();

    r2.getTrackingGroups().put(TRACK, TrackMethods.bgpRoute(trackPrefix, DEFAULT_VRF_NAME));
    Vrf dst = _nf.vrfBuilder().setOwner(r2).setName(DST_VRF).build();
    bgpProcess(dst, "2.2.2.3").setTracks(ImmutableSet.of(TRACK));
    String leakPolicy =
        _nf.routingPolicyBuilder()
            .setOwner(r2)
            .setStatements(
                ImmutableList.of(
                    new If(
                        new TrackSucceeded(TRACK),
                        ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                        ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
            .build()
            .getName();
    dst.setVrfLeakConfig(
        VrfLeakConfig.builder(true)
            .addBgpVrfLeakConfig(
                BgpVrfLeakConfig.builder()
                    .setImportFromVrf(DEFAULT_VRF_NAME)
                    .setImportPolicy(leakPolicy)
                    .setAdmin(20)
                    .setWeight(0)
                    .build())
            .build());

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of("r1", r1, "r2", r2), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish
        .loadDataPlane(batfish.getSnapshot())
        .getRibs()
        .get("r2", DST_VRF)
        .getRoutes()
        .stream()
        .map(AbstractRoute::getNetwork)
        .collect(Collectors.toSet());
  }

  @Test
  public void testALeakGatedOnALateTrackLeaksOnceItHolds() throws IOException {
    assertThat(leakedInto(TRACKED), hasItem(LEAKED));
  }

  @Test
  public void testALeakGatedOnAFailingTrackLeaksNothing() throws IOException {
    // The companion: the route is not leaked by default, only because the track came to hold.
    assertThat(leakedInto(Prefix.parse("10.9.9.0/24")), not(hasItem(LEAKED)));
  }
}
