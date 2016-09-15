package org.batfish.plugin;

import org.batfish.common.BatfishLogger;
import org.batfish.common.plugin.Plugin;
import org.batfish.main.Batfish;
import org.batfish.main.Settings;

public abstract class BatfishPlugin extends Plugin {

   protected Batfish _batfish;

   protected BatfishLogger _logger;

   protected Settings _settings;

   protected abstract void batfishPluginInitialize();

   @Override
   protected final void pluginInitialize() {
      switch (_pluginConsumer.getType()) {
      case BATFISH:
         _batfish = (Batfish) _pluginConsumer;
         _settings = _batfish.getSettings();
         _logger = _batfish.getLogger();
         batfishPluginInitialize();
         break;
      case CLIENT:
         break;
      default:
         break;
      }
   }

}
