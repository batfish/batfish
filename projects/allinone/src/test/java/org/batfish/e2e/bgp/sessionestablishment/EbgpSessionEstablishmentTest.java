package org.batfish.e2e.bgp.sessionestablishment;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.batfish.datamodel.BgpProcess.testBgpProcess;
import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.batfish.datamodel.matchers.HopMatchers.hasNodeName;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHops;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.ValueGraph;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * End-to-end tests for eBGP session establishment with varying hop counts and ACL configurations.
 *
 * <p>Topology:
 *
 * <pre>
 * +-----------+                       +-------------+                   +--------------+
 * |           |1.0.0.0/31             |             |                   |              |
 * |           +-----------------------+             |                   |    node3     |
 * |   node1   |            1.0.0.1/31 |   node2     |1.0.0.2/31         |              |
 * |           |                       |             +-------------------+              |
 * |           |                       |             |         1.0.0.3/31|              |
 * +-----------+                       +-------------+                   +--------------+
 * </pre>
 *
 * Static routes provide full connectivity between node1 and node3 through node2.
 */
public class EbgpSessionEstablishmentTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final Ip NODE1_IP = Ip.parse("1.0.0.0");
  private static final Ip NODE2_LEFT_IP = Ip.parse("1.0.0.1");
  private static final Ip NODE2_RIGHT_IP = Ip.parse("1.0.0.2");
  private static final Ip NODE3_IP = Ip.parse("1.0.0.3");

  /**
   * Generates a three-node linear network with optional ACLs.
   *
   * <p>BGP sessions are configured between {@code initiatorNode} and {@code listenerNode}, with the
   * specified multihop and AS settings.
   *
   * @param denyIntoNode3 if true, add an incoming ACL on node3 that blocks all traffic
   * @param allowOnlyEstablishedIntoNode1 if true, add an ACL on node1 that only permits established
   *     TCP connections
   * @param ebgpMultihop whether to enable eBGP multihop on the BGP sessions
   * @param initiatorIp the local IP used by the initiator (determines which nodes peer)
   * @param listenerIp the peer address (determines which nodes peer)
   */
  private static SortedMap<String, Configuration> buildNetwork(
      boolean denyIntoNode3,
      boolean allowOnlyEstablishedIntoNode1,
      boolean ebgpMultihop,
      Ip initiatorIp,
      Ip listenerIp,
      long initiatorAs,
      long listenerAs) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    // node1
    Configuration c1 = cb.setHostname("node1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    Interface i11 =
        nf.interfaceBuilder()
            .setOwner(c1)
            .setVrf(v1)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31"))
            .build();

    // node2
    Configuration c2 = cb.setHostname("node2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/31"))
        .build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.2/31"))
        .build();

    // node3
    Configuration c3 = cb.setHostname("node3").build();
    Vrf v3 = nf.vrfBuilder().setOwner(c3).build();
    Interface i31 =
        nf.interfaceBuilder()
            .setOwner(c3)
            .setVrf(v3)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.3/31"))
            .build();

    // Static routes for node1<->node3 connectivity through node2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.3/32"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i11.getName())
                .setNextHopIp(NODE2_LEFT_IP)
                .build()));
    v3.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.0.0.0/32"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i31.getName())
                .setNextHopIp(NODE2_RIGHT_IP)
                .build()));

    // Configure BGP on the initiator and listener nodes
    Configuration initiatorConfig = initiatorIp.equals(NODE1_IP) ? c1 : c3;
    Configuration listenerConfig =
        listenerIp.equals(NODE1_IP) ? c1 : listenerIp.equals(NODE2_LEFT_IP) ? c2 : c3;
    Vrf initiatorVrf = initiatorConfig.getVrfs().values().iterator().next();
    Vrf listenerVrf = listenerConfig.getVrfs().values().iterator().next();

    Ipv4UnicastAddressFamily ipv4af =
        Ipv4UnicastAddressFamily.builder()
            .setExportPolicy(nf.routingPolicyBuilder().setOwner(initiatorConfig).build().getName())
            .build();
    BgpProcess initiatorBgpProcess = testBgpProcess(initiatorIp);
    initiatorVrf.setBgpProcess(initiatorBgpProcess);
    nf.bgpNeighborBuilder()
        .setBgpProcess(initiatorBgpProcess)
        .setLocalIp(initiatorIp)
        .setPeerAddress(listenerIp)
        .setLocalAs(initiatorAs)
        .setRemoteAs(listenerAs)
        .setEbgpMultihop(ebgpMultihop)
        .setIpv4UnicastAddressFamily(ipv4af)
        .build();

    Ipv4UnicastAddressFamily listenerIpv4af =
        Ipv4UnicastAddressFamily.builder()
            .setExportPolicy(nf.routingPolicyBuilder().setOwner(listenerConfig).build().getName())
            .build();
    BgpProcess listenerBgpProcess = testBgpProcess(listenerIp);
    listenerVrf.setBgpProcess(listenerBgpProcess);
    nf.bgpNeighborBuilder()
        .setBgpProcess(listenerBgpProcess)
        .setLocalIp(listenerIp)
        .setPeerAddress(initiatorIp)
        .setLocalAs(listenerAs)
        .setRemoteAs(initiatorAs)
        .setEbgpMultihop(ebgpMultihop)
        .setIpv4UnicastAddressFamily(listenerIpv4af)
        .build();

    // Optional ACLs
    if (denyIntoNode3) {
      i31.setIncomingFilter(
          nf.aclBuilder()
              .setOwner(c3)
              .setLines(
                  ImmutableList.of(
                      ExprAclLine.rejecting(AclLineMatchExprs.matchSrc(UniverseIpSpace.INSTANCE))))
              .build());
    }
    if (allowOnlyEstablishedIntoNode1) {
      i11.setOutgoingFilter(
          nf.aclBuilder().setOwner(c1).setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL)).build());
      i11.setIncomingFilter(
          nf.aclBuilder()
              .setOwner(c1)
              .setLines(
                  ImmutableList.of(
                      ExprAclLine.acceptingHeaderSpace(
                          HeaderSpace.builder()
                              .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
                              .setTcpFlags(ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG))
                              .build()),
                      REJECT_ALL))
              .build());
    }

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3);
  }

  private Batfish computeDataPlane(SortedMap<String, Configuration> configs) throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  /**
   * Returns true if the BGP session between initiator and listener is established in the computed
   * BGP topology.
   */
  private static boolean isSessionEstablished(
      Batfish batfish, String initiatorHost, String initiatorVrf, Ip listenerIp) {
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpGraph =
        batfish.getTopologyProvider().getBgpTopology(batfish.getSnapshot()).getGraph();
    BgpPeerConfigId initiatorId =
        new BgpPeerConfigId(initiatorHost, initiatorVrf, listenerIp.toPrefix(), false);
    return bgpGraph.degree(initiatorId) > 0;
  }

  /** eBGP single-hop session between directly connected peers succeeds. */
  @Test
  public void testEbgpSinglehopSuccess() throws IOException {
    // node1 (AS 1) peers with node2 (AS 2) -- directly connected, single-hop
    SortedMap<String, Configuration> configs =
        buildNetwork(false, false, false, NODE1_IP, NODE2_LEFT_IP, 1L, 2L);
    Batfish batfish = computeDataPlane(configs);

    String initiatorVrf = configs.get("node1").getVrfs().values().iterator().next().getName();
    assertTrue(isSessionEstablished(batfish, "node1", initiatorVrf, NODE2_LEFT_IP));

    // Verify the BGP TCP flow is forwarded directly (single hop)
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(batfish.getSnapshot());
    Flow bgpFlow =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(true)
            .setIngressNode("node1")
            .setIngressVrf(initiatorVrf)
            .setSrcIp(NODE1_IP)
            .setDstIp(NODE2_LEFT_IP)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.BGP.number())
            .build();
    Trace trace =
        getOnlyElement(
            tracerouteEngine
                .computeTraces(ImmutableSet.of(bgpFlow), false)
                .get(bgpFlow)
                .iterator());
    assertThat(trace, hasHops(contains(hasNodeName("node1"), hasNodeName("node2"))));
    assertThat(trace, hasDisposition(FlowDisposition.ACCEPTED));
  }

  /** eBGP single-hop session fails when the peer is two hops away. */
  @Test
  public void testEbgpSinglehopFailure() throws IOException {
    // node1 (AS 1) peers with node3 (AS 2) -- two hops apart, single-hop only
    SortedMap<String, Configuration> configs =
        buildNetwork(false, false, false, NODE1_IP, NODE3_IP, 1L, 2L);
    Batfish batfish = computeDataPlane(configs);

    String initiatorVrf = configs.get("node1").getVrfs().values().iterator().next().getName();
    assertFalse(isSessionEstablished(batfish, "node1", initiatorVrf, NODE3_IP));

    // The flow is accepted by node3 (reachability exists), but the session shouldn't establish
    // because it traverses more than one hop
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(batfish.getSnapshot());
    Flow bgpFlow =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(true)
            .setIngressNode("node1")
            .setIngressVrf(initiatorVrf)
            .setSrcIp(NODE1_IP)
            .setDstIp(NODE3_IP)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.BGP.number())
            .build();
    Trace trace =
        getOnlyElement(
            tracerouteEngine
                .computeTraces(ImmutableSet.of(bgpFlow), false)
                .get(bgpFlow)
                .iterator());
    // Flow is accepted (reachability is fine), but session fails due to hop count
    assertThat(trace, hasDisposition(FlowDisposition.ACCEPTED));
    assertThat(
        trace, hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3"))));
  }

  /** eBGP multihop session succeeds when the peer is two hops away. */
  @Test
  public void testEbgpMultihopSuccess() throws IOException {
    // node1 (AS 1) peers with node3 (AS 2) -- two hops apart, multihop enabled
    SortedMap<String, Configuration> configs =
        buildNetwork(false, false, true, NODE1_IP, NODE3_IP, 1L, 2L);
    Batfish batfish = computeDataPlane(configs);

    String initiatorVrf = configs.get("node1").getVrfs().values().iterator().next().getName();
    assertTrue(isSessionEstablished(batfish, "node1", initiatorVrf, NODE3_IP));

    // Verify the BGP TCP flow traverses all three nodes
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(batfish.getSnapshot());
    Flow bgpFlow =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(true)
            .setIngressNode("node1")
            .setIngressVrf(initiatorVrf)
            .setSrcIp(NODE1_IP)
            .setDstIp(NODE3_IP)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.BGP.number())
            .build();
    Trace trace =
        getOnlyElement(
            tracerouteEngine
                .computeTraces(ImmutableSet.of(bgpFlow), false)
                .get(bgpFlow)
                .iterator());
    assertThat(trace, hasDisposition(FlowDisposition.ACCEPTED));
    assertThat(
        trace, hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3"))));
  }

  /** eBGP multihop session fails when an ACL blocks traffic at the listener. */
  @Test
  public void testEbgpMultihopFailureWithAcl() throws IOException {
    // node1 (AS 1) peers with node3 (AS 2) -- multihop but node3 has a deny-all inbound ACL
    SortedMap<String, Configuration> configs =
        buildNetwork(true, false, true, NODE1_IP, NODE3_IP, 1L, 2L);
    Batfish batfish = computeDataPlane(configs);

    String initiatorVrf = configs.get("node1").getVrfs().values().iterator().next().getName();
    assertFalse(isSessionEstablished(batfish, "node1", initiatorVrf, NODE3_IP));

    // The flow is denied by the ACL on node3
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(batfish.getSnapshot());
    Flow bgpFlow =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(true)
            .setIngressNode("node1")
            .setIngressVrf(initiatorVrf)
            .setSrcIp(NODE1_IP)
            .setDstIp(NODE3_IP)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.BGP.number())
            .build();
    Trace trace =
        getOnlyElement(
            tracerouteEngine
                .computeTraces(ImmutableSet.of(bgpFlow), false)
                .get(bgpFlow)
                .iterator());
    assertThat(
        trace,
        allOf(
            hasDisposition(FlowDisposition.DENIED_IN),
            hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3")))));
  }

  /**
   * eBGP multihop session succeeds when an ACL on the initiator permits established connections.
   */
  @Test
  public void testEbgpMultihopWithAclPermitEstablished() throws IOException {
    // node1 (AS 1) peers with node3 (AS 2) -- multihop, ACL on node1 permits only established TCP
    SortedMap<String, Configuration> configs =
        buildNetwork(false, true, true, NODE1_IP, NODE3_IP, 1L, 2L);
    Batfish batfish = computeDataPlane(configs);

    String initiatorVrf = configs.get("node1").getVrfs().values().iterator().next().getName();
    assertTrue(isSessionEstablished(batfish, "node1", initiatorVrf, NODE3_IP));

    // Verify the forward BGP TCP flow traverses all three nodes
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(batfish.getSnapshot());
    Flow bgpFlow =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(true)
            .setIngressNode("node1")
            .setIngressVrf(initiatorVrf)
            .setSrcIp(NODE1_IP)
            .setDstIp(NODE3_IP)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.BGP.number())
            .build();
    Trace trace =
        getOnlyElement(
            tracerouteEngine
                .computeTraces(ImmutableSet.of(bgpFlow), false)
                .get(bgpFlow)
                .iterator());
    assertThat(trace, hasDisposition(FlowDisposition.ACCEPTED));
    assertThat(
        trace, hasHops(contains(hasNodeName("node1"), hasNodeName("node2"), hasNodeName("node3"))));
  }
}
