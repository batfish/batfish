package org.batfish.dataplane;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.representation.juniper.JuniperConfiguration.DEFAULT_BGP_ADMIN_DISTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.dataplane.rib.RibGroup;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.dataplane.ibdp.IncrementalDataPlane;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of RIB groups applied to BGP neighbors */
public class BgpRibGroupsTest {
  private static final String EXPORT_STATIC = "EXPORT_STATIC";
  private static final String RG_POLICY_NAME = "RG_IMPORT_POLICY";
  private static final String VRF_2 = "vrf2";
  private static final int ADMIN_OVERWRITE = 123;
  private static final String EXPORT_ALL = "EXPORT_ALL";

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  /*

  * EBGP on all nodes, 2 peers setup at r1
  * r1 has "vrf2", without any BGP process running.
  * r1 will leak route from default VRF to VRF2 using a rib group applied to the peering with R2 only
  * r2 & r3 will both advertise a different route into bgp
  * r2's static route should be in the VRF2's RIB
  * r3's static route should NOT be in the VRF2's RIB

                                     .3/31
                                   +----------+
              .2/31                |          |
               +-------------------+    r2    |
               |                   |          |
          +----+-----+             +----------+
          |          |
          |    r1    |
          |          |
          +----+-----+             +----------+
               |                   |          |
               +-------------------+    r3    |
              .4/31                |          |
                                   +----------+
                                    .5/31


   */
  private static SortedMap<String, Configuration> makeTestNetwork() {

    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.JUNIPER);

    Configuration c1 = cb.setHostname("r1").build();
    Configuration c2 = cb.setHostname("r2").build();
    Configuration c3 = cb.setHostname("r3").build();

    // Rib group import policy on R1:
    nf.routingPolicyBuilder()
        .setName(RG_POLICY_NAME)
        .setStatements(
            Collections.singletonList(
                new If(
                    new MatchProtocol(RoutingProtocol.BGP),
                    ImmutableList.of(
                        new SetAdministrativeCost(new LiteralAdministrativeCost(ADMIN_OVERWRITE)),
                        Statements.ReturnTrue.toStaticStatement()))))
        .setOwner(c1)
        .build();

    // Export static policies on r2 & r3
    RoutingPolicy.Builder rpb =
        nf.routingPolicyBuilder()
            .setName(EXPORT_STATIC)
            .setStatements(
                Collections.singletonList(
                    new If(
                        new MatchProtocol(RoutingProtocol.STATIC),
                        ImmutableList.of(
                            new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                            Statements.ReturnTrue.toStaticStatement()))));
    rpb.setOwner(c2).build();
    rpb.setOwner(c3).build();

    // export policy (set origin type, allow) for all nodes
    nf.routingPolicyBuilder()
        .setName(EXPORT_ALL)
        .setStatements(
            ImmutableList.of(
                new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)),
                Statements.ReturnTrue.toStaticStatement()))
        .setOwner(c1)
        .build();

    // R1
    Vrf c1v1 = nf.vrfBuilder().setOwner(c1).setName(Configuration.DEFAULT_VRF_NAME).build();
    nf.vrfBuilder().setOwner(c1).setName(VRF_2).build();

    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/31"))
        .setOwner(c1)
        .setVrf(c1v1)
        .build();
    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.4/31"))
        .setOwner(c1)
        .setVrf(c1v1)
        .build();

    BgpProcess bgpProc1 =
        nf.bgpProcessBuilder()
            .setVrf(c1v1)
            .setRouterId(Ip.parse("1.1.1.1"))
            .setEbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setIbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setLocalAdminCost(DEFAULT_BGP_ADMIN_DISTANCE) /* Not relevant for JunOS. */
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .build();
    RibGroup rg =
        new RibGroup(
            "RIB_GROUP",
            ImmutableList.of(new RibId("r1", VRF_2, RibId.DEFAULT_RIB_NAME)),
            RG_POLICY_NAME,
            null);
    nf.bgpNeighborBuilder()
        .setBgpProcess(bgpProc1)
        .setLocalAs(1L)
        .setRemoteAs(2L)
        .setPeerAddress(Ip.parse("1.1.1.3"))
        .setLocalIp(Ip.parse("1.1.1.2"))
        .setAppliedRibGroup(rg)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(EXPORT_ALL).build())
        .build();
    nf.bgpNeighborBuilder()
        .setBgpProcess(bgpProc1)
        .setLocalAs(1L)
        .setRemoteAs(3L)
        .setPeerAddress(Ip.parse("1.1.1.5"))
        .setLocalIp(Ip.parse("1.1.1.4"))
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(EXPORT_ALL).build())
        .build();

    // R2
    Vrf v2 = nf.vrfBuilder().setOwner(c2).setName(Configuration.DEFAULT_VRF_NAME).build();
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("2.2.2.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
                .build()));

    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.3/31"))
        .setOwner(c2)
        .setVrf(v2)
        .build();

    BgpProcess bgpProc2 =
        nf.bgpProcessBuilder()
            .setVrf(v2)
            .setRouterId(Ip.parse("2.2.2.2"))
            .setEbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setIbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setLocalAdminCost(DEFAULT_BGP_ADMIN_DISTANCE) /* Not relevant for JunOS. */
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .build();
    nf.bgpNeighborBuilder()
        .setBgpProcess(bgpProc2)
        .setLocalAs(2L)
        .setRemoteAs(1L)
        .setPeerAddress(Ip.parse("1.1.1.2"))
        .setLocalIp(Ip.parse("1.1.1.3"))
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(EXPORT_STATIC).build())
        .build();

    // R3
    Vrf v3 = nf.vrfBuilder().setOwner(c3).setName(Configuration.DEFAULT_VRF_NAME).build();
    v3.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("3.3.3.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
                .build()));

    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.5/31"))
        .setOwner(c3)
        .setVrf(v3)
        .build();

    BgpProcess bgpProc3 =
        nf.bgpProcessBuilder()
            .setVrf(v3)
            .setRouterId(Ip.parse("3.3.3.3"))
            .setEbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setIbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setLocalAdminCost(DEFAULT_BGP_ADMIN_DISTANCE) /* Not relevant for JunOS. */
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .build();
    nf.bgpNeighborBuilder()
        .setBgpProcess(bgpProc3)
        .setLocalAs(3L)
        .setRemoteAs(1L)
        .setPeerAddress(Ip.parse("1.1.1.4"))
        .setLocalIp(Ip.parse("1.1.1.5"))
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(EXPORT_STATIC).build())
        .build();

    return ImmutableSortedMap.of("r1", c1, "r2", c2, "r3", c3);
  }

  @Test
  public void testBgpRibGroupAcrossVrfs() throws IOException {
    SortedMap<String, Configuration> configs = makeTestNetwork();

    Batfish batfish = BatfishTestUtils.getBatfish(configs, folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());

    // Only 2.2.2.0/24 in VRF2
    Set<AnnotatedRoute<AbstractRoute>> vrf2Routes =
        dp.getRibsForTesting().get("r1").get(VRF_2).getRoutes();
    assertThat(vrf2Routes, hasSize(1));
    assertThat(
        vrf2Routes,
        hasItem(
            new AnnotatedRoute<>(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.parse("2.2.2.0/24"))
                    .setAdmin(ADMIN_OVERWRITE)
                    .setAsPath(AsPath.ofSingletonAsSets(2L))
                    .setOriginatorIp(Ip.parse("2.2.2.2"))
                    .setOriginMechanism(OriginMechanism.LEARNED)
                    .setOriginType(OriginType.IGP)
                    .setProtocol(RoutingProtocol.BGP)
                    .setLocalPreference(100)
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.3")))
                    .setNextHopIp(Ip.parse("1.1.1.3"))
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .build(),
                Configuration.DEFAULT_VRF_NAME))); // In VRF_2, but came from default VRF!

    // 3.3.3.0/24 as expected in default VRF
    Set<AnnotatedRoute<AbstractRoute>> defaultVrfRoutes =
        dp.getRibsForTesting().get("r1").get(Configuration.DEFAULT_VRF_NAME).getRoutes();
    assertThat(
        defaultVrfRoutes,
        hasItem(
            new AnnotatedRoute<>(
                Bgpv4Route.testBuilder()
                    .setNetwork(Prefix.parse("3.3.3.0/24"))
                    .setAdmin(
                        RoutingProtocol.BGP.getDefaultAdministrativeCost(
                            ConfigurationFormat.JUNIPER))
                    .setAsPath(AsPath.ofSingletonAsSets(3L))
                    .setOriginatorIp(Ip.parse("3.3.3.3"))
                    .setOriginMechanism(OriginMechanism.LEARNED)
                    .setOriginType(OriginType.IGP)
                    .setProtocol(RoutingProtocol.BGP)
                    .setLocalPreference(100)
                    .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.5")))
                    .setNextHopIp(Ip.parse("1.1.1.5"))
                    .setSrcProtocol(RoutingProtocol.BGP)
                    .build(),
                Configuration.DEFAULT_VRF_NAME)));
  }
}
