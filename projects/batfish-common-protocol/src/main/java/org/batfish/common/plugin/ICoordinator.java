package org.batfish.common.plugin;

import java.nio.file.Path;

import org.batfish.common.BatfishLogger;

public interface ICoordinator {

  Path getdirContainer(String containerName);

  Path getdirTestrigs(String containerName);

  BatfishLogger getLogger();

  void registerTestrigSyncer(String name, SyncTestrigsPlugin plugin);
}
