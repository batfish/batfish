package org.batfish.question.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.computeBidirectionalTraces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.BidirectionalTrace;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.junit.Test;

/** Tests for {@link BidirectionalTracerouteAnswerer}. */
public final class BidirectionalTracerouteAnswererTest {
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

  class MockTracerouteEngine implements TracerouteEngine {
    private final Map<Flow, List<TraceAndReverseFlow>> _result;

    MockTracerouteEngine(Map<Flow, List<TraceAndReverseFlow>> result) {
      _result = result;
    }

    @Override
    public SortedMap<Flow, Set<FlowTrace>> processFlows(Set<Flow> flows, boolean ignoreFilters) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
        Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
      SetView<Flow> unexpectedFlows = Sets.difference(flows, _result.keySet());
      checkArgument(unexpectedFlows.isEmpty(), "unexpected Flows");
      return _result.entrySet().stream()
          .filter(entry -> flows.contains(entry.getKey()))
          .collect(
              ImmutableSortedMap.toImmutableSortedMap(
                  Ordering.natural(), Entry::getKey, Entry::getValue));
    }
  }

  @Test
  public void testNoReverseFlow() {
    Trace forwardTrace = new Trace(NO_ROUTE, ImmutableList.of());
    TraceAndReverseFlow traceAndReverseFlow = new TraceAndReverseFlow(forwardTrace, null);

    TracerouteEngine tracerouteEngine =
        new MockTracerouteEngine(
            ImmutableMap.of(FORWARD_FLOW, ImmutableList.of(traceAndReverseFlow)));

    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);

    assertThat(
        bidirectionalTraces,
        equalTo(ImmutableList.of(new BidirectionalTrace(FORWARD_FLOW, forwardTrace, null, null))));
  }

  @Test
  public void testReverseFlow() {
    Trace forwardTrace = new Trace(ACCEPTED, ImmutableList.of());
    TraceAndReverseFlow forwardTarf = new TraceAndReverseFlow(forwardTrace, REVERSE_FLOW);
    Trace reverseTrace = new Trace(DENIED_IN, ImmutableList.of());
    TraceAndReverseFlow reverseTarf = new TraceAndReverseFlow(reverseTrace, null);

    TracerouteEngine tracerouteEngine =
        new MockTracerouteEngine(
            ImmutableMap.of(
                FORWARD_FLOW,
                ImmutableList.of(forwardTarf),
                REVERSE_FLOW,
                ImmutableList.of(reverseTarf)));

    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);

    assertThat(
        bidirectionalTraces,
        equalTo(
            ImmutableList.of(
                new BidirectionalTrace(FORWARD_FLOW, forwardTrace, REVERSE_FLOW, reverseTrace))));
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
        new MockTracerouteEngine(
            ImmutableMap.of(
                FORWARD_FLOW,
                ImmutableList.of(forwardTarf1, forwardTarf2, forwardTarf3),
                REVERSE_FLOW,
                ImmutableList.of(reverseTarf1, reverseTarf2)));

    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(ImmutableSet.of(FORWARD_FLOW), tracerouteEngine, false);

    assertThat(
        bidirectionalTraces,
        equalTo(
            ImmutableList.of(
                new BidirectionalTrace(FORWARD_FLOW, forwardTrace1, REVERSE_FLOW, reverseTrace1),
                new BidirectionalTrace(FORWARD_FLOW, forwardTrace1, REVERSE_FLOW, reverseTrace2),
                new BidirectionalTrace(FORWARD_FLOW, forwardTrace2, null, null),
                new BidirectionalTrace(FORWARD_FLOW, forwardTrace3, REVERSE_FLOW, reverseTrace1),
                new BidirectionalTrace(FORWARD_FLOW, forwardTrace3, REVERSE_FLOW, reverseTrace2))));
  }
}
