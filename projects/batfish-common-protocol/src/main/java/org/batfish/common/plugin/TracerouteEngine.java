package org.batfish.common.plugin;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/**
 * Indicates ability to process a set of {@link Flow} objects and return a set of {@link FlowTrace},
 * performing a traceroute.
 */
public interface TracerouteEngine {
  SortedMap<Flow, Set<FlowTrace>> processFlows(Set<Flow> flows, boolean ignoreFilters);

  /**
   * Computes the {@link Trace Traces} for a {@link Set} of {@link Flow Flows}
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace Traces} are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow Flows} to {@link List} of {@link Trace Traces}
   */
  SortedMap<Flow, List<Trace>> computeTraces(Set<Flow> flows, boolean ignoreFilters);

  /**
   * Computes {@link Trace Traces} with reverse-direction {@link Flow Flows} for a {@link Set} of
   * forward {@link Flow Flows}.
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace Traces} are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow Flows} to {@link List} of {@link Trace Traces}
   */
  SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, boolean ignoreFilters);
}
