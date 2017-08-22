package org.batfish.common.plugin;

import java.nio.file.Path;

public interface ICoordinator {
  Path getdirContainer(String containerName);
}
