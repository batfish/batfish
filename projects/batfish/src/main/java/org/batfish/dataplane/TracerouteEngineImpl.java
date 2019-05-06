package org.batfish.dataplane;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.dataplane.traceroute.TracerouteEngineImplContext;

/** The default implementation of a traceroute engine */
public final class TracerouteEngineImpl implements TracerouteEngine {
  private final DataPlane _dataPlane;
  private final Topology _topology;

  public TracerouteEngineImpl(DataPlane dataPlane, Topology topology) {
    _dataPlane = dataPlane;
    _topology = topology;
  }

  @Override
  public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
    return new TracerouteEngineImplContext(
            _dataPlane, _topology, sessions, flows, _dataPlane.getFibs(), ignoreFilters)
        .buildTracesAndReturnFlows();
  }
}
