package org.batfish.dataplane.traceroute;

import java.util.List;

/** Used by {@link FlowTracer} to record complete and partial traces. */
public interface TraceRecorder {
  void recordTrace(List<HopInfo> hops);

  boolean tryRecordPartialTrace(List<HopInfo> hops);
}
