package org.batfish.dataplane;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.traceroute.TraceDag;
import org.batfish.datamodel.Configuration;
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
    return computeTraceDags(flows, sessions, ignoreFilters).entrySet().parallelStream()
        .map(
            entry ->
                new SimpleEntry<>(
                    entry.getKey(),
                    entry.getValue().getTraces().collect(ImmutableList.toImmutableList())))
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Ordering.natural(), Entry::getKey, Entry::getValue));
  }

  @Override
  public Map<Flow, TraceDag> computeTraceDags(
      Set<Flow> flows, Set<FirewallSessionTraceInfo> sessions, boolean ignoreFilters) {
    return new TracerouteEngineImplContext(
            _dataPlane,
            _topology,
            sessions,
            flows,
            _dataPlane.getFibs(),
            ignoreFilters,
            _configurations)
        .buildTraceDags();
  }
}
