package org.batfish.common.plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.flow.Trace;

/**
 * Indicates ability to process a set of {@link Flow} objects and return a set of {@link FlowTrace},
 * performing a traceroute.
 */
public interface ITracerouteEngine {
  SortedMap<Flow, Set<FlowTrace>> processFlows(
      DataPlane dataPlane,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreFilters);

  /**
   * Builds the {@link Trace}s for a {@link Set} of {@link Flow}s
   *
   * @param dataPlane {@link DataPlane} for this network snapshot
   * @param flows {@link Set} of {@link Flow} for which {@link Trace}s are to be found
   * @param fibs {@link Fib} for the dataplane
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow}s to {@link List} of {@link Trace}s
   */
  SortedMap<Flow, List<Trace>> buildFlows(
      DataPlane dataPlane,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreFilters);
}
