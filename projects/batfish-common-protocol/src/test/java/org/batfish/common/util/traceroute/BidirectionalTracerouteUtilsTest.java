package org.batfish.common.util.traceroute;

import static org.batfish.common.util.traceroute.BidirectionalTracerouteUtils.computeBidirectionalTraces;
import static org.batfish.common.util.traceroute.MockTracerouteEngine.forFlow;
import static org.batfish.common.util.traceroute.MockTracerouteEngine.forFlows;
import static org.batfish.common.util.traceroute.MockTracerouteEngine.forSessions;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.BidirectionalTrace;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.junit.Test;

public class BidirectionalTracerouteUtilsTest {
  private static final Flow FORWARD_FLOW =
      Flow.builder()
          .setDstIp(Ip.parse("1.1.1.1"))
          .setTag("TAG")
          .setIngressNode("forwardIngressNode")
          .setIngressInterface("forwardIngressInterface")
          .build();

  private static final Flow REVERSE_FLOW =
      Flow.builder()
          .setDstIp(Ip.parse("1.1.1.1"))
          .setTag("TAG")
          .setIngressNode("reverseIngressNode")
          .setIngressInterface("reverseIngressInterface")
          .build();

  @Test
  public void testNoReverseFlow() {
    Trace forwardTrace = new Trace(NO_ROUTE, ImmutableList.of());
    TraceAndReverseFlow traceAndReverseFlow = new TraceAndReverseFlow(forwardTrace, null);

    TracerouteEngine tracerouteEngine =
        forFlow(FORWARD_FLOW, ImmutableList.of(traceAndReverseFlow));

    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);

    assertThat(
        bidirectionalTraces,
        contains(
            new BidirectionalTrace(FORWARD_FLOW, forwardTrace, ImmutableSet.of(), null, null)));
  }

  @Test
  public void testReverseFlow() {
    Trace forwardTrace = new Trace(ACCEPTED, ImmutableList.of());
    TraceAndReverseFlow forwardTarf = new TraceAndReverseFlow(forwardTrace, REVERSE_FLOW);
    Trace reverseTrace = new Trace(DENIED_IN, ImmutableList.of());
    TraceAndReverseFlow reverseTarf = new TraceAndReverseFlow(reverseTrace, null);

    TracerouteEngine tracerouteEngine =
        forFlows(
            ImmutableMap.of(
                FORWARD_FLOW,
                ImmutableList.of(forwardTarf),
                REVERSE_FLOW,
                ImmutableList.of(reverseTarf)));

    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);

    assertThat(
        bidirectionalTraces,
        contains(
            new BidirectionalTrace(
                FORWARD_FLOW, forwardTrace, ImmutableSet.of(), REVERSE_FLOW, reverseTrace)));
  }

  @Test
  public void testMultipath() {
    Trace forwardTrace1 = new Trace(ACCEPTED, ImmutableList.of());
    Trace forwardTrace2 = new Trace(DENIED_IN, ImmutableList.of());
    Trace forwardTrace3 = new Trace(DELIVERED_TO_SUBNET, ImmutableList.of());
    TraceAndReverseFlow forwardTarf1 = new TraceAndReverseFlow(forwardTrace1, REVERSE_FLOW);
    TraceAndReverseFlow forwardTarf2 = new TraceAndReverseFlow(forwardTrace2, null);
    TraceAndReverseFlow forwardTarf3 = new TraceAndReverseFlow(forwardTrace3, REVERSE_FLOW);
    Trace reverseTrace1 = new Trace(DENIED_IN, ImmutableList.of());
    Trace reverseTrace2 = new Trace(EXITS_NETWORK, ImmutableList.of());
    TraceAndReverseFlow reverseTarf1 = new TraceAndReverseFlow(reverseTrace1, null);
    TraceAndReverseFlow reverseTarf2 = new TraceAndReverseFlow(reverseTrace2, FORWARD_FLOW);

    TracerouteEngine tracerouteEngine =
        forFlows(
            ImmutableMap.of(
                FORWARD_FLOW,
                ImmutableList.of(forwardTarf1, forwardTarf2, forwardTarf3),
                REVERSE_FLOW,
                ImmutableList.of(reverseTarf1, reverseTarf2)));

    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);

    assertThat(
        bidirectionalTraces,
        contains(
            new BidirectionalTrace(
                FORWARD_FLOW, forwardTrace1, ImmutableSet.of(), REVERSE_FLOW, reverseTrace1),
            new BidirectionalTrace(
                FORWARD_FLOW, forwardTrace1, ImmutableSet.of(), REVERSE_FLOW, reverseTrace2),
            new BidirectionalTrace(FORWARD_FLOW, forwardTrace2, ImmutableSet.of(), null, null),
            new BidirectionalTrace(
                FORWARD_FLOW, forwardTrace3, ImmutableSet.of(), REVERSE_FLOW, reverseTrace1),
            new BidirectionalTrace(
                FORWARD_FLOW, forwardTrace3, ImmutableSet.of(), REVERSE_FLOW, reverseTrace2)));
  }

  @Test
  public void testSession() {
    Trace forwardTrace = new Trace(ACCEPTED, ImmutableList.of());
    Trace reverseTrace = new Trace(NEIGHBOR_UNREACHABLE, ImmutableList.of());
    FirewallSessionTraceInfo session =
        new FirewallSessionTraceInfo("session", null, null, ImmutableSet.of(), TRUE, null);
    TraceAndReverseFlow forwardTarf =
        new TraceAndReverseFlow(forwardTrace, REVERSE_FLOW, ImmutableList.of(session));
    TraceAndReverseFlow reverseTarf =
        new TraceAndReverseFlow(reverseTrace, null, ImmutableList.of());
    TracerouteEngine tracerouteEngine =
        forSessions(
            ImmutableMap.of(
                ImmutableSet.of(),
                ImmutableMap.of(FORWARD_FLOW, ImmutableList.of(forwardTarf)),
                ImmutableSet.of(session),
                ImmutableMap.of(REVERSE_FLOW, ImmutableList.of(reverseTarf))));
    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);
    assertThat(
        bidirectionalTraces,
        contains(
            new BidirectionalTrace(
                FORWARD_FLOW, forwardTrace, ImmutableSet.of(session), REVERSE_FLOW, reverseTrace)));
  }

  /** Make sure we don't mix traces and sessions. */
  @Test
  public void testSessions() {
    Trace sessionForwardTrace = new Trace(ACCEPTED, ImmutableList.of());
    Trace noSessionForwardTrace = new Trace(DELIVERED_TO_SUBNET, ImmutableList.of());
    FirewallSessionTraceInfo session =
        new FirewallSessionTraceInfo("session", null, null, ImmutableSet.of(), TRUE, null);
    TraceAndReverseFlow sessionForwardTarf =
        new TraceAndReverseFlow(sessionForwardTrace, REVERSE_FLOW, ImmutableList.of(session));
    TraceAndReverseFlow noSessionForwardTarf =
        new TraceAndReverseFlow(noSessionForwardTrace, REVERSE_FLOW, ImmutableList.of());

    Trace sessionReverseTrace = new Trace(DENIED_IN, ImmutableList.of());
    Trace noSessionReverseTrace = new Trace(DENIED_OUT, ImmutableList.of());
    TraceAndReverseFlow sessionReverseTarf =
        new TraceAndReverseFlow(sessionReverseTrace, null, ImmutableList.of());
    TraceAndReverseFlow noSessionReverseTarf =
        new TraceAndReverseFlow(noSessionReverseTrace, null, ImmutableList.of());

    TracerouteEngine tracerouteEngine =
        forSessions(
            ImmutableMap.of(
                ImmutableSet.of(),
                ImmutableMap.of(
                    FORWARD_FLOW, ImmutableList.of(sessionForwardTarf, noSessionForwardTarf),
                    REVERSE_FLOW, ImmutableList.of(noSessionReverseTarf)),
                ImmutableSet.of(session),
                ImmutableMap.of(REVERSE_FLOW, ImmutableList.of(sessionReverseTarf))));
    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);
    assertThat(
        bidirectionalTraces,
        containsInAnyOrder(
            new BidirectionalTrace(
                FORWARD_FLOW,
                sessionForwardTrace,
                ImmutableSet.of(session),
                REVERSE_FLOW,
                sessionReverseTrace),
            new BidirectionalTrace(
                FORWARD_FLOW,
                noSessionForwardTrace,
                ImmutableSet.of(),
                REVERSE_FLOW,
                noSessionReverseTrace)));
  }
}
