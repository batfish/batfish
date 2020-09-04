package org.batfish.dataplane.traceroute;

import java.util.List;

/** Used by {@link FlowTracer} to record complete and partial traces. */
public interface TraceRecorder {
  /**
   * Record a complete trace, i.e. one in which the final {@link HopInfo} has a nonnull {@link
   * HopInfo#getDisposition()} disposition}.
   */
  void recordTrace(List<HopInfo> hops);

  /**
   * Try to record a partial trace (i.e. one in which the final {@link HopInfo} does not have a
   * nonnull {@link HopInfo#getDisposition() disposition}). This requires all possible suffixes of
   * the partial trace to have been previously recorded.
   *
   * @return whether the trace was recorded successfully.
   */
  boolean tryRecordPartialTrace(List<HopInfo> hops);
}
