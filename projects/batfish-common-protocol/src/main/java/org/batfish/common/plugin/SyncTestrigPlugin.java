package org.batfish.common.plugin;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public abstract class SyncTestrigPlugin extends CoordinatorPlugin {

  @Override
  protected final void coordinatorPluginInitialize() {
    syncTestrigPluginInitialize();
  }

  public abstract boolean syncNow(String container);

  public abstract void syncTestrigPluginInitialize();

  public abstract boolean updateSettings(String container, Map<String, String> settings);
}
