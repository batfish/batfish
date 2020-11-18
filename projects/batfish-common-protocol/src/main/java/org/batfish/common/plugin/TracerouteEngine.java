package org.batfish.common.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.traceroute.TraceDag;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/**
 * Indicates ability to process a set of {@link Flow} objects and return a set of {@link Trace},
 * performing a traceroute.
 */
public interface TracerouteEngine {

  /**
   * Builds the {@link Trace}s for a {@link Set} of {@link Flow}s
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace}s are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow}s to {@link List} of {@link Trace}s
   */
  default SortedMap<Flow, List<Trace>> computeTraces(Set<Flow> flows, boolean ignoreFilters) {
    return CollectionUtil.toImmutableSortedMap(
        computeTracesAndReverseFlows(flows, ignoreFilters),
        Entry::getKey,
        entry ->
            entry.getValue().stream()
                .map(TraceAndReverseFlow::getTrace)
                .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Computes {@link Trace Traces} with reverse-direction {@link Flow Flows} for a {@link Set} of
   * forward {@link Flow Flows}.
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace Traces} are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow Flows} to {@link List} of {@link Trace Traces}
   */
  default SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, boolean ignoreFilters) {
    return computeTracesAndReverseFlows(flows, ImmutableSet.of(), ignoreFilters);
  }

  /**
   * Computes {@link Trace Traces} with reverse-direction {@link Flow Flows} for a {@link Set} of
   * forward {@link Flow Flows}.
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace Traces} are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow Flows} to {@link List} of {@link Trace Traces}
   */
  SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters);

  /** Computes {@link TraceDag} for a {@link Set} of forward {@link Flow Flows}. */
  Map<Flow, TraceDag> computeTraceDags(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters);
}
