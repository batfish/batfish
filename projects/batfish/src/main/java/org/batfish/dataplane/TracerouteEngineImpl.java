package org.batfish.dataplane;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.plugin.ITracerouteEngine;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;

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
    return new TracerouteEngineImplContext(dataPlane, flows, fibs, ignoreAcls).processFlows();
  }
}
