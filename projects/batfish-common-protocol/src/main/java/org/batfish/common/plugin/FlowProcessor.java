package org.batfish.common.plugin;

import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;

/**
 * Indicates ability to process a set of {@link Flow} objects and return a set of {@link FlowTrace},
 * performing a traceroute.
 */
public interface FlowProcessor {
  SortedMap<Flow, Set<FlowTrace>> processFlows(DataPlane dp, Set<Flow> flows);
}
