package org.batfish.question.traceroute;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.batfish.common.traceroute.TraceDag;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/** Mock {@link org.batfish.common.plugin.TracerouteEngine}. */
public final class MockTracerouteEngine implements TracerouteEngine {
  private final Map<Set<FirewallSessionTraceInfo>, Map<Flow, List<TraceAndReverseFlow>>>
      _resultsForSessions;

  private MockTracerouteEngine(
      Map<Set<FirewallSessionTraceInfo>, Map<Flow, List<TraceAndReverseFlow>>> resultsForSessions) {
    _resultsForSessions = resultsForSessions;
  }

  static MockTracerouteEngine forFlow(Flow flow, List<TraceAndReverseFlow> traces) {
    return new MockTracerouteEngine(
        ImmutableMap.of(ImmutableSet.of(), ImmutableMap.of(flow, traces)));
  }

  static MockTracerouteEngine forFlows(Map<Flow, List<TraceAndReverseFlow>> flows) {
    return new MockTracerouteEngine(ImmutableMap.of(ImmutableSet.of(), flows));
  }

  static MockTracerouteEngine forSessions(
      Map<Set<FirewallSessionTraceInfo>, Map<Flow, List<TraceAndReverseFlow>>> resultsForSessions) {
    return new MockTracerouteEngine(resultsForSessions);
  }

  @Override
  public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
    checkArgument(_resultsForSessions.containsKey(sessions), "unexpected sessions");
    Map<Flow, List<TraceAndReverseFlow>> results = _resultsForSessions.get(sessions);
    SetView<Flow> unexpectedFlows = Sets.difference(flows, results.keySet());
    checkArgument(unexpectedFlows.isEmpty(), "unexpected Flows");
    return results.entrySet().stream()
        .filter(entry -> flows.contains(entry.getKey()))
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Ordering.natural(), Entry::getKey, Entry::getValue));
  }

  @Override
  public Map<Flow, TraceDag> computeTraceDags(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
    throw new UnsupportedOperationException();
  }
}
