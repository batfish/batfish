package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.hasLines;
import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.isAclIpSpaceThat;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.junit.Before;
import org.junit.Test;

public class DataPlaneArpAnalysisTest {

  private static final Edge E1 = new Edge("c1", "i1", "c2", "i2");

  private static final String INTERFACE1 = "interface1";

  private static final String INTERFACE2 = "interface2";

  private static final IpSpace IPSPACE1 = new MockIpSpace(1);

  private static final IpSpace IPSPACE2 = new MockIpSpace(2);

  private static final Prefix P1 = Prefix.parse("1.0.0.0/8");

  private static final Prefix P2 = Prefix.parse("2.0.0.0/16");

  private static final Prefix P3 = Prefix.parse("3.0.0.0/24");

  private Map<String, Map<String, IpSpace>> _arpReplies;

  private Map<Edge, IpSpace> _arpTrueEdge;

  private Map<Edge, IpSpace> _arpTrueEdgeDestIp;

  private Map<Edge, IpSpace> _arpTrueEdgeNextHopIp;

  private Configuration.Builder _cb;

  private Interface.Builder _ib;

  private Map<String, Map<String, IpSpace>> _ipsRoutedOutInterfaces;

  private Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachable;

  private Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachableArpDestIp;

  private Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachableArpNextHopIp;

  private NetworkFactory _nf;

  private Map<String, Map<String, IpSpace>> _nullRoutedIps;

  private Map<String, Map<String, IpSpace>> _routableIps;

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWhereDstIpCanBeArpIp;

  private Map<Edge, Set<AbstractRoute>> _routesWithDestIpEdge;

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWithNextHop;

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWithNextHopIpArpFalse;

  private Map<Edge, Set<AbstractRoute>> _routesWithNextHopIpArpTrue;

  private Map<String, Map<String, IpSpace>> _someoneReplies;

  private Vrf.Builder _vb;

  private DataPlaneArpAnalysis initDataPlaneArpAnalysis() {
    return new DataPlaneArpAnalysis(
        _arpReplies,
        _arpTrueEdge,
        _arpTrueEdgeDestIp,
        _arpTrueEdgeNextHopIp,
        _ipsRoutedOutInterfaces,
        _neighborUnreachable,
        _neighborUnreachableArpDestIp,
        _neighborUnreachableArpNextHopIp,
        _nullRoutedIps,
        _routableIps,
        _routesWhereDstIpCanBeArpIp,
        _routesWithDestIpEdge,
        _routesWithNextHop,
        _routesWithNextHopIpArpFalse,
        _routesWithNextHopIpArpTrue,
        _someoneReplies);
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder();
    _ib = _nf.interfaceBuilder();
  }

  @Test
  public void testComputeArpReplies() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    Vrf vrf1 = _vb.setOwner(c1).build();
    Vrf vrf2 = _vb.setOwner(c2).build();
    Interface i1 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(new InterfaceAddress(P1.getStartIp(), P1.getPrefixLength()))
            .setProxyArp(true)
            .build();
    Interface i2 =
        _ib.setOwner(c2)
            .setVrf(vrf2)
            .setAddress(new InterfaceAddress(P2.getStartIp(), P2.getPrefixLength()))
            .setProxyArp(false)
            .build();
    IpSpace ipsRoutedOutI1 =
        IpWildcardSetIpSpace.builder().including(new IpWildcard(P1), new IpWildcard(P3)).build();
    IpSpace ipsRoutedOutI2 = IpWildcardSetIpSpace.builder().including(new IpWildcard(P2)).build();
    Map<String, Configuration> configurations = ImmutableMap.of(c1.getName(), c1, c2.getName(), c2);
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            c1.getName(),
            ImmutableSortedMap.of(
                vrf1.getName(), MockRib.builder().setRoutableIps(UniverseIpSpace.INSTANCE).build()),
            c2.getName(),
            ImmutableSortedMap.of(
                vrf2.getName(),
                MockRib.builder().setRoutableIps(UniverseIpSpace.INSTANCE).build()));
    _ipsRoutedOutInterfaces =
        ImmutableMap.of(
            c1.getName(),
            ImmutableMap.of(i1.getName(), ipsRoutedOutI1),
            c2.getName(),
            ImmutableMap.of(i2.getName(), ipsRoutedOutI2));
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    Map<String, Map<String, IpSpace>> result =
        dataPlaneArpAnalysis.computeArpReplies(configurations, ribs);

    /* Proxy-arp: Match interface IP, reject what's routed through i1, accept everything else*/
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getName()), hasEntry(equalTo(i1.getName()), containsIp(P1.getStartIp()))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getName()),
            hasEntry(equalTo(i1.getName()), not(containsIp(P1.getEndIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getName()),
            hasEntry(equalTo(i1.getName()), not(containsIp(P3.getStartIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getName()), hasEntry(equalTo(i1.getName()), containsIp(P2.getStartIp()))));
    /* No proxy-arp: just match interface ip*/
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getName()), hasEntry(equalTo(i2.getName()), containsIp(P2.getStartIp()))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getName()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P2.getEndIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getName()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P3.getStartIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getName()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P1.getStartIp())))));
  }

  @Test
  public void testComputeArpRepliesByInterface() {
    Vrf vrf1 = _vb.build();
    Vrf vrf2 = _vb.build();
    Interface i1 =
        _ib.setVrf(vrf1)
            .setAddress(new InterfaceAddress(P1.getStartIp(), P1.getPrefixLength()))
            .setProxyArp(true)
            .build();
    Interface i2 =
        _ib.setVrf(vrf2)
            .setAddress(new InterfaceAddress(P2.getStartIp(), P2.getPrefixLength()))
            .setProxyArp(false)
            .build();
    IpSpace ipsRoutedOutI1 =
        IpWildcardSetIpSpace.builder().including(new IpWildcard(P1), new IpWildcard(P3)).build();
    IpSpace ipsRoutedOutI2 = IpWildcardSetIpSpace.builder().including(new IpWildcard(P2)).build();
    Map<String, Interface> interfaces = ImmutableMap.of(i1.getName(), i1, i2.getName(), i2);
    Map<String, IpSpace> routableIpsByVrf =
        ImmutableMap.of(
            vrf1.getName(), UniverseIpSpace.INSTANCE, vrf2.getName(), UniverseIpSpace.INSTANCE);
    Map<String, IpSpace> ipsRoutedOutInterfaces =
        ImmutableMap.of(i1.getName(), ipsRoutedOutI1, i2.getName(), ipsRoutedOutI2);
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    Map<String, IpSpace> result =
        dataPlaneArpAnalysis.computeArpRepliesByInterface(
            interfaces, routableIpsByVrf, ipsRoutedOutInterfaces);

    /* Proxy-arp: Match interface IP, reject what's routed through i1, accept everything else*/
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(P1.getStartIp())));
    assertThat(result, hasEntry(equalTo(i1.getName()), not(containsIp(P1.getEndIp()))));
    assertThat(result, hasEntry(equalTo(i1.getName()), not(containsIp(P3.getStartIp()))));
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(P2.getStartIp())));
    /* No proxy-arp: just match interface ip*/
    assertThat(result, hasEntry(equalTo(i2.getName()), containsIp(P2.getStartIp())));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P2.getEndIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P3.getStartIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P1.getStartIp()))));
  }

  @Test
  public void testComputeArpTrueEdge() {
    IpSpace nextHopIpSpace = new MockIpSpace(1);
    IpSpace dstIpSpace = new MockIpSpace(2);
    _arpTrueEdgeDestIp = ImmutableMap.of(E1, dstIpSpace);
    _arpTrueEdgeNextHopIp = ImmutableMap.of(E1, nextHopIpSpace);
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    Map<Edge, IpSpace> result = dataPlaneArpAnalysis.computeArpTrueEdge();

    assertThat(
        result,
        hasEntry(
            equalTo(E1),
            isAclIpSpaceThat(
                hasLines(
                    containsInAnyOrder(
                        AclIpSpaceLine.permit(nextHopIpSpace),
                        AclIpSpaceLine.permit(dstIpSpace))))));
  }

  @Test
  public void testComputeArpTrueEdgeDestIp() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    Vrf vrf1 = _vb.setOwner(c1).build();
    Vrf vrf2 = _vb.setOwner(c2).build();
    Interface i1 = _ib.setOwner(c1).setVrf(vrf1).build();
    Ip i2Ip = new Ip(P1.getStartIp().asLong() + 1);
    Interface i2 = _ib.setOwner(c2).setVrf(vrf2).build();
    Map<String, Configuration> configurations = ImmutableMap.of(c1.getName(), c1, c2.getName(), c2);
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            c1.getName(),
            ImmutableSortedMap.of(
                vrf1.getName(),
                MockRib.builder()
                    .setMatchingIps(
                        ImmutableMap.of(
                            P1,
                            AclIpSpace.rejecting(
                                    new Prefix(P1.getEndIp(), Prefix.MAX_PREFIX_LENGTH))
                                .thenPermitting(P1)
                                .build()))
                    .build()));
    Edge edge = new Edge(c1.getName(), i1.getName(), c2.getName(), i2.getName());
    _routesWithDestIpEdge =
        ImmutableMap.of(edge, ImmutableSet.of(new ConnectedRoute(P1, i1.getName())));
    _arpReplies =
        ImmutableMap.of(
            c2.getName(),
            ImmutableMap.of(
                i2.getName(), AclIpSpace.permitting(i2Ip).thenPermitting(P1.getEndIp()).build()));
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    Map<Edge, IpSpace> result = dataPlaneArpAnalysis.computeArpTrueEdgeDestIp(configurations, ribs);

    /* Respond to request for IP on i2. */
    assertThat(result, hasEntry(equalTo(edge), containsIp(i2Ip)));
    /* Do not make ARP request for IP matched by more specific route not going out i1.  */
    assertThat(result, hasEntry(equalTo(edge), not(containsIp(P1.getEndIp()))));
    /* Do not receive response for IP i2 does not own. */
    assertThat(result, hasEntry(equalTo(edge), not(containsIp(P1.getStartIp()))));
  }

  @Test
  public void testComputeArpTrueEdgeNextHopIp() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    Vrf vrf1 = _vb.setOwner(c1).build();
    Vrf vrf2 = _vb.setOwner(c2).build();
    Interface i1 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(new InterfaceAddress(P1.getStartIp(), P1.getPrefixLength()))
            .build();
    Ip i2Ip = new Ip(P1.getStartIp().asLong() + 1);
    Interface i2 =
        _ib.setOwner(c2)
            .setVrf(vrf2)
            .setAddress(new InterfaceAddress(i2Ip, P1.getPrefixLength()))
            .build();
    Edge edge = new Edge(c1.getName(), i1.getName(), c2.getName(), i2.getName());
    Map<String, Configuration> configurations = ImmutableMap.of(c1.getName(), c1, c2.getName(), c2);
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            c1.getName(),
            ImmutableSortedMap.of(
                vrf1.getName(),
                MockRib.builder()
                    .setMatchingIps(
                        ImmutableMap.of(
                            P1,
                            AclIpSpace.rejecting(
                                    new Prefix(P1.getEndIp(), Prefix.MAX_PREFIX_LENGTH))
                                .thenPermitting(P1)
                                .build()))
                    .build()));
    _routesWithNextHopIpArpTrue =
        ImmutableMap.of(
            edge,
            ImmutableSet.of(
                StaticRoute.builder().setNetwork(P1).setNextHopIp(P2.getStartIp()).build()));
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    Map<Edge, IpSpace> result =
        dataPlaneArpAnalysis.computeArpTrueEdgeNextHopIp(configurations, ribs);

    /*
     * Respond for any destination IP in network not matching more specific route not going out i1.
     */
    assertThat(result, hasEntry(equalTo(edge), containsIp(P1.getStartIp())));
    assertThat(result, hasEntry(equalTo(edge), containsIp(i2Ip)));
    /* Do not respond for destination IP matching more specific route not going out i1 */
    assertThat(result, hasEntry(equalTo(edge), not(containsIp(P1.getEndIp()))));
    /* Do not respond for destination IPs not matching route */
    assertThat(result, hasEntry(equalTo(edge), not(containsIp(P2.getStartIp()))));
    assertThat(result, hasEntry(equalTo(edge), not(containsIp(P2.getEndIp()))));
  }

  @Test
  public void testComputeInterfaceArpReplies() {
    InterfaceAddress primary = new InterfaceAddress(P1.getStartIp(), P1.getPrefixLength());
    InterfaceAddress secondary = new InterfaceAddress(P2.getStartIp(), P2.getPrefixLength());
    Interface iNoProxyArp = _ib.setAddresses(primary, secondary).build();
    Interface iProxyArp = _ib.setProxyArp(true).build();
    IpSpace routableIpsForThisVrf = UniverseIpSpace.INSTANCE;
    IpSpace ipsRoutedThroughInterface =
        IpWildcardSetIpSpace.builder().including(new IpWildcard(P1), new IpWildcard(P2)).build();
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    IpSpace noProxyArpResult =
        dataPlaneArpAnalysis.computeInterfaceArpReplies(
            iNoProxyArp, routableIpsForThisVrf, ipsRoutedThroughInterface);
    IpSpace proxyArpResult =
        dataPlaneArpAnalysis.computeInterfaceArpReplies(
            iProxyArp, routableIpsForThisVrf, ipsRoutedThroughInterface);

    /* No proxy-ARP */
    /* Accept IPs belonging to interface */
    assertThat(noProxyArpResult, containsIp(P1.getStartIp()));
    assertThat(noProxyArpResult, containsIp(P2.getStartIp()));
    /* Reject all other IPs */
    assertThat(noProxyArpResult, not(containsIp(P1.getEndIp())));
    assertThat(noProxyArpResult, not(containsIp(P2.getEndIp())));
    assertThat(noProxyArpResult, not(containsIp(P3.getStartIp())));

    /* Proxy-ARP */
    /* Accept IPs belonging to interface */
    assertThat(proxyArpResult, containsIp(P1.getStartIp()));
    assertThat(proxyArpResult, containsIp(P2.getStartIp()));
    /* Reject IPs routed through interface */
    assertThat(proxyArpResult, not(containsIp(P1.getEndIp())));
    assertThat(proxyArpResult, not(containsIp(P2.getEndIp())));
    /* Accept all other routable IPs */
    assertThat(proxyArpResult, containsIp(P3.getStartIp()));
  }

  @Test
  public void testComputeIpsAssignedToThisInterface() {
    InterfaceAddress primary = new InterfaceAddress(P1.getStartIp(), P1.getPrefixLength());
    InterfaceAddress secondary = new InterfaceAddress(P2.getStartIp(), P2.getPrefixLength());
    Interface i = _ib.setAddresses(primary, secondary).build();
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    IpSpace result = dataPlaneArpAnalysis.computeIpsAssignedToThisInterface(i);

    assertThat(result, containsIp(P1.getStartIp()));
    assertThat(result, containsIp(P2.getStartIp()));
    assertThat(result, not(containsIp(P2.getEndIp())));
  }

  @Test
  public void testComputeNullRoutedIps() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    GenericRib<AbstractRoute> rib1 =
        MockRib.builder()
            .setMatchingIps(
                ImmutableMap.of(
                    P1, AclIpSpace.permitting(P1).build(), P2, AclIpSpace.permitting(P2).build()))
            .build();
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(c1, ImmutableSortedMap.of(v1, rib1));
    AbstractRoute nullRoute =
        StaticRoute.builder()
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setNetwork(P1)
            .build();
    AbstractRoute otherRoute = new ConnectedRoute(P2, i1);
    Map<String, Map<String, Fib>> fibs =
        ImmutableSortedMap.of(
            c1,
            ImmutableSortedMap.of(
                v1,
                MockFib.builder()
                    .setNextHopInterfaces(
                        ImmutableMap.of(
                            nullRoute,
                            ImmutableMap.of(
                                Interface.NULL_INTERFACE_NAME,
                                ImmutableMap.of(
                                    Route.UNSET_ROUTE_NEXT_HOP_IP, ImmutableSet.of(nullRoute))),
                            otherRoute,
                            ImmutableMap.of(
                                i1,
                                ImmutableMap.of(
                                    Route.UNSET_ROUTE_NEXT_HOP_IP, ImmutableSet.of(otherRoute)))))
                    .build()));
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();
    Map<String, Map<String, IpSpace>> result =
        dataPlaneArpAnalysis.computeNullRoutedIps(ribs, fibs);

    /* IPs for the null route should appear */
    assertThat(result, hasEntry(equalTo(c1), hasEntry(equalTo(v1), containsIp(P1.getStartIp()))));
    assertThat(result, hasEntry(equalTo(c1), hasEntry(equalTo(v1), containsIp(P1.getEndIp()))));
    /* IPs for the non-null route should not appear */
    assertThat(
        result, hasEntry(equalTo(c1), hasEntry(equalTo(v1), not(containsIp(P2.getStartIp())))));
    assertThat(
        result, hasEntry(equalTo(c1), hasEntry(equalTo(v1), not(containsIp(P2.getEndIp())))));
  }

  @Test
  public void testComputeRouteMatchConditions() {
    Set<AbstractRoute> routes =
        ImmutableSet.of(new ConnectedRoute(P1, INTERFACE1), new ConnectedRoute(P2, INTERFACE2));
    MockRib rib =
        MockRib.builder().setMatchingIps(ImmutableMap.of(P1, IPSPACE1, P2, IPSPACE2)).build();
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();

    /* Resulting IP space should permit matching IPs */
    assertThat(
        dataPlaneArpAnalysis.computeRouteMatchConditions(routes, rib),
        isAclIpSpaceThat(
            hasLines(
                containsInAnyOrder(
                    AclIpSpaceLine.permit(IPSPACE1), AclIpSpaceLine.permit(IPSPACE2)))));
  }
}
