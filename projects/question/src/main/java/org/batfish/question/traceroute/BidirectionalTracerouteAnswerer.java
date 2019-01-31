package org.batfish.question.traceroute;

import static org.batfish.common.util.CommonUtil.toImmutableMap;
import static org.batfish.datamodel.flow.BidirectionalTracePruner.prune;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
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
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.flow.BidirectionalTrace;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** {@link Answerer} for {@link BidirectionalTracerouteQuestion}. */
public class BidirectionalTracerouteAnswerer extends Answerer {
  static final String COL_FORWARD_FLOW = "Forward_Flow";
  static final String COL_FORWARD_TRACE = "Forward_Trace";
  static final String COL_REVERSE_FLOW = "Reverse_Flow";
  static final String COL_REVERSE_TRACE = "Reverse_Trace";

  private final TracerouteAnswererHelper _helper;
  private final boolean _ignoreFilters;
  private final int _maxTraces;

  public BidirectionalTracerouteAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
    BidirectionalTracerouteQuestion bidirectionalTracerouteQuestion =
        (BidirectionalTracerouteQuestion) question;
    _helper =
        new TracerouteAnswererHelper(
            bidirectionalTracerouteQuestion.getHeaderConstraints(),
            bidirectionalTracerouteQuestion.getSourceLocationStr(),
            _batfish.specifierContext());
    _ignoreFilters = bidirectionalTracerouteQuestion.getIgnoreFilters();
    _maxTraces = bidirectionalTracerouteQuestion.getMaxTraces();
  }

  @Override
  public AnswerElement answer() {
    String tag = _batfish.getFlowTag();
    Set<Flow> flows = _helper.getFlows(tag);
    TracerouteEngine tracerouteEngine = _batfish.getTracerouteEngine();
    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(flows, tracerouteEngine, _ignoreFilters);
    ImmutableMultiset<Row> rows =
        prune(bidirectionalTraces, _maxTraces).stream()
            .map(BidirectionalTracerouteAnswerer::toRow)
            .collect(ImmutableMultiset.toImmutableMultiset());
    TableAnswerElement table = new TableAnswerElement(metadata());
    table.postProcessAnswer(_question, rows);
    return table;
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

  @VisibleForTesting
  static List<BidirectionalTrace> computeBidirectionalTraces(
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
                  if (reverseFlow == null) {
                    result.add(new BidirectionalTrace(forwardFlow, forwardTrace, null, null));
                  } else {
                    FlowAndSessions fas =
                        new FlowAndSessions(
                            forwardTraceAndReverseFlow.getReverseFlow(),
                            forwardTraceAndReverseFlow.getNewFirewallSessions());
                    reverseTraces.get(fas).stream()
                        .map(
                            reverseTrace ->
                                new BidirectionalTrace(
                                    forwardFlow, forwardTrace, reverseFlow, reverseTrace))
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

  /** Create metadata for the new traceroute v2 answer */
  public static TableMetadata metadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_FORWARD_FLOW, Schema.FLOW, "The forward flow.", true, false),
            new ColumnMetadata(COL_FORWARD_TRACE, Schema.TRACE, "The forward trace.", false, true),
            new ColumnMetadata(COL_REVERSE_FLOW, Schema.FLOW, "The reverse flow.", false, true),
            new ColumnMetadata(COL_REVERSE_TRACE, Schema.TRACE, "The reverse trace.", false, true));
    return new TableMetadata(
        columnMetadata, String.format("Bidirectional paths for flow ${%s}", COL_FORWARD_FLOW));
  }

  private static Row toRow(BidirectionalTrace trace) {
    return Row.of(
        COL_FORWARD_FLOW,
        trace.getForwardFlow(),
        COL_FORWARD_TRACE,
        trace.getForwardTrace(),
        COL_REVERSE_FLOW,
        trace.getReverseFlow(),
        COL_REVERSE_TRACE,
        trace.getReverseTrace());
  }
}
