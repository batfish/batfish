package org.batfish.plugin;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Batfish;
import org.batfish.main.Settings;

public abstract class Plugin {

   protected final Batfish _batfish;

   protected final BatfishLogger _logger;

   protected final Settings _settings;

   public Plugin(Batfish batfish) {
      _batfish = batfish;
      _settings = batfish.getSettings();
      _logger = batfish.getLogger();
   }

   public abstract void initialize();

}
