package org.batfish.dataplane.ibdp;

import com.google.auto.service.AutoService;
import java.util.Map;
import java.util.Set;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.IncrementalBdpAnswerElement;
import org.batfish.datamodel.isis.IsisTopology;

/** A batfish plugin that registers the Incremental Batfish Data Plane (ibdp) Engine. */
@AutoService(Plugin.class)
public final class IncrementalDataPlanePlugin extends DataPlanePlugin {

  public static final String PLUGIN_NAME = "ibdp";

  private IncrementalBdpEngine _engine;

  public IncrementalDataPlanePlugin() {}

  @Override
  public ComputeDataPlaneResult computeDataPlane(NetworkSnapshot snapshot) {
    Map<String, Configuration> configurations = _batfish.loadConfigurations(snapshot);
    Set<BgpAdvertisement> externalAdverts =
        _batfish.loadExternalBgpAnnouncements(snapshot, configurations);
    TopologyProvider topologyProvider = _batfish.getTopologyProvider();
    TopologyContext topologyContext =
        TopologyContext.builder()
            .setIpsecTopology(topologyProvider.getInitialIpsecTopology(snapshot))
            .setIsisTopology(
                IsisTopology.initIsisTopology(
                    configurations, topologyProvider.getInitialLayer3Topology(snapshot)))
            .setLayer1LogicalTopology(topologyProvider.getLayer1LogicalTopology(snapshot))
            .setLayer2Topology(topologyProvider.getInitialLayer2Topology(snapshot))
            .setLayer3Topology(topologyProvider.getInitialLayer3Topology(snapshot))
            .setOspfTopology(topologyProvider.getInitialOspfTopology(snapshot))
            .setRawLayer1PhysicalTopology(topologyProvider.getRawLayer1PhysicalTopology(snapshot))
            .setTunnelTopology(topologyProvider.getInitialTunnelTopology(snapshot))
            .build();

    ComputeDataPlaneResult answer =
        _engine.computeDataPlane(configurations, topologyContext, externalAdverts);
    _logger.infof(
        "Generated data-plane for snapshot:%s; iterations:%s",
        snapshot.getSnapshot(),
        ((IncrementalBdpAnswerElement) answer._answerElement).getDependentRoutesIterations());
    return answer;
  }

  @Override
  protected void dataPlanePluginInitialize() {
    _engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(_batfish.getSettingsConfiguration()));
  }

  @Override
  public String getName() {
    return PLUGIN_NAME;
  }
}
