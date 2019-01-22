package org.batfish.dataplane;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.flow.Trace;
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

  /**
   * Builds the {@link Trace}s for a {@link Set} of {@link Flow}s
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace}s are to be found
   * @param ignoreFilters if true, will ignore ACLs
   * @return {@link SortedMap} of {@link Flow}s to {@link List} of {@link Trace}s
   */
  @Override
  public SortedMap<Flow, List<Trace>> computeTraces(Set<Flow> flows, boolean ignoreFilters) {
    return CommonUtil.toImmutableSortedMap(
        computeTracesAndReverseFlows(flows, ignoreFilters),
        Entry::getKey,
        entry ->
            entry.getValue().stream()
                .map(TraceAndReverseFlow::getTrace)
                .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public SortedMap<Flow, List<TraceAndReverseFlow>> computeTracesAndReverseFlows(
      Set<Flow> flows, boolean ignoreFilters) {
    return new TracerouteEngineImplContext(_dataPlane, flows, _dataPlane.getFibs(), ignoreFilters)
        .buildTracesAndReturnFlows();
  }
}
