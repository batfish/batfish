package org.batfish.common.plugin;

import org.batfish.common.BatfishLogger;

public abstract class BatfishPlugin extends Plugin {

  protected IBatfish _batfish;

  protected BatfishLogger _logger;

  protected abstract void batfishPluginInitialize();

  @Override
  protected final void pluginInitialize() {
    switch (_pluginConsumer.getType()) {
      case BATFISH:
        _batfish = (IBatfish) _pluginConsumer;
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
