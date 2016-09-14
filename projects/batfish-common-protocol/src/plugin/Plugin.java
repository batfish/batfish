package plugin;

public abstract class Plugin {

   protected PluginClient _pluginClient;
   
   public final void initialize(PluginClient client) {
      _pluginClient = client;
      pluginInitialize();
   }

   protected abstract void pluginInitialize();

}
