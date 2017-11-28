package org.batfish.bdp;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.config.BdpSettings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.BdpAnswerElement;
import org.batfish.datamodel.collections.IbgpTopology;
import org.batfish.datamodel.collections.NodeInterfacePair;

@AutoService(Plugin.class)
public class BdpDataPlanePlugin extends DataPlanePlugin {

  private final Map<BdpDataPlane, Map<Flow, Set<FlowTrace>>> _flowTraces;

  private BdpEngine _engine;

  public BdpDataPlanePlugin() {
    _flowTraces = new HashMap<>();
  }

  @Override
  public Answer computeDataPlane(boolean differentialContext) {
    Answer answer = new Answer();
    BdpAnswerElement ae = new BdpAnswerElement();
    answer.addAnswerElement(ae);
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Topology topology = _batfish.computeTopology(configurations);
    Set<BgpAdvertisement> externalAdverts = _batfish.loadExternalBgpAnnouncements(configurations);
    Set<NodeInterfacePair> flowSinks =
        _batfish.computeFlowSinks(configurations, differentialContext, topology);
    BdpDataPlane dp =
        _engine.computeDataPlane(
            differentialContext, configurations, topology, externalAdverts, flowSinks, ae);
    _logger.resetTimer();
    _batfish.newBatch("Writing data plane to disk", 0);
    _batfish.writeDataPlane(dp, ae);
    _logger.printElapsedTime();
    return answer;
  }

  @Override
  protected void dataPlanePluginInitialize() {
    _engine =
        new BdpEngine(
            (BdpSettings) _batfish.getDataPlanePluginSettings(),
            _batfish.getLogger(),
            _batfish::newBatch);
  }

  @Override
  public Set<BgpAdvertisement> getAdvertisements() {
    Set<BgpAdvertisement> adverts = new LinkedHashSet<>();
    BdpDataPlane dp = loadDataPlane();
    for (Node node : dp._nodes.values()) {
      for (VirtualRouter vrf : node._virtualRouters.values()) {
        adverts.addAll(vrf._receivedBgpAdvertisements);
        adverts.addAll(vrf._sentBgpAdvertisements);
      }
    }
    return adverts;
  }

  @Override
  public List<Flow> getHistoryFlows() {
    BdpDataPlane dp = loadDataPlane();
    List<Flow> flowList = new ArrayList<>();
    _flowTraces
        .get(dp)
        .forEach(
            (flow, flowTraces) -> {
              for (int i = 0; i < flowTraces.size(); i++) {
                flowList.add(flow);
              }
            });
    return flowList;
  }

  @Override
  public List<FlowTrace> getHistoryFlowTraces() {
    BdpDataPlane dp = loadDataPlane();
    List<FlowTrace> flowTraceList = new ArrayList<>();
    _flowTraces
        .get(dp)
        .forEach(
            (flow, flowTraces) -> {
              for (FlowTrace flowTrace : flowTraces) {
                flowTraceList.add(flowTrace);
              }
            });
    return flowTraceList;
  }

  @Override
  public IbgpTopology getIbgpNeighbors() {
    throw new UnsupportedOperationException("no implementation for generated method");
    // TODO Auto-generated method stub
  }

  @Override
  public SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes() {
    BdpDataPlane dp = loadDataPlane();
    return _engine.getRoutes(dp);
  }

  private BdpDataPlane loadDataPlane() {
    return (BdpDataPlane) _batfish.loadDataPlane();
  }

  @Override
  public void processFlows(Set<Flow> flows) {
    BdpDataPlane dp = loadDataPlane();
    _flowTraces.put(dp, _engine.processFlows(dp, flows));
  }
}
