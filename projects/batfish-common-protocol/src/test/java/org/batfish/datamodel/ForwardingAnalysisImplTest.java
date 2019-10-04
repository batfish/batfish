package org.batfish.datamodel;

import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpFalseDestIp;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeArpFalseNextHopIp;
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
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeMatchingIps;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeNeighborUnreachable;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeNextVrfIpsByNodeVrf;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeNullRoutedIps;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWhereDstIpCanBeArpIp;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWithDestIpEdge;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpFalse;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpFalseForInterface;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeRoutesWithNextHopIpArpTrue;
import static org.batfish.datamodel.ForwardingAnalysisImpl.computeSomeoneReplies;
import static org.batfish.datamodel.ForwardingAnalysisImpl.union;
import static org.batfish.datamodel.IpWildcard.ipWithWildcardMask;
import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.hasLines;
import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.isAclIpSpaceThat;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link ForwardingAnalysisImpl}. */
public class ForwardingAnalysisImplTest {

  private static final String CONFIG1 = "config1";

  private static final String VRF1 = "vrf1";

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
            .setAddress(ConcreteInterfaceAddress.create(P1.getStartIp(), P1.getPrefixLength()))
            .setProxyArp(true)
            .build();
    Interface i2 =
        _ib.setOwner(c2)
            .setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.create(P2.getStartIp(), P2.getPrefixLength()))
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
        IpOwners.computeInterfaceOwnedIps(configs, false);
    Map<String, Map<String, IpSpace>> result =
        computeArpReplies(configurations, ipsRoutedOutInterfaces, interfaceOwnedIps, routableIps);

    /* Proxy-arp: Match interface IP, reject what's routed through i1, accept everything else*/
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(i1.getName()), containsIp(P1.getStartIp()))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(i1.getName()), not(containsIp(P1.getEndIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(i1.getName()), not(containsIp(P3.getStartIp())))));
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
            hasEntry(equalTo(i2.getName()), containsIp(P2.getStartIp()))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P2.getEndIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P3.getStartIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c2.getHostname()),
            hasEntry(equalTo(i2.getName()), not(containsIp(P1.getStartIp())))));
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
            .setAddress(ConcreteInterfaceAddress.create(P1.getStartIp(), P1.getPrefixLength()))
            .setProxyArp(true)
            .build();
    Interface i2 =
        _ib.setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.create(P2.getStartIp(), P2.getPrefixLength()))
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
        ImmutableMap.of(i1.getName(), i1, i2.getName(), i2, i3.getName(), i3, i4.getName(), i4);
    Map<String, IpSpace> routableIpsByVrf =
        ImmutableMap.of(
            vrf1.getName(), UniverseIpSpace.INSTANCE, vrf2.getName(), UniverseIpSpace.INSTANCE);
    Map<String, Map<String, IpSpace>> ipsRoutedOutInterfaces =
        ImmutableMap.of(
            vrf1.getName(),
            ImmutableMap.of(i1.getName(), ipsRoutedOutI1),
            vrf2.getName(),
            ImmutableMap.of(i2.getName(), ipsRoutedOutI2, i3.getName(), ipsRoutedOutI3));

    i1.setAdditionalArpIps(new IpIpSpace(Ip.parse("10.10.10.1")));
    i2.setAdditionalArpIps(new IpIpSpace(Ip.parse("10.10.10.2")));
    i3.setAdditionalArpIps(new IpIpSpace(Ip.parse("10.10.10.3")));
    i4.setAdditionalArpIps(new IpIpSpace(Ip.parse("10.10.10.4")));

    Map<String, Configuration> configs = ImmutableMap.of(config.getHostname(), config);
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        IpOwners.computeInterfaceOwnedIps(configs, false);
    Map<String, IpSpace> result =
        ForwardingAnalysisImpl.computeArpRepliesByInterface(
            interfaces, routableIpsByVrf, ipsRoutedOutInterfaces, interfaceOwnedIps);

    /* Proxy-arp: Match interface IP, reject what's routed through i1, accept everything else*/
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(P1.getStartIp())));
    assertThat(result, hasEntry(equalTo(i1.getName()), not(containsIp(P1.getEndIp()))));
    assertThat(result, hasEntry(equalTo(i1.getName()), not(containsIp(P3.getStartIp()))));
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(P2.getStartIp())));
    assertThat(result, hasEntry(equalTo(i1.getName()), containsIp(Ip.parse("10.10.10.1"))));
    /* No proxy-arp: just match interface ip and additional arp ip */
    assertThat(result, hasEntry(equalTo(i2.getName()), containsIp(P2.getStartIp())));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P2.getEndIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P3.getStartIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), not(containsIp(P1.getStartIp()))));
    assertThat(result, hasEntry(equalTo(i2.getName()), containsIp(Ip.parse("10.10.10.2"))));
    /* No interface IPs: reject everything */
    assertThat(result, hasEntry(equalTo(i3.getName()), equalTo(EmptyIpSpace.INSTANCE)));
    /* Link-local address is present, honor additional ARP IPs  */
    assertThat(result, hasEntry(equalTo(i4.getName()), containsIp(Ip.parse("10.10.10.4"))));
  }

  @Test
  public void testComputeArpReplies_VRRP() {
    Configuration c = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    _ib.setOwner(c);
    Vrf vrf1 = _vb.build();
    Vrf vrf2 = _vb.build();
    Interface i1 =
        _ib.setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.create(P1.getStartIp(), P1.getPrefixLength()))
            .setVrrpGroups(
                ImmutableSortedMap.of(
                    1,
                    VrrpGroup.builder()
                        .setName(1)
                        .setPriority(100)
                        .setVirtualAddress(ConcreteInterfaceAddress.parse("1.1.1.1/32"))
                        .build()))
            .build();

    Interface i2 =
        _ib.setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.create(P1.getEndIp(), P1.getPrefixLength()))
            .setVrrpGroups(
                ImmutableSortedMap.of(
                    1,
                    VrrpGroup.builder()
                        .setName(1)
                        .setPriority(110)
                        .setVirtualAddress(ConcreteInterfaceAddress.parse("1.1.1.1/32"))
                        .build()))
            .build();

    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        IpOwners.computeInterfaceOwnedIps(configs, false);

    IpSpace p1IpSpace = IpWildcard.create(P1).toIpSpace();
    IpSpace i1ArpReplies =
        computeInterfaceArpReplies(i1, UniverseIpSpace.INSTANCE, p1IpSpace, interfaceOwnedIps);
    IpSpace i2ArpReplies =
        computeInterfaceArpReplies(i2, UniverseIpSpace.INSTANCE, p1IpSpace, interfaceOwnedIps);

    assertThat(i1ArpReplies, not(containsIp(Ip.parse("1.1.1.1"))));
    assertThat(i2ArpReplies, containsIp(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testComputeArpTrueEdge() {
    IpSpace nextHopIpSpace = new MockIpSpace(1);
    IpSpace dstIpSpace = new MockIpSpace(2);
    Edge e1 = Edge.of("c1", "i1", "c2", "i2");
    Map<String, Map<String, Map<Edge, IpSpace>>> arpTrueEdgeDestIp =
        ImmutableMap.of("c1", ImmutableMap.of("v1", ImmutableMap.of(e1, dstIpSpace)));
    Map<String, Map<String, Map<Edge, IpSpace>>> arpTrueEdgeNextHopIp =
        ImmutableMap.of("c1", ImmutableMap.of("v1", ImmutableMap.of(e1, nextHopIpSpace)));
    Map<String, Map<String, Map<Edge, IpSpace>>> result =
        computeArpTrueEdge(arpTrueEdgeDestIp, arpTrueEdgeNextHopIp);

    assertThat(
        result,
        hasEntry(
            equalTo("c1"),
            hasEntry(
                equalTo("v1"),
                hasEntry(
                    equalTo(e1),
                    isAclIpSpaceThat(
                        hasLines(
                            containsInAnyOrder(
                                AclIpSpaceLine.permit(nextHopIpSpace),
                                AclIpSpaceLine.permit(dstIpSpace))))))));
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
                            AclIpSpace.rejecting(
                                    Prefix.create(P1.getEndIp(), Prefix.MAX_PREFIX_LENGTH)
                                        .toIpSpace())
                                .thenPermitting(P1.toIpSpace())
                                .build()))
                    .build()));
    Edge edge = Edge.of(c1.getHostname(), i1.getName(), c2.getHostname(), i2.getName());
    Map<String, Map<String, Map<Edge, Set<AbstractRoute>>>> routesWithDestIpEdge =
        ImmutableMap.of(
            c1.getHostname(),
            ImmutableMap.of(
                vrf1.getName(),
                ImmutableMap.of(edge, ImmutableSet.of(new ConnectedRoute(P1, i1.getName())))));
    Map<String, Map<String, IpSpace>> arpReplies =
        ImmutableMap.of(
            c2.getHostname(),
            ImmutableMap.of(
                i2.getName(),
                AclIpSpace.permitting(i2Ip.toIpSpace())
                    .thenPermitting(P1.getEndIp().toIpSpace())
                    .build()));
    Map<String, Map<String, Map<Edge, IpSpace>>> result =
        computeArpTrueEdgeDestIp(computeMatchingIps(fibs), routesWithDestIpEdge, arpReplies);

    /* Respond to request for IP on i2. */
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(vrf1.getName()), hasEntry(equalTo(edge), containsIp(i2Ip)))));
    /* Do not make ARP request for IP matched by more specific route not going out i1.  */
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(vrf1.getName()), hasEntry(equalTo(edge), not(containsIp(P1.getEndIp()))))));
    /* Do not receive response for IP i2 does not own. */
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(vrf1.getName()),
                hasEntry(equalTo(edge), not(containsIp(P1.getStartIp()))))));
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
            .setAddress(ConcreteInterfaceAddress.create(P1.getStartIp(), P1.getPrefixLength()))
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
                            AclIpSpace.rejecting(
                                    Prefix.create(P1.getEndIp(), Prefix.MAX_PREFIX_LENGTH)
                                        .toIpSpace())
                                .thenPermitting(P1.toIpSpace())
                                .build()))
                    .build()));
    Map<String, Map<String, Map<Edge, Set<AbstractRoute>>>> routesWithNextHopIpArpTrue =
        ImmutableMap.of(
            c1.getHostname(),
            ImmutableMap.of(
                vrf1.getName(),
                ImmutableMap.of(
                    edge,
                    ImmutableSet.of(
                        StaticRoute.builder()
                            .setNetwork(P1)
                            .setNextHopIp(P2.getStartIp())
                            .setAdministrativeCost(1)
                            .build()))));
    Map<String, Map<String, Map<Edge, IpSpace>>> result =
        computeArpTrueEdgeNextHopIp(computeMatchingIps(fibs), routesWithNextHopIpArpTrue);

    /*
     * Respond for any destination IP in network not matching more specific route not going out i1.
     */
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(vrf1.getName()), hasEntry(equalTo(edge), containsIp(P1.getStartIp())))));

    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(equalTo(vrf1.getName()), hasEntry(equalTo(edge), containsIp(i2Ip)))));

    /* Do not respond for destination IP matching more specific route not going out i1 */
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(vrf1.getName()), hasEntry(equalTo(edge), not(containsIp(P1.getEndIp()))))));

    /* Do not respond for destination IPs not matching route */
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(vrf1.getName()),
                hasEntry(equalTo(edge), not(containsIp(P2.getStartIp()))))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(vrf1.getName()), hasEntry(equalTo(edge), not(containsIp(P2.getEndIp()))))));
  }

  @Test
  public void testComputeInterfaceArpReplies() {
    Configuration config = _cb.build();
    _ib.setOwner(config);
    ConcreteInterfaceAddress primary =
        ConcreteInterfaceAddress.create(P1.getStartIp(), P1.getPrefixLength());
    ConcreteInterfaceAddress secondary =
        ConcreteInterfaceAddress.create(P2.getStartIp(), P2.getPrefixLength());
    Interface iNoProxyArp = _ib.setAddresses(primary, secondary).build();
    Interface iProxyArp = _ib.setProxyArp(true).build();
    IpSpace routableIpsForThisVrf = UniverseIpSpace.INSTANCE;
    IpSpace ipsRoutedThroughInterface =
        IpWildcardSetIpSpace.builder()
            .including(IpWildcard.create(P1), IpWildcard.create(P2))
            .build();
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        IpOwners.computeInterfaceOwnedIps(ImmutableMap.of(config.getHostname(), config), false);
    IpSpace noProxyArpResult =
        computeInterfaceArpReplies(
            iNoProxyArp, routableIpsForThisVrf, ipsRoutedThroughInterface, interfaceOwnedIps);
    IpSpace proxyArpResult =
        computeInterfaceArpReplies(
            iProxyArp, routableIpsForThisVrf, ipsRoutedThroughInterface, interfaceOwnedIps);

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
  public void testComputeIpsAssignedToThisInterfaceForArpReplies() {
    Configuration config = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(config.getHostname(), config);
    _ib.setOwner(config);
    ConcreteInterfaceAddress primary =
        ConcreteInterfaceAddress.create(P1.getStartIp(), P1.getPrefixLength());
    InterfaceAddress secondary =
        ConcreteInterfaceAddress.create(P2.getStartIp(), P2.getPrefixLength());
    Interface i = _ib.setAddresses(primary, secondary).build();
    Map<String, Map<String, Set<Ip>>> interfaceOwnedIps =
        IpOwners.computeInterfaceOwnedIps(configs, false);
    IpSpace result = computeIpsAssignedToThisInterfaceForArpReplies(i, interfaceOwnedIps);

    assertThat(result, containsIp(P1.getStartIp()));
    assertThat(result, containsIp(P2.getStartIp()));
    assertThat(result, not(containsIp(P2.getEndIp())));

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
        StaticRoute.builder()
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
    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeIpsRoutedOutInterfaces(computeMatchingIps(fibs), routesWithNextHop);

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
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWhereDstIpCanBeArpIp =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(ifaceRoute))));
    Map<String, Map<String, IpSpace>> someoneReplies =
        ImmutableMap.of(c1, ImmutableMap.of(i1, P1.getEndIp().toIpSpace()));
    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeArpFalseDestIp(computeMatchingIps(fibs), routesWhereDstIpCanBeArpIp, someoneReplies);

    /* Should contain IP in the route's prefix that sees no reply */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getStartIp())))));
    /* Should not contain IP in the route's prefix that sees reply */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), not(containsIp(P1.getEndIp()))))));

    /* Should not contain other IPs */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), not(containsIp(P2.getEndIp()))))));
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
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWhereDstIpCanBeArpIp =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(ifaceRoute))));
    Map<String, Map<String, IpSpace>> someoneReplies = ImmutableMap.of();
    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeArpFalseDestIp(computeMatchingIps(fibs), routesWhereDstIpCanBeArpIp, someoneReplies);

    /*
     * Since _someoneReplies is empty, all IPs for which longest-prefix-match route has no
     * next-hop-ip should be in the result space.
     */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getStartIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1), hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getEndIp())))));

    /* Should not contain other IPs */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), not(containsIp(P2.getEndIp()))))));
  }

  @Test
  public void testComputeNeighborUnreachableArpNextHopIp() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    AbstractRoute r1 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHopIpArpFalse =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(r1))));
    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1,
            ImmutableMap.of(
                v1, MockFib.builder().setMatchingIps(ImmutableMap.of(P1, P1.toIpSpace())).build()));

    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeArpFalseNextHopIp(computeMatchingIps(fibs), routesWithNextHopIpArpFalse);

    /* IPs matching some route on interface with no response should appear */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getStartIp())))));
    assertThat(
        result,
        hasEntry(
            equalTo(c1), hasEntry(equalTo(v1), hasEntry(equalTo(i1), containsIp(P1.getEndIp())))));
    /* Other IPs should not appear */
    assertThat(
        result,
        hasEntry(
            equalTo(c1),
            hasEntry(equalTo(v1), hasEntry(equalTo(i1), not(containsIp(P2.getStartIp()))))));
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
                                    new FibNextVrf(v2),
                                    ImmutableList.of(
                                        StaticRoute.builder()
                                            .setAdmin(1)
                                            .setNetwork(P1)
                                            .setNextVrf(v2)
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
                                    new FibNextVrf(v1),
                                    ImmutableList.of(
                                        StaticRoute.builder()
                                            .setAdmin(1)
                                            .setNetwork(P2)
                                            .setNextVrf(v1)
                                            .build())))))
                    .build()));

    // Each VRF should delegate the matching IpSpace for its nextVrf route to the other VRF.
    assertThat(
        computeNextVrfIpsByNodeVrf(computeMatchingIps(fibs), fibs),
        equalTo(
            ImmutableMap.of(
                c1,
                ImmutableMap.of(
                    v1,
                    ImmutableMap.of(v2, P1_1.toIpSpace()),
                    v2,
                    ImmutableMap.of(v1, P2_2.toIpSpace())))));
  }

  @Test
  public void testComputeNullRoutedIps() {
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    AbstractRoute nullRoute =
        StaticRoute.builder()
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
                                    new FibForward(
                                        Route.UNSET_ROUTE_NEXT_HOP_IP,
                                        otherRoute.getNextHopInterface()),
                                    ImmutableList.of(otherRoute)))))
                    .build()));
    Map<String, Map<String, IpSpace>> result = computeNullRoutedIps(computeMatchingIps(fibs), fibs);

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
  public void testComputeRoutableIps() {
    String c1 = "c1";
    String v1 = "v1";
    Prefix prefix = Prefix.parse("1.0.0.0/8");
    // The only important part of the Fib for this test is the route networks.
    MockFib fib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    Ip.ZERO,
                    ImmutableSet.of(
                        new FibEntry(
                            new FibForward(Ip.ZERO, "iface"),
                            ImmutableList.of(
                                StaticRoute.builder()
                                    .setAdministrativeCost(1)
                                    .setNetwork(prefix)
                                    .build())))))
            .build();
    Map<String, Map<String, Fib>> fibs = ImmutableMap.of(c1, ImmutableMap.of(v1, fib));
    Map<String, Map<String, IpSpace>> result = ForwardingAnalysisImpl.computeRoutableIps(fibs);

    assertThat(
        result,
        equalTo(
            ImmutableMap.of(
                c1,
                ImmutableMap.of(
                    v1,
                    IpWildcardSetIpSpace.builder().including(IpWildcard.create(prefix)).build()))));
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
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    AbstractRoute r1 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, i1);
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop =
        ImmutableMap.of(
            c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(r1, ifaceRoute))));
    Map<String, Map<String, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>>
        nextHopInterfacesByNodeVrf =
            ImmutableMap.of(
                c1,
                ImmutableMap.of(
                    v1,
                    ImmutableMap.of(
                        r1,
                        ImmutableMap.of(
                            i1, ImmutableMap.of(r1.getNextHopIp(), ImmutableSet.of(ifaceRoute))),
                        ifaceRoute,
                        ImmutableMap.of(
                            i1,
                            ImmutableMap.of(
                                Route.UNSET_ROUTE_NEXT_HOP_IP, ImmutableSet.of(ifaceRoute))))));
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> result =
        computeRoutesWhereDstIpCanBeArpIp(nextHopInterfacesByNodeVrf, routesWithNextHop);

    /* Only the interface route should show up */
    assertThat(
        result,
        equalTo(
            ImmutableMap.of(
                c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(ifaceRoute))))));
  }

  @Test
  public void testComputeRoutesWithDestIpEdge() {
    String c1 = "c1";
    String c2 = "c2";
    String v1 = "v1";
    String i1 = "i1";
    String i2 = "i2";
    AbstractRoute r1 = new ConnectedRoute(P1, i1);
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWhereDstIpCanBeArpIp =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(r1))));
    Edge e1 = Edge.of(c1, i1, c2, i2);
    Topology topology = new Topology(ImmutableSortedSet.of(e1));
    Map<String, Map<String, Map<Edge, Set<AbstractRoute>>>> result =
        computeRoutesWithDestIpEdge(topology, routesWhereDstIpCanBeArpIp);

    assertThat(
        result,
        hasEntry(
            equalTo(c1), hasEntry(equalTo(v1), equalTo(ImmutableMap.of(e1, ImmutableSet.of(r1))))));
  }

  @Test
  public void testComputeRoutesWithNextHop() {
    String c1 = "c1";
    String v1 = "v1";
    String v2 = "v2";
    String i1 = "i1";
    ConnectedRoute r1 = new ConnectedRoute(P1, i1);
    final MockFib mockFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    Ip.AUTO,
                    ImmutableSet.of(
                        new FibEntry(new FibForward(Ip.AUTO, i1), ImmutableList.of(r1)))))
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
        ForwardingAnalysisImpl.computeRoutesWithNextHop(fibs);

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
    String c1 = "c1";
    String v1 = "v1";
    String i1 = "i1";
    AbstractRoute r1 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(r1))));
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, i1);
    Map<String, Map<String, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>>
        nextHopInterfacesByNodeVrf =
            ImmutableMap.of(
                c1,
                ImmutableMap.of(
                    v1,
                    ImmutableMap.of(
                        r1,
                        ImmutableMap.of(
                            i1, ImmutableMap.of(r1.getNextHopIp(), ImmutableSet.of(ifaceRoute))))));
    Map<String, Map<String, IpSpace>> someoneReplies =
        ImmutableMap.of(c1, ImmutableMap.of(i1, P2.getEndIp().toIpSpace()));
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> result =
        computeRoutesWithNextHopIpArpFalse(
            nextHopInterfacesByNodeVrf, routesWithNextHop, someoneReplies);

    assertThat(
        result,
        equalTo(
            ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(r1))))));
  }

  @Test
  public void testComputeRoutesWithNextHopIpArpFalseForInterface() {
    String outInterface = "i1";
    AbstractRoute nextHopIpRoute1 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute nextHopIpRoute2 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getEndIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, outInterface);
    Set<AbstractRoute> candidateRoutes =
        ImmutableSet.of(nextHopIpRoute1, nextHopIpRoute2, ifaceRoute);
    Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces =
        ImmutableMap.of(
            nextHopIpRoute1,
            ImmutableMap.of(
                outInterface,
                ImmutableMap.of(nextHopIpRoute1.getNextHopIp(), ImmutableSet.of(ifaceRoute))),
            nextHopIpRoute2,
            ImmutableMap.of(
                outInterface,
                ImmutableMap.of(nextHopIpRoute2.getNextHopIp(), ImmutableSet.of(ifaceRoute))),
            ifaceRoute,
            ImmutableMap.of(
                outInterface,
                ImmutableMap.of(Route.UNSET_ROUTE_NEXT_HOP_IP, ImmutableSet.of(ifaceRoute))));
    Set<AbstractRoute> result =
        computeRoutesWithNextHopIpArpFalseForInterface(
            nextHopInterfaces,
            outInterface,
            candidateRoutes,
            ImmutableMap.of(outInterface, P2.getStartIp().toIpSpace()));

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
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute nextHopIpRoute2 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getEndIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, outInterface);
    Set<AbstractRoute> candidateRoutes =
        ImmutableSet.of(nextHopIpRoute1, nextHopIpRoute2, ifaceRoute);
    Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces =
        ImmutableMap.of(
            nextHopIpRoute1,
            ImmutableMap.of(
                outInterface,
                ImmutableMap.of(nextHopIpRoute1.getNextHopIp(), ImmutableSet.of(ifaceRoute))),
            nextHopIpRoute2,
            ImmutableMap.of(
                outInterface,
                ImmutableMap.of(nextHopIpRoute2.getNextHopIp(), ImmutableSet.of(ifaceRoute))),
            ifaceRoute,
            ImmutableMap.of(
                outInterface,
                ImmutableMap.of(Route.UNSET_ROUTE_NEXT_HOP_IP, ImmutableSet.of(ifaceRoute))));
    Set<AbstractRoute> result =
        computeRoutesWithNextHopIpArpFalseForInterface(
            nextHopInterfaces, outInterface, candidateRoutes, ImmutableMap.of());

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
    String v1 = "v1";
    AbstractRoute r1 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getStartIp())
            .setAdministrativeCost(1)
            .build();
    AbstractRoute r2 =
        StaticRoute.builder()
            .setNetwork(P1)
            .setNextHopIp(P2.getEndIp())
            .setAdministrativeCost(1)
            .build();
    Map<String, Map<String, Map<String, Set<AbstractRoute>>>> routesWithNextHop =
        ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(i1, ImmutableSet.of(r1, r2))));
    AbstractRoute ifaceRoute = new ConnectedRoute(P2, i1);
    Map<String, Map<String, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>>
        nextHopInterfacesByNodeVrf =
            ImmutableMap.of(
                c1,
                ImmutableMap.of(
                    v1,
                    ImmutableMap.of(
                        r1,
                        ImmutableMap.of(
                            i1, ImmutableMap.of(r1.getNextHopIp(), ImmutableSet.of(ifaceRoute))),
                        r2,
                        ImmutableMap.of(
                            i1, ImmutableMap.of(r2.getNextHopIp(), ImmutableSet.of(ifaceRoute))))));
    Map<String, Map<String, Map<Edge, Set<AbstractRoute>>>> result =
        computeRoutesWithNextHopIpArpTrue(
            nextHopInterfacesByNodeVrf, topology, arpReplies, routesWithNextHop);

    /* Only the route with the next hop ip that gets a reply should be present. */
    assertThat(
        result,
        equalTo(
            ImmutableMap.of(c1, ImmutableMap.of(v1, ImmutableMap.of(e1, ImmutableSet.of(r1))))));
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
    Map<String, Map<String, IpSpace>> result = computeSomeoneReplies(topology, arpReplies);

    /* IPs allowed by neighbor should appear */
    assertThat(result, hasEntry(equalTo(c1), hasEntry(equalTo(i1), containsIp(P1.getStartIp()))));
    assertThat(result, hasEntry(equalTo(c1), hasEntry(equalTo(i1), containsIp(P1.getEndIp()))));
    /* IPs not allowed by neighbor should not appear */
    assertThat(
        result, hasEntry(equalTo(c1), hasEntry(equalTo(i1), not(containsIp(P2.getStartIp())))));
  }

  @Test
  public void testComputeDeliveredToSubnetNoArpFalse() {
    String c1 = "c1";
    String vrf1 = "vrf1";
    String i1 = "i1";
    Ip ip = Ip.parse("10.0.0.1");

    Map<String, Map<String, Map<String, IpSpace>>> arpFalseDestIp =
        ImmutableMap.of(c1, ImmutableMap.of(vrf1, ImmutableMap.of(i1, EmptyIpSpace.INSTANCE)));
    Map<String, Map<String, IpSpace>> interfaceHostSubnetIps =
        ImmutableMap.of(c1, ImmutableMap.of(i1, ip.toIpSpace()));
    IpSpace ownedIps = EmptyIpSpace.INSTANCE;

    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);

    assertThat(
        result,
        hasEntry(equalTo(c1), hasEntry(equalTo(vrf1), hasEntry(equalTo(i1), not(containsIp(ip))))));
  }

  @Test
  public void testComputeDeliveredToSubnetNoInterfaceHostIps() {
    String c1 = "c1";
    String vrf1 = "vrf1";
    String i1 = "i1";
    Ip ip = Ip.parse("10.0.0.1");

    Map<String, Map<String, Map<String, IpSpace>>> arpFalseDestIp =
        ImmutableMap.of(c1, ImmutableMap.of(vrf1, ImmutableMap.of(i1, ip.toIpSpace())));
    Map<String, Map<String, IpSpace>> interfaceHostSubnetIps =
        ImmutableMap.of(c1, ImmutableMap.of(i1, EmptyIpSpace.INSTANCE));
    IpSpace ownedIps = EmptyIpSpace.INSTANCE;

    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);

    assertThat(
        result,
        hasEntry(equalTo(c1), hasEntry(equalTo(vrf1), hasEntry(equalTo(i1), not(containsIp(ip))))));
  }

  @Test
  public void testComputeDeliveredToSubnetEqual() {
    String c1 = "c1";
    String vrf1 = "vrf1";
    String i1 = "i1";
    Ip ip = Ip.parse("10.0.0.1");

    Map<String, Map<String, Map<String, IpSpace>>> arpFalseDestIp =
        ImmutableMap.of(c1, ImmutableMap.of(vrf1, ImmutableMap.of(i1, ip.toIpSpace())));
    Map<String, Map<String, IpSpace>> interfaceHostSubnetIps =
        ImmutableMap.of(c1, ImmutableMap.of(i1, ip.toIpSpace()));
    IpSpace ownedIps = EmptyIpSpace.INSTANCE;

    Map<String, Map<String, Map<String, IpSpace>>> result =
        computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);

    assertThat(
        result,
        hasEntry(equalTo(c1), hasEntry(equalTo(vrf1), hasEntry(equalTo(i1), containsIp(ip)))));
  }

  //  @Test
  //  public void testComputeDeliveredToSubnet() {
  //    // host IP
  //    {
  //      computeDeliveredToSubnet(arpFalseDestIp, interfaceHostSubnetIps, ownedIps);
  //    }
  //
  //  }

  private static final BDDPacket PKT = new BDDPacket();
  private static final IpSpaceToBDD DST = PKT.getDstIpSpaceToBDD();
  private static final BDD ZERO = PKT.getFactory().zero();
  private static final IpSpace PREFIX_IP_SPACE = Prefix.parse("1.1.1.0/24").toIpSpace();
  private static final IpSpace IP_IP_SPACE = Ip.parse("1.1.1.1").toIpSpace();

  /** No IPs have exits_network if the interface is full. */
  @Test
  public void testComputeExitsNetwork_full() {
    boolean isInterfaceFull = true;
    IpSpace dstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace arpFalseDstIp = UniverseIpSpace.INSTANCE;
    IpSpace arpFalseDstIpNetworkBroadcast = EmptyIpSpace.INSTANCE;
    IpSpace externalIps = UniverseIpSpace.INSTANCE;
    IpSpace exitsNetwork =
        computeExitsNetwork(
            isInterfaceFull,
            dstIpsWithUnownedNextHopIpArpFalse,
            arpFalseDstIp,
            arpFalseDstIpNetworkBroadcast,
            externalIps);
    assertEquals(ZERO, DST.visit(exitsNetwork));
  }

  /**
   * No IPs have exits_network if there are no unowned next-hop IPs and all dst IPs get ARP replies.
   */
  @Test
  public void testComputeExitsNetwork_noUnownedNextHop_noArpFalse() {
    boolean isInterfaceFull = false;
    IpSpace dstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace arpFalseDstIp = EmptyIpSpace.INSTANCE;
    IpSpace arpFalseDstIpNetworkBroadcast = EmptyIpSpace.INSTANCE;
    IpSpace externalIps = UniverseIpSpace.INSTANCE;
    IpSpace exitsNetwork =
        computeExitsNetwork(
            isInterfaceFull,
            dstIpsWithUnownedNextHopIpArpFalse,
            arpFalseDstIp,
            arpFalseDstIpNetworkBroadcast,
            externalIps);
    BDD expected = ZERO;
    BDD actual = DST.visit(exitsNetwork);
    assertEquals(expected, actual);
  }

  /**
   * Of the dstIPs we ARP for without a reply, the Network/broadcast IPs of the route's network
   * should not get exits_network.
   */
  @Test
  public void testComputeExitsNetwork_excludeNetworkBroadcastInArpFalse() {
    boolean isInterfaceFull = false;
    IpSpace dstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace arpFalseDstIp = PREFIX_IP_SPACE;
    IpSpace arpFalseDstIpNetworkBroadcast = IP_IP_SPACE;
    IpSpace externalIps = UniverseIpSpace.INSTANCE;
    IpSpace exitsNetwork =
        computeExitsNetwork(
            isInterfaceFull,
            dstIpsWithUnownedNextHopIpArpFalse,
            arpFalseDstIp,
            arpFalseDstIpNetworkBroadcast,
            externalIps);
    BDD expected = DST.visit(arpFalseDstIp).diff(DST.visit(arpFalseDstIpNetworkBroadcast));
    BDD actual = DST.visit(exitsNetwork);
    assertEquals(expected, actual);
  }

  /**
   * network/broadcast IPs should get exits_network if they are routable using an external next-hop
   * IP (even if they're also in arpFalseDstIp).
   */
  @Test
  public void testComputeExitsNetwork_includeNetworkBroadcastIPsInUnownedNextHop() {
    boolean isInterfaceFull = false;
    IpSpace dstIpsWithUnownedNextHopIpArpFalse = PREFIX_IP_SPACE;
    IpSpace arpFalseDstIp = PREFIX_IP_SPACE;
    IpSpace arpFalseDstIpNetworkBroadcast = IP_IP_SPACE;
    IpSpace externalIps = UniverseIpSpace.INSTANCE;
    IpSpace exitsNetwork =
        computeExitsNetwork(
            isInterfaceFull,
            dstIpsWithUnownedNextHopIpArpFalse,
            arpFalseDstIp,
            arpFalseDstIpNetworkBroadcast,
            externalIps);
    BDD expected = DST.visit(arpFalseDstIp);
    BDD actual = DST.visit(exitsNetwork);
    assertEquals(expected, actual);
  }

  /**
   * Of the dst IPs routable to an external next-hop, only external dst IPs should get
   * exits_network.
   */
  @Test
  public void testComputeExitsNetwork_includeOnlyExternalDstIpsWithUnownedNextHop() {
    boolean isInterfaceFull = false;
    IpSpace dstIpsWithUnownedNextHopIpArpFalse = PREFIX_IP_SPACE;
    IpSpace arpFalseDstIp = EmptyIpSpace.INSTANCE;
    IpSpace arpFalseDstIpNetworkBroadcast = EmptyIpSpace.INSTANCE;
    IpSpace externalIps = IP_IP_SPACE;
    IpSpace exitsNetwork =
        computeExitsNetwork(
            isInterfaceFull,
            dstIpsWithUnownedNextHopIpArpFalse,
            arpFalseDstIp,
            arpFalseDstIpNetworkBroadcast,
            externalIps);
    BDD expected = DST.visit(externalIps);
    BDD actual = DST.visit(exitsNetwork);
    assertEquals(expected, actual);
  }

  /**
   * Of the dst IPs that are routed to an external next-hop IP, only those that are external get
   * exits_network.
   */
  @Test
  public void testComputeExitsNetwork_unownedNextHop_include_only_external_dstIps() {
    // case: for unowned next-hop IPs, only include external dst IPs
    boolean isInterfaceFull = false;
    IpSpace dstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace arpFalseDstIp = PREFIX_IP_SPACE;
    IpSpace arpFalseDstIpNetworkBroadcast = EmptyIpSpace.INSTANCE;
    IpSpace externalIps = IP_IP_SPACE;
    IpSpace exitsNetwork =
        computeExitsNetwork(
            isInterfaceFull,
            dstIpsWithUnownedNextHopIpArpFalse,
            arpFalseDstIp,
            arpFalseDstIpNetworkBroadcast,
            externalIps);
    BDD expected = DST.visit(externalIps);
    BDD actual = DST.visit(exitsNetwork);
    assertEquals(expected, actual);
  }

  /**
   * If the interface is not full, only give a dstIP insufficient_info if we ARP for it without
   * reply and its the network or broadcast IP of the route's network.
   */
  @Test
  public void testInsufficientInfo_notFull() {
    boolean isInterfaceFull = false;
    IpSpace internalIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDstIpNetworkBroadcastIps = IP_IP_SPACE;
    IpSpace ifaceHostSubnetIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDstIp = EmptyIpSpace.INSTANCE;
    IpSpace ifaceDstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace ifaceDstIpsWithOwnedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace insufficientInfo =
        computeInsufficientInfo(
            isInterfaceFull,
            internalIps,
            ifaceArpFalseDstIpNetworkBroadcastIps,
            ifaceHostSubnetIps,
            ifaceArpFalseDstIp,
            ifaceDstIpsWithUnownedNextHopIpArpFalse,
            ifaceDstIpsWithOwnedNextHopIpArpFalse);
    BDD expected = DST.visit(IP_IP_SPACE);
    BDD actual = DST.visit(insufficientInfo);
    assertEquals(expected, actual);
  }

  /**
   * Case 1a: A dstIp should get insufficient_info if: we ARP for it but do not get a reply, and it
   * is internal but not connected to the interface.
   */
  @Test
  public void testInsufficientInfo_arpFalseDstIp_internalElsewhere() {
    boolean isInterfaceFull = true;
    IpSpace internalIps = PREFIX_IP_SPACE;
    IpSpace ifaceArpFalseDstIpNetworkBroadcastIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceHostSubnetIps = IP_IP_SPACE;
    IpSpace ifaceArpFalseDstIp = PREFIX_IP_SPACE;
    IpSpace ifaceDstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace ifaceDstIpsWithOwnedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace insufficientInfo =
        computeInsufficientInfo(
            isInterfaceFull,
            internalIps,
            ifaceArpFalseDstIpNetworkBroadcastIps,
            ifaceHostSubnetIps,
            ifaceArpFalseDstIp,
            ifaceDstIpsWithUnownedNextHopIpArpFalse,
            ifaceDstIpsWithOwnedNextHopIpArpFalse);
    BDD expected = DST.visit(PREFIX_IP_SPACE).diff(DST.visit(IP_IP_SPACE));
    BDD actual = DST.visit(insufficientInfo);
    assertEquals(expected, actual);
  }

  /**
   * Case 1b: A dstIp should get insufficient_info if: we ARP for it but do not get a reply, and it
   * is a network or broadcast IP of the route's network.
   */
  @Test
  public void testInsufficientInfo_arpFalseNetworkBroadcastDstIp() {
    boolean isInterfaceFull = true;
    IpSpace internalIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDstIpNetworkBroadcastIps = IP_IP_SPACE;
    IpSpace ifaceHostSubnetIps = PREFIX_IP_SPACE;
    IpSpace ifaceArpFalseDstIp = PREFIX_IP_SPACE;
    IpSpace ifaceDstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace ifaceDstIpsWithOwnedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace insufficientInfo =
        computeInsufficientInfo(
            isInterfaceFull,
            internalIps,
            ifaceArpFalseDstIpNetworkBroadcastIps,
            ifaceHostSubnetIps,
            ifaceArpFalseDstIp,
            ifaceDstIpsWithUnownedNextHopIpArpFalse,
            ifaceDstIpsWithOwnedNextHopIpArpFalse);
    BDD expected = DST.visit(IP_IP_SPACE);
    BDD actual = DST.visit(insufficientInfo);
    assertEquals(expected, actual);
  }

  /** Case 2: Internal dstIPs routable to an external next-hop should get insufficient_info. */
  @Test
  public void testInsufficientInfo_internalDstIpExternalNextHopIp() {
    boolean isInterfaceFull = true;
    IpSpace internalIps = IP_IP_SPACE;
    IpSpace ifaceArpFalseDstIpNetworkBroadcastIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceHostSubnetIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDstIp = EmptyIpSpace.INSTANCE;
    IpSpace ifaceDstIpsWithUnownedNextHopIpArpFalse = PREFIX_IP_SPACE;
    IpSpace ifaceDstIpsWithOwnedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace insufficientInfo =
        computeInsufficientInfo(
            isInterfaceFull,
            internalIps,
            ifaceArpFalseDstIpNetworkBroadcastIps,
            ifaceHostSubnetIps,
            ifaceArpFalseDstIp,
            ifaceDstIpsWithUnownedNextHopIpArpFalse,
            ifaceDstIpsWithOwnedNextHopIpArpFalse);
    BDD expected = DST.visit(IP_IP_SPACE);
    BDD actual = DST.visit(insufficientInfo);
    assertEquals(expected, actual);
  }

  /**
   * Case 3: Internal dstIPs routable to an owned next-hop IP for which we don't get an ARP reply
   * should get insufficient_info.
   */
  @Test
  public void testInsufficientInfo_ownedNextHopIp() {
    boolean isInterfaceFull = true;
    IpSpace internalIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDstIpNetworkBroadcastIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceHostSubnetIps = EmptyIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDstIp = EmptyIpSpace.INSTANCE;
    IpSpace ifaceDstIpsWithUnownedNextHopIpArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace ifaceDstIpsWithOwnedNextHopIpArpFalse = PREFIX_IP_SPACE;
    IpSpace insufficientInfo =
        computeInsufficientInfo(
            isInterfaceFull,
            internalIps,
            ifaceArpFalseDstIpNetworkBroadcastIps,
            ifaceHostSubnetIps,
            ifaceArpFalseDstIp,
            ifaceDstIpsWithUnownedNextHopIpArpFalse,
            ifaceDstIpsWithOwnedNextHopIpArpFalse);
    BDD expected = DST.visit(PREFIX_IP_SPACE);
    BDD actual = DST.visit(insufficientInfo);
    assertEquals(expected, actual);
  }

  /**
   * If an interface is full (all connected host IPs are owned by the network), all dstIPs for which
   * we get an ARP failure (regardless of the ARP IP) get neighbor_unreachable.
   */
  @Test
  public void testComputeNeighborUnreachable_full() {
    boolean isIfaceFull = true;
    IpSpace ownedIps = UniverseIpSpace.INSTANCE;
    IpSpace ifaceArpFalse = PREFIX_IP_SPACE;
    IpSpace ifaceArpFalseDestIp = UniverseIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDestIpNetworkBroadcast = IP_IP_SPACE;
    IpSpace ifaceHostSubnetIps = UniverseIpSpace.INSTANCE;
    IpSpace neighborUnreachable =
        computeNeighborUnreachable(
            isIfaceFull, ownedIps, ifaceArpFalse, ifaceArpFalseDestIp, ifaceArpFalseDestIpNetworkBroadcast, ifaceHostSubnetIps);
    BDD expected = DST.visit(PREFIX_IP_SPACE).diff(DST.visit(IP_IP_SPACE));
    BDD actual = DST.visit(neighborUnreachable);
    assertEquals(expected, actual);
  }

  /**
   * For an interface that is not full, we give a dst IP neighbor_unreachable if: it's not the
   * network or broadcast IP of the route's network, we ARP for the dst IP but don't get a reply,
   * the dst IP is a host IP of the connected subnet, and it's owned in the network.
   */
  @Test
  public void testComputeNeighborUnreachable_notFull() {
    boolean isIfaceFull = false;
    IpSpace ownedIps = ipWithWildcardMask(Ip.parse("255.0.0.0"), 0x00FFFFFFL).toIpSpace();
    IpSpace ifaceArpFalse = EmptyIpSpace.INSTANCE;
    IpSpace ifaceArpFalseDestIp =
        ipWithWildcardMask(Ip.parse("0.255.0.0"), 0xFF00FFFFL).toIpSpace();
    IpSpace ifaceArpFalseDestIpNetworkBroadcast = ipWithWildcardMask(Ip.parse("0.0.255.0"), 0xFFFF00FFL).toIpSpace();
    IpSpace ifaceHostSubnetIps =ipWithWildcardMask(Ip.parse("0.0.0.255"), 0xFFFFFF00L).toIpSpace();
    IpSpace neighborUnreachable =
        computeNeighborUnreachable(
            isIfaceFull, ownedIps, ifaceArpFalse, ifaceArpFalseDestIp, ifaceArpFalseDestIpNetworkBroadcast, ifaceHostSubnetIps);
    BDD expected =
        DST.visit(ownedIps).and(DST.visit(ifaceArpFalseDestIp)).and(DST.visit(ifaceHostSubnetIps))
        .diff(DST.visit(ifaceArpFalseDestIpNetworkBroadcast));
    BDD actual = DST.visit(neighborUnreachable);
    assertEquals(expected, actual);
  }

  /**
   * A dstIp gets delivered_to_subnet if we ARP for it and do not get a reply, it is not owned in
   * the network, and is in a connected subnet of the interface.
   */
  @Test
  public void testComputeDeliveredToSubnet() {
    IpSpace ownedIps = ipWithWildcardMask(Ip.parse("255.0.0.0"), 0x00FFFFFFL).toIpSpace();
    IpSpace ifaceArpFalseDstIp = ipWithWildcardMask(Ip.parse("0.255.0.0"), 0xFF00FFFFL).toIpSpace();
    IpSpace ifaceHostSubnetIps = ipWithWildcardMask(Ip.parse("0.0.255.0"), 0xFFFF00FFL).toIpSpace();
    IpSpace deliveredToSubnet =
        computeDeliveredToSubnet(ownedIps, ifaceArpFalseDstIp, ifaceHostSubnetIps);
    BDD expected =
        DST.visit(ifaceArpFalseDstIp).and(DST.visit(ifaceHostSubnetIps)).diff(DST.visit(ownedIps));
    BDD actual = DST.visit(deliveredToSubnet);
    assertEquals(expected, actual);
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
    _ib.setActive(true);
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
        StaticRoute.builder()
            .setNetwork(prefix)
            .setNextHopInterface(i1.getName())
            .setAdministrativeCost(1)
            .build();

    StaticRoute route2 =
        StaticRoute.builder()
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
                    Ip.AUTO,
                    ImmutableSet.of(
                        new FibEntry(
                            new FibForward(Ip.AUTO, i1.getName()), ImmutableList.of(route1)))))
            .build();

    MockFib fib2 =
        MockFib.builder()
            .setMatchingIps(ImmutableMap.of(prefix, ipSpace))
            .setFibEntries(
                ImmutableMap.of(
                    Ip.AUTO,
                    ImmutableSet.of(
                        new FibEntry(
                            new FibForward(Ip.AUTO, i2.getName()), ImmutableList.of(route2)))))
            .build();

    Map<String, Map<String, Fib>> fibs =
        ImmutableMap.of(
            c1.getHostname(), ImmutableMap.of(v1.getName(), fib1),
            c2.getHostname(), ImmutableMap.of(v2.getName(), fib2));

    ForwardingAnalysis analysis =
        new ForwardingAnalysisImpl(
            ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2),
            fibs,
            new Topology(ImmutableSortedSet.of()));

    assertFalse(
        analysis
            .getDeliveredToSubnet()
            .get(c1.getHostname())
            .get(v1.getName())
            .get(i1.getName())
            .containsIp(ip2, c1.getIpSpaces()));

    assertTrue(
        analysis
            .getNeighborUnreachable()
            .get(c1.getHostname())
            .get(v1.getName())
            .get(i1.getName())
            .containsIp(ip2, c1.getIpSpaces()));

    assertFalse(
        analysis
            .getInsufficientInfo()
            .get(c1.getHostname())
            .get(v1.getName())
            .get(i1.getName())
            .containsIp(ip2, c1.getIpSpaces()));
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
    public boolean containsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    protected boolean exprEquals(Object o) {
      return _num == ((MockIpSpace) o)._num;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_num);
    }

    @Override
    public String toString() {
      return String.format("TestIpSpace%d", _num);
    }
  }
}
