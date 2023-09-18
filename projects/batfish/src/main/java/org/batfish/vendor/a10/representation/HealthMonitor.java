package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Datamodel class representing the configuration of a {@code health monitor}, used to monitor the
 * health of load balancer service or server.
 */
public final class HealthMonitor implements Serializable {
  public @Nonnull String getName() {
    return _name;
  }

  public HealthMonitor(String name) {
    _name = name;
  }

  private final @Nonnull String _name;
}
