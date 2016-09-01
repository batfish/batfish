package org.batfish.plugin;

import org.batfish.datamodel.answers.Answer;
import org.batfish.main.Batfish;

public abstract class TaskPlugin {

   private static Class<? extends TaskPlugin> DATA_PLANE_PLUGIN;

   public static synchronized Class<? extends TaskPlugin> getDataPlanePlugin() {
      return DATA_PLANE_PLUGIN;
   }

   public static synchronized void setDataPlanePlugin(
         Class<? extends TaskPlugin> dataPlanePlugin) {
      DATA_PLANE_PLUGIN = dataPlanePlugin;
   }

   protected final Batfish _batfish;

   public TaskPlugin(Batfish batfish) {
      _batfish = batfish;
   }

   public abstract Answer run();

}
