package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.hasLines;
import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.isAclIpSpaceThat;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.hamcrest.Matchers;
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
  public void testComputeIpsAssignedToThisInterface() {
    InterfaceAddress primary = new InterfaceAddress(P1.getStartIp(), P1.getPrefixLength());
    InterfaceAddress secondary = new InterfaceAddress(P2.getStartIp(), P2.getPrefixLength());
    Interface i = _ib.setAddresses(primary, ImmutableSet.of(secondary)).build();
    DataPlaneArpAnalysis dataPlaneArpAnalysis = initDataPlaneArpAnalysis();

    assertThat(
        dataPlaneArpAnalysis.computeIpsAssignedToThisInterface(i), containsIp(P1.getStartIp()));
    assertThat(
        dataPlaneArpAnalysis.computeIpsAssignedToThisInterface(i), containsIp(P2.getStartIp()));
    assertThat(
        dataPlaneArpAnalysis.computeIpsAssignedToThisInterface(i), not(containsIp(P2.getEndIp())));
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
                    AclIpSpaceLine.builder().setIpSpace(IPSPACE1).build(),
                    AclIpSpaceLine.builder().setIpSpace(IPSPACE2).build()))));
  }
}
