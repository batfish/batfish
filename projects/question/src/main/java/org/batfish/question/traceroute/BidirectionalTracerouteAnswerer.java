package org.batfish.question.traceroute;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.flow.BidirectionalTracePruner.prune;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
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
  static final String COL_FORWARD_TRACES = "Forward_Traces";
  static final String COL_NEW_SESSIONS = "New_Sessions";
  static final String COL_REVERSE_FLOW = "Reverse_Flow";
  static final String COL_REVERSE_TRACES = "Reverse_Traces";

  public BidirectionalTracerouteAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    BidirectionalTracerouteQuestion q = (BidirectionalTracerouteQuestion) _question;
    TracerouteAnswererHelper helper =
        new TracerouteAnswererHelper(
            q.getHeaderConstraints(),
            q.getSourceLocationStr(),
            _batfish.specifierContext(snapshot));
    Set<Flow> flows = helper.getFlows();
    TracerouteEngine tracerouteEngine = _batfish.getTracerouteEngine(snapshot);
    return bidirectionalTracerouteAnswerElement(
        _question, flows, tracerouteEngine, q.getIgnoreFilters(), q.getMaxTraces());
  }

  public static AnswerElement bidirectionalTracerouteAnswerElement(
      Question question,
      Set<Flow> flows,
      TracerouteEngine tracerouteEngine,
      boolean ignoreFilters,
      @Nullable Integer maxTraces) {
    List<BidirectionalTrace> bidirectionalTraces =
        computeBidirectionalTraces(flows, tracerouteEngine, ignoreFilters);
    List<BidirectionalTrace> prunedTraces =
        maxTraces == null ? bidirectionalTraces : prune(bidirectionalTraces, maxTraces);
    ImmutableMultiset<Row> rows =
        groupTraces(prunedTraces).entrySet().stream()
            .map(entry -> toRow(entry.getKey(), entry.getValue()))
            .collect(ImmutableMultiset.toImmutableMultiset());
    TableAnswerElement table = new TableAnswerElement(metadata());
    table.postProcessAnswer(question, rows);
    return table;
  }

  private static final class FlowAndSessions {
    final Flow _flow;
    final Set<FirewallSessionTraceInfo> _sessions;

    private FlowAndSessions(Flow flow, Set<FirewallSessionTraceInfo> sessions) {
      _flow = flow;
      _sessions = ImmutableSet.copyOf(sessions);
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

    Set<FlowAndSessions> reverseFlowsAndSessions =
        forwardTraces.values().stream()
            .flatMap(List::stream)
            .filter(tarf -> tarf.getReverseFlow() != null)
            .map(tarf -> new FlowAndSessions(tarf.getReverseFlow(), tarf.getNewFirewallSessions()))
            .collect(ImmutableSet.toImmutableSet());

    Map<FlowAndSessions, List<Trace>> reverseTraces =
        computeReverseTraces(tracerouteEngine, reverseFlowsAndSessions, ignoreFilters);

    List<BidirectionalTrace> result = new ArrayList<>();
    forwardTraces.forEach(
        (forwardFlow, forwardTraceAndReverseFlows) ->
            forwardTraceAndReverseFlows.forEach(
                forwardTraceAndReverseFlow -> {
                  Trace forwardTrace = forwardTraceAndReverseFlow.getTrace();
                  Flow reverseFlow = forwardTraceAndReverseFlow.getReverseFlow();
                  Set<FirewallSessionTraceInfo> newSessions =
                      forwardTraceAndReverseFlow.getNewFirewallSessions();
                  if (reverseFlow == null) {
                    result.add(
                        new BidirectionalTrace(forwardFlow, forwardTrace, newSessions, null, null));
                  } else {
                    FlowAndSessions fas = new FlowAndSessions(reverseFlow, newSessions);
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
                .get(fas._flow)
                .stream()
                .map(TraceAndReverseFlow::getTrace)
                .collect(ImmutableList.toImmutableList()));
  }

  /** Create metadata for the new traceroute v2 answer */
  public static TableMetadata metadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_FORWARD_FLOW, Schema.FLOW, "The forward flow.", true, false),
            new ColumnMetadata(
                COL_FORWARD_TRACES, Schema.list(Schema.TRACE), "The forward traces.", false, true),
            new ColumnMetadata(
                COL_NEW_SESSIONS,
                Schema.list(Schema.STRING),
                "Sessions initialized by the forward trace.",
                true,
                false),
            new ColumnMetadata(COL_REVERSE_FLOW, Schema.FLOW, "The reverse flow.", true, false),
            new ColumnMetadata(
                COL_REVERSE_TRACES, Schema.list(Schema.TRACE), "The reverse traces.", false, true));
    return new TableMetadata(
        columnMetadata, String.format("Bidirectional paths for flow ${%s}", COL_FORWARD_FLOW));
  }

  @VisibleForTesting
  static Map<BidirectionalTrace.Key, List<BidirectionalTrace>> groupTraces(
      List<BidirectionalTrace> traces) {
    return traces.stream()
        .collect(Collectors.groupingBy(BidirectionalTrace::getKey, Collectors.toList()));
  }

  @VisibleForTesting
  static Row toRow(BidirectionalTrace.Key key, List<BidirectionalTrace> traces) {
    // Invariant: each trace has getKey() equal to key.
    assert traces.stream().allMatch(trace -> trace.getKey().equals(key));

    List<Trace> forwardTraces =
        traces.stream()
            .map(BidirectionalTrace::getForwardTrace)
            .distinct()
            .collect(ImmutableList.toImmutableList());
    List<Trace> reverseTraces =
        traces.stream()
            .map(BidirectionalTrace::getReverseTrace)
            .filter(Objects::nonNull)
            .distinct()
            .collect(ImmutableList.toImmutableList());
    List<String> sessionHops =
        key.getNewSessions().stream()
            .map(FirewallSessionTraceInfo::getHostname)
            .collect(ImmutableList.toImmutableList());
    return Row.of(
        COL_FORWARD_FLOW,
        key.getForwardFlow(),
        COL_FORWARD_TRACES,
        forwardTraces,
        COL_NEW_SESSIONS,
        sessionHops,
        COL_REVERSE_FLOW,
        key.getReverseFlow(),
        COL_REVERSE_TRACES,
        reverseTraces);
  }
}
