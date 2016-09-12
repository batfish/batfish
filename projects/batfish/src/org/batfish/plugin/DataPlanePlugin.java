package org.batfish.plugin;

import java.util.List;
import java.util.Set;

import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.IbgpTopology;
import org.batfish.datamodel.collections.RouteSet;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public abstract class DataPlanePlugin extends Plugin {

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

   public DataPlanePlugin(Batfish batfish) {
      super(batfish);
   }

   public abstract Answer computeDataPlane(TestrigSettings testrigSettings,
         boolean differentialContext);

   public abstract AdvertisementSet getAdvertisements(
         TestrigSettings testrigSettings);

   public abstract DataPlane getDataPlane(TestrigSettings testrigSettings);

   public abstract List<Flow> getHistoryFlows(TestrigSettings testrigSettings);

   public abstract List<FlowTrace> getHistoryFlowTraces(
         TestrigSettings testrigSettings);

   public abstract IbgpTopology getIbgpNeighbors(
         TestrigSettings testrigSettings);

   public abstract RouteSet getRoutes(TestrigSettings testrigSettings);

   public abstract void processFlows(Set<Flow> flows,
         TestrigSettings testrigSettings);

}
