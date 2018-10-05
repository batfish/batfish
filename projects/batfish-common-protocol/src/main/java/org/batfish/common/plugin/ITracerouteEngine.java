package org.batfish.common.plugin;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.flow2.Trace;

/**
 * Indicates ability to process a set of {@link Flow} objects and return a set of {@link FlowTrace},
 * performing a traceroute.
 */
public interface ITracerouteEngine {
  SortedMap<Flow, Set<FlowTrace>> processFlows(
      DataPlane dataPlane, Set<Flow> flows, Map<String, Map<String, Fib>> fibs, boolean ignoreAcls);

  SortedMap<Flow, Set<Trace>> processFlowsNew(
      DataPlane dataPlane, Set<Flow> flows, Map<String, Map<String, Fib>> fibs, boolean ignoreAcls);
}
