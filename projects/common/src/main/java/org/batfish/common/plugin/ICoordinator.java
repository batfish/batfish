package org.batfish.common.plugin;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import org.batfish.common.BatfishLogger;

public interface ICoordinator {

  BatfishLogger getLogger();

  Set<String> getNetworkNames();

  void initSnapshot(String networkName, String snapshotName, Path srcDir, Instant creationTime);
}
