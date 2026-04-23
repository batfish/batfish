package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.isEvpnType3RouteThat;
import static org.batfish.datamodel.matchers.VniMatchers.hasBumTransportIps;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Table;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.Layer2VniConfig;
import org.batfish.datamodel.bgp.Layer2VniConfig.Builder;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests with small networks with BGP EVPN peerings */
public class EvpnTest {
  static final int SUBNET_LEN = 24;
  private static String policyName;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static SortedMap<String, Configuration> twoNodeNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1");

    Configuration c1 = cb.build();
    Configuration c2 = cb.setHostname("n2").build();

    policyName = "EXPORT_ALL";
    RoutingPolicy.Builder policyBuilder =
        RoutingPolicy.builder()
            .setName(policyName)
            .addStatement(Statements.ReturnTrue.toStaticStatement());
    policyBuilder.setOwner(c1).build();
    policyBuilder.setOwner(c2).build();

    Ip ipNode1 = Ip.parse("1.1.1.1");
    Ip ipNode2 = Ip.parse("1.1.1.2");

    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).setName(DEFAULT_VRF_NAME).build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgpProcess1 = BgpProcess.testBgpProcess(ipNode1);
    BgpProcess bgpProcess2 = BgpProcess.testBgpProcess(ipNode2);
    vrf1.setBgpProcess(bgpProcess1);
    vrf2.setBgpProcess(bgpProcess2);

    nf.interfaceBuilder()
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.create(ipNode1, SUBNET_LEN))
        .build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(vrf2)
        .setAddress(ConcreteInterfaceAddress.create(ipNode2, SUBNET_LEN))
        .build();

    int vni = 10001;
    Ip vniIp1 = Ip.parse("1.111.111.111");
    Ip vniIp2 = Ip.parse("2.222.222.222");
    vrf1.setLayer2Vnis(
        ImmutableSet.of(
            Layer2Vni.testBuilder()
                .setVni(vni)
                .setVlan(1)
                .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                .setSourceAddress(vniIp1)
                .build()));
    vrf2.setLayer2Vnis(
        ImmutableSet.of(
            Layer2Vni.testBuilder()
                .setVni(vni)
                .setVlan(1)
                .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                .setSourceAddress(vniIp2)
                .build()));

    Builder vniConfigBuilder =
        Layer2VniConfig.builder()
            .setVni(vni)
            .setVrf(DEFAULT_VRF_NAME)
            .setRouteDistinguisher(RouteDistinguisher.from(bgpProcess1.getRouterId(), 1))
            .setRouteTarget(ExtendedCommunity.target(65500, vni));
    Layer2VniConfig vniConfig1 = vniConfigBuilder.build();
    Layer2VniConfig vniConfig2 =
        vniConfigBuilder
            .setVni(vni)
            .setRouteDistinguisher(RouteDistinguisher.from(bgpProcess2.getRouterId(), 2))
            .build();
    nf.bgpNeighborBuilder()
        .setPeerAddress(ipNode2)
        .setRemoteAs(2L)
        .setLocalIp(ipNode1)
        .setLocalAs(1L)
        .setBgpProcess(bgpProcess1)
        .setEvpnAddressFamily(
            EvpnAddressFamily.builder()
                .setL2Vnis(ImmutableSet.of(vniConfig1))
                .setL3Vnis(ImmutableSet.of())
                .setPropagateUnmatched(true)
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(true)
                        .setSendExtendedCommunity(true)
                        .build())
                .setExportPolicy(policyName)
                .build())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(policyName).build())
        .build();
    nf.bgpNeighborBuilder()
        .setPeerAddress(ipNode1)
        .setRemoteAs(1L)
        .setLocalIp(ipNode2)
        .setLocalAs(2L)
        .setBgpProcess(bgpProcess2)
        .setEvpnAddressFamily(
            EvpnAddressFamily.builder()
                .setL2Vnis(ImmutableSet.of(vniConfig2))
                .setL3Vnis(ImmutableSet.of())
                .setPropagateUnmatched(true)
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(true)
                        .setSendExtendedCommunity(true)
                        .build())
                .setExportPolicy(policyName)
                .build())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(policyName).build())
        .build();

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
  }

  @Test
  public void testEbgpL2Vni() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(twoNodeNetwork(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());

    Table<String, String, Set<EvpnRoute<?, ?>>> ribs = dp.getEvpnRoutes();
    Set<EvpnRoute<?, ?>> n1Routes = ribs.get("n1", DEFAULT_VRF_NAME);
    Set<EvpnRoute<?, ?>> n2Routes = ribs.get("n2", DEFAULT_VRF_NAME);
    // Ensure routes are present in the main RIB
    assertThat(
        n1Routes, hasItem(isEvpnType3RouteThat(hasPrefix(Prefix.parse("2.222.222.222/32")))));
    assertThat(
        n2Routes, hasItem(isEvpnType3RouteThat(hasPrefix(Prefix.parse("1.111.111.111/32")))));
    // Ensure VNI flood lists were updated with peer's VTEP address
    assertThat(
        dp.getLayer2Vnis().column(DEFAULT_VRF_NAME),
        hasEntry(equalTo("n1"), contains(hasBumTransportIps(contains(Ip.parse("2.222.222.222"))))));
    assertThat(
        dp.getLayer2Vnis().column(DEFAULT_VRF_NAME),
        hasEntry(equalTo("n2"), contains(hasBumTransportIps(contains(Ip.parse("1.111.111.111"))))));
  }

  /**
   * Build a 2-node network with L2 VNIs and VLAN interfaces that have no physical switchport
   * members. VTEP source IPs are on loopbacks, reachable via direct link. VLAN interfaces should be
   * deactivated by autostate pre-dataplane, then reactivated when VXLAN tunnels come up.
   */
  private static SortedMap<String, Configuration> twoNodeNetworkWithVlanInterfaces() {
    NetworkFactory nf = new NetworkFactory();

    // -- Node 1 --
    Configuration c1 =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();
    policyName = "EXPORT_ALL";
    RoutingPolicy.builder()
        .setOwner(c1)
        .setName(policyName)
        .addStatement(Statements.ReturnTrue.toStaticStatement())
        .build();
    Ip loopback1 = Ip.parse("10.0.0.1");
    Ip loopback2 = Ip.parse("10.0.0.2");
    Ip link1 = Ip.parse("10.1.0.1");
    Ip link2 = Ip.parse("10.1.0.2");

    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgp1 = BgpProcess.testBgpProcess(loopback1);
    vrf1.setBgpProcess(bgp1);
    // Loopback (VTEP source)
    nf.interfaceBuilder()
        .setOwner(c1)
        .setVrf(vrf1)
        .setName("Loopback0")
        .setAddress(ConcreteInterfaceAddress.create(loopback1, 32))
        .build();
    // Physical link to n2
    nf.interfaceBuilder()
        .setOwner(c1)
        .setVrf(vrf1)
        .setName("Ethernet1")
        .setAddress(ConcreteInterfaceAddress.create(link1, 30))
        .build();
    // VLAN interface — no switchport members, autostate enabled
    nf.interfaceBuilder()
        .setOwner(c1)
        .setVrf(vrf1)
        .setName("Vlan100")
        .setType(InterfaceType.VLAN)
        .setVlan(100)
        .setAutoState(true)
        .setAddress(ConcreteInterfaceAddress.parse("172.16.100.1/24"))
        .build();
    // L2 VNI for VLAN 100
    int vni = 10100;
    vrf1.setLayer2Vnis(
        ImmutableSet.of(
            Layer2Vni.testBuilder()
                .setVni(vni)
                .setVlan(100)
                .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                .setSourceAddress(loopback1)
                .build()));
    Layer2VniConfig vniConfig1 =
        Layer2VniConfig.builder()
            .setVni(vni)
            .setVrf(DEFAULT_VRF_NAME)
            .setRouteDistinguisher(RouteDistinguisher.from(bgp1.getRouterId(), 1))
            .setRouteTarget(ExtendedCommunity.target(65500, vni))
            .build();
    nf.bgpNeighborBuilder()
        .setPeerAddress(loopback2)
        .setRemoteAs(2L)
        .setLocalIp(loopback1)
        .setLocalAs(1L)
        .setBgpProcess(bgp1)
        .setEvpnAddressFamily(
            EvpnAddressFamily.builder()
                .setL2Vnis(ImmutableSet.of(vniConfig1))
                .setL3Vnis(ImmutableSet.of())
                .setPropagateUnmatched(true)
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(true)
                        .setSendExtendedCommunity(true)
                        .build())
                .setExportPolicy(policyName)
                .build())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(policyName).build())
        .build();

    // -- Node 2 --
    Configuration c2 =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n2")
            .build();
    RoutingPolicy.builder()
        .setOwner(c2)
        .setName(policyName)
        .addStatement(Statements.ReturnTrue.toStaticStatement())
        .build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).setName(DEFAULT_VRF_NAME).build();
    BgpProcess bgp2 = BgpProcess.testBgpProcess(loopback2);
    vrf2.setBgpProcess(bgp2);
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(vrf2)
        .setName("Loopback0")
        .setAddress(ConcreteInterfaceAddress.create(loopback2, 32))
        .build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(vrf2)
        .setName("Ethernet1")
        .setAddress(ConcreteInterfaceAddress.create(link2, 30))
        .build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(vrf2)
        .setName("Vlan100")
        .setType(InterfaceType.VLAN)
        .setVlan(100)
        .setAutoState(true)
        .setAddress(ConcreteInterfaceAddress.parse("172.16.100.2/24"))
        .build();
    vrf2.setLayer2Vnis(
        ImmutableSet.of(
            Layer2Vni.testBuilder()
                .setVni(vni)
                .setVlan(100)
                .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                .setSourceAddress(loopback2)
                .build()));
    Layer2VniConfig vniConfig2 =
        Layer2VniConfig.builder()
            .setVni(vni)
            .setVrf(DEFAULT_VRF_NAME)
            .setRouteDistinguisher(RouteDistinguisher.from(bgp2.getRouterId(), 2))
            .setRouteTarget(ExtendedCommunity.target(65500, vni))
            .build();
    nf.bgpNeighborBuilder()
        .setPeerAddress(loopback1)
        .setRemoteAs(1L)
        .setLocalIp(loopback2)
        .setLocalAs(2L)
        .setBgpProcess(bgp2)
        .setEvpnAddressFamily(
            EvpnAddressFamily.builder()
                .setL2Vnis(ImmutableSet.of(vniConfig2))
                .setL3Vnis(ImmutableSet.of())
                .setPropagateUnmatched(true)
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(true)
                        .setSendExtendedCommunity(true)
                        .build())
                .setExportPolicy(policyName)
                .build())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(policyName).build())
        .build();

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
  }

  @Test
  public void testVxlanAutostateReactivatesIrb() throws IOException {
    SortedMap<String, Configuration> configs = twoNodeNetworkWithVlanInterfaces();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());

    // After dataplane computation, VXLAN tunnels should be up and IRBs reactivated.
    // Check that Vlan100 on both nodes is active.
    Configuration n1 = configs.get("n1");
    Configuration n2 = configs.get("n2");
    assertThat(n1.getAllInterfaces().get("Vlan100").getActive(), equalTo(true));
    assertThat(n2.getAllInterfaces().get("Vlan100").getActive(), equalTo(true));

    // Connected routes for the IRB subnets should be in the main RIBs
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibsForTesting();
    assertThat(
        ribs.get("n1").get(DEFAULT_VRF_NAME).getUnannotatedRoutes(),
        hasItem(hasPrefix(Prefix.parse("172.16.100.0/24"))));
    assertThat(
        ribs.get("n2").get(DEFAULT_VRF_NAME).getUnannotatedRoutes(),
        hasItem(hasPrefix(Prefix.parse("172.16.100.0/24"))));
  }

  @Test(expected = AssertionError.class) // xfail this until NX-OS supports type 5 routes
  public void testEvpnSymmetricSingleSpine() throws IOException {
    String testRigResourcePrefix = "org/batfish/dataplane/ibdp/evpn-nxos-symmetric";
    String exitGw = "exitgw";
    String leaf1 = "leaf1";
    String spine = "spine";

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(testRigResourcePrefix, exitGw, leaf1, spine)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    Table<String, String, Set<EvpnRoute<?, ?>>> evpnRoutes = dataplane.getEvpnRoutes();
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> mainRibRoutes =
        dataplane.getRibsForTesting();

    String vrf1 = "vrf1";

    Prefix leaf1VtepPrefix = Prefix.parse("1.1.1.3/32");
    Prefix exitgwVtepPrefix = Prefix.parse("2.2.2.2/32");
    {
      Set<EvpnRoute<?, ?>> exitgwRoutes = evpnRoutes.get(exitGw, vrf1);
      assertThat(
          exitgwRoutes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(leaf1VtepPrefix),
                      hasCommunities(ExtendedCommunity.target(65000, 10010))))));
      assertThat(
          exitgwRoutes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(leaf1VtepPrefix),
                      hasCommunities(ExtendedCommunity.target(65000, 10020))))));
      // Ensure not in main RIB
      assertThat(
          mainRibRoutes.get(exitGw).get(vrf1).getUnannotatedRoutes(),
          not(hasItem(hasPrefix(leaf1VtepPrefix))));
      // Locally-generated routes will have no next hop ip in the EVPN rib
      assertThat(
          exitgwRoutes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(exitgwVtepPrefix),
                      hasNextHopIp(nullValue()),
                      hasCommunities(ExtendedCommunity.target(65000, 10010))))));
      assertThat(
          exitgwRoutes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(exitgwVtepPrefix),
                      hasNextHopIp(nullValue()),
                      hasCommunities(ExtendedCommunity.target(65000, 10020))))));
    }

    {
      Set<EvpnRoute<?, ?>> leaf1Routes = evpnRoutes.get(leaf1, vrf1);
      assertThat(
          leaf1Routes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(exitgwVtepPrefix),
                      hasCommunities(ExtendedCommunity.target(65000, 10010))))));
      assertThat(
          leaf1Routes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(exitgwVtepPrefix),
                      hasCommunities(ExtendedCommunity.target(65000, 10020))))));
      // Ensure not in main RIB
      assertThat(
          mainRibRoutes.get(leaf1).get(vrf1).getUnannotatedRoutes(),
          not(hasItem(hasPrefix(exitgwVtepPrefix))));
      // Locally-generated routes will have no next hop ip in the EVPN rib
      assertThat(
          leaf1Routes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(leaf1VtepPrefix),
                      hasNextHopIp(nullValue()),
                      hasCommunities(ExtendedCommunity.target(65000, 10010))))));
      assertThat(
          leaf1Routes,
          hasItem(
              isEvpnType3RouteThat(
                  allOf(
                      hasPrefix(leaf1VtepPrefix),
                      hasNextHopIp(nullValue()),
                      hasCommunities(ExtendedCommunity.target(65000, 10020))))));
    }
  }
}
