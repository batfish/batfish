package org.batfish.common.plugin;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReturnFlow;

/**
 * Indicates ability to process a set of {@link Flow} objects and return a set of {@link FlowTrace},
 * performing a traceroute.
 */
public interface TracerouteEngine {
  SortedMap<Flow, Set<FlowTrace>> processFlows(Set<Flow> flows, boolean ignoreFilters);

  /**
   * Builds the {@link Trace}s for a {@link Set} of {@link Flow}s
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace}s are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow}s to {@link List} of {@link Trace}s
   */
  SortedMap<Flow, List<Trace>> buildFlows(Set<Flow> flows, boolean ignoreFilters);

  /**
   * Builds bidirectional {@link Trace Traces} for a {@link Set} of {@link Flow Flows}.
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace}s are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow}s to {@link List} of {@link Trace}s
   */
  SortedMap<Flow, List<TraceAndReturnFlow>> buildTracesAndReturnFlows(
      Set<Flow> flows, boolean ignoreFilters);
}
