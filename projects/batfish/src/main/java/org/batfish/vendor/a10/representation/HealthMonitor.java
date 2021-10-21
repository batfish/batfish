package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Datamodel class representing the configuration of a {@code health monitor}, used to monitor the
 * health of load balancer service or server.
 */
public final class HealthMonitor implements Serializable {
  @Nonnull
  public String getName() {
    return _name;
  }

  public HealthMonitor(String name) {
    _name = name;
  }

  @Nonnull private final String _name;
}
