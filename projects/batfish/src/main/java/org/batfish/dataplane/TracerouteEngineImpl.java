package org.batfish.dataplane;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.ITracerouteEngine;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.flow.Trace;
import org.batfish.dataplane.traceroute.TracerouteEngineImplContext;

/** The default implementation of a traceroute engine */
public class TracerouteEngineImpl implements ITracerouteEngine {
  private static ITracerouteEngine _instance = new TracerouteEngineImpl();

  public static ITracerouteEngine getInstance() {
    return _instance;
  }

  private TracerouteEngineImpl() {}

  @Override
  public SortedMap<Flow, Set<FlowTrace>> processFlows(
      DataPlane dataPlane,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreAcls) {
    return new org.batfish.dataplane.TracerouteEngineImplContext(dataPlane, flows, fibs, ignoreAcls).processFlows();
  }

  @Override
  public SortedMap<Flow, List<Trace>> buildFlows(
      DataPlane dataPlane,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreAcls) {
    return new TracerouteEngineImplContext(dataPlane, flows, fibs, ignoreAcls).buildFlows();
  }
}
