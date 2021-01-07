package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Map;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test integration of batfish and forwarding analysis impl */
public class ForwardingAnalysisImplIntegrationTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  /**
   * When there is a non-forwarding route in the rib with no shorter matching route, the route's
   * network should not be routable.
   */
  @Test
  public void testNonForwardingRoute_noShorterMatch() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Prefix nonForwardingRoutePrefix = Prefix.parse("2.2.0.0/16");
    StaticRoute nonForwardingRoute =
        StaticRoute.testBuilder()
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setNetwork(nonForwardingRoutePrefix)
            .setNonForwarding(true)
            .setAdministrativeCost(100)
            .build();
    vrf.setStaticRoutes(ImmutableSortedSet.of(nonForwardingRoute));

    String hostname = c.getHostname();
    String vrfName = vrf.getName();

    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(hostname, c), temp);
    batfish.computeDataPlane(batfish.getSnapshot());

    ForwardingAnalysis forwardingAnalysis =
        batfish.loadDataPlane(batfish.getSnapshot()).getForwardingAnalysis();

    // the null route's prefix should not actually null route anything
    assertThat(
        forwardingAnalysis.getNullRoutedIps().get(hostname).get(vrfName),
        not(containsIp(nonForwardingRoutePrefix.getStartIp())));

    // the null route's prefix should not be routable
    assertThat(
        forwardingAnalysis.getRoutableIps().get(hostname).get(vrfName),
        not(containsIp(nonForwardingRoutePrefix.getStartIp())));
  }

  /**
   * When there is a non-forwarding route in the rib with a shorter matching route, the
   * non-forwarding route's network should not routed using the shorter matching route.
   */
  @Test
  public void testNonForwardingRoute_shorterMatch() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Interface i =
        nf.interfaceBuilder()
            .setOwner(c)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/31"))
            .build();

    String hostname = c.getHostname();
    String vrfName = vrf.getName();
    String ifaceName = i.getName();

    Prefix forwardingRoutePrefix = Prefix.parse("2.0.0.0/8");
    Prefix nonForwardingRoutePrefix = Prefix.parse("2.2.0.0/16");
    StaticRoute forwardingRoute =
        StaticRoute.testBuilder()
            .setNextHopInterface(ifaceName)
            .setNetwork(forwardingRoutePrefix)
            .setAdministrativeCost(100)
            .build();
    StaticRoute nonForwardingRoute =
        StaticRoute.testBuilder()
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setNetwork(nonForwardingRoutePrefix)
            .setNonForwarding(true)
            .setAdministrativeCost(100)
            .build();
    vrf.setStaticRoutes(ImmutableSortedSet.of(forwardingRoute, nonForwardingRoute));

    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(hostname, c), temp);
    batfish.computeDataPlane(batfish.getSnapshot());

    ForwardingAnalysis forwardingAnalysis =
        batfish.loadDataPlane(batfish.getSnapshot()).getForwardingAnalysis();

    // the non-forwarding route's prefix should not be null routed
    assertThat(
        forwardingAnalysis.getNullRoutedIps().get(hostname).get(vrfName),
        not(containsIp(nonForwardingRoutePrefix.getStartIp())));

    IpSpace exitsNetwork =
        forwardingAnalysis.getExitsNetwork().get(hostname).get(vrfName).get(ifaceName);

    // the forwarding route's prefix should be routed out the interface
    assertThat(exitsNetwork, containsIp(forwardingRoutePrefix.getStartIp()));

    /* the non-forwarding route's prefix should also be routed out the interface (using the
     * forwarding route).
     */
    assertThat(exitsNetwork, containsIp(nonForwardingRoutePrefix.getStartIp()));
  }

  @Test
  public void testDispositionWithStaticArpIp() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();

    Interface i1 =
        nf.interfaceBuilder()
            .setOwner(c1)
            .setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/24"))
            .build();

    Prefix prefix = Prefix.parse("10.0.0.0/16");
    StaticRoute route =
        StaticRoute.testBuilder()
            .setNextHopInterface(i1.getName())
            .setNetwork(prefix)
            .setAdministrativeCost(100)
            .build();
    vrf1.setStaticRoutes(ImmutableSortedSet.of(route));

    Configuration c2 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).build();
    Interface i2 =
        nf.interfaceBuilder()
            .setOwner(c2)
            .setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.2/24"))
            .setProxyArp(true)
            .build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2), temp);
    batfish.computeDataPlane(batfish.getSnapshot());

    ForwardingAnalysis forwardingAnalysis =
        batfish.loadDataPlane(batfish.getSnapshot()).getForwardingAnalysis();
    Map<String, Map<String, Map<String, IpSpace>>> exitsNetwork =
        forwardingAnalysis.getExitsNetwork();

    assertThat(
        exitsNetwork.get(c1.getHostname()).get(vrf1.getName()).get(i1.getName()),
        containsIp(prefix.getStartIp()));

    // after setting the static arp on i2, should not be exits network anymore
    i2.setAdditionalArpIps(prefix.getStartIp().toIpSpace());
    batfish.computeDataPlane(batfish.getSnapshot());
    forwardingAnalysis = batfish.loadDataPlane(batfish.getSnapshot()).getForwardingAnalysis();
    exitsNetwork = forwardingAnalysis.getExitsNetwork();
    assertThat(
        exitsNetwork.get(c1.getHostname()).get(vrf1.getName()).get(i1.getName()),
        not(containsIp(prefix.getStartIp())));
  }
}
