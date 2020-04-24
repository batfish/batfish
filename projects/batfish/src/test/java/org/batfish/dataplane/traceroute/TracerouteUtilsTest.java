package org.batfish.dataplane.traceroute;

import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.dataplane.traceroute.FlowTracer.matchSessionReturnFlow;
import static org.batfish.dataplane.traceroute.TracerouteUtils.buildSessionsByIngressInterface;
import static org.batfish.dataplane.traceroute.TracerouteUtils.buildSessionsByOriginatingVrf;
import static org.batfish.dataplane.traceroute.TracerouteUtils.getTcpFlagsForReverse;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.IncomingSessionScope;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.flow.SessionAction;
import org.batfish.datamodel.flow.SessionMatchExpr;
import org.batfish.datamodel.flow.SessionScope;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

/** Tests for {@link TracerouteUtils}. */
public final class TracerouteUtilsTest {
  @Test
  public void returnFlow() {
    Ip ip1 = Ip.parse("1.1.1.1");
    int port1 = 1111;
    Ip ip2 = Ip.parse("2.2.2.2");
    int port2 = 2222;
    String node1 = "node1";
    String vrf1 = "vrf1";
    String iface1 = "iface1";
    Flow forwardFlow =
        Flow.builder()
            .setSrcIp(ip1)
            .setSrcPort(port1)
            .setDstIp(ip2)
            .setDstPort(port2)
            .setIngressNode(node1)
            .setIngressVrf(vrf1)
            .setIngressInterface(iface1)
            .build();
    String node2 = "node2";
    String vrf2 = "vrf2";
    String iface2 = "iface2";
    assertThat(
        TracerouteUtils.returnFlow(forwardFlow, node2, vrf2, null),
        equalTo(
            Flow.builder()
                .setSrcIp(ip2)
                .setSrcPort(port2)
                .setDstIp(ip1)
                .setDstPort(port1)
                .setIngressNode(node2)
                .setIngressVrf(vrf2)
                .build()));
    assertThat(
        TracerouteUtils.returnFlow(forwardFlow, node2, null, iface2),
        equalTo(
            Flow.builder()
                .setSrcIp(ip2)
                .setSrcPort(port2)
                .setDstIp(ip1)
                .setDstPort(port1)
                .setIngressNode(node2)
                .setIngressInterface(iface2)
                .build()));
  }

  @Test
  public void testGetTcpFlagsForReverseNoSynNoAck() {
    // !SYN, !ACK -> !SYN, !ACK
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().build());
    assertFalse(reverseTcpFlags.getSyn());
    assertFalse(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseOnlySyn() {
    // SYN, !ACK -> SYN, ACK
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().setSyn(true).build());
    assertTrue(reverseTcpFlags.getSyn());
    assertTrue(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseSynAck() {
    // SYN, ACK -> !SYN, ACK
    TcpFlags reverseTcpFlags =
        getTcpFlagsForReverse(TcpFlags.builder().setSyn(true).setAck(true).build());
    assertFalse(reverseTcpFlags.getSyn());
    assertTrue(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseOnlyAck() {
    // !SYN, ACK -> !SYN, ACK
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().setAck(true).build());
    assertFalse(reverseTcpFlags.getSyn());
    assertTrue(reverseTcpFlags.getAck());
  }

  @Test
  public void testGetTcpFlagsForReverseAllTrue() {
    TcpFlags reverseTcpFlags =
        getTcpFlagsForReverse(
            TcpFlags.builder()
                .setRst(true)
                .setFin(true)
                .setUrg(true)
                .setEce(true)
                .setPsh(true)
                .setCwr(true)
                .build());

    assertTrue(reverseTcpFlags.getRst());
    assertTrue(reverseTcpFlags.getFin());
    assertTrue(reverseTcpFlags.getUrg());
    assertTrue(reverseTcpFlags.getEce());
    assertTrue(reverseTcpFlags.getPsh());
    assertTrue(reverseTcpFlags.getCwr());
  }

  @Test
  public void testGetTcpFlagsForReverseAllFalse() {
    TcpFlags reverseTcpFlags = getTcpFlagsForReverse(TcpFlags.builder().build());

    assertFalse(reverseTcpFlags.getRst());
    assertFalse(reverseTcpFlags.getFin());
    assertFalse(reverseTcpFlags.getUrg());
    assertFalse(reverseTcpFlags.getEce());
    assertFalse(reverseTcpFlags.getPsh());
    assertFalse(reverseTcpFlags.getCwr());
  }

  @Test
  public void testSessionTransformation() {
    Ip srcIp1 = Ip.parse("1.1.1.1");
    Ip dstIp1 = Ip.parse("2.2.2.2");
    int srcPort1 = 10001;
    int dstPort1 = 10002;
    Ip srcIp2 = Ip.parse("3.3.3.3");
    Ip dstIp2 = Ip.parse("4.4.4.4");
    int srcPort2 = 10003;
    int dstPort2 = 10004;

    Flow inputFlow =
        Flow.builder()
            .setIngressNode("inNode")
            .setIngressInterface("inInterf")
            .setDstIp(dstIp1)
            .setSrcIp(srcIp1)
            .setDstPort(dstPort1)
            .setSrcPort(srcPort1)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    Flow currentFlow =
        Flow.builder()
            .setIngressNode("inNode")
            .setIngressInterface("inInterf")
            .setDstIp(dstIp2)
            .setSrcIp(srcIp2)
            .setDstPort(dstPort2)
            .setSrcPort(srcPort2)
            .setIpProtocol(IpProtocol.TCP)
            .build();

    Transformation transformation = TracerouteUtils.sessionTransformation(inputFlow, currentFlow);

    assertThat(
        transformation,
        equalTo(
            new Transformation(
                AclLineMatchExprs.TRUE,
                ImmutableList.of(
                    assignSourceIp(dstIp1, dstIp1),
                    assignSourcePort(dstPort1, dstPort1),
                    assignDestinationIp(srcIp1, srcIp1),
                    assignDestinationPort(srcPort1, srcPort1)),
                null,
                null)));
  }

  @Test
  public void testBuildSessionsByIngressInterface() {
    Flow flow =
        Flow.builder()
            .setIngressNode("n1")
            .setIngressInterface("i1")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(22)
            .setDstPort(22)
            .setSrcIp(Ip.parse("1.1.1.1"))
            .setDstIp(Ip.parse("2.2.2.2"))
            .build();
    SessionMatchExpr matchExpr = matchSessionReturnFlow(flow);
    SessionScope incomingI1 = new IncomingSessionScope(ImmutableSet.of("i1"));
    // need two different actions to differentiate two sessions with same node and ingress interface
    SessionAction action1 = new ForwardOutInterface("i1", null);
    SessionAction action2 = new ForwardOutInterface("i2", null);

    // Incoming interface based sessions: should appear in sessions by ingress interface map
    FirewallSessionTraceInfo n1i1Session =
        new FirewallSessionTraceInfo("n1", action1, incomingI1, matchExpr, null);
    FirewallSessionTraceInfo n1i1i2Session =
        new FirewallSessionTraceInfo(
            "n1", action2, new IncomingSessionScope(ImmutableSet.of("i1", "i2")), matchExpr, null);
    FirewallSessionTraceInfo n2i1Session =
        new FirewallSessionTraceInfo("n2", action1, incomingI1, matchExpr, null);

    // VRF-originated session: should not affect ingress interfaces map
    FirewallSessionTraceInfo originatingVrfSession =
        new FirewallSessionTraceInfo(
            "n1", action1, new OriginatingSessionScope("vrf"), matchExpr, null);

    Set<FirewallSessionTraceInfo> sessions =
        ImmutableSet.of(n1i1Session, n1i1i2Session, n2i1Session, originatingVrfSession);
    Multimap<NodeInterfacePair, FirewallSessionTraceInfo> ifaceSessionsMap =
        buildSessionsByIngressInterface(sessions);

    NodeInterfacePair n1i1 = NodeInterfacePair.of("n1", "i1");
    NodeInterfacePair n1i2 = NodeInterfacePair.of("n1", "i2");
    NodeInterfacePair n2i1 = NodeInterfacePair.of("n2", "i1");
    Multimap<NodeInterfacePair, FirewallSessionTraceInfo> expected =
        ImmutableMultimap.of(
            n1i1, n1i1Session, n1i1, n1i1i2Session, n1i2, n1i1i2Session, n2i1, n2i1Session);
    assertEquals(ifaceSessionsMap, expected);
  }

  @Test
  public void testBuildSessionsByOriginatingVrf() {
    Flow flow =
        Flow.builder()
            .setIngressNode("n1")
            .setIngressInterface("i1")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(22)
            .setDstPort(22)
            .setSrcIp(Ip.parse("1.1.1.1"))
            .setDstIp(Ip.parse("2.2.2.2"))
            .build();
    SessionMatchExpr matchExpr = matchSessionReturnFlow(flow);
    SessionScope originatingVrf1 = new OriginatingSessionScope("vrf1");
    SessionScope originatingVrf2 = new OriginatingSessionScope("vrf2");
    // need two different actions to differentiate two sessions on the same node and vrf
    SessionAction action1 = new ForwardOutInterface("i1", null);
    SessionAction action2 = new ForwardOutInterface("i2", null);

    // VRF-originated sessions: should appear in sessions by originating vrf map
    FirewallSessionTraceInfo n1Vrf1Session1 =
        new FirewallSessionTraceInfo("n1", action1, originatingVrf1, matchExpr, null);
    FirewallSessionTraceInfo n1Vrf1Session2 =
        new FirewallSessionTraceInfo("n1", action2, originatingVrf1, matchExpr, null);
    FirewallSessionTraceInfo n1Vrf2Session =
        new FirewallSessionTraceInfo("n1", action1, originatingVrf2, matchExpr, null);
    FirewallSessionTraceInfo n2Vrf1Session =
        new FirewallSessionTraceInfo("n2", action1, originatingVrf1, matchExpr, null);

    // Incoming interface based session: should not affect originating vrf map
    FirewallSessionTraceInfo incomingSession =
        new FirewallSessionTraceInfo(
            "n1", action1, new IncomingSessionScope(ImmutableSet.of("i1")), matchExpr, null);

    Set<FirewallSessionTraceInfo> sessions =
        ImmutableSet.of(
            n1Vrf1Session1, n1Vrf1Session2, n1Vrf2Session, n2Vrf1Session, incomingSession);
    Map<String, Multimap<String, FirewallSessionTraceInfo>> vrfSessionsMap =
        buildSessionsByOriginatingVrf(sessions);

    Map<String, Multimap<String, FirewallSessionTraceInfo>> expected =
        ImmutableMap.of(
            "n1",
            ImmutableMultimap.of(
                "vrf1", n1Vrf1Session1, "vrf1", n1Vrf1Session2, "vrf2", n1Vrf2Session),
            "n2",
            ImmutableMultimap.of("vrf1", n2Vrf1Session));
    assertEquals(vrfSessionsMap, expected);
  }
}
