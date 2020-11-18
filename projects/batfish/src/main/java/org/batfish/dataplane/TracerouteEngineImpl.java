package org.batfish.dataplane;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.dataplane.traceroute.TraceRecorder;
import org.batfish.dataplane.traceroute.TracerouteEngineImplContext;

/** The default implementation of a traceroute engine */
public final class TracerouteEngineImpl implements TracerouteEngine {
  private final DataPlane _dataPlane;
  private final Topology _topology;
  private final Map<String, Configuration> _configurations;

  public TracerouteEngineImpl(
      DataPlane dataPlane, Topology topology, Map<String, Configuration> configurations) {
    _dataPlane = dataPlane;
    _topology = topology;
    _configurations = configurations;
  }

  @Override
  public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
    return new TracerouteEngineImplContext(
            _dataPlane,
            _topology,
            sessions,
            flows,
            _dataPlane.getFibs(),
            ignoreFilters,
            _configurations)
        .buildTracesAndReturnFlows();
  }

  @Override
  public <R extends TraceRecorder> SortedMap<Flow, R> recordTraces(
      Set<Flow> flows,
      Set<FirewallSessionTraceInfo> sessions,
      boolean ignoreFilters,
      Function<Flow, R> recorderSupplier) {
    return new TracerouteEngineImplContext(
            _dataPlane,
            _topology,
            sessions,
            flows,
            _dataPlane.getFibs(),
            ignoreFilters,
            _configurations)
        .recordAllTraces(recorderSupplier);
  }
}
