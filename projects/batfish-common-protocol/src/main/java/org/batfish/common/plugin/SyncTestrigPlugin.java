package org.batfish.common.plugin;

import java.util.Map;

public abstract class SyncTestrigPlugin extends CoordinatorPlugin {

  @Override
  protected final void coordinatorPluginInitialize() {
    syncTestrigPluginInitialize();
  }

  public abstract void syncNow(String container);

  public abstract void syncTestrigPluginInitialize();

  public abstract void updateSettings(String container, Map<String, String> settings);
}
