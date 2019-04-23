package org.batfish.dataplane.ibdp;

import com.google.auto.service.AutoService;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.IncrementalBdpAnswerElement;

/** A batfish plugin that registers the Incremental Batfish Data Plane (ibdp) Engine. */
@AutoService(Plugin.class)
public class IncrementalDataPlanePlugin extends DataPlanePlugin {

  public static final String PLUGIN_NAME = "ibdp";

  private IncrementalBdpEngine _engine;

  public IncrementalDataPlanePlugin() {}

  @Override
  public ComputeDataPlaneResult computeDataPlane() {
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Topology topology =
        _batfish.getTopologyProvider().getLayer3Topology(_batfish.getNetworkSnapshot());
    return computeDataPlane(configurations, topology);
  }

  @Override
  @Deprecated
  public ComputeDataPlaneResult computeDataPlane(boolean differentialContext) {
    return computeDataPlane();
  }

  @Override
  public ComputeDataPlaneResult computeDataPlane(
      Map<String, Configuration> configurations, Topology topology) {
    Set<BgpAdvertisement> externalAdverts = _batfish.loadExternalBgpAnnouncements(configurations);
    ComputeDataPlaneResult answer =
        _engine.computeDataPlane(
            configurations,
            topology,
            _batfish.getLayer2Topology(),
            _batfish.getTopologyProvider().getOspfTopology(_batfish.getNetworkSnapshot()),
            externalAdverts);
    double averageRoutes =
        ((IncrementalDataPlane) answer._dataPlane)
            .getNodes().values().stream()
                .flatMap(n -> n.getVirtualRouters().values().stream())
                .mapToInt(vr -> vr._mainRib.getTypedRoutes().size())
                .average()
                .orElse(0.00d);
    _logger.infof(
        "Generated data-plane for testrig:%s; iterations:%s, avg entries per node:%.2f\n",
        _batfish.getTestrigName(),
        ((IncrementalBdpAnswerElement) answer._answerElement).getDependentRoutesIterations(),
        averageRoutes);
    return answer;
  }

  @Override
  @Deprecated
  public ComputeDataPlaneResult computeDataPlane(
      boolean differentialContext, Map<String, Configuration> configurations, Topology topology) {
    return computeDataPlane(configurations, topology);
  }

  @Override
  protected void dataPlanePluginInitialize() {
    _engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(_batfish.getSettingsConfiguration()),
            _batfish.getLogger(),
            _batfish::newBatch);
  }

  @Override
  public SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes(DataPlane dp) {
    return IncrementalBdpEngine.getRoutes((IncrementalDataPlane) dp);
  }

  @Override
  public String getName() {
    return PLUGIN_NAME;
  }
}
