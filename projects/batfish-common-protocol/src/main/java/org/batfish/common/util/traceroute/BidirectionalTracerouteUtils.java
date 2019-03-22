package org.batfish.common.util.traceroute;

import static org.batfish.common.util.CommonUtil.toImmutableMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.BidirectionalTrace;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

public class BidirectionalTracerouteUtils {

  public static List<BidirectionalTrace> computeBidirectionalTraces(
      Set<Flow> flows, TracerouteEngine tracerouteEngine, boolean ignoreFilters) {
    SortedMap<Flow, List<TraceAndReverseFlow>> forwardTraces =
        tracerouteEngine.computeTracesAndReverseFlows(flows, ignoreFilters);

    Map<Flow, Set<Optional<FlowAndSessions>>> forwardFlowToReverseFlows =
        toImmutableMap(
            forwardTraces,
            Entry::getKey,
            entry -> {
              Function<TraceAndReverseFlow, Optional<FlowAndSessions>> mapper =
                  tarf ->
                      tarf.getReverseFlow() == null
                          ? Optional.empty()
                          : Optional.of(
                              new FlowAndSessions(
                                  tarf.getReverseFlow(), tarf.getNewFirewallSessions()));
              return entry.getValue().stream().map(mapper).collect(ImmutableSet.toImmutableSet());
            });

    Set<FlowAndSessions> reverseFlowsAndSessions =
        forwardFlowToReverseFlows.values().stream()
            .flatMap(Set::stream)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableSet.toImmutableSet());

    Map<FlowAndSessions, List<Trace>> reverseTraces =
        computeReverseTraces(tracerouteEngine, reverseFlowsAndSessions, ignoreFilters);

    List<BidirectionalTrace> result = new ArrayList<>();
    forwardTraces.forEach(
        (forwardFlow, forwardTraceAndReverseFlows) ->
            forwardTraceAndReverseFlows.forEach(
                forwardTraceAndReverseFlow -> {
                  Flow reverseFlow = forwardTraceAndReverseFlow.getReverseFlow();
                  Trace forwardTrace = forwardTraceAndReverseFlow.getTrace();
                  Set<FirewallSessionTraceInfo> newSessions =
                      forwardTraceAndReverseFlow.getNewFirewallSessions();
                  if (reverseFlow == null) {
                    result.add(
                        new BidirectionalTrace(forwardFlow, forwardTrace, newSessions, null, null));
                  } else {
                    FlowAndSessions fas =
                        new FlowAndSessions(
                            forwardTraceAndReverseFlow.getReverseFlow(),
                            forwardTraceAndReverseFlow.getNewFirewallSessions());
                    reverseTraces.get(fas).stream()
                        .map(
                            reverseTrace ->
                                new BidirectionalTrace(
                                    forwardFlow,
                                    forwardTrace,
                                    newSessions,
                                    reverseFlow,
                                    reverseTrace))
                        .forEach(result::add);
                  }
                }));
    return result;
  }

  private static Map<FlowAndSessions, List<Trace>> computeReverseTraces(
      TracerouteEngine tracerouteEngine,
      Set<FlowAndSessions> reverseFlowsAndSessions,
      boolean ignoreFilters) {
    return toImmutableMap(
        reverseFlowsAndSessions,
        Function.identity(),
        fas ->
            tracerouteEngine
                .computeTracesAndReverseFlows(
                    ImmutableSet.of(fas._flow), fas._sessions, ignoreFilters)
                .get(fas._flow).stream()
                .map(TraceAndReverseFlow::getTrace)
                .collect(ImmutableList.toImmutableList()));
  }

  private static final class FlowAndSessions {
    final Flow _flow;
    final Set<FirewallSessionTraceInfo> _sessions;

    private FlowAndSessions(Flow flow, Set<FirewallSessionTraceInfo> sessions) {
      _flow = flow;
      _sessions = sessions;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof FlowAndSessions)) {
        return false;
      }
      FlowAndSessions that = (FlowAndSessions) o;
      return Objects.equals(_flow, that._flow) && Objects.equals(_sessions, that._sessions);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_flow, _sessions);
    }
  }
}
