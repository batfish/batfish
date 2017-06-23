package org.batfish.common.plugin;

public abstract class Plugin {

   protected PluginConsumer _pluginConsumer;

   public final void initialize(PluginConsumer pluginConsumer) {
      _pluginConsumer = pluginConsumer;
      pluginInitialize();
   }

   protected abstract void pluginInitialize();

}
