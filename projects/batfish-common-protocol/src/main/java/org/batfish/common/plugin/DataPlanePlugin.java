package org.batfish.common.plugin;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.collections.IbgpTopology;

public abstract class DataPlanePlugin extends BatfishPlugin implements IDataPlanePlugin {

  private static DataPlanePlugin DATA_PLANE_PLUGIN;

  private static Class<? extends DataPlanePlugin> DATA_PLANE_PLUGIN_CLASS;

  public static synchronized DataPlanePlugin getDataPlanePlugin() {
    return DATA_PLANE_PLUGIN;
  }

  public static synchronized Class<? extends DataPlanePlugin> getDataPlanePluginClass() {
    return DATA_PLANE_PLUGIN_CLASS;
  }

  public static synchronized void setDataPlanePlugin(DataPlanePlugin dataPlanePlugin) {
    DATA_PLANE_PLUGIN = dataPlanePlugin;
  }

  public static synchronized void setDataPlanePluginClass(
      Class<? extends DataPlanePlugin> dataPlanePlugin) {
    DATA_PLANE_PLUGIN_CLASS = dataPlanePlugin;
  }

  @Override
  protected final void batfishPluginInitialize() {
    _batfish.registerDataPlanePlugin(this, getName());
    dataPlanePluginInitialize();
  }

  public abstract Answer computeDataPlane(boolean differentialContext);

  protected void dataPlanePluginInitialize() {}

  public abstract Set<BgpAdvertisement> getAdvertisements();

  public abstract List<Flow> getHistoryFlows();

  public abstract List<FlowTrace> getHistoryFlowTraces();

  public abstract IbgpTopology getIbgpNeighbors();

  public abstract SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes();

  public abstract void processFlows(Set<Flow> flows);

  public abstract String getName();
}
