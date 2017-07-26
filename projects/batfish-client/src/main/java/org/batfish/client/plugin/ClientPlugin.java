package org.batfish.client.plugin;

import org.batfish.client.Client;
import org.batfish.client.config.Settings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.plugin.Plugin;

public abstract class ClientPlugin extends Plugin {

  protected Client _client;

  protected BatfishLogger _logger;

  protected Settings _settings;

  protected abstract void clientPluginInitialize();

  @Override
  protected final void pluginInitialize() {
    switch (_pluginConsumer.getType()) {
      case BATFISH:
        break;
      case CLIENT:
        _client = (Client) _pluginConsumer;
        _settings = _client.getSettings();
        _logger = _client.getLogger();
        clientPluginInitialize();
        break;
      default:
        break;
    }
  }
}
