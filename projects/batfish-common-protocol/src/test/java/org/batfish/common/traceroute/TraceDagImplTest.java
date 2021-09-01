package org.batfish.common.traceroute;

import static org.batfish.datamodel.flow.HopTestUtils.acceptedHop;
import static org.batfish.datamodel.flow.HopTestUtils.forwardedHop;
import static org.batfish.datamodel.flow.HopTestUtils.noRouteHop;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.common.traceroute.TraceDagImpl.Node;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.flow.PostNatFibLookup;
import org.batfish.datamodel.flow.SessionMatchExpr;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.junit.Test;

public class TraceDagImplTest {
  private static final Flow TEST_FLOW =
      Flow.builder().setDstIp(Ip.parse("1.1.1.1")).setIngressNode("src").build();

  @Test
  public void testTraceDag() {
    Hop hopA = forwardedHop("A", "vrf"); // 0
    Hop hopB1 = forwardedHop("B1", "vrf"); // 1
    Hop hopB2 = forwardedHop("B2", "vrf"); // 2
    Hop hopC1 = acceptedHop("C1"); // 3
    Hop hopC2 = noRouteHop("C2"); // 4
    Hop hopC3 = noRouteHop("C3"); // 5
    FirewallSessionTraceInfo session =
        new FirewallSessionTraceInfo(
            "B1",
            PostNatFibLookup.INSTANCE,
            new OriginatingSessionScope("vrf"),
            new SessionMatchExpr(IpProtocol.TCP, Ip.ZERO, Ip.ZERO, 0, 0),
            null);
    Flow c1ReturnFlow = TEST_FLOW.toBuilder().setIngressNode("C1").build();
    Flow c2ReturnFlow = TEST_FLOW.toBuilder().setIngressNode("C2").build();
    Node nodeA = new Node(hopA, null, null, null, ImmutableList.of(1, 2));
    Node nodeB1 = new Node(hopB1, session, null, null, ImmutableList.of(3, 4, 5));
    Node nodeB2 = new Node(hopB2, null, null, null, ImmutableList.of(3, 4, 5));
    Node nodeC1 = new Node(hopC1, null, FlowDisposition.ACCEPTED, c1ReturnFlow, ImmutableList.of());
    Node nodeC2 = new Node(hopC2, null, FlowDisposition.ACCEPTED, c2ReturnFlow, ImmutableList.of());
    Node nodeC3 = new Node(hopC3, null, FlowDisposition.NO_ROUTE, null, ImmutableList.of());
    TraceDagImpl dag =
        new TraceDagImpl(
            ImmutableList.of(nodeA, nodeB1, nodeB2, nodeC1, nodeC2, nodeC3), ImmutableList.of(0));
    List<TraceAndReverseFlow> traces = dag.getTraces().collect(Collectors.toList());
    assertThat(
        traces,
        contains(
            // A -> B1 -> C1
            new TraceAndReverseFlow(
                new Trace(FlowDisposition.ACCEPTED, ImmutableList.of(hopA, hopB1, hopC1)),
                c1ReturnFlow,
                ImmutableList.of(session)),
            // A -> B1 -> C2
            new TraceAndReverseFlow(
                new Trace(FlowDisposition.ACCEPTED, ImmutableList.of(hopA, hopB1, hopC2)),
                c2ReturnFlow,
                ImmutableList.of(session)),
            // A -> B1 -> C3
            new TraceAndReverseFlow(
                new Trace(FlowDisposition.NO_ROUTE, ImmutableList.of(hopA, hopB1, hopC3)),
                null,
                ImmutableList.of(session)),
            // A -> B2 -> C1
            new TraceAndReverseFlow(
                new Trace(FlowDisposition.ACCEPTED, ImmutableList.of(hopA, hopB2, hopC1)),
                c1ReturnFlow,
                ImmutableList.of()),
            // A -> B2 -> C2
            new TraceAndReverseFlow(
                new Trace(FlowDisposition.ACCEPTED, ImmutableList.of(hopA, hopB2, hopC2)),
                c2ReturnFlow,
                ImmutableList.of()),
            // A -> B2 -> C3
            new TraceAndReverseFlow(
                new Trace(FlowDisposition.NO_ROUTE, ImmutableList.of(hopA, hopB2, hopC3)),
                null,
                ImmutableList.of())));
    assertEquals(6, dag.size());
    assertEquals(6, dag.countNodes());
    assertEquals(8, dag.countEdges());
  }
}
