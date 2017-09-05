package org.batfish.common.plugin;

import java.util.Map;

public abstract class SyncTestrigsPlugin extends CoordinatorPlugin {

  @Override
  protected final void coordinatorPluginInitialize() {
    syncTestrigsPluginInitialize();
  }

  public abstract int syncNow(String container, boolean force);

  public abstract void syncTestrigsPluginInitialize();

  public abstract boolean updateSettings(String container, Map<String, String> settings);
}
