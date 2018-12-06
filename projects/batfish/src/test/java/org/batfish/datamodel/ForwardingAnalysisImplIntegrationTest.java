package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
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
        StaticRoute.builder()
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setNetwork(nonForwardingRoutePrefix)
            .setNonForwarding(true)
            .setAdministrativeCost(100)
            .build();
    vrf.setStaticRoutes(ImmutableSortedSet.of(nonForwardingRoute));

    String hostname = c.getHostname();
    String vrfName = vrf.getName();

    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(hostname, c), temp);
    batfish.computeDataPlane(false);

    ForwardingAnalysis forwardingAnalysis = batfish.loadDataPlane().getForwardingAnalysis();

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
            .setAddress(new InterfaceAddress("1.1.1.1/31"))
            .build();

    String hostname = c.getHostname();
    String vrfName = vrf.getName();
    String ifaceName = i.getName();

    Prefix forwardingRoutePrefix = Prefix.parse("2.0.0.0/8");
    Prefix nonForwardingRoutePrefix = Prefix.parse("2.2.0.0/16");
    StaticRoute forwardingRoute =
        StaticRoute.builder()
            .setNextHopInterface(ifaceName)
            .setNetwork(forwardingRoutePrefix)
            .setAdministrativeCost(100)
            .build();
    StaticRoute nonForwardingRoute =
        StaticRoute.builder()
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setNetwork(nonForwardingRoutePrefix)
            .setNonForwarding(true)
            .setAdministrativeCost(100)
            .build();
    vrf.setStaticRoutes(ImmutableSortedSet.of(forwardingRoute, nonForwardingRoute));

    Batfish batfish = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(hostname, c), temp);
    batfish.computeDataPlane(false);

    ForwardingAnalysis forwardingAnalysis = batfish.loadDataPlane().getForwardingAnalysis();

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
}
