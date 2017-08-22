package org.batfish.common.plugin;

import java.util.Map;

public interface ITestrigSyncPlugin extends ICoordinatorPlugin {

  void syncNow(ICoordinator coordinator, String container);

  void updateSettings(ICoordinator coordinator, String container, Map<String, String> settings);
}
