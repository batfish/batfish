package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

public class LegacyTraceRecorder implements TraceRecorder {

  private final Consumer<TraceAndReverseFlow> _consumer;

  public LegacyTraceRecorder(Consumer<TraceAndReverseFlow> consumer) {
    _consumer = consumer;
  }

  @Override
  public void recordTrace(List<HopInfo> hops) {
    HopInfo lastHop = hops.get(hops.size() - 1);
    _consumer.accept(
        new TraceAndReverseFlow(
            new Trace(
                checkNotNull(
                    lastHop.getDisposition(),
                    "Last hop of a complete trace must have a disposition"),
                hops.stream().map(HopInfo::getHop).collect(ImmutableList.toImmutableList())),
            lastHop.getReturnFlow(),
            hops.stream()
                .map(HopInfo::getFirewallSessionTraceInfo)
                .filter(Objects::nonNull)
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Override
  public boolean tryRecordPartialTrace(List<HopInfo> hops) {
    return false;
  }
}
