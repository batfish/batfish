package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/**
 * A {@link TraceRecorder} that only records complete traces, and passes them to a {@link Consumer}.
 */
public final class LegacyTraceRecorder implements TraceRecorder {

  private final Consumer<TraceAndReverseFlow> _consumer;

  LegacyTraceRecorder(Consumer<TraceAndReverseFlow> consumer) {
    _consumer = consumer;
  }

  @Override
  public void recordTrace(List<HopInfo> hopInfos) {
    HopInfo lastHop = hopInfos.get(hopInfos.size() - 1);
    FlowDisposition disposition = lastHop.getDisposition();
    checkArgument(disposition != null, "Last hop of a complete trace must have a disposition");
    List<Hop> hops =
        hopInfos.stream().map(HopInfo::getHop).collect(ImmutableList.toImmutableList());
    Set<FirewallSessionTraceInfo> newSessions =
        hopInfos.stream()
            .map(HopInfo::getFirewallSessionTraceInfo)
            .filter(Objects::nonNull)
            .collect(ImmutableSet.toImmutableSet());
    _consumer.accept(
        new TraceAndReverseFlow(
            new Trace(disposition, hops), lastHop.getReturnFlow(), newSessions));
  }

  @Override
  public boolean tryRecordPartialTrace(List<HopInfo> hops) {
    return false;
  }
}
