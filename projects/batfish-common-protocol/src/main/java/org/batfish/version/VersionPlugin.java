package org.batfish.version;

import org.batfish.common.plugin.ICoordinator;
import org.batfish.common.plugin.IVersionPlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.plugin.PluginClientType;

public abstract class VersionPlugin extends Plugin implements IVersionPlugin {

  protected abstract String getName();

  protected abstract String getVersion();

  @Override
  protected final void pluginInitialize() {
    String name = getName();
    String version = getVersion();
    if (_pluginConsumer.getType() == PluginClientType.COORDINATOR) {
      ICoordinator coordinator = (ICoordinator) _pluginConsumer;
      coordinator.registerVersion(name, version);
    }
  }
}
