package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.CumulusConversions.computeBgpGenerationPolicyName;
import static org.batfish.representation.cumulus.CumulusConversions.computeMatchSuppressedSummaryOnlyPolicyName;
import static org.batfish.representation.cumulus.CumulusNcluConfiguration.getSetNextHop;
import static org.batfish.representation.cumulus.CumulusNcluConfiguration.toCommunityList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.junit.Test;

/** Test for {@link CumulusNcluConfiguration}. */
public class CumulusNcluConfigurationTest {
  @Test
  public void testToInterface_active() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_inactive() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertFalse(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_active() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_inactive() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertFalse(viIface.getActive());
  }

  @Test
  public void testToRouteFilterLine() {
    IpPrefixListLine prefixListLine =
        new IpPrefixListLine(
            LineAction.PERMIT, 10, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30));

    RouteFilterLine rfl = CumulusNcluConfiguration.toRouteFilterLine(prefixListLine);
    assertThat(
        rfl,
        equalTo(
            new RouteFilterLine(
                LineAction.PERMIT, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30))));
  }

  @Test
  public void testToRouteFilter() {
    IpPrefixList prefixList = new IpPrefixList("name");
    prefixList
        .getLines()
        .put(
            10L,
            new IpPrefixListLine(
                LineAction.DENY, 10, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30)));
    prefixList
        .getLines()
        .put(
            20L,
            new IpPrefixListLine(
                LineAction.PERMIT, 20, Prefix.parse("10.0.2.1/24"), new SubRange(28, 31)));

    RouteFilterList rfl = CumulusNcluConfiguration.toRouteFilterList(prefixList);

    assertThat(
        rfl,
        equalTo(
            new RouteFilterList(
                "name",
                ImmutableList.of(
                    new RouteFilterLine(
                        LineAction.DENY, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30)),
                    new RouteFilterLine(
                        LineAction.PERMIT, Prefix.parse("10.0.2.1/24"), new SubRange(28, 31))))));
  }

  @Test
  public void testToIpCommunityList() {
    String name = "name";
    IpCommunityListExpanded ipCommunityList =
        new IpCommunityListExpanded(
            name,
            LineAction.PERMIT,
            ImmutableList.of(StandardCommunity.of(10000, 1), StandardCommunity.of(20000, 2)));
    CommunityList result = toCommunityList(ipCommunityList);
    assertThat(
        result,
        equalTo(
            new CommunityList(
                name,
                ImmutableList.of(
                    new CommunityListLine(
                        LineAction.PERMIT, new LiteralCommunity(StandardCommunity.of(10000, 1))),
                    new CommunityListLine(
                        LineAction.PERMIT, new LiteralCommunity(StandardCommunity.of(20000, 2)))),
                false)));
  }

  @Test
  public void testGetAcceptStatements() {
    BgpNeighbor bgpNeighbor = new BgpIpNeighbor("10.0.0.1");
    BgpVrf bgpVrf = new BgpVrf("bgpVrf");

    {
      // if no as set, do not set next-hop-self
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is not ibgp, do not set next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(20000L);
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is ibgp but no address family set, do not set next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is ibgp and has address family set but no neighbor configuration, do not set
      // next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      bgpVrf.setIpv4Unicast(new BgpIpv4UnicastAddressFamily());
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is ibgp but neighbor configuration does not have next-hop-self set, do not set
      // next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      BgpIpv4UnicastAddressFamily ipv4Unicast = new BgpIpv4UnicastAddressFamily();
      ipv4Unicast
          .getNeighborAddressFamilyConfigurations()
          .put("10.0.0.1", new BgpVrfNeighborAddressFamilyConfiguration());
      bgpVrf.setIpv4Unicast(ipv4Unicast);

      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is ibgp and neighbor configuration set next-hop-self set, then set
      // next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      BgpIpv4UnicastAddressFamily ipv4Unicast = new BgpIpv4UnicastAddressFamily();
      ipv4Unicast
          .getNeighborAddressFamilyConfigurations()
          .computeIfAbsent(
              "10.0.0.1",
              k -> {
                BgpVrfNeighborAddressFamilyConfiguration neighborConfig =
                    new BgpVrfNeighborAddressFamilyConfiguration();
                neighborConfig.setNextHopSelf(true);
                return neighborConfig;
              });
      bgpVrf.setIpv4Unicast(ipv4Unicast);

      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertThat(setNextHop, equalTo(new SetNextHop(SelfNextHop.getInstance(), false)));
    }
  }

  @Test
  public void testToBgpProcess_aggregateRoutes() {
    testToBgpProcess_aggregateRoutes(true);
    testToBgpProcess_aggregateRoutes(false);
  }

  private static void testToBgpProcess_aggregateRoutes(boolean summaryOnly) {
    // setup VI model
    NetworkFactory nf = new NetworkFactory();
    Configuration viConfig =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();
    Vrf viVrf = nf.vrfBuilder().setOwner(viConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    // setup VS model
    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    BgpProcess bgpProcess = new BgpProcess();
    vsConfig.setBgpProcess(bgpProcess);
    vsConfig.setConfiguration(viConfig);

    // setup BgpVrf
    BgpVrf vrf = bgpProcess.getDefaultVrf();
    vrf.setRouterId(Ip.parse("1.1.1.1"));
    BgpIpv4UnicastAddressFamily ipv4Unicast = new BgpIpv4UnicastAddressFamily();
    Prefix prefix = Prefix.parse("1.2.3.0/24");

    BgpVrfAddressFamilyAggregateNetworkConfiguration agg =
        new BgpVrfAddressFamilyAggregateNetworkConfiguration();
    agg.setSummaryOnly(summaryOnly);

    ipv4Unicast.getAggregateNetworks().put(prefix, agg);
    vrf.setIpv4Unicast(ipv4Unicast);

    // the method under test
    vsConfig.toBgpProcess(Configuration.DEFAULT_VRF_NAME, vrf);

    // generation policy exists
    assertThat(
        viConfig.getRoutingPolicies(),
        hasKey(
            computeBgpGenerationPolicyName(
                true, Configuration.DEFAULT_VRF_NAME, prefix.toString())));

    // generated route exists
    assertTrue(viVrf.getGeneratedRoutes().stream().anyMatch(gr -> gr.getNetwork().equals(prefix)));

    if (summaryOnly) {
      // suppress summary only filter list exists
      assertThat(
          viConfig.getRouteFilterLists(),
          hasKey(computeMatchSuppressedSummaryOnlyPolicyName(viVrf.getName())));
    } else {
      // suppress summary only filter list does not exist
      assertThat(
          viConfig.getRouteFilterLists(),
          not(hasKey(computeMatchSuppressedSummaryOnlyPolicyName(viVrf.getName()))));
    }
  }

  @Test
  public void testGenerateBgpActivePeerConfig_SetEbgpMultiHop() {

    // set VI configuration
    Configuration configuration = new Configuration("Host", ConfigurationFormat.CUMULUS_NCLU);
    configuration
        .getAllInterfaces()
        .put(
            "i1",
            org.batfish.datamodel.Interface.builder()
                .setName("i1")
                .setType(InterfaceType.PHYSICAL)
                .setOwner(configuration)
                .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
                .build());

    // set bgp neighbor
    BgpIpNeighbor neighbor = new BgpIpNeighbor("BgpNeighbor");
    neighbor.setRemoteAs(10000L);
    neighbor.setRemoteAsType(RemoteAsType.INTERNAL);
    neighbor.setPeerIp(Ip.parse("10.0.0.2"));
    neighbor.setEbgpMultihop(3L);

    // set bgp process
    org.batfish.datamodel.BgpProcess newProc =
        new org.batfish.datamodel.BgpProcess(
            Ip.parse("10.0.0.1"), ConfigurationFormat.CUMULUS_NCLU);

    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    ncluConfiguration.generateBgpActivePeerConfig(
        neighbor,
        10000L,
        new BgpVrf("Vrf"),
        newProc,
        new RoutingPolicy("Routing", configuration),
        configuration);

    BgpActivePeerConfig peerConfig = newProc.getActiveNeighbors().get(Prefix.parse("10.0.0.2/32"));

    assertTrue(peerConfig.getEbgpMultihop());
  }

  @Test
  public void testGenerateBgpUnnumberedPeerConfig_SetEbgpMultiHop() {

    // set VI configuration
    Configuration configuration = new Configuration("Host", ConfigurationFormat.CUMULUS_NCLU);
    configuration
        .getAllInterfaces()
        .put(
            "i1",
            org.batfish.datamodel.Interface.builder()
                .setName("i1")
                .setType(InterfaceType.PHYSICAL)
                .setOwner(configuration)
                .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
                .build());

    // set bgp neighbor
    BgpInterfaceNeighbor neighbor = new BgpInterfaceNeighbor("BgpNeighbor");
    neighbor.setRemoteAs(10000L);
    neighbor.setRemoteAsType(RemoteAsType.INTERNAL);
    neighbor.setEbgpMultihop(3L);

    // set bgp process
    org.batfish.datamodel.BgpProcess newProc =
        new org.batfish.datamodel.BgpProcess(
            Ip.parse("10.0.0.1"), ConfigurationFormat.CUMULUS_NCLU);

    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    ncluConfiguration.generateBgpUnnumberedPeerConfig(
        neighbor, 10000L, new BgpVrf("Vrf"), newProc, new RoutingPolicy("Routing", configuration));

    BgpUnnumberedPeerConfig peerConfig = newProc.getInterfaceNeighbors().get("BgpNeighbor");

    assertTrue(peerConfig.getEbgpMultihop());
  }
}
