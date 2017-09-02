package org.batfish.common.plugin;

import java.util.Map;

public abstract class SyncTestrigsPlugin extends CoordinatorPlugin {

  @Override
  protected final void coordinatorPluginInitialize() {
    syncTestrigsPluginInitialize();
  }

  public abstract void syncNow(String container);

  public abstract void syncTestrigsPluginInitialize();

  public abstract void updateSettings(String container, Map<String, String> settings);
}
