package org.batfish.datamodel;

import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpFalseDestIp;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpFalseNextHopIp;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpFalseNhipRoutes;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpReplies;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpTrueEdge;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpTrueEdgeDestIp;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpTrueEdgeNextHopIp;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeDeliveredToSubnet;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeExitsNetwork;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeInsufficientInfo;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeInterfaceArpReplies;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeIpsAssignedToThisInterfaceForArpReplies;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeIpsRoutedOutInterfaces;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeNeighborUnreachable;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeNextVrfIps;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeNullRoutedIps;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeOwnedIpsByVrf;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWhereDstIpCanBeArpIp;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWithDestIpEdge;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpFalseForInterface;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpTrue;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeSomeoneReplies;
import static org.batfish.datamodel.ForwardingAnalysisImpl.routableSpace;
import static org.batfish.datamodel.ForwardingAnalysisImpl.sparseKeys;
import static org.batfish.datamodel.ForwardingAnalysisImpl.union;
import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.hasLines;
import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.isAclIpSpaceThat;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.specifier.LocationInfoUtils.computeLocationInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.IpOwnersBaseImpl;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link ForwardingAnalysisImpl}. */
public class ForwardingAnalysisImplTest {

  private static final String INTERFACE1 = "interface1";

  private static final String INTERFACE2 = "interface2";

  private static final IpSpace IPSPACE1 = new MockIpSpace(1);

  private static final IpSpace IPSPACE2 = new MockIpSpace(2);

  private static final Prefix P1 = Prefix.parse("1.0.0.0/8");
  private static final Prefix P1_1 = Prefix.parse("1.0.1.0/24");

  private static final Prefix P2 = Prefix.parse("2.0.0.0/16");
  private static final Prefix P2_2 = Prefix.parse("2.0.2.0/24");

  private static final Prefix P3 = Prefix.parse("3.0.0.0/24");

  private Configuration.Builder _cb;

  private Interface.Builder _ib;

  private Vrf.Builder _vb;

  private static Map<String, Map<String, Map<Prefix, IpSpace>>> computeMatchingIps(
      Map<String, Map<String, Fib>> fibs) {
    return ForwardingAnalysisImpl.computeMatchingIps(fibs, ForwardingAnalysisImpl.sparseKeys(fibs));
  }

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    _cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = nf.vrfBuilder();
    _ib = nf.interfaceBuilder();
  }

  @Test
  public void testComputeArpReplies() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    Map<String, Configuration> configs =
        ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Vrf vrf1 = _vb.setOwner(c1).build();
    Vrf vrf2 = _vb.setOwner(c2).build();
    Interface i1 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength()))
            .setProxyArp(true)
            .build();
    Interface i2 =
        _ib.setOwner(c2)
            .setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.create(P2.getFirstHostIp(), P2.getPrefixLength()))
            .setProxyArp(false)
            .build();
    Ip additionalIp = Ip.parse("10.10.10.1");
    i1.setAdditionalArpIps(additionalIp.toIpSpace());
    i2.setAdditionalArpIps(additionalIp.toIpSpace());
    IpSpace ipsRoutedOutI1 =
        IpWildcardSetIpSpace.builder()
            .including(IpWildcard.create(P1), IpWildcard.create(P3))
            .build();
    IpSpace ipsRoutedOutI2 =
        IpWildcardSetIpSpace.builder().including(IpWildcard.create(P2)).build();
    Map<String, Configuration> configurations =
        ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Map<String, Map<String, IpSpace>> routableIps =
        ImmutableMap.of(
            c1.getHostname(),
            ImmutableMap.of(vrf1.getName(), UniverseIpSpace.INSTANCE),
            c2.getHostname(),
            ImmutableMap.of(vrf2.getName(), UniverseIpSpace.INSTANCE));
    Map<String, Map<String, Map<String, IpSpace>>> ipsRoutedOutInterfaces =
        ImmutableMap.of(
            c1.getHostname(),
            ImmutableMap.of(vrf1.getName(), ImmutableMap.of(i1.getName(), ipsRoutedOutI1)),
            c2.getHostname(),
            ImmutableMap.of(vrf2.getName(), ImmutableMap.of(i2.getName(), ipsRoutedOutI2)));
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        new TestIpOwners(configs).getInterfaceOwners(false);
    Map<String, Map<String, IpSpace>> result =
        computeArpReplies(configurations, ipsRoutedOutInterfaces, interfaceOwnedIps, routableIps);

    /* Proxy-arp: Match interface IP, reject what's routed through i1, accept everything else*/
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(i1.getName()), containsIp(P1.getFirstHostIp()))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(i1.getName()), not(containsIp(P1.getLastHostIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(i1.getName()), not(containsIp(P3.getFirstHostIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(i1.getName()), containsIp(P2.getStartIp()))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()), hasEntry(equalTo(i1.getName()), containsIp(additionalIp))));
    /* No proxy-arp: just match interface ip and additional arp ip */
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()),
            hasEntry(equalTo(i2.getName()), containsIp(P2.getFirstHostIp()))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P2.getLastHostIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P3.getFirstHostIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P1.getFirstHostIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()), hasEntry(equalTo(i2.getName()), containsIp(additionalIp))));
  }

  @Test
  public void testComputeArpRepliesByInterface() {
    Configuration config = _cb.build();
    _ib.setOwner(config);
    _vb.setOwner(config);
    Vrf vrf1 = _vb.build();
    Vrf vrf2 = _vb.build();
    Interface i1 =
        _ib.setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength()))
            .setProxyArp(true)
            .build();
    Ip nonRoutedOwnedIp = Ip.parse("1.0.0.2"); // In P1, neither first nor last host IP.
    ConcreteInterfaceAddress nonRoutedOwnedAddress =
        ConcreteInterfaceAddress.create(nonRoutedOwnedIp, Prefix.MAX_PREFIX_LENGTH);
    Interface i1B =
        _ib.setVrf(vrf1)
            .setAddresses(nonRoutedOwnedAddress)
            .setAddressMetadata(
                ImmutableMap.of(
                    nonRoutedOwnedAddress,
                    ConnectedRouteMetadata.builder()
                        .setGenerateConnectedRoute(false)
                        .setGenerateLocalRoute(false)
                        .build()))
            .setProxyArp(false)
            .build();
    Interface i2 =
        _ib.setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.create(P2.getFirstHostIp(), P2.getPrefixLength()))
            .setProxyArp(false)
            .build();
    Interface i3 = _ib.setAddress(null).setProxyArp(true).build();
    Interface i4 =
        _ib.setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1"))).setProxyArp(false).build();
    IpSpace ipsRoutedOutI1 =
        IpWildcardSetIpSpace.builder()
            .including(IpWildcard.create(P1), IpWildcard.create(P3))
            .build();
    IpSpace ipsRoutedOutI2 =
        IpWildcardSetIpSpace.builder().including(IpWildcard.create(P2)).build();
    IpSpace ipsRoutedOutI3 = EmptyIpSpace.INSTANCE;
    Map<String, Interface> interfaces =
        ImmutableMap.of(
            i1.getName(),
            i1,
            i1B.getName(),
            i1B,
            i2.getName(),
            i2,
            i3.getName(),
            i3,
            i4.getName(),
            i4);
    Map<String, IpSpace> routableIpsByVrf =
        ImmutableMap.of(
            vrf1.getName(), UniverseIpSpace.INSTANCE, vrf2.getName(), UniverseIpSpace.INSTANCE);
    Map<String, Map<String, IpSpace>> ipsRoutedOutInterfaces =
        ImmutableMap.of(
            vrf1.getName(),
            ImmutableMap.of(i1.getName(), ipsRoutedOutI1, i1B.getName(), EmptyIpSpace.INSTANCE),
            vrf2.getName(),
            ImmutableMap.of(i2.getName(), ipsRoutedOutI2, i3.getName(), ipsRoutedOutI3));

    i1.setAdditionalArpIps(IpIpSpace.create(Ip.parse("10.10.10.1")));
    i2.setAdditionalArpIps(IpIpSpace.create(Ip.parse("10.10.10.2")));
    i3.setAdditionalArpIps(IpIpSpace.create(Ip.parse("10.10.10.3")));
    i4.setAdditionalArpIps(IpIpSpace.create(Ip.parse("10.10.10.4")));

    Map<String, Configuration> configs = ImmutableMap.of(config.getHostname(), config);
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        new TestIpOwners(configs).getInterfaceOwners(false);
    Map<String, IpSpace> ownedIpsByVrf =
        computeOwnedIpsByVrf(
            config.getActiveInterfaces(), interfaceOwnedIps.get(config.getHostname()));
    Map<String, IpSpace> result =
        ForwardingAnalysisImpl.computeArpRepliesByInterface(
            interfaces, routableIpsByVrf, ipsRoutedOutInterfaces, interfaceOwnedIps, ownedIpsByVrf);

    /* Proxy-arp: Match interface IP, owned IP of other interface in vrf, reject what's routed
    through i1, accept everything else */
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(P1.getFirstHostIp())));
    assertThat(result, hasEntry(equalTo(i1.getName()), not(containsIp(P1.getLastHostIp()))));
    assertThat(result, hasEntry(equalTo(i1.getName()), not(containsIp(P3.getFirstHostIp()))));
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(P2.getStartIp())));
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(Ip.parse("10.10.10.1"))));
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(nonRoutedOwnedIp)));
    /* No proxy-arp: just match interface ip and additional arp ip */
    assertThat(result, hasEntry(equalTo(i1B.getName()), containsIp(nonRoutedOwnedIp)));
    assertThat(result, hasEntry(equalTo(i1B.getName()), not(containsIp(P1.getFirstHostIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), containsIp(P2.getFirstHostIp())));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P2.getLastHostIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P3.getFirstHostIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P1.getFirstHostIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), containsIp(Ip.parse("10.10.10.2"))));
    /* No interface IPs: reject everything */
    assertThat(result, hasEntry(equalTo(i3.getName()), equalTo(EmptyIpSpace.INSTANCE)));
    /* Link-local address is present, honor additional ARP IPs  */
    assertThat(result, hasEntry(equalTo(i4.getName()), containsIp(Ip.parse("10.10.10.4"))));
    /* Proxy-arp: Match owned IPs by other interface in VRF not covered by route */
  }

  @Test
  public void testComputeArpReplies_VRRP() {
    Configuration c = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    _ib.setOwner(c);
    Vrf vrf1 = _vb.build();
    Vrf vrf2 = _vb.build();
    ConcreteInterfaceAddress i1SourceAddress =
        ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength());
    String i1Name = "i1";
    String i2Name = "i2";
    Interface i1 =
        _ib.setVrf(vrf1)
            .setName(i1Name)
            .setAddress(i1SourceAddress)
            .setVrrpGroups(
                ImmutableSortedMap.of(
                    1,
                    VrrpGroup.builder()
                        .setPriority(100)
                        .setSourceAddress(i1SourceAddress)
                        .setVirtualAddresses(i1Name, Ip.parse("1.1.1.1"))
                        .build()))
            .build();

    ConcreteInterfaceAddress i2SourceAddress =
        ConcreteInterfaceAddress.create(P1.getLastHostIp(), P1.getPrefixLength());
    Interface i2 =
        _ib.setVrf(vrf2)
            .setName(i2Name)
            .setAddress(i2SourceAddress)
            .setVrrpGroups(
                ImmutableSortedMap.of(
                    1,
                    VrrpGroup.builder()
                        .setPriority(110)
                        .setSourceAddress(i2SourceAddress)
                        .setVirtualAddresses(i2Name, Ip.parse("1.1.1.1"))
                        .build()))
            .build();

    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        new TestIpOwners(configs).getInterfaceOwners(false);
    IpSpace p1IpSpace = IpWildcard.create(P1).toIpSpace();
    IpSpace i1ArpReplies =
        computeInterfaceArpReplies(
            i1, UniverseIpSpace.INSTANCE, p1IpSpace, interfaceOwnedIps, EmptyIpSpace.INSTANCE);
    IpSpace i2ArpReplies =
        computeInterfaceArpReplies(
            i2, UniverseIpSpace.INSTANCE, p1IpSpace, interfaceOwnedIps, EmptyIpSpace.INSTANCE);

    assertThat(i1ArpReplies, not(containsIp(Ip.parse("1.1.1.1"))));
    assertThat(i2ArpReplies, containsIp(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testComputeArpTrueEdge() {
    IpSpace nextHopIpSpace = new MockIpSpace(1);
    IpSpace dstIpSpace = new MockIpSpace(2);
    Edge e1 = Edge.of("c1", "i1", "c2", "i2");
    Map<Edge, IpSpace> arpTrueEdgeDestIp = ImmutableMap.of(e1, dstIpSpace);
    Map<Edge, IpSpace> arpTrueEdgeNextHopIp = ImmutableMap.of(e1, nextHopIpSpace);
    Map<Edge, IpSpace> result = computeArpTrueEdge(arpTrueEdgeDestIp, arpTrueEdgeNextHopIp);

    assertThat(
        result,
        hasEntry(
            equalTo(e1),
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
    Ip i2Ip = Ip.create(P1.getStartIp().asLong() + 1);
    Interface i2 = _ib.setOwner(c2).setVrf(vrf2).build();
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1.getHostname(),
            ImmutableMap.of(
                vrf1.getName(),
                MockFib.builder()
                    .setMatchingIps(
                        ImmutableMap.of(
                            P1,
                            AclIpSpace.rejecting(P1.getEndIp().toIpSpace())
                                .thenPermitting(P1.toIpSpace())
                                .build()))
                    .build()));
    Edge edge = Edge.of(c1.getHostname(), i1.getName(), c2.getHostname(), i2.getName());
    Map<Edge, Set<AbstractRoute>> routesWithDestIpEdge =
        ImmutableMap.of(edge, ImmutableSet.of(new ConnectedRoute(P1, i1.getName())));
    Map<String, Map<String, IpSpace>> arpReplies =
        ImmutableMap.of(
            c2.getHostname(),
            ImmutableMap.of(
                i2.getName(),
                AclIpSpace.permitting(i2Ip.toIpSpace())
                    .thenPermitting(P1.getEndIp().toIpSpace())
                    .build()));
    Map<Edge, IpSpace> result =
        computeArpTrueEdgeDestIp(
            computeMatchingIps(fibs).get(c1.getHostname()).get(vrf1.getName()),
            routesWithDestIpEdge,
            arpReplies);

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
            .setAddress(ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength()))
            .build();
    Ip i2Ip = Ip.create(P1.getStartIp().asLong() + 1);
    Interface i2 =
        _ib.setOwner(c2)
            .setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.create(i2Ip, P1.getPrefixLength()))
            .build();
    Edge edge = Edge.of(c1.getHostname(), i1.getName(), c2.getHostname(), i2.getName());
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1.getHostname(),
            ImmutableMap.of(
                vrf1.getName(),
                MockFib.builder()
                    .setMatchingIps(
                        ImmutableMap.of(
                            P1,
                            AclIpSpace.rejecting(P1.getEndIp().toIpSpace())
                                .thenPermitting(P1.toIpSpace())
                                .build()))
                    .build()));
    Map<Edge, Set<AbstractRoute>> routesWithNextHopIpArpTrue =
        ImmutableMap.of(
            edge,
            ImmutableSet.of(
                StaticRoute.testBuilder()
                    .setNetwork(P1)
                    .setNextHopIp(P2.getStartIp())
                    .setAdministrativeCost(1)
                    .build()));
    Map<Edge, IpSpace> result =
        computeArpTrueEdgeNextHopIp(
            computeMatchingIps(fibs).get(c1.getHostname()).get(vrf1.getName()),
            routesWithNextHopIpArpTrue);

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
  public void testComputeOwnedIpsByVrf() {
    Configuration config = _cb.build();
    _ib.setOwner(config);
    Interface iNoAddresses = _ib.build();
    ConcreteInterfaceAddress address =
        ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength());
    // address not read, just set for clarity
    Interface iAddress = _ib.setAddress(address).build();
    Map<String, Interface> activeInterfaces =
        ImmutableMap.of(iNoAddresses.getName(), iNoAddresses, iAddress.getName(), iAddress);
    // Note there is no key for iNoAddresses
    Map<String, Set<Ip>> interfaceOwnedIps =
        ImmutableMap.of(iAddress.getName(), ImmutableSet.of(P1.getFirstHostIp()));
    Map<String, IpSpace> ownedIpsByVrf = computeOwnedIpsByVrf(activeInterfaces, interfaceOwnedIps);

    assertThat(ownedIpsByVrf, hasKeys(iAddress.getVrfName()));
    assertThat(ownedIpsByVrf.get(iAddress.getVrfName()), containsIp(P1.getFirstHostIp()));
  }

  @Test
  public void testComputeInterfaceArpReplies() {
    Configuration config = _cb.build();
    _ib.setOwner(config);
    ConcreteInterfaceAddress primary =
        ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength());
    ConcreteInterfaceAddress secondary =
        ConcreteInterfaceAddress.create(P2.getFirstHostIp(), P2.getPrefixLength());
    Interface iNoProxyArp = _ib.setAddresses(primary, secondary).build();
    Interface iProxyArp = _ib.setProxyArp(true).build();
    IpSpace routableIpsForThisVrf = UniverseIpSpace.INSTANCE;
    IpSpace ipsRoutedThroughInterface =
        IpWildcardSetIpSpace.builder()
            .including(IpWildcard.create(P1), IpWildcard.create(P2))
            .build();
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        new TestIpOwners(ImmutableMap.of(config.getHostname(), config)).getInterfaceOwners(false);
    Map<String, IpSpace> ownedIpsByVrf =
        computeOwnedIpsByVrf(
            config.getActiveInterfaces(), interfaceOwnedIps.get(config.getHostname()));
    IpSpace noProxyArpResult =
        computeInterfaceArpReplies(
            iNoProxyArp,
            routableIpsForThisVrf,
            ipsRoutedThroughInterface,
            interfaceOwnedIps,
            ownedIpsByVrf.get(iNoProxyArp.getVrfName()));
    IpSpace proxyArpResult =
        computeInterfaceArpReplies(
            iProxyArp,
            routableIpsForThisVrf,
            ipsRoutedThroughInterface,
            interfaceOwnedIps,
            ownedIpsByVrf.get(iProxyArp.getVrfName()));

    /* No proxy-ARP */
    /* Accept IPs belonging to interface */
    assertThat(noProxyArpResult, containsIp(P1.getFirstHostIp()));
    assertThat(noProxyArpResult, containsIp(P2.getFirstHostIp()));
    /* Reject all other IPs */
    assertThat(noProxyArpResult, not(containsIp(P1.getLastHostIp())));
    assertThat(noProxyArpResult, not(containsIp(P2.getLastHostIp())));
    assertThat(noProxyArpResult, not(containsIp(P3.getFirstHostIp())));

    /* Proxy-ARP */
    /* Accept IPs belonging to interface */
    assertThat(proxyArpResult, containsIp(P1.getFirstHostIp()));
    assertThat(proxyArpResult, containsIp(P2.getFirstHostIp()));
    /* Reject IPs routed through interface */
    assertThat(proxyArpResult, not(containsIp(P1.getLastHostIp())));
    assertThat(proxyArpResult, not(containsIp(P2.getLastHostIp())));
    /* Accept all other routable IPs */
    assertThat(proxyArpResult, containsIp(P3.getFirstHostIp()));
  }

  @Test
  public void testComputeIpsAssignedToThisInterfaceForArpReplies() {
    Configuration config = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(config.getHostname(), config);
    _ib.setOwner(config);
    ConcreteInterfaceAddress primary =
        ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength());
    InterfaceAddress secondary =
        ConcreteInterfaceAddress.create(P2.getFirstHostIp(), P2.getPrefixLength());
    Interface i = _ib.setAddresses(primary, secondary).build();
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        new TestIpOwners(configs).getInterfaceOwners(false);
    IpSpace result = computeIpsAssignedToThisInterfaceForArpReplies(i, interfaceOwnedIps);

    assertThat(result, containsIp(P1.getFirstHostIp()));
    assertThat(result, containsIp(P2.getFirstHostIp()));
    assertThat(result, not(containsIp(P2.getLastHostIp())));

    Ip linkLocalIp = Ip.parse("169.254.0.1");
    Interface i2 = _ib.setAddresses(LinkLocalAddress.of(linkLocalIp)).build();
    IpSpace result2 = computeIpsAssignedToThisInterfaceForArpReplies(i2, interfaceOwnedIps);
    assertThat(result2, containsIp(linkLocalIp));
  }

  @Test
  public void testComputeIpsRoutedOutInterfaces() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    ConnectedRoute r1 = new ConnectedRoute(P1, i1);
    StaticRoute nullRoute =
        StaticRoute.testBuilder()
            .setNetwork(P2)
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setAdministrativeCost(1)
            .build();
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1,
                MockFib.builder()
                    .setMatchingIps(ImmutableMap.of(P1, P1.toIpSpace(), P2, P2.toIpSpace()))
                    .build()));
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1,
                ImmutableMap.of(
                    i1,
                    ImmutableSet.of(r1),
                    Interface.NULL_INTERFACE_NAME,
                    ImmutableSet.of(nullRoute))));
    List<Entry<String, String>> allVrfs = sparseKeys(fibs);
    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeIpsRoutedOutInterfaces(
            ForwardingAnalysisImpl.computeMatchingIps(fibs, allVrfs), routesWithNextHop, allVrfs);

    /* Should contain IPs matching the route */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getStartIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1), hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getEndIp())))));
    /* Should not contain IP not matching the route */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), not(containsIp(P2.getStartIp()))))));
    /* Null interface should be excluded because we would not be able to tie back to single VRF. */
    assertThat(result, hasEntry(equalTo(c1), not(hasKey(equalTo(Interface.NULL_INTERFACE_NAME)))));
  }

  @Test
  public void testComputeNeighborUnreachableOrExitsNetwork() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    Map<String, Map<String, Map<String, IpSpace>>> arpFalseDestIp =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, P1.getStartIp().toIpSpace())));
    Map<String, Map<String, Map<String, IpSpace>>> arpFalseNextHopIp =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, P1.getEndIp().toIpSpace())));
    Map<String, Map<String, Map<String, IpSpace>>> result =
        union(arpFalseDestIp, arpFalseNextHopIp);

    /* Should contain both IPs. */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getStartIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1), hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getEndIp())))));
    /* Should not contain unrelated IPs. */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), not(containsIp(P2.getEndIp()))))));
  }

  @Test
  public void testComputeArpFalseDestIp() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    AbstractRoute ifaceRoute = new ConnectedRoute(P1, i1);
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1, MockFib.builder().setMatchingIps(ImmutableMap.of(P1, P1.toIpSpace())).build()));
    Set<AbstractRoute> routesWhereDstIpCanBeArpIp = ImmutableSet.of(ifaceRoute);
    IpSpace someoneReplies = P1.getEndIp().toIpSpace();
    IpSpace result =
        computeArpFalseDestIp(
            computeMatchingIps(fibs).get(c1).get(v1), routesWhereDstIpCanBeArpIp, someoneReplies);

    /* Should contain IP in the route's prefix that sees no reply */
    assertThat(result, containsIp(P1.getStartIp()));
    /* Should not contain IP in the route's prefix that sees reply */
    assertThat(result, not(containsIp(P1.getEndIp())));
    /* Should not contain other IPs */
    assertThat(result, not(containsIp(P2.getEndIp())));
  }

  @Test
  public void testComputeArpFalseDestIpNoNeighbors() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    AbstractRoute ifaceRoute = new ConnectedRoute(P1, i1);
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1, MockFib.builder().setMatchingIps(ImmutableMap.of(P1, P1.toIpSpace())).build()));
    Set<AbstractRoute> routesWhereDstIpCanBeArpIp = ImmutableSet.of(ifaceRoute);
    IpSpace someoneReplies = EmptyIpSpace.INSTANCE;
    IpSpace result =
        computeArpFalseDestIp(
            computeMatchingIps(fibs).get(c1).get(v1), routesWhereDstIpCanBeArpIp, someoneReplies);

    /*
     * Since _someoneReplies is empty, all IPs for which longest-prefix-match route has no
     * next-hop-ip should be in the result space.
     */
    assertThat(result, containsIp(P1.getStartIp()));
    assertThat(result, containsIp(P1.getEndIp()));

    /* Should not contain other IPs */
    assertThat(result, not(containsIp(P2.getEndIp())));
  }

  @Test
  public void testComputeNeighborUnreachableArpNextHopIp() {
    String c1 = "c1";
    String v1 = "v1";
    AbstractRoute r1 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    Set<AbstractRoute> routesWithNextHopIpArpFalse = ImmutableSet.of(r1);
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1, MockFib.builder().setMatchingIps(ImmutableMap.of(P1, P1.toIpSpace())).build()));

    IpSpace result =
        computeArpFalseNextHopIp(
            computeMatchingIps(fibs).get(c1).get(v1), routesWithNextHopIpArpFalse);

    /* IPs matching some route on interface with no response should appear */
    assertThat(result, containsIp(P1.getStartIp()));
    assertThat(result, containsIp(P1.getEndIp()));
    /* Other IPs should not appear */
    assertThat(result, not(containsIp(P2.getStartIp())));
  }

  @Test
  public void testComputeNextVrfIpsByNodeVrf() {
    String c1 = "c1";
    String v1 = "v1";
    String v2 = "v2";
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1,
                MockFib.builder()
                    .setMatchingIps(ImmutableMap.of(P1, P1_1.toIpSpace()))
                    .setFibEntries(
                        ImmutableMap.of(
                            Ip.ZERO,
                            ImmutableSet.of(
                                new FibEntry(
                                    FibNextVrf.of(v2),
                                    ImmutableList.of(
                                        StaticRoute.testBuilder()
                                            .setAdmin(1)
                                            .setNetwork(P1)
                                            .setNextHop(
                                                org.batfish.datamodel.route.nh.NextHopVrf.of(v2))
                                            .build())))))
                    .build(),
                v2,
                MockFib.builder()
                    .setMatchingIps(ImmutableMap.of(P2, P2_2.toIpSpace()))
                    .setFibEntries(
                        ImmutableMap.of(
                            Ip.ZERO,
                            ImmutableSet.of(
                                new FibEntry(
                                    FibNextVrf.of(v1),
                                    ImmutableList.of(
                                        StaticRoute.testBuilder()
                                            .setAdmin(1)
                                            .setNetwork(P2)
                                            .setNextHop(
                                                org.batfish.datamodel.route.nh.NextHopVrf.of(v1))
                                            .build())))))
                    .build()));

    // Each VRF should delegate the matching IpSpace for its nextVrf route to the other VRF.
    Map<String, Map<Prefix, IpSpace>> matchingIps = computeMatchingIps(fibs).get(c1);
    assertThat(
        computeNextVrfIps(matchingIps.get(v1), fibs.get(c1).get(v1)),
        equalTo(ImmutableMap.of(v2, P1_1.toIpSpace())));
    assertThat(
        computeNextVrfIps(matchingIps.get(v2), fibs.get(c1).get(v2)),
        equalTo(ImmutableMap.of(v1, P2_2.toIpSpace())));
  }

  @Test
  public void testComputeNullRoutedIps() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    AbstractRoute nullRoute =
        StaticRoute.testBuilder()
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .setNetwork(P1)
            .setAdministrativeCost(1)
            .build();
    AbstractRoute otherRoute = new ConnectedRoute(P2, i1);
    Map<String, Map<String, Fib>> fibs =
        ImmutableSortedMap.of(
            c1,
            ImmutableSortedMap.of(
                v1,
                MockFib.builder()
                    .setMatchingIps(
                        ImmutableMap.of(
                            P1,
                            AclIpSpace.permitting(P1.toIpSpace()).build(),
                            P2,
                            AclIpSpace.permitting(P2.toIpSpace()).build()))
                    .setFibEntries(
                        ImmutableMap.of(
                            Ip.ZERO,
                            ImmutableSet.of(
                                new FibEntry(FibNullRoute.INSTANCE, ImmutableList.of(nullRoute)),
                                new FibEntry(
                                    FibForward.of(null, otherRoute.getNextHopInterface()),
                                    ImmutableList.of(otherRoute)))))
                    .build()));
    IpSpace result =
        computeNullRoutedIps(computeMatchingIps(fibs).get(c1).get(v1), fibs.get(c1).get(v1));

    /* IPs for the null route should appear */
    assertThat(result, containsIp(P1.getStartIp()));
    assertThat(result, containsIp(P1.getEndIp()));
    /* IPs for the non-null route should not appear */
    assertThat(result, not(containsIp(P2.getStartIp())));
    assertThat(result, not(containsIp(P2.getEndIp())));
  }

  private static FibEntry mockFibEntry(Prefix network) {
    return new FibEntry(
        FibForward.of(Ip.ZERO, "iface"),
        ImmutableList.of(
            StaticRoute.testBuilder().setAdministrativeCost(1).setNetwork(network).build()));
  }

  @Test
  public void testRoutableIpSpace() {
    Fib hasDefault =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    Ip.ZERO,
                    ImmutableSet.of(
                        mockFibEntry(Prefix.parse("1.2.3.0/24")), mockFibEntry(Prefix.ZERO))))
            .build();
    assertThat(routableSpace(hasDefault), equalTo(UniverseIpSpace.INSTANCE));

    Fib isEmpty = MockFib.builder().setFibEntries(ImmutableMap.of()).build();
    assertThat(routableSpace(isEmpty), equalTo(EmptyIpSpace.INSTANCE));

    Fib standard =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    Ip.ZERO,
                    ImmutableSet.of(
                        mockFibEntry(Prefix.parse("1.2.3.0/24")),
                        mockFibEntry(Prefix.parse("2.3.4.0/24")))))
            .build();
    assertThat(
        routableSpace(standard),
        allOf(
            containsIp(Ip.parse("1.2.3.4")),
            containsIp(Ip.parse("2.3.4.5")),
            not(containsIp(Ip.ZERO)),
            not(containsIp(Ip.parse("2.0.0.0"))),
            not(containsIp(Ip.MAX))));

    // Singleton is not converted to wildcard set
    Fib singleton =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(Ip.ZERO, ImmutableSet.of(mockFibEntry(Prefix.parse("1.2.3.0/24")))))
            .build();
    assertThat(routableSpace(singleton), equalTo(Prefix.parse("1.2.3.0/24").toIpSpace()));

    // Compressible to singleton is compressed and not converted to wildcard set
    Fib compressibleToSingleton =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    Ip.ZERO,
                    ImmutableSet.of(
                        mockFibEntry(Prefix.parse("1.2.3.1/32")),
                        mockFibEntry(Prefix.parse("1.2.3.0/24")),
                        mockFibEntry(Prefix.parse("1.2.3.4/32")))))
            .build();
    assertThat(
        routableSpace(compressibleToSingleton), equalTo(Prefix.parse("1.2.3.0/24").toIpSpace()));
  }

  @Test
  public void testComputeRoutableIps() {
    String c1 = "c1";
    String v1 = "v1";
    Prefix prefix = Prefix.parse("1.0.0.0/8");
    // The only important part of the Fib for this test is the route networks.
    MockFib fib =
        MockFib.builder()
            .setFibEntries(ImmutableMap.of(Ip.ZERO, ImmutableSet.of(mockFibEntry(prefix))))
            .build();
    Map<String, Map<String, Fib>> fibs = ImmutableMap.of(c1, ImmutableMap.of(v1, fib));
    Map<String, Map<String, IpSpace>> result =
        ForwardingAnalysisImpl.computeRoutableIps(fibs, sparseKeys(fibs));

    assertThat(result, equalTo(ImmutableMap.of(c1, ImmutableMap.of(v1, prefix.toIpSpace()))));
  }

  @Test
  public void testComputeRouteMatchConditions() {
    Set<AbstractRoute> routes =
        ImmutableSet.of(new ConnectedRoute(P1, INTERFACE1), new ConnectedRoute(P2, INTERFACE2));
    Map<Prefix, IpSpace> matchingIps = ImmutableMap.of(P1, IPSPACE1, P2, IPSPACE2);

    /* Resulting IP space should permit matching IPs */
    assertThat(
        ForwardingAnalysisImpl.computeRouteMatchConditions(routes, matchingIps),
        isAclIpSpaceThat(
            hasLines(
                containsInAnyOrder(
                    AclIpSpaceLine.permit(IPSPACE1), AclIpSpaceLine.permit(IPSPACE2)))));
  }

  @Test
  public void testComputeRoutesWhereDstIpCanBeArpIp() {
    String i1 = "i1";
    AbstractRoute r1 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, i1);
    Map<String, Set<AbstractRoute>> routesWithNextHop =
        ImmutableMap.of(i1, ImmutableSet.of(r1, ifaceRoute));
    Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces =
        ImmutableMap.of(
            r1,
            ImmutableMap.of(i1, ImmutableSet.of(Optional.of(r1.getNextHopIp()))),
            ifaceRoute,
            ImmutableMap.of(i1, ImmutableSet.of(Optional.empty())));
    Map<String, Set<AbstractRoute>> result =
        computeRoutesWhereDstIpCanBeArpIp(nextHopInterfaces, routesWithNextHop);

    /* Only the interface route should show up */
    assertThat(result, equalTo(ImmutableMap.of(i1, ImmutableSet.of(ifaceRoute))));
  }

  @Test
  public void testComputeRoutesWithDestIpEdge() {
    String c1 = "c1";
    String c2 = "c2";
    String i1 = "i1";
    String i2 = "i2";
    AbstractRoute r1 = new ConnectedRoute(P1, i1);
    Map<String, Set<AbstractRoute>> routesWhereDstIpCanBeArpIp =
        ImmutableMap.of(i1, ImmutableSet.of(r1));
    Edge e1 = Edge.of(c1, i1, c2, i2);
    Topology topology = new Topology(ImmutableSortedSet.of(e1));
    Map<Edge, Set<AbstractRoute>> result =
        computeRoutesWithDestIpEdge(c1, topology, routesWhereDstIpCanBeArpIp);

    assertThat(result, equalTo(ImmutableMap.of(e1, ImmutableSet.of(r1))));
  }

  @Test
  public void testComputeRoutesWithNextHop() {
    String c1 = "c1";
    String v1 = "v1";
    String v2 = "v2";
    String i1 = "i1";
    ConnectedRoute r1 = new ConnectedRoute(P1, i1);
    MockFib mockFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    P1.getFirstHostIp(),
                    ImmutableSet.of(new FibEntry(FibForward.of(null, i1), ImmutableList.of(r1)))))
            .build();
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1, mockFib,
                v2, mockFib));

    Configuration config = _cb.setHostname(c1).build();
    Vrf vrf1 = _vb.setName(v1).setOwner(config).build();
    _vb.setName(v2).setOwner(config).build();
    _ib.setName(i1).setVrf(vrf1).setOwner(config).build();
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> result =
        ForwardingAnalysisImpl.computeRoutesWithNextHop(fibs, sparseKeys(fibs));

    assertThat(
        result,
        equalTo(
            ImmutableMap.of(
                c1,
                ImmutableMap.of(
                    v1,
                    ImmutableMap.of(i1, ImmutableSet.of(r1)),
                    v2,
                    ImmutableMap.of(i1, ImmutableSet.of(r1))))));
  }

  @Test
  public void testComputeRoutesWithNextHopIpArpFalse() {
    String i1 = "i1";
    AbstractRoute r1 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    Set<AbstractRoute> routesWithNextHop = ImmutableSet.of(r1);
    Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces =
        ImmutableMap.of(r1, ImmutableMap.of(i1, ImmutableSet.of(Optional.of(r1.getNextHopIp()))));
    IpSpace someoneReplies = P2.getEndIp().toIpSpace();
    Set<AbstractRoute> result =
        computeArpFalseNhipRoutes(i1, nextHopInterfaces, routesWithNextHop, someoneReplies);

    assertThat(result, equalTo(ImmutableSet.of(r1)));
  }

  @Test
  public void testComputeRoutesWithNextHopIpArpFalseForInterface() {
    String outInterface = "i1";
    AbstractRoute nextHopIpRoute1 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute nextHopIpRoute2 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getEndIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, outInterface);
    Set<AbstractRoute> candidateRoutes =
        ImmutableSet.of(nextHopIpRoute1, nextHopIpRoute2, ifaceRoute);
    Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces =
        ImmutableMap.of(
            nextHopIpRoute1,
            ImmutableMap.of(
                outInterface, ImmutableSet.of(Optional.of(nextHopIpRoute1.getNextHopIp()))),
            nextHopIpRoute2,
            ImmutableMap.of(
                outInterface, ImmutableSet.of(Optional.of(nextHopIpRoute2.getNextHopIp()))),
            ifaceRoute,
            ImmutableMap.of(outInterface, ImmutableSet.of(Optional.empty())));
    Set<AbstractRoute> result =
        computeRoutesWithNextHopIpArpFalseForInterface(
            nextHopInterfaces, outInterface, candidateRoutes, P2.getStartIp().toIpSpace());

    /*
     * Should only contain nextHopIpRoute1 since it is the only route with a next-hop-ip for which
     * there is no ARP reply.
     */
    assertThat(result, contains(nextHopIpRoute2));
  }

  @Test
  public void testComputeRoutesWithNextHopIpArpFalseForInterfaceNoNeighbors() {
    String outInterface = "i1";
    AbstractRoute nextHopIpRoute1 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute nextHopIpRoute2 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getEndIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, outInterface);
    Set<AbstractRoute> candidateRoutes =
        ImmutableSet.of(nextHopIpRoute1, nextHopIpRoute2, ifaceRoute);
    Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces =
        ImmutableMap.of(
            nextHopIpRoute1,
            ImmutableMap.of(
                outInterface, ImmutableSet.of(Optional.of(nextHopIpRoute1.getNextHopIp()))),
            nextHopIpRoute2,
            ImmutableMap.of(
                outInterface, ImmutableSet.of(Optional.of(nextHopIpRoute2.getNextHopIp()))),
            ifaceRoute,
            ImmutableMap.of(outInterface, ImmutableSet.of(Optional.empty())));
    Set<AbstractRoute> result =
        computeRoutesWithNextHopIpArpFalseForInterface(
            nextHopInterfaces, outInterface, candidateRoutes, EmptyIpSpace.INSTANCE);

    /*
     * Should contain both nextHopIpRoute1 and nextHopIpRoute2, since:
     * 1) They are the only routes with a next hop IP.
     * 2) Their next hop IPs do not receive ARP reply since _someoneReplies is empty.
     */
    assertThat(result, equalTo(ImmutableSet.of(nextHopIpRoute1, nextHopIpRoute2)));
  }

  @Test
  public void testComputeRoutesWithNextHopIpArpTrue() {
    String c1 = "c1";
    String i1 = "i1";
    String c2 = "c2";
    String i2 = "i2";
    Edge e1 = Edge.of(c1, i1, c2, i2);
    Map<String, Map<String, IpSpace>> arpReplies =
        ImmutableMap.of(c2, ImmutableMap.of(i2, P2.getStartIp().toIpSpace()));
    Topology topology = new Topology(ImmutableSortedSet.of(e1));
    AbstractRoute r1 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute r2 =
        StaticRoute.testBuilder()
            .setNetwork(P1)
            .setNextHopIp(P2.getEndIp())
            .setAdministrativeCost(1)
            .build();
    Map<String, Set<AbstractRoute>> routesWithNextHop =
        ImmutableMap.of(i1, ImmutableSet.of(r1, r2));
    Map<AbstractRoute, Map<String, Set<Optional<Ip>>>> nextHopInterfaces =
        ImmutableMap.of(
            r1,
            ImmutableMap.of(i1, ImmutableSet.of(Optional.of(r1.getNextHopIp()))),
            r2,
            ImmutableMap.of(i1, ImmutableSet.of(Optional.of(r2.getNextHopIp()))));
    Map<Edge, Set<AbstractRoute>> result =
        computeRoutesWithNextHopIpArpTrue(
            c1, nextHopInterfaces, topology, arpReplies, routesWithNextHop);

    /* Only the route with the next hop ip that gets a reply should be present. */
    assertThat(result, equalTo(ImmutableMap.of(e1, ImmutableSet.of(r1))));
  }

  @Test
  public void testComputeSomeoneReplies() {
    String c1 = "c1";
    String i1 = "i1";
    String c2 = "c2";
    String i2 = "i2";
    Edge e1 = Edge.of(c1, i1, c2, i2);
    Map<String, Map<String, IpSpace>> arpReplies =
        ImmutableMap.of(c2, ImmutableMap.of(i2, P1.toIpSpace()));
    Topology topology = new Topology(ImmutableSortedSet.of(e1));
    IpSpace result = computeSomeoneReplies(c1, i1, topology, arpReplies);

    /* IPs allowed by neighbor should appear */
    assertThat(result, containsIp(P1.getStartIp()));
    assertThat(result, containsIp(P1.getEndIp()));
    /* IPs not allowed by neighbor should not appear */
    assertThat(result, not(containsIp(P2.getStartIp())));
  }

  @Test
  public void testComputeDeliveredToSubnetNoArpFalse() {
    Ip ip = Ip.parse("10.0.0.1");

    IpSpace arpFalseDestIp = EmptyIpSpace.INSTANCE;
    IpSpace interfaceHostSubnetIps = ip.toIpSpace();
    IpSpace ownedIps = EmptyIpSpace.INSTANCE;

    IpSpace result = computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);
    assertThat(result, not(containsIp(ip)));
  }

  @Test
  public void testComputeDeliveredToSubnetNoInterfaceHostIps() {
    Ip ip = Ip.parse("10.0.0.1");

    IpSpace arpFalseDestIp = ip.toIpSpace();
    IpSpace interfaceHostSubnetIps = EmptyIpSpace.INSTANCE;
    IpSpace ownedIps = EmptyIpSpace.INSTANCE;

    IpSpace result = computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);
    assertThat(result, not(containsIp(ip)));
  }

  @Test
  public void testComputeDeliveredToSubnetEqual() {
    Ip ip = Ip.parse("10.0.0.1");

    IpSpace arpFalseDestIp = ip.toIpSpace();
    IpSpace interfaceHostSubnetIps = ip.toIpSpace();
    IpSpace ownedIps = EmptyIpSpace.INSTANCE;

    IpSpace result = computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);
    assertThat(result, containsIp(ip));
  }

  enum NextHopIpStatus {
    NONE,
    INTERNAL,
    EXTERNAL
  }

  private static void testDispositionComputationTemplate(
      NextHopIpStatus nextHopIpStatus,
      boolean isSubnetFull,
      boolean isDstIpInternal,
      boolean isDstIpInSubnet,
      FlowDisposition expectedDisposition) {
    String nextHopIpString = "1.0.0.1";
    Prefix dstPrefix = P3;
    Ip nextHopIp = Ip.parse(nextHopIpString);

    AclIpSpace.Builder internalIpsBuilder = AclIpSpace.builder();

    boolean hasMissingDevices = !isSubnetFull;

    IpSpace arpFalseDestIp;
    IpSpace dstIpsWithOwnedNextHopIpArpFalse;
    IpSpace dstIpsWithUnownedNextHopIpArpFalse;
    if (nextHopIpStatus == NextHopIpStatus.EXTERNAL) {
      arpFalseDestIp = EmptyIpSpace.INSTANCE;
      dstIpsWithOwnedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
      dstIpsWithUnownedNextHopIpArpFalse = dstPrefix.toIpSpace();
    } else if (nextHopIpStatus == NextHopIpStatus.INTERNAL) {
      arpFalseDestIp = EmptyIpSpace.INSTANCE;
      internalIpsBuilder.thenPermitting(nextHopIp.toIpSpace());
      dstIpsWithOwnedNextHopIpArpFalse = dstPrefix.toIpSpace();
      dstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    } else {
      arpFalseDestIp = dstPrefix.toIpSpace();
      dstIpsWithOwnedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
      dstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    }

    if (isDstIpInternal) {
      internalIpsBuilder.thenPermitting(dstPrefix.toIpSpace());
    }

    IpSpace internalIps = internalIpsBuilder.build();
    IpSpace externalIps = internalIps.complement();

    IpSpace interfaceHostSubnetIps;
    if (isDstIpInSubnet) {
      interfaceHostSubnetIps = dstPrefix.toIpSpace();
    } else {
      interfaceHostSubnetIps = EmptyIpSpace.INSTANCE;
    }

    IpSpace arpFalse = dstPrefix.toIpSpace();

    IpSpace ownedIps = EmptyIpSpace.INSTANCE;

    IpSpace deliveredToSubnetIpSpace =
        computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);
    IpSpace exitsNetworkIpSpace =
        computeExitsNetwork(
            hasMissingDevices, dstIpsWithUnownedNextHopIpArpFalse, arpFalseDestIp, externalIps);

    IpSpace insufficientInfoIpSpace =
        computeInsufficientInfo(
            interfaceHostSubnetIps,
            hasMissingDevices,
            arpFalseDestIp,
            dstIpsWithUnownedNextHopIpArpFalse,
            dstIpsWithOwnedNextHopIpArpFalse,
            internalIps);
    IpSpace neighborUnreachableIpSpace =
        computeNeighborUnreachable(
            arpFalse, hasMissingDevices, arpFalseDestIp, interfaceHostSubnetIps, ownedIps);

    if (expectedDisposition == FlowDisposition.EXITS_NETWORK) {
      assertThat(exitsNetworkIpSpace, containsIp(dstPrefix.getStartIp()));
      assertThat(exitsNetworkIpSpace, containsIp(dstPrefix.getEndIp()));
    } else {
      assertThat(exitsNetworkIpSpace, not(containsIp(dstPrefix.getStartIp())));
      assertThat(exitsNetworkIpSpace, not(containsIp(dstPrefix.getEndIp())));
    }

    if (expectedDisposition == FlowDisposition.INSUFFICIENT_INFO) {
      assertThat(insufficientInfoIpSpace, containsIp(dstPrefix.getStartIp()));
      assertThat(insufficientInfoIpSpace, containsIp(dstPrefix.getEndIp()));
    } else {
      assertThat(insufficientInfoIpSpace, not(containsIp(dstPrefix.getStartIp())));
      assertThat(insufficientInfoIpSpace, not(containsIp(dstPrefix.getEndIp())));
    }

    if (expectedDisposition == FlowDisposition.DELIVERED_TO_SUBNET) {
      assertThat(deliveredToSubnetIpSpace, containsIp(dstPrefix.getStartIp()));
      assertThat(deliveredToSubnetIpSpace, containsIp(dstPrefix.getEndIp()));
    } else {
      assertThat(deliveredToSubnetIpSpace, not(containsIp(dstPrefix.getStartIp())));
      assertThat(deliveredToSubnetIpSpace, not(containsIp(dstPrefix.getEndIp())));
    }

    if (expectedDisposition == FlowDisposition.NEIGHBOR_UNREACHABLE) {
      assertThat(neighborUnreachableIpSpace, (containsIp(dstPrefix.getStartIp())));
      assertThat(neighborUnreachableIpSpace, (containsIp(dstPrefix.getEndIp())));
    } else {
      assertThat(neighborUnreachableIpSpace, not(containsIp(dstPrefix.getStartIp())));
      assertThat(neighborUnreachableIpSpace, not(containsIp(dstPrefix.getEndIp())));
    }
  }

  @Test
  public void testDispositionComputation() {
    /*
     * Avoid the case where arp dst ip, interface is full, and dst ip is in subnet (would be accepted).
     * Avoid cases where dst ip is internal but not in subet.
     */

    // Arp dst ip, interface is full, dst ip is internal -> neighbor unreachable
    testDispositionComputationTemplate(
        NextHopIpStatus.NONE, true, true, false, FlowDisposition.NEIGHBOR_UNREACHABLE);

    // Arp dst ip, interface is full, dst ip is external -> neighbor unreachable
    testDispositionComputationTemplate(
        NextHopIpStatus.NONE, true, false, false, FlowDisposition.NEIGHBOR_UNREACHABLE);

    // Arp dst ip, interface is not full, dst ip is subnet -> delivered to subnet
    testDispositionComputationTemplate(
        NextHopIpStatus.NONE, false, true, true, FlowDisposition.DELIVERED_TO_SUBNET);

    // Arp dst ip, interface is not full, dst ip is internal -> insufficient info
    testDispositionComputationTemplate(
        NextHopIpStatus.NONE, false, true, false, FlowDisposition.INSUFFICIENT_INFO);

    // Arp dst ip, interface is not full, dst ip is external -> exits network
    testDispositionComputationTemplate(
        NextHopIpStatus.NONE, false, false, false, FlowDisposition.EXITS_NETWORK);

    // nhip external, interface is full, dst ip is internal -> neighbor unreachable
    testDispositionComputationTemplate(
        NextHopIpStatus.EXTERNAL, true, true, true, FlowDisposition.NEIGHBOR_UNREACHABLE);

    testDispositionComputationTemplate(
        NextHopIpStatus.EXTERNAL, true, true, false, FlowDisposition.NEIGHBOR_UNREACHABLE);

    // nhip external, interface is full, dst ip is external -> neighbor unreachable
    testDispositionComputationTemplate(
        NextHopIpStatus.EXTERNAL, true, false, false, FlowDisposition.NEIGHBOR_UNREACHABLE);

    // nhip external, interface is not full, dst ip is internal -> insufficient info
    testDispositionComputationTemplate(
        NextHopIpStatus.EXTERNAL, false, true, true, FlowDisposition.INSUFFICIENT_INFO);

    testDispositionComputationTemplate(
        NextHopIpStatus.EXTERNAL, false, true, false, FlowDisposition.INSUFFICIENT_INFO);

    // nhip external, interface is not full, dst ip is external -> exits network
    testDispositionComputationTemplate(
        NextHopIpStatus.EXTERNAL, false, false, false, FlowDisposition.EXITS_NETWORK);

    // nhip internal, interface is full, dst ip is internal -> neighbor unreachable
    testDispositionComputationTemplate(
        NextHopIpStatus.INTERNAL, true, true, true, FlowDisposition.NEIGHBOR_UNREACHABLE);

    testDispositionComputationTemplate(
        NextHopIpStatus.INTERNAL, true, true, false, FlowDisposition.NEIGHBOR_UNREACHABLE);

    // nhip internal, interface is full, dst ip is external -> neighbor unreachable
    testDispositionComputationTemplate(
        NextHopIpStatus.INTERNAL, true, false, false, FlowDisposition.NEIGHBOR_UNREACHABLE);

    // nhip internal, interface is not full, dst ip is internal -> insufficient info
    testDispositionComputationTemplate(
        NextHopIpStatus.INTERNAL, false, true, true, FlowDisposition.INSUFFICIENT_INFO);

    testDispositionComputationTemplate(
        NextHopIpStatus.INTERNAL, false, true, false, FlowDisposition.INSUFFICIENT_INFO);

    // nhip internal, interface is not full, dst ip is external -> insufficient info
    testDispositionComputationTemplate(
        NextHopIpStatus.INTERNAL, false, false, false, FlowDisposition.INSUFFICIENT_INFO);
  }

  /**
   * Test that if a route has an unowned next hop IP, but it resolves to an owned ARP IP and we
   * don't get an ARP response, we get insufficient info.
   */
  @Test
  public void testUnknownedNextHopIpOwnedArpIp() {
    Configuration n1 = _cb.setHostname("n1").build();
    Vrf v1 = _vb.setOwner(n1).build();
    Interface i1 =
        _ib.setOwner(n1)
            .setVrf(v1)
            .setAddress(ConcreteInterfaceAddress.parse("10.0.1.0/31"))
            .build();

    Ip arpIp = Ip.parse("3.3.3.3"); // unowned

    StaticRoute route =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setAdmin(1)
            .build();

    MockFib fib1 =
        MockFib.builder()
            .setMatchingIps(ImmutableMap.of(route.getNetwork(), route.getNetwork().toIpSpace()))
            .setFibEntries(
                ImmutableMap.of(
                    arpIp,
                    ImmutableSet.of(
                        new FibEntry(FibForward.of(arpIp, i1.getName()), ImmutableList.of(route)))))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(n1.getHostname(), ImmutableMap.of(v1.getName(), fib1));

    IpOwners ipOwners = new TestIpOwners(configs);

    ForwardingAnalysis fa =
        new ForwardingAnalysisImpl(
            configs, fibs, Topology.EMPTY, computeLocationInfo(ipOwners, configs), ipOwners);

    InterfaceForwardingBehavior ifb =
        fa.getVrfForwardingBehavior()
            .get(n1.getHostname())
            .get(v1.getName())
            .getInterfaceForwardingBehavior()
            .get(i1.getName());
    assertThat(ifb.getInsufficientInfo(), not(containsIp(route.getNetwork().getStartIp())));

    assertThat(ifb.getExitsNetwork(), containsIp(route.getNetwork().getStartIp()));
  }

  // If two nodes are in the same subnet but not connected per the given topology,
  // sending packets from one to the other should result in Neighbor Unreachable.
  @Test
  public void testDispositionWithTopology() {
    Prefix prefix = Prefix.parse("1.0.0.0/24");
    IpSpace ipSpace = prefix.toIpSpace();
    Ip ip2 = Ip.parse("1.0.0.2");

    Configuration c1 = _cb.setHostname("c1").build();
    Configuration c2 = _cb.setHostname("c2").build();
    Vrf v1 = _vb.setName("v1").setOwner(c1).build();
    Vrf v2 = _vb.setName("v2").setOwner(c2).build();
    Interface i1 =
        _ib.setAddresses(ConcreteInterfaceAddress.parse("1.0.0.1/24"))
            .setName("i1")
            .setOwner(c1)
            .setVrf(v1)
            .build();
    Interface i2 =
        _ib.setAddresses(ConcreteInterfaceAddress.parse("1.0.0.2/24"))
            .setName("i2")
            .setOwner(c2)
            .setVrf(v2)
            .build();

    StaticRoute route1 =
        StaticRoute.testBuilder()
            .setNetwork(prefix)
            .setNextHopInterface(i1.getName())
            .setAdministrativeCost(1)
            .build();

    StaticRoute route2 =
        StaticRoute.testBuilder()
            .setNextHopInterface(i2.getName())
            .setNetwork(prefix)
            .setAdministrativeCost(1)
            .build();

    v1.setStaticRoutes(ImmutableSortedSet.of(route1));
    v2.setStaticRoutes(ImmutableSortedSet.of(route2));

    MockFib fib1 =
        MockFib.builder()
            .setMatchingIps(ImmutableMap.of(prefix, ipSpace))
            .setFibEntries(
                ImmutableMap.of(
                    prefix.getFirstHostIp(),
                    ImmutableSet.of(
                        new FibEntry(FibForward.of(null, i1.getName()), ImmutableList.of(route1)))))
            .build();

    MockFib fib2 =
        MockFib.builder()
            .setMatchingIps(ImmutableMap.of(prefix, ipSpace))
            .setFibEntries(
                ImmutableMap.of(
                    prefix.getFirstHostIp(),
                    ImmutableSet.of(
                        new FibEntry(FibForward.of(null, i2.getName()), ImmutableList.of(route2)))))
            .build();

    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1.getHostname(), ImmutableMap.of(v1.getName(), fib1),
            c2.getHostname(), ImmutableMap.of(v2.getName(), fib2));

    Map<String, Configuration> configs =
        ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2);

    IpOwners ipOwners = new TestIpOwners(configs);
    ForwardingAnalysis analysis =
        new ForwardingAnalysisImpl(
            configs, fibs, Topology.EMPTY, computeLocationInfo(ipOwners, configs), ipOwners);

    InterfaceForwardingBehavior i1ForwardingBehavior =
        analysis
            .getVrfForwardingBehavior()
            .get(c1.getHostname())
            .get(v1.getName())
            .getInterfaceForwardingBehavior()
            .get(i1.getName());
    assertFalse(i1ForwardingBehavior.getDeliveredToSubnet().containsIp(ip2, c1.getIpSpaces()));

    assertTrue(i1ForwardingBehavior.getNeighborUnreachable().containsIp(ip2, c1.getIpSpaces()));

    assertFalse(i1ForwardingBehavior.getInsufficientInfo().containsIp(ip2, c1.getIpSpaces()));
  }

  private static class MockIpSpace extends IpSpace {

    private final int _num;

    public MockIpSpace(int num) {
      _num = num;
    }

    @Override
    public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    protected int compareSameClass(IpSpace o) {
      return Integer.compare(_num, ((MockIpSpace) o)._num);
    }

    @Override
    protected boolean exprEquals(Object o) {
      return _num == ((MockIpSpace) o)._num;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_num);
    }

    @Override
    public String toString() {
      return String.format("TestIpSpace%d", _num);
    }
  }

  private static class TestIpOwners extends IpOwnersBaseImpl {
    protected TestIpOwners(Map<String, Configuration> configurations) {
      super(
          configurations,
          GlobalBroadcastNoPointToPoint.instance(),
          PreDataPlaneTrackMethodEvaluator::new,
          false);
    }
  }
}
