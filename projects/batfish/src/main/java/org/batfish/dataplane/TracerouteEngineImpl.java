package org.batfish.dataplane;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.dataplane.traceroute.TracerouteEngineImplContext;

/** The default implementation of a traceroute engine */
public final class TracerouteEngineImpl implements TracerouteEngine {
  private final DataPlane _dataPlane;

  public TracerouteEngineImpl(DataPlane dataPlane) {
    _dataPlane = dataPlane;
  }

  @Override
  public SortedMap<Flow, Set<FlowTrace>> processFlows(Set<Flow> flows, boolean ignoreFilters) {
    return new org.batfish.dataplane.TracerouteEngineImplContext(
            _dataPlane, flows, _dataPlane.getFibs(), ignoreFilters)
        .processFlows();
  }

  @Override
  public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
    return new TracerouteEngineImplContext(
            _dataPlane, sessions, flows, _dataPlane.getFibs(), ignoreFilters)
        .buildTracesAndReturnFlows();
  }
}
