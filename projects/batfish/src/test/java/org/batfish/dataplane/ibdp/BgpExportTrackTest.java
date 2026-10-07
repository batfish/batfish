package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.TrackMethods;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * A BGP export policy that reads a track must be re-applied to what a session already carries when
 * that track changes.
 *
 * <p>r1 sends {@link #TRACKED} and {@link #EXPORTED} to r2, which exports to r3 under a policy
 * reading a track that holds once {@link #TRACKED} is in r2's BGP RIB. The track starts failing, so
 * r3 first receives what the policy gives a failing track, and then has to end up with what it
 * gives a holding one.
 */
public final class BgpExportTrackTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final Prefix TRACKED = Prefix.parse("10.9.0.0/24");
  private static final Prefix EXPORTED = Prefix.parse("10.9.1.0/24");
  private static final Prefix REDISTRIBUTED = Prefix.parse("10.9.2.0/24");
  private static final String TRACK = "tracked-learned";

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

  private String policy(Configuration c, List<Statement> statements) {
    return _nf.routingPolicyBuilder().setOwner(c).setStatements(statements).build().getName();
  }

  private String acceptAll(Configuration c) {
    return policy(c, ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
  }

  private static Configuration node(NetworkFactory nf, String name) {
    return nf.configurationBuilder()
        .setHostname(name)
        .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
        .build();
  }

  /**
   * The data plane with r2 exporting to r3 under {@code r2Export}, which may read {@link #TRACK}.
   * r2 also holds {@link #REDISTRIBUTED} as a static, for the export policy to redistribute.
   */
  private DataPlane dataPlane(List<Statement> r2Export) throws IOException {
    Configuration r1 = node(_nf, "r1");
    Vrf v1 = _nf.vrfBuilder().setOwner(r1).setName(DEFAULT_VRF_NAME).build();
    _nf.interfaceBuilder()
        .setOwner(r1)
        .setVrf(v1)
        .setAddress(ConcreteInterfaceAddress.parse("10.8.0.0/31"))
        .build();
    for (Prefix p : ImmutableList.of(TRACKED, EXPORTED)) {
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
            Ipv4UnicastAddressFamily.builder().setExportPolicy(acceptAll(r1)).build())
        .build();

    Configuration r2 = node(_nf, "r2");
    Vrf v2 = _nf.vrfBuilder().setOwner(r2).setName(DEFAULT_VRF_NAME).build();
    _nf.interfaceBuilder()
        .setOwner(r2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("10.8.0.1/31"))
        .build();
    _nf.interfaceBuilder()
        .setOwner(r2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("10.8.1.0/31"))
        .build();
    v2.getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setNetwork(REDISTRIBUTED)
                .setNextHop(NextHopDiscard.instance())
                .setAdministrativeCost(1)
                .build());
    BgpProcess p2 = bgpProcess(v2, "2.2.2.2");
    p2.setTracks(ImmutableSet.of(TRACK));
    r2.getTrackingGroups().put(TRACK, TrackMethods.bgpRoute(TRACKED, DEFAULT_VRF_NAME));
    String reject = policy(r2, ImmutableList.of(Statements.ExitReject.toStaticStatement()));
    _nf.bgpNeighborBuilder()
        .setBgpProcess(p2)
        .setPeerAddress(Ip.parse("10.8.0.0"))
        .setLocalIp(Ip.parse("10.8.0.1"))
        .setLocalAs(65001L)
        .setRemoteAs(65000L)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setImportPolicy(acceptAll(r2))
                .setExportPolicy(reject)
                .build())
        .build();
    _nf.bgpNeighborBuilder()
        .setBgpProcess(p2)
        .setPeerAddress(Ip.parse("10.8.1.1"))
        .setLocalIp(Ip.parse("10.8.1.0"))
        .setLocalAs(65001L)
        .setRemoteAs(65002L)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(policy(r2, r2Export)).build())
        .build();

    Configuration r3 = node(_nf, "r3");
    Vrf v3 = _nf.vrfBuilder().setOwner(r3).setName(DEFAULT_VRF_NAME).build();
    _nf.interfaceBuilder()
        .setOwner(r3)
        .setVrf(v3)
        .setAddress(ConcreteInterfaceAddress.parse("10.8.1.1/31"))
        .build();
    _nf.bgpNeighborBuilder()
        .setBgpProcess(bgpProcess(v3, "3.3.3.3"))
        .setPeerAddress(Ip.parse("10.8.1.0"))
        .setLocalIp(Ip.parse("10.8.1.1"))
        .setLocalAs(65002L)
        .setRemoteAs(65001L)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setImportPolicy(acceptAll(r3))
                .setExportPolicy(
                    policy(r3, ImmutableList.of(Statements.ExitReject.toStaticStatement())))
                .build())
        .build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of("r1", r1, "r2", r2, "r3", r3), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish.loadDataPlane(batfish.getSnapshot());
  }

  private static Set<Bgpv4Route> r3Routes(DataPlane dp, Prefix prefix) {
    return dp.getBgpRoutes().get("r3", DEFAULT_VRF_NAME).stream()
        .filter(r -> r.getNetwork().equals(prefix))
        .collect(Collectors.toSet());
  }

  private static Set<Prefix> r3Networks(DataPlane dp) {
    return dp.getRibs().get("r3", DEFAULT_VRF_NAME).getRoutes().stream()
        .map(AbstractRoute::getNetwork)
        .collect(Collectors.toSet());
  }

  private static If matching(Prefix p, List<Statement> then) {
    return new If(
        new MatchPrefixSet(
            DestinationNetwork.instance(),
            new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(p)))),
        then);
  }

  @Test
  public void testARouteTheTrackStopsExportingIsWithdrawn() throws IOException {
    // Exported while the track fails, then rejected once it holds.
    DataPlane dp =
        dataPlane(
            ImmutableList.of(
                matching(
                    EXPORTED,
                    ImmutableList.of(
                        new If(
                            new TrackSucceeded(TRACK),
                            ImmutableList.of(Statements.ExitReject.toStaticStatement()),
                            ImmutableList.of(Statements.ExitAccept.toStaticStatement())))),
                Statements.ExitReject.toStaticStatement()));

    assertThat(r3Routes(dp, EXPORTED), empty());
  }

  @Test
  public void testARouteTheTrackStartsExportingIsAdded() throws IOException {
    // The companion: rejected while the track fails, exported once it holds.
    DataPlane dp =
        dataPlane(
            ImmutableList.of(
                matching(
                    EXPORTED,
                    ImmutableList.of(
                        new If(
                            new TrackSucceeded(TRACK),
                            ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                            ImmutableList.of(Statements.ExitReject.toStaticStatement())))),
                Statements.ExitReject.toStaticStatement()));

    assertThat(r3Networks(dp), hasItem(EXPORTED));
  }

  @Test
  public void testARouteTheTrackChangesIsReplacedNotDuplicated() throws IOException {
    // Exported with MED 0 while the track fails and with MED 7 once it holds: r3 keeps only the
    // second.
    DataPlane dp =
        dataPlane(
            ImmutableList.of(
                matching(
                    EXPORTED,
                    ImmutableList.of(
                        new If(
                            new TrackSucceeded(TRACK),
                            ImmutableList.of(new SetMetric(new LiteralLong(7)))),
                        Statements.ExitAccept.toStaticStatement())),
                Statements.ExitReject.toStaticStatement()));

    assertThat(
        r3Routes(dp, EXPORTED).stream().map(Bgpv4Route::getMetric).collect(Collectors.toList()),
        contains(7L));
  }

  @Test
  public void testARedistributedRouteTheTrackStopsExportingIsWithdrawn() throws IOException {
    // The same for a main-RIB route the export policy redistributes.
    DataPlane dp =
        dataPlane(
            ImmutableList.of(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            new MatchProtocol(RoutingProtocol.STATIC),
                            new Not(new TrackSucceeded(TRACK)))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement())),
                Statements.ExitReject.toStaticStatement()));

    assertThat(r3Networks(dp), not(hasItem(REDISTRIBUTED)));
  }
}
