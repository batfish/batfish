package org.batfish.common.plugin;

import java.util.List;
import java.util.Set;

import org.batfish.common.plugin.IDataPlanePlugin;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.IbgpTopology;
import org.batfish.datamodel.collections.RouteSet;

public abstract class DataPlanePlugin extends BatfishPlugin
      implements IDataPlanePlugin {

   private static DataPlanePlugin DATA_PLANE_PLUGIN;

   private static Class<? extends DataPlanePlugin> DATA_PLANE_PLUGIN_CLASS;

   public static synchronized DataPlanePlugin getDataPlanePlugin() {
      return DATA_PLANE_PLUGIN;
   }

   public static synchronized Class<? extends DataPlanePlugin> getDataPlanePluginClass() {
      return DATA_PLANE_PLUGIN_CLASS;
   }

   public static synchronized void setDataPlanePlugin(
         DataPlanePlugin dataPlanePlugin) {
      DATA_PLANE_PLUGIN = dataPlanePlugin;
   }

   public static synchronized void setDataPlanePluginClass(
         Class<? extends DataPlanePlugin> dataPlanePlugin) {
      DATA_PLANE_PLUGIN_CLASS = dataPlanePlugin;
   }

   @Override
   protected final void batfishPluginInitialize() {
      _batfish.setDataPlanePlugin(this);
      dataPlanePluginInitialize();
   }

   public abstract Answer computeDataPlane(boolean differentialContext);

   protected void dataPlanePluginInitialize() {
   }

   public abstract AdvertisementSet getAdvertisements();

   public abstract List<Flow> getHistoryFlows();

   public abstract List<FlowTrace> getHistoryFlowTraces();

   public abstract IbgpTopology getIbgpNeighbors();

   public abstract RouteSet getRoutes();

   public abstract void processFlows(Set<Flow> flows);

}
