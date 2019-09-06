package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.dataplane.ibdp.TestUtils.assertRoute;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
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

public class BgpNextHopUnchangedTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private NetworkFactory _nf = new NetworkFactory();

  /** Routing policies */
  private RoutingPolicy.Builder routingPolicyNhUnchanged =
      _nf.routingPolicyBuilder()
          .setStatements(
              ImmutableList.of(
                  new SetNextHop(UnchangedNextHop.getInstance()),
                  Statements.ExitAccept.toStaticStatement()));

  private RoutingPolicy.Builder permitAllBgp =
      _nf.routingPolicyBuilder()
          .setStatements(
              ImmutableList.of(
                  new If(
                      new Disjunction(
                          ImmutableList.of(
                              new MatchProtocol(RoutingProtocol.BGP),
                              new MatchProtocol(RoutingProtocol.IBGP))),
                      ImmutableList.of(Statements.ExitAccept.toStaticStatement()))));

  private RoutingPolicy.Builder routingPolicyRedistributeStatic =
      _nf.routingPolicyBuilder()
          .setStatements(
              ImmutableList.of(
                  new If(
                      new MatchProtocol(RoutingProtocol.STATIC),
                      ImmutableList.of(Statements.ExitAccept.toStaticStatement()))));

  /** Hostnames, IPs and prefixes */
  private Prefix advertisedBgpPrefix = Prefix.parse("23.23.23.23/24");

  private String r1Name = "r1";
  private String r2Name = "r2";
  private String r3Name = "r3";
  private Ip routerId1 = Ip.parse("1.1.1.1");
  private Ip routerId2 = Ip.parse("2.2.2.2");
  private Ip routerId3 = Ip.parse("3.3.3.3");
  private ConcreteInterfaceAddress r1Addr1 = ConcreteInterfaceAddress.parse("2.2.2.2/24");
  private ConcreteInterfaceAddress r2Addr1 = ConcreteInterfaceAddress.parse("2.2.2.3/24");
  private ConcreteInterfaceAddress r2Addr2 = ConcreteInterfaceAddress.parse("3.3.3.3/24");
  private ConcreteInterfaceAddress r3Addr1 = ConcreteInterfaceAddress.parse("3.3.3.4/24");

  /** A default BGP process to start with */
  private BgpProcess.Builder _bgpProcessBuilder =
      _nf.bgpProcessBuilder().setEbgpAdminCost(1).setIbgpAdminCost(1);

  /**
   * Returns a three node network where first node (r1) advertises a static route and other nodes
   * install and re-advertise it. AS number of the three nodes are 1, 2, and 3 for EBGP and is equal
   * to 1 otherwise
   *
   * @param ebgp if true then puts the three nodes in ebgp adjacencies
   * @return {@link SortedMap} of {@link Configuration}s
   */
  //
  // +--------------+             +---------------+            +---------------+
  // |              |             |               |            |               |
  // |     r1       |r1_r2        |     r2        |r2_r3       |      r3       |
  // |              +-------------+               +------------+               |
  // |              |        r2_r1|               |      r3_r2 |               |
  // |              |             |               |            |               |
  // +--------------+             +---------------+            +---------------+
  //
  public SortedMap<String, Configuration> network(boolean ebgp, boolean nhUnchanged) {
    // r1
    Configuration r1 =
        _nf.configurationBuilder()
            .setHostname(r1Name)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf r1Vrf = _nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(r1).setOwner(r1).build();
    _nf.interfaceBuilder().setName("r1_r2").setAddress(r1Addr1).setVrf(r1Vrf).setOwner(r1).build();
    // needed to activate static route
    Interface ifaceForStaticRoute =
        _nf.interfaceBuilder()
            .setName("ifaceStaticRoute")
            .setOwner(r1)
            .setVrf(r1Vrf)
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
            .build();
    r1Vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setAdministrativeCost(1)
                .setNetwork(advertisedBgpPrefix)
                .setNextHopInterface(ifaceForStaticRoute.getName())
                .build()));
    BgpProcess bgpProcessR1 = _bgpProcessBuilder.setRouterId(routerId1).setVrf(r1Vrf).build();
    _nf.bgpNeighborBuilder()
        .setPeerAddress(r2Addr1.getIp())
        .setLocalAs(1L)
        .setLocalIp(r1Addr1.getIp())
        .setRemoteAs(ebgp ? 2L : 1L)
        .setBgpProcess(bgpProcessR1)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(routingPolicyRedistributeStatic.setOwner(r1).build().getName())
                .build())
        .build();

    // r2
    Configuration r2 =
        _nf.configurationBuilder()
            .setHostname(r2Name)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf r2Vrf = _nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(r2).setOwner(r2).build();
    _nf.interfaceBuilder().setName("r2_r1").setAddress(r2Addr1).setVrf(r2Vrf).setOwner(r2).build();
    _nf.interfaceBuilder().setName("r2_r3").setAddress(r2Addr2).setVrf(r2Vrf).setOwner(r2).build();
    BgpProcess bgpProcessR2 = _bgpProcessBuilder.setRouterId(routerId2).setVrf(r2Vrf).build();
    _nf.bgpNeighborBuilder()
        .setPeerAddress(r1Addr1.getIp())
        .setLocalIp(r2Addr1.getIp())
        .setLocalAs(ebgp ? 2L : 1L)
        .setRemoteAs(1L)
        .setBgpProcess(bgpProcessR2)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(permitAllBgp.setOwner(r2).build().getName())
                .build())
        .build();
    _nf.bgpNeighborBuilder()
        .setLocalIp(r2Addr2.getIp())
        .setPeerAddress(r3Addr1.getIp())
        .setLocalAs(ebgp ? 2L : 1L)
        .setRemoteAs(ebgp ? 3L : 1L)
        .setBgpProcess(bgpProcessR2)
        // possible place to apply routing policy unchanged
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(
                    (nhUnchanged ? routingPolicyNhUnchanged : permitAllBgp)
                        .setOwner(r2)
                        .build()
                        .getName())
                .build())
        .build();

    // r3
    Configuration r3 =
        _nf.configurationBuilder()
            .setHostname(r3Name)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf r3Vrf = _nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(r3).setOwner(r3).build();
    _nf.interfaceBuilder().setName("r3_r2").setAddress(r3Addr1).setVrf(r3Vrf).setOwner(r3).build();
    BgpProcess bgpProcessR3 = _bgpProcessBuilder.setRouterId(routerId3).setVrf(r3Vrf).build();
    _nf.bgpNeighborBuilder()
        .setLocalIp(r3Addr1.getIp())
        .setPeerAddress(r2Addr2.getIp())
        .setLocalAs(ebgp ? 3L : 1L)
        .setRemoteAs(ebgp ? 2L : 1L)
        .setBgpProcess(bgpProcessR3)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(permitAllBgp.setOwner(r3).build().getName())
                .build())
        .build();

    return ImmutableSortedMap.of(r1.getHostname(), r1, r2.getHostname(), r2, r3.getHostname(), r3);
  }

  @Test
  public void ebgpNhUnchangedNotPresent() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(network(true, false), _folder);
    batfish.computeDataPlane();
    IncrementalDataPlane dataplane = (IncrementalDataPlane) batfish.loadDataPlane();
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    // next hop IPs get overwritten
    assertRoute(routes, RoutingProtocol.BGP, r2Name, advertisedBgpPrefix, 0, r1Addr1.getIp());
    assertRoute(routes, RoutingProtocol.BGP, r3Name, advertisedBgpPrefix, 0, r2Addr2.getIp());
  }

  @Test
  public void ebgpNhUnchangedPresent() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(network(true, true), _folder);
    batfish.computeDataPlane();
    IncrementalDataPlane dataplane = (IncrementalDataPlane) batfish.loadDataPlane();
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    assertRoute(routes, RoutingProtocol.BGP, r2Name, advertisedBgpPrefix, 0, r1Addr1.getIp());
    // nh IP will remain the same at r3
    assertRoute(routes, RoutingProtocol.BGP, r3Name, advertisedBgpPrefix, 0, r1Addr1.getIp());
  }

  @Test
  public void ibgpNhUnchangedNotPresent() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(network(false, false), _folder);
    batfish.computeDataPlane();
    IncrementalDataPlane dataplane = (IncrementalDataPlane) batfish.loadDataPlane();
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    // irrespective of absence of NH unchanged command, next hops are preserved for iBGP peerings
    assertRoute(routes, RoutingProtocol.IBGP, r2Name, advertisedBgpPrefix, 0, r1Addr1.getIp());
    assertRoute(routes, RoutingProtocol.IBGP, r3Name, advertisedBgpPrefix, 0, r1Addr1.getIp());
  }
}
