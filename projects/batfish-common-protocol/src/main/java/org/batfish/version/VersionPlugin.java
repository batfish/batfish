package org.batfish.version;

import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.ICoordinator;
import org.batfish.common.plugin.IVersionPlugin;
import org.batfish.common.plugin.Plugin;

public abstract class VersionPlugin extends Plugin implements IVersionPlugin {

  protected abstract String getName();

  protected abstract String getVersion();

  @Override
  protected final void pluginInitialize() {
    String name = getName();
    String version = getVersion();
    switch (_pluginConsumer.getType()) {
      case BATFISH:
        {
          IBatfish batfish = (IBatfish) _pluginConsumer;
          batfish.registerVersion(name, version);
          break;
        }
      case COORDINATOR:
        {
          ICoordinator coordinator = (ICoordinator) _pluginConsumer;
          coordinator.registerVersion(name, version);
          break;
        }
      default:
        break;
    }
  }
}
