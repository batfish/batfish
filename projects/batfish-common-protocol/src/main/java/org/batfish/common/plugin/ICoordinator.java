package org.batfish.common.plugin;

import java.nio.file.Path;
import java.util.Set;

import org.batfish.common.BatfishLogger;

public interface ICoordinator {

  Path getdirContainer(String containerName);

  Path getdirTestrigs(String containerName);

  BatfishLogger getLogger();

  Set<String> getContainerNames();

  void initTestrig(Path testrigDir, Path srcDir, boolean autoProcess);

  void registerTestrigSyncer(String name, SyncTestrigsPlugin plugin);
}
