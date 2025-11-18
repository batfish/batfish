package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.RoutingProtocol.IBGP;
import static org.batfish.datamodel.RoutingProtocol.STATIC;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.dataplane.ibdp.TestUtils.assertNoRoute;
import static org.batfish.dataplane.ibdp.TestUtils.assertRoute;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.UnchangedNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of BGP resolution condition on main RIB LPM routes for BGPv4 route next hop IP. */
public final class BgpResolutionConditionTest {

  private static final long AS = 1L;

  private static final String R1 = "r1";
  private static final String R2 = "r2";
  private static final Ip R1_ROUTER_ID = Ip.parse("1.1.1.1");
  private static final Ip R2_ROUTER_ID = Ip.parse("2.2.2.2");

  private static final Prefix DEPENDENT_ROUTE1_NETWORK = Prefix.parse("10.0.0.1/32");
  private static final Prefix DEPENDENT_ROUTE2_NETWORK = Prefix.parse("10.0.0.2/32");
  private static final ConcreteInterfaceAddress R1_RESOLVER_INTERFACE1_ADDRESS =
      ConcreteInterfaceAddress.parse("10.0.1.1/24");
  private static final ConcreteInterfaceAddress R1_RESOLVER_INTERFACE2_ADDRESS =
      ConcreteInterfaceAddress.parse("10.0.2.1/24");
  private static final ConcreteInterfaceAddress R2_RESOLVER_INTERFACE1_ADDRESS =
      ConcreteInterfaceAddress.parse("10.0.1.2/24");
  private static final ConcreteInterfaceAddress R2_RESOLVER_INTERFACE2_ADDRESS =
      ConcreteInterfaceAddress.parse("10.0.2.2/24");
  private static final Ip DEPENDENT_ROUTE1_NEXT_HOP_IP = Ip.parse("10.0.1.254");
  private static final Ip DEPENDENT_ROUTE2_NEXT_HOP_IP = Ip.parse("10.0.2.254");
  private static final ConcreteInterfaceAddress R1_PEERING_ADDR =
      ConcreteInterfaceAddress.parse("2.2.2.2/24");
  private static final ConcreteInterfaceAddress R2_PEERING_ADDR =
      ConcreteInterfaceAddress.parse("2.2.2.3/24");

  /** A default BGP process to start with */
  private static @Nonnull BgpProcess.Builder bgpProcessBuilder() {
    return BgpProcess.builder()
        .setEbgpAdminCost(1)
        .setIbgpAdminCost(1)
        .setLocalAdminCost(1)
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP);
  }

  /**
   * Returns a two node network where first node (r1) advertises two static routes and the second
   * node installs only the received route whose resolver is matched by r2's BGP process's
   * resolution restriction.
   */
  //
  // +--------------+             +---------------+
  // |              |             |               |
  // |     r1       |r1_r2        |     r2        |
  // |              +-------------+               +
  // |              |        r2_r1|               |
  // |              |             |               |
  // +--------------+             +---------------+
  //
  private @Nonnull SortedMap<String, Configuration> network(
      boolean r2ResolutionRestrictionToNetwork1) {
    // r1
    Configuration r1 =
        Configuration.builder()
            .setHostname(R1)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    r1.setExportBgpFromBgpRib(true);
    Vrf r1Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(r1).build();
    TestInterface.builder()
        .setName("r1_r2")
        .setAddress(R1_PEERING_ADDR)
        .setVrf(r1Vrf)
        .setOwner(r1)
        .build();
    // needed to activate static route
    String r1StaticRoute1ResolverInterfaceName = "r1sr1";
    TestInterface.builder()
        .setName(r1StaticRoute1ResolverInterfaceName)
        .setOwner(r1)
        .setVrf(r1Vrf)
        .setAddress(R1_RESOLVER_INTERFACE1_ADDRESS)
        .build();
    String r1StaticRoute2ResolverInterfaceName = "r1sr2";
    TestInterface.builder()
        .setName(r1StaticRoute2ResolverInterfaceName)
        .setOwner(r1)
        .setVrf(r1Vrf)
        .setAddress(R1_RESOLVER_INTERFACE2_ADDRESS)
        .build();
    StaticRoute.Builder staticRouteBuilder = StaticRoute.builder().setAdministrativeCost(1);
    r1Vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            staticRouteBuilder
                .setNetwork(DEPENDENT_ROUTE1_NETWORK)
                .setNextHop(NextHopIp.of(DEPENDENT_ROUTE1_NEXT_HOP_IP))
                .build(),
            staticRouteBuilder
                .setNetwork(DEPENDENT_ROUTE2_NETWORK)
                .setNextHop(NextHopIp.of(DEPENDENT_ROUTE2_NEXT_HOP_IP))
                .build()));
    String r1RedistributionPolicyName = "r1Redistribute";
    RoutingPolicy.builder()
        .setName(r1RedistributionPolicyName)
        .setStatements(
            ImmutableList.of(
                new If(
                    new MatchProtocol(STATIC),
                    ImmutableList.of(
                        new SetNextHop(UnchangedNextHop.getInstance()),
                        Statements.ExitAccept.toStaticStatement()))))
        .setOwner(r1)
        .build();
    String r1ExportPolicyName = "r1export";
    RoutingPolicy.builder()
        .setName(r1ExportPolicyName)
        .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
        .setOwner(r1)
        .build();
    BgpProcess r1BgpProcess =
        bgpProcessBuilder()
            .setRouterId(R1_ROUTER_ID)
            .setVrf(r1Vrf)
            .setRedistributionPolicy(r1RedistributionPolicyName)
            .build();
    BgpActivePeerConfig.builder()
        .setPeerAddress(R2_PEERING_ADDR.getIp())
        .setLocalAs(AS)
        .setLocalIp(R1_PEERING_ADDR.getIp())
        .setRemoteAs(AS)
        .setBgpProcess(r1BgpProcess)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(r1ExportPolicyName).build())
        .build();

    // r2
    Configuration r2 =
        Configuration.builder()
            .setHostname(R2)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    r2.setExportBgpFromBgpRib(true);
    Vrf r2Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(r2).build();
    TestInterface.builder()
        .setName("r2_r1")
        .setAddress(R2_PEERING_ADDR)
        .setVrf(r2Vrf)
        .setOwner(r2)
        .build();
    // interface whose routes resolve the received BGP routes
    String r2ResolverInterface1Name = "r2resolver1";
    TestInterface.builder()
        .setName(r2ResolverInterface1Name)
        .setOwner(r2)
        .setVrf(r2Vrf)
        .setAddress(R2_RESOLVER_INTERFACE1_ADDRESS)
        .build();
    String r2ResolverInterface2Name = "r2resolver2";
    TestInterface.builder()
        .setName(r2ResolverInterface2Name)
        .setOwner(r2)
        .setVrf(r2Vrf)
        .setAddress(R2_RESOLVER_INTERFACE2_ADDRESS)
        .build();
    String r2BgpNextHopResolverPolicyName = "r2Restriction";
    RoutingPolicy.builder()
        .setName(r2BgpNextHopResolverPolicyName)
        .setStatements(
            ImmutableList.of(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(),
                        new ExplicitPrefixSet(
                            new PrefixSpace(
                                PrefixRange.fromPrefix(
                                    R2_RESOLVER_INTERFACE1_ADDRESS.getPrefix())))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .setOwner(r2)
        .build();
    String r2ExportPolicyName = "denyAll";
    RoutingPolicy.builder()
        .setName(r2ExportPolicyName)
        .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
        .setOwner(r2)
        .build();
    BgpProcess r2BgpProcess =
        bgpProcessBuilder()
            .setRouterId(R2_ROUTER_ID)
            .setVrf(r2Vrf)
            .setRedistributionPolicy(r2ExportPolicyName)
            .build();
    if (r2ResolutionRestrictionToNetwork1) {
      r2BgpProcess.setNextHopIpResolverRestrictionPolicy(r2BgpNextHopResolverPolicyName);
    }
    BgpActivePeerConfig.builder()
        .setPeerAddress(R1_PEERING_ADDR.getIp())
        .setLocalAs(AS)
        .setLocalIp(R2_PEERING_ADDR.getIp())
        .setRemoteAs(AS)
        .setBgpProcess(r2BgpProcess)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(r2ExportPolicyName).build())
        .build();

    return ImmutableSortedMap.of(r1.getHostname(), r1, r2.getHostname(), r2);
  }

  @Test
  public void testResolutionRestriction() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(network(true), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    // r1 has routes to be sent
    assertRoute(routes, STATIC, R1, DEPENDENT_ROUTE1_NETWORK, 0, DEPENDENT_ROUTE1_NEXT_HOP_IP);
    assertRoute(routes, STATIC, R1, DEPENDENT_ROUTE2_NETWORK, 0, DEPENDENT_ROUTE2_NEXT_HOP_IP);

    // r2 only installs the first route, as the second does not match the resolution restriction
    assertRoute(routes, IBGP, R2, DEPENDENT_ROUTE1_NETWORK, 0, DEPENDENT_ROUTE1_NEXT_HOP_IP);
    assertNoRoute(routes, R2, DEPENDENT_ROUTE2_NETWORK);
  }

  @Test
  public void testNoResolutionRestriction() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(network(false), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    // r1 has routes to be sent
    assertRoute(routes, STATIC, R1, DEPENDENT_ROUTE1_NETWORK, 0, DEPENDENT_ROUTE1_NEXT_HOP_IP);
    assertRoute(routes, STATIC, R1, DEPENDENT_ROUTE2_NETWORK, 0, DEPENDENT_ROUTE2_NEXT_HOP_IP);

    // r2 installs both routes, as there is no resolution restriction
    assertRoute(routes, IBGP, R2, DEPENDENT_ROUTE1_NETWORK, 0, DEPENDENT_ROUTE1_NEXT_HOP_IP);
    assertRoute(routes, IBGP, R2, DEPENDENT_ROUTE2_NETWORK, 0, DEPENDENT_ROUTE2_NEXT_HOP_IP);
  }

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
}
