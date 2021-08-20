package org.batfish.vendor.check_point_management;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;

/** Configuration encompassing settings for all CheckPoint management servers in a snapshot. */
public final class CheckpointManagementConfiguration implements Serializable {

  public CheckpointManagementConfiguration(Map<String, Server> servers) {
    _servers = servers;
  }

  public @Nonnull Map<String, Server> getServers() {
    return _servers;
  }

  private final @Nonnull Map<String, Server> _servers;
}
