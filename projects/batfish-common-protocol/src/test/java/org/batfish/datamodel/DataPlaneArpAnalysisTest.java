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
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class DataPlaneArpAnalysisTest {

  private static final String INTERFACE1 = "interface1";

  private static final String INTERFACE2 = "interface2";

  private static final IpSpace IPSPACE1 = new MockIpSpace(1);

  private static final IpSpace IPSPACE2 = new MockIpSpace(2);

  private static final Prefix P1 = Prefix.parse("1.0.0.0/8");

  private static final Prefix P2 = Prefix.parse("2.0.0.0/16");

  private static final Prefix P3 = Prefix.parse("3.0.0.0/24");

  private Configuration.Builder _cb;

  private Interface.Builder _ib;

  private NetworkFactory _nf;

  private Vrf.Builder _vb;

  private Map<String, Map<String, IpSpace>> _arpReplies;

  private Map<Edge, IpSpace> _arpTrueEdge;

  private Map<Edge, IpSpace> _arpTrueEdgeDestIp;

  private Map<Edge, IpSpace> _arpTrueEdgeNextHopIp;

  private Map<String, Map<String, IpSpace>> _ipsRoutedOutInterfaces;

  private Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachable;

  private Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachableArpDestIp;

  private Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachableArpNextHopIp;

  private Map<String, Map<String, IpSpace>> _nullRoutedIps;

  private Map<String, Map<String, IpSpace>> _routableIps;

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWhereDstIpCanBeArpIp;

  private Map<Edge, Set<AbstractRoute>> _routesWithDestIpEdge;

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWithNextHop;

  private Map<String, Map<String, Map<String, Set<AbstractRoute>>>> _routesWithNextHopIpArpFalse;

  private Map<Edge, Set<AbstractRoute>> _routesWithNextHopIpArpTrue;

  private Map<String, Map<String, IpSpace>> _someoneReplies;

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
  public void testComputeArpReplies() {}

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
    //    IpSpace ipsRoutedOutI1 = Sets.union(P1, P2)
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
